package com.linfeng.licamera;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
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
import com.linfeng.licamera.picture.PictureFragment;
import com.linfeng.licamera.util.BitmapUtils;
import com.linfeng.licamera.videoEditor.TrimVideoActivity;

public class MainActivity extends AppCompatActivity {
  private static final String TAG = "MainActivity";
  public static final int ACTION_REQUEST_EDIT_IMAGE = 1;
  private final static int ACTION_REQUEST_FOR_ALBUM = 2;
  private final static int ACTION_REQUEST_FOR_VIDEO = 3;
  private static final int LOGIN_RESULT_OK = 1;
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

  private static final int REQUEST_EXTERNAL_STORAGE = 1;

  private static String[] PERMISSIONS_STORAGE = {"android.permission.READ_EXTERNAL_STORAGE","android.permission.WRITE_EXTERNAL_STORAGE" };

  private void requestPermission() {
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
      ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA
      }, 0/*requestCode*/);
    } while (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {}
    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
      ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 0);
    }
    try{
      //检测是否有写的权限
      int permission= ActivityCompat.checkSelfPermission(this, "android.permission.WRITE_EXTERNAL_STORAGE");
      if(permission!= PackageManager.PERMISSION_GRANTED){
        // 没有写的权限，去申请写的权限，会弹出对话框
        ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE,REQUEST_EXTERNAL_STORAGE);
      }
    }catch(Exception e){
      e.printStackTrace();
    }
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (resultCode == LOGIN_RESULT_OK) {
      mCameraFragment.onLoginSuccessful();
    }
    if (requestCode == ACTION_REQUEST_EDIT_IMAGE) {
      handleEditorImage(data);
    } else if (requestCode == ACTION_REQUEST_FOR_ALBUM) {
      if (resultCode == Activity.RESULT_OK) {
        String imagePath = getFilePath(data, MediaStore.Images.Media.DATA);
        if(!TextUtils.isEmpty(imagePath)) {
          int degree = BitmapUtils.getRotateDegree(imagePath);
          Bitmap bitmap= BitmapFactory.decodeFile(imagePath);
          bitmap = BitmapUtils.rotateBitmap(degree, bitmap);
          startPictureFragment(bitmap);
        }
      }
    } else if (requestCode == ACTION_REQUEST_FOR_VIDEO) {
      if (resultCode == Activity.RESULT_OK) {
        String videoPath = getFilePath(data, MediaStore.Video.Media.DATA);
        if(!TextUtils.isEmpty(videoPath)) {
          TrimVideoActivity.startActivity(this,videoPath);
        }
      }
    }
  }

  private void startPictureFragment(Bitmap bitmap) {
    FragmentTransaction transaction = mCameraFragment.getParentFragmentManager()
            .beginTransaction()
            .setCustomAnimations(
                    R.anim.slide_right_in,
                    R.anim.slide_left_out,
                    R.anim.slide_left_in,
                    R.anim.slide_right_out);

    PictureFragment pictureFragment = new PictureFragment(bitmap);
    transaction.replace(mCameraFragment.getId(), pictureFragment);
    transaction.addToBackStack(null);
    transaction.show(pictureFragment);
    transaction.commit();
  }

  private String getFilePath(Intent data, String type) {
    Uri uri = data.getData();//得到uri，后面就是将uri转化成file的过程。
    String[] projection = {type};
    Cursor actualFileCursor = getContentResolver().query(uri, projection, null, null, null);
    int actualFileColumnIndex = actualFileCursor.getColumnIndexOrThrow(type);

    actualFileCursor.moveToFirst();
    return actualFileCursor.getString(actualFileColumnIndex);
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