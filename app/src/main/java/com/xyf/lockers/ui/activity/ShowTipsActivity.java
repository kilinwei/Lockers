package com.xyf.lockers.ui.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
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
    private int time = 2;
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            mTvTimer.setText("(" + time-- + "秒后自动关闭)");
            if (time < 0) {
                Intent intent = new Intent(ShowTipsActivity.this, MainActivity.class);
                if (mOpeningLockers != null) {
                    intent.putExtra(MainActivity.CHECK_CLOSE, mOpeningLockers);
                }
                startActivity(intent);
            } else {
                handler.sendEmptyMessageDelayed(0, 1000);
            }

        }
    };
    private String mTips;
    private byte[] mOpeningLockers;

    @Override
    protected int getLayout() {
        return R.layout.activity_show_tips;
    }

    @Override
    protected void initEventAndData(Bundle savedInstanceState) {
        Intent intent = getIntent();
        mTips = intent.getStringExtra(TIPS);
        if ("您没有保存物品，请先保存物品".equals(mTips)) {
            mTvTips.setTextColor(Color.RED);
        }
        mOpeningLockers = intent.getByteArrayExtra(MainActivity.CHECK_CLOSE);
        mTvTips.setText(mTips);
        handler.sendEmptyMessageDelayed(0, 500);
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
