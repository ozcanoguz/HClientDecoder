package com.hclient.decoder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "HClient Decoder";
    TextView textViewFilename;
    TextView textViewDecoded;

    MyBroadcastReceiver receiver;
    List<String> textList;

    //Start Text Fields of notification
    public void init(){
        textViewFilename = (TextView) findViewById(R.id.decodedTextFilename);
        textViewDecoded = (TextView) findViewById(R.id.decodedText);
    }

    public void updateView(String fileName, String fileContent){
        //UpdateView from broadcastReceiver, so that onMessageReceived can update mainactivity UI
        textViewFilename.setText(fileName);
        textViewDecoded.setText(fileContent);
    }

    @Override
    protected void onResume() {
        textList = new ArrayList<String>();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.hclient.decoder.onMessageReceived");
        receiver = new MyBroadcastReceiver();
        registerReceiver(receiver, intentFilter);
        super.onResume();
    }

    @Override
    protected void onStop()
    {
        unregisterReceiver(receiver);
        super.onStop();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                String pnToken = FirebaseInstanceId.getInstance().getToken();
                FirebaseMessaging.getInstance().subscribeToTopic("upload");
                Log.d(TAG, "PN token: " + pnToken);
            }
        });
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    //BroadcastReceiver that holds connection between FirebaseMessagingService
    private class MyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle extras = intent.getExtras();
            String filePath = extras.getString("filePath");
            String fileContent = extras.getString("fileContent");
            updateView(filePath,fileContent);// update your textView in the main layout
        }
    }



}
