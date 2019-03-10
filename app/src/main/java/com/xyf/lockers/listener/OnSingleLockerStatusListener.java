package com.xyf.lockers.listener;

/**
 * @项目名： Lockers
 * @包名： com.xyf.lockers.listener
 * @文件名: OnSingleLockerStatusListener
 * @创建者: kilin
 * @创建时间: 2019/3/10 22:40
 * @描述： TODO
 */
public interface OnSingleLockerStatusListener  extends BaseSerialportInterface{
    void onSingleLockerStatusResponse(int way, int status);
}
