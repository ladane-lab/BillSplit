package com.example.billsplit;

import java.text.NumberFormat;
import java.util.Locale;

public class MemberSplit {
    private String memberName;
    private long sharePaise;

    public MemberSplit(String memberName, long sharePaise) {
        this.memberName = memberName;
        this.sharePaise = sharePaise;
    }

    public String getMemberName() {
        return memberName;
    }

    public long getSharePaise() {
        return sharePaise;
    }

    public String getFormattedShare() {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
        return formatter.format(sharePaise / 100.0);
    }
}
