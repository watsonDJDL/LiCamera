package com.linfeng.licamera.imageEditor.fragment;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.linfeng.licamera.R;
import com.linfeng.licamera.imageEditor.EditImageActivity;
import com.linfeng.licamera.imageEditor.EditorBaseActivity;
import com.linfeng.licamera.imageEditor.ModuleConfig;
import com.linfeng.licamera.imageEditor.model.RatioItem;
import com.linfeng.licamera.imageEditor.view.CropImageView;
import com.linfeng.licamera.imageEditor.view.image.ImageViewTouchBase;
import com.linfeng.licamera.util.Matrix3;

import java.util.ArrayList;
import java.util.List;

public class CropFragment extends BaseEditFragment {
    public static final int INDEX = ModuleConfig.INDEX_CROP;
    public static final String TAG = CropFragment.class.getName();
    private View mainView;
    private View backToMenu;// 返回主菜单
    public CropImageView mCropPanel;// 剪裁操作面板
    private LinearLayout ratioList;
    private static List<RatioItem> dataList = new ArrayList<RatioItem>();
    static {
        // init data
        dataList.add(new RatioItem("自定义", -1f));
        dataList.add(new RatioItem("1:1", 1f));
        dataList.add(new RatioItem("1:2", 1 / 2f));
        dataList.add(new RatioItem("1:3", 1 / 3f));
        dataList.add(new RatioItem("3:4", 3 / 4f));
        dataList.add(new RatioItem("2:1", 2f));
        dataList.add(new RatioItem("4:3", 4 / 3f));
    }
    private List<TextView> textViewList = new ArrayList<TextView>();

    public static int SELECTED_COLOR = Color.YELLOW;
    public static int UNSELECTED_COLOR = Color.WHITE;
    private CropRationClick mCropRationClick = new CropRationClick();
    public TextView selectedTextView;

    public static CropFragment newInstance() {
        CropFragment fragment = new CropFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mainView = inflater.inflate(R.layout.fragment_edit_image_crop, null);
        return mainView;
    }

    private void setUpRatioList() {
        ratioList.removeAllViews();
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER_VERTICAL;
        params.leftMargin = 20;
        params.rightMargin = 20;
        for (int i = 0, len = dataList.size(); i < len; i++) {
            TextView text = new TextView(activity);
            text.setTextColor(UNSELECTED_COLOR);
            text.setTextSize(20);
            text.setText(dataList.get(i).getText());
            textViewList.add(text);
            ratioList.addView(text, params);
            text.setTag(i);
            if (i == 0) {
                selectedTextView = text;
            }
            dataList.get(i).setIndex(i);
            text.setTag(dataList.get(i));
            text.setOnClickListener(mCropRationClick);
        }
        selectedTextView.setTextColor(SELECTED_COLOR);
    }

    /**
     * 选择剪裁比率
     */
    private final class CropRationClick implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            TextView curTextView = (TextView) v;
            selectedTextView.setTextColor(UNSELECTED_COLOR);
            RatioItem dataItem = (RatioItem) v.getTag();
            selectedTextView = curTextView;
            selectedTextView.setTextColor(SELECTED_COLOR);
            mCropPanel.setRatioCropRect(activity.mainImage.getBitmapRect(), dataItem.getRatio());
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        backToMenu = mainView.findViewById(R.id.back_to_main);
        ratioList = mainView.findViewById(R.id.ratio_list_group);
        setUpRatioList();
        this.mCropPanel = ensureEditActivity().mCropPanel;
        backToMenu.setOnClickListener(new BackToMenuClick());// 返回主菜单
    }

    @Override
    public void onShow() {
        activity.mode = EditImageActivity.MODE_CROP;
        activity.mCropPanel.setVisibility(View.VISIBLE);
        activity.mainImage.setImageBitmap(activity.getMainBit());
        activity.mainImage.setDisplayType(ImageViewTouchBase.DisplayType.FIT_TO_SCREEN);
        activity.mainImage.setScaleEnabled(false);// 禁用缩放
        activity.bannerFlipper.showNext();
        activity.mainImage.post(() -> {
            final RectF r = activity.mainImage.getBitmapRect();
            activity.mCropPanel.setCropRect(r);
        });
    }

    /**
     * 返回按钮逻辑
     */
    private final class BackToMenuClick implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            backToMain();
        }
    }

    /**
     * 返回主菜单
     */
    @Override
    public void backToMain() {
        activity.mode = EditImageActivity.MODE_NONE;
        mCropPanel.setVisibility(View.GONE);
        activity.mainImage.setScaleEnabled(true);// 恢复缩放功能
        activity.bottomGallery.setCurrentItem(0);
        if (selectedTextView != null) {
            selectedTextView.setTextColor(UNSELECTED_COLOR);
        }
        mCropPanel.setRatioCropRect(activity.mainImage.getBitmapRect(), -1);
        activity.bannerFlipper.showPrevious();
    }

    /**
     * 保存剪切图片
     */
    public void applyCropImage() {
        CropImageTask task = new CropImageTask();
        task.execute(activity.getMainBit());
    }

    /**
     * 图片剪裁生成 异步任务
     */
    private final class CropImageTask extends AsyncTask<Bitmap, Void, Bitmap> {
        private Dialog dialog;

        @Override
        protected void onCancelled() {
            super.onCancelled();
            dialog.dismiss();
        }

        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        @Override
        protected void onCancelled(Bitmap result) {
            super.onCancelled(result);
            dialog.dismiss();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = EditorBaseActivity.getLoadingDialog(getActivity(), R.string.saving_image,
                    false);
            dialog.show();
        }

        @Override
        protected Bitmap doInBackground(Bitmap... params) {
            RectF cropRect = mCropPanel.getCropRect();// 剪切区域矩形
            Matrix touchMatrix = activity.mainImage.getImageViewMatrix();
            float[] data = new float[9];
            touchMatrix.getValues(data);// 底部图片变化记录矩阵原始数据
            Matrix3 cal = new Matrix3(data);// 辅助矩阵计算类
            Matrix3 inverseMatrix = cal.inverseMatrix();// 计算逆矩阵
            Matrix m = new Matrix();
            m.setValues(inverseMatrix.getValues());
            m.mapRect(cropRect);// 变化剪切矩形
            Bitmap resultBit = Bitmap.createBitmap(params[0],
                    (int) cropRect.left, (int) cropRect.top,
                    (int) cropRect.width(), (int) cropRect.height());
            return resultBit;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            dialog.dismiss();
            if (result == null)
                return;
            activity.changeMainBitmap(result,true);
            activity.mCropPanel.setCropRect(activity.mainImage.getBitmapRect());
            backToMain();
        }
    }
}
