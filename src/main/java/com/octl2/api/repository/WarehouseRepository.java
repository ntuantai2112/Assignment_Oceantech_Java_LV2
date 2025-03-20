package com.octl2.api.repository;

import com.octl2.api.entity.Partner;
import com.octl2.api.entity.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {


}
