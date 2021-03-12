package com.example.licamera;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class PictureFragment extends BaseFragment{
    private PicturePresenter mPicturePresenter;

    public PictureFragment(Bitmap bitmap) {
        mPicturePresenter = new PicturePresenter(this, bitmap);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPicturePresenter.onViewCreated(view);
        Button backButton = view.findViewById(R.id.delete_button);
        backButton.setOnClickListener(v -> onBackBtnClick());
        Button selectButton = view.findViewById(R.id.select_button);
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
        mPicturePresenter.saveImage();
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
