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
import java.util.Collections;
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
        rvRecentExpenses.setNestedScrollingEnabled(false);

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
                SessionManager sessionManager = new SessionManager(this);
                JSONArray defaultMembers = new JSONArray();
                defaultMembers.put(sessionManager.getUserName());
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
        String savedExpenses = preferences.getString("expenses", "[]");
        String savedSettlements = preferences.getString("settlements", "[]");

        try {
            JSONArray expenses = new JSONArray(savedExpenses);
            JSONArray settlementsArray = new JSONArray(savedSettlements);

            long totalPaise = 0;
            List<Expense> expenseList = new ArrayList<>();
            List<Expense> activityList = new ArrayList<>();
            List<Settlement> settlements = new ArrayList<>();

            // 1. Load Group Expenses
            for (int i = 0; i < expenses.length(); i++) {
                JSONObject obj = expenses.getJSONObject(i);
                String expGroupId = obj.optString("groupId", "group_pune_flat");

                if (activeGroupId.equals(expGroupId)) {
                    String id = obj.optString("id", String.valueOf(i));
                    String description = obj.getString("description");
                    String category = obj.optString("category", "Other");
                    String paidBy = obj.optString("paidBy", "You");
                    long amountPaise = obj.optLong("amountPaise", 0);
                    long date = obj.optLong("date", 0);

                    totalPaise += amountPaise;

                    JSONArray splitsArray = obj.optJSONArray("splits");
                    List<MemberSplit> splitList = new ArrayList<>();
                    if (splitsArray != null) {
                        for (int k = 0; k < splitsArray.length(); k++) {
                            JSONObject splitObj = splitsArray.getJSONObject(k);
                            splitList.add(new MemberSplit(splitObj.optString("memberName"), splitObj.optLong("sharePaise")));
                        }
                    }

                    String splitType = obj.optString("splitType", "EQUAL");
                    Expense exp = new Expense(id, description, amountPaise, category, paidBy, date, i, expGroupId, splitType, splitList);
                    expenseList.add(exp);
                    activityList.add(exp);
                }
            }

            // 2. Load Group Settlements
            for (int i = 0; i < settlementsArray.length(); i++) {
                JSONObject obj = settlementsArray.getJSONObject(i);
                if (activeGroupId.equals(obj.optString("groupId", "group_pune_flat"))) {
                    String id = obj.optString("id");
                    String payer = obj.optString("payer");
                    String receiver = obj.optString("receiver");
                    long amountPaise = obj.optLong("amountPaise", 0);
                    long date = obj.optLong("date", System.currentTimeMillis());

                    Settlement s = new Settlement(id, activeGroupId, payer, receiver, amountPaise, date);
                    settlements.add(s);

                    // Add to recent activity feed
                    String desc = payer + " paid " + receiver;
                    Expense settlementExp = new Expense(id, desc, amountPaise, "Settlement", payer, date, -1, activeGroupId, "SETTLEMENT", new ArrayList<>());
                    activityList.add(settlementExp);
                }
            }

            // 3. Sort Recent Activity by Date Descending
            Collections.sort(activityList, (e1, e2) -> Long.compare(e2.getDate(), e1.getDate()));

            // 4. Update Total Expenses Display
            tvTotalExpenses.setText(formatMoney(totalPaise));

            // 5. Calculate Net Balances Including Settlements
            SessionManager sessionManager = new SessionManager(this);
            String currentUserName = sessionManager.getUserName();

            java.util.Map<String, Long> netBalances = BalanceCalculator.calculateNetBalances(activeGroupMembers, expenseList, settlements);

            Long userNetBalanceObj = null;
            for (java.util.Map.Entry<String, Long> entry : netBalances.entrySet()) {
                if (entry.getKey().equalsIgnoreCase(currentUserName) || entry.getKey().equalsIgnoreCase("You")) {
                    userNetBalanceObj = entry.getValue();
                    break;
                }
            }
            long youNetBalance = (userNetBalanceObj != null) ? userNetBalanceObj : 0L;

            if (youNetBalance < 0) {
                tvYouOwe.setText(formatMoney(-youNetBalance));
                tvOwedToYou.setText(formatMoney(0));
            } else {
                tvYouOwe.setText(formatMoney(0));
                tvOwedToYou.setText(formatMoney(youNetBalance));
            }

            // 6. Populate Recent Activity RecyclerView
            if (activityList.isEmpty()) {
                tvRecentExpenses.setVisibility(View.VISIBLE);
                rvRecentExpenses.setVisibility(View.GONE);
                tvRecentExpenses.setText("No expenses or settlements yet in this group.\nTap + Add Expense or Settle Up to start!");
            } else {
                tvRecentExpenses.setVisibility(View.GONE);
                rvRecentExpenses.setVisibility(View.VISIBLE);

                ExpenseAdapter adapter = new ExpenseAdapter(activityList);
                adapter.setOnItemClickListener((expense, position) -> {
                    if ("SETTLEMENT".equalsIgnoreCase(expense.getSplitType())) {
                        Intent intent = new Intent(MainActivity.this, SettlementHistoryActivity.class);
                        startActivity(intent);
                    } else {
                        Intent intent = new Intent(MainActivity.this, ExpenseDetailActivity.class);
                        intent.putExtra("is_from_click", true);
                        intent.putExtra(ExpenseDetailActivity.EXTRA_EXPENSE_INDEX, expense.getOriginalIndex());
                        startActivity(intent);
                    }
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
                if (activeGroupId.equals(obj.optString("groupId", "group_pune_flat"))) {
                    list.add(new Settlement(
                            obj.optString("id"),
                            obj.optString("groupId", "group_pune_flat"),
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
