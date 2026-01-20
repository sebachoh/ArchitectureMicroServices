package com.example.catalogue.controller;

import com.example.catalogue.entity.Product;
import com.example.catalogue.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")

public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping
    public List<Product> getAllProducts() {
        return productService.getAllProducts();
    }

    @GetMapping("/{id}")
    public Product getProductById(@PathVariable Long id) {
        return productService.getProductById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con el id: " + id));
    }

    @PostMapping
    public Product createProduct(@Valid @RequestBody Product product) {
        return productService.saveProduct(product);
    }

    @DeleteMapping("/{id}")
    public void deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
    }

    @PutMapping("/{id}")
    public Product updateProduct(@PathVariable Long id, @Valid @RequestBody Product product) {
        return productService.updateProduct(id, product);
    }

    // Check stock availability
    @GetMapping("/{id}/check-stock")
    public ResponseEntity<Map<String, Object>> checkStock(
            @PathVariable Long id, 
            @RequestParam int quantity) {
        boolean hasStock = productService.hasStock(id, quantity);
        Product product = productService.getProductById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        
        return ResponseEntity.ok(Map.of(
            "productId", id,
            "available", hasStock,
            "currentStock", product.getQuantity(),
            "requestedQuantity", quantity
        ));
    }

    // Reduce stock (called by panier-service during checkout)
    @PutMapping("/{id}/reduce-stock")
    public ResponseEntity<String> reduceStock(
            @PathVariable Long id, 
            @RequestParam int quantity) {
        productService.reduceStock(id, quantity);
        return ResponseEntity.ok("Stock reduced successfully");
    }

    // Restore stock (called when order is cancelled)
    @PutMapping("/{id}/restore-stock")
    public ResponseEntity<String> restoreStock(
            @PathVariable Long id, 
            @RequestParam int quantity) {
        productService.restoreStock(id, quantity);
        return ResponseEntity.ok("Stock restored successfully");
    }

}
