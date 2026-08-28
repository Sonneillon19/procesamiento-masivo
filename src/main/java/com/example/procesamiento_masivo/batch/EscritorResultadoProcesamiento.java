package com.example.procesamiento_masivo.batch;

import com.example.procesamiento_masivo.entity.ErrorProcesamiento;
import com.example.procesamiento_masivo.entity.Transaccion;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Persistira los resultados procesados en PostgreSQL.
 */
public class EscritorResultadoProcesamiento
        implements ItemWriter<ResultadoProcesamiento> {

    private static final String INSERT_TRANSACCION = """
            INSERT INTO transacciones (
                id_transaccion,
                cuenta_origen,
                cuenta_destino,
                monto,
                fecha_hora,
                tipo_operacion,
                lote_id
            )
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

    private static final String INSERT_ERROR = """
            INSERT INTO errores_procesamiento (
                lote_id,
                numero_linea,
                registro_original,
                motivo_error
            )
            VALUES (?, ?, ?, ?)
            """;

    private final JdbcTemplate jdbcTemplate;

    public EscritorResultadoProcesamiento(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Registros agrupados por chunk.
     */
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

        guardarTransacciones(transacciones);
        guardarErrores(errores);
    }

    private void guardarTransacciones(List<Transaccion> transacciones) {

        if (transacciones.isEmpty()) {
            return;
        }

        jdbcTemplate.batchUpdate(
                INSERT_TRANSACCION,
                transacciones,
                transacciones.size(),
                (preparedStatement, transaccion) -> {

                    preparedStatement.setString(
                            1,
                            transaccion.getIdTransaccion()
                    );

                    preparedStatement.setString(
                            2,
                            transaccion.getCuentaOrigen()
                    );

                    preparedStatement.setString(
                            3,
                            transaccion.getCuentaDestino()
                    );

                    preparedStatement.setBigDecimal(
                            4,
                            transaccion.getMonto()
                    );

                    preparedStatement.setTimestamp(
                            5,
                            Timestamp.valueOf(transaccion.getFechaHora())
                    );

                    preparedStatement.setString(
                            6,
                            transaccion.getTipoOperacion().name()
                    );

                    preparedStatement.setLong(
                            7,
                            transaccion.getLote().getId()
                    );
                }
        );
    }

    private void guardarErrores(List<ErrorProcesamiento> errores) {

        if (errores.isEmpty()) {
            return;
        }

        jdbcTemplate.batchUpdate(
                INSERT_ERROR,
                errores,
                errores.size(),
                (preparedStatement, error) -> {

                    preparedStatement.setLong(
                            1,
                            error.getLote().getId()
                    );

                    preparedStatement.setLong(
                            2,
                            error.getNumeroLinea()
                    );

                    preparedStatement.setString(
                            3,
                            error.getRegistroOriginal()
                    );

                    preparedStatement.setString(
                            4,
                            error.getMotivoError()
                    );
                }
        );
    }
}