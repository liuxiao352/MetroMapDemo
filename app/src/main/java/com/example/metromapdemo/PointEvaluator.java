package com.example.metromapdemo;

import android.animation.TypeEvaluator;
import android.graphics.PointF;

public class PointEvaluator implements TypeEvaluator {

    @Override
    public Object evaluate(float fraction, Object startValue, Object endValue) {
        PointF startPoint = (PointF) startValue;
        PointF endPoint = (PointF) endValue;
        float x = startPoint.x + fraction * (endPoint.x - startPoint.x);
        float y = startPoint.y + fraction * (endPoint.y - startPoint.y);
        return new PointF(x, y);
    }

}