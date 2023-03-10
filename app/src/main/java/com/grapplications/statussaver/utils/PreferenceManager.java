package com.grapplications.statussaver.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceManager {
    private final SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private static final String PREF_NAME = "STATUS_SAVER_PREF";
    private static final String FIRST_ENTRY = "IS_FIRST_ENTRY";
    private static final String WHICH_APP = "WHICH_APP";
    private static final String IS_WA_PERMISSION_GRANTED = "IS_WA_PERMISSION_GRANTED";
    private static final String IS_W4B_PERMISSION_GRANTED = "IS_W4B_PERMISSION_GRANTED";
    private static final String SAVED_ROUTE_WA = "SAVED_ROUTE_WA";
    private static final String SAVED_ROUTE_W4B = "SAVED_ROUTE_W4B";
    private static final String PAGER_COUNT = "PAGER_COUNT";
    private static final String IMAGE_ADAPTER_COUNT = "IMAGE_ADAPTER_COUNT";
    private static final String VIDEO_ADAPTER_COUNT = "VIDEO_ADAPTER_COUNT";
    private static final String SAVE_ADAPTER_COUNT = "SAVE_ADAPTER_COUNT";
    private static final String OPEN_COUNT = "OPEN_COUNT";
    private static final String NIGHT_MODE = "IS_NIGHT_MODE";
    private static final String AD_CLICK = "AD_CLICK";

    public PreferenceManager(Context context) {
        int PRIVATE_MODE = 0;
        sharedPreferences = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
    }

    public void createPreference() {
        editor = sharedPreferences.edit();
        editor.putBoolean(FIRST_ENTRY, true);
        editor.apply();
    }

    // For first time open
    public boolean isFirstEntry() {
        return sharedPreferences.getBoolean(FIRST_ENTRY, false);
    }

    // Which app chosen by user
    public void setApp(String appName) {
        editor = sharedPreferences.edit();
        editor.putString(WHICH_APP, appName);
        editor.commit();
    }

    public String getApp() {
        return sharedPreferences.getString(WHICH_APP, "WhatsApp");
    }

    // Path for WhatsApp
    public void setWaSavedRoute(String string) {
        editor = sharedPreferences.edit();
        editor.putString(SAVED_ROUTE_WA, string);
        editor.commit();
    }

    public String getWaSavedRoute() {
        return sharedPreferences.getString(SAVED_ROUTE_WA, null);
    }

    // Path for WhatsApp
    public void setW4bSavedRoute(String string) {
        editor = sharedPreferences.edit();
        editor.putString(SAVED_ROUTE_W4B, string);
        editor.commit();
    }

    public String getW4bSavedRoute() {
        return sharedPreferences.getString(SAVED_ROUTE_W4B, null);
    }

    // Slider tab selection count
    public void setPagerCount(int count) {
        editor = sharedPreferences.edit();
        editor.putInt(PAGER_COUNT, count);
        editor.commit();
    }

    public int getPagerCount() {
        return sharedPreferences.getInt(PAGER_COUNT, 0);
    }

    public void clearPagerCount() {
        editor = sharedPreferences.edit();
        editor.remove(PAGER_COUNT);
        editor.commit();
    }

    // Image adapter count
    public void setImageAdapterCount(int count) {
        editor = sharedPreferences.edit();
        editor.putInt(IMAGE_ADAPTER_COUNT, count);
        editor.commit();
    }

    public int getImageAdapterCount() {
        return sharedPreferences.getInt(IMAGE_ADAPTER_COUNT, 0);
    }

    // Video adapter count
    public void setVideoAdapterCount(int count) {
        editor = sharedPreferences.edit();
        editor.putInt(VIDEO_ADAPTER_COUNT, count);
        editor.commit();
    }

    public int getVideoAdapterCount() {
        return sharedPreferences.getInt(VIDEO_ADAPTER_COUNT, 0);
    }

    // Saved video adapter count
    public void setSaveAdapterCount(int count) {
        editor = sharedPreferences.edit();
        editor.putInt(SAVE_ADAPTER_COUNT, count);
        editor.commit();
    }

    public int getSaveAdapterCount() {
        return sharedPreferences.getInt(SAVE_ADAPTER_COUNT, 0);
    }

    // Item click count
    public void storeItemClick(int count) {
        editor = sharedPreferences.edit();
        if (count == 0) editor.putInt(AD_CLICK, 0);
        else if (sharedPreferences.getInt(AD_CLICK, 0) == 5) editor.putInt(AD_CLICK, 0);
        else editor.putInt(AD_CLICK, (sharedPreferences.getInt(AD_CLICK, 0) + count));

        editor.commit();
    }

    public int getItemClick() {
        return sharedPreferences.getInt(AD_CLICK, 0);
    }

    // Application opening count for rating bar initialization
    public int setOpenCount() {
        editor = sharedPreferences.edit();
        if (sharedPreferences.getInt(OPEN_COUNT, 0) == 9) {
            editor.putInt(OPEN_COUNT, 0);

        } else if (sharedPreferences.getInt(OPEN_COUNT, 0) >= 60) {
            editor.putInt(OPEN_COUNT, 10);
        } else {
            editor.putInt(OPEN_COUNT, (sharedPreferences.getInt(OPEN_COUNT, 0) + 1));
        }
        editor.apply();

        return sharedPreferences.getInt(OPEN_COUNT, 0);
    }

    public void disableRateDialog() {
        editor = sharedPreferences.edit();
        editor.putInt(OPEN_COUNT, 10);
        editor.apply();
    }

    // Night mode status
    public void setNightMode(boolean state) {
        editor = sharedPreferences.edit();
        editor.putBoolean(NIGHT_MODE, state);
        editor.apply();
    }

    public boolean loadNightMode() {
        return sharedPreferences.getBoolean(NIGHT_MODE, false);
    }
}
