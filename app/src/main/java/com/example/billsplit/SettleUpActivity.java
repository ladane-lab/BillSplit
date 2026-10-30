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
import java.util.UUID;

public class SettleUpActivity extends AppCompatActivity {

    private Spinner spSettleGroup;
    private Spinner spSettlePayer;
    private Spinner spSettleReceiver;
    private EditText etSettleAmount;
    private TextView tvOutstandingDebtNotice;

    private SharedPreferences preferences;
    private final List<Group> groupList = new ArrayList<>();
    private Group selectedGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settle_up);

        preferences = getSharedPreferences("SplitMateData", MODE_PRIVATE);

        spSettleGroup = findViewById(R.id.spSettleGroup);
        spSettlePayer = findViewById(R.id.spSettlePayer);
        spSettleReceiver = findViewById(R.id.spSettleReceiver);
        etSettleAmount = findViewById(R.id.etSettleAmount);
        tvOutstandingDebtNotice = findViewById(R.id.tvOutstandingDebtNotice);

        Button btnConfirmSettle = findViewById(R.id.btnConfirmSettle);
        Button btnSettleCancel = findViewById(R.id.btnSettleCancel);

        loadGroups();

        AdapterView.OnItemSelectedListener memberChangeListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateDebtNotice();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        };

        spSettlePayer.setOnItemSelectedListener(memberChangeListener);
        spSettleReceiver.setOnItemSelectedListener(memberChangeListener);

        btnConfirmSettle.setOnClickListener(v -> saveSettlement());
        btnSettleCancel.setOnClickListener(v -> finish());
    }

    private void loadGroups() {
        String savedGroups = preferences.getString("groups", "[]");
        groupList.clear();

        try {
            JSONArray array = new JSONArray(savedGroups);
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
            spSettleGroup.setAdapter(groupAdapter);
            spSettleGroup.setSelection(selectedIndex);

            spSettleGroup.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (position >= 0 && position < groupList.size()) {
                        selectedGroup = groupList.get(position);
                        updateMemberSpinners();
                        updateDebtNotice();
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });

            if (!groupList.isEmpty()) {
                selectedGroup = groupList.get(selectedIndex);
                updateMemberSpinners();
                updateDebtNotice();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateMemberSpinners() {
        if (selectedGroup == null) return;

        List<String> members = selectedGroup.getMembers();
        if (members.isEmpty()) {
            members = new ArrayList<>();
            members.add("You");
        }

        ArrayAdapter<String> payerAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                members
        );
        payerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spSettlePayer.setAdapter(payerAdapter);

        ArrayAdapter<String> receiverAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                members
        );
        receiverAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spSettleReceiver.setAdapter(receiverAdapter);

        // Pre-select second member as receiver if available
        if (members.size() > 1) {
            spSettleReceiver.setSelection(1);
        }
    }

    private void updateDebtNotice() {
        if (spSettlePayer.getSelectedItem() == null || spSettleReceiver.getSelectedItem() == null) {
            return;
        }

        String payer = spSettlePayer.getSelectedItem().toString();
        String receiver = spSettleReceiver.getSelectedItem().toString();

        if (payer.equalsIgnoreCase(receiver)) {
            tvOutstandingDebtNotice.setText("Payer and Receiver cannot be the same person.");
            tvOutstandingDebtNotice.setTextColor(0xFFC24141);
            return;
        }

        // Calculate current debts
        List<Expense> groupExpenses = loadGroupExpenses();
        List<Settlement> groupSettlements = loadGroupSettlements();
        java.util.Map<String, Long> balances = BalanceCalculator.calculateNetBalances(
                selectedGroup != null ? selectedGroup.getMembers() : new ArrayList<>(),
                groupExpenses,
                groupSettlements
        );
        List<Debt> debts = BalanceCalculator.calculateSimplifiedDebts(balances);

        long debtPaise = 0;
        for (Debt debt : debts) {
            if (debt.getFromMember().equalsIgnoreCase(payer) && debt.getToMember().equalsIgnoreCase(receiver)) {
                debtPaise = debt.getAmountPaise();
                break;
            }
        }

        if (debtPaise > 0) {
            String formattedDebt = String.format(Locale.getDefault(), "₹%.2f", debtPaise / 100.0);
            tvOutstandingDebtNotice.setText(payer + " currently owes " + receiver + " " + formattedDebt);
            tvOutstandingDebtNotice.setTextColor(0xFF246B50);
            etSettleAmount.setText(String.format(Locale.US, "%.2f", debtPaise / 100.0));
        } else {
            tvOutstandingDebtNotice.setText("No recorded debt between " + payer + " and " + receiver + ".");
            tvOutstandingDebtNotice.setTextColor(0xFF687386);
        }
    }

    private List<Expense> loadGroupExpenses() {
        List<Expense> list = new ArrayList<>();
        if (selectedGroup == null) return list;

        String savedData = preferences.getString("expenses", "[]");
        try {
            JSONArray expenses = new JSONArray(savedData);
            for (int i = 0; i < expenses.length(); i++) {
                JSONObject obj = expenses.getJSONObject(i);
                if (selectedGroup.getId().equals(obj.optString("groupId", "group_pune_flat"))) {
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
                    list.add(new Expense(id, description, amountPaise, category, paidBy, date, i, selectedGroup.getId(), splitType, splitList));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    private List<Settlement> loadGroupSettlements() {
        List<Settlement> list = new ArrayList<>();
        if (selectedGroup == null) return list;

        String savedData = preferences.getString("settlements", "[]");
        try {
            JSONArray array = new JSONArray(savedData);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                if (selectedGroup.getId().equals(obj.optString("groupId"))) {
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

    private void saveSettlement() {
        if (spSettlePayer.getSelectedItem() == null || spSettleReceiver.getSelectedItem() == null) {
            Toast.makeText(this, "Select payer and receiver.", Toast.LENGTH_SHORT).show();
            return;
        }

        String payer = spSettlePayer.getSelectedItem().toString();
        String receiver = spSettleReceiver.getSelectedItem().toString();

        if (payer.equalsIgnoreCase(receiver)) {
            Toast.makeText(this, "Payer and receiver cannot be the same.", Toast.LENGTH_SHORT).show();
            return;
        }

        String amountText = etSettleAmount.getText().toString().trim();
        if (amountText.isEmpty()) {
            etSettleAmount.setError("Enter settlement amount");
            return;
        }

        long amountPaise;
        try {
            BigDecimal amount = new BigDecimal(amountText);
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                etSettleAmount.setError("Amount must be greater than zero");
                return;
            }
            amountPaise = amount.setScale(2, RoundingMode.HALF_UP).movePointRight(2).longValueExact();
        } catch (Exception e) {
            etSettleAmount.setError("Enter a valid amount");
            return;
        }

        String groupId = (selectedGroup != null) ? selectedGroup.getId() : "group_pune_flat";
        String settlementId = "settle_" + UUID.randomUUID().toString().substring(0, 8);

        try {
            String savedData = preferences.getString("settlements", "[]");
            JSONArray array = new JSONArray(savedData);

            JSONObject settlementObj = new JSONObject();
            settlementObj.put("id", settlementId);
            settlementObj.put("groupId", groupId);
            settlementObj.put("payer", payer);
            settlementObj.put("receiver", receiver);
            settlementObj.put("amountPaise", amountPaise);
            settlementObj.put("date", System.currentTimeMillis());

            array.put(settlementObj);

            preferences.edit().putString("settlements", array.toString()).apply();
            Toast.makeText(this, "Settlement recorded successfully!", Toast.LENGTH_SHORT).show();
            finish();

        } catch (Exception e) {
            Toast.makeText(this, "Failed to record settlement.", Toast.LENGTH_SHORT).show();
        }
    }
}
