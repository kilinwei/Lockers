package com.xyf.lockers.ui.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import com.baidu.idl.facesdk.model.Feature;
import com.baidu.idl.facesdk.utils.PreferencesUtil;
import com.tencent.bugly.beta.Beta;
import com.xyf.lockers.R;
import com.xyf.lockers.app.MainAppliction;
import com.xyf.lockers.base.BaseActivity;
import com.xyf.lockers.common.GlobalSet;
import com.xyf.lockers.common.serialport.LockersCommHelperNew;
import com.xyf.lockers.db.DBManager;
import com.xyf.lockers.listener.OnAllLockersStatusListener;
import com.xyf.lockers.manager.UserInfoManager;
import com.xyf.lockers.utils.LockerUtils;
import com.xyf.lockers.utils.SharedPreferenceUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;


public class MainActivity
        extends BaseActivity implements OnAllLockersStatusListener {
    private static final String TAG = "MainActivity";
    public static final String CHECK_CLOSE = "check_close";
    public static final int MSG_CHECK_CLOSE = 0x123;
    public static final int MSG_TEST = 0x111;
    @BindView(R.id.btn_storage)
    Button btnStorage;
    @BindView(R.id.btn_take)
    Button btnTake;
    @BindView(R.id.btn_temporary_take)
    Button btnEmporaryTake;
    @BindView(R.id.btn_control_test)
    Button mBtnControlTest;
    @BindView(R.id.btn_control_query)
    Button mBtnQuery;
    @BindView(R.id.fl_hide)
    FrameLayout mFlHide;
    @BindView(R.id.fl_hide_right)
    FrameLayout flHideRight;
    private List<Feature> mListFeatureInfo;
    private UserInfoManager.UserInfoListener mUserInfoListener;
    int i;
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_TEST:
                    if (i % 2 == 0) {
                        btnStorage.performClick();
                    } else {
                        btnTake.performClick();
                    }
                    i++;
                    break;
                case MSG_CHECK_CLOSE:
                    LockersCommHelperNew.get().queryAll(mCurrentOpenLockerBytes[0]);
//                    Log.i(TAG, "handleMessage: 判断,如果此时用户未关门,控制闪灯");
                    break;
            }
        }
    };
    private byte[] mCurrentOpenLockerBytes;

    @Override
    protected void onStart() {
        super.onStart();
//        if (BuildConfig.DEBUG) {
//            mHandler.sendEmptyMessageDelayed(MSG_TEST, 10 * 1000);
//        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        LockersCommHelperNew.get().setOnAllLockersStatusListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        LockersCommHelperNew.get().setOnAllLockersStatusListener(null);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mHandler.removeCallbacksAndMessages(null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LockersCommHelperNew.get().uninit();
    }

    @Override
    protected int getLayout() {
        return R.layout.activity_main;
    }

    @Override
    protected void initEventAndData(Bundle savedInstanceState) {
        LockersCommHelperNew.get().init();
        mUserInfoListener = new UserListener();
        DBManager.getInstance().init(getApplicationContext());
        UserInfoManager.getInstance().getFeatureInfo(null, mUserInfoListener);
        PreferencesUtil.putInt(GlobalSet.TYPE_PREVIEW_ANGLE, GlobalSet.TYPE_TPREVIEW_NINETY_ANGLE);
        mFlHide.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Intent intent = new Intent(MainActivity.this, PasswordActivity.class);
                intent.putExtra(PasswordActivity.TARGET,PasswordActivity.ADMIN_ACTIVITY);
                startActivity(intent);
                return false;
            }
        });
        flHideRight.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
//                Intent intent = new Intent(MainActivity.this, PasswordActivity.class);
//                intent.putExtra(PasswordActivity.TARGET,PasswordActivity.STAORAGE_RECORD_ACTIVITY);
//                startActivity(intent);
                String passwordStorageJson = SharedPreferenceUtil.getPasswordStorageJson();
                Log.i(TAG, "onLongClick: "+ passwordStorageJson);
                return false;
            }
        });
        Beta.checkUpgrade(false, false);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        mCurrentOpenLockerBytes = intent.getByteArrayExtra(CHECK_CLOSE);
        if (mCurrentOpenLockerBytes != null && mCurrentOpenLockerBytes.length == 4) {
            mHandler.sendEmptyMessageDelayed(MSG_CHECK_CLOSE, 10 * 1000);
        }
    }

    @OnClick({R.id.btn_storage,
            R.id.btn_take,
            R.id.btn_temporary_take,
            R.id.btn_control_query,
            R.id.btn_control_test})
    public void onViewClicked(View view) {
        Intent intent = null;
        switch (view.getId()) {
            case R.id.btn_storage:
                intent = new Intent(this, StorageActivity.class);
                break;
            case R.id.btn_take:
                intent = new Intent(this, TakeActivity.class);
                break;
            case R.id.btn_temporary_take:
                intent = new Intent(this, StorageActivity.class);
                break;
            case R.id.btn_control_test:
                intent = new Intent(this, ControlTestActivityNew.class);
                break;
            case R.id.btn_control_query:
                intent = new Intent(this, UserActivity.class);
                break;
        }
        if (intent != null) {
            startActivity(intent);
        }
    }

    @Override
    public void onAllLockersStatusResponse(int allLockers) {

    }

    @Override
    public void onAllLockersStatusResponse(byte[] bRec) {
        int boardBinary = bRec[1];
        byte lockerBinary = bRec[2];
        ArrayList<Integer> openingLockesIndexs = LockerUtils.getOpeningLockesIndexs(boardBinary, lockerBinary);
        if (openingLockesIndexs != null && openingLockesIndexs.size() > 0) {
            MainAppliction.getInstance().playShortMusic(R.raw.close_door,"close_door.mp3");
            mHandler.sendEmptyMessageDelayed(MSG_CHECK_CLOSE, 5 * 1000);
        }else{
            mHandler.removeMessages(MSG_CHECK_CLOSE);
        }
    }

    @Override
    public void disConnectDevice() {

    }

    @Override
    public void onResponseTime() {
        Log.i(TAG, "onResponseTime: 检查门是否关闭,串口数据返回超时");
    }


    // 用于返回读取数据库的结果
    private class UserListener extends UserInfoManager.UserInfoListener {

        // 人脸库信息查找成功
        @Override
        public void featureQuerySuccess(final List<Feature> listFeatureInfo) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (listFeatureInfo == null || listFeatureInfo.size() == 0) {
                        mListFeatureInfo = null;
                        Log.i(TAG, "run: 查询到百度人脸库数量为空");
                    } else {
                        mListFeatureInfo = listFeatureInfo;
                        for (Feature feature : mListFeatureInfo) {
                            Log.i(TAG, "run: feature：　" + feature.getUserName());
                        }
                        Log.i(TAG, "run: 查询到百度人脸库数量为: " + listFeatureInfo.size());
                    }
                }
            });
        }

        // 人脸库信息查找失败
        @Override
        public void featureQueryFailure(final String message) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                }
            });
        }

        // 显示删除进度条
        @Override
        public void showDeleteProgressDialog(final float progress) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                }
            });
        }

        // 删除成功
        @Override
        public void deleteSuccess() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.i(TAG, "run: 百度人脸库删除成功");
                    // 读取数据库信息
                    UserInfoManager.getInstance().getFeatureInfo(null, mUserInfoListener);
                }
            });

        }
    }
}
