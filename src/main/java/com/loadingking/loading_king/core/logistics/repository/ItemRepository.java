package com.loadingking.loading_king.core.logistics.repository;

import java.util.Optional;
import java.util.List;
import com.loadingking.loading_king.core.logistics.domain.model.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;



@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    Optional<Item> findByBarcode(String barcode);
    List<Item> findByDeliveryJobIdOrderByIdDesc(Long deliveryJobId);
}
