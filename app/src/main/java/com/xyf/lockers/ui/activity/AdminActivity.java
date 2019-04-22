package com.xyf.lockers.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.baidu.idl.facesdk.model.Feature;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.xyf.lockers.R;
import com.xyf.lockers.adapter.GridAdapter;
import com.xyf.lockers.base.BaseActivity;
import com.xyf.lockers.common.serialport.LockersCommHelper;
import com.xyf.lockers.common.serialport.LockersCommHelperNew;
import com.xyf.lockers.listener.OnSingleLockerStatusListener;
import com.xyf.lockers.manager.UserInfoManager;
import com.xyf.lockers.model.bean.GridBean;
import com.xyf.lockers.model.bean.User;
import com.xyf.lockers.utils.LockerUtils;
import com.xyf.lockers.utils.SharedPreferenceUtil;
import com.xyf.lockers.utils.ToastUtil;
import com.xyf.lockers.utils.UserDBManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * 显示所有柜门状态，以及每个柜门是否有存东西，显示用户头像，显示最后保存时间，是否超过三天储存时间
 * 显示所有门的状态，灯的状态
 * 每个item上有打开的按钮，以及下方具有打开全部柜门的按钮，以及一键打开保存超过三天的按钮，打开皆需要二次确认
 * 以及进入数据库界面，查询历史保存界面
 */
public class AdminActivity extends BaseActivity implements BaseQuickAdapter.OnItemChildClickListener, OnSingleLockerStatusListener {
    private static final String TAG = "AdminActivity";
    public static final long STORAGE_TIME_OUT = 3 * 24 * 60 * 60 * 1000;
    @BindView(R.id.recyclerview_grid)
    RecyclerView recyclerviewGrid;
    List<GridBean> mGridBeans;
    @BindView(R.id.btn_open_all)
    Button mBtnOpenAll;
    @BindView(R.id.btn_control_delete_all)
    Button mBtnControlDeleteAll;
    @BindView(R.id.btn_back)
    Button mBtnBack;
    @BindView(R.id.btn_config_angle)
    Button mBtnConfigAngle;

    EditText mEditUpDown;
    EditText mEditRightLeftAngle;
    EditText mEditRotateAngle;
    private Map<Integer, User> mCacheMap;
    private int mCurrentOpenLockerIndex;
    private Disposable mSubscribe;
    private List<Feature> mListFeatureInfo;
    private UserInfoManager.UserInfoListener mUserInfoListener;
    private MaterialDialog mShowAngleConfigDialog;

    @Override
    protected int getLayout() {
        return R.layout.activity_admin;
    }

    @Override
    protected void initEventAndData(Bundle savedInstanceState) {
//        final List<User> list = new ArrayList<>();
//        for (int i = 0; i < 32; i++) {
//            User user = new User();
//            user.setUserName("" + 1);
//            list.add(user);
//        }
        LockersCommHelperNew.get().setOnSingleLockerStatusListener(this);
        mUserInfoListener = new UserListener();
        UserInfoManager.getInstance().getFeatureInfo(null, mUserInfoListener);
        Disposable subscribe = Observable.just(1)
                .subscribeOn(Schedulers.io())
                .map(new Function<Integer, List<GridBean>>() {
                    @Override
                    public List<GridBean> apply(Integer integer) throws Exception {
                        List<GridBean> gridBeanList = new ArrayList<>();
                        List<User> allStorageUser = UserDBManager.getAllStorageUser();
//                        if (allStorageUser != null && !allStorageUser.isEmpty()) {
                        if (allStorageUser != null && allStorageUser.isEmpty()) {//测试用,上面的才是生产用的
                            mCacheMap = new HashMap<>();
                            for (User user : allStorageUser) {
                                int storageIndex = user.getStorageIndexs();
                                List<Integer> storageIndexs = LockerUtils.getStorageIndexs(storageIndex);
                                if (storageIndexs != null && !storageIndexs.isEmpty()) {
                                    for (Integer index : storageIndexs) {
                                        User put = mCacheMap.put(index, user);
                                        if (put != null) {
                                            Log.w(TAG, "警告：序号为：" + index + "的柜子保存了两个人的信息");
                                            ToastUtil.showMessage("警告：序号为：" + index + "的柜子保存了两个人的信息");
                                        }
                                    }
                                }
                            }

                            for (int i = 1; i <= LockersCommHelper.LOCKER_COUNT; i++) {
                                GridBean gridBean = new GridBean();
                                User user = mCacheMap.get(i);
                                if (user != null) {
                                    gridBean.userID = user.getId();
                                    gridBean.imagePath = user.getCropImageName();
                                    gridBean.lastStorageTime = user.getLastTime();
                                    gridBean.isStorageTimeout = System.currentTimeMillis() / 1000 - user.getLastTime() > STORAGE_TIME_OUT;
                                    gridBean.lightStatus = 0;
                                    gridBean.lockerStatus = 0;
                                }
                                gridBeanList.add(gridBean);
                            }
                        }
                        return gridBeanList;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<GridBean>>() {
                    @Override
                    public void accept(List<GridBean> gridBeanList) throws Exception {
                        if (gridBeanList.isEmpty()) {
                            return;
                        }
                        mGridBeans = gridBeanList;
                        GridLayoutManager gridLayoutManager = new GridLayoutManager(AdminActivity.this, 8);
                        gridLayoutManager.setOrientation(GridLayoutManager.VERTICAL);
                        recyclerviewGrid.setLayoutManager(gridLayoutManager);
                        GridAdapter gridAdapter = new GridAdapter(R.layout.grid_item, mGridBeans);
                        // TODO: 2019/4/9 just for  test
//                        GridAdapter gridAdapter = new GridAdapter(R.layout.grid_item, list);
                        recyclerviewGrid.setAdapter(gridAdapter);
                        gridAdapter.setOnItemChildClickListener(AdminActivity.this);

                    }
                });
    }

    @Override
    public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
        if (mGridBeans != null) {
            GridBean gridBean = mGridBeans.get(position);
            // TODO: 2019/4/20 打开之后，把用户储存的信息去掉
            byte[] openSingleLockerBytes = LockerUtils.getOpenSingleLockerBytes(position);
            LockersCommHelperNew.get().controlSingleLock(openSingleLockerBytes[0], openSingleLockerBytes[1], openSingleLockerBytes[2], openSingleLockerBytes[3]);
            ToastUtil.showMessage("第" + (1 + position) + "柜子被打开");
            if (gridBean != null && gridBean.userID != -1) {
                User user = UserDBManager.getUser(gridBean.userID);
                if (user != null) {
                    int storageIndexs = user.getStorageIndexs();
                    //获取当前打开的箱位
                    int wayBinary = 1 << position;
                    //二进制取反,比如00001000变成111110111
                    int i = ~wayBinary;
                    //将指定位数的1抹去
                    storageIndexs &= i;
                    user.setStorageIndexs(storageIndexs);
                    //更新数据库信息
                    UserDBManager.update(user);
                    int allLockersStatus = SharedPreferenceUtil.getAllLockersStatus();
                    //用原来以保存的箱位或上现保存的箱位,然后记录所有已存东西的箱位索引
                    allLockersStatus &= i;
                    SharedPreferenceUtil.setAllLockersStatus(allLockersStatus);
                }
            }
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mSubscribe != null && !mSubscribe.isDisposed()) {
            mSubscribe.dispose();
        }
        if (mShowAngleConfigDialog != null && mShowAngleConfigDialog.isShowing()) {
            mShowAngleConfigDialog.dismiss();
        }
        LockersCommHelperNew.get().setOnSingleLockerStatusListener(null);
    }


    @OnClick({R.id.btn_open_all,
            R.id.btn_back,
            R.id.btn_config_angle,
            R.id.btn_control_delete_all})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_open_all:
                mSubscribe = Observable.just(1).subscribeOn(Schedulers.io())
                        .subscribe(new Consumer<Integer>() {
                            @Override
                            public void accept(Integer integer) throws Exception {
                                for (int i = 0; i < 32; i++) {
                                    byte[] openSingleLockerBytes = LockerUtils.getOpenSingleLockerBytes(i);
                                    LockersCommHelperNew.get().controlSingleLock(openSingleLockerBytes[0], openSingleLockerBytes[1], openSingleLockerBytes[2], openSingleLockerBytes[3]);
                                    SystemClock.sleep(LockerUtils.OPEN_LOCKER_INTEVAL);
                                }
                                deleteAll();
                            }
                        });
                break;
            case R.id.btn_back:
                startActivity(new Intent(this, MainActivity.class));
                break;
            case R.id.btn_control_delete_all:
                deleteAll();
                break;
            case R.id.btn_config_angle:
                showAngleConfigDialog();
                break;
        }
    }

    private void deleteAll() {
        if (mListFeatureInfo == null) {
            return;
        }
        Log.i(TAG, "onViewClicked: 删除之前的百度人脸库数量: " + mListFeatureInfo.size());
        for (Feature feature : mListFeatureInfo) {
            feature.setChecked(true);
        }
        UserInfoManager.getInstance().batchRemoveFeatureInfo(mListFeatureInfo, mUserInfoListener, mListFeatureInfo.size());
        SharedPreferenceUtil.setAllLockersStatus(0);
        List<User> allStorageUser = UserDBManager.getAllStorageUser();
        for (User user : allStorageUser) {
            user.setStorageIndexs(0);
        }
    }

    @Override
    public void onSingleLockerStatusResponse(int way, int status) {

    }

    @Override
    public void onSingleLockerStatusResponse(byte[] bRec) {

    }

    @Override
    public void disConnectDevice() {

    }


    // 用于返回读取数据库的结果
    private class UserListener extends UserInfoManager.UserInfoListener {

        // 人脸库信息查找成功
        @Override
        public void featureQuerySuccess(final List<Feature> listFeatureInfo) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (listFeatureInfo == null || listFeatureInfo.size() == 0) {
                        mListFeatureInfo = null;
                        Log.i(TAG, "run: 查询到百度人脸库数量为空");
                    } else {
                        mListFeatureInfo = listFeatureInfo;
                        for (Feature feature : mListFeatureInfo) {
                            Log.i(TAG, "run: feature：　" + feature.getUserName());
                        }
                        Log.i(TAG, "run: 查询到百度人脸库数量为: " + listFeatureInfo.size());
                    }
                }
            });
        }

        // 人脸库信息查找失败
        @Override
        public void featureQueryFailure(final String message) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                }
            });
        }

        // 显示删除进度条
        @Override
        public void showDeleteProgressDialog(final float progress) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                }
            });
        }

        // 删除成功
        @Override
        public void deleteSuccess() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.i(TAG, "run: 百度人脸库删除成功");
                    ToastUtil.showMessage(" 百度人脸库删除成功");
                    // 读取数据库信息
                    UserInfoManager.getInstance().getFeatureInfo(null, mUserInfoListener);
                }
            });

        }
    }

    private void showAngleConfigDialog() {
        mShowAngleConfigDialog = new MaterialDialog.Builder(this)
                .title("人脸角度限制")
                .customView(R.layout.angle_config, true)
                .positiveText(android.R.string.ok)
                .negativeText(android.R.string.no)
                .autoDismiss(false)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull final MaterialDialog dialog, @NonNull DialogAction which) {
                        final String upDownAngle = mEditUpDown.getText().toString().trim();
                        final String leftRightAngle = mEditRightLeftAngle.getText().toString().trim();
                        final String rotateAngle = mEditRotateAngle.getText().toString().trim();
                        if (TextUtils.isEmpty(upDownAngle) || TextUtils.isEmpty(leftRightAngle) || TextUtils.isEmpty(rotateAngle)) {
                            ToastUtil.showMessage("输入框全部都不能为空!");
                            return;
                        }

                        SharedPreferenceUtil.setUpDownAngle(Integer.parseInt(upDownAngle));
                        SharedPreferenceUtil.setLeftRightAngle(Integer.parseInt(leftRightAngle));
                        SharedPreferenceUtil.setRotateAngle(Integer.parseInt(rotateAngle));
                        dialog.dismiss();
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                })
                .build();
        mEditUpDown = (EditText) mShowAngleConfigDialog.findViewById(R.id.edit_up_down);
        mEditRightLeftAngle = (EditText) mShowAngleConfigDialog.findViewById(R.id.edit_right_left_angle);
        mEditRotateAngle = (EditText) mShowAngleConfigDialog.findViewById(R.id.edit_rotate_angle);
        mEditUpDown.setText(String.valueOf(SharedPreferenceUtil.getUpDownAngle()));
        mEditRightLeftAngle.setText(String.valueOf(SharedPreferenceUtil.getLeftRightAngle()));
        mEditRotateAngle.setText(String.valueOf(SharedPreferenceUtil.getRotateAngle()));
        mShowAngleConfigDialog.show();
    }
}
