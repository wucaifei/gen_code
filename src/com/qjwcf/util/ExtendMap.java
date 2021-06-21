package com.qjwcf.util;

import java.util.HashMap;
import java.util.Map;

public class ExtendMap<K,V> extends HashMap<K,V>{
	private static final long serialVersionUID = 1L;
	
	public ExtendMap() {
		super();
	}
	
	public ExtendMap(Map<K,V> map) {
		super();
		this.putAll(map);
	}
	
	public ExtendMap<K,V> add(K key, V value) {
		super.put(key, value);
		return this;
	}

	public String getString(K key){
		return this.get(key)!=null ? this.get(key).toString() : "";
	}
	
	public int getInt(K key){
		return getString(key).equals("") ? 0 : Integer.parseInt(getString(key));
	}
	
	public long getLong(K key){
		return getString(key).equals("") ? 0 : Long.parseLong(getString(key));
	}
	
	public float getFloat(K key){
		return getString(key).equals("") ? 0 : Float.parseFloat(getString(key));
	}
	
	public double getDouble(K key){
		return getString(key).equals("") ? 0 : Double.parseDouble(getString(key));
	}
	
	public boolean getBoolean(K key){
		return Boolean.parseBoolean(getString(key));
	}
}