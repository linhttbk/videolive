package wallpaper.videolive.views.custom;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import wallpaper.videolive.R;

public class RangeSeekBarView extends View {
    private static final int INVALID_POINTER_ID = 255;
    //private static final int DEFAULT_THUMB_WIDTH = 80;
    //private static final int DEFAULT_THUMB_HEIGHT = 80;

    private final float NO_STEP = -1f;
    private final float NO_FIXED_GAP = -1f;

    //////////////////////////////////////////
    // PUBLIC CONSTANTS CLASS
    //////////////////////////////////////////

    public static final class DataType {
        public static final int INTEGER = 0;
        public static final int DOUBLE = 1;
        public static final int LONG = 2;
        public static final int FLOAT = 3;
        public static final int SHORT = 4;
        public static final int BYTE = 5;
    }


    private OnRangeSeekbarChangeListener onRangeSeekbarChangeListener;
    private OnRangeSeekbarFinalValueListener onRangeSeekbarFinalValueListener;


    private float absoluteMinValue;
    private float absoluteMaxValue;
    private float absoluteMinStartValue;
    private float absoluteMaxStartValue;
    private float minValue;
    private float maxValue;
    private float minStartValue;
    private float maxStartValue;
    private float steps;
    private float gap;
    private float fixGap;

    private int mActivePointerId = INVALID_POINTER_ID;

    private int dataType;
    private float cornerRadius;
    private int barColorMode;
    private int barColor;
    private int barGradientStart;
    private int barGradientEnd;
    private int barHighlightColorMode;
    private int barHighlightColor;
    private int barHighlightGradientStart;
    private int barHighlightGradientEnd;
    private int leftThumbColor;
    private int rightThumbColor;
    private int leftThumbColorNormal;
    private int leftThumbColorPressed;
    private int rightThumbColorNormal;
    private int rightThumbColorPressed;
    private boolean seekBarTouchEnabled;
    private float barPadding;
    private float barHeight;
    private float _barHeight;
    private float thumbWidth;
    private float thumbDiameterHeight;
    private float thumbDiameterWidth;

    //private float thumbHalfWidth;
    //private float thumbHalfHeight;
    private float thumbHeight;
    private Drawable leftDrawable;
    private Drawable rightDrawable;
    private Drawable leftDrawablePressed;
    private Drawable rightDrawablePressed;
    private Bitmap leftThumb;
    private Bitmap leftThumbPressed;
    private Bitmap rightThumb;
    private Bitmap rightThumbPressed;
    private Thumb pressedThumb;
    private double normalizedMinValue = 0d;
    private double normalizedMaxValue = 100d;
    private int pointerIndex;
    private RectF _rect;
    private Paint _paint;

    private final Paint mShadow = new Paint();

    private RectF rectLeftThumb, rectRightThumb;

    private boolean mIsDragging;


    //////////////////////////////////////////
    // ENUMERATION
    //////////////////////////////////////////

    protected enum Thumb {MIN, MAX}

    //////////////////////////////////////////
    // CONSTRUCTOR
    //////////////////////////////////////////

    public RangeSeekBarView(Context context) {
        this(context, null);
    }

    public RangeSeekBarView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RangeSeekBarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        // prevent render is in edit mode
        if (isInEditMode()) return;

        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.RangeSeekBarView);
        try {
            cornerRadius = getCornerRadius(array);
            minValue = getMinValue(array);
            maxValue = getMaxValue(array);
            minStartValue = getMinStartValue(array);
            maxStartValue = getMaxStartValue(array);
            steps = getSteps(array);
            gap = getGap(array);
            fixGap = getFixedGap(array);
            _barHeight = getBarHeight(array);
            barColorMode = getBarColorMode(array);
            barColor = getBarColor(array);
            barGradientStart = getBarGradientStart(array);
            barGradientEnd = getBarGradientEnd(array);
            barHighlightColorMode = getBarHighlightColorMode(array);
            barHighlightColor = getBarHighlightColor(array);
            barHighlightGradientStart = getBarHighlightGradientStart(array);
            barHighlightGradientEnd = getBarHighlightGradientEnd(array);
            leftThumbColorNormal = getLeftThumbColor(array);
            rightThumbColorNormal = getRightThumbColor(array);
            leftThumbColorPressed = getLeftThumbColorPressed(array);
            rightThumbColorPressed = getRightThumbColorPressed(array);
            leftDrawable = getLeftDrawable(array);
            rightDrawable = getRightDrawable(array);
            leftDrawablePressed = getLeftDrawablePressed(array);
            rightDrawablePressed = getRightDrawablePressed(array);
            thumbDiameterHeight = getDiameterHeight(array);
            thumbDiameterWidth = getDiameterWidth(array);
            dataType = getDataType(array);
            seekBarTouchEnabled = isSeekBarTouchEnabled(array);
        } finally {
            array.recycle();
        }

        init();
    }

    //////////////////////////////////////////
    // INITIALIZING
    //////////////////////////////////////////

    protected void init() {
        absoluteMinValue = minValue;
        absoluteMaxValue = maxValue;
        leftThumbColor = leftThumbColorNormal;
        rightThumbColor = rightThumbColorNormal;
        leftThumb = getBitmap(leftDrawable);
        rightThumb = getBitmap(rightDrawable);
        leftThumbPressed = getBitmap(leftDrawablePressed);
        rightThumbPressed = getBitmap(rightDrawablePressed);
        leftThumbPressed = (leftThumbPressed == null) ? leftThumb : leftThumbPressed;
        rightThumbPressed = (rightThumbPressed == null) ? rightThumb : rightThumbPressed;

        gap = Math.max(0, Math.min(gap, absoluteMaxValue - absoluteMinValue));
        gap = gap / (absoluteMaxValue - absoluteMinValue) * 100;
        if (fixGap != NO_FIXED_GAP) {
            fixGap = Math.min(fixGap, absoluteMaxValue);
            fixGap = fixGap / (absoluteMaxValue - absoluteMinValue) * 100;
            addFixGap(true);
        }

        thumbWidth = getThumbWidth();
        thumbHeight = getThumbHeight();

        //thumbHalfWidth = thumbWidth / 2;
        //thumbHalfHeight = thumbHeight / 2;

        barHeight = getBarHeight();
        barPadding = getBarPadding();

        _paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mShadow.setAntiAlias(true);
        mShadow.setColor(getContext().getResources().getColor(R.color.shadow_color_range));
        _rect = new RectF();
        rectLeftThumb = new RectF();
        rectRightThumb = new RectF();

        pressedThumb = null;

        setMinStartValue();
        setMaxStartValue();

        setWillNotDraw(false);
    }

    //////////////////////////////////////////
    // PUBLIC METHODS
    //////////////////////////////////////////

    public RangeSeekBarView setCornerRadius(float cornerRadius) {
        this.cornerRadius = cornerRadius;
        return this;
    }

    public RangeSeekBarView setMinValue(float minValue) {
        this.minValue = minValue;
        this.absoluteMinValue = minValue;
        return this;
    }

    public RangeSeekBarView setMaxValue(float maxValue) {
        this.maxValue = maxValue;
        this.absoluteMaxValue = maxValue;
        return this;
    }

    public RangeSeekBarView setMinStartValue(float minStartValue) {
        this.minStartValue = minStartValue;
        this.absoluteMinStartValue = minStartValue;
        setMinStartValue();
        return this;
    }

    public RangeSeekBarView setMaxStartValue(float maxStartValue) {
        this.maxStartValue = maxStartValue;
        this.absoluteMaxStartValue = maxStartValue;
        setMaxStartValue();
        return this;
    }

    public RangeSeekBarView setSteps(float steps) {
        this.steps = steps;
        return this;
    }

    public RangeSeekBarView setGap(float gap) {
        this.gap = gap;
        return this;
    }

    public RangeSeekBarView setFixGap(float fixGap) {
        this.fixGap = fixGap;
        return this;
    }

    public RangeSeekBarView setBarHeight(float height) {
        _barHeight = height;
        return this;
    }

    public RangeSeekBarView setBarColorMode(int barColorMode) {
        this.barColorMode = barColorMode;
        return this;
    }

    public RangeSeekBarView setBarColor(int barColor) {
        this.barColor = barColor;
        return this;
    }

    public RangeSeekBarView setBarGradientStart(int barGradientStart) {
        this.barGradientStart = barGradientStart;
        return this;
    }

    public RangeSeekBarView setBarGradientEnd(int barGradientEnd) {
        this.barGradientEnd = barGradientEnd;
        return this;
    }

    public RangeSeekBarView setBarHighlightColorMode(int barHighlightColorMode) {
        this.barHighlightColorMode = barHighlightColorMode;
        return this;
    }

    public RangeSeekBarView setBarHighlightColor(int barHighlightColor) {
        this.barHighlightColor = barHighlightColor;
        return this;
    }

    public RangeSeekBarView setBarHighlightGradientStart(int barHighlightGradientStart) {
        this.barHighlightGradientStart = barHighlightGradientStart;
        return this;
    }

    public RangeSeekBarView setBarHighlightGradientEnd(int barHighlightGradientEnd) {
        this.barHighlightGradientEnd = barHighlightGradientEnd;
        return this;
    }

    public RangeSeekBarView setLeftThumbColor(int leftThumbColorNormal) {
        this.leftThumbColorNormal = leftThumbColorNormal;
        return this;
    }

    public RangeSeekBarView setLeftThumbHighlightColor(int leftThumbColorPressed) {
        this.leftThumbColorPressed = leftThumbColorPressed;
        return this;
    }

    public RangeSeekBarView setLeftThumbDrawable(int resId) {
        setLeftThumbDrawable(ContextCompat.getDrawable(getContext(), resId));
        return this;
    }

    public RangeSeekBarView setLeftThumbDrawable(Drawable drawable) {
        setLeftThumbBitmap(getBitmap(drawable));
        return this;
    }

    public RangeSeekBarView setLeftThumbBitmap(Bitmap bitmap) {
        leftThumb = bitmap;
        return this;
    }

    public RangeSeekBarView setLeftThumbHighlightDrawable(int resId) {
        setLeftThumbHighlightDrawable(ContextCompat.getDrawable(getContext(), resId));
        return this;
    }

    public RangeSeekBarView setLeftThumbHighlightDrawable(Drawable drawable) {
        setLeftThumbHighlightBitmap(getBitmap(drawable));
        return this;
    }

    public RangeSeekBarView setLeftThumbHighlightBitmap(Bitmap bitmap) {
        leftThumbPressed = bitmap;
        return this;
    }

    public RangeSeekBarView setRightThumbColor(int rightThumbColorNormal) {
        this.rightThumbColorNormal = rightThumbColorNormal;
        return this;
    }

    public RangeSeekBarView setRightThumbHighlightColor(int rightThumbColorPressed) {
        this.rightThumbColorPressed = rightThumbColorPressed;
        return this;
    }

    public RangeSeekBarView setRightThumbDrawable(int resId) {
        setRightThumbDrawable(ContextCompat.getDrawable(getContext(), resId));
        return this;
    }

    public RangeSeekBarView setRightThumbDrawable(Drawable drawable) {
        setRightThumbBitmap(getBitmap(drawable));
        return this;
    }

    public RangeSeekBarView setRightThumbBitmap(Bitmap bitmap) {
        rightThumb = bitmap;
        return this;
    }

    public RangeSeekBarView setRightThumbHighlightDrawable(int resId) {
        setRightThumbHighlightDrawable(ContextCompat.getDrawable(getContext(), resId));
        return this;
    }

    public RangeSeekBarView setRightThumbHighlightDrawable(Drawable drawable) {
        setRightThumbHighlightBitmap(getBitmap(drawable));
        return this;
    }

    public RangeSeekBarView setRightThumbHighlightBitmap(Bitmap bitmap) {
        rightThumbPressed = bitmap;
        return this;
    }

    public RangeSeekBarView setDataType(int dataType) {
        this.dataType = dataType;
        return this;
    }

    public void setOnRangeSeekbarChangeListener(OnRangeSeekbarChangeListener onRangeSeekbarChangeListener) {
        this.onRangeSeekbarChangeListener = onRangeSeekbarChangeListener;
        if (this.onRangeSeekbarChangeListener != null) {
            this.onRangeSeekbarChangeListener.valueChanged(getSelectedMinValue(), getSelectedMaxValue());
        }
    }

    public void setOnRangeSeekbarFinalValueListener(OnRangeSeekbarFinalValueListener onRangeSeekbarFinalValueListener) {
        this.onRangeSeekbarFinalValueListener = onRangeSeekbarFinalValueListener;
    }

    public Number getSelectedMinValue() {
        double nv = normalizedMinValue;
        if (steps > 0 && steps <= ((Math.abs(absoluteMaxValue)) / 2)) {
            float stp = steps / (absoluteMaxValue - absoluteMinValue) * 100;
            double half_step = stp / 2;
            double mod = nv % stp;
            if (mod > half_step) {
                nv = nv - mod;
                nv = nv + stp;
            } else {
                nv = nv - mod;
            }
        } else {
            if (steps != NO_STEP)
                throw new IllegalStateException("steps out of range " + steps);
        }

        return formatValue(normalizedToValue(nv));
    }

    public Number getSelectedMaxValue() {

        double nv = normalizedMaxValue;
        if (steps > 0 && steps <= (Math.abs(absoluteMaxValue) / 2)) {
            float stp = steps / (absoluteMaxValue - absoluteMinValue) * 100;
            double half_step = stp / 2;
            double mod = nv % stp;
            if (mod > half_step) {
                nv = nv - mod;
                nv = nv + stp;
            } else {
                nv = nv - mod;
            }
        } else {
            if (steps != NO_STEP)
                throw new IllegalStateException("steps out of range " + steps);
        }

        return formatValue(normalizedToValue(nv));
    }

    public void apply() {

        // reset normalize min and max value
        normalizedMinValue = 0d;
        normalizedMaxValue = 100d;

        gap = Math.max(0, Math.min(gap, absoluteMaxValue - absoluteMinValue));
        gap = gap / (absoluteMaxValue - absoluteMinValue) * 100;
        if (fixGap != NO_FIXED_GAP) {
            fixGap = Math.min(fixGap, absoluteMaxValue);
            fixGap = fixGap / (absoluteMaxValue - absoluteMinValue) * 100;
            addFixGap(true);
        }

        thumbWidth = getThumbWidth();
        thumbHeight = getThumbHeight();

        //thumbHalfWidth = thumbWidth / 2;
        //thumbHalfHeight = thumbHeight / 2;

        barHeight = getBarHeight();
        barPadding = thumbWidth * 0.5f;

        // set min start value
        if (minStartValue <= absoluteMinValue) {
            minStartValue = 0;
            setNormalizedMinValue(minStartValue);
        } else if (minStartValue >= absoluteMaxValue) {
            minStartValue = absoluteMaxValue;
            setMinStartValue();
        } else {
            setMinStartValue();
        }

        // set max start value
        if (maxStartValue < absoluteMinStartValue || maxStartValue <= absoluteMinValue) {
            maxStartValue = 0;
            setNormalizedMaxValue(maxStartValue);
        } else if (maxStartValue >= absoluteMaxValue) {
            maxStartValue = absoluteMaxValue;
            setMaxStartValue();
        } else {
            setMaxStartValue();
        }
        invalidate();

        if (onRangeSeekbarChangeListener != null) {
            onRangeSeekbarChangeListener.valueChanged(getSelectedMinValue(), getSelectedMaxValue());
        }
    }

    //////////////////////////////////////////
    // PROTECTED METHODS
    //////////////////////////////////////////

    protected Thumb getPressedThumb() {
        return pressedThumb;
    }

    protected float getThumbWidth() {
        return (leftThumb != null) ? leftThumb.getWidth() : getThumbDiameterWidth();
    }

    protected float getThumbHeight() {
        return (leftThumb != null) ? leftThumb.getHeight() : getThumbDiameterHeight();
    }

    protected float getThumbDiameterWidth() {
        return (thumbDiameterWidth > 0) ? thumbDiameterWidth : getResources().getDimension(R.dimen.thumb_width);
    }

    protected float getThumbDiameterHeight() {
        return (thumbDiameterHeight > 0) ? thumbDiameterHeight : getResources().getDimension(R.dimen.thumb_height);
    }

    protected float getBarHeight() {
        return _barHeight > 0 ? _barHeight : (thumbHeight * 0.5f) * 0.3f;
    }

    protected float getBarPadding() {
        return thumbWidth * 0.5f;
    }

    protected Bitmap getBitmap(Drawable drawable) {
        return (drawable != null) ? ((BitmapDrawable) drawable).getBitmap() : null;
    }

    protected float getCornerRadius(final TypedArray typedArray) {
        return typedArray.getFloat(R.styleable.RangeSeekBarView_corner_radius, 0f);
    }

    protected float getMinValue(final TypedArray typedArray) {
        return typedArray.getFloat(R.styleable.RangeSeekBarView_min_value, 0f);
    }

    protected float getMaxValue(final TypedArray typedArray) {
        return typedArray.getFloat(R.styleable.RangeSeekBarView_max_value, 100f);
    }

    protected float getMinStartValue(final TypedArray typedArray) {
        return typedArray.getFloat(R.styleable.RangeSeekBarView_min_start_value, minValue);
    }

    protected float getMaxStartValue(final TypedArray typedArray) {
        return typedArray.getFloat(R.styleable.RangeSeekBarView_max_start_value, maxValue);
    }

    protected float getSteps(final TypedArray typedArray) {
        return typedArray.getFloat(R.styleable.RangeSeekBarView_steps, NO_STEP);
    }

    protected float getGap(final TypedArray typedArray) {
        return typedArray.getFloat(R.styleable.RangeSeekBarView_gap, 0f);
    }

    protected float getFixedGap(final TypedArray typedArray) {
        return typedArray.getFloat(R.styleable.RangeSeekBarView_fix_gap, NO_FIXED_GAP);
    }

    protected int getBarColorMode(final TypedArray typedArray) {
        return typedArray.getInt(R.styleable.RangeSeekBarView_bar_color_mode, 0);
    }

    protected float getBarHeight(final TypedArray typedArray) {
        return typedArray.getDimensionPixelSize(R.styleable.RangeSeekBarView_bar_height, 0);
    }

    protected int getBarColor(final TypedArray typedArray) {
        return typedArray.getColor(R.styleable.RangeSeekBarView_bar_color, Color.GRAY);
    }

    protected int getBarGradientStart(final TypedArray typedArray) {
        return typedArray.getColor(R.styleable.RangeSeekBarView_bar_gradient_start, Color.GRAY);
    }

    protected int getBarGradientEnd(final TypedArray typedArray) {
        return typedArray.getColor(R.styleable.RangeSeekBarView_bar_gradient_end, Color.DKGRAY);
    }

    protected int getBarHighlightColorMode(final TypedArray typedArray) {
        return typedArray.getInt(R.styleable.RangeSeekBarView_bar_highlight_color_mode, 0);
    }

    protected int getBarHighlightColor(final TypedArray typedArray) {
        return typedArray.getColor(R.styleable.RangeSeekBarView_bar_highlight_color, Color.BLACK);
    }

    protected int getBarHighlightGradientStart(final TypedArray typedArray) {
        return typedArray.getColor(R.styleable.RangeSeekBarView_bar_highlight_gradient_start, Color.DKGRAY);
    }

    protected int getBarHighlightGradientEnd(final TypedArray typedArray) {
        return typedArray.getColor(R.styleable.RangeSeekBarView_bar_highlight_gradient_end, Color.BLACK);
    }

    protected int getLeftThumbColor(final TypedArray typedArray) {
        return typedArray.getColor(R.styleable.RangeSeekBarView_left_thumb_color, Color.BLACK);
    }

    protected int getRightThumbColor(final TypedArray typedArray) {
        return typedArray.getColor(R.styleable.RangeSeekBarView_right_thumb_color, Color.BLACK);
    }

    protected int getLeftThumbColorPressed(final TypedArray typedArray) {
        return typedArray.getColor(R.styleable.RangeSeekBarView_left_thumb_color_pressed, Color.DKGRAY);
    }

    protected int getRightThumbColorPressed(final TypedArray typedArray) {
        return typedArray.getColor(R.styleable.RangeSeekBarView_right_thumb_color_pressed, Color.DKGRAY);
    }

    protected Drawable getLeftDrawable(final TypedArray typedArray) {
        return typedArray.getDrawable(R.styleable.RangeSeekBarView_left_thumb_image);
    }

    protected Drawable getRightDrawable(final TypedArray typedArray) {
        return typedArray.getDrawable(R.styleable.RangeSeekBarView_right_thumb_image);
    }

    protected Drawable getLeftDrawablePressed(final TypedArray typedArray) {
        return typedArray.getDrawable(R.styleable.RangeSeekBarView_left_thumb_image_pressed);
    }

    protected Drawable getRightDrawablePressed(final TypedArray typedArray) {
        return typedArray.getDrawable(R.styleable.RangeSeekBarView_right_thumb_image_pressed);
    }

    protected int getDataType(final TypedArray typedArray) {
        return typedArray.getInt(R.styleable.RangeSeekBarView_data_type, DataType.INTEGER);
    }

    protected boolean isSeekBarTouchEnabled(final TypedArray typedArray) {
        return typedArray.getBoolean(R.styleable.RangeSeekBarView_seek_bar_touch_enabled, false);
    }

    protected float getDiameterHeight(final TypedArray typedArray) {
        return typedArray.getDimensionPixelSize(R.styleable.RangeSeekBarView_thumb_diameter, getResources().getDimensionPixelSize(R.dimen.thumb_height));
    }

    protected float getDiameterWidth(final TypedArray typedArray) {
        return typedArray.getDimensionPixelSize(R.styleable.RangeSeekBarView_thumb_diameter, getResources().getDimensionPixelSize(R.dimen.thumb_width));
    }

    protected RectF getLeftThumbRect() {
        return rectLeftThumb;
    }

    protected RectF getRightThumbRect() {
        return rectRightThumb;
    }

    protected void drawRangeBar(Canvas canvas, Paint paint, RectF rect) {
        rect.left = normalizedToScreen(normalizedMinValue);
        rect.right = Math.min(normalizedToScreen(normalizedMaxValue) + (getThumbWidth() / 2) + barPadding, getWidth());
        rect.top = 0;
        rect.bottom = getHeight();
        paint.setColor(barColor);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);
        canvas.drawRect(rect, paint);

    }

    protected void setupBar(final Canvas canvas, final Paint paint, final RectF rect) {
        rect.left = barPadding;
        rect.top = 0.5f * (getHeight() - barHeight);
        rect.right = getWidth() - barPadding;
        rect.bottom = 0.5f * (getHeight() + barHeight);

        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);

        if (barColorMode == 0) {
            paint.setColor(barColor);
            drawBar(canvas, paint, rect);

        } else {
            paint.setShader(
                    new LinearGradient(rect.left, rect.bottom, rect.right, rect.top,
                            barGradientStart,
                            barGradientEnd,
                            Shader.TileMode.MIRROR)
            );

            drawBar(canvas, paint, rect);

            paint.setShader(null);
        }
    }

    protected void drawBar(final Canvas canvas, final Paint paint, final RectF rect) {
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint);
    }

    protected void setupHighlightBar(final Canvas canvas, final Paint paint, final RectF rect) {
        rect.left = normalizedToScreen(normalizedMinValue) + (getThumbWidth() / 2);
        rect.right = normalizedToScreen(normalizedMaxValue) + (getThumbWidth() / 2);

        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);

        if (barHighlightColorMode == 0) {
            paint.setColor(barHighlightColor);
            drawHighlightBar(canvas, paint, rect);

        } else {
            paint.setShader(
                    new LinearGradient(rect.left, rect.bottom, rect.right, rect.top,
                            barHighlightGradientStart,
                            barHighlightGradientEnd,
                            Shader.TileMode.MIRROR)
            );

            drawHighlightBar(canvas, paint, rect);

            paint.setShader(null);
        }
    }

    protected void drawHighlightBar(final Canvas canvas, final Paint paint, final RectF rect) {
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint);
    }

    protected void setupLeftThumb(final Canvas canvas, final Paint paint, final RectF rect) {
        leftThumbColor = (Thumb.MIN.equals(pressedThumb)) ? leftThumbColorPressed : leftThumbColorNormal;
        paint.setColor(leftThumbColor);
        Log.e("drawThumb: ", normalizedMinValue + " " + minValue + " " + minStartValue);
        //float leftL = normalizedToScreen(normalizedMinValue);
        //float rightL = Math.min(leftL + thumbHalfWidth + barPadding, getWidth());
        rectLeftThumb.left = normalizedToScreen(normalizedMinValue);
        rectLeftThumb.right = Math.min(rectLeftThumb.left + (getThumbWidth() / 2) + barPadding, getWidth());
        rectLeftThumb.top = 0f;
        rectLeftThumb.bottom = thumbHeight;

        if (leftThumb != null) {
            Bitmap lThumb = (Thumb.MIN.equals(pressedThumb)) ? leftThumbPressed : leftThumb;
            drawLeftThumbWithImage(canvas, paint, rectLeftThumb, lThumb);
        } else {
            drawLeftThumbWithColor(canvas, paint, rectLeftThumb);
        }
    }

    protected void drawLeftThumbWithColor(final Canvas canvas, final Paint paint, final RectF rect) {
        canvas.drawRect(rect, paint);
    }

    protected void drawLeftThumbWithImage(final Canvas canvas, final Paint paint, final RectF rect, final Bitmap image) {
        canvas.drawBitmap(image, rect.left, rect.top, paint);
    }

    protected void setupRightThumb(final Canvas canvas, final Paint paint, final RectF rect) {

        rightThumbColor = (Thumb.MAX.equals(pressedThumb)) ? rightThumbColorPressed : rightThumbColorNormal;
        paint.setColor(rightThumbColor);

        //float leftR = normalizedToScreen(normalizedMaxValue);
        //float rightR = Math.min(leftR + thumbHalfWidth + barPadding, getWidth());
        Log.e("drawThumb: ", normalizedMaxValue + " " + maxValue + " " + maxStartValue);
        rectRightThumb.left = normalizedToScreen(normalizedMaxValue);
        rectRightThumb.right = Math.min(rectRightThumb.left + (getThumbWidth() / 2) + barPadding, getWidth());
        rectRightThumb.top = 0f;
        rectRightThumb.bottom = thumbHeight;

        if (rightThumb != null) {
            Bitmap rThumb = (Thumb.MAX.equals(pressedThumb)) ? rightThumbPressed : rightThumb;
            drawRightThumbWithImage(canvas, paint, rectRightThumb, rThumb);
        } else {
            drawRightThumbWithColor(canvas, paint, rectRightThumb);
        }
    }

    protected void drawRightThumbWithColor(final Canvas canvas, final Paint paint, final RectF rect) {
        canvas.drawRect(rect, paint);
    }

    protected void drawRightThumbWithImage(final Canvas canvas, final Paint paint, final RectF rect, final Bitmap image) {
        canvas.drawBitmap(image, rect.left, rect.top, paint);
    }

    protected void trackTouchEvent(MotionEvent event) {
        final int pointerIndex = event.findPointerIndex(mActivePointerId);
        try {
            final float x = event.getX(pointerIndex);

            if (Thumb.MIN.equals(pressedThumb)) {
                setNormalizedMinValue(screenToNormalized(x));
            } else if (Thumb.MAX.equals(pressedThumb)) {
                setNormalizedMaxValue(screenToNormalized(x));
            }
        } catch (Exception ignored) {
        }
    }

    protected void touchDown(final float x, final float y) {

    }

    protected void touchMove(final float x, final float y) {

    }

    protected void touchUp(final float x, final float y) {

    }

    protected int getMeasureSpecWith(int widthMeasureSpec) {
        int width = 200;
        if (MeasureSpec.UNSPECIFIED != MeasureSpec.getMode(widthMeasureSpec)) {
            width = MeasureSpec.getSize(widthMeasureSpec);
        }
        return width;
    }

    protected int getMeasureSpecHeight(int heightMeasureSpec) {
        int height = Math.round(thumbHeight);
        if (MeasureSpec.UNSPECIFIED != MeasureSpec.getMode(heightMeasureSpec)) {
            height = Math.min(height, MeasureSpec.getSize(heightMeasureSpec));
        }
        return height;
    }

    protected final void log(Object object) {
        Log.d("CRS=>", String.valueOf(object));
    }

    //////////////////////////////////////////
    // PRIVATE METHODS
    //////////////////////////////////////////

    private void setMinStartValue() {
        if (minStartValue > minValue && minStartValue <= maxValue) {
            minStartValue = Math.min(minStartValue, absoluteMaxValue);
            minStartValue -= absoluteMinValue;
            minStartValue = minStartValue / (absoluteMaxValue - absoluteMinValue) * 100;
            setNormalizedMinValue(minStartValue);
        }
    }

    private void setMaxStartValue() {
        if (maxStartValue <= absoluteMaxValue && maxStartValue > absoluteMinValue && maxStartValue >= absoluteMinStartValue) {
            maxStartValue = Math.max(absoluteMaxStartValue, absoluteMinValue);
            maxStartValue -= absoluteMinValue;
            maxStartValue = maxStartValue * 1.0f / (absoluteMaxValue - absoluteMinValue) * 100;
            setNormalizedMaxValue(maxStartValue);
        }
    }

    private Thumb evalPressedThumb(float touchX) {
        Thumb result = null;

        boolean minThumbPressed = isInThumbRange(touchX, normalizedMinValue);
        boolean maxThumbPressed = isInThumbRange(touchX, normalizedMaxValue);
        if (minThumbPressed && maxThumbPressed) {
            // if both thumbs are pressed (they lie on top of each other), choose the one with more room to drag. this avoids "stalling" the thumbs in a corner, not being able to drag them apart anymore.
            result = (touchX / getWidth() > 0.5f) ? Thumb.MIN : Thumb.MAX;
        } else if (minThumbPressed) {
            result = Thumb.MIN;
        } else if (maxThumbPressed) {
            result = Thumb.MAX;
        }

        if (seekBarTouchEnabled && result == null) {
            result = findClosestThumb(touchX);
        }
        return result;
    }

    private boolean isInThumbRange(float touchX, double normalizedThumbValue) {
        float thumbPos = normalizedToScreen(normalizedThumbValue);
        float left = thumbPos - (getThumbWidth() / 2);
        float right = thumbPos + (getThumbWidth() / 2);
        float x = touchX - (getThumbWidth() / 2);
        if (thumbPos > (getWidth() - thumbWidth)) x = touchX;
        return (x >= left && x <= right);
        //return Math.abs(touchX - normalizedToScreen(normalizedThumbValue)) <= thumbWidth;
    }

    private Thumb findClosestThumb(float touchX) {
        float screenMinX = normalizedToScreen(normalizedMinValue);
        float screenMaxX = normalizedToScreen(normalizedMaxValue);
        if (touchX >= screenMaxX) {
            return Thumb.MAX;
        } else if (touchX <= screenMinX) {
            return Thumb.MIN;
        }

        double minDiff = Math.abs(screenMinX - touchX);
        double maxDiff = Math.abs(screenMaxX - touchX);
        return minDiff < maxDiff ? Thumb.MIN : Thumb.MAX;
    }

    private void onStartTrackingTouch() {
        mIsDragging = true;
    }

    private void onStopTrackingTouch() {
        mIsDragging = false;
    }

    private float normalizedToScreen(double normalizedCoord) {
        float width = getWidth() - (barPadding * 2);
        return (float) normalizedCoord / 100f * width;
    }

    private double screenToNormalized(float screenCoord) {
        double width = getWidth();

        if (width <= 2 * barPadding) {
            // prevent division by zero, simply return 0.
            return 0d;
        } else {
            width = width - (barPadding * 2);
            double result = screenCoord / width * 100d;
            result = result - (barPadding / width * 100d);
            result = Math.min(100d, Math.max(0d, result));
            return result;

        }
    }

    private void setNormalizedMinValue(double value) {
        normalizedMinValue = Math.max(0d, Math.min(100d, Math.min(value, normalizedMaxValue)));
        if (fixGap != NO_FIXED_GAP && fixGap > 0) {
            addFixGap(true);
        } else {
            addMinGap();
        }
        invalidate();
    }

    private void setNormalizedMaxValue(double value) {
        normalizedMaxValue = Math.max(0d, Math.min(100d, Math.max(value, normalizedMinValue)));
        if (fixGap != NO_FIXED_GAP && fixGap > 0) {
            addFixGap(false);
        } else {
            addMaxGap();
        }
        invalidate();
    }

    private void addFixGap(boolean leftThumb) {
        if (leftThumb) {
            normalizedMaxValue = normalizedMinValue + fixGap;
            if (normalizedMaxValue >= 100) {
                normalizedMaxValue = 100;
                normalizedMinValue = normalizedMaxValue - fixGap;
            }
        } else {
            normalizedMinValue = normalizedMaxValue - fixGap;
            if (normalizedMinValue <= 0) {
                normalizedMinValue = 0;
                normalizedMaxValue = normalizedMinValue + fixGap;
            }
        }
    }

    private void addMinGap() {
        if ((normalizedMinValue + gap) > normalizedMaxValue) {
            double g = normalizedMinValue + gap;
            normalizedMaxValue = g;
            normalizedMaxValue = Math.max(0d, Math.min(100d, Math.max(g, normalizedMinValue)));

            if (normalizedMinValue >= (normalizedMaxValue - gap)) {
                normalizedMinValue = normalizedMaxValue - gap;
            }
        }
    }

    private void addMaxGap() {
        if ((normalizedMaxValue - gap) < normalizedMinValue) {
            double g = normalizedMaxValue - gap;
            normalizedMinValue = g;
            normalizedMinValue = Math.max(0d, Math.min(100d, Math.min(g, normalizedMaxValue)));
            if (normalizedMaxValue <= (normalizedMinValue + gap)) {
                normalizedMaxValue = normalizedMinValue + gap;
            }
        }
    }

    private double normalizedToValue(double normalized) {
        double val = normalized / 100 * (maxValue - minValue);
        val = val + minValue;
        return val;
    }

    private void attemptClaimDrag() {
        if (getParent() != null) {
            getParent().requestDisallowInterceptTouchEvent(true);
        }
    }

    private <T extends Number> Number formatValue(T value) throws IllegalArgumentException {
        final Double v = (Double) value;
        if (dataType == DataType.LONG) {
            return v.longValue();
        }
        if (dataType == DataType.DOUBLE) {
            return v;
        }
        if (dataType == DataType.INTEGER) {
            return v.intValue();
        }
        if (dataType == DataType.FLOAT) {
            return v.floatValue();
        }
        if (dataType == DataType.SHORT) {
            return v.shortValue();
        }
        if (dataType == DataType.BYTE) {
            return v.byteValue();
        }
        throw new IllegalArgumentException("Number class '" + value.getClass().getName() + "' is not supported");
    }

    //////////////////////////////////////////
    // OVERRIDE METHODS
    //////////////////////////////////////////

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // prevent render is in edit mode
        if (isInEditMode()) return;
        Log.e("onDraw: ", "drawRangeBar1");
        // setup bar
        //  setupBar(canvas, _paint, _rect);
        drawRangeBar(canvas, _paint, _rect);


        _paint.setStyle(Paint.Style.FILL);
        // draw left thumb
        setupLeftThumb(canvas, _paint, _rect);

        // draw right thumb
        setupRightThumb(canvas, _paint, _rect);
        float bg_middle_left = 0;
        float bg_middle_right = getWidth() - getPaddingRight();
        float rangeL = rectLeftThumb.left;
        float rangeR = rectRightThumb.right;
        RectF leftRect = new RectF((int) bg_middle_left, 0, (int) rangeL, getHeight());
        RectF rightRect = new RectF((int) rangeR, 0, (int) bg_middle_right, getHeight());
        canvas.drawRect(leftRect, mShadow);
        canvas.drawRect(rightRect, mShadow);

    }

    @Override
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(getMeasureSpecWith(widthMeasureSpec), getMeasureSpecHeight(heightMeasureSpec));
    }

    /**
     * Handles thumb selection and movement. Notifies listener callback on certain events.
     */
    @Override
    public synchronized boolean onTouchEvent(MotionEvent event) {

        if (!isEnabled())
            return false;


        final int action = event.getAction();

        switch (action & MotionEvent.ACTION_MASK) {

            case MotionEvent.ACTION_DOWN:
                mActivePointerId = event.getPointerId(event.getPointerCount() - 1);
                pointerIndex = event.findPointerIndex(mActivePointerId);
                float mDownMotionX = event.getX(pointerIndex);

                pressedThumb = evalPressedThumb(mDownMotionX);

                if (pressedThumb == null) return super.onTouchEvent(event);

                touchDown(event.getX(pointerIndex), event.getY(pointerIndex));
                setPressed(true);
                invalidate();
                onStartTrackingTouch();
                trackTouchEvent(event);
                attemptClaimDrag();

                break;

            case MotionEvent.ACTION_MOVE:
                if (pressedThumb != null) {

                    if (mIsDragging) {
                        touchMove(event.getX(pointerIndex), event.getY(pointerIndex));
                        trackTouchEvent(event);
                    }

                    if (onRangeSeekbarChangeListener != null) {
                        if (pressedThumb == Thumb.MIN)
                            onRangeSeekbarChangeListener.valueMinChanged(getSelectedMinValue(), getSelectedMaxValue());
                        else
                            onRangeSeekbarChangeListener.valueMaxChanged(getSelectedMinValue(), getSelectedMaxValue());
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mIsDragging) {
                    trackTouchEvent(event);
                    onStopTrackingTouch();
                    setPressed(false);
                    touchUp(event.getX(pointerIndex), event.getY(pointerIndex));
                    if (onRangeSeekbarFinalValueListener != null) {
                        onRangeSeekbarFinalValueListener.finalValue(getSelectedMinValue(), getSelectedMaxValue());
                    }
                } else {
                    // Touch up when we never crossed the touch slop threshold
                    // should be interpreted as a tap-seek to that location.
                    onStartTrackingTouch();
                    trackTouchEvent(event);
                    onStopTrackingTouch();
                }

                pressedThumb = null;
                invalidate();
                break;
            case MotionEvent.ACTION_POINTER_DOWN: {
                //final int index = event.getPointerCount() - fc;
                // final int index = ev.getActionIndex();
                /*mDownMotionX = event.getX(index);
                mActivePointerId = event.getPointerId(index);
                invalidate();*/
                break;
            }
            case MotionEvent.ACTION_POINTER_UP:
                /*onSecondaryPointerUp(event);*/
                invalidate();
                break;
            case MotionEvent.ACTION_CANCEL:
                if (mIsDragging) {
                    onStopTrackingTouch();
                    setPressed(false);
                    touchUp(event.getX(pointerIndex), event.getY(pointerIndex));
                    if (onRangeSeekbarFinalValueListener != null) {
                        onRangeSeekbarFinalValueListener.finalValue(getSelectedMinValue(), getSelectedMaxValue());
                    }
                }
                invalidate(); // see above explanation
                break;
        }

        return true;

    }

}
