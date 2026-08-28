package com.example.procesamiento_masivo.batch;

public record RegistroCsvTransaccion(
        long numeroLinea,
        String idTransaccion,
        String cuentaOrigen,
        String cuentaDestino,
        String monto,
        String fechaHora,
        String tipoOperacion,
        String registroOriginal
) {
}