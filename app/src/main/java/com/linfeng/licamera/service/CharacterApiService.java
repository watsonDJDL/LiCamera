package com.linfeng.licamera.service;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

/**
 * 不用这个，后面重写
 * 文字识别相关的网络请求接口
 */
public interface CharacterApiService {
    @Multipart
    @POST("v1/recognizetext")
    Call<CharacterResponse> requestCharacterAnalyse(@Part("api_key") RequestBody key,
                                                    @Part("api_secret") RequestBody secret,
                                                    @Part MultipartBody.Part image);
}
