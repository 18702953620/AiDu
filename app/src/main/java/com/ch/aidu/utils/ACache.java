
package com.ch.aidu.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.util.ArrayMap;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 缓存类  缓存路径/data/data/app-package-name/cache/ACache
 *
 */
public class ACache {
	public static final int TIME_HOUR =60*60;//缓存一个小时
	public static final int TIME_DAY = TIME_HOUR * 24;//一天
	private static final int MAX_SIZE = 1000 * 1000 * 50; // 50 mb
	private static final int MAX_COUNT = Integer.MAX_VALUE; // 不限制存放数据的数量
	private static Map<String, ACache> mInstanceMap = new ArrayMap<String, ACache>();
	private ACacheManager mCache;
	//实例化应用程序场景缓存
	public static ACache get(Context ctx) {
		return get(ctx, "ACache");
	}
	//新建缓存目录
	public static ACache get(Context ctx, String cacheName) {
		//新建文件夹，文件路径为应用场景缓存路径目录，文件夹名为ACache(new File()也可新建文件，带上后缀即可)
		File f = new File(ctx.getCacheDir(), cacheName);
		return get(f, MAX_SIZE, MAX_COUNT);
	}

	public static ACache get(File cacheDir) {
		return get(cacheDir, MAX_SIZE, MAX_COUNT);
	}

	public static ACache get(Context ctx, long max_zise, int max_count) {
		File f = new File(ctx.getCacheDir(), "ACache");
		return get(f, max_zise, max_count);
	}
	//新建缓存实例，存入实例map，key为缓存目录+每次应用开启的进程id
	public static ACache get(File cacheDir, long max_zise, int max_count) {
		//返回key为缓存目录+每次应用开启的进程id的map的value值，赋给缓存实例manager
		ACache manager = mInstanceMap.get(cacheDir.getAbsoluteFile() + myPid());
		if (manager == null) {
			manager = new ACache(cacheDir, max_zise, max_count);
			mInstanceMap.put(cacheDir.getAbsolutePath() + myPid(), manager);
		}
		return manager;
	}

	private static String myPid() {
		return "_" + android.os.Process.myPid();
	}
	//ACache构造函数 为private私有（所以在其他类里获得实例只能通过get()方法）
	private ACache(File cacheDir, long max_size, int max_count) {
		if (!cacheDir.exists() && !cacheDir.mkdirs()) {
			throw new RuntimeException("can't make dirs in " + cacheDir.getAbsolutePath());
		}
		mCache = new ACacheManager(cacheDir, max_size, max_count);
	}


	class xFileOutputStream extends FileOutputStream {
		File file;

		public xFileOutputStream(File file) throws FileNotFoundException {
			super(file);
			this.file = file;
		}

		public void close() throws IOException {
			super.close();
			mCache.put(file);
		}
	}

	// =======================================
	// ============ String数据 读写 ==============
	// =======================================
	/**
	 * 保存 String数据 到 缓存中
	 * 
	 * @param key
	 *            保存的key
	 * @param value
	 *            保存的String数据
	 */
	public void put(String key, String value) {
		File file = mCache.newFile(key);//新建文件
		BufferedWriter out = null;//缓冲字符输出流,作用是为其他字符输出流添加一些缓冲功能
		try {
			out = new BufferedWriter(new FileWriter(file), 1024);//获取file字符流
			out.write(value);//  把value写入
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (out != null) {
				try {
					out.flush();
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			mCache.put(file); //更新cacheCount和cacheSize  lastUsageDates插入新建文件和时间的键值对
		}
	}

	/**
	 * 保存 String数据 到 缓存中
	 * 
	 * @param key
	 *            保存的key
	 * @param value
	 *            保存的String数据
	 * @param saveTime
	 *            保存的时间，单位：秒
	 */
	public void put(String key, String value, int saveTime) {
		put(key, Utils.newStringWithDateInfo(saveTime, value));
	}

	/**
	 * 读取 String数据
	 * 
	 * @param key
	 * @return String 数据
	 */
	public String getAsString(String key) {
		File file = mCache.get(key);//获取文件
		if (!file.exists())
			return null;
		boolean removeFile = false;
		BufferedReader in = null;
		try {
			in = new BufferedReader(new FileReader(file));
			String readString = "";
			String currentLine;
			while ((currentLine = in.readLine()) != null) {//逐行遍历
				readString += currentLine;//每行字符串连接
			}
			if (!Utils.isDue(readString)) {//String数据未到期
				return Utils.clearDateInfo(readString);//去除时间信息的字符串内容
			} else {
				removeFile = true;//移除文件标志位为真
				return null;
			}
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (removeFile)
				remove(key);//移除缓存
		}
	}

	// =======================================
	// ============= JSONObject 数据 读写 ==============
	// =======================================
	/**
	 * 保存 JSONObject数据 到 缓存中
	 * 
	 * @param key
	 *            保存的key
	 * @param value
	 *            保存的JSON数据
	 */
	public void put(String key, JSONObject value) {
		put(key, value.toString());
	}

	/**
	 * 保存 JSONObject数据 到 缓存中
	 * 
	 * @param key
	 *            保存的key
	 * @param value
	 *            保存的JSONObject数据
	 * @param saveTime
	 *            保存的时间，单位：秒
	 */
	public void put(String key, JSONObject value, int saveTime) {
		put(key, value.toString(), saveTime);
	}

	/**
	 * 读取JSONObject数据
	 * 
	 * @param key
	 * @return JSONObject数据
	 */
	public JSONObject getAsJSONObject(String key) {
		String JSONString = getAsString(key);
		try {
			JSONObject obj = new JSONObject(JSONString);
			return obj;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	// =======================================
	// ============ JSONArray 数据 读写 =============
	// =======================================
	/**
	 * 保存 JSONArray数据 到 缓存中
	 * 
	 * @param key
	 *            保存的key
	 * @param value
	 *            保存的JSONArray数据
	 */
	public void put(String key, JSONArray value) {
		put(key, value.toString());
	}

	/**
	 * 保存 JSONArray数据 到 缓存中
	 * 
	 * @param key
	 *            保存的key
	 * @param value
	 *            保存的JSONArray数据
	 * @param saveTime
	 *            保存的时间，单位：秒
	 */
	public void put(String key, JSONArray value, int saveTime) {
		put(key, value.toString(), saveTime);
	}

	/**
	 * 读取JSONArray数据
	 * 
	 * @param key
	 * @return JSONArray数据
	 */
	public JSONArray getAsJSONArray(String key) {
		String JSONString = getAsString(key);
		try {
			JSONArray obj = new JSONArray(JSONString);
			return obj;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	// =======================================
	// ============== byte 数据 读写 =============
	// =======================================
	/**
	 * 保存 byte数据 到 缓存中
	 * 
	 * @param key
	 *            保存的key
	 * @param value
	 *            保存的数据
	 */
	public void put(String key, byte[] value) {
		File file = mCache.newFile(key);
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(file);
			out.write(value);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (out != null) {
				try {
					out.flush();
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			mCache.put(file);
		}
	}

	/**
	 * Cache for a stream
	 * 
	 * @param key
	 *            the file name.
	 * @return OutputStream stream for writing data.
	 * @throws FileNotFoundException
	 *             if the file can not be created.
	 */
	public OutputStream put(String key) throws FileNotFoundException {
		return new xFileOutputStream(mCache.newFile(key));
	}

	/**
	 * 
	 * @param key
	 *            the file name.
	 * @return (InputStream or null) stream previously saved in cache.
	 * @throws FileNotFoundException
	 *             if the file can not be opened
	 */
	public InputStream get(String key) throws FileNotFoundException {
		File file = mCache.get(key);
		if (!file.exists())
			return null;
		return new FileInputStream(file);
	}

	/**
	 * 保存 byte数据 到 缓存中
	 * 
	 * @param key
	 *            保存的key
	 * @param value
	 *            保存的数据
	 * @param saveTime
	 *            保存的时间，单位：秒
	 */
	public void put(String key, byte[] value, int saveTime) {
		put(key, Utils.newByteArrayWithDateInfo(saveTime, value));
	}

	/**
	 * 获取 byte 数据
	 * 
	 * @param key
	 * @return byte 数据
	 */
	public byte[] getAsBinary(String key) {
		RandomAccessFile RAFile = null;
		boolean removeFile = false;
		try {
			File file = mCache.get(key);
			if (!file.exists())
				return null;
			RAFile = new RandomAccessFile(file, "r");
			byte[] byteArray = new byte[(int) RAFile.length()];
			RAFile.read(byteArray);
			if (!Utils.isDue(byteArray)) {
				return Utils.clearDateInfo(byteArray);
			} else {
				removeFile = true;
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			if (RAFile != null) {
				try {
					RAFile.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (removeFile)
				remove(key);
		}
	}

	// =======================================
	// ============= 序列化 数据 读写 ===============
	// =======================================
	/**
	 * 保存 Serializable数据 到 缓存中
	 * 
	 * @param key
	 *            保存的key
	 * @param value
	 *            保存的value
	 */
	public void put(String key, Serializable value) {
		put(key, value, -1);
	}

	/**
	 * 保存 Serializable数据到 缓存中
	 * 
	 * @param key
	 *            保存的key
	 * @param value
	 *            保存的value
	 * @param saveTime
	 *            保存的时间，单位：秒
	 */
	public void put(String key, Serializable value, int saveTime) {
		ByteArrayOutputStream baos = null;
		ObjectOutputStream oos = null;
		try {
			baos = new ByteArrayOutputStream();
			oos = new ObjectOutputStream(baos);
			oos.writeObject(value);
			byte[] data = baos.toByteArray();
			if (saveTime != -1) {
				put(key, data, saveTime);
			} else {
				put(key, data);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				oos.close();
			} catch (IOException e) {
			}
		}
	}

	/**
	 * 读取 Serializable数据
	 * 
	 * @param key
	 * @return Serializable 数据
	 */
	public Object getAsObject(String key) {
		byte[] data = getAsBinary(key);
		if (data != null) {
			ByteArrayInputStream bais = null;
			ObjectInputStream ois = null;
			try {
				bais = new ByteArrayInputStream(data);
				ois = new ObjectInputStream(bais);
				Object reObject = ois.readObject();
				return reObject;
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			} finally {
				try {
					if (bais != null)
						bais.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				try {
					if (ois != null)
						ois.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return null;

	}


	// =======================================
	// ============== bitmap 数据 读写 =============
	// =======================================
	/**
	 * 保存 bitmap 到 缓存中
	 * 
	 * @param key
	 *            保存的key
	 * @param value
	 *            保存的bitmap数据
	 */
	public void put(String key, Bitmap value) {
		put(key, Utils.Bitmap2Bytes(value));
	}

	/**
	 * 保存 bitmap 到 缓存中
	 * 
	 * @param key
	 *            保存的key
	 * @param value
	 *            保存的 bitmap 数据
	 * @param saveTime
	 *            保存的时间，单位：秒
	 */
	public void put(String key, Bitmap value, int saveTime) {
		put(key, Utils.Bitmap2Bytes(value), saveTime);
	}

	/**
	 * 读取 bitmap 数据
	 * 
	 * @param key
	 * @return bitmap 数据
	 */
	public Bitmap getAsBitmap(String key) {
		if (getAsBinary(key) == null) {
			return null;
		}
		return Utils.Bytes2Bimap(getAsBinary(key));
	}

	// =======================================
	// ============= drawable 数据 读写 =============
	// =======================================
	/**
	 * 保存 drawable 到 缓存中
	 * 
	 * @param key
	 *            保存的key
	 * @param value
	 *            保存的drawable数据
	 */
	public void put(String key, Drawable value) {
		put(key, Utils.drawable2Bitmap(value));
	}

	/**
	 * 保存 drawable 到 缓存中
	 * 
	 * @param key
	 *            保存的key
	 * @param value
	 *            保存的 drawable 数据
	 * @param saveTime
	 *            保存的时间，单位：秒
	 */
	public void put(String key, Drawable value, int saveTime) {
		put(key, Utils.drawable2Bitmap(value), saveTime);
	}

	/**
	 * 读取 Drawable 数据
	 * 
	 * @param key
	 * @return Drawable 数据
	 */
	public Drawable getAsDrawable(String key) {
		if (getAsBinary(key) == null) {
			return null;
		}
		return Utils.bitmap2Drawable(Utils.Bytes2Bimap(getAsBinary(key)));
	}

	/**
	 * 获取缓存文件
	 * 
	 * @param key
	 * @return value 缓存的文件
	 */
	public File file(String key) {
		File f = mCache.newFile(key);
		if (f.exists())
			return f;
		return null;
	}

	/**
	 * 移除某个key
	 * 
	 * @param key
	 * @return 是否移除成功
	 */
	public boolean remove(String key) {
		return mCache.remove(key);
	}

	/**
	 * 清除所有数据
	 */
	public void clear() {
		mCache.clear();
	}

	/**
	 * @title 缓存管理器
	 * @author 杨福海（michael） www.yangfuhai.com
	 * @version 1.0
	 */
	public class ACacheManager {
		private final AtomicLong cacheSize;
		private final AtomicInteger cacheCount;
		private final long sizeLimit;
		private final int countLimit;
		private final Map<File, Long> lastUsageDates = Collections.synchronizedMap(new ArrayMap<File, Long>());
		protected File cacheDir;
		//内部类ACacheManager的构造函数
		private ACacheManager(File cacheDir, long sizeLimit, int countLimit) {
			this.cacheDir = cacheDir;
			this.sizeLimit = sizeLimit;
			this.countLimit = countLimit;
			cacheSize = new AtomicLong();//原子类实例cacheSize，不用加锁保证线程安全
			cacheCount = new AtomicInteger();//原子类实例cacheCount，不用加锁保证线程安全
			calculateCacheSizeAndCacheCount();
		}

		/**
		 * 计算 cacheSize和cacheCount
		 */
		private void calculateCacheSizeAndCacheCount() {
			new Thread(new Runnable() {
				@Override
				public void run() {
					long size = 0;//这里long类型才对
					int count = 0;
					File[] cachedFiles = cacheDir.listFiles();//返回缓存目录cacheDir下的文件数组
					if (cachedFiles != null) {
						for (File cachedFile : cachedFiles) {//对文件数组遍历
							size += calculateSize(cachedFile);
							count += 1;
							lastUsageDates.put(cachedFile, cachedFile.lastModified());//将缓存文件和最后修改时间插入map
						}
						cacheSize.set(size);//设置为给定值
						cacheCount.set(count);//设置为给定值
					}
				}
			}).start();
		}
		//更新cacheCount和cacheSize  lastUsageDates插入新建文件和时间的键值对
		//文件放入程序缓存后，统计缓存总量，总数，文件存放到文件map中（value值为文件最后修改时间，便于根据设置的销毁时间进行销毁）
		//缓存没有超过限制，则增加缓存总量，总数的数值
		//缓存超过限制，则减少缓存总量，总数的数值
		//通过removeNext方法找到最老文件的大小
		private void put(File file) {
			int curCacheCount = cacheCount.get();//获取数量
			while (curCacheCount + 1 > countLimit) {//大于上限
				long freedSize = removeNext();//移除旧的文件,返回文件大小
				cacheSize.addAndGet(-freedSize);//更新cacheSize

				curCacheCount = cacheCount.addAndGet(-1);//更新cacheCount
			}
			cacheCount.addAndGet(1);//更新cacheCount

			long valueSize = calculateSize(file);//计算文件大小
			long curCacheSize = cacheSize.get(); //获取当前缓存大小
			while (curCacheSize + valueSize > sizeLimit) {//大于上限
				long freedSize = removeNext(); //移除旧的文件,返回文件大小
				curCacheSize = cacheSize.addAndGet(-freedSize);//更新curCacheSize
			}
			cacheSize.addAndGet(valueSize);//更新cacheSize

			Long currentTime = System.currentTimeMillis();
			file.setLastModified(currentTime);//设置文件最后修改时间
			lastUsageDates.put(file, currentTime);//插入map
		}

		private File get(String key) {
			File file = newFile(key);
			Long currentTime = System.currentTimeMillis();
			file.setLastModified(currentTime);
			lastUsageDates.put(file, currentTime);

			return file;
		}
		//新建文件
		private File newFile(String key) {
			return new File(cacheDir, key.hashCode() + "");//新建文件，文件名为key的整型哈希码
		}

		private boolean remove(String key) {
			File image = get(key);
			return image.delete();
		}

		private void clear() {
			lastUsageDates.clear();
			cacheSize.set(0);
			File[] files = cacheDir.listFiles();
			if (files != null) {
				for (File f : files) {
					f.delete();
				}
			}
		}

		/**
		 * 移除旧的文件
		 * 
		 * @return
		 */
		private long removeNext() {
			if (lastUsageDates.isEmpty()) {
				return 0;
			}

			Long oldestUsage = null;
			File mostLongUsedFile = null;
			Set<Entry<File, Long>> entries = lastUsageDates.entrySet();
			synchronized (lastUsageDates) {
				for (Entry<File, Long> entry : entries) {
					if (mostLongUsedFile == null) {
						mostLongUsedFile = entry.getKey();
						oldestUsage = entry.getValue();
					} else {
						Long lastValueUsage = entry.getValue();
						if (lastValueUsage < oldestUsage) {
							oldestUsage = lastValueUsage;
							mostLongUsedFile = entry.getKey();
						}
					}
				}
			}

			long fileSize = calculateSize(mostLongUsedFile);
			if (mostLongUsedFile.delete()) {
				lastUsageDates.remove(mostLongUsedFile);
			}
			return fileSize;
		}

		private long calculateSize(File file) {
			return file.length();
		}
	}

	/**
	 * @title 时间计算工具类
	 * @author 何秀月
	 * @version 1.0
	 */
	private static class Utils {

		/**
		 * 判断缓存的String数据是否到期
		 * 
		 * @param str
		 * @return true：到期了 false：还没有到期
		 */
		private static boolean isDue(String str) {
			return isDue(str.getBytes());
		}

		/**
		 * 判断缓存的byte数据是否到期
		 * 
		 * @param data
		 * @return true：到期了 false：还没有到期
		 */
		private static boolean isDue(byte[] data) {
			String[] strs = getDateInfoFromDate(data);
			if (strs != null && strs.length == 2) {
				String saveTimeStr = strs[0];
				while (saveTimeStr.startsWith("0")) {
					saveTimeStr = saveTimeStr.substring(1, saveTimeStr.length());
				}
				long saveTime = Long.valueOf(saveTimeStr);
				long deleteAfter = Long.valueOf(strs[1]);
				if (System.currentTimeMillis() > saveTime + deleteAfter * 1000) {
					return true;
				}
			}
			return false;
		}
		//返回时间信息+value
		private static String newStringWithDateInfo(int second, String strInfo) {
			return createDateInfo(second) + strInfo;
		}

		private static byte[] newByteArrayWithDateInfo(int second, byte[] data2) {
			byte[] data1 = createDateInfo(second).getBytes();
			byte[] retdata = new byte[data1.length + data2.length];
			System.arraycopy(data1, 0, retdata, 0, data1.length);
			System.arraycopy(data2, 0, retdata, data1.length, data2.length);
			return retdata;
		}

		private static String clearDateInfo(String strInfo) {
			if (strInfo != null && hasDateInfo(strInfo.getBytes())) {
				strInfo = strInfo.substring(strInfo.indexOf(mSeparator) + 1, strInfo.length());
			}
			return strInfo;
		}

		private static byte[] clearDateInfo(byte[] data) {
			if (hasDateInfo(data)) {
				return copyOfRange(data, indexOf(data, mSeparator) + 1, data.length);
			}
			return data;
		}

		private static boolean hasDateInfo(byte[] data) {
			return data != null && data.length > 15 && data[13] == '-' && indexOf(data, mSeparator) > 14;
		}

		private static String[] getDateInfoFromDate(byte[] data) {
			if (hasDateInfo(data)) {
				String saveDate = new String(copyOfRange(data, 0, 13));
				String deleteAfter = new String(copyOfRange(data, 14, indexOf(data, mSeparator)));
				return new String[] { saveDate, deleteAfter };
			}
			return null;
		}

		private static int indexOf(byte[] data, char c) {
			for (int i = 0; i < data.length; i++) {
				if (data[i] == c) {
					return i;
				}
			}
			return -1;
		}

		private static byte[] copyOfRange(byte[] original, int from, int to) {
			int newLength = to - from;
			if (newLength < 0)
				throw new IllegalArgumentException(from + " > " + to);
			byte[] copy = new byte[newLength];
			System.arraycopy(original, from, copy, 0, Math.min(original.length - from, newLength));
			return copy;
		}

		private static final char mSeparator = ' ';
		//时间信息
		private static String createDateInfo(int second) {
			String currentTime = System.currentTimeMillis() + "";
			while (currentTime.length() < 13) {//小于13，前面补0
				currentTime = "0" + currentTime;
			}
			return currentTime + "-" + second + mSeparator;//当前时间-保存时间
		}

		/*
		 * Bitmap → byte[]
		 */
		private static byte[] Bitmap2Bytes(Bitmap bm) {
			if (bm == null) {
				return null;
			}
			ByteArrayOutputStream baos = new ByteArrayOutputStream();//byte[]输出流
			bm.compress(Bitmap.CompressFormat.PNG, 100, baos);//按指定的图片格式以及画质，将图片转换为输出流。压缩图片,不压缩是100，表示压缩率为0
			return baos.toByteArray();
		}

		/*
		 * byte[] → Bitmap
		 */
		private static Bitmap Bytes2Bimap(byte[] b) {
			if (b.length == 0) {
				return null;
			}
			return BitmapFactory.decodeByteArray(b, 0, b.length);//解析
		}

		/*
		 * Drawable → Bitmap
		 */
		private static Bitmap drawable2Bitmap(Drawable drawable) {
			if (drawable == null) {
				return null;
			}
			// 取 drawable 的长宽
			int w = drawable.getIntrinsicWidth();
			int h = drawable.getIntrinsicHeight();
			// 取 drawable 的颜色格式
			Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565;
			// 建立对应 bitmap
			Bitmap bitmap = Bitmap.createBitmap(w, h, config);
			// 建立对应 bitmap 的画布
			Canvas canvas = new Canvas(bitmap);
			drawable.setBounds(0, 0, w, h);
			// 把 drawable 内容画到画布中
			drawable.draw(canvas);
			return bitmap;
		}

		/*
		 * Bitmap → Drawable
		 */
		@SuppressWarnings("deprecation")
		private static Drawable bitmap2Drawable(Bitmap bm) {
			if (bm == null) {
				return null;
			}
			BitmapDrawable bd=new BitmapDrawable(bm);
			bd.setTargetDensity(bm.getDensity());
			return new BitmapDrawable(bm);
		}
	}

}
