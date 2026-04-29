import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import models.Expense;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;
import services.ExpenseService;
import utils.CsvHelper;
import validations.ExpenseValidation;

@Command(name = "expense-tracker", mixinStandardHelpOptions = true, version = "expense 1.0",
         description = "Tracks and manages your expenses.")
public class ExpenseTrackerMain implements Callable<Integer>{
    private final static Path FILE_PATH = Path.of("expense.csv");

    @Spec CommandSpec spec;

    @Parameters(index = "0", description = "Command to execute: add, delete, update, list, summary")
    private String command;

    @Option(names = {"-d", "--description"}, description = "Description of the expense")
    private String description;

    @Option(names = {"-a", "--amount"}, description = "Amount of the expense")
    private double amount;

    @Option(names = {"-i", "--id"}, description = "ID of the expense")  
    private Integer expenseId;

    @Option(names = {"-m", "--month"}, description = "Month for summary (e.g., JANUARY, FEBRUARY, etc.)")
    private Month month;

    @Override
    public Integer call() throws Exception {
        if (!ExpenseValidation.validateAmount(amount)) {
            throw new ParameterException(spec.commandLine(),
                    "Amount must be a non-negative number.");
        }

        List<String[]> expenses = new ArrayList<String[]>();
        if (Files.exists(FILE_PATH)) {
            expenses = CsvHelper.parseCSV(FILE_PATH.toFile());
        }
        List<Expense> expenseList = getExpenseList(expenses);

        ExpenseService expenseService = new ExpenseService(expenseList);
        
        switch (command) {
            case "add":
                if(!ExpenseValidation.validateDescription(description)) {
                    throw new ParameterException(spec.commandLine(),
                            "Description must be non-empty and less than 50 characters.");
                }
                
                int newExpenseId = expenseService.addExpense(description, amount, 0);
                if (newExpenseId == -1) {
                    System.err.println("Failed to add expense. Please check the input values.");
                } else {
                    CsvHelper.writeCSV(expenseService.getAllExpensesAsCSV(), FILE_PATH.toFile());
                    System.out.println("Expense added with ID: " + newExpenseId);
                }
                break;
            case "delete":
                if (expenseId == null) {
                    throw new ParameterException(spec.commandLine(),
                            "Expense ID must be provided for delete operation.");
                }
                if(!expenseService.deleteExpense(expenseId)) {
                    System.err.println("Expense with ID " + expenseId + " not found. No deletion performed.");
                }else{
                    CsvHelper.writeCSV(expenseService.getAllExpensesAsCSV(), FILE_PATH.toFile());
                    System.out.println("Expense with ID " + expenseId + " deleted successfully.");
                }
                break;
            case "update":
                if (expenseId == null) {
                    throw new ParameterException(spec.commandLine(),
                            "Expense ID must be provided for update operation.");
                }
                boolean validDescription = description == null || ExpenseValidation.validateDescription(description);
                boolean validAmount = amount == 0 || ExpenseValidation.validateAmount(amount);

                if(!validDescription && !validAmount) {
                    throw new ParameterException(spec.commandLine(),
                            "--description or --amount must be provided for update operation.");
                }
                else if(!validDescription) {
                    throw new ParameterException(spec.commandLine(),
                            "--description must be non-empty and less than 50 characters.");
                }
                else if(!validAmount) {
                    throw new ParameterException(spec.commandLine(),
                            "--amount must be a non-negative number.");
                }

                if(!expenseService.updateExpense(expenseId, description, amount, 0)) {
                    System.err.println("Expense with ID " + expenseId + " not found. No update performed.");
                }else {
                    CsvHelper.writeCSV(expenseService.getAllExpensesAsCSV(), FILE_PATH.toFile());
                    System.out.println("Expense with ID " + expenseId + " updated successfully.");
                }
                break;
            case "list":
                expenseService.listExpenses();
                break;
            case "summary":
                expenseService.summarizeExpenses(month);
                break;
            default:
                throw new ParameterException(spec.commandLine(),
                        "Invalid command: " + command + ". Valid commands are: add, delete, update, list, summary.");
        }

        return 0;
    }

    private List<Expense> getExpenseList(List<String[]> expenses) {
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
        return expenseList;
    }

    

    public static void main(String[] args) {
        int exitCode = new CommandLine(new ExpenseTrackerMain()).execute(args);
        System.exit(exitCode);
    }
}
