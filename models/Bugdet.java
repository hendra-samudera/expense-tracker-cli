package models;

import java.time.Instant;

import enums.Month;

public class Bugdet {
    
    private Month month;
    private int year;
    private double maxAmount;
    private double currentAmount;
    private Instant createdAt;
    private Instant updatedAt;

    public Bugdet(Month month, int year, double maxAmount, double currentAmount, Instant createdAt, Instant updatedAt) {
        this.month = month;
        this.year = year;
        this.maxAmount = maxAmount;
        this.currentAmount = currentAmount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Month getMonth() {
        return month;
    }

    public void setMonth(Month month) {
        this.month = month;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public double getMaxAmount() {
        return maxAmount;
    }

    public void setMaxAmount(double maxAmount) {
        this.maxAmount = maxAmount;
    }

    public double getCurrentAmount() {
        return currentAmount;
    }

    public void setCurrentAmount(double currentAmount) {
        this.currentAmount = currentAmount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
