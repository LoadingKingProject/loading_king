package com.loadingking.loading_king.core.area.domain.model;

import jakarta.persistence.*;
import org.locationtech.jts.geom.MultiPolygon;

@Entity
public class Village {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @Column
    private Integer version = 1;

    @Column(nullable = false)
    private String code;

    @ManyToOne
    @JoinColumn(name = "district_id", nullable = false)
    private District district;

    @Column(columnDefinition = "MULTIPOLYGON SRID 4326", nullable = false)
    private MultiPolygon field;

    protected Village() {}

    public Village(District district, String name, String code, MultiPolygon field) {
        this.district = district;
        this.name = name;
        this.code = code;
        this.field = field;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public District getDistrict() {
        return district;
    }

    public MultiPolygon getField() {
        return field;
    }
}


