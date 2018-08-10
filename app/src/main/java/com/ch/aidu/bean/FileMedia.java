package com.ch.aidu.bean;

import android.os.Parcel;

/**
 * 作者： ch
 * 时间： 2018/4/26 0026-下午 3:17
 * 描述：
 * 来源：
 */


public class FileMedia extends BaseMedia {
    private boolean isDir;
    private int count;
    private String name;
    private boolean isSelect;

    public static final int TYPE_UNKONE = 0;
    public static final int TYPE_TXT = 1;
    public static final int TYPE_PNG = 2;
    public static final int TYPE_JPG = 3;
    public static final int TYPE_ZIP = 4;
    public static final int TYPE_DOC = 5;
    public static final int TYPE_DOCX = 6;
    private int type;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public boolean isDir() {
        return isDir;
    }

    public void setDir(boolean dir) {
        isDir = dir;
    }

    protected FileMedia(Parcel in) {
        super(in);
    }

    public FileMedia(String path) {
        super(path, path, "0");
    }
}
