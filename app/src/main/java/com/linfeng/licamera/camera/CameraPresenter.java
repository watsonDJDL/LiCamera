package com.linfeng.licamera.camera;

import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;

import androidx.fragment.app.FragmentTransaction;

import com.linfeng.licamera.camera.tab.CameraTabEntity;
import com.linfeng.licamera.R;
import com.linfeng.licamera.base.BasePresenter;
import com.linfeng.licamera.camera.tab.CameraTabId;
import com.linfeng.licamera.camera.tab.CameraTabPresenter;
import com.linfeng.licamera.picture.PictureFragment;

import java.util.List;

import io.reactivex.rxjava3.core.Observable;

public class CameraPresenter implements BasePresenter, CameraHelper.OnImageCaptureListener {
  private final static String TAG = "CameraPresenter";
  private CameraViewGroup mCameraViewGroup;
  //控制相机
  private CameraHelper mCameraHelper;
  private CameraFragment mFragment;
  private boolean mIsRecording;

  private CameraTabPresenter mCameraTabPresenter;//后面单独抽出来做addPresenter

  public CameraPresenter(CameraFragment cameraFragment) {
    mFragment = cameraFragment;
  }

  @Override
  public void onCreate() {
    mCameraTabPresenter = new CameraTabPresenter();
    mCameraTabPresenter.onCreate();
  }

  @Override
  public void onViewCreated(View view) {
    mCameraViewGroup = view.findViewById(R.id.camera_view);
    mCameraHelper = CameraHelper.getInstance();
    assert mCameraHelper != null;
    mCameraHelper.addOnImageAvailableListener(this);
    mCameraTabPresenter.onViewCreated(view);
  }

  @Override
  public void onResume() {
    if (mCameraViewGroup != null && mCameraViewGroup.getCameraFocusHandler() == null) {
      setCameraFocusHandler();
    }
    mCameraTabPresenter.onResume();
  }

  public void onPause() {
    mCameraHelper.removeOnImageAvailableListener(this);
  }

  @Override
  public void onDestroyView() {
    mCameraHelper.removeOnImageAvailableListener(this);
    mCameraTabPresenter.onDestroyView();
  }


  public void onCameraSwitch() {
    if (mCameraHelper != null) {
      mCameraHelper.switchCamera();
    }
  }

  public void onCameraBtnClick() {
    if (mCameraHelper == null) {
      return;
    }
    int status = mCameraTabPresenter.getCameraButtonStatus();
    if (status == CameraTabId.TAKE_PICTURE) {
      mCameraHelper.takePicture();
    } else if (status == CameraTabId.RECORD) {
      if (mIsRecording) {
        stopRecord();
        mIsRecording = false;
      } else {
        startRecord();
        mIsRecording = true;
      }
    }
  }

  public void startRecord() {
      mCameraHelper.startRecord();
  }

  public void stopRecord() {
      mCameraHelper.stopRecord();
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

  public List<CameraTabEntity> getCameraTabList() {
    return mCameraTabPresenter.getCameraTabList();
  }

  public CameraTabPresenter getCameraTabPresenter() {
    return mCameraTabPresenter;
  }
}
