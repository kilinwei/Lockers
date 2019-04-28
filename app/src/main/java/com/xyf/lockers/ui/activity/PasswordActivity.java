package com.xyf.lockers.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;

import com.xyf.lockers.R;
import com.xyf.lockers.base.BaseActivity;
import com.xyf.lockers.view.PasswordView;

import butterknife.BindView;

public class PasswordActivity extends BaseActivity {
    private static final String TAG = "PasswordActivity";
    private static final String PWD = "123456";
    public static final String TARGET = "target";
    public static final int ADMIN_ACTIVITY = 0;
    public static final int STAORAGE_RECORD_ACTIVITY = 1;
    @BindView(R.id.pwd_view)
    PasswordView pwdView;
    @BindView(R.id.id_timer_close)
    TextView mTimerText;
    private int time = 60;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            mTimerText.setText("(" + time-- + "秒后自动关闭)");
            if (time == 0) {
                finish();
            } else {
                handler.sendEmptyMessageDelayed(0, 1000);
            }

        }
    };

    @Override
    protected int getLayout() {
        return R.layout.activity_password;
    }

    @Override
    protected void initEventAndData(Bundle savedInstanceState) {
        Intent intent = getIntent();
        final int target = intent.getIntExtra(TARGET, 0);
        handler.sendEmptyMessageDelayed(0, 500);
        pwdView.setOnFinishInput(new PasswordView.OnPasswordInputFinish() {
            @Override
            public void inputFinish(String password) {
                if (PWD.equals(password)) {
                    if (target == ADMIN_ACTIVITY) {
                        Intent intent = new Intent(PasswordActivity.this, AdminActivity.class);
                        startActivity(intent);
                    } else {
                        Intent intent = new Intent(PasswordActivity.this, StorageRecordActivity.class);
                        startActivity(intent);
                    }
                    finish();
                } else {
                    pwdView.passwordError();
                }

            }
        });
        pwdView.setOnPasswordClose(new PasswordView.OnPasswordClose() {
            @Override
            public void onPasswordClose() {
                finish();
                Log.i(TAG, "onPasswordClose: ");
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }

    private void closeActivity(int resultCode) {
        handler.removeCallbacksAndMessages(null);
        handler = null;
        setResult(resultCode);
        finish();
    }
}
