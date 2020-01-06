package com.focus.cos.digg;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.focus.util.QuickSort;
import com.focus.util.Tools;

public class Cache extends ArrayList<JSONObject>
{
	private static final long serialVersionUID = 1527961926046745492L;
	private static JSONArray Pq_sort;
	
	public Cache(byte[] data) throws JSONException, UnsupportedEncodingException
	{
		JSONArray array = new JSONArray(new String(data, "UTF-8"));
		for(int i = 0; i < array.length(); i++ )
		{
			this.add(array.getJSONObject(i));
		}
	}
	public Cache()
	{
	}
	
	/**
	 * 得到cached的数据
	 * @return
	 */
	public JSONArray getData()
	{
		if( pageSize == 0 ) pageSize = super.size();
		JSONArray data = new JSONArray();
		for(int i = start(); i < this.size(); i++ )
		{
			JSONObject row = this.get(i);
			data.put(row);
		}
		return data;
	}
	
	/**
	 * 得到分页的数
	 * @param skip
	 * @param len
	 * @return
	 */
	private int pageSize;
	private int skip = -1;
	public void setSkip(int skip, int pageSize)
	{
		this.skip = skip;
		this.pageSize = skip + pageSize;
		if( this.pageSize > this.size() ) this.pageSize = this.size();
	}
	
	public int start()
	{
		return skip==-1?0:skip;
	}
	
	public int size()
	{
		return skip==-1?super.size():(this.pageSize>super.size()?super.size():this.pageSize);
	}
	
	public byte[] toBytes() throws UnsupportedEncodingException
	{
		return this.toString().getBytes("UTF-8");
	}
	
	/**
	 * 排序
	 * @param dataIndx
	 * @param dataType
	 * @param dir
	 */
	public void sort(JSONArray pq_sort)
	{
		Pq_sort = pq_sort;
		QuickSort handler = new QuickSort(){
			@Override
			public boolean compareTo(Object sortSrc, Object pivot) {
				String dataIndx, dir;
				JSONObject l = (JSONObject)sortSrc;
				JSONObject r = (JSONObject)pivot;
				boolean result = false;
				for(int i = 0; i < Pq_sort.length(); )
				{
					JSONObject e = Pq_sort.getJSONObject(i++);
					dataIndx = e.getString("dataIndx");	
					dir = e.getString("dir");
					String lv = l.has(dataIndx)?l.get(dataIndx).toString():"";
					String rv = r.has(dataIndx)?r.get(dataIndx).toString():"";
					if( Tools.isNumeric(lv) && Tools.isNumeric(rv) )
					{
						boolean r1 = dir.equals("down")?Long.parseLong(lv)>Long.parseLong(rv):Long.parseLong(lv)<Long.parseLong(rv);
						result = result || r1;
					}
					else
					{
						boolean r1 = dir.equals("down")?lv.compareTo(rv)>0:lv.compareTo(rv)<0;
						result = result || r1;
					}
				}
				return result;
			}
		};
		handler.sort(this);
	}
	
}
