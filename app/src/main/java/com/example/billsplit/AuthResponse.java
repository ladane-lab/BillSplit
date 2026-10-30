package com.example.billsplit;

public class AuthResponse {
    private boolean success;
    private String message;
    private String token;
    private UserData user;

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public String getToken() {
        return token;
    }

    public UserData getUser() {
        return user;
    }

    public static class UserData {
        private String id;
        private String name;
        private String email;

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getEmail() {
            return email;
        }
    }
}
