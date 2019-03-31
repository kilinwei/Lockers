package com.xyf.lockers.manager;

import android.content.Context;

import com.baidu.idl.facesdk.FaceDetect;
import com.baidu.idl.facesdk.FaceFeature;
import com.baidu.idl.facesdk.model.Feature;
import com.xyf.lockers.api.FaceApi;
import com.xyf.lockers.api.LRUCache;
import com.xyf.lockers.callback.FaceCallback;
import com.xyf.lockers.common.FaceEnvironment;
import com.xyf.lockers.common.GlobalSet;
import com.xyf.lockers.db.DBManager;
import com.xyf.lockers.model.LivenessModel;
import com.xyf.lockers.utils.ToastUtil;

import java.util.List;
import java.util.Map;

public class FaceSDKManager {
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
                        ToastUtil.showMessage( code + "  " + response);
                    }
                });
        faceDetector.loadConfig(getFaceEnvironmentConfig());
        faceFeature.initModel(context, "",
                "recognize_rgb_live_anakin_2.0.0.bin",
                "", new FaceCallback() {
                    @Override
                    public void onResponse(int code, String response) {
                       ToastUtil.showMessage( code + "  " + response);
                    }
                });
        faceLiveness.initModel(context, "liveness_rgb_anakin_2.0.0.bin",
                "liveness_nir_anakin_2.0.0.bin",
                "liveness_depth_anakin_2.0.0.bin", new FaceCallback() {
                    @Override
                    public void onResponse(int code, String response) {
                       ToastUtil.showMessage( code + "  " + response);
                    }
                });
    }

    public FaceEnvironment getFaceEnvironmentConfig() {
        faceEnvironment.setMinFaceSize(50);
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
                float similariry;
                if (featureType == FaceFeature.FeatureType.FEATURE_VIS) {
                    similariry = faceFeature.featureCompare(feature.getFeature(), curFeature);
                    if (similariry > GlobalSet.getFeatureRgbValue()) {
                        liveModel.setFeatureScore(similariry);
                        featureLRUCache.put(feature.getUserName(), feature);
                        return feature;
                    }
                } else if (featureType == FaceFeature.FeatureType.FEATURE_ID_PHOTO) {
                    similariry = faceFeature.featureIDCompare(feature.getFeature(), curFeature);
                    if (similariry > GlobalSet.getFeaturePhoneValue()) {
                        liveModel.setFeatureScore(similariry);
                        featureLRUCache.put(feature.getUserName(), feature);
                        return feature;
                    }
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
}