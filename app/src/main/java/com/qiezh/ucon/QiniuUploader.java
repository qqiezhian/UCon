package com.qiezh.ucon;

import android.util.Base64;
import android.util.Log;

import com.qiniu.android.http.ResponseInfo;
import com.qiniu.android.storage.UpCompletionHandler;
import com.qiniu.android.storage.UploadManager;
import com.qiniu.android.utils.UrlSafeBase64;

import org.json.JSONObject;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;


public class QiniuUploader {
    private String secret_key = "hRa6BuifYJH3AMhc4S4Q9b94S2OJ2Q1n2KGQzHY3";
    private String access_key = "x6vSLbq6kdVLWdY-TsQM-Z3GXi1sUoooDFCIY5fa";
    private UploadManager uploadManager;

    public QiniuUploader() {
        uploadManager = new UploadManager();
    }

    public void up(String file, String key) {
        //String token = genUpToken();
        //Log.d("genUpToken", token);
        uploadManager.put(file, key, genUpToken(key),
                new UpCompletionHandler() {
                    @Override
                    public void complete(String key, ResponseInfo info,
                                         JSONObject response) {
                        Log.d("QiniuUploader", info.toString());
                    }
                }, null);
    }

    public String genUpToken(String key) {
        String uploadToken = "";
        try {
            // 1 构造上传策略
            JSONObject _json = new JSONObject();
            long _dataline = System.currentTimeMillis() / 1000 + 3600;
            _json.put("scope", "trackx" + ":" + key);
            _json.put("deadline", _dataline);// 有效时间为一个小时
            //_json.put("scope", "trackx:2019-02-01-15-00-24.gpx");
            //_json.put("deadline", 1549036944);
            Log.d("json", _json.toString());
            String _encodedPutPolicy = UrlSafeBase64.encodeToString(_json.toString().getBytes());
            Log.d("_encodedPutPolicy ", _encodedPutPolicy);
            byte[] _sign = hmacSHA1Encrypt(_encodedPutPolicy, secret_key);
            //Log.d("_sign ", _sign);
            String _encodedSign = UrlSafeBase64.encodeToString(_sign);
            Log.d("_encodedSign ", _encodedSign);
            uploadToken = access_key + ':' + _encodedSign + ':' + _encodedPutPolicy;
            Log.d("QiniuUploader", uploadToken);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return uploadToken;
    }

    private static byte[] hmacSHA1Encrypt(String base, String key)
            throws NoSuchAlgorithmException, InvalidKeyException {
        String type = "HmacSHA1";
        SecretKeySpec secret = new SecretKeySpec(key.getBytes(), type);
        Mac mac = Mac.getInstance(type);
        mac.init(secret);
        //byte[] digest = mac.doFinal(base.getBytes());
        return  mac.doFinal(base.getBytes());
    }
}
