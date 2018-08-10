package com.ch.aidu.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.view.WindowManager;

import com.ch.aidu.base.AC_Base;

import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * 作者： ch
 * 时间： 2018/8/7 0007-上午 11:52
 * 描述： 开屏页
 * 来源：
 */


public class AC_Splash extends AC_Base implements EasyPermissions.PermissionCallbacks {

    private static final int RC_CAMERA_AND_LOCATION = 1001;
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            startA(AC_Main.class);
            finish();
        }
    };

    @Override
    protected void setStatusBar() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    @Override
    protected int getLayoutId() {
        return 0;
    }

    @Override
    protected void initView() {
        requestPermision();
    }

    @Override
    protected void addListener() {

    }


    @AfterPermissionGranted(RC_CAMERA_AND_LOCATION)
    private void requestPermision() {
        String[] perms = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE,};
        if (EasyPermissions.hasPermissions(this, perms)) {
            // Already have permission, do the thing
            handler.sendEmptyMessageDelayed(0, 1500);
            // ...
        } else {
            // Do not have permissions, request them now
            EasyPermissions.requestPermissions(this, "",
                    RC_CAMERA_AND_LOCATION, perms);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        // Some permissions have been granted
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        // Some permissions have been denied
        finish();
    }
}
