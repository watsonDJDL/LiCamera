package com.example.licamera.Camera;

import android.content.Context;
import android.util.Log;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;

public class FocusView extends FrameLayout {
  public FocusView(@NonNull Context context) {
    super(context);
  }

  private void startFocus() {
    Log.d("FocusView", "start focus");
  }
}
