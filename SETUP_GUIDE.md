# Enterprise Java Full Stack (JFS) Setup Guide
**Project**: Smart Resume Builder Platform

## 1. Prerequisites
- Java 17+
- Maven 3.8+
- Node.js 18+
- MySQL 8.0+

## 2. Infrastructure Setup (MySQL)
The system leverages `ddl-auto: update`, so schemas are built dynamically. You only need to create the core databases.
Run the following SQL commands in your MySQL client:
```sql
CREATE DATABASE auth_db;
CREATE DATABASE resume_db;
CREATE DATABASE user_db;
CREATE DATABASE notification_db;
CREATE DATABASE payment_db;
CREATE DATABASE ai_db;
```

## 3. Environment Variables
Verify your `application.yml` files, specifically ensuring:
- **Database Credentials**: Update `spring.datasource.password` in each microservice to match your local MySQL root password.
- **JWT Secret**: The `auth-service`, `resume-service`, and `api-gateway` MUST all share the same 256-bit Hex secret key: `5367566B59703373367639792F423F4528482B4D6251655468576D5A71347437`

## 4. Boot Sequence (Strict Ordering)
To prevent startup failures, microservices must be booted in this exact order:
1. `eureka-server` (Port 8761)
2. `api-gateway` (Port 8084)
3. `auth-service` (Port 8085)
4. `resume-service` (Port 8086)
5. `user-service`, `payment-service`, `notification-service`, `ai-service`

## 5. Running the Frontend
1. Open a new terminal and navigate to `frontend/`
2. Run `npm install`
3. Run `npm run dev`
4. Access the UI at `http://localhost:3000` (or `localhost:5173`)

## 6. Enterprise Features Included
- **OTP MFA System**: `auth-service` generates secure 6-digit OTPs, hashes them with BCrypt, stores them with 5-minute expiries, and routes them via `notification-service`.
- **API Gateway**: Handles CORS policy and JWT Authentication filtering.
- **Service Discovery**: Netflix Eureka Registry.
- **Role-Based Access Control**: Centralized via API Gateway & JWT Claims.
- **Global Error Handling**: Standardized `ApiResponse` payload.

## 7. Testing OTP & Login
1. Go to the Registration Page on the frontend.
2. Register a new account.
3. Go to the Login Page and enter your credentials.
4. The system will prompt you for an OTP. Check your email (or the backend console logs if Brevo SMTP is disabled).
5. Enter the OTP to receive your JWT Access & Refresh Tokens.
