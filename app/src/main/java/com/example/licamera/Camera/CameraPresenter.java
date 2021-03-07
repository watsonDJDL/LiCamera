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

import com.example.licamera.BasePresenter;
import com.example.licamera.R;

import io.reactivex.rxjava3.core.Observable;

public class CameraPresenter implements BasePresenter {
  private final static String TAG = "CameraPresenter";
  private CameraViewGroup mCameraViewGroup;
  //控制相机的Controller
  private CameraController mCameraController;

  @Override
  public void onViewCreated(View view) {
    mCameraViewGroup = view.findViewById(R.id.camera_preview_view);
    mCameraController = CameraController.getInstance();
  }

  @Override
  public void onResume() {
    if (mCameraViewGroup != null && mCameraViewGroup.getHandler() == null) {
      setCameraFocusHandler();
    }
  }

  //保存文件到指定路径
  public static boolean saveImageToGallery(Context context, Bitmap bmp) {
    // 首先保存图片
    String storePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "dearxy";
    File appDir = new File(storePath);
    if (!appDir.exists()) {
      appDir.mkdir();
    }
    String fileName = System.currentTimeMillis() + ".jpg";
    File file = new File(appDir, fileName);
    try {
      FileOutputStream fos = new FileOutputStream(file);
      //通过io流的方式来压缩保存图片
      boolean isSuccess = bmp.compress(Bitmap.CompressFormat.JPEG, 60, fos);
      fos.flush();
      fos.close();

      //把文件插入到系统图库
      MediaStore.Images.Media.insertImage(context.getContentResolver(), file.getAbsolutePath(), fileName, null);

      //保存图片后发送广播通知更新数据库
      Uri uri = Uri.fromFile(file);
      context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
      if (isSuccess) {
        return true;
      } else {
        return false;
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return false;
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
}
