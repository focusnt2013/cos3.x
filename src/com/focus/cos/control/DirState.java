package com.focus.cos.control;

import java.io.File;
import java.io.Serializable;

import com.focus.util.Tools;

public class DirState implements Serializable{
	private static final long serialVersionUID = -2774718389848334616L;
	/*目录大小*/
	private long size;
	/*文件个数*/
	private int count;
	/*文件夹路径*/
	private String path;

	public DirState(File dir) {
		this.path = dir.getPath();
	}
	
	public long size() {
		return size;
	}
	public void size(long size) {
		this.size += size;
	}
	public int count() {
		return count;
	}
	public void count(int count) {
		this.count += count;
	}
	public String getPath() {
		return path;
	}
	
	public String toString(){
		return String.format("%s %s %s", path, count, bytesScale(size));
	}

	/**
	 * 
	 * @param length
	 * @return
	 */
	public final static long Ks = 1024;
	public final static long Ms = 1024*Ks;
	public final static long Gs = 1024*Ms;
	public final static long Ts = 1024*Gs;
	public final static long Ps = 1024*Ts;
	public static String bytesScale(long length)
	{
		double size = length;
		StringBuffer sb = new StringBuffer();
        if( size < Ks )
        {
        	sb.append(length + "B");
        }
        else if( size < Ms )
        {
        	sb.append(Tools.DF.format(size/Ks) + "K");
        }
        else if( size < Gs )
        {
        	sb.append(Tools.DF.format(size/Ms) + "M");
        }
        else if( size < Ts )
        {
        	sb.append(Tools.DF.format(size/Gs) + "G");
        }
        else
        {
        	sb.append(Tools.DF.format(size/Ts) + "T");
        }
        return sb.toString();		
	}
}
