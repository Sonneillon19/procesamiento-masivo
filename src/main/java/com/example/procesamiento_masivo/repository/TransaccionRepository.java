package com.example.procesamiento_masivo.repository;

import com.example.procesamiento_masivo.entity.Transaccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Set;

public interface TransaccionRepository
        extends JpaRepository<Transaccion, Long> {

    boolean existsByIdTransaccion(String idTransaccion);

    long countByLoteId(Long loteId);

    /**
     * Se consulta un conjunto completo por chunk para evitar ir uno por uno
     */
    @Query("""
            SELECT t.idTransaccion
            FROM Transaccion t
            WHERE t.idTransaccion IN :ids
            """)
    Set<String> buscarIdsExistentes(
            @Param("ids") Collection<String> ids
    );
}