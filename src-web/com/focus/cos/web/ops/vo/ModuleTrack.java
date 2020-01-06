package com.focus.cos.web.ops.vo;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ModuleTrack implements java.io.Serializable
{
	private static final long serialVersionUID = -4628809975829656193L;
	/*模块id*/
    private String id;
    /*模块名称*/
    private String name;
    /*模块的版本号*/
    private String version = null;
    /*模块依赖*/
    private String dependence;
    /*模块开始时间戳*/
    private Date statupTime;
    //是否调试信息输出
    private boolean debug = false;
    // 模块可以运行的最早时间
    private String startTime;
    // 模块可以运行的最晚时间
    private String endTime;
    //主备模块0表示非主备模块，1表示备份模块，2表示主备都适用的模块（比如监控模块、数据库模块）
    private int standby; 
    //监控信息
    private String monitorInfo;//模块监控信息
    //模块状态
    private int state = 0;
    //模块描述
    private String remark;
    //内存使用情况
    private String usageMemory;
    //模块运行时间
    private String runtime;
    //模块负责程序员
    private String programmer;
    //内存参数
    private long virt;
	private long res;
    //配置文件路径
    private String cfgfile;
    //进程ID
    private String pid;
    //死进程标识
    private String dead;
    //网络状态
    private String netstat;
    //程序类型
    private int type;//0缺省，1java，2?
    //是否守护
    private boolean daemon;
    //异常计数
    private int countException;
    //打印的异常信息
    private String printException;
    //过期异常
    private boolean expiredException;

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

	public String getVersion()
	{
		return version;
	}

	public void setVersion(String version)
	{
		this.version = version;
	}

	public String getDependence()
	{
		return dependence;
	}

	public void setDependence(String dependence)
	{
		this.dependence = dependence;
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

	public void setStatupTime(Date statupTime)
	{
		this.statupTime = statupTime;
	}

	public String getRemark()
	{
		if( remark == null )
		{
			return "N/A";
		}
		return remark;
	}

	public void setRemark(String remark)
	{
		this.remark = remark;
	}

	public String getUsageMemory()
	{
		if( usageMemory == null )
		{
			return "N/A";
		}
		return usageMemory;
	}

	public void setUsageMemory(String usageMemory)
	{
		this.usageMemory = usageMemory;
	}

	public String getRuntime()
	{
		if( runtime == null )
		{
			return "N/A";
		}
		return runtime;
	}

	public void setRuntime(String runtime)
	{
		this.runtime = runtime;
	}

	public String getProgrammer()
	{
		return programmer!=null?programmer:"N/A";
	}

	public void setProgrammer(String programmer)
	{
		this.programmer = programmer;
	}
	public long getVirt() {
		return virt;
	}

	public void setVirt(long virt) {
		this.virt = virt;
	}

	public long getRes() {
		return res;
	}

	public void setRes(long res) {
		this.res = res;
	}

	public String getCfgfile() {
		return cfgfile;
	}

	public void setCfgfile(String cfgfile) {
		this.cfgfile = cfgfile!=null?cfgfile:"";
	}

	public String getPid() {
		return pid;
	}

	public void setPid(String pid) {
		this.pid = pid;
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
	
	public boolean isExpiredException() {
		return expiredException;
	}

	public void setExpiredException(boolean expiredException) {
		this.expiredException = expiredException;
	}

}
