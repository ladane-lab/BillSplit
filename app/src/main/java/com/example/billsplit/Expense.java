package com.example.billsplit;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Expense {
    private String id;
    private String description;
    private long amountPaise;
    private String category;
    private String paidBy;
    private long date;
    private int originalIndex;
    private String groupId;
    private String splitType;
    private List<MemberSplit> splits;

    public Expense(String id, String description, long amountPaise, String category, String paidBy, long date, int originalIndex) {
        this(id, description, amountPaise, category, paidBy, date, originalIndex, "group_pune_flat", "EQUAL", new ArrayList<>());
    }

    public Expense(String id, String description, long amountPaise, String category, String paidBy, long date, int originalIndex, String groupId) {
        this(id, description, amountPaise, category, paidBy, date, originalIndex, groupId, "EQUAL", new ArrayList<>());
    }

    public Expense(String id, String description, long amountPaise, String category, String paidBy, long date, int originalIndex, String groupId, String splitType, List<MemberSplit> splits) {
        this.id = id;
        this.description = description;
        this.amountPaise = amountPaise;
        this.category = category;
        this.paidBy = paidBy;
        this.date = date;
        this.originalIndex = originalIndex;
        this.groupId = (groupId != null && !groupId.isEmpty()) ? groupId : "group_pune_flat";
        this.splitType = (splitType != null && !splitType.isEmpty()) ? splitType : "EQUAL";
        this.splits = (splits != null) ? splits : new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public long getAmountPaise() {
        return amountPaise;
    }

    public String getCategory() {
        return category;
    }

    public String getPaidBy() {
        return paidBy;
    }

    public long getDate() {
        return date;
    }

    public int getOriginalIndex() {
        return originalIndex;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getSplitType() {
        return splitType;
    }

    public List<MemberSplit> getSplits() {
        return splits;
    }

    public String getFormattedAmount() {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
        return formatter.format(amountPaise / 100.0);
    }

    public String getFormattedDate() {
        if (date <= 0) {
            return "";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        return sdf.format(new Date(date));
    }
}
