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
import android.view.inputmethod.InputMethodManager;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


// ... imports remain the same

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
        loadTasks();
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        taskAdapter = new TaskAdapter(tasks, new TaskAdapter.OnTaskLongClickListener() {
            @Override
            public void onTaskLongClicked(int position) {
                showDeleteTaskDialog(position);
            }
        });
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
    private void saveTasks() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        JSONArray jsonArray = new JSONArray();
        for (Task task : tasks) {
            JSONObject obj = new JSONObject();
            try {
                obj.put("title", task.getTitle());
                // If you later add more fields (e.g., due date), add them here.
            } catch (JSONException e) {
                e.printStackTrace();
            }
            jsonArray.put(obj);
        }
        editor.putString(TASKS_KEY, jsonArray.toString());
        editor.apply();
    }

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
                    // If you add more fields later, retrieve them here.
                    tasks.add(new Task(title));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
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
                        saveTasks();
                        updateViewVisibility();

                        // Scroll to the bottom (latest task)
                        recyclerView.scrollToPosition(tasks.size() - 1);
                        hideKeyboard(editText);

                    } else {
                        Toast.makeText(this, "Task title cannot be empty", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void showDeleteTaskDialog(int position) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Task")
                .setMessage("Are you sure you want to delete this task?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    tasks.remove(position);
                    taskAdapter.notifyItemRemoved(position);
                    saveTasks();
                    updateViewVisibility();
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
