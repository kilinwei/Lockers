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
import com.xyf.lockers.common.serialport.LockersCommHelper;
import com.xyf.lockers.listener.OnAllLockersStatusListener;
import com.xyf.lockers.listener.OnSingleLockerStatusListener;
import com.xyf.lockers.manager.FaceLiveness;
import com.xyf.lockers.manager.FaceSDKManager;
import com.xyf.lockers.model.LivenessModel;
import com.xyf.lockers.model.bean.User;
import com.xyf.lockers.model.bean.UserDao;
import com.xyf.lockers.utils.DensityUtil;
import com.xyf.lockers.utils.LockerUtils;
import com.xyf.lockers.utils.UserDBManager;
import com.xyf.lockers.view.BinocularView;
import com.xyf.lockers.view.MonocularView;

import java.util.List;

import butterknife.BindView;

public class StorageActivity extends BaseActivity implements ILivenessCallBack {
    private static final String TAG = "StorageActivity";
    private static final int CHECK_FACE = 0x01;
    private static final int MSG_REGISTER_TIME_OUT = 0x02;
    private static final int PASS_TIME = 3 * 1000;
    private static final int REGISTER_TIME_OUT = 30 * 1000;

    @BindView(R.id.layout_camera)
    RelativeLayout mCameraView;
    @BindView(R.id.image_track)
    ImageView mImageTrack;
    private Context mContext;
    private BinocularView mBinocularView;
    private MonocularView mMonocularView;
    private boolean mNeedRegister;
    private String mNickName;
    private int mCurrentOpenLockerIndex = -1;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CHECK_FACE:
                    mNeedRegister = true;
                    sendEmptyMessageDelayed(MSG_REGISTER_TIME_OUT, REGISTER_TIME_OUT);
                    removeMessages(CHECK_FACE);
                    break;
                case MSG_REGISTER_TIME_OUT:
                    if (faceRegistCalllBack != null) {
                        faceRegistCalllBack.onRegistCallBack(1, null, null);
                    }
                    removeMessages(MSG_REGISTER_TIME_OUT);
                    break;
                default:
                    break;
            }
        }
    };
    private User mCurrentUser;

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
        mHandler.sendEmptyMessageDelayed(CHECK_FACE, PASS_TIME);
    }

    @Override
    protected void onPause() {
        super.onPause();
        LockersCommHelper.get().setOnAllLockersStatusListener(null);
        LockersCommHelper.get().setOnSingleLockerStatusListener(null);
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
                                //未大于三个,可以存,先打开箱门，然后再把箱位记录到数据库中

                            }
                        } else {
                            //说明无记录,可以存,先打开箱门，然后再把箱位记录到数据库中

                        }
                    } else {
                        //说明无记录,可以存,先打开箱门，然后再把箱位记录到数据库中

                    }
                } else {
                    //说明facesdk的数据库里有数据,但是user数据库没有.需要写入user数据库，再打开箱门，然后再把箱位记录到数据库中
                    long currentTimeMillis = System.currentTimeMillis();
                    User user = UserDBManager.insertUser2DB(String.valueOf(currentTimeMillis / 1000),
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
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mBinocularView != null && mCameraView != null) {
                        mBinocularView.onPause();
                        mCameraView.removeView(mBinocularView);
                    }
                    if (mMonocularView != null && mCameraView != null) {
                        mMonocularView.onPause();
                        mCameraView.removeView(mMonocularView);
                    }
                }
            });
            switch (code) {
                case 0: {
                    mHandler.removeMessages(MSG_REGISTER_TIME_OUT);
                    // 设置注册信息
                    Feature feature = livenessModel.getFeature();
                    String userName = feature.getUserName();
                    mCurrentUser = UserDBManager.insertUser2DB(userName, Long.parseLong(userName),
                            Long.parseLong(userName), feature.getCropImageName(), feature.getImageName());
                    Log.i(TAG, "onRegistCallBack: 注册成功");
                    LockersCommHelper.get().getAllLockStatus(new OnAllLockersStatusListener() {
                        @Override
                        public void onAllLockersStatusResponse(long allLockers) {
                            int canOpen = LockerUtils.checkCanOpen(allLockers);
                            if (canOpen != -1) {
                                mCurrentOpenLockerIndex = canOpen;
                                openSingleLocker(canOpen);
                            } else {
                                // TODO: 2019/3/10 已存满
                            }
                        }

                        @Override
                        public void disConnectDevice() {
                            // TODO: 2019/3/10 串口未打开
                        }
                    });
                }
                break;
                case 1: {
                    //注册超时
                    // TODO: 2019/3/10 注册超时
                }
                break;
                default:
                    break;
            }
        }
    };

    /**
     * 打开单个锁
     *
     * @param way
     */
    private void openSingleLocker(int way) {
        LockersCommHelper.get().controlSingleLock(way, 1, new OnSingleLockerStatusListener() {
            @Override
            public void onSingleLockerStatusResponse(int way, int status) {
                //锁已打开,此时需要判断是否是我们打开的锁,以及录入user数据库中
                if (mCurrentOpenLockerIndex != -1 && mCurrentOpenLockerIndex == way) {
                    if (mCurrentUser != null) {
                        String storageIndexs = mCurrentUser.getStorageIndexs();
                        JSONArray jsonArray = JSON.parseArray(storageIndexs);
                        jsonArray.add(way);
                        mCurrentUser.setStorageIndexs(JSON.toJSONString(jsonArray));
                        UserDBManager.update(mCurrentUser);
                    }
                }
            }

            @Override
            public void disConnectDevice() {
                // TODO: 2019/3/10 串口未打开
            }
        });
    }

}
