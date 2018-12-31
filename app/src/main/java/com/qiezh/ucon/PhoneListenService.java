package com.qiezh.ucon;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

public class PhoneListenService extends Service {
    public PhoneListenService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
    public static final String TAG = PhoneListenService.class.getSimpleName();

    public static final String ACTION_REGISTER_LISTENER = "action_register_listener";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand action: " + intent.getAction() +
                " flags: " + flags + " startId: " + startId);
        String action = intent.getAction();
        if (action.equals(ACTION_REGISTER_LISTENER)) {
            registerPhoneStateListener();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void registerPhoneStateListener() {
        CustomPhoneStateListener customPhoneStateListener = new CustomPhoneStateListener(this);
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        Log.d(TAG, "registerPhoneStateListener 1");
        if (telephonyManager != null) {
            Log.d(TAG, "registerPhoneStateListener 2");
            telephonyManager.listen(customPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        }
    }
}
