package com.xyf.lockers.app;

import android.app.Application;

import com.baidu.crabsdk.CrabSDK;


public class MainAppliction extends Application {

    public static final String BAIDU_APP_KEY = "xhp0k5Fv97eknBYjht7XYwI2";

    @Override
    public void onCreate() {
        super.onCreate();
        CrabSDK.init(this, BAIDU_APP_KEY);
    }
}
