# Role-Based University ERP System

A Java-based academic ERP system developed as a university project to manage
users, courses, registrations, and academic records using role-based access.

## Features
- Role-based authentication (Admin, Faculty, Student)
- Course and section management
- Student course registration with validation
- Grade management and transcript export
- GUI-based dashboards using Java Swing

## Tech Stack
- Java
- Java Swing
- MySQL
- JDBC

## Architecture
The system follows a layered architecture:

UI → Service → DAO → Database

Business logic is handled in the Service layer, database access is isolated
using DAO classes, and UI components interact only with services.

## Notes
This project was developed as an academic exercise and focuses on clean
architecture and role-based workflows rather than production deployment.

## How to Run
1. Create databases using `database/erp_schema.sql`
2. Update database credentials in `config.properties`
3. Run `AppMain.java`


