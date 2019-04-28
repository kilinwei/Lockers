package com.xyf.lockers.utils;

import com.xyf.lockers.app.MainAppliction;
import com.xyf.lockers.model.bean.StorageBean;
import com.xyf.lockers.model.bean.StorageBeanDao;

import java.util.List;

/**
 * @项目名： Lockers
 * @包名： com.xyf.lockers.utils
 * @文件名: UserDBManager
 * @创建者: kilin
 * @创建时间: 2019/3/9 21:56
 * @描述： TODO
 */
public class StorageDBManager {

    /**
     * -*
     * 向storage数据库中插入一条新记录
     */
    public static StorageBean inserStorage2DB(String userName, String image, long time, int lockerNum, int type) {
        StorageBeanDao storageBeanDao = MainAppliction.getInstance().getDaoSession().getStorageBeanDao();
        StorageBean storageBean = new StorageBean();
        storageBean.setUserName(userName);
        storageBean.setCropImageName(image);
        storageBean.setTime(time);
        storageBean.setLockerNum(lockerNum);
        storageBean.setType(type);
        storageBeanDao.insert(storageBean);
        return storageBean;
    }

    public static StorageBean inserStorage2DB(StorageBean storageBean) {
        StorageBeanDao storageBeanDao = MainAppliction.getInstance().getDaoSession().getStorageBeanDao();
        storageBeanDao.insert(storageBean);
        return storageBean;
    }

    public static StorageBean getStorageBean(long id) {
        StorageBeanDao storageBeanDao = MainAppliction.getInstance().getDaoSession().getStorageBeanDao();
        StorageBean storageBean = storageBeanDao.loadByRowId(id);
        return storageBean;
    }

    public static List<StorageBean> getAllStorageRccord() {

        StorageBeanDao storageBeanDao = MainAppliction.getInstance().getDaoSession().getStorageBeanDao();
        List<StorageBean> storageBeans = storageBeanDao.loadAll();
        return storageBeans;

    }

    public static void deleteAll() {
        StorageBeanDao storageBeanDao = MainAppliction.getInstance().getDaoSession().getStorageBeanDao();
        storageBeanDao.deleteAll();
    }
}
