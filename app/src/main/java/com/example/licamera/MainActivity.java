package com.example.licamera;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.Manifest;
import android.animation.ValueAnimator;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
  private final static String TAG = "MainActivity";
  private final static String CAMERA_FRONT = "0";
  private final static String CAMERA_BACK = "1";
  private final static float RATIO_9_16 = 9f/16;
  private CameraManager mCameraManager;
  private String mCameraId;
  private CameraDevice mCameraDevice;
  private CaptureRequest.Builder mBuilder;
  private TextureView mTextureView;
  private int mWidth;
  private int mHeight;
  private CameraCaptureSession mCaptureSession;
  private CameraCaptureSession.CaptureCallback mSessionCaptureCallback = new CameraCaptureSession.CaptureCallback() {
    @Override
    public void onCaptureStarted(CameraCaptureSession session, CaptureRequest request, long timestamp, long frameNumber) {
      super.onCaptureStarted(session, request, timestamp, frameNumber);
    }
  };
  private CameraDevice.StateCallback mCameraDeviceStateCallback = new CameraDevice.StateCallback() {
    @Override
    public void onOpened(CameraDevice cameraDevice) {
      mCameraDevice = cameraDevice;
     try{
        createCameraPreview();
      } catch (CameraAccessException e) {
        e.printStackTrace();
      }
    }

    @Override
    public void onDisconnected(@NonNull CameraDevice camera) {
      mCameraDevice.close();
      mCameraDevice = null;
    }

    @Override
    public void onError(@NonNull CameraDevice camera, int error) {
      mCameraDevice.close();
      mCameraDevice = null;
    }
  };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    Button switchBtn = findViewById(R.id.switch_btn);
    switchBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        switchCamera();
      }
    });
    mTextureView = findViewById(R.id.texture_view);
    mTextureView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Log.d(TAG, "cameraView is clicked");
        //focusView 不记忆前一个位置
      }
    });
    mTextureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
      @Override
      public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width,
          int height) {
        mWidth = width;

        WindowManager manager = getWindowManager();
        DisplayMetrics outMetrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(outMetrics);
        mHeight = (int)(mWidth/(RATIO_9_16));
        getCameraId();
        openCamera();
      }

      @Override
      public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width,
          int height) {

      }

      @Override
      public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
        return false;
      }

      @Override
      public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {

      }
    });

    if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
      ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA
      }, 0/*requestCode*/);
    } while (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {}
    mCameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);
  }

  private void openCamera() {
    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
      Log.e(TAG, "Lacking privileges to access camera service, please request permission first.");
      ActivityCompat.requestPermissions(this, new String[]{
          Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE
      }, 0);
    }

    try {
      mCameraManager.openCamera(mCameraId, mCameraDeviceStateCallback, null);
      //startBackgroundThread();
    } catch (CameraAccessException e) {
      e.printStackTrace();
    }
  }

  private void switchCamera() {
    if (mCameraId.equals(CAMERA_FRONT)) {
      mCameraId = CAMERA_BACK;
      closeCamera();
      openCamera();

    } else if (mCameraId.equals(CAMERA_BACK)) {
      mCameraId = CAMERA_FRONT;
      closeCamera();
      openCamera();
    }

  }

  private void closeCamera() {
    try {

      if (null != mCaptureSession) {
        mCaptureSession.close();
        mCaptureSession = null;
      }
      if (null != mCameraDevice) {
        mCameraDevice.close();
        mCameraDevice = null;

      }
    } catch (Exception e) {
      throw new RuntimeException("Interrupted while trying to lock camera closing.", e);
    } finally {
      Log.d(TAG, "close camera finally");
    }
  }

  private void createCameraPreview() throws CameraAccessException {
    Size previewSize;
    SurfaceTexture texture = mTextureView.getSurfaceTexture();
    CameraCharacteristics characteristics = mCameraManager.getCameraCharacteristics(mCameraId);
    StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
    previewSize = getPreferredPreviewSize(map.getOutputSizes(SurfaceTexture.class), mWidth, mHeight);
    ViewGroup.LayoutParams textureLayoutParams = mTextureView.getLayoutParams();
    textureLayoutParams.height = (int)(mTextureView.getWidth()/RATIO_9_16) ;
    mTextureView.setLayoutParams(textureLayoutParams);
    texture.setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight());
    Surface surface = new Surface(texture);
    mBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
    mBuilder.addTarget(surface);
    mCameraDevice.createCaptureSession(Arrays.asList(surface),
        new CameraCaptureSession.StateCallback() {
          @Override
          public void onConfigured(@NonNull CameraCaptureSession session) {
            //如果相机已经关闭了
            if (null == mCameraDevice) {
              return;
            }
            mCaptureSession = session;
            try {
              mCaptureSession.setRepeatingRequest(mBuilder.build(), mSessionCaptureCallback, null);
            } catch (CameraAccessException e) {
              e.printStackTrace();
            }
          }

          @Override
          public void onConfigureFailed(@NonNull CameraCaptureSession session) {

          }
        }, null);
  }

  private Size getPreferredPreviewSize(Size[] sizes, int width, int height) {
    List<Size> collectorSizes = new ArrayList<>();
    if (width < height) {
      int temp = width;
      width = height;
      height = temp;
    }
    for (Size option : sizes) {
      if (option.getWidth() >= width && option.getHeight() >= height) {
        collectorSizes.add(option);
      }
    }
    if (collectorSizes.size() > 0) {
      return Collections.min(collectorSizes, new Comparator<Size>() {
        @Override
        public int compare(Size s1, Size s2) {
          return Long.signum(s1.getWidth() * s1.getHeight() - s2.getWidth() * s2.getHeight());
        }
      });
    }
    return sizes[0];
  }

  private void getCameraId() {
    try {
      for (String cameraId : mCameraManager.getCameraIdList()) {
        CameraCharacteristics characteristics = mCameraManager.getCameraCharacteristics(cameraId);
        if (characteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT) {
          continue;
        }
        mCameraId = cameraId;
        return;
      }
    } catch (CameraAccessException e) {
      e.printStackTrace();
    }
  }

}