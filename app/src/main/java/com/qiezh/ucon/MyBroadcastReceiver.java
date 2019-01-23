package com.qiezh.ucon;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class MyBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
            String action = intent.getAction();
            if(action.equals(Intent.ACTION_TIME_TICK)) {
                //do what you want to do ...13
                Log.d("BroadcastReceiver", "every minute");
            }
    }

    private void isServiceRunning() {
        boolean isServiceRunning = false;
        ActivityManager manager = (ActivityManager)ThisApp.getContext().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service :manager.getRunningServices(Integer.MAX_VALUE)) {
            if("so.xxxx.WidgetUpdateService".equals(service.service.getClassName()))
//Service的类名
            {
                isServiceRunning = true;
            }
        }
        if (!isServiceRunning) {
            registerPhoneStateListener
        }
    }
}
