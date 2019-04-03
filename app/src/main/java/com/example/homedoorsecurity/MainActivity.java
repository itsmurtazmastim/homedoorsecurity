package com.example.homedoorsecurity;

import android.Manifest;
import android.app.ActivityManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Telephony;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.gsm.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {

    Button btnSendSMS;
    EditText txtPhoneNo;
    EditText txtMessage;

    boolean bRemoteDetailsRegistered = false;
    String strPhoneNumber;
    String strEndpointURL;

    private static final int REQUEST_READ_PHONE_STATE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnSendSMS = findViewById(R.id.btnSendSMS);
        txtPhoneNo = findViewById(R.id.editText);
        txtMessage = findViewById(R.id.editText2);

        registerReceiver(usbReceiver, new IntentFilter("usbStateChanged"));
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, REQUEST_READ_PHONE_STATE);
        } else {
            //TODO
        }

        btnSendSMS.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                // Todo Add validation
                strPhoneNumber = txtPhoneNo.getText().toString();
                strEndpointURL = txtMessage.getText().toString();

            // String SMSText = getResources().getString(R.string.app_name) + " " + getResources().getString(R.string.door_ring_msg) + " " + getResources().getString(R.string.web_rtc_server_URL);
                //sendSMS(phoneNumber, SMSText);
                txtPhoneNo.setEnabled(false);
                txtMessage.setEnabled(false);
                bRemoteDetailsRegistered = true;
                Toast.makeText(MainActivity.this, " Remote Registration successful strPhoneNumber = " + strPhoneNumber + " strEndpointURL " + strEndpointURL, Toast.LENGTH_LONG).show();




                LaunchBrowser();
                String strRemoteEndpoint = strEndpointURL + "/remote.html";
                String SMSText = getResources().getString(R.string.app_name) + " " + getResources().getString(R.string.door_ring_msg) + " " + strRemoteEndpoint;
                sendSMS(strPhoneNumber, SMSText);
            }
                // String strPackageName = getResources().getString(R.string.browser_package_name);
                // KillApplication(strPackageName);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(usbReceiver);
    }

    public void LaunchBrowser(){
        Log.d("HomeDoorSecurity", "launching browser");
        String strPackageName = getResources().getString(R.string.browser_package_name);
        String strLocalEndpoint = strEndpointURL + "/index.html";

        Intent launchIntent = getPackageManager().getLaunchIntentForPackage(strPackageName);

        if (launchIntent != null) {
            // String url = getResources().getString(R.string.web_rtc_server_URL);
            launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            launchIntent.putExtra("args", "--url=" + strLocalEndpoint);
            launchIntent.setData(Uri.parse(strEndpointURL));
            startActivity(launchIntent);//null pointer check in case package name was not found
        }
        else{
            Log.d("HomeDoorSecurity", "Launch Intent is null");
        }
    }

    // Add this inside your class
    UsbBroadcastReceiver usbReceiver =  new UsbBroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (bRemoteDetailsRegistered) {
                Bundle b = intent.getExtras();
                String message = b.getString("message");

                if (message.equalsIgnoreCase(getResources().getString(R.string.msg_usb_attached))) {

                    // phoneNumber = getResources().getString(R.string.phone_number);
                    // String SMSText = getResources().getString(R.string.app_name) + " " + getResources().getString(R.string.door_ring_msg) + " " + getResources().getString(R.string.web_rtc_server_URL);
                    String strRemoteEndpoint = strEndpointURL + "/remote.html";
                    /*if (strEndpointURL.endsWith("\\")){
                        strRemoteEndpoint += "remote";
                    }
                    else{
                        strRemoteEndpoint += "\\remote";
                    }*/
                    String SMSText = getResources().getString(R.string.app_name) + " " + getResources().getString(R.string.door_ring_msg) + " " + strRemoteEndpoint;
                    sendSMS(strPhoneNumber, SMSText);

                    Toast.makeText(context, "Sending SMS to Ph No " + strPhoneNumber + " SMS Text " + SMSText, Toast.LENGTH_LONG).show();
                    LaunchBrowser();
                } else if (message.equalsIgnoreCase(getResources().getString(R.string.msg_usb_detached))) {
                    Toast.makeText(context, "Detaching now" + intent.getAction(), Toast.LENGTH_LONG).show();
                    String strPackageName = getResources().getString(R.string.browser_package_name);
                    KillApplication(strPackageName);
                }
            }
            else{
                Toast.makeText(MainActivity.this, "Kindly do the remote registration to enable home security ", Toast.LENGTH_LONG).show();
            }
        }
    };

    //---sends an SMS message to another device---
    private void sendSMS(String phoneNumber, String message)
    {
        Log.d("HomeDoorSecurity", "Sending SMS " + message + " to " + phoneNumber);
        PendingIntent pi = PendingIntent.getActivity(this, 0,
                new Intent(this, Telephony.Sms.class), 0);
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message, pi, null);
    }

    public void KillApplication(String KillPackage)
    {
        // ActivityManager am = (ActivityManager)this.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> processList;
        ActivityManager am;

        am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        // list all running process
        processList = am.getRunningAppProcesses();

        /*Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        this.startActivity(startMain);*/
        // List<ActivityManager.RunningAppProcessInfo> processList = am.getRunningAppProcesses();

        Log.d("HomeDoorSecurity", " Size of processes is " + processList.size());
        for(int i = 0; i < processList.size(); ++i){
            Log.d("HomeDoorSecurity", processList.get(i).processName);
        }

        List<ActivityManager.RunningTaskInfo> runningTask = am.getRunningTasks(1000);
        Log.d("HomeDoorSecurity", " Size of Running Task is " + runningTask.size());

        for(int i = 0; i < runningTask.size(); ++i){
            Log.d("HomeDoorSecurity", runningTask.get(i).baseActivity.toString());
        }

        List<ActivityManager.RunningServiceInfo> runningServices = am.getRunningServices(1000);
        Log.d("HomeDoorSecurity", " Size of Running Services is " + runningServices.size());
        for(int i = 0; i < runningServices.size(); ++i){
            Log.d("HomeDoorSecurity", runningServices.get(i).service.toString());
        }

        am.killBackgroundProcesses(KillPackage);
        Toast.makeText(getBaseContext(),"Process Killed : " + KillPackage  ,Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_READ_PHONE_STATE:
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    //TODO
                }
                break;

            default:
                Log.d("HomeDoorSecurity", "onRequestPermissionsResult called for Request Code %d" + requestCode);
                break;
        }
    }
}