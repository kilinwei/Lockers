package com.xyf.lockers.utils;

import java.util.ArrayList;
import java.util.List;

import static com.xyf.lockers.common.serialport.LockersCommHelper.LOCKER_COUNT;

/**
 * @项目名： Lockers
 * @包名： com.xyf.lockers.utils
 * @文件名: TestUtil
 * @创建者: kilin
 * @创建时间: 2019/3/10 0:22
 * @描述： TODO
 */
public class TestUtil {

    public static void main(String[] args) {
        long aLong = Long.parseLong("08B001", 16);
        String s = Long.toBinaryString(aLong);
        int count1 = Long.bitCount(aLong);
//        System.out.println("count1: " + count1);
//        System.out.println("二进制: " + s);
//        getStorageIndexs(aLong);
        System.out.println(Integer.toBinaryString(getSendData(5)));
    }

    /**
     * 将十进制的数据,转化为发送的数据
     *
     * @param locker
     */
    private static byte getSendData(int locker) {
        byte binary = (byte) (1 << (locker - 1));
        //二进制取反,比如00001000变成111110111
        return (byte) ~binary;
    }

    /**
     * 是否存满,如果存满返回-1
     *
     * @param aLong
     * @return
     */
    public static long checkCanOpen(long aLong) {
        if (Long.bitCount(aLong) >= LOCKER_COUNT) {
            return -1;
        }
        for (int i = 1; i <= LOCKER_COUNT; i++) {
            if ((aLong & 0x01) == 0) {
                return i;
            }
            aLong >>>= 1;
        }
        return -1;
    }

    public static List<Integer> getLockers(long aLong) {
        List<Integer> list = new ArrayList<>();
        for (int i = 1; i <= 24; i++) {
            System.out.println(Long.toBinaryString(aLong));
            if ((aLong & 0x01) == 1) {
                list.add(i);
            }
            aLong >>>= 1;
        }
        System.out.println(list);
        return list;
    }

    public static void getBitCount(long aLong) {
        int count = 0;
        while (aLong != 0) {
            if ((aLong & 0x01) == 1) {//检测当前位是否为1
                count++;
            }
            aLong >>>= 1;
        }
        System.out.println("count2: " + count);
    }
}
