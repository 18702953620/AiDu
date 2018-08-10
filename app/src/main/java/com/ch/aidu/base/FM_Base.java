package com.ch.aidu.base;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * @author ch
 */
public abstract class FM_Base extends Fragment {

    public Context context;
    private ProgressDialog dialog;

    // 控件是否初始化完成
    protected boolean isViewCreated;

    // 当前fragment是否加载过数据,如加载过数据，则不再加载
    protected boolean isLoadCompleted;
    //是不是可见
    protected boolean isUIVisible;

    protected Unbinder unbinder;


    // 懒加载,强制子类重写
    protected abstract void loadData();

    protected abstract int getLayoutId();

    protected abstract void initView();

    protected abstract void addListener();


    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        isUIVisible = isVisibleToUser;
        if (isVisibleToUser && isViewCreated && isUIVisible && !isLoadCompleted) {
            isLoadCompleted = true;
            loadData();
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (isViewCreated && isUIVisible) {
            loadData();
            isLoadCompleted = true;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(getLayoutId(), container, false);
        unbinder = ButterKnife.bind(this, rootView);

        initView();
        addListener();
        isViewCreated = true;
        return rootView;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        isUIVisible = !hidden;
        isLoadCompleted = !hidden;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (unbinder != null) {
            unbinder.unbind();
        }
    }

    /**
     * 打开指定的activity
     *
     * @param cls
     */
    public void startA(@NonNull Class<?> cls) {
        Intent intent = new Intent(context, cls);
        startActivity(intent);
    }

    public void setStatusBar(View view) {
    }


    /**
     * toast
     *
     * @param msg
     */
    public void showtoast(@NonNull String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
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
}
