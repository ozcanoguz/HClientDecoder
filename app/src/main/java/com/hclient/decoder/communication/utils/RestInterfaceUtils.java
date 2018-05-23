package com.hclient.decoder.communication.utils;

import com.hclient.decoder.communication.RestInterfaceClient;
import com.hclient.decoder.communication.RestInterfaceController;


public class RestInterfaceUtils {
    //AWS Server Public IP
    public static final String BASE_URL = "http://52.59.194.10:2100";
    public static RestInterfaceController getRestInterfaceController(){
        return RestInterfaceClient.getClient(BASE_URL).create(RestInterfaceController.class);
    }

}
