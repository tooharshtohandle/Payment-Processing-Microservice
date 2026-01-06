# 🟦 SecurePay — Spring Boot Payment Gateway (MVP)

A mini production-style payment gateway built using **Java Spring Boot**, featuring secure merchant authentication, payment processing, refunds, and webhooks with retry logic.

---

## 🚀 Features

### Core Payment Flow

- Create Payment
- Authorize Payment
- Capture Payment
- Fetch Payment Status
- Full & Partial Refunds

### Security

- API Key–based authentication (`X-API-KEY`)
- Per-merchant isolation

### Webhooks

- Events: `PAYMENT_AUTHORIZED`, `PAYMENT_CAPTURED`, `REFUND_SUCCESS`
- Persistent webhook event queue
- Scheduled webhook delivery with retries & exponential backoff
- Admin endpoint to inspect webhook events

### Developer Experience

- H2 in-memory database
- Easy to test with curl or Postman
- Clean layered architecture

---

## 🏗 Tech Stack

- Java 17
- Spring Boot 3
- Spring Web
- Spring Validation
- Spring Data JPA
- H2 Database
- Lombok
- RestTemplate
- Scheduled Tasks

---

## ▶️ Running the Project (End-to-End)

### 1. Clone the project

```bash
git clone <your-repo-url>
cd securepay-payment-gateway
```

### 2. Run the application

#### Option A — VS Code

Use Spring Boot Dashboard → Run

#### Option B — Terminal

```bash
mvn spring-boot:run
```

### 3. Copy the Seeded Test Merchant API Key

When the app starts, it prints:

```
Seeded Test Merchant:
  API KEY: TEST_API_KEY_xxx
```

Use this key in all API calls.

---

## 🧪 Complete API Testing Guide (curl commands)

### 1️⃣ Create Payment

```cmd
curl -X POST "http://localhost:8080/api/payments" -H "Content-Type: application/json" -H "X-API-KEY: YOUR_API_KEY" -d "{\"externalReferenceId\":\"ORDER_1\",\"amount\":1500.00,\"currency\":\"INR\",\"paymentMethod\":\"CARD\",\"returnUrl\":\"https://merchant.example/return\"}"
```

Copy the `transactionId` from the response.

---

### 2️⃣ Get Payment

```cmd
curl -X GET "http://localhost:8080/api/payments/YOUR_TXN_ID" -H "X-API-KEY: YOUR_API_KEY"
```

---

### 3️⃣ Authorize Payment

```cmd
curl -X POST "http://localhost:8080/api/payments/YOUR_TXN_ID/authorize" -H "X-API-KEY: YOUR_API_KEY"
```

---

### 4️⃣ Capture Payment

```cmd
curl -X POST "http://localhost:8080/api/payments/YOUR_TXN_ID/capture" -H "X-API-KEY: YOUR_API_KEY"
```

---

### 5️⃣ Refund Payment

```cmd
curl -X POST "http://localhost:8080/api/payments/YOUR_TXN_ID/refunds" -H "Content-Type: application/json" -H "X-API-KEY: YOUR_API_KEY" -d "{\"refundReferenceId\":\"REF_1\",\"amount\":1500.00}"
```

---

### 6️⃣ View Webhook Events

```cmd
curl -X GET "http://localhost:8080/admin/webhooks" -H "X-API-KEY: YOUR_API_KEY"
```

---

## 🌐 Testing Webhook Delivery (Optional)

### Option A — Webhook.site (Recommended)

1. Visit https://webhook.site
2. Copy your unique URL
3. Update merchant callback URL in H2 console:

Open `http://localhost:8080/h2-console`  
JDBC URL: `jdbc:h2:mem:testdb`

Run:

```sql
UPDATE merchants SET callback_url='https://webhook.site/your-id' WHERE name='Test Merchant';
```

Webhook delivery occurs automatically every 15 seconds.

---

## 🧱 Project Structure

```
src/main/java/com/securepay/payment_gateway
├── config/ApiKeyAuthFilter.java
├── controller/
│   ├── PaymentController.java
│   └── AdminController.java
├── dto/
├── entity/
│   ├── Merchant.java
│   ├── PaymentTransaction.java
│   ├── Refund.java
│   └── WebhookEvent.java
├── repository/
├── service/
│   ├── PaymentService.java
│   └── WebhookService.java
└── PaymentGatewayApplication.java
```

---

## 🧹 Resetting the Database

This project uses H2 in-memory DB.  
Restart the app to reset the database and generate a fresh merchant API key.

---

## 📦 Optional: Switch to Postgres

Change `application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/securepay
    username: postgres
    password: yourpass
  jpa:
    hibernate:
      ddl-auto: update
```

---

## 👤 Author

Harsh Singh  
SecurePay — Payment Gateway Project using Java Spring Boot

---
