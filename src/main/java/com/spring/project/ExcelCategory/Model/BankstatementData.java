package com.spring.project.ExcelCategory.Model;

import java.util.Date;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;


@Entity
public class BankstatementData {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	private String description;
	private double deposit;
	private double withdrawl;
	private Date date;
	private double balance;
		
	
	
	public BankstatementData() {
		super();
	}
	public BankstatementData(String description, double deposit, double withdrawl, Date date, double balance) {
		super();
		this.description = description;
		this.deposit = deposit;
		this.withdrawl = withdrawl;
		this.date = date;
		this.balance = balance;
	}
	@Override
	public String toString() {
		return "BankstatementData [id=" + id + ", description=" + description + ", deposit=" + deposit + ", withdrawl="
				+ withdrawl + ", date=" + date + ", balance=" + balance + "]";
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public double getDeposit() {
		return deposit;
	}
	public void setDeposit(double deposit) {
		this.deposit = deposit;
	}
	public double getWithdrawl() {
		return withdrawl;
	}
	public void setWithdrawl(double withdrawl) {
		this.withdrawl = withdrawl;
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public double getBalance() {
		return balance;
	}
	public void setBalance(double balance) {
		this.balance = balance;
	}
	
}
