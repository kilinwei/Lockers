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

    /**
     * bRec[1] 板子序号 01:1号板 02:2号板 04:3号板
     * bRec[2];这块板子的锁状态 11111110:代表1号锁开启,2到8号锁闭合
     *
     * @param bRec
     */
    void onAllLockersStatusResponse(byte[] bRec);

}