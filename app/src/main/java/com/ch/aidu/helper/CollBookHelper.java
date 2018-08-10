package com.ch.aidu.helper;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;

import com.ch.aidu.base.MyApplication;
import com.ch.aidu.bean.CollBookBean;
import com.ch.aidu.utils.ACache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 作者： ch
 * 时间： 2018/8/7 0007-下午 4:49
 * 描述：
 * 来源：
 */


public class CollBookHelper {
    private static final String COLL_KEY = "COLL_KEY";

    private static volatile CollBookHelper sInstance;

    public static CollBookHelper getsInstance() {
        if (sInstance == null) {
            synchronized (CollBookHelper.class) {
                if (sInstance == null) {
                    sInstance = new CollBookHelper();
                }
            }
        }
        return sInstance;
    }

    private HashMap<String, CollBookBean> getCollMap() {
        ACache cache = ACache.get(MyApplication.getAppContext());
        HashMap<String, CollBookBean> hashMap = (HashMap<String, CollBookBean>) cache.getAsObject(COLL_KEY);
        return hashMap == null ? new HashMap<String, CollBookBean>() : hashMap;
    }

    private void saveCollMap(HashMap<String, CollBookBean> hashMap) {
        ACache cache = ACache.get(MyApplication.getAppContext());
        cache.put(COLL_KEY, hashMap);
    }

    /**
     * 保存一本书籍 同步
     *
     * @param collBookBean
     */
    public void saveBook(CollBookBean collBookBean) {
        HashMap<String, CollBookBean> hashMap = getCollMap();

        if (!hashMap.containsValue(collBookBean)) {
            hashMap.put(collBookBean.get_id(), collBookBean);
        }
        saveCollMap(hashMap);
    }

    /**
     * 保存多本书籍 同步
     *
     * @param collBookBeans
     */
    public void saveBooks(List<CollBookBean> collBookBeans) {
        HashMap<String, CollBookBean> hashMap = getCollMap();
        for (CollBookBean bookBean : collBookBeans) {
            if (!hashMap.containsValue(bookBean)) {
                hashMap.put(bookBean.get_id(), bookBean);
            }
        }
        saveCollMap(hashMap);
    }

    /**
     * 删除书籍
     *
     * @param bookId
     */
    public void removeBookInRx(String bookId) {

        HashMap<String, CollBookBean> hashMap = getCollMap();
        if (hashMap.containsKey(bookId)) {
            hashMap.remove(bookId);
            saveCollMap(hashMap);
        }
    }

    /**
     * 删除所有书籍
     */
    public void removeAllBook() {
        saveCollMap(null);
    }

    /**
     * 查询一本书籍
     */
    public CollBookBean findBookById(String id) {
        if (TextUtils.isEmpty(id)) {
            return null;
        }

        HashMap<String, CollBookBean> hashMap = getCollMap();
        for (CollBookBean bookBean : hashMap.values()) {
            if (id.equals(bookBean.get_id())) {
                return bookBean;
            }
        }
        return null;
    }

    /**
     * 查询所有书籍
     */
    public List<CollBookBean> findAllBooks() {

        HashMap<String, CollBookBean> hashMap = getCollMap();

        List<CollBookBean> bookBeanList = new ArrayList<>();
        for (CollBookBean bookBean : hashMap.values()) {
            bookBeanList.add(bookBean);
        }
        return bookBeanList;
    }
}
