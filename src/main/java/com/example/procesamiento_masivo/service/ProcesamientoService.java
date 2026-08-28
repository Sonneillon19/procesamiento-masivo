package com.example.procesamiento_masivo.service;

import com.example.procesamiento_masivo.entity.LoteProcesamiento;
import com.example.procesamiento_masivo.repository.LoteProcesamientoRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Servicio encargado de orquestar el procesamiento de archivos CSV.
 */
@Service
public class ProcesamientoService {

    private final JobLauncher jobLauncher;
    private final Job procesarTransaccionesJob;
    private final LoteProcesamientoRepository loteRepository;

    public ProcesamientoService(
            JobLauncher jobLauncher,
            Job procesarTransaccionesJob,
            LoteProcesamientoRepository loteRepository) {

        this.jobLauncher = jobLauncher;
        this.procesarTransaccionesJob = procesarTransaccionesJob;
        this.loteRepository = loteRepository;
    }

    /**
     * Inicia el procesamiento del archivo CSV.
     */
    public LoteProcesamiento procesarArchivo(MultipartFile archivo) {

        validarArchivo(archivo);

        // Se crea el lote para disponer de un identificador
        LoteProcesamiento lote = crearLoteInicial(archivo);

        try {

            Path rutaArchivo = guardarArchivoTemporal(archivo);

            JobParameters parametros = new JobParametersBuilder()
                    .addString(
                            "rutaArchivo",
                            rutaArchivo.toAbsolutePath().toString()
                    )
                    .addLong("loteId", lote.getId())
                    .toJobParameters();

            jobLauncher.run(
                    procesarTransaccionesJob,
                    parametros
            );

            /*
             * El listener del Job actualiza los contadores y el estado.
             */
            return loteRepository.findById(lote.getId())
                    .orElseThrow();

        } catch (Exception ex) {

            /*
             * Un error de infraestructura o ejecución provoca que el lote quede fallido.
             */
            lote.setEstado("FALLIDO");
            lote.setFechaFin(LocalDateTime.now());

            loteRepository.save(lote);

            throw new IllegalStateException(
                    "Ocurrió un error al procesar el archivo",
                    ex
            );
        }
    }

    /**
     * Consulta un lote ya registrado.
     */
    public LoteProcesamiento obtenerLote(Long id) {

        return loteRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "No existe el lote con id: " + id
                        )
                );
    }

    /**
     * Genera el registro inicial antes de procesar
     */
    private LoteProcesamiento crearLoteInicial(MultipartFile archivo) {

        LoteProcesamiento lote = new LoteProcesamiento();

        lote.setNombreArchivo(
                archivo.getOriginalFilename()
        );

        lote.setEstado("PENDIENTE");

        lote.setFechaInicio(LocalDateTime.now());

        lote.setTotalRegistros(0L);
        lote.setRegistrosExitosos(0L);
        lote.setRegistrosFallidos(0L);

        return loteRepository.save(lote);
    }

    /**
     * validaciones basicas antes de iniciar
     */
    private void validarArchivo(MultipartFile archivo) {

        if (archivo == null || archivo.isEmpty()) {
            throw new IllegalArgumentException(
                    "El archivo CSV es obligatorio"
            );
        }

        String nombreArchivo = archivo.getOriginalFilename();

        if (nombreArchivo == null
                || !nombreArchivo.toLowerCase().endsWith(".csv")) {

            throw new IllegalArgumentException(
                    "El archivo debe tener extensión .csv"
            );
        }
    }

    /**
     * Guarda el MultipartFile en un temporal.
     */
    private Path guardarArchivoTemporal(MultipartFile archivo)
            throws IOException {

        Path directorio = Path.of(
                System.getProperty("java.io.tmpdir"),
                "procesamiento-masivo"
        );

        Files.createDirectories(directorio);
        Path archivoTemporal = directorio.resolve(
                UUID.randomUUID() + ".csv"
        );

        archivo.transferTo(archivoTemporal);

        return archivoTemporal;
    }
}