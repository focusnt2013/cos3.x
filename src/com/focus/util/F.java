package com.focus.util;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;

/**
 * 继承File类重构方法，解决java.io.File类中相对路径问题
 * 在构造File类的时候强行将相对路径转成绝对路径
 * @author focus
 * @since 2017-05-07 16:21
 */
public class F extends File 
{
	private static final long serialVersionUID = -9193637518138597193L;
	private static String USER_DIR;

	public F(String parent, String child)
	{
		super(getRealpath(parent), child);
	}

	public F(String path)
	{
		super(getRealpath(path));
	}
	
	public F(F parent, String child) 
	{
		super(parent, child);
	}
	
	public static final String getRealpath(String path)
	{
		if( path.indexOf('\\') != -1 )
		{
			path = path.replace("\\", "/");
		}
		if( path.startsWith("/") ) return path;
		int i = path.indexOf(':');
		if( i > 0 ) return path;

		if( USER_DIR == null )
		{
			String userdir = System.getProperty("user.dir");
			if( userdir.indexOf('\\') > 0 )
			{
				userdir = userdir.replace("\\", "/");
			}
			if( !userdir.endsWith("/") ) userdir += "/";
			USER_DIR = userdir;
		}
		return USER_DIR+path;
	}
	
	/**
	 * 重构listFies方法
	 */
	public F[] listFiles()
	{
		File[] files = super.listFiles();
		if( files == null ) return null;
		F[] fs = new F[files.length];
		int i = 0;
		for(File file : files)
		{
			fs[i++] = new F(file.getPath());
		}
		return fs;
	}

	
	public F[] listFiles(FileFilter filter)
	{
		File[] files = super.listFiles(filter);
		if( files == null ) return null;
		F[] fs = new F[files.length];
		int i = 0;
		for(File file : files)
		{
			fs[i++] = new F(file.getPath());
		}
		return fs;
	}
	
	public F[] listFiles(FilenameFilter filter)
	{
		File[] files = super.listFiles(filter);
		if( files == null ) return null;
		F[] fs = new F[files.length];
		int i = 0;
		for(File file : files)
		{
			fs[i++] = new F(file.getPath());
		}
		return fs;
	}
}
