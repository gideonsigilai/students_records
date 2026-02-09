package com.demo.schoolerp.features.students.services;

import com.demo.schoolerp.features.students.models.StudentModel;
import com.demo.schoolerp.features.students.repository.StudentRepository;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@Service
public class ExportService {

    @Autowired
    private StudentRepository studentRepository;

    public byte[] generatePdf(String studentClass) {
        List<StudentModel> students = fetchStudents(studentClass);
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            Font fontHeader = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
            fontHeader.setSize(18);

            Paragraph title = new Paragraph("Student Report", fontHeader);
            title.setAlignment(Paragraph.ALIGN_CENTER);
            document.add(title);
            document.add(Chunk.NEWLINE);

            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            table.setWidths(new int[] { 1, 3, 3, 3, 2, 2 });

            addTableHeader(table);
            addRows(table, students);

            document.add(table);
            document.close();

        } catch (DocumentException e) {
            e.printStackTrace();
        }

        return out.toByteArray();
    }

    private void addTableHeader(PdfPTable table) {
        String[] headers = { "ID", "First Name", "Last Name", "DOB", "Class", "Score" };
        for (String header : headers) {
            PdfPCell headerCell = new PdfPCell();
            headerCell.setBackgroundColor(java.awt.Color.LIGHT_GRAY);
            headerCell.setPhrase(new Phrase(header));
            table.addCell(headerCell);
        }
    }

    private void addRows(PdfPTable table, List<StudentModel> students) {
        for (StudentModel student : students) {
            table.addCell(String.valueOf(student.getStudentId()));
            table.addCell(student.getFirstName());
            table.addCell(student.getLastName());
            table.addCell(student.getDob().toString());
            table.addCell(student.getStudentClass());
            table.addCell(String.valueOf(student.getScore()));
        }
    }

    public byte[] generateExcel(String studentClass) throws IOException {
        List<StudentModel> students = fetchStudents(studentClass);
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Students");

            Row headerRow = sheet.createRow(0);
            String[] headers = { "Student ID", "First Name", "Last Name", "DOB", "Class", "Score" };
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }

            int rowIdx = 1;
            for (StudentModel student : students) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(student.getStudentId());
                row.createCell(1).setCellValue(student.getFirstName());
                row.createCell(2).setCellValue(student.getLastName());
                row.createCell(3).setCellValue(student.getDob().toString());
                row.createCell(4).setCellValue(student.getStudentClass());
                row.createCell(5).setCellValue(student.getScore());
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    public byte[] generateCsv(String studentClass) {
        List<StudentModel> students = fetchStudents(studentClass);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (CSVPrinter csvPrinter = new CSVPrinter(new PrintWriter(out),
                CSVFormat.DEFAULT.withHeader("Student ID", "First Name", "Last Name", "DOB", "Class", "Score"))) {
            for (StudentModel student : students) {
                csvPrinter.printRecord(
                        student.getStudentId(),
                        student.getFirstName(),
                        student.getLastName(),
                        student.getDob(),
                        student.getStudentClass(),
                        student.getScore());
            }
            csvPrinter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return out.toByteArray();
    }

    private List<StudentModel> fetchStudents(String studentClass) {
        if (studentClass != null && !studentClass.isEmpty()) {
            // Assuming the method exists in repository, otherwise will rely on findAll and
            // filter or add method
            return studentRepository.findByStudentClass(studentClass, Sort.by("studentId").ascending());
        }
        return studentRepository.findAll(Sort.by("studentId").ascending());
    }
}
