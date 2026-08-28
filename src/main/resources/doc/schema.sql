CREATE TABLE lotes_procesamiento (
    id BIGSERIAL PRIMARY KEY,
    nombre_archivo VARCHAR(255) NOT NULL,
    total_registros BIGINT NOT NULL DEFAULT 0,
    registros_exitosos BIGINT NOT NULL DEFAULT 0,
    registros_fallidos BIGINT NOT NULL DEFAULT 0,
    fecha_inicio TIMESTAMP NOT NULL,
    fecha_fin TIMESTAMP NULL,
    estado VARCHAR(30) NOT NULL
);

CREATE TABLE transacciones (
    id BIGSERIAL PRIMARY KEY,
    id_transaccion VARCHAR(100) NOT NULL,
    cuenta_origen VARCHAR(50) NOT NULL,
    cuenta_destino VARCHAR(50) NOT NULL,
    monto NUMERIC(19,2) NOT NULL,
    fecha_hora TIMESTAMP NOT NULL,
    tipo_operacion VARCHAR(30) NOT NULL,
    lote_id BIGINT NOT NULL,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_transacciones_id_transaccion
       UNIQUE (id_transaccion),

    CONSTRAINT fk_transacciones_lote
       FOREIGN KEY (lote_id)
           REFERENCES lotes_procesamiento(id)
);

CREATE TABLE errores_procesamiento (
    id BIGSERIAL PRIMARY KEY,
    lote_id BIGINT NOT NULL,
    numero_linea BIGINT NOT NULL,
    registro_original TEXT NOT NULL,
    motivo_error VARCHAR(500) NOT NULL,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_errores_lote
       FOREIGN KEY (lote_id)
           REFERENCES lotes_procesamiento(id)
);

CREATE INDEX idx_transacciones_lote_id
    ON transacciones(lote_id);

CREATE INDEX idx_transacciones_cuenta_origen
    ON transacciones(cuenta_origen);

CREATE INDEX idx_transacciones_cuenta_destino
    ON transacciones(cuenta_destino);

CREATE INDEX idx_transacciones_fecha_hora
    ON transacciones(fecha_hora);

CREATE INDEX idx_transacciones_tipo_operacion
    ON transacciones(tipo_operacion);

CREATE INDEX idx_errores_lote_id
    ON errores_procesamiento(lote_id);

CREATE INDEX idx_lotes_estado
    ON lotes_procesamiento(estado);