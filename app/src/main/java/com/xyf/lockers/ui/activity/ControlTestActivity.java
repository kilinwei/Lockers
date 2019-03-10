package com.xyf.lockers.ui.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.xyf.lockers.R;
import com.xyf.lockers.base.BaseActivity;
import com.xyf.lockers.common.serialport.LockersCommHelper;

import butterknife.BindView;
import butterknife.OnClick;

public class ControlTestActivity
        extends BaseActivity {
    private static final String TAG = "ControlTestActivity";
    @BindView(R.id.btn_auto_upload)
    Button mBtnAutoUpload;
    @BindView(R.id.btn_un_upload)
    Button mBtnUnUpload;
    @BindView(R.id.tv_lock_status)
    TextView mTvLockStatus;
    @BindView(R.id.edit_lock)
    EditText mEditLock;
    @BindView(R.id.btn_open_lock)
    Button mBtnOpenLock;
    @BindView(R.id.btn_get_all_lightstatus)
    Button mBtnGetAllLightstatus;
    @BindView(R.id.btn_all_light_open)
    Button mBtnAllLightOpen;
    @BindView(R.id.btn_all_light_close)
    Button mBtnAllLightClose;
    @BindView(R.id.btn_all_light_flicker)
    Button mBtnAllLightFlicker;
    @BindView(R.id.edit_light)
    EditText mEditLight;
    @BindView(R.id.btn_single_light_open)
    Button mBtnSingleLightOpen;
    @BindView(R.id.btn_single_light_close)
    Button mBtnSingleLightClose;
    @BindView(R.id.btn_qurey_all_lock_status)
    Button mBtnQureyAllLockStatus;
    private String singleLight;

    @Override
    protected int getLayout() {
        return R.layout.activity_control_test;
    }

    @Override
    protected void initEventAndData(Bundle savedInstanceState) {

    }

    @OnClick({R.id.btn_auto_upload,
            R.id.btn_un_upload,
            R.id.btn_open_lock,
            R.id.btn_get_all_lightstatus,
            R.id.btn_all_light_open,
            R.id.btn_all_light_close,
            R.id.btn_all_light_flicker,
            R.id.btn_single_light_open,
            R.id.btn_single_light_close,
            R.id.btn_qurey_all_lock_status})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_auto_upload:
                LockersCommHelper.get().autoUpload(true);
                break;
            case R.id.btn_un_upload:
                LockersCommHelper.get().autoUpload(false);
                break;
            case R.id.btn_open_lock:
                String singleLock = mEditLock.getText().toString().trim();
                if (!TextUtils.isEmpty(singleLock)) {
//                    LockersCommHelper.get().controlSingleLock(Integer.parseInt(singleLock), 1);
                }
                break;
            case R.id.btn_get_all_lightstatus:
                LockersCommHelper.get().getAllLightStatus();
                break;
            case R.id.btn_all_light_open:
                LockersCommHelper.get().controlAllLight(1);
                break;
            case R.id.btn_all_light_close:
                LockersCommHelper.get().controlAllLight(0);
                break;
            case R.id.btn_all_light_flicker:
                LockersCommHelper.get().controlAllLight(2);
                break;
            case R.id.btn_single_light_open:
                singleLight = mEditLight.getText().toString().trim();
                if (!TextUtils.isEmpty(singleLight)) {
                    LockersCommHelper.get().controlSingleLight(Integer.parseInt(singleLight), 1);
                }
                break;
            case R.id.btn_single_light_close:
                singleLight = mEditLight.getText().toString().trim();
                if (!TextUtils.isEmpty(singleLight)) {
                    LockersCommHelper.get().controlSingleLight(Integer.parseInt(singleLight), 0);
                }
                break;
            case R.id.btn_qurey_all_lock_status:
//                LockersCommHelper.get().getAllLockStatus();
                break;
        }
    }
}
