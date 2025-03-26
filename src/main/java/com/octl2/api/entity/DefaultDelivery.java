package com.octl2.api.entity;

import jdk.jfr.Timestamp;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.repository.Temporal;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(name = "cf_default_delivery")
@Getter
@Setter

public class DefaultDelivery extends AbstractEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cf_default_do_id")
    private Long id;

    @Column(name = "location_id")
    private Integer location;

    @Column(name = "ffm_id", nullable = false)
    private Long fulfilmentId;

    @Column(name = "lastmile_id", nullable = false)
    private Long lastmileId;

    @Column(name = "warehouse_id", nullable = false)
    private Long warehouseId;

    @Column(name = "modifyby")
    private Long modifyby;






}
