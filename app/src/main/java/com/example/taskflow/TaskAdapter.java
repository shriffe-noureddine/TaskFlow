package com.example.taskflow;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {


    private List<Task> tasks;
    private OnTaskLongClickListener longClickListener;
    private OnTaskClickListener clickListener;

    public TaskAdapter(List<Task> tasks,
                       OnTaskLongClickListener longClickListener,
                       OnTaskClickListener clickListener) {
        this.tasks = tasks;
        this.longClickListener = longClickListener;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = tasks.get(position);
        holder.titleView.setText(task.getTitle());

        // Description
        if (task.getDescription() != null && !task.getDescription().isEmpty()) {
            holder.descriptionView.setVisibility(View.VISIBLE);
            holder.descriptionView.setText(task.getDescription());
        } else {
            holder.descriptionView.setVisibility(View.GONE);
        }

        // Due date
        if (task.getDueDate() != null && !task.getDueDate().isEmpty()) {
            holder.dueDateView.setVisibility(View.VISIBLE);
            holder.dueDateView.setText("Due: " + task.getDueDate());
        } else {
            holder.dueDateView.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onTaskClicked(holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView titleView, descriptionView, dueDateView;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            titleView = itemView.findViewById(R.id.taskTitle);
            descriptionView = itemView.findViewById(R.id.taskDescription);
            dueDateView = itemView.findViewById(R.id.taskDueDate);
        }
    }


    // Listener interfaces INSIDE the class!
    public interface OnTaskLongClickListener {
        void onTaskLongClicked(int position);
    }

    public interface OnTaskClickListener {
        void onTaskClicked(int position);
    }
}
