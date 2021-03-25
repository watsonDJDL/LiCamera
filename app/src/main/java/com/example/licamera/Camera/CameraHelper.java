package com.example.licamera.Camera;

import static android.content.Context.CAMERA_SERVICE;
import static android.hardware.camera2.params.SessionConfiguration.SESSION_REGULAR;
import static android.os.Looper.getMainLooper;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
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
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.OutputConfiguration;
import android.hardware.camera2.params.SessionConfiguration;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import com.example.licamera.CollectionUtil;
import com.example.licamera.FrameMode;
import com.example.licamera.LiApplication;

public class CameraHelper {

  private final static String CAMERA_FRONT = "0";
  private final static String CAMERA_BACK = "1";
  private final static float RATIO_9_16 = 9f / 16;
  private static final String TAG = "CameraHelper";

  private Activity mCameraActivity;
  private CameraManager mCameraManager;
  private TextureView mTextureView;
  private String mCameraId = "1";
  private CameraCaptureSession mCaptureSession;
  private MediaRecorder mMediaRecorder;
  private ImageReader mImageReader;
  private CameraDevice mCameraDevice;
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
      createCameraPreview();
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

  public static CameraHelper mInstance;

  public static CameraHelper getInstance()
  {
    CameraHelper helper = mInstance;
    if (mInstance == null) {
      synchronized (CameraHelper.class)
      {
        helper = mInstance;
        if (mInstance == null)
        {
          return null;//这里要改进，看怎么才能拿到合适的Activity
        }
      }
    }

    return helper;
  }

  public CameraHelper(Activity cameraActivity) {
    mInstance = this;
    mCameraActivity = cameraActivity;
    mCameraManager = (CameraManager)mCameraActivity.getSystemService(CAMERA_SERVICE);
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

  /**
   * 创建预览部分，session用于预览
   */
  private void createCameraPreview() {
    try {
      SurfaceTexture texture = mTextureView.getSurfaceTexture();
      CameraCharacteristics characteristics = mCameraManager.getCameraCharacteristics(mCameraId);
      StreamConfigurationMap map =
              characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
      Size previewSize = CameraUtils
              .getPreferredPreviewSize(map.getOutputSizes(SurfaceTexture.class), mWidth, mHeight);
      //ViewGroup.LayoutParams textureLayoutParams = mTextureView.getLayoutParams();
      //textureLayoutParams.height = (int) (mTextureView.getWidth() / RATIO_9_16);
      //mTextureView.setLayoutParams(textureLayoutParams);
      texture.setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight());
      Surface surface = new Surface(texture);
      CaptureRequest.Builder  builder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
      builder.addTarget(surface);
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
                    mCaptureSession.setRepeatingRequest(builder.build(), mSessionCaptureCallback, childHandler);
                  } catch (CameraAccessException e) {
                    e.printStackTrace();
                  }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {

                }
              }, childHandler);
    } catch (CameraAccessException e) {
      Log.e(TAG, "CreateCameraPreview error ", e);
    }
  }

  //创建录制时相关的方法
  @RequiresApi(api = Build.VERSION_CODES.P)
  private void createRecordCameraSession(){
    try {
      closeCaptureSession();
      //创建一个ImageRecorder通过这个来拿到需要的surface
      setUpMediaRecorder();
      CaptureRequest.Builder requestBuilder = createRecordRequestBuilder();
      SessionConfiguration sessionConfiguration = new SessionConfiguration(SESSION_REGULAR,
              createRecordOutputConfiguration(), LiApplication.getContext().getMainExecutor(),
              new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                  mCaptureSession = session;
                  if (mCameraDevice == null) {
                    return;
                  }
                  try {
                    requestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
                    HandlerThread thread = new HandlerThread("CameraPreview");
                    thread.start();
                    mCaptureSession.setRepeatingRequest(requestBuilder.build(), mSessionCaptureCallback, childHandler);
                    mCameraActivity.runOnUiThread(() -> {
                      mMediaRecorder.start();
                    });
                  } catch (CameraAccessException e) {
                    Log.e(TAG, "CameraRecord Set repeating request failed", e);
                  }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                  Log.e(TAG, "Error: RecordCaptureSession configured failed");
                }
              });

      mCameraDevice.createCaptureSession(sessionConfiguration);
    } catch (CameraAccessException e) {
        e.printStackTrace();
    }
  }

  private CaptureRequest.Builder createRecordRequestBuilder() throws CameraAccessException {
    CaptureRequest.Builder builder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
    SurfaceTexture texture = getTexture();
    Surface previewSurface = new Surface(texture);
    builder.addTarget(previewSurface);
    // Set up Surface for the MediaRecorder
    Surface recorderSurface = mMediaRecorder.getSurface();
    builder.addTarget(recorderSurface);
    return builder;
  }

  @RequiresApi(api = Build.VERSION_CODES.N)
  private List<OutputConfiguration> createRecordOutputConfiguration() throws CameraAccessException {
    SurfaceTexture texture = getTexture();
    List<OutputConfiguration> outputs = new ArrayList<>();
    // Set up Surface for the camera preview
    Surface previewSurface = new Surface(texture);
    outputs.add(new OutputConfiguration(previewSurface));
    // Set up Surface for the MediaRecorder
    Surface recorderSurface = mMediaRecorder.getSurface();
    outputs.add(new OutputConfiguration(recorderSurface));
    return outputs;
  }

  private SurfaceTexture getTexture() throws CameraAccessException {
    SurfaceTexture texture = mTextureView.getSurfaceTexture();
    CameraCharacteristics characteristics = mCameraManager.getCameraCharacteristics(mCameraId);
    StreamConfigurationMap map =
        characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
    Size previewSize = CameraUtils
        .getPreferredPreviewSize(map.getOutputSizes(SurfaceTexture.class), mWidth, mHeight);
    assert texture != null;
    texture.setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight());
    return texture;
  }

  private void setUpMediaRecorder() throws CameraAccessException {
    mMediaRecorder = new MediaRecorder();

    mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
    mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
    mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
    mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
    mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
    CameraCharacteristics characteristics = mCameraManager.getCameraCharacteristics(mCameraId);
    StreamConfigurationMap map =
            characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
    Size previewSize = CameraUtils
            .getPreferredPreviewSize(map.getOutputSizes(SurfaceTexture.class), mWidth, mHeight);
    mMediaRecorder.setVideoSize(previewSize.getWidth(), previewSize.getHeight());
    mMediaRecorder.setVideoFrameRate(30);
    String mNextVideoAbsolutePath = getOutputMediaFile();
    if (mNextVideoAbsolutePath != null) {
      mMediaRecorder.setOutputFile(mNextVideoAbsolutePath);
    }
    mMediaRecorder.setVideoEncodingBitRate(10000000);

    mMediaRecorder.setOrientationHint(90);

    try {
      mMediaRecorder.prepare();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /*
   * 获取手机外部存储路径
   * */
  private String getOutputFile() {
    File mediaFile = null;
    boolean OutputExist = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
    if (OutputExist) {
      mediaFile = LiApplication.getContext().getExternalCacheDir();
      return mediaFile.toString();
    }
    return null;
  }

  /*
   * 获取录制视频的日期 作为存储文件路径一部分
   * */
  private String getDate() {
    Log.d(TAG, "获取录制视频的日期 ");
    Calendar ca = Calendar.getInstance();
    int year = ca.get(Calendar.YEAR);           // 获取年份
    int month = ca.get(Calendar.MONTH);         // 获取月份
    int day = ca.get(Calendar.DATE);            // 获取日
    String date = "" + year + "_" + (month + 1) + "_" + day;
    return date;
  }

  /*
   *创建视频存储文件夹 录制好的视频存储在手机外部存储中 以录像时间+mp4格式命名
   * */
  private String getOutputMediaFile() {
    Log.d(TAG, "获取视频存储的位置 ");
    String mediaPath = getOutputFile();
    if (mediaPath != null) {
      File mediaFile = new File(mediaPath + "/recordVideo");
      if (!mediaFile.exists()) {
        mediaFile.mkdirs();
      }
      return mediaFile.getAbsolutePath() + File.separator + getDate() + ".mp4";
    }
    return null;
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
        Bitmap bitmap = rotateBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
        bitmap = resizeBitMap(bitmap, mWidth, mHeight);
        if (bitmap != null) {
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

  //bitmap拿到的大小是1200*1600不符合要求，不过经过这个函数后画面会被拉伸扭曲，继续寻找解决方案
  private Bitmap resizeBitMap(Bitmap rootImg, int goalW, int goalH) {
    int rootW = rootImg.getWidth();
    int rootH = rootImg.getHeight();
    // graphics 包下的
    Matrix matrix = new Matrix();
    matrix.postScale(goalW * 1.0f / rootW, goalH * 1.0f / rootH);
    return Bitmap.createBitmap(rootImg, 0, 0, rootW, rootH, matrix, true);
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
      int rotation = mCameraActivity.getWindowManager().getDefaultDisplay().getRotation();
      // 根据设备方向计算设置照片的方向
      captureRequestBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));
      //拍照
      CaptureRequest mCaptureRequest = captureRequestBuilder.build();
      mCaptureSession.capture(mCaptureRequest, null, childHandler);
    } catch (CameraAccessException e) {
      e.printStackTrace();
    }
  }

  public void startRecord() {
    if(mCameraDevice == null) return;
    closeCaptureSession();
    createRecordCameraSession();
  }

  public void stopRecord() {
    mMediaRecorder.stop();
    mMediaRecorder.reset();
    createCameraPreview();
  }

  //关闭会话（用于录制时关闭预览的会话）
  private void closeCaptureSession() {
    if (mCaptureSession != null) {
      mCaptureSession.close();
      mCaptureSession = null;
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

  public void onCameraFrameChanged(FrameMode frameMode) {
    closeCaptureSession();
    mHeight = CameraUtils.getCameraViewHeight(frameMode,mWidth);
    createCameraPreview();

  }
}
