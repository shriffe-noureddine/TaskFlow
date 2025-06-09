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
import android.app.DatePickerDialog;
import android.widget.ImageButton;
import java.util.Calendar;
import androidx.appcompat.app.AppCompatDelegate;
import android.widget.Switch;


public class MainActivity extends AppCompatActivity {
    private List<Task> tasks = new ArrayList<>();
    private TaskAdapter taskAdapter;
    private RecyclerView recyclerView;
    private View welcomeText;
    private static final String PREFS_NAME = "taskflow_prefs";
    private static final String TASKS_KEY = "tasks";
    private static final String KEY_THEME_MODE = "theme_mode";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 1. Apply the saved theme mode before super.onCreate()
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int mode = prefs.getInt(KEY_THEME_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        AppCompatDelegate.setDefaultNightMode(mode);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 2. Set up the dark mode switch
        Switch switchTheme = findViewById(R.id.switchTheme);
        switchTheme.setChecked(mode == AppCompatDelegate.MODE_NIGHT_YES);
        switchTheme.setOnCheckedChangeListener((buttonView, isChecked) -> {
            int newMode = isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO;
            applyTheme(newMode);
            recreate(); // Restart activity to apply the theme
        });

        // 3. Load saved tasks
        loadTasks();

        // 4. Set up RecyclerView and adapter
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
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

        // 5. Set up welcome message
        welcomeText = findViewById(R.id.welcomeText);

        // 6. FloatingActionButton for adding tasks
        FloatingActionButton fabAddTask = findViewById(R.id.fabAddTask);
        fabAddTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddTaskDialog();
            }
        });
    }


    private void applyTheme(int mode) {
        AppCompatDelegate.setDefaultNightMode(mode);
        // Persist preference
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putInt(KEY_THEME_MODE, mode).apply();
    }

    private void showAddTaskDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_task, null);
        EditText inputTitle = dialogView.findViewById(R.id.inputTitle);
        EditText inputDescription = dialogView.findViewById(R.id.inputDescription);
        EditText inputDueDate = dialogView.findViewById(R.id.inputDueDate);
        ImageButton btnPickDate = dialogView.findViewById(R.id.btnPickDate);

        View.OnClickListener pickDateListener = v -> {
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    (view, year1, monthOfYear, dayOfMonth) -> {
                        String selectedDate = String.format("%04d-%02d-%02d", year1, monthOfYear + 1, dayOfMonth);
                        inputDueDate.setText(selectedDate);
                    },
                    year, month, day);
            datePickerDialog.show();
        };

        inputDueDate.setOnClickListener(pickDateListener);
        btnPickDate.setOnClickListener(pickDateListener);

        new AlertDialog.Builder(this)
                .setTitle("Add Task")
                .setView(dialogView)
                .setPositiveButton("Add", (dialog, which) -> {
                    String title = inputTitle.getText().toString().trim();
                    String desc = inputDescription.getText().toString().trim();
                    String due = inputDueDate.getText().toString().trim();

                    if (!title.isEmpty()) {
                        tasks.add(new Task(title, desc, due));
                        taskAdapter.notifyItemInserted(tasks.size() - 1);
                        saveTasks();
                        updateViewVisibility();
                        recyclerView.scrollToPosition(tasks.size() - 1);
                    } else {
                        Toast.makeText(this, "Task title cannot be empty", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showEditTaskDialog(int position) {
        Task task = tasks.get(position);
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_task, null);
        EditText inputTitle = dialogView.findViewById(R.id.inputTitle);
        EditText inputDescription = dialogView.findViewById(R.id.inputDescription);
        EditText inputDueDate = dialogView.findViewById(R.id.inputDueDate);
        ImageButton btnPickDate = dialogView.findViewById(R.id.btnPickDate);

        inputTitle.setText(task.getTitle());
        inputDescription.setText(task.getDescription());
        inputDueDate.setText(task.getDueDate());

        View.OnClickListener pickDateListener = v -> {
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    (view, year1, monthOfYear, dayOfMonth) -> {
                        String selectedDate = String.format("%04d-%02d-%02d", year1, monthOfYear + 1, dayOfMonth);
                        inputDueDate.setText(selectedDate);
                    },
                    year, month, day);
            datePickerDialog.show();
        };

        inputDueDate.setOnClickListener(pickDateListener);
        btnPickDate.setOnClickListener(pickDateListener);

        new AlertDialog.Builder(this)
                .setTitle("Edit Task")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String title = inputTitle.getText().toString().trim();
                    String desc = inputDescription.getText().toString().trim();
                    String due = inputDueDate.getText().toString().trim();

                    if (!title.isEmpty()) {
                        task.setTitle(title);
                        task.setDescription(desc);
                        task.setDueDate(due);
                        taskAdapter.notifyItemChanged(position);
                        saveTasks();
                    } else {
                        Toast.makeText(this, "Task title cannot be empty", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

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

    private void saveTasks() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        JSONArray jsonArray = new JSONArray();
        for (Task task : tasks) {
            JSONObject obj = new JSONObject();
            try {
                obj.put("title", task.getTitle());
                obj.put("description", task.getDescription());
                obj.put("dueDate", task.getDueDate());
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
                    String desc = obj.optString("description", "");
                    String due = obj.optString("dueDate", "");
                    tasks.add(new Task(title, desc, due));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
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
