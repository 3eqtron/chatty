package com.example.firstapp.network;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.HeaderMap;
import retrofit2.http.POST;

public interface ApiServices {
    @POST("send")
    Call<String> sendmessage(
            @HeaderMap HashMap<String ,String> headers,
            @Body String messageBody
            );
}
