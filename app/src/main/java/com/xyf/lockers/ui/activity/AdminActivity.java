package com.xyf.lockers.ui.activity;

import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.xyf.lockers.R;
import com.xyf.lockers.adapter.GridAdapter;
import com.xyf.lockers.base.BaseActivity;
import com.xyf.lockers.common.serialport.LockersCommHelper;
import com.xyf.lockers.listener.OnSingleLockerStatusListener;
import com.xyf.lockers.model.bean.GridBean;
import com.xyf.lockers.model.bean.User;
import com.xyf.lockers.utils.LockerUtils;
import com.xyf.lockers.utils.SharedPreferenceUtil;
import com.xyf.lockers.utils.ToastUtils;
import com.xyf.lockers.utils.UserDBManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
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
public class AdminActivity extends BaseActivity implements BaseQuickAdapter.OnItemChildClickListener {
    private static final String TAG = "AdminActivity";
    public static final long STORAGE_TIME_OUT = 3 * 24 * 60 * 60 * 1000;
    @BindView(R.id.recyclerview_grid)
    RecyclerView recyclerviewGrid;
    /**
     * 用于
     */
    List<GridBean> mGridBeans;
    private Map<Integer, User> mCacheMap;
    private int mCurrentOpenLockerIndex;


    @Override
    protected int getLayout() {
        return R.layout.activity_admin;
    }

    @Override
    protected void initEventAndData(Bundle savedInstanceState) {
        List<User> list = new ArrayList<>();
        for (int i = 0; i < 32; i++) {
            User user = new User();
            user.setUserName("" + 1);
            list.add(user);
        }
        Disposable subscribe = Observable.just(1)
                .subscribeOn(Schedulers.io())
                .map(new Function<Integer, List<GridBean>>() {
                    @Override
                    public List<GridBean> apply(Integer integer) throws Exception {
                        List<GridBean> gridBeanList = new ArrayList<>();
                        List<User> allStorageUser = UserDBManager.getAllStorageUser();
                        if (allStorageUser != null && !allStorageUser.isEmpty()) {
//                        if (allStorageUser != null && allStorageUser.isEmpty()) {//测试用
                            mCacheMap = new HashMap<>();
                            for (User user : allStorageUser) {
                                int storageIndex = user.getStorageIndexs();
                                List<Integer> storageIndexs = LockerUtils.getStorageIndexs(storageIndex);
                                if (storageIndexs != null && !storageIndexs.isEmpty()) {
                                    for (Integer index : storageIndexs) {
                                        User put = mCacheMap.put(index, user);
                                        if (put != null) {
                                            Log.w(TAG, "警告：序号为：" + index + "的柜子保存了两个人的信息");
                                            ToastUtils.toast(AdminActivity.this, "警告：序号为：" + index + "的柜子保存了两个人的信息");
                                        }
                                    }
                                }
                            }

                            for (int i = 1; i <= LockersCommHelper.LOCKER_COUNT; i++) {
                                GridBean gridBean = new GridBean();
                                User user = mCacheMap.get(i);
                                if (user != null) {
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
                        recyclerviewGrid.setAdapter(gridAdapter);
                        gridAdapter.setOnItemChildClickListener(AdminActivity.this);

                    }
                });
    }

    @Override
    public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
        if (mGridBeans != null) {
            GridBean gridBean = mGridBeans.get(position);

        }
        ToastUtils.toast(this, position + "被点击");
    }

}
