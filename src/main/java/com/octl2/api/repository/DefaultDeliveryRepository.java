package com.octl2.api.repository;

import com.octl2.api.dto.LogisticDTO;
import com.octl2.api.entity.DefaultDelivery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DefaultDeliveryRepository extends JpaRepository<DefaultDelivery, Long> {


    // Lấy ra danh sách Logistic và danh sách Province có phân trang
    @Query(value = "SELECT DISTINCT " +
                   "p.province_id AS provinceId, " +
                   "ffm.partner_id AS ffmId, " +
                   "lm.partner_id AS lmId, " +
                   "wh.warehouse_id AS warehouseId " +
                   "FROM lc_province p " +
                   "LEFT JOIN lc_district dtr ON p.province_id = dtr.province_id " +
                   "LEFT JOIN lc_subdistrict ls ON dtr.district_id = ls.district_id " +
                   "LEFT JOIN cf_default_delivery cfd " +
                   "ON cfd.location_id = " +
                   "CASE " +
                   "WHEN :levelMapping = 1 THEN p.province_id " +
                   "WHEN :levelMapping = 2 THEN dtr.district_id " +
                   "WHEN :levelMapping = 3 THEN ls.subdistrict_id " +
                   "END " +
                   "LEFT JOIN bp_partner ffm ON cfd.ffm_id = ffm.partner_id AND ffm.partner_type = 122 " +
                   "LEFT JOIN bp_partner lm ON cfd.lastmile_id = lm.partner_id AND lm.partner_type = 121 " +
                   "LEFT JOIN bp_warehouse wh ON cfd.warehouse_id = wh.warehouse_id " +
                   "ORDER BY p.province_id",
            nativeQuery = true)
    Page<LogisticDTO> findLogisticsByProvinces(@Param("levelMapping") int levelMapping, Pageable pageable);


    // Lấy ra thông tin các Districts và  các Logistics của District trong 1 Province
    @Query(value =
            "SELECT DISTINCT " +
            "p.province_id AS provinceId, " +
            "dtr.district_id AS districtId, " +
            "ffm.partner_id AS ffmId, " +
            "lm.partner_id AS lmId, " +
            "wh.warehouse_id AS warehouseId " +
            "FROM lc_province p " +
            "LEFT JOIN lc_district dtr ON p.province_id = dtr.province_id " +
            "LEFT JOIN lc_subdistrict ls ON dtr.district_id = ls.district_id " +
            "LEFT JOIN cf_default_delivery cfd ON cfd.location_id = " +
            "    CASE " +
            "        WHEN :levelMapping = 1 THEN p.province_id " +
            "        WHEN :levelMapping = 2 THEN dtr.district_id " +
            "        WHEN :levelMapping = 3 THEN ls.subdistrict_id " +
            "        ELSE NULL " +
            "    END " +
            "LEFT JOIN bp_partner ffm ON cfd.ffm_id = ffm.partner_id AND ffm.partner_type = 122 " +
            "LEFT JOIN bp_partner lm ON cfd.lastmile_id = lm.partner_id AND lm.partner_type = 121 " +
            "LEFT JOIN bp_warehouse wh ON cfd.warehouse_id = wh.warehouse_id " +
            "WHERE p.province_id = :provinceId " +
            "ORDER BY dtr.district_id",
            nativeQuery = true)
    Page<LogisticDTO> findLogisticsByDistricts(@Param("levelMapping") int levelMapping,
                                               @Param("provinceId") Long provinceId,
                                               Pageable pageable);

    // Lấy ra thông tin các SubDistricts và  các Logistics của District trong 1 Province
    @Query(value = "SELECT DISTINCT " +
                   "p.province_id AS provinceId, " +
                   "dtr.district_id AS districtId, " +
                   "subd.subdistrict_id AS subDistrictId," +
                   "ffm.partner_id AS ffmId, " +
                   "lm.partner_id AS lmId, " +
                   "wh.warehouse_id AS warehouseId " +
                   "FROM lc_province p " +
                   "LEFT JOIN lc_district dtr ON p.province_id = dtr.province_id " +
                   "LEFT JOIN lc_subdistrict subd ON dtr.district_id = subd.district_id  " +
                   "LEFT JOIN cf_default_delivery cfd ON cfd.location_id = " +
                   "    CASE " +
                   "        WHEN :levelMapping = 1 THEN p.province_id " +
                   "        WHEN :levelMapping = 2 THEN dtr.district_id " +
                   "        WHEN :levelMapping = 3 THEN subd.subdistrict_id " +
                   "        ELSE NULL " +
                   "    END " +
                   "LEFT JOIN bp_partner ffm ON cfd.ffm_id = ffm.partner_id AND ffm.partner_type = 122 " +
                   "LEFT JOIN bp_partner lm ON cfd.lastmile_id = lm.partner_id AND lm.partner_type = 121 " +
                   "LEFT JOIN bp_warehouse wh ON cfd.warehouse_id = wh.warehouse_id " +
                   "WHERE dtr.district_id  = :districtId " +
                   "ORDER BY subd.subdistrict_id",
            nativeQuery = true)
    Page<LogisticDTO> getLogisticsBySubDistricts(@Param("levelMapping") int levelMapping,
                                                 @Param("districtId") Long districtId,
                                                 Pageable pageable);

    // Lấy ra danh sách LogisticDTO để thực hiện chức năng Export Excel theo Level
    @Query(value = "SELECT " +
                   "p.province_id AS provinceId, " +
                   "ffm.partner_id AS ffmId, " +
                   "lm.partner_id AS lmId, " +
                   "wh.warehouse_id AS warehouseId " +
                   "FROM lc_province p " +
                   "LEFT JOIN lc_district d ON p.province_id = d.province_id " +
                   "LEFT JOIN lc_subdistrict s ON d.district_id = s.district_id " +
                   "LEFT JOIN cf_default_delivery cfd ON cfd.location_id = p.province_id " +
                   "LEFT JOIN bp_partner ffm ON cfd.ffm_id = ffm.partner_id AND ffm.partner_type = 122 " +
                   "LEFT JOIN bp_partner lm ON cfd.lastmile_id = lm.partner_id AND lm.partner_type = 121 " +
                   "LEFT JOIN bp_warehouse wh ON cfd.warehouse_id = wh.warehouse_id " +
                   "WHERE :levelMapping IN (1,2,3)",
            nativeQuery = true)
    List<LogisticDTO> findLogisticsByLevel(@Param("levelMapping") int levelMapping);


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
    List<LogisticDTO> findLogisticsByProvince();

    @Query(value = "SELECT " +
                   "p.province_id AS provinceId, " +
                   "d.district_id AS districtId, " +
                   "s.subdistrict_id AS subdistrictId, " +
                   "ffm.partner_id AS ffmId, " +
                   "lm.partner_id AS lmId, " +
                   "wh.warehouse_id AS warehouseId " +
                   "FROM lc_province p " +
                   "JOIN lc_district d ON p.province_id = d.province_id " +
                   "JOIN lc_subdistrict s ON d.district_id = s.district_id " +
                   "LEFT JOIN cf_default_delivery cfd ON cfd.location_id = d.district_id " +
                   "LEFT JOIN bp_partner ffm ON cfd.ffm_id = ffm.partner_id AND ffm.partner_type = 122 " +
                   "LEFT JOIN bp_partner lm ON cfd.lastmile_id = lm.partner_id AND lm.partner_type = 121 " +
                   "LEFT JOIN bp_warehouse wh ON cfd.warehouse_id = wh.warehouse_id " +
                   "ORDER BY p.province_id ",
            nativeQuery = true)
    List<LogisticDTO> findLogisticsByDistrict();

    @Query(value = "SELECT " +
                   "p.province_id AS provinceId, " +
                   "d.district_id AS districtId, " +
                   "s.subdistrict_id AS subdistrictId, " +
                   "ffm.partner_id AS ffmId, " +
                   "lm.partner_id AS lmId, " +
                   "wh.warehouse_id AS warehouseId " +
                   "FROM lc_province p " +
                   "JOIN lc_district d ON p.province_id = d.province_id " +
                   "JOIN lc_subdistrict s ON d.district_id = s.district_id " +
                   "LEFT JOIN cf_default_delivery cfd ON cfd.location_id = s.subdistrict_id " +
                   "LEFT JOIN bp_partner ffm ON cfd.ffm_id = ffm.partner_id AND ffm.partner_type = 122 " +
                   "LEFT JOIN bp_partner lm ON cfd.lastmile_id = lm.partner_id AND lm.partner_type = 121 " +
                   "LEFT JOIN bp_warehouse wh ON cfd.warehouse_id = wh.warehouse_id " +
                   "ORDER BY p.province_id ",
            nativeQuery = true)
    List<LogisticDTO> findLogisticsBySubDistrict();


    @Query(value = "SELECT " +
                   "p.province_id AS provinceId, " +
                   "p.name AS provinceName, " +
                   "CASE WHEN :levelMapping > 1 THEN d.district_id ELSE NULL END AS districtId, " +
                   "CASE WHEN :levelMapping = 3 THEN s.subdistrict_id ELSE NULL END AS subdistrictId, " +
                   "ffm.name AS ffmName, " +
                   "lm.name AS lmName, " +
                   "wh.name AS whName " +
                   "FROM lc_province p " +
                   "LEFT JOIN lc_district d ON p.province_id = d.province_id " +
                   "LEFT JOIN lc_subdistrict s ON d.district_id = s.district_id " +
                   "LEFT JOIN cf_default_delivery cfd ON cfd.location_id IN (p.province_id, d.district_id, s.subdistrict_id) " +
                   "LEFT JOIN bp_partner ffm ON cfd.ffm_id = ffm.partner_id AND ffm.partner_type = 122 " +
                   "LEFT JOIN bp_partner lm ON cfd.lastmile_id = lm.partner_id AND lm.partner_type = 121 " +
                   "LEFT JOIN bp_warehouse wh ON cfd.warehouse_id = wh.warehouse_id " +
                   "WHERE :levelMapping IN (1,2,3)",
            nativeQuery = true)
    List<LogisticDTO> findLogisticsByLevelParam(@Param("levelMapping") int levelMapping);


}
