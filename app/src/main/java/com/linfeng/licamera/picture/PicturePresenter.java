package com.linfeng.licamera.picture;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.baidu.ocr.sdk.OCR;
import com.baidu.ocr.sdk.OnResultListener;
import com.baidu.ocr.sdk.exception.OCRError;
import com.baidu.ocr.sdk.model.AccessToken;
import com.baidu.ocr.sdk.model.GeneralBasicParams;
import com.baidu.ocr.sdk.model.GeneralResult;
import com.baidu.ocr.sdk.model.WordSimple;
import com.linfeng.licamera.R;
import com.linfeng.licamera.base.BasePresenter;
import com.linfeng.licamera.imageEditor.EditImageActivity;
import com.linfeng.licamera.service.ApiService;
import com.linfeng.licamera.service.GetDiscernResultResponse;
import com.linfeng.licamera.service.GetTokenResponse;
import com.linfeng.licamera.service.NetCallBack;
import com.linfeng.licamera.service.ServiceGenerator;
import com.linfeng.licamera.util.BitmapUtils;
import com.linfeng.licamera.util.CommonUtil;
import com.linfeng.licamera.util.Constant;
import com.linfeng.licamera.util.FileUtil;
import com.linfeng.licamera.util.SPUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

import static com.linfeng.licamera.MainActivity.ACTION_REQUEST_EDIT_IMAGE;

public class PicturePresenter implements BasePresenter {
    private static final String TAG = "PicturePresenter";
    private static final String API_KEY = "PHeN40zP7FSVgGyZ3zyLPqXi";
    private static final String SECRET_KEY = "DbPNdjhCh7SHfNQRKaWTKYwCuO79BN08";
    private Context context = CommonUtil.context();
    private PictureFragment mFragment;
    public Bitmap mBitmap;
    private String path;
    private ApiService service;
    private String accessToken;
    public PicturePresenter(PictureFragment pictureFragment, Bitmap bitmap) {
        mFragment = pictureFragment;
        mBitmap = bitmap;
    }

    public PicturePresenter(PictureFragment pictureFragment) {
        mFragment = pictureFragment;
    }

    @Override
    public void onCreate() {
        path = mFragment.getContext().getExternalCacheDir().toString() + File.separator + "cache.jpg";
        Log.d(TAG, "cache picture path is : " + path);
        BitmapUtils.saveBitmap(mBitmap, path);
        service = ServiceGenerator.createService(ApiService.class);
    }

    @Override
    public void onViewCreated(View view) {
        ImageView captureView = view.findViewById(R.id.picture_view);
        captureView.setImageBitmap(mBitmap);
        ImageView characterBtn = view.findViewById(R.id.character_recognition_btn);
        ImageView pictureBtn = view.findViewById(R.id.picture_recognition_btn);
        characterBtn.setOnClickListener(v -> {
            requestIfNoInternetPermission();
            onCharacterBtnClick();
        });
        pictureBtn.setOnClickListener(v -> {
            requestIfNoInternetPermission();
            onPictureBtnClick();
        });
        initRecognitionSDK();
    }

    private void initRecognitionSDK() {
        OCR.getInstance(CommonUtil.context()).initAccessTokenWithAkSk(new OnResultListener() {
            @Override
            public void onResult(Object o) {
                // 调用成功，返回AccessToken对象
                String token = ((AccessToken)o).getAccessToken();
            }

            @Override public void onError(OCRError error) {
                // 调用失败，返回OCRError子类SDKError对象
            }
        }, CommonUtil.context(), API_KEY, SECRET_KEY);
    }

    @Override
    public void onResume() {

    }

    @Override
    public void onDestroyView() {

    }

    public void onSelectBtnClick() {
        File outputFile = FileUtil.genEditFile();
        EditImageActivity.start(mFragment.getActivity(),path,outputFile.getAbsolutePath(),ACTION_REQUEST_EDIT_IMAGE);
        //saveImage();
    }

    private void onCharacterBtnClick() {
        onCharacterRecognized();
    }

    private void onPictureBtnClick() {
        onPictureRecognized();
    }

    private void requestIfNoInternetPermission() {
        if(ContextCompat.checkSelfPermission(mFragment.getContext(), Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(mFragment.getActivity(),new String[]{Manifest.permission.INTERNET},0);
        }
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

    private void onCharacterRecognized() {
        GeneralBasicParams param = new GeneralBasicParams();
        param.setDetectDirection(true);
        param.setImageFile(new File(path));
        Log.d(TAG, "filePath : " + path);

        recGeneralBasic(CommonUtil.context(), path, result -> Log.d(TAG, "result is : " + result));
    }

    public static void recGeneralBasic(Context context, String filePath, final ServiceListener listener) {
        GeneralBasicParams param = new GeneralBasicParams();
        param.setDetectDirection(true);
        param.setImageFile(new File(filePath));
        OCR.getInstance(context).
                recognizeGeneralBasic(param, new OnResultListener<GeneralResult>() {
                    @Override
                    public void onResult(GeneralResult result) {
                        StringBuilder sb = new StringBuilder();
                        for (WordSimple wordSimple : result.getWordList()) {
                            WordSimple word = wordSimple;
                            sb.append(word.getWords());
                            sb.append("\n");
                        }
                        listener.onResult(result.getJsonRes());
                    }

                    @Override
                    public void onError(OCRError error) {
                        Log.d(TAG, "error :" + error.getMessage());
                        listener.onResult(error.getMessage());
                    }
                });
    }

    private void onPictureRecognized() {
        try {
            String token = getAccessToken();
            //通过图片路径显示图片
            //Glide.with(this).load(imagePath).into(ivPicture);
            //按字节读取文件
            byte[] imgData = FileUtil.readFileByBytes(path);
            //字节转Base64
            String imageBase64 = BitmapUtils.encode(imgData);
            //图像识别
            ImageDiscern(token, imageBase64, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取鉴权Token
     */
    private String getAccessToken() {
        String token = SPUtils.getString(Constant.TOKEN, null, context);
        if (token == null) {
            //访问API获取接口
            requestApiGetToken();
        } else {
            //则判断Token是否过期
            if (isTokenExpired()) {
                //过期
                requestApiGetToken();
            } else {
                accessToken = token;
            }
        }
        return accessToken;
    }

    /**
     * Token是否过期
     *
     * @return
     */
    private boolean isTokenExpired() {
        //获取Token的时间
        long getTokenTime = SPUtils.getLong(Constant.GET_TOKEN_TIME, 0, context);
        //获取Token的有效时间
        long effectiveTime = SPUtils.getLong(Constant.TOKEN_VALID_PERIOD, 0, context);
        //获取当前系统时间
        long currentTime = System.currentTimeMillis() / 1000;

        return (currentTime - getTokenTime) >= effectiveTime;
    }


    /**
     * 图像识别请求
     *
     * @param token       token
     * @param imageBase64 图片Base64
     * @param imgUrl      网络图片Url
     */
    private void ImageDiscern(String token, String imageBase64, String imgUrl) {
        Log.d(TAG, "token is : " + token);
        service.getDiscernResult(token, imageBase64, imgUrl).enqueue(new NetCallBack<GetDiscernResultResponse>() {
            @Override
            public void onSuccess(Call<GetDiscernResultResponse> call, retrofit2.Response<GetDiscernResultResponse> response) {
                Log.d(TAG, "response is :" + response.body());
                List<GetDiscernResultResponse.ResultBean> result = response.body() != null ? response.body().getResult() : null;
                if (result != null && result.size() > 0) {
                    Log.d(TAG, "result: " + result);
                    //显示识别结果
                    //showDiscernResult(result);
                } else {
                    Log.d(TAG, "result: " + result);
                    //pbLoading.setVisibility(View.GONE);
                    //showMsg("未获得相应的识别结果");
                }
            }

            @Override
            public void onFailed(String errorStr) {
                //pbLoading.setVisibility(View.GONE);
                Log.e(TAG, "图像识别失败，失败原因：" + errorStr);
            }
        });
    }

    /**
     * 访问API获取接口
     */
    private void requestApiGetToken() {
        String grantType = "client_credentials";
        service.getToken(grantType, API_KEY, SECRET_KEY)
                .enqueue(new NetCallBack<GetTokenResponse>() {
                    @Override
                    public void onSuccess(Call<GetTokenResponse> call, Response<GetTokenResponse> response) {
                        if (response.body() != null) {
                            //鉴权Token
                            accessToken = response.body().getAccess_token();
                            //过期时间 秒
                            long expiresIn = response.body().getExpires_in();
                            //当前时间 秒
                            long currentTimeMillis = System.currentTimeMillis() / 1000;
                            //放入缓存
                            SPUtils.putString(Constant.TOKEN, accessToken, context);
                            SPUtils.putLong(Constant.GET_TOKEN_TIME, currentTimeMillis, context);
                            SPUtils.putLong(Constant.TOKEN_VALID_PERIOD, expiresIn, context);
                        }
                    }

                    @Override
                    public void onFailed(String errorStr) {
                        Log.e(TAG, "获取Token失败，失败原因：" + errorStr);
                        accessToken = null;
                    }
                });
    }

    interface ServiceListener {
        public void onResult(String result);
    }
}
