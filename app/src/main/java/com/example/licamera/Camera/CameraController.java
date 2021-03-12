package com.example.licamera.Camera;

import static android.content.Context.CAMERA_SERVICE;
import static android.os.Looper.getMainLooper;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.example.licamera.CollectionUtil;
import com.example.licamera.R;

public class CameraController {

  private final static String CAMERA_FRONT = "0";
  private final static String CAMERA_BACK = "1";
  private final static float RATIO_9_16 = 9f / 16;
  private static final String TAG = "CameraController";

  private Activity mCameraActivity;
  private CameraManager mCameraManager;
  private TextureView mTextureView;
  private ImageView iv_show;
  private String mCameraId = "1";
  private CameraCaptureSession mCaptureSession;
  private ImageReader mImageReader;
  private CameraDevice mCameraDevice;
  private CaptureRequest.Builder mBuilder;
  private Bitmap mCurrentCaptureBitMap;
  private Handler childHandler, mainHandler;
  private List<OnImageCaptureListener> mImageAvailableListeners = new ArrayList<>();
  private int mWidth;
  private int mHeight;
  private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
  ///为了使照片竖直显示
  static {
    ORIENTATIONS.append(Surface.ROTATION_0, 0);
    ORIENTATIONS.append(Surface.ROTATION_90, 90);
    ORIENTATIONS.append(Surface.ROTATION_180, 180);
    ORIENTATIONS.append(Surface.ROTATION_270, 270);
  }
  private CameraCaptureSession.CaptureCallback mSessionCaptureCallback =
      new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureStarted(CameraCaptureSession session, CaptureRequest request,
            long timestamp, long frameNumber) {
          super.onCaptureStarted(session, request, timestamp, frameNumber);
        }
      };
  private CameraDevice.StateCallback mCameraDeviceStateCallback = new CameraDevice.StateCallback() {
    @Override
    public void onOpened(CameraDevice cameraDevice) {
      mCameraDevice = cameraDevice;
      try {
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

  public static CameraController mInstance;

  public static CameraController getInstance()
  {
    CameraController controller = mInstance;
    if (mInstance == null) {
      synchronized (CameraController.class)
      {
        controller = mInstance;
        if (mInstance == null)
        {
          return null;//这里要改进，看怎么才能拿到合适的Activity
        }
      }
    }

    return controller;
  }

  public CameraController(Activity cameraActivity) {
    mInstance = this;
    mCameraActivity = cameraActivity;
    mCameraManager = (CameraManager)mCameraActivity.getSystemService(CAMERA_SERVICE);
    iv_show =  mCameraActivity.findViewById(R.id.capture_view);
    HandlerThread handlerThread = new HandlerThread("Camera2");
    handlerThread.start();
    childHandler = new Handler(handlerThread.getLooper());
    mainHandler = new Handler(getMainLooper());
  }

  //目前只由CameraFragment设置这里的TextureView
  public void setTextureView(TextureView textureView) {
    if (textureView == null) {
      Log.d(TAG, "textureView is null!");
      return;
    }
    mTextureView = textureView;
    initTextureView();
  }

  private void createCameraPreview() throws CameraAccessException {
    Size previewSize;
    SurfaceTexture texture = mTextureView.getSurfaceTexture();
    CameraCharacteristics characteristics = mCameraManager.getCameraCharacteristics(mCameraId);
    StreamConfigurationMap map =
        characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
    previewSize = CameraUtils
        .getPreferredPreviewSize(map.getOutputSizes(SurfaceTexture.class), mWidth, mHeight);
    ViewGroup.LayoutParams textureLayoutParams = mTextureView.getLayoutParams();
    textureLayoutParams.height = (int) (mTextureView.getWidth() / RATIO_9_16);
    mTextureView.setLayoutParams(textureLayoutParams);
    texture.setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight());
    Surface surface = new Surface(texture);
    mBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
    mBuilder.addTarget(surface);
    mCameraDevice.createCaptureSession(Arrays.asList(surface, mImageReader.getSurface()),
        new CameraCaptureSession.StateCallback() {
          @Override
          public void onConfigured(@NonNull CameraCaptureSession session) {
            //如果相机已经关闭了
            if (null == mCameraDevice) {
              return;
            }
            mCaptureSession = session;
            try {
              mCaptureSession.setRepeatingRequest(mBuilder.build(), mSessionCaptureCallback, childHandler);
            } catch (CameraAccessException e) {
              e.printStackTrace();
            }
          }

          @Override
          public void onConfigureFailed(@NonNull CameraCaptureSession session) {

          }
        }, childHandler);
  }

  private void initTextureView() {
    mTextureView.setClickable(true);
    mTextureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
      @Override
      public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width,
          int height) {
        mWidth = width;
        WindowManager manager = mCameraActivity.getWindowManager();//看是不是放在Activity中
        DisplayMetrics outMetrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(outMetrics);
        mHeight = (int) (mWidth / (RATIO_9_16));
        initImageReader();
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
  }

  private void initImageReader() {
    Log.d(TAG, "textureView width, height: " + mWidth + "  " + mHeight);
    mImageReader = ImageReader.newInstance(mWidth, mHeight, ImageFormat.JPEG, 1);
    mImageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() { //可以在这里处理拍照得到的临时照片 例如，写入本地
      @Override
      public void onImageAvailable(ImageReader reader) {
        //mCameraDevice.close();
        // 拿到拍照照片数据
        mTextureView.setVisibility(View.INVISIBLE);
        Image image = reader.acquireNextImage();
        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);//由缓冲区存入字节数组
        final Bitmap bitmap = rotateBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
        if (bitmap != null) {
          //iv_show.setImageBitmap(bitmap);//这里就拿到了拍到的照片
          if (!CollectionUtil.isEmpty(mImageAvailableListeners)) {
            for (OnImageCaptureListener listener : mImageAvailableListeners) {
              listener.onImageCapture(bitmap);
            }
          }
          mCurrentCaptureBitMap = bitmap;
        }
      }
    }, mainHandler);
  }

  interface OnImageCaptureListener {
    void onImageCapture(Bitmap bitmap);
  }

  public void addOnImageAvailableListener(OnImageCaptureListener listener) {
    mImageAvailableListeners.add(listener);
  }

  public void removeOnImageAvailableListener(OnImageCaptureListener listener) {
    mImageAvailableListeners.remove(listener);
  }


  private void openCamera() {
    if (ActivityCompat.checkSelfPermission(mCameraActivity, Manifest.permission.CAMERA)
        != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mCameraActivity,
        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
      Log.e(TAG, "Lacking privileges to access camera service, please request permission first.");
      ActivityCompat.requestPermissions(mCameraActivity, new String[]{
          Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE
      }, 0);
    }
    try {
      mCameraManager.openCamera(mCameraId, mCameraDeviceStateCallback, mainHandler);
      //startBackgroundThread();
    } catch (CameraAccessException e) {
      e.printStackTrace();
    }
  }

  public void switchCamera() {
    if (mCameraId.equals(CAMERA_FRONT)) {
      mCameraId = CAMERA_BACK;
    } else if (mCameraId.equals(CAMERA_BACK)) {
      mCameraId = CAMERA_FRONT;
    }
    closeCamera();
    openCamera();
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

  /**
   * 获取后置摄像头ID
   * get the back camera's id
   */
  private void getCameraId() {
    try {
      for (String cameraId : mCameraManager.getCameraIdList()) {
        CameraCharacteristics characteristics = mCameraManager.getCameraCharacteristics(cameraId);
        if (characteristics.get(CameraCharacteristics.LENS_FACING) ==
            CameraCharacteristics.LENS_FACING_FRONT) {
          continue;
        }
        mCameraId = cameraId;
        return;
      }
    } catch (CameraAccessException e) {
      e.printStackTrace();
    }
  }

  /**
   * 拍照
   * take Picture
   */
  public void takePicture() {
    if (mCameraDevice == null) return;
    // 创建拍照需要的CaptureRequest.Builder
    final CaptureRequest.Builder captureRequestBuilder;
    try {
      captureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
      // 将imageReader的surface作为CaptureRequest.Builder的目标
      captureRequestBuilder.addTarget(mImageReader.getSurface());
      // 自动对焦
      captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
      // 自动曝光
      captureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
      // 获取手机方向
      int rotation = mCameraActivity.getWindowManager().getDefaultDisplay().getRotation();//看是不是放在Activity中
      // 根据设备方向计算设置照片的方向
      captureRequestBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));
      //拍照
      CaptureRequest mCaptureRequest = captureRequestBuilder.build();
      mCaptureSession.capture(mCaptureRequest, null, childHandler);
    } catch (CameraAccessException e) {
      e.printStackTrace();
    }
  }

  /**
   * 旋转图片
   * @return 旋转后图片（只是修改了Bitmap对象，没有修改图片文件)
   */
  public Bitmap rotateBitmap(Bitmap bmp) {
    Matrix matrix = new Matrix();
    matrix.postRotate(90);
    Bitmap rotatedBitMap = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
    return rotatedBitMap;
  }

  public boolean setFocus(@NonNull Rect r, int width, int height) {
    return true;
  }


}
