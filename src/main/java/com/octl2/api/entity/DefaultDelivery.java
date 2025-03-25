package com.octl2.api.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "cf_default_delivery")
@Getter
@Setter
public class DefaultDelivery {

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

    @Column(name = "modifydate")
    private LocalDateTime modifydate;


}
