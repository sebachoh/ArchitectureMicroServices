package com.example.panier.service;

import com.example.panier.entity.CartItem;
import com.example.panier.repository.CartItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class CartService {

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private RestTemplate restTemplate;

    private final String CATALOGUE_URL = "http://localhost:8081/api/products/";

    public CartItem addToCart(Long productId, Integer quantity) {
        // 1. Comunicación Síncrona: Llamamos al Catálogo
        // Obtenemos el producto como un Map
        Map<String, Object> product = restTemplate.getForObject(CATALOGUE_URL + productId, Map.class);

        if (product == null) {
            throw new RuntimeException("Producto no encontrado en el catálogo");
        }

        // Extraemos el precio del Map (el catálogo lo envía como double)
        double price = Double.parseDouble(product.get("price").toString());

        // 2. Lógica de negocio: Verificamos si ya existe en el carrito
        CartItem item = cartItemRepository.findByProductId(productId)
                .orElse(new CartItem(null, productId, 0, price));

        item.setQuantity(item.getQuantity() + quantity);
        item.setUnitPrice(price);

        return cartItemRepository.save(item);
    }

    public List<CartItem> getCart() {
        return cartItemRepository.findAll();
    }

    public void clearCart() {
        cartItemRepository.deleteAll();
    }
}