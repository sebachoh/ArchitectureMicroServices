# 🛒 E-Commerce Microservices Architecture

Multi-module Spring Boot project implementing a microservices-based e-commerce system with catalogue, shopping cart, and order tracking services.

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

---

## 🏗️ Architecture Overview

This project follows a **microservices architecture** where each service:
- Runs independently on its own port
- Has its own H2 in-memory database
- Exposes REST APIs
- Can communicate with other services via HTTP

```
┌─────────────────────┐   ┌─────────────────────┐   ┌─────────────────────┐
│  Catalogue Service  │   │   Panier Service    │   │  Tracking Service   │
│     Port: 8081      │   │     Port: 8082      │   │     Port: 8083      │
│                     │   │                     │   │                     │
│   Products DB       │◄──│   Cart Items DB     │   │   Tracking DB       │
│   (cataloguedb)     │   │   (panierdb)        │   │   (trackingdb)      │
└─────────────────────┘   └─────────────────────┘   └─────────────────────┘
```

---

## 🎯 Microservices

### 1. **Catalogue Service** (Port: 8081)
Manages the product catalogue with full CRUD operations.

**Endpoints:** `/api/products`
- `GET /api/products` - List all products
- `GET /api/products/{id}` - Get product by ID
- `POST /api/products` - Create product
- `PUT /api/products/{id}` - Update product
- `DELETE /api/products/{id}` - Delete product

### 2. **Panier Service** (Port: 8082)
Manages shopping cart functionality. Communicates with Catalogue Service.

**Endpoints:** `/cart`
- `GET /cart` - View cart items
- `POST /cart/add?productId={id}&quantity={qty}` - Add to cart
- `DELETE /cart` - Clear cart

### 3. **Tracking Service** (Port: 8083)
Tracks order status and location throughout delivery process.

**Endpoints:** `/api/tracking`
- `GET /api/tracking` - List all trackings
- `GET /api/tracking/{id}` - Get tracking by ID
- `GET /api/tracking/order/{orderId}` - Get tracking by order ID
- `GET /api/tracking/product/{productId}` - Get trackings by product ID
- `POST /api/tracking` - Create tracking
- `PUT /api/tracking/{id}` - Update tracking status
- `DELETE /api/tracking/{id}` - Delete tracking

📄 **Detailed API documentation:** [tracking-service/ENDPOINTS_TEST.md](tracking-service/ENDPOINTS_TEST.md)

---

## ⚙️ Prerequisites

Before running this project, make sure you have:

- **Java 17** or higher ([Download](https://adoptium.net/))
- **Maven 3.6+** (or use included Maven Wrapper)
- **Git** (optional, for version control)

To verify your Java version:
```bash
java -version
```

---

## 🚀 Getting Started

### 1. Clone the repository
```bash
git clone <repository-url>
cd ArchitectureMicroServices
```

### 2. Build the entire project
Build all microservices at once:

**On Windows (PowerShell/CMD):**
```bash
mvnw.cmd clean install
```

**On Linux/Mac:**
```bash
./mvnw clean install
```

This will:
- Download all dependencies
- Compile all services
- Run tests (if any)
- Package each service as a JAR file

---

## 🏃 Running the Services

Each service runs independently. You can start them in any order, but if **panier-service** needs to communicate with **catalogue-service**, start catalogue first.

### Option A: Run from JAR files (Recommended)

**Terminal 1 - Catalogue Service:**
```bash
cd catalogue-service
java -jar target/catalogue-service-0.0.1-SNAPSHOT.jar
```
✅ Service running on: http://localhost:8081

**Terminal 2 - Panier Service:**
```bash
cd panier-service
java -jar target/panier-service-0.0.1-SNAPSHOT.jar
```
✅ Service running on: http://localhost:8082

**Terminal 3 - Tracking Service:**
```bash
cd tracking-service
java -jar target/tracking-service-0.0.1-SNAPSHOT.jar
```
✅ Service running on: http://localhost:8083

### Option B: Run with Maven

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

Each service has an H2 console to view the database:

| Service | H2 Console URL | JDBC URL | Username |
|---------|---------------|----------|----------|
| Catalogue | http://localhost:8081/h2-console | `jdbc:h2:mem:cataloguedb` | `sa` |
| Panier | http://localhost:8082/h2-console | `jdbc:h2:mem:panierdb` | `sa` |
| Tracking | http://localhost:8083/h2-console | `jdbc:h2:mem:trackingdb` | `sa` |

**Password:** (leave empty)

---

## 📁 Project Structure

```
ArchitectureMicroServices/
│
├── pom.xml                          # Parent POM (multi-module aggregator)
├── mvnw, mvnw.cmd                   # Maven Wrapper scripts
├── README.md                        # This file
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
└── tracking-service/                # Order tracking microservice
    ├── pom.xml
    ├── ENDPOINTS_TEST.md            # API documentation
    └── src/
        ├── main/java/com/example/tracking/
        │   ├── TrackingApplication.java
        │   ├── controller/TrackingController.java
        │   ├── entity/TrackingInfo.java
        │   ├── repository/TrackingRepository.java
        │   └── service/TrackingService.java
        └── resources/application.properties
```

---

## 🛠️ Technologies Used

| Technology | Version | Purpose |
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

- Your Name - Initial development

---

## 📝 License

This project is for educational purposes.

---

**Happy Coding! 🚀**
