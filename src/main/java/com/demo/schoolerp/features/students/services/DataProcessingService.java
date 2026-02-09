package com.demo.schoolerp.features.students.services;

import com.demo.schoolerp.features.students.models.StudentModel;
import com.demo.schoolerp.features.students.repository.StudentRepository;
import jakarta.transaction.Transactional;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.XMLHelper;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.XSSFComment;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class DataProcessingService {

    @Autowired
    private StudentRepository repo;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final String PATH = "C:\\var\\log\\applications\\API\\dataprocessing\\";

    static {
        IOUtils.setByteArrayMaxOverride(500000000);
    }

    public String processExcelToCsv(InputStream excelStream) throws Exception {
        Files.createDirectories(Paths.get(PATH));
        String csvFilename = "processed_students_" + System.currentTimeMillis() + ".csv";
        String fullPath = PATH + csvFilename;

        File tempExcel = File.createTempFile("upload-", ".xlsx");
        Files.copy(excelStream, tempExcel.toPath(), StandardCopyOption.REPLACE_EXISTING);

        try (OPCPackage pkg = OPCPackage.open(tempExcel);
                PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(fullPath)))) {

            writer.println("studentId,firstName,lastName,DOB,class,score");

            XSSFReader reader = new XSSFReader(pkg);
            ReadOnlySharedStringsTable strings = new ReadOnlySharedStringsTable(pkg);
            StylesTable styles = reader.getStylesTable();
            XSSFReader.SheetIterator iter = (XSSFReader.SheetIterator) reader.getSheetsData();

            if (iter.hasNext()) {
                try (InputStream sheetStream = iter.next()) {
                    processSheet(styles, strings, sheetStream, writer);
                }
            }
        } finally {
            if (tempExcel.exists())
                tempExcel.delete();
        }
        return fullPath;
    }

    private void processSheet(StylesTable styles, ReadOnlySharedStringsTable strings, InputStream sheetData,
            PrintWriter writer) throws Exception {
        XMLReader parser = XMLHelper.newXMLReader();

        XSSFSheetXMLHandler handler = new XSSFSheetXMLHandler(styles, strings,
                new XSSFSheetXMLHandler.SheetContentsHandler() {
                    private final String[] currentRow = new String[6];
                    private boolean isHeader = true;

                    @Override
                    public void startRow(int rowNum) {
                        for (int i = 0; i < 6; i++)
                            currentRow[i] = "";
                    }

                    @Override
                    public void endRow(int rowNum) {
                        if (isHeader) {
                            isHeader = false;
                            return;
                        }
                        try {
                            double originalScore = Double.parseDouble(currentRow[5]);
                            double csvScore = originalScore + 10;
                            writer.println(String.format("%s,%s,%s,%s,%s,%.2f",
                                    currentRow[0], currentRow[1], currentRow[2],
                                    currentRow[3], currentRow[4], csvScore));
                        } catch (Exception e) {
                        }
                    }

                    @Override
                    public void cell(String cellRef, String value, XSSFComment comment) {
                        int col = new CellReference(cellRef).getCol();
                        if (col < 6)
                            currentRow[col] = value;
                    }

                    @Override
                    public void headerFooter(String text, boolean isHeader, String tagName) {
                    }
                }, false);

        parser.setContentHandler(handler);
        parser.parse(new InputSource(sheetData));
    }

    @Transactional
    public void processAndUpload(InputStream csvStream) throws IOException {
        jdbcTemplate.execute("TRUNCATE TABLE students");

        String sql = "INSERT INTO students (student_id, first_name, last_name, dob, student_class, score) VALUES (?, ?, ?, ?, ?, ?)";

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(csvStream))) {
            reader.readLine(); // skip header

            List<Object[]> batch = new ArrayList<>();
            String line;
            int count = 0;

            while ((line = reader.readLine()) != null) {
                String[] v = line.split(",");
                if (v.length < 6)
                    continue;

                double csvScore = Double.parseDouble(v[5]);
                Object[] studentData = {
                        Long.parseLong(v[0]),
                        v[1],
                        v[2],
                        LocalDate.parse(v[3]),
                        v[4],
                        csvScore - 5
                };

                batch.add(studentData);
                count++;

                if (batch.size() >= 1000) {
                    jdbcTemplate.batchUpdate(sql, batch);
                    batch.clear();
                    if (count % 100000 == 0)
                        System.out.println("Uploaded " + count + " records...");
                }
            }
            if (!batch.isEmpty())
                jdbcTemplate.batchUpdate(sql, batch);
            System.out.println("Finalized upload of " + count + " records.");
        }
    }
}