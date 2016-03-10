package com.example.yaoxinxin.colorclock.utils;

import android.content.Context;

/**
 * Created by yaoxinxin on 16/3/8.
 */
public class Tools {



    public static int dip2px(Context c,int dip){

      float m =  c.getResources().getDisplayMetrics().density;



        return (int) (dip*m + 0.5f);
    }

    public static int px2dip(Context c,int px){
        float m = c.getResources().getDisplayMetrics().density;

        return (int) (px/m+0.5f);


    }

}
