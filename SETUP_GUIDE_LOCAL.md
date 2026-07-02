# Run WITHOUT Docker — Setup Guide

Docker removed. App now runs as plain JVM processes + local MySQL + local RabbitMQ.

## 1. Install prerequisites (once)
- **MySQL 8** — set root password to `root123` (or edit configs to match yours)
- **RabbitMQ** — https://www.rabbitmq.com/docs/install-windows (needed by auth-service + notification-service)
- **Java 17+**, **Maven** (Eclipse ships these, just check `Window > Preferences > Java`)
- **Node.js** (for frontend)

## 2. Create the databases (once)
Open MySQL client / Workbench, run the included `init.sql`:
```
mysql -u root -p < init.sql
```
This creates all 6 databases (`auth_db`, `userdb`, `resume_db`, `paymentdb`, `notificationdb`, `ai_db`) + service DB users. Tables auto-create on first run (`ddl-auto=update`).

## 3. Start order (matters — Eureka first, then user-service, then rest)
Run each as a Spring Boot App in Eclipse (right-click project → Run As → Spring Boot App), or `mvn spring-boot:run` in terminal, **in this order**:

1. `eureka-server` — wait till `http://localhost:8761` loads
2. `user-service`
3. `auth-service`
4. `resume-service`
5. `AI-service`
6. `payment-service`
7. `notification-service`
8. `api-gateway` — wait till it's up
9. `frontend` — `cd frontend && npm install && npm run dev` → opens on `http://localhost:5174`

## 4. Notes
- RabbitMQ must be running before `auth-service` / `notification-service` start, or they'll fail health checks (retry on restart if RabbitMQ was late).
- Gemini/Razorpay/Brevo keys are already in the configs (dummy/real values as originally set) — those features work same as before, unrelated to Docker.
- `node_modules`, `target`, `.git`, `.metadata` folders were stripped from this zip to keep it light. Maven regenerates `target` on build; run `npm install` in `frontend` to regenerate `node_modules`.
