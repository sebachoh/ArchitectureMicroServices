package com.example.paiment_service.controller;

import com.example.paiment_service.entity.PaimentItem;
import com.example.paiment_service.service.PaimentItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/payments")
public class PaimentItemController {

    @Autowired
    private PaimentItemService paimentItemService;

    @PostMapping
    public ResponseEntity<PaimentItem> createPayment(@RequestBody PaimentItem paiment) {
        PaimentItem createdPayment = paimentItemService.createPayment(paiment);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPayment);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaimentItem> getPayment(@PathVariable Long id) {
        Optional<PaimentItem> paiment = paimentItemService.getPayment(id);
        return paiment.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<PaimentItem>> getAllPayments() {
        List<PaimentItem> payments = paimentItemService.getAllPayments();
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/card/{cardNumber}")
    public ResponseEntity<PaimentItem> getPaymentByCardNumber(@PathVariable String cardNumber) {
        Optional<PaimentItem> paiment = paimentItemService.getPaymentByCardNumber(cardNumber);
        return paiment.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/process")
    public ResponseEntity<PaimentItem> processPayment(@PathVariable Long id) {
        PaimentItem paiment = paimentItemService.processPayment(id);
        if (paiment != null) {
            return ResponseEntity.ok(paiment);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePayment(@PathVariable Long id) {
        paimentItemService.deletePayment(id);
        return ResponseEntity.noContent().build();
    }
}