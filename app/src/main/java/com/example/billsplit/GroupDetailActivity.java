package com.example.billsplit;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class GroupDetailActivity extends AppCompatActivity {

    private TextView tvGroupDetailTitle;
    private TextView tvGroupDetailType;
    private TextView tvGroupDetailMemberCount;
    private Button btnSetActiveGroup;
    private RecyclerView rvMembers;
    private RecyclerView rvGroupExpenses;
    private TextView tvEmptyGroupExpenses;
    private TextView tvGroupBalances;

    private SharedPreferences preferences;
    private String groupId;
    private JSONObject currentGroupObj;
    private int currentGroupIndex = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_detail);

        preferences = getSharedPreferences("SplitMateData", MODE_PRIVATE);
        groupId = getIntent().getStringExtra("group_id");

        tvGroupDetailTitle = findViewById(R.id.tvGroupDetailTitle);
        tvGroupDetailType = findViewById(R.id.tvGroupDetailType);
        tvGroupDetailMemberCount = findViewById(R.id.tvGroupDetailMemberCount);
        btnSetActiveGroup = findViewById(R.id.btnSetActiveGroup);
        rvMembers = findViewById(R.id.rvMembers);
        rvGroupExpenses = findViewById(R.id.rvGroupExpenses);
        tvEmptyGroupExpenses = findViewById(R.id.tvEmptyGroupExpenses);
        tvGroupBalances = findViewById(R.id.tvGroupBalances);

        View btnBack = findViewById(R.id.btnBack);
        Button btnAddMember = findViewById(R.id.btnAddMember);

        rvMembers.setLayoutManager(new LinearLayoutManager(this));
        rvGroupExpenses.setLayoutManager(new LinearLayoutManager(this));

        rvMembers.setNestedScrollingEnabled(false);
        rvGroupExpenses.setNestedScrollingEnabled(false);

        btnBack.setOnClickListener(v -> finish());
        btnAddMember.setOnClickListener(v -> showAddMemberDialog());
        btnSetActiveGroup.setOnClickListener(v -> setActiveGroup());

        loadGroupData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadGroupData();
    }

    private void loadGroupData() {
        if (groupId == null || groupId.isEmpty()) {
            Toast.makeText(this, "Group not found.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        try {
            String savedGroups = preferences.getString("groups", "[]");
            JSONArray array = new JSONArray(savedGroups);

            currentGroupObj = null;
            currentGroupIndex = -1;

            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                if (groupId.equals(obj.optString("id"))) {
                    currentGroupObj = obj;
                    currentGroupIndex = i;
                    break;
                }
            }

            if (currentGroupObj == null) {
                finish();
                return;
            }

            String groupName = currentGroupObj.getString("name");
            String groupType = currentGroupObj.optString("type", "Group");
            JSONArray membersArray = currentGroupObj.optJSONArray("members");

            List<String> memberList = new ArrayList<>();
            if (membersArray != null) {
                for (int i = 0; i < membersArray.length(); i++) {
                    memberList.add(membersArray.getString(i));
                }
            }

            tvGroupDetailTitle.setText(groupName);
            tvGroupDetailType.setText(groupType);
            tvGroupDetailMemberCount.setText(memberList.size() + (memberList.size() == 1 ? " Member" : " Members"));

            String activeGroupId = preferences.getString("active_group_id", "group_pune_flat");
            if (groupId.equals(activeGroupId)) {
                btnSetActiveGroup.setText("Active Group ✓");
                btnSetActiveGroup.setEnabled(false);
            } else {
                btnSetActiveGroup.setText("Set as Active Group");
                btnSetActiveGroup.setEnabled(true);
            }

            MemberAdapter memberAdapter = new MemberAdapter(memberList);
            memberAdapter.setOnRemoveMemberClickListener((memberName, position) -> removeMember(memberName, position));
            rvMembers.setAdapter(memberAdapter);

            List<Expense> groupExpenses = loadGroupExpenses();
            updateGroupBalancesUI(memberList, groupExpenses);

        } catch (Exception e) {
            Toast.makeText(this, "Error loading group data.", Toast.LENGTH_SHORT).show();
        }
    }

    private List<Expense> loadGroupExpenses() {
        String savedExpenses = preferences.getString("expenses", "[]");
        List<Expense> groupExpenseList = new ArrayList<>();

        try {
            JSONArray expenses = new JSONArray(savedExpenses);
            for (int i = expenses.length() - 1; i >= 0; i--) {
                JSONObject obj = expenses.getJSONObject(i);
                String expGroupId = obj.optString("groupId", "group_pune_flat");

                if (groupId.equals(expGroupId)) {
                    String id = obj.optString("id", String.valueOf(i));
                    String description = obj.getString("description");
                    long amountPaise = obj.getLong("amountPaise");
                    String category = obj.getString("category");
                    String paidBy = obj.getString("paidBy");
                    long date = obj.optLong("date", 0);
                    String splitType = obj.optString("splitType", "EQUAL");

                    JSONArray splitsArray = obj.optJSONArray("splits");
                    List<MemberSplit> splitList = new ArrayList<>();
                    if (splitsArray != null) {
                        for (int k = 0; k < splitsArray.length(); k++) {
                            JSONObject splitObj = splitsArray.getJSONObject(k);
                            splitList.add(new MemberSplit(splitObj.optString("memberName"), splitObj.optLong("sharePaise")));
                        }
                    }

                    groupExpenseList.add(new Expense(id, description, amountPaise, category, paidBy, date, i, expGroupId, splitType, splitList));
                }
            }

            if (groupExpenseList.isEmpty()) {
                tvEmptyGroupExpenses.setVisibility(View.VISIBLE);
                rvGroupExpenses.setVisibility(View.GONE);
            } else {
                tvEmptyGroupExpenses.setVisibility(View.GONE);
                rvGroupExpenses.setVisibility(View.VISIBLE);

                ExpenseAdapter adapter = new ExpenseAdapter(groupExpenseList);
                adapter.setOnItemClickListener((expense, position) -> {
                    Intent intent = new Intent(GroupDetailActivity.this, ExpenseDetailActivity.class);
                    intent.putExtra("is_from_click", true);
                    intent.putExtra(ExpenseDetailActivity.EXTRA_EXPENSE_INDEX, expense.getOriginalIndex());
                    startActivity(intent);
                });
                rvGroupExpenses.setAdapter(adapter);
            }

        } catch (Exception e) {
            tvEmptyGroupExpenses.setVisibility(View.VISIBLE);
            rvGroupExpenses.setVisibility(View.GONE);
        }

        return groupExpenseList;
    }

    private void updateGroupBalancesUI(List<String> members, List<Expense> groupExpenses) {
        java.util.Map<String, Long> balances = BalanceCalculator.calculateNetBalances(members, groupExpenses);
        List<Debt> debts = BalanceCalculator.calculateSimplifiedDebts(balances);

        if (debts.isEmpty()) {
            tvGroupBalances.setText("All members are settled up ✓");
            tvGroupBalances.setTextColor(0xFF246B50);
        } else {
            StringBuilder sb = new StringBuilder("Simplified Debts:\n");
            for (Debt debt : debts) {
                sb.append("• ").append(debt.getDisplayText()).append("\n");
            }
            tvGroupBalances.setText(sb.toString().trim());
            tvGroupBalances.setTextColor(0xFF172033);
        }
    }

    private void showAddMemberDialog() {
        final EditText etMemberName = new EditText(this);
        etMemberName.setHint("e.g. Rahul");
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        etMemberName.setPadding(padding, padding, padding, padding);

        new AlertDialog.Builder(this)
                .setTitle("Add Member")
                .setMessage("Enter the name of the new group member:")
                .setView(etMemberName)
                .setPositiveButton("Add", (dialog, which) -> {
                    String name = etMemberName.getText().toString().trim();
                    if (!name.isEmpty()) {
                        addMemberToGroup(name);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void addMemberToGroup(String name) {
        try {
            JSONArray membersArray = currentGroupObj.optJSONArray("members");
            if (membersArray == null) {
                membersArray = new JSONArray();
            }

            // Avoid duplicate members
            for (int i = 0; i < membersArray.length(); i++) {
                if (membersArray.getString(i).equalsIgnoreCase(name)) {
                    Toast.makeText(this, name + " is already in this group.", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            membersArray.put(name);
            currentGroupObj.put("members", membersArray);

            saveCurrentGroup();
            Toast.makeText(this, name + " added to group!", Toast.LENGTH_SHORT).show();
            loadGroupData();

        } catch (Exception e) {
            Toast.makeText(this, "Failed to add member.", Toast.LENGTH_SHORT).show();
        }
    }

    private void removeMember(String memberName, int position) {
        try {
            JSONArray membersArray = currentGroupObj.optJSONArray("members");
            if (membersArray != null) {
                if (membersArray.length() <= 1) {
                    Toast.makeText(this, "A group must have at least 1 member.", Toast.LENGTH_SHORT).show();
                    return;
                }

                JSONArray updatedMembers = new JSONArray();
                for (int i = 0; i < membersArray.length(); i++) {
                    if (!membersArray.getString(i).equalsIgnoreCase(memberName)) {
                        updatedMembers.put(membersArray.getString(i));
                    }
                }

                currentGroupObj.put("members", updatedMembers);
                saveCurrentGroup();
                Toast.makeText(this, memberName + " removed.", Toast.LENGTH_SHORT).show();
                loadGroupData();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error removing member.", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveCurrentGroup() {
        try {
            String savedGroups = preferences.getString("groups", "[]");
            JSONArray array = new JSONArray(savedGroups);

            if (currentGroupIndex >= 0 && currentGroupIndex < array.length()) {
                array.put(currentGroupIndex, currentGroupObj);
                preferences.edit().putString("groups", array.toString()).apply();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setActiveGroup() {
        preferences.edit().putString("active_group_id", groupId).apply();
        Toast.makeText(this, "Set as active group!", Toast.LENGTH_SHORT).show();
        loadGroupData();
    }
}
