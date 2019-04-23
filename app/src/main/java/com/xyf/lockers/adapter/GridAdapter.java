package com.xyf.lockers.adapter;

import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.xyf.lockers.R;
import com.xyf.lockers.model.bean.GridBean;

import java.util.List;

public class GridAdapter extends BaseQuickAdapter<GridBean, BaseViewHolder> {

    public GridAdapter(int layoutResId, @Nullable List<GridBean> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, GridBean item) {
        int layoutPosition = helper.getLayoutPosition();
        if (layoutPosition == 24) {
            // TODO: 2019/4/23 各种状态
            helper.addOnClickListener(R.id.btn_open_lock)
                    .setText(R.id.tv_num, "主控")
                    .setText(R.id.tv_storage, "最后储存时间")
                    .setText(R.id.tv_timeout, item.isStorageTimeout ? "已超过三天" : "未超过三天");
            Button view = helper.getView(R.id.btn_open_lock);
            if (view != null) {
                view.setVisibility(View.INVISIBLE);
            }
//                .setText(R.id.tv_lock_status, "门已关闭")
//                .setText(R.id.tv_light_status, "灯的状态闪烁");
        } else {
            // TODO: 2019/4/23 各种状态
            helper.addOnClickListener(R.id.btn_open_lock)
                    .setText(R.id.tv_num, String.valueOf(layoutPosition + 1) + "号柜")
                    .setText(R.id.tv_storage, "最后储存时间")
                    .setText(R.id.tv_timeout, item.isStorageTimeout ? "已超过三天" : "未超过三天");
//                .setText(R.id.tv_lock_status, "门已关闭")
//                .setText(R.id.tv_light_status, "灯的状态闪烁");
        }

    }
}
