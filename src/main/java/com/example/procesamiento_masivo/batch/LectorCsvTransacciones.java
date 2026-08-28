package com.example.procesamiento_masivo.batch;

import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemStreamReader;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class LectorCsvTransacciones implements ItemStreamReader<RegistroCsvTransaccion> {

    private final String rutaArchivo;

    private BufferedReader reader;
    private long numeroLinea;

    public LectorCsvTransacciones(String rutaArchivo) {
        this.rutaArchivo = rutaArchivo;
    }

    @Override
    public RegistroCsvTransaccion read() throws Exception {

        String linea = reader.readLine();

        if (linea == null) {
            return null;
        }

        numeroLinea++;

        if (numeroLinea == 1) {
            return read();
        }

        return convertirLinea(linea, numeroLinea);
    }

    private RegistroCsvTransaccion convertirLinea(String linea, long numeroLinea) {

        String[] campos = linea.split(",", -1);

        String idTransaccion = obtenerCampo(campos, 0);
        String cuentaOrigen = obtenerCampo(campos, 1);
        String cuentaDestino = obtenerCampo(campos, 2);
        String monto = obtenerCampo(campos, 3);
        String fechaHora = obtenerCampo(campos, 4);
        String tipoOperacion = obtenerCampo(campos, 5);

        return new RegistroCsvTransaccion(
                numeroLinea,
                idTransaccion,
                cuentaOrigen,
                cuentaDestino,
                monto,
                fechaHora,
                tipoOperacion,
                linea
        );
    }

    private String obtenerCampo(String[] campos, int posicion) {

        if (posicion >= campos.length) {
            return "";
        }

        return campos[posicion].trim();
    }

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {

        try {
            Path path = Paths.get(rutaArchivo);

            reader = Files.newBufferedReader(path);

            numeroLinea = executionContext.getLong("numeroLinea", 0L);

            if (numeroLinea > 0) {
                for (long i = 0; i < numeroLinea; i++) {
                    reader.readLine();
                }
            }

        } catch (IOException ex) {
            throw new ItemStreamException(
                    "No fue posible abrir el archivo CSV: " + rutaArchivo,
                    ex
            );
        }
    }

    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {
        executionContext.putLong("numeroLinea", numeroLinea);
    }

    @Override
    public void close() throws ItemStreamException {

        if (reader == null) {
            return;
        }

        try {
            reader.close();
        } catch (IOException ex) {
            throw new ItemStreamException(
                    "No fue posible cerrar el archivo CSV",
                    ex
            );
        }
    }
}