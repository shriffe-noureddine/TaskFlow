package com.taskflow;
import com.google.android.material.snackbar.Snackbar;
import android.view.ViewGroup;
import android.app.DatePickerDialog;
import android.widget.ImageButton;
import java.util.Calendar;
import android.view.Menu;
import android.view.MenuItem;
import androidx.appcompat.app.AppCompatDelegate;
import android.content.SharedPreferences;
import androidx.appcompat.widget.Toolbar;

import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
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

    private static final String PREFS_NAME = "taskflow_prefs";
    private static final String TASKS_KEY = "tasks";

    private List<Task> tasks = new ArrayList<>();
    private TaskAdapter taskAdapter;
    private RecyclerView recyclerView;
    private View welcomeText;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_toggle_theme) {
            toggleDarkMode();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void hideKeyboard(View view) {
        InputMethodManager imm = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        }
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void toggleDarkMode() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean darkMode = prefs.getBoolean("dark_mode", false);
        darkMode = !darkMode;
        prefs.edit().putBoolean("dark_mode", darkMode).apply();

        AppCompatDelegate.setDefaultNightMode(
                darkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );
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
            jsonArray.put(obj);  // Add this task's data to the array
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
                    JSONObject obj = jsonArray.getJSONObject(i);  // This obj comes from the stored array
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

    private void showEditTaskDialog(int position) {
        Task task = tasks.get(position);
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_task, null);
        EditText inputTitle = dialogView.findViewById(R.id.inputTitle);
        EditText inputDescription = dialogView.findViewById(R.id.inputDescription);
        EditText inputDueDate = dialogView.findViewById(R.id.inputDueDate);

        // Pre-fill with existing values
        inputTitle.setText(task.getTitle());
        inputDescription.setText(task.getDescription());
        inputDueDate.setText(task.getDueDate());

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
        ImageButton btnPickDate = dialogView.findViewById(R.id.btnPickDate);

        View.OnClickListener pickDateListener = v -> {
            // Get current date as default
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    (view, year1, monthOfYear, dayOfMonth) -> {
                        // monthOfYear is zero-based, so add 1
                        String selectedDate = String.format("%04d-%02d-%02d", year1, monthOfYear + 1, dayOfMonth);
                        inputDueDate.setText(selectedDate);
                    },
                    year, month, day);
            datePickerDialog.show();
        };

        inputDueDate.setOnClickListener(pickDateListener);
        btnPickDate.setOnClickListener(pickDateListener);


    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean darkMode = prefs.getBoolean("dark_mode", false);
        AppCompatDelegate.setDefaultNightMode(
                darkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );

        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        loadTasks();
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

        welcomeText = findViewById(R.id.welcomeText);

        FloatingActionButton fabAddTask = findViewById(R.id.fabAddTask);
        fabAddTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddTaskDialog();
            }
        });
    }


    private void showDeleteTaskDialog(int position) {
        Task deletedTask = tasks.get(position); // Save for undo
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
                        // Restore the task
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


    private void updateViewVisibility() {
        if (tasks.isEmpty()) {
            welcomeText.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            welcomeText.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }
    private void showAddTaskDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_task, null);
        EditText inputTitle = dialogView.findViewById(R.id.inputTitle);
        EditText inputDescription = dialogView.findViewById(R.id.inputDescription);
        EditText inputDueDate = dialogView.findViewById(R.id.inputDueDate);

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
        ImageButton btnPickDate = dialogView.findViewById(R.id.btnPickDate);

        View.OnClickListener pickDateListener = v -> {
            // Get current date as default
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    (view, year1, monthOfYear, dayOfMonth) -> {
                        // monthOfYear is zero-based, so add 1
                        String selectedDate = String.format("%04d-%02d-%02d", year1, monthOfYear + 1, dayOfMonth);
                        inputDueDate.setText(selectedDate);
                    },
                    year, month, day);
            datePickerDialog.show();
        };

        inputDueDate.setOnClickListener(pickDateListener);
        btnPickDate.setOnClickListener(pickDateListener);

    }




    @Override
    protected void onResume() {
        super.onResume();
        updateViewVisibility();
    }
}
