# E-Commerce Microservices - Sequence Diagrams

This document contains detailed sequence diagrams for all business flows.

## Flow 1: Add to Cart (Enhanced with Stock Validation)

```
┌─────────┐         ┌────────┐         ┌──────────┐
│ Frontend│         │ Panier │         │ Catalogue│
└────┬────┘         └───┬────┘         └────┬─────┘
     │                  │                    │
     │ POST /cart/add   │                    │
     │ productId=1      │                    │
     │ quantity=2       │                    │
     ├─────────────────>│                    │
     │                  │                    │
     │                  │ GET /products/1    │
     │                  ├───────────────────>│
     │                  │                    │
     │                  │ {                  │
     │                  │   id: 1,           │
     │                  │   name: "Empanada",│
     │                  │   price: 3.00,     │
     │                  │   quantity: 15     │
     │                  │ }                  │
     │                  │<───────────────────┤
     │                  │                    │
     │                  │ Validate:          │
     │                  │ 15 >= 2 ✓         │
     │                  │                    │
     │                  │ findByProductId(1) │
     │                  │ (check if exists)  │
     │                  │                    │
     │                  │ Save CartItem:     │
     │                  │ {                  │
     │                  │   productId: 1,    │
     │                  │   quantity: 2,     │
     │                  │   unitPrice: 3.00  │
     │                  │ }                  │
     │                  │                    │
     │   200 OK         │                    │
     │   CartItem       │                    │
     │<─────────────────┤                    │
     │                  │                    │
```

**Error Case - Insufficient Stock:**

```
┌─────────┐         ┌────────┐         ┌──────────┐
│ Frontend│         │ Panier │         │ Catalogue│
└────┬────┘         └───┬────┘         └────┬─────┘
     │                  │                    │
     │ POST /cart/add   │                    │
     │ productId=1      │                    │
     │ quantity=20      │                    │
     ├─────────────────>│                    │
     │                  │                    │
     │                  │ GET /products/1    │
     │                  ├───────────────────>│
     │                  │                    │
     │                  │ { quantity: 10 }   │
     │                  │<───────────────────┤
     │                  │                    │
     │                  │ Check: 10 < 20 ✗  │
     │                  │ throw Exception    │
     │                  │                    │
     │ 400 Bad Request  │                    │
     │ {                │                    │
     │   "message": "Insufficient stock.    │
     │                Available: 10,         │
     │                Requested: 20"         │
     │ }                │                    │
     │<─────────────────┤                    │
```

---

## Flow 2: Complete Checkout Process

```
┌─────────┐    ┌────────┐    ┌──────────┐    ┌─────────┐    ┌──────────┐
│ Frontend│    │ Panier │    │ Catalogue│    │ Paiment │    │ Tracking │
└────┬────┘    └───┬────┘    └────┬─────┘    └────┬────┘    └────┬─────┘
     │             │              │               │              │
     │ 1. POST /cart/checkout     │               │              │
     │ {                          │               │              │
     │   cardType: "VISA",        │               │              │
     │   cardNumber: "4111...",   │               │              │
     │   shippingAddress: "..."   │               │              │
     │ }            │              │               │              │
     ├────────────>│              │               │              │
     │             │              │               │              │
     │             │ Get Cart     │               │              │
     │             │ Items from DB│               │              │
     │             │              │               │              │
     │             │ For each item:               │              │
     │             │ GET /products/{id}           │              │
     │             ├─────────────>│               │              │
     │             │              │               │              │
     │             │ Validate Stock Available     │              │
     │             │<─────────────┤               │              │
     │             │              │               │              │
     │             │ Calculate Total: 9.90        │              │
     │             │              │               │              │
     │             │ 2. Create Payment            │              │
     │             │ POST /payments               │              │
     │             │ {                            │              │
     │             │   cardType: "VISA",          │              │
     │             │   cardNumber: "4111...",     │              │
     │             │   amount: 9.90,              │              │
     │             │   approved: false            │              │
     │             │ }            │               │              │
     │             ├──────────────────────────────>│              │
     │             │              │               │              │
     │             │ { id: 1, approved: false }   │              │
     │             │<──────────────────────────────┤              │
     │             │              │               │              │
     │             │ 3. Process Payment           │              │
     │             │ PUT /payments/1/process      │              │
     │             ├──────────────────────────────>│              │
     │             │              │               │              │
     │             │ { id: 1, approved: true }    │              │
     │             │<──────────────────────────────┤              │
     │             │              │               │              │
     │             │ 4. Deduct Stock              │              │
     │             │ For each item:               │              │
     │             │ PUT /products/1/stock/deduct?quantity=2     │
     │             ├─────────────>│               │              │
     │             │ 200 OK       │               │              │
     │             │<─────────────┤               │              │
     │             │              │               │              │
     │             │ PUT /products/2/stock/deduct?quantity=1     │
     │             ├─────────────>│               │              │
     │             │ 200 OK       │               │              │
     │             │<─────────────┤               │              │
     │             │              │               │              │
     │             │ 5. Create Order in DB        │              │
     │             │ {                            │              │
     │             │   id: 1,                     │              │
     │             │   items: [...]               │              │
     │             │   totalAmount: 9.90,         │              │
     │             │   status: "PENDING",         │              │
     │             │   paymentId: 1               │              │
     │             │ }            │               │              │
     │             │              │               │              │
     │             │ 6. Create Tracking           │              │
     │             │ POST /tracking               │              │
     │             │ {                            │              │
     │             │   orderId: 1,                │              │
     │             │   productId: 1,              │              │
     │             │   status: "PENDING",         │              │
     │             │   currentLocation: "Warehouse"              │
     │             │ }            │               │              │
     │             ├──────────────────────────────────────────────>│
     │             │              │               │              │
     │             │ { id: 1, orderId: 1, ... }   │              │
     │             │<──────────────────────────────────────────────┤
     │             │              │               │              │
     │             │ 7. Clear Cart│               │              │
     │             │ DELETE all CartItems         │              │
     │             │              │               │              │
     │ 200 OK      │              │               │              │
     │ {           │              │               │              │
     │   orderId: 1,              │               │              │
     │   totalAmount: 9.90,       │               │              │
     │   paymentId: 1,            │               │              │
     │   trackingId: 1,           │               │              │
     │   status: "PENDING",       │               │              │
     │   items: [...]             │               │              │
     │ }           │              │               │              │
     │<────────────┤              │               │              │
```

**Error Scenario - Payment Failed:**

```
┌─────────┐    ┌────────┐    ┌──────────┐    ┌─────────┐
│ Frontend│    │ Panier │    │ Catalogue│    │ Paiment │
└────┬────┘    └───┬────┘    └────┬─────┘    └────┬────┘
     │             │              │               │
     │ POST /cart/checkout        │               │
     ├────────────>│              │               │
     │             │              │               │
     │             │ Validate Stock ✓            │
     │             ├─────────────>│               │
     │             │<─────────────┤               │
     │             │              │               │
     │             │ POST /payments               │
     │             ├──────────────────────────────>│
     │             │              │               │
     │             │ Payment Declined ✗           │
     │             │ 402 Payment Required         │
     │             │<──────────────────────────────┤
     │             │              │               │
     │             │ Rollback:    │               │
     │             │ No stock     │               │
     │             │ deduction    │               │
     │             │              │               │
     │ 402 Payment Required       │               │
     │ { message: "Payment declined" }            │
     │<────────────┤              │               │
```

---

## Flow 3: Track Order with Enriched Product Data

```
┌─────────┐         ┌──────────┐         ┌──────────┐
│ Frontend│         │ Tracking │         │ Catalogue│
└────┬────┘         └────┬─────┘         └────┬─────┘
     │                   │                     │
     │ GET /tracking/order/1/enriched          │
     ├──────────────────>│                     │
     │                   │                     │
     │                   │ Find TrackingInfo   │
     │                   │ from DB:            │
     │                   │ {                   │
     │                   │   id: 1,            │
     │                   │   orderId: 1,       │
     │                   │   productId: 1,     │
     │                   │   status: "PENDING",│
     │                   │   location: "Warehouse"│
     │                   │ }                   │
     │                   │                     │
     │                   │ GET /products/1     │
     │                   ├────────────────────>│
     │                   │                     │
     │                   │ {                   │
     │                   │   id: 1,            │
     │                   │   name: "Empanada", │
     │                   │   description: "...",│
     │                   │   price: 3.00       │
     │                   │ }                   │
     │                   │<────────────────────┤
     │                   │                     │
     │                   │ Merge data:         │
     │                   │ EnrichedTrackingDTO │
     │                   │                     │
     │   200 OK          │                     │
     │   {               │                     │
     │     id: 1,        │                     │
     │     orderId: 1,   │                     │
     │     productId: 1, │                     │
     │     status: "PENDING",                  │
     │     currentLocation: "Warehouse",       │
     │     productName: "Empanada",            │
     │     productDescription: "...",          │
     │     productPrice: 3.00                  │
     │   }               │                     │
     │<──────────────────┤                     │
```

---

## Flow 4: Cancel Order (Stock Restoration)

```
┌─────────┐    ┌──────────┐    ┌────────┐    ┌──────────┐
│ Frontend│    │ Tracking │    │ Panier │    │ Catalogue│
└────┬────┘    └────┬─────┘    └───┬────┘    └────┬─────┘
     │              │               │              │
     │ PUT /tracking/1/cancel       │              │
     ├─────────────>│               │              │
     │              │               │              │
     │              │ Get TrackingInfo from DB:    │
     │              │ { status: "PENDING" }        │
     │              │               │              │
     │              │ Validate Status:             │
     │              │ Can cancel ✓  │              │
     │              │               │              │
     │              │ GET /cart/orders/1           │
     │              ├──────────────>│              │
     │              │               │              │
     │              │ {             │              │
     │              │   id: 1,      │              │
     │              │   items: [    │              │
     │              │     {productId: 1, qty: 2},  │
     │              │     {productId: 2, qty: 1}   │
     │              │   ]           │              │
     │              │ }             │              │
     │              │<──────────────┤              │
     │              │               │              │
     │              │ For each item:               │
     │              │ PUT /products/1/stock/restore?quantity=2
     │              ├──────────────────────────────>│
     │              │ 200 OK        │              │
     │              │<──────────────────────────────┤
     │              │               │              │
     │              │ PUT /products/2/stock/restore?quantity=1
     │              ├──────────────────────────────>│
     │              │ 200 OK        │              │
     │              │<──────────────────────────────┤
     │              │               │              │
     │              │ Update TrackingInfo:         │
     │              │ status = "CANCELLED"         │
     │              │ lastUpdate = now()           │
     │              │               │              │
     │   200 OK     │               │              │
     │   {          │               │              │
     │     id: 1,   │               │              │
     │     status: "CANCELLED",     │              │
     │     ...      │               │              │
     │   }          │               │              │
     │<─────────────┤               │              │
```

**Error Scenario - Cannot Cancel Shipped Order:**

```
┌─────────┐         ┌──────────┐
│ Frontend│         │ Tracking │
└────┬────┘         └────┬─────┘
     │                   │
     │ PUT /tracking/1/cancel
     ├──────────────────>│
     │                   │
     │                   │ Get TrackingInfo:
     │                   │ { status: "SHIPPED" }
     │                   │
     │                   │ Validate Status:
     │                   │ Cannot cancel ✗
     │                   │
     │ 400 Bad Request   │
     │ {                 │
     │   "message": "Cannot cancel order
     │                in status: SHIPPED"
     │ }                 │
     │<──────────────────┤
```

---

## Flow 5: Update Order Status (Tracking Lifecycle)

```
┌─────────┐         ┌──────────┐         ┌─────────┐
│Admin/Job│         │ Tracking │         │ Frontend│
└────┬────┘         └────┬─────┘         └────┬────┘
     │                   │                     │
     │ Order Lifecycle:  │                     │
     │                   │                     │
     │ 1. PUT /tracking/1?status=PREPARING&location=Kitchen
     ├──────────────────>│                     │
     │ 200 OK            │                     │
     │<──────────────────┤                     │
     │                   │                     │
     ╎                   ╎                     ╎
     │ (10 minutes later)│                     │
     ╎                   ╎                     ╎
     │                   │                     │
     │ 2. PUT /tracking/1?status=READY&location=Counter
     ├──────────────────>│                     │
     │ 200 OK            │                     │
     │<──────────────────┤                     │
     │                   │                     │
     ╎                   ╎                     ╎
     │ (5 minutes later) │                     │
     ╎                   ╎                     ╎
     │                   │                     │
     │ 3. PUT /tracking/1?status=IN_TRANSIT&location=Delivery Van
     ├──────────────────>│                     │
     │ 200 OK            │                     │
     │<──────────────────┤                     │
     │                   │                     │
     │                   │     User checks status
     │                   │ GET /tracking/order/1/enriched
     │                   │<────────────────────┤
     │                   │                     │
     │                   │ {                   │
     │                   │   status: "IN_TRANSIT",
     │                   │   location: "Delivery Van",
     │                   │   productName: "Empanada",
     │                   │   ...               │
     │                   │ }                   │
     │                   ├────────────────────>│
     │                   │                     │
     ╎                   ╎                     ╎
     │ (20 minutes later)│                     │
     ╎                   ╎                     ╎
     │                   │                     │
     │ 4. PUT /tracking/1?status=DELIVERED&location=Customer Address
     ├──────────────────>│                     │
     │ 200 OK            │                     │
     │<──────────────────┤                     │
```

---

## Flow 6: Service Communication Error Handling

**Scenario: Catalogue Service Down During Add to Cart**

```
┌─────────┐         ┌────────┐         ┌──────────┐
│ Frontend│         │ Panier │         │ Catalogue│
└────┬────┘         └───┬────┘         └────┬─────┘
     │                  │                  X │ (DOWN)
     │ POST /cart/add   │                    │
     │ productId=1      │                    │
     │ quantity=2       │                    │
     ├─────────────────>│                    │
     │                  │                    │
     │                  │ GET /products/1    │
     │                  ├───────────────────>X
     │                  │                    │
     │                  │ Timeout / Connection Error
     │                  │                    │
     │                  │ catch Exception    │
     │                  │ throw RuntimeException
     │                  │                    │
     │ 503 Service Unavailable              │
     │ {                │                    │
     │   "message": "Failed to communicate
     │                with catalogue service:
     │                Connection refused"
     │ }                │                    │
     │<─────────────────┤                    │
     │                  │                    │
     │ Show user:       │                    │
     │ "Product service│                    │
     │  temporarily     │                    │
     │  unavailable.    │                    │
     │  Please try again│                    │
     │  later."         │                    │
```

---

## Complete E-Commerce User Journey

```
┌──────┐ ┌───────┐ ┌──────┐ ┌─────────┐ ┌────────┐ ┌──────────┐
│ User │ │Gateway│ │Panier│ │Catalogue│ │Paiment │ │ Tracking │
└──┬───┘ └───┬───┘ └──┬───┘ └────┬────┘ └───┬────┘ └────┬─────┘
   │         │        │          │           │           │
   │ 1. Browse Products                      │           │
   │ GET /api/products                       │           │
   ├────────>├───────────────────>│          │           │
   │         │        │   All products       │           │
   │ 200 OK  │<───────────────────┤          │           │
   │<────────┤        │          │           │           │
   │         │        │          │           │           │
   │ 2. Add to Cart (Product 1, Qty 2)       │           │
   │ POST /cart/add?productId=1&quantity=2   │           │
   ├────────>├───────>│          │           │           │
   │         │        ├─────────>│ Check stock            │
   │         │        │<─────────┤ (15 available)         │
   │         │        │ Save CartItem        │           │
   │ 200 OK  │<───────┤          │           │           │
   │<────────┤        │          │           │           │
   │         │        │          │           │           │
   │ 3. Add to Cart (Product 2, Qty 1)       │           │
   │ POST /cart/add?productId=2&quantity=1   │           │
   ├────────>├───────>│          │           │           │
   │         │        ├─────────>│ Check stock            │
   │         │        │<─────────┤ (20 available)         │
   │         │        │ Save CartItem        │           │
   │ 200 OK  │<───────┤          │           │           │
   │<────────┤        │          │           │           │
   │         │        │          │           │           │
   │ 4. View Cart                            │           │
   │ GET /cart                               │           │
   ├────────>├───────>│          │           │           │
   │         │        │ [CartItem1, CartItem2]            │
   │ 200 OK  │<───────┤          │           │           │
   │<────────┤        │          │           │           │
   │         │        │          │           │           │
   │ User reviews cart, total: 9.90€         │           │
   │         │        │          │           │           │
   │ 5. Checkout                             │           │
   │ POST /cart/checkout                     │           │
   │ { cardType: "VISA", ... }               │           │
   ├────────>├───────>│          │           │           │
   │         │        │ Validate stock       │           │
   │         │        ├─────────>│           │           │
   │         │        │<─────────┤           │           │
   │         │        │          │           │           │
   │         │        │ Process Payment      │           │
   │         │        ├──────────────────────>│           │
   │         │        │<──────────────────────┤           │
   │         │        │          │           │           │
   │         │        │ Deduct Stock         │           │
   │         │        ├─────────>│           │           │
   │         │        │<─────────┤           │           │
   │         │        │          │           │           │
   │         │        │ Create Order         │           │
   │         │        │          │           │           │
   │         │        │ Create Tracking      │           │
   │         │        ├──────────────────────────────────>│
   │         │        │<──────────────────────────────────┤
   │         │        │          │           │           │
   │         │        │ Clear Cart           │           │
   │         │        │          │           │           │
   │ 200 OK  │<───────┤          │           │           │
   │ OrderDTO│        │          │           │           │
   │ + TrackingId     │          │           │           │
   │<────────┤        │          │           │           │
   │         │        │          │           │           │
   │ 6. Track Order                          │           │
   │ GET /api/tracking/order/1/enriched      │           │
   ├────────>├───────────────────────────────────────────>│
   │         │        │          │           │           │
   │         │        │  Get Product Details │           │
   │         │        │<─────────┤           │           │
   │         │        │          │           │           │
   │ 200 OK  │<───────────────────────────────────────────┤
   │ Enriched Tracking + Product Info        │           │
   │<────────┤        │          │           │           │
   │         │        │          │           │           │
   │ Order status: PENDING → PREPARING → SHIPPED → DELIVERED
   │         │        │          │           │           │
```

---

## Service Dependencies Matrix

```
┌────────────┬───────────┬────────┬─────────┬──────────┐
│ Service    │ Catalogue │ Panier │ Paiment │ Tracking │
├────────────┼───────────┼────────┼─────────┼──────────┤
│ Catalogue  │     -     │   No   │   No    │    No    │
├────────────┼───────────┼────────┼─────────┼──────────┤
│ Panier     │    YES    │   -    │   YES   │   YES    │
│            │ (stock,   │        │ (pay)   │ (create) │
│            │  price)   │        │         │          │
├────────────┼───────────┼────────┼─────────┼──────────┤
│ Paiment    │     No    │   No   │    -    │    No    │
├────────────┼───────────┼────────┼─────────┼──────────┤
│ Tracking   │    YES    │  YES   │   No    │    -     │
│            │ (product  │ (order)│         │          │
│            │  details) │        │         │          │
└────────────┴───────────┴────────┴─────────┴──────────┘

Legend:
- Catalogue: Independent (called by others)
- Panier: Orchestrator (calls 3 services)
- Paiment: Independent
- Tracking: Calls 2 services for enrichment/cancellation
```

---

## Data Flow Summary

### Add to Cart
```
User Input → Panier → Catalogue (validate) → Panier DB → Response
```

### Checkout
```
User Input → Panier → [
  Catalogue (validate stock)
  → Paiment (process payment)
  → Catalogue (deduct stock)
  → Panier DB (create order)
  → Tracking (create tracking)
] → Response
```

### Track Order
```
User Request → Tracking → [
  Tracking DB (get tracking)
  → Catalogue (get product details)
] → Enriched Response
```

### Cancel Order
```
User Request → Tracking → [
  Tracking DB (get tracking)
  → Panier (get order details)
  → Catalogue (restore stock)
  → Tracking DB (update status)
] → Response
```

---

**Last Updated:** January 19, 2026
