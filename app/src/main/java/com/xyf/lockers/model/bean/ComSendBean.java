package com.xyf.lockers.model.bean;

/**
 * Created by Administrator on 2018/7/7.
 */

public class ComSendBean {
    private byte cmd;
    private byte[] sendData;

    public ComSendBean(byte cmd, byte[] sendData) {
        this.cmd = cmd;
        this.sendData = sendData;
    }

    public byte getCmd() {
        return cmd;
    }

    public void setCmd(byte cmd) {
        this.cmd = cmd;
    }

    public byte[] getSendData() {
        return sendData;
    }

    public void setSendData(byte[] sendData) {
        this.sendData = sendData;
    }
}
