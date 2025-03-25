package com.octl2.api.repository;

import com.octl2.api.entity.Partner;
import com.octl2.api.entity.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {

    @Query("SELECT w FROM Warehouse w WHERE w.id = :id")
    Optional<Warehouse> findByWarehouseId(@Param("id") Long id);

    @Query("SELECT w FROM Warehouse w WHERE w.id = :id")
    Optional<Warehouse> findById(@Param("id") Long id);
}
