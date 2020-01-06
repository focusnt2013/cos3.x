package com.focus.util;

import java.io.File;
import java.io.FileFilter;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: www.lazybug.com Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author 刘学
 * @version 1.0
 */

public class ConfigUtil
{
    public static String WEB_PATH = "";
    public static String APP_PATH = "";
    private static Config defaultConfig = null;

	/**
	 * 根据规则设置应用程序工作目录
	 * @param path
	 */
	public static void setAppPath(File path)
	{
		if( path == null || !path.exists() || path.isFile() ) return;
		File[] files = path.listFiles(new FileFilter(){
			public boolean accept(File pathname)
			{
				return pathname.isDirectory()&&pathname.getName().equals("lib");
			}
		});
		if( files.length == 1 )
		{
			File dir = files[0];
			File file = new File(dir, "cos.jar");
			if( file.exists() )
			{
				APP_PATH = path.getAbsolutePath();
			}
			else
			{
				APP_PATH = path.getAbsolutePath();
				if( APP_PATH.endsWith("bin") )
					APP_PATH = path.getParent();
			}
			final String separator = System.getProperty("file.separator");
			APP_PATH = APP_PATH.endsWith(separator)?APP_PATH:(APP_PATH+separator);
			return;
		}
		setAppPath(path.getParentFile());
	}
	/*
    public static void setPath( String path )
    {
        int i;
        if( ( i = path.lastIndexOf( "bin" ) ) != -1 )
        {
            APP_PATH = path.substring( 0, i );
        }
        else
        {
            APP_PATH = path;
        }
        APP_CONFIGPATH = APP_PATH + "config/config.properties";
    }
	 */
    public static double getDouble( String id )
    {
        if( defaultConfig == null )
        {
            defaultConfig = new Config( new File(APP_PATH, "config/config.properties") );
        }

        return defaultConfig.getDouble( id );
    }

    public static int getInteger( String id, int def )
    {
    	String val = System.getProperty(id);
    	if( val != null && Tools.isNumeric(val) ) return Integer.parseInt(val);
    	
        if( APP_PATH != null && APP_PATH.length() == 0 )
        {
            ConfigUtil.setAppPath(new File(System.getProperty( "user.dir" )));
        }

        if( defaultConfig == null )
        {
            defaultConfig = new Config( new File(APP_PATH, "config/config.properties") );
        }

        return defaultConfig.getInteger( id, def );
    }

    public static void setString( String id, String value )
    {
        if( defaultConfig == null )
        {
            defaultConfig = new Config( new File(APP_PATH, "config/config.properties") );
        }
        defaultConfig.setString(id, value);
    }

    /*
     * Read the information from Config table
     */
    public static String getString( String id )
    {
        return getString( id, null );
    }
    
    public static String getString( String id, String deft )
    {
    	String val = System.getProperty(id);
    	if( val != null ) return val;
    	
        if( APP_PATH != null && APP_PATH.isEmpty() )
        {
            ConfigUtil.setAppPath(new File(System.getProperty( "user.dir" )));
        }
        if( defaultConfig == null )
        {
            defaultConfig = new Config( new File(APP_PATH, "config/config.properties") );
        }

        return defaultConfig.getString(id, deft);
    }

    public static String getValue( String id, String deft )
    {
        if( APP_PATH != null && APP_PATH.isEmpty() )
        {
            ConfigUtil.setAppPath(new File(System.getProperty( "user.dir" )));
        }
        if( defaultConfig == null )
        {
            defaultConfig = new Config( new File(APP_PATH, "config/config.properties") );
        }

        return defaultConfig.getString(id, deft);
    }

    public Config createConfig( String path )
    {
        return new Config( path );
    }

    /**
     * 获取工作路径
     * @return String
     */
    public static String getWorkPath()
    {
        if( APP_PATH == null || APP_PATH.isEmpty() )
        {
            ConfigUtil.setAppPath(new File(System.getProperty( "user.dir" )));
        }
        return APP_PATH==null||APP_PATH.isEmpty()?System.getProperty( "user.dir" ):APP_PATH;
    }
}
