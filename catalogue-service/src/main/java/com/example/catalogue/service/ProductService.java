package com.example.catalogue.service;

import com.example.catalogue.entity.Product;
import com.example.catalogue.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service // Esto le dice a Spring que esta clase es el "Service" (Lógica)
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    // Get all products
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    // Save a product
    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    // Update product
    public Product updateProduct(Long id, Product productDetails) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        product.setName(productDetails.getName());
        product.setDescription(productDetails.getDescription());
        product.setPrice(productDetails.getPrice());
        product.setQuantity(productDetails.getQuantity());

        return productRepository.save(product);
    }

    // Find by ID
    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    // Delete
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }



    // Check if product has sufficient stock
    public boolean hasStock(Long productId, int requestedQuantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));
        return product.getQuantity() >= requestedQuantity;
    }

    // Reduce stock (for checkout)
    public void reduceStock(Long productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));
        
        if (product.getQuantity() < quantity) {
            throw new RuntimeException("Insufficient stock for product: " + product.getName() + 
                    ". Available: " + product.getQuantity() + ", Requested: " + quantity);
        }
        
        product.setQuantity(product.getQuantity() - quantity);
        productRepository.save(product);
    }

    // Restore stock (for order cancellation)
    public void restoreStock(Long productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));
        
        product.setQuantity(product.getQuantity() + quantity);
        productRepository.save(product);
    }
}