package com.xyf.lockers.ui.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.baidu.idl.facesdk.model.Feature;
import com.xyf.lockers.R;
import com.xyf.lockers.app.Constants;
import com.xyf.lockers.app.MainAppliction;
import com.xyf.lockers.base.BaseActivity;
import com.xyf.lockers.callback.IFaceRegistCalllBack;
import com.xyf.lockers.callback.ILivenessCallBack;
import com.xyf.lockers.common.GlobalSet;
import com.xyf.lockers.common.serialport.LockersCommHelper;
import com.xyf.lockers.listener.OnSingleLockerStatusListener;
import com.xyf.lockers.manager.FaceLiveness;
import com.xyf.lockers.manager.FaceSDKManager;
import com.xyf.lockers.model.LivenessModel;
import com.xyf.lockers.model.bean.User;
import com.xyf.lockers.model.bean.UserDao;
import com.xyf.lockers.utils.DensityUtil;
import com.xyf.lockers.utils.LockerUtils;
import com.xyf.lockers.utils.SharedPreferenceUtil;
import com.xyf.lockers.utils.UserDBManager;
import com.xyf.lockers.view.BinocularView;
import com.xyf.lockers.view.MonocularView;

import java.util.List;

import butterknife.BindView;

public class StorageActivity extends BaseActivity implements ILivenessCallBack {
    private static final String TAG = "StorageActivity";
    private static final int MSG_CHECK_FACE = 0x01;
    private static final int MSG_REGISTER_TIME_OUT = 0x02;
    private static final int MSG_NOT_CLOSE_DOOR = 0x03;
    private static final int PASS_TIME = 3 * 1000;
    private static final int REGISTER_TIME_OUT = 30 * 1000;
    private static final int CLOSE_DOOR_TIME_OUT = 60 * 1000;

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
    private User mCurrentUser;
    /**
     * 已识别到已注册用户
     */
    private boolean mIsRecognized;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_CHECK_FACE:
                    mNeedRegister = true;
                    sendEmptyMessageDelayed(MSG_REGISTER_TIME_OUT, REGISTER_TIME_OUT);
                    removeMessages(MSG_CHECK_FACE);
                    break;
                case MSG_REGISTER_TIME_OUT:
                    if (faceRegistCalllBack != null) {
                        faceRegistCalllBack.onRegistCallBack(1, null, null);
                    }
                    removeMessages(MSG_REGISTER_TIME_OUT);
                    break;
                case MSG_NOT_CLOSE_DOOR:
                    // TODO: 2019/3/13 用户未关门,此时控制闪灯,当用户关门时有没有关门回调?还是需要手动去查询门有没有关?
                    LockersCommHelper.get().controlSingleLight(mCurrentOpenLockerIndex, 2);
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
        mHandler.sendEmptyMessageDelayed(MSG_CHECK_FACE, PASS_TIME);
    }

    @Override
    protected void onPause() {
        super.onPause();
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
        if (mIsRecognized) {
            return;
        }
        //子线程
        if (livenessModel != null) {
            if (code == 0) {
                //匹配到相似人脸,说明这个人已经存过东西,检测是否已经存>=3,如果是,提示先取出
                //相似度
                float featureScore = livenessModel.getFeatureScore();
                if (featureScore < Constants.PASS_SCORE) {
                    return;
                }
                mIsRecognized = true;
                Feature feature = livenessModel.getFeature();
                //储存的名字,
                String userName = feature.getUserName();
                UserDao userDao = MainAppliction.getInstance().getDaoSession().getUserDao();
                List<User> users = userDao.queryRaw("where user_name=?", userName);
                if (users.size() > 0) {
                    User user = users.get(0);
                    int storageIndexs = user.getStorageIndexs();
                    int count = Integer.bitCount(storageIndexs);
                    if (count < 3) {
                        //未大于三个,可以存,先打开箱门，然后再把箱位记录到数据库中
                        mCurrentUser = user;
                        openSingleLocker();
                    } else {
                        // TODO: 2019/3/12  已存大于等于三个,提示用户需要先取出已存的东西
                        Log.i(TAG, "onCallback: 已存大于等于三个");

                    }
                } else {
                    //说明facesdk的数据库里有数据,但是user数据库没有.需要写入user数据库，再打开箱门，然后再把箱位记录到数据库中
                    long currentTimeMillis = System.currentTimeMillis();
                    mCurrentUser = UserDBManager.insertUser2DB(String.valueOf(currentTimeMillis / 1000),
                            currentTimeMillis / 1000,
                            currentTimeMillis / 1000,
                            feature.getCropImageName(), feature.getImageName());
                    openSingleLocker();
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
                    openSingleLocker();
                }
                break;
                case 1: {
                    //注册超时
                    // TODO: 2019/3/10 注册超时
                    Log.i(TAG, "onRegistCallBack: 注册超时");
                }
                break;
                default:
                    break;
            }
        }
    };


    /**
     * 打开单个锁
     */
    private void openSingleLocker() {
        int allLockersStatus = SharedPreferenceUtil.getAllLockersStatus();
        int way = LockerUtils.checkCanOpen(allLockersStatus);
        if (way == -1) {
            // TODO: 2019/3/10 已存满
            Log.i(TAG, "openSingleLocker: 已存满");
            return;
        }
        mCurrentOpenLockerIndex = way;
        LockersCommHelper.get().controlSingleLock(way, 1, new OnSingleLockerStatusListener() {
            @Override
            public void onSingleLockerStatusResponse(int way, int status) {
                if (status == 1) {
                    //锁已打开,此时需要判断是否是我们打开的锁,以及录入user数据库中
                    if (mCurrentOpenLockerIndex != -1 && mCurrentOpenLockerIndex == way) {
                        if (mCurrentUser != null) {
                            //获取当前打开的箱位
                            int wayBinary = 1 << (way - 1);
                            int allLockersStatus = SharedPreferenceUtil.getAllLockersStatus();
                            //用原来以保存的箱位或上现保存的箱位,然后记录所有已存东西的箱位索引
                            allLockersStatus |= wayBinary;
                            SharedPreferenceUtil.setAllLockersStatus(allLockersStatus);

                            //更新当前用户储存的箱位索引
                            int storageIndexs = mCurrentUser.getStorageIndexs();
                            storageIndexs |= wayBinary;
                            mCurrentUser.setStorageIndexs(storageIndexs);
                            UserDBManager.update(mCurrentUser);
                            //此时需要post一个定时任务,假如到时间用户未关门,那么闪灯
                            mHandler.sendEmptyMessageDelayed(MSG_NOT_CLOSE_DOOR, CLOSE_DOOR_TIME_OUT);
                        }
                    }
                } else {
                    // TODO: 2019/3/15 锁已关闭
                    mHandler.removeMessages(MSG_NOT_CLOSE_DOOR);
                    LockersCommHelper.get().controlSingleLight(mCurrentOpenLockerIndex, 2);
                }
            }

            @Override
            public void disConnectDevice() {
                // TODO: 2019/3/10 串口未打开
                Log.e(TAG, "disConnectDevice: 串口未打开");
            }
        });
    }

}
