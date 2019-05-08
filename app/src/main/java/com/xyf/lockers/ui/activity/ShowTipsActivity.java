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
    public static final String TEXT_COLOR = "text_color";
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
                Intent intent = new Intent(ShowTipsActivity.this, MainActivity.class);
                if (mOpeningLockers != null && mOpeningLockers.length == 4) {
                    intent.putExtra(MainActivity.CHECK_CLOSE, mOpeningLockers);
                }
                startActivity(intent);
            } else {
                handler.sendEmptyMessageDelayed(0, 1000);
            }

        }
    };
    private String mTips;
    private int mColor;
    private byte[] mOpeningLockers;

    @Override
    protected int getLayout() {
        return R.layout.activity_show_tips;
    }

    @Override
    protected void initEventAndData(Bundle savedInstanceState) {
        Intent intent = getIntent();
        mTips = intent.getStringExtra(TIPS);
        mColor = intent.getIntExtra(TEXT_COLOR, 0);
        mOpeningLockers = intent.getByteArrayExtra(MainActivity.CHECK_CLOSE);
        if (mColor == 0) {
            mTvTips.setTextColor(Color.BLACK);
        } else {
            mTvTips.setTextColor(mColor);
        }
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
