package com.linfeng.licamera.service;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

/**
 * 文字识别相关的网络请求接口
 */
public interface ApiService {
    /**
     * 获取鉴权认证Token
     * @param grant_type 类型
     * @param client_id API Key
     * @param client_secret Secret Key
     * @return GetTokenResponse
     */
    @FormUrlEncoded
    @POST("/oauth/2.0/token")
    Call<GetTokenResponse> getToken(@Field("grant_type") String grant_type,
                                    @Field("client_id") String client_id,
                                    @Field("client_secret") String client_secret);

    /**
     * 获取图像识别结果
     * @param accessToken 获取鉴权认证Token
     * @param url 网络图片Url
     * @return JsonObject
     */
    @FormUrlEncoded
    @POST("/rest/2.0/image-classify/v2/advanced_general")
    @Headers("Content-Type:application/x-www-form-urlencoded; charset=utf-8")
    Call<GetDiscernResultResponse> getDiscernResult(@Field("access_token") String accessToken,
                                                    @Field("image") String image,
                                                    @Field("url") String url);
}
