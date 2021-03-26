package com.linfeng.licamera;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.linfeng.licamera.camera.CameraHelper;
import com.linfeng.licamera.camera.CameraFragment;

public class MainActivity extends AppCompatActivity {
  private final static String TAG = "MainActivity";
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
}