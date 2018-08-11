package com.example.park.dronecontroller.util;

import com.google.gson.Gson;

public class GsonFactory {
    private static final Gson gson = new Gson();

    private GsonFactory() {

    }

    public static Gson get() {
        return gson;
    }
}
