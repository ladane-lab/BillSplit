package com.example.billsplit;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class EditExpenseActivity extends AppCompatActivity {

    public static final String EXTRA_EXPENSE_INDEX = "expense_index";

    private EditText etEditDescription;
    private EditText etEditAmount;
    private Spinner spEditGroup;
    private Spinner spEditCategory;
    private Spinner spEditPaidBy;
    private TextView tvEditSplitPreview;

    private SharedPreferences preferences;
    private int expenseIndex = -1;
    private final List<Group> groupList = new ArrayList<>();
    private Group selectedGroup;
    private String initialGroupId = "group_pune_flat";
    private String initialPaidBy = "You";

    private final String[] categories = {
            "Rent",
            "Groceries",
            "Electricity",
            "Wi-Fi",
            "Water",
            "Other"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_expense);

        preferences = getSharedPreferences("SplitMateData", MODE_PRIVATE);
        expenseIndex = getIntent().getIntExtra(EXTRA_EXPENSE_INDEX, -1);

        etEditDescription = findViewById(R.id.etEditDescription);
        etEditAmount = findViewById(R.id.etEditAmount);
        spEditGroup = findViewById(R.id.spEditGroup);
        spEditCategory = findViewById(R.id.spEditCategory);
        spEditPaidBy = findViewById(R.id.spEditPaidBy);
        tvEditSplitPreview = findViewById(R.id.tvEditSplitPreview);

        setSpinnerAdapter(spEditCategory, categories);

        etEditAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateSplitPreview();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        Button btnUpdateExpense = findViewById(R.id.btnUpdateExpense);
        Button btnEditCancel = findViewById(R.id.btnEditCancel);

        btnUpdateExpense.setOnClickListener(v -> updateExpense());
        btnEditCancel.setOnClickListener(v -> finish());

        loadExistingExpense();
    }

    private void setSpinnerAdapter(Spinner spinner, String[] items) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                items
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private void loadGroups() {
        String savedGroups = preferences.getString("groups", "[]");
        groupList.clear();

        try {
            JSONArray array = new JSONArray(savedGroups);
            List<String> groupNames = new ArrayList<>();
            int selectedIndex = 0;

            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                String id = obj.optString("id", "group_pune_flat");
                String name = obj.getString("name");
                String type = obj.optString("type", "Group");

                JSONArray membersArray = obj.optJSONArray("members");
                List<String> members = new ArrayList<>();
                if (membersArray != null) {
                    for (int j = 0; j < membersArray.length(); j++) {
                        members.add(membersArray.getString(j));
                    }
                }

                Group group = new Group(id, name, type, members, 0);
                groupList.add(group);
                groupNames.add(name + " (" + type + ")");

                if (id.equals(initialGroupId)) {
                    selectedIndex = i;
                }
            }

            ArrayAdapter<String> groupAdapter = new ArrayAdapter<>(
                    this,
                    android.R.layout.simple_spinner_item,
                    groupNames
            );
            groupAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spEditGroup.setAdapter(groupAdapter);
            spEditGroup.setSelection(selectedIndex);

            spEditGroup.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (position >= 0 && position < groupList.size()) {
                        selectedGroup = groupList.get(position);
                        updateMembersSpinner();
                        updateSplitPreview();
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });

            if (!groupList.isEmpty()) {
                selectedGroup = groupList.get(selectedIndex);
                updateMembersSpinner();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateMembersSpinner() {
        if (selectedGroup == null) return;

        List<String> members = selectedGroup.getMembers();
        if (members.isEmpty()) {
            members = new ArrayList<>();
            members.add("You");
        }

        ArrayAdapter<String> memberAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                members
        );
        memberAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spEditPaidBy.setAdapter(memberAdapter);

        // Retain initial paidBy selection if available
        for (int i = 0; i < members.size(); i++) {
            if (members.get(i).equalsIgnoreCase(initialPaidBy)) {
                spEditPaidBy.setSelection(i);
                break;
            }
        }
    }

    private void loadExistingExpense() {
        if (expenseIndex < 0) {
            Toast.makeText(this, "Error loading expense details.", Toast.LENGTH_SHORT).show();
            finish();
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
            initialPaidBy = obj.getString("paidBy");
            initialGroupId = obj.optString("groupId", "group_pune_flat");

            etEditDescription.setText(description);

            double amountInRupees = amountPaise / 100.0;
            if (amountPaise % 100 == 0) {
                etEditAmount.setText(String.format(Locale.US, "%d", amountPaise / 100));
            } else {
                etEditAmount.setText(String.format(Locale.US, "%.2f", amountInRupees));
            }

            setSelectedSpinnerValue(spEditCategory, categories, category);

            loadGroups();
            updateSplitPreview();

        } catch (Exception e) {
            Toast.makeText(this, "Failed to load expense.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setSelectedSpinnerValue(Spinner spinner, String[] items, String value) {
        for (int i = 0; i < items.length; i++) {
            if (items[i].equalsIgnoreCase(value)) {
                spinner.setSelection(i);
                break;
            }
        }
    }

    private List<MemberSplit> getCalculatedSplits(long amountPaise) {
        List<String> members = (selectedGroup != null && !selectedGroup.getMembers().isEmpty())
                ? selectedGroup.getMembers() : new ArrayList<>();
        if (members.isEmpty()) {
            members.add("You");
            members.add("Rahul");
            members.add("Amit");
        }
        return SplitCalculator.calculateEqualSplits(amountPaise, members);
    }

    private void updateSplitPreview() {
        String amountText = etEditAmount.getText().toString().trim();
        long amountPaise = 0;

        try {
            if (!amountText.isEmpty()) {
                BigDecimal amount = new BigDecimal(amountText);
                if (amount.compareTo(BigDecimal.ZERO) > 0) {
                    amountPaise = amount.setScale(2, RoundingMode.HALF_UP).movePointRight(2).longValueExact();
                }
            }
        } catch (Exception ignored) {}

        List<MemberSplit> splits = getCalculatedSplits(amountPaise);
        StringBuilder sb = new StringBuilder("Equal Split Breakdown:\n");

        for (MemberSplit split : splits) {
            sb.append("• ").append(split.getMemberName()).append(" – ").append(split.getFormattedShare()).append("\n");
        }

        tvEditSplitPreview.setText(sb.toString().trim());
    }

    private void updateExpense() {
        String description = etEditDescription.getText().toString().trim();
        String amountText = etEditAmount.getText().toString().trim();

        if (description.isEmpty()) {
            etEditDescription.setError("Enter an expense description");
            return;
        }

        if (amountText.isEmpty()) {
            etEditAmount.setError("Enter the amount");
            return;
        }

        long amountPaise;
        try {
            BigDecimal amount = new BigDecimal(amountText);
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                etEditAmount.setError("Amount must be greater than zero");
                return;
            }
            amountPaise = amount.setScale(2, RoundingMode.HALF_UP).movePointRight(2).longValueExact();
        } catch (Exception e) {
            etEditAmount.setError("Enter a valid amount");
            return;
        }

        List<MemberSplit> splits = getCalculatedSplits(amountPaise);
        long sumShares = 0;
        JSONArray splitsArray = new JSONArray();

        try {
            for (MemberSplit split : splits) {
                sumShares += split.getSharePaise();
                JSONObject splitObj = new JSONObject();
                splitObj.put("memberName", split.getMemberName());
                splitObj.put("sharePaise", split.getSharePaise());
                splitsArray.put(splitObj);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error calculating split shares.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (sumShares != amountPaise) {
            Toast.makeText(this, "Split calculation mismatch. Cannot update.", Toast.LENGTH_SHORT).show();
            return;
        }

        String category = spEditCategory.getSelectedItem().toString();
        String paidBy = (spEditPaidBy.getSelectedItem() != null) ? spEditPaidBy.getSelectedItem().toString() : "You";
        String groupId = (selectedGroup != null) ? selectedGroup.getId() : "group_pune_flat";

        try {
            String savedData = preferences.getString("expenses", "[]");
            JSONArray expenses = new JSONArray(savedData);

            if (expenseIndex >= 0 && expenseIndex < expenses.length()) {
                JSONObject obj = expenses.getJSONObject(expenseIndex);
                obj.put("description", description);
                obj.put("amountPaise", amountPaise);
                obj.put("category", category);
                obj.put("paidBy", paidBy);
                obj.put("groupId", groupId);
                obj.put("splitType", "EQUAL");
                obj.put("splits", splitsArray);

                preferences.edit().putString("expenses", expenses.toString()).apply();
                Toast.makeText(this, "Expense updated successfully!", Toast.LENGTH_SHORT).show();
                finish();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Unable to update expense.", Toast.LENGTH_SHORT).show();
        }
    }
}
