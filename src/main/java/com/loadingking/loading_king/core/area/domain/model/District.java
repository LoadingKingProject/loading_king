package com.loadingking.loading_king.core.area.domain.model;

import com.loadingking.loading_king.core.area.api.dto.CityRequest;
import jakarta.persistence.*;

import java.util.ArrayList;
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

    @Column
    private Integer version = 1;

    @OneToMany(mappedBy = "district")
    private List<Village> villages = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "city_id", nullable = false)
    private City city;

    protected District() {}

    public Long getId() {
        return id;
    }

    public District(City city, String name, String code) {
        this.city = city;
        this.name = name;
        this.code = code;
    }
}
