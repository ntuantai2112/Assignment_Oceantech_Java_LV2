package com.octl2.api.repository;

import com.octl2.api.entity.SubDistrict;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubDistrictRepository extends JpaRepository<SubDistrict, Long> {


}
