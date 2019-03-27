package com.xyf.lockers.common.serialport;


import android.util.Log;

import com.xyf.lockers.model.bean.ComRevBean;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidParameterException;
import java.util.Arrays;

import android_serialport_api.SerialPort;

/**
 * 串口辅助工具类
 */
public abstract class SerialHelper {
    private SerialPort mSerialPort;
    private OutputStream mOutputStream;
    private InputStream mInputStream;
    private ReadThread mReadThread;
    private String sPort = "/dev/";
    private int iBaudRate = 9600;
    private boolean _isOpen = false;

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
                try {
                    if (mInputStream == null) {
                        return;
                    }
                    byte[] buffer = new byte[128];
                    int size = mInputStream.read(buffer);
                    if (size > 0) {
                        ComRevBean ComRecData = new ComRevBean(sPort, buffer, size);
                        Log.i("kilin", "run: buffer: "+ Arrays.asList(buffer));
                        onDataReceived(ComRecData);
                    }
                    try {
                        sleep(80);//延时80ms
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                    return;
                }
            }
        }

        public void exit() {
            isExit = true;
        }
    }


    //----------------------------------------------------
    public boolean isOpen() {
        return _isOpen;
    }

    //----------------------------------------------------
    protected abstract void onDataReceived(ComRevBean comRecData);
}