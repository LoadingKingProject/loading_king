//package com.loadingking.loading_king.core.sector.domain;
//
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.locationtech.jts.geom.Coordinate;
//import org.locationtech.jts.geom.GeometryFactory;
//import org.locationtech.jts.geom.Point;
//import org.locationtech.jts.geom.Polygon;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
//
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//@DataJpaTest
//@DisplayName("SectorRepository 테스트")
//class SectorRepositoryTest {
//
//    @Autowired
//    private SectorRepository sectorRepository;
//
//    private final GeometryFactory factory = new GeometryFactory();
//
//    @Test
//    @DisplayName("ID 목록이 주어졌을 때 해당하는 모든 섹터를 조회한다")
//    void shouldFindAllByIds() {
//        // Given
//        Polygon polygon = factory.createPolygon(new Coordinate[]{
//                new Coordinate(0, 0), new Coordinate(1, 0), new Coordinate(1, 1), new Coordinate(0, 1), new Coordinate(0, 0)
//        });
//
//        Sector sector1 = Sector.create("섹터1", polygon, factory.createPoint(new Coordinate(0.5, 0.5)));
//        Sector sector2 = Sector.create("섹터2", polygon, factory.createPoint(new Coordinate(0.7, 0.7)));
//
//        sectorRepository.save(sector1);
//        sectorRepository.save(sector2);
//
//        // When
//        List<Sector> foundSectors = sectorRepository.findAllById(List.of(sector1.getId(), sector2.getId()));
//
//        // Then
//        assertEquals(2, foundSectors.size());
//        assertTrue(foundSectors.stream().anyMatch(s -> s.getSectorName().equals("섹터1")));
//        assertTrue(foundSectors.stream().anyMatch(s -> s.getSectorName().equals("섹터2")));
//    }
//}