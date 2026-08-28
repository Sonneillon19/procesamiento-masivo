package com.example.procesamiento_masivo.batch;

import com.example.procesamiento_masivo.entity.ErrorProcesamiento;
import com.example.procesamiento_masivo.entity.Transaccion;
import com.example.procesamiento_masivo.repository.TransaccionRepository;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Persiste los resultados procesados
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
    private final TransaccionRepository transaccionRepository;

    public EscritorResultadoProcesamiento(
            JdbcTemplate jdbcTemplate,
            TransaccionRepository transaccionRepository) {

        this.jdbcTemplate = jdbcTemplate;
        this.transaccionRepository = transaccionRepository;
    }

    @Override
    public void write(Chunk<? extends ResultadoProcesamiento> chunk) {

        List<ResultadoProcesamiento> resultadosValidos = new ArrayList<>();
        List<ErrorProcesamiento> errores = new ArrayList<>();

        /*
         * Separamos los errores que ya fueron detectados por el Processor de los registros que pueden persistirse.
         */
        for (ResultadoProcesamiento resultado : chunk) {

            if (resultado.esExitoso()) {
                resultadosValidos.add(resultado);
            } else {
                errores.add(resultado.error());
            }
        }

        procesarTransaccionesValidas(
                resultadosValidos,
                errores
        );

        guardarErrores(errores);
    }

    private void procesarTransaccionesValidas(
            List<ResultadoProcesamiento> resultados,
            List<ErrorProcesamiento> errores) {

        if (resultados.isEmpty()) {
            return;
        }

        Set<String> idsDelChunk = new HashSet<>();

        for (ResultadoProcesamiento resultado : resultados) {
            idsDelChunk.add(
                    resultado.transaccion().getIdTransaccion()
            );
        }

        /*
         * Recupera los IDs que ya existen
         */
        Set<String> idsExistentes =
                transaccionRepository.buscarIdsExistentes(idsDelChunk);

        Set<String> idsProcesadosEnChunk = new HashSet<>();
        List<Transaccion> transaccionesNuevas = new ArrayList<>();

        for (ResultadoProcesamiento resultado : resultados) {

            Transaccion transaccion = resultado.transaccion();
            String idTransaccion = transaccion.getIdTransaccion();

            boolean existeEnBase =
                    idsExistentes.contains(idTransaccion);

            boolean repetidoEnChunk =
                    !idsProcesadosEnChunk.add(idTransaccion);

            if (existeEnBase || repetidoEnChunk) {

                errores.add(
                        crearErrorDuplicado(resultado)
                );

                continue;
            }

            transaccionesNuevas.add(transaccion);
        }

        guardarTransacciones(transaccionesNuevas);
    }

    private ErrorProcesamiento crearErrorDuplicado(
            ResultadoProcesamiento resultado) {

        ErrorProcesamiento error = new ErrorProcesamiento();

        error.setLote(
                resultado.transaccion().getLote()
        );

        error.setNumeroLinea(
                resultado.numeroLinea()
        );

        error.setRegistroOriginal(
                resultado.registroOriginal()
        );

        error.setMotivoError(
                "El id de transacción ya existe: "
                        + resultado.transaccion().getIdTransaccion()
        );

        return error;
    }

    private void guardarTransacciones(
            List<Transaccion> transacciones) {

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
                            Timestamp.valueOf(
                                    transaccion.getFechaHora()
                            )
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

    private void guardarErrores(
            List<ErrorProcesamiento> errores) {

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