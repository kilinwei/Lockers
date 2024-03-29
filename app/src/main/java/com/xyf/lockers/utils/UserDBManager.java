package com.xyf.lockers.utils;

import com.xyf.lockers.app.MainAppliction;
import com.xyf.lockers.model.bean.User;
import com.xyf.lockers.model.bean.UserDao;

import org.greenrobot.greendao.query.QueryBuilder;

import java.util.List;

/**
 * @项目名： Lockers
 * @包名： com.xyf.lockers.utils
 * @文件名: UserDBManager
 * @创建者: kilin
 * @创建时间: 2019/3/9 21:56
 * @描述： TODO
 */
public class UserDBManager {

    /**
     * 向user数据库中插入一个新用户
     *
     * @param userName
     * @param firstTime
     * @param lastTime
     * @param cropImageName
     * @param imageName
     */
    public static User insertUser2DB(String userName, long firstTime, long lastTime, String cropImageName, String imageName) {
        UserDao userDao = MainAppliction.getInstance().getDaoSession().getUserDao();
        User user = new User();
        user.setUserName(userName);
        user.setFirstTime(firstTime);
        user.setLastTime(lastTime);
        user.setCropImageName(cropImageName);
        user.setImageName(imageName);
        userDao.insert(user);
        return user;
    }

    public static void update(User user) {
        if (user == null) {
            return;
        }
        UserDao userDao = MainAppliction.getInstance().getDaoSession().getUserDao();
        userDao.update(user);
    }

    public static User getUser(long id) {
        UserDao userDao = MainAppliction.getInstance().getDaoSession().getUserDao();
        User user = userDao.loadByRowId(id);
        return user;
    }

    public static List<User> getAllStorageUser() {
        UserDao userDao = MainAppliction.getInstance().getDaoSession().getUserDao();
        QueryBuilder<User> userQueryBuilder = userDao.queryBuilder();
        //查询大于1的
        List<User> storageUsers = userQueryBuilder.where(UserDao.Properties.StorageIndexs.gt(0)).list();
        return storageUsers;

    }
}
