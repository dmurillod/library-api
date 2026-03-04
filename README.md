# 📚 Library API

REST API for library management built with Spring Boot, PostgreSQL and Docker.

## 🚀 Environments

| Environment | URL | Branch |
|---|---|---|
| Staging | https://library-api-staging.onrender.com | `develop` |
| Production | https://library-api-prod.onrender.com | `main` |

## 📋 Entities

- **Member** - Library members
- **Book** - Book catalog
- **Loan** - Book loans management

## 🛠️ Tech Stack

- Java 21
- Spring Boot 3.5
- PostgreSQL 16
- Docker + Docker Compose
- GitHub Actions CI / CD
- JaCoCo (coverage ≥ 60% staging, ≥ 85% production)

## 📡 API Endpoints

### Members
- `GET /api/v1/members` - Get all members
- `GET /api/v1/members/{id}` - Get member by id
- `POST /api/v1/members` - Create member
- `PUT /api/v1/members/{id}` - Update member
- `DELETE /api/v1/members/{id}` - Delete member

### Books
- `GET /api/v1/books` - Get all books
- `GET /api/v1/books/{id}` - Get book by id
- `POST /api/v1/books` - Create book
- `PUT /api/v1/books/{id}` - Update book
- `DELETE /api/v1/books/{id}` - Delete book

### Loans
- `GET /api/v1/loans` - Get all loans
- `GET /api/v1/loans/{id}` - Get loan by id
- `POST /api/v1/loans` - Create loan
- `PUT /api/v1/loans/{id}/return` - Return book

## 🐳 Run with Docker
```bash
docker compose up --build
```

## 📊 API Documentation

- Local: http://localhost:8080/swagger-ui.html
- Staging: https://library-api-staging.onrender.com/swagger-ui.html
- Production: https://library-api-prod.onrender.com/swagger-ui.html

## ⚙️ CI/CD Pipelines

| Pipeline | Trigger | Coverage | Deploy |
|---|---|---|---|
| Staging | Push to `develop` | ≥ 60% | Render Staging |
| Production | Push to `main` | ≥ 85% | Render Production |
