package com.example.campusgo.sharedpreferences;

import android.content.Context;
import android.content.SharedPreferences;

public class LoginStorage {

    // Almacena las credenciales en SharedPreferences
    public static void saveCredentials(Context context, String user, String password) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("user_credentials", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("user", user);
        editor.putString("password", password);
        editor.apply();
    }

    public static void clearCredentials(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("user_credentials", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

    public static String[] getCredentials(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("user_credentials", context.MODE_PRIVATE);
        String user = sharedPreferences.getString("user", null);
        String password = sharedPreferences.getString("password", null);
        return new String[] {user, password};
    }

    public static boolean autoLogin(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("user_credentials", context.MODE_PRIVATE);
        String user = sharedPreferences.getString("user", null);
        String password = sharedPreferences.getString("password", null);
        if (user != null && password != null) {
            return true;
        }
        return false;
    }


    public static int getUserId(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("user_credentials", Context.MODE_PRIVATE);
        return sharedPreferences.getInt("user_id", 0);
    }
    public static void saveUserId(Context context, int userId) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("user_credentials", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("user_id", userId);
        editor.apply();

    }

}
