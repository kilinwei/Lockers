package com.xyf.lockers.ui.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.baidu.idl.facesdk.model.Feature;
import com.xyf.lockers.R;
import com.xyf.lockers.app.Constants;
import com.xyf.lockers.app.MainAppliction;
import com.xyf.lockers.base.BaseActivity;
import com.xyf.lockers.callback.IFaceRegistCalllBack;
import com.xyf.lockers.callback.ILivenessCallBack;
import com.xyf.lockers.common.GlobalSet;
import com.xyf.lockers.common.serialport.LockersCommHelper;
import com.xyf.lockers.common.serialport.LockersCommHelperNew;
import com.xyf.lockers.listener.OnSingleLockerStatusListener;
import com.xyf.lockers.manager.FaceLiveness;
import com.xyf.lockers.manager.FaceSDKManager;
import com.xyf.lockers.model.LivenessModel;
import com.xyf.lockers.model.bean.User;
import com.xyf.lockers.model.bean.UserDao;
import com.xyf.lockers.utils.DensityUtil;
import com.xyf.lockers.utils.LockerUtils;
import com.xyf.lockers.utils.SharedPreferenceUtil;
import com.xyf.lockers.utils.ToastUtil;
import com.xyf.lockers.utils.UserDBManager;
import com.xyf.lockers.view.BinocularView;
import com.xyf.lockers.view.MonocularView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

public class StorageActivity extends BaseActivity implements ILivenessCallBack, OnSingleLockerStatusListener {
    private static final String TAG = "StorageActivity";
    private static final int MSG_CHECK_FACE = 0x01;
    private static final int MSG_REGISTER_TIME_OUT = 0x0;
    private static final int MSG_NOT_CLOSE_DOOR = 0x03;
    private static final int PASS_TIME = 800;
    private static final int REGISTER_TIME_OUT = 10 * 1000;
    private static final int CLOSE_DOOR_TIME_OUT = 10 * 1000;

    @BindView(R.id.layout_camera)
    RelativeLayout mCameraView;
    @BindView(R.id.image_track)
    ImageView mImageTrack;
    @BindView(R.id.tv_similarity)
    TextView mTvSimilarity;
    @BindView(R.id.tv_countdown)
    TextView mTvCountdown;
    private Context mContext;
    private BinocularView mBinocularView;
    private MonocularView mMonocularView;
    private boolean mNeedRegister = false;
    private String mNickName;
    private int mCurrentOpenLockerIndex = -1;
    private User mCurrentUser;
    private boolean mFirstRecogniceFace;

    /**
     * 是否发送了检测关门命令
     */
    private boolean mCheckLockerClose;
    /**
     * 已识别到已注册用户
     */
    private boolean mIsRecognized;
    private byte[] mCurrentOpenLockerBytes;

    private int mConutdown = 2;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_CHECK_FACE:
                    if (mConutdown >= 0) {
                        mTvCountdown.setText(String.valueOf(mConutdown));
                        mConutdown--;
                        sendEmptyMessageDelayed(MSG_CHECK_FACE, PASS_TIME);
                    } else {
                        mTvCountdown.setText("");
                        mNeedRegister = true;
                        removeMessages(MSG_REGISTER_TIME_OUT);
                        sendEmptyMessageDelayed(MSG_REGISTER_TIME_OUT, REGISTER_TIME_OUT);
                        removeMessages(MSG_CHECK_FACE);
                    }
                    break;
                case MSG_REGISTER_TIME_OUT:
                    if (faceRegistCalllBack != null) {
                        faceRegistCalllBack.onRegistCallBack(1, null, null);
                    }
                    removeMessages(MSG_REGISTER_TIME_OUT);
                    break;
                case MSG_NOT_CLOSE_DOOR:
                    // TODO: 2019/3/13 用户未关门,此时控制闪灯,当用户关门时有没有关门回调?还是需要手动去查询门有没有关?
//                    LockersCommHelper.get().controlSingleLight(mCurrentOpenLockerIndex, 2);
                    if (mCurrentOpenLockerBytes != null) {
                        mCheckLockerClose = true;
                        LockersCommHelperNew.get().queryAll(mCurrentOpenLockerBytes[0], mCurrentOpenLockerBytes[1], mCurrentOpenLockerBytes[2], mCurrentOpenLockerBytes[3]);
                        Log.i(TAG, "handleMessage: 判断,如果此时用户未关门,控制闪灯");
                    }
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
        FaceSDKManager.getInstance().getFaceLiveness().removeRegistCallBack(faceRegistCalllBack);
        FaceSDKManager.getInstance().getFaceLiveness().addRegistCallBack(faceRegistCalllBack);
        //进入界面首先设置为通行,确保同一用户不会被注册两次
        FaceSDKManager.getInstance().getFaceLiveness().setCurrentTaskType(FaceLiveness.TaskType.TASK_TYPE_REGIST);
        initFaceData();
        calculateCameraView();
        //设置本次注册的用户名,用户过滤多次注册的问题,开启一次洁面只允许注册一次
        mNickName = String.valueOf(System.currentTimeMillis() / 1000);
        Log.i(TAG, "run: mNickName: " + mNickName);
        FaceSDKManager.getInstance().getFaceLiveness().setRegistNickName(mNickName);
    }


    @Override
    protected void onResume() {
        super.onResume();
        mHandler.sendEmptyMessageDelayed(MSG_REGISTER_TIME_OUT, REGISTER_TIME_OUT);
        if (GlobalSet.getLiveStatusValue() == GlobalSet.LIVE_STATUS.RGN_NIR) {
            mBinocularView.onResume();
        } else {
            mMonocularView.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (GlobalSet.getLiveStatusValue() == GlobalSet.LIVE_STATUS.RGN_NIR) {
            mBinocularView.onPause();
        } else {
            mMonocularView.onPause();
        }
        LockersCommHelper.get().setOnSingleLockerStatusListener(null);
        mHandler.removeCallbacksAndMessages(null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBinocularView != null) {
            mBinocularView.setLivenessCallBack(null);
            mBinocularView = null;
        }
        if (mMonocularView != null) {
            mMonocularView.setLivenessCallBack(null);
            mMonocularView = null;
        }
        mHandler.removeCallbacksAndMessages(null);
        //进入界面首先设置为通行,确保同一用户不会被注册两次
        FaceSDKManager.getInstance().getFaceLiveness().setCurrentTaskType(FaceLiveness.TaskType.TASK_TYPE_ONETON);
        FaceSDKManager.getInstance().getFaceLiveness().removeRegistCallBack(faceRegistCalllBack);
        LockersCommHelperNew.get().setOnSingleLockerStatusListener(null);
        faceRegistCalllBack = null;
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
    public void onTip(int code, final String msg) {
        if (mTvSimilarity != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mTvSimilarity != null) {
                        mTvSimilarity.setText(msg);
                    }
                }
            });
        }
        Log.d(TAG, "onTip: " + msg);
    }

    @Override
    public void onCanvasRectCallback(LivenessModel livenessModel) {

    }

    @Override
    public synchronized void onCallback(final int code, final LivenessModel livenessModel) {
        //子线程
        Log.d(TAG, "onCallback: code: " + code + "  livenessModel为空吗:" + (livenessModel == null) + "  当前activity的hashcode: " + this.hashCode());
        if (mIsRecognized) {
            return;
        }
        if (livenessModel == null) {
            return;
        }
        if (code == 0) {
            //匹配到相似人脸,说明这个人已经存过东西,检测是否已经存>=3,如果是,提示先取出
            //相似度
            final float featureScore = livenessModel.getFeatureScore();
            if (mTvSimilarity != null) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mTvSimilarity.setText(String.format("相似度: %s", featureScore));
                    }
                });
            }
            if (featureScore < Constants.PASS_SCORE) {
                return;
            }
            mHandler.removeMessages(MSG_CHECK_FACE);
            mHandler.removeMessages(MSG_REGISTER_TIME_OUT);
            Log.i(TAG, "onCallback: 识别到相似用户,去掉注册倒计时消息");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTvCountdown.setText("");
                }
            });

            Feature feature = livenessModel.getFeature();
            //储存的名字,
            String userName = feature.getUserName();
            UserDao userDao = MainAppliction.getInstance().getDaoSession().getUserDao();
            List<User> users = userDao.queryRaw("where user_name=?", userName);
            if (users.size() > 0) {
                User user = users.get(0);
                int storageIndexs = user.getStorageIndexs();
                int count = Integer.bitCount(storageIndexs);
                if (count < 1) {
                    //未大于1个,可以存,先打开箱门，然后再把箱位记录到数据库中
                    mCurrentUser = user;
                    Log.i(TAG, "onCallback: 识别到老用户,存物品未大于1个,可以存,当前存储个数: " + count + "用户ID: " + mCurrentUser.getUserName());
                    ToastUtil.showMessage(" 识别到老用户,存物品未大于1个,可以存,当前存储个数: " + count + "用户ID: " + mCurrentUser.getUserName());
                    openSingleLocker();
                } else {
                    // TODO: 2019/3/12  已存大于等于1个,提示用户需要先取出已存的东西
                    Log.i(TAG, "onCallback: 识别到老用户,已存大于等于1个");
                    ToastUtil.showMessage("您已保存过一个物品了");
                    removeCameraView("您已保存过一个物品了");
                }
            } else {
                //说明facesdk的数据库里有数据,但是user数据库没有.需要写入user数据库，再打开箱门，然后再把箱位记录到数据库中
                long currentTimeMillis = System.currentTimeMillis();
                mCurrentUser = UserDBManager.insertUser2DB(String.valueOf(currentTimeMillis / 1000),
                        currentTimeMillis / 1000,
                        currentTimeMillis / 1000,
                        feature.getCropImageName(), feature.getImageName());
                openSingleLocker();
                ToastUtil.showMessage("facesdk的数据库里有数据,user数据库没有,需要写入user数据库，再打开箱门,用户ID: " + mCurrentUser.getUserName());
                Log.i(TAG, "onCallback: facesdk的数据库里有数据,user数据库没有,需要写入user数据库，再打开箱门,用户ID: " + mCurrentUser.getUserName());
            }
            mIsRecognized = true;
        } else {
            if (mTvSimilarity != null) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mTvSimilarity.setText("未匹配到相似人脸");
                    }
                });
            }
            Log.i(TAG, "onCallback: 未匹配到相似人脸");
            if (!mFirstRecogniceFace) {
                mHandler.removeMessages(MSG_CHECK_FACE);
                mHandler.sendEmptyMessageDelayed(MSG_CHECK_FACE, PASS_TIME);
                mFirstRecogniceFace = true;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mTvCountdown.setText("开始注册倒计时");
                    }
                });
                ToastUtil.showMessage("开始注册倒计时");
                Log.i(TAG, "onCallback: 开始注册倒计时");
            }

            if (mNeedRegister) {
                mNeedRegister = false;
                mNickName = String.valueOf(System.currentTimeMillis() / 1000);
                Log.i(TAG, "run: mNickName: " + mNickName);
                FaceSDKManager.getInstance().getFaceLiveness().setRegistNickName(mNickName);
                //设置为注册模式
                FaceSDKManager.getInstance().getFaceLiveness().setCurrentTaskType(FaceLiveness.TaskType.TASK_TYPE_REGIST);
                ToastUtil.showMessage("已设置为注册模式");
                Log.i(TAG, "onCallback: 已设置为注册模式");
            } else {

            }
        }

    }

    // 注册结果
    private IFaceRegistCalllBack faceRegistCalllBack = new IFaceRegistCalllBack() {
        private String lastUserName;

        @Override
        public void onRegistCallBack(int code, LivenessModel livenessModel, final Bitmap cropBitmap) {
            switch (code) {
                case 0: {
                    //第一次注册
                    mHandler.removeMessages(MSG_REGISTER_TIME_OUT);
                    // 设置注册信息
                    Feature feature = livenessModel.getFeature();
                    String userName = feature.getUserName();
                    Log.i(TAG, "onRegistCallBack: 注册成功,注册人为: " + userName);
                    mCurrentUser = UserDBManager.insertUser2DB(userName, Long.parseLong(userName),
                            Long.parseLong(userName), feature.getCropImageName(), feature.getImageName());
                    if (!TextUtils.isEmpty(userName) && !userName.equalsIgnoreCase(lastUserName)) {
                        openSingleLocker();
                    }
                }
                break;
                case 2: {
                    //已注册过
                    mHandler.removeMessages(MSG_REGISTER_TIME_OUT);
                    // 设置注册信息
                    Feature feature = livenessModel.getFeature();
                    if (feature != null) {
                        String userName = feature.getUserName();
                        Log.i(TAG, "onRegistCallBack: 之前已注册过,注册人为: " + userName);
                        UserDao userDao = MainAppliction.getInstance().getDaoSession().getUserDao();
                        List<User> users = userDao.queryRaw("where user_name=?", userName);
                        if (users.size() > 0) {
                            User user = users.get(0);
                            int storageIndexs = user.getStorageIndexs();
                            int count = Integer.bitCount(storageIndexs);
                            if (count < 1) {
                                //未大于1个,可以存,先打开箱门，然后再把箱位记录到数据库中
                                mCurrentUser = user;
                                Log.i(TAG, "onRegistCallBack: 识别到老用户,存物品未大于1个,可以存,当前存储个数: " + count + "用户ID: " + mCurrentUser.getUserName());
                                ToastUtil.showMessage(" 识别到老用户,存物品未大于1个,可以存,当前存储个数: " + count + "用户ID: " + mCurrentUser.getUserName());
                                openSingleLocker();
                            } else {
                                // TODO: 2019/3/12  已存大于等于1个,提示用户需要先取出已存的东西
                                Log.i(TAG, "onRegistCallBack: 识别到老用户,已存大于等于1个");
                                ToastUtil.showMessage("您已保存过一个物品了");
                                removeCameraView("您已保存过一个物品了");
                            }
                        } else {
                            //说明facesdk的数据库里有数据,但是user数据库没有.需要写入user数据库，再打开箱门，然后再把箱位记录到数据库中
                            long currentTimeMillis = System.currentTimeMillis();
                            mCurrentUser = UserDBManager.insertUser2DB(String.valueOf(currentTimeMillis / 1000),
                                    currentTimeMillis / 1000,
                                    currentTimeMillis / 1000,
                                    feature.getCropImageName(), feature.getImageName());
                            openSingleLocker();
                            ToastUtil.showMessage("facesdk的数据库里有数据,user数据库没有,需要写入user数据库，再打开箱门,用户ID: " + mCurrentUser.getUserName());
                            Log.i(TAG, "onRegistCallBack: facesdk的数据库里有数据,user数据库没有,需要写入user数据库，再打开箱门,用户ID: " + mCurrentUser.getUserName());
                        }
                    }
                }
                break;
                case 1: {
                    //注册超时
                    // TODO: 2019/3/10 注册超时
                    removeCameraView("注册超时");
                    ToastUtil.showMessage("注册超时");
                    Log.i(TAG, "onRegistCallBack: 注册超时");
                }
                break;
                default:
                    break;
            }
        }
    };

    private void removeCameraView(final String tips) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mBinocularView != null && mCameraView != null) {
                    mBinocularView.onPause();
                    mCameraView.removeView(mBinocularView);
                }
                if (mMonocularView != null && mCameraView != null) {
                    mMonocularView.onPause();
                    Log.i(TAG, "run: removeCameraView");
                    mCameraView.removeView(mMonocularView);
                }

                Intent intent = new Intent(StorageActivity.this, ShowTipsActivity.class);
                intent.putExtra(ShowTipsActivity.TIPS, tips);
                startActivity(intent);
            }
        });
    }


    /**
     * 打开单个锁
     */
    private void openSingleLocker() {
        int allLockersStatus = SharedPreferenceUtil.getAllLockersStatus();
        int canOpenWayIndex = LockerUtils.checkCanOpen(allLockersStatus);
        Log.i(TAG, "openSingleLocker: canOpenWayIndex: " + canOpenWayIndex);
        if (canOpenWayIndex == -1) {
            // TODO: 2019/3/10 已存满
            Log.i(TAG, "openSingleLocker: 已存满");
            ToastUtil.showMessage("已存满");
            return;
        }
        mCurrentOpenLockerIndex = canOpenWayIndex;
        LockersCommHelperNew.get().setOnSingleLockerStatusListener(this);
        mCurrentOpenLockerBytes = LockerUtils.getOpenSingleLockerBytes(canOpenWayIndex);
        LockersCommHelperNew.get().controlSingleLock(mCurrentOpenLockerBytes[0], mCurrentOpenLockerBytes[1], mCurrentOpenLockerBytes[2], mCurrentOpenLockerBytes[3]);
    }

    /**
     * 获取到用户保存的柜子索引,更新状态
     *
     * @param currentOpeningLocker
     */
    private void updateStorageStatus(Integer currentOpeningLocker) {
        Log.i(TAG, "updateStorageStatus: currentOpeningLocker: " + currentOpeningLocker);
        if (mCurrentUser != null) {
            Log.i(TAG, "updateStorageStatus: 更新用户状态 mCurrentUser: " + mCurrentUser.getUserName());
            //获取当前打开的箱位
            int wayBinary = 1 << currentOpeningLocker;
            Log.i(TAG, "updateStorageStatus: wayBinary: " + Integer.toBinaryString(wayBinary));
            int allLockersStatus = SharedPreferenceUtil.getAllLockersStatus();
            Log.i(TAG, "updateStorageStatus: 更新之前所有柜子物品的状态:　" + Integer.toBinaryString(allLockersStatus));
            //用原来以保存的箱位或上现保存的箱位,然后记录所有已存东西的箱位索引
            allLockersStatus |= wayBinary;
            SharedPreferenceUtil.setAllLockersStatus(allLockersStatus);
            Log.i(TAG, "updateStorageStatus: 更新之后所有柜子物品的状态:　" + Integer.toBinaryString(allLockersStatus));
            //更新当前用户储存的箱位索引
            int storageIndexs = mCurrentUser.getStorageIndexs();
            storageIndexs |= wayBinary;
            Log.i(TAG, "updateStorageStatus: 用户存的位置： " + Integer.toBinaryString(storageIndexs));
            mCurrentUser.setStorageIndexs(storageIndexs);
            UserDBManager.update(mCurrentUser);
            //此时需要post一个定时任务,假如到时间用户未关门,那么闪灯
            mHandler.sendEmptyMessageDelayed(MSG_NOT_CLOSE_DOOR, CLOSE_DOOR_TIME_OUT);
        }
    }

    @Override
    public void onSingleLockerStatusResponse(int way, int status) {
//        if (status == 1) {
//            //锁已打开,此时需要判断是否是我们打开的锁,以及录入user数据库中
//            if (mCurrentOpenLockerIndex != -1 && mCurrentOpenLockerIndex == way) {
//                updateStorageStatus(way);
//            }
//        } else {
//            // TODO: 2019/3/15 锁已关闭
//            mHandler.removeMessages(MSG_NOT_CLOSE_DOOR);
//            LockersCommHelper.get().controlSingleLight(mCurrentOpenLockerIndex, 2);
//        }
    }

    @Override
    public void onSingleLockerStatusResponse(byte[] bRec) {
        int boardBinary = bRec[1];
        byte lockerBinary = bRec[2];
        ArrayList<Integer> openingLockesIndexs = LockerUtils.getOpeningLockesIndexs(boardBinary, lockerBinary);
        if (mCheckLockerClose) {
            if (openingLockesIndexs == null || openingLockesIndexs.isEmpty()) {
                Log.i(TAG, "onSingleLockerStatusResponse: 门已全部关闭");
            }
        } else {
            if (openingLockesIndexs == null || openingLockesIndexs.isEmpty()) {
                return;
            }
            for (int i = 0; i < openingLockesIndexs.size(); i++) {
                Integer openingLockesIndex = openingLockesIndexs.get(i);
                if (mCurrentOpenLockerIndex != -1 && mCurrentOpenLockerIndex == openingLockesIndex) {
                    removeCameraView("已打开" + (mCurrentOpenLockerIndex + 1) + "号柜门");
                    updateStorageStatus(openingLockesIndex);
                }
                Log.i(TAG, "onSingleLockerStatusResponse: 当前开的柜门索引为:　" + openingLockesIndex);
                ToastUtil.showMessage("当前开的柜门索引为:　" + openingLockesIndex);
            }
            Log.i(TAG, "onSingleLockerStatusResponse: 当前用户开门的索引为 mCurrentOpenLockerIndex: " + mCurrentOpenLockerIndex + " 当前已开的所有门索引为 openingLockesIndexs: " + openingLockesIndexs);
            Log.i(TAG, "onSingleLockerStatusResponse: 开了 " + openingLockesIndexs.size() + "个柜门");
        }
    }

    @Override
    public void disConnectDevice() {

    }
}
