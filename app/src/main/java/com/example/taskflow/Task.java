package com.example.taskflow;

// Simple model class for a Task
public class Task {
    private String title;

    // Constructor
    public Task(String title) {
        this.title = title;
    }

    // Getter for the task title
    public String getTitle() {
        return title;
    }

    // Setter for the task title (needed for editing)
    public void setTitle(String title) {
        this.title = title;
    }
}
