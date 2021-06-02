package com.linfeng.licamera.util;

import android.text.TextUtils;

public class StatisticUtil {

    public static void updateUserPhotoCount() {
        String username = SPUtils.getString("userName", "",CommonUtil.context());
        if (!TextUtils.isEmpty(username)) {
            int videoCount = SPUtils.getInt(Constant.PRODUCE_PHOTO_COUNT, 0 ,CommonUtil.context()) + 1;
            SPUtils.putInt(Constant.PRODUCE_PHOTO_COUNT,videoCount,CommonUtil.context());
        }
    }

    public static void updateUserVideoCount() {
        String username = SPUtils.getString("userName", "",CommonUtil.context());
        if (!TextUtils.isEmpty(username)) {
            int videoCount = SPUtils.getInt(Constant.PRODUCE_VIDEO_COUNT, 0 ,CommonUtil.context()) + 1;
            SPUtils.putInt(Constant.PRODUCE_VIDEO_COUNT,videoCount,CommonUtil.context());
        }
    }
}
