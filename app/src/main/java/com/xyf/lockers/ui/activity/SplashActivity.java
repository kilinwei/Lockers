package com.xyf.lockers.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.Toast;

import com.baidu.idl.facesdk.FaceAuth;
import com.baidu.idl.facesdk.callback.AuthCallback;
import com.baidu.idl.facesdk.utils.PreferencesUtil;
import com.xyf.lockers.R;
import com.xyf.lockers.base.BaseActivity;
import com.xyf.lockers.common.GlobalSet;
import com.xyf.lockers.db.DBManager;
import com.xyf.lockers.manager.FaceSDKManager;
import com.xyf.lockers.utils.ToastUtil;

import butterknife.BindView;

import static com.xyf.lockers.common.GlobalSet.LICENSE_ONLINE;

public class SplashActivity extends BaseActivity {
    private static final String TAG = "SplashActivity";

    @BindView(R.id.iv_splash_bg)
    ImageView ivSplashBg;
    private AlphaAnimation mAnimation;
    private FaceAuth faceAuth;

    @Override
    protected int getLayout() {
        return R.layout.activity_splash;
    }

    @Override
    protected void initEventAndData(Bundle savedInstanceState) {
        mAnimation = new AlphaAnimation(0.3f, 1.0f);
        mAnimation.setDuration(300);
        ivSplashBg.startAnimation(mAnimation);
        mAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                initLicence();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        faceAuth = new FaceAuth();
        // 建议3288板子flagsThreads设置2,3399板子设置4
        faceAuth.setAnakinThreadsConfigure(2, 0);
    }

    private void initLicence() {
        if (GlobalSet.getLicenseStatus() == 2) {
            String key = GlobalSet.getLicenseOnLineKey();
            initLicenseOnLine(key);
        } else {
//            startActivity(new Intent(this, MainActivity.class));
            startActivity(new Intent(this, LicenseActivity.class));
            finish();
        }
        Log.i(TAG, "initLicence: 序列号: "+ PreferencesUtil.getString("activate_on_key", ""));
    }

    // 在线鉴权
    private void initLicenseOnLine(final String key) {
        if (TextUtils.isEmpty(key)) {
            Toast.makeText(this, "序列号不能为空!", Toast.LENGTH_SHORT).show();
            return;
        }
        faceAuth.initLicenseOnLine(this, key, new AuthCallback() {
            @Override
            public void onResponse(final int code, final String response, String licenseKey) {
                if (code == 0) {
                    GlobalSet.FACE_AUTH_STATUS = 0;
                    // 初始化人脸
                    FaceSDKManager.getInstance().initModel(SplashActivity.this);
                    // 初始化数据库
                    DBManager.getInstance().init(getApplicationContext());
                    // 加载feature 内存
                    FaceSDKManager.getInstance().setFeature();
                    GlobalSet.setLicenseOnLineKey(key);
                    GlobalSet.setLicenseStatus(LICENSE_ONLINE);
                    startActivity(new Intent(SplashActivity.this, MainActivity.class));
                    finish();
                } else {
                    ToastUtil.showMessage(code + "  " + response);
                }
            }
        });
    }
}
