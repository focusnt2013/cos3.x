package com.focus.cos.web.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import com.focus.util.QuickSort;

public class AjaxResult<T> implements java.io.Serializable
{
	private static final long serialVersionUID = 5619048741952184646L;
	private boolean succeed = false;//true表示成功,false失败
	private String message;
	protected ArrayList<Object> objects = new ArrayList<Object>();
	protected HashMap<String, Boolean> container = new HashMap<String, Boolean>();
	private T t = null; // 用于包装用户需要返回的对象
	private long timestamp;

	public void setTimestamp(long timestamp)
	{
		this.timestamp = timestamp;
	}
	
	public long getTimestamp()
	{
		return timestamp;
	}
	
	public String getMessage()
	{
		return message;
	}
	public void setMessage(String message)
	{
		this.message = message;
	}
	public boolean isSucceed()
	{
		return succeed;
	}
	public void setSucceed(boolean succeed)
	{
		this.succeed = succeed;
	}
	public Object[] getObjects()
	{
		Object[] array = new Object[objects.size()];
		objects.toArray(array);
		return array;
	}
	
	public void sort(QuickSort qs)
	{
		qs.sort(this.objects);
	}
	
	public Object getFirstObject()
	{
		return objects.isEmpty()?null:objects.get(objects.size()-1);
	}
	
	public void setResult(T t) {
		this.t = t;
	}
	
	public T getResult() {
		return t;
	}
	
	public void add(Object object)
	{
		objects.add(object);
	}
	public void add(int i, Object object)
	{
		objects.add(i, object);
	}
	public void sort(Comparator<Object> c)
	{
		Collections.sort(objects, c);
	}
	public int objectsSize()
	{
		return objects.size();
	}
	public HashMap<String, Boolean> getContainer()
	{
		return container;
	}
}
