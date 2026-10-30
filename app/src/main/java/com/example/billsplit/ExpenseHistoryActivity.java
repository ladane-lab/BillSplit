package com.example.billsplit;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ExpenseHistoryActivity extends AppCompatActivity {

    private RecyclerView rvExpenseHistory;
    private TextView tvEmptyHistory;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_history);

        preferences = getSharedPreferences("SplitMateData", MODE_PRIVATE);

        rvExpenseHistory = findViewById(R.id.rvExpenseHistory);
        tvEmptyHistory = findViewById(R.id.tvEmptyHistory);
        View btnBack = findViewById(R.id.btnBack);

        rvExpenseHistory.setLayoutManager(new LinearLayoutManager(this));
        rvExpenseHistory.setNestedScrollingEnabled(false);

        btnBack.setOnClickListener(v -> finish());

        loadExpenseHistory();
    }

    private void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadExpenseHistory();
    }

    private void loadExpenseHistory() {
        String savedData = preferences.getString("expenses", "[]");
        List<Expense> expenseList = new ArrayList<>();

        try {
            JSONArray expenses = new JSONArray(savedData);

            // Load expenses in reverse order (newest first)
            for (int i = expenses.length() - 1; i >= 0; i--) {
                JSONObject obj = expenses.getJSONObject(i);

                String id = obj.optString("id", String.valueOf(i));
                String description = obj.getString("description");
                long amountPaise = obj.getLong("amountPaise");
                String category = obj.getString("category");
                String paidBy = obj.getString("paidBy");
                long date = obj.optLong("date", 0);

                expenseList.add(new Expense(id, description, amountPaise, category, paidBy, date, i));
            }

            if (expenseList.isEmpty()) {
                tvEmptyHistory.setVisibility(View.VISIBLE);
                rvExpenseHistory.setVisibility(View.GONE);
            } else {
                tvEmptyHistory.setVisibility(View.GONE);
                rvExpenseHistory.setVisibility(View.VISIBLE);

                ExpenseAdapter adapter = new ExpenseAdapter(expenseList);
                adapter.setOnItemClickListener((expense, position) -> {
                    Intent intent = new Intent(
                            ExpenseHistoryActivity.this,
                            ExpenseDetailActivity.class
                    );
                    intent.putExtra("is_from_click", true);
                    intent.putExtra(ExpenseDetailActivity.EXTRA_EXPENSE_INDEX, expense.getOriginalIndex());
                    startActivity(intent);
                });
                rvExpenseHistory.setAdapter(adapter);
            }

        } catch (Exception e) {
            tvEmptyHistory.setText("Error loading expense history.");
            tvEmptyHistory.setVisibility(View.VISIBLE);
            rvExpenseHistory.setVisibility(View.GONE);
        }
    }
}
