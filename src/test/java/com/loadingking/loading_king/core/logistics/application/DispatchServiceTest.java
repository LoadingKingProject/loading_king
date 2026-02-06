package com.loadingking.loading_king.core.logistics.application;

import com.loadingking.loading_king.core.sector.application.SectorService;
import com.loadingking.loading_king.core.sector.domain.Sector;
import com.loadingking.loading_king.core.user.domain.User;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("DispatchService 테스트")
class DispatchServiceTest {

    @Mock
    private SectorService sectorService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DispatchService dispatchService;

    private final GeometryFactory factory = new GeometryFactory();

    @Test
    @DisplayName("포함 섹터가 있으면 해당 섹터 ID를 반환한다")
    void dispatch_포함_섹터_있으면_해당_섹터_ID_반환() {
        // Given
        Long userId = 1L;
        Long sectorId = 10L;
        Point itemLocation = factory.createPoint(new Coordinate(0.5, 0.5));

        User user = createUserWithSectors(userId, List.of(sectorId));
        Sector sector = createSectorWithId(sectorId, 0, 0, 1, 1);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(sectorService.findContainingSector(anyList(), any(Point.class)))
                .willReturn(Optional.of(sector));

        // When
        Long result = dispatchService.dispatch(user, itemLocation);

        // Then
        assertEquals(sectorId, result);
        verify(sectorService, times(1)).findContainingSector(anyList(), any(Point.class));
        verify(sectorService, times(0)).findNearestSector(anyList(), any(Point.class)); // fallback 호출 안됨
    }

    @Test
    @DisplayName("포함 섹터가 없으면 가장 가까운 섹터 ID를 반환한다")
    void dispatch_포함_섹터_없으면_가장_가까운_섹터_ID_반환() {
        // Given
        Long userId = 1L;
        Long nearestSectorId = 20L;
        Point itemLocation = factory.createPoint(new Coordinate(5, 5));

        User user = createUserWithSectors(userId, List.of(10L, nearestSectorId));
        Sector nearestSector = createSectorWithId(nearestSectorId, 3, 3, 4, 4);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(sectorService.findContainingSector(anyList(), any(Point.class)))
                .willReturn(Optional.empty()); // 포함 섹터 없음
        given(sectorService.findNearestSector(anyList(), any(Point.class)))
                .willReturn(Optional.of(nearestSector)); // 가장 가까운 섹터 반환

        // When
        Long result = dispatchService.dispatch(user, itemLocation);

        // Then
        assertEquals(nearestSectorId, result);
        verify(sectorService, times(1)).findContainingSector(anyList(), any(Point.class));
        verify(sectorService, times(1)).findNearestSector(anyList(), any(Point.class)); // fallback 호출됨
    }

    @Test
    @DisplayName("섹터 목록이 비어있으면 null을 반환한다")
    void dispatch_섹터_목록_비어있으면_null_반환() {
        // Given
        Long userId = 1L;
        Point itemLocation = factory.createPoint(new Coordinate(5, 5));

        User user = createUserWithSectors(userId, List.of()); // 빈 섹터 목록

        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        // When
        Long result = dispatchService.dispatch(user, itemLocation);

        // Then
        assertNull(result);
        verify(sectorService, times(0)).findContainingSector(anyList(), any(Point.class));
        verify(sectorService, times(0)).findNearestSector(anyList(), any(Point.class));
    }

    // 헬퍼 메서드
    private User createUserWithSectors(Long userId, List<Long> sectorIds) {
        User user = User.builder()
                .email("test@example.com")
                .build();
        ReflectionTestUtils.setField(user, "id", userId);
        ReflectionTestUtils.setField(user, "sectors", sectorIds);
        return user;
    }

    private Sector createSectorWithId(Long id, double x1, double y1, double x2, double y2) {
        Polygon p = factory.createPolygon(new Coordinate[]{
                new Coordinate(x1, y1), new Coordinate(x2, y1),
                new Coordinate(x2, y2), new Coordinate(x1, y2), new Coordinate(x1, y1)
        });
        Sector s = Sector.create("TestSector", p, factory.createPoint(new Coordinate((x1 + x2) / 2, (y1 + y2) / 2)));
        ReflectionTestUtils.setField(s, "id", id);
        return s;
    }
}
