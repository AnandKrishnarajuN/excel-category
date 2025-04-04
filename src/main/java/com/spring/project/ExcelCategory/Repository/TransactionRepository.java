package com.spring.project.ExcelCategory.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.spring.project.ExcelCategory.Model.BankstatementData;

@Repository
public interface TransactionRepository extends JpaRepository<BankstatementData, Long> {

}
