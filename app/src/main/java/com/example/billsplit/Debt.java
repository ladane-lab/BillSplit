package com.example.billsplit;

import java.text.NumberFormat;
import java.util.Locale;

public class Debt {
    private String fromMember;
    private String toMember;
    private long amountPaise;

    public Debt(String fromMember, String toMember, long amountPaise) {
        this.fromMember = fromMember;
        this.toMember = toMember;
        this.amountPaise = amountPaise;
    }

    public String getFromMember() {
        return fromMember;
    }

    public String getToMember() {
        return toMember;
    }

    public long getAmountPaise() {
        return amountPaise;
    }

    public String getFormattedAmount() {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
        return formatter.format(amountPaise / 100.0);
    }

    public String getDisplayText() {
        if (fromMember.equalsIgnoreCase("You")) {
            return "You owe " + toMember + " " + getFormattedAmount();
        } else if (toMember.equalsIgnoreCase("You")) {
            return fromMember + " owes You " + getFormattedAmount();
        } else {
            return fromMember + " owes " + toMember + " " + getFormattedAmount();
        }
    }
}
