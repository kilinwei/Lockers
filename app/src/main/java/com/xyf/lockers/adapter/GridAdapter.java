package com.xyf.lockers.adapter;

import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.xyf.lockers.R;
import com.xyf.lockers.model.bean.GridBean;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class GridAdapter extends BaseQuickAdapter<GridBean, BaseViewHolder> {

    public GridAdapter(int layoutResId, @Nullable List<GridBean> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, GridBean item) {
        int layoutPosition = helper.getLayoutPosition();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        java.util.Date date = new Date(item.lastStorageTime);
        String lasttime = sdf.format(date);
        TextView tvLastTime = helper.getView(R.id.tv_storage);
        TextView tvTimeout = helper.getView(R.id.tv_timeout);
        if (layoutPosition == 24) {
            helper.addOnClickListener(R.id.btn_open_lock)
                    .setText(R.id.tv_num, "主控");
            Button view = helper.getView(R.id.btn_open_lock);
            if (view != null) {
                view.setVisibility(View.GONE);
                tvLastTime.setVisibility(View.GONE);
                tvTimeout.setVisibility(View.GONE);
            }
        } else {
            // TODO: 2019/4/23 各种状态
            helper.addOnClickListener(R.id.btn_open_lock)
                    .setText(R.id.tv_num, String.valueOf(layoutPosition + 1) + "号柜")
                    .setText(R.id.tv_storage, "最后储存时间:\n" + lasttime)
                    .setText(R.id.tv_timeout, item.isStorageTimeout ? "已超过三天" : "未超过三天");

            if (item.lastStorageTime == 0) {
                tvLastTime.setText("未使用");
                tvTimeout.setVisibility(View.GONE);
            }
//                .setText(R.id.tv_lock_status, "门已关闭")
//                .setText(R.id.tv_light_status, "灯的状态闪烁");
        }

    }
}
