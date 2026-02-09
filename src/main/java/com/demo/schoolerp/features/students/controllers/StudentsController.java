package com.demo.schoolerp.features.students.controllers;

import com.demo.schoolerp.features.students.models.StudentModel;
import com.demo.schoolerp.features.students.repository.StudentRepository;
import com.demo.schoolerp.features.students.services.DataGenerationService;
import com.demo.schoolerp.features.students.services.DataProcessingService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/students")
@CrossOrigin(origins = "*")
public class StudentsController {

    @Autowired
    private StudentRepository studentsRepo;

    @Autowired
    private DataGenerationService dataGenerationService;

    @Autowired
    private DataProcessingService dataProcessingService;

    @PostMapping("/generate")
    public ResponseEntity<Map<String, Object>> generateData(@RequestParam(defaultValue = "1000000") int count) {
        Map<String, Object> response = new HashMap<>();
        long startTime = System.currentTimeMillis();

        try {
            String filePath = dataGenerationService.generateStudentData(count);
            long endTime = System.currentTimeMillis();

            response.put("message", "Successfully generated " + count + " records");
            response.put("filePath", filePath);
            response.put("timeInMillis", endTime - startTime);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("message", "Generation failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/process")
    public ResponseEntity<Object> process(@RequestParam("file") MultipartFile file) {
        long startTime = System.currentTimeMillis();
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("File is empty");
            }

            String csvPath = dataProcessingService.processExcelToCsv(file.getInputStream());
            long duration = System.currentTimeMillis() - startTime;

            File csvFile = new File(csvPath);
            if (!csvFile.exists()) {
                throw new Exception("CSV file was not created at " + csvPath);
            }

            Resource resource = new FileSystemResource(csvFile);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + csvFile.getName() + "\"")
                    .header("X-Performance-Ms", String.valueOf(duration))
                    .contentType(MediaType.parseMediaType("text/csv"))
                    .body(resource);

        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Processing failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadCsv(@RequestParam("file") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();
        long startTime = System.currentTimeMillis();

        try {
            dataProcessingService.processAndUpload(file.getInputStream());
            long endTime = System.currentTimeMillis();

            response.put("message", "CSV uploaded successfully");
            response.put("timeInMillis", endTime - startTime);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("message", "Upload failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/report")
    public Page<StudentModel> getReport(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long id,
            @RequestParam(required = false, name = "class") String studentClass) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("studentId").ascending());

        if (id != null) {
            return studentsRepo.findByStudentId(id, pageable);
        } else if (studentClass != null && !studentClass.isEmpty()) {
            return studentsRepo.findByStudentClass(studentClass, pageable);
        }

        return studentsRepo.findAll(pageable);
    }

    @PostMapping("/add")
    public StudentModel addStudent(@RequestBody StudentModel student) {
        return studentsRepo.save(student);
    }
}