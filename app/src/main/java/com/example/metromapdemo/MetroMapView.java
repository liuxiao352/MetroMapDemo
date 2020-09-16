package com.example.metromapdemo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import androidx.annotation.Nullable;

import com.shopgun.android.zoomlayout.ZoomLayout;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MetroMapView extends View {

    private float minScale;
    private int scaledTouchSlop;

    static class LineData {
        String color;
        int id;
        String name;

        public LineData(String color, int id, String name) {
            this.color = color;
            this.id = id;
            this.name = name;
        }
    }

    static class SectionsData {
        Path path;
        private int lineId;
        private int stationId;

        public SectionsData(Path path, int lineId, int stationId) {
            this.path = path;
            this.lineId = lineId;
            this.stationId = stationId;
        }
    }

    static class LinesData {
        int id;

        public LinesData(int id) {
            this.id = id;
        }
    }

    static class StationsData {
        int id;
        String name;
        double nameX;
        double nameY;
        double iconCenterX;
        double iconCenterY;
        double latitude;
        double longitude;
        List<LinesData> linesData;
        private RectF rectF;

        public StationsData(int id, String name, double nameX, double nameY, double iconCenterX, double iconCenterY,
                            double latitude, double longitude, List<LinesData> linesData, RectF rectF) {
            this.id = id;
            this.name = name;
            this.nameX = nameX;
            this.nameY = nameY;
            this.iconCenterX = iconCenterX;
            this.iconCenterY = iconCenterY;
            this.latitude = latitude;
            this.longitude = longitude;
            this.linesData = linesData;
            this.rectF = rectF;
        }

    }

    public static final String TAG = "MetroMapView";

    private List<LineData> lineDataList = new ArrayList<>();
    private List<SectionsData> sectionsDataList = new ArrayList<>();
    private List<StationsData> stationsDataList = new ArrayList<>();
    private Paint paint;
    private Paint railwayPaint;
    private Paint stationsPaint;
    private Paint txtPaint;
    private float mapWidth, mapHeight;

    public MetroMapView(Context context) {
        this(context, null);
    }

    public MetroMapView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(15);

        railwayPaint = new Paint();
        railwayPaint.setAntiAlias(true);
        railwayPaint.setStyle(Paint.Style.STROKE);
        railwayPaint.setStrokeWidth(10);
        railwayPaint.setColor(Color.WHITE);
        DashPathEffect effect = new DashPathEffect(new float[]{35, 35, 35, 35}, 0);
        railwayPaint.setPathEffect(effect);

        stationsPaint = new Paint();
        stationsPaint.setAntiAlias(true);

        txtPaint = new Paint();
        txtPaint.setAntiAlias(true);
        txtPaint.setTextSize(15);
        txtPaint.setColor(Color.parseColor("#333333"));

        parserMapJson();

        scaledTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    /**
     * 解析这一步应该放到线程里 因为是写着玩就放到主线程了
     */
    private void parserMapJson() {
        try {
            String mapJson = AssetUtils.getJson("map.json", getContext());
            JSONObject jsonObject = new JSONObject(mapJson);
            JSONObject commonBaseMapData = jsonObject.getJSONObject("common_base_map_data");
            JSONObject mapMeta = commonBaseMapData.optJSONObject("map_meta");
            if (mapMeta != null) {
                mapWidth = (float) mapMeta.optDouble("width");
                mapHeight = (float) mapMeta.optDouble("height");
            }

            JSONArray linesData = jsonObject.getJSONArray("lines_data");
            for (int i = 0; i < linesData.length(); i++) {
                JSONObject linesDataItem = linesData.optJSONObject(i);
                String color = linesDataItem.optString("color");
                int id = linesDataItem.optInt("id");
                String cn_name = linesDataItem.optString("cn_name");
                lineDataList.add(new LineData(color, id, cn_name));
            }

            JSONArray stationsData = jsonObject.getJSONArray("stations_data");
            for (int i = 0; i < stationsData.length(); i++) {
                JSONObject stationsDataItem = stationsData.optJSONObject(i);
                int id = stationsDataItem.optInt("id");
                String name = stationsDataItem.optString("cn_name");
                double nameX = stationsDataItem.optDouble("cn_name_x");
                double nameY = stationsDataItem.optDouble("cn_name_y");
                double iconCenterX = stationsDataItem.optDouble("icon_center_x");
                double iconCenterY = stationsDataItem.optDouble("icon_center_y");
                double latitude = stationsDataItem.optDouble("latitude");
                double longitude = stationsDataItem.optDouble("longitude");
                JSONArray lines = stationsDataItem.getJSONArray("lines");
                List<LinesData> linesList = new ArrayList<>();
                for (int j = 0; j < lines.length(); j++) {
                    JSONObject linesItem = lines.getJSONObject(j);
                    int lineId = linesItem.getInt("id");
                    linesList.add(new LinesData(lineId));
                }
                RectF rectF = new RectF((float) iconCenterX - 25f, (float) iconCenterY - 25f, (float) iconCenterX + 25f, (float) iconCenterY + 25f);
                stationsDataList.add(new StationsData(id, name, nameX, nameY, iconCenterX, iconCenterY, latitude, longitude, linesList, rectF));
            }

            JSONArray sectionsData = jsonObject.getJSONArray("sections_data");
            for (int i = 0; i < sectionsData.length(); i++) {
                JSONObject sectionsDataItem = sectionsData.getJSONObject(i);
                JSONArray commonMapPaths = sectionsDataItem.getJSONArray("common_map_paths");
                int lineId = sectionsDataItem.optInt("begin_line_id");
                int stationId = sectionsDataItem.optInt("begin_station_id");
                for (int j = 0; j < commonMapPaths.length(); j++) {
                    JSONObject commonMapPathsItem = commonMapPaths.getJSONObject(j);
                    float p1_x = (float) commonMapPathsItem.optDouble("p1_x");
                    float p1_y = (float) commonMapPathsItem.optDouble("p1_y");
                    float p2_x = (float) commonMapPathsItem.optDouble("p2_x");
                    float p2_y = (float) commonMapPathsItem.optDouble("p2_y");
                    float c1_x = (float) commonMapPathsItem.optDouble("c1_x");
                    float c1_y = (float) commonMapPathsItem.optDouble("c1_y");
                    float c2_x = (float) commonMapPathsItem.optDouble("c2_x");
                    float c2_y = (float) commonMapPathsItem.optDouble("c2_y");
                    Path path = new Path();
                    path.moveTo(p1_x, p1_y);
                    if (c1_x > 0) {
                        if (c2_x > 0) {
                            path.cubicTo(c1_x, c1_y, c2_x, c2_y, p2_x, p2_y);
                        } else {
                            path.quadTo(c1_x, c1_y, p2_x, p2_y);
                        }
                    } else {
                        if (p2_x > 0) {
                            path.lineTo(p2_x, p2_y);
                        }
                    }
                    sectionsDataList.add(new SectionsData(path, lineId, stationId));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        float scaleX = getWidth() / mapWidth;
        float scaleY = getHeight() / mapHeight;
        minScale = Math.min(scaleX, scaleY);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.scale(minScale, minScale);
        canvas.translate(0, (getHeight() / minScale) / 2f - mapHeight / 2f);

        stationsPaint.setStyle(Paint.Style.STROKE);
        for (int i = 0; i < sectionsDataList.size(); i++) {
            SectionsData sectionsData = sectionsDataList.get(i);
            Path path = sectionsData.path;
            String color = findLineDataColor(sectionsData.lineId);
            paint.setColor(Color.parseColor(color));
            canvas.drawPath(path, paint);
            if (railwayBgColor.equals(color)) {
                canvas.drawPath(path, railwayPaint);
            }
        }

        for (int i = 0; i < stationsDataList.size(); i++) {
            StationsData stationsData = stationsDataList.get(i);
            stationsPaint.setStyle(Paint.Style.STROKE);
            stationsPaint.setStrokeWidth(5);
            List<LinesData> stationsLinesData = stationsData.linesData;
            if (stationsLinesData.size() > 1) { //换乘站
                stationsPaint.setColor(Color.parseColor("#333333"));
                canvas.drawCircle((float) stationsData.iconCenterX, (float) stationsData.iconCenterY, 15, stationsPaint);
                stationsPaint.setStyle(Paint.Style.FILL);
                stationsPaint.setColor(Color.WHITE);
                canvas.drawCircle((float) stationsData.iconCenterX, (float) stationsData.iconCenterY, 12, stationsPaint);
            } else {
                stationsPaint.setStrokeWidth(3);
                LinesData linesData = stationsLinesData.get(0);
                stationsPaint.setColor(Color.parseColor(findLineDataColor(linesData.id)));
                canvas.drawCircle((float) stationsData.iconCenterX, (float) stationsData.iconCenterY, 10, stationsPaint);
                stationsPaint.setStyle(Paint.Style.FILL);
                stationsPaint.setColor(Color.WHITE);
                canvas.drawCircle((float) stationsData.iconCenterX, (float) stationsData.iconCenterY, 8, stationsPaint);
            }
            canvas.drawText(stationsData.name, (float) stationsData.nameX, (float) stationsData.nameY, txtPaint);
        }
    }

    private String railwayBgColor = "#cccccc";

    public String findLineDataColor(int id) {
        for (LineData lineDatum : lineDataList) {
            if (lineDatum.id == id) {
                if (!"000000".equals(lineDatum.color)) {
                    return "#" + lineDatum.color;
                }
            }
        }
        return railwayBgColor;
    }

    public String findLineDataNumber(int id) {
        for (LineData lineDatum : lineDataList) {
            if (lineDatum.id == id) {
                return lineDatum.name;
            }
        }
        return railwayBgColor;
    }

    public StationsData checkDown(ZoomLayout.TapInfo info){
        for (StationsData stationsData : stationsDataList) {
            float ty = (getHeight() / minScale) / 2f - mapHeight / 2f;
            float realX = info.getRelativeX() / minScale;
            float realY = info.getRelativeY() / minScale - ty;
            if (stationsData.rectF.contains(realX, realY)) {
                return stationsData;
            }
        }
        return null;
    }

}
