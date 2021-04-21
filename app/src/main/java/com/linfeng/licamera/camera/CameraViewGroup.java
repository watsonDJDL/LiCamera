package com.linfeng.licamera.camera;

import android.content.Context;
import android.util.AttributeSet;
import android.view.TextureView;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;

public class CameraViewGroup extends FrameLayout {

  private TextureView mTextureView;
  private CameraView mCameraView;

  public CameraViewGroup(@NonNull Context context) {
    super(context);
    init(context);
  }
  public CameraViewGroup(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    init(context);
  }
  public CameraViewGroup(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
    init(context);
  }

  private void init(Context context) {
    mTextureView = new TextureView(context);
    mCameraView = new CameraView(context);
    addView(mTextureView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    addView(mCameraView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
  }

  public void bindPresenter(CameraPresenter presenter) {
    mCameraView.bindPresenter(presenter);
  }

  //应该由外部来设置这里的CameraFocusHandler
  public void setCameraFocusHandler(CameraView.CameraFocusHandler h) {
    mCameraView.setFocusHandler(h);
  }

  public CameraView.CameraFocusHandler getCameraFocusHandler() {
    return mCameraView.getFocusHandler();
  }


  public TextureView getTextureView() {
    return mTextureView;
  }

}
