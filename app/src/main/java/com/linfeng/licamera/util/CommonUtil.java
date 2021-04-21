package com.linfeng.licamera.util;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import androidx.annotation.DimenRes;
import androidx.annotation.StringRes;

import com.linfeng.licamera.LiApplication;

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

  public static int getScreenWidth() {
    DisplayMetrics dm = context().getResources().getDisplayMetrics();
    return dm.widthPixels;
  }

  public static int getScreenHeight() {
    DisplayMetrics dm = context().getResources().getDisplayMetrics();
    return dm.heightPixels;
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


  //Rect相关工具方法

  /**
   * 缩放指定Rect
   */
  public static void scaleRect(RectF rect, float scale) {
    float w = rect.width();
    float h = rect.height();
    float newW = scale * w;
    float newH = scale * h;
    float dx = (newW - w) / 2;
    float dy = (newH - h) / 2;
    rect.left -= dx;
    rect.top -= dy;
    rect.right += dx;
    rect.bottom += dy;
  }

  /**
   * 矩形绕指定点旋转
   */
  public static void rotateRect(RectF rect, float center_x, float center_y, float rotateAngle) {
    float x = rect.centerX();
    float y = rect.centerY();
    float sinA = (float) Math.sin(Math.toRadians(rotateAngle));
    float cosA = (float) Math.cos(Math.toRadians(rotateAngle));
    float newX = center_x + (x - center_x) * cosA - (y - center_y) * sinA;
    float newY = center_y + (y - center_y) * cosA + (x - center_x) * sinA;
    float dx = newX - x;
    float dy = newY - y;
    rect.offset(dx, dy);
  }

  /**
   * 旋转Point点
   */
  public static void rotatePoint(Point p, float center_x, float center_y, float rotateAngle) {
    float sinA = (float) Math.sin(Math.toRadians(rotateAngle));
    float cosA = (float) Math.cos(Math.toRadians(rotateAngle));
    // calc new point
    float newX = center_x + (p.x - center_x) * cosA - (p.y - center_y) * sinA;
    float newY = center_y + (p.y - center_y) * cosA + (p.x - center_x) * sinA;
    p.set((int)newX , (int)newY);
  }


  /**
   * 矩形在Y轴方向上的加法操作
   */
  public static void rectAddV(final RectF srcRect, final RectF addRect, int padding) {
    if (srcRect == null || addRect == null)
      return;
    float left = srcRect.left;
    float top = srcRect.top;
    float right = srcRect.right;
    float bottom = srcRect.bottom;
    if (srcRect.width() <= addRect.width()) {
      right = left + addRect.width();
    }
    bottom += padding + addRect.height();
    srcRect.set(left, top, right, bottom);
  }

  /**
   * 矩形在Y轴方向上的加法操作
   */
  public static void rectAddV(final Rect srcRect, final Rect addRect, int padding , int charMinHeight) {
    if (srcRect == null || addRect == null)
      return;
    int left = srcRect.left;
    int top = srcRect.top;
    int right = srcRect.right;
    int bottom = srcRect.bottom;
    if (srcRect.width() <= addRect.width()) {
      right = left + addRect.width();
    }
    bottom += padding + Math.max(addRect.height(), charMinHeight);
    srcRect.set(left, top, right, bottom);
  }
}
