package com.example.taskflow;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private List<Task> tasks = new ArrayList<>();
    private TaskAdapter taskAdapter;
    private RecyclerView recyclerView;
    private View welcomeText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setup RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        taskAdapter = new TaskAdapter(tasks);
        recyclerView.setAdapter(taskAdapter);

        welcomeText = findViewById(R.id.welcomeText);

        FloatingActionButton fabAddTask = findViewById(R.id.fabAddTask);
        fabAddTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddTaskDialog();
            }
        });
    }

    private void showAddTaskDialog() {
        final EditText editText = new EditText(this);
        editText.setHint("Enter task title");

        new AlertDialog.Builder(this)
                .setTitle("Add Task")
                .setView(editText)
                .setPositiveButton("Add", (dialog, which) -> {
                    String taskTitle = editText.getText().toString().trim();
                    if (!taskTitle.isEmpty()) {
                        tasks.add(new Task(taskTitle));
                        taskAdapter.notifyItemInserted(tasks.size() - 1);
                        updateViewVisibility();
                    } else {
                        Toast.makeText(this, "Task title cannot be empty", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateViewVisibility() {
        if (tasks.isEmpty()) {
            welcomeText.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            welcomeText.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateViewVisibility();
    }
}
