package com.xyf.lockers.common.serialport;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.xyf.lockers.model.bean.ComRevBean;
import com.xyf.lockers.model.bean.ComSendBean;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static com.xyf.lockers.common.serialport.LockersCommHelper.LockersCmd.CONTROL_SINGLE_WAY;

/**
 * @项目名： Lockers
 * @包名： com.xyf.lockers.common.serialport
 * @文件名: LockersCommHelper
 * @创建者: kilin
 * @创建时间: 2019/2/23 15:05
 * @描述： TODO
 */
public class LockersCommHelper {

    private static final String TAG = "LockersCommHelper";

    public static final String SERIAL_DEVICE = "/dev/ttyS3";

    //    public static final int SERIAL_RATE = 9600;
    public static final int SERIAL_RATE = 115200;
    /**
     * 数据接收超时时间
     */
    private static final int RECEIVER_DATA_TIMEOUT = 1000*1000;

    private static LockersCommHelper instance;

    private Handler handler;
    /**
     * 发送的消息包
     */
    private volatile ComSendBean sendData = null;
    /**
     * 线程同步锁，消息分发线程，等待返回后再分发。
     */
    private final Object lock = new Object();
    private DispatchQueueThread dispatchQueueThread;
    private LockersSerialHelper mSerialHelper;
    /**
     * 连续超时次数
     */
    private volatile int timeoutCount = 0;

    private LockersCommHelper() {
    }

    public static LockersCommHelper get() {
        if (instance == null) {
            synchronized (LockersCommHelper.class) {
                if (instance == null) {
                    instance = new LockersCommHelper();
                }
            }
        }
        return instance;
    }

    public void init() {
        handler = new Handler(Looper.getMainLooper());
        dispatchQueueThread = new DispatchQueueThread("TD-dispatch-Thread");
        dispatchQueueThread.start();

        mSerialHelper = new LockersSerialHelper(SERIAL_DEVICE, SERIAL_RATE);
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
                while ((sendData = dataQueue.poll()) != null) {
                    synchronized (lock) {
                        handler.postDelayed(timeoutRunnable, RECEIVER_DATA_TIMEOUT);
                        try {
                            mSerialHelper.send(sendData.getSendData());
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
            if (sendData != null) {
                // TODO: 2019/2/23 此处可能空指针,因为sendData此时可能已经被赋值null
                int cmd = sendData.getCmd();
                switch (cmd) {
                    case CONTROL_SINGLE_WAY:
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

    private class LockersSerialHelper extends SerialHelper {

        public LockersSerialHelper(String sPort, int iBaudRate) {
            super(sPort, iBaudRate);
        }

        @Override
        protected void onDataReceived(ComRevBean comRecData) {
            if (sendData == null) {
                Log.i(TAG, "onDataReceived: sendData is Null");
                return;
            }
            int cmd = sendData.getCmd();
            switch (cmd) {
                case CONTROL_SINGLE_WAY:
                    byte[] bRec = comRecData.bRec;
                    int way = bRec[0];
                    int status = bRec[1];
                    Log.i(TAG, "onDataReceived: way: " + way + "  status: " + status);
                    break;
                default:
                    break;
            }
            reset();
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
    }

    /**
     * 控制单路通断
     *
     * @param way   线路,十进制
     * @param staus 01通,00断
     */
    public void controlSingleLock(int way, int staus) {
        if (isOpenDev()) {
            //ab 02 02 01 ba
            String wayHex = Integer.toHexString(way);
            ComSendBean comSendBean = new ComSendBean(CONTROL_SINGLE_WAY, new byte[]{(byte) 0xAB, 0x02, (byte) way, (byte) staus, (byte) 0xBA});
            dispatchQueueThread.addComSendBean(comSendBean);
            Log.i(TAG, "controlSingleLock: 控制单路通断: way: " + way + "  wayHex: " + wayHex + " status: " + staus);
        } else {
            Log.w(TAG, "controlSingleLock: 控制单路通断: way: " + way + " status: " + staus);
        }
    }

    public class LockersCmd {

        public static final byte CONTROL_SINGLE_WAY = 0x01;



    }
}
