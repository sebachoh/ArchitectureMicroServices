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

    // Obtener todos los productos
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    // Guardar un producto
    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    /////
    ///
    ///
    ///
    public Product updateProduct(Long id, Product productDetails) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        product.setName(productDetails.getName());
        product.setDescription(productDetails.getDescription());
        product.setPrice(productDetails.getPrice());
        product.setQuantity(productDetails.getQuantity());

        return productRepository.save(product);
    }

    // Buscar por ID
    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    // Eliminar
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }
}