package com.example.billsplit;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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

public class AddExpenseActivity extends AppCompatActivity {

    private EditText etDescription;
    private EditText etAmount;
    private Spinner spGroup;
    private Spinner spCategory;
    private Spinner spPaidBy;
    private RadioGroup rgSplitType;
    private RadioButton rbEqualSplit;
    private RadioButton rbCustomAmount;
    private RadioButton rbPercentage;
    private LinearLayout llCustomInputs;
    private TextView tvRemainingStatus;
    private TextView tvSplitPreview;

    private SharedPreferences preferences;
    private final List<Group> groupList = new ArrayList<>();
    private Group selectedGroup;

    private final List<EditText> customInputFields = new ArrayList<>();

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
        setContentView(R.layout.activity_add_expense);

        preferences = getSharedPreferences("SplitMateData", MODE_PRIVATE);

        etDescription = findViewById(R.id.etDescription);
        etAmount = findViewById(R.id.etAmount);
        spGroup = findViewById(R.id.spGroup);
        spCategory = findViewById(R.id.spCategory);
        spPaidBy = findViewById(R.id.spPaidBy);
        rgSplitType = findViewById(R.id.rgSplitType);
        rbEqualSplit = findViewById(R.id.rbEqualSplit);
        rbCustomAmount = findViewById(R.id.rbCustomAmount);
        rbPercentage = findViewById(R.id.rbPercentage);
        llCustomInputs = findViewById(R.id.llCustomInputs);
        tvRemainingStatus = findViewById(R.id.tvRemainingStatus);
        tvSplitPreview = findViewById(R.id.tvSplitPreview);

        setSpinnerAdapter(spCategory, categories);

        loadGroups();

        etAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateSplitUI();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        rgSplitType.setOnCheckedChangeListener((group, checkedId) -> updateSplitUI());

        Button btnSaveExpense = findViewById(R.id.btnSaveExpense);
        Button btnCancel = findViewById(R.id.btnCancel);
        View btnHeaderBack = findViewById(R.id.btnHeaderBack);

        if (btnHeaderBack != null) {
            btnHeaderBack.setOnClickListener(v -> finish());
        }

        btnSaveExpense.setOnClickListener(view -> saveExpense());
        btnCancel.setOnClickListener(view -> finish());
    }

    private void loadGroups() {
        String savedGroups = preferences.getString("groups", "[]");
        groupList.clear();

        try {
            JSONArray array = new JSONArray(savedGroups);

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
            }

            List<String> groupNames = new ArrayList<>();
            String activeGroupId = preferences.getString("active_group_id", "group_pune_flat");
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

                if (id.equals(activeGroupId)) {
                    selectedIndex = i;
                }
            }

            ArrayAdapter<String> groupAdapter = new ArrayAdapter<>(
                    this,
                    android.R.layout.simple_spinner_item,
                    groupNames
            );
            groupAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spGroup.setAdapter(groupAdapter);
            spGroup.setSelection(selectedIndex);

            spGroup.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (position >= 0 && position < groupList.size()) {
                        selectedGroup = groupList.get(position);
                        updateMembersSpinner();
                        updateSplitUI();
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
        spPaidBy.setAdapter(memberAdapter);
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

    private List<String> getGroupMembers() {
        if (selectedGroup != null && !selectedGroup.getMembers().isEmpty()) {
            return selectedGroup.getMembers();
        }
        List<String> members = new ArrayList<>();
        members.add("You");
        members.add("Rahul");
        members.add("Amit");
        return members;
    }

    private void updateSplitUI() {
        List<String> members = getGroupMembers();

        if (rbEqualSplit.isChecked()) {
            llCustomInputs.setVisibility(View.GONE);
            tvRemainingStatus.setVisibility(View.GONE);
            updateEqualPreview(members);
        } else {
            llCustomInputs.setVisibility(View.VISIBLE);
            tvRemainingStatus.setVisibility(View.VISIBLE);

            boolean isAmountMode = rbCustomAmount.isChecked();
            setupCustomInputs(members, isAmountMode);
            calculateCustomSplit(members, isAmountMode);
        }
    }

    private void updateEqualPreview(List<String> members) {
        long amountPaise = getAmountInPaise();
        List<MemberSplit> splits = SplitCalculator.calculateEqualSplits(amountPaise, members);

        StringBuilder sb = new StringBuilder("Equal Split Breakdown:\n");
        for (MemberSplit split : splits) {
            sb.append("• ").append(split.getMemberName()).append(" – ").append(split.getFormattedShare()).append("\n");
        }
        tvSplitPreview.setText(sb.toString().trim());
    }

    private void setupCustomInputs(List<String> members, boolean isAmountMode) {
        llCustomInputs.removeAllViews();
        customInputFields.clear();

        float density = getResources().getDisplayMetrics().density;
        int etWidthPx = (int) (130 * density);
        int etHeightPx = (int) (44 * density);
        int paddingPx = (int) (10 * density);
        int rowPaddingY = (int) (6 * density);

        for (String member : members) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(android.view.Gravity.CENTER_VERTICAL);
            row.setPadding(0, rowPaddingY, 0, rowPaddingY);

            TextView label = new TextView(this);
            LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
            label.setLayoutParams(labelParams);
            label.setText(member);
            label.setTextColor(0xFF172033);
            label.setTextSize(15);
            label.setTypeface(null, android.graphics.Typeface.BOLD);

            EditText et = new EditText(this);
            LinearLayout.LayoutParams etParams = new LinearLayout.LayoutParams(etWidthPx, etHeightPx);
            et.setLayoutParams(etParams);
            et.setHint(isAmountMode ? "₹0.00" : "0%");
            et.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            et.setBackgroundResource(R.drawable.bg_input_field);
            et.setPadding(paddingPx, 0, paddingPx, 0);
            et.setTextColor(0xFF172033);
            et.setTextSize(14);

            et.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    calculateCustomSplit(members, isAmountMode);
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });

            row.addView(label);
            row.addView(et);

            llCustomInputs.addView(row);
            customInputFields.add(et);
        }
    }

    private void calculateCustomSplit(List<String> members, boolean isAmountMode) {
        long totalPaise = getAmountInPaise();

        if (isAmountMode) {
            long enteredPaise = 0;
            List<Long> customAmounts = new ArrayList<>();

            for (EditText et : customInputFields) {
                String val = et.getText().toString().trim();
                long valPaise = 0;
                try {
                    if (!val.isEmpty()) {
                        valPaise = new BigDecimal(val).setScale(2, RoundingMode.HALF_UP).movePointRight(2).longValueExact();
                    }
                } catch (Exception ignored) {}
                customAmounts.add(valPaise);
                enteredPaise += valPaise;
            }

            long diffPaise = totalPaise - enteredPaise;
            if (diffPaise == 0 && totalPaise > 0) {
                tvRemainingStatus.setText("Total matches expense amount ✓");
                tvRemainingStatus.setTextColor(0xFF246B50);
            } else if (diffPaise > 0) {
                tvRemainingStatus.setText(String.format(Locale.getDefault(), "Remaining amount to allocate: ₹%.2f", diffPaise / 100.0));
                tvRemainingStatus.setTextColor(0xFFC24141);
            } else {
                tvRemainingStatus.setText(String.format(Locale.getDefault(), "Exceeds total expense by: ₹%.2f", (-diffPaise) / 100.0));
                tvRemainingStatus.setTextColor(0xFFC24141);
            }

            List<MemberSplit> splits = SplitCalculator.calculateExactSplits(members, customAmounts);
            StringBuilder sb = new StringBuilder("Custom Amount Split:\n");
            for (MemberSplit split : splits) {
                sb.append("• ").append(split.getMemberName()).append(" – ").append(split.getFormattedShare()).append("\n");
            }
            tvSplitPreview.setText(sb.toString().trim());

        } else {
            double enteredPercent = 0;
            List<Double> percentages = new ArrayList<>();

            for (EditText et : customInputFields) {
                String val = et.getText().toString().trim();
                double pVal = 0;
                try {
                    if (!val.isEmpty()) {
                        pVal = Double.parseDouble(val);
                    }
                } catch (Exception ignored) {}
                percentages.add(pVal);
                enteredPercent += pVal;
            }

            double diffPercent = 100.0 - enteredPercent;
            if (Math.abs(diffPercent) < 0.01) {
                tvRemainingStatus.setText("Total percentage equals 100% ✓");
                tvRemainingStatus.setTextColor(0xFF246B50);
            } else if (diffPercent > 0) {
                tvRemainingStatus.setText(String.format(Locale.getDefault(), "Remaining percentage: %.1f%%", diffPercent));
                tvRemainingStatus.setTextColor(0xFFC24141);
            } else {
                tvRemainingStatus.setText(String.format(Locale.getDefault(), "Exceeds 100%% by: %.1f%%", -diffPercent));
                tvRemainingStatus.setTextColor(0xFFC24141);
            }

            List<MemberSplit> splits = SplitCalculator.calculatePercentageSplits(totalPaise, members, percentages);
            StringBuilder sb = new StringBuilder("Percentage Split Breakdown:\n");
            for (int i = 0; i < splits.size(); i++) {
                double pct = (i < percentages.size()) ? percentages.get(i) : 0;
                sb.append("• ").append(splits.get(i).getMemberName())
                        .append(" (").append(String.format(Locale.getDefault(), "%.1f%%", pct)).append(") – ")
                        .append(splits.get(i).getFormattedShare()).append("\n");
            }
            tvSplitPreview.setText(sb.toString().trim());
        }
    }

    private long getAmountInPaise() {
        String text = etAmount.getText().toString().trim();
        try {
            if (!text.isEmpty()) {
                BigDecimal amount = new BigDecimal(text);
                if (amount.compareTo(BigDecimal.ZERO) > 0) {
                    return amount.setScale(2, RoundingMode.HALF_UP).movePointRight(2).longValueExact();
                }
            }
        } catch (Exception ignored) {}
        return 0;
    }

    private void saveExpense() {
        String description = etDescription.getText().toString().trim();
        String amountText = etAmount.getText().toString().trim();

        if (description.isEmpty()) {
            etDescription.setError("Enter an expense description");
            return;
        }

        if (amountText.isEmpty()) {
            etAmount.setError("Enter the amount");
            return;
        }

        long amountPaise;
        try {
            BigDecimal amount = new BigDecimal(amountText);
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                etAmount.setError("Amount must be greater than zero");
                return;
            }
            amountPaise = amount.setScale(2, RoundingMode.HALF_UP).movePointRight(2).longValueExact();
        } catch (Exception e) {
            etAmount.setError("Enter a valid amount");
            return;
        }

        List<String> members = getGroupMembers();
        String splitType = "EQUAL";
        List<MemberSplit> splits;

        if (rbEqualSplit.isChecked()) {
            splitType = "EQUAL";
            splits = SplitCalculator.calculateEqualSplits(amountPaise, members);
        } else if (rbCustomAmount.isChecked()) {
            splitType = "EXACT";
            List<Long> customAmounts = new ArrayList<>();
            long sum = 0;
            for (EditText et : customInputFields) {
                String val = et.getText().toString().trim();
                long valPaise = 0;
                try {
                    if (!val.isEmpty()) {
                        valPaise = new BigDecimal(val).setScale(2, RoundingMode.HALF_UP).movePointRight(2).longValueExact();
                    }
                } catch (Exception ignored) {}
                customAmounts.add(valPaise);
                sum += valPaise;
            }

            if (sum != amountPaise) {
                Toast.makeText(this, "Custom amounts must sum to exact expense amount!", Toast.LENGTH_LONG).show();
                return;
            }
            splits = SplitCalculator.calculateExactSplits(members, customAmounts);

        } else {
            splitType = "PERCENT";
            List<Double> percentages = new ArrayList<>();
            double sumPct = 0;
            for (EditText et : customInputFields) {
                String val = et.getText().toString().trim();
                double pVal = 0;
                try {
                    if (!val.isEmpty()) {
                        pVal = Double.parseDouble(val);
                    }
                } catch (Exception ignored) {}
                percentages.add(pVal);
                sumPct += pVal;
            }

            if (Math.abs(sumPct - 100.0) > 0.01) {
                Toast.makeText(this, "Percentages must sum to exactly 100%!", Toast.LENGTH_LONG).show();
                return;
            }
            splits = SplitCalculator.calculatePercentageSplits(amountPaise, members, percentages);
        }

        // Final sanity check: sum of splits must equal expense amount
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
            Toast.makeText(this, "Error formatting splits.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (sumShares != amountPaise) {
            Toast.makeText(this, "Split sum mismatch. Cannot save.", Toast.LENGTH_SHORT).show();
            return;
        }

        String category = spCategory.getSelectedItem().toString();
        String paidBy = (spPaidBy.getSelectedItem() != null)
                ? spPaidBy.getSelectedItem().toString() : "You";
        String groupId = (selectedGroup != null) ? selectedGroup.getId() : "group_pune_flat";

        try {
            String savedData = preferences.getString("expenses", "[]");
            JSONArray expenses = new JSONArray(savedData);

            JSONObject expense = new JSONObject();
            expense.put("description", description);
            expense.put("amountPaise", amountPaise);
            expense.put("category", category);
            expense.put("paidBy", paidBy);
            expense.put("groupId", groupId);
            expense.put("splitType", splitType);
            expense.put("splits", splitsArray);
            expense.put("date", System.currentTimeMillis());

            expenses.put(expense);

            boolean saved = preferences.edit().putString("expenses", expenses.toString()).commit();

            if (saved) {
                Toast.makeText(this, "Expense saved successfully!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Unable to save expense.", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Toast.makeText(this, "Error saving expense.", Toast.LENGTH_SHORT).show();
        }
    }
}
