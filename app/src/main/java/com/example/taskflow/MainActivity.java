package com.example.taskflow;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FloatingActionButton fabAddTask = findViewById(R.id.fabAddTask);

        fabAddTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show the dialog to add a new task
                showAddTaskDialog();
            }
        });
    }

    private void showAddTaskDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        final View dialogView = inflater.inflate(android.R.layout.simple_list_item_1, null);
        final EditText editText = new EditText(this);
        editText.setHint("Enter task title");

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Task")
                .setView(editText)
                .setPositiveButton("Add", (dialog, which) -> {
                    String taskTitle = editText.getText().toString().trim();
                    if (!taskTitle.isEmpty()) {
                        Toast.makeText(this, "Task added: " + taskTitle, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Task title cannot be empty", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
