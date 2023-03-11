package com.example.bluetooth;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class ChatUtils extends MainActivity{
    private Context context;
    private final Handler handler;
    private BluetoothAdapter bluetoothAdapter;
    private final int BLUETOOTH_PERMISSON = 99;

    private connectThread connectThread;
    private AcceptThread acceptThread;
    private ConnectedThread connectedThread;

    private final UUID APP_UUID = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
    private final String APP_NAME = "Bluetooth";

    public static final int STATE_NONE = 0;
    public static final int STATE_LISTEN = 1;
    public static final int STATE_CONNECTING = 2;
    public static final int STATE_CONNECTED = 3;

    private int state;

    public ChatUtils (Context context, Handler handler) {
        this.context = context;
        this.handler = handler;

        state = STATE_NONE;
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public int getState() {
        return state;
    }

    public synchronized void setState(int state) {
        this.state = state;
        handler.obtainMessage(MainActivity.MESSAGE_STATE_CHANGED, state, -1).sendToTarget();
    }

    public synchronized void start() {
        if (connectThread != null) {
            connectThread.cancel();
            connectThread = null;
        }
        if (acceptThread == null) {
            acceptThread = new AcceptThread();
            acceptThread.start();
        }
        if(connectedThread!= null){
            connectedThread.cancel();
            connectedThread = null;
        }
        setState(STATE_LISTEN);
    }

    public synchronized void stop() {
        if (connectThread != null) {
            connectThread.cancel();
            connectThread = null;
        }
        if (acceptThread != null) {
            acceptThread.cancel();
            acceptThread = null;
        }if(connectedThread!= null){
            connectedThread.cancel();
            connectedThread = null;
        }
        setState(STATE_NONE);

    }

    public void connect(BluetoothDevice device){
        if(state == STATE_CONNECTING){
            connectThread.cancel();
            connectThread = null;
        }
        connectThread = new connectThread(device);
        connectThread.start();
        if(connectedThread!= null){
            connectedThread.cancel();
            connectedThread = null;
        }

        setState(STATE_CONNECTING);
    }

    public void write(byte[] buffer){
        ConnectedThread connThread;
        synchronized (this){
            if(state != STATE_CONNECTED){
                return;
            }
            connThread = connectedThread;
        }
        connThread.write(buffer);
    }


    private class AcceptThread extends Thread {
        private BluetoothServerSocket serverSocket;

        public AcceptThread() {
            BluetoothServerSocket tmp = null;
            try {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                tmp = bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(APP_NAME, APP_UUID);
            }catch (IOException e){
                Log.e("Accept->Constructor", e.toString());
            }

            serverSocket = tmp;
        }
        public void run(){
            BluetoothSocket socket = null;
            try {
               socket = serverSocket.accept();
            }catch (IOException e){
                Log.e("Accept->Run",e.toString());
                try {
                    serverSocket.close();
                }catch (IOException e1){
                    Log.e("Accept->Close",e1.toString());
                }
            }
            if(socket!= null){
                switch (state){
                    case STATE_LISTEN:
                    case STATE_CONNECTING:
                        connect(socket.getRemoteDevice());
                        break;
                    case STATE_NONE:
                    case STATE_CONNECTED:
                        try {
                            socket.close();
                        }catch (IOException e){
                            Log.e("Accept->CloseSocket",e.toString());
                        }
                        break;
                }
            }
        }


        public void cancel(){
            try{
                serverSocket.close();
            }catch (IOException e){
                Log.e("Accept->CloseServer", e.toString());

            }
        }
    }
    private class ConnectedThread extends Thread{
        private final BluetoothSocket socket;
        private final InputStream inputStream;
        private final OutputStream outputStream;


        public ConnectedThread(BluetoothSocket socket){
            this.socket = socket;

            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            }catch (IOException e){
            }

            inputStream = tmpIn;
            outputStream = tmpOut;

        }


        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;

            try {
                bytes = inputStream.read(buffer);

                handler.obtainMessage(MainActivity.MESSAGE_READ, bytes,-1,buffer).sendToTarget();
            }catch (IOException e){
                connectonLost();

            }
        }
        public void write(byte[] buffer){
            try {
                outputStream.write(buffer);
                handler.obtainMessage(MainActivity.MESSAGE_WRITE,-1,-1, buffer).sendToTarget();
            }catch (IOException e){

            }
        }

        public void cancel(){
            try {
                socket.close();
            }catch (IOException e){

            }

        }
    }
    private void connectonLost(){
        Message message = handler.obtainMessage(MainActivity.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(MainActivity.TOAST, " Connection lost");
        message.setData(bundle);
        handler.sendMessage(message);

        ChatUtils.this.start();
    }

    private class connectThread extends Thread {
        private BluetoothSocket socket;
        private final BluetoothDevice device;

        public connectThread(BluetoothDevice device) {
            this.device = device;

            BluetoothSocket tmp = null;
            try {

                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(ChatUtils.this,
                            Manifest.permission.BLUETOOTH_CONNECT)) {
                        new AlertDialog.Builder(context)
                                .setTitle("Bluetooth permission")
                                .setMessage("This app needs bluetooth permission please grant")
                                .setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                        ActivityCompat.requestPermissions(ChatUtils.this,
                                                new String[]{Manifest.permission.BLUETOOTH_CONNECT},
                                                BLUETOOTH_PERMISSON);
                                    }
                                })
                                .create()
                                .show();


                    } else {

                        ActivityCompat.requestPermissions(ChatUtils.this,
                                new String[]{Manifest.permission.BLUETOOTH_CONNECT},
                                BLUETOOTH_PERMISSON);
                    }
                    return;
                }
                tmp = device.createRfcommSocketToServiceRecord(APP_UUID);
            } catch (IOException e) {
                Log.e("Connect->Constructor", e.toString());
            }
            socket = tmp;
        }

        public void run() {
            try {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                socket.connect();
            } catch (IOException e) {
                Log.e("Connect->Run", e.toString());
                try {
                    socket.close();
                } catch (IOException e1) {
                    Log.e("Connect->CloseSocket", e.toString());
                }
                connectionFailed();
                return;
            }

            synchronized (ChatUtils.this) {
                connectThread = null;
            }
            connected(socket,device);
        }

        public void cancel() {
            try {
                socket.close();
            } catch (IOException e) {
                Log.e("Connect->Cancel", e.toString());
            }
        }
    }

    private synchronized void connectionFailed() {
        Message msg = handler.obtainMessage(MainActivity.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(MainActivity.TOAST, "Can't connect to the device");
        msg.setData(bundle);
        handler.sendMessage(msg);

        ChatUtils.this.start();
    }

    private synchronized void connected(BluetoothSocket socket,BluetoothDevice device) {
        if (connectThread != null) {
            connectThread.cancel();
            connectThread = null;
        }
        if(connectedThread!= null){
            connectedThread.cancel();
            connectedThread = null;
        }
        connectedThread = new ConnectedThread(socket);
        connectedThread.start();

        Message msg = handler.obtainMessage(MainActivity.MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        bundle.putString(MainActivity.DEVICE_NAME, device.getName());
        msg.setData(bundle);
        handler.sendMessage(msg);

        setState(STATE_CONNECTED);
    }
}

