package com.hclient.decoder.communication;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


/*
 *  Singleton rest interface client
 */
public class RestInterfaceClient {
    private static Retrofit retrofit = null;
    public static Retrofit getClient(String baseUrl) {
        if (retrofit==null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
