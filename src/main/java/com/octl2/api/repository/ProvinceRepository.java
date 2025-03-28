package com.octl2.api.repository;

import com.octl2.api.dto.ProvinceDTO;
import com.octl2.api.entity.Province;
import com.octl2.api.entity.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProvinceRepository extends JpaRepository<Province, Long> {

    @Query(value = "SELECT " +
                   "   p.province_id AS id " +
                   "   , p.name " +
                   "   , p.shortname " +
                   "   , p.code " +
                   "   , p.dcsr AS description " +
                   " FROM lc_province AS p " +
                   " WHERE " +
                   "   p.province_id = :id "
            , nativeQuery = true)
    ProvinceDTO getDtoById(@Param("id") long id);


    @Query(value = "SELECT " +
                   "   p.province_id AS id " +
                   "   , p.name " +
                   "   , p.shortname " +
                   "   , p.code " +
                   "   , p.dcsr AS description " +
                   " FROM lc_province AS p " +
                   " LEFT JOIN lc_district ld on p.province_id  = ld.province_id " +
                   " WHERE " +
                   "   ld.district_id = :districtId "
            , nativeQuery = true)
    Optional<ProvinceDTO> findByDistrictId(Long districtId);


}
