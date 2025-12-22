package com.loadingking.loading_king.core.sector.domain;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Sector Domain 테스트")
class SectorTest {

    private final GeometryFactory factory = new GeometryFactory();
    private Sector sector;

    private Point insidePoint;
    private Point outsidePoint;
    private Point onBoundaryPoint;

    @BeforeEach
    void setUp() {
        // 1. 공통으로 사용할 다각형(Boundary) 설정
        Polygon boundary = factory.createPolygon(new Coordinate[] {
                new Coordinate(126.97, 37.56),
                new Coordinate(127.00, 37.56),
                new Coordinate(127.00, 37.58),
                new Coordinate(126.97, 37.58),
                new Coordinate(126.97, 37.56)
        });

        // 2. 공통 Sector 객체 생성
        sector = Sector.create("Test Sector", boundary, factory.createPoint(new Coordinate(126.985, 37.57)));

        // 3. 테스트 케이스별 좌표 데이터 준비
        insidePoint = factory.createPoint(new Coordinate(126.98, 37.57));
        outsidePoint = factory.createPoint(new Coordinate(126.98, 37.60));
        onBoundaryPoint = factory.createPoint(new Coordinate(126.97, 37.57));
    }

    @Test
    @DisplayName("TC-SECTOR-1: 배송 물품의 좌표가 현재의 경계 내부에 포함이 되는 경우 True를 반환한다")
    void shouldReturnTrue_WhenItemPointInsideBoundary() {
        assertTrue(sector.contains(insidePoint), "배송 물품 좌표가 섹터 경계 내이면 True를 반환해야 합니다.");
    }

    @Test
    @DisplayName("TC-SECTOR-2: location이 NULL 일 경우 IllegalArgumentException을 던져야 한다")
    void shouldThrowIllegalArgumentException_WhenLocationIsNull() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> sector.contains(null));
    }

    @Test
    @DisplayName("TC-SECTOR-3: 배송 물품의 좌표가 현재의 경계 외부에 있는 경우 False를 반환한다")
    void shouldReturnFalse_WhenItemPointOutsideBoundary() {
        assertFalse(sector.contains(outsidePoint), "배송 물품 좌표가 섹터 경계 외부이면 False를 반환해야 합니다.");
    }

    @Test
    @DisplayName("TC-SECTOR-4: 주어진 좌표가 섹터 경계선 위에 있을 때 false를 반환해야 한다")
    void shouldReturnFalse_WhenPointIsOnBoundaryLine() {
        assertFalse(sector.contains(onBoundaryPoint), "섹터 경계선에 있는 좌표는 포함되지 않은 것으로 간주되어 False를 반환해야 합니다.");
    }

    @Test
    @DisplayName("TC-SECTOR-5: 복잡한 형태의 다각형에서도 contains가 올바르게 작동해야 한다")
    void shouldWorkCorrectly_WhenComplexPolygon() {
        // Given
        Polygon complexPolygon = factory.createPolygon(new Coordinate[] {
                new Coordinate(126.97, 37.56),
                new Coordinate(127.00, 37.56),
                new Coordinate(127.01, 37.57),
                new Coordinate(127.00, 37.58),
                new Coordinate(126.98, 37.58),
                new Coordinate(126.97, 37.57),
                new Coordinate(126.97, 37.56)
        });

        // When, Then
        assertTrue(sector.contains(insidePoint), "복잡한 다각형 내부의 좌표는 포함되어야 합니다.");
        assertFalse(sector.contains(outsidePoint), "복잡한 다각형 외부의 좌표는 포함되지 않아야 합니다.");
    }
}
