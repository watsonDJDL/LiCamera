package com.linfeng.licamera;

public class CameraTabEntity {
    private String mText;
    private int mId;

    public CameraTabEntity(String text, int cameraTabId){
        mText = text;
        mId = cameraTabId;
    }

    public String getText() {
        return mText;
    }

    public int getTabId() {
        return mId;
    }
}
