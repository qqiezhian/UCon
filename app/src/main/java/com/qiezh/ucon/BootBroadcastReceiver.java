package com.qiezh.ucon;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        Log.d("BootBroadcastReceiver", "onReceive begin");
        Intent mBootIntent = new Intent(context, PhoneListenService.class);
        context.startService(mBootIntent);

        Intent mLocalIntent = new Intent(context, MyLocalService.class);
        context.startService(mLocalIntent);
    }
}
