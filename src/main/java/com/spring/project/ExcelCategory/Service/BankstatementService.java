package com.spring.project.ExcelCategory.Service;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.spring.project.ExcelCategory.Repository.TransactionRepository;


@Service
public class BankstatementService {
	
	@Autowired 
	TransactionRepository transactionRepo;
	
	
	    private static final Logger logger = LoggerFactory.getLogger(BankstatementService.class);
	    
	    public byte[] extractAndExportData(MultipartFile file, String password) throws IOException {
	        List<String[]> extractedData;

	        String filename = file.getOriginalFilename();
	        if (filename == null) throw new IllegalArgumentException("Invalid file name");

	        if (filename.endsWith(".pdf")) {
	            extractedData = extractFromPDF(file.getBytes(), password);
	        } else if (filename.endsWith(".xls") || filename.endsWith(".xlsx")) {
	            extractedData = extractFromExcel(file);
	        } else {
	            throw new IllegalArgumentException("Unsupported file type");
	        }

	        return writeToExcel(extractedData);
	    }

	    private List<String[]> extractFromPDF(byte[] pdfBytes, String password) throws IOException {
	        List<String[]> data = new ArrayList<>();

	        PDDocument document;
	        try {
	            document = PDDocument.load(pdfBytes, password);
	        } catch (Exception e) {
	            throw new IOException("Failed to open PDF. Possibly due to incorrect password or format.", e);
	        }

	        if (document.isEncrypted()) {
	            document.setAllSecurityToBeRemoved(true);
	        }

	        PDFTextStripper stripper = new PDFTextStripper();
	        String text = stripper.getText(document);
	        
//	        System.out.println(text);
	        
	        document.close();

	        String[] lines = text.split("\\r?\\n");

	        for (String line : lines) {
	            line = line.trim();
	            
	            System.out.print(line);

	            // Match lines starting with a date in dd-MM-yyyy format
	            if (line.matches("^\\d{2}-\\d{2}-\\d{4}.*") || line.matches("^\\d{2}/\\d{2}/\\d{4}.*")) {
	                String[] tokens = line.split("\\s+");
	                
	                System.out.println("TOKENS: " + Arrays.toString(tokens));
	                System.out.println("LENGTH: " + tokens.length);

	                
//	                System.out.println(tokens);

	                // Find last 3 elements (amount, balance, CR/DR type)
	                if (tokens.length >= 4) {
	                    String date = tokens[0];
	                    String balance = tokens[tokens.length - 1];
	                    String amount = tokens[tokens.length - 2];
	                    String type = tokens[tokens.length - 3];
	                    String narration = String.join(" ", Arrays.copyOfRange(tokens, 1, tokens.length - 3));

	                    String deposit = "";
	                    String withdrawal = "";

	                    if (type.equalsIgnoreCase("CR")) {
	                        deposit = amount;
	                    } else {
	                        withdrawal = amount;
	                    }

	                    data.add(new String[]{date, narration, deposit, withdrawal, balance});
	                }
	                
	            }
	        }
	        return data;
	    }

	    private List<String[]> extractFromExcel(MultipartFile file) throws IOException {
	        List<String[]> data = new ArrayList<>();
	        Workbook workbook = WorkbookFactory.create(file.getInputStream());
	        Sheet sheet = workbook.getSheetAt(0);

	        for (Row row : sheet) {
	            String[] rowData = new String[5]; // Date, Description, Deposit, Withdrawal, Balance

	            for (int i = 0; i < 5 && i < row.getLastCellNum(); i++) {
	                Cell cell = row.getCell(i);
	                rowData[i] = (cell != null) ? getStringCellValue(cell) : "";
	            }

	            data.add(rowData);
	        }

	        workbook.close();
	        return data;
	    }

	    private String getStringCellValue(Cell cell) {
	        switch (cell.getCellType()) {
	            case NUMERIC:
	                if (DateUtil.isCellDateFormatted(cell)) {
	                    return new SimpleDateFormat("dd/MM/yyyy").format(cell.getDateCellValue());
	                } else {
	                    return String.valueOf(cell.getNumericCellValue());
	                }
	            case STRING:
	                return cell.getStringCellValue();
	            default:
	                return "";
	        }
	    }

	    private byte[] writeToExcel(List<String[]> data) throws IOException {
	        Workbook workbook = new XSSFWorkbook();
	        Sheet sheet = workbook.createSheet("Bank Statement");

	        String[] header = {"Date", "Description", "Deposit", "Withdrawal", "Balance"};
	        Row headerRow = sheet.createRow(0);
	        for (int i = 0; i < header.length; i++) {
	            headerRow.createCell(i).setCellValue(header[i]);
	        }
	        
	        System.out.println("Extracted " + data.size() + " transactions.");


	        int rowIndex = 1;
	        for (String[] rowData : data) {
	            Row row = sheet.createRow(rowIndex++);
	            for (int i = 0; i < rowData.length; i++) {
	                row.createCell(i).setCellValue(rowData[i]);
	            }
	        }

	        ByteArrayOutputStream out = new ByteArrayOutputStream();
	        workbook.write(out);
	        workbook.close();
	        return out.toByteArray();
	    }
}
