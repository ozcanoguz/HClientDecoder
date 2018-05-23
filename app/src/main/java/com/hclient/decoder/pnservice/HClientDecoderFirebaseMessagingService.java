package com.hclient.decoder.pnservice;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import android.support.v4.app.NotificationCompat;
import android.util.Log;


import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.hclient.decoder.MainActivity;
import com.hclient.decoder.R;
import com.hclient.decoder.communication.RestInterfaceController;
import com.hclient.decoder.communication.utils.RestInterfaceUtils;
import com.hclient.decoder.decode.CharacterCount;
import com.hclient.decoder.decode.Read;
import com.hclient.decoder.decode.Write;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class HClientDecoderFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "FCM Service";
    private static Read reader;
    private static Write writer;
    String filePath = "";
    String decodedText = "";
    private boolean downloaded = false;
    private boolean decoded = false;
    FileOutputStream fileOutputStream;
    RestInterfaceController restInterfaceController = RestInterfaceUtils.getRestInterfaceController();


    public void downloadFileFromUrl(final String content) {
        //Use retrofit controller to make simple GET operation into our Server
        restInterfaceController.download(content).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, final Response<ResponseBody> response) {
                fileOutputStream = null;
                try {
                    //Get Response of file and let's begin to write this file to our SDCARD
                    //Our samsung phone can't use getExternalPublicDir() method, so below worked for me.
                    String pathSecond = System.getenv("SECONDARY_STORAGE") + "/Android/data/com.hclient.decoder/files/";
                    File path = new File(pathSecond);
                    final File file = new File(path, content);
                    if (!file.exists())
                        file.createNewFile();
                    //Use AsyncTask to not to stop main thread, writing file is a hard operation so we can face with NetworkGuard exception
                    AsyncTask.execute(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                byte[] data = response.body().bytes();
                                try {
                                    FileOutputStream fos;
                                    fos = new FileOutputStream(file);
                                    fos.write(data);
                                    fos.flush();
                                    fos.close();
                                } catch (FileNotFoundException e) {
                                    e.printStackTrace();
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    Log.i(TAG, "File downloaded successfuly");
                    downloaded = true;
                    Log.i(TAG, pathSecond);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "Error on download service");
            }
        });
    }

    @SuppressLint("WrongThread")
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "From: " + remoteMessage.getFrom());
        final String fileName = remoteMessage.getData().get("fileName");
        /*decodedText = "";
        decoded = false;
        downloaded = false;
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                //Download file from url that has been sent in fileName of data
                downloadFileFromUrl(fileName);
            }
        });

        //AsyncTasks should wait each other, otherwise we could face with such an errors, file can't be found
        //Scenario : while doing get, but didn't write file into phone, try to get the file from directory
        //Be aware of FileNotFoundException!
        do {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while (downloaded == false);

        //Let's decode our file, simple huffman decode mechanism is implemented in decode package, by using character counts and print heaps
        //Server uploads the file in the format .huf, so it has been decoded and binary.
        //We are getting text by implying algorithm simply counting characters
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                //reader = new Read(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + fileName);
                try {
                    String pathSecond = System.getenv("SECONDARY_STORAGE") + "/Android/data/com.hclient.decoder/files/";
                    BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(new File(pathSecond + "/" + fileName)));
                    reader = new Read(bufferedInputStream);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                try {
                    String pathSecond = System.getenv("SECONDARY_STORAGE") + "/Android/data/com.hclient.decoder/files/";
                    String timeString = fileName.substring(fileName.lastIndexOf("_") + 1) + ".txt";
                    filePath = pathSecond + "/" + fileName.substring(0, fileName.length() - 22) + "_" + timeString;
                    writer = new Write(new BufferedOutputStream(new FileOutputStream(new File(filePath))));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                CharacterCount root = decode();
                printHeap(root);
                try {
                    int lengthOfFile = reader.readInt();

                    for (int i = 0; i < lengthOfFile; i++) {
                        CharacterCount node = root;
                        while (node.isNode) {
                            boolean bit = reader.readBit();
                            if (bit) {
                                node = node.right;
                            } else {
                                node = node.left;
                            }
                        }
                        decodedText = decodedText + node.ch;
                        writer.write(node.ch);
                    }
                    reader.close();
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
                writer.close();
                decoded = true;
            }
        });

        //AsyncTasks should wait each other, otherwise we could face with such an errors, file can't be found
        //Scenario : Parallel thread mechanism couldn't create all content of file
        //Be aware of File content can be empty here ;)!
        do {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while (decoded == false);
        */

        //show Notification to user, that we've received and decoded a file
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                // optional, this is to make beautiful icon
                .setLargeIcon(BitmapFactory.decodeResource(
                        getResources(), R.mipmap.ic_launcher))
                .setSmallIcon(R.mipmap.ic_launcher).setContentText("Filecontent : ")
                .setContentTitle(fileName + " Content");
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(getApplicationContext(),
                (int) System.currentTimeMillis(), intent, 0);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, notificationBuilder.build());

        //Sent decoded file directory and content of this file into UI
       /* Intent intent2 = new Intent();
        intent2.putExtra("filePath", filePath);
        intent2.putExtra("fileContent", decodedText);
        intent2.setAction("com.hclient.decoder.onMessageReceived");
        sendBroadcast(intent2);*/
    }

    public static CharacterCount decode() {
        boolean isNode;
        try {
            isNode = reader.readBit();

            if (isNode) {
                CharacterCount left = decode();
                CharacterCount right = decode();

                return new CharacterCount(-1, right, left);
            } else {
                return new CharacterCount(reader.readChar(), -1);
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    public static void printHeap(CharacterCount character) {

        if (character.isNode) {
            if (character.right != null) {
                printHeap(character.right);
            } else {
                System.out.println("Right found null");
            }
            if (character.left != null) {
                printHeap(character.left);
            } else {
                System.out.println("Left found null");
            }

        }
    }

    public static void eraseFile(String filename) {
        File file = new File(filename);
        file.delete();
    }
}

