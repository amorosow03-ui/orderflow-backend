# 🧾 OrderFlow API

Eine **Spring Boot REST API** zur Verwaltung von Bestellungen, Kunden und Produkten inklusive **Bestandsverwaltung**, **Status-Logik** und **Pagination**.

---

## 🚀 Features

- 👤 Customer Management (CRUD)
- 📦 Product Management (CRUD + Pagination + Filtering)
- 🧾 Order Management (Status-Workflow)
- 🧩 Order Items (inkl. Lagerbestand-Handling)
- 🔄 Automatische Bestandsanpassung
- ✅ Validierung & Exception Handling
- 📄 OpenAPI / Swagger Dokumentation
- 🧪 Unit Tests mit JUnit & Mockito

---

## 🏗️ Architektur

- **Controller Layer** – REST Endpoints
- **Service Layer** – Business Logic
- **Repository Layer** – Datenbankzugriff (Spring Data JPA)
- **DTO + Mapper Pattern** – saubere Trennung von API & Entity

---

## ⚙️ Technologien

- Java 17+
- Spring Boot
- Spring Data JPA
- Hibernate
- PostgreSQL / H2
- Maven
- JUnit 5
- Mockito
- Swagger / OpenAPI

---

## 📦 API Endpoints (Auszug)

### 👤 Customers
- POST /api/customers
- GET /api/customers
- GET /api/customers/{id}
- PUT /api/customers/{id}
- PATCH /api/customers/{id}
- DELETE /api/customers/{id}

### 📦 Products
- POST /api/products
- GET /api/products?page=0&size=10&sort=name
- GET /api/products/{id}
- GET /api/products/search?name=...&minPrice=...
- PUT /api/products/{id}
- PATCH /api/products/{id}
- DELETE /api/products/{id}

### 🧾 Orders
- POST /api/orders
- GET /api/orders
- GET /api/orders/{id}
- GET /api/orders/customer/{customerId}
- PUT /api/orders/{id}
- PATCH /api/orders/{id}
- DELETE /api/orders/{id}

### 🧩 Order Items
- POST /api/order-items
- GET /api/order-items
- GET /api/order-items/{id}
- GET /api/order-items/order/{orderId}
- DELETE /api/order-items/{id}

## 🔄 Order Status Workflow
CREATED → PAID → SHIPPED → CANCELLED

### Regeln:
- ❌ Keine Änderungen nach `SHIPPED`
- ❌ OrderItems nur bei `CREATED`
- 🔁 Stock wird zurückgegeben bei:
    - Order CANCELLED
    - OrderItem gelöscht

## 📄 Pagination Beispiel

```http
GET /api/products?page=0&size=5&sort=price,desc

Response:
{
  "content": [...],
  "page": 0,
  "size": 5,
  "totalElements": 20,
  "totalPages": 4,
  "last": false
}
```

## 🧪 Testing

### Enthaltene Tests:
- OrderServiceTest
- OrderItemServiceTest

### Technologien:
- JUnit 5
- Mockito