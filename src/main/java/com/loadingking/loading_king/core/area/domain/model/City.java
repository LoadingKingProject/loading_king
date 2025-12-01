package com.loadingking.loading_king.core.area.domain.model;

import jakarta.persistence.*;

import java.util.List;

@Entity
public class City {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @Column(nullable = false)
    private String code;

    @OneToMany
    @JoinColumn(name = "district_id")
    private List<District> districts;

}
