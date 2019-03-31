package com.xyf.lockers.ui.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.baidu.idl.facesdk.model.Feature;
import com.xyf.lockers.R;
import com.xyf.lockers.app.Constants;
import com.xyf.lockers.app.MainAppliction;
import com.xyf.lockers.base.BaseActivity;
import com.xyf.lockers.callback.ILivenessCallBack;
import com.xyf.lockers.common.GlobalSet;
import com.xyf.lockers.common.serialport.LockersCommHelperNew;
import com.xyf.lockers.listener.OnSingleLockerStatusListener;
import com.xyf.lockers.model.LivenessModel;
import com.xyf.lockers.model.bean.User;
import com.xyf.lockers.model.bean.UserDao;
import com.xyf.lockers.utils.DensityUtil;
import com.xyf.lockers.utils.LockerUtils;
import com.xyf.lockers.utils.SharedPreferenceUtil;
import com.xyf.lockers.utils.UserDBManager;
import com.xyf.lockers.view.BinocularView;
import com.xyf.lockers.view.MonocularView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class TakeActivity extends BaseActivity implements ILivenessCallBack, OnSingleLockerStatusListener {
    // TODO: 2019/3/15  待写功能：判断用户取出所有物品之后，关闭本界面

    private static final String TAG = "TakeActivity";
    public static final int MSG_PASS_TIME_OUT = 0x04;
    public static final int PASS_OUT_TIME = 30 * 1000;
    public static final int OPEN_LOCKER_INTEVAL = 2 * 1000;
    @BindView(R.id.layout_camera)
    RelativeLayout mCameraView;
    @BindView(R.id.image_track)
    ImageView mImageTrack;
    private Context mContext;
    private BinocularView mBinocularView;
    private MonocularView mMonocularView;
    /**
     * 已识别到已注册用户
     */
    private boolean mIsRecognizing;
    private Disposable mSubscribe;
    /**
     * 当前开门的集合
     */
    private List<Integer> mCurrentStorageList;
    /**
     * 当前取物的用户
     */
    private User mCurrentUser;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_PASS_TIME_OUT:
                    if (mIsRecognizing) {

                    } else {
                        // TODO: 2019/3/15 取出超时，提示用户，关闭界面

                    }
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected int getLayout() {
        return R.layout.activity_take;
    }

    @Override
    protected void initEventAndData(Bundle savedInstanceState) {
        mContext = this;
        //设置为注册模式
        calculateCameraView();
        LockersCommHelperNew.get().setOnSingleLockerStatusListener(this);
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
    protected void onResume() {
        super.onResume();
        if (GlobalSet.getLiveStatusValue() == GlobalSet.LIVE_STATUS.RGN_NIR) {
            mBinocularView.onResume();
        } else {
            mMonocularView.onResume();
        }
        mHandler.sendEmptyMessageDelayed(MSG_PASS_TIME_OUT, PASS_OUT_TIME);
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (GlobalSet.getLiveStatusValue() == GlobalSet.LIVE_STATUS.RGN_NIR) {
            mBinocularView.onPause();
        } else {
            mMonocularView.onPause();
        }
        if (mSubscribe != null && !mSubscribe.isDisposed()) {
            mSubscribe.dispose();
        }
        mHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onTip(int code, String msg) {

    }

    @Override
    public void onCanvasRectCallback(LivenessModel livenessModel) {

    }

    @Override
    public void onCallback(final int code, final LivenessModel livenessModel) {
        if (code == 0) {
            //匹配到相似人脸,说明这个人已经存过东西,检测已经存几个,将用户存的箱子一次性打开
            //相似度
            float featureScore = livenessModel.getFeatureScore();
            if (featureScore < Constants.PASS_SCORE) {
                return;
            }
            if (mIsRecognizing) {
                // TODO: 2019/3/15 请等待上一个用户取完物品再取
                Log.i(TAG, "onCallback: 请等待上一个用户取完物品再取");
                return;
            }
            Feature feature = livenessModel.getFeature();
            //储存的名字,
            String userName = feature.getUserName();
            UserDao userDao = MainAppliction.getInstance().getDaoSession().getUserDao();
            List<User> users = userDao.queryRaw("where user_name=?", userName);
            if (users.size() > 0) {
                User user = users.get(0);
                long storageIndexs = user.getStorageIndexs();
                int count = Long.bitCount(storageIndexs);
                if (count > 0) {
                    //说明已存物品,需要开箱
                    //获取到需要开箱的集合
                    List<Integer> storageList = LockerUtils.getStorageIndexs(storageIndexs);
                    if (storageList != null && !storageList.isEmpty()) {
                        mCurrentUser = user;
                        mIsRecognizing = true;
                        openLockers(storageList);
                    }
                } else {
                    //说明没有存物品,提醒用户没有存物品
                    Log.i(TAG, "onCallback: 说明没有存物品,提醒用户没有存物品");
                }
            } else {
                //说明facesdk的数据库里有数据,但是user数据库没有,说明user已被删除,没有存东西,不需要处理
            }
        } else {
            Log.i(TAG, "run: 未匹配到相似人脸");
        }
    }

    private void openLockers(final List<Integer> storageList) {
        mCurrentStorageList = storageList;
        mSubscribe = Observable.just(1).subscribeOn(Schedulers.io())
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        for (Integer index : storageList) {
                            byte[] openSingleLockerBytes = LockerUtils.getOpenSingleLockerBytes(index);
                            LockersCommHelperNew.get().controlSingleLock(openSingleLockerBytes[0], openSingleLockerBytes[1], openSingleLockerBytes[2], openSingleLockerBytes[3]);
                            //延迟开门,是因为如果同一时间开门,用户可能没有听到两个门的声音,将声音分开,以及电流不够同时开几把锁
                            SystemClock.sleep(OPEN_LOCKER_INTEVAL);
                        }
                    }
                });
    }

    @Override
    public void onSingleLockerStatusResponse(int way, int status) {

    }

    /**
     * *bRec[1] 板子序号 01:1号板 02:2号板 04:3号板
     * *bRec[2];这块板子的锁状态 11111110:代表1号锁开启,2到8号锁闭合
     *
     * @param bRec
     */
    @Override
    public void onSingleLockerStatusResponse(byte[] bRec) {
        if (mCurrentStorageList == null || mCurrentStorageList.isEmpty() || mCurrentUser == null) {
            Log.e(TAG, "onSingleLockerStatusResponse: 门意外打开!!!!!!!!!!!!");
            return;
        }
        int boardBinary = bRec[1];
        byte lockerBinary = bRec[2];
        ArrayList<Integer> lockers = LockerUtils.getOpeningLockesIndexs(boardBinary, lockerBinary);
        if (lockers != null) {
            int storageIndexs = mCurrentUser.getStorageIndexs();
            for (Integer locker : lockers) {
                //获取当前打开的箱位
                int wayBinary = 1 << locker;
                //二进制取反,比如00001000变成111110111
                int i = ~wayBinary;
                //将指定位数的1抹去
                storageIndexs &= i;

                int allLockersStatus = SharedPreferenceUtil.getAllLockersStatus();
                //用原来以保存的箱位或上现保存的箱位,然后记录所有已存东西的箱位索引
                allLockersStatus &= i;

                SharedPreferenceUtil.setAllLockersStatus(allLockersStatus);
                mCurrentUser.setStorageIndexs(storageIndexs);
                //更新数据库信息
                UserDBManager.update(mCurrentUser);
            }
        } else {
            Log.i(TAG, "onSingleLockerStatusResponse: 没有打开任何柜门");
        }
    }

    @Override
    public void disConnectDevice() {
        // TODO: 2019/3/10 串口未打开
        Log.e(TAG, "disConnectDevice: 串口未打开");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LockersCommHelperNew.get().setOnSingleLockerStatusListener(null);
    }
}