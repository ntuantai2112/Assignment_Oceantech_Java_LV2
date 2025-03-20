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

    @ManyToOne
    @JoinColumn(name = "ffm_id", nullable = false)
    private Partner fulfilment;

    @ManyToOne
    @JoinColumn(name = "lastmile_id", nullable = false)
    private Partner lastmile;

    @ManyToOne
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @Column(name = "modifyby")
    private Long modifyby;

    @Column(name = "modifydate")
    private LocalDateTime modifydate;


}
