package com.octl2.api.repository;

import com.octl2.api.entity.DefaultDelivery;
import com.octl2.api.entity.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DefaultDeliveryRepository extends JpaRepository<DefaultDelivery, Long> {


}
