package com.loadingking.loading_king.core.sector.domain;

import jakarta.persistence.*;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

/**
 * 섹터 도메인 클래스
 */
@Entity
@Table(name = "sectors")
public class Sector {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sector_id")
    private Long id;          // 식별자

    @Column(name = "sector_name", nullable = false)
    private String sectorName; // 섹터 이름 (예: 잠실본동)

    @Column(name = "boundary", nullable = false)
    private Polygon boundary; // 행정동 경계

    @Column(name = "center", nullable = false)
    private Point centerCoordinate; // 중심 좌표

    protected Sector() {}

    /**
     * 섹터 생성
     * @param sectorName 섹터 이름
     * @param boundary 경계 다각형
     * @param centerCoordinate 중심 좌표
     * @return
     */
    public static Sector create(String sectorName, Polygon boundary, Point centerCoordinate) {
        return new Builder()
                .sectorName(sectorName)
                .boundary(boundary)
                .centerCoordinate(centerCoordinate)
                .build();
    }

    /**
     * 주어진 위치가 섹터의 경계 내에 포함되는지 여부를 확인
     * @param location 확인할 위치
     * @return 경계 내에 포함되면 true, 그렇지 않으면 false
     * @throws IllegalArgumentException location이 null인 경우 발생
     */
    public boolean contains(Point location) {
        if(location == null) {
            throw new IllegalArgumentException("location cannot be null");
        }
        
        return this.boundary.contains(location);
    }

    public Long getId() {
        return id;
    }

    public String getSectorName() {
        return sectorName;
    }

    public Polygon getBoundary() {
        return boundary;
    }

    public Point getCenterCoordinate() {
        return centerCoordinate;
    }

    private Sector(Builder builder) {
        this.sectorName = builder.sectorName;
        this.boundary = builder.boundary;
        this.centerCoordinate = builder.centerCoordinate;
    }

    public static class Builder {
        private String sectorName;
        private Polygon boundary;
        private Point centerCoordinate;

        public Builder sectorName(String sectorName) {
            this.sectorName = sectorName;
            return this;
        }

        public Builder boundary(Polygon boundary) {
            this.boundary = boundary;
            return this;
        }

        public Builder centerCoordinate(Point centerCoordinate) {
            this.centerCoordinate = centerCoordinate;
            return this;
        }

        public Sector build() {
            return new Sector(this);
        }
    }
}
