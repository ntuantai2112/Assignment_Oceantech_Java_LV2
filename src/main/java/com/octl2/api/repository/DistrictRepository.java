package com.octl2.api.repository;

import com.octl2.api.dto.ProvinceDTO;
import com.octl2.api.entity.District;
import com.octl2.api.entity.Province;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DistrictRepository extends JpaRepository<District, Long> {


}
