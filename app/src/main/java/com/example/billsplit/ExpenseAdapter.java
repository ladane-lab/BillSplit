package com.example.billsplit;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> {

    private final List<Expense> expenseList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Expense expense, int position);
    }

    public ExpenseAdapter(List<Expense> expenseList) {
        this.expenseList = expenseList;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_expense, parent, false);
        return new ExpenseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        Expense expense = expenseList.get(position);

        holder.tvDescription.setText(expense.getDescription());

        String details = expense.getCategory() + " · Paid by " + expense.getPaidBy();
        if (!expense.getFormattedDate().isEmpty()) {
            details += " · " + expense.getFormattedDate();
        }
        holder.tvDetails.setText(details);

        holder.tvAmount.setText(expense.getFormattedAmount());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                int adapterPos = holder.getBindingAdapterPosition();
                if (adapterPos != RecyclerView.NO_POSITION) {
                    listener.onItemClick(expense, adapterPos);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return expenseList.size();
    }

    public static class ExpenseViewHolder extends RecyclerView.ViewHolder {

        TextView tvDescription;
        TextView tvDetails;
        TextView tvAmount;

        public ExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDescription = itemView.findViewById(R.id.tvExpenseDescription);
            tvDetails = itemView.findViewById(R.id.tvExpenseDetails);
            tvAmount = itemView.findViewById(R.id.tvExpenseAmount);
        }
    }
}
