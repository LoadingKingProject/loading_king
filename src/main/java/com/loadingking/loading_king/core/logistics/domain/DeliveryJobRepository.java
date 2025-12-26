package com.loadingking.loading_king.core.logistics.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DeliveryJobRepository extends JpaRepository<DeliveryJob, Long> {

    Optional<DeliveryJob> findActiveJobByDriverId(Long driverId);

}
