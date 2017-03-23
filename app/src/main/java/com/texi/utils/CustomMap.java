package com.texi.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;

/**
 * Created by techintegrity on 11/10/16.
 */
public class CustomMap extends com.google.android.gms.maps.MapView {

    RectF rectF = new RectF();

//    private int cornerRadiusXDP = (int) getResources().getDimension(R.dimen.height_80);
//    private int cornerRadiusYDP = (int) getResources().getDimension(R.dimen.height_80);
//
//    private int mapWidthDP = (int) getResources().getDimension(R.dimen.height_160);
//    private int mapHeightDP = (int) getResources().getDimension(R.dimen.height_160);

    private int cornerRadiusXDP = 100;
    private int cornerRadiusYDP = 100;

    private int mapWidthDP = 200;
    private int mapHeightDP = 200;

    private Context context;

    public CustomMap(Context context) {
        super(context);
        this.context = context;
    }

    public CustomMap(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public CustomMap(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        float mapWidth = convertDpToPixel(mapWidthDP, this.context);
        float mapHeigth = convertDpToPixel(mapHeightDP, this.context);

        rectF.set(0, 0, mapWidth, mapHeigth);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        Path path = new Path();
        int count = canvas.save();

        float cornerRadiusX = convertDpToPixel(cornerRadiusXDP, this.context);
        float cornerRadiusY = convertDpToPixel(cornerRadiusYDP, this.context);

        path.addRoundRect(rectF, cornerRadiusX, cornerRadiusY, Path.Direction.CW);

        canvas.clipPath(path);
        super.dispatchDraw(canvas);
        canvas.restoreToCount(count);
    }

    /**
     * Converts dp unit to equivalent pixels, depending on device density.
     *
     * @param dp A value in dp (density independent pixels) unit. Which we need to convert into pixels
     * @return A float value to represent px equivalent to dp depending on device density
     */
    public static float convertDpToPixel(float dp, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return dp * (metrics.densityDpi / 160f);
    }

}
