package com.hclient.decoder.communication;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.Streaming;


public interface RestInterfaceController {
    //Retrofit can use REST API by using query pattern
    @GET("/api/download")
    @Streaming
    Call<ResponseBody> download(@Query("fileName") String fileName);
}
