package com.ch.aidu.helper;

import com.ch.aidu.base.MyApplication;
import com.ch.aidu.bean.BookRecordBean;
import com.ch.aidu.utils.ACache;

import java.util.HashMap;

/**
 * 作者： ch
 * 时间： 2018/8/7 0007-下午 4:42
 * 描述：
 * 来源：
 */


public class BookRecordHelper {
    private static final String RECORD_KEY = "RECORD_KEY";
    private static volatile BookRecordHelper sInstance;

    public static BookRecordHelper getsInstance() {
        if (sInstance == null) {
            synchronized (BookRecordHelper.class) {
                if (sInstance == null) {
                    sInstance = new BookRecordHelper();
                }
            }
        }
        return sInstance;
    }

    /**
     * 保存阅读记录
     */
    public void saveRecordBook(BookRecordBean collBookBean) {
        ACache cache = ACache.get(MyApplication.getAppContext());
        HashMap<String, BookRecordBean> hashMap = (HashMap<String, BookRecordBean>) cache.getAsObject(RECORD_KEY);

        if (hashMap == null) {
            hashMap = new HashMap<>();
        }
        hashMap.put(collBookBean.getBookId(), collBookBean);

        cache.put(RECORD_KEY, hashMap);

    }

    /**
     * 删除书籍记录
     */
    public void removeBook(String bookId) {
        ACache cache = ACache.get(MyApplication.getAppContext());
        HashMap<String, BookRecordBean> hashMap = (HashMap<String, BookRecordBean>) cache.getAsObject(RECORD_KEY);
        if (hashMap != null && hashMap.containsKey(bookId)) {
            hashMap.remove(bookId);
        }
        cache.put(RECORD_KEY, hashMap);

    }


    /**
     * 查询阅读记录
     */
    public BookRecordBean findBookRecordById(String bookId) {
        ACache cache = ACache.get(MyApplication.getAppContext());
        HashMap<String, BookRecordBean> hashMap = (HashMap<String, BookRecordBean>) cache.getAsObject(RECORD_KEY);
        if (hashMap != null) {
            return hashMap.get(bookId);
        }

        return null;
    }
}
