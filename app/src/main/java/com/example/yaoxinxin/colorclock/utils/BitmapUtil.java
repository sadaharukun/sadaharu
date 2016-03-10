package com.example.yaoxinxin.colorclock.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.InputStream;

/**
 * Created by yaoxinxin on 16/3/8.
 */
public class BitmapUtil {

    private static final String TAG = BitmapUtil.class.getSimpleName();

    private static BitmapUtil mInstance;

    private BitmapUtil() {
    }

    public BitmapUtil getInstance() {
        if (mInstance == null) {
            synchronized (BitmapUtil.class) {
                if (mInstance == null) {
                    mInstance = new BitmapUtil();
                }
            }
        }

        return mInstance;
    }

    /**
     *
     */
    public static Bitmap readBitmap(Context c,int resId){

        BitmapFactory.Options options= new BitmapFactory.Options();
        options.inInputShareable = true;

        options.inPreferredConfig = Bitmap.Config.ARGB_8888;

        options.inPurgeable = true;

        InputStream in = c.getResources().openRawResource(resId);


       return BitmapFactory.decodeStream(in,null,options);


    }





}
