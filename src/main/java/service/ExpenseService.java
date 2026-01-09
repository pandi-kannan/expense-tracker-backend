package com.example.expensetracker.service;

import com.example.expensetracker.model.Expense;
import com.example.expensetracker.model.User;
import com.example.expensetracker.repository.ExpenseRepository;
import com.example.expensetracker.repository.UserRepository;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;

    @Autowired
    public ExpenseService(ExpenseRepository expenseRepository, UserRepository userRepository) {
        this.expenseRepository = expenseRepository;
        this.userRepository = userRepository;
    }

    public List<Expense> getAll() {
        return expenseRepository.findAll();
    }

    public Expense save(Expense expense) {
        return expenseRepository.save(expense);
    }

    public Expense getById(Long id) {
        return expenseRepository.findById(id).orElse(null);
    }

    public void delete(Long id) {
        expenseRepository.deleteById(id);
    }

    public Expense update(Long id, Expense updated) {
        Expense existing = expenseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Expense not found"));
        existing.setTitle(updated.getTitle());
        existing.setAmount(updated.getAmount());
        existing.setCategory(updated.getCategory());
        existing.setDate(updated.getDate());
        return expenseRepository.save(existing);
    }
    public List<Object[]> getMonthlyTotals(Long userId) { return expenseRepository.getMonthlyTotals(userId); }
    public List<Object[]> getMonthlyAverage(Long userId) { return expenseRepository.getMonthlyAverage(userId); }
    public List<Object[]> getCategoryTotals(Long userId) { return expenseRepository.getCategoryTotals(userId); }
    public List<Object[]> getMaxPerCategory(Long userId) { return expenseRepository.getMaxPerCategory(userId); }

    public Double getMonthlyTotal(Long userId, Integer year, Integer month) {
        return expenseRepository.getMonthlyTotal(userId, year, month);
    }

    public Double getYearlyTotal(Long userId, Integer year) {
        return expenseRepository.getYearlyTotal(userId, year);
    }

    public Double getTotalBetween(Long userId, String start, String end) {
        return expenseRepository.getTotalBetweenDates(userId, start, end);
    }

    public List<Object[]> getTotalsByCategory(Long userId) {
        return expenseRepository.getTotalsByCategory(userId);
    }

    public List<Object[]> getTotalsByTitle(Long userId) {
        return expenseRepository.getTotalsByTitle(userId);
    }

    public ByteArrayInputStream exportToPDF(Long userId) throws IOException, DocumentException {
        List<Expense> expenses = expenseRepository.findByUserId(userId);

        Document document = new Document();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, out);
        document.open();

        com.itextpdf.text.Font titleFont = com.itextpdf.text.FontFactory.getFont(com.itextpdf.text.FontFactory.HELVETICA_BOLD, 16);
        Paragraph title = new Paragraph("Expense Report", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        User user = userRepository.findById(userId).orElse(null);
        document.add(new Paragraph("User ID: " + userId));
        document.add(new Paragraph("User Name: " + (user != null ? user.getName() : "")));
        document.add(Chunk.NEWLINE);

        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.addCell("ID"); table.addCell("Title"); table.addCell("Amount"); table.addCell("Category"); table.addCell("Date");

        double total = 0;
        for (Expense e : expenses) {
            table.addCell(String.valueOf(e.getId()));
            table.addCell(e.getTitle());
            table.addCell(String.valueOf(e.getAmount()));
            table.addCell(e.getCategory());
            table.addCell(e.getDate());
            total += e.getAmount();
        }

        document.add(table);
        document.add(Chunk.NEWLINE);
        Paragraph totalPara = new Paragraph("Total Amount Spent: $" + total);
        totalPara.setAlignment(Element.ALIGN_RIGHT);
        document.add(totalPara);

        document.close();
        return new ByteArrayInputStream(out.toByteArray());
    }

    public ByteArrayInputStream exportToExcel(Long userId) throws IOException {
        List<Expense> expenses = expenseRepository.findByUserId(userId);

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Expenses");

        Row topRow = sheet.createRow(0);
        topRow.createCell(0).setCellValue("User ID: " + userId);
        User user = userRepository.findById(userId).orElse(null);
        topRow.createCell(1).setCellValue("User Name: " + (user != null ? user.getName() : ""));

        Row header = sheet.createRow(2);
        header.createCell(0).setCellValue("ID");
        header.createCell(1).setCellValue("Title");
        header.createCell(2).setCellValue("Amount");
        header.createCell(3).setCellValue("Category");
        header.createCell(4).setCellValue("Date");

        int rowIdx = 3;
        double total = 0;
        for (Expense e : expenses) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(e.getId());
            row.createCell(1).setCellValue(e.getTitle());
            row.createCell(2).setCellValue(e.getAmount());
            row.createCell(3).setCellValue(e.getCategory());
            row.createCell(4).setCellValue(e.getDate());
            total += e.getAmount();
        }

        Row totalRow = sheet.createRow(rowIdx + 1);
        totalRow.createCell(1).setCellValue("Total Amount Spent");
        totalRow.createCell(2).setCellValue(total);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();

        return new ByteArrayInputStream(out.toByteArray());
    }
}
