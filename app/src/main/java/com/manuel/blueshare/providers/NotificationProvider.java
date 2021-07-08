package com.manuel.blueshare.providers;

import com.manuel.blueshare.models.FCMBody;
import com.manuel.blueshare.models.FCMResponse;
import com.manuel.blueshare.retrofit.IFCMApi;
import com.manuel.blueshare.retrofit.RetrofitClient;

import retrofit2.Call;

public class NotificationProvider {
    public NotificationProvider() {
    }

    public Call<FCMResponse> sendNotification(FCMBody body) {
        String url = "https://fcm.googleapis.com";
        return RetrofitClient.getClient(url).create(IFCMApi.class).send(body);
    }
}