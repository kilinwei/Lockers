package com.xyf.lockers.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import com.baidu.idl.facesdk.model.Feature;
import com.baidu.idl.facesdk.utils.PreferencesUtil;
import com.tencent.bugly.beta.Beta;
import com.xyf.lockers.R;
import com.xyf.lockers.base.BaseActivity;
import com.xyf.lockers.common.GlobalSet;
import com.xyf.lockers.common.serialport.LockersCommHelperNew;
import com.xyf.lockers.db.DBManager;
import com.xyf.lockers.manager.UserInfoManager;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;


public class MainActivity
        extends BaseActivity {
    private static final String TAG = "MainActivity";
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
    private List<Feature> mListFeatureInfo;
    private UserInfoManager.UserInfoListener mUserInfoListener;


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
                startActivity(intent);
                return false;
            }
        });
        Beta.checkUpgrade(false,false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LockersCommHelperNew.get().uninit();
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
                intent = new Intent(this, TemporaryTakeActivity.class);
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
