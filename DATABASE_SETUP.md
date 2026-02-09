# PostgreSQL Database Setup

This guide walks you through setting up the PostgreSQL database for the SchoolERP backend.

## Prerequisites

- PostgreSQL 14+ installed
- Access to `psql` or a PostgreSQL client

## Quick Setup

### 1. Create the Database

Run as postgres superuser:

```sql
CREATE DATABASE school;
```

### 2. Run Schema Migration

The application uses Hibernate's `ddl-auto=update` mode, which automatically creates tables on startup.

Alternatively, run manually:

```sql
\c school

CREATE TABLE IF NOT EXISTS students (
    student_id BIGINT PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    dob DATE NOT NULL,
    student_class VARCHAR(50) NOT NULL,
    score DECIMAL(5,2) NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_students_class ON students(student_class);
CREATE INDEX IF NOT EXISTS idx_students_id ON students(student_id);
```

### 3. Configure Application

Update `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/school
spring.datasource.username=postgres
spring.datasource.password=your_password_here
```

Or use environment variables in `.env`:

```
DB_HOST=localhost
DB_PORT=5432
DB_NAME=school
DB_USERNAME=postgres
DB_PASSWORD=your_password_here
```

## Verify Setup

```bash
./gradlew bootRun
```

The application should start without errors and create the `students` table automatically.
