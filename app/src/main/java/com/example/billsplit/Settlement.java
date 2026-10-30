package com.example.billsplit;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Settlement {
    private String id;
    private String groupId;
    private String payer;
    private String receiver;
    private long amountPaise;
    private long date;

    public Settlement(String id, String groupId, String payer, String receiver, long amountPaise, long date) {
        this.id = id;
        this.groupId = groupId;
        this.payer = payer;
        this.receiver = receiver;
        this.amountPaise = amountPaise;
        this.date = date;
    }

    public String getId() {
        return id;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getPayer() {
        return payer;
    }

    public String getReceiver() {
        return receiver;
    }

    public long getAmountPaise() {
        return amountPaise;
    }

    public long getDate() {
        return date;
    }

    public String getFormattedAmount() {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
        return formatter.format(amountPaise / 100.0);
    }

    public String getFormattedDate() {
        if (date <= 0) {
            return "";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
        return sdf.format(new Date(date));
    }

    public String getDisplayText() {
        return payer + " paid " + receiver + " " + getFormattedAmount();
    }
}
