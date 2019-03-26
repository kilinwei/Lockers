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

    public static void test2() {
        for (int i = 1; i <= 4; i++) {
            int ban = 1 << i - 1;
            for (int j = 1; j <= 8; j++) {
                byte[] pre = {0x5A, (byte) ban, getSendDataConversion(j), 0x00, 0x00};
                String bcc = getBCC(pre);
                System.out.println("第" + i + "块板子,第" + j + "把锁,开锁命令:      5A 0"
                        + Integer.toHexString(ban) + " " + Integer.toHexString((getSendDataConversion(j) & 0x000000FF) | 0xFFFFFF00).substring(6).toUpperCase() + " 00 00 " + bcc);
            }
        }
    }

    private static byte getSendDataConversion(int locker) {
        byte binary = (byte) (1 << (locker - 1));
        //二进制取反,比如00001000变成111110111
        return (byte) ~binary;
    }

    public static void test() {
        long aLong = Long.parseLong("08B001", 16);
        String s = Long.toBinaryString(aLong);
        int count1 = Long.bitCount(aLong);
//        System.out.println("count1: " + count1);
//        System.out.println("二进制: " + s);
//        getStorageIndexs(aLong);
        byte[] bytes = {0x5A, 0x01, (byte) 0xFE, (byte) 0xFE, (byte) 0xFE};
        String bcc = getBCC(bytes);
        int i = Integer.parseInt(bcc, 16);
        byte b = (byte) (i);
        b &= 0xff;
        System.out.println(b);
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
