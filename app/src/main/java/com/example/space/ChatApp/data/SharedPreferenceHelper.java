package com.example.space.ChatApp.data;


import android.content.Context;
import android.content.SharedPreferences;

import com.example.space.ChatApp.models.User;

/**
 * to keep track of user who logged in now
 */

public class SharedPreferenceHelper {
    private static SharedPreferenceHelper instance = null;
    private static SharedPreferences preferences;
    private static SharedPreferences.Editor editor;
    private static String SHARE_USER_INFO = "userinfo";
    private static String SHARE_KEY_NAME = "name";
    private static String SHARE_KEY_EMAIL = "email";
    private static String SHARE_KEY_AVATAR = "avatar";
    private static String SHARE_KEY_UID = "uid";
    private static String SHARE_KEY_TOKEN = "token";
    private static String SHARE_KEY_BIO = "bioText";


    private SharedPreferenceHelper() {
    }

    public static SharedPreferenceHelper getInstance(Context context) {
        if (instance == null) {
            instance = new SharedPreferenceHelper();
            preferences = context.getSharedPreferences(SHARE_USER_INFO, Context.MODE_PRIVATE);
            editor = preferences.edit();
        }
        return instance;
    }

    public void saveUserInfo(User user) {
        editor.putString(SHARE_KEY_NAME, user.getName());
        editor.putString(SHARE_KEY_EMAIL, user.getEmail());
        editor.putString(SHARE_KEY_AVATAR, user.getAvatar());
        editor.putString(SHARE_KEY_UID, StaticConfig.UID);
        editor.putString(SHARE_KEY_TOKEN, user.getToken());
        editor.putString(SHARE_KEY_BIO, user.getBioText());
        editor.apply();
    }

    public User getUserInfo() {
        String userName = preferences.getString(SHARE_KEY_NAME, "");
        String email = preferences.getString(SHARE_KEY_EMAIL, "");
        String avatar = preferences.getString(SHARE_KEY_AVATAR, "default");

        User user = new User();
        user.setName(userName);
        user.setEmail(email);
        user.setAvatar(avatar);

        return user;
    }

    public String getUID() {
        return preferences.getString(SHARE_KEY_UID, "");
    }
}
