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
                .inflate(android.R.layout.simple_list_item_1, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = tasks.get(position);
        holder.textView.setText(task.getTitle());

        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onTaskLongClicked(holder.getAdapterPosition());
            }
            return true;
        });

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
        TextView textView;
        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(android.R.id.text1);
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
