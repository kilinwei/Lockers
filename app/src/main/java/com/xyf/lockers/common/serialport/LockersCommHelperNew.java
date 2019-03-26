package com.xyf.lockers.common.serialport;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.xyf.lockers.listener.OnAllLockersStatusListener;
import com.xyf.lockers.listener.OnSingleLockerStatusListener;
import com.xyf.lockers.model.bean.ComRevBean;
import com.xyf.lockers.model.bean.ComSendBean;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;


/**
 * @项目名： Lockers
 * @包名： com.xyf.lockers.common.serialport
 * @文件名: LockersCommHelper
 * @创建者: kilin
 * @创建时间: 2019/2/23 15:05
 * @描述： TODO
 */
public class LockersCommHelperNew {

    private static final String TAG = "LockersCommHelper";

    public static final int LOCKER_COUNT = 32;

    public static final String SERIAL_DEVICE = "/dev/ttyS3";

    //    public static final int SERIAL_RATE = 9600;
    public static final int SERIAL_RATE = 115200;
    /**
     * 数据接收超时时间 todo 记得把时间回去
     */
    private static final int RECEIVER_DATA_TIMEOUT = 1000 * 1000;

    private static LockersCommHelperNew instance;

    private Handler handler;
    /**
     * 发送的消息包
     */
    private volatile ComSendBean mCurrentSendData = null;
    /**
     * 线程同步锁，消息分发线程，等待返回后再分发。
     */
    private final Object lock = new Object();
    private DispatchQueueThread dispatchQueueThread;
    private LockersSerialHelperNew mSerialHelper;
    /**
     * 连续超时次数
     */
    private volatile int timeoutCount = 0;
    private OnAllLockersStatusListener mOnAllLockersStatusListener;
    private OnSingleLockerStatusListener mOnSingleLockerStatusListener;


    private LockersCommHelperNew() {
    }

    public static LockersCommHelperNew get() {
        if (instance == null) {
            synchronized (LockersCommHelperNew.class) {
                if (instance == null) {
                    instance = new LockersCommHelperNew();
                }
            }
        }
        return instance;
    }

    public void init() {
        handler = new Handler(Looper.getMainLooper());
        dispatchQueueThread = new DispatchQueueThread("TD-dispatch-Thread");
        dispatchQueueThread.start();

        mSerialHelper = new LockersSerialHelperNew(SERIAL_DEVICE, SERIAL_RATE);
        openSerial();
    }

    public void uninit() {
        if (dispatchQueueThread != null) {
            dispatchQueueThread.exit();
            dispatchQueueThread = null;
        }
        closeSerial();
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
            handler = null;
        }
        mSerialHelper = null;
        instance = null;
    }


    private void openSerial() {
        try {
            mSerialHelper.open();
            Log.i(TAG, "openSerial: " + SERIAL_DEVICE + "  串口打开成功");
        } catch (SecurityException e) {
            Log.i(TAG, "openSerial: " + SERIAL_DEVICE + "，打开串口失败:没有串口读/写权限!");
        } catch (IOException e) {
            Log.i(TAG, "openSerial: " + SERIAL_DEVICE + "，打开串口失败:未知错误!");
        } catch (InvalidParameterException e) {
            Log.i(TAG, "openSerial: " + SERIAL_DEVICE + "，打开串口失败:参数错误!");
        }
    }

    public boolean isOpenDev() {
        return mSerialHelper != null && mSerialHelper.isOpen();
    }

    private void closeSerial() {
        if (mSerialHelper != null) {
            mSerialHelper.close();
        }
    }

    /**
     * 分发线程
     */
    private class DispatchQueueThread extends Thread {

        private Queue<ComSendBean> dataQueue = new ConcurrentLinkedQueue<>();

        private boolean isExit = false;

        public DispatchQueueThread(String name) {
            super(name);
        }

        @Override
        public void run() {
            super.run();
            while (!isExit && !isInterrupted()) {
                while ((mCurrentSendData = dataQueue.poll()) != null) {
                    synchronized (lock) {
                        handler.postDelayed(timeoutRunnable, RECEIVER_DATA_TIMEOUT);
                        try {
                            mSerialHelper.send(mCurrentSendData.getSendData());
                            lock.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

        synchronized void addComSendBean(ComSendBean comSendBean) {
            if (isExit) {
                return;
            }
            dataQueue.add(comSendBean);
        }

        void exit() {
            isExit = true;
            handler.removeCallbacks(timeoutRunnable);
            dataQueue.clear();
        }
    }

    /**
     * 超时提醒任务
     */
    private Runnable timeoutRunnable = new Runnable() {
        @Override
        public void run() {
            timeoutCount++;
            Log.e(TAG, "发送指令后，接受数据响应超时，请检查设备！+timeoutCount = " + timeoutCount);
            if (mCurrentSendData != null) {
                // TODO: 2019/2/23 此处可能空指针,因为sendData此时可能已经被赋值null
                int cmd = mCurrentSendData.getCmd();
                switch (cmd) {
                    case LockersCmd.CONTROL_SINGLE_LOCKER:
                        //控制单路
                        Log.i(TAG, "控制单路超时 cmd : " + cmd);
                        break;
                    default:
                        break;
                }
            }
            synchronized (lock) {
                lock.notify();
            }
        }
    };

    private class LockersSerialHelperNew extends SerialHelper {
        private static final long DELAY = 400;
        private DelayResetRunnable mDelayResetRunnable;

        public LockersSerialHelperNew(String sPort, int iBaudRate) {
            super(sPort, iBaudRate);
        }

        @Override
        protected void onDataReceived(ComRevBean comRecData) {
            if (mCurrentSendData == null) {
                Log.i(TAG, "onDataReceived: mCurrentSendData is Null");
                return;
            }
            int cmd = mCurrentSendData.getCmd();
            byte[] bRec = comRecData.bRec;
            switch (cmd) {
                case LockersCmd.CONTROL_SINGLE_LOCKER:
                    if (mOnSingleLockerStatusListener != null) {
                        mOnSingleLockerStatusListener.onSingleLockerStatusResponse(bRec);
                    }
                    reset();
                    break;
                case LockersCmd.QUERY_CIRCUIT_BOARD:
                    if (mDelayResetRunnable != null) {
                        handler.removeCallbacks(mDelayResetRunnable);
                        mDelayResetRunnable = null;
                    }
                    //先走resetRunnable,再走timeoutRunnable
                    mDelayResetRunnable = new DelayResetRunnable();
                    handler.postDelayed(mDelayResetRunnable, DELAY);
                    handler.removeCallbacks(timeoutRunnable);
                    handler.postDelayed(timeoutRunnable, RECEIVER_DATA_TIMEOUT);
                    break;
                case LockersCmd.QUERY_ALL:
                    reset();
                    break;
                case LockersCmd.AUTO_LIGHT:
                    reset();
                    break;
                default:
                    break;
            }
        }

        private void reset() {
            synchronized (lock) {
                timeoutCount = 0;
                if (handler != null && timeoutRunnable != null) {
                    handler.removeCallbacks(timeoutRunnable);
                }
                lock.notify();
            }
        }


        public class DelayResetRunnable implements Runnable {

            @Override
            public void run() {
                reset();
            }
        }

    }

    /**
     * 控制单路
     */
    public void controlSingleLock(byte circuitBoard, byte locker, byte light, byte sensor) {
        if (isOpenDev()) {
            byte[] bytes = {0x5A, circuitBoard, locker, light, sensor};
            String bcc = getBCC(bytes);
            int b = Integer.parseInt(bcc, 16);
            ComSendBean comSendBean = new ComSendBean(LockersCmd.CONTROL_SINGLE_LOCKER, new byte[]{0x5A, circuitBoard, locker, light, sensor, (byte) b});
            dispatchQueueThread.addComSendBean(comSendBean);
        } else {
            Log.w(TAG, "controlSingleLock: 控制单路锁通断: 串口未连接");
        }
    }

    /**
     * 查板地址
     */
    public void queryCircuiBboard() {
        if (isOpenDev()) {
            ComSendBean comSendBean = new ComSendBean(LockersCmd.QUERY_CIRCUIT_BOARD, new byte[]{(byte) 0x8A, 0x01, 0x01, 0x00, 0x00, (byte) 0x8A});
            dispatchQueueThread.addComSendBean(comSendBean);
        } else {
            Log.w(TAG, "queryCircuiBboard: 查板地址: 串口未连接");
        }
    }

    /**
     * 查询全部
     */
    public void queryAll(byte circuitBoard, byte locker, byte light, byte sensor) {
        if (isOpenDev()) {
            byte[] bytes = {(byte) 0x9A, circuitBoard, locker, light, sensor};
            String bcc = getBCC(bytes);
            int b = Integer.parseInt(bcc, 16);
            ComSendBean comSendBean = new ComSendBean(LockersCmd.QUERY_ALL, new byte[]{(byte) 0x9A, circuitBoard, locker, light, sensor, (byte) b});
            dispatchQueueThread.addComSendBean(comSendBean);
        } else {
            Log.w(TAG, "queryAll: 查询全部: 串口未连接");
        }
    }

    /**
     * 开锁自动亮灯，关锁自动灭灯
     */
    public void autoLight(byte circuitBoard, byte locker, byte light, byte sensor) {
        if (isOpenDev()) {
            byte[] bytes = {0x3A, circuitBoard, locker, light, sensor};
            String bcc = getBCC(bytes);
            int b = Integer.parseInt(bcc, 16);
            ComSendBean comSendBean = new ComSendBean(LockersCmd.AUTO_LIGHT, new byte[]{(byte) 0x3A, circuitBoard, locker, light, sensor, (byte) b});
            dispatchQueueThread.addComSendBean(comSendBean);
        } else {
            Log.w(TAG, "queryAll: 开锁自动亮灯，关锁自动灭灯: 串口未连接");
        }
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


    public static class LockersCmd {

        public static final byte CONTROL_SINGLE_LOCKER = 0x01;

        public static final byte QUERY_CIRCUIT_BOARD = 0x02;

        public static final byte QUERY_ALL = 0x03;

        public static final byte AUTO_LIGHT = 0x04;


    }

    public void setOnAllLockersStatusListener(OnAllLockersStatusListener listener) {
        mOnAllLockersStatusListener = listener;
    }

    public void setOnSingleLockerStatusListener(OnSingleLockerStatusListener listener) {
        mOnSingleLockerStatusListener = listener;
    }
}
