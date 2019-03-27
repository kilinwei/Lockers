package com.xyf.lockers.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.xyf.lockers.R;
import com.xyf.lockers.base.BaseActivity;

import butterknife.BindView;
import butterknife.OnClick;


public class MainActivity
        extends BaseActivity {
    private static final String TAG = "MainActivity";
    @BindView(R.id.btn_storage)
    Button btnStorage;
    @BindView(R.id.btn_take)
    Button btnTake;
    @BindView(R.id.btn_control_test)
    Button mBtnControlTest;


    @Override
    protected int getLayout() {
        return R.layout.activity_main;
    }

    @Override
    protected void initEventAndData(Bundle savedInstanceState) {

    }


    @OnClick({R.id.btn_storage,
            R.id.btn_take,
            R.id.btn_control_test})
    public void onViewClicked(View view) {
        Intent intent = null;
        switch (view.getId()) {
            case R.id.btn_storage:
                intent = new Intent(this, PassActivity.class);
                break;
            case R.id.btn_take:
                intent = new Intent(this, StorageActivity.class);
                break;
            case R.id.btn_control_test:
                intent = new Intent(this, ControlTestActivityNew.class);
                break;
        }
        if (intent != null) {
            startActivity(intent);
        }
    }
}
