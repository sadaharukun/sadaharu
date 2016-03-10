package com.example.yaoxinxin.colorclock.widget;/*
 * Copyright 2012 Lars Werkman
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.example.yaoxinxin.colorclock.R;

/**
 * Displays a holo-themed color picker.
 * <p/>
 * <p>
 * </p>
 */
@SuppressLint("DrawAllocation")
public class ColorPicker extends View {
    private static final String TAG = "ColorPicker";
    private static final String STATE_PARENT = "parent";
    private static final String STATE_ANGLE = "angle";
    private Matrix mMatrix = new Matrix();
    /**
     * Colors to construct the color wheel using
     * {@link android.graphics.SweepGradient}.
     */
    public static int[] COLORS = new int[]{0xFFFF0000, 0xFFFF00FF, 0xFF0000FF, 0xFF00FFFF, 0xFF00FF00, 0xFFFFFF00,
            0xFFFF0000};


    private Shader s;
    private Paint textPaint;
    /**
     * {@code Paint} instance used to draw the color wheel.
     */
    private Paint mColorWheelPaint;

    /**
     * {@code Paint} instance used to draw the pointer's "halo".
     */
    private Paint mPointerHaloPaint;

    /**
     * {@code Paint} instance used to draw the pointer (the selected color).
     */
    private Paint mPointerColor;
    private Paint newmPointerColor;

    /**
     * The width of the color wheel thickness.
     */
    private int mColorWheelThickness;

    /**
     * The radius of the color wheel.
     */
    private int mColorWheelRadius;
    private int mPreferredColorWheelRadius;

    /**
     * The radius of the center circle inside the color wheel.
     */
    private int mColorCenterRadius;
    private int mPreferredColorCenterRadius;

    /**
     * The radius of the halo of the center circle inside the color wheel.
     */
    private int mColorCenterHaloRadius;
    private int mPreferredColorCenterHaloRadius;

    /**
     * The radius of the pointer.
     */
    private int mColorPointerRadius;

    /**
     * The radius of the halo of the pointer.
     */
    private int mColorPointerHaloRadius;

    /**
     * The rectangle enclosing the color wheel.
     */
    private RectF mColorWheelRectangle = new RectF();

    /**
     * {@code true} if the user clicked on the pointer to start the move mode. <br>
     * {@code false} once the user stops touching the screen.
     *
     * @see #onTouchEvent(android.view.MotionEvent)
     */
    private boolean mUserIsMovingPointer = false;

    /**
     * The ARGB value of the currently selected color.
     */
    private int mColor;
    private int[] rgbColors = new int[3];

    /**
     * Number of pixels the origin of this view is moved in X- and Y-direction.
     * <p/>
     * <p>
     * We use the center of this (quadratic) View as origin of our internal
     * coordinate system. Android uses the upper left corner as origin for the
     * View-specific coordinate system. So this is the value we use to translate
     * from one coordinate system to the other.
     * </p>
     * <p/>
     * <p>
     * Note: (Re)calculated in {@link #onMeasure(int, int)}.
     * </p>
     *
     * @see #onDraw(android.graphics.Canvas)
     */
    private float mTranslationOffset;

    /**
     * Distance between pointer and user touch in X-direction.
     */
    private float mSlopX;

    /**
     * Distance between pointer and user touch in Y-direction.
     */
    private float mSlopY;

    /**
     * The pointer's position expressed as angle (in rad).
     */
    private float mAngle;

    /**
     * {@code Paint} instance used to draw the center with the new selected
     * color.
     */
    private Paint mCenterNewPaint;
    private Paint defaultPaint;

    /**
     * {@code Paint} instance used to draw the halo of the center selected
     * colors.
     */
    private Paint mCenterHaloPaint;

    /**
     * An array of floats that can be build into a {@code Color} <br>
     * Where we can extract the Saturation and Value from.
     */
    private float[] mHSV = new float[3];


    /**
     * {@code TouchAnywhereOnColorWheelEnabled} instance used to control <br>
     * if the color wheel accepts input anywhere on the wheel or just <br>
     * on the halo.
     */
    private boolean mTouchAnywhereOnColorWheelEnabled = true;


    /**
     * {@code onColorChangedListener} instance of the onColorChangedListener
     */
    private OnColorChangedListener onColorChangedListener;

    public ColorPicker(Context context) {
        super(context);
        init(null, 0);
    }

    public ColorPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public ColorPicker(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    /**
     * An interface that is called whenever the color is changed. Currently it
     * is always called when the color is changes.
     *
     * @author lars
     */
    public interface OnColorChangedListener {
        public void onColorChanged(int color);

        public void onColorSelected(int color);
    }

    /**
     * Set a onColorChangedListener
     *
     * @param {@code OnColorChangedListener}
     */
    public void setOnColorChangedListener(OnColorChangedListener listener) {
        this.onColorChangedListener = listener;
    }

    /**
     * Gets the onColorChangedListener
     *
     * @return {@code OnColorChangedListener}
     */
    public OnColorChangedListener getOnColorChangedListener() {
        return this.onColorChangedListener;
    }


    private void init(AttributeSet attrs, int defStyle) {
        final TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.ColorPicker, defStyle, 0);
        final Resources b = getContext().getResources();

        mColorWheelThickness = a.getDimensionPixelSize(R.styleable.ColorPicker_color_wheel_thickness,
                b.getDimensionPixelSize(R.dimen.color_wheel_thickness));
        mColorWheelRadius = a.getDimensionPixelSize(R.styleable.ColorPicker_color_wheel_radius,
                b.getDimensionPixelSize(R.dimen.color_wheel_radius));
        mPreferredColorWheelRadius = mColorWheelRadius;
        mColorCenterRadius = a.getDimensionPixelSize(R.styleable.ColorPicker_color_center_radius,
                b.getDimensionPixelSize(R.dimen.color_center_radius));
        mPreferredColorCenterRadius = mColorCenterRadius;
        mColorCenterHaloRadius = a.getDimensionPixelSize(R.styleable.ColorPicker_color_center_halo_radius,
                b.getDimensionPixelSize(R.dimen.color_center_halo_radius));
        mPreferredColorCenterHaloRadius = mColorCenterHaloRadius;
        mColorPointerRadius = a.getDimensionPixelSize(R.styleable.ColorPicker_color_pointer_radius,
                b.getDimensionPixelSize(R.dimen.color_pointer_radius));
        mColorPointerHaloRadius = a.getDimensionPixelSize(R.styleable.ColorPicker_color_pointer_halo_radius,
                b.getDimensionPixelSize(R.dimen.color_pointer_halo_radius));

        a.recycle();

        mAngle = (float) (-Math.PI / 2);

        s = new SweepGradient(0, 0, COLORS, null);

        mColorWheelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mColorWheelPaint.setShader(s);
        mColorWheelPaint.setStyle(Paint.Style.STROKE);
        mColorWheelPaint.setStrokeWidth(mColorWheelThickness);


        mPointerHaloPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPointerHaloPaint.setColor(Color.WHITE);
        // mPointerHaloPaint.setAlpha(0x50);

        mPointerColor = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPointerColor.setColor(calculateColor(mAngle));

        newmPointerColor = new Paint(Paint.ANTI_ALIAS_FLAG);
        newmPointerColor.setColor(Color.BLACK);

        mCenterNewPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCenterNewPaint.setColor(calculateColor(mAngle));
        mCenterNewPaint.setStyle(Paint.Style.FILL);

        defaultPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        defaultPaint.setStyle(Paint.Style.FILL);

        mCenterHaloPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCenterHaloPaint.setColor(Color.WHITE);
        mCenterHaloPaint.setAlpha(0x00);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextSize(20);
        textPaint.setColor(Color.rgb(99, 99, 99));

    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.translate(mTranslationOffset, mTranslationOffset);//自定义控件的偏移量(使自定义控件的中心为圆心，便于计算角度)
        s = new SweepGradient(0, 0, COLORS, null);
        mColorWheelPaint.setShader(s);
        // Draw the color wheel.
        canvas.drawOval(mColorWheelRectangle, mColorWheelPaint);

        float[] pointerPosition = calculatePointerPosition(mAngle);

        // Draw the pointer's "halo"
        canvas.drawCircle(pointerPosition[0], pointerPosition[1], mColorPointerHaloRadius - 5, mPointerHaloPaint);

        // Draw the pointer (the currently selected color) slightly smaller on
        // top.
        canvas.drawCircle(pointerPosition[0], pointerPosition[1], mColorPointerRadius - 3, newmPointerColor);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int intrinsicSize = 2 * (mPreferredColorWheelRadius + mColorPointerHaloRadius);

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width;
        int height;

        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            width = Math.min(intrinsicSize, widthSize);
        } else {
            width = intrinsicSize;
        }

        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            height = Math.min(intrinsicSize, heightSize);
        } else {
            height = intrinsicSize;
        }

        int min = Math.min(width, height);
        setMeasuredDimension(min, min);
        mTranslationOffset = min / 2;
        mColorWheelRectangle.set(-mColorWheelRadius, -mColorWheelRadius, mColorWheelRadius, mColorWheelRadius);

        mColorCenterRadius = (int) ((float) mPreferredColorCenterRadius * ((float) mColorWheelRadius / (float) mPreferredColorWheelRadius));
        mColorCenterHaloRadius = (int) ((float) mPreferredColorCenterHaloRadius * ((float) mColorWheelRadius / (float) mPreferredColorWheelRadius));
    }

    private int ave(int s, int d, float p) {
        return s + Math.round(p * (d - s));
    }

    /**
     * Calculate the color using the supplied angle.
     *
     * @param angle The selected color's position expressed as angle (in rad).
     * @return The ARGB value of the color on the color wheel at the specified
     * angle.
     */
    private int calculateColor(float angle) {
        float unit = (float) (angle / (2 * Math.PI));
        if (true) {

            if (unit < 0) {
                unit += 1;
            }

            if (unit <= 0) {
                mColor = COLORS[0];
                return COLORS[0];
            }
            if (unit >= 1) {
                mColor = COLORS[COLORS.length - 1];
                return COLORS[COLORS.length - 1];
            }

            float p = unit * (COLORS.length - 1);
            int i = (int) p;
            p -= i;

            int c0 = COLORS[i];
            int c1 = COLORS[i + 1];
            int a = ave(Color.alpha(c0), Color.alpha(c1), p);
            int r = ave(Color.red(c0), Color.red(c1), p);
            int g = ave(Color.green(c0), Color.green(c1), p);
            int b = ave(Color.blue(c0), Color.blue(c1), p);
            rgbColors[0] = r;
            rgbColors[1] = g;
            rgbColors[2] = b;
            mColor = Color.argb(a, r, g, b);
            return mColor;
        } else {
            float[] mHSVColor = new float[3];
            Color.colorToHSV(Color.rgb(255, 174, 84), mHSVColor);
            float jiaodu = (float) (180 * angle / Math.PI);
            if (jiaodu >= 135 && jiaodu <= 180) {
                jiaodu = jiaodu - 135;
            } else if (jiaodu <= 45 && jiaodu >= -180) {
                jiaodu = jiaodu + 225;
            }
            mColor = Color.HSVToColor(new float[]{mHSVColor[0], (float) ((jiaodu) / 270f), 1f});
            //Log.i("color", mColor + "---mColor:" + (jiaodu) / 270f);
            return mColor;
        }

    }

    public void setWarmColor(float angle) {
        float[] mHSVColor = new float[3];
        Color.colorToHSV(Color.rgb(255, 174, 84), mHSVColor);
        float jiaodu = (float) (180 * angle / Math.PI);
        if (jiaodu >= 135 && jiaodu <= 180) {
            jiaodu = jiaodu - 135;
        } else if (jiaodu <= 45 && jiaodu >= -180) {
            jiaodu = jiaodu + 225;
        }
        mColor = Color.HSVToColor(new float[]{mHSVColor[0], (float) ((jiaodu) / 270f), 1f});
        this.setColor(mColor);
    }


    /**
     * Set the color to be highlighted by the pointer. </br> </br> If the
     * instances {@code SVBar} and the {@code OpacityBar} aren't null the color
     * will also be set to them
     *
     * @param color The RGB value of the color to highlight. If this is not a
     *              color displayed on the color wheel a very simple algorithm is
     *              used to map it to the color wheel. The resulting color often
     *              won't look close to the original color. This is especially
     *              true for shades of grey. You have been warned!
     */
    public void setColor(int color) {
        mAngle = colorToAngle(color);
        mPointerColor.setColor(calculateColor(mAngle));

    }

    /**
     * Convert a color to an angle.
     *
     * @param color The RGB value of the color to "find" on the color wheel.
     * @return The angle (in rad) the "normalized" color is displayed on the
     * color wheel.
     */
    private float colorToAngle(int color) {
        float[] colors = new float[3];
        Color.colorToHSV(color, colors);

        return (float) Math.toRadians(-colors[0]);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        getParent().requestDisallowInterceptTouchEvent(true);

        // Convert coordinates to our internal coordinate system
        float x = event.getX() - mTranslationOffset;
        float y = event.getY() - mTranslationOffset;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // Check whether the user pressed on the pointer.
                float[] pointerPosition = calculatePointerPosition(mAngle);
                if (x >= (pointerPosition[0] - mColorPointerHaloRadius)
                        && x <= (pointerPosition[0] + mColorPointerHaloRadius)
                        && y >= (pointerPosition[1] - mColorPointerHaloRadius)
                        && y <= (pointerPosition[1] + mColorPointerHaloRadius)) {
                    mSlopX = x - pointerPosition[0];
                    mSlopY = y - pointerPosition[1];
                    mUserIsMovingPointer = true;
                    invalidate();
                }

                // Check whether the user pressed anywhere on the wheel.
                else if (Math.sqrt(x * x + y * y) <= mColorWheelRadius + mColorPointerHaloRadius
                        && Math.sqrt(x * x + y * y) >= mColorWheelRadius - mColorPointerHaloRadius
                        && mTouchAnywhereOnColorWheelEnabled) {
                    mUserIsMovingPointer = true;
                    invalidate();
                }
                // If user did not press pointer or center, report event not handled
                else {
                    getParent().requestDisallowInterceptTouchEvent(false);
                    return false;
                }
                break;
            case MotionEvent.ACTION_MOVE:

                if (mUserIsMovingPointer) {
                    mAngle = (float) Math.atan2(y - mSlopY, x - mSlopX);

                    if (onColorChangedListener != null) {
                        onColorChangedListener.onColorChanged(calculateColor(mAngle));
                    }

                    invalidate();
                }

                break;
            case MotionEvent.ACTION_UP:

                if (mUserIsMovingPointer) {

                    mAngle = (float) Math.atan2(y - mSlopY, x - mSlopX);

                    if (onColorChangedListener != null) {
                        onColorChangedListener.onColorSelected(calculateColor(mAngle));
                    }
                    invalidate();
                }

                mUserIsMovingPointer = false;
                break;

            case MotionEvent.ACTION_CANCEL:
                mUserIsMovingPointer = false;
                break;

        }
        return true;
    }

    public int[] getRgbColors() {
        return rgbColors;
    }

    public void setRgbColors(int[] rgbColors) {
        this.rgbColors = rgbColors;
    }

    /**
     * Calculate the pointer's coordinates on the color wheel using the supplied
     * angle.
     *
     * @param angle The position of the pointer expressed as angle (in rad).
     * @return The coordinates of the pointer's center in our internal
     * coordinate system.
     */
    private float[] calculatePointerPosition(float angle) {
        float x = (float) (mColorWheelRadius * Math.cos(angle));
        float y = (float) (mColorWheelRadius * Math.sin(angle));

        return new float[]{x, y};
    }


    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();

        Bundle state = new Bundle();
        state.putParcelable(STATE_PARENT, superState);
        state.putFloat(STATE_ANGLE, mAngle);

        return state;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        Bundle savedState = (Bundle) state;

        Parcelable superState = savedState.getParcelable(STATE_PARENT);
        super.onRestoreInstanceState(superState);

        mAngle = savedState.getFloat(STATE_ANGLE);
        int currentColor = calculateColor(mAngle);
        mPointerColor.setColor(currentColor);
    }

    public void setTouchAnywhereOnColorWheelEnabled(boolean TouchAnywhereOnColorWheelEnabled) {
        mTouchAnywhereOnColorWheelEnabled = TouchAnywhereOnColorWheelEnabled;
    }

    public boolean getTouchAnywhereOnColorWheel() {
        return mTouchAnywhereOnColorWheelEnabled;
    }

    public float getmAngle() {
        return mAngle;
    }

    public void setmAngle(float mAngle) {
        this.mAngle = mAngle;
    }
}
