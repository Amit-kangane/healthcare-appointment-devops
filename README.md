# End-to-End DevOps Pipeline for Healthcare Appointment Management System

[![Java 21](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot 3.3.5](https://img.shields.io/badge/Spring%20Boot-3.3.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Maven](https://img.shields.io/badge/Build-Maven-C71A36.svg)](https://maven.apache.org/)
[![JUnit 5](https://img.shields.io/badge/Tested%20with-JUnit%205-25A162.svg)](https://junit.org/junit5/)
[![Docker](https://img.shields.io/badge/Container-Docker-2496ED.svg)](https://www.docker.com/)
[![Kubernetes](https://img.shields.io/badge/Orchestration-Kubernetes-326CE5.svg)](https://kubernetes.io/)
[![Prometheus](https://img.shields.io/badge/Metrics-Prometheus-E6522C.svg)](https://prometheus.io/)
[![Grafana](https://img.shields.io/badge/Observability-Grafana-F46800.svg)](https://grafana.com/)

A production-grade, enterprise-ready DevOps capstone project demonstrating continuous integration, automated testing, containerization, Kubernetes orchestration, and real-time observability for a Healthcare Appointment Management REST API.

---

## 📌 Project Overview

This project provides a robust RESTful API for scheduling, viewing, updating, and cancelling fictional healthcare appointments. It automates the entire software lifecycle from the moment a developer commits code to GitHub through to automated deployment and real-time metric tracking in Grafana.

> [!NOTE]
> **Data Privacy Notice:** All data utilized in this application consists strictly of **fictional, generated patient and doctor records**. No Protected Health Information (PHI) or real patient data is used or stored.
> 
> **Storage Note:** To ensure fast, zero-dependency CI/CD automated testing, the repository uses an in-memory thread-safe store (`ConcurrentHashMap`). In this demo mode, state resets whenever the application restarts.

---

## 🏗️ DevOps Pipeline Architecture

```
                                      +---------------------------------------------+
                                      | 1. Developer pushes code to GitHub          |
                                      +----------------------+----------------------+
                                                             | Webhook / Polling
                                                             v
+-------------------------------------------------------------------------------------------------------------------+
| 2. JENKINS AUTOMATION SERVER (CI/CD PIPELINE)                                                                     |
|    Stage 1: Checkout Git Source Code                                                                              |
|    Stage 2: Compile & Execute Automated JUnit 5 Tests (mvn clean test) -> FAILS FAST IF BROKEN                    |
|    Stage 3: Package Application JAR (mvn package)                                                                 |
|    Stage 4: Build Multi-Stage Docker Image (Tagged with immutable Git Commit SHA :a1b2c3d)                        |
|    Stage 5: Push Versioned Image to Container Registry (Docker Hub)                                               |
|    Stage 6: Deploy / Rolling Update to Kubernetes Cluster (kubectl set image)                                     |
+------------------------------------------------------------+------------------------------------------------------+
                                                             | Deploys Pods
                                                             v
+-------------------------------------------------------------------------------------------------------------------+
| 3. KUBERNETES CLUSTER (PRODUCTION RUNTIME)                                                                        |
|    - 2 Replicas (High Availability & Zero-Downtime Rolling Updates)                                               |
|    - Resource Limits (CPU: 500m, Memory: 512Mi) & Requests (CPU: 100m, Memory: 256Mi)                             |
|    - Liveness Probe:  HTTP /actuator/health/liveness                                                              |
|    - Readiness Probe: HTTP /actuator/health/readiness                                                             |
+------------------------------------+-----------------------------------------------+------------------------------+
                                     | Exposes Metrics                               | Performance Logs
                                     v                                               v
+------------------------------------+-----------------------+       +---------------+------------------------------+
| 4. PROMETHEUS (METRICS COLLECTION)                         |       | 5. GRAFANA (OBSERVABILITY DASHBOARDS)        |
|    - Scrapes /actuator/prometheus every 15s                |------>|    - System Availability & Uptime (SLO)      |
|    - Evaluates Alerting Rules:                             |       |    - HTTP Request Rate & Latency (P95/P99)   |
|      * InstanceDown, HighMemoryUsage, HighErrorRate        |       |    - JVM Heap & Thread Utilization           |
+------------------------------------------------------------+       +----------------------------------------------+
```

---

## 🛠️ Technology Stack

| Domain | Technology | Version | Purpose |
| :--- | :--- | :--- | :--- |
| **Language** | Java (JDK) | 21 LTS | Core programming language |
| **Framework** | Spring Boot | 3.3.5 | Production REST microservice |
| **Build & Dependencies** | Apache Maven | 3.9.9 (Wrapper included) | Dependency resolution & lifecycle management |
| **Automated Testing** | JUnit 5 + MockMvc + AssertJ | 5.10.5 | Automated unit & slice testing |
| **Observability** | Spring Boot Actuator + Micrometer | 3.3.5 | Health checks, probes, & Prometheus metrics |
| **Version Control** | Git & GitHub | Latest | Source control, feature branches, release tags |
| **CI/CD Automation** | Jenkins | LTS | Declarative pipeline automation |
| **Containerization** | Docker | Latest | Multi-stage, non-root secure container image |
| **Orchestration** | Kubernetes (Minikube / Docker K8s) | 1.30+ | Container orchestration, self-healing, scaling |
| **Monitoring** | Prometheus | Latest | Metric scraping and alert rule evaluation |
| **Visualization** | Grafana | Latest | Real-time dashboards and analytics |

---

## 📦 Project Architecture & Packages

```
com.healthcare.appointment
 ├── controller/           # REST endpoints and centralized exception handling
 │    ├── AppointmentController.java
 │    └── GlobalExceptionHandler.java
 ├── service/              # Core business rules, validation, and metrics tracking
 │    ├── AppointmentService.java
 │    ├── AppointmentServiceImpl.java
 │    └── AppointmentMetricsService.java
 ├── repository/           # In-memory thread-safe data persistence
 │    ├── AppointmentRepository.java
 │    └── InMemoryAppointmentRepository.java
 ├── model/                # Domain entities and status enumerations
 │    ├── Appointment.java
 │    └── AppointmentStatus.java
 ├── dto/                  # Data transfer objects and validation constraints
 │    ├── AppointmentRequest.java
 │    ├── AppointmentResponse.java
 │    └── ErrorResponse.java
 └── exception/            # Domain exceptions (AppointmentNotFoundException, etc.)
```

---

## 🚀 REST API Reference

All requests and responses use `application/json`.

### 1. Appointments Endpoints

| Method | Endpoint | Description | Status Code |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/appointments` | Retrieve all appointments | `200 OK` |
| `GET` | `/api/appointments/{id}` | Retrieve single appointment by ID | `200 OK` / `404 Not Found` |
| `POST` | `/api/appointments` | Create/Book a new appointment | `201 Created` / `400 Bad Request` |
| `PUT` | `/api/appointments/{id}` | Update appointment details | `200 OK` / `404 Not Found` / `400 Bad Request` |
| `DELETE`| `/api/appointments/{id}` | Cancel/Delete appointment by ID | `204 No Content` / `404 Not Found` |

#### Create Appointment Sample (`POST /api/appointments`)
```json
{
  "patientName": "Fictional Patient Robert Vance",
  "doctorName": "Dr. Emily Watson (Cardiology)",
  "appointmentDateTime": "2026-11-20T10:30:00",
  "status": "SCHEDULED",
  "reason": "Routine annual consultation"
}
```

#### Success Response (`201 Created` with `Location` Header):
```json
{
  "id": 4,
  "patientName": "Fictional Patient Robert Vance",
  "doctorName": "Dr. Emily Watson (Cardiology)",
  "appointmentDateTime": "2026-11-20T10:30:00",
  "status": "SCHEDULED",
  "reason": "Routine annual consultation",
  "createdAt": "2026-10-23T12:00:00",
  "updatedAt": "2026-10-23T12:00:00"
}
```

#### Validation Error Sample (`400 Bad Request`):
```json
{
  "timestamp": "2026-10-23T12:00:00",
  "status": 400,
  "error": "Validation Failed",
  "message": "One or more fields failed validation checks",
  "details": [
    "patientName: Patient name is required and cannot be blank",
    "appointmentDateTime: Appointment date and time must be in the future"
  ],
  "path": "/api/appointments"
}
```

---

### 2. Observability & Health Endpoints

- **General Health:** `GET http://localhost:8080/actuator/health`
- **Kubernetes Liveness Probe:** `GET http://localhost:8080/actuator/health/liveness`
- **Kubernetes Readiness Probe:** `GET http://localhost:8080/actuator/health/readiness`
- **Prometheus Metrics:** `GET http://localhost:8080/actuator/prometheus`

---

## 💻 Local Quickstart

### Prerequisites
- Java 21 JDK
- Git

### Build and Run Locally
```bash
# Clone the repository
git clone https://github.com/YOUR_GITHUB_USERNAME/healthcare-appointment-devops.git
cd healthcare-appointment-devops

# Execute tests
./mvnw clean test

# Package executable JAR
./mvnw clean package

# Run the application
java -jar target/healthcare-appointment-devops-1.0.0.jar
```

The application starts on `http://localhost:8080`.

---

## 🔒 Security Best Practices

1. **No Credentials in Code:** Passwords, API tokens, and registry credentials must NEVER be checked into Git. Use environment variables or Jenkins Credentials Manager.
2. **Immutable Image Tags:** Every Docker image built by CI is tagged with the unique Git Commit SHA (`${GIT_COMMIT}`) instead of `:latest` to guarantee traceability and rollback safety.
3. **Least Privilege Container Runtime:** Docker containers run under a non-root application user (`appuser`).
4. **Input Validation:** All REST API inputs are validated using Bean Validation with sanitized error messages.
