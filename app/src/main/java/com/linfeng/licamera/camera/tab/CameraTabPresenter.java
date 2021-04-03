package com.linfeng.licamera.camera.tab;

import android.animation.ValueAnimator;
import android.view.View;

import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatImageView;

import com.linfeng.licamera.R;
import com.linfeng.licamera.base.BasePresenter;
import com.linfeng.licamera.camera.FocusView;

import java.util.ArrayList;
import java.util.List;

public class CameraTabPresenter implements BasePresenter {
    private AppCompatImageButton mCameraBtn;
    private AppCompatImageView mRecordRedPoint; //录制时显示的小红点
    private int mCurrentCameraStatus;
    private int mLastCameraStatus = CameraTabId.TAKE_PICTURE; //默认上一个是拍照
    private List<CameraTabEntity> mTabList = new ArrayList<>();

    @Override
    public void onCreate() {
        mTabList.add(new CameraTabEntity("照片", CameraTabId.TAKE_PICTURE));
        mTabList.add(new CameraTabEntity("视频", CameraTabId.RECORD));
    }

    @Override
    public void onViewCreated(View view) {
        mCameraBtn = view.findViewById(R.id.camera_btn);
        mRecordRedPoint = view.findViewById(R.id.little_red_point);
    }

    @Override
    public void onResume() {

    }

    @Override
    public void onDestroyView() {

    }

    /**
     * 获得CameraTab的列表
     */
    public List<CameraTabEntity> getCameraTabList() {
        return mTabList;
    }

    public void onCameraTabChanged(int tabId) {
        mCurrentCameraStatus = tabId;
        updateCameraButtonStatus();
        mLastCameraStatus = mCurrentCameraStatus;
    }

    public void updateCameraButtonStatus() {
        if (mLastCameraStatus == mCurrentCameraStatus) {
            return;
        }
        switch(mCurrentCameraStatus) {
            case CameraTabId.TAKE_PICTURE :
                setTakePictureButtonMode();
                break;
            case CameraTabId.RECORD:
                setRecordButtonMode();
                break;
        }
    }

    /**
     * 设置拍照状态，将红点向右移动
     */
    public void setTakePictureButtonMode() {
        if (mCameraBtn == null || mRecordRedPoint == null) {
            return;
        }
        if (mLastCameraStatus == CameraTabId.RECORD) {
            int r = mCameraBtn.getWidth() / 2 + mRecordRedPoint.getWidth() / 2;
            int[] startLocation = new int[2];
            mRecordRedPoint.getLocationOnScreen(startLocation);
            int startX = mRecordRedPoint.getLeft();
            ValueAnimator animator = new ValueAnimator();
            animator.setInterpolator(new FocusView.CubicEaseOutInterpolator());
            animator.setFloatValues(0, 1f);
            animator.setDuration(300);
            animator.addUpdateListener(animation -> {
                float rate = (float) animation.getAnimatedValue();
                mRecordRedPoint.layout((int) (startX + rate * r), mRecordRedPoint.getTop(),
                        (int) (startX + rate * r + mRecordRedPoint.getWidth()), mRecordRedPoint.getBottom());
            });
            animator.start();
        }

    }

    /**
     * 设置录制状态，将红点向左移入
     */
    public void setRecordButtonMode() {
        if (mCameraBtn == null || mRecordRedPoint == null) {
            return;
        }
        if (mLastCameraStatus == CameraTabId.TAKE_PICTURE) {
            int r = mCameraBtn.getWidth() / 2 + mRecordRedPoint.getWidth() / 2;
            int[] startLocation = new int[2];
            mRecordRedPoint.getLocationOnScreen(startLocation);
            int startX = mRecordRedPoint.getLeft();
            ValueAnimator animator = new ValueAnimator();
            animator.setInterpolator(new FocusView.CubicEaseOutInterpolator());
            animator.setFloatValues(0, 1f);
            animator.setDuration(300);
            animator.addUpdateListener(animation -> {
                float rate = (float) animation.getAnimatedValue();
                mRecordRedPoint.layout((int) (startX - rate * r), mRecordRedPoint.getTop(),
                        (int) (startX - rate * r + mRecordRedPoint.getWidth()), mRecordRedPoint.getBottom());
            });
            animator.start();
        }
    }

    public int getCameraButtonStatus() {
        return mCurrentCameraStatus;
    }

}
