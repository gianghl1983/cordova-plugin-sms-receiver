package eu.ttbox.cordova.sms;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.provider.Telephony;


public class Util {

    @SuppressLint("NewApi")
    public static boolean isDefaultSmsProvider(Context context) {
        return (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) ||
                (context.getPackageName().equals(Telephony.Sms.getDefaultSmsPackage(context)));
    }


}