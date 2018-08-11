package com.example.park.dronecontroller.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;

import com.example.park.dronecontroller.MainActivity;
import com.example.park.dronecontroller.handler.event.MainActivityEvent;

import java.io.IOException;
import java.util.UUID;

public class BluetoothAcceptor extends Thread {
    private final String TAG = getClass().getSimpleName();

    private static final String NAME = "드론 조종기";
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private final BluetoothServerSocket mmServerSocket;

    private MainActivity activity;
    private Handler handler;
    private BluetoothManager bluetoothManager;

    public BluetoothAcceptor(Activity activity) {
        this.activity = (MainActivity) activity;
        this.handler = this.activity.getHandler();

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // Use a temporary object that is later assigned to mmServerSocket,
        // because mmServerSocket is final
        BluetoothServerSocket tmp = null;
        try {
            // MY_UUID is the app's UUID string, also used by the client code
            tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
        } catch (IOException e) {
        }
        mmServerSocket = tmp;
    }

    public void run() {
        showToast("블루투스 서버 시작");

        BluetoothSocket socket;
        // Keep listening until exception occurs or a socket is returned
        while (true) {
            try {
                showToast("블루투스 연결을 기다립니다....");
                socket = mmServerSocket.accept();
            } catch (IOException e) {
                showToast("블루투스 서버 연결을 종료 합니다.");
                break;
            }
            // If a connection was accepted
            if (socket != null) {
                showToast("블루투스를 연결하였습니다.");
                // Do work to manage the connection (in a separate thread)
                manageConnectedSocket(socket);
                try {
                    mmServerSocket.close();
                } catch (IOException e) {
                }
                break;
            }
        }
    }

    /**
     * Will cancel the listening socket, and cause the thread to finish
     */
    public void cancel() {
        try {
            mmServerSocket.close();
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
