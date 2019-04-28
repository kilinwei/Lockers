package com.xyf.lockers.model.bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.Generated;

/**
 * @项目名： Lockers
 * @包名： com.xyf.lockers.model.bean
 * @文件名: StorageBean
 * @创建者: kilin
 * @创建时间: 2019/4/28 16:06
 * @描述： TODO
 */
@Entity(nameInDb = "storage")
public class StorageBean {

    @Id(autoincrement = true)
    private Long id;

    /**
     * 使用第一次识别的时间戳为名字,十位数
     */
    @Property(nameInDb = "user_name")
    private String userName;

    /**
     * 头像截图
     */
    @Property(nameInDb = "crop_image_name")
    private String cropImageName;

    @Property(nameInDb = "time")
    private long time;

    private int lockerNum;

    /**
     * 1:存 2:临时取  3.取
     */
    @Property(nameInDb = "type")
    private int type;

    @Generated(hash = 939470705)
    public StorageBean(Long id, String userName, String cropImageName, long time,
            int lockerNum, int type) {
        this.id = id;
        this.userName = userName;
        this.cropImageName = cropImageName;
        this.time = time;
        this.lockerNum = lockerNum;
        this.type = type;
    }

    @Generated(hash = 806242961)
    public StorageBean() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCropImageName() {
        return this.cropImageName;
    }

    public void setCropImageName(String cropImageName) {
        this.cropImageName = cropImageName;
    }

    public long getTime() {
        return this.time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public int getLockerNum() {
        return this.lockerNum;
    }

    public void setLockerNum(int lockerNum) {
        this.lockerNum = lockerNum;
    }

    public int getType() {
        return this.type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getUserName() {
        return this.userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
