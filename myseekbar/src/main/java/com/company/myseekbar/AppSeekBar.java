package com.company.myseekbar;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.Window;
import android.view.WindowManager;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import java.math.BigDecimal;
import static com.company.myseekbar.ModuleUtil.dp2px;
import static com.company.myseekbar.ModuleUtil.sp2px;


public class AppSeekBar extends View {

    private float mMin; // min
    private float mMax; // max
    private float mProgress; // real time value
    private boolean isFloatType; // support for float type output
    private int mTrackSize; // height of right-track(on the right of thumb)
    private int mSecondTrackSize; // height of left-track(on the left of thumb)
    private int mThumbRadiusOnDragging; // radius of thumb when be dragging
    private int mTrackColor; // color of right-track
    private int mThumbColor; // color of thumb
    private int mSectionCount; // shares of whole progress(max - min)
    private boolean isShowSectionMark; // show demarcation points or not--是否显示分界点
    private boolean isShowSectionText; // show section-text or not
    private int mSectionTextSize; // text size of section-text
    private int mSectionTextColor; // text color of section-text
    private int mSectionTextInterval; // the interval of two section-text

    private boolean isTouchToSeek; // touch anywhere on track to quickly seek

    private long mAnimDuration; // duration of animation
    private boolean isAlwaysShowBubble; // bubble shows all time

    private int mBubbleColor;// color of bubble
    private int mBubbleTextSize; // text size of bubble-progress
    private int mBubbleTextColor; // text color of bubble-progress

    private float mDelta; // max - min
    private float mSectionValue; // (mDelta / mSectionCount)
    private float mThumbCenterX; // X coordinate of thumb's center
    private float mTrackLength; // pixel length of whole track
    private float mSectionOffset; // pixel length of one section
    private boolean isThumbOnDragging; // is thumb on dragging or not
    private int mTextSpace; // space between text and track
    private boolean triggerBubbleShowing;
    private SparseArray<String> mSectionTextArray = new SparseArray<>();
    private boolean isShowProgressInFloat;
    private OnProgressChangedListener mProgressListener; // progress changing listener
    private float mLeft; // space between left of track and left of the view
    private float mRight; // space between right of track and left of the view
    private Paint mPaint;
    private Rect mRectText;

    private WindowManager mWindowManager;
    private BubbleView mBubbleView;
    private int mBubbleRadius;
    private float mBubbleCenterRawSolidX;
    private float mBubbleCenterRawSolidY;
    private float mBubbleCenterRawX;
    private WindowManager.LayoutParams mLayoutParams;
    private int[] mPoint = new int[2];
    private int lineWidthBase = ModuleUtil.dp2px(1);
    private float minLineWidthBase = ModuleUtil.dp2pxF(0.45f);

    public AppSeekBar(Context context) {
        this(context, null);
    }

    public AppSeekBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AppSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AppSeekBar, defStyleAttr, 0);
        mMin = a.getFloat(R.styleable.AppSeekBar_bsb_min, 18.0f);
        mMax = a.getFloat(R.styleable.AppSeekBar_bsb_max, 48.0f);
        mProgress = a.getFloat(R.styleable.AppSeekBar_bsb_progress, 34);
        isFloatType = a.getBoolean(R.styleable.AppSeekBar_bsb_is_float_type, false);
        mTrackSize = a.getDimensionPixelSize(R.styleable.AppSeekBar_bsb_track_size, dp2px(1));
        mThumbRadiusOnDragging = a.getDimensionPixelSize(R.styleable.AppSeekBar_bsb_thumb_radius_on_dragging,
                mTrackSize + dp2px(4));
        mSectionCount = a.getInteger(R.styleable.AppSeekBar_bsb_section_count, 3);
        mTrackColor = a.getColor(R.styleable.AppSeekBar_bsb_track_color,
                ContextCompat.getColor(context, R.color.colorPrimary));
        mThumbColor = a.getColor(R.styleable.AppSeekBar_bsb_thumb_color,  ContextCompat.getColor(context, R.color.colorPrimary));
        isShowSectionText = a.getBoolean(R.styleable.AppSeekBar_bsb_show_section_text, true);
        mSectionTextSize = a.getDimensionPixelSize(R.styleable.AppSeekBar_bsb_section_text_size, sp2px(14));
        mSectionTextColor = a.getColor(R.styleable.AppSeekBar_bsb_section_text_color,  ContextCompat.getColor(context, R.color.colorSectionText)); // 分段线下文字颜色

        mSectionTextInterval = a.getInteger(R.styleable.AppSeekBar_bsb_section_text_interval, 1);
        mBubbleColor = a.getColor(R.styleable.AppSeekBar_bsb_bubble_color, ContextCompat.getColor(context, R.color.colorBubble));
        mBubbleTextSize = a.getDimensionPixelSize(R.styleable.AppSeekBar_bsb_bubble_text_size, sp2px(40));
        mBubbleTextColor = a.getColor(R.styleable.AppSeekBar_bsb_bubble_text_color, Color.BLACK);
        isShowSectionMark = a.getBoolean(R.styleable.AppSeekBar_bsb_show_section_mark, true);
        isShowProgressInFloat = a.getBoolean(R.styleable.AppSeekBar_bsb_show_progress_in_float, false);
        int duration = a.getInteger(R.styleable.AppSeekBar_bsb_anim_duration, -1);
        mAnimDuration = duration < 0 ? 200 : duration;
        isTouchToSeek = a.getBoolean(R.styleable.AppSeekBar_bsb_touch_to_seek, false);
        isAlwaysShowBubble = a.getBoolean(R.styleable.AppSeekBar_bsb_always_show_bubble, false);
        setEnabled(a.getBoolean(R.styleable.AppSeekBar_android_enabled, isEnabled()));
        a.recycle();

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setTextAlign(Paint.Align.CENTER);

        mRectText = new Rect();
        mTextSpace = dp2px(2);
        initConfigByPriority();
        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        mBubbleView = new BubbleView(context);
        mBubbleView.setProgressText(isShowProgressInFloat ?
                String.valueOf(getProgressFloat()) : String.valueOf(getProgress()));

        mLayoutParams = new WindowManager.LayoutParams();
        mLayoutParams.gravity = Gravity.START | Gravity.TOP;
        mLayoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        mLayoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        mLayoutParams.format = PixelFormat.TRANSLUCENT;
        mLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
        if (ModuleUtil.isMIUI() || Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            mLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION;
        } else {
            mLayoutParams.type = WindowManager.LayoutParams.TYPE_TOAST;
        }
        calculateRadiusOfBubble();
    }



    private void initConfigByPriority() {
        if (mMin == mMax) {
            mMin = 0.0f;
            mMax = 100.0f;
        }
        if (mMin > mMax) {
            float tmp = mMax;
            mMax = mMin;
            mMin = tmp;
        }
        if (mProgress < mMin) {
            mProgress = mMin;
        }
        if (mProgress > mMax) {
            mProgress = mMax;
        }

        mDelta = mMax - mMin;
        mSectionValue = mDelta / mSectionCount;

        if (mSectionValue < 1) {
            isFloatType = true;
        }
        if (isFloatType) {
            isShowProgressInFloat = true;
        }

        if (mSectionTextInterval < 1) {
            mSectionTextInterval = 1;
        }

        initSectionTextArray();

        if (isAlwaysShowBubble) {
            setProgress(mProgress);
        }

    }

    /**
     * Calculate radius of bubble according to the Min and the Max
     */
    private void calculateRadiusOfBubble() {
        mPaint.setTextSize(mBubbleTextSize);

        // 计算滑到两端气泡里文字需要显示的宽度，比较取最大值为气泡的半径
        String text;
        if (isShowProgressInFloat) {
            text = float2String( mMin);
        } else {
                text = isFloatType ? float2String(mMin) : String.valueOf((int) mMin);
        }
        mPaint.getTextBounds(text, 0, text.length(), mRectText);
        int w1 = (mRectText.width() + mTextSpace * 2) >> 1;

        if (isShowProgressInFloat) {
            text = float2String( mMax);
        } else {
                text = isFloatType ? float2String(mMax) : String.valueOf((int) mMax);
        }
        mPaint.getTextBounds(text, 0, text.length(), mRectText);
        int w2 = (mRectText.width() + mTextSpace * 2) >> 1;

        mBubbleRadius = dp2px(35); // default 14dp
        int max = Math.max(mBubbleRadius, Math.max(w1, w2));
        mBubbleRadius = max + mTextSpace;
    }

    private void initSectionTextArray() {
        float sectionValue;
        for (int i = 0; i <= mSectionCount; i++) {
            sectionValue =  mMin + mSectionValue * i;
            mSectionTextArray.put(i, isFloatType ? float2String(sectionValue) : (int) sectionValue + "");
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int height = mThumbRadiusOnDragging * 2;
        if (isShowSectionText) {
            mPaint.setTextSize(mSectionTextSize);
            mPaint.getTextBounds("j", 0, 1, mRectText);
            height = Math.max(height, mThumbRadiusOnDragging * 2 + mRectText.height());
        }
        height += mTextSpace * 2;
        setMeasuredDimension(resolveSize(dp2px(180), widthMeasureSpec), height);

        mLeft = getPaddingLeft() + mThumbRadiusOnDragging;
        mRight = getMeasuredWidth() - getPaddingRight() - mThumbRadiusOnDragging;

        if (isShowSectionText) {
            mPaint.setTextSize(mSectionTextSize);
            String text = mSectionTextArray.get(0);
                mPaint.getTextBounds(text, 0, text.length(), mRectText);
                float max = Math.max(mThumbRadiusOnDragging, mRectText.width() / 2f);
                mLeft = getPaddingLeft() + max + mTextSpace;

                text = mSectionTextArray.get(mSectionCount);
                mPaint.getTextBounds(text, 0, text.length(), mRectText);
                max = Math.max(mThumbRadiusOnDragging, mRectText.width() / 2f);
                mRight = getMeasuredWidth() - getPaddingRight() - max - mTextSpace;
        }

        mTrackLength = mRight - mLeft;
        mSectionOffset = mTrackLength * 1f / mSectionCount;

        mBubbleView.measure(widthMeasureSpec, heightMeasureSpec);

    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        locatePositionInWindow();
    }

    /**
     *
     * 气泡BubbleView实际是通过WindowManager动态添加的一个视图，因此与SeekBar唯一的位置联系就是它们在屏幕上的
     * 绝对坐标。
     * 先计算进度mProgress为mMin时BubbleView的中心坐标（mBubbleCenterRawSolidX，mBubbleCenterRawSolidY），
     * 然后根据进度来增量计算横坐标mBubbleCenterRawX，再动态设置LayoutParameter.x，就实现了气泡跟随滑动移动。
     */
    private void locatePositionInWindow() {
        getLocationInWindow(mPoint);
        ViewParent parent = getParent();
        if (parent instanceof View && ((View) parent).getMeasuredWidth() > 0) {
            mPoint[0] %= ((View) parent).getMeasuredWidth();
        }

        mBubbleCenterRawSolidX = mPoint[0] + mLeft - mBubbleView.getMeasuredWidth() / 2f;
        mBubbleCenterRawX = calculateCenterRawXofBubbleView();
        mBubbleCenterRawSolidY = mPoint[1] - mBubbleView.getMeasuredHeight();
        mBubbleCenterRawSolidY -= dp2px(24);
        if (ModuleUtil.isMIUI()) {
            mBubbleCenterRawSolidY -= dp2px(4);
        }

        Context context = getContext();
        if (context instanceof Activity) {
            Window window = ((Activity) context).getWindow();
            if (window != null) {
                int flags = window.getAttributes().flags;
                if ((flags & WindowManager.LayoutParams.FLAG_FULLSCREEN) != 0) {
                    Resources res = Resources.getSystem();
                    int id = res.getIdentifier("status_bar_height", "dimen", "android");
                    mBubbleCenterRawSolidY += res.getDimensionPixelSize(id);
                }
            }
        }
    }



    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float xLeft = getPaddingLeft();
        float xRight = getMeasuredWidth() - getPaddingRight();
        float yTop = getPaddingTop() + mThumbRadiusOnDragging;

        // draw sectionText BOTTOM_SIDES
        if (isShowSectionText) {
            mPaint.setColor(mSectionTextColor);
            mPaint.setTextSize(mSectionTextSize);
            mPaint.getTextBounds("0123456789", 0, "0123456789".length(), mRectText); // compute solid height

                String text = mSectionTextArray.get(0);
                mPaint.getTextBounds(text, 0, text.length(), mRectText);
                xLeft = mLeft;


                text = mSectionTextArray.get(mSectionCount);
                mPaint.getTextBounds(text, 0, text.length(), mRectText);
                xRight = mRight;
        }

        boolean isShowTextBelowSectionMark = isShowSectionText;

        // draw sectionMark & sectionText BELOW_SECTION_MARK
        if (isShowTextBelowSectionMark || isShowSectionMark) {
            mPaint.setTextSize(mSectionTextSize);
            mPaint.getTextBounds("0123456789", 0, "0123456789".length(), mRectText); // compute solid height

            float x_;
            float y_ = yTop + mRectText.height() + mThumbRadiusOnDragging + mTextSpace;

            for (int i = 0; i <= mSectionCount; i++) {
                x_ = xLeft + i * mSectionOffset;
                mPaint.setColor(mTrackColor);
                // sectionMark
                mPaint.setStrokeWidth(lineWidthBase);
                canvas.drawLine(x_,0,x_,yTop,mPaint);
                float x_mini = 0;
                // section mini Mark
                for(int j=1;j<10;j++){
                    x_mini =  x_ + mSectionOffset/10*j;
                    mPaint.setStrokeWidth(minLineWidthBase);
                    canvas.drawLine(x_mini,5,x_mini,yTop,mPaint);
                }
                // sectionText belows section
                if (isShowTextBelowSectionMark) {
                    mPaint.setColor(mSectionTextColor);
                    if (mSectionTextArray.get(i, null) != null) {
                        canvas.drawText(mSectionTextArray.get(i), x_, y_, mPaint);
                    }
                }
            }
        }

        if (!isThumbOnDragging || isAlwaysShowBubble) {

                mThumbCenterX = xLeft + mTrackLength / mDelta * (mProgress - mMin);
        }


        // draw init track
        mPaint.setColor(mTrackColor);
        mPaint.setStrokeWidth(dp2px(1));
        canvas.drawLine(xLeft, yTop, xRight, yTop, mPaint);

        // draw thumb inditar
        mPaint.setColor(mThumbColor);
        canvas.drawLine(mThumbCenterX, yTop+30, mThumbCenterX, yTop-30, mPaint);

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        post(new Runnable() {
            @Override
            public void run() {
                requestLayout();
            }
        });
    }

    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        if (visibility != VISIBLE) {
            hideBubble();
        } else {
            if (triggerBubbleShowing) {
                showBubble();
            }
        }
        super.onVisibilityChanged(changedView, visibility);
    }

    @Override
    protected void onDetachedFromWindow() {
        hideBubble();
        super.onDetachedFromWindow();
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    float dx;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                performClick();
                getParent().requestDisallowInterceptTouchEvent(true);

                isThumbOnDragging = isThumbTouched(event);
                if (isThumbOnDragging) {
                    if (isAlwaysShowBubble && !triggerBubbleShowing) {
                        triggerBubbleShowing = true;
                    }
                    showBubble();
                    invalidate();
                } else if (isTouchToSeek && isTrackTouched(event)) {
                    isThumbOnDragging = true;
                    if (isAlwaysShowBubble) {
                        hideBubble();
                        triggerBubbleShowing = true;
                    }


                        mThumbCenterX = event.getX();
                        if (mThumbCenterX < mLeft) {
                            mThumbCenterX = mLeft;
                        }
                        if (mThumbCenterX > mRight) {
                            mThumbCenterX = mRight;
                        }


                    mProgress = calculateProgress();
                    mBubbleCenterRawX = calculateCenterRawXofBubbleView();
                    showBubble();
                    invalidate();
                }

                dx = mThumbCenterX - event.getX();

                break;
            case MotionEvent.ACTION_MOVE:
                if (isThumbOnDragging) {
                    boolean flag = true;

                        mThumbCenterX = event.getX() + dx;
                        if (mThumbCenterX < mLeft) {
                            mThumbCenterX = mLeft;
                        }
                        if (mThumbCenterX > mRight) {
                            mThumbCenterX = mRight;
                        }

                    if (flag) {
                        mProgress = calculateProgress();
                        if ( mBubbleView.getParent() != null) {
                            mBubbleCenterRawX = calculateCenterRawXofBubbleView();
                            mLayoutParams.x = (int) (mBubbleCenterRawX + 0.5f);
                            mWindowManager.updateViewLayout(mBubbleView, mLayoutParams);
                            mBubbleView.setProgressText(isShowProgressInFloat ?
                                    String.valueOf(getProgressFloat()) : String.valueOf(getProgress()));
                        } else {
                            processProgress();
                        }
                        invalidate();
                        if (mProgressListener != null) {
                            mProgressListener.onProgressChanged(this, getProgress(), getProgressFloat(), true);
                        }
                    }
                }

                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                getParent().requestDisallowInterceptTouchEvent(false);

               if (isThumbOnDragging || isTouchToSeek) {
                    postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mBubbleView.animate()
                                    .alpha(isAlwaysShowBubble ? 1f : 0f)
                                    .setDuration(mAnimDuration)
                                    .setListener(new AnimatorListenerAdapter() {
                                        @Override
                                        public void onAnimationEnd(Animator animation) {
                                            if (!isAlwaysShowBubble) {
                                                hideBubble();
                                            }

                                            isThumbOnDragging = false;
                                            invalidate();
                                        }

                                        @Override
                                        public void onAnimationCancel(Animator animation) {
                                            if (!isAlwaysShowBubble) {
                                                hideBubble();
                                            }

                                            isThumbOnDragging = false;
                                            invalidate();
                                        }
                                    }).start();
                        }
                    }, mAnimDuration);
                }


                if (mProgressListener != null) {
                    mProgressListener.onProgressChanged(this, getProgress(), getProgressFloat(), true);
                    mProgressListener.getProgressOnActionUp(this, getProgress(), getProgressFloat());
                }

                break;
        }

        return isThumbOnDragging || isTouchToSeek || super.onTouchEvent(event);
    }

    /**
     * Detect effective touch of thumb
     */
    private boolean isThumbTouched(MotionEvent event) {
        if (!isEnabled())
            return false;

        float distance = mTrackLength / mDelta * (mProgress - mMin);
        float x =  mLeft + distance;
        float y = getMeasuredHeight() / 2f;
        return (event.getX() - x) * (event.getX() - x) + (event.getY() - y) * (event.getY() - y)
                <= (mLeft + dp2px(8)) * (mLeft + dp2px(8));
    }

    /**
     * Detect effective touch of track
     */
    private boolean isTrackTouched(MotionEvent event) {
        return isEnabled() && event.getX() >= getPaddingLeft() && event.getX() <= getMeasuredWidth() - getPaddingRight()
                && event.getY() >= getPaddingTop() && event.getY() <= getMeasuredHeight() - getPaddingBottom();
    }


    /**
     * Showing the Bubble depends the way that the WindowManager adds a Toast type view to the Window.
     * 显示气泡
     * 原理是利用WindowManager动态添加一个与Toast相同类型的BubbleView，消失时再移除
     */
    private void showBubble() {
        if (mBubbleView == null || mBubbleView.getParent() != null) {
            return;
        }
        mLayoutParams.x = (int) (mBubbleCenterRawX + 0.5f);
        mLayoutParams.y = (int) (mBubbleCenterRawSolidY + 0.5f);

        mBubbleView.setAlpha(0);
        mBubbleView.setVisibility(VISIBLE);
        mBubbleView.animate().alpha(1f).setDuration(isTouchToSeek ? 0 : mAnimDuration)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        mWindowManager.addView(mBubbleView, mLayoutParams);
                    }
                }).start();
        mBubbleView.setProgressText(isShowProgressInFloat ?
                String.valueOf(getProgressFloat()) : String.valueOf(getProgress()));
    }

    /**
     * The WindowManager removes the BubbleView from the Window.
     */
    private void hideBubble() {
        if (mBubbleView == null)
            return;

        mBubbleView.setVisibility(GONE);
        if (mBubbleView.getParent() != null) {
            mWindowManager.removeViewImmediate(mBubbleView);
        }
    }

    private String float2String(float value) {
        return String.valueOf(formatFloat(value));
    }

    private float formatFloat(float value) {
        BigDecimal bigDecimal = BigDecimal.valueOf(value);
        return bigDecimal.setScale(1, BigDecimal.ROUND_HALF_UP).floatValue();
    }

    private float calculateCenterRawXofBubbleView() {
        return mBubbleCenterRawSolidX + mTrackLength * (mProgress - mMin) / mDelta;
    }

    private float calculateProgress() {
        return (mThumbCenterX - mLeft) * mDelta / mTrackLength + mMin;
    }


    public void setProgress(float progress) {
        mProgress = progress;
        if (mProgressListener != null) {
            mProgressListener.onProgressChanged(this, getProgress(), getProgressFloat(), false);
            mProgressListener.getProgressOnFinally(this, getProgress(), getProgressFloat(), false);
        }
        mBubbleCenterRawX = calculateCenterRawXofBubbleView();

        if (isAlwaysShowBubble) {
            hideBubble();

            postDelayed(new Runnable() {
                @Override
                public void run() {
                    showBubble();
                    triggerBubbleShowing = true;
                }
            }, 0);
        }


        postInvalidate();
    }

    public int getProgress() {
        return Math.round(processProgress());
    }

    public float getProgressFloat() {
        return formatFloat(processProgress());
    }

    private float processProgress() {
        return mProgress;
    }


    public void setOnProgressChangedListener(OnProgressChangedListener onProgressChangedListener) {
        mProgressListener = onProgressChangedListener;
    }



    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable("save_instance", super.onSaveInstanceState());
        bundle.putFloat("progress", mProgress);
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            mProgress = bundle.getFloat("progress");
            super.onRestoreInstanceState(bundle.getParcelable("save_instance"));
            if (mBubbleView != null) {
                mBubbleView.setProgressText(isShowProgressInFloat ?
                        String.valueOf(getProgressFloat()) : String.valueOf(getProgress()));
            }
            setProgress(mProgress);
            return;
        }

        super.onRestoreInstanceState(state);
    }

    /**
     * Listen to progress onChanged, onActionUp, onFinally
     */
    public interface OnProgressChangedListener {

        void onProgressChanged(AppSeekBar AppSeekBar, int progress, float progressFloat, boolean fromUser);

        void getProgressOnActionUp(AppSeekBar AppSeekBar, int progress, float progressFloat);

        void getProgressOnFinally(AppSeekBar AppSeekBar, int progress, float progressFloat, boolean fromUser);
    }


    /**
     * 自定义气球
     */
    private class BubbleView extends View {

        private Paint mBubblePaint;
        private Path mBubblePath;
        private RectF mBubbleRectF;
        private Rect mRect;
        private String mProgressText = "";

        BubbleView(Context context) {
            this(context, null);
        }

        BubbleView(Context context, AttributeSet attrs) {
            this(context, attrs, 0);
        }

        BubbleView(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);

            mBubblePaint = new Paint();
            mBubblePaint.setAntiAlias(true);
            mBubblePaint.setTextAlign(Paint.Align.CENTER);

            mBubblePath = new Path();
            mBubbleRectF = new RectF();
            mRect = new Rect();
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            setMeasuredDimension(3 * mBubbleRadius, 3 * mBubbleRadius);
            mBubbleRectF.set(getMeasuredWidth() / 2f - mBubbleRadius, 0,
                    getMeasuredWidth() / 2f + mBubbleRadius, 2 * mBubbleRadius);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            mBubblePath.reset();
            float x0 = getMeasuredWidth() / 2f;
            float y0 = getMeasuredHeight() - mBubbleRadius / 3f;
            mBubblePath.moveTo(x0, y0);
            float x1 = (float) (getMeasuredWidth() / 2f - Math.sqrt(3) / 2f * mBubbleRadius);
            float y1 = 3 / 2f * mBubbleRadius;
            mBubblePath.quadTo(
                    x1 - dp2px(2), y1 - dp2px(2),
                    x1, y1
            );
            mBubblePath.arcTo(mBubbleRectF, 150, 240);

            float x2 = (float) (getMeasuredWidth() / 2f + Math.sqrt(3) / 2f * mBubbleRadius);
            mBubblePath.quadTo(
                    x2 + dp2px(2), y1 - dp2px(2),
                    x0, y0
            );
            mBubblePath.close();

            mBubblePaint.setColor(mBubbleColor);
            canvas.drawPath(mBubblePath, mBubblePaint);

            mBubblePaint.setTextSize(mBubbleTextSize);
            mBubblePaint.setColor(mBubbleTextColor);
            mBubblePaint.getTextBounds(mProgressText, 0, mProgressText.length(), mRect);
            Paint.FontMetrics fm = mBubblePaint.getFontMetrics();
            float baseline = mBubbleRadius + (fm.descent - fm.ascent) / 2f - fm.descent;
            canvas.drawText(mProgressText, getMeasuredWidth() / 2f, baseline, mBubblePaint);
        }

        void setProgressText(String progressText) {
            if (progressText != null && !mProgressText.equals(progressText)) {
                mProgressText = progressText;
                invalidate();
            }
        }
    }
}
