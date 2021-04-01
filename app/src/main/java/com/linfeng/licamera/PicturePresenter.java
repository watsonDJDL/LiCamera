package com.linfeng.licamera;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;

import androidx.core.app.ActivityCompat;

import com.linfeng.licamera.base.BasePresenter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import jp.co.cyberagent.android.gpuimage.GPUImage;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageGrayscaleFilter;

public class PicturePresenter implements BasePresenter {
    PictureFragment mFragment;
    Bitmap mBitmap;
    public PicturePresenter(PictureFragment pictureFragment, Bitmap bitmap) {
        mFragment = pictureFragment;
        mBitmap = bitmap;
    }

    public PicturePresenter(PictureFragment pictureFragment) {
        mFragment = pictureFragment;
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onViewCreated(View view) {
        ImageView captureView = view.findViewById(R.id.picture_view);
        captureView.setImageBitmap(mBitmap);
    }

    @Override
    public void onResume() {

    }

    @Override
    public void onDestroyView() {

    }

    public void setTestFilter() {
        GPUImage gpuImage = new GPUImage(mFragment.getContext());
        gpuImage.setImage(mBitmap);
        gpuImage.setFilter(new GPUImageGrayscaleFilter());
        mBitmap = gpuImage.getBitmapWithFilterApplied();
    }

    public void saveImage() {
        verifyStoragePermissions(mFragment.getActivity());
        if (mBitmap != null) {
            if(saveImageToGallery(mFragment.getContext(), mBitmap)) {
                mFragment.showSaveImageSuccessfully();
                mFragment.backToCamera();
            }
        }
    }

    //保存文件到指定路径
    public static boolean saveImageToGallery(Context context, Bitmap bmp) {
        // 首先保存图片
        String storePath = context.getExternalCacheDir().toString() + File.separator + "dearxy";
        File appDir = new File(storePath);
        if (!appDir.exists()) {
            appDir.mkdirs();
        }
        String fileName = System.currentTimeMillis() + ".jpg";
        File file = new File(appDir, fileName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            //通过io流的方式来压缩保存图片
            boolean isSuccess = bmp.compress(Bitmap.CompressFormat.JPEG, 60, fos);
            fos.flush();
            fos.close();

            //把文件插入到系统图库
            MediaStore.Images.Media.insertImage(context.getContentResolver(), file.getAbsolutePath(), fileName, null);

            //保存图片后发送广播通知更新数据库
            Uri uri = Uri.fromFile(file);
            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
            if (isSuccess) {
                return true;
            } else {
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static final int REQUEST_EXTERNAL_STORAGE = 1;

    private static String[] PERMISSIONS_STORAGE = {"android.permission.READ_EXTERNAL_STORAGE","android.permission.WRITE_EXTERNAL_STORAGE" };

    public static void verifyStoragePermissions(Activity activity){
        try{
            //检测是否有写的权限
            int permission= ActivityCompat.checkSelfPermission(activity, "android.permission.WRITE_EXTERNAL_STORAGE");
            if(permission!= PackageManager.PERMISSION_GRANTED){
                // 没有写的权限，去申请写的权限，会弹出对话框
                ActivityCompat.requestPermissions(activity,PERMISSIONS_STORAGE,REQUEST_EXTERNAL_STORAGE);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}