-- Students Table Schema
-- This script creates the students table used by the application

CREATE TABLE IF NOT EXISTS students (
    student_id BIGINT PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    dob DATE NOT NULL,
    student_class VARCHAR(50) NOT NULL,
    score DECIMAL(5,2) NOT NULL
);

-- Create index for faster filtering by class
CREATE INDEX IF NOT EXISTS idx_students_class ON students(student_class);

-- Create index for faster search by student_id
CREATE INDEX IF NOT EXISTS idx_students_id ON students(student_id);
