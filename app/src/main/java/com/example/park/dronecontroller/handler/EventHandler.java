package com.example.park.dronecontroller.handler;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.example.park.dronecontroller.MainActivity;
import com.example.park.dronecontroller.handler.event.MainActivityEvent;

public class EventHandler extends Handler {
    private MainActivity activity;
    private Context context;

    public EventHandler(MainActivity activity, Context context) {
        this.activity = activity;
        this.context = context;
    }

    @Override
    public void handleMessage(Message msg) {
        /* 해당 이벤트 실행 */
        MainActivityEvent event = MainActivityEvent.getEventStatus(msg.what);
        if (event == null) {
            throw new IllegalArgumentException("이벤트를 찾을 수 없습니다. status no : " + msg.what);
        }

        event.execute(activity, context, msg);
    }
}
