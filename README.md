# 🏓 Padel Court Management System

A full-stack web application for managing users, clubs, courts, and rentals.

This project was developed as part of the **Software Laboratory course** at **Instituto Superior de Engenharia de Lisboa (ISEL)**, in a team of 3 students. It provides a complete system for booking and managing padel courts through a RESTful API and a Single Page Application (SPA).

---

## 🚀 Overview

The system allows users to:

* Register and authenticate using Bearer Tokens
* Create and manage clubs and courts
* Book and manage court rentals
* Check court availability
* Browse data with pagination and filtering

---

## 🛠️ Tech Stack

* **Backend:** Kotlin + http4k
* **Frontend:** HTML, CSS, JavaScript (SPA)
* **Database:** PostgreSQL
* **Server:** Jetty
* **Deployment:** Render
* **Containerization:** Docker
* **Version Control:** Git & GitHub

---

## ⚙️ Features

* RESTful API design
* Authentication with Bearer Tokens
* Full CRUD operations
* Pagination and search support
* Relational database with foreign keys
* Separation of concerns (API, Services, Repositories)

---

## 🖥️ Running the Project

This project can be executed in two modes:

* **Local environment (recommended for development)**
* **Render deployment (remote environment)**

---

## 🧪 Run Locally

### 1. Configure database connection

In `Server.kt`, enable the local database:

```kotlin id="localdb2"
setURL("jdbc:postgresql://localhost/postgres?user=postgres&password=YOUR_PASSWORD")
// setURL("jdbc:postgresql://render-url...")
```

---

### 2. Setup PostgreSQL

Make sure PostgreSQL is running locally.

Run:

```bash id="sqlrun2"
psql -U postgres -d postgres -f createschema.sql
psql -U postgres -d postgres -f adddata.sql
```

---

### 3. Configure frontend

In:

```text id="routerfile3"
static-content/spa/router.js
```

set:

```javascript id="localapi2"
export const API_BASE_URL = "http://localhost:8080/";
```

---

### 4. Run the application

Using IntelliJ:

* Run the `main` function

Or via Gradle:

```bash id="gradle2"
./gradlew run
```

---

### 5. Access the app

```text id="localurl2"
http://localhost:8080
```

---

## ☁️ Run on Render

### 1. Configure backend

In `Server.kt`, enable the remote database:

```kotlin id="renderdb2"
// setURL("jdbc:postgresql://localhost/postgres?user=postgres&password=YOUR_PASSWORD")
setURL("jdbc:postgresql://render-url...")
```

---

### 2. Configure frontend

In:

```text id="routerfile4"
static-content/spa/router.js
```

set:

```javascript id="renderapi2"
export const API_BASE_URL = "https://your-render-url.onrender.com/";
```

---

### 3. Deploy

* Push the project to GitHub
* Connect the repository to Render
* Deploy the service

---

### 4. Access the app

```text id="renderurl2"
https://your-render-url.onrender.com
```

---

## 📡 API Example Endpoints

* `GET /users`
* `POST /users`
* `POST /login`
* `GET /clubs`
* `POST /clubs`
* `GET /clubs/{cid}/courts`
* `POST /clubs/{cid}/courts`
* `GET /rentals`
* `POST /clubs/{cid}/courts/{crid}/rentals`

---

## ⚠️ Notes

* When switching environments, update both:

    * Database connection (`Server.kt`)
    * Frontend API URL (`router.js`)
* PostgreSQL must be running before starting the server
* If no data appears, re-run the SQL scripts

---

## 👩‍💻 Authors

Developed by a team of 3 students at **Instituto Superior de Engenharia de Lisboa (ISEL)**.
