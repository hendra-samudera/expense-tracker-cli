package utils;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import models.Expense;

public class PrintHelper {

    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            .withZone(ZoneId.systemDefault());

    public static void printExpenseAsTable(List<Expense> expenses) {
        if (expenses.isEmpty()) {
            System.out.println("No expenses found.");
            return;
        }

        int descWidth = expenses.stream()
                .mapToInt(e -> e.getDescription().length())
                .max()
                .orElse(11);
        descWidth = Math.max(descWidth, 11);

        String fmt = "%-4s  %-12s  %-" + descWidth + "s  %-12s%n";

        System.out.printf(fmt, "ID", "Date", "Description", "Amount");
        System.out.println("-".repeat(4 + 2 + 12 + 2 + descWidth + 2 + 12 + 2));

        for (Expense expense : expenses) {
            System.out.printf(fmt,
                    expense.getId(),
                    dateTimeFormatter.format(expense.getCreatedAt()),
                    expense.getDescription(),
                    String.valueOf(expense.getAmount()));
        }
    }
}
