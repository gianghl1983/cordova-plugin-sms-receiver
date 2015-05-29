package eu.ttbox.cordova.sms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.util.Log;


public class SmsBroadcastReceiver extends BroadcastReceiver {

    private static final String SMS_RECEIVED_ACTION = Telephony.Sms.Intents.SMS_RECEIVED_ACTION;
    private static final String SMS_DELIVERED_ACTION = Telephony.Sms.Intents.SMS_DELIVER_ACTION;


    @Override
    public void onReceive(Context context, Intent intent) {
        Log.w("SMSListener", "Got SMS broadcast...");

        if ((SMS_DELIVERED_ACTION.equals(intent.getAction()))
                || (SMS_RECEIVED_ACTION.equals(intent.getAction()))
                && isRelevant(context, intent)
                ) {
            Object[] pdus = (Object[]) intent.getExtras().get("pdus");
        //    ApplicationContext.getInstance(context).getJobManager().add(new SmsReceiveJob(context, pdus));

//      Intent receivedIntent = new Intent(context, SendReceiveService.class);
//      receivedIntent.setAction(SendReceiveService.RECEIVE_SMS_ACTION);
//      receivedIntent.putExtra("ResultCode", this.getResultCode());
//      receivedIntent.putParcelableArrayListExtra("text_messages",getAsTextMessages(intent));
//      context.startService(receivedIntent);

            abortBroadcast();
        }
    }


    private boolean isRelevant(Context context, Intent intent) {
        SmsMessage message = getSmsMessageFromIntent(intent);
        String messageBody = getSmsMessageBodyFromIntent(intent);

        if (message == null && messageBody == null) {
            return false;
        }

        if (isExemption(message, messageBody)) {
            return false;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT &&
                SMS_RECEIVED_ACTION.equals(intent.getAction()) &&
                Util.isDefaultSmsProvider(context))  {
            return false;
        }

        //https://github.com/SMSSecure/SMSSecure/blob/master/src/org/smssecure/smssecure/protocol/WirePrefix.java
        return true;
    }

    private SmsMessage getSmsMessageFromIntent(Intent intent) {
        Bundle bundle = intent.getExtras();
        Object[] pdus = (Object[]) bundle.get("pdus");

        if (pdus == null || pdus.length == 0) {
            return null;
        }

        return SmsMessage.createFromPdu((byte[]) pdus[0]);
    }

    private String getSmsMessageBodyFromIntent(Intent intent) {
        Bundle bundle = intent.getExtras();
        Object[] pdus = (Object[]) bundle.get("pdus");
        StringBuilder bodyBuilder = new StringBuilder();

        if (pdus == null) {
            return null;
        }

        for (Object pdu : pdus) {
            bodyBuilder.append(SmsMessage.createFromPdu((byte[]) pdu).getDisplayMessageBody());
        }

        return bodyBuilder.toString();
    }


    private boolean isExemption(SmsMessage message, String messageBody) {

        // ignore CLASS0 ("flash") messages
        if (message.getMessageClass() == SmsMessage.MessageClass.CLASS_0)
            return true;

        // ignore OTP messages from Sparebank1 (Norwegian bank)
        if (messageBody.startsWith("Sparebank1://otp?")) {
            return true;
        }

        return
                message.getOriginatingAddress().length() < 7 &&
                        (messageBody.toUpperCase().startsWith("//ANDROID:") || // Sprint Visual Voicemail
                                messageBody.startsWith("//BREW:")); //BREW stands for “Binary Runtime Environment for Wireless"
    }


}