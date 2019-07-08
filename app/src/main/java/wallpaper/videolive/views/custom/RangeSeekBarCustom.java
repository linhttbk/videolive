package wallpaper.videolive.views.custom;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import wallpaper.videolive.R;

public class RangeSeekBarCustom extends View {

    private int progressColor;
    private int thumbColor;
    private boolean isDragging = false;

    public int getMinValue() {
        return minValue;
    }

    public int getMaxValue() {
        return maxValue;
    }

    private int minValue;
    private int maxValue;
    public static final Integer DEFAULT_MINIMUM = 0;
    public static final Integer DEFAULT_MAXIMUM = 100;
    public static final Integer DEFAULT_WIDTH_THUMB = 20;
    private boolean showLabel;
    private int currentDuration;
    private int totalValue;
    int width, height;
    private Thumb currentTouch = null;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private int widthThumb;
    private OnRangeChangeListener onRangeChangeListener;

    public void setOnRangeChangeListener(OnRangeChangeListener onRangeChangeListener) {
        this.onRangeChangeListener = onRangeChangeListener;
    }

    private enum Thumb {
        MIN, MAX
    }

    public RangeSeekBarCustom(Context context) {
        this(context, null);
    }

    public RangeSeekBarCustom(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public RangeSeekBarCustom(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public RangeSeekBarCustom(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.RangeSeekBarCustom, 0, 0);
        try {
            progressColor = ta.getColor(R.styleable.RangeSeekBarCustom_progressColor, getResources().getColor(R.color.white));
            thumbColor = ta.getColor(R.styleable.RangeSeekBarCustom_thumbColor, getResources().getColor(R.color.black));
            minValue = ta.getInt(R.styleable.RangeSeekBarCustom_minValue, DEFAULT_MINIMUM);
            maxValue = ta.getInt(R.styleable.RangeSeekBarCustom_minValue, DEFAULT_MAXIMUM);
            widthThumb = ta.getDimensionPixelSize(R.styleable.RangeSeekBarCustom_widthThumb, DEFAULT_WIDTH_THUMB);
            totalValue = DEFAULT_MAXIMUM;
            showLabel = ta.getBoolean(R.styleable.RangeSeekBarCustom_showLabel, true);
            currentDuration = 0;
        } finally {
            ta.recycle();
        }

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        if (MeasureSpec.UNSPECIFIED != MeasureSpec.getMode(widthMeasureSpec)) {
            width = MeasureSpec.getSize(widthMeasureSpec);
        }
        if (MeasureSpec.UNSPECIFIED != MeasureSpec.getMode(heightMeasureSpec)) {
            height = MeasureSpec.getSize(heightMeasureSpec);
        }
        setMeasuredDimension(width, height);
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        //draw rect view
        RectF rect = new RectF(0, 0, getWidth(), getHeight());
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(progressColor);
        paint.setAntiAlias(true);
        canvas.drawRect(rect, paint);

        //draw rect rangeBar
        rect.left = getLeftThumb(minValue);
        rect.right = getLeftThumb(maxValue);
        rect.top = 0;
        rect.bottom = getHeight();
        paint.setColor(thumbColor);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);
        canvas.drawRect(rect, paint);
        //draw thumb left
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeCap(Paint.Cap.ROUND);
        rect.left = getLeftThumb(minValue);
        rect.right = rect.left + 20;
        rect.top = 0;
        rect.bottom = getHeight();
        paint.setColor(thumbColor);
        canvas.drawRect(rect, paint);


        //draw thumb right
        rect.left = getLeftThumb(maxValue) - 20;
        rect.right = rect.left + 20;
        rect.top = 0;
        rect.bottom = getHeight();
        paint.setColor(thumbColor);
        canvas.drawRect(rect, paint);

        //draw progress
        Log.e("draw: ", isDragging + " ");
        if (!isDragging) {
            rect.left = getLeftCurrent(currentDuration);
            Log.e("draw: ", rect.left + " ");
            rect.right = rect.left + 5;
            rect.top = 0;
            rect.bottom = getHeight();
            paint.setColor(Color.BLACK);
            canvas.drawRect(rect, paint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.e("onTouchEvent: ", event.getAction() + " ");
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                currentTouch = evalPressedThumb(event.getX());
                break;
            case MotionEvent.ACTION_MOVE:
                if (currentTouch != null) {
                    isDragging = true;
                    Log.e("onTouchEvent: ", minValue + " " + maxValue);
                    int value = calculatorValue(event.getX());
                    ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) this.getLayoutParams();
                    Log.e("onTouchEvent: ", value + " " + maxValue + " " + lp.leftMargin + " " + getWidth());
                    if (currentTouch == Thumb.MIN) {

                        if (value > maxValue - 2 * calculatorValue(widthThumb)-5 || value < 0)
                            return false;
                        minValue = value;
                        if (onRangeChangeListener != null)
                            onRangeChangeListener.onSeekWithThumbLeft(minValue, maxValue);
                    } else {
                        if (value < minValue + 2 * calculatorValue(widthThumb)+5 || value > totalValue)
                            return false;
                        maxValue = value;
                        if (onRangeChangeListener != null)
                            onRangeChangeListener.onSeekWithThumbRight(minValue, maxValue);
                    }

                    invalidate();

                }
                break;
            case MotionEvent.ACTION_UP:
                currentTouch = null;
                isDragging = false;
                if (onRangeChangeListener != null)
                    onRangeChangeListener.onSeekFinish(minValue, maxValue);
                invalidate();
                break;
            case MotionEvent.ACTION_CANCEL:
                currentTouch = null;
                isDragging = false;
                if (onRangeChangeListener != null)
                    onRangeChangeListener.onSeekFinish(minValue, maxValue);
                invalidate();
                break;

        }

        return true;
    }

    private int getLeftThumb(int value) {
        double ratio = value * 1.0 / totalValue;
        return (int) (ratio * getWidth());
    }

    private int getLeftCurrent(int value) {
        double ratio = value * 1.0 / totalValue;
        int result = (int) (ratio * getWidth());
        int maxLeftThumb = getLeftThumb(maxValue);
        int currentLeftThumb = getLeftThumb(currentDuration);
        if (maxValue - minValue < 10)
            result = getLeftThumb((int) ((minValue + maxValue) * 1.0 / 2));
        else if (result >= getLeftThumb(maxValue) - DEFAULT_WIDTH_THUMB - 5)
            result = maxLeftThumb - DEFAULT_WIDTH_THUMB;
        else
            result = currentLeftThumb + DEFAULT_WIDTH_THUMB < maxLeftThumb - DEFAULT_WIDTH_THUMB ? currentLeftThumb+DEFAULT_WIDTH_THUMB : maxLeftThumb;
        return result;
    }

    public void setTotalValue(int totalValue) {
        this.totalValue = totalValue;
        invalidate();
    }

    public void setRangeValue(int minValue, int maxValue) {
        this.minValue = minValue;
        this.maxValue = maxValue;
        Log.e("setRangeValue: ", minValue + " " + maxValue);
        invalidate();
    }

    public void setCurrentDuration(int currentDuration) {
        this.currentDuration = currentDuration;
        invalidate();
    }

    private Thumb evalPressedThumb(float touchX) {
        Thumb result = null;
        boolean minThumbPressed = Math.abs((int) touchX - (getLeftThumb(minValue) + widthThumb/2)) <= 40 ? true : false;
        boolean maxThumbPressed = Math.abs((int) touchX - (getLeftThumb(maxValue)- widthThumb/2)) <= 40 ? true : false;
        if (minThumbPressed) {
            result = Thumb.MIN;
        } else if (maxThumbPressed) {
            result = Thumb.MAX;
        }
        return result;
    }

    private int calculatorValue(float dx) {
        double ratio = dx * 1.0 / getWidth();
        int result = (int) (ratio * totalValue);
        return result > 0 ? result : 0;
    }

    private int calculatorValue(int size) {
        double ratio = size * 1.0 / getWidth();
        int result = (int) (ratio * totalValue);
        return result > 0 ? result : 0;
    }

    public interface OnRangeChangeListener {
        void onSeekWithThumbLeft(int minValue, int maxValue);

        void onSeekWithThumbRight(int minValue, int maxValue);

        void onSeekFinish(int minValue, int maxValue);

    }
}
