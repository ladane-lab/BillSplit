package com.example.billsplit;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ReportsActivity extends AppCompatActivity {

    private TextView tvReportTotalSpending;
    private TextView tvReportHighestCategory;
    private TextView tvCategoryBreakdown;
    private TextView tvMemberSpending;

    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports);

        preferences = getSharedPreferences("SplitMateData", MODE_PRIVATE);

        tvReportTotalSpending = findViewById(R.id.tvReportTotalSpending);
        tvReportHighestCategory = findViewById(R.id.tvReportHighestCategory);
        tvCategoryBreakdown = findViewById(R.id.tvCategoryBreakdown);
        tvMemberSpending = findViewById(R.id.tvMemberSpending);

        View btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> navigateToMain());

        getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                navigateToMain();
            }
        });

        generateReports();
    }

    private void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    private void generateReports() {
        String activeGroupId = preferences.getString("active_group_id", "group_pune_flat");
        String savedData = preferences.getString("expenses", "[]");

        long totalSpendingPaise = 0;
        Map<String, Long> categoryTotals = new HashMap<>();
        Map<String, Long> memberTotals = new HashMap<>();

        try {
            JSONArray expenses = new JSONArray(savedData);
            for (int i = 0; i < expenses.length(); i++) {
                JSONObject obj = expenses.getJSONObject(i);
                if (activeGroupId.equals(obj.optString("groupId", "group_pune_flat"))) {
                    long amountPaise = obj.getLong("amountPaise");
                    String category = obj.getString("category");
                    String paidBy = obj.getString("paidBy");

                    totalSpendingPaise += amountPaise;

                    Long catSum = categoryTotals.get(category);
                    categoryTotals.put(category, (catSum != null ? catSum : 0L) + amountPaise);

                    Long memSum = memberTotals.get(paidBy);
                    memberTotals.put(paidBy, (memSum != null ? memSum : 0L) + amountPaise);
                }
            }

            tvReportTotalSpending.setText(formatMoney(totalSpendingPaise));

            if (totalSpendingPaise == 0) {
                tvReportHighestCategory.setText("No expense records in this group.");
                tvCategoryBreakdown.setText("No category expense data recorded.");
                tvMemberSpending.setText("No member spending data recorded.");
                return;
            }

            // Find highest spending category
            String highestCat = "None";
            long maxCatPaise = 0;
            StringBuilder catSb = new StringBuilder();

            for (Map.Entry<String, Long> entry : categoryTotals.entrySet()) {
                long cPaise = entry.getValue();
                double pct = (double) cPaise / totalSpendingPaise * 100.0;
                catSb.append("• ").append(entry.getKey())
                        .append(": ").append(formatMoney(cPaise))
                        .append(String.format(Locale.getDefault(), " (%.1f%%)", pct))
                        .append("\n");

                if (cPaise > maxCatPaise) {
                    maxCatPaise = cPaise;
                    highestCat = entry.getKey();
                }
            }

            tvReportHighestCategory.setText("Top Spending Category: " + highestCat + " (" + formatMoney(maxCatPaise) + ")");
            tvCategoryBreakdown.setText(catSb.toString().trim());

            // Member breakdown
            StringBuilder memSb = new StringBuilder();
            for (Map.Entry<String, Long> entry : memberTotals.entrySet()) {
                long mPaise = entry.getValue();
                double pct = (double) mPaise / totalSpendingPaise * 100.0;
                memSb.append("• ").append(entry.getKey())
                        .append(": ").append(formatMoney(mPaise))
                        .append(String.format(Locale.getDefault(), " (%.1f%%)", pct))
                        .append("\n");
            }

            tvMemberSpending.setText(memSb.toString().trim());

        } catch (Exception e) {
            tvReportTotalSpending.setText(formatMoney(0));
            tvReportHighestCategory.setText("Error generating reports.");
        }
    }

    private String formatMoney(long amountPaise) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
        return formatter.format(amountPaise / 100.0);
    }
}
