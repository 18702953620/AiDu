package com.ch.aidu.base;

import android.app.Application;

/**
 * 作者： ch
 * 时间： 2018/8/7 0007-上午 11:28
 * 描述：
 * 来源：
 */


public class MyApplication extends Application {
    private static MyApplication appContext;

    public static MyApplication getAppContext() {
        return appContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        appContext = this;
    }
}
