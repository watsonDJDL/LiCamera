package com.linfeng.licamera;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.linfeng.licamera.camera.CameraHelper;
import com.linfeng.licamera.camera.CameraFragment;
import com.linfeng.licamera.imageEditor.EditImageActivity;
import com.linfeng.licamera.util.BitmapUtils;

public class MainActivity extends AppCompatActivity {
  private static final String TAG = "MainActivity";
  public static final int ACTION_REQUEST_EDIT_IMAGE = 1;
  private CameraHelper cameraHelper;
  private CameraFragment mCameraFragment;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    //getWindow().requestFeature(Window.FEATURE_NO_TITLE);
    setContentView(R.layout.activity_main);
    requestPermission();
    cameraHelper = new CameraHelper(this);
    mCameraFragment = new CameraFragment();
    FragmentManager manager = getSupportFragmentManager();
    FragmentTransaction transaction = manager.beginTransaction();
    transaction.add(R.id.camera_fragment, mCameraFragment);
    transaction.commit();
  }

  private void requestPermission() {
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
      ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA
      }, 0/*requestCode*/);
    } while (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {}
    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
      ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 0);
    }
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == ACTION_REQUEST_EDIT_IMAGE) {
      handleEditorImage(data);
    }
  }

  private void handleEditorImage(Intent data) {
    String newFilePath = data.getStringExtra(EditImageActivity.EXTRA_OUTPUT);
    boolean isImageEdit = data.getBooleanExtra(EditImageActivity.IMAGE_IS_EDIT, false);

    if (isImageEdit){
      Toast.makeText(this, getString(R.string.save_path, newFilePath), Toast.LENGTH_LONG).show();
    }else{//未编辑  还是用原来的图片
      newFilePath = data.getStringExtra(EditImageActivity.FILE_PATH);;
    }
    //System.out.println("newFilePath---->" + newFilePath);
    //File file = new File(newFilePath);
    //System.out.println("newFilePath size ---->" + (file.length() / 1024)+"KB");
    Log.d("image is edit", isImageEdit + "");
  }
}