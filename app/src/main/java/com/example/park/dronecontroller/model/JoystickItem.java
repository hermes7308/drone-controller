package com.example.park.dronecontroller.model;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

public class JoystickItem {
    private String from;
    private int angle;
    private int power;
    private int direction;

    public JoystickItem(String from, int angle, int power, int direction) {
        this.from = from;
        this.angle = angle;
        this.power = power;
        this.direction = direction;
    }

    public String getFrom() {
        return from;
    }

    public int getAngle() {
        return angle;
    }

    public int getPower() {
        return power;
    }

    public int getDirection() {
        return direction;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }
}
