package com.xyf.lockers.base;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.baidu.idl.facesdk.utils.PreferencesUtil;
import com.xyf.lockers.manager.FaceSDKManager;
import com.xyf.lockers.ui.activity.MainActivity;
import com.xyf.lockers.ui.activity.ShowTipsActivity;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by litonghui on 2018/11/16.
 */

public abstract class BaseActivity extends Activity {
    private static final String TAG = "BaseActivity";
    protected TextView mLableTxt;
    private Unbinder mUnBinder;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(getLayout());
        PreferencesUtil.initPrefs(this);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        requestPermissions(99);
        mUnBinder = ButterKnife.bind(this);
        initEventAndData(savedInstanceState);
    }


    protected void initFaceData() {
        int num = FaceSDKManager.getInstance().setFeature();
        Log.i(TAG, "initFaceData: " + String.format("底库人脸数: %s 个", num));
    }


    // 请求权限
    public void requestPermissions(int requestCode) {
        try {
            if (Build.VERSION.SDK_INT >= 23) {
                ArrayList<String> requestPerssionArr = new ArrayList<>();
                int hasCamrea = checkSelfPermission(Manifest.permission.CAMERA);
                if (hasCamrea != PackageManager.PERMISSION_GRANTED) {
                    requestPerssionArr.add(Manifest.permission.CAMERA);
                }

                int hasSdcardRead = checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
                if (hasSdcardRead != PackageManager.PERMISSION_GRANTED) {
                    requestPerssionArr.add(Manifest.permission.READ_EXTERNAL_STORAGE);
                }

                int hasSdcardWrite = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                if (hasSdcardWrite != PackageManager.PERMISSION_GRANTED) {
                    requestPerssionArr.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                }
                // 是否应该显示权限请求
                if (!requestPerssionArr.isEmpty()) {
                    String[] requestArray = new String[requestPerssionArr.size()];
                    for (int i = 0; i < requestArray.length; i++) {
                        requestArray[i] = requestPerssionArr.get(i);
                    }
                    requestPermissions(requestArray, requestCode);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        boolean flag = false;
        for (int i = 0; i < permissions.length; i++) {
            if (PackageManager.PERMISSION_GRANTED == grantResults[i]) {
                flag = true;
            }
        }
        if (!flag) {
            Log.i("BaseActivity", "权限未申请");
        }
    }

    public void onBackClick(View view) {
        finish();
    }

    protected abstract int getLayout();

    protected abstract void initEventAndData(Bundle savedInstanceState);


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mUnBinder.unbind();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        hideBottomUIMenu();
    }

    /**
     * 隐藏虚拟按键，并且全屏
     */
    public void hideBottomUIMenu() {
        //隐藏虚拟按键，并且全屏
        if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) { // lower api
            View v = this.getWindow().getDecorView();
            v.setSystemUiVisibility(View.GONE);
        } else if (Build.VERSION.SDK_INT >= 19) {
            //for new api versions.
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }

    protected void showTipsActivity(String tip) {
        Intent intent = new Intent(this, ShowTipsActivity.class);
        intent.putExtra(ShowTipsActivity.TIPS, tip);
        startActivity(intent);
        finish();
    }

    protected void showTipsActivity(String tip, int color) {
        Intent intent = new Intent(this, ShowTipsActivity.class);
        intent.putExtra(ShowTipsActivity.TIPS, tip);
        intent.putExtra(ShowTipsActivity.TEXT_COLOR, color);
        startActivity(intent);
        finish();
    }

    protected void showTipsActivity(String tip, byte[] openingLockers) {
        Intent intent = new Intent(this, ShowTipsActivity.class);
        intent.putExtra(ShowTipsActivity.TIPS, tip);
        intent.putExtra(MainActivity.CHECK_CLOSE, openingLockers);
        startActivity(intent);
        finish();
    }


}
