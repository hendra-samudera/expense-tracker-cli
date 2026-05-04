package utils;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import models.Category;
import models.Expense;

public class PrintHelper {

    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
            .withZone(ZoneId.systemDefault());

    public static void printExpenseAsTable(List<Expense> expenses, List<Category> categories) {
        if (expenses.isEmpty()) {
            System.out.println("No expenses found.");
            return;
        }

        int descWidth = expenses.stream()
                .mapToInt(e -> e.getDescription().length())
                .max()
                .orElse(11);
        descWidth = Math.max(descWidth, 11);

        String fmt = "%-4s  %-16s  %-" + descWidth + "s  %-12s  %-12s%n";

        System.out.printf(fmt, "ID", "Date", "Description", "Amount", "Category");
        System.out.println("-".repeat(4 + 2 + 16 + 2 + descWidth + 2 + 12 + 2 + 12 + 2));

        for (Expense expense : expenses) {
            String categoryName = categories.stream()
                    .filter(c -> c.getId() == expense.getCategoryId())
                    .map(Category::getName)
                    .findFirst()
                    .orElse("Uncategorized");
            System.out.printf(fmt,
                    expense.getId(),
                    dateTimeFormatter.format(expense.getCreatedAt()),
                    expense.getDescription(),
                    String.valueOf(expense.getAmount()),
                    categoryName);
        }
    }

    public static void printCategoryAsTable(List<Category> categories) {
        if (categories.isEmpty()) {
            System.out.println("No categories found.");
            return;
        }

        int nameWidth = categories.stream()
                .mapToInt(c -> c.getName().length())
                .max()
                .orElse(4);
        nameWidth = Math.max(nameWidth, 4);

        String fmt = "%-4s  %-" + nameWidth + "s  %-20s  %-20s%n";

        System.out.printf(fmt, "ID", "Name", "Created At", "Updated At");
        System.out.println("-".repeat(4 + 2 + nameWidth + 2 + 20 + 2 + 20 + 2));

        for (Category category : categories) {
            System.out.printf(fmt,
                    category.getId(),
                    category.getName(),
                    dateTimeFormatter.format(category.getCreatedAt()),
                    category.getUpdatedAt() != null ? dateTimeFormatter.format(category.getUpdatedAt()) : "null");
        }
    }
}
