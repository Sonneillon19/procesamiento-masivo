package com.example.procesamiento_masivo.batch;

import com.example.procesamiento_masivo.entity.ErrorProcesamiento;
import com.example.procesamiento_masivo.entity.Transaccion;
import com.example.procesamiento_masivo.repository.ErrorProcesamientoRepository;
import com.example.procesamiento_masivo.repository.TransaccionRepository;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

import java.util.ArrayList;
import java.util.List;

public class EscritorResultadoProcesamiento
        implements ItemWriter<ResultadoProcesamiento> {

    private final TransaccionRepository transaccionRepository;
    private final ErrorProcesamientoRepository errorProcesamientoRepository;

    public EscritorResultadoProcesamiento(
            TransaccionRepository transaccionRepository,
            ErrorProcesamientoRepository errorProcesamientoRepository) {

        this.transaccionRepository = transaccionRepository;
        this.errorProcesamientoRepository = errorProcesamientoRepository;
    }

    @Override
    public void write(Chunk<? extends ResultadoProcesamiento> chunk) {

        List<Transaccion> transacciones = new ArrayList<>();
        List<ErrorProcesamiento> errores = new ArrayList<>();

        for (ResultadoProcesamiento resultado : chunk) {

            if (resultado.esExitoso()) {
                transacciones.add(resultado.transaccion());
            } else {
                errores.add(resultado.error());
            }
        }

        if (!transacciones.isEmpty()) {
            transaccionRepository.saveAll(transacciones);
        }

        if (!errores.isEmpty()) {
            errorProcesamientoRepository.saveAll(errores);
        }
    }
}