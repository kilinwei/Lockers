package com.xyf.lockers.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.xyf.lockers.R;
import com.xyf.lockers.app.MainAppliction;
import com.xyf.lockers.base.BaseActivity;
import com.xyf.lockers.model.bean.User;
import com.xyf.lockers.model.bean.UserDao;

import java.util.ArrayList;
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
                UserDao userDao = MainAppliction.getInstance().getDaoSession().getUserDao();
                User user = new User();
                user.setUserName(String.valueOf(System.currentTimeMillis() / 1000));
                user.setLastTime(System.currentTimeMillis() / 1000);
                List list = new ArrayList();
                for (int i = 0; i < 3; i++) {
                    list.add(i);
                }
                String s = JSON.toJSONString(list);
                user.setStorageIndexs(s);
                userDao.insert(user);
                break;
            case R.id.btn_control_test:
//                intent = new Intent(this, TestActivity.class);
                UserDao userDao2 = MainAppliction.getInstance().getDaoSession().getUserDao();
                List<User> users = userDao2.loadAll();
                for (User user1 : users) {
                    String storageIndexs = user1.getStorageIndexs();
                    Log.i(TAG, "onViewClicked: storageIndexs:" + storageIndexs);
                    JSONArray objects = JSON.parseArray(storageIndexs);
//                    Log.i(TAG, "onViewClicked: " + objects.size());
//                    for (int i = 0; i < objects.size(); i++) {
//                        Object o = objects.get(i);
//                        Log.i(TAG, "onViewClicked: o: " + o);
//                    }
                }
                break;
        }
        if (intent != null) {
            startActivity(intent);
        }
    }
}
