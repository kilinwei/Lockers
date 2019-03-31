package com.xyf.lockers.ui.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;

import com.xyf.lockers.R;
import com.xyf.lockers.base.BaseActivity;

import butterknife.BindView;

public class ShowTipsActivity extends BaseActivity {
    private static final String TAG = "ShowTipsActivity";
    public static final String TIPS = "tips";
    @BindView(R.id.tv_timer)
    TextView mTvTimer;
    @BindView(R.id.tv_tips)
    TextView mTvTips;
    private int time = 3;
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            mTvTimer.setText("(" + time-- + "秒后自动关闭)");
            if (time < 0) {
                startActivity(new Intent(ShowTipsActivity.this, MainActivity.class));
            } else {
                handler.sendEmptyMessageDelayed(0, 1000);
            }

        }
    };
    private String mTips;

    @Override
    protected int getLayout() {
        return R.layout.activity_show_tips;
    }

    @Override
    protected void initEventAndData(Bundle savedInstanceState) {
        Intent intent = getIntent();
        mTips = intent.getStringExtra(TIPS);
        mTvTips.setText(mTips);
        handler.sendEmptyMessageDelayed(0,500);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent != null) {
            mTips = intent.getStringExtra(TIPS);
            mTvTimer.setText(mTips);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}
