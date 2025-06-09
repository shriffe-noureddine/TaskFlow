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
import android.content.Context;
import android.content.SharedPreferences;
import android.view.inputmethod.InputMethodManager;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.google.android.material.snackbar.Snackbar;

public class MainActivity extends AppCompatActivity {
    private List<Task> tasks = new ArrayList<>();
    private TaskAdapter taskAdapter;
    private RecyclerView recyclerView;
    private View welcomeText;
    private static final String PREFS_NAME = "taskflow_prefs";
    private static final String TASKS_KEY = "tasks";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Load saved tasks before setting up the adapter
        loadTasks();

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize adapter with both long-click and click listeners
        taskAdapter = new TaskAdapter(
                tasks,
                new TaskAdapter.OnTaskLongClickListener() {
                    @Override
                    public void onTaskLongClicked(int position) {
                        showDeleteTaskDialog(position);
                    }
                },
                new TaskAdapter.OnTaskClickListener() {
                    @Override
                    public void onTaskClicked(int position) {
                        showEditTaskDialog(position);
                    }
                }
        );
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

    // Show dialog to add a new task
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
                        saveTasks();
                        updateViewVisibility();
                        recyclerView.scrollToPosition(tasks.size() - 1);
                        hideKeyboard(editText);
                    } else {
                        Toast.makeText(this, "Task title cannot be empty", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // Show dialog to edit an existing task
    private void showEditTaskDialog(int position) {
        final EditText editText = new EditText(this);
        editText.setText(tasks.get(position).getTitle());
        editText.setSelection(editText.getText().length());

        new AlertDialog.Builder(this)
                .setTitle("Edit Task")
                .setView(editText)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newTitle = editText.getText().toString().trim();
                    if (!newTitle.isEmpty()) {
                        tasks.get(position).setTitle(newTitle);
                        taskAdapter.notifyItemChanged(position);
                        saveTasks();
                    } else {
                        Toast.makeText(this, "Task title cannot be empty", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // Show dialog to delete a task, with undo support
    private void showDeleteTaskDialog(int position) {
        Task deletedTask = tasks.get(position);
        new AlertDialog.Builder(this)
                .setTitle("Delete Task")
                .setMessage("Are you sure you want to delete this task?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    tasks.remove(position);
                    taskAdapter.notifyItemRemoved(position);
                    saveTasks();
                    updateViewVisibility();

                    // Show Snackbar for undo
                    Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),
                            "Task deleted", Snackbar.LENGTH_LONG);
                    snackbar.setAction("Undo", v -> {
                        tasks.add(position, deletedTask);
                        taskAdapter.notifyItemInserted(position);
                        saveTasks();
                        updateViewVisibility();
                        recyclerView.scrollToPosition(position);
                    });
                    snackbar.show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // Save tasks to SharedPreferences as JSON
    private void saveTasks() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        JSONArray jsonArray = new JSONArray();
        for (Task task : tasks) {
            JSONObject obj = new JSONObject();
            try {
                obj.put("title", task.getTitle());
                // Add other fields here if needed
            } catch (JSONException e) {
                e.printStackTrace();
            }
            jsonArray.put(obj);
        }
        editor.putString(TASKS_KEY, jsonArray.toString());
        editor.apply();
    }

    // Load tasks from SharedPreferences
    private void loadTasks() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String jsonString = prefs.getString(TASKS_KEY, null);
        tasks.clear();
        if (jsonString != null) {
            try {
                JSONArray jsonArray = new JSONArray(jsonString);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject obj = jsonArray.getJSONObject(i);
                    String title = obj.getString("title");
                    tasks.add(new Task(title));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    // Hide keyboard utility
    private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    // Show/hide welcome message vs. task list
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
