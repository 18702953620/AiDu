package com.ch.aidu.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 作者： ch
 * 时间： 2018/4/10 0010-上午 9:13
 * 描述：
 * 来源：
 */


public class BaseMedia implements Parcelable {


    protected String path;
    protected String id;
    protected String size;
    private int height;
    private int width;
    private boolean isSelected;

    protected BaseMedia(Parcel in) {
        path = in.readString();
        id = in.readString();
        size = in.readString();
        height = in.readInt();
        width = in.readInt();
        isSelected = in.readByte() != 0;
    }

    public BaseMedia(String path, String id, String size) {
        this.path = path;
        this.id = id;
        this.size = size;
    }

    public static final Creator<BaseMedia> CREATOR = new Creator<BaseMedia>() {
        @Override
        public BaseMedia createFromParcel(Parcel in) {
            return new BaseMedia(in);
        }

        @Override
        public BaseMedia[] newArray(int size) {
            return new BaseMedia[size];
        }
    };

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.path);
        dest.writeString(this.id);
        dest.writeString(this.size);
        dest.writeInt(this.height);
        dest.writeInt(this.width);
        dest.writeByte((byte) (this.isSelected ? 1 : 0));
    }
}
