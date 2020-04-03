package com.example.space.ChatApp.models;


import java.util.ArrayList;

public class Notifications {

    private ArrayList<Request> requests;

    public Notifications() {
        requests = new ArrayList<>();
    }

    public ArrayList<Request> getRequests() {
        return requests;
    }
}
