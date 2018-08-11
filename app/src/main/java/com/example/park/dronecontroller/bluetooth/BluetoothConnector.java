package com.example.park.dronecontroller.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

import com.example.park.dronecontroller.MainActivity;
import com.example.park.dronecontroller.handler.event.MainActivityEvent;

import java.io.IOException;
import java.util.UUID;

public class BluetoothConnector extends Thread {
    private final String TAG = getClass().getSimpleName();

    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private final BluetoothSocket mmSocket;
    private final BluetoothDevice mmDevice;

    private MainActivity activity;
    private Handler handler;
    private BluetoothManager bluetoothManager;

    public BluetoothConnector(Activity activity, BluetoothDevice device) {
        this.activity = (MainActivity) activity;
        this.handler = this.activity.getHandler();

        // Use a temporary object that is later assigned to mmSocket,
        // because mmSocket is final
        BluetoothSocket tmp = null;
        mmDevice = device;

        // Get a BluetoothSocket to connect with the given BluetoothDevice
        try {
            // MY_UUID is the app's UUID string, also used by the server code
            tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) {
        }
        mmSocket = tmp;
    }

    public void run() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // Cancel discovery because it will slow down the connection
        mBluetoothAdapter.cancelDiscovery();

        try {
            // Connect the device through the socket. This will block
            // until it succeeds or throws an exception
            mmSocket.connect();
        } catch (IOException connectException) {
            // Unable to connect; close the socket and get out
            showToast("블루투스 연결이 종료 되었습니다.");
            Log.e(TAG, "블루투스 연결이 종료 되었습니다.", connectException);
            try {
                mmSocket.close();
            } catch (IOException closeException) {
            }
            return;
        }

        showToast("블루투스를 연결하였습니다.");
        // Do work to manage the connection (in a separate thread)
        manageConnectedSocket(mmSocket);
    }

    /**
     * Will cancel an in-progress connection, and close the socket
     */
    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) {
        }
    }

    private void manageConnectedSocket(BluetoothSocket bluetoothSocket) {
        bluetoothManager = new BluetoothManager(bluetoothSocket, activity.getHandler());
        bluetoothManager.start();

        activity.setBluetoothManager(bluetoothManager);
    }

    private void showToast(String message) {
        handler.obtainMessage(MainActivityEvent.SHOW_LONG_TOAST.getStatus(), message)
                .sendToTarget();
    }
}
