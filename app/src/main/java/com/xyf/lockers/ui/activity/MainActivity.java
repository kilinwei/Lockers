package com.xyf.lockers.ui.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.tencent.bugly.crashreport.CrashReport;
import com.xyf.lockers.R;
import com.xyf.lockers.common.serialport.LockersCommHelper;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.observers.DefaultObserver;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        LockersCommHelper.get().init();
        LockersCommHelper.get().controlSingleLock(24, (int) (2 % 2));
        Observable.interval(10 * 1000, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .subscribe(new DefaultObserver<Long>() {
                    @Override
                    public void onNext(Long o) {
                        CrashReport.testJavaCrash();
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }
}
