package com.xyf.lockers.app;

import android.annotation.SuppressLint;
import android.app.Application;
import android.database.sqlite.SQLiteDatabase;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.support.annotation.RequiresApi;

import com.baidu.crabsdk.CrabSDK;
import com.squareup.leakcanary.LeakCanary;
import com.tencent.bugly.Bugly;
import com.xyf.lockers.model.bean.DaoMaster;
import com.xyf.lockers.model.bean.DaoSession;

import io.reactivex.Observable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

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

    public DaoSession getDaoSession() {
        return mDaoSession;
    }

    @SuppressLint("CheckResult")
    public void playShortMusic(final int music) {
//        if (BuildConfig.DEBUG) {
//            return;
//        }
        Observable.just(1).subscribeOn(Schedulers.io())
                .subscribe(new Consumer<Integer>() {
                    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public void accept(Integer integer) throws Exception {
                        final SoundPool.Builder soundPollBuilder = new SoundPool.Builder();
                        soundPollBuilder.setMaxStreams(1);
                        AudioAttributes.Builder AudioAttributesBuilder = new AudioAttributes.Builder();
                        AudioAttributesBuilder.setContentType(AudioAttributes.CONTENT_TYPE_MUSIC);
                        AudioAttributesBuilder.setFlags(AudioAttributes.FLAG_AUDIBILITY_ENFORCED);
                        AudioAttributesBuilder.setLegacyStreamType(AudioManager.STREAM_MUSIC);
                        AudioAttributesBuilder.setUsage(AudioAttributes.USAGE_MEDIA);
                        soundPollBuilder.setAudioAttributes(AudioAttributesBuilder.build());
                        SoundPool soundPool = soundPollBuilder.build();
                        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
                            @Override
                            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                                soundPool.play(sampleId, 1f, 1f, 0, 0, 1);
                            }
                        });
                        soundPool.load(getInstance(), music, 1);
                    }
                });
    }

    public void openDoor(int index) {
        playShortMusic(Constants.OPEN_DOOR_AUDIOS[index]);
    }

    private static void initBugly() {
        Bugly.init(getInstance(), APP_ID, true);
    }
}
