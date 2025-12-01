package com.loadingking.loading_king.core.area.domain.model;

import jakarta.persistence.*;

@Entity
public class City {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @Column(nullable = false)
    private String code;

    @Column
    private Integer version = 1;

    protected City() {}

    public City(String name, String code) {
        this.name = name;
        this.code = code;
    }


    public Long getId() {
        return id;
    }
}
