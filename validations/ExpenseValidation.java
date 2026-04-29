package validations;

public class ExpenseValidation {

    private static final int MAX_DESCRIPTION_LENGTH = 50;
    
    public static boolean validateAmount(double amount) {
        if (amount < 0) {
            return false;
        }
        return true;
    }

    public static boolean validateDescription(String description) {
        if (description == null || description.trim().isEmpty()) {
            return false;
        }
        if (description.length() > MAX_DESCRIPTION_LENGTH) {
            return false;
        }
        return true;
    }
}
