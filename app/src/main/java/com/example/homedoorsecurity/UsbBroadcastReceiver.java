package com.example.homedoorsecurity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import android.app.ActivityManager;

public class UsbBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("HomeDoorSecurity","Received Intent");
        String action = intent.getAction();
        Toast.makeText(context, "Intent Detected. By Home door security" + action , Toast.LENGTH_LONG).show();

        // Thread.sleep(2000);
        // String action = intent.getAction();
        Intent i = new Intent("usbStateChanged");

        if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action))
        {
            i.putExtra("message", "USB Attached");
            context.sendBroadcast(i);
        }
        else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)){
            i.putExtra("message", "USB Detached");
            context.sendBroadcast(i);
        }
    }
}
