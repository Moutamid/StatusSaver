package com.grapplications.statussaver.utils;

import androidx.multidex.MultiDexApplication;

public class StatusSaver extends MultiDexApplication {
    private static StatusSaver mInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
    }

    public static synchronized StatusSaver getInstance() {
        return mInstance;
    }

    public void setConnectivityListener(ConnectivityReceiver.ConnectivityReceiverListener listener) {
        ConnectivityReceiver.listener = listener;
    }
}
