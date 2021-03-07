package com.example.licamera.Camera;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.hardware.Camera;
import android.util.Size;

public class CameraUtils {

  public static int getCameraNumbers() {
    return Camera.getNumberOfCameras();
  }

  public static Size getPreferredPreviewSize(Size[] sizes, int width, int height) {
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

}
