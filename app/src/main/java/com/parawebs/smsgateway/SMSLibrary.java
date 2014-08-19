package com.parawebs.smsgateway;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.telephony.SmsManager;
import android.util.Log;

/**
 * Created by gerswin on 18/08/14.
 */
public class SMSLibrary {

    public void sendSMS(final Context context, final String phoneNumber, final String message, final String idm) {

        String SENT = "SMS_SENT";
        String DELIVERED = "SMS_DELIVERED";

        final PendingIntent sentPI = PendingIntent.getBroadcast(context, 0, new Intent(SENT), 0);

        final PendingIntent deliveredPI = PendingIntent.getBroadcast(context, 0, new Intent(DELIVERED), 0);

            //---when the SMS has been sent---
        Intent intent = context.registerReceiver(new BroadcastReceiver() {

            @Override
            public void onReceive(Context arg0, Intent arg1) {
                smsgateway  pt = new  smsgateway();
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Log.w("LOG", "SMS send");
                        pt.poster("send","SMS send",idm);
                        pt.send_count(1);
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        Log.w("LOG", "Generic failure");
                        pt.poster("fail","Generic failure",idm);
                        pt.send_count(2);
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        Log.w("LOG", "No service");
                        pt.poster("fail","No service",idm);
                        pt.send_count(2);
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        Log.w("LOG", "Null PDU");
                        pt.poster("fail","Null PDU",idm);
                        pt.send_count(2);
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        Log.w("LOG", "Radio off");
                        pt.poster("fail","Radio off",idm);
                        pt.send_count(2);
                        break;
                }
            }

        }, new IntentFilter(SENT));

        //---when the SMS has been delivered---
        context.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Log.w("LOG", "SMS delivered");

                        break;
                    case Activity.RESULT_CANCELED:
                        Log.w("LOG", "SMS not delivered");
                        break;
                }
            }
        }, new IntentFilter(DELIVERED));

        SmsManager sms = SmsManager.getDefault();

        try {
            sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);

                /* To save in Sent items */
            ContentValues values = new ContentValues();
            values.put("address", phoneNumber);
            values.put("body", message);

            context.getContentResolver().insert(Uri.parse("content://sms/sent"), values);
        } catch (IllegalArgumentException e) {
            // TODO: handle exception
            Log.d("LOG", "" + e.getMessage());
        }
    }
}
