package com.linfeng.licamera.service;

import android.util.Log;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * 不用这个，后面重写
 */
public class CharacterResponse {
    @SerializedName("image_id")
    public String imageId;

    @SerializedName("result")
    public Object result;

    @SerializedName("request_id")
    public String requestId;

    @SerializedName("time_used")
    public int timeUsed;

    @SerializedName("error_message")
    public String errorMessage;

    public static class Data {
        @SerializedName("type")
        public String type;

        @SerializedName("value")
        public String value;

        @SerializedName("position")
        public ArrayList<Position> position;

        @SerializedName("child-objects")
        public ArrayList<Data> childObjects;
    }

    public static class Position {
        public int y;
        public int x;
    }

    public void show() {
        ArrayList<Data> results = (ArrayList<Data>) result;
        for (Data data : results) {
            System.out.println(data.value);
        }
        if (errorMessage != null) {
            Log.d("CharacterResponse error", "show:  " + errorMessage);
        }
    }
}
