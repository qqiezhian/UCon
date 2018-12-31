package com.qiezh.ucon;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.media.MediaRecorder;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.os.SystemClock.sleep;

public class PhoneRecorder {
    private Context mContext;
    private static final String LOG_TAG = "AudioRecordTest";
    private static String mFilePath = null;
    private static String mFileName = null;

    private MediaRecorder mRecorder = null;

    public PhoneRecorder(Context context) {
        mContext = context;
        mFilePath = Environment.getExternalStorageDirectory().getAbsolutePath();
        //mFilePath = Environment.getDataDirectory().getAbsolutePath();
        Log.d("AudioRecorder Output", mFilePath);
    }

    public void onRecord(boolean start) {
        if (start) {
            startRecording();
        } else {
            stopRecording();
        }
    }

    private void startRecording() {
        setFileName();
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile(mFileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }

        mRecorder.start();
    }

    private void stopRecording() {
        mRecorder.setOnErrorListener(null);
        mRecorder.setOnInfoListener(null);
        mRecorder.setPreviewDisplay(null);
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;
        sleep(100);
        MyUploadService.uploadBinary(mContext, mFileName);
    }
    private void setFileName() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        Date date = new Date(System.currentTimeMillis());
        String currentDate = simpleDateFormat.format(date);

        mFileName = mFilePath + "/" + currentDate + "-audiorecordtest.3gp";
        Log.d("SetFileName", mFileName);
    }

}
