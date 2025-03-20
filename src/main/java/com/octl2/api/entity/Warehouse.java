package com.octl2.api.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;


@Entity
@Table(name = "bp_warehouse")
@Getter
@Setter
public class Warehouse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "warehouse_id", nullable = false)
    private Long id;

    @Column(name = "org_id")
    private Long orgId;

    @ManyToOne
    @JoinColumn(name = "ffm_id", nullable = false)
    private Partner fulfilment;

    @Column(name = "warehouse_name")
    private String warehouseName;

    @Column(name = "warehouse_shortname")
    private String warehouseShortname;

    @Column(name = "contact_name")
    private String contactName;

    @Column(name = "contact_phone")
    private String contactPhone;

    @Column(name = "address")
    private String address;

    @Column(name = "full_address")
    private String fullAddress;

    @ManyToOne
    @JoinColumn(name = "wards_id", nullable = false)
    private SubDistrict subdistrict;

    @ManyToOne
    @JoinColumn(name = "district_id")
    private District district;

    @ManyToOne
    @JoinColumn(name = "province_id")
    private Province province;

    @Column(name = "email")
    private String email;

    @Column(name = "modifyby")
    private Long modifyby;

    @Column(name = "modifydate")
    private LocalDateTime modifydate;

    @Column(name = "wh_code_inpartner")
    private String whCodeInpartner;


}
