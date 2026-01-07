package com.example.panier.controller;

import com.example.panier.entity.CartItem;
import com.example.panier.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    // Endpoint para añadir productos
    // Ejemplo: POST http://localhost:8082/cart/add?productId=1&quantity=2
    @PostMapping("/add")
    public CartItem addToCart(@RequestParam Long productId, @RequestParam Integer quantity) {
        return cartService.addToCart(productId, quantity);
    }

    // Endpoint para ver el carrito actual
    @GetMapping
    public List<CartItem> getCart() {
        return cartService.getCart();
    }

    // Endpoint para vaciar el carrito
    @DeleteMapping
    public String clearCart() {
        cartService.clearCart();
        return "Carrito vaciado correctamente";
    }
}