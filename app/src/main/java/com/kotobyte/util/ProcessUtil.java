package com.kotobyte.util;

import android.os.Handler;

/**
 * Created by andree.surya on 2017/01/02.
 */
public class ProcessUtil {

    public static void executeSoon(Runnable runnable) {
        new Handler().post(runnable);
    }

    public static void executeAfterDelay(long delayInMillis, Runnable runnable) {
        new Handler().postDelayed(runnable, delayInMillis);
    }
}
