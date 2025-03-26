package com.octl2.api.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;


@Entity
@Table(name = "bp_warehouse")
@Getter
@Setter
public class Warehouse extends AbstractEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "warehouse_id", nullable = false)
    private Long id;

    @Column(name = "org_id")
    private Long orgId;


    @Column(name = "ffm_id", nullable = false)
    private Long fulfilmentId;

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

    @Column(name = "wards_id", nullable = false)
    private Long subdistrictId;

    @Column(name = "district_id")
    private Long districtId;

    @Column(name = "province_id")
    private Long provinceId;

    @Column(name = "email")
    private String email;

    @Column(name = "modifyby")
    private Long modifyby;

    @Column(name = "wh_code_inpartner")
    private String whCodeInpartner;


}
