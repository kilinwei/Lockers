package com.xyf.lockers.listener;

/**
 * @项目名： Lockers
 * @包名： com.xyf.lockers.listener
 * @文件名: OnSingleLockerStatusListener
 * @创建者: kilin
 * @创建时间: 2019/3/10 22:40
 * @描述： TODO
 */
public interface OnSingleLockerStatusListener extends BaseSerialportInterface {
    void onSingleLockerStatusResponse(int way, int status);

    /**
     * bRec[1] 板子序号 01:1号板 02:2号板 04:3号板
     * bRec[2];这块板子的锁状态 11111110:代表1号锁开启,2到8号锁闭合
     *
     * @param bRec
     */
    void onSingleLockerStatusResponse(byte[] bRec);
}
