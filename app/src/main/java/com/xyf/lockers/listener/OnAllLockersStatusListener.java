package com.xyf.lockers.listener;

/**
 * @项目名： Lockers
 * @包名： com.xyf.lockers.listener
 * @文件名: OnAllLockersStatusListener
 * @创建者: kilin
 * @创建时间: 2019/3/10 22:39
 * @描述： TODO
 */
public interface OnAllLockersStatusListener extends BaseSerialportInterface {

    void onAllLockersStatusResponse(int allLockers);

}