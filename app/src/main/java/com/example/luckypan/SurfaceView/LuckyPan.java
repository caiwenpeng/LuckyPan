package com.example.luckypan.SurfaceView;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;

import com.example.luckypan.R;

import java.sql.Time;

/**
 * Created by 蔡大爷 on 2016/8/21.
 */
public class LuckyPan extends SurfaceView implements SurfaceHolder.Callback, Runnable {

    private SurfaceHolder mHolder;
    private Canvas mCanvas;
    private boolean mIsDrawing;


    private Paint backgroundPaint;
    private Paint textPaint;
    private Paint arcPaint;

    int padding = 40;
    int radius = 300;
    int startAngle = 0;
    static int speed = 0;
    RectF recf;
    static boolean isStopLuckyPan = true;

    static boolean isStartAngle = false;

    int[] ArcColor = {Color.BLUE, Color.GREEN, Color.BLUE, Color.GREEN, Color.BLUE, Color.GREEN};
    static int[] Angle = {240, 180, 120, 60, 360, 300};
    String[] TextContent = {"一等奖", "二等奖", "谢谢参与", "三等奖", "优胜奖", "谢谢参与"};


    public LuckyPan(Context context) {
        this(context, null);
        initView();
    }

    public LuckyPan(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public LuckyPan(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }


    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {

        mIsDrawing = true;
        new Thread(this).start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

        mIsDrawing = false;
    }


    @Override
    public void run() {

        while (mIsDrawing) {

            long startTime = System.currentTimeMillis();
            draw();
            long endTime = System.currentTimeMillis();

            if (endTime - startTime < 100) {
                try {
                    Thread.sleep(100 - (endTime - startTime));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void draw() {

        try {

            mCanvas = mHolder.lockCanvas();

            drawBackground();
            drawArc();


        } catch (Exception e) {


        } finally {

            if (mCanvas != null) {
                mHolder.unlockCanvasAndPost(mCanvas);
            }
        }
    }

    private void drawBitmap(float angle) {

        int imgWidth = radius / 8;
        //取一半
        float arc = (float) (angle * Math.PI / 180);

        int y = (int) (Math.sin(arc) * radius / 2 + getHeight() / 2);
        int x = (int) (Math.cos(arc) * radius / 2 + getWidth() / 2);

        Rect rect = new Rect(x - imgWidth, y - imgWidth, x + imgWidth, y + imgWidth);
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        mCanvas.drawBitmap(bitmap, null, rect, new Paint());

    }


    private void drawBackground() {

        mCanvas.drawColor(Color.WHITE);
        mCanvas.drawCircle(getWidth() / 2, getHeight() / 2, radius, backgroundPaint);
    }

    private void drawPathText(int startAngle, int sweepAngle, int pos) {

        float hOffset = (float) Math.PI * radius * 60 / 180 / 2 - textPaint.measureText(TextContent[pos]) / 2 - 20;
        float vOffset = padding;
        Path path = new Path();
        path.arcTo(recf, startAngle, sweepAngle);

        mCanvas.drawTextOnPath(TextContent[pos], path, hOffset, vOffset, textPaint);
    }

    private void drawArc() {

        int sweepAngle = 60;

        /*
        **      isStopLuckyPan 是否需要停止转盘
        */
        if (isStopLuckyPan) {
            if (speed > 0) {
                speed--;
            } else {
                isStopLuckyPan = false;
            }
        }
        if (isStartAngle) {
            startAngle = 0;
            isStartAngle = false;
        }

//        if (!isStopLuckyPan) {
//            if (speed <50) {
//                speed++;
//            }
//        }


        recf = new RectF(getWidth() / 2 - radius + padding, getHeight() / 2 - radius + padding, getWidth() / 2 + radius - padding, getHeight() / 2 + radius - padding);

        for (int i = 0; i < 6; i++) {

            startAngle = startAngle + 60;
//            Log.e("aaaa", "startAngle: " + startAngle);
            arcPaint.setColor(ArcColor[i]);
            mCanvas.drawArc(recf, startAngle, sweepAngle, true, arcPaint);

            drawPathText(startAngle, sweepAngle, i);
            drawBitmap(startAngle + sweepAngle / 2);
        }
        startAngle += speed;


    }

    private void initView() {

        mHolder = getHolder();
        mHolder.addCallback(this);
        setFocusable(true);
        setFocusableInTouchMode(true);
        this.setKeepScreenOn(true);

        initPaint();

    }

    public void initPaint() {
        backgroundPaint = new Paint();
        backgroundPaint.setAntiAlias(true);
        backgroundPaint.setColor(Color.RED);

        arcPaint = new Paint();
        arcPaint.setAntiAlias(true);
        arcPaint.setColor(Color.GREEN);

        textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 16, getResources().getDisplayMetrics()));
        textPaint.setColor(Color.WHITE);

    }

    /*
    **  正常开启
     */
    public static void startLuckyPan() {

        speed = 50;
        isStopLuckyPan = false;
    }

    /*
    ** 作弊开启
     */


    public static void stopLuckyPan(int dex) {

        /*
        ** index :  0   所在的盘区域
        * angle1 :  210  到达目标所需要的最小角度
        * angle2 :  270  到达目标所需要的最大角度
        *  v1    : v1 = -1 +- sqr(1-4(-2dy))/2(-2dy)   dy=210  最小speed
        *  v2    : v2 = -1 +- sqr(1-4(-2dy))/2(-2dy)   dy=270  最大speed
         */


        int angle = 60;
        int angleCanch = 360;

        int dAngle1 = Angle[dex];
        int dAngle2 = Angle[dex] + angle;

        int v0 = (int) (-1 + Math.sqrt(1 + 8 * angleCanch)) / 2;
        int v1 = (int) (-1 + Math.sqrt(1 + 8 * dAngle1)) / 2 + 1;
        int v2 = (int) (-1 + Math.sqrt(1 + 8 * dAngle2)) / 2 - 1;

        speed = (int) (v1 + Math.random() * (v2 - v1+2)) ;


        Log.e("aaaa", "speed: " + speed);
        isStartAngle = true;
        isStopLuckyPan = true;
    }

}
