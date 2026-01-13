-- =========================
-- DATABASES
-- =========================
CREATE DATABASE IF NOT EXISTS Auth_DB;
CREATE DATABASE IF NOT EXISTS ERP_DB;

-- =========================
-- AUTH DATABASE
-- =========================
USE Auth_DB;

CREATE TABLE users_auth (
  user_id INT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(50) UNIQUE NOT NULL,
  role VARCHAR(50) NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  status VARCHAR(50),
  last_login TIMESTAMP NULL DEFAULT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =========================
-- ERP DATABASE
-- =========================
USE ERP_DB;

CREATE TABLE students (
  user_id INT PRIMARY KEY,
  roll_no VARCHAR(50) UNIQUE NOT NULL,
  program VARCHAR(50) NOT NULL,
  year INT
);

CREATE TABLE instructors (
  user_id INT PRIMARY KEY,
  department VARCHAR(255) NOT NULL
);

CREATE TABLE courses (
  course_id INT AUTO_INCREMENT PRIMARY KEY,
  code VARCHAR(10) UNIQUE NOT NULL,
  title VARCHAR(100) NOT NULL,
  credits INT NOT NULL
);

CREATE TABLE sections (
  section_id INT AUTO_INCREMENT PRIMARY KEY,
  course_id INT NOT NULL,
  instructor_id INT NOT NULL,
  day_time VARCHAR(50) NOT NULL,
  room VARCHAR(20) NOT NULL,
  capacity INT NOT NULL,
  semester VARCHAR(20) NOT NULL,
  year INT NOT NULL,
  FOREIGN KEY (course_id) REFERENCES courses(course_id),
  FOREIGN KEY (instructor_id) REFERENCES instructors(user_id)
);

CREATE TABLE enrollments (
  enrollment_id INT AUTO_INCREMENT PRIMARY KEY,
  student_id INT NOT NULL,
  section_id INT NOT NULL,
  status VARCHAR(20) NOT NULL,
  UNIQUE KEY uq_student_section (student_id, section_id),
  FOREIGN KEY (student_id) REFERENCES students(user_id),
  FOREIGN KEY (section_id) REFERENCES sections(section_id)
);

CREATE TABLE grades (
  grade_id INT AUTO_INCREMENT PRIMARY KEY,
  enrollment_id INT NOT NULL,
  component VARCHAR(50) NOT NULL,
  score DECIMAL(5,2) NOT NULL,
  final_grade VARCHAR(2),
  UNIQUE KEY uq_enrollment_component (enrollment_id, component),
  FOREIGN KEY (enrollment_id) REFERENCES enrollments(enrollment_id)
);

CREATE TABLE settings (
  key_ VARCHAR(50) PRIMARY KEY,
  value VARCHAR(255) NOT NULL
);
