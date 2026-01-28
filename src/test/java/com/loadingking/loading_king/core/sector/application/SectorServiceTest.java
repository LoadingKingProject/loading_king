package com.loadingking.loading_king.core.sector.application;

import com.loadingking.loading_king.core.sector.domain.Sector;
import com.loadingking.loading_king.core.sector.domain.SectorRepository;
import com.loadingking.loading_king.core.area.domain.repository.VillageRepository;
import com.loadingking.loading_king.core.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("SectorService 테스트")
class SectorServiceTest {

    @Mock
    private SectorRepository sectorRepository;

    @Mock
    private VillageRepository villageRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SectorService sectorService;

    private final GeometryFactory factory = new GeometryFactory();

    @Test
    @DisplayName("좌표를 포함하는 섹터가 목록에 있으면 해당 섹터를 반환한다")
    void shouldReturnSector_WhenLocationIsInside() {
        // Given
        Long id1 = 1L;
        Long id2 = 2L;
        Point targetLocation = factory.createPoint(new Coordinate(0.5, 0.5));

        // Mock 데이터 생성 (Reflection이나 Builder를 통해 ID를 세팅했다고 가정)
        Sector s1 = createSectorWithId(id1, 10, 10, 11, 11); // 좌표 미포함 섹터
        Sector s2 = createSectorWithId(id2, 0, 0, 1, 1);     // 좌표 포함 섹터

        // Repository 동작 정의: 어떤 ID 리스트를 넣든 우리가 만든 섹터 리스트 반환
        given(sectorRepository.findAllById(anyList())).willReturn(List.of(s1, s2));

        // When
        Optional<Sector> result = sectorService.findContainingSector(List.of(id1, id2), targetLocation);

        // Then
        assertTrue(result.isPresent());
        assertEquals(id2, result.get().getId()); // s2가 반환되어야 함
        verify(sectorRepository, times(1)).findAllById(anyList()); // 리포지토리가 1번 호출되었나 확인
    }

    // ID 주입이 포함된 헬퍼 메서드
    private Sector createSectorWithId(Long id, double x1, double y1, double x2, double y2) {
        Polygon p = factory.createPolygon(new Coordinate[]{
                new Coordinate(x1, y1), new Coordinate(x2, y1),
                new Coordinate(x2, y2), new Coordinate(x1, y2), new Coordinate(x1, y1)
        });
        Sector s = Sector.create("dd", p, factory.createPoint(new Coordinate((x1 + x2) / 2, (y1 + y2) / 2)));

        ReflectionTestUtils.setField(s, "id", id);
        return s;
    }
}
