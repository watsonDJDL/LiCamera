package com.example.licamera.Camera;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.licamera.BaseFragment;
import com.example.licamera.CommonUtil;
import com.example.licamera.R;

public class CameraFragment extends BaseFragment {
  private static final String TAG = "CameraFragment";
  private final static float RATIO_9_16 = 9f / 16;
  private final static float RATIO_3_4 = 3f / 4;
  private CameraPresenter mCameraPresenter;
  private CameraViewGroup mCameraViewGroup;
  private Button mTakePicBtn;
  private Button mSwitchBtn;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mCameraPresenter = new CameraPresenter(this);
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
    mCameraViewGroup = view.findViewById(R.id.camera_preview_view);
    TextureView textureView = mCameraViewGroup.getTextureView();
    assert CameraController.getInstance() != null;
    CameraController.getInstance().setTextureView(textureView);
    mTakePicBtn = view.findViewById(R.id.take_picture_btn);
    mSwitchBtn = view .findViewById(R.id.switch_btn);
    setViewsListener();
    setFrame();
  }

  @Override
  public void onResume() {
    super.onResume();
    mCameraPresenter.onResume();
  }

  public CameraViewGroup getCameraViewGroup() {
    return mCameraViewGroup;
  }

  /**
   * 设置Views的监听
   */
  @SuppressLint("ClickableViewAccessibility")
  private void setViewsListener() {
    if (mSwitchBtn != null) {
      mSwitchBtn.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          switchCamera();
        }
      });
    }
    if (mTakePicBtn != null) {
      mTakePicBtn.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          //mFragment.setContentView(mTextureView.getBitmap().);
          takePicture();
        }
      });
    }
  }

  /**
   * 后面要拆分到Frame控制逻辑中
   */
  private void setFrame() {
    if (getCameraViewGroup() != null) {
      //先默认9：16
      int width = CommonUtil.getScreenShortAxis();
      int height = width;//(int) (width / RATIO_3_4);
      ViewGroup.LayoutParams cameraViewLayoutParams = mCameraViewGroup.getLayoutParams();
      cameraViewLayoutParams.width = width;
      cameraViewLayoutParams.height = height;
      mCameraViewGroup.setLayoutParams(cameraViewLayoutParams);
    }
  }

  private void switchCamera() {
    if (mCameraPresenter != null) {
      mCameraPresenter.onCameraSwitch();
    }
  }

  private void takePicture() {
    if (mCameraPresenter != null) {
      mCameraPresenter.onTakingPicture();
    }
  }
}
