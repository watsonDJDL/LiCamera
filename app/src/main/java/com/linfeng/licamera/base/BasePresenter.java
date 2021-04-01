package com.linfeng.licamera.base;

import android.view.View;

public interface BasePresenter {
  void onCreate();
  void onViewCreated(View view);
  void onResume();
  void onDestroyView();
}
