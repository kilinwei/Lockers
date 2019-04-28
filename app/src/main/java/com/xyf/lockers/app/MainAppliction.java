package com.xyf.lockers.app;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.res.AssetFileDescriptor;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaPlayer;

import com.baidu.crabsdk.CrabSDK;
import com.squareup.leakcanary.LeakCanary;
import com.tencent.bugly.Bugly;
import com.xyf.lockers.model.bean.DaoMaster;
import com.xyf.lockers.model.bean.DaoSession;

import java.io.IOException;

public class MainAppliction extends Application {

    public static final String BAIDU_APP_KEY = "xhp0k5Fv97eknBYjht7XYwI2";
    private static final String APP_ID = "d4d0ebc9af";//bugly ID
    private static MainAppliction instance;
    private DaoSession mDaoSession;
    private MediaPlayer mMp;

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

    public DaoSession getDaoSession() {
        return mDaoSession;
    }

    @SuppressLint("CheckResult")
    public void playShortMusic(final int music, String name) {
//        if (BuildConfig.DEBUG) {
//            return;
//        }
        if (mMp == null) {
            mMp = new MediaPlayer();
        } else {
            mMp.stop();
            mMp.reset();
        }
        AssetFileDescriptor file = getResources().openRawResourceFd(music);
        try {
            mMp.setDataSource(file.getFileDescriptor(), file.getStartOffset(), file.getLength());
            mMp.prepare();
            file.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mMp.setVolume(1f, 1f);
        mMp.setLooping(false);
        mMp.start();
    }

    public void openDoor(int index) {
        playShortMusic(Constants.OPEN_DOOR_AUDIOS[index], Constants.names[index]);
    }

    private static void initBugly() {
        Bugly.init(getInstance(), APP_ID, true);
    }
}
