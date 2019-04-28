package com.xyf.lockers.listener;

/**
 * @项目名： Lockers
 * @包名： com.xyf.lockers.listener
 * @文件名: BaseSerialportInterface
 * @创建者: kilin
 * @创建时间: 2019/3/10 22:47
 * @描述： TODO
 */
public interface BaseSerialportInterface {
    void disConnectDevice();

    /**
     * 串口返回超时
     */
    void onResponseTime();
}
