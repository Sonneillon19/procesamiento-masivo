package com.example.procesamiento_masivo.batch;

import com.example.procesamiento_masivo.entity.ErrorProcesamiento;
import com.example.procesamiento_masivo.entity.Transaccion;

public record ResultadoProcesamiento(
        Transaccion transaccion,
        ErrorProcesamiento error
) {

    public static ResultadoProcesamiento exitoso(Transaccion transaccion) {
        return new ResultadoProcesamiento(transaccion, null);
    }

    public static ResultadoProcesamiento fallido(ErrorProcesamiento error) {
        return new ResultadoProcesamiento(null, error);
    }

    public boolean esExitoso() {
        return transaccion != null;
    }
}