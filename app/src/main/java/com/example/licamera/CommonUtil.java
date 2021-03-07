package com.example.licamera;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import androidx.annotation.DimenRes;
import androidx.annotation.StringRes;

public class CommonUtil {
  private static int sScreenLongAxis = 0;
  private static int sScreenShortAxis = 0;

  public static int getScreenShortAxis() {
    if (sScreenShortAxis == 0) {
      Context context = context();
      WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
      DisplayMetrics displayMetrics = new DisplayMetrics();
      wm.getDefaultDisplay().getMetrics(displayMetrics);
      sScreenShortAxis = Math.min(displayMetrics.widthPixels, displayMetrics.heightPixels);
    }
    return sScreenShortAxis;
  }

  public static int getScreenLongAxis() {
    if (sScreenLongAxis == 0) {
      Context context = context();
      WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
      DisplayMetrics displayMetrics = new DisplayMetrics();
      wm.getDefaultDisplay().getMetrics(displayMetrics);
      sScreenLongAxis = Math.max(displayMetrics.widthPixels, displayMetrics.heightPixels);
    }
    return sScreenLongAxis;
  }

  public static Resources res() {
    return context().getResources();
  }

  public static int dip2px(float dip) {
    return dip2px(context(), dip);
  }

  public static int dimen(@DimenRes int res) {
    return context().getResources().getDimensionPixelOffset(res);
  }

  public static String string(@StringRes int res) {
    return context().getResources().getString(res);
  }

  public static String string(@StringRes int res, int number) {
    return context().getResources().getString(res, number);
  }

  public static String string(@StringRes int res, String label) {
    return context().getResources().getString(res, label);
  }


  /**
   * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
   */
  public static int dip2px(Context context, float dpValue) {
    final float scale = context.getResources().getDisplayMetrics().density;
    return (int) (dpValue * scale + 0.5f);
  }

  public static int px2dip(Context context, float pxValue) {
    final float scale = context.getResources().getDisplayMetrics().density;
    return (int) (pxValue / scale + 0.5f);
  }

  public static int sp2px(Context context, float spValue) {
    final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
    return (int) (spValue * fontScale + 0.5f);
  }

  public static Context context() {
    return LiApplication.getContext();
  }

}
