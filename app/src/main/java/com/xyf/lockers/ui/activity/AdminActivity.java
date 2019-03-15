package com.xyf.lockers.ui.activity;

import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.xyf.lockers.R;
import com.xyf.lockers.adapter.GridAdapter;
import com.xyf.lockers.base.BaseActivity;
import com.xyf.lockers.model.bean.User;
import com.xyf.lockers.utils.ToastUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 显示所有柜门状态，以及每个柜门是否有存东西，显示用户头像，显示最后保存时间，是否超过三天储存时间
 * 显示所有门的状态，灯的状态
 * 每个item上有打开的按钮，以及下方具有打开全部柜门的按钮，以及一键打开保存超过三天的按钮，打开皆需要二次确认
 * 以及进入数据库界面，查询历史保存界面
 */
public class AdminActivity extends BaseActivity implements BaseQuickAdapter.OnItemChildClickListener {
    private static final String TAG = "AdminActivity";
    @BindView(R.id.recyclerview_grid)
    RecyclerView recyclerviewGrid;

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
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 8);
        gridLayoutManager.setOrientation(GridLayoutManager.VERTICAL);
        recyclerviewGrid.setLayoutManager(gridLayoutManager);
        GridAdapter gridAdapter = new GridAdapter(R.layout.grid_item, list);
        recyclerviewGrid.setAdapter(gridAdapter);
        gridAdapter.setOnItemChildClickListener(this);
    }

    @Override
    public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
        ToastUtils.toast(this, position + "被点击");
    }
}
