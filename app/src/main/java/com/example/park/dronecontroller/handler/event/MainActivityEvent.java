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
    PRINT_MESSAGE(-1) {
        @Override
        public void execute(MainActivity activity, Context context, Message msg) {
            /* 로그 내용 추가 */
            activity.printMessage((String) msg.obj);
        }
    },
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
    CANCEL_BLUETOOTH_DIALOG(2) {
        @Override
        public void execute(MainActivity activity, Context context, Message msg) {
            activity.cancelSearchBluetoothDialog();
        }
    },
    CONNECT(100) {
        @Override
        public void execute(MainActivity activity, Context context, Message msg) {
            activity.printMessage("블루투스를 연결하였습니다.");
            activity.setBluetoothManager((BluetoothManager) msg.obj);
        }
    },
    SEND(300) {
        @Override
        public void execute(MainActivity activity, Context context, Message msg) {
            String message = (String) msg.obj;

            activity.printMessage(message);
            activity.send(message);
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
