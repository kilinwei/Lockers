package com.xyf.lockers.ui.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.xyf.lockers.R;
import com.xyf.lockers.common.serialport.LockersCommHelper;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        LockersCommHelper.get().init();
                        LockersCommHelper.get().controlSingleLock(24, (int) (2 % 2));
//        Observable.interval(1000*1000, TimeUnit.MILLISECONDS)
//                .subscribeOn(Schedulers.io())
//                .subscribe(new DefaultObserver<Long>() {
//                    @Override
//                    public void onNext(Long o) {
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//
//                    }
//
//                    @Override
//                    public void onComplete() {
//
//                    }
//                });
    }
}
