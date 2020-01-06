package com.focus.control;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Date;

public class HostPerf implements java.io.Serializable
{
	private static final long serialVersionUID = -3877597797984739810L;
	private long id;
	private String host;
	private Date perfTime;
	private int cpuLoad;
	private String cpuLoadInfo;
	private int phyMemUsage;
	private String phyMemUsageInof;
	private long phyMemUsed;
	private int diskUsage;
	private String diskUsageInfo;
	private long diskUsed;
	private long sysUpTime;
	private String filesOpen;
	private String systemUpTime;
    private DecimalFormat df = (DecimalFormat)NumberFormat.getPercentInstance(); 
	private long phyMemTotal;
    
    public HostPerf()
    {
    	df.applyPattern("##,##.00%");
    }
    
	public long getId()
	{
		return id;
	}
	public void setId(long id)
	{
		this.id = id;
	}
	public String getHost()
	{
		return host;
	}
	public void setHost(String host)
	{
		this.host = host;
	}
	public Date getPerfTime()
	{
		return perfTime;
	}
	public void setPerfTime(Date perfTime)
	{
		this.perfTime = perfTime;
	}
	public int getCpuLoad()
	{
		return cpuLoad;
	}
	public void setCpuLoad(int cpuLoad)
	{
		this.cpuLoad = cpuLoad;
	}
	public int getPhyMemUsage()
	{
		return phyMemUsage;
	}
	public void setPhyMemUsage(int phyMemUsage)
	{
		this.phyMemUsage = phyMemUsage;
	}
	public long getPhyMemUsed()
	{
		return phyMemUsed;
	}
	public void setPhyMemUsed(long phyMemUsed)
	{
		this.phyMemUsed = phyMemUsed;
	}
	public int getDiskUsage()
	{
		return diskUsage;
	}
	public void setDiskUsage(int diskUsage)
	{
		this.diskUsage = diskUsage;
	}
	public long getDiskUsed()
	{
		return diskUsed;
	}
	public void setDiskUsed(long diskUsed)
	{
		this.diskUsed = diskUsed;
	}
	public long getSysUpTime()
	{
		return sysUpTime;
	}
	public void setSysUpTime(long sysUpTime)
	{
		this.sysUpTime = sysUpTime;
	}
	public String getFilesOpen()
	{
		return filesOpen;
	}
	public void setFilesOpen(String filesOpen)
	{
		this.filesOpen = filesOpen;
	}
	public String getCpuLoadInfo()
	{
		return cpuLoadInfo;
	}
	public void setCpuLoadInfo(double cpuLoadInfo)
	{
		this.cpuLoadInfo = df.format(cpuLoadInfo);
	}
	public String getPhyMemUsageInof()
	{
		return phyMemUsageInof;
	}
	public void setPhyMemUsageInof(double phyMemUsageInof)
	{
		this.phyMemUsageInof = df.format(phyMemUsageInof);
	}
	public String getDiskUsageInfo()
	{
		return diskUsageInfo;
	}
	public void setDiskUsageInfo(double diskUsageInfo)
	{
		this.diskUsageInfo = df.format(diskUsageInfo);
	}

	public String getSystemUpTime()
	{
		return systemUpTime;
	}

	public void setSystemUpTime(String systemUpTime)
	{
		this.systemUpTime = systemUpTime;
	}

	public long getPhyMemTotal()
	{
		return phyMemTotal;
	}
}
