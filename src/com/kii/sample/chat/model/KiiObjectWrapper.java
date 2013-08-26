package com.kii.sample.chat.model;

import com.kii.cloud.storage.KiiObject;
import com.kii.cloud.storage.exception.IllegalKiiBaseObjectFormatException;

/**
 * Key-Valueで値を管理するKiiObjectをラップして、ドメインクラスを作成するための基本クラスです。
 * このクラスを継承してドメインクラスを作成します。
 * 
 * @author noriyoshi.fukuzaki@kii.com
 */
public abstract class KiiObjectWrapper {
	
	public static final String FIELD_CREATED = "_created";
	protected final KiiObject kiiObject;
	
	public KiiObjectWrapper(KiiObject object) {
		this.kiiObject = object;
	}
	
	protected void set(String key, String value) {
		this.kiiObject.set(key, value);
	}
	protected void remove(String key) {
		this.kiiObject.remove(key);
	}
	protected String getString(String key) {
		try {
			return this.kiiObject.getString(key);
		} catch (IllegalKiiBaseObjectFormatException e) {
			return null;
		}
	}
	protected Boolean getBoolean(String key) {
		try {
			return this.kiiObject.getBoolean(key);
		} catch (IllegalKiiBaseObjectFormatException e) {
			return null;
		}
	}
	public KiiObject getKiiObject() {
		return this.kiiObject;
	}
	@Override
	public String toString() {
		return this.kiiObject.toString();
	}
	
}
