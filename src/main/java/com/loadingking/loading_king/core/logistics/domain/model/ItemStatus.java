package com.loadingking.loading_king.core.logistics.domain.model;


public enum ItemStatus {
    SCANNED,   // 스캔만 된 상태 (아직 섹터 미확정일 수도 있음)
    ASSIGNED,  // 섹터 배정 완료
    LOADED     // 상차 완료 (적재 순서 확정 시)
}
