package com.oudmon.ble.base.bluetooth.spp;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.HandlerThread;
import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.Executors;

public class SerialSocket implements Runnable {
    private static final UUID BLUETOOTH_SPP_JieLi = UUID.fromString("FE010000-1234-5678-ABCD-00805F9B34FB");
    private SerialListener listener;
    private BluetoothDevice device;
    private BluetoothSocket socket;

    private static SerialSocket serialSocket;

    private SerialSocket(){

    }

    public static SerialSocket getInstance() {
        if (serialSocket == null) {
            synchronized (SerialSocket.class) {
                if (serialSocket == null) {
                    serialSocket = new SerialSocket();
                }
            }
        }
        return serialSocket;
    }


    public void setListener(SerialListener listener) {
        this.listener = listener;
    }

    public void setDevice(BluetoothDevice device) {
        this.device = device;
    }

    /**
     * connect-success and most connect-errors are returned asynchronously to listener
     */
    public void connect(SerialListener listener) {
        this.listener = listener;
        Executors.newSingleThreadExecutor().submit(this);
    }

    public void connect() {
        Executors.newSingleThreadExecutor().submit(this);
    }

    public boolean isConnected() {
        if (socket == null) {
            return false;
        }
        return socket.isConnected();
    }

    public void disconnect() {
        try {
            listener = null; // ignore remaining data and errors
            if (socket != null) {
                try {
                    socket.close();
                } catch (Exception ignored) {
                }
                socket = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void write(byte[] data) {
        try {
//             Log.i(TAG, data.length);
//             Log.i(TAG, ByteUtil.byteArrayToString(data));
            if(socket==null){
                if (listener != null){
                    listener.onSerialConnect();
                }
                return;
            }
            socket.getOutputStream().write(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void write(byte[] data, int off, int len) {
        try {
            socket.getOutputStream().write(data, off, len);
        } catch (IOException e) {
            if (listener != null) {
                listener.onSerialIoError(e);
            }
            e.printStackTrace();
        }
    }

    @SuppressLint({"MissingPermission"})
    @Override
    public void run() { // connect & read
        try {
            socket = device.createRfcommSocketToServiceRecord(BLUETOOTH_SPP_JieLi);
            socket.connect();
            if (listener != null)
                listener.onSerialConnect();
        } catch (Exception e) {
            if (listener != null)
                listener.onSerialConnectError(e);
            try {
                socket.close();
            } catch (Exception ignored) {
            }
            socket = null;
            return;
        }
        try {
            byte[] buffer = new byte[1024];
            int len;
            //noinspection InfiniteLoopStatement
            while (true) {
                len = socket.getInputStream().read(buffer);
                byte[] data = Arrays.copyOf(buffer, len);
                if (listener != null){
                    if(data.length>0){
                        listener.onSerialRead(data);
                    }
                }

            }
        } catch (Exception e) {
            if (listener != null)
                listener.onSerialIoError(e);
            try {
                socket.close();
            } catch (Exception ignored) {
            }
            socket = null;
        }
    }

}
