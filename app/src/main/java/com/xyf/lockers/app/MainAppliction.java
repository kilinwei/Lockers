package com.xyf.lockers.app;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;

import com.baidu.crabsdk.CrabSDK;
import com.squareup.leakcanary.LeakCanary;
import com.tencent.bugly.Bugly;
import com.xyf.lockers.model.bean.DaoMaster;
import com.xyf.lockers.model.bean.DaoSession;

public class MainAppliction extends Application {

    public static final String BAIDU_APP_KEY = "xhp0k5Fv97eknBYjht7XYwI2";
    private static final String APP_ID = "d4d0ebc9af";//bugly ID
    private static MainAppliction instance;
    private DaoSession mDaoSession;

    public static synchronized MainAppliction getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        CrabSDK.init(this, BAIDU_APP_KEY);
        initBugly();
        initGreenDao();
        //初始化内存泄漏检测
        LeakCanary.install(MainAppliction.getInstance());
    }

    private void initGreenDao() {
        DaoMaster.DevOpenHelper devOpenHelper = new DaoMaster.DevOpenHelper(this, "locker.db");
        SQLiteDatabase db = devOpenHelper.getWritableDatabase();
        DaoMaster daoMaster = new DaoMaster(db);
        mDaoSession = daoMaster.newSession();
    }

    public DaoSession getDaoSession(){
        return mDaoSession;
    }

    private static void initBugly() {
//        if (BuildConfig.DEBUG) {
//            return;
//        }
        //true表示app启动自动初始化升级模块; false不会自动初始化; 开发者如果担心sdk初始化影响app启动速度，可以设置为false，在后面某个时刻手动调用Beta.init(getApplicationContext(),false);
//        Beta.autoInit = true;
////        //true表示初始化时自动检查升级; false表示不会自动检查升级,需要手动调用Beta.checkUpgrade()方法;
////        Beta.autoCheckUpgrade = true;
////        //设置升级检查周期为60s(默认检查周期为0s)，60s内SDK不重复向后台请求策略);
////        Beta.upgradeCheckPeriod = 60 * 1000;
////        //设置启动延时为1s（默认延时3s），APP启动1s后初始化SDK，避免影响APP启动速度;
////        Beta.initDelay = 1 * 1000;
////        //设置sd卡的Download为更新资源存储目录
////        Beta.storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
////        Beta.showInterruptedStrategy = true;
////        //升级SDK默认是开启热更新能力的，如果你不需要使用热更新，可以将这个接口设置为false。
////        Beta.enableHotfix = true;
//        CrashReport.initCrashReport(getInstance(), APP_ID, true);
        Bugly.init(getInstance(), APP_ID, true);
    }
}
