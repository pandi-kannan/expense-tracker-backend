package com.example.expensetracker.controller;

import com.example.expensetracker.model.Expense;
import com.example.expensetracker.service.ExpenseService;
import com.itextpdf.text.DocumentException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/expenses")
@CrossOrigin
public class ExpenseController {

    @Autowired
    private ExpenseService expenseService;

    @GetMapping
    public List<Expense> getAll() { return expenseService.getAll(); }

    @PostMapping
    public Expense save(@RequestBody Expense expense) { return expenseService.save(expense); }

    @GetMapping("/{id}")
    public Expense getById(@PathVariable Long id) { return expenseService.getById(id); }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) { expenseService.delete(id); }

    @PutMapping("/{id}")
    public ResponseEntity<Expense> update(@PathVariable Long id, @RequestBody Expense expense) {
        try {
            Expense updated = expenseService.update(id, expense);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/export/pdf")
    public ResponseEntity<InputStreamResource> exportPDF(@RequestParam Long userId) throws IOException, DocumentException {
        ByteArrayInputStream file = expenseService.exportToPDF(userId);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=expenses.pdf");
        return ResponseEntity.ok().headers(headers).contentType(MediaType.APPLICATION_PDF).body(new InputStreamResource(file));
    }

    @GetMapping("/export/excel")
    public ResponseEntity<InputStreamResource> exportExcel(@RequestParam Long userId) throws IOException {
        ByteArrayInputStream file = expenseService.exportToExcel(userId);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=expenses.xlsx");
        return ResponseEntity.ok().headers(headers).contentType(MediaType.parseMediaType("application/vnd.ms-excel")).body(new InputStreamResource(file));
    }

    @GetMapping("/summary/month")
    public Double getMonthlyTotal(@RequestParam Long userId, @RequestParam Integer year, @RequestParam Integer month) {
        return expenseService.getMonthlyTotal(userId, year, month);
    }

    @GetMapping("/summary/year")
    public Double getYearlyTotal(@RequestParam Long userId, @RequestParam Integer year) {
        return expenseService.getYearlyTotal(userId, year);
    }

    @GetMapping("/summary/between")
    public Double getTotalBetween(@RequestParam Long userId, @RequestParam String start, @RequestParam String end) {
        return expenseService.getTotalBetween(userId, start, end);
    }

    @GetMapping("/summary/category/totals")
    public List<Object[]> getTotalsByCategory(@RequestParam Long userId) {
        return expenseService.getTotalsByCategory(userId);
    }

    @GetMapping("/summary/title/totals")
    public List<Object[]> getTotalsByTitle(@RequestParam Long userId) {
        return expenseService.getTotalsByTitle(userId);
    }
}
