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
        getOpeningLocker((byte) 254);
    }

    public static List<Integer> getStorageIndexs(long storageIndexs) {
        List<Integer> list = new ArrayList<>();
        for (int i = 1; i <= LOCKER_COUNT; i++) {
            if ((storageIndexs & 0x01) == 1) {
                list.add(i);
            }
            storageIndexs >>>= 1;
        }
        System.out.println(list);
        return list;
    }


    public static List<Integer> getOpeningLocker(byte b) {
        List<Integer> list = new ArrayList<>();
        int help = 0x01;
        for (int i = 1; i <= 8; i++) {
            System.out.println(Integer.toBinaryString(b));
            System.out.println(Integer.toBinaryString(help));
            if ((b & help) == 0) {
                list.add(i);
            }
            help = (help << 1);
        }
        System.out.println(list);
        return list;
    }


    public static String getBCC(byte[] data) {
        String ret = "";
        byte BCC[] = new byte[1];
        for (int i = 0; i < data.length; i++) {
            BCC[0] = (byte) (BCC[0] ^ data[i]);
        }
        String hex = Integer.toHexString(BCC[0] & 0xFF);
        if (hex.length() == 1) {
            hex = '0' + hex;
        }
        ret += hex.toUpperCase();
        return ret;
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
        for (int i = 0; i < LOCKER_COUNT; i++) {
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
