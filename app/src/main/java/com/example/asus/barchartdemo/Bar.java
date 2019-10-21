package com.example.asus.barchartdemo;

//柱状条对象Bar
public class Bar {
    //绘制柱状条的四个位置
    int left;
    //与页面顶部的距离，越小，柱状条越高
    int top;
    int right;
    int bottom;
    //柱状条原始数据的大小
    float value;
    //柱状条原始数据大小转换成对应的像素大小
    float transformedValue;
    //当前的top值
    int currentTop;
    //检查点(x,y)是否在柱状条内部
    boolean isInside(float x,float y){
        return x>left&&x<right&&y>top&&y<bottom;
    }
}
