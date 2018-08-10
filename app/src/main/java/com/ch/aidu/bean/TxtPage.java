package com.ch.aidu.bean;

import java.util.List;

/**
 * 作者： ch
 * 时间： 2018/8/7 0007-下午 4:02
 * 描述：
 * 来源：
 */


public class TxtPage {
    private int position;
    private String title;
    private int titleLines; //当前 lines 中为 title 的行数。
    private List<String> lines;

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getTitleLines() {
        return titleLines;
    }

    public void setTitleLines(int titleLines) {
        this.titleLines = titleLines;
    }

    public List<String> getLines() {
        return lines;
    }

    public void setLines(List<String> lines) {
        this.lines = lines;
    }
}
