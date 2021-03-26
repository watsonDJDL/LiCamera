package com.linfeng.licamera.camera;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.linfeng.licamera.R;
import com.linfeng.licamera.base.BaseFragment;
import com.linfeng.licamera.FramePresenter;

import static com.linfeng.licamera.FrameMode.FRAME_9_16;

public class CameraFragment extends BaseFragment {
  private static final String TAG = "CameraFragment";
  private CameraPresenter mCameraPresenter;
  private FramePresenter mFramePresenter;
  private CameraViewGroup mCameraViewGroup;
  private Button mTakePicBtn;
  private Button mRecordBtn;
  private ImageView mSwitchBtn;
  private ImageView mFrameSwitchBtn;

  public CameraFragment() {
    mCameraPresenter = new CameraPresenter(this);
    mFramePresenter = new FramePresenter(this);
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.camera_container_layout, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    mCameraPresenter.onViewCreated(view);
    mFramePresenter.onViewCreated(view);
    mCameraViewGroup = view.findViewById(R.id.camera_view);
    TextureView textureView = mCameraViewGroup.getTextureView();
    assert CameraHelper.getInstance() != null;
    CameraHelper.getInstance().setTextureView(textureView);
    mTakePicBtn = view.findViewById(R.id.take_picture_btn);
    mRecordBtn = view.findViewById(R.id.record_btn);
    mSwitchBtn = view .findViewById(R.id.camera_switch_btn);
    mFrameSwitchBtn = view.findViewById(R.id.frame_switch_btn);
    setViewsClickListener();
    mFramePresenter.onFrameStatusChanged(FRAME_9_16);
  }

  @Override
  public void onResume() {
    super.onResume();
    mCameraPresenter.onResume();
    mFramePresenter.onResume();
  }

  @Override
  public void onPause() {
    super.onPause();
    mCameraPresenter.onPause();
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    mCameraPresenter.onDestroyView();
    mFramePresenter.onDestroyView();
  }

  public CameraViewGroup getCameraViewGroup() {
    return mCameraViewGroup;
  }

  /**
   * 设置Views的监听
   */
  @SuppressLint("ClickableViewAccessibility")
  private void setViewsClickListener() {
    if (mSwitchBtn != null) {
      mSwitchBtn.setOnClickListener(v ->  mCameraPresenter.onCameraSwitch());
    }
    if (mTakePicBtn != null) {
      mTakePicBtn.setOnClickListener(v -> mCameraPresenter.onTakePictureBtnClick());
    }
    if(mRecordBtn != null) {
      mRecordBtn.setOnClickListener(v -> mCameraPresenter.onRecordBtnClick());
    }
    if (mFrameSwitchBtn != null) {
      mFrameSwitchBtn.setOnClickListener(v -> mFramePresenter.onFrameBtnClick());
    }
  }
}
