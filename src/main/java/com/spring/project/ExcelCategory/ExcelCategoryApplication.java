package com.spring.project.ExcelCategory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan
@AutoConfiguration
public class ExcelCategoryApplication {

	public static void main(String[] args) {
		SpringApplication.run(ExcelCategoryApplication.class, args);
	}

}
