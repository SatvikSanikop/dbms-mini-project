-- Run this script in MySQL Workbench (Server > Data Import or open and execute).
CREATE DATABASE IF NOT EXISTS student_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE student_db;

CREATE TABLE IF NOT EXISTS students (
    student_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    course VARCHAR(100) NOT NULL,
    semester TINYINT UNSIGNED NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_semester CHECK (semester BETWEEN 1 AND 12)
);

-- Optional demonstration records. Remove the comments to insert sample rows.
-- INSERT INTO students (name,email,course,semester) VALUES
-- ('Asha Rao','asha@example.com','Computer Science',3),
-- ('Rohan Kumar','rohan@example.com','Information Technology',2);

SELECT * FROM students;
