# Testing Checklist & API Documentation

Complete testing guide with curl commands for all microservices integrations.

## Prerequisites

- All services running:
  - Gateway: http://localhost:8080
  - Catalogue: http://localhost:8081
  - Panier: http://localhost:8082
  - Paiment: http://localhost:8083
  - Tracking: http://localhost:8084

**Verify services:**
```bash
# Test Gateway
curl http://localhost:8080

# Test Catalogue
curl http://localhost:8081/api/products

# Test Panier
curl http://localhost:8082/cart

# Test Paiment
curl http://localhost:8083/api/payments

# Test Tracking
curl http://localhost:8084/api/tracking
```

---

## Test Suite 1: Catalogue Service (Stock Management)

### ✅ Test 1.1: List All Products
```bash
curl http://localhost:8080/api/products
```

**Expected Response:**
```json
[
  {
    "id": 1,
    "name": "Empanada",
    "description": "Pâte de maïs croustillante...",
    "price": 3.0,
    "quantity": 15
  },
  {
    "id": 2,
    "name": "Arepa",
    "description": "Arepa de maïs...",
    "price": 4.9,
    "quantity": 20
  },
  {
    "id": 3,
    "name": "Bandeja Paisa",
    "description": "Le plat emblématique...",
    "price": 12.45,
    "quantity": 10
  }
]
```

### ✅ Test 1.2: Get Product by ID
```bash
curl http://localhost:8080/api/products/1
```

**Expected Response:**
```json
{
  "id": 1,
  "name": "Empanada",
  "description": "Pâte de maïs croustillante...",
  "price": 3.0,
  "quantity": 15
}
```

### ✅ Test 1.3: Check Stock Availability (NEW)
```bash
# Check if 5 units available
curl "http://localhost:8080/api/products/1/stock/check?quantity=5"
```

**Expected Response:**
```json
true
```

```bash
# Check if 100 units available (should return false)
curl "http://localhost:8080/api/products/1/stock/check?quantity=100"
```

**Expected Response:**
```json
false
```

### ✅ Test 1.4: Deduct Stock (NEW)
```bash
# Deduct 3 units from product 1
curl -X PUT "http://localhost:8080/api/products/1/stock/deduct?quantity=3"
```

**Expected Response:** `200 OK`

**Verify:**
```bash
curl http://localhost:8080/api/products/1
# Should show quantity: 12 (15 - 3)
```

### ✅ Test 1.5: Restore Stock (NEW)
```bash
# Restore 3 units to product 1
curl -X PUT "http://localhost:8080/api/products/1/stock/restore?quantity=3"
```

**Expected Response:** `200 OK`

**Verify:**
```bash
curl http://localhost:8080/api/products/1
# Should show quantity: 15 (back to original)
```

### ❌ Test 1.6: Deduct More Than Available Stock (Error Case)
```bash
# Try to deduct 100 units (only 15 available)
curl -X PUT "http://localhost:8080/api/products/1/stock/deduct?quantity=100"
```

**Expected Response:** `500 Internal Server Error`
```json
{
  "timestamp": "2026-01-19T...",
  "status": 500,
  "error": "Internal Server Error",
  "message": "Not enough stock. Available: 15, Requested: 100"
}
```

---

## Test Suite 2: Panier Service (Add to Cart with Validation)

### ✅ Test 2.1: Add Product to Cart (Success)
```bash
# Add 2 Empanadas to cart
curl -X POST "http://localhost:8080/cart/add?productId=1&quantity=2"
```

**Expected Response:**
```json
{
  "id": 1,
  "productId": 1,
  "quantity": 2,
  "unitPrice": 3.0
}
```

### ✅ Test 2.2: Add Another Product to Cart
```bash
# Add 1 Arepa to cart
curl -X POST "http://localhost:8080/cart/add?productId=2&quantity=1"
```

**Expected Response:**
```json
{
  "id": 2,
  "productId": 2,
  "quantity": 1,
  "unitPrice": 4.9
}
```

### ✅ Test 2.3: View Cart
```bash
curl http://localhost:8080/cart
```

**Expected Response:**
```json
[
  {
    "id": 1,
    "productId": 1,
    "quantity": 2,
    "unitPrice": 3.0
  },
  {
    "id": 2,
    "productId": 2,
    "quantity": 1,
    "unitPrice": 4.9
  }
]
```

**Total:** 2 × 3.00 + 1 × 4.90 = 10.90€

### ❌ Test 2.4: Add More Than Available Stock (Error Case)
```bash
# Try to add 100 units (only 15 available)
curl -X POST "http://localhost:8080/cart/add?productId=1&quantity=100"
```

**Expected Response:** `400 Bad Request` or `500 Internal Server Error`
```json
{
  "message": "Insufficient stock. Available: 15, Requested: 100"
}
```

### ✅ Test 2.5: Add Same Product Again (Incremental)
```bash
# Add 1 more Empanada (should increment to 3)
curl -X POST "http://localhost:8080/cart/add?productId=1&quantity=1"
```

**Expected Response:**
```json
{
  "id": 1,
  "productId": 1,
  "quantity": 3,
  "unitPrice": 3.0
}
```

### ✅ Test 2.6: Clear Cart
```bash
curl -X DELETE http://localhost:8080/cart
```

**Expected Response:**
```
Cart cleared successfully
```

---

## Test Suite 3: Checkout Process (Integration Test)

### Setup: Add Items to Cart
```bash
# Add 2 Empanadas
curl -X POST "http://localhost:8080/cart/add?productId=1&quantity=2"

# Add 1 Arepa
curl -X POST "http://localhost:8080/cart/add?productId=2&quantity=1"
```

### ✅ Test 3.1: Complete Checkout (Success)
```bash
curl -X POST http://localhost:8080/cart/checkout \
  -H "Content-Type: application/json" \
  -d '{
    "cardType": "VISA",
    "cardNumber": "4111111111111111",
    "shippingAddress": "123 Rue de Paris, 75001 Paris, France"
  }'
```

**Expected Response:**
```json
{
  "orderId": 1,
  "totalAmount": 10.9,
  "paymentId": 1,
  "trackingId": 1,
  "status": "PENDING",
  "items": [
    {
      "productId": 1,
      "productName": "Empanada",
      "quantity": 2,
      "unitPrice": 3.0,
      "subtotal": 6.0
    },
    {
      "productId": 2,
      "productName": "Arepa",
      "quantity": 1,
      "unitPrice": 4.9,
      "subtotal": 4.9
    }
  ]
}
```

**What Should Happen:**
1. ✅ Stock deducted: Product 1 (15 → 13), Product 2 (20 → 19)
2. ✅ Payment created and approved
3. ✅ Order created in panier DB
4. ✅ Tracking created
5. ✅ Cart cleared

### ✅ Test 3.2: Verify Stock Was Deducted
```bash
# Check Product 1
curl http://localhost:8080/api/products/1
# Expected quantity: 13 (was 15, deducted 2)

# Check Product 2
curl http://localhost:8080/api/products/2
# Expected quantity: 19 (was 20, deducted 1)
```

### ✅ Test 3.3: Verify Payment Was Created
```bash
curl http://localhost:8080/api/payments/1
```

**Expected Response:**
```json
{
  "id": 1,
  "cardType": "VISA",
  "cardNumber": "4111111111111111",
  "amount": 10.9,
  "approved": true
}
```

### ✅ Test 3.4: Verify Order Was Created
```bash
curl http://localhost:8080/cart/orders/1
```

**Expected Response:**
```json
{
  "id": 1,
  "items": [
    {
      "id": 1,
      "productId": 1,
      "productName": "Empanada",
      "quantity": 2,
      "unitPrice": 3.0,
      "subtotal": 6.0
    },
    {
      "id": 2,
      "productId": 2,
      "productName": "Arepa",
      "quantity": 1,
      "unitPrice": 4.9,
      "subtotal": 4.9
    }
  ],
  "totalAmount": 10.9,
  "status": "PENDING",
  "paymentId": 1,
  "trackingId": 1,
  "createdAt": "2026-01-19T...",
  "updatedAt": "2026-01-19T..."
}
```

### ✅ Test 3.5: Verify Tracking Was Created
```bash
curl http://localhost:8080/api/tracking/order/1
```

**Expected Response:**
```json
{
  "id": 1,
  "orderId": 1,
  "productId": 1,
  "status": "PENDING",
  "currentLocation": "Warehouse",
  "lastUpdate": "2026-01-19T..."
}
```

### ✅ Test 3.6: Verify Cart Was Cleared
```bash
curl http://localhost:8080/cart
```

**Expected Response:**
```json
[]
```

### ❌ Test 3.7: Checkout with Empty Cart (Error Case)
```bash
curl -X POST http://localhost:8080/cart/checkout \
  -H "Content-Type: application/json" \
  -d '{
    "cardType": "VISA",
    "cardNumber": "4111111111111111",
    "shippingAddress": "123 Rue de Paris"
  }'
```

**Expected Response:** `500 Internal Server Error`
```json
{
  "message": "Cart is empty"
}
```

---

## Test Suite 4: Tracking Service (Enriched Data)

### ✅ Test 4.1: Get Basic Tracking Info
```bash
curl http://localhost:8080/api/tracking/order/1
```

**Expected Response:**
```json
{
  "id": 1,
  "orderId": 1,
  "productId": 1,
  "status": "PENDING",
  "currentLocation": "Warehouse",
  "lastUpdate": "2026-01-19T..."
}
```

### ✅ Test 4.2: Get Enriched Tracking (with Product Details)
```bash
curl http://localhost:8080/api/tracking/order/1/enriched
```

**Expected Response:**
```json
{
  "id": 1,
  "orderId": 1,
  "productId": 1,
  "status": "PENDING",
  "currentLocation": "Warehouse",
  "lastUpdate": "2026-01-19T...",
  "productName": "Empanada",
  "productDescription": "Pâte de maïs croustillante...",
  "productPrice": 3.0
}
```

### ✅ Test 4.3: Update Tracking Status
```bash
# Update to PREPARING
curl -X PUT "http://localhost:8080/api/tracking/1?status=PREPARING&location=Kitchen"
```

**Expected Response:**
```json
{
  "id": 1,
  "orderId": 1,
  "productId": 1,
  "status": "PREPARING",
  "currentLocation": "Kitchen",
  "lastUpdate": "2026-01-19T..."
}
```

```bash
# Update to READY
curl -X PUT "http://localhost:8080/api/tracking/1?status=READY&location=Counter"

# Update to IN_TRANSIT
curl -X PUT "http://localhost:8080/api/tracking/1?status=IN_TRANSIT&location=Delivery%20Van"

# Update to DELIVERED
curl -X PUT "http://localhost:8080/api/tracking/1?status=DELIVERED&location=Customer%20Address"
```

### ✅ Test 4.4: Get All Trackings for a Product
```bash
curl http://localhost:8080/api/tracking/product/1/enriched
```

**Expected Response:**
```json
[
  {
    "id": 1,
    "orderId": 1,
    "productId": 1,
    "status": "DELIVERED",
    "currentLocation": "Customer Address",
    "lastUpdate": "2026-01-19T...",
    "productName": "Empanada",
    "productDescription": "Pâte de maïs croustillante...",
    "productPrice": 3.0
  }
]
```

---

## Test Suite 5: Cancel Order (Rollback Flow)

### Setup: Create a New Order
```bash
# Add items
curl -X POST "http://localhost:8080/cart/add?productId=1&quantity=5"

# Checkout
curl -X POST http://localhost:8080/cart/checkout \
  -H "Content-Type: application/json" \
  -d '{
    "cardType": "MASTERCARD",
    "cardNumber": "5500000000000004",
    "shippingAddress": "456 Avenue des Champs"
  }'
```

**Note the orderId and trackingId from response (e.g., orderId=2, trackingId=2)**

### ✅ Test 5.1: Check Stock Before Cancellation
```bash
curl http://localhost:8080/api/products/1
# Expected quantity: 8 (13 - 5 from new order)
```

### ✅ Test 5.2: Cancel Order
```bash
# Cancel using trackingId
curl -X PUT http://localhost:8080/api/tracking/2/cancel
```

**Expected Response:**
```json
{
  "id": 2,
  "orderId": 2,
  "productId": 1,
  "status": "CANCELLED",
  "currentLocation": "Warehouse",
  "lastUpdate": "2026-01-19T..."
}
```

### ✅ Test 5.3: Verify Stock Was Restored
```bash
curl http://localhost:8080/api/products/1
# Expected quantity: 13 (8 + 5 restored)
```

### ❌ Test 5.4: Try to Cancel Already Cancelled Order
```bash
curl -X PUT http://localhost:8080/api/tracking/2/cancel
```

**Expected Response:** `500 Internal Server Error`
```json
{
  "message": "Cannot cancel order in status: CANCELLED"
}
```

### ❌ Test 5.5: Try to Cancel Delivered Order
```bash
# First update to delivered
curl -X PUT "http://localhost:8080/api/tracking/1?status=DELIVERED&location=Customer"

# Then try to cancel
curl -X PUT http://localhost:8080/api/tracking/1/cancel
```

**Expected Response:** `500 Internal Server Error`
```json
{
  "message": "Cannot cancel order in status: DELIVERED"
}
```

---

## Test Suite 6: Edge Cases & Error Scenarios

### ❌ Test 6.1: Product Not Found
```bash
curl http://localhost:8080/api/products/999
```

**Expected Response:** `500 Internal Server Error`
```json
{
  "message": "Producto no encontrado con el id: 999"
}
```

### ❌ Test 6.2: Add Non-Existent Product to Cart
```bash
curl -X POST "http://localhost:8080/cart/add?productId=999&quantity=1"
```

**Expected Response:** `500 Internal Server Error`
```json
{
  "message": "Product not found: 999"
}
```

### ❌ Test 6.3: Checkout with Invalid Payment Data
```bash
curl -X POST http://localhost:8080/cart/checkout \
  -H "Content-Type: application/json" \
  -d '{
    "cardType": "",
    "cardNumber": "",
    "shippingAddress": ""
  }'
```

**Expected:** Payment service should handle this (implementation dependent)

### ❌ Test 6.4: Get Non-Existent Order
```bash
curl http://localhost:8080/cart/orders/999
```

**Expected Response:** `500 Internal Server Error`
```json
{
  "message": "Order not found: 999"
}
```

### ❌ Test 6.5: Get Non-Existent Tracking
```bash
curl http://localhost:8080/api/tracking/999
```

**Expected Response:** `500 Internal Server Error`
```json
{
  "message": "Tracking not found with id: 999"
}
```

---

## Test Suite 7: Complete E2E User Journey

Run this complete workflow to test the entire system:

```bash
#!/bin/bash

echo "=== E-Commerce E2E Test ==="
echo ""

echo "1. List all products"
curl -s http://localhost:8080/api/products | jq .
echo ""

echo "2. Add 2 Empanadas to cart"
curl -s -X POST "http://localhost:8080/cart/add?productId=1&quantity=2" | jq .
echo ""

echo "3. Add 1 Arepa to cart"
curl -s -X POST "http://localhost:8080/cart/add?productId=2&quantity=1" | jq .
echo ""

echo "4. View cart (total should be 10.90€)"
curl -s http://localhost:8080/cart | jq .
echo ""

echo "5. Checkout"
CHECKOUT_RESPONSE=$(curl -s -X POST http://localhost:8080/cart/checkout \
  -H "Content-Type: application/json" \
  -d '{
    "cardType": "VISA",
    "cardNumber": "4111111111111111",
    "shippingAddress": "123 Rue de Paris, France"
  }')
echo $CHECKOUT_RESPONSE | jq .
ORDER_ID=$(echo $CHECKOUT_RESPONSE | jq -r .orderId)
TRACKING_ID=$(echo $CHECKOUT_RESPONSE | jq -r .trackingId)
echo ""

echo "6. Verify stock deducted for Product 1"
curl -s http://localhost:8080/api/products/1 | jq .
echo ""

echo "7. View order details (Order ID: $ORDER_ID)"
curl -s "http://localhost:8080/cart/orders/$ORDER_ID" | jq .
echo ""

echo "8. Track order with enriched data (Tracking ID: $TRACKING_ID)"
curl -s "http://localhost:8080/api/tracking/order/$ORDER_ID/enriched" | jq .
echo ""

echo "9. Update tracking status to PREPARING"
curl -s -X PUT "http://localhost:8080/api/tracking/$TRACKING_ID?status=PREPARING&location=Kitchen" | jq .
echo ""

echo "10. Update tracking status to IN_TRANSIT"
curl -s -X PUT "http://localhost:8080/api/tracking/$TRACKING_ID?status=IN_TRANSIT&location=Delivery%20Van" | jq .
echo ""

echo "11. Update tracking status to DELIVERED"
curl -s -X PUT "http://localhost:8080/api/tracking/$TRACKING_ID?status=DELIVERED&location=Customer%20Address" | jq .
echo ""

echo "=== Test Complete ==="
```

Save this as `test-e2e.sh` and run with:
```bash
chmod +x test-e2e.sh
./test-e2e.sh
```

---

## Test Suite 8: Performance & Stress Testing

### Test 8.1: Multiple Simultaneous Checkouts

Create 5 orders in quick succession:

```bash
for i in {1..5}; do
  curl -X POST "http://localhost:8080/cart/add?productId=1&quantity=1"
  curl -X POST http://localhost:8080/cart/checkout \
    -H "Content-Type: application/json" \
    -d '{
      "cardType": "VISA",
      "cardNumber": "4111111111111111",
      "shippingAddress": "Address '$i'"
    }'
done
```

**Verify:**
- All orders created successfully
- Stock correctly deducted (should be 15 - 5 = 10)
- No race conditions

### Test 8.2: Check Stock Consistency

```bash
# Add many items to cart from different terminals
# Terminal 1:
curl -X POST "http://localhost:8080/cart/add?productId=1&quantity=5"

# Terminal 2 (at the same time):
curl -X POST "http://localhost:8080/cart/add?productId=1&quantity=5"

# Both should succeed since total (10) < available (15)
```

---

## Quick Reference: All Endpoints

### Catalogue Service (8081)
```
GET    /api/products                          - List all products
GET    /api/products/{id}                     - Get product by ID
GET    /api/products/{id}/stock/check?quantity={qty} - Check stock
POST   /api/products                          - Create product
PUT    /api/products/{id}                     - Update product
PUT    /api/products/{id}/stock/deduct?quantity={qty} - Deduct stock
PUT    /api/products/{id}/stock/restore?quantity={qty} - Restore stock
DELETE /api/products/{id}                     - Delete product
```

### Panier Service (8082)
```
POST   /cart/add?productId={id}&quantity={qty} - Add to cart
GET    /cart                                   - View cart
DELETE /cart                                   - Clear cart
POST   /cart/checkout                          - Checkout
GET    /cart/orders                            - Get all orders
GET    /cart/orders/{orderId}                  - Get order by ID
```

### Paiment Service (8083)
```
POST   /api/payments                           - Create payment
GET    /api/payments/{id}                      - Get payment by ID
GET    /api/payments                           - Get all payments
PUT    /api/payments/{id}/process              - Process payment
DELETE /api/payments/{id}                      - Delete payment
```

### Tracking Service (8084)
```
POST   /api/tracking                           - Create tracking
GET    /api/tracking                           - Get all trackings
GET    /api/tracking/{id}                      - Get tracking by ID
GET    /api/tracking/order/{orderId}           - Get tracking by order
GET    /api/tracking/order/{orderId}/enriched  - Get enriched tracking
GET    /api/tracking/product/{productId}       - Get trackings by product
GET    /api/tracking/product/{productId}/enriched - Get enriched trackings
PUT    /api/tracking/{id}?status={s}&location={l} - Update status
PUT    /api/tracking/{id}/cancel               - Cancel order
DELETE /api/tracking/{id}                      - Delete tracking
```

---

## Testing Checklist

### Phase 1: Individual Services
- [ ] Catalogue: List products
- [ ] Catalogue: Get product by ID
- [ ] Catalogue: Check stock
- [ ] Catalogue: Deduct stock
- [ ] Catalogue: Restore stock
- [ ] Panier: Add to cart (basic)
- [ ] Panier: View cart
- [ ] Panier: Clear cart
- [ ] Paiment: Create payment
- [ ] Paiment: Process payment
- [ ] Tracking: Create tracking
- [ ] Tracking: Get tracking

### Phase 2: Integration Tests
- [ ] Add to cart with stock validation
- [ ] Add to cart with insufficient stock (error)
- [ ] Checkout complete flow
- [ ] Verify stock deducted after checkout
- [ ] Verify payment created
- [ ] Verify order created
- [ ] Verify tracking created
- [ ] Verify cart cleared

### Phase 3: Enrichment Tests
- [ ] Get enriched tracking with product details
- [ ] Get enriched trackings by product

### Phase 4: Cancellation Tests
- [ ] Cancel pending order
- [ ] Verify stock restored
- [ ] Cannot cancel delivered order (error)
- [ ] Cannot cancel already cancelled order (error)

### Phase 5: Error Handling
- [ ] Product not found
- [ ] Insufficient stock
- [ ] Empty cart checkout
- [ ] Invalid order ID
- [ ] Invalid tracking ID

### Phase 6: E2E Journey
- [ ] Complete user journey from browse to delivery
- [ ] Multiple orders
- [ ] Stock consistency

---

## Troubleshooting Guide

### Problem: 404 Not Found
**Solution:** Check if gateway routes are correct in `application.yml`

### Problem: Connection Refused
**Solution:** Verify all services are running on correct ports

### Problem: Stock not deducting
**Solution:** Check if `deductStock` endpoint is implemented and called

### Problem: Tracking not created
**Solution:** Check logs in panier-service, may be non-critical failure

### Problem: Payment not approved
**Solution:** Check if `/process` endpoint is called after payment creation

---

**Last Updated:** January 19, 2026
**Total Test Cases:** 60+
