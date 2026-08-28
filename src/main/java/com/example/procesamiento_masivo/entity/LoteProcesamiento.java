package com.example.procesamiento_masivo.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "lotes_procesamiento")
public class LoteProcesamiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre_archivo", nullable = false)
    private String nombreArchivo;

    @Column(name = "total_registros", nullable = false)
    private Long totalRegistros = 0L;

    @Column(name = "registros_exitosos", nullable = false)
    private Long registrosExitosos = 0L;

    @Column(name = "registros_fallidos", nullable = false)
    private Long registrosFallidos = 0L;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDateTime fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDateTime fechaFin;

    @Column(name = "estado", nullable = false, length = 30)
    private String estado;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombreArchivo() {
        return nombreArchivo;
    }

    public void setNombreArchivo(String nombreArchivo) {
        this.nombreArchivo = nombreArchivo;
    }

    public Long getTotalRegistros() {
        return totalRegistros;
    }

    public void setTotalRegistros(Long totalRegistros) {
        this.totalRegistros = totalRegistros;
    }

    public Long getRegistrosExitosos() {
        return registrosExitosos;
    }

    public void setRegistrosExitosos(Long registrosExitosos) {
        this.registrosExitosos = registrosExitosos;
    }

    public Long getRegistrosFallidos() {
        return registrosFallidos;
    }

    public void setRegistrosFallidos(Long registrosFallidos) {
        this.registrosFallidos = registrosFallidos;
    }

    public LocalDateTime getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDateTime fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDateTime getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDateTime fechaFin) {
        this.fechaFin = fechaFin;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}