package com.portal.exceptions;

import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

public class BusException extends Exception {

	private static final long serialVersionUID = 3884976565984496471L;
	
	public static final int CODE = 600;
	
	public static final String CODE_REASON = "Wrong business rule";
	
	private Properties properties = new Properties();

	public BusException() {
	}

	public BusException(String message) {
		super(message);
	}

	public BusException(String type, String message) {
		super();
	}
	public BusException(Throwable cause) {
		super(cause);
	}

	public BusException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public void setType( String type ) {
		this.addProperty( "type" , type);
	}
	
	public void addProperty(Object key, Object value) {
		this.properties.put(key, value);
	}
	
	public void removeProperty(Object key) {
		this.properties.remove(key);
	}
	
	public Set<Entry<Object, Object>> listProperties() {
		return this.properties.entrySet();
	}
	
	public Object getProperty(Object key) {
		if( this.properties.containsKey(key) ) {
			return this.properties.get(key);
		} else {
			return null;
		}
	}
}
