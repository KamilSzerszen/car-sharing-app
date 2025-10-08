# 🚗 Car Sharing App

A **Spring Boot application** simulating a car sharing platform with features such as **user authentication, car rental management, payments via Stripe, and Telegram notifications**.  
This project was created as part of my portfolio to showcase backend development skills in **Java**.  

---

### 🏗️ Architecture Diagrams

**High-level architecture:**

![Screenshot](https://drive.google.com/uc?export=view&id=15fccth8FS2NVRy46A0CHnVbMe4fvc0RA)

---

## ✨ Features
- 🔐 **User authentication & roles** (JWT + Spring Security)  
  - Users can register and log in.  
  - Roles: `CUSTOMER`, `MANAGER`. 
  - Special testing case: a user with the MANAGER role who is already in the database for testing purposes.
    > login: manager@test.com \
    > password: 123456

- 🚘 **Car management**  
  - CRUD operations for cars.  
  - Manager endpoints to manage availability.  

- 📅 **Car rental workflow**  
  - Users can rent a car.  
  - Manager/Admin can approve or reject requests.  
  - Rentals are linked with payments.  

- 💳 **Payments (Stripe integration)**  
  - Stripe Checkout Session for rental payments.  
  - For demo purposes:  
    - After payment, Stripe redirects to `/success` or `/cancel`.  
    - Since Stripe does not send IDs back to localhost, you need to manually append `?id=PAYMENT_ID`.  
    - In production, Stripe **webhooks** would be used to update payment status automatically.  
  - This simplified flow makes it possible to test payments locally without exposing public endpoints.
<br>

   > ⚠️ **Note:** These endpoints are **for testing/demo purposes only**.  
   > In production, Stripe webhooks should automatically update the payment status.  
   > For demo purposes, you need to manually append `?id=PAYMENT_ID` to the URL after Stripe redirects,  
   > because Stripe does not send the payment ID or any data to localhost.  
   > For your convenience, it has been implemented this way 🙂
<br>
- 📲 **Telegram notifications**  
  - When a new reservation is created, a notification is sent to a Telegram bot.  
  - Example screenshot:  
    `![telegram.png](docs/telegram.png)`  

- 📖 **API documentation**  
  - Available via **Swagger**:  
    ```
    http://localhost:8081/swagger-ui/index.html#/
    ```
    (port can be configured via `DOCKER_APP_PORT` in `.env`)  
  - Includes an **Authorize** button to authenticate with JWT tokens.  

- 🛠 **DevOps & Quality**  
  - ✅ Multi-stage Docker build.  
  - ✅ Docker Compose with `.env` configuration.  
  - ✅ CI/CD pipeline with GitHub Actions.  
  - ✅ Test coverage **74% (JaCoCo)**.  
  - ✅ Global error handling (`@ControllerAdvice`).  

---

## 🗂️ Project structure

```text
src/
└── main/java/com/example/carsharing
    ├── config/        Security & OpenAPI configs
    ├── controller/    REST controllers (Cars, Rentals, Payments, Users)
    ├── dto/           DTOs for requests and responses
    ├── mapper/        MapStruct mappers
    ├── model/         JPA entities
    ├── repository/    JPA repositories
    ├── security/      JWT and auth logic
    └── service/       Business logic
```

## ⚙️ Getting started

### 1. Clone repository
```bash
git clone https://github.com/<your-username>/car-sharing-app.git
cd car-sharing-app
```

### 2. Configure environment

Create a `.env` file (use `.env.example` as a template):

```env
DOCKER_DB_PORT=3307
DOCKER_APP_PORT=8081
MYSQL_DATABASE=carsharing
MYSQL_USER=root
MYSQL_PASSWORD=secret
STRIPE_SECRET_KEY=sk_test_xxx
STRIPE_PUBLIC_KEY=pk_test_xxx
TELEGRAM_BOT_TOKEN=your_token
TELEGRAM_CHAT_ID=your_chat_id
JWT_SECRET=your_jwt_secret
```

### 3. Run with Docker

```bash
docker compose up --build
```

## 🚀 App Overview

The application will be available at:

- **API:** [http://localhost:8081](http://localhost:8081)  
- **Swagger UI:** [http://localhost:8081/swagger-ui/index.html#/](http://localhost:8081/swagger-ui/index.html#/)

---

### 🔑 Authentication

- **Register:** `POST /auth/register`  
- **Login:** `POST /auth/login` → returns JWT token  

Use the **Authorize** button in Swagger to provide your token.

**Special testing account:**

> Registering with `manager@example.com` automatically assigns the `MANAGER` role.  
> Intended only for demo/testing.

---

### 💳 Payments (demo flow)

Payments are integrated with **Stripe Checkout**:

1. User creates a rental.  
2. Stripe Checkout Session starts.  
3. After completion:  
   - `/success` → payment approved  
   - `/cancel` → payment canceled  

**⚠️ For demo/testing:**  
Append `?id=PAYMENT_ID` manually to the redirect URL.  
In production, Stripe webhooks would update payment status automatically.  

> This approach keeps local testing simple while still demonstrating integration with an external payment system.

---

### 📲 Telegram Notifications

- Each new rental triggers a Telegram message.  
- Notifications are sent via the **Telegram Bot API**.

![Screenshot](https://drive.google.com/uc?export=view&id=1S0hKXUlZ14wBRAebI29o_7NspEMdfzkJ)

---

### 🔐 Security (Spring Security + JWT)

**Public endpoints:**

- `/auth/**` (login & register)  
- `/swagger-ui/**`, `/v3/api-docs/**` (API docs)
- `/cars/all` (all cars)

**Protected endpoints:**

- `/cars/**`, `/rentals/**`, `/payments/**` (JWT required)  

---

### 🧪 Tests

- Implemented with **JUnit 5**, **Mockito**, and **Testcontainers**  
- **Integration tests** run inside Docker containers (e.g., MySQL)  
- Ensures realistic testing environment close to production  
- **Coverage:** 74% (JaCoCo)  

⚠️ Docker must be running to execute tests locally.

![Screenshot](https://drive.google.com/uc?export=view&id=1IxntDiKI0O_73Is0quY0CxkrWYObaO4y)

---

### 📌 Tech Stack

- **Backend:** Java 17, Spring Boot, Spring Data JPA, Spring Security, MapStruct, Lombok  
- **Database:** MySQL, H2 (for tests via Testcontainers)  
- **Payments:** Stripe API  
- **Notifications:** Telegram Bot API  
- **Docs:** Swagger (SpringDoc OpenAPI)  
- **DevOps:** Docker (multi-stage build), Docker Compose, GitHub Actions (CI/CD)  
- **Testing:** JUnit 5, Mockito, Testcontainers, JaCoCo (74% coverage)  

---

### 🚀 Future Improvements

- Replace demo Stripe success/cancel flow with real webhooks  
- Extend rental pricing logic (per hour, discounts)  

💡 This project is not just a coding exercise — it’s designed to look and feel like a **real-world backend system**, with authentication, roles, payments, and integration with external services.

