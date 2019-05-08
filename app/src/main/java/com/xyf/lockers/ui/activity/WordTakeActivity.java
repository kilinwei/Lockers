package com.xyf.lockers.ui.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.xyf.lockers.R;
import com.xyf.lockers.app.MainAppliction;
import com.xyf.lockers.base.BaseActivity;
import com.xyf.lockers.common.serialport.LockersCommHelperNew;
import com.xyf.lockers.listener.OnSingleLockerStatusListener;
import com.xyf.lockers.model.bean.PasswordStorageBean;
import com.xyf.lockers.utils.LockerUtils;
import com.xyf.lockers.utils.SharedPreferenceUtil;
import com.xyf.lockers.utils.ToastUtil;
import com.xyf.lockers.view.PasswordView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

public class WordTakeActivity extends BaseActivity implements OnSingleLockerStatusListener {
    private static final String TAG = "WordTakeActivity";
    @BindView(R.id.pwd_view)
    PasswordView pwdView;
    @BindView(R.id.id_timer_close)
    TextView mTimerText;
    private int time = 60;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            mTimerText.setText("(" + time-- + "秒后自动关闭)");
            if (time == 0) {
                finish();
            } else {
                handler.sendEmptyMessageDelayed(0, 1000);
            }

        }
    };
    private List<PasswordStorageBean> mAllBeans;
    private PasswordStorageBean mCurrentOpenBean;
    private byte[] mCurrentOpenLockerBytes;

    @Override
    protected int getLayout() {
        return R.layout.activity_password;
    }

    @Override
    protected void initEventAndData(Bundle savedInstanceState) {
        LockersCommHelperNew.get().setOnSingleLockerStatusListener(this);
        handler.sendEmptyMessageDelayed(0, 500);
        pwdView.setOnFinishInput(new PasswordView.OnPasswordInputFinish() {
            @Override
            public void inputFinish(String password) {
                String passwordStorageJson = SharedPreferenceUtil.getPasswordStorageJson();
                mAllBeans = JSON.parseArray(passwordStorageJson, PasswordStorageBean.class);
                for (PasswordStorageBean passwordStorageBean : mAllBeans) {
                    if (!TextUtils.isEmpty(passwordStorageBean.password)
                            && passwordStorageBean.password.equals(password)) {
                        mCurrentOpenBean = passwordStorageBean;
                        int i = passwordStorageBean.locker;
                        Log.i(TAG, "当前开门索引: " + i);
                        mCurrentOpenLockerBytes = LockerUtils.getOpenSingleLockerBytes(i);
                        LockersCommHelperNew.get().autoLightOpen(mCurrentOpenLockerBytes[0], mCurrentOpenLockerBytes[1], mCurrentOpenLockerBytes[2], mCurrentOpenLockerBytes[3]);
                        return;
                    }
                }
                    //说明没有存物品,提醒用户没有存物品
                    ToastUtil.showMessage(" 说明没有存物品,提醒用户没有存物品");
                    Log.i(TAG, "onCallback: 说明没有存物品,提醒用户没有存物品");
                    showTipsActivity(getString(R.string.no_storage), Color.RED);
            }
        });
        pwdView.setOnPasswordClose(new PasswordView.OnPasswordClose() {
            @Override
            public void onPasswordClose() {
                finish();
                Log.i(TAG, "onPasswordClose: ");
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LockersCommHelperNew.get().setOnSingleLockerStatusListener(null);
        handler.removeCallbacksAndMessages(null);
    }

    private void closeActivity(int resultCode) {
        handler.removeCallbacksAndMessages(null);
        handler = null;
        setResult(resultCode);
        finish();
    }

    @Override
    public void onSingleLockerStatusResponse(int way, int status) {

    }

    @Override
    public void onSingleLockerStatusResponse(byte[] bRec) {
        int boardBinary = bRec[1];
        byte lockerBinary = bRec[2];
        ArrayList<Integer> lockers = LockerUtils.getOpeningLockesIndexs(boardBinary, lockerBinary);
        if (lockers != null) {
            ToastUtil.showMessage("取出成功");
            for (final Integer locker : lockers) {
                if (mCurrentOpenBean.locker == locker) {
                    mCurrentOpenBean.password = "";
                    String newJson = JSON.toJSONString(mAllBeans);
                    SharedPreferenceUtil.setPasswordStorageJson(newJson);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            MainAppliction.getInstance().openDoor(locker);
                            showTipsActivity("已打开" + (locker + 1) + "号柜门", mCurrentOpenLockerBytes);
                        }
                    });
                }
            }
        } else {
            Log.i(TAG, "onSingleLockerStatusResponse: 没有打开任何柜门");
        }
    }

    @Override
    public void disConnectDevice() {

    }

    @Override
    public void onResponseTime() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showTipsActivity(getString(R.string.seriaport_take_timeout), Color.RED);
            }
        });
    }
}
