package com.linfeng.licamera.picture;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.FileUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.linfeng.licamera.R;
import com.linfeng.licamera.base.BaseFragment;
import com.linfeng.licamera.imageEditor.EditImageActivity;
import com.linfeng.licamera.util.FileUtil;

import java.io.File;

import static com.linfeng.licamera.MainActivity.ACTION_REQUEST_EDIT_IMAGE;

public class PictureFragment extends BaseFragment {
    private PicturePresenter mPicturePresenter;

    public PictureFragment(Bitmap bitmap) {
        mPicturePresenter = new PicturePresenter(this, bitmap);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPicturePresenter.onCreate();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPicturePresenter.onViewCreated(view);
        ImageButton backButton = view.findViewById(R.id.delete_button);
        backButton.setOnClickListener(v -> onBackBtnClick());
        ImageButton selectButton = view.findViewById(R.id.select_button);
        selectButton.setOnClickListener(v -> onSelectButtonClick());
    }

    private void onBackBtnClick() {
     backToCamera();
    }

    public void backToCamera() {
        getParentFragmentManager().popBackStack();
        mPicturePresenter.mBitmap = null;
    }

    private void onSelectButtonClick() {
        mPicturePresenter.onSelectBtnClick();
    }

    public void showSaveImageSuccessfully() {
        Toast toast = Toast.makeText(getContext(), "成功保存到相册！", Toast.LENGTH_SHORT);
        toast.show();
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.picture_intermediate_state_layout, container, false);
    }
}
