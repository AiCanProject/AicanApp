package com.aican.aicanapp.retrofit;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface DeviceService {

    //  https://192.168.4.1:83/setting?ssid=Device1&pass=123456

    @GET("setting")
    Call<ResponseBody> connect(@Query("ssid") String ssid, @Query("pass") String pass);

}
