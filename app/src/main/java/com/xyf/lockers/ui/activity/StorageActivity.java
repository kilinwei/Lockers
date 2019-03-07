package com.xyf.lockers.ui.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.baidu.idl.facesdk.model.Feature;
import com.xyf.lockers.R;
import com.xyf.lockers.app.MainAppliction;
import com.xyf.lockers.base.BaseActivity;
import com.xyf.lockers.callback.IFaceRegistCalllBack;
import com.xyf.lockers.callback.ILivenessCallBack;
import com.xyf.lockers.common.GlobalSet;
import com.xyf.lockers.manager.FaceLiveness;
import com.xyf.lockers.manager.FaceSDKManager;
import com.xyf.lockers.model.LivenessModel;
import com.xyf.lockers.model.bean.User;
import com.xyf.lockers.model.bean.UserDao;
import com.xyf.lockers.utils.DensityUtil;
import com.xyf.lockers.view.BinocularView;
import com.xyf.lockers.view.MonocularView;

import java.util.List;

import butterknife.BindView;

public class StorageActivity extends BaseActivity implements ILivenessCallBack {
    private static final String TAG = "StorageActivity";
    private static final int CHECK_FACE = 0x01;
    private static final int TIME = 3 * 1000;

    @BindView(R.id.layout_camera)
    RelativeLayout mCameraView;
    @BindView(R.id.image_track)
    ImageView mImageTrack;
    private Context mContext;
    private BinocularView mBinocularView;
    private MonocularView mMonocularView;
    private boolean mNeedRegister;
    private String mNickName;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CHECK_FACE:
                    mNeedRegister = true;
                    removeMessages(CHECK_FACE);
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected int getLayout() {
        return R.layout.activity_storage;
    }

    @Override
    protected void initEventAndData(Bundle savedInstanceState) {
        mContext = this;
        // 注册人脸注册事件
        FaceSDKManager.getInstance().getFaceLiveness().addRegistCallBack(faceRegistCalllBack);
        //进入界面首先设置为通行,确保同一用户不会被注册两次
        FaceSDKManager.getInstance().getFaceLiveness().setCurrentTaskType(FaceLiveness.TaskType.TASK_TYPE_ONETON);

    }


    @Override
    protected void onResume() {
        super.onResume();
        if (GlobalSet.getLiveStatusValue() == GlobalSet.LIVE_STATUS.RGN_NIR) {
            mBinocularView.onResume();
        } else {
            mMonocularView.onResume();
        }
        mHandler.sendEmptyMessageDelayed(CHECK_FACE, TIME);
    }

    /**
     * 计算并适配显示图像容器的宽高
     */
    private void calculateCameraView() {
        String newPix;
        if (GlobalSet.getLiveStatusValue() == GlobalSet.LIVE_STATUS.RGN_NIR) {
            newPix = DensityUtil.calculateCameraOrbView(mContext);
        } else {
            newPix = DensityUtil.calculateCameraView(mContext);
        }
        String[] newPixs = newPix.split(" ");
        int newWidth = Integer.parseInt(newPixs[0]);
        int newHeight = Integer.parseInt(newPixs[1]);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(newWidth, newHeight);

        if (GlobalSet.getLiveStatusValue() == GlobalSet.LIVE_STATUS.RGN_NIR) {
            mBinocularView = new BinocularView(mContext);
            mBinocularView.setImageView(mImageTrack);
            mBinocularView.setLivenessCallBack(this);
            mCameraView.addView(mBinocularView, layoutParams);
        } else {
            mMonocularView = new MonocularView(mContext);
            mMonocularView.setImageView(mImageTrack);
            mMonocularView.setLivenessCallBack(this);
            mCameraView.addView(mMonocularView, layoutParams);
        }
    }

    @Override
    public void onTip(int code, String msg) {

    }

    @Override
    public void onCanvasRectCallback(LivenessModel livenessModel) {

    }

    @Override
    public void onCallback(final int code, final LivenessModel livenessModel) {
        //子线程
        if (livenessModel != null) {
            if (code == 0) {
                //匹配到相似人脸,说明这个人已经存过东西,检测是否已经存>=3,如果是,提示先取出
                Feature feature = livenessModel.getFeature();
                //相似度
                float featureScore = livenessModel.getFeatureScore();
                //储存的名字,
                String userName = feature.getUserName();
                UserDao userDao = MainAppliction.getInstance().getDaoSession().getUserDao();
                List<User> users = userDao.queryRaw("where user_name=?", userName);
                if (users.size() > 0) {
                    User user = users.get(0);
                    String storageIndexs = user.getStorageIndexs();
                    JSONArray jsonArray;
                    if (!TextUtils.isEmpty(storageIndexs)) {
                        jsonArray = JSON.parseArray(storageIndexs);
                        if (jsonArray != null) {
                            if (jsonArray.size() >= 3) {
                                //已存大于等于三个,提示用户需要先取出已存的东西

                            } else {
                                //未大于三个,可以存

                            }
                        } else {
                            //说明无记录,可以存

                        }
                    } else {
                        //说明无记录,可以存

                    }
                } else {
                    //说明facesdk的数据库里有数据,但是user数据库没有.需要写入user数据库
                    long currentTimeMillis = System.currentTimeMillis();
                    User user = insertUser2DB(String.valueOf(currentTimeMillis / 1000),
                            currentTimeMillis / 1000,
                            currentTimeMillis / 1000,
                            feature.getCropImageName(), feature.getImageName());
                }
            } else {
                if (mNeedRegister) {
                    mNeedRegister = false;
                    mNickName = String.valueOf(System.currentTimeMillis() / 1000);
                    Log.i(TAG, "run: mNickName: " + mNickName);
                    FaceSDKManager.getInstance().getFaceLiveness().setRegistNickName(mNickName);
                    //设置为注册模式
                    FaceSDKManager.getInstance().getFaceLiveness().setCurrentTaskType(FaceLiveness.TaskType.TASK_TYPE_REGIST);
                }
            }
        }
    }

    // 注册结果
    private IFaceRegistCalllBack faceRegistCalllBack = new IFaceRegistCalllBack() {

        @Override
        public void onRegistCallBack(int code, LivenessModel livenessModel, final Bitmap cropBitmap) {

            switch (code) {
                case 0: {
                    // 设置注册信息
                    Log.i(TAG, "onRegistCallBack: 注册成功");
                    Feature feature = livenessModel.getFeature();
                    String userName = feature.getUserName();
                    User user = insertUser2DB(userName, Long.parseLong(userName), Long.parseLong(userName), feature.getCropImageName(), feature.getImageName());
                }
                break;
                default:
                    break;
            }
        }
    };

    /**
     * 向user数据库中插入一个新用户
     *
     * @param userName
     * @param firstTime
     * @param lastTime
     * @param cropImageName
     * @param imageName
     */
    private User insertUser2DB(String userName, long firstTime, long lastTime, String cropImageName, String imageName) {
        UserDao userDao = MainAppliction.getInstance().getDaoSession().getUserDao();
        User user = new User();
        user.setUserName(userName);
        user.setFirstTime(firstTime);
        user.setLastTime(lastTime);
        user.setCropImageName(cropImageName);
        user.setImageName(imageName);
        userDao.insert(user);
        return user;
    }
}
