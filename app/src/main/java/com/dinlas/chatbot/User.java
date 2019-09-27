package com.dinlas.chatbot;

class User {
    
    private static String userName = "";
    
    static String getUserName() {
        return userName;
    }

    static void setUserName(String username) {
        userName = username;
    }
}