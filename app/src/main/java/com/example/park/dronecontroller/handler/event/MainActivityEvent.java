package com.example.park.dronecontroller.handler.event;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Message;
import android.widget.Toast;

import com.example.park.dronecontroller.MainActivity;
import com.example.park.dronecontroller.bluetooth.BluetoothManager;

import java.util.HashMap;
import java.util.Map;

public enum MainActivityEvent {
    SHOW_SHORT_TOAST(0) {
        @Override
        public void execute(MainActivity activity, Context context, Message msg) {
            String message = (String) msg.obj;
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }
    },
    SHOW_LONG_TOAST(1) {
        @Override
        public void execute(MainActivity activity, Context context, Message msg) {
            String message = (String) msg.obj;
            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
        }
    },
    BLUETOOTH_CONNECTION_SUCCESS(200) {
        @Override
        public void execute(MainActivity activity, Context context, Message msg) {

        }
    },
    BLUETOOTH_CONNECTION_FAIL(201) {
        @Override
        public void execute(MainActivity activity, Context context, Message msg) {

        }
    },
    BLUETOOTH_CONNECTION_CLOSE(202) {
        @Override
        public void execute(MainActivity activity, Context context, Message msg) {

        }
    },
    SEND(300) {
        @Override
        public void execute(MainActivity activity, Context context, Message msg) {
            BluetoothManager bluetoothManager = activity.getBluetoothManager();
            if (bluetoothManager == null) {
                return;
            }

            if (!bluetoothManager.isConnected()) {
                return;
            }

            String message = (String) msg.obj;
            bluetoothManager.write(message);
        }
    };

    private int status;

    @SuppressLint("UseSparseArrays")
    private static final Map<Integer, MainActivityEvent> mapper = new HashMap<>();

    static {
        for (MainActivityEvent mainActivityEvent : values()) {
            mapper.put(mainActivityEvent.getStatus(), mainActivityEvent);
        }
    }

    MainActivityEvent(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }

    public static MainActivityEvent getEventStatus(int status) {
        return mapper.get(status);
    }

    public abstract void execute(MainActivity activity, Context context, Message msg);
}
