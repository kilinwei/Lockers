package com.xyf.lockers.common.serialport;


import android.util.Log;

import com.xyf.lockers.model.bean.ComRevBean;
import com.xyf.lockers.utils.ProtConvert;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidParameterException;

import android_serialport_api.SerialPort;

/**
 * 串口辅助工具类
 */
public abstract class SerialHelper {
    private static final String TAG = "SerialHelper";
    private SerialPort mSerialPort;
    private OutputStream mOutputStream;
    private InputStream mInputStream;
    private ReadThread mReadThread;
    private String sPort = "/dev/";
    private int iBaudRate = 9600;
    private boolean _isOpen = false;
    private static byte[] sCacheBytes;


    public SerialHelper() {
        this("/dev/", 9600);
    }

    public SerialHelper(String sPort, int iBaudRate) {
        this.sPort = sPort;
        this.iBaudRate = iBaudRate;
    }

    public void open() throws SecurityException, IOException, InvalidParameterException {
        mSerialPort = new SerialPort(new File(sPort), iBaudRate, 0);
        mOutputStream = mSerialPort.getOutputStream();
        mInputStream = mSerialPort.getInputStream();
        mReadThread = new ReadThread();
        mReadThread.start();
        _isOpen = true;
    }

    public void close() {
        if (mReadThread != null) {
            mReadThread.exit();
            mReadThread = null;
        }

        if (mSerialPort != null) {
            mSerialPort.close();
            mSerialPort = null;
        }
        _isOpen = false;
    }

    //----------------------------------------------------
    public int getBaudRate() {
        return iBaudRate;
    }

    public boolean setBaudRate(int iBaud) {
        if (_isOpen) {
            return false;
        } else {
            iBaudRate = iBaud;
            return true;
        }
    }

    public boolean setBaudRate(String sBaud) {
        int iBaud = Integer.parseInt(sBaud);
        return setBaudRate(iBaud);
    }

    //----------------------------------------------------
    public String getPort() {
        return sPort;
    }

    public boolean setPort(String sPort) {
        if (_isOpen) {
            return false;
        } else {
            this.sPort = sPort;
            return true;
        }
    }

    public int send(byte[] bOutArray) {
        int iResult = 0;
        try {
            if (mOutputStream == null) {
                return 0;
            }
            if (!_isOpen) {
                return 0;
            }
            mOutputStream.write(bOutArray);
            iResult = bOutArray.length;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return iResult;
    }

    private class ReadThread extends Thread {

        private boolean isExit = false;

        @Override
        public void run() {
            super.run();
            while (!isExit && !isInterrupted()) {
                Log.i(TAG, "run: ReadThread还存活着");
                try {
                    if (mInputStream == null) {
                        return;
                    }
                    byte[] buffer = new byte[128];
                    int size = mInputStream.read(buffer);
                    if (size > 0) {
                        byte[] bRec = new byte[size];
                        for (int i = 0; i < size; i++) {
                            bRec[i] = buffer[i];
                        }
                        Log.i(TAG, "串口ReadThread收到数据: " + ProtConvert.ByteArrToHex(bRec));
                        appendByte(bRec);
                    }
                    try {
                        sleep(50);//延时50ms
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                    Log.i(TAG, "run: ReadThread已结束");
                    return;
                }
            }
            Log.i(TAG, "run: ReadThread已结束");
        }

        public void exit() {
            Log.i(TAG, "run: ReadThread 执行exit");
            isExit = true;
            interrupt();
        }
    }


    private void appendByte(byte[] bytes) {
        if (sCacheBytes == null) {
            if (bytes.length == 6) {
                ComRevBean ComRecData = new ComRevBean(sPort, bytes, bytes.length);
                Log.i(TAG, "串口收到一条长度为6的完整数据: " + ProtConvert.ByteArrToHex(ComRecData.bRec));
                onDataReceived(ComRecData);
                sCacheBytes = null;
            } else if (bytes.length < 6) {
                String first = ProtConvert.Byte2Hex(bytes[0]);
                if ("6A".equalsIgnoreCase(first) || "8A".equalsIgnoreCase(first)) {
                    sCacheBytes = bytes;
                    Log.i(TAG, "sCacheBytes:首次被赋值: " + ProtConvert.ByteArrToHex(sCacheBytes));
                }else{
                    Log.i(TAG, "串口收到一条无效字符: " + ProtConvert.ByteArrToHex(bytes));
                }
            }
        } else {
            int lenth = sCacheBytes.length + bytes.length;
            byte[] lastBytes = new byte[lenth];
            System.arraycopy(sCacheBytes, 0, lastBytes, 0, sCacheBytes.length);
            System.arraycopy(bytes, 0, lastBytes, sCacheBytes.length, bytes.length);
            sCacheBytes = lastBytes;
            if (lenth == 6) {
                ComRevBean ComRecData = new ComRevBean(sPort, sCacheBytes, lenth);
                Log.i(TAG, "串口拼接数据已拼接为一条完整的数据: " + ProtConvert.ByteArrToHex(sCacheBytes));
                onDataReceived(ComRecData);
                sCacheBytes = null;
            } else if (lenth > 6) {
                //大于六,说明已出错,清空数据
                Log.i(TAG, "串口拼接后的数据长度已大于6,舍弃: " + ProtConvert.ByteArrToHex(sCacheBytes));
                sCacheBytes = null;
            } else {
                Log.i(TAG, "串口拼接中的数据: " + ProtConvert.ByteArrToHex(sCacheBytes));
            }
        }
    }


    //----------------------------------------------------
    public boolean isOpen() {
        return _isOpen;
    }

    //----------------------------------------------------
    protected abstract void onDataReceived(ComRevBean comRecData);
}