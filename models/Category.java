package models;

import java.time.Instant;

public class Category {

    private int id;
    private String name;
    private Instant createdAt;
    private Instant updatedAt;

    public Category(int id, String name, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
    public void setName(String name) {
        this.name = name;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
