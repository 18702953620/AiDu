package com.ch.aidu.utils;

import android.text.TextUtils;


import com.ch.aidu.bean.Charset;
import com.ch.aidu.bean.CollBookBean;
import com.ch.aidu.bean.FileMedia;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 作者： ch
 * 时间： 2018/4/10 0010-上午 9:17
 * 描述：
 * 来源：
 */


public class FileUtils {

    /**
     * 返回指定文件夹下的文件
     *
     * @param dir
     * @return
     */
    public static List<FileMedia> listFiles(String dir) {
        if (TextUtils.isEmpty(dir)) {
            return null;
        }

        if (!isDir(dir)) {
            return null;
        }

        List<FileMedia> list = new ArrayList<>();
        File[] files = new File(dir).listFiles();
        if (files != null && files.length != 0) {
            for (File f : files) {
                FileMedia media = new FileMedia(f.getPath());
                if (isDir(f)) {
                    media.setDir(true);
                    media.setSize("0");
                    media.setCount(getDirCount(f));
                } else {
                    media.setDir(false);
                    media.setSize(getFileLength(f) + "");
                    media.setCount(0);
                }
                media.setName(f.getName());
                media.setType(getFileType(f.getName()));
                list.add(media);
            }
        }
        return list;
    }

    /**
     * 返回指定文件夹下的文件
     *
     * @param dir
     * @return
     */
    public static void listFiles(String dir, List<FileMedia> mediaList) {
        if (TextUtils.isEmpty(dir)) {
            return;
        }

        if (!isDir(dir)) {
            return;
        }
        if (mediaList == null) {
            mediaList = new ArrayList<>();
        }

        File[] files = new File(dir).listFiles();
        if (files != null && files.length != 0) {
            for (File f : files) {
                if (isDir(f)) {
                    listFiles(f.getPath(), mediaList);
                } else {
                    long size = getFileLength(f);
                    if (size > 1024 * 40 && getFileType(f.getName()) == FileMedia.TYPE_TXT) {
                        FileMedia media = new FileMedia(f.getPath());
                        media.setDir(false);
                        media.setSize(getFileLength(f) + "");
                        media.setCount(0);
                        media.setName(f.getName());
                        media.setType(getFileType(f.getName()));
                        mediaList.add(media);
                    }
                }

            }
        }
    }

    /**
     * 返回指定文件夹下的文件
     *
     * @param dir
     * @return
     */
    public static List<FileMedia> listFiles(String dir, int fileType) {
        if (TextUtils.isEmpty(dir)) {
            return null;
        }

        if (!isDir(dir)) {
            return null;
        }

        List<FileMedia> list = new ArrayList<>();
        File[] files = new File(dir).listFiles();
        if (files != null && files.length != 0) {
            for (File f : files) {
                FileMedia media = new FileMedia(f.getPath());
                if (isDir(f)) {
                    media.setDir(true);
                    media.setSize("0");
                    media.setCount(getDirCount(f));
                    media.setName(f.getName());
                    list.add(media);
                } else {
                    media.setDir(false);
                    if (getFileType(f.getName()) == fileType) {
                        media.setName(f.getName());
                        media.setType(fileType);
                        media.setSize(getFileLength(f) + "");
                        list.add(media);
                    }
                }

            }
        }
        return list;
    }

    /**
     * 返回指定文件夹下的文件
     *
     * @param dir
     * @return
     */
    public static List<FileMedia> listFiles(String dir, boolean isShowFile) {
        if (TextUtils.isEmpty(dir)) {
            return null;
        }
        if (!isDir(dir)) {
            return null;
        }

        List<FileMedia> list = new ArrayList<>();
        File[] files = new File(dir).listFiles();
        if (files != null && files.length != 0) {
            for (File f : files) {
                FileMedia media = new FileMedia(f.getPath());
                if (isShowFile) {

                    if (isDir(f)) {
                        media.setDir(true);
                        media.setSize("0");
                        media.setCount(getDirCount(f));
                    } else {
                        media.setDir(false);
                        media.setSize(getFileLength(f) + "");
                        media.setCount(0);
                    }
                    media.setName(f.getName());
                    media.setType(getFileType(f.getName()));
                    list.add(media);
                } else {
                    if (isDir(f)) {
                        media.setDir(true);
                        media.setSize("0");
                        media.setCount(getDirCount(f));
                        media.setName(f.getName());
                        media.setType(getFileType(f.getName()));
                        list.add(media);

                    }

                }
            }
        }
        return list;
    }

    /**
     * 是否是文件夹
     *
     * @param path
     * @return
     */
    public static boolean isDir(String path) {
        return isDir(new File(path));

    }

    /**
     * 是否是文件夹
     *
     * @param file
     * @return
     */
    public static boolean isDir(File file) {
        return file == null ? false : file.exists() && file.isDirectory();
    }

    /**
     * 获得文件夹内文件的个数
     *
     * @param f
     * @return
     */
    public static int getDirCount(File f) {
        if (f == null || !f.exists()) {
            return 0;
        }

        if (isDir(f)) {
            return f.listFiles() == null ? 0 : f.listFiles().length;

        } else {
            return 0;
        }
    }

    /**
     * 获得文件夹的大小
     *
     * @param dir
     * @return
     */
    public static long getDirLength(final File dir) {
        if (!isDir(dir)) {
            return getFileLength(dir);
        }
        long len = 0;
        File[] files = dir.listFiles();
        if (files != null && files.length != 0) {
            for (File file : files) {
                if (file.isDirectory()) {
                    len += getDirLength(file);
                } else {
                    len += file.length();
                }
            }
        }
        return len;
    }

    /**
     * 获得文件的大小
     *
     * @param path
     * @return
     */
    public static long getFileLength(String path) {

        return getFileLength(new File(path));
    }

    /**
     * 获得文件的大小
     *
     * @param f
     * @return
     */
    public static long getFileLength(File f) {
        if (f == null || !f.exists()) {

            return 0;
        }
        return f.length();

    }

    public static int getFileType(String name) {
        if (TextUtils.isEmpty(name)) {
            return FileMedia.TYPE_UNKONE;
        }

        if (name.endsWith(".txt") || name.endsWith(".TXT")) {
            return FileMedia.TYPE_TXT;
        } else if (name.endsWith(".png") || name.endsWith(".PNG")) {
            return FileMedia.TYPE_PNG;
        } else if (name.endsWith(".jpg") || name.endsWith(".JPG")) {
            return FileMedia.TYPE_JPG;
        } else if (name.endsWith(".doc") || name.endsWith(".DOC")) {
            return FileMedia.TYPE_DOC;
        }

        return FileMedia.TYPE_UNKONE;
    }

    /**
     * 删除文件
     *
     * @param path
     * @return
     */
    public static boolean delete(String path) {
        if (TextUtils.isEmpty(path)) {
            return false;
        }
        return delete(new File(path));
    }

    public static boolean delete(File f) {
        if (f == null || !f.exists()) {
            return false;
        }
        if (isDir(f)) {

            File[] files = f.listFiles();
            if (files == null || files.length == 0) {
                return f.delete();
            }
            for (File fe : files) {
                fe.delete();
            }
            return f.delete();

        } else {
            return f.delete();
        }
    }

    public static String getFormatSize(String size) {
        if (TextUtils.isEmpty(size)) {
            return "0";
        }
        long msize = Long.parseLong(size);
        if (msize > 1024) {
            if (msize < 1024) {
                return msize + "B";
            } else if (msize > 1024 && msize < 1024 * 1024) {
                return msize / 1024 + "KB";
            } else if (msize > 1024 * 1024 && msize < 1024 * 1024 * 1024) {
                return msize / 1024 / 1024 + "MB";
            } else if (msize > 1024 * 1024 * 1024) {
                return msize / 1024 / 1024 / 1024 + "GB";
            } else {

            }
        }

        return "0";
    }


    //获取文件的编码格式
    public static Charset getCharset(String fileName) {
        BufferedInputStream bis = null;
        Charset charset = Charset.GBK;
        byte[] first3Bytes = new byte[3];
        try {
            bis = new BufferedInputStream(new FileInputStream(fileName));
            bis.mark(0);
            int read = bis.read(first3Bytes, 0, 3);
            if (read == -1)
                return charset;
            if (first3Bytes[0] == (byte) 0xEF
                    && first3Bytes[1] == (byte) 0xBB
                    && first3Bytes[2] == (byte) 0xBF) {
                charset = Charset.UTF8;
            } else if (first3Bytes[0] == (byte) 0xFF && first3Bytes[1] == (byte) 0xFE) {
                charset = Charset.UTF16LE;
            } else if (first3Bytes[0] == (byte) 0xFE
                    && first3Bytes[1] == (byte) 0xFF) {
                charset = Charset.UTF16BE;
            } else {
                bis.mark(0);
                while ((read = bis.read()) != -1) {
                    if (read >= 0xF0)
                        break;
                    if (0x80 <= read && read <= 0xBF) // 单独出现BF以下的，也算是GBK
                        break;
                    if (0xC0 <= read && read <= 0xDF) {
                        read = bis.read();
                        if (0x80 <= read && read <= 0xBF) // 双字节 (0xC0 - 0xDF)
                            // (0x80 - 0xBF),也可能在GB编码内
                            continue;
                        else
                            break;
                    } else if (0xE0 <= read && read <= 0xEF) {// 也有可能出错，但是几率较小
                        read = bis.read();
                        if (0x80 <= read && read <= 0xBF) {
                            read = bis.read();
                            if (0x80 <= read && read <= 0xBF) {
                                charset = Charset.UTF8;
                                break;
                            } else
                                break;
                        } else
                            break;
                    }
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
        return charset;
    }

    /**
     * 将文件转换成CollBook
     *
     * @param files:需要加载的文件列表
     * @return
     */
    public static List<CollBookBean> convertCollBook(List<File> files) {
        List<CollBookBean> collBooks = new ArrayList<>(files.size());
        for (File file : files) {
            //判断文件是否存在
            if (!file.exists()) continue;

            CollBookBean collBook = new CollBookBean();
            collBook.setLocal(true);
            collBook.set_id(file.getAbsolutePath());
            collBook.setTitle(file.getName().replace(".txt", ""));
            collBook.setLastChapter("开始阅读");
            collBook.setLastRead(DataUtils.
                    dateConvert(System.currentTimeMillis(), "yyyy-MM-dd'T'HH:mm:ss"));
            collBooks.add(collBook);
        }
        return collBooks;
    }


}
