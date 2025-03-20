package com.octl2.api.repository;

import com.octl2.api.entity.District;
import com.octl2.api.entity.Partner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PartnerRepository extends JpaRepository<Partner, Long> {


}
