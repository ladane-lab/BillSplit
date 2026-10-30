package com.example.billsplit;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SettlementHistoryActivity extends AppCompatActivity {

    private RecyclerView rvSettlementHistory;
    private TextView tvEmptySettlementHistory;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settlement_history);

        preferences = getSharedPreferences("SplitMateData", MODE_PRIVATE);

        rvSettlementHistory = findViewById(R.id.rvSettlementHistory);
        tvEmptySettlementHistory = findViewById(R.id.tvEmptySettlementHistory);
        View btnBack = findViewById(R.id.btnBack);

        rvSettlementHistory.setLayoutManager(new LinearLayoutManager(this));
        rvSettlementHistory.setNestedScrollingEnabled(false);

        btnBack.setOnClickListener(v -> finish());

        loadSettlementHistory();
    }

    private void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    private void loadSettlementHistory() {
        String activeGroupId = preferences.getString("active_group_id", "group_pune_flat");
        String savedData = preferences.getString("settlements", "[]");
        List<Settlement> settlementList = new ArrayList<>();

        try {
            JSONArray array = new JSONArray(savedData);
            for (int i = array.length() - 1; i >= 0; i--) {
                JSONObject obj = array.getJSONObject(i);
                if (activeGroupId.equals(obj.optString("groupId", "group_pune_flat"))) {
                    settlementList.add(new Settlement(
                            obj.optString("id"),
                            obj.optString("groupId", "group_pune_flat"),
                            obj.getString("payer"),
                            obj.getString("receiver"),
                            obj.getLong("amountPaise"),
                            obj.optLong("date")
                    ));
                }
            }

            if (settlementList.isEmpty()) {
                tvEmptySettlementHistory.setVisibility(View.VISIBLE);
                rvSettlementHistory.setVisibility(View.GONE);
            } else {
                tvEmptySettlementHistory.setVisibility(View.GONE);
                rvSettlementHistory.setVisibility(View.VISIBLE);

                SettlementAdapter adapter = new SettlementAdapter(settlementList);
                rvSettlementHistory.setAdapter(adapter);
            }

        } catch (Exception e) {
            tvEmptySettlementHistory.setVisibility(View.VISIBLE);
            rvSettlementHistory.setVisibility(View.GONE);
        }
    }
}
