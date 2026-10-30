package com.example.billsplit;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.UUID;

public class AddGroupActivity extends AppCompatActivity {

    private EditText etGroupName;
    private Spinner spGroupType;
    private EditText etGroupMembers;
    private SharedPreferences preferences;

    private final String[] groupTypes = {
            "Flat 🏠",
            "Trip ✈️",
            "Home 🏡",
            "Office 🏢",
            "Other 👥"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_group);

        preferences = getSharedPreferences("SplitMateData", MODE_PRIVATE);

        etGroupName = findViewById(R.id.etGroupName);
        spGroupType = findViewById(R.id.spGroupType);
        etGroupMembers = findViewById(R.id.etGroupMembers);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                groupTypes
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spGroupType.setAdapter(adapter);

        Button btnSaveGroup = findViewById(R.id.btnSaveGroup);
        Button btnCancelGroup = findViewById(R.id.btnCancelGroup);
        android.view.View btnHeaderBack = findViewById(R.id.btnHeaderBack);

        if (btnHeaderBack != null) {
            btnHeaderBack.setOnClickListener(v -> finish());
        }

        btnSaveGroup.setOnClickListener(v -> saveGroup());
        btnCancelGroup.setOnClickListener(v -> finish());
    }

    private void saveGroup() {
        String groupName = etGroupName.getText().toString().trim();
        String membersText = etGroupMembers.getText().toString().trim();

        if (groupName.isEmpty()) {
            etGroupName.setError("Enter group name");
            return;
        }

        JSONArray membersArray = new JSONArray();

        if (!membersText.isEmpty()) {
            String[] parts = membersText.split(",");
            for (String part : parts) {
                String cleanName = part.trim();
                if (!cleanName.isEmpty()) {
                    membersArray.put(cleanName);
                }
            }
        }

        // Always ensure "You" is included as a member if list is empty or doesn't have it
        boolean hasYou = false;
        for (int i = 0; i < membersArray.length(); i++) {
            if (membersArray.optString(i).equalsIgnoreCase("You")) {
                hasYou = true;
                break;
            }
        }
        if (!hasYou) {
            membersArray.put("You");
        }

        String groupType = spGroupType.getSelectedItem().toString();
        String groupId = "group_" + UUID.randomUUID().toString().substring(0, 8);

        try {
            String savedGroups = preferences.getString("groups", "[]");
            JSONArray array = new JSONArray(savedGroups);

            JSONObject newGroup = new JSONObject();
            newGroup.put("id", groupId);
            newGroup.put("name", groupName);
            newGroup.put("type", groupType);
            newGroup.put("members", membersArray);
            newGroup.put("createdAt", System.currentTimeMillis());

            array.put(newGroup);

            preferences.edit().putString("groups", array.toString()).apply();
            Toast.makeText(this, "Group created successfully!", Toast.LENGTH_SHORT).show();
            finish();

        } catch (Exception e) {
            Toast.makeText(this, "Failed to create group.", Toast.LENGTH_SHORT).show();
        }
    }
}
