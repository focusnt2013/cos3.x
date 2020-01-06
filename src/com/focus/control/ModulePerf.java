package com.focus.control;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import com.focus.util.Tools;

/**
 * 程序模块的运行对象
 * @author think
 *
 */
public class ModulePerf implements java.io.Serializable
{
	private static final long serialVersionUID = -2295402330667237427L;
	/*模块id*/
    private String id;
    /*模块名称*/
    private String name;
    /*模块重启时间间隔*/
    private int restartInterval;
    /*模块的版本号*/
    private String version = null;
    /*关闭子程序的文本*/
    private String shutdownText;
    /*关闭子程序的二进制码流*/
    private String shutdownBinary;
    /*关闭子程序的端口*/
    private ArrayList<String> startupCommands;
    /*关闭子程序的端口*/
    private ArrayList<String> shutdownCommands;
    /*模块依赖*/
    private String dependence;
    /*暂停模块执行*/
    private boolean suspend = false;
    /*模块开始时间戳*/
    private Date statupTime;
    /*延迟启动时间*/
    private int delayedStartInterval;
    //是否调试信息输出
    private boolean debug = false;
    // 模块可以运行的最早时间
    private String startTime;
    // 模块可以运行的最晚时间
    private String endTime;
    // 模块在到达可以运行最晚时间后，如果还在运行，是否需要关闭（通常是强行杀掉）。
    private boolean isShutdownRunning = false;
    //主备模块0表示非主备模块，1表示备份模块，2表示主备都适用的模块（比如监控模块、数据库模块）
    private int standby; 
    //监控信息
    private String monitorInfo;//模块监控信息
    //模块状态
    private int state = -1;
    //模块日志路径
    private File moduleLogPath;
    //模块描述
    private String remark;
    //内存使用情况
    private String usageMemory;
    //模块运行时间
    private String runtime;
    //记忆内存数据
    private LinkedList<ModuleMemeory> memories = new LinkedList<ModuleMemeory>();
    //模块负责程序员
    private String programmer;
    //模块负责程序员的联系方式
    private String programmerContact;
    //配置文件路径
    private String cfgfile;
    //程序运行的累积时间（CPU）
    private long cpuused;
    //程序PID
    private String pid;
    //死进程标识
    private String dead;
    //网络状态
    private String netstat;
    //程序类型
    private int type;//0缺省，1java，2?
    //进程个数
    private int pcount;
    //是否是守护进程
    private boolean daemon;
    //异常计数
    private int countException;
    //打印的异常信息
    private String printException;

	public synchronized void addModuleMemeory(ModuleMemeory memory)
    {
    	while( memories.size() > 360 )
    	{
    		memories.pop();
    	}
    	memories.addLast(memory);
    }
    
    public String[] getModuleLogFiles()
    {
    	if( moduleLogPath != null )
    	{
    		File files[] = moduleLogPath.listFiles();
    		String args[] = new String[files.length];
    		int i = 0;
    		for( File file : files )
    		{
    			args[i++] = id+"/"+file.getName();
    		}
    	}
    	String args[] = new String[1];
    	args[0] = id;
    	return args;
    }
    
	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public int getRestartInterval()
	{
		return restartInterval;
	}

	public void setRestartInterval(int restartInterval)
	{
		this.restartInterval = restartInterval;
	}

	public String getVersion()
	{
		if( version != null && !version.equals("unknonwn") )
		{
			version = version.trim().toLowerCase();
			for(int i = 0; i < version.length(); i++)
			{
				char c = version.charAt(i);
				if( c >= '0' && c <= '9' )
				{
					version = version.substring(i);
					break;
				}
			}
			String args[] = Tools.split(version, ".");
			String v = "";
			if( args.length > 0 ) v = args[0];
			else v = "0";
			if( args.length > 1 ) v += "."+args[1];
			else v += ".0";
			if( args.length > 2 ) v += "."+args[2];
			else v += ".0";
			if( args.length > 3 ) v += "."+args[3];
			else v += ".0";
			return v;
		}
		return "0.0.0.0";
	}

	public void setVersion(String version)
	{
		this.version = version;
	}

	public String getShutdownText()
	{
		return shutdownText;
	}

	public void setShutdownText(String shutdownText)
	{
		this.shutdownText = shutdownText;
	}

	public String getShutdownBinary()
	{
		return shutdownBinary;
	}

	public void setShutdownBinary(String shutdownBinary)
	{
		this.shutdownBinary = shutdownBinary;
	}

	public ArrayList<String> getStartupCommands()
	{
		return startupCommands;
	}

	public void setStartupCommands(ArrayList<String> startupCommands)
	{
		this.startupCommands = startupCommands;
	}

	public ArrayList<String> getShutdownCommands()
	{
		return shutdownCommands;
	}

	public void setShutdownCommands(ArrayList<String> shutdownCommands)
	{
		this.shutdownCommands = shutdownCommands;
	}

	public String getDependence()
	{
		return dependence;
	}

	public void setDependence(String dependence)
	{
		this.dependence = dependence;
	}

	public boolean isSuspend()
	{
		return suspend;
	}

	public void setSuspend(boolean suspend)
	{
		this.suspend = suspend;
	}

	public int getDelayedStartInterval()
	{
		return delayedStartInterval;
	}

	public void setDelayedStartInterval(int delayedStartInterval)
	{
		this.delayedStartInterval = delayedStartInterval;
	}

	public boolean isDebug()
	{
		return debug;
	}

	public void setDebug(boolean debug)
	{
		this.debug = debug;
	}

	public String getStartTime()
	{
		return startTime;
	}
	
	public String getStartupTime()
	{
		if( statupTime == null )
		{
			return "N/A";
		}
		final SimpleDateFormat SDF = new SimpleDateFormat("MM-dd HH:mm:ss");
		return SDF.format(this.statupTime);
	}

	public void setStartTime(String startTime)
	{
		this.startTime = startTime;
	}

	public String getEndTime()
	{
		return endTime;
	}

	public void setEndTime(String endTime)
	{
		this.endTime = endTime;
	}

	public boolean isShutDownRunning()
	{
		return isShutdownRunning;
	}

	public void setShutdownRunning(boolean isShutdownRunning)
	{
		this.isShutdownRunning = isShutdownRunning;
	}

	public int getStandby()
	{
		return standby;
	}

	public void setStandby(int standby)
	{
		this.standby = standby;
	}

	public String getMonitorInfo()
	{
		return monitorInfo;
	}

	public void setMonitorInfo(String monitorInfo)
	{
		this.monitorInfo = monitorInfo;
	}

	public int getState()
	{
		return state;
	}

	public void setState(int state)
	{
		this.state = state;
	}

	public Date getStatupTime()
	{
		return statupTime;
	}
	
	public Date getStartupDate()
	{
		return statupTime;
	}

	public long getStartupTimestamp()
	{
		return statupTime!=null?statupTime.getTime():0;
	}
	
	public void setStartupTime(Date startupTime)
	{
		this.statupTime = startupTime;
	}
	
	public void setModuleLogPath(File moduleLogPath)
	{
		this.moduleLogPath = moduleLogPath;
	}

	public String getRemark()
	{
		return remark!=null?remark:"N/A";
	}

	public void setRemark(String remark)
	{
		this.remark = remark;
	}

	public String getUsageMemory()
	{
		return usageMemory;
	}

	public void setUsageMemory(String usageMemory)
	{
		this.usageMemory = usageMemory;
	}

	public String getRuntime()
	{
		return runtime;
	}
	
	public long getCpuused() {
		return cpuused;
	}

	public void setRuntime(int runtime)
	{
		cpuused = runtime;
		int s = runtime;
		if( runtime < 60 )
		{
			this.runtime = "Run "+s+"s";
		}
		else if( runtime < 60*60 )
		{
			int m = s/60;
			s = s%60;
			this.runtime = "Run "+m+"m,"+s+"s";
		}
		else if( runtime < 24*60*60 )
		{
			int h = s/3600;
			int m = s%3600/60;
			this.runtime = "Run "+h+"h,"+m+"s";
		}
		else if( runtime >= 24*60*60 )
		{
			int d = s/(24*3600);
			int h = s%(24*3600)/3600;
			this.runtime = "Run "+d+"d,"+h+"h";
		}
	}

	public void setRuntime(long runtime)
	{
		cpuused = runtime;
		long s = runtime;
		if( runtime < 60000 )
		{
			this.runtime = "Run "+s/1000+"s";
		}
		else if( runtime < 60*60 )
		{
			long m = s/60000;
			s = s%60000;
			this.runtime = "Run "+m+"m,"+s+"s";
		}
		else if( runtime < 24*60*60000 )
		{
			long h = s/3600000;
			long m = s%3600/60000;
			this.runtime = "Run "+h+"h,"+m+"s";
		}
		else if( runtime >= 24*60*60 )
		{
			long d = s/(24*3600000);
			long h = s%24;
			this.runtime = "Run "+d+"d,"+h+"h";
		}
	}

	public List<ModuleMemeory> getMemories()
	{
		return memories;
	}

	public String getProgrammer()
	{
		return programmer==null?"超级管理员":programmer;
	}

	public void setProgrammer(String programmer)
	{
		this.programmer = programmer;
	}

	public File getModuleLogPath()
	{
		return moduleLogPath;
	}

	public String getProgrammerContact()
	{
		return programmerContact;
	}

	public void setProgrammerContact(String programmerContact)
	{
		this.programmerContact = programmerContact;
	}

	public String getCfgfile() {
		return cfgfile;
	}

	public void setCfgfile(String cfgfile) {
		this.cfgfile = cfgfile;
	}
    
    public String getPid() {
		return pid;
	}

	public void setProcessInfo(String pid, int count) {
		this.pid = pid;
		this.pcount = count;
	}

	public String getDead() {
		return dead;
	}

	public void setDead(String dead) {
		this.dead = dead;
	}

	public String getNetstat() {
		return netstat;
	}

	public void setNetstat(String netstat) {
		this.netstat = netstat;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getPcount() {
		return pcount;
	}

	public boolean isDaemon() {
		return daemon;
	}

	public void setDaemon(boolean daemon) {
		this.daemon = daemon;
	}

	public int getCountException() {
		return countException;
	}

	public void setCountException(int countException) {
		this.countException = countException;
	}

	public String getPrintException() {
		return printException;
	}

	public void setPrintException(String printException) {
		this.printException = printException;
	}
}
