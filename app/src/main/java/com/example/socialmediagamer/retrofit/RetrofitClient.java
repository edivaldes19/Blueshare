package com.example.socialmediagamer.retrofit;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    public static Retrofit getClient(String url) {
        return new Retrofit.Builder().baseUrl(url).addConverterFactory(GsonConverterFactory.create()).build();
    }
}