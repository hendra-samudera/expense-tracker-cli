package services;

import java.time.Instant;
import java.time.Month;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

import models.Expense;

public class ExpenseService {

    private List<Expense> expenses;

    public ExpenseService(List<Expense> expenses) {
        this.expenses = expenses;
    }

    public List<Expense> getAllExpenses() {
        return expenses;
    }

    public List<String[]> getAllExpensesAsCSV() {
        return expenses.stream()
                .map(this::expenseToArray)
                .toList();
    }

    public int addExpense(String description, double amount, Integer categoryId) {
        int lastId = expenses.size() > 0 ? expenses.get(expenses.size() - 1).getId() : 0;
        int newId = lastId + 1;
        int finalCategoryId = categoryId == null ? 0 : categoryId;
        Expense newExpense = new Expense(newId, description, amount, finalCategoryId, Instant.now(), null);
        expenses.add(newExpense);
        return newId;
    }

    public boolean deleteExpense(int id) {
        return expenses.removeIf(expense -> expense.getId() == id);
    }

    public void summarizeExpenses(Month month) {
        if (month == null) {
            double summary = expenses.stream()
                    .collect(
                            Collectors.summingDouble(Expense::getAmount));
            System.out.printf("Total Amount for all months: %.2f%n", summary);
            return;
        }
        double summary = expenses.stream()
                .filter(expense -> expense.getCreatedAt().atZone(ZoneId.systemDefault()).getMonth() == month)
                .collect(
                        Collectors.summingDouble(Expense::getAmount));
        System.out.printf("Total Amount for %s: %.2f%n", month, summary);
    }

    public boolean updateExpense(int id, String description, double amount, Integer categoryId) {
        for (Expense expense : expenses) {
            if (expense.getId() == id) {
                expense.setDescription(description == null ? expense.getDescription() : description);
                expense.setAmount(amount == 0 ? expense.getAmount() : amount);
                expense.setCategoryId(categoryId == null ? expense.getCategoryId() : categoryId);
                expense.setUpdatedAt(Instant.now());
                return true;
            }
        }
        return false;
    }

    public void setExpensesToUncategorizedByCategoryId(int categoryId) {
        for (Expense expense : expenses) {
            if (expense.getCategoryId() == categoryId) {
                expense.setCategoryId(0);
                expense.setUpdatedAt(Instant.now());
            }
        }
    }

    private String[] expenseToArray(Expense expense) {
        return new String[] {
                String.valueOf(expense.getId()),
                expense.getDescription(),
                String.valueOf(expense.getAmount()),
                String.valueOf(expense.getCategoryId()),
                expense.getCreatedAt().toString(),
                expense.getUpdatedAt() != null ? expense.getUpdatedAt().toString() : "null"
        };
    }
}
