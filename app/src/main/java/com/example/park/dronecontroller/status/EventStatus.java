package com.example.park.dronecontroller.status;

public enum EventStatus {
    SHOW_TOAST(1);

    private int status;

    EventStatus(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}
