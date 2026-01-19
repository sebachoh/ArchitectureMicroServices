package com.example.paiment_service.repository;

import com.example.paiment_service.entity.PaimentItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PaimentItemRepository extends JpaRepository<PaimentItem, Long> {
    Optional<PaimentItem> findByCardNumber(String cardNumber);
}