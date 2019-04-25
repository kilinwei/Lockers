package com.xyf.lockers.utils;

import java.util.ArrayList;
import java.util.Collections;
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

    public static final int OPEN_LOCKER_INTEVAL = 1000;

    /**
     * 返回32位二进制第几位是1的集合,从右到左,第一位为0,然后获取哪一位是1,返回包含1的索引的集合,例如5返回的集合为{0,2}
     *
     * @param storageIndexs
     * @return
     */
    public static List<Integer> getStorageIndexs(long storageIndexs) {
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < LOCKER_COUNT; i++) {
            if ((storageIndexs & 0x01) == 1) {
                list.add(i);
            }
            storageIndexs >>>= 1;
        }
        System.out.println(list);
        return list;
    }

    /**
     * 获取8bit中哪一位是0
     *
     * @param b
     * @return
     */
    public static List<Integer> getOpeningLocker(byte b) {
        List<Integer> list = new ArrayList<>();
        byte help = 0x01;
        for (int i = 0; i < 8; i++) {
            if ((b & help) == 0) {
                list.add(i);
            }
            help = (byte) (help << 1);
        }
        return list;
    }

    public static ArrayList<Integer> getOpeningLockesIndexs(int boardBinary, byte lockerBinary) {
        ArrayList<Integer> openLockers = null;
        List<Integer> openingLocker = LockerUtils.getOpeningLocker(lockerBinary);
        if (openingLocker != null && !openingLocker.isEmpty()) {
            Object[] objects = openingLocker.toArray();
            if (objects != null) {
                Integer[] integers = new Integer[objects.length];
                for (int i = 0; i < objects.length; i++) {
                    integers[i] = (Integer) objects[i];
                }
                while (boardBinary > 1) {
                    boardBinary = boardBinary >>> 1;
                    for (int i = 0; i < openingLocker.size(); i++) {
                        integers[i] = integers[i] + 8;
                    }
                }
                openLockers = new ArrayList<>(integers.length);
                Collections.addAll(openLockers, integers);
//                System.out.println(Arrays.toString(integers));
            }

        }
        return openLockers;
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
        for (int i = 0; i < LOCKER_COUNT; i++) {
            if ((allLockers & 0x01) == 0) {
                return i;
            }
            allLockers >>>= 1;
        }
        return -1;
    }


    public static byte[] getOpenSingleLockerBytes(int way) {
        byte[] bytes = new byte[4];
        if (way < 8) {
            bytes[0] = 0x01;
            bytes[1] = getSendDataConversion(way);
        } else if (8 <= way && way < 16) {
            bytes[0] = 0x02;
            bytes[1] = getSendDataConversion(way - 8);
        } else if (16 <= way && way < 24) {
            bytes[1] = getSendDataConversion(way - 16);
            bytes[0] = 0x04;
        } else if (24 <= way && way < 32) {
            bytes[1] = getSendDataConversion(way - 24);
            bytes[0] = 0x08;
        }
        bytes[2] = 0;
        bytes[3] = 0;
        return bytes;
    }

    /**
     * 将十进制的数据,取反为发送的数据
     *
     * @param locker
     */
    public static byte getSendDataConversion(int locker) {
        byte binary = (byte) (1 << locker);
        //二进制取反,比如00001000变成111110111
        return (byte) ~binary;
    }
}
