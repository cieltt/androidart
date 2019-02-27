package com.ryg.chapter_3.ui;

import com.nineoldandroids.view.ViewHelper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.TextView;

public class TestButton extends TextView {
    private static final String TAG = "TestButton";
    private int mScaledTouchSlop;
    // 分别记录上次滑动的坐标
    private int mLastRawX = 0;
    private int mLastRawY = 0;
    private int mLastX =0;
    private int mLastY=0;

    public TestButton(Context context) {
        this(context, null);
    }

    public TestButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TestButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        mScaledTouchSlop = ViewConfiguration.get(getContext())
                .getScaledTouchSlop();
        Log.d(TAG, "sts:" + mScaledTouchSlop);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x = (int) event.getRawX();
        int y = (int) event.getRawY();
        int x1= (int)event.getX();
        int y1= (int)event.getY();
        switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN: {
            break;
        }
        case MotionEvent.ACTION_MOVE: {
            int deltaRawX = x - mLastRawX;
            int deltaRawY = y - mLastRawY;
            int deltaX  = x1-mLastX;
            int deltaY  = y1-mLastY;
            Log.d(TAG, "move, RAWX:" + x + " lastRAWX:" + mLastRawX);
            Log.d(TAG, "move, X:" + x1 + " lastX:" + mLastX);
            Log.d(TAG, "move, RAWY:" +y + " lastRAWY:" + mLastRawY);
            Log.d(TAG, "move, Y" + y1 +" lastY:" + mLastY);
            Log.d(TAG, "move, deltaRAWX:" + deltaRawX + " deltaRAWY:" + deltaRawY);
            Log.d(TAG, "move, deltaX:" + deltaX + " deltaY:" + deltaY);
            int translationX = (int)ViewHelper.getTranslationX(this) + deltaRawX;
            int translationY = (int)ViewHelper.getTranslationY(this) + deltaRawY;
            ViewHelper.setTranslationX(this, translationX);
            ViewHelper.setTranslationY(this, translationY);
            break;
        }
        case MotionEvent.ACTION_UP: {
            break;
        }
        default:
            break;
        }

        mLastRawX = x;
        mLastRawY = y;
        mLastX = x1;
        mLastY = y1;
        return true;
    }

}
