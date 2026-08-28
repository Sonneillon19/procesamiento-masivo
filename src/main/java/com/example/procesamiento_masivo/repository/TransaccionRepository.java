package com.example.procesamiento_masivo.repository;

import com.example.procesamiento_masivo.entity.Transaccion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransaccionRepository extends JpaRepository<Transaccion, Long> {

    boolean existsByIdTransaccion(String idTransaccion);

    long countByLoteId(Long loteId);
}