package com.spring.project.ExcelCategory.Controller;

import java.io.IOException;
import java.text.ParseException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.spring.project.ExcelCategory.Service.BankstatementService;


@RestController
@CrossOrigin(origins = "http://localhost:5173")
public class BankstatementController {
	
	@Autowired
    private BankstatementService bankStatementService;
	
	@PostMapping("/convert")
    public ResponseEntity<ByteArrayResource> convertStatement(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "password", required = false) String password
    ) throws Exception {

        byte[] convertedExcel = bankStatementService.extractAndExportData(file, password);
        ByteArrayResource resource = new ByteArrayResource(convertedExcel);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=processed_statement.xlsx")
                .body(resource);
    }
}
