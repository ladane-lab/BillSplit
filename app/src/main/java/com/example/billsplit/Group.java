package com.example.billsplit;

import java.util.ArrayList;
import java.util.List;

public class Group {
    private String id;
    private String name;
    private String type;
    private List<String> members;
    private long createdAt;

    public Group(String id, String name, String type, List<String> members, long createdAt) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.members = members != null ? members : new ArrayList<>();
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public List<String> getMembers() {
        return members;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public String getMemberCountText() {
        int count = members.size();
        return count + (count == 1 ? " member" : " members");
    }

    public String getFormattedMembersList() {
        if (members.isEmpty()) {
            return "No members";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < members.size(); i++) {
            sb.append(members.get(i));
            if (i < members.size() - 1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }
}
