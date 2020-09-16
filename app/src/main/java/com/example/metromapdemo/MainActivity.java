package com.example.metromapdemo;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.graphics.PointF;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;

import androidx.appcompat.app.AppCompatActivity;

import com.shopgun.android.zoomlayout.ZoomLayout;
import com.shopgun.android.zoomlayout.ZoomOnDoubleTapListener;

public class MainActivity extends AppCompatActivity {

    private ZoomLayout zoomLayout;
    private MetroMapView metroMapView;
    private ViewGroup topControl;

    private Animation topIn;
    private Animation topOut;

    private boolean isLocationScale = false;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        topIn = AnimationUtils.loadAnimation(this, R.anim.slide_top_in);
        topOut = AnimationUtils.loadAnimation(this, R.anim.slide_top_out);

        topControl = findViewById(R.id.topControl);
        zoomLayout = findViewById(R.id.zoomLayout);
        metroMapView = findViewById(R.id.metroMapView);

        zoomLayout.setMaxScale(6);
        zoomLayout.setMinScale(2);
        zoomLayout.post(new Runnable() {
            @Override
            public void run() {
                zoomLayout.setScale(2f,
                        zoomLayout.getWidth() / 2f,
                        zoomLayout.getHeight() / 2f,
                        false);
            }
        });
        zoomLayout.addOnZoomListener(new ZoomLayout.OnZoomListener() {
            @Override
            public void onZoomBegin(ZoomLayout view, float scale) {

            }

            @Override
            public void onZoom(ZoomLayout view, float scale) {

            }

            @Override
            public void onZoomEnd(ZoomLayout view, float scale) {
                if (!isLocationScale) {
                    topControlHide();
                } else {
                    isLocationScale = false;
                }
            }
        });
        zoomLayout.addOnTapListener(new ZoomLayout.OnTapListener() {
            @Override
            public boolean onTap(ZoomLayout view, ZoomLayout.TapInfo info) {
                MetroMapView.StationsData stationsData = metroMapView.checkDown(info);
                if (stationsData != null) {
                    topControlShow();
                    float scale = zoomLayout.getScale();
                    float x = info.getX();
                    float y = info.getY();
                    if (scale < 6) {
                        isLocationScale = true;
                        view.setScale(6, x, y, true);
                    }
                    float dx = x - zoomLayout.getWidth() / 2f;
                    float dy = y - zoomLayout.getHeight() / 2f;
                    startMoveCenter(view, dx, dy);
                    showPopupWindow(zoomLayout.getWidth() / 2f, zoomLayout.getHeight() / 2f, stationsData);
                } else {
                    toggleTopControl();
                }
                return true;
            }
        });
        zoomLayout.addOnDoubleTapListener(new ZoomOnDoubleTapListener(false));
    }

    private void showPopupWindow(float x, float y, MetroMapView.StationsData stationsData) {
        String color = metroMapView.findLineDataColor(stationsData.linesData.get(0).id);
        String name = metroMapView.findLineDataNumber(stationsData.linesData.get(0).id);
        MyPopupWindow popupWindow = new MyPopupWindow(MainActivity.this, stationsData.name, color, name);
        View contentView = findViewById(android.R.id.content);
        popupWindow.showAsDropDown(contentView, (int) x - popupWindow.getWidth() / 2,
                (int) y - 35, Gravity.TOP);
    }

    private void toggleTopControl() {
        if (topControl.getVisibility() == View.VISIBLE) {
            topControlHide();
        } else {
            topControlShow();
        }
    }

    private void topControlHide() {
        if (topControl.getVisibility() == View.GONE) {
            return;
        }
        //消失
        topControl.setVisibility(View.GONE);
        topControl.startAnimation(topOut);
    }

    private void topControlShow() {
        if (topControl.getVisibility() == View.VISIBLE) {
            return;
        }
        //消失
        topControl.setVisibility(View.VISIBLE);
        topControl.startAnimation(topIn);
    }

    private void startMoveCenter(final ZoomLayout view, final float dx, final float dy) {
        final float readDx = zoomLayout.getPosX() + dx;
        final float readDy = zoomLayout.getPosY() + dy;
        ValueAnimator valueAnimator = ValueAnimator.ofObject(new PointEvaluator(),
                new PointF(zoomLayout.getPosX(), zoomLayout.getPosY()),
                new PointF(readDx, readDy));
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                PointF pointF = (PointF) valueAnimator.getAnimatedValue();
                view.moveTo(pointF.x, pointF.y);
            }
        });
        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.setDuration(200L);
        valueAnimator.start();
    }

}