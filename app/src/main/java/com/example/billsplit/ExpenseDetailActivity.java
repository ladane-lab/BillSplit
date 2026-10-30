package com.example.billsplit;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ExpenseDetailActivity extends AppCompatActivity {

    public static final String EXTRA_EXPENSE_INDEX = "expense_index";

    private TextView tvDetailCategory;
    private TextView tvDetailDescription;
    private TextView tvDetailAmount;
    private TextView tvDetailPaidBy;
    private TextView tvDetailDate;
    private TextView tvDetailSplit;

    private SharedPreferences preferences;
    private int expenseIndex = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_detail);

        preferences = getSharedPreferences("SplitMateData", MODE_PRIVATE);

        if (isTaskRoot() || !getIntent().hasExtra(EXTRA_EXPENSE_INDEX)) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        expenseIndex = getIntent().getIntExtra(EXTRA_EXPENSE_INDEX, -1);

        tvDetailCategory = findViewById(R.id.tvDetailCategory);
        tvDetailDescription = findViewById(R.id.tvDetailDescription);
        tvDetailAmount = findViewById(R.id.tvDetailAmount);
        tvDetailPaidBy = findViewById(R.id.tvDetailPaidBy);
        tvDetailDate = findViewById(R.id.tvDetailDate);
        tvDetailSplit = findViewById(R.id.tvDetailSplit);

        View btnBack = findViewById(R.id.btnBack);
        Button btnEditExpense = findViewById(R.id.btnEditExpense);
        Button btnDeleteExpense = findViewById(R.id.btnDeleteExpense);

        btnBack.setOnClickListener(v -> finish());

        btnEditExpense.setOnClickListener(v -> {
            Intent intent = new Intent(ExpenseDetailActivity.this, EditExpenseActivity.class);
            intent.putExtra(EditExpenseActivity.EXTRA_EXPENSE_INDEX, expenseIndex);
            startActivity(intent);
        });

        btnDeleteExpense.setOnClickListener(v -> confirmDeleteExpense());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadExpenseDetails();
    }

    private void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void loadExpenseDetails() {
        if (expenseIndex < 0) {
            Toast.makeText(this, "Expense not found.", Toast.LENGTH_SHORT).show();
            navigateToMain();
            return;
        }

        try {
            String savedData = preferences.getString("expenses", "[]");
            JSONArray expenses = new JSONArray(savedData);

            if (expenseIndex >= expenses.length()) {
                finish();
                return;
            }

            JSONObject obj = expenses.getJSONObject(expenseIndex);
            String description = obj.getString("description");
            long amountPaise = obj.getLong("amountPaise");
            String category = obj.getString("category");
            String paidBy = obj.getString("paidBy");
            long date = obj.optLong("date", 0);

            tvDetailCategory.setText(category.toUpperCase(Locale.getDefault()));
            tvDetailDescription.setText(description);
            tvDetailAmount.setText(formatMoney(amountPaise));
            tvDetailPaidBy.setText(paidBy);

            if (date > 0) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy, hh:mm a", Locale.getDefault());
                tvDetailDate.setText(sdf.format(new Date(date)));
            } else {
                tvDetailDate.setText("Not recorded");
            }

            StringBuilder splitText = new StringBuilder("Split Type: Equal Split\n\nIndividual Shares:\n");
            JSONArray splitsArray = obj.optJSONArray("splits");

            long sumSharesPaise = 0;
            if (splitsArray != null && splitsArray.length() > 0) {
                for (int i = 0; i < splitsArray.length(); i++) {
                    JSONObject splitObj = splitsArray.getJSONObject(i);
                    String mName = splitObj.optString("memberName", "Member " + (i + 1));
                    long mShare = splitObj.optLong("sharePaise", 0);
                    sumSharesPaise += mShare;
                    splitText.append("• ").append(mName).append(": ").append(formatMoney(mShare)).append("\n");
                }
            } else {
                // Fallback for older expense records without saved splits array
                List<String> defaultMembers = new ArrayList<>();
                defaultMembers.add("You");
                defaultMembers.add("Rahul");
                defaultMembers.add("Amit");
                List<MemberSplit> fallbackSplits = SplitCalculator.calculateEqualSplits(amountPaise, defaultMembers);
                for (MemberSplit ms : fallbackSplits) {
                    sumSharesPaise += ms.getSharePaise();
                    splitText.append("• ").append(ms.getMemberName()).append(": ").append(ms.getFormattedShare()).append("\n");
                }
            }

            splitText.append("\nTotal of shares: ").append(formatMoney(sumSharesPaise));
            tvDetailSplit.setText(splitText.toString().trim());

        } catch (Exception e) {
            Toast.makeText(this, "Unable to load expense details.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void confirmDeleteExpense() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Expense")
                .setMessage("Are you sure you want to delete this expense? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> deleteExpense())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteExpense() {
        try {
            String savedData = preferences.getString("expenses", "[]");
            JSONArray expenses = new JSONArray(savedData);

            if (expenseIndex >= 0 && expenseIndex < expenses.length()) {
                JSONArray updatedArray = new JSONArray();
                for (int i = 0; i < expenses.length(); i++) {
                    if (i != expenseIndex) {
                        updatedArray.put(expenses.getJSONObject(i));
                    }
                }

                preferences.edit().putString("expenses", updatedArray.toString()).apply();
                Toast.makeText(this, "Expense deleted successfully!", Toast.LENGTH_SHORT).show();
                finish();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error deleting expense.", Toast.LENGTH_SHORT).show();
        }
    }

    private String formatMoney(long amountPaise) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
        return formatter.format(amountPaise / 100.0);
    }
}
