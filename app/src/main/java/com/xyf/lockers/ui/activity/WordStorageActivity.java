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

public class WordStorageActivity extends BaseActivity implements OnSingleLockerStatusListener {
    private static final String TAG = "WordStorageActivity";
    public static final int ADMIN_ACTIVITY = 0;
    public static final int STAORAGE_RECORD_ACTIVITY = 1;
    private int inputCount = 0;
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
    private String lastPassword;
    private PasswordStorageBean mCurrentOpenBean;
    private byte[] mCurrentOpenLockerBytes;
    private List<PasswordStorageBean> mAllBeans;

    @Override
    protected int getLayout() {
        return R.layout.activity_password;
    }

    @Override
    protected void initEventAndData(Bundle savedInstanceState) {
        LockersCommHelperNew.get().setOnSingleLockerStatusListener(WordStorageActivity.this);
        handler.sendEmptyMessageDelayed(0, 500);
        pwdView.setOnFinishInput(new PasswordView.OnPasswordInputFinish() {
            @Override
            public void inputFinish(String password) {
                if (inputCount == 0) {
                    String passwordStorageJson = SharedPreferenceUtil.getPasswordStorageJson();
                    mAllBeans = JSON.parseArray(passwordStorageJson, PasswordStorageBean.class);
                    if (mAllBeans != null) {
                        for (PasswordStorageBean storageBean : mAllBeans) {
                            if (password.equals(storageBean.password)) {
                                Log.i(TAG, "与另外一个用户密码相同,禁止使用此密码");
                                ToastUtil.showMessage("请使用更复杂的密码");
                                pwdView.userHard();
                                return;
                            }
                        }
                        lastPassword = password;
                        pwdView.inputAgin();
                        inputCount++;
                    }
                } else {
                    if (!TextUtils.isEmpty(lastPassword) && lastPassword.equals(password) && mAllBeans != null) {

                        for (PasswordStorageBean passwordStorageBean : mAllBeans) {
                            if (TextUtils.isEmpty(passwordStorageBean.password) && passwordStorageBean.locker != 24) {
                                mCurrentOpenBean = passwordStorageBean;
                                //没有存过,去开门
                                int i = passwordStorageBean.locker;
                                Log.i(TAG, "当前开门索引: " + i);
                                mCurrentOpenLockerBytes = LockerUtils.getOpenSingleLockerBytes(i);
                                LockersCommHelperNew.get().autoLightOpen(mCurrentOpenLockerBytes[0], mCurrentOpenLockerBytes[1], mCurrentOpenLockerBytes[2], mCurrentOpenLockerBytes[3]);
                                return;
                            } else {
                                //存过了,跳过
                            }
                        }
                        Log.i(TAG, ": 柜子已存满");
                        ToastUtil.showMessage("柜子已存满");
                        showTipsActivity("柜子已存满", Color.RED);
                        //两次输入一致,打开柜门
                    } else {
                        //两次输入不一致
                        lastPassword = "";
                        inputCount = 0;
                        pwdView.notSame();
                    }
                }
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
        handler.removeCallbacksAndMessages(null);
        LockersCommHelperNew.get().setOnSingleLockerStatusListener(null);
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
        ArrayList<Integer> openingLockesIndexs = LockerUtils.getOpeningLockesIndexs(boardBinary, lockerBinary);
        if (openingLockesIndexs == null || openingLockesIndexs.isEmpty() || mCurrentOpenBean == null) {
            return;
        }
        for (int i = 0; i < openingLockesIndexs.size(); i++) {
            Integer openingLockesIndex = openingLockesIndexs.get(i);
            if (mCurrentOpenBean.locker == openingLockesIndex) {
                MainAppliction.getInstance().openDoor(openingLockesIndex);
                mCurrentOpenBean.password = lastPassword;
                // TODO: 2019/5/1

                String newJson = JSON.toJSONString(mAllBeans);
                SharedPreferenceUtil.setPasswordStorageJson(newJson);
                showTipsActivity("已打开" + (openingLockesIndex + 1) + "号柜门", mCurrentOpenLockerBytes);
                Log.i(TAG, "onSingleLockerStatusResponse: 当前开的柜门索引为:　" + openingLockesIndex);
                ToastUtil.showMessage("当前开的柜门索引为:　" + openingLockesIndex);
                Log.i(TAG, "onSingleLockerStatusResponse: 开了 " + openingLockesIndexs.size() + "个柜门");
                Log.i(TAG, "onSingleLockerStatusResponse: 当前用户开门的索引为 mCurrentOpenLockerIndex: " + openingLockesIndex + " 当前已开的所有门索引为 openingLockesIndexs: " + openingLockesIndexs);
                break;
            }
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
                showTipsActivity(getString(R.string.seriaport_storage_timeout), Color.RED);
            }
        });
    }
}
