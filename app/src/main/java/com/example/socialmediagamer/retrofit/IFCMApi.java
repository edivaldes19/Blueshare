package com.example.socialmediagamer.retrofit;

import com.example.socialmediagamer.models.FCMBody;
import com.example.socialmediagamer.models.FCMResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface IFCMApi {
    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAmHPoloo:APA91bEovuBWuV8Qc5pZ6Z03KMq0q7a0h3sHN_QEExBWWTmENokBv7zJoBM6UAaJU7pGtKWUiCF30UEMIf-Z4fAqCFdDZA8eb56PVrLdxx1yQkT2xz3r_m1vE2Z02xvnnvanef9rzqcc"
    })
    @POST("fcm/send")
    Call<FCMResponse> send(@Body FCMBody body);
}