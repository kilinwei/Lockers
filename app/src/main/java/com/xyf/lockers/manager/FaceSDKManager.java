package com.xyf.lockers.manager;

import android.content.Context;
import android.util.Log;

import com.baidu.idl.facesdk.FaceDetect;
import com.baidu.idl.facesdk.FaceFeature;
import com.baidu.idl.facesdk.model.Feature;
import com.xyf.lockers.api.FaceApi;
import com.xyf.lockers.api.LRUCache;
import com.xyf.lockers.app.Constants;
import com.xyf.lockers.callback.FaceCallback;
import com.xyf.lockers.common.FaceEnvironment;
import com.xyf.lockers.common.GlobalSet;
import com.xyf.lockers.db.DBManager;
import com.xyf.lockers.model.LivenessModel;
import com.xyf.lockers.utils.ToastUtil;

import java.util.List;
import java.util.Map;

public class FaceSDKManager {
    private static final String TAG = "FaceSDKManager";

    private FaceDetector faceDetector;
    private FaceFeatures faceFeature;
    private FaceLiveness faceLiveness;


    private FaceEnvironment faceEnvironment;

    private LRUCache<String, Feature> featureLRUCache = new LRUCache<>(1000);

    private FaceSDKManager() {
        faceDetector = new FaceDetector();
        faceFeature = new FaceFeatures();
        faceLiveness = new FaceLiveness();
        faceEnvironment = new FaceEnvironment();
    }

    private static class HolderClass {
        private static final FaceSDKManager instance = new FaceSDKManager();
    }

    public static FaceSDKManager getInstance() {
        return HolderClass.instance;
    }

    public FaceDetector getFaceDetector() {
        return faceDetector;
    }

    public FaceFeatures getFaceFeature() {
        return faceFeature;
    }

    public FaceLiveness getFaceLiveness() {
        return faceLiveness;
    }


    public void initModel(final Context context) {
        faceDetector.initModel(context, "detect_rgb_anakin_2.0.0.bin",
                "",
                "align_2.0.0.anakin.bin", new FaceCallback() {
                    @Override
                    public void onResponse(int code, String response) {
                        ToastUtil.showMessage(code + "  " + response);
                    }
                });
        faceDetector.loadConfig(getFaceEnvironmentConfig());
        faceFeature.initModel(context, "",
                "recognize_rgb_live_anakin_2.0.0.bin",
                "", new FaceCallback() {
                    @Override
                    public void onResponse(int code, String response) {
                        ToastUtil.showMessage(code + "  " + response);
                    }
                });
        faceLiveness.initModel(context, "liveness_rgb_anakin_2.0.0.bin",
                "liveness_nir_anakin_2.0.0.bin",
                "liveness_depth_anakin_2.0.0.bin", new FaceCallback() {
                    @Override
                    public void onResponse(int code, String response) {
                        ToastUtil.showMessage(code + "  " + response);
                    }
                });
//        faceDetector.initQuality(context, "blur_2.0.2.binary",
//                "occlusion_anakin_2.0.2.bin", new FaceCallback() {
//                    @Override
//                    public void onResponse(int code, String response) {
//                        ToastUtil.showMessage(code + "  " + response);
//                    }
//                });
    }

    public FaceEnvironment getFaceEnvironmentConfig() {
        faceEnvironment.setMinFaceSize(70);
        faceEnvironment.setMaxFaceSize(-1);
        faceEnvironment.setDetectInterval(200);
        faceEnvironment.setTrackInterval(500);
        faceEnvironment.setNoFaceSize(0.5f);
        faceEnvironment.setPitch(30);
        faceEnvironment.setYaw(30);
        faceEnvironment.setRoll(30);
        faceEnvironment.setCheckBlur(true);
        faceEnvironment.setOcclusion(true);
        faceEnvironment.setIllumination(true);
        faceEnvironment.setDetectMethodType(FaceDetect.DetectType.DETECT_VIS);
        return faceEnvironment;
    }

    /**
     * 单独图片质量检测方法（多人脸track 不做质量检测，可以通过该方法质量检测）
     *
     * @param imageData
     * @param height
     * @param width
     * @param landmark
     * @param bluriness
     * @param illum
     * @param occlusion
     * @param nOccluPart
     * @return
     */
    public int imgQuality(int[] imageData, int height, int width, int[] landmark,
                          float[] bluriness, int[] illum, float[] occlusion, int[] nOccluPart) {
        if (faceDetector != null) {
            return faceDetector.imgQuality(imageData, height, width, landmark, bluriness, illum, occlusion, nOccluPart);
        } else {
            return 0;
        }
    }

    public int setFeature() {
        List<Feature> listFeatures = FaceApi.getInstance().featureQuery();
        if (listFeatures != null && faceFeature != null) {
            faceFeature.setFeature(listFeatures);
            return listFeatures.size();
        }
        return 0;
    }

    public LRUCache<String, Feature> getFeatureLRUCache() {
        return featureLRUCache;
    }

    public Feature getFeature(FaceFeature.FeatureType featureType, byte[] curFeature, LivenessModel liveModel) {

        if (featureLRUCache.getAll().size() > 0) {
            for (Map.Entry<String, Feature> featureEntry : featureLRUCache.getAll()) {
                Feature feature = featureEntry.getValue();
                if (compare(Constants.PASS_SCORE, featureType, curFeature, liveModel, feature)) {
                    return feature;
                }
            }
        }

        Feature featureCpp = faceFeature.featureCompareCpp(curFeature, featureType,
                featureType == FaceFeature.FeatureType.FEATURE_VIS ? GlobalSet.getFeatureRgbValue()
                        : GlobalSet.getFeaturePhoneValue());

        if (featureCpp != null) {
            liveModel.setFeatureScore(featureCpp.getScore());
            List<Feature> features = DBManager.getInstance().queryFeatureById(featureCpp.getId());
            if (features != null && features.size() > 0) {
                Feature feature = features.get(0);
                featureLRUCache.put(feature.getUserName(), feature);
                return feature;
            }
        }
        return null;
    }

    /**
     * 返回null代表没有注册
     *
     * @param featureType
     * @param curFeature
     * @param liveModel
     * @return
     */
    public Feature isRegistered(FaceFeature.FeatureType featureType, byte[] curFeature, LivenessModel liveModel) {

        if (featureLRUCache.getAll().size() > 0) {
            for (Map.Entry<String, Feature> featureEntry : featureLRUCache.getAll()) {
                Feature feature = featureEntry.getValue();
                if (compare(Constants.REGISTER_SCORE, featureType, curFeature, liveModel, feature)) {
                    liveModel.setFeature(feature);
                    return feature;
                }
            }
        }

        List<Feature> allFeature = DBManager.getInstance().queryFeature();
        if (allFeature != null) {
            Log.i(TAG, "isRegistered: " + allFeature.size());
            for (Feature feature : allFeature) {
                if (compare(Constants.REGISTER_SCORE, featureType, curFeature, liveModel, feature)) {
                    featureLRUCache.put(feature.getUserName(), feature);
                    liveModel.setFeature(feature);
                    return feature;
                }
            }
        }
        return null;
    }

    private boolean compare(float f, FaceFeature.FeatureType featureType, byte[] curFeature,
                            LivenessModel liveModel, Feature feature) {
        float similariry;
        if (featureType == FaceFeature.FeatureType.FEATURE_VIS) {
            similariry = faceFeature.featureCompare(feature.getFeature(), curFeature);
            Log.i(TAG, "compare: 相似度: " + similariry + "  阈值: " + f);
            if (similariry > f) {
                liveModel.setFeatureScore(similariry);
                featureLRUCache.put(feature.getUserName(), feature);
                return true;
            } else {
            }
        } else if (featureType == FaceFeature.FeatureType.FEATURE_ID_PHOTO) {
            similariry = faceFeature.featureIDCompare(feature.getFeature(), curFeature);
            if (similariry > GlobalSet.getFeaturePhoneValue()) {
                liveModel.setFeatureScore(similariry);
                featureLRUCache.put(feature.getUserName(), feature);
                return true;
            }
        }
        return false;
    }
}