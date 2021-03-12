package com.example.licamera.Camera;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.licamera.BasePresenter;
import com.example.licamera.PictureFragment;
import com.example.licamera.R;

import io.reactivex.rxjava3.core.Observable;

public class CameraPresenter implements BasePresenter, CameraController.OnImageCaptureListener {
  private final static String TAG = "CameraPresenter";
  private CameraViewGroup mCameraViewGroup;
  //控制相机的Controller
  private CameraController mCameraController;
  private CameraFragment mFragment;

  public CameraPresenter(CameraFragment cameraFragment) {
    mFragment = cameraFragment;
  }

  @Override
  public void onViewCreated(View view) {
    mCameraViewGroup = view.findViewById(R.id.camera_preview_view);
    mCameraController = CameraController.getInstance();
    assert mCameraController != null;
    mCameraController.addOnImageAvailableListener(this);
  }

  @Override
  public void onResume() {
    if (mCameraViewGroup != null && mCameraViewGroup.getCameraFocusHandler() == null) {
      setCameraFocusHandler();
    }
  }

  public void onPause() {
    mCameraController.removeOnImageAvailableListener(this);
  }

  @Override
  public void onDestroyView() {
    mCameraController.removeOnImageAvailableListener(this);
  }


  public void onCameraSwitch() {
    if (mCameraController != null) {
      mCameraController.switchCamera();
    }
  }

  public void onTakingPicture() {
    if (mCameraController != null) {
      mCameraController.takePicture();
    }

  }

  private void setCameraFocusHandler() {
    getCameraViewGroup().setCameraFocusHandler(r -> {
      Log.d(TAG, "camera start focusing");
      return Observable.fromCallable(() -> mCameraController
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
