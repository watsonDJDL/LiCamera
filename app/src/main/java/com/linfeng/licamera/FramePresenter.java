package com.linfeng.licamera;

import android.view.View;
import android.view.ViewGroup;

import com.linfeng.licamera.base.BasePresenter;
import com.linfeng.licamera.camera.CameraFragment;
import com.linfeng.licamera.camera.CameraHelper;
import com.linfeng.licamera.util.CommonUtil;

import static com.linfeng.licamera.FrameMode.FRAME_3_4;
import static com.linfeng.licamera.FrameMode.FRAME_9_16;

public class FramePresenter implements BasePresenter {
    private static final float RATIO_9_16 = 9f / 16;
    private static final float RATIO_3_4 = 3f / 4;
    private static final float RATIO_1_1 = 1;

    private CameraFragment mFragment;
    //当前的画幅模式
    private FrameMode mCurrentMode = FRAME_9_16;

    public FramePresenter(CameraFragment fragment) {
        mFragment = fragment;
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onViewCreated(View view) {

    }

    @Override
    public void onResume() {

    }

    @Override
    public void onDestroyView() {

    }

    public void onFrameBtnClick() {
        mCurrentMode = mCurrentMode.getNextMode();
        onFrameStatusChanged(mCurrentMode);
        CameraHelper cameraHelper= CameraHelper.getInstance();
        cameraHelper.onCameraFrameChanged(mCurrentMode);
    }

    public void onFrameStatusChanged(FrameMode mode) {
        if (getCameraViewGroup() == null) {
            return;
        }
        //目前只适配于普遍机型
        int width = CommonUtil.getScreenShortAxis();
        int height;
        switch(mode) {
            case FRAME_1_1:
                height = (int) (width / RATIO_1_1);
                break;
            case FRAME_3_4:
                height = (int) (width / RATIO_3_4);
                break;
            case FRAME_9_16:
            default:
                height = (int) (width / RATIO_9_16);
        }
        setFrameSize(width, height);
        setCameraViewTopMargin(getFrameTopMargin(mode));
    }

    private void setFrameSize(int width, int height) {
        if (getCameraViewGroup() == null) {
            return;
        }
        ViewGroup.LayoutParams cameraViewLayoutParams = getCameraViewGroup().getLayoutParams();
        cameraViewLayoutParams.width = width;
        cameraViewLayoutParams.height = height;
        getCameraViewGroup().setLayoutParams(cameraViewLayoutParams);
    }

    private void setCameraViewTopMargin(int topMargin) {
        if (getCameraViewGroup() == null) {
            return;
        }
        ViewGroup.LayoutParams cameraViewLayoutParams = getCameraViewGroup().getLayoutParams();
        if (cameraViewLayoutParams instanceof ViewGroup.MarginLayoutParams) {
            ((ViewGroup.MarginLayoutParams) cameraViewLayoutParams).topMargin = topMargin;
        }
        getCameraViewGroup().setLayoutParams(cameraViewLayoutParams);
    }

    /**
     * 画幅位置规则
     * 3:4画幅与9:16顶对齐
     * 1:1画幅与3:4居中对齐
     * @param mode
     * @return
     */
    private int getFrameTopMargin(FrameMode mode) {
        switch(mode) {
            //默认宽比高小
            case FRAME_3_4:
            case FRAME_9_16:
                int width = CommonUtil.getScreenShortAxis();
                return (CommonUtil.getScreenLongAxis() - (int) (width / RATIO_9_16)) / 2;
            case FRAME_1_1:
                int height_3_4 = (int)(CommonUtil.getScreenShortAxis() / RATIO_3_4);
                int height_1_1 = CommonUtil.getScreenShortAxis();
                return getFrameTopMargin(FRAME_3_4) + (height_3_4 - height_1_1) / 2;
            default:
                return getFrameTopMargin(FRAME_9_16);
        }

    }

    private View getCameraViewGroup() {
        return mFragment.getCameraViewGroup();
    }
}
