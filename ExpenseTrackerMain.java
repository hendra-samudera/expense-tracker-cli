import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import models.Category;
import models.Expense;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;
import services.CategoryService;
import services.ExpenseService;
import utils.CsvHelper;
import utils.PrintHelper;
import validations.ExpenseValidation;

@Command(name = "expense-tracker", mixinStandardHelpOptions = true, version = "expense 1.0",
         description = "Tracks and manages your expenses.")
public class ExpenseTrackerMain implements Callable<Integer>{
    private final static Path EXPENSE_FILE_PATH = Path.of("expense.csv");
    private final static Path CATEGORY_FILE_PATH = Path.of("category.csv");

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

    @Option(names = {"-n", "--name"}, description = "Category name")
    private String categoryName;

    @Option(names = {"-c", "--category-id"}, description = "ID of the category")
    private Integer categoryId = null;

    @Override
    public Integer call() throws Exception {
        if (!ExpenseValidation.validateAmount(amount)) {
            throw new ParameterException(spec.commandLine(),
                    "Amount must be a non-negative number.");
        }

        List<String[]> expenses = new ArrayList<String[]>();
        if (Files.exists(EXPENSE_FILE_PATH)) {
            expenses = CsvHelper.parseCSV(EXPENSE_FILE_PATH.toFile());
        }
        List<Expense> expenseList = getExpenseList(expenses);

        ExpenseService expenseService = new ExpenseService(expenseList);

        List<String[]> categories = new ArrayList<String[]>();
        if (Files.exists(CATEGORY_FILE_PATH)) {
            categories = CsvHelper.parseCSV(CATEGORY_FILE_PATH.toFile());
        }
        List<Category> categoryList = getCategoryList(categories);
        CategoryService categoryService = new CategoryService(categoryList, expenseService);
        
        switch (command) {
            case "add":
                if(!ExpenseValidation.validateDescription(description)) {
                    throw new ParameterException(spec.commandLine(),
                            "Description must be non-empty and less than 50 characters.");
                }
                
                int newExpenseId = expenseService.addExpense(description, amount, categoryId);
                if (newExpenseId == -1) {
                    System.err.println("Failed to add expense. Please check the input values.");
                } else {
                    CsvHelper.writeCSV(expenseService.getAllExpensesAsCSV(), EXPENSE_FILE_PATH.toFile());
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
                    CsvHelper.writeCSV(expenseService.getAllExpensesAsCSV(), EXPENSE_FILE_PATH.toFile());
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

                if(!expenseService.updateExpense(expenseId, description, amount, categoryId)) {
                    System.err.println("Expense with ID " + expenseId + " not found. No update performed.");
                }else {
                    CsvHelper.writeCSV(expenseService.getAllExpensesAsCSV(), EXPENSE_FILE_PATH.toFile());
                    System.out.println("Expense with ID " + expenseId + " updated successfully.");
                }
                break;
            case "list":
                PrintHelper.printExpenseAsTable(expenseService.getAllExpenses(), categoryService.getAllCategories());
                break;
            case "summary":
                expenseService.summarizeExpenses(month);
                break;
            case "add-category":
                if (categoryName == null) {
                    throw new ParameterException(spec.commandLine(),
                            "Category name must be provided for add-category operation.");
                }
                int newCategoryId = categoryService.addCategory(categoryName);
                CsvHelper.writeCSV(categoryService.getAllCategoriesAsCSV(), CATEGORY_FILE_PATH.toFile());
                System.out.println("Category added with ID: " + newCategoryId);
                break;
            case "update-category":
                if (categoryId == null || categoryName == null) {
                    throw new ParameterException(spec.commandLine(),
                            "Category ID and new name must be provided for update-category operation.");
                }
                if(!categoryService.updateCategory(categoryId, categoryName)) {
                    System.err.println("Category with ID " + categoryId + " not found. No update performed.");
                }else {
                    CsvHelper.writeCSV(categoryService.getAllCategoriesAsCSV(), CATEGORY_FILE_PATH.toFile());
                    System.out.println("Category with ID " + categoryId + " updated successfully.");
                }
                break;
            case "list-categories":
                PrintHelper.printCategoryAsTable(categoryService.getAllCategories());
                break;
            case "delete-category":
                if (categoryId == null) {
                    throw new ParameterException(spec.commandLine(),
                            "Category ID must be provided for delete-category operation.");
                }
                if(!categoryService.deleteCategory(categoryId)) {
                    System.err.println("Category with ID " + categoryId + " not found. No deletion performed.");
                }else{
                    CsvHelper.writeCSV(categoryService.getAllCategoriesAsCSV(), CATEGORY_FILE_PATH.toFile());
                    CsvHelper.writeCSV(expenseService.getAllExpensesAsCSV(), EXPENSE_FILE_PATH.toFile());
                    System.out.println("Category with ID " + categoryId + " deleted successfully.");
                }
                break;
            default:
                throw new ParameterException(spec.commandLine(),        
                        "Invalid command: " + command + ". Valid commands are: add, delete, update, list, summary, add-category, list-categories.");
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

    private List<Category> getCategoryList(List<String[]> categories) {
        List<Category> categoryList = new ArrayList<>();
        for (String[] record : categories) {
            if (record.length >= 4) {
                try {
                    int id = Integer.parseInt(record[0]);
                    String name = record[1];
                    Instant createdAt = Instant.parse(record[2]);
                    Instant updatedAt = record.length > 3 && !"null".equals(record[3]) ? Instant.parse(record[3]) : null;
                    categoryList.add(new Category(id, name, createdAt, updatedAt));
                } catch (NumberFormatException e) {
                    System.err.println("Skipping invalid record: " + String.join(",", record));
                }
            } else {
                System.err.println("Skipping incomplete record: " + String.join(",", record));
            }
        }
        return categoryList;
    }
    

    public static void main(String[] args) {
        int exitCode = new CommandLine(new ExpenseTrackerMain()).execute(args);
        System.exit(exitCode);
    }
}
