package com.example.procesamiento_masivo.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "errores_procesamiento")
public class ErrorProcesamiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lote_id", nullable = false)
    private LoteProcesamiento lote;

    @Column(name = "numero_linea", nullable = false)
    private Long numeroLinea;

    @Column(name = "registro_original", nullable = false, columnDefinition = "TEXT")
    private String registroOriginal;

    @Column(name = "motivo_error", nullable = false, length = 500)
    private String motivoError;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LoteProcesamiento getLote() {
        return lote;
    }

    public void setLote(LoteProcesamiento lote) {
        this.lote = lote;
    }

    public Long getNumeroLinea() {
        return numeroLinea;
    }

    public void setNumeroLinea(Long numeroLinea) {
        this.numeroLinea = numeroLinea;
    }

    public String getRegistroOriginal() {
        return registroOriginal;
    }

    public void setRegistroOriginal(String registroOriginal) {
        this.registroOriginal = registroOriginal;
    }

    public String getMotivoError() {
        return motivoError;
    }

    public void setMotivoError(String motivoError) {
        this.motivoError = motivoError;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
}