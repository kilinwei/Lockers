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

    public static final int SCREEN_INDEX = 25;

    private static SharedPreferences getAppSp() {
        return MainAppliction.getInstance()
                .getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    public static int getAllLockersStatus() {
        return getAppSp().getInt(Constants.SP_ALL_LOCKERS_STATUS, 1 << (SCREEN_INDEX - 1));
    }

    public static void setAllLockersStatus(int allLockersStatus) {
        allLockersStatus |= (1 << (SCREEN_INDEX - 1));
        getAppSp().edit()
                .putInt(Constants.SP_ALL_LOCKERS_STATUS, allLockersStatus)
                .apply();
    }

    public static int getUpDownAngle() {
        return getAppSp().getInt(Constants.SP_UP_DOWN_ANGLE, 20);
    }

    public static void setUpDownAngle(int angle) {
        getAppSp().edit()
                .putInt(Constants.SP_UP_DOWN_ANGLE, angle)
                .apply();
    }

    public static int getLeftRightAngle() {
        return getAppSp().getInt(Constants.SP_LEFT_RIGHT_ANGLE, 20);
    }

    public static void setLeftRightAngle(int angle) {
        getAppSp().edit()
                .putInt(Constants.SP_LEFT_RIGHT_ANGLE, angle)
                .apply();
    }

    public static int getRotateAngle() {
        return getAppSp().getInt(Constants.SP_ROTATE_ANGLE, 20);
    }

    public static void setRotateAngle(int angle) {
        getAppSp().edit()
                .putInt(Constants.SP_ROTATE_ANGLE, angle)
                .apply();
    }

    public static String getPasswordStorageJson() {
        return getAppSp().getString(Constants.SP_PASSWORD_JSON, "[{\"locker\":0,\"password\":\"\"},{\"locker\":1,\"password\":\"\"},{\"locker\":2,\"password\":\"\"},{\"locker\":3,\"password\":\"\"},{\"locker\":4,\"password\":\"\"},{\"locker\":5,\"password\":\"\"},{\"locker\":6,\"password\":\"\"},{\"locker\":7,\"password\":\"\"},{\"locker\":8,\"password\":\"\"},{\"locker\":9,\"password\":\"\"},{\"locker\":10,\"password\":\"\"},{\"locker\":11,\"password\":\"\"},{\"locker\":12,\"password\":\"\"},{\"locker\":13,\"password\":\"\"},{\"locker\":14,\"password\":\"\"},{\"locker\":15,\"password\":\"\"},{\"locker\":16,\"password\":\"\"},{\"locker\":17,\"password\":\"\"},{\"locker\":18,\"password\":\"\"},{\"locker\":19,\"password\":\"\"},{\"locker\":20,\"password\":\"\"},{\"locker\":21,\"password\":\"\"},{\"locker\":22,\"password\":\"\"},{\"locker\":23,\"password\":\"\"},{\"locker\":24,\"password\":\"\"},{\"locker\":25,\"password\":\"\"},{\"locker\":26,\"password\":\"\"},{\"locker\":27,\"password\":\"\"},{\"locker\":28,\"password\":\"\"},{\"locker\":29,\"password\":\"\"},{\"locker\":30,\"password\":\"\"},{\"locker\":31,\"password\":\"\"}]");
    }

    public static void setPasswordStorageJson(String json) {
        getAppSp().edit()
                .putString(Constants.SP_PASSWORD_JSON, json)
                .apply();
    }
}

