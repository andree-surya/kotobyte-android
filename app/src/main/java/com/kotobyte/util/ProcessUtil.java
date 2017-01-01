package com.kotobyte.util;

import android.os.Handler;
import android.os.Looper;

/**
 * Created by andree.surya on 2016/12/25.
 */
public class ProcessUtil {

    public static void executeAfterDelay(long uptimeMillis, Runnable runnable) {

        new Handler().postDelayed(runnable, uptimeMillis);
    }
}
