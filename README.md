# 🛒 E-Commerce Microservices Architecture

Multi-module Spring Boot project implementing a **fully integrated** microservices-based e-commerce system with catalogue, shopping cart, order tracking services, API gateway, and frontend application.

> **🎉 Integration Complete!** All microservices are now connected and communicating with each other. See [INTEGRATION_SUMMARY.md](INTEGRATION_SUMMARY.md) for detailed integration flow and testing guide.

---

## 📋 Table of Contents

- [Architecture Overview](#architecture-overview)
- [Microservices](#microservices)
- [Prerequisites](#prerequisites)
- [Getting Started](#getting-started)
- [Running the Services](#running-the-services)
- [Testing the APIs](#testing-the-apis)
- [Project Structure](#project-structure)
- [Technologies Used](#technologies-used)
- [Troubleshooting](#troubleshooting)

---

## 🏗️ Architecture Overview

This project follows a **microservices architecture** with an API Gateway pattern where:
- Each backend service runs independently on its own port
- Each service has its own H2 in-memory database
- All services expose REST APIs
- Services communicate via HTTP
- API Gateway routes external requests to appropriate services
- Frontend application consumes the APIs

```
                         ┌─────────────────┐
                         │    Frontend     │
                         │    (React)      │
                         └────────┬────────┘
                                  │
                         ┌────────▼────────┐
                         │  API Gateway    │
                         │   Port: 8080    │
                         └────────┬────────┘
                                  │
        ┌─────────────────────────┼─────────────────────────┐
        │                         │                         │
┌───────▼───────┐       ┌─────────▼────────┐     ┌────────▼────────┐
│   Catalogue   │       │     Panier       │     │    Tracking     │
│   Service     │◄──────│    Service       │     │    Service      │
│  Port: 8081   │       │   Port: 8082     │     │   Port: 8084    │
│               │       │                  │     │                 │
│ Products DB   │       │ Cart Items DB    │     │  Tracking DB    │
│(cataloguedb)  │       │  (panierdb)      │     │ (trackingdb)    │
└───────────────┘       └──────────────────┘     └─────────────────┘
```

---

## 🎯 Microservices

### 1. **Gateway Service** (Port: 8080)
API Gateway using Spring Cloud Gateway for routing requests to backend services.

**Routes:**
- `/api/products/**` → Catalogue Service (8081)
- `/cart/**` → Panier Service (8082)
- `/api/tracking/**` → Tracking Service (8084)

### 2. **Catalogue Service** (Port: 8081)
Manages the product catalogue with full CRUD operations and **stock management**.

**Endpoints:** `/api/products`
- `GET /api/products` - List all products
- `GET /api/products/{id}` - Get product by ID
- `POST /api/products` - Create product
- `PUT /api/products/{id}` - Update product
- `DELETE /api/products/{id}` - Delete product
- `GET /api/products/check-stock?productId={id}&quantity={qty}`** - Check stock availability
- `PUT /api/products/reduce-stock?productId={id}&quantity={qty}`** - Reduce stock (used by checkout)
- `PUT /api/products/restore-stock?productId={id}&quantity={qty}`** - Restore stock (on cancellation)

### 3. **Panier Service** (Port: 8082)
**Order orchestrator** that manages cart and coordinates the complete checkout workflow.

**Endpoints:** `/cart`
- `GET /cart` - View cart items
- `POST /cart/add?productId={id}&quantity={qty}` - Add to cart *(validates stock)*
- `DELETE /cart` - Clear cart
- `POST /cart/checkout`** - Complete checkout (creates order, reduces stock, creates tracking)
- `GET /cart/orders`** - Get all orders
- `GET /cart/orders/{id}`** - Get specific order details

**Integration:** Calls Catalogue (stock validation/reduction) and Tracking (order tracking creation)

### 4. **Tracking Service** (Port: 8084)
Tracks order status and provides **enriched tracking data** with product details.

**Endpoints:** `/api/tracking`
- `GET /api/tracking` - List all trackings
- `GET /api/tracking/{id}` - Get tracking by ID
- `GET /api/tracking/order/{orderId}` - Get tracking by order ID
- `GET /api/tracking/product/{productId}` - Get trackings by product ID
- `POST /api/tracking` - Create tracking
- `PUT /api/tracking/{id}` - Update tracking status
- `DELETE /api/tracking/{id}` - Delete tracking
- `GET /api/tracking/order/{orderId}/enriched`** - Get tracking + product details
- `PUT /api/tracking/order/{orderId}/cancel`** - Cancel order

**Integration:** Calls Catalogue to enrich tracking data with product information

📄 **Detailed API documentation:** [tracking-service/ENDPOINTS_TEST.md](tracking-service/ENDPOINTS_TEST.md)

### 5. **Frontend** (Port: 5173)
Web application built with Vite and React for the user interface.

---

## ⚙️ Prerequisites

Before running this project, make sure you have:

- **Java 17** or higher ([Download](https://adoptium.net/))
- **Maven 3.6+** (or use included Maven Wrapper)
- **Node.js 18+** and **npm** (for frontend) ([Download](https://nodejs.org/))
- **Git** (optional, for version control)

To verify your installations:
```bash
java -version
mvn -version
node -version
npm -version
```

---

## 🚀 Getting Started

### 1. Clone the repository
```bash
git clone <repository-url>
cd ArchitectureMicroServices
```

### 2. Clean and build the entire project

**⚠️ IMPORTANT:** After cloning or merging branches, always run a clean build to avoid dependency and compilation issues:

**On Windows (PowerShell/CMD):**
```bash
mvnw.cmd clean install
```

**On Linux/Mac:**
```bash
./mvnw clean install
```

**If you encounter build issues:**
```bash
# Force update all dependencies
mvnw.cmd clean install -U

# Skip tests if they're failing
mvnw.cmd clean install -DskipTests
```

This will:
- Clean previous build artifacts
- Download all dependencies
- Compile all backend services
- Run tests (if any)
- Package each service as a JAR file

### 3. Install frontend dependencies

```bash
cd frontend
npm install
cd ..
```

---

## 🏃 Running the Services

### Backend Services

Each service runs independently. Recommended startup order:

**Terminal 1 - Gateway Service (Start FIRST):**
```bash
cd gateway-service
java -jar target/gateway-service-0.0.1-SNAPSHOT.jar
```
✅ Gateway running on: http://localhost:8080

**Terminal 2 - Catalogue Service:**
```bash
cd catalogue-service
java -jar target/catalogue-service-0.0.1-SNAPSHOT.jar
```
✅ Service running on: http://localhost:8081

**Terminal 3 - Panier Service:**
```bash
cd panier-service
java -jar target/panier-service-0.0.1-SNAPSHOT.jar
```
✅ Service running on: http://localhost:8082

**Terminal 4 - Tracking Service:**
```bash
cd tracking-service
java -jar target/tracking-service-0.0.1-SNAPSHOT.jar
```
✅ Service running on: http://localhost:8084

**Terminal 5 - Frontend (Start LAST):**
```bash
cd frontend
npm run dev
```
✅ Frontend running on: http://localhost:5173

### Fall services are running, test them:

```bash
# Test Gateway Service
curl http://localhost:8080

# Test Catalogue Service (direct)
curl http://localhost:8081/api/products

# Test Catalogue Service (via Gateway)
curl http://localhost:8080/api/products

# Test Panier Service (direct)
curl http://localhost:8082/cart

# Test Panier Service (via Gateway)
curl http://localhost:8080/cart

# Test Tracking Service (direct)
curl http://localhost:8084/api/tracking

# Test Tracking Service (via Gateway)
curl http://localhost:8080/api/tracking
```

### Access Points

| Service | Direct URL | Via Gateway | Frontend |
|---------|-----------|-------------|----------|
| Gateway | - | http://localhost:8080 | - |
| Catalogue | http://localhost:8081/api/products | http://localhost:8080/api/products | - |
| Panier | http://localhost:8082/cart | http://localhost:8080/cart | - |
| Tracking | http://localhost:8084/api/tracking | http://localhost:8080/api/tracking | - |
| Frontend | - | - | http://localhost:5173 |

### H2 Database Consoles

Each backend

**Catalogue Service:**
```bash
cd catalogue-service
mvn spring-boot:run
```

**Panier Service:**
```bash
cd panier-service
mvn spring-boot:run
```

**Tracking Service:**
```bash
cd tracking-service
mvn spring-boot:run
```

---

## 🧪 Testing the APIs

### Quick Health Check

Once services are running, test them:

```bash
# Test Catalogue Service
curl http://localhost:8081/api/products

# Test Panier Service
curl http://localhost:8082/cart

# Test Tracking Service
curl http://localhost:8083/api/tracking
```

### H2 Database Consoles
gateway-service/                 # API Gateway (Spring Cloud Gateway)
│   ├── pom.xml
│   └── src/
│       └── main/java/com/example/gatewayservice/
│           └── GatewayServiceApplication.java
│
├── catalogue-service/               # Product catalogue microservice
│   ├── pom.xml
│   └── src/
│       ├── main/java/com/example/catalogue/
│       │   ├── CatalogueApplication.java
│       │   ├── controller/ProductController.java
│       │   ├── entity/Product.java
│       │   ├── repository/ProductRepository.java
│       │   └── service/ProductService.java
│       └── resources/application.properties
│
├── panier-service/                  # Shopping cart microservice
│   ├── pom.xml
│   └── src/
│       ├── main/java/com/example/panier/
│       │   ├── PanierApplication.java
│       │   ├── controller/CartController.java
│       │   ├── entity/CartItem.java
│       │   ├── repository/CartItemRepository.java
│       │   └── service/CartService.java
│       └── resources/application.properties
│
├── tracking-service/                # Order tracking microservice
│   ├── pom.xml
│   ├── ENDPOINTS_TEST.md            # API documentation
│   └── src/
│       ├── main/java/com/example/tracking/
│       │   ├── TrackingApplication.java
│       │   ├── controller/TrackingController.java
│       │   ├── entity/TrackingInfo.java
│       │   ├── repository/TrackingRepository.java
│       │   └── service/TrackingService.java
│       └── resources/application.properties
│
└── frontend/                        # Frontend application (React)
    ├── package.json
    ├── vite.config.ts
    ├── src/
    └── public/
├── panier-service/                  # Shopping cart microservice
│   ├── pom.xml
│   └── src/
│       ├── main/java/com/example/panier/
│       │   ├── PanierApplication.java
│       │   ├── controller/CartController.java
│       │   ├── entity/CartItem.java
│   ️ Technologies Used

### Backend
| Technology | Version | Purpose |
|------------|---------|---------|
| **Java** | 17 | Programming language |
| **Spring Boot** | 3.4.1 | Application framework |
| **Spring Cloud Gateway** | 2024.0.0 | API Gateway |
| **Spring Data JPA** | 3.4.1 | Database access layer |
| **Spring Web** | 3.4.1 | REST API creation |
| **H2 Database** | Runtime | In-memory database |
| **Hibernate** | 6.6.4 | ORM (Object-Relational Mapping) |
| **Maven** | 3.x | Build tool & dependency management |
| **Lombok** | Latest | Code generation (optional) |

### Frontend
| Technology | Purpose |
|------------|---------|
| **Vite** | Build tool and dev server |
| **React** | Frontend framework |
| **TypeScript** | Type-safe JavaScript |
| **Tailwind CSS** | Utility-first CSS framework |
| **Node.js** | JavaScript runtime |

---

## 🔧 Troubleshooting

### After Git Merge - Build Issues

**Problem:** After merging branches, you get compilation errors or dependency issues.

**Solution:** Always clean and rebuild:
```bash
# Windows
mvnw.cmd clean install -U

# Linux/Mac
./mvnw clean install -U
```

The `-U` flag forces Maven to update all dependencies.

### Port already in use
If you get an error like "Port 8081 is already in use":

**Windows:**
```bash
# Find process using the port
netstat -ano | findstr :8081

# Kill the process (replace PID)
taskkill /PID <process-id> /F
```

**Linux/Mac:**
```bash
# Find and kill process
lsof -ti:8081 | xargs kill -9
```

Or change the port in `application.properties`:
```properties
server.port=8084
```

### Build failures
If the build fails:
```bash
# Clean everything and rebuild
mvnw.cmd clean install -U

# Skip tests if they're failing
mvnw.cmd clean install -DskipTests

# Build specific module only
cd catalogue-service
mvn clean install
```

### Maven dependency conflicts
```bash
# Clear Maven cache
rm -rf ~/.m2/repository  # Linux/Mac
rmdir /s %USERPROFILE%\.m2\repository  # Windows

# Rebuild
mvnw.cmd clean install
```

### IDE not recognizing classes
If your IDE shows errors but the build works:

**VS Code:**
1. Press `F1` → "Java: Clean Java Language Server Workspace"
2. Select "Restart and delete"
3. Reload window

**IntelliJ IDEA:**
1. File → Invalidate Caches
2. Right-click `pom.xml` → Maven → Reload Project

### Frontend won't start
```bash
cd frontend
rm -rf node_modules package-lock.json
npm install
npm run dev
```

### Gateway can't reach services
Make sure all backend services are running BEFORE starting the gateway.

**Check services are up:**
```bash
curl http://localhost:8081/api/products
curl http://localhost:8082/cart
curl http://localhost:8084/api/tracking
```ion | Purpose |
|------------|---------|---------|
| **Java** | 17 | Programming language |
| **Spring Boot** | 3.4.1 | Application framework |
| **Spring Data JPA** | 3.4.1 | Database access layer |
| **Spring Web** | 3.4.1 | REST API creation |
| **H2 Database** | Runtime | In-memory database |
| **Hibernate** | 6.6.4 | ORM (Object-Relational Mapping) |
| **Maven** | 3.x | Build tool & dependency management |
| **Lombok** | Latest | Code generation (optional) |

---

## 🔧 Common Issues & Solutions

### Port already in use
If you get an error like "Port 8081 is already in use":
1. Check if another service is running: `netstat -ano | findstr :8081` (Windows)
2. Kill the process or change the port in `application.properties`

### Build failures
If the build fails:
```bash
# Clean and rebuild
mvnw.cmd clean install -U

# Skip tests if needed
mvnw.cmd clean install -DskipTests
```

### IDE not recognizing classes
If your IDE shows errors:
1. Reload the Maven project
2. In VS Code: `F1` → "Java: Clean Java Language Server Workspace"
3. Restart your IDE

---

## 📚 Additional Resources

- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Spring Data JPA Guide](https://spring.io/guides/gs/accessing-data-jpa/)
- [REST API Best Practices](https://restfulapi.net/)

---

## 👥 Contributors

- Sebastian Ruiz
- Jose Villa
- Javier Vargas

---

## 📝 License

This project is for educational purposes.

---

**Happy Coding! 🚀**
