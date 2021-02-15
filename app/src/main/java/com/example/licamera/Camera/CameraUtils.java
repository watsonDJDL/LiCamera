package com.example.licamera.Camera;

import android.hardware.Camera;

public class CameraUtils {

  public static int getCameraNumbers() {
    return Camera.getNumberOfCameras();
  }

}
