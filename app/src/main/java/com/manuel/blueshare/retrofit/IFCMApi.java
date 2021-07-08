package com.manuel.blueshare.retrofit;

import com.manuel.blueshare.models.FCMBody;
import com.manuel.blueshare.models.FCMResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface IFCMApi {
    @Headers({"Content-Type:application/json", "Authorization:key=AAAAaZ0EwQE:APA91bGt79jRiv7uvaLuDhHcUJI5YgxIz8ysi2j450v_12FYkIgdSpW2PWGajjYhqRthv8ibYz4Az4vxs37Klt--zAhqyG4ZMpOKlK1OY5tXUAVQtYj1bHKYAGWW80cP0Mb0AsZog3vN"})
    @POST("fcm/send")
    Call<FCMResponse> send(@Body FCMBody body);
}