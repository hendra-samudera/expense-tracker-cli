import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import models.Expense;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import services.ExpenseService;
import utils.CsvHelper;

@Command(name = "expense-tracker", mixinStandardHelpOptions = true, version = "expense 1.0",
         description = "Tracks and manages your expenses.")
public class ExpenseTrackerMain implements Callable<Integer>{
    private final static Path FILE_PATH = Path.of("expense.csv");

    @Parameters(index = "0", description = "Command to execute: add, delete, update, list, summary")
    private String command;

    @Option(names = {"-d", "--description"}, description = "Description of the expense")
    private String description;

    @Option(names = {"-a", "--amount"}, description = "Amount of the expense")
    private double amount;

    @Override
    public Integer call() throws Exception {
        List<String[]> expenses = new ArrayList<String[]>();
        if (Files.exists(FILE_PATH)) {
            expenses = CsvHelper.parseCSV(FILE_PATH.toFile());
        }
        List<Expense> expenseList = new ArrayList<>();
        for (String[] record : expenses) {
            if (record.length >= 5) {
                try {
                    int id = Integer.parseInt(record[0]);
                    String desc = record[1];
                    double amt = Double.parseDouble(record[2]);
                    int categoryId = Integer.parseInt(record[3]);
                    Instant createdAt = Instant.parse(record[4]);
                    Instant updatedAt = record.length > 5 && !"null".equals(record[5]) ? Instant.parse(record[5]) : null;
                    expenseList.add(new Expense(id, desc, amt, categoryId, createdAt, updatedAt));
                } catch (NumberFormatException e) {
                    System.err.println("Skipping invalid record: " + String.join(",", record));
                }
            } else {
                System.err.println("Skipping incomplete record: " + String.join(",", record));
            }
        }

        ExpenseService expenseService = new ExpenseService(expenseList);
        
        switch (command) {
            case "add":
                expenseService.addExpense(description, amount, 0);
                utils.CsvHelper.writeCSV(expenseService.getAllExpensesAsCSV(), FILE_PATH.toFile());
                break;
            case "delete":
                
                break;
            case "update":
                
                break;
            case "list":
                
                break;
            case "summary":
                
                break;
            default:
                break;
        }

        return 0;
    }

    

    public static void main(String[] args) {
        int exitCode = new CommandLine(new ExpenseTrackerMain()).execute(args);
        System.exit(exitCode);
    }
}
