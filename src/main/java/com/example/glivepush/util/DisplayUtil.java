package com.example.glivepush.util;

import android.content.Context;
import android.util.DisplayMetrics;

public class DisplayUtil {

    /***********************************    解决横屏问题-START    *********************************/
    //获取屏幕宽高
    public static int getScreenWidth(Context context)
    {
        DisplayMetrics metric = context.getResources().getDisplayMetrics();
        return metric.widthPixels;
    }

    public static int getScreenHeight(Context context)
    {
        DisplayMetrics metric = context.getResources().getDisplayMetrics();
        return metric.heightPixels;
    }
    /***********************************    解决横屏问题-START    *********************************/

}
