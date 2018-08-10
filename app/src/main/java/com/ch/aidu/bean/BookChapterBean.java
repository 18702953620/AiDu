package com.ch.aidu.bean;

import java.io.Serializable;

/**
 * 作者： ch
 * 时间： 2018/8/7 0007-下午 4:43
 * 描述：
 * 来源：
 */


public class BookChapterBean implements Serializable {

    //链接是唯一的
    private String link;

    private String title;

    //所属的下载任务
    private String taskName;
    //所属的书籍
    private String bookId;

    private boolean unreadble;

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public boolean isUnreadble() {
        return unreadble;
    }

    public void setUnreadble(boolean unreadble) {
        this.unreadble = unreadble;
    }
}
