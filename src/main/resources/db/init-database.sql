-- PostgreSQL Database Setup Script
-- Run this script as postgres superuser to create the database and user

-- Create the database
CREATE DATABASE schoolerp;

-- Create a user (optional - modify credentials as needed)
CREATE USER schoolerp_user WITH ENCRYPTED PASSWORD 'your_password_here';

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE schoolerp TO schoolerp_user;

-- Connect to the schoolerp database before running the table creation
\c schoolerp

-- Grant schema privileges
GRANT ALL ON SCHEMA public TO schoolerp_user;
