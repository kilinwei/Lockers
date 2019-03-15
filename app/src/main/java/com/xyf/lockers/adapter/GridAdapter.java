package com.xyf.lockers.adapter;

import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.xyf.lockers.R;
import com.xyf.lockers.model.bean.User;

import java.util.List;

public class GridAdapter extends BaseQuickAdapter<User, BaseViewHolder> {

    public GridAdapter(int layoutResId, @Nullable List<User> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, User item) {
        ImageView imageView = helper.getView(R.id.iv_user);
        Glide.with(imageView.getContext()).load(R.mipmap.ic_launcher).into(imageView);
        helper.addOnClickListener(R.id.btn_open_lock)
                .setText(R.id.tv_storage, "最后储存时间")
                .setText(R.id.tv_timeout, "已超过三天");
//                .setText(R.id.tv_lock_status, "门已关闭")
//                .setText(R.id.tv_light_status, "灯的状态闪烁");
    }
}
