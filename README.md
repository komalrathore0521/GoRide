# GoRide 🚖  
**Ride-Hailing Backend – Java · Spring Boot · PostgreSQL (PostGIS) · JWT**

GoRide is a backend service that powers a modern ride-hailing experience end-to-end: **book → match → live track → complete**. It features geospatial driver matching, secure authentication, and a clean, layered architecture ready for scale. ([github.com](https://github.com/komalrathore0521/GoRide))

> **Live API Docs (Swagger):** `https://goride-g1n3.onrender.com/swagger-ui/index.html`  
> (If the Render instance is sleeping, run locally using the steps below.)

---

## ✨ Features

- **Full ride lifecycle APIs:** book, match, live track, and complete/cancel.  
- **Proximity-based driver matching:** geospatial queries with Postgres + Hibernate Spatial for accurate ETAs.  
- **Secure auth:** Spring Security + JWT for stateless authentication.  
- **Clean architecture:** controller → service → repository, DTOs, and validation.  
- **Containerized:** Dockerized backend for simple deploys.

---

## 🧱 Tech Stack

- **Language:** Java  
- **Framework:** Spring Boot (Web, Security, Data JPA)  
- **Auth:** JWT  
- **Database:** PostgreSQL + **PostGIS** (Hibernate Spatial)  
- **Build:** Maven  
- **Container:** Docker  

---

**Driver matching:** nearest available driver within a radius, using geo-indexed queries and driver/ride state machines.

---

## 🚀 Getting Started (Local)

### 1) Prerequisites
- Java 21
- Maven 3.9+  
- Docker (optional, for containerized run)  
- PostgreSQL 14+ with **PostGIS** extension

Create a DB and enable PostGIS:
```sql
CREATE DATABASE goride;
\c goride
CREATE EXTENSION IF NOT EXISTS postgis;
```
## 📐 High-Level Architecture


---

Feel free to ask if you’d like a **docker-compose file**, **CI/CD badge**, or to update the **Contributing section** for your team workflows.
::contentReference[oaicite:0]{index=0}


