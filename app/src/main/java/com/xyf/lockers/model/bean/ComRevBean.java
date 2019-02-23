package com.xyf.lockers.model.bean;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class ComRevBean {
    public byte[] bRec = null;
    public String sRecTime = "";
    public String sComPort = "";
    public int length;

    public ComRevBean(String sPort, byte[] buffer, int size) {
        sComPort = sPort;
        bRec = new byte[size];
        length = size;
        for (int i = 0; i < size; i++) {
            bRec[i] = buffer[i];
        }
        SimpleDateFormat sDateFormat = new SimpleDateFormat("hh:mm:ss", Locale.CHINA);
        sRecTime = sDateFormat.format(new java.util.Date());
    }
}