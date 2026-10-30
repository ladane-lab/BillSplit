package com.example.billsplit;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class GroupsActivity extends AppCompatActivity {

    private RecyclerView rvGroups;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groups);

        preferences = getSharedPreferences("SplitMateData", MODE_PRIVATE);

        rvGroups = findViewById(R.id.rvGroups);
        View btnBack = findViewById(R.id.btnBack);
        Button btnCreateGroup = findViewById(R.id.btnCreateGroup);

        rvGroups.setLayoutManager(new LinearLayoutManager(this));
        rvGroups.setNestedScrollingEnabled(false);

        btnBack.setOnClickListener(v -> finish());
        btnCreateGroup.setOnClickListener(v -> {
            Intent intent = new Intent(GroupsActivity.this, AddGroupActivity.class);
            startActivity(intent);
        });

        ensureDefaultGroup();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadGroups();
    }

    private void ensureDefaultGroup() {
        String savedGroups = preferences.getString("groups", null);
        if (savedGroups == null || savedGroups.trim().isEmpty() || savedGroups.equals("[]")) {
            try {
                JSONArray array = new JSONArray();
                JSONObject defaultGroup = new JSONObject();
                defaultGroup.put("id", "group_pune_flat");
                defaultGroup.put("name", "Pune Flat");
                defaultGroup.put("type", "Flat 🏠");

                JSONArray members = new JSONArray();
                members.put("You");
                members.put("Rahul");
                members.put("Amit");
                defaultGroup.put("members", members);
                defaultGroup.put("createdAt", System.currentTimeMillis());

                array.put(defaultGroup);
                preferences.edit().putString("groups", array.toString()).apply();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void loadGroups() {
        ensureDefaultGroup();
        String savedGroups = preferences.getString("groups", "[]");
        List<Group> groupList = new ArrayList<>();

        try {
            JSONArray array = new JSONArray(savedGroups);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                String id = obj.optString("id", String.valueOf(i));
                String name = obj.getString("name");
                String type = obj.optString("type", "Group");

                JSONArray membersArray = obj.optJSONArray("members");
                List<String> members = new ArrayList<>();
                if (membersArray != null) {
                    for (int j = 0; j < membersArray.length(); j++) {
                        members.add(membersArray.getString(j));
                    }
                }

                long createdAt = obj.optLong("createdAt", 0);
                groupList.add(new Group(id, name, type, members, createdAt));
            }

            GroupAdapter adapter = new GroupAdapter(groupList);
            adapter.setOnGroupClickListener(group -> {
                Intent intent = new Intent(GroupsActivity.this, GroupDetailActivity.class);
                intent.putExtra("group_id", group.getId());
                startActivity(intent);
            });
            rvGroups.setAdapter(adapter);

        } catch (Exception e) {
            Toast.makeText(this, "Error loading groups.", Toast.LENGTH_SHORT).show();
        }
    }
}
