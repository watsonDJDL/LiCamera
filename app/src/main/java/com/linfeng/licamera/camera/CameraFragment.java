package com.linfeng.licamera.camera;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.media.Image;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.RecyclerView;

import com.linfeng.licamera.R;
import com.linfeng.licamera.base.BaseFragment;
import com.linfeng.licamera.camera.frame.FramePresenter;
import com.linfeng.licamera.login.WebServiceGet;
import com.linfeng.licamera.util.CommonUtil;
import com.linfeng.licamera.util.SPUtils;

import static com.linfeng.licamera.camera.frame.FrameMode.FRAME_9_16;

public class CameraFragment extends BaseFragment {
  private static final String TAG = "CameraFragment";
  private CameraPresenter mCameraPresenter;
  private CameraViewGroup mCameraViewGroup;
  private AppCompatImageButton mCameraBtn;
  private ImageView mSwitchBtn;
  private TextView mFrameSwitchBtn;
  private TextView mLoginView;
  private ImageView mStatisticBtn;

  public CameraFragment() {
    mCameraPresenter = new CameraPresenter(this);
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mCameraPresenter.onCreate();
  }

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.camera_container_layout, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);


    mCameraViewGroup = view.findViewById(R.id.camera_view);
    TextureView textureView = mCameraViewGroup.getTextureView();
    mCameraViewGroup.bindPresenter(mCameraPresenter);
    assert CameraHelper.getInstance() != null;
    CameraHelper.getInstance().setTextureView(textureView);

    mCameraBtn = view.findViewById(R.id.camera_btn);
    mSwitchBtn = view .findViewById(R.id.camera_switch_btn);
    mFrameSwitchBtn = view.findViewById(R.id.frame_btn);

    setViewsClickListener();
    mCameraPresenter.onViewCreated(view);
    mLoginView = view.findViewById(R.id.login);
    mLoginView.setOnClickListener(v -> mCameraPresenter.onLoginBtnClick());
    mStatisticBtn = view.findViewById(R.id.statistic_btn);
    mStatisticBtn.setOnClickListener( v -> new Thread(new QueryUsageInfo()).start());
    mStatisticBtn.setVisibility(View.GONE);
    if (SPUtils.getBoolean("hasLogin", false, CommonUtil.context())) {
      mLoginView.setVisibility(View.GONE);
      mStatisticBtn.setVisibility(View.VISIBLE);
    }
    ImageView albumEntry = view.findViewById(R.id.album_entry);
    albumEntry.setOnClickListener(v -> mCameraPresenter.onAlbumEntryBtnClick());
    ImageView videoEntry = view.findViewById(R.id.video_entry);
    videoEntry.setOnClickListener(v -> mCameraPresenter.onVideoEntryBtnClick());
  }

  private void showUsageStatistic(String content) {
    AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
    dialog.setTitle("创作统计");
    dialog.setMessage(content);
    dialog.show();
  }

  //后面放到presenter里面去
  public class QueryUsageInfo implements Runnable {
    @Override
    public void run() {
      String username = SPUtils.getString("userName", "",CommonUtil.context());
      if (!TextUtils.isEmpty(username)) {
        String attr = "?username=" + username;
        String infoString = WebServiceGet.executeHttpGet("UsageInfoServlet", attr);//获取服务器返回的数据
        String[] usage = infoString.split(",");
        String message = "你已经在LiCamera上"  + "\n" + "创作图片：" + usage[0] + "  张" + "\n" + "创作视频：" + usage[1] + "  部" + "\n";
        getActivity().runOnUiThread(() -> showUsageStatistic(message));
      }
    }
  }

  @Override
  public void onResume() {
    super.onResume();
    mCameraPresenter.onResume();
  }

  @Override
  public void onPause() {
    super.onPause();
    mCameraPresenter.onPause();
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    mCameraPresenter.onDestroyView();
  }

  public CameraViewGroup getCameraViewGroup() {
    return mCameraViewGroup;
  }

  public void onLoginSuccessful() {
    mLoginView.setVisibility(View.GONE);
    mStatisticBtn.setVisibility(View.VISIBLE);
    SPUtils.putBoolean("hasLogin", true, CommonUtil.context());

  }

  /**
   * 设置Views的监听
   */
  @SuppressLint("ClickableViewAccessibility")
  private void setViewsClickListener() {
    if (mSwitchBtn != null) {
      mSwitchBtn.setOnClickListener(v ->  mCameraPresenter.onCameraSwitch());
    }
    if (mCameraBtn != null) {
      mCameraBtn.setOnClickListener(v -> mCameraPresenter.onCameraBtnClick());
    }
    if (mFrameSwitchBtn != null) {
      mFrameSwitchBtn.setOnClickListener(v -> mCameraPresenter.onFrameBtnClick());
    }
  }
}
