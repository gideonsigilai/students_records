package com.demo.schoolerp.features.students.repository;

import com.demo.schoolerp.features.students.models.StudentModel;
import org.springframework.data.jpa.repository.Query;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<StudentModel, Long> {
    Page<StudentModel> findByStudentId(Long studentId, Pageable pageable);

    Page<StudentModel> findByStudentClass(String studentClass, Pageable pageable);
}