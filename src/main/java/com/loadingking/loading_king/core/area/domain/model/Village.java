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
    private MultiPolygon point;

    protected Village() {}

    public Village(District district, String name, String code, MultiPolygon point) {
        this.district = district;
        this.name = name;
        this.code = code;
        this.point = point;
    }

    public Long getId() {
        return id;
    }

    public MultiPolygon getPoint() {
        return point;
    }
}


