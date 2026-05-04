package services;

import java.time.Instant;
import java.util.List;

import models.Category;

public class CategoryService {

    private List<Category> categories;
    private ExpenseService expenseService;

    public CategoryService(List<Category> categories, ExpenseService expenseService) {
        this.categories = categories;
        this.expenseService = expenseService;
    }

    public List<Category> getAllCategories() {
        return categories;
    }

    public List<String[]> getAllCategoriesAsCSV() {
        return categories.stream()
                .map(this::categoryToArray)
                .toList();
    }

    private String[] categoryToArray(Category category) {
        return new String[]{
                String.valueOf(category.getId()),
                category.getName(),
                category.getCreatedAt().toString(),
                category.getUpdatedAt() != null ? category.getUpdatedAt().toString() : "null"
        };
    }

    public int addCategory(String name) {
        int lastId = categories.size() > 0 ? categories.get(categories.size() - 1).getId() : 0;
        int newId = lastId + 1;
        Category newCategory = new Category(newId, name, Instant.now(), null);
        categories.add(newCategory);
        return newId;
    }

    public boolean updateCategory(int id, String newName) {
        for (Category category : categories) {
            if (category.getId() == id) {
                category.setName(newName);
                category.setUpdatedAt(Instant.now());
                return true;
            }
        }
        return false;
    }

    public boolean deleteCategory(int id) {
        boolean removed = categories.removeIf(category -> category.getId() == id);
        if (removed) {
            expenseService.setExpensesToUncategorizedByCategoryId(id);
        }
        return removed;
    }

    public String getCategoryNameById(int categoryId) {
        return categories.stream()
                .filter(category -> category.getId() == categoryId)
                .map(Category::getName)
                .findFirst()
                .orElse("Uncategorized");
    }
}
