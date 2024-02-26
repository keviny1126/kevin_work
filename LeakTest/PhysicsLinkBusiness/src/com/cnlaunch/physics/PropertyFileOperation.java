package com.cnlaunch.physics;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.cnlaunch.physics.utils.MLog;

import android.util.Pair;

/**
 * 下位机硬件信息
 * 当序列号为空时，需要连接接头，读取下位机信息
 * @author xiefeihong
 * 
 */
public class PropertyFileOperation {
	private static final String TAG = "PropertyFileOperation";
	private ConcurrentHashMap<String, String> mPropertyInformationMap;
	private String mPath;

	public PropertyFileOperation(String path) {		
		mPath = path;
		mPropertyInformationMap = new ConcurrentHashMap<String, String>();
		Properties props = open();
		String key = null;
		if (MLog.isDebug) {
			StringBuilder sb = new StringBuilder();
			Enumeration<?> e = props.propertyNames();
			sb.append("PropertyFileOperation file init (key) collenction:");
			while (e.hasMoreElements()) {
				sb.append(String.format("(%s),", e.nextElement().toString()));
			}
			MLog.d(TAG, sb.toString());
		}
		for (Enumeration<?> e = props.propertyNames(); e.hasMoreElements();) {
			key = e.nextElement().toString();
			mPropertyInformationMap.put(key, props.getProperty(key));
		}
		if (MLog.isDebug) {
			StringBuilder sb = new StringBuilder();
			Set<Map.Entry<String, String>> set = mPropertyInformationMap.entrySet();
			Iterator<Map.Entry<String, String>> iterator = set.iterator();
			sb.append("PropertyFileOperation file (key,value) collenction:");
			while (iterator.hasNext()) {
				Map.Entry<String, String> entry = iterator.next();
				sb.append(String.format("(%s,%s),", entry.getKey(),
						entry.getValue()));
			}
			MLog.d(TAG, sb.toString());
		}
	}

	/**
	 * 保存键值对列表
	 * 
	 * @param pairList
	 * @return
	 */
	public boolean put(List<Pair<String, String>> pairList) {
		synchronized (this) {
			if (MLog.isDebug) {
				StringBuilder sb = new StringBuilder();
				Iterator<Pair<String, String>> iterator = pairList.iterator();
				sb.append("PropertyFileOperation pair List (key,value) collenction:");
				while (iterator.hasNext()) {
					Pair<String, String> entry = iterator.next();
					sb.append(String.format("(%s,%s),", entry.first,entry.second));
				}
				MLog.d(TAG, sb.toString());
			}
			Iterator<Pair<String, String>> iterator = pairList.iterator();
			while (iterator.hasNext()) {
				Pair<String, String> entry = iterator.next();
				mPropertyInformationMap.put(entry.first, entry.second);
			}
		}
		return save();
	}

	public boolean put(String key, String value) {
		mPropertyInformationMap.put(key, value);
		return save();
	}

	public String get(String key) {
		if (MLog.isDebug) {
			MLog.d(TAG, "PropertyFileOperation file get key="+key);
			StringBuilder sb = new StringBuilder();
			Set<Map.Entry<String, String>> set = mPropertyInformationMap.entrySet();
			Iterator<Map.Entry<String, String>> iterator = set.iterator();
			sb.append("PropertyFileOperation file (key,value) collenction:");
			while (iterator.hasNext()) {
				Map.Entry<String, String> entry = iterator.next();
				sb.append(String.format("(%s,%s),", entry.getKey(),
						entry.getValue()));
			}
			MLog.d(TAG, sb.toString());
		}
		return mPropertyInformationMap.get(key);
	}

	/**
	 * 返回同组相关的设定列表
	 * @param groupKey
	 * @return
	 */
	public List<Pair<String,String>> getGroups(String groupKey) {
		StringBuilder sb = new StringBuilder();
		if (MLog.isDebug) {
			MLog.d(TAG, "PropertyFileOperation file get groupKey=" + groupKey);
			sb.append("PropertyFileOperation file (key,value) collenction:");
		}
		Set<Map.Entry<String, String>> set = mPropertyInformationMap.entrySet();
		Iterator<Map.Entry<String, String>> iterator = set.iterator();
		ArrayList<Pair<String, String>> list = new ArrayList<Pair<String, String>>();
		while (iterator.hasNext()) {
			Map.Entry<String, String> entry = iterator.next();
			if (MLog.isDebug) {
				sb.append(String.format("(%s,%s),", entry.getKey(), entry.getValue()));
			}
			String[] resultValue = entry.getKey().split(".");
			if (resultValue.length >= 2) {
				if (MLog.isDebug) {
					sb.append(String.format("(split %s,%s),", resultValue[0], resultValue[1]));
				}
				if (resultValue[1].equalsIgnoreCase("groupKey")) {
					Pair<String, String> pair = new Pair<String, String>(entry.getKey(), entry.getValue());
					list.add(pair);
				}
			}
		}
		if (MLog.isDebug) {
			MLog.d(TAG, sb.toString());
		}
		return list;
	}


	private Properties open() {
		File file = new File(mPath);
		Properties props = new Properties();
		if (file.exists()) {
			try {
				FileInputStream fileInputStream = new FileInputStream(file);
				InputStreamReader inputStreamReader = new InputStreamReader(
						fileInputStream, Charset.forName("UTF-8"));
				props.load(inputStreamReader);
				inputStreamReader.close();
				fileInputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
		return props;
	}

	public boolean save() {
		boolean state = false;
		try {
			File file = new File(mPath);
			if (file.getParentFile().exists() == false) {
				file.getParentFile().mkdirs();
			}
			FileOutputStream fileOutputStream = new FileOutputStream(file);
			OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream, Charset.forName("UTF-8"));
			if (MLog.isDebug) {
				StringBuilder sb = new StringBuilder();
				Set<Map.Entry<String, String>> set = mPropertyInformationMap.entrySet();
				Iterator<Map.Entry<String, String>> iterator = set.iterator();
				sb.append("PropertyFileOperation save file (key,value) collenction:");
				while (iterator.hasNext()) {
					Map.Entry<String, String> entry = iterator.next();
					sb.append(String.format("(%s,%s),", entry.getKey(),entry.getValue()));
				}
				MLog.d(TAG, sb.toString());
			}
			Properties props = new Properties();
			Set<Map.Entry<String, String>> set = mPropertyInformationMap.entrySet();
			Iterator<Map.Entry<String, String>> iterator = set.iterator();
			while (iterator.hasNext()) {
				Map.Entry<String, String> entry = iterator.next();
				props.setProperty(entry.getKey(), entry.getValue());
			}
			props.store(outputStreamWriter, null);
			outputStreamWriter.close();
			fileOutputStream.close();
			state = true;
		} catch (Exception e) {
			e.printStackTrace();
			state = false;
		}
		return state;
	}
}
