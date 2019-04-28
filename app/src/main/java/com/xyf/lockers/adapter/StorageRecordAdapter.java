package com.xyf.lockers.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.xyf.lockers.BuildConfig;
import com.xyf.lockers.R;
import com.xyf.lockers.app.Constants;
import com.xyf.lockers.model.bean.StorageBean;
import com.xyf.lockers.utils.FileUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class StorageRecordAdapter extends BaseQuickAdapter<StorageBean, BaseViewHolder> {
    private Context mContext;

    public StorageRecordAdapter(Context context, int layoutResId, @Nullable List<StorageBean> data) {
        super(layoutResId, data);
        this.mContext = context;
    }

    @Override
    protected void convert(BaseViewHolder helper, StorageBean item) {
        int layoutPosition = helper.getLayoutPosition();
        String openType = "";
        int textColor = 0;
        switch (item.getType()) {
            case Constants.STORAGE:
                openType = "存";
                textColor = Color.GREEN;
                break;
            case Constants.TEMPORARY_TAKE:
                openType = "临时开柜";
                textColor = Color.MAGENTA;
                break;
            case Constants.TAKE:
                openType = "取";
                textColor = Color.RED;
                break;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        java.util.Date date = new Date(item.getTime());
        String time = sdf.format(date);
        String imgPath = FileUtils.getFaceCropPicDirectory().getAbsolutePath()
                + "/" + item.getCropImageName();
        Log.i(TAG, "convert: imgPath: " + imgPath);
        ImageView imageView = helper.getView(R.id.iv_crop_mage);
        if (BuildConfig.DEBUG) {
            imageView.setVisibility(View.VISIBLE);
            Glide.with(mContext).load(imgPath).into(imageView);
        } else {
            imageView.setVisibility(View.GONE);
        }
        TextView tvType = helper.getView(R.id.tv_open_type);
        tvType.setText(openType);
        tvType.setTextColor(textColor);
        helper.setText(R.id.tv_user_name, item.getUserName())
                .setText(R.id.tv_locker_num, item.getLockerNum() + "号柜")
                .setText(R.id.tv_time, time)
                .setText(R.id.tv_index, String.valueOf(layoutPosition + 1));
    }
}
