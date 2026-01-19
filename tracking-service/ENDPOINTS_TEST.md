# 🚀 Tracking Service - API Endpoints

Service running on: `http://localhost:8083`

---

## 📋 Available Endpoints

### 1️⃣ **Create new tracking** (POST)

**Endpoint:** `POST http://localhost:8083/api/tracking`

**Body (JSON):**
```json
{
  "orderId": 100,
  "productId": 5,
  "status": "PREPARING",
  "currentLocation": "Main Warehouse",
  "lastUpdate": "2026-01-19T14:00:00"
}
```

**Using curl:**
```bash
curl -X POST http://localhost:8083/api/tracking \
  -H "Content-Type: application/json" \
  -d "{\"orderId\":100,\"productId\":5,\"status\":\"PREPARING\",\"currentLocation\":\"Main Warehouse\"}"
```

---

### 2️⃣ **Get ALL trackings** (GET)

**Endpoint:** `GET http://localhost:8083/api/tracking`

**Using curl:**
```bash
curl http://localhost:8083/api/tracking
```

---

### 3️⃣ **Get tracking by ID** (GET)

**Endpoint:** `GET http://localhost:8083/api/tracking/{id}`

**Example:**
```bash
curl http://localhost:8083/api/tracking/1
```

---

### 4️⃣ **Get tracking by Order ID** (GET)

**Endpoint:** `GET http://localhost:8083/api/tracking/order/{orderId}`

**Example:**
```bash
curl http://localhost:8083/api/tracking/order/100
```

---

### 5️⃣ **Get trackings by Product ID** (GET)

**Endpoint:** `GET http://localhost:8083/api/tracking/product/{productId}`

**Example:**
```bash
curl http://localhost:8083/api/tracking/product/5
```

---

### 6️⃣ **Update tracking status** (PUT)

**Endpoint:** `PUT http://localhost:8083/api/tracking/{id}?status={status}&location={location}`

**Example:**
```bash
curl -X PUT "http://localhost:8083/api/tracking/1?status=SHIPPED&location=Madrid Distribution Center"
```

---

### 7️⃣ **Delete tracking** (DELETE)

**Endpoint:** `DELETE http://localhost:8083/api/tracking/{id}`

**Example:**
```bash
curl -X DELETE http://localhost:8083/api/tracking/1
```

---

## 🗄️ H2 Console (View database)

**URL:** http://localhost:8083/h2-console

**Configuration:**
- **JDBC URL:** `jdbc:h2:mem:trackingdb`
- **User Name:** `sa`
- **Password:** (leave empty)

**Query to view all records:**
```sql
SELECT * FROM TRACKING_INFO;
```

---

## 🔄 Typical usage flow

1. **Create tracking** when an order is placed
2. **Update status** as it changes (PREPARING → SHIPPED → IN_TRANSIT → DELIVERED)
3. **Query by orderId** to show customer where their order is
4. **Query by productId** to view shipping history of a product

---

## 📊 Recommended status values

- `PREPARING` - Order being prepared
- `SHIPPED` - Order shipped
- `IN_TRANSIT` - On the way
- `DELIVERED` - Delivered
- `RETURNED` - Returned
- `CANCELLED` - Cancelled
