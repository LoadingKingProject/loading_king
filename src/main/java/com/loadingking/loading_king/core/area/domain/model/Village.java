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

    @Column(nullable = false)
    private String code;

    @ManyToOne
    @JoinColumn(name = "district_id")
    private District district;

    private MultiPolygon point;
}
