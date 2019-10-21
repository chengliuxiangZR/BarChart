package com.example.asus.barchartdemo;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class BarChartView extends View {
    private static final String TAG="BarChartView";

    private static int maxBarHei= Integer.MAX_VALUE;
    //保存点击柱状条的位置
    private int mSelectedIndex=-1;
    //柱状条每一次增长的长度
    private int BAR_GROW_STEP=15;
    //延时触发重新绘制时间
    private long DELAY=10;
    //是否还需绘制增长动画
    private boolean enableGrowAnimation=true;
    //文本画笔
    private Paint mAxisPaint;
    //柱状条颜色
    private Paint mBarPaint;
    //坐标文本占据的矩形边界
    private Rect mTextRect;
    //绘制柱状条的矩形
    private RectF mTemp;

    //坐标文本与柱状条之间间隔大小的变量
    private int mGap;

    //柱状条宽度的变量
    private float mBarsWidth;
    //可以设置mRadius为柱状条宽度的一半
    private int mRadius;
    //柱状条的集合
    private List<Bar> mBars=new ArrayList<Bar>();
    //柱状图数据列表
    private float[] mDataList;
    //水平方向x轴坐标
    private String[] mHorizontalAxis;
    //数据数组中的最大值,最大值用来计算绘制时的高度比例
    private int mMax;


    public BarChartView(Context context) {
        this(context,null);
//        super(context);
    }

    public BarChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    @Override
    protected void onDraw(Canvas canvas) {
//        super.onDraw(canvas);
        if(enableGrowAnimation){
            drawBarsWidthAnimation(canvas);
        }else {
            drawBars(canvas);
        }

    }
    private void init(){
        //设置文本画笔
        mAxisPaint=new Paint();
        mAxisPaint.setAntiAlias(true);
        mAxisPaint.setTextSize(20);
        mAxisPaint.setTextAlign(Paint.Align.CENTER);

        //设置柱状条画笔
        mBarPaint=new Paint();
        mBarPaint.setColor(Color.BLUE);
        mBarPaint.setAntiAlias(true);

        mTextRect=new Rect();
        mTemp=new RectF();
        //设置柱状条宽度 默认为8dp
        mBarsWidth= TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,8,getResources().getDisplayMetrics());
        //柱状条与坐标文本之间的间隔大小，默认为8sp
        mGap=(int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,8,getResources().getDisplayMetrics());
    }

    public void setmDataList(float[] mDataList,int max) {
        this.mDataList = mDataList;
        this.mMax=max;
    }

    public void setmHorizontalAxis(String[] mHorizontalAxis) {
        this.mHorizontalAxis = mHorizontalAxis;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        //清除柱状条Bar的集合
        mBars.clear();

        //去除padding,计算绘制所有柱状条所占的宽和高
        int width=w-getPaddingLeft()-getPaddingRight();
        int height=h-getPaddingTop()-getPaddingBottom();

        //按照数据集合的大小平分宽度
        int step=width/mDataList.length;
        //mBarWidth为柱状条宽度的变量，可以设置mRadius为柱状条宽度的一半
        mRadius=(int) (mBarsWidth/2);

        //计算第一条柱状条的左边位置
        int barLeft=getPaddingLeft()+step/2-mRadius;

        //通过坐标文本画笔计算绘制x轴第一个坐标文本占据的矩形边界，这里主要获取其高度，为计算maxBarHeight提供数据
        mAxisPaint.getTextBounds(mHorizontalAxis[0],0,mHorizontalAxis[0].length(),mTextRect);

        //通过柱状条高度的最大像素大小，mTextRect.height为底部x轴坐标文本的高度，mGap为坐标文本与柱状条之间间隔大小的变量
        int maxBarHeight=height-mTextRect.height()-mGap;
        //计算柱状条最大像素大小与最大数据值的比值
        float heightRadio=maxBarHeight/mMax;

        for(float data:mDataList){
            Bar bar=new Bar();
            //设置原始数据
            bar.value=data;
            //计算原始数据对应的像素高度大小
            bar.transformedValue=bar.value*heightRadio;

            //计算绘制柱状条的四个位置
            bar.left=barLeft;
            bar.top=(int) (getPaddingTop()+maxBarHeight-bar.transformedValue);
            if(bar.top<maxBarHei){
                maxBarHei=bar.top;
            }
//            Log.i(TAG,"top: "+String.valueOf(bar.top));
            bar.right=(int)(barLeft+mBarsWidth);
            bar.bottom=getPaddingTop()+maxBarHeight;

            //初始化绘制柱状条时当前的top值，用作动画
            bar.currentTop=bar.bottom;
            //将初始化好的bar添加到集合中
            mBars.add(bar);
            //更新柱状条左边位置，为初始化下一个bar对象做准备
            barLeft+=step;
        }
    }
    private void drawBars(Canvas canvas){
        //遍历所有的bar对象，一个个绘制
        for(int i=0;i<mBars.size();i++){
            Bar bar=mBars.get(i);
            //绘制底部x轴坐标文本
            String axis=mHorizontalAxis[i];//获取对应位置的坐标文本
            //计算绘制文本的起始位置(textX,textY),textX为柱状条的中线位置
            //由于我们对画笔mAxisPaint设置了Paint.Aligen.CENTER,
            // 所以绘制出来的文本的中线与柱状条的中线是重合的
            float textX=bar.left+mRadius;
            float textY=getHeight()-getPaddingBottom();
            //绘制坐标文本
            canvas.drawText(axis,textX,textY,mAxisPaint);

            if(i==mSelectedIndex){
                mBarPaint.setColor(Color.RED);
                float x=bar.left+mRadius;
                float y=bar.top-mGap;
                //绘制坐标文本
                canvas.drawText(String.valueOf(bar.value),x,y,mAxisPaint);
            }else {
                mBarPaint.setColor(Color.BLUE);
            }

            //设置柱状条颜色为蓝色
//            mBarPaint.setColor(Color.BLUE);
            //绘制柱状条矩形的四个位置
            mTemp.set(bar.left,bar.top,bar.right,bar.bottom);
            //绘制圆角矩形
            canvas.drawRoundRect(mTemp,mRadius,mRadius,mBarPaint);
            //绘制直角矩形
//            canvas.drawRect(mTemp,mBarPaint);
        }
    }
    private void drawBarsWidthAnimation(Canvas canvas){
        for(int i=0;i<mDataList.length;i++){
            Bar bar =mBars.get(i);
            //绘制底部x轴坐标文本
            String axis=mHorizontalAxis[i];//获取对应位置的坐标文本
            //计算绘制文本的起始位置(textX,textY),textX为柱状条的中线位置
            //由于我们对画笔mAxisPaint设置了Paint.Aligen.CENTER,
            // 所以绘制出来的文本的中线与柱状条的中线是重合的
            float textX=bar.left+mRadius;
            float textY=getHeight()-getPaddingBottom();
            //绘制坐标文本
            canvas.drawText(axis,textX,textY,mAxisPaint);
            //设置柱状条颜色为蓝色
//            mBarPaint.setColor(Color.BLUE);
            //更新当前柱状条顶部位置变量，BAR_GROW_STEP为柱状条增长的步长，
            //即让柱状条长高BAR_GROW_STEP长度
            bar.currentTop-=BAR_GROW_STEP;
            //当计算出来的currentTop小于柱状条本来的top值时，说明越界了
            if(bar.currentTop<=bar.top){
                //将currentTop重置成本来的top值，解决越界问题
                bar.currentTop=bar.top;
//                Log.i(TAG,String.valueOf(bar.currentTop));
                //当最高的一个柱状条绘制完成后，修改enableGrowAnimation的值，让点击事件可以被响应
                if(bar.currentTop==maxBarHei){
                    enableGrowAnimation=false;
                }
            }
            //绘制柱状条矩形的四个位置
            mTemp.set(bar.left,bar.currentTop,bar.right,bar.bottom);
            //绘制圆角矩形
            canvas.drawRoundRect(mTemp,mRadius,mRadius,mBarPaint);
            //绘制直角矩形
//            canvas.drawRect(mTemp,mBarPaint);
        }

        if(enableGrowAnimation){
            postInvalidateDelayed(DELAY);
//            enableGrowAnimation=false;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //如果还在增长动画，则不处理触摸事件
        if(enableGrowAnimation){
//            Log.i(TAG,"enable=true");
            return false;
        }
        switch (event.getActionMasked()){
            case MotionEvent.ACTION_DOWN:
                for(int i=0;i<mBars.size();i++){
                    if(mBars.get(i).isInside(event.getX(),event.getY())){
                        enableGrowAnimation=false;
                        //保存点击柱状条位置
                        mSelectedIndex=i;
                        invalidate();
                        break;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                //重置
                mSelectedIndex=-1;
                enableGrowAnimation=false;
                invalidate();
                break;
        }
//        Log.i(TAG,"enable=false");
        return true;
    }

    public void setmSelectedIndex(int mSelectedIndex) {
        this.mSelectedIndex = mSelectedIndex;
    }

    public void setEnableGrowAnimation(boolean enableGrowAnimation) {
        this.enableGrowAnimation = enableGrowAnimation;
    }
}
