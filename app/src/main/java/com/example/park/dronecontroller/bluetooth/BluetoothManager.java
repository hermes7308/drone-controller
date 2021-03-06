package com.example.park.dronecontroller.bluetooth;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;

import com.example.park.dronecontroller.handler.event.MainActivityEvent;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class BluetoothManager extends Thread {
    private final String TAG = getClass().getSimpleName();

    public static final UUID BT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;

    private Handler handler;

    public BluetoothManager(Handler handler, BluetoothSocket socket) {
        this.handler = handler;

        mmSocket = socket;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        // Get the input and output streams, using temp objects because
        // member streams are final
        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException e) {
        }

        mmInStream = tmpIn;
        mmOutStream = tmpOut;
    }

    public void run() {
        byte[] buffer = new byte[1024];  // buffer store for the stream
        int bytes; // bytes returned from read()

        // Keep listening to the InputStream until an exception occurs
        while (true) {
            try {
                // Read from the InputStream
                bytes = mmInStream.read(buffer);
                String message = new String(buffer, 0, bytes);

                // Send the obtained bytes to the UI activity
                handler.obtainMessage(MainActivityEvent.RECEIVE.getStatus(), message)
                        .sendToTarget();
            } catch (IOException e) {
                handler.obtainMessage(MainActivityEvent.CLOSE_CONNECTION.getStatus())
                        .sendToTarget();
                break;
            }
        }
    }

    public boolean isConnected() {
        return mmSocket.isConnected();
    }

    public void write(String message) {
        write(message.getBytes());
    }

    /* Call this from the main activity to send data to the remote device */
    private void write(byte[] bytes) {
        try {
            mmOutStream.write(bytes);
        } catch (IOException e) {
        }
    }

    /* Call this from the main activity to shutdown the connection */
    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) {
        }
    }

    private void showToast(String message) {
        handler.obtainMessage(MainActivityEvent.SHOW_LONG_TOAST.getStatus(), message)
                .sendToTarget();
    }
}
