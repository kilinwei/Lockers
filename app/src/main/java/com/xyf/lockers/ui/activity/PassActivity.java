package com.xyf.lockers.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
import com.xyf.lockers.utils.FileUtils;
import com.xyf.lockers.utils.LockerUtils;
import com.xyf.lockers.utils.SharedPreferenceUtil;
import com.xyf.lockers.utils.ToastUtil;
import com.xyf.lockers.utils.UserDBManager;
import com.xyf.lockers.utils.Utils;
import com.xyf.lockers.view.BinocularView;
import com.xyf.lockers.view.CircleImageView;
import com.xyf.lockers.view.CirclePercentView;
import com.xyf.lockers.view.MonocularView;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;


/**
 * Created by litonghui on 2018/11/17.
 */

public class PassActivity extends BaseActivity implements ILivenessCallBack, View.OnClickListener, OnSingleLockerStatusListener {
    private static final String TAG = "PassActivity";
    private Context mContext;

    private RelativeLayout mCameraView;
    private BinocularView mBinocularView;
    private MonocularView mMonocularView;

    private CircleImageView mImage;
    private TextView mNickNameTv;
    private TextView mSimilariryTv;
    private TextView mNumTv;
    private TextView mDetectTv;
    private TextView mFeatureTv;
    private TextView mLiveTv;
    private TextView mAllTv;

    private Bitmap mBitmap;
    private String mUserName;

    private CirclePercentView mRgbCircleView;
    private CirclePercentView mNirCircleView;
    private CirclePercentView mDepthCircleView;

    private RelativeLayout mLayoutInfo;
    private LinearLayout mLinearTime;
    private LinearLayout mLinearUp;
    private ImageView mImageTrack;


    @Override
    protected int getLayout() {
        return R.layout.activity_pass;
    }

    @Override
    protected void initEventAndData(Bundle savedInstanceState) {
        mContext = this;
        initView();
        initFaceData();
        LockersCommHelperNew.get().setOnSingleLockerStatusListener(this);
    }

    private void initView() {
        mLableTxt = findViewById(R.id.title);
        mLableTxt.setText(R.string.pass_1_n);
        mCameraView = findViewById(R.id.layout_camera);
        mImageTrack = findViewById(R.id.image_track);
        // 计算并适配显示图像容器的宽高
        calculateCameraView();
        mImage = findViewById(R.id.image);
        mNickNameTv = findViewById(R.id.tv_nick_name);
        mSimilariryTv = findViewById(R.id.tv_similarity);
        mNumTv = findViewById(R.id.tv_num);
        mDetectTv = findViewById(R.id.tv_detect);
        mFeatureTv = findViewById(R.id.tv_feature);
        mLiveTv = findViewById(R.id.tv_live);
        mAllTv = findViewById(R.id.tv_all);

        mRgbCircleView = findViewById(R.id.circle_rgb_live);
        mNirCircleView = findViewById(R.id.circle_nir_live);
        mDepthCircleView = findViewById(R.id.circle_depth_live);

        mLayoutInfo = findViewById(R.id.layout_info);
        mLinearTime = findViewById(R.id.linear_time);
        mLinearUp = findViewById(R.id.linear_up);
        RelativeLayout relativeDown = findViewById(R.id.relative_down);
        RelativeLayout relativeUp = findViewById(R.id.relative_up);
        relativeDown.setOnClickListener(this);
        relativeUp.setOnClickListener(this);
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
    }

    @Override
    protected void onStop() {
        if (GlobalSet.getLiveStatusValue() == GlobalSet.LIVE_STATUS.RGN_NIR) {
            mBinocularView.onPause();
        } else {
            mMonocularView.onPause();
        }
        super.onStop();
    }

    @Override
    public void onTip(int code, String msg) {

    }

    @Override
    public void onCanvasRectCallback(LivenessModel livenessModel) {

    }

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

    @Override
    public void onCallback(final int code, final LivenessModel livenessModel) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mDetectTv.setText(String.format("人脸检测耗时: %s ms", livenessModel == null
                        ? 0 : livenessModel.getRgbDetectDuration()));
                mFeatureTv.setText(String.format("特征提取耗时: %s ms", livenessModel == null
                        ? 0 : livenessModel.getFeatureDuration()));
                mLiveTv.setText(String.format("活体检测耗时: %s ms", livenessModel == null
                        ? 0 : livenessModel.getLiveDuration()));
                mAllTv.setText(String.format("1:N人脸检索耗时: %s ms", livenessModel == null
                        ? 0 : livenessModel.getCheckDuration()));

                mRgbCircleView.setCurPercent(livenessModel == null
                        ? 0 : livenessModel.getRgbLivenessScore());

                mNirCircleView.setCurPercent(livenessModel == null
                        ? 0 : livenessModel.getIrLivenessScore());

                mDepthCircleView.setCurPercent(livenessModel == null
                        ? 0 : livenessModel.getDepthLivenessScore());

                if (livenessModel == null) {
                    mLayoutInfo.setVisibility(View.INVISIBLE);

                } else {
                    mLayoutInfo.setVisibility(View.VISIBLE);
                    if (code == 0) {
                        Feature feature = livenessModel.getFeature();
                        mSimilariryTv.setText(String.format("相似度: %s", livenessModel.getFeatureScore()));
                        mNickNameTv.setText(String.format("%s，你好!", feature.getUserName()));

                        if (!TextUtils.isEmpty(mUserName) && feature.getUserName().equals(mUserName)) {
                            mImage.setImageBitmap(mBitmap);
                        } else {
                            String imgPath = FileUtils.getFaceCropPicDirectory().getAbsolutePath()
                                    + "/" + feature.getCropImageName();
                            Bitmap bitmap = Utils.getBitmapFromFile(imgPath);
                            mImage.setImageBitmap(bitmap);
                            mBitmap = bitmap;
                            mUserName = feature.getUserName();
                        }

                        //匹配到相似人脸,说明这个人已经存过东西,检测已经存几个,将用户存的箱子一次性打开
                        //相似度
                        float featureScore = livenessModel.getFeatureScore();
                        if (featureScore < Constants.PASS_SCORE) {
                            ToastUtil.showMessage("分数低于： " + Constants.PASS_SCORE);
                            return;
                        }
                        if (mIsRecognizing) {
                            // TODO: 2019/3/15 请等待上一个用户取完物品再取
                            Log.i(TAG, "onCallback: 请等待上一个用户取完物品再取");
                            ToastUtil.showMessage("请等待上一个用户取完物品再取");
                            return;
                        }
                        //储存的名字,
                        String userName = feature.getUserName();
                        UserDao userDao = MainAppliction.getInstance().getDaoSession().getUserDao();
                        List<User> users = userDao.queryRaw("where user_name=?", userName);
                        if (users.size() > 0) {
                            for (User user : users) {
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
                                    ToastUtil.showMessage(" 说明没有存物品,提醒用户没有存物品");
                                    Log.i(TAG, "onCallback: 说明没有存物品,提醒用户没有存物品");
                                    Intent intent = new Intent(PassActivity.this, ShowTipsActivity.class);
                                    intent.putExtra(ShowTipsActivity.TIPS, getString(R.string.no_storage));
                                    startActivity(intent);
                                }
                            }
                        }
                    } else {
                        mSimilariryTv.setText("未匹配到相似人脸");
                        mNickNameTv.setText("陌生访客");
                        mImage.setImageResource(R.mipmap.preview_image_angle);
                    }
                }
            }
        });
    }


    private synchronized void openLockers(final List<Integer> storageList) {
        mCurrentStorageList = storageList;
        mSubscribe = Observable.just(1).subscribeOn(Schedulers.io())
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        for (Integer index : storageList) {
                            byte[] openSingleLockerBytes = LockerUtils.getOpenSingleLockerBytes(index);
                            LockersCommHelperNew.get().autoLightOpen(openSingleLockerBytes[0], openSingleLockerBytes[1], openSingleLockerBytes[2], openSingleLockerBytes[3]);
                            //延迟开门,是因为如果同一时间开门,用户可能没有听到两个门的声音,将声音分开,以及电流不够同时开几把锁
                            SystemClock.sleep(LockerUtils.OPEN_LOCKER_INTEVAL);
                        }
                    }
                });
        Intent intent = new Intent(PassActivity.this, ShowTipsActivity.class);
        intent.putExtra(ShowTipsActivity.TIPS, "已打开柜门");
        startActivity(intent);
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
    public void onResponseTime() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showTipsActivity(getString(R.string.seriaport_take_timeout), Color.RED);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LockersCommHelperNew.get().setOnSingleLockerStatusListener(null);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.relative_down) {
            mLinearTime.setVisibility(View.GONE);
            mLinearUp.setVisibility(View.VISIBLE);
        }

        if (view.getId() == R.id.relative_up) {
            mLinearTime.setVisibility(View.VISIBLE);
            mLinearUp.setVisibility(View.GONE);
        }
    }

}
