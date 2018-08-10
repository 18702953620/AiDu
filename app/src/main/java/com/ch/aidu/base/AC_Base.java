package com.ch.aidu.base;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.ch.aidu.R;
import com.ch.aidu.utils.StatusBarUtils;
import com.jaeger.library.StatusBarUtil;

import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * 基类 ch
 */
public abstract class AC_Base extends AppCompatActivity {
    public Context context;
    protected Unbinder unbinder;
    private ProgressDialog dialog;
    private View view;
    private ImageView iv_loading;

    protected abstract int getLayoutId();

    protected abstract void initView();

    protected abstract void addListener();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStatusBar();
        if (getLayoutId() != 0) {
            setContentView(getLayoutId());
        }
        unbinder = ButterKnife.bind(this);
        context = this;
        initView();
        addListener();
    }

    protected void setStatusBar() {
        //设置状态栏颜色;
        StatusBarUtil.setColorForSwipeBack(this,
                getResources().getColor(R.color.cffffff), 0);
////        //亮色
//        StatusBarUtils.StatusBarDarkMode(this, StatusBarUtils.StatusBarLightMode(this));
////        //暗色
        StatusBarUtils.StatusBarLightMode(this, StatusBarUtils.StatusBarLightMode(this));
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    public void addWindowView() {
        WindowManager windowManager = (WindowManager) MyApplication.getAppContext().getSystemService(Context.WINDOW_SERVICE);

        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
//悬浮窗参数设置
        layoutParams.format = PixelFormat.RGBA_8888;
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        layoutParams.gravity = Gravity.LEFT | Gravity.TOP;
        layoutParams.width = 200;//悬浮窗宽度
        layoutParams.height = 200;//悬浮窗高度
        layoutParams.x = 500;//悬浮窗位置
        layoutParams.y = 500;//悬浮窗位置

        if (Build.VERSION.SDK_INT > 24) {
            layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        } else {
            layoutParams.type = WindowManager.LayoutParams.TYPE_TOAST;
        }

//重点,必须设置此参数，用于窗口机制验证
        IBinder windowToken = getWindow().getDecorView().getWindowToken();
        layoutParams.token = windowToken;

        TextView textView = new TextView(context);

        textView.setBackgroundColor(Color.BLUE);
        WindowManager.LayoutParams textpParams = new WindowManager.LayoutParams(100, 100);
        textView.setLayoutParams(textpParams);

        windowManager.addView(textView, layoutParams);
    }

    @Override
    protected void onPause() {
        super.onPause();
//        if (windowManager != null && textView != null) {
//            windowManager.removeViewImmediate(textView);
//        }
    }

    /**
     * 打开Activity
     *
     * @param cls
     */
    public void startA(@NonNull Class<?> cls) {
        if (cls == null) {
            return;
        }

        Intent intent = new Intent(context, cls);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (unbinder != null) {
            unbinder.unbind();
        }

    }

    /**
     * @param s
     */
    public void showtoast(@NonNull String s) {
        Toast.makeText(context, s, Toast.LENGTH_SHORT).show();
    }

    /**
     * 显示加载动画
     */
    public void showLoadingDialog() {
        if (dialog != null && dialog.isShowing()) {
            return;
        }
        if (view == null) {
            view = getViewByRes(R.layout.dialog_loading);
        }
        if (iv_loading == null) {
            iv_loading = view.findViewById(R.id.iv_loading);
        }
        RequestOptions options = new RequestOptions()
                .centerInside()
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC);

        Glide.with(context).load(R.mipmap.num16).apply(options).into(iv_loading);
        if (dialog == null) {
            dialog = new ProgressDialog(context, R.style.dialog);
        }
        dialog.setCancelable(false);
        dialog.show();
        dialog.setContentView(view);

    }

    /**
     * 隐藏加载动画
     */
    public void closeLoadingDialog() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }


    /**
     * 通过资源res获得view
     *
     * @param res
     * @return
     */
    public View getViewByRes(@LayoutRes int res) {
        return LayoutInflater.from(context).inflate(res, null);
    }

    /**
     * 获得TextView 的文本
     *
     * @param tv
     * @return
     */
    public String getTV(TextView tv) {
        return tv == null ? "" : tv.getText().toString().trim();
    }


    /**
     * 取值
     *
     * @param key
     * @return
     */
    public String getValueByKey(String key) {
        SharedPreferences preferences = context.getSharedPreferences(AppConstant.APP_KEY_PRE, Context.MODE_PRIVATE);
        return preferences.getString(key, "");
    }

    /**
     * 存值
     *
     * @param key
     * @param value
     */
    public void putValueByKey(String key, String value) {
        SharedPreferences preferences = context.getSharedPreferences(AppConstant.APP_KEY_PRE, Context.MODE_PRIVATE);
        preferences.edit().putString(key, value).commit();
    }

    /**
     * 清空
     */
    public void clearAll() {
        SharedPreferences preferences = context.getSharedPreferences(AppConstant.APP_KEY_PRE, Context.MODE_PRIVATE);
        preferences.edit().clear().commit();
    }


}
