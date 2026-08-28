package com.example.procesamiento_masivo.batch;

import com.example.procesamiento_masivo.entity.ErrorProcesamiento;
import com.example.procesamiento_masivo.entity.Transaccion;

/**
 * Representa el resultado de procesar una línea del CSV.
 */
public record ResultadoProcesamiento(
        Transaccion transaccion,
        ErrorProcesamiento error,
        long numeroLinea,
        String registroOriginal
) {

    public static ResultadoProcesamiento exitoso(
            Transaccion transaccion,
            RegistroCsvTransaccion registro) {

        return new ResultadoProcesamiento(
                transaccion,
                null,
                registro.numeroLinea(),
                registro.registroOriginal()
        );
    }

    public static ResultadoProcesamiento fallido(
            ErrorProcesamiento error) {

        return new ResultadoProcesamiento(
                null,
                error,
                error.getNumeroLinea(),
                error.getRegistroOriginal()
        );
    }

    public boolean esExitoso() {
        return transaccion != null;
    }
}