package com.example.billsplit;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.MemberViewHolder> {

    private final List<String> memberList;
    private OnRemoveMemberClickListener removeListener;

    public interface OnRemoveMemberClickListener {
        void onRemoveClick(String memberName, int position);
    }

    public MemberAdapter(List<String> memberList) {
        this.memberList = memberList;
    }

    public void setOnRemoveMemberClickListener(OnRemoveMemberClickListener removeListener) {
        this.removeListener = removeListener;
    }

    @NonNull
    @Override
    public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_member, parent, false);
        return new MemberViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {
        String name = memberList.get(position);
        holder.tvMemberName.setText(name);

        holder.btnRemoveMember.setOnClickListener(v -> {
            if (removeListener != null) {
                int pos = holder.getBindingAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    removeListener.onRemoveClick(name, pos);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return memberList.size();
    }

    public static class MemberViewHolder extends RecyclerView.ViewHolder {
        TextView tvMemberName;
        Button btnRemoveMember;

        public MemberViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMemberName = itemView.findViewById(R.id.tvMemberName);
            btnRemoveMember = itemView.findViewById(R.id.btnRemoveMember);
        }
    }
}
