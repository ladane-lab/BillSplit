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

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextView tvTotalExpenses;
    private TextView tvYouOwe;
    private TextView tvOwedToYou;
    private TextView tvRecentExpenses;
    private TextView tvGroupInfo;
    private RecyclerView rvRecentExpenses;

    private SharedPreferences preferences;
    private String activeGroupId = "group_pune_flat";
    private List<String> activeGroupMembers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SessionManager sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        preferences = getSharedPreferences("SplitMateData", MODE_PRIVATE);

        tvTotalExpenses = findViewById(R.id.tvTotalExpenses);
        tvYouOwe = findViewById(R.id.tvYouOwe);
        tvOwedToYou = findViewById(R.id.tvOwedToYou);
        tvRecentExpenses = findViewById(R.id.tvRecentExpenses);
        tvGroupInfo = findViewById(R.id.tvGroupInfo);
        rvRecentExpenses = findViewById(R.id.rvRecentExpenses);

        rvRecentExpenses.setLayoutManager(new LinearLayoutManager(this));

        Button btnAddExpense = findViewById(R.id.btnAddExpense);
        Button btnSettleUp = findViewById(R.id.btnSettleUp);
        Button btnViewHistory = findViewById(R.id.btnViewHistory);
        Button btnManageGroups = findViewById(R.id.btnManageGroups);

        btnAddExpense.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, AddExpenseActivity.class);
            startActivity(intent);
        });

        btnSettleUp.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, SettleUpActivity.class);
            startActivity(intent);
        });

        btnViewHistory.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, ExpenseHistoryActivity.class);
            startActivity(intent);
        });

        View.OnClickListener openGroupsListener = view -> {
            Intent intent = new Intent(MainActivity.this, GroupsActivity.class);
            startActivity(intent);
        };

        View btnReports = findViewById(R.id.btnReports);
        View btnProfile = findViewById(R.id.btnProfile);

        if (btnReports != null) {
            btnReports.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, ReportsActivity.class)));
        }
        if (btnProfile != null) {
            btnProfile.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, ProfileActivity.class)));
        }

        btnManageGroups.setOnClickListener(openGroupsListener);
        tvGroupInfo.setOnClickListener(openGroupsListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadActiveGroup();
        loadExpenses();
    }

    private void loadActiveGroup() {
        activeGroupId = preferences.getString("active_group_id", "group_pune_flat");
        String savedGroups = preferences.getString("groups", "[]");

        activeGroupMembers.clear();
        String groupName = "Pune Flat";
        String groupType = "Flat 🏠";

        try {
            JSONArray array = new JSONArray(savedGroups);

            // Ensure default group if empty
            if (array.length() == 0) {
                JSONObject defaultGroup = new JSONObject();
                defaultGroup.put("id", "group_pune_flat");
                defaultGroup.put("name", "Pune Flat");
                defaultGroup.put("type", "Flat 🏠");
                JSONArray defaultMembers = new JSONArray();
                defaultMembers.put("You");
                defaultMembers.put("Rahul");
                defaultMembers.put("Amit");
                defaultGroup.put("members", defaultMembers);
                array.put(defaultGroup);
                preferences.edit().putString("groups", array.toString()).apply();
            }

            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                if (activeGroupId.equals(obj.optString("id"))) {
                    groupName = obj.getString("name");
                    groupType = obj.optString("type", "Group");
                    JSONArray membersArray = obj.optJSONArray("members");
                    if (membersArray != null) {
                        for (int j = 0; j < membersArray.length(); j++) {
                            activeGroupMembers.add(membersArray.getString(j));
                        }
                    }
                    break;
                }
            }

            if (activeGroupMembers.isEmpty()) {
                activeGroupMembers.add("You");
                activeGroupMembers.add("Rahul");
                activeGroupMembers.add("Amit");
            }

            tvGroupInfo.setText(groupName + "  " + groupType + "\n" + activeGroupMembers.size() + " members · Active Group");

        } catch (Exception e) {
            tvGroupInfo.setText("Pune Flat  🏠\n3 members · Active Group");
        }
    }

    private void loadExpenses() {
        String savedData = preferences.getString("expenses", "[]");

        try {
            JSONArray expenses = new JSONArray(savedData);

            long totalPaise = 0;
            long youBalance = 0;

            int memberCount = activeGroupMembers.isEmpty() ? 3 : activeGroupMembers.size();
            List<Expense> recentList = new ArrayList<>();

            for (int i = expenses.length() - 1; i >= 0; i--) {
                JSONObject expense = expenses.getJSONObject(i);

                String expGroupId = expense.optString("groupId", "group_pune_flat");

                // Filter expenses for current active group
                if (activeGroupId.equals(expGroupId)) {
                    String id = expense.optString("id", String.valueOf(i));
                    String description = expense.getString("description");
                    String category = expense.getString("category");
                    String paidBy = expense.getString("paidBy");
                    long amountPaise = expense.getLong("amountPaise");
                    long date = expense.optLong("date", 0);

                    totalPaise += amountPaise;

                    long yourSharePaise = amountPaise / memberCount;
                    JSONArray splitsArray = expense.optJSONArray("splits");
                    List<MemberSplit> splitList = new ArrayList<>();

                    if (splitsArray != null && splitsArray.length() > 0) {
                        for (int k = 0; k < splitsArray.length(); k++) {
                            JSONObject splitObj = splitsArray.getJSONObject(k);
                            String mName = splitObj.optString("memberName", "");
                            long mShare = splitObj.optLong("sharePaise", 0);
                            splitList.add(new MemberSplit(mName, mShare));
                            if (mName.equalsIgnoreCase("You")) {
                                yourSharePaise = mShare;
                            }
                        }
                    }

                    if (paidBy.equalsIgnoreCase("You")) {
                        youBalance += amountPaise;
                    }
                    youBalance -= yourSharePaise;

                    String splitType = expense.optString("splitType", "EQUAL");
                    recentList.add(new Expense(id, description, amountPaise, category, paidBy, date, i, expGroupId, splitType, splitList));
                }
            }

            tvTotalExpenses.setText(formatMoney(totalPaise));

            // Load settlements for active group
            List<Settlement> settlements = loadActiveGroupSettlements();
            java.util.Map<String, Long> netBalances = BalanceCalculator.calculateNetBalances(activeGroupMembers, recentList, settlements);
            Long youNetBalanceObj = netBalances.get("You");
            long youNetBalance = (youNetBalanceObj != null) ? youNetBalanceObj : 0L;

            if (youNetBalance < 0) {
                tvYouOwe.setText(formatMoney(-youNetBalance));
                tvOwedToYou.setText(formatMoney(0));
            } else {
                tvYouOwe.setText(formatMoney(0));
                tvOwedToYou.setText(formatMoney(youNetBalance));
            }

            if (recentList.isEmpty()) {
                tvRecentExpenses.setVisibility(View.VISIBLE);
                rvRecentExpenses.setVisibility(View.GONE);
                tvRecentExpenses.setText("No expenses yet in this group.\nTap + Add Expense to get started!");
            } else {
                tvRecentExpenses.setVisibility(View.GONE);
                rvRecentExpenses.setVisibility(View.VISIBLE);

                ExpenseAdapter adapter = new ExpenseAdapter(recentList);
                adapter.setOnItemClickListener((expense, position) -> {
                    Intent intent = new Intent(MainActivity.this, ExpenseDetailActivity.class);
                    intent.putExtra("is_from_click", true);
                    intent.putExtra(ExpenseDetailActivity.EXTRA_EXPENSE_INDEX, expense.getOriginalIndex());
                    startActivity(intent);
                });
                rvRecentExpenses.setAdapter(adapter);
            }

        } catch (Exception e) {
            tvRecentExpenses.setVisibility(View.VISIBLE);
            rvRecentExpenses.setVisibility(View.GONE);
            tvRecentExpenses.setText("Unable to load expenses.");
        }
    }

    private List<Settlement> loadActiveGroupSettlements() {
        List<Settlement> list = new ArrayList<>();
        String savedData = preferences.getString("settlements", "[]");
        try {
            JSONArray array = new JSONArray(savedData);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                if (activeGroupId.equals(obj.optString("groupId"))) {
                    list.add(new Settlement(
                            obj.optString("id"),
                            obj.optString("groupId"),
                            obj.getString("payer"),
                            obj.getString("receiver"),
                            obj.getLong("amountPaise"),
                            obj.optLong("date")
                    ));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    private String formatMoney(long amountPaise) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
        return formatter.format(amountPaise / 100.0);
    }
}
