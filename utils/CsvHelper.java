package utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CsvHelper {

    public static List<String[]> parseCSV(File file) {
        List<String[]> records = new ArrayList<>();
        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] values = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
                records.add(values);
            }
        } catch (FileNotFoundException e) {
            System.err.println("File not found: " + e.getMessage());
        }
        return records;
    }

    public static void writeCSV(List<String[]> data, File file) {
        try (PrintWriter writer = new PrintWriter(file)) {
            for (String[] record : data) {
                String csvLine = convertToCSV(record);
                writer.println(csvLine);
            }
        } catch (FileNotFoundException e) {
            System.err.println("File not found: " + e.getMessage());
        }
    }

    public static String convertToCSV(String[] data) {
        return Stream.of(data)
                .map(CsvHelper::escapeSpecialCharacters)
                .collect(Collectors.joining(","));
    }

    private static String escapeSpecialCharacters(String data) {
        if (data == null) {
            throw new IllegalArgumentException("Input data cannot be null");
        }
        String escapedData = data.replaceAll("\\R", " ");
        if (data.contains(",") || data.contains("\"") || data.contains("'")) {
            data = data.replace("\"", "\"\"");
            escapedData = "\"" + data + "\"";
        }
        return escapedData;
    }
}
