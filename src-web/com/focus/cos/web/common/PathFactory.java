package com.focus.cos.web.common;

import java.io.File;
import java.io.FileFilter;

/**
 * 路径工程，记录平台所有可能的配置文件的绝对路径
 * @author focus
 *
 */
public class PathFactory
{
	private static File appPath;
	private static File cfgPath;
	private static File dataPath;

	private static File appCfgPath;
	private static File accCfgPath;
	
	private static File perfPath;
	private static File columnPath;
	private static File terminalAdapterPath;
	private static File reportPath;
	private static File webappPath;//应用的路径
	static
	{
		try
		{
			webappPath = new File(ClassLoaderUtil.getSystemPath("")).getParentFile().getParentFile();
//			appPath = webappPath.getParentFile().getParentFile().getParentFile();
			setAppPath(webappPath.getParentFile());
			if( appPath == null )
			{
				System.err.println("Failed to setup the program for not found the environment of config(config/modules.xml).");
				System.exit(-1);
			}
			setCfgPath(new File(appPath, "config/"));
			setDataPath(new File(appPath, "data/"));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * 根据规则设置应用程序工作目录
	 * @param path
	 */
	private static void setAppPath(File path)
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
				appPath = path;
				return;
			}
		}
		setAppPath(path.getParentFile());
	}

	public static void setDataPath(File path)
	{
		if (dataPath == null)
		{
			dataPath = path;
//			emaMailPath = new File(dataPath, "emamail/");
//			alarmPath = new File(dataPath, "alarm/");
			perfPath = new File(dataPath, "perf/");
//			monitorPath = new File(dataPath, "monitor/");
			columnPath = new File(dataPath, "column/");
			reportPath = new File(dataPath,"report/");
		}
	}

	public static void setCfgPath(File path)
	{
		if (cfgPath == null)
		{
			cfgPath = path;
//			cmpCfgPath = new File(cfgPath, "cmp/");
			appCfgPath = new File(cfgPath, "app/");
			accCfgPath = new File(cfgPath, "acc/");
//			sysCfgPath = new File(cfgPath, "sys/");
			terminalAdapterPath = new File(cfgPath, "terminalAdapter/");
		}
	}

	public static void setPath(File path)
	{
		if (appPath == null)
		{
			appPath = path;
			cfgPath = new File(path, "config/");
//			cmpCfgPath = new File(cfgPath, "cmp/");
			appCfgPath = new File(cfgPath, "app/");
			accCfgPath = new File(cfgPath, "acc/");
//			sysCfgPath = new File(cfgPath, "sys/");
			terminalAdapterPath = new File(cfgPath, "terminalAdapter/");
//			emaMailPath = new File(appPath, "data/emamail/");
//			alarmPath = new File(appPath, "data/alarm/");
			perfPath = new File(appPath, "data/perf/");
//			monitorPath = new File(appPath, "data/monitor/");
			columnPath = new File(appPath, "data/column/");
			reportPath = new File(appPath,"data/report/");
//			if (!cmpCfgPath.exists())
//			{
//				cmpCfgPath.mkdirs();
//			}
//			if (!appCfgPath.exists())
//			{
//				appCfgPath.mkdirs();
//			}
//			if (!accCfgPath.exists())
//			{
//				accCfgPath.mkdirs();
//			}
//			if (!terminalAdapterPath.exists())
//			{
//				terminalAdapterPath.mkdirs();
//			}
		}
	}

	/**
	 * 取应用根目录,亦即安装目录
	 * 
	 * @return String
	 */
	public static File getAppPath()
	{
		return appPath;
	}

	/**
	 * 取应用配置路径,应用配置存放在安装目录下config目录下
	 * 
	 * @return String
	 */
	public static File getCfgPath()
	{
		return cfgPath;
	}

	/**
	 * 应用配置路径
	 * 
	 * @return String
	 */
	public static File getAppCfgPath()
	{
		if( !appCfgPath.exists() )
		{
			appCfgPath.mkdirs();
		}
		return appCfgPath;
	}

	/**
	 * 访问接入配置路径
	 * 
	 * @return
	 */
	public static File getAccCfgPath()
	{
		if( !accCfgPath.exists() )
		{
			accCfgPath.mkdirs();
		}
		return accCfgPath;
	}

//	public static File getCmpCfgPath()
//	{
//		return cmpCfgPath;
//	}

//	public static File getSysCfgPath()
//	{
//		return sysCfgPath;
//	}
	/**
	 * 企业移动信箱Data路径
	 * 
	 * @return
	public static File getEmaMailPath()
	{
		return emaMailPath;
	}
	 */

	/**
	 * 实时告警文件路径
	 * 
	 * @return String
	public static File getAlarmPath()
	{
		return alarmPath;
	}
	public static File getMonitorPath()
	{
		return monitorPath;
	}
	 */

	/**
	 * 性能数据文件路径
	 * 
	 * @return String
	 */
	public static File getPerfPath()
	{
		return perfPath;
	}
	

	public static File getColumnPath()
	{
		return columnPath;
	}

	public static File getTerminalAdapterPath()
	{
		return terminalAdapterPath;
	}
	
	public static File getReportPath()
	{
		return reportPath;
	}

	public static File getDataPath()
	{
		return dataPath;
	}

	public static File getWebappPath()
	{
		return webappPath;
	}
	
	public static void main(String[] args)
	{
		System.out.println(PathFactory.getCfgPath());
	}
}
