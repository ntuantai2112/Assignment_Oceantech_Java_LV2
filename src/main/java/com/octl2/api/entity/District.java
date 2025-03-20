package com.octl2.api.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;


@Entity
@Table(name = "lc_district")
@Getter
@Setter
public class District {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "district_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "province_id", nullable = false)
    private Province province;

    @Column(name = "name")
    private String name;

    @Column(name = "shortname")
    private String shortname;

    @Column(name = "code")
    private String code;

    @Column(name = "dcsr")
    private String description;


}
