package com.example.tracking.repository;

import com.example.tracking.entity.TrackingInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrackingRepository extends JpaRepository<TrackingInfo, Long> {

    // Spring genera automáticamente la query SQL basándose en el nombre del método
    Optional<TrackingInfo> findByOrderId(Long orderId);

    // Find all tracking info for a specific product
    List<TrackingInfo> findByProductId(Long productId);
}
