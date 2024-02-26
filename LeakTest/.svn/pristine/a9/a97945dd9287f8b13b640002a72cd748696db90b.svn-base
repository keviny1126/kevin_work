package com.cnlaunch.physics.utils;

import android.content.Context;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;

/**
 * Assert 资源属性文件操作
 * @author xiefeihong
 *
 */
public class AssertPropertyFileOperation {
	private static final String TAG = "AssertPropertyFileOperation";
	private Properties props;
	private String filename;

	/**
	 *
	 * @param context
	 * @param filename Assert资源中的绝对路径
     */
	public AssertPropertyFileOperation(Context context, String filename) {
		props = new Properties();
		this.filename = filename;
		try {
			InputStream input = context.getAssets().open(filename);
			if (input != null) {
				props.load(input);
				input.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
			props = null;
		}
	}
	public String getProperty(String key) {
		if(props==null){
			return "";
		}
		String property = props.getProperty(key);
		if(property==null){
			return "";
		}
		else{
			return property;
		}
	}

	@Override
	public String toString() {
		if (props == null) {
			return String.format(" %s no property", filename);
		}
		StringBuilder sb = new StringBuilder();
		String key = null;
		for (Enumeration<?> e = props.propertyNames(); e.hasMoreElements(); ) {
			key = e.nextElement().toString();
			sb.append(String.format("(%s=%s),", key, props.getProperty(key)));
		}
		return sb.toString();
	}
}
