package com.demo.schoolerp.features.students.controllers;

import com.demo.schoolerp.features.students.services.ExportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/students/export")
@CrossOrigin(origins = "*")
public class ExportController {

    @Autowired
    private ExportService exportService;

    @GetMapping("/pdf")
    public ResponseEntity<Resource> exportPdf(@RequestParam(required = false, name = "class") String studentClass) {
        byte[] pdfBytes = exportService.generatePdf(studentClass);
        ByteArrayResource resource = new ByteArrayResource(pdfBytes);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"students.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(resource);
    }

    @GetMapping("/excel")
    public ResponseEntity<Resource> exportExcel(@RequestParam(required = false, name = "class") String studentClass)
            throws IOException {
        byte[] excelBytes = exportService.generateExcel(studentClass);
        ByteArrayResource resource = new ByteArrayResource(excelBytes);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"students.xlsx\"")
                .contentType(
                        MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(resource);
    }

    @GetMapping("/csv")
    public ResponseEntity<Resource> exportCsv(@RequestParam(required = false, name = "class") String studentClass) {
        byte[] csvBytes = exportService.generateCsv(studentClass);
        ByteArrayResource resource = new ByteArrayResource(csvBytes);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"students.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(resource);
    }
}
