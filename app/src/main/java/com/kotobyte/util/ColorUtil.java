package com.kotobyte.util;

import android.content.Context;
import android.os.Build;
import android.support.annotation.ColorRes;

/**
 * Created by andree.surya on 2017/01/17.
 */
public class ColorUtil {

    public static int getColor(Context context, @ColorRes int colorRes) {
        int color;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            color = context.getColor(colorRes);

        } else {
            color = context.getResources().getColor(colorRes);
        }

        return color;
    }
}
