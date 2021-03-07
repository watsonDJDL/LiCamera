package com.example.licamera.Camera;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.animation.Interpolator;
import androidx.annotation.NonNull;

import com.example.licamera.CommonUtil;
import com.example.licamera.R;

public class FocusView extends View implements Runnable {
  public static final float CIRCLE_RADIUS = CommonUtil.dip2px(80); // 对焦环直径
  public static final int LINE_LENGTH = CommonUtil.dip2px(56); // 曝光直线
  public static final int LIGHT_WIDTH = CommonUtil.dip2px(22); // 曝光图标宽度
  public static final int LIGHT_LINE_AREA_OFFSET = CommonUtil.dip2px(10); // 调光条的边界缓冲偏移

  public static final int DISMISS_INTERVAL_TIME = 1000;
  public static final int FADE_INTERVAL_TIME = 2000;
  ValueAnimator mAlphaAni;
  ValueAnimator mScaleAni;
  private Rect mCircleArea;
  private Rect mLightArea;
  private Rect mLineArea;

  private Drawable mCircleDrawable;
  private Drawable mLightDrawable;
  private Paint mLinePaint;
  public boolean mIsShowing;
  public boolean mIsLineFading;
  public boolean mIsFading;
  public boolean mIsRefresh;


  public FocusView(@NonNull Context context) {
    super(context);
  }

  @Override
  public void run() {
    removeCallbacks(this);
    if (mCircleArea == null || !isShowing()) {
      return;
    }
    if (mIsLineFading) {
      mIsLineFading = false;
      //changeLineAlpha(true);
      postDelayed(this, DISMISS_INTERVAL_TIME);
      return;
    }
    if (!mIsFading) {
      mIsFading = true;
      mIsRefresh = false;
      mAlphaAni = ValueAnimator.ofInt(255, 125);
      mAlphaAni.setInterpolator(new QuadEaseOutInterpolator());
      mAlphaAni.setDuration(200);
      mAlphaAni.addUpdateListener(animation -> postInvalidate());
      mAlphaAni.start();
      postDelayed(this, FADE_INTERVAL_TIME);
    } else {
      dismiss();
    }
  }

  public boolean isShowing() {
    return mIsShowing;
  }

  public void dismiss() {
    reset();
    invalidate();
  }

  private void reset() {
    mIsFading = false;
    mCircleArea = null;
    mIsShowing = false;
    mIsRefresh = false;
    mIsLineFading = false;
    //mLineAlphaAni = null;
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    if (mCircleArea == null || mLightArea == null) {
      return;
    }
    if (mCircleDrawable == null || mLightDrawable == null) {
      mCircleDrawable = getResources().getDrawable(R.drawable.switch_camera);
    }
    resizeCircleArea();

    canvas.save();

    int alpha = mIsRefresh ? 255 : (Integer) mAlphaAni.getAnimatedValue();

    mCircleDrawable.setAlpha(alpha);
    mCircleDrawable.setBounds(mCircleArea);
    mCircleDrawable.draw(canvas);
  }

  private void resizeCircleArea() {
    int centerX = mCircleArea.centerX();
    int centerY = mCircleArea.centerY();
    int newLength = (int) ((CIRCLE_RADIUS * (float) mScaleAni.getAnimatedValue()));

    mCircleArea.left = centerX - newLength / 2;
    mCircleArea.top = centerY - newLength / 2;
    mCircleArea.right = mCircleArea.left + newLength;
    mCircleArea.bottom = mCircleArea.top + newLength;
  }

  private void setCircleArea(Rect circleArea) {
    mCircleArea = circleArea;
    mLightArea = new Rect();
    mLineArea = new Rect();

    mIsShowing = true;
    mIsFading = false;
    mIsLineFading = false;
  }

  public void startFocus(Rect rect) {
    boolean isLightShowLeft = rect.right + FocusView.LIGHT_WIDTH > CommonUtil.getScreenShortAxis();
    setCircleArea(rect);
    mAlphaAni = ValueAnimator.ofInt(255, 205, 255);
    mAlphaAni.setInterpolator(new QuadEaseOutInterpolator());
    mAlphaAni.setDuration(100);

    mScaleAni = ValueAnimator.ofFloat(2, 1);
    mScaleAni.setInterpolator(new CubicEaseOutInterpolator());
    mScaleAni.setDuration(200);
    mScaleAni.addUpdateListener(animation -> postInvalidate());
    AnimatorSet set = new AnimatorSet();
    set.playTogether(mAlphaAni, mScaleAni);
    set.start();

    removeCallbacks(this);
    if (mCircleArea != null) {
      postDelayed(this, 4000);
    }
  }

  /**
   * 函数曲线: https://easings.net/zh-cn#easeOutCubic
   * 公式: y(x)=1−(1−x)^3,x∈[0,1]
   */
  public class CubicEaseOutInterpolator implements Interpolator {
    @Override
    public float getInterpolation(float input) {
      input -= 1;
      return input * input * input + 1;
    }
  }

  /**
   * 函数曲线见: https://easings.net/zh-cn#easeOutQuad
   * 公式: y(x)=1−(1−x)^2,x∈[0,1]
   */
  public class QuadEaseOutInterpolator implements Interpolator {
    @Override
    public float getInterpolation(float input) {
      return input * (2 - input);
    }
  }
}
