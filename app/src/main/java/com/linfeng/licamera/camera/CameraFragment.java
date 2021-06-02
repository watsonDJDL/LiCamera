package com.linfeng.licamera.camera;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageButton;

import com.linfeng.licamera.R;
import com.linfeng.licamera.base.BaseFragment;
import com.linfeng.licamera.login.WebServiceGet;
import com.linfeng.licamera.util.CommonUtil;
import com.linfeng.licamera.util.Constant;
import com.linfeng.licamera.util.SPUtils;

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
    mStatisticBtn.setOnClickListener(v -> onStatisticBtnClick());
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

  private void onStatisticBtnClick() {
    int photoCount = SPUtils.getInt(Constant.PRODUCE_PHOTO_COUNT, 0, CommonUtil.context());
    int videoCount = SPUtils.getInt(Constant.PRODUCE_VIDEO_COUNT, 0, CommonUtil.context());
    String message = "你已经在LiCamera上" + "\n" + "创作图片：" + photoCount + "  张" + "\n" + "创作视频：" + videoCount + "  部" + "\n";
    showUsageStatistic(message);
    new Thread(new UpdateUsageInfo()).start();
  }

  //登录的时候将服务器的同步到本地，登录后的全部由本地同步到服务器
  //表中再加个用户登录状态？
  //后面放到presenter里面去
  public class UpdateUsageInfo implements Runnable {
    @Override
    public void run() {
      //先判断是否服务器和本地数据一致，一致则不用更新
      String username = SPUtils.getString("userName", "",CommonUtil.context());
      if (!TextUtils.isEmpty(username)) {
        String attr = "?username=" + username;
        String infoString = WebServiceGet.executeHttpGet("UsageInfoServlet", attr);//获取服务器返回的数据
        if (infoString == null) {
          getActivity().runOnUiThread(() -> {
            //后面弄一个Toast工具类
            Toast error = Toast.makeText(CommonUtil.context(),"获取服务器数据失败，请检查网络状态",Toast.LENGTH_SHORT);
            error.setGravity(Gravity.CENTER,0,0);
            error.show();
          });
          return;
        }
        String[] usage = infoString.split(",");
        int photoCount = SPUtils.getInt(Constant.PRODUCE_PHOTO_COUNT, 0, CommonUtil.context());
        int videoCount = SPUtils.getInt(Constant.PRODUCE_VIDEO_COUNT, 0, CommonUtil.context());
        if (Integer.parseInt(usage[0]) != photoCount || Integer.parseInt(usage[1]) != videoCount) {
          attr = "?username=" + username + "&photocount=" + photoCount + "&videocount=" + videoCount;
          infoString = WebServiceGet.executeHttpGet("PhotoCountServlet", attr);//获取服务器返回的数据
          if(infoString == null) {
            Toast error = Toast.makeText(CommonUtil.context(),"同步数据失败，请检查网络状态",Toast.LENGTH_SHORT);
            error.setGravity(Gravity.CENTER,0,0);
            error.show();
          }
        }
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
