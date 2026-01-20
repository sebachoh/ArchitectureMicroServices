package com.example.panier.controller;

import com.example.panier.entity.CartItem;
import com.example.panier.entity.Order;
import com.example.panier.service.CartService;
import com.example.panier.dto.CheckoutRequest;
import com.example.panier.dto.CheckoutResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    // Add product to cart
    @PostMapping("/add")
    public CartItem addToCart(@RequestParam Long productId, @RequestParam Integer quantity) {
        return cartService.addToCart(productId, quantity);
    }

    // View cart
    @GetMapping
    public List<CartItem> getCart() {
        return cartService.getCart();
    }

    // Clear cart
    @DeleteMapping
    public String clearCart() {
        cartService.clearCart();
        return "Cart cleared successfully";
    }

    // Complete checkout
    @PostMapping("/checkout")
    public CheckoutResponse checkout(@RequestBody CheckoutRequest request) {
        return cartService.checkout(request);
    }

    // Get all orders
    @GetMapping("/orders")
    public List<Order> getAllOrders() {
        return cartService.getAllOrders();
    }

    // Get order by ID
    @GetMapping("/orders/{orderId}")
    public Order getOrderById(@PathVariable Long orderId) {
        return cartService.getOrderById(orderId);
    }
}