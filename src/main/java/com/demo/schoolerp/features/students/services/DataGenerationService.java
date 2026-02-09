package com.demo.schoolerp.features.students.services;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class DataGenerationService {
    private final String PATH = "C:\\var\\log\\applications\\API\\dataprocessing\\";

    public String generateStudentData(int count) throws IOException {
        Files.createDirectories(Paths.get(PATH));
        String filename = "generated_students_" + System.currentTimeMillis() + ".xlsx";

        // SXSSFWorkbook is memory-efficient for writing large files
        try (SXSSFWorkbook wb = new SXSSFWorkbook(100);
                FileOutputStream out = new FileOutputStream(PATH + filename)) {

            Sheet sh = wb.createSheet("Students");
            String[] headers = { "studentId", "firstName", "lastName", "DOB", "class", "score" };
            Row headerRow = sh.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }

            String[] classes = { "Class1", "Class2", "Class3", "Class4", "Class5" };

            for (int i = 1; i <= count; i++) {
                Row row = sh.createRow(i);
                row.createCell(0).setCellValue(i);
                row.createCell(1).setCellValue(randomStr());
                row.createCell(2).setCellValue(randomStr());
                row.createCell(3).setCellValue(randomDate().toString());
                row.createCell(4).setCellValue(classes[ThreadLocalRandom.current().nextInt(5)]);
                row.createCell(5).setCellValue(ThreadLocalRandom.current().nextInt(55, 76));

                if (i % 200000 == 0) {
                    System.out.println("Generated " + i + " records...");
                }
            }

            wb.write(out);
            wb.dispose();
            return PATH + filename;
        }
    }

    private String randomStr() {
        String alph = "abcdefghijklmnopqrstuvwxyz";
        int len = ThreadLocalRandom.current().nextInt(3, 9);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) {
            sb.append(alph.charAt(ThreadLocalRandom.current().nextInt(26)));
        }
        return sb.toString();
    }

    private LocalDate randomDate() {
        long start = LocalDate.of(2000, 1, 1).toEpochDay();
        long end = LocalDate.of(2010, 12, 31).toEpochDay();
        return LocalDate.ofEpochDay(ThreadLocalRandom.current().nextLong(start, end));
    }
}
