package com.xyf.lockers.model.bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.Generated;

/**
 * @项目名： Lockers
 * @包名： com.xyf.lockers.model.bean
 * @文件名: User
 * @创建者: kilin
 * @创建时间: 2019/3/6 22:02
 * @描述： TODO
 */
@Entity(nameInDb = "user")
public class User {

    @Id(autoincrement = true)
    private Long id;

    /**
     * 使用第一次识别的时间戳为名字,十位数
     */
    @Property(nameInDb = "user_name")
    private String userName;

    /**
     * 第一次使用的时间
     */
    @Property(nameInDb = "first_time")
    private long firstTime;

    /**
     * 最后使用的时间
     */
    @Property(nameInDb = "last_time")
    private long lastTime;

    /**
     * 储存在柜子的索引,可能有多个
     */
    @Property(nameInDb = "storage_indexs")
    private int storageIndexs;


    /**
     * 完整图片
     */
    @Property(nameInDb = "image_name")
    private String imageName;


    /**
     * 头像截图
     */
    @Property(nameInDb = "crop_image_name")
    private String cropImageName;


    @Generated(hash = 845847584)
    public User(Long id, String userName, long firstTime, long lastTime,
            int storageIndexs, String imageName, String cropImageName) {
        this.id = id;
        this.userName = userName;
        this.firstTime = firstTime;
        this.lastTime = lastTime;
        this.storageIndexs = storageIndexs;
        this.imageName = imageName;
        this.cropImageName = cropImageName;
    }


    @Generated(hash = 586692638)
    public User() {
    }


    public Long getId() {
        return this.id;
    }


    public void setId(Long id) {
        this.id = id;
    }


    public String getUserName() {
        return this.userName;
    }


    public void setUserName(String userName) {
        this.userName = userName;
    }


    public long getFirstTime() {
        return this.firstTime;
    }


    public void setFirstTime(long firstTime) {
        this.firstTime = firstTime;
    }


    public long getLastTime() {
        return this.lastTime;
    }


    public void setLastTime(long lastTime) {
        this.lastTime = lastTime;
    }


    public int getStorageIndexs() {
        return this.storageIndexs;
    }


    public void setStorageIndexs(int storageIndexs) {
        this.storageIndexs = storageIndexs;
    }


    public String getImageName() {
        return this.imageName;
    }


    public void setImageName(String imageName) {
        this.imageName = imageName;
    }


    public String getCropImageName() {
        return this.cropImageName;
    }


    public void setCropImageName(String cropImageName) {
        this.cropImageName = cropImageName;
    }


}
