package com.example.billsplit;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SettlementAdapter extends RecyclerView.Adapter<SettlementAdapter.SettlementViewHolder> {

    private final List<Settlement> settlementList;

    public SettlementAdapter(List<Settlement> settlementList) {
        this.settlementList = settlementList;
    }

    @NonNull
    @Override
    public SettlementViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_settlement, parent, false);
        return new SettlementViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SettlementViewHolder holder, int position) {
        Settlement settlement = settlementList.get(position);
        holder.tvTitle.setText(settlement.getDisplayText());
        holder.tvDate.setText(settlement.getFormattedDate());
        holder.tvAmount.setText(settlement.getFormattedAmount());
    }

    @Override
    public int getItemCount() {
        return settlementList.size();
    }

    public static class SettlementViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        TextView tvDate;
        TextView tvAmount;

        public SettlementViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvSettlementTitle);
            tvDate = itemView.findViewById(R.id.tvSettlementDate);
            tvAmount = itemView.findViewById(R.id.tvSettlementAmount);
        }
    }
}
