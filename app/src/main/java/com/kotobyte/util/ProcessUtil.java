package com.kotobyte.util;

import android.os.Handler;

/**
 * Created by andree.surya on 2017/01/02.
 */
public class ProcessUtil {

    public static void executeLater(Runnable runnable) {
        new Handler().post(runnable);
    }
}
