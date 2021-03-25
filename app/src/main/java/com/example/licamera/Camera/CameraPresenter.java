package com.example.licamera.Camera;

import android.graphics.Bitmap;
import android.os.Build;
import android.util.Log;
import android.view.View;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.FragmentTransaction;

import com.example.licamera.BasePresenter;
import com.example.licamera.PictureFragment;
import com.example.licamera.R;

import io.reactivex.rxjava3.core.Observable;

public class CameraPresenter implements BasePresenter, CameraHelper.OnImageCaptureListener {
  private final static String TAG = "CameraPresenter";
  private CameraViewGroup mCameraViewGroup;
  //控制相机
  private CameraHelper mCameraHelper;
  private CameraFragment mFragment;
  private boolean mIsRecording;

  public CameraPresenter(CameraFragment cameraFragment) {
    mFragment = cameraFragment;
  }

  @Override
  public void onViewCreated(View view) {
    mCameraViewGroup = view.findViewById(R.id.camera_view);
    mCameraHelper = CameraHelper.getInstance();
    assert mCameraHelper != null;
    mCameraHelper.addOnImageAvailableListener(this);
  }

  @Override
  public void onResume() {
    if (mCameraViewGroup != null && mCameraViewGroup.getCameraFocusHandler() == null) {
      setCameraFocusHandler();
    }
  }

  public void onPause() {
    mCameraHelper.removeOnImageAvailableListener(this);
  }

  @Override
  public void onDestroyView() {
    mCameraHelper.removeOnImageAvailableListener(this);
  }


  public void onCameraSwitch() {
    if (mCameraHelper != null) {
      mCameraHelper.switchCamera();
    }
  }

  public void onTakingPicture() {
    if (mCameraHelper != null) {
      mCameraHelper.takePicture();
    }
  }

  public void onRecordBtnClick() {
    if (mIsRecording) {
      stopRecord();
      mIsRecording = false;
    } else {
      startRecord();
      mIsRecording = true;
    }
  }

  public void startRecord() {
    if (mCameraHelper != null) {
      mCameraHelper.startRecord();
    }
  }

  public void stopRecord() {
    if (mCameraHelper != null) {
      mCameraHelper.stopRecord();
    }
  }

  private void setCameraFocusHandler() {
    getCameraViewGroup().setCameraFocusHandler(r -> {
      Log.d(TAG, "camera start focusing");
      return Observable.fromCallable(() -> mCameraHelper
          .setFocus(r, getCameraViewGroup().getWidth(), getCameraViewGroup().getHeight()));
    });
  }

  private CameraViewGroup getCameraViewGroup() {
    return mCameraViewGroup;
  }

  @Override
  public void onImageCapture(Bitmap bitmap) {
    startPictureFragment(bitmap);
  }

  private void startPictureFragment(Bitmap bitmap) {
    FragmentTransaction transaction = mFragment.getParentFragmentManager().beginTransaction();
    PictureFragment pictureFragment = new PictureFragment(bitmap);
    transaction.replace(mFragment.getId(), pictureFragment);
    transaction.addToBackStack(null);
    transaction.show(pictureFragment);
    transaction.commit();
  }
}
