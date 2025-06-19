# 🏋️ Fitness App - Microservices Architecture

This is a **Fitness Tracking Application** built using **Spring Boot** and **Spring Cloud** in a microservices architecture. It features authentication with **Keycloak**, service discovery via **Eureka**, inter-service messaging with **RabbitMQ**, persistent storage with **PostgreSQL**, and an **API Gateway** for routing.

---

## 🧰 Tech Stack

| Layer | Technology |
|------|------------|
| Core Framework | Spring Boot 3.x |
| Authentication | Keycloak |
| Service Discovery | Eureka |
| API Gateway | Spring Cloud Gateway |
| Messaging | RabbitMQ |
| Database | PostgreSQL |
| Configuration | Spring Cloud Config |
| Build Tool | Maven |
| Containerization | Docker (optional) |

---

## 🧱 Microservices Overview

| Service | Description |
|--------|-------------|
| `api-gateway` | Entry point for all client requests |
| `user-service` | Manages user profiles, roles, preferences |
| `workout-service` | Handles workouts, routines, and plans |
| `nutrition-service` | Manages meal tracking and nutrition plans |
| `notification-service` | Sends emails/alerts via RabbitMQ |
| `auth-server` | Keycloak authentication server |
| `config-server` | Centralized externalized configuration |
| `discovery-server` | Eureka service registry |

---

## 🔐 Keycloak Setup

1. Download and run Keycloak:
   ```bash
   docker run -p 8080:8080 -e KEYCLOAK_ADMIN=admin -e KEYCLOAK_ADMIN_PASSWORD=admin quay.io/keycloak/keycloak:24.0.3 start-dev

