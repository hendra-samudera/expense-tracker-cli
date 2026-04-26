package services;

import java.time.Instant;
import java.util.List;

import models.Expense;

public class ExpenseService {

    private List<Expense> expenses;

    public ExpenseService(List<Expense> expenses) {
        this.expenses = expenses;
    }

    public List<Expense> getAllExpenses() {
        return expenses;
    }

    public int addExpense(String description, double amount, int categoryId) {
        int lastId = expenses.size() > 0 ? expenses.get(expenses.size() - 1).getId() : 0;
        int newId = lastId + 1;
        Expense newExpense = new Expense(newId, description, amount, categoryId, Instant.now(), null);
        expenses.add(newExpense);
        return newId;
    }

    public boolean deleteExpense(int id) {
        return expenses.removeIf(expense -> expense.getId() == id);
    }

    public boolean updateExpense(int id, String description, double amount, int categoryId) {
        for (Expense expense : expenses) {
            if (expense.getId() == id) {
                expense.setDescription(description == null ? expense.getDescription() : description);
                expense.setAmount(amount == 0 ? expense.getAmount() : amount);
                expense.setCategoryId(categoryId == 0 ? expense.getCategoryId() : categoryId);
                expense.setUpdatedAt(Instant.now());
                return true;
            }
        }
        return false;
    }
}
