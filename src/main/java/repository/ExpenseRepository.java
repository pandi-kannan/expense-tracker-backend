package com.example.expensetracker.repository;

import com.example.expensetracker.model.Expense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    List<Expense> findByUserId(Long userId);
    List<Expense> findByUserIdAndCategory(Long userId, String category);
    List<Expense> findByUserIdAndDateBetween(Long userId, String start, String end);

    Page<Expense> findByUserId(Long userId, Pageable pageable);

    @Query(
            value = "SELECT * FROM expense " +
                    "WHERE user_id = :userId " +
                    "AND (:year IS NULL OR YEAR(STR_TO_DATE(date, '%Y-%m-%d')) = :year) " +
                    "AND (:month IS NULL OR MONTH(STR_TO_DATE(date, '%Y-%m-%d')) = :month)",
            nativeQuery = true
    )
    List<Expense> findByUserAndOptionalDate(Long userId, Integer year, Integer month);

    @Query(value = "SELECT DATE_FORMAT(STR_TO_DATE(date, '%Y-%m-%d'), '%Y-%m') as month, SUM(amount) as total " +
            "FROM expense WHERE user_id = ?1 GROUP BY month", nativeQuery = true)
    List<Object[]> getMonthlyTotals(Long userId);

    @Query(value = "SELECT DATE_FORMAT(STR_TO_DATE(date, '%Y-%m-%d'), '%Y-%m') as month, AVG(amount) as average " +
            "FROM expense WHERE user_id = ?1 GROUP BY month", nativeQuery = true)
    List<Object[]> getMonthlyAverage(Long userId);

    @Query(value = "SELECT category, SUM(amount) as total FROM expense WHERE user_id = ?1 GROUP BY category", nativeQuery = true)
    List<Object[]> getCategoryTotals(Long userId);

    @Query(value = "SELECT category, MAX(amount) as maxAmount FROM expense WHERE user_id = ?1 GROUP BY category", nativeQuery = true)
    List<Object[]> getMaxPerCategory(Long userId);

    @Query(value = "SELECT SUM(amount) FROM expense " +
            "WHERE user_id = :userId AND YEAR(STR_TO_DATE(date, '%Y-%m-%d')) = :year AND MONTH(STR_TO_DATE(date, '%Y-%m-%d')) = :month",
            nativeQuery = true)
    Double getMonthlyTotal(Long userId, Integer year, Integer month);

    @Query(value = "SELECT SUM(amount) FROM expense " +
            "WHERE user_id = :userId AND YEAR(STR_TO_DATE(date, '%Y-%m-%d')) = :year",
            nativeQuery = true)
    Double getYearlyTotal(Long userId, Integer year);

    @Query(value = "SELECT SUM(amount) FROM expense " +
            "WHERE user_id = :userId AND date BETWEEN :start AND :end",
            nativeQuery = true)
    Double getTotalBetweenDates(Long userId, String start, String end);

    @Query(value = "SELECT category, SUM(amount) FROM expense " +
            "WHERE user_id = :userId GROUP BY category", nativeQuery = true)
    List<Object[]> getTotalsByCategory(Long userId);

    @Query(value = "SELECT title, SUM(amount) FROM expense " +
            "WHERE user_id = :userId GROUP BY title", nativeQuery = true)
    List<Object[]> getTotalsByTitle(Long userId);
}
