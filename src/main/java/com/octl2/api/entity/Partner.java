package com.octl2.api.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "bp_partner")
@Getter
@Setter
public class Partner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "partner_id")
    private Long id;

    @Column(name = "partner_type")
    private Integer partnerType;

    @Column(name = "name")
    private String name;

    @Column(name = "shortname")
    private String shortname;

    @Column(name = "contact")
    private String contact;

    @Column(name = "phone")
    private String phone;

    @Column(name = "email")
    private String email;

    @Column(name = "address")
    private String address;

    @Column(name = "modifyby")
    private Long modifyby;

    @Column(name = "modifydate")
    private LocalDateTime modifydate;


}
