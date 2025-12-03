package com.loadingking.loading_king.core.logistics.repository;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.loadingking.loading_king.core.logistics.domain.model.Item;
import com.loadingking.loading_king.core.logistics.domain.model.ItemStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest // JPA 관련 빈만 로드하여 가볍게 테스트 (H2 DB 자동 사용)
class ItemRepositoryTest {

    @Autowired
    private ItemRepository itemRepository;

    @Test
    @DisplayName("송장 번호로 물품을 저장하고 조회할 수 있다")
    void save_and_find_by_invoice() {
        // 1. Given (준비)
        Long jobId = 100L;
        String invoiceNo = "INV-TEST-001";
        String address = "서울시 강남구 역삼동";

        Item newItem = Item.create(jobId, invoiceNo, address);

        // 2. When (실행)
        Item savedItem = itemRepository.save(newItem);

        // 영속성 컨텍스트 초기화 (DB에서 진짜 조회하는지 확인 위해)
        // TestEntityManager를 주입받아 clear() 해도 됨

        // 3. Then (검증)
        Optional<Item> foundItem = itemRepository.findByInvoiceNo(invoiceNo);

        assertThat(foundItem).isPresent();
        assertThat(foundItem.get().getInvoiceNo()).isEqualTo(invoiceNo);
        assertThat(foundItem.get().getJobId()).isEqualTo(userId);
        assertThat(foundItem.get().getStatus()).isEqualTo(ItemStatus.SCANNED);
    }

    @Test
    @DisplayName("특정 기사의 모든 물품을 삭제할 수 있다 (초기화)")
    void delete_all_by_user_id() {
        // 1. Given
        Long userIdA = 1L;
        Long userIdB = 2L;

        // 기사 A의 물품 2개 저장
        itemRepository.save(Item.create(userIdA, "INV-A-1", "주소1"));
        itemRepository.save(Item.create(userIdA, "INV-A-2", "주소2"));

        // 기사 B의 물품 1개 저장
        itemRepository.save(Item.create(userIdB, "INV-B-1", "주소3"));

        // 2. When (기사 A의 데이터만 삭제)
        itemRepository.deleteAllByUserId(userIdA);

        // 3. Then
        List<Item> itemsA = itemRepository.findAllByUserId(userIdA);
        List<Item> itemsB = itemRepository.findAllByUserId(userIdB);

        assertThat(itemsA).isEmpty();   // A는 다 지워져야 함
        assertThat(itemsB).hasSize(1);  // B는 남아있어야 함
    }

    @Test
    @DisplayName("섹터가 배정되면 sectorId가 업데이트 되어야 한다")
    void assign_sector_update() {
        // 1. Given
        Item item = itemRepository.save(Item.create(1L, "INV-002", "부산시"));
        Long sectorId = 55L;

        // 2. When
        item.assignSector(sectorId);
        itemRepository.save(item); // 변경 감지 혹은 명시적 저장

        // 3. Then
        Item updatedItem = itemRepository.findById(item.getId()).get();
        assertThat(updatedItem.getSectorId()).isEqualTo(sectorId);
    }
}
