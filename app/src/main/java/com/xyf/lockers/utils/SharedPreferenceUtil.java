package com.xyf.lockers.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.xyf.lockers.app.Constants;
import com.xyf.lockers.app.MainAppliction;

/**
 * @ 项目名： EwaaAndroidApp
 * @ 包名： com.szyh.ewaalauncher.util
 * @ 文件名: SharedPreferenceUtil
 * @ 创建者: ruanhouli
 * @ 创建时间: 2016/11/23 17:42
 * @ 描述： 共享文件工具类，程序中所有SP都由它来完成
 */
public class SharedPreferenceUtil {

    private static final String SHARED_PREFERENCES_NAME = "robot_sp";

    private static SharedPreferences getAppSp() {
        return MainAppliction.getInstance()
                .getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    public static int getAllLockersStatus() {
        return getAppSp().getInt(Constants.SP_ALL_LOCKERS_STATUS, 0);
    }

    public static void setAllLockersStatus(int allLockersStatus) {
        getAppSp().edit()
                .putInt(Constants.SP_ALL_LOCKERS_STATUS, allLockersStatus)
                .apply();
    }

}

