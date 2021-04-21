package com.linfeng.licamera.camera;

import android.content.Context;
import android.graphics.PointF;
import android.graphics.Rect;
import android.hardware.camera2.params.MeteringRectangle;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import androidx.annotation.Nullable;

import com.linfeng.licamera.util.CommonUtil;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.internal.functions.Functions;

public class CameraView extends FrameLayout {
  private static final String TAG = "CameraView";
  private final PointF mTouchPoint = new PointF();
  private final PointF mTouchUpPoint = new PointF();
  private CameraPresenter mCameraPresenter;
  public CameraFocusHandler mHandler;
  private FocusView mFocusView;

  private final OnClickListener mFocusListener = v -> {
    if (Math.abs(mTouchUpPoint.x - mTouchPoint.x) < 20
        && Math.abs(mTouchUpPoint.y - mTouchPoint.y) < 20) {
      computeFocusArea(mTouchPoint.x, mTouchPoint.y);
    }
  };

  public CameraView(Context context) {
    super(context);
    init(context);
  }

  public CameraView(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    init(context);
  }

  public CameraView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(context);
  }

  private void init(Context context) {
    mFocusView = new FocusView(context);
    addView(mFocusView, FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
    setOnClickListener(this::handleClick);
  }

  public void bindPresenter(CameraPresenter presenter) {
    mCameraPresenter = presenter;
  }

  private void handleClick(View view) {
    mFocusListener.onClick(view);
  }

  private float oldDist = 1f;
  @Override
  public boolean onTouchEvent(MotionEvent event) {
    if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_DOWN) {
      mTouchPoint.set(event.getX(), event.getY());
    }
    if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP) {
      mTouchUpPoint.set(event.getX(), event.getY());
    }
    if(event.getPointerCount() > 1) {
      switch (event.getAction() & MotionEvent.ACTION_MASK) {
        case MotionEvent.ACTION_POINTER_DOWN:
          oldDist = getFingerSpacing(event);
          break;
        case MotionEvent.ACTION_MOVE:
          float newDist = getFingerSpacing(event);
          if (newDist > oldDist) {
            handleZoom(true);
          } else if (newDist < oldDist) {
            handleZoom(false);
          }
          Log.d(TAG, "oldDist   newDist:  " + oldDist + "   " + newDist);
          oldDist = newDist;
          break;
      }
    }
    return super.onTouchEvent(event);
  }

  private void handleZoom(boolean zoomOut) {
    if (mCameraPresenter != null) {
      mCameraPresenter.handleZoom(zoomOut);
    }
  }

  /** Determine the space between the first two fingers */
  private float getFingerSpacing(MotionEvent event) {
    // ...
    float x = event.getX(0) - event.getX(1);
    float y = event.getY(0) - event.getY(1);
    return (float) Math.sqrt(x * x + y * y);
  }


  private void computeFocusArea(float x, float y) {
    int width = getWidth(), height = getHeight();
    if (width == 0 || height == 0) {
      return;
    }
    CameraFocusHandler h = mHandler;
    if (h == null) {
      return;
    }

    final int size = 100;
    Rect focusR = new Rect((int) (x - size), (int) (y - size), (int) (x + size), (int) (y + size));
    //handler执行完后进行，result拿到是true代表请求对焦成功
    h.requestCameraFocus(focusR).subscribe((result) -> {
      if (result) {
        float radius = 40 * getResources().getDisplayMetrics().density;

        int left = (int) (x - radius);
        int right = (int) (x + radius);
        int top = (int) (y - radius);
        int bottom = (int) (y + radius);

        if (x - radius < 0) {
          left = 0;
          right = (int) (radius * 2);
        } else if (x + radius > CommonUtil.getScreenShortAxis()) {
          right = CommonUtil.getScreenShortAxis();
          left = (int) (right - radius * 2);
        }

        if (y - radius < 0) {
          top = 0;
          bottom = (int) (radius * 2);
        } else if (y + radius > CommonUtil.getScreenLongAxis()) {
          bottom = CommonUtil.getScreenLongAxis();
          top = (int) (bottom - radius * 2);
        }
        Rect r = new Rect(left, top, right, bottom);
        // Rect r = new Rect((focusR.left + 1000) * width / 2000, (focusR.top + 1000) * height /
        // 2000,
        // (focusR.right + 1000) * width / 2000, (focusR.bottom + 1000) * height / 2000);
        mFocusView.startFocus(r);
        CameraHelper.getInstance().startControlAFRequest(r);
        /*if (mAECompensationListener != null) {
          mAECompensationListener.onFocus();
        }*/
      }
    }, Functions.emptyConsumer());
  }

  public void setFocusHandler(CameraFocusHandler handler) {
    mHandler = handler;
  }

  public CameraFocusHandler getFocusHandler() {
    return mHandler;
  }

  public interface CameraFocusHandler {
    /**
     * Set camera focus limit with [-1000, -1000, 1000, 1000]
     *
     * @param r The focus rect
     * @return true on success, false otherwise
     */
    Observable<Boolean> requestCameraFocus(Rect r);
  }

}
