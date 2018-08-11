package com.example.park.dronecontroller.handler;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ScrollView;

import com.example.park.dronecontroller.MainActivity;
import com.example.park.dronecontroller.R;
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
        /* 로그 내용 추가 */
        activity.addLog((String) msg.obj);

        /* 해당 이벤트 실행 */
        MainActivityEvent.getEventStatus(msg.what)
                .execute(activity, context, msg);
    }
}
