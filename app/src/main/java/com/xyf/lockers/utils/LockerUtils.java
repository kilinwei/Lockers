package com.xyf.lockers.utils;

import java.util.ArrayList;
import java.util.List;

import static com.xyf.lockers.common.serialport.LockersCommHelper.LOCKER_COUNT;

/**
 * @项目名： Lockers
 * @包名： com.xyf.lockers.utils
 * @文件名: LockerUtils
 * @创建者: kilin
 * @创建时间: 2019/3/10 23:13
 * @描述： TODO
 */
public class LockerUtils {


    /**
     * 获取所有锁的状态,16进制转2进制,从右到左,第一位为1,然后获取哪一位是1,返回包含1的集合
     *
     * @param allLockers
     * @return
     */
    public static List<Integer> getLockers(int allLockers) {
        List<Integer> list = new ArrayList<>();
        for (int i = 1; i <= LOCKER_COUNT; i++) {
            System.out.println(Long.toBinaryString(allLockers));
            if ((allLockers & 0x01) == 1) {
                list.add(i);
            }
            allLockers >>>= 1;
        }
        System.out.println(list);
        return list;
    }

    /**
     * 是否存满,如果存满返回-1,不满,可以存的话,返回可存的索引
     *
     * @param allLockers
     * @return
     */
    public static int checkCanOpen(int allLockers) {
        if (Integer.bitCount(allLockers) >= LOCKER_COUNT) {
            return -1;
        }
        for (int i = 1; i <= LOCKER_COUNT; i++) {
            if ((allLockers & 0x01) == 0) {
                return i;
            }
            allLockers >>>= 1;
        }
        return -1;
    }
}
