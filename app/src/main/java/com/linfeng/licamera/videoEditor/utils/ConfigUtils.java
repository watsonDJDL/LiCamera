package com.linfeng.licamera.videoEditor.utils;

import android.os.Environment;

import com.linfeng.licamera.videoEditor.filter.MagicFilterType;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;


public class ConfigUtils {

    private static ConfigUtils sInstance;
    private MagicFilterType mMagicFilterType = MagicFilterType.NONE;
    private String mOutPutFilterVideoPath;

    private ConfigUtils() {

    }

    public static ConfigUtils getInstance() {
        if (sInstance == null) {
            synchronized (ConfigUtils.class) {
                if (sInstance == null) {
                    sInstance = new ConfigUtils();
                }
            }
        }
        return sInstance;
    }

    public void setMagicFilterType(MagicFilterType type) {
        mMagicFilterType = type;
    }

    public MagicFilterType getMagicFilterType() {
        return mMagicFilterType;
    }

    public String getOutPutFilterVideoPath() {
        return getAndroidMoviesFolder().getAbsolutePath() + "/" + new SimpleDateFormat(
                "yyyyMM_dd-HHmmss").format(new Date()) + "filter-effect.mp4";
    }

    public File getAndroidMoviesFolder() {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
    }
}

