package com.octl2.api.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "lc_subdistrict")
@Getter
@Setter
public class SubDistrict {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "subdistrict_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "district_id", nullable = false)
    private District district;

    @Column(name = "name")
    private String name;

    @Column(name = "shortname")
    private String shortname;

    @Column(name = "code")
    private String code;

    @Column(name = "dcsr")
    private String description;


}
