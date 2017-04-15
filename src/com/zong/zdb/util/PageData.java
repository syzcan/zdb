package com.zong.zdb.util;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("rawtypes")
public class PageData extends HashMap implements Map, Serializable {

	private static final long serialVersionUID = 1L;

	public PageData() {
	}

	@SuppressWarnings("unchecked")
	public PageData(Object key, Object value) {
		super.put(key, value);
	}

	public String getString(Object key) {
		return (String) get(key);
	}

	@SuppressWarnings("unchecked")
	public PageData put(Object key, Object value) {
		super.put(key, value);
		return this;
	}

}
