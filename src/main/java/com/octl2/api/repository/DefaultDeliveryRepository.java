package com.octl2.api.repository;

import com.octl2.api.dto.response.LogisticDTO;
import com.octl2.api.dto.response.LogisticResponse;
import com.octl2.api.entity.DefaultDelivery;
import com.octl2.api.entity.Warehouse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DefaultDeliveryRepository extends JpaRepository<DefaultDelivery, Long> {


    // Lấy ra danh sách Logistic theo Province Id
    @Query(value = "SELECT " +
            "p.province_id AS provinceId, " +
            "ffm.partner_id AS ffmId, " +
            "lm.partner_id AS lmId, " +
            "wh.warehouse_id AS warehouseId " +
            "FROM lc_province p " +
            "LEFT JOIN cf_default_delivery cfd ON cfd.location_id = p.province_id " +
            "LEFT JOIN bp_partner ffm ON cfd.ffm_id = ffm.partner_id AND ffm.partner_type = 122 " +
            "LEFT JOIN bp_partner lm ON cfd.lastmile_id = lm.partner_id AND lm.partner_type = 121 " +
            "LEFT JOIN bp_warehouse wh ON cfd.warehouse_id = wh.warehouse_id " +
            "WHERE p.province_id = :provinceId",
            nativeQuery = true)
    List<com.octl2.api.dto.response.LogisticDTO> findLogisticsByProvince(@Param("provinceId") Integer provinceId);

    // Lấy ra danh sách Logistic theo Province Name
    @Query(value = "SELECT " +
            "p.province_id AS provinceId, " +
            "ffm.partner_id AS ffmId, " +
            "lm.partner_id AS lmId, " +
            "wh.warehouse_id AS warehouseId " +
            "FROM lc_province p " +
            "LEFT JOIN cf_default_delivery cfd ON cfd.location_id = p.province_id " +
            "LEFT JOIN bp_partner ffm ON cfd.ffm_id = ffm.partner_id AND ffm.partner_type = 122 " +
            "LEFT JOIN bp_partner lm ON cfd.lastmile_id = lm.partner_id AND lm.partner_type = 121 " +
            "LEFT JOIN bp_warehouse wh ON cfd.warehouse_id = wh.warehouse_id " +
            "WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :provinceName, '%'))",
            nativeQuery = true)
    List<com.octl2.api.dto.response.LogisticDTO> findLogisticsByProvinceName(@Param("provinceName") String provinceName);

    // Lấy ra danh sách Logistic và danh sách Province
    @Query(value = "SELECT " +
            "p.province_id AS provinceId, " +
            "ffm.partner_id AS ffmId, " +
            "lm.partner_id AS lmId, " +
            "wh.warehouse_id AS warehouseId " +
            "FROM lc_province p " +
            "LEFT JOIN cf_default_delivery cfd ON cfd.location_id = p.province_id " +
            "LEFT JOIN bp_partner ffm ON cfd.ffm_id = ffm.partner_id AND ffm.partner_type = 122 " +
            "LEFT JOIN bp_partner lm ON cfd.lastmile_id = lm.partner_id AND lm.partner_type = 121 " +
            "LEFT JOIN bp_warehouse wh ON cfd.warehouse_id = wh.warehouse_id " +
            "ORDER BY p.province_id ",
            nativeQuery = true)
    Page<com.octl2.api.dto.response.LogisticDTO> getLogisticsByProvinces(Pageable pageable);

    @Query(value = "SELECT " +
            "p.province_id AS provinceId, " +
            "ffm.partner_id AS ffmId, " +
            "lm.partner_id AS lmId, " +
            "wh.warehouse_id AS warehouseId " +
            "FROM lc_province p " +
            "LEFT JOIN cf_default_delivery cfd ON cfd.location_id = p.province_id " +
            "LEFT JOIN bp_partner ffm ON cfd.ffm_id = ffm.partner_id AND ffm.partner_type = 122 " +
            "LEFT JOIN bp_partner lm ON cfd.lastmile_id = lm.partner_id AND lm.partner_type = 121 " +
            "LEFT JOIN bp_warehouse wh ON cfd.warehouse_id = wh.warehouse_id ",
            nativeQuery = true)
    List<com.octl2.api.dto.response.LogisticDTO> findLogisticsByProvinces();

    // Lấy ra thông tin các Districts và  các Logistics của District trong 1 Province
    @Query(value = "SELECT DISTINCT " +
            "dtr.district_id AS districtId, " +
            "p.province_id AS provinceId, " +
            "ffm.partner_id AS ffmId, " +
            "lm.partner_id AS lmId, " +
            "wh.warehouse_id AS warehouseId, " +
            "FROM lc_province p " +
            "LEFT JOIN lc_district dtr ON p.province_id = dtr.province_id " +
            "LEFT JOIN cf_default_delivery cfd ON cfd.location_id = p.province_id " +
            "LEFT JOIN bp_partner ffm ON cfd.ffm_id = ffm.partner_id AND ffm.partner_type = 122 " +
            "LEFT JOIN bp_partner lm ON cfd.lastmile_id = lm.partner_id AND lm.partner_type = 121 " +
            "LEFT JOIN bp_warehouse wh ON cfd.warehouse_id = wh.warehouse_id " +
            "WHERE cfd.location_id = :provinceId " +
            "AND dtr.province_id IN (SELECT lp.province_id FROM lc_province lp WHERE lp.province_id = :provinceId) " +
            "ORDER BY dtr.district_id",
            nativeQuery = true)
    Page<com.octl2.api.dto.response.LogisticDTO> getLogisticsByDistricts(@Param("provinceId") Long provinceId, Pageable pageable);

}
