package com.example.space.ChatApp.models;

import java.util.ArrayList;

public class FriendList {

    private ArrayList<Friend> friendsList;

    public FriendList() {
        friendsList = new ArrayList<>();
    }

    public ArrayList<Friend> getFriendsList() {
        return friendsList;
    }

    public void setFriendsList(ArrayList<Friend> friendsList) {
        this.friendsList = friendsList;
    }

    public String getAvatarById(String id) {
        for (Friend friend : friendsList) {
            if (id.equals(friend.id)) {
                return friend.getAvatar();
            }
        }
        return "";
    }
}
