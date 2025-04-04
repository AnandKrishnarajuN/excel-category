package com.spring.project.ExcelCategory.Service;


import com.spring.project.ExcelCategory.Model.BankstatementData;
import com.spring.project.ExcelCategory.Model.TransactionCategory;
import com.spring.project.ExcelCategory.Repository.TransactionRepository;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;


@Service
public class BankstatementService {

	@Autowired
	TransactionRepository transactionRepo;


	private static final Logger logger = LoggerFactory.getLogger(BankstatementService.class);

	public byte[] extractAndExportData(MultipartFile file, String password) throws IOException, ParseException {
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

	private List<String[]> extractFromPDF(byte[] pdfBytes, String password) throws IOException, ParseException {
		List<String[]> data = new ArrayList<>();
		List<BankstatementData> transactions = new ArrayList<>();

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
		String prevBalance = "-1";

		for (String line : lines) {
			line = line.trim();

			System.out.print(line);

			// Match lines starting with a date in dd-MM-yyyy format
			if (line.matches("^\\d{2}-\\d{2}-\\d{4}.*") || line.matches("^\\d{2}/\\d{2}/\\d{4}.*") || line.matches("^\\d{2}/\\d{2}/\\d{2}.*") || line.matches("^\\d{2}-\\d{2}-\\d{2}.*")) {
				String[] tokens = line.split("\\s+");

				System.out.println("TOKENS: " + Arrays.toString(tokens));
				System.out.println("LENGTH: " + tokens.length);


//	                System.out.println(tokens);

				// Find last 3 elements (amount, balance, CR/DR type)
				if (tokens.length >= 4) {
					String date = tokens[0];
					String balance = tokens[tokens.length - 1];
					balance = validate(balance);
					String amount = tokens[tokens.length - 2];
					amount = validate(amount);
					String type = tokens[tokens.length - 3];
					String narration = String.join(" ", Arrays.copyOfRange(tokens, 1, tokens.length - 3));
					String category = TransactionCategory.categorize(narration);

					if(amount.equals("error") || balance.equals("error")) continue;

					String deposit = "";
					String withdrawal = "";
					if (Double.parseDouble(prevBalance) != -1 && !type.equalsIgnoreCase("CR")) {
						if (Double.parseDouble(balance) > Double.parseDouble(prevBalance)) {
							deposit = amount;
						} else {
							withdrawal = amount;
						}
					} else {
						if (type.equalsIgnoreCase("CR")) {
							deposit = amount;
						} else {
							withdrawal = amount;
						}
					}

					prevBalance = balance;

					//Prpare Data For Transaction
					Date dbDate = parseDate(date);
					double dbBalance = Double.parseDouble(balance);
					double dbDeposit = deposit.isEmpty() || !deposit.matches("\\d+(\\.\\d+)?") ? 0.0 : Double.parseDouble(deposit.trim());
					double dbWithdrawal = withdrawal.isEmpty() || !withdrawal.matches("\\d+(\\.\\d+)?") ? 0.0 : Double.parseDouble(withdrawal.trim());

					transactions.add(new BankstatementData(narration, dbDeposit, dbWithdrawal, dbDate, dbBalance, category));

					data.add(new String[]{date, narration, deposit, withdrawal, balance});
				}

			}
		}

		saveAllBankStatements(transactions);

		return data;
	}

	private String validate(String data) {
		if (data == null) {
			return "error";
		}
		try {
			// Remove commas before parsing
			data = data.replace(",", "");

			if (!data.matches("-?\\d+(\\.\\d+)?")){
				return "error";
			}
			return data;

		} catch (NumberFormatException e) {
			return "error";
		}
	}

	private List<String[]> extractFromExcel(MultipartFile file) throws IOException, ParseException {
		List<String[]> data = new ArrayList<>();
		List<BankstatementData> transactions = new ArrayList<>();
		Workbook workbook = WorkbookFactory.create(file.getInputStream());
		Sheet sheet = workbook.getSheetAt(0);

		for (Row row : sheet) {
			String[] rowData = new String[row.getLastCellNum()]; // Date, Description, Deposit, Withdrawal, Balance

			for (int i = 0; i < row.getLastCellNum(); i++) {
				Cell cell = row.getCell(i);
				rowData[i] = (cell != null) ? getStringCellValue(cell) : "";
			}

			if (rowData[1].matches("^\\d{2}-\\d{2}-\\d{4}.*") || rowData[1].matches("^\\d{2}/\\d{2}/\\d{4}.*") || rowData[1].matches("^\\d{2}/\\d{2}/\\d{2}.*") || rowData[1].matches("^\\d{2}-\\d{2}-\\d{2}.*")) {
				String date = rowData[1];
				String narration = rowData[3];
				String withdrawal = rowData[5];
				String deposit = rowData[6];
				String balance = rowData[7];
				String category = TransactionCategory.categorize(narration);

				//Prpare Data For Transaction
				Date dbDate = parseDate(date);
				double dbBalance = Double.parseDouble(balance);
				double dbDeposit = deposit.isEmpty() || !deposit.matches("\\d+(\\.\\d+)?") ? 0.0 : Double.parseDouble(deposit.trim());
				double dbWithdrawal = withdrawal.isEmpty() || !withdrawal.matches("\\d+(\\.\\d+)?") ? 0.0 : Double.parseDouble(withdrawal.trim());

				transactions.add(new BankstatementData(narration, dbDeposit, dbWithdrawal, dbDate, dbBalance, category));
			}

			data.add(rowData);
		}

		workbook.close();
		saveAllBankStatements(transactions);
		return data;
	}

	public void saveAllBankStatements(List<BankstatementData> transactions) {
		try {
			transactionRepo.saveAll(transactions);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static Date parseDate(String date) throws ParseException {
		String[] formats = {"dd-MM-yyyy", "dd/MM/yyyy", "dd/MM/yy", "dd-MM-yy"};
		Date baseYear2000 = new SimpleDateFormat("yyyy").parse("2000");

		for (String format : formats) {
			try {
				if ((format.equals("dd/MM/yy") || format.equals("dd-MM-yy")) && date.length() != 8) {
					continue; // Skip if input is not like "dd/MM/yy"
				}
				if ((format.equals("dd/MM/yyyy") || format.equals("dd-MM-yyyy")) && date.length() != 10) {
					continue; // Skip if input is not like "dd/MM/yyyy"
				}

				SimpleDateFormat sdf = new SimpleDateFormat(format);
				sdf.setLenient(false);

				if (format.equals("dd/MM/yy") || format.equals("dd-MM-yy")) {
					sdf.set2DigitYearStart(baseYear2000);
				}

				return sdf.parse(date);
			} catch (ParseException ignored) {
				// Try next format
			}
		}

		throw new ParseException("Unparseable date: " + date, 0);
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
