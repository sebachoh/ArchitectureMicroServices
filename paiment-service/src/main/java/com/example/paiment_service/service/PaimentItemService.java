package com.example.paiment_service.service;

import com.example.paiment_service.entity.PaimentItem;
import com.example.paiment_service.repository.PaimentItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class PaimentItemService {

    @Autowired
    private PaimentItemRepository paimentItemRepository;

    public PaimentItem createPayment(PaimentItem paiment) {
        return paimentItemRepository.save(paiment);
    }

    public PaimentItem processPayment(Long id) {
        Optional<PaimentItem> paiment = paimentItemRepository.findById(id);
        if (paiment.isPresent()) {
            PaimentItem p = paiment.get();
            p.setApproved(true);
            return paimentItemRepository.save(p);
        }
        return null;
    }

    public Optional<PaimentItem> getPayment(Long id) {
        return paimentItemRepository.findById(id);
    }

    public Optional<PaimentItem> getPaymentByCardNumber(String cardNumber) {
        return paimentItemRepository.findByCardNumber(cardNumber);
    }

    public List<PaimentItem> getAllPayments() {
        return paimentItemRepository.findAll();
    }

    public void deletePayment(Long id) {
        paimentItemRepository.deleteById(id);
    }
}