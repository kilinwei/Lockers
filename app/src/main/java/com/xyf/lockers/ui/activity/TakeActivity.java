package com.xyf.lockers.ui.activity;

import android.content.Context;
import android.os.Bundle;
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
import com.xyf.lockers.common.serialport.LockersCommHelper;
import com.xyf.lockers.listener.OnSingleLockerStatusListener;
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
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class TakeActivity extends BaseActivity implements ILivenessCallBack, OnSingleLockerStatusListener {
    private static final String TAG = "TakeActivity";
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
    private boolean mIsRecognized;
    private Disposable mSubscribe;
    /**
     * 当前开门的集合
     */
    private List<Integer> mCurrentStorageList;
    /**
     * 当前取物的用户
     */
    private User mCurrentUser;


    @Override
    protected int getLayout() {
        return R.layout.activity_take;
    }

    @Override
    protected void initEventAndData(Bundle savedInstanceState) {
        mContext = this;
        calculateCameraView();
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
//        mHandler.sendEmptyMessageDelayed(CHECK_FACE, TIME);
    }

    @Override
    protected void onStop() {
        if (GlobalSet.getLiveStatusValue() == GlobalSet.LIVE_STATUS.RGN_NIR) {
            mBinocularView.onPause();
        } else {
            mMonocularView.onPause();
        }
        if (mSubscribe != null && !mSubscribe.isDisposed()) {
            mSubscribe.dispose();
        }
        super.onStop();
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
                    openLockers(storageList);
                    mCurrentUser = user;
                } else {
                    //说明没有存物品,提醒用户没有存物品
                    Log.i(TAG, "onCallback: 已存大于等于三个");
                }
            } else {
                //说明facesdk的数据库里有数据,但是user数据库没有,说明user已被删除,没有存东西
            }
        } else {
            Log.i(TAG, "run: 未匹配到相似人脸");
        }
    }

    private void openLockers(final List<Integer> storageList) {
        if (storageList == null || storageList.isEmpty()) {
            return;
        }
        mCurrentStorageList = storageList;
        mSubscribe = Observable.just(1).subscribeOn(Schedulers.io())
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        for (Integer index : storageList) {
                            LockersCommHelper.get().controlSingleLock(index, 1, TakeActivity.this);
                            //延迟开门,是因为如果同一时间开门,用户可能没有听到两个门的声音,将声音分开
                            SystemClock.sleep(500);
                        }
                    }
                });
    }

    @Override
    public void onSingleLockerStatusResponse(int way, int status) {
        if (mCurrentStorageList == null || mCurrentStorageList.isEmpty() || mCurrentUser == null) {
            return;
        }
        if (status == 1) {
            //锁已打开,此时需要判断是否是我们打开的锁,以及录入user数据库中
            int storageIndexs = mCurrentUser.getStorageIndexs();
            //获取当前打开的箱位
            int wayBinary = 1 << (way - 1);
            //二进制取反,比如00001000变成111110111
            int i = ~wayBinary;
            //将指定位数的1抹去
            storageIndexs &= i;
            mCurrentUser.setStorageIndexs(storageIndexs);
            //更新数据库信息
            UserDBManager.update(mCurrentUser);
        } else {
            // TODO: 2019/3/15 锁已关闭
        }
    }

    @Override
    public void disConnectDevice() {
        // TODO: 2019/3/10 串口未打开
        Log.e(TAG, "disConnectDevice: 串口未打开");
    }
}