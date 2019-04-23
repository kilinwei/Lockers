/*
 * Copyright (C) 2018 Baidu, Inc. All Rights Reserved.
 */
package com.xyf.lockers.manager;

import android.content.Context;

import com.baidu.idl.facesdk.FaceFeature;
import com.baidu.idl.facesdk.callback.Callback;
import com.baidu.idl.facesdk.model.Feature;
import com.xyf.lockers.callback.FaceCallback;

import java.util.List;

public class FaceFeatures {

    private FaceFeature mFaceFeature;

    public void initModel(Context context, String idPhotoModel, String visModel, String nirModel, final FaceCallback faceCallback) {
        if (mFaceFeature == null) {
            mFaceFeature = new FaceFeature();
            mFaceFeature.initModel(context, idPhotoModel, visModel, nirModel, new Callback() {
                @Override
                public void onResponse(int code, String response) {
                    faceCallback.onResponse(code, response);
                }
            });
        } else {
            mFaceFeature.initModel(context, idPhotoModel, visModel, nirModel, new Callback() {
                @Override
                public void onResponse(int code, String response) {
                    faceCallback.onResponse(code, response);
                }
            });
        }

    }

    /**
     * 人脸特征提取
     *
     * @param argb
     * @param landmarks
     * @param height
     * @param width
     * @param feature
     * @return
     */

    public float extractFeature(int[] argb, int height, int width, byte[] feature, int[] landmarks) {
        return mFaceFeature.feature(FaceFeature.FeatureType.FEATURE_VIS, argb, height, width, landmarks, feature);
    }

    public float extractFeatureForIDPhoto(int[] argb, int height, int width, byte[] feature, int[] landmarks) {
        return mFaceFeature.feature(FaceFeature.FeatureType.FEATURE_ID_PHOTO, argb, height, width, landmarks, feature);
    }

    /**
     * 人脸特征比对,并且映射到0--100
     *
     * @param feature1
     * @param feature2
     * @return
     */
    public float featureCompare(byte[] feature1, byte[] feature2) {
        if (mFaceFeature != null) {
            return mFaceFeature.featureCompare(FaceFeature.FeatureType.FEATURE_VIS, feature1, feature2);
        } else {
            return 0;
        }
    }

    public float featureIDCompare(byte[] feature1, byte[] feature2) {
        if (mFaceFeature != null) {
            return mFaceFeature.featureCompare(FaceFeature.FeatureType.FEATURE_ID_PHOTO, feature1, feature2);
        } else {
            return 0;
        }
    }

    public int setFeature(List<Feature> features) {
        if (mFaceFeature != null) {
            return mFaceFeature.setFeature(features);
        } else {
            return 0;
        }
    }

    public Feature featureCompareCpp(byte[] firstFaceFeature, FaceFeature.FeatureType featureType, float thresholdValue) {
        if (mFaceFeature != null) {
            return mFaceFeature.featureCompareCpp(firstFaceFeature, featureType, thresholdValue);
        } else {
            return null;
        }
    }

}
