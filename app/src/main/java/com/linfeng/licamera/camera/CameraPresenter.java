package com.linfeng.licamera.camera;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.fragment.app.FragmentTransaction;

import com.linfeng.licamera.camera.frame.FramePresenter;
import com.linfeng.licamera.camera.tab.CameraTabEntity;
import com.linfeng.licamera.R;
import com.linfeng.licamera.base.BasePresenter;
import com.linfeng.licamera.camera.tab.CameraTabId;
import com.linfeng.licamera.camera.tab.CameraTabPresenter;
import com.linfeng.licamera.login.LoginActivity;
import com.linfeng.licamera.picture.PictureFragment;
import com.linfeng.licamera.util.FileUtil;
import com.linfeng.licamera.videoEditor.TrimVideoActivity;

import java.util.List;

import io.reactivex.rxjava3.core.Observable;

import static com.linfeng.licamera.camera.frame.FrameMode.FRAME_9_16;

public class CameraPresenter implements BasePresenter, CameraHelper.OnImageCaptureListener {
  private final static String TAG = "CameraPresenter";
  private final static int REQUEST_FOR_LOGIN = 1;
  private final static int REQUEST_FOR_ALBUM = 2;
  private final static int REQUEST_FOR_VIDEO = 3;
  private CameraViewGroup mCameraViewGroup;
  //控制相机
  private CameraHelper mCameraHelper;
  private CameraFragment mFragment;
  private boolean mIsRecording;
  private float mZoom = 1f;
  private Activity activity;

  private CameraTabPresenter mCameraTabPresenter;//后面单独抽出来做addPresenter
  private FramePresenter mFramePresenter;

  public CameraPresenter(CameraFragment cameraFragment) {
    mFragment = cameraFragment;
  }

  @Override
  public void onCreate() {
    mCameraTabPresenter = new CameraTabPresenter(mFragment);
    mFramePresenter = new FramePresenter(mFragment);
    mCameraTabPresenter.onCreate();
    mFramePresenter.onCreate();
    activity = mFragment.getActivity();
  }

  @Override
  public void onViewCreated(View view) {
    mCameraViewGroup = view.findViewById(R.id.camera_view);
    mCameraHelper = CameraHelper.getInstance();
    assert mCameraHelper != null;
    mCameraHelper.addOnImageAvailableListener(this);
    mCameraTabPresenter.onViewCreated(view);
    mFramePresenter.onViewCreated(view);
    mFramePresenter.onFrameStatusChanged(FRAME_9_16);
  }

  @Override
  public void onResume() {
    if (mCameraViewGroup != null && mCameraViewGroup.getCameraFocusHandler() == null) {
      setCameraFocusHandler();
    }
    mCameraTabPresenter.onResume();
    mFramePresenter.onResume();
  }

  public void onPause() {
    mCameraHelper.removeOnImageAvailableListener(this);
  }

  @Override
  public void onDestroyView() {
    mCameraHelper.removeOnImageAvailableListener(this);
    mCameraTabPresenter.onDestroyView();
    mFramePresenter.onDestroyView();
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
    switch(status) {
      case CameraTabId.UNKNOWN:
      case CameraTabId.TAKE_PICTURE:
          mCameraHelper.takePicture();
          break;
      case CameraTabId.RECORD:
        if (mIsRecording) {
          stopRecord();
          mIsRecording = false;
        } else {
          startRecord();
          mIsRecording = true;
        }
    }
  }

  public void handleZoom(boolean isZoomOut) {
    CameraHelper helper = CameraHelper.getInstance();
    float maxZoom  = helper.getMaxZoom();
    if (isZoomOut && mZoom < maxZoom) {
      mZoom++;
    } else if(mZoom > 0){
      mZoom--;
    }
    helper.applyZoom(mZoom / 10);
  }

  public void startRecord() {
      mCameraHelper.startRecord();
    Toast.makeText(mFragment.getContext(),"开始录制", Toast.LENGTH_SHORT).show();
  }

  public void stopRecord() {
      mCameraHelper.stopRecord();
    Toast.makeText(mFragment.getContext(),"结束录制", Toast.LENGTH_SHORT).show();
      TrimVideoActivity.startActivity(mFragment.getContext(), FileUtil.getOutputMediaFile());
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
    FragmentTransaction transaction = mFragment.getParentFragmentManager()
            .beginTransaction()
            .setCustomAnimations(
            R.anim.slide_right_in,
            R.anim.slide_left_out,
            R.anim.slide_left_in,
            R.anim.slide_right_out);

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

  public void onLoginBtnClick() {
    Intent intent = new Intent(activity, LoginActivity.class);
    activity.startActivityForResult(intent,REQUEST_FOR_LOGIN);
  }

  public void onAlbumEntryBtnClick() {
    Intent intent= new Intent(Intent.ACTION_PICK, null);
    intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
    activity.startActivityForResult(intent, REQUEST_FOR_ALBUM);
  }

  public void onVideoEntryBtnClick() {
    Intent intent= new Intent(Intent.ACTION_PICK, null);
    intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "video/*");
    activity.startActivityForResult(intent, REQUEST_FOR_VIDEO);
  }


  public void onFrameBtnClick() {
    mFramePresenter.onFrameBtnClick();
  }
}
