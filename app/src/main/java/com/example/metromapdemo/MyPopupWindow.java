package com.example.metromapdemo;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.TextView;

public class MyPopupWindow extends PopupWindow {

    public MyPopupWindow(Context context, String text, String color, String line) {
        super(context);
        View view = LayoutInflater.from(context).inflate(R.layout.popup_my, null);
        TextView textView = view.findViewById(R.id.textView);
        textView.setText(text);
        TextView tvNumber = view.findViewById(R.id.tvNumber);
        tvNumber.setBackgroundColor(Color.parseColor(color));
        tvNumber.setText(line);
        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        setContentView(view);
        setWidth(view.getMeasuredWidth());
        setHeight(view.getMeasuredHeight());
        setOutsideTouchable(true);
        setBackgroundDrawable(new BitmapDrawable());
    }
}
