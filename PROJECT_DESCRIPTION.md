# E-Commerce Microservices Architecture - Project Description

## 📝 Project Overview

This project implements a complete **e-commerce system** using **microservices architecture** with Spring Boot and Docker. The system allows users to browse products, manage a shopping cart, process payments, and track orders.

---

## 🏗️ Architecture Description

### Microservices Pattern

The application follows a **distributed microservices architecture** where each service:
- Runs independently in its own container
- Has its own dedicated database (H2 in-memory)
- Exposes REST APIs for communication
- Can be developed, deployed, and scaled independently

### Services Communication

```
┌──────────────────────────────────────────────────────────────┐
│                     Client (Browser)                          │
└──────────────────────┬───────────────────────────────────────┘
                       │
                       ▼
┌──────────────────────────────────────────────────────────────┐
│                  Frontend (React/Vite)                        │
│                      Port: 80                                 │
└──────────────────────┬───────────────────────────────────────┘
                       │
                       ▼
┌──────────────────────────────────────────────────────────────┐
│            API Gateway (Spring Cloud Gateway)                 │
│                      Port: 8080                               │
│  - Routes requests to appropriate microservices               │
│  - Handles CORS                                               │
│  - Single entry point for all backend services                │
└────┬────────────┬────────────┬────────────┬──────────────────┘
     │            │            │            │
     ▼            ▼            ▼            ▼
┌─────────┐  ┌─────────┐  ┌─────────┐  ┌─────────┐
│Catalogue│  │ Panier  │  │ Paiment │  │Tracking │
│Service  │  │ Service │  │ Service │  │ Service │
│         │  │         │  │         │  │         │
│Port 8081│  │Port 8082│  │Port 8083│  │Port 8084│
└─────────┘  └─────────┘  └─────────┘  └─────────┘
     │            │            │            │
     ▼            ▼            ▼            ▼
┌─────────┐  ┌─────────┐  ┌─────────┐  ┌─────────┐
│   H2    │  │   H2    │  │   H2    │  │   H2    │
│  DB     │  │  DB     │  │  DB     │  │  DB     │
└─────────┘  └─────────┘  └─────────┘  └─────────┘
```

### Inter-Service Communication

Services communicate via **synchronous HTTP REST calls**:

1. **Panier → Catalogue**: Stock validation and reduction during checkout
2. **Panier → Tracking**: Order tracking creation after successful checkout
3. **Tracking → Catalogue**: Product information enrichment for tracking data

---

## 🎯 Microservices Details

### 1. Gateway Service (Port 8080)
**Technology**: Spring Cloud Gateway  
**Purpose**: API Gateway and routing layer

**Key Features**:
- Single entry point for all API requests
- Route management and load balancing
- CORS configuration for frontend
- Request/response filtering

**Routes**:
- `/api/products/**` → Catalogue Service
- `/cart/**` → Panier Service
- `/api/payments/**` → Paiment Service
- `/api/tracking/**` → Tracking Service

---

### 2. Catalogue Service (Port 8081)
**Technology**: Spring Boot + Spring Data JPA  
**Purpose**: Product catalogue and inventory management

**Key Features**:
- Product CRUD operations
- Stock management (check, reduce, restore)
- Product search and filtering
- Real-time stock validation

**Database Schema**:
```
Product {
  id: Long (PK)
  name: String
  description: String
  price: Double
  quantity: Integer (stock level)
}
```

**Business Logic**:
- Validates stock availability before purchases
- Automatically reduces stock on successful checkout
- Restores stock when orders are cancelled

---

### 3. Panier Service (Port 8082)
**Technology**: Spring Boot + RestTemplate  
**Purpose**: Shopping cart and order orchestration

**Key Features**:
- Cart management (add, view, clear)
- **Order orchestration**: Coordinates complete purchase workflow
- Stock validation integration
- Order history tracking

**Database Schema**:
```
CartItem {
  id: Long (PK)
  productId: Long (FK)
  productName: String
  quantity: Integer
  price: Double
}

Order {
  id: Long (PK)
  customerName: String
  customerEmail: String
  shippingAddress: String
  totalAmount: Double
  status: String (PENDING, CONFIRMED, CANCELLED)
  orderDate: Timestamp
}

OrderItem {
  id: Long (PK)
  orderId: Long (FK)
  productId: Long
  productName: String
  quantity: Integer
  price: Double
}
```

**Checkout Workflow** (7 steps):
1. Validate cart is not empty
2. Check stock availability (calls Catalogue)
3. Create Order entity
4. Convert CartItems to OrderItems
5. Reduce stock (calls Catalogue)
6. Create tracking record (calls Tracking)
7. Clear cart

---

### 4. Paiment Service (Port 8083)
**Technology**: Spring Boot + Spring Data JPA  
**Purpose**: Payment processing and transaction management

**Key Features**:
- Payment record creation
- Payment approval/processing
- Transaction history
- Card number lookup

**Database Schema**:
```
PaimentItem {
  id: Long (PK)
  cardNumber: String
  amount: Double
  approved: Boolean
  createdAt: Timestamp
}
```

**Business Logic**:
- Records payment transactions
- Processes payment approval
- Maintains payment history

---

### 5. Tracking Service (Port 8084)
**Technology**: Spring Boot + RestTemplate  
**Purpose**: Order tracking and status management

**Key Features**:
- Order tracking creation
- Status updates
- **Data enrichment**: Combines tracking + product info
- Order cancellation with stock restoration

**Database Schema**:
```
TrackingInfo {
  id: Long (PK)
  orderId: Long (Unique)
  productId: Long
  status: String (PENDING, PROCESSING, SHIPPED, DELIVERED, CANCELLED)
  estimatedDelivery: Date
  createdAt: Timestamp
  updatedAt: Timestamp
}
```

**Business Logic**:
- Creates tracking record when order is placed
- Enriches tracking data with product details from Catalogue
- Updates order status throughout delivery lifecycle
- Handles order cancellations

---

### 6. Frontend (Port 80)
**Technology**: React + TypeScript + Vite + Tailwind CSS  
**Purpose**: User interface

**Key Features**:
- Product catalogue browsing
- Shopping cart management
- Checkout process
- Order tracking
- Responsive design

**Components**:
- `StoreContent`: Product display and cart management
- `CartSidebar`: Shopping cart UI
- `Checkout`: Checkout form
- `OrderTracking`: Order status display

---

## 🔄 Complete User Flow

### 1. Browse Products
```
User → Frontend → Gateway → Catalogue Service
```
User views available products with prices and stock levels

### 2. Add to Cart
```
User → Frontend → Gateway → Panier Service → Catalogue Service (stock check)
```
System validates stock before adding to cart

### 3. Checkout
```
User → Frontend → Gateway → Panier Service
         ↓
    Order Created
         ↓
    Catalogue Service (reduce stock)
         ↓
    Tracking Service (create tracking)
         ↓
    Response to User
```
Complete purchase workflow with multiple service coordination

### 4. Track Order
```
User → Frontend → Gateway → Tracking Service → Catalogue Service (enrich data)
```
User can track order status with detailed product information

---

## 🛠️ Technologies Used

### Backend Stack
- **Java 17**: Modern LTS version
- **Spring Boot 3.4.1**: Application framework
- **Spring Cloud Gateway 2024.0.0**: API Gateway
- **Spring Data JPA**: Database access
- **H2 Database**: In-memory database for development
- **Maven**: Build and dependency management
- **Docker**: Containerization

### Frontend Stack
- **React 18**: UI library
- **TypeScript**: Type-safe JavaScript
- **Vite**: Build tool
- **Tailwind CSS**: Styling
- **Axios**: HTTP client

### DevOps
- **Docker Compose**: Multi-container orchestration
- **Git**: Version control

---

## 📦 Deployment Architecture

### Docker Containers
Each service runs in its own isolated Docker container:

```yaml
services:
  - gateway-service      (Java 17 Alpine)
  - catalogue-service    (Java 17 Alpine)
  - panier-service       (Java 17 Alpine)
  - paiment-service      (Java 17 Alpine)
  - tracking-service     (Java 17 Alpine)
  - frontend             (Nginx Alpine)
```

### Networking
All containers communicate via a Docker bridge network:
- **Network Name**: `microservices-network`
- **DNS Resolution**: Services resolve each other by container name
- **Isolation**: Network is isolated from external networks

---

## ✅ Key Features Implemented

### Microservices Principles
✅ Service independence and autonomy  
✅ Single responsibility per service  
✅ Independent deployment  
✅ Decentralized data management  
✅ API-based communication  

### Integration Patterns
✅ API Gateway pattern  
✅ Service orchestration (Panier as orchestrator)  
✅ Data enrichment (Tracking enriches with Catalogue data)  
✅ Saga pattern (distributed transaction handling)  

### Best Practices
✅ RESTful API design  
✅ Error handling and validation  
✅ CORS configuration  
✅ Database per service  
✅ Containerization  
✅ Environment configuration  

---

## 🧪 Testing Strategy

### Manual Testing
- REST API testing with curl/Postman
- Integration testing via complete user flows
- UI testing in browser

### Test Coverage
- Product CRUD operations
- Stock management
- Cart operations
- Checkout workflow
- Order tracking
- Payment processing

See [TESTING_GUIDE.md](TESTING_GUIDE.md) for detailed test scenarios.

---

## 📊 Project Statistics

- **Total Services**: 6 (5 backend + 1 frontend)
- **Total Endpoints**: ~35 REST API endpoints
- **Programming Languages**: Java, TypeScript
- **Lines of Code**: ~3000+ (estimated)
- **Database Tables**: 6 (Product, CartItem, Order, OrderItem, PaimentItem, TrackingInfo)

---

## 👥 Team

- **Sebastian Ruiz**
- **Jose Villa**
- **Javier Vargas**

**Institution**: IMT Nord Europe  
**Course**: Architecture Microservices  
**Year**: 2024-2025

---

## 📝 Conclusion

This project demonstrates a complete understanding and implementation of microservices architecture principles, including:

- Service decomposition and domain modeling
- Inter-service communication patterns
- API Gateway implementation
- Distributed data management
- Container orchestration with Docker
- Full-stack development (Backend + Frontend)

The system is production-ready in terms of architecture and can be easily extended with additional features such as:
- Authentication and authorization
- Message queuing (Kafka, RabbitMQ)
- Service discovery (Eureka, Consul)
- Circuit breakers (Resilience4j)
- Centralized logging and monitoring
- Database migration to PostgreSQL/MySQL

---

**Note**: This is an educational project demonstrating microservices concepts and is not intended for production use without additional security and scalability enhancements.
