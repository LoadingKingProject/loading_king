package com.loadingking.loading_king.core.logistics.repository;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.loadingking.loading_king.core.logistics.domain.model.Item;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    // 1. 송장 번호로 물품 조회 (중복 스캔 방지 및 기존 정보 로딩)
    Optional<Item> findByInvoiceNo(String invoiceNo);

    // 2. 특정 기사의 모든 물품 조회 (적재 순서 계산 시 사용)
    List<Item> findAllByJobId(Long jobId);

    // 3. 특정 기사의 특정 섹터 물품 조회 (섹터별 리스트 필요 시)
    List<Item> findAllByUserIdAndSectorId(Long jobId, Long sectorId);

    // 4. 데이터 초기화용 (배송 완료 시 해당 기사의 물품 전체 삭제)
    // 주의: @Transactional이 서비스 계층에 있어야 동작함
    void deleteAllByUserId(Long jobId);

    // 5. (Optional) 이미 스캔된 송장인지 체크용
    boolean existsByInvoiceNo(String invoiceNo);
}
