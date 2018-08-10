package com.ch.aidu.bean;

import java.io.Serializable;

/**
 * 作者： ch
 * 时间： 2018/8/7 0007-下午 4:27
 * 描述：
 * 来源：
 */


public class BookRecordBean implements Serializable {

    //所属的书的id
    private String bookId;
    //阅读到了第几章
    private int chapter;
    //当前的页码
    private int pagePos;

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public int getChapter() {
        return chapter;
    }

    public void setChapter(int chapter) {
        this.chapter = chapter;
    }

    public int getPagePos() {
        return pagePos;
    }

    public void setPagePos(int pagePos) {
        this.pagePos = pagePos;
    }
}
