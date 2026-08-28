package com.example.procesamiento_masivo.batch;

import com.example.procesamiento_masivo.entity.ErrorProcesamiento;
import com.example.procesamiento_masivo.entity.LoteProcesamiento;
import com.example.procesamiento_masivo.entity.TipoOperacion;
import com.example.procesamiento_masivo.entity.Transaccion;
import org.springframework.batch.item.ItemProcessor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

public class ProcesadorTransaccion
        implements ItemProcessor<RegistroCsvTransaccion, ResultadoProcesamiento> {

    private final LoteProcesamiento lote;

    public ProcesadorTransaccion(LoteProcesamiento lote) {
        this.lote = lote;
    }

    @Override
    public ResultadoProcesamiento process(RegistroCsvTransaccion registro) {

        try {

            validarCamposObligatorios(registro);

            BigDecimal monto = convertirMonto(registro.monto());

            if (monto.compareTo(BigDecimal.ZERO) <= 0) {
                return crearError(
                        registro,
                        "El monto debe ser mayor a cero"
                );
            }

            LocalDateTime fechaHora = convertirFecha(registro.fechaHora());

            TipoOperacion tipoOperacion =
                    convertirTipoOperacion(registro.tipoOperacion());

            Transaccion transaccion = new Transaccion();

            transaccion.setIdTransaccion(registro.idTransaccion());
            transaccion.setCuentaOrigen(registro.cuentaOrigen());
            transaccion.setCuentaDestino(registro.cuentaDestino());
            transaccion.setMonto(monto);
            transaccion.setFechaHora(fechaHora);
            transaccion.setTipoOperacion(tipoOperacion);
            transaccion.setLote(lote);

            return ResultadoProcesamiento.exitoso(transaccion);

        } catch (IllegalArgumentException ex) {

            return crearError(
                    registro,
                    ex.getMessage()
            );
        }
    }

    private void validarCamposObligatorios(RegistroCsvTransaccion registro) {

        if (estaVacio(registro.idTransaccion())) {
            throw new IllegalArgumentException(
                    "El id de transacción es obligatorio"
            );
        }

        if (estaVacio(registro.cuentaOrigen())) {
            throw new IllegalArgumentException(
                    "La cuenta origen es obligatoria"
            );
        }

        if (estaVacio(registro.cuentaDestino())) {
            throw new IllegalArgumentException(
                    "La cuenta destino es obligatoria"
            );
        }

        if (estaVacio(registro.monto())) {
            throw new IllegalArgumentException(
                    "El monto es obligatorio"
            );
        }

        if (estaVacio(registro.fechaHora())) {
            throw new IllegalArgumentException(
                    "La fecha y hora son obligatorias"
            );
        }

        if (estaVacio(registro.tipoOperacion())) {
            throw new IllegalArgumentException(
                    "El tipo de operación es obligatorio"
            );
        }
    }

    private BigDecimal convertirMonto(String monto) {

        try {
            return new BigDecimal(monto.trim());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(
                    "Monto inválido: " + monto
            );
        }
    }

    private LocalDateTime convertirFecha(String fechaHora) {

        try {
            return LocalDateTime.parse(fechaHora.trim());
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException(
                    "Fecha inválida: " + fechaHora
            );
        }
    }

    private TipoOperacion convertirTipoOperacion(String tipoOperacion) {

        try {
            return TipoOperacion.valueOf(
                    tipoOperacion.trim().toUpperCase()
            );
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(
                    "Tipo de operación inválido: " + tipoOperacion
            );
        }
    }

    private ResultadoProcesamiento crearError(
            RegistroCsvTransaccion registro,
            String mensaje) {

        ErrorProcesamiento error = new ErrorProcesamiento();

        error.setLote(lote);
        error.setNumeroLinea(registro.numeroLinea());
        error.setRegistroOriginal(registro.registroOriginal());
        error.setMotivoError(mensaje);

        return ResultadoProcesamiento.fallido(error);
    }

    private boolean estaVacio(String valor) {
        return valor == null || valor.isBlank();
    }
}