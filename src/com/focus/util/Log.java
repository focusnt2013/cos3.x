/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: lutong Copyright (c) 2004</p>
 * <p>Company: </p>
 * @author 锟斤拷学
 * @version 0.5
 */
package com.focus.util;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.LinkedList;
import java.util.List;

public class Log implements Runnable
{
	public static final int _FILE_SIZE = 32 * 1024 * 1024;
	/* 加上volatile可以防止多线程间的无序写入问题 */
	private static volatile Log instance;
	/**/
	private LinkedList<String> queue;
	/**/
	private boolean debug;
	/**/
	private boolean writable;
	/**/
	private boolean logable;
	/**/
	private boolean running;
	/* 日志目录 */
	// private String subroot = "";
	/* 日志文件 */
	private String logfilename;
	/* 日志路径 */
	private String logPath;

	// 枷锁可以防止多线程竞争
	public static Log getInstance()
	{
		if (instance != null)
		{
			return instance;
		}
		synchronized (Log.class)
		{
			if (instance != null)
			{
				return instance;
			}
			instance = new Log();
			return instance;
		}
	}

	public static Log createInstance()
	{
		return new Log();
	}

	private Log()
	{
		queue = new LinkedList<String>();
		writable = false;
	}

	public synchronized void start()
	{
		if (!running)
		{
			Thread thread = new Thread(this);
			thread.setDaemon(true);// 设置为非守护线程
			running = true;
			thread.start();
		}

		// 在次检查日志 确保从缓存队列中输出到文件
		Runtime.getRuntime().addShutdownHook(new Thread()
		{
			public void run()
			{
				try
				{
					if (instance != null && instance.isLogable())
						instance.writeLog();
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		});
	}

	public synchronized void close()
	{
		if (this.running)
		{
			this.notify();
			this.running = false;
			if (!this.queue.isEmpty())
			{
				try
				{
					this.wait(3000);
				}
				catch (InterruptedException ex)
				{
					// System.out.println( "InterruptedException:" + ex );
				}
				// System.out.println( "Loger close." );
			}
		}
	}

	public boolean isWritable()
	{
		return this.writable;
	}

	public boolean isDebug()
	{
		return debug;
	}

	public boolean isLogable()
	{
		return logable;
	}

	private synchronized void writeLog() throws Exception
	{
		OutputStreamWriter out = null;
		try
		{
			F path = new F(logPath != null ? logPath : (ConfigUtil.getWorkPath() + "log/"));
			if (!path.exists())
			{
				path.mkdirs();
			}
			String filename = logfilename != null ? logfilename : (Tools.getFormatTime("yyyy-MM-dd", System.currentTimeMillis()) + ".txt");
			F file = new F(path, filename);
			out = new OutputStreamWriter(new FileOutputStream(file, true), System.getProperty("file.encoding"));
			Object item = null;
			while ((item = queue.poll()) != null)
			{
				out.write(item.toString());
			}
			out.flush();
		}
		catch (Exception e)
		{
			throw e;
		}
		finally
		{
			if (out != null)
			{
				out.close();
			}
		}
	}

	private void clearLogFile(F path)
	{
		if (null == path)
			return;
		if (path.exists())
		{
			// 删除1个月前过期太久的日志或者大于100M一星期内的数据
			F[] files = path.listFiles();
			if (files != null)
			{
				for (F file : files)
				{
					if (file.isDirectory())
						clearLogFile(file);// 递归清理文件

					if (System.currentTimeMillis() - file.lastModified() > 30 * Tools.MILLI_OF_DAY)
					{
						file.delete();
					}
					else if (file.length() > 100 * 1024 * 1024 && System.currentTimeMillis() - file.lastModified() > 7 * Tools.MILLI_OF_DAY)
					{
						file.delete();
					}
				}
			}
		}
	}

	public void run()
	{
		writable = true;
		synchronized (this)
		{
			this.notify();
		}

		F path = new F(logPath != null ? logPath : (ConfigUtil.getWorkPath() + "log/"));
		if (path.exists())
		{
			// 删除1个月前过期太久的日志或者大于100M一星期内的数据
			clearLogFile(path);
		}
		// 如果运行标识有效或者待日志的队列不为空
		while (running || !this.queue.isEmpty())
		{
			synchronized (this)
			{
				if (this.queue.size() == 0 && running)
				{
					try
					{
						this.wait();
						continue;
					}
					catch (InterruptedException e)
					{
					}
				}
			}

			try
			{
				this.writeLog();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		writable = false;
		logable = false;
		synchronized (this)
		{
			this.notify();
		}
	}

	private synchronized void write(String log)
	{
		if (this.writable)
		{
			this.queue.addLast(log);
			this.notify();
		}
	}

	public static final void msg(List<?> list)
	{
		StringBuffer sb = new StringBuffer("List[" + list.size() + "]:\r\n");
		for (Object line : list)
		{
			sb.append(line.toString());
			sb.append("\r\n");
		}
		getInstance().println(null, "Inf", sb.toString());
	}

	public static final void print(String text, Object... args)
	{
		if( text == null || text.isEmpty() ) return;
		text = String.format(text, args);
		msg(text);
	}

	
	public static final void msg(String txt)
	{
		getInstance().println(null, "Inf", txt);
	}

	public static final void msg(String txt, boolean println)
	{
		if( println ) System.out.println(txt);
		msg(txt);
	}

	public static final void msg(Object obj, String txt)
	{
		getInstance().println(obj, "Inf", txt);
	}

	public static final void err(String txt)
	{
		getInstance().println(null, "Err", txt);
	}

	public static final void err(Exception e)
	{
		getInstance().error(null, e);
	}

	public static final void printf(String text, Object... args)
	{
		if( text == null || text.isEmpty() ) return;
		text = String.format(text, args);
		err(text);
	}
	
	public static final void err(String txt, Exception e)
	{
		getInstance().error(txt, e);
	}
 
	public static final void debug(Object obj, String txt, String filter)
	{
		if (Log.getInstance().isDebug() && filter != null)
		{
			if (obj.getClass().getName().lastIndexOf(filter) >= 0)
			{
				getInstance().println(obj, "Debug", txt);
			}
		}
	}

	public static final void err(Object obj, String txt)
	{
		getInstance().println(obj, "Err", txt);
	}

	public static final void war(String txt)
	{
		getInstance().println(null, "War", txt);
	}
	
	public static final void war(String txt, boolean println)
	{
		if( println ) System.out.println(txt);
		war(txt);
	}


	public static final void war(Object obj, String txt)
	{
		getInstance().println(obj, "War", txt);
	}

	/*
	 * private static final void log( Object obj, String txt, String info ) {
	 * String content; if( obj != null ) { content = "[" + Tools.getFormatTime(
	 * "yyyy-MM-dd HH:mm:ss", System.currentTimeMillis() ) + "] " +
	 * obj.getClass().getName() + "\n" + info + ":" + txt + "\n"; } else {
	 * content = "[" + Tools.getFormatTime( "yyyy-MM-dd HH:mm:ss",
	 * System.currentTimeMillis() ) + "] \n" + info + ":" + txt + "\n"; } // if(
	 * !Log.getInstance().isLogable() ) // { // 不可日志时，当debug为true就，允许通过sysout输出
	 * if(Log.getInstance().isDebug()) { System.out.println(content); } //
	 * return; // } if( Log.getInstance().isLogable() ) {
	 * Log.getInstance().write( content ); } }
	 */
	public static final void logMessage(Object obj, String log)
	{
		getInstance().println(obj, "Info", log);
	}

	public static final void logWarning(Object obj, String log)
	{
		getInstance().println(obj, "War", log);
	}

	public static final void logError(Object obj, String log)
	{
		getInstance().println(obj, "Err", log);
	}

	public void setDebug(boolean debug)
	{
		this.debug = debug;
	}

	public void setLogable(boolean logable)
	{
		this.logable = logable;
	}

	public void setSubroot(String subroot)
	{
		// this.subroot = subroot;
		this.logPath = ConfigUtil.getWorkPath() + "log/" + subroot;
	}

	public void setLogfile(String logfilename)
	{
		if (logfilename != null)
		{
			this.logfilename = logfilename + ".txt";
		}
	}

	/**
	 * 直接给一个文件作为日志文件
	 * 
	 * @param logFile
	 */
	public void setLogfile(F logFile)
	{
		this.logfilename = logFile.getName();
		this.logPath = logFile.getParent();
	}

	/**
	 * 以下方法是非单实例模式下日志打印
	 * 
	 * @param content
	 */
	public void info(String content)
	{
		this.println(null, "Inf", content);
	}

	public void info(Object obj, String content)
	{
		this.println(obj, "Inf", content);
	}

	public void warn(String content)
	{
		this.println(null, "War", content);
	}

	public void warn(Object obj, String content)
	{
		this.println(obj, "War", content);
	}

	public void error(String content)
	{
		this.println(null, "Err", content);
	}

	public void error(Object obj, String content)
	{
		this.println(obj, "Err", content);
	}

	public void error(String tip, Exception e)
	{
		if (e == null)
			return;
		StringBuffer sb = new StringBuffer();
		sb.append(tip==null?"":(tip+": "));
		sb.append(e.getMessage());
		sb.append("\r\n");
		StackTraceElement[] el = e.getStackTrace();
		for (int i = 0; i < el.length; i++)
		{
			sb.append(el[i].getClassName());
			sb.append(' ');
			sb.append(el[i].getMethodName());
			sb.append('[');
			sb.append(el[i].getLineNumber());
			sb.append("][");
			sb.append(el[i].isNativeMethod());
			sb.append("]\r\n");
		}

		error(sb.toString());
	}

	/**
	 * 打印输出
	 * 
	 * @param obj
	 * @param txt
	 * @param info
	 */
	private void println(Object obj, String info, String txt)
	{
		String content;
		if( obj != null )
		{
			if( txt == null && obj instanceof String ){
				content = "[" + Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", System.currentTimeMillis()) + "]" + info + ":\r\n" + obj + "\r\n";
			}
			else{
				content = "[" + Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", System.currentTimeMillis()) + "]" + obj.getClass().getName() + " " + info + ":\r\n" + txt + "\r\n";
			}
		}
		else
		{
			content = "[" + Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", System.currentTimeMillis()) + "]" + info + ":\r\n" + txt + "\r\n";
		}
		// 不可日志时，当debug为true就，允许通过sysout输出
		if (isDebug())
		{
			System.out.println(content);
		}
		if (isLogable())
		{
			write(content);
		}
	}
}
