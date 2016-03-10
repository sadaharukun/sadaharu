package com.example.yaoxinxin.colorclock.widget;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.example.yaoxinxin.colorclock.R;
import com.example.yaoxinxin.colorclock.utils.BitmapUtil;

import java.util.Calendar;

/**
 * Created by yaoxinxin on 16/3/3.
 */
public class ColorClock extends View {

    private static final String TAG = ColorClock.class.getSimpleName();

    /**
     * width and height of view
     */
    private int mWidth;
    private int mHeight;

    /**
     * radius of clock
     */
    private int mRadius;

    /**
     * thickness of clokc
     */
    private int mThickness;

    /**
     * color of text
     */
    private int mTextColor;

    /**
     * paint of clock
     */
    private Paint mPaint;

    /**
     * paint of text
     */
    private Paint mTextPaint;

    /**
     * color of paint
     */
    private int mColor;

    /**
     * center of circle
     */
    private int mCenterX, mCenterY;

    /**
     * the shader of paint
     */
    private Shader mGradient;

    /**
     * hand of hour,min
     */
    private BitmapDrawable mSecHand, mMinHand;

    /**
     * runnable of tick
     */
    private Runnable mTickRunnable;

    /**
     * paint of hand
     */
    private Paint mHandPaint;

    /**
     * dp of minhand and sedhand
     */
    private int mTempWidth, mTempHeight, mTempWidth2, mTempHeight2;


    private String TimeZone = "GMT+8:00";

    private Matrix mMatrix = new Matrix();

    public ColorClock(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }


    public ColorClock(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ColorClock(Context context) {
        this(context, null);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ColorClock);
        Resources b = context.getResources();

        mRadius = a.getDimensionPixelOffset(R.styleable.ColorClock_radius, (int) b.getDimension(R.dimen.colorClock_default_radius));
        mThickness = a.getDimensionPixelOffset(R.styleable.ColorClock_thickness2, (int) b.getDimension(R.dimen.colorClock_default_thickness));
        mTextColor = a.getColor(R.styleable.ColorClock_textColor, b.getColor(R.color.colorClock_default_textColor));
        mColor = a.getColor(R.styleable.ColorClock_circleColor, b.getColor(R.color.colorClock_default_color));

        a.recycle();

        Bitmap sechand = BitmapUtil.readBitmap(context, R.mipmap.android_clock_minute);
        mSecHand = new BitmapDrawable(b, sechand);
        Bitmap minhand = BitmapUtil.readBitmap(context, R.mipmap.android_clock_hour);
        mMinHand = new BitmapDrawable(b, minhand);

        initPaint();

        mTickRunnable = new TickRunnable();
        mHandler.post(mTickRunnable);
    }

    /**
     * init Paint
     */
    private void initPaint() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(mThickness);
        mPaint.setColor(mColor);

        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(mTextColor);
        mTextPaint.setTextSize(24);


    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int range = mRadius * 2 + getPaddingLeft() + getPaddingRight() + mThickness * 2;
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        if (widthMode == MeasureSpec.EXACTLY) {
            mWidth = widthSize;
        } else {
            mWidth = Math.min(range, widthSize);
        }

        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        if (heightMode == MeasureSpec.EXACTLY) {
            mHeight = heightSize;
        } else {
            mHeight = Math.min(range, heightSize);
        }

        mHeight = mWidth = Math.min(mWidth, mHeight);

        Log.e(TAG, "mWidth=" + mWidth + " Height=" + mHeight);

        setMeasuredDimension(mWidth, mHeight);

        mCenterX = mWidth / 2;
        mCenterY = mHeight / 2;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Calendar c = Calendar.getInstance(java.util.TimeZone.getTimeZone(TimeZone));
        int hour = c.get(Calendar.HOUR);
        int min = c.get(Calendar.MINUTE);
        int sec = c.get(Calendar.SECOND);

        float minDegree = hour * 6.0f;
        float secDegree = sec * 6.0f;

//        mTempWidth = Tools.dip2px(this.getContext(), mSecHand.getIntrinsicWidth() /3);
//        mTempHeight = Tools.dip2px(this.getContext(), mSecHand.getIntrinsicHeight() / 3);
//        mTempWidth2 = Tools.dip2px(this.getContext(), mMinHand.getIntrinsicWidth() / 3);
//        mTempHeight2 = Tools.dip2px(this.getContext(), mMinHand.getIntrinsicHeight() / 3);
        mTempWidth = mSecHand.getIntrinsicWidth()/2;
        mTempHeight = mSecHand.getIntrinsicHeight()/2;
        mTempWidth2 = mMinHand.getIntrinsicWidth()/2;
        mTempHeight2 = mMinHand.getIntrinsicHeight()/2;


        mSecHand.setBounds(mCenterX - mTempWidth / 2, mCenterY - 3*mTempHeight/4 , mCenterX +  mTempWidth / 2,
                mCenterY +mTempHeight/4);
        mMinHand.setBounds(mCenterX -  mTempWidth2 / 2, mCenterY - 3* mTempHeight2/4    , mCenterX + mTempWidth2 / 2,
                mCenterY + mTempHeight2/4);
        canvas.save();
        canvas.rotate(secDegree, mCenterX, mCenterY);
        mSecHand.draw(canvas);
        canvas.restore();

        mGradient = new SweepGradient(mCenterX, mCenterY, new int[]{Color.WHITE, mColor}, null);
        mMatrix.setRotate(-90, mCenterX, mCenterY);
        mGradient.setLocalMatrix(mMatrix);
        mPaint.setShader(mGradient);
        canvas.drawCircle(mCenterX, mCenterY, mRadius + mThickness / 2, mPaint);

        canvas.drawText("0", mCenterX - getTextRange("0")[0] / 2, mThickness * 3, mTextPaint);
        canvas.drawText("15", mWidth - mThickness * 4,mCenterY + getTextRange("15")[1] / 2 ,mTextPaint);
        canvas.drawText("30", mCenterX - getTextRange("30")[0] / 2, mHeight - 3*mThickness/2,mTextPaint);
        canvas.drawText("45",3*mThickness/2,mCenterY+getTextRange("45")[1]/2,mTextPaint);
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
//            postInvalidate();
        }
    };

    private class TickRunnable implements Runnable {

        @Override
        public void run() {
            postInvalidate();

            mHandler.postDelayed(mTickRunnable, 1000);


        }
    }

    private Rect mTextBound = new Rect();
    /**
     * get width and height of text
     * @param str
     * @return
     */
    private int[] getTextRange(String str){
        mTextPaint.getTextBounds(str,0,str.length(),mTextBound);

        int width = mTextBound.width();
        int height = mTextBound.height();

        return new int[]{width,height};
    }

    /**
     * change color of the paint
     * @param color
     */
    public void setmPaintColor(int color){
//        mPaint.setColor(color);

        mColor =  color;
        invalidate();
    }
}
