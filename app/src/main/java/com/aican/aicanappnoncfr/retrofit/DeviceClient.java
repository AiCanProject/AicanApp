package com.aican.aicanappnoncfr.retrofit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class DeviceClient {
    private static Gson gson = new GsonBuilder()
            .setLenient()
            .create();

    private static Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("http://192.168.4.1:83/")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build();

    public static DeviceService client = retrofit.create(DeviceService.class);

}
