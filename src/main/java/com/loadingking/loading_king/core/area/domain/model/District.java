package com.loadingking.loading_king.core.area.domain.model;

import jakarta.persistence.*;

import java.awt.geom.Area;
import java.util.List;

@Entity
public class District {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @Column(nullable = false)
    private String code;

    @OneToMany
    @JoinColumn(name = "villages_id")
    private List<Village> villages;

    @ManyToOne
    @JoinColumn(name = "city_id")
    private City city;
}
