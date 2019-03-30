package com.xyf.lockers.ui.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.xyf.lockers.R;
import com.xyf.lockers.base.BaseActivity;
import com.xyf.lockers.common.serialport.LockersCommHelperNew;
import com.xyf.lockers.utils.LockerUtils;
import com.xyf.lockers.utils.ToastUtils;

import butterknife.BindView;
import butterknife.OnClick;

public class ControlTestActivityNew
        extends BaseActivity {
    private static final String TAG = "ControlTestActivityNew";
    @BindView(R.id.edit_circuit_board)
    EditText mEditCircuitBoard;
    @BindView(R.id.edit_locker)
    EditText mEditLocker;
    @BindView(R.id.edit_light)
    EditText mEditLight;
    @BindView(R.id.edit_sensor)
    EditText mEditSensor;
    @BindView(R.id.btn_open_locker)
    Button mBtnOpenLocker;
    @BindView(R.id.btn_query_circuit_board)
    Button mBtnQueryCircuitBoard;
    @BindView(R.id.btn_query_all)
    Button mBtnQueryAll;
    @BindView(R.id.btn_auto_light)
    Button mBtnAutoLight;


    @Override
    protected int getLayout() {
        return R.layout.activity_control_test_new;
    }

    @Override
    protected void initEventAndData(Bundle savedInstanceState) {
        if (!LockersCommHelperNew.get().isOpenDev()) {
            LockersCommHelperNew.get().init();
        }
    }

    @OnClick({R.id.btn_open_locker, R.id.btn_query_circuit_board, R.id.btn_query_all, R.id.btn_auto_light})
    public void onViewClicked(View view) {
        int circuitBoard;
        int locker;
        int light;
        int sonsor;
        byte circuitBoardSend;
        byte lockerSend;
        byte lightSend;
        byte sonsorSend;
        String circuitBoardStr = mEditCircuitBoard.getText().toString().trim();
        String lockerStr = mEditLocker.getText().toString().trim();
        String lightStr = mEditLight.getText().toString().trim();
        String sonsorStr = mEditSensor.getText().toString().trim();
        switch (view.getId()) {
            case R.id.btn_open_locker:
                if (TextUtils.isEmpty(circuitBoardStr) || TextUtils.isEmpty(lockerStr) || TextUtils.isEmpty(lightStr) || TextUtils.isEmpty(sonsorStr)) {
                    ToastUtils.toast(this, "输入不能为空");
                    return;
                }
                circuitBoard = Integer.parseInt(circuitBoardStr);
                locker = Integer.parseInt(lockerStr);
                light = Integer.parseInt(lightStr);
                sonsor = Integer.parseInt(sonsorStr);
                circuitBoardSend = getSendData(circuitBoard);
                lockerSend = LockerUtils.getSendDataConversion(locker);
                lightSend = getSendData(light);
                sonsorSend = getSendData(sonsor);
                LockersCommHelperNew.get().controlSingleLock(circuitBoardSend, lockerSend, lightSend, sonsorSend);
                break;
            case R.id.btn_query_circuit_board:
                LockersCommHelperNew.get().queryCircuiBboard();
                break;
            case R.id.btn_query_all:
                if (TextUtils.isEmpty(circuitBoardStr) || TextUtils.isEmpty(lockerStr) || TextUtils.isEmpty(lightStr) || TextUtils.isEmpty(sonsorStr)) {
                    ToastUtils.toast(this, "输入不能为空");
                    return;
                }
                circuitBoard = Integer.parseInt(circuitBoardStr);
                locker = Integer.parseInt(lockerStr);
                light = Integer.parseInt(lightStr);
                sonsor = Integer.parseInt(sonsorStr);
                circuitBoardSend = getSendData(circuitBoard);
                lockerSend = LockerUtils.getSendDataConversion(locker);
                lightSend = getSendData(light);
                sonsorSend = getSendData(sonsor);
                Log.i(TAG, "onViewClicked: btn_query_circuit_board: circuitBoardSend: " + Integer.toHexString(circuitBoardSend)
                        + " lockerSend: " + Integer.toHexString(lockerSend)
                        + " lightSend: " + Integer.toHexString(lightSend)
                        + " sonsorSend: " + Integer.toHexString(sonsorSend));
                LockersCommHelperNew.get().queryAll(circuitBoardSend, lockerSend, lightSend, sonsorSend);
                break;
            case R.id.btn_auto_light:
                if (TextUtils.isEmpty(circuitBoardStr) || TextUtils.isEmpty(lockerStr) || TextUtils.isEmpty(lightStr) || TextUtils.isEmpty(sonsorStr)) {
                    ToastUtils.toast(this, "输入不能为空");
                    return;
                }
                circuitBoard = Integer.parseInt(circuitBoardStr);
                locker = Integer.parseInt(lockerStr);
                light = Integer.parseInt(lightStr);
                sonsor = Integer.parseInt(sonsorStr);
                circuitBoardSend = getSendData(circuitBoard);
                lockerSend = LockerUtils.getSendDataConversion(locker);
                lightSend = getSendData(light);
                sonsorSend = getSendData(sonsor);
                Log.i(TAG, "onViewClicked: btn_auto_light: circuitBoardSend: " + Integer.toHexString(circuitBoardSend)
                        + " lockerSend: " + Integer.toHexString(lockerSend)
                        + " lightSend: " + Integer.toHexString(lightSend)
                        + " sonsorSend: " + Integer.toHexString(sonsorSend));
                LockersCommHelperNew.get().autoLight(circuitBoardSend, lockerSend, lightSend, sonsorSend);
                break;
        }
    }

    /**
     * 将十进制的数据,转化为发送的数据
     *
     * @param locker
     */
    private static byte getSendData(int locker) {
        byte binary = (byte) (1 << (locker - 1));
        return binary;
    }
}
