package com.xyf.lockers.adapter;

import android.support.annotation.Nullable;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.xyf.lockers.R;
import com.xyf.lockers.app.Constants;
import com.xyf.lockers.model.bean.StorageBean;
import com.xyf.lockers.utils.FileUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class StorageRecordAdapter extends BaseQuickAdapter<StorageBean, BaseViewHolder> {

    public StorageRecordAdapter(int layoutResId, @Nullable List<StorageBean> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, StorageBean item) {
        int layoutPosition = helper.getLayoutPosition();
        String openType = "";
        switch (item.getType()) {
            case Constants.STORAGE:
                openType = "存";
                break;
            case Constants.TEMPORARY_TAKE:
                openType = "临时开柜";
                break;
            case Constants.TAKE:
                openType = "取";
                break;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        java.util.Date date = new Date(item.getTime());
        String time = sdf.format(date);
        String imgPath = FileUtils.getFaceCropPicDirectory().getAbsolutePath()
                + "/" + item.getCropImageName()+".png";
        ImageView imageView = helper.getView(R.id.iv_crop_mage);
        Glide.with(imageView).load(imgPath).into(imageView);
        helper.setText(R.id.tv_user_name, item.getUserName())
                .setText(R.id.tv_locker_num, item.getLockerNum() + "号柜")
                .setText(R.id.tv_time, time)
                .setText(R.id.tv_index, String.valueOf(layoutPosition + 1))
                .setText(R.id.tv_open_type, openType);
    }
}
