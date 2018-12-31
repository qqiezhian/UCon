package com.qiezh.ucon;

import android.content.Context;
import android.util.Log;

import net.gotev.uploadservice.BinaryUploadRequest;
import net.gotev.uploadservice.UploadNotificationConfig;
import net.gotev.uploadservice.UploadService;
import net.gotev.uploadservice.okhttp.OkHttpStack;

import java.io.File;

import okhttp3.OkHttpClient;

public class MyUploadService {
    public static void InitUploadService() {
        // Gradle automatically generates proper variable as below.
        //UploadService.NAMESPACE = BuildConfig.APPLICATION_ID;
        // Or, you can define it manually.
        UploadService.NAMESPACE = "com.qiezh.ucon";
        OkHttpClient client = new OkHttpClient(); // create your own OkHttp client
        UploadService.HTTP_STACK = new OkHttpStack(client); // make the library use your own OkHttp client
    }
    public static void uploadBinary(final Context context, String filePath) {
        try {
            // starting from 3.1+, you can also use content:// URI string instead of absolute file
            //String filePath = "/absolute/path/to/file";
            String uploadId = filePath;
            BinaryUploadRequest request = new BinaryUploadRequest(context, uploadId,
                    "http://192.168.1.107:3000/upload/binary")
                    .setFileToUpload(filePath)
                    .addHeader("file-name", new File(filePath).getName())
                    //.setNotificationConfig(new UploadNotificationConfig())
                    .setMaxRetries(2);
            request.startUpload();
            Log.d("AndroidUploadService", filePath);
        } catch (Exception exc) {
            Log.e("AndroidUploadService", exc.getMessage(), exc);
        }
    }
}
