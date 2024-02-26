package com.cnlaunch.physics.utils.message;

import org.json.JSONException;
import org.json.JSONObject;

import com.cnlaunch.physics.utils.MLog;

public class Letter implements ILetter {
	private static final String TAG = "Letter";
	private String content;
	private String receiver;
	private String sender;

	public Letter() {
		this.receiver ="";
		this.sender = "";
		this.content =null;
	}

	public Letter(String content) {
		this();
		this.content = content;
	}
	public Letter(String receiver, String sender,String content) {
		this.receiver =receiver;
		this.sender = sender;
		this.content = content;
	}
	@Override
	public void setReceiver(String receiver) {
		this.receiver = receiver;

	}

	@Override
	public String getReceiver() {
		return this.receiver;
	}

	@Override
	public void setSender(String sender) {
		this.sender = sender;

	}

	@Override
	public String getSender() {
		return this.sender;
	}

	@Override
	public void setContent(String content) {
		this.content = content;

	}

	@Override
	public String getContent() {
		return this.content;
	}
	
	@Override
	public String toJSONString() {
		try {
			JSONObject json = new JSONObject();
			json.put("receiver", this.receiver);
			json.put("sender", this.sender);
			json.put("content", this.content);
			if(MLog.isDebug){
				MLog.d(TAG, "this.content="+this.content);
				MLog.d(TAG, "json.toString()="+json.toString());
			}
			return json.toString();
		} catch (JSONException e) {
			e.printStackTrace();
			MLog.d(TAG, "信件序列化失败");
			return "";
		}

	}

	static ILetter fromString(String Buffer) {
		try {
			JSONObject json = new JSONObject(Buffer);
			return new Letter(json.getString("receiver"),json.getString("sender"),json.getString("content"));			
		} catch (JSONException e) {
			e.printStackTrace();
			MLog.d(TAG, "信件反序列化失败");
			return null;
		}
	}
}