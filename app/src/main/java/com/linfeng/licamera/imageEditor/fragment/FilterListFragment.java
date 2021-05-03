package com.linfeng.licamera.imageEditor.fragment;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.Color;
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
import com.linfeng.licamera.imageEditor.PhotoProcessing;
import com.linfeng.licamera.imageEditor.view.image.ImageViewTouchBase;

public class FilterListFragment extends BaseEditFragment {
    public static final int INDEX = ModuleConfig.INDEX_FILTER;
    public static final String TAG = FilterListFragment.class.getName();
    private View mainView;
    private View backBtn;// 返回主菜单按钮

    private Bitmap filterBit;// 滤镜处理后的bitmap

    private LinearLayout mFilterGroup;// 滤镜列表
    private String[] filters;
    private Bitmap currentBitmap;// 标记变量

    public static FilterListFragment newInstance() {
        FilterListFragment fragment = new FilterListFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mainView = inflater.inflate(R.layout.fragment_edit_image_fliter, null);
        return mainView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        backBtn = mainView.findViewById(R.id.back_to_main);
        mFilterGroup = (LinearLayout) mainView.findViewById(R.id.filter_group);

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backToMain();
            }
        });
        setUpFliters();
    }

    @Override
    public void onShow() {
        activity.mode = EditImageActivity.MODE_FILTER;
        activity.mFilterListFragment.setCurrentBitmap(activity.getMainBit());
        activity.mainImage.setImageBitmap(activity.getMainBit());
        activity.mainImage.setDisplayType(ImageViewTouchBase.DisplayType.FIT_TO_SCREEN);
        activity.mainImage.setScaleEnabled(false);
        activity.bannerFlipper.showNext();
    }

    /**
     * 返回主菜单
     */
    @Override
    public void backToMain() {
        currentBitmap = activity.getMainBit();
        filterBit = null;
        activity.mainImage.setImageBitmap(activity.getMainBit());// 返回原图
        activity.mode = EditImageActivity.MODE_NONE;
        activity.bottomGallery.setCurrentItem(0);
        activity.mainImage.setScaleEnabled(true);
        activity.bannerFlipper.showPrevious();
    }

    /**
     * 保存滤镜处理后的图片
     */
    public void applyFilterImage() {
        if (currentBitmap == activity.getMainBit()) {// 原始图片
            backToMain();
            return;
        } else {// 经滤镜处理后的图片
            activity.changeMainBitmap(filterBit,true);
            backToMain();
        }
    }

    /**
     * 装载滤镜
     */
    private void setUpFliters() {
        filters = getResources().getStringArray(R.array.filters);
        if (filters == null)
            return;

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER_VERTICAL;
        params.leftMargin = 20;
        params.rightMargin = 20;
        mFilterGroup.removeAllViews();
        for (int i = 0, len = filters.length; i < len; i++) {
            TextView text = new TextView(activity);
            text.setTextColor(Color.WHITE);
            text.setTextSize(20);
            text.setText(filters[i]);
            mFilterGroup.addView(text, params);
            text.setTag(i);
            text.setOnClickListener(new FliterClick());
        }
    }

    @Override
    public void onDestroy() {
        if (filterBit != null && (!filterBit.isRecycled())) {
            filterBit.recycle();
        }
        super.onDestroy();
    }

    /**
     * 选择滤镜效果
     */
    private final class FliterClick implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            int position = ((Integer) v.getTag()).intValue();
            if (position == 0) {// 原始图片效果
                activity.mainImage.setImageBitmap(activity.getMainBit());
                currentBitmap = activity.getMainBit();
                return;
            }
            // 滤镜处理
            ProcessingImage task = new ProcessingImage();
            task.execute(position);
        }
    }

    /**
     * 图片滤镜处理任务
     */
    private final class ProcessingImage extends AsyncTask<Integer, Void, Bitmap> {
        private Dialog dialog;
        private Bitmap srcBitmap;

        @Override
        protected Bitmap doInBackground(Integer... params) {
            int type = params[0];
            if (srcBitmap != null && !srcBitmap.isRecycled()) {
                srcBitmap.recycle();
            }

            srcBitmap = Bitmap.createBitmap(activity.getMainBit().copy(
                    Bitmap.Config.ARGB_8888, true));
            return PhotoProcessing.filterPhoto(srcBitmap, type);
        }

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
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            dialog.dismiss();
            if (result == null)
                return;
            if (filterBit != null && (!filterBit.isRecycled())) {
                filterBit.recycle();
            }
            filterBit = result;
            activity.mainImage.setImageBitmap(filterBit);
            currentBitmap = filterBit;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = EditorBaseActivity.getLoadingDialog(getActivity(), R.string.handing,
                    false);
            dialog.show();
        }

    }

    public Bitmap getCurrentBitmap() {
        return currentBitmap;
    }

    public void setCurrentBitmap(Bitmap currentBitmap) {
        this.currentBitmap = currentBitmap;
    }
}
