package com.octl2.api.repository;

import com.octl2.api.dto.response.LogisticDTO;
import com.octl2.api.dto.response.LogisticResponse;
import com.octl2.api.entity.DefaultDelivery;
import com.octl2.api.entity.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DefaultDeliveryRepository extends JpaRepository<DefaultDelivery, Long> {

    @Query(value = "SELECT " +
            "p.province_id AS provinceId, " +
            "p.name AS provinceName, " +
            "ffm.partner_id AS ffmId, " +
            "ffm.name AS ffmName, " +
            "lm.partner_id AS lmId, " +
            "lm.name AS lmName, " +
            "wh.warehouse_id AS warehouseId, " +
            "wh.warehouse_name AS warehouseName " +
            "FROM lc_province p " +
            "LEFT JOIN cf_default_delivery cfd ON cfd.location_id = p.province_id " +
            "LEFT JOIN bp_partner ffm ON cfd.ffm_id = ffm.partner_id AND ffm.partner_type = 122 " +
            "LEFT JOIN bp_partner lm ON cfd.lastmile_id = lm.partner_id AND lm.partner_type = 121 " +
            "LEFT JOIN bp_warehouse wh ON cfd.warehouse_id = wh.warehouse_id " +
            "WHERE p.province_id = :provinceId",
            nativeQuery = true)
    List<com.octl2.api.dto.response.LogisticDTO> findLogisticsByProvince(@Param("provinceId") Integer provinceId);
}
