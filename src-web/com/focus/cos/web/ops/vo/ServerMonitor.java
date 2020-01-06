package com.focus.cos.web.ops.vo;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import com.focus.control.HostPerfPoint;
import com.focus.cos.web.common.Kit;

/**
 * 服务器监控
 * @author Focus
 *
 */
public class ServerMonitor
{
	private String host;//主机标志
	private HostPerfPoint current;
	private HostPerfPoint oneday;
	private HostPerfPoint sevenday;
	private String busyhours;//忙时

	public void setCurrent(HostPerfPoint current)
	{
		this.current = current;
	}

	public void setOneday(HostPerfPoint oneday)
	{
		this.oneday = oneday;
	}

	public void setSevenday(HostPerfPoint sevenday)
	{
		this.sevenday = sevenday;
	}
	
	public String getCurrentNetload()
	{
		return current==null?"N/A":(Kit.bytesScale(current.getNetload0())+"/s,"+Kit.bytesScale(current.getNetload1())+"/s");
	}

	public String getCurrentNetloadI()
	{
		return current==null?"N/A":(Kit.bytesScale(current.getNetload0())+"/s");
	}

	public String getCurrentNetloadO()
	{
		return current==null?"N/A":(Kit.bytesScale(current.getNetload1())+"/s");
	}

	public String getCurrentCpuload()
	{
		final DecimalFormat df = (DecimalFormat)NumberFormat.getPercentInstance();
		return current==null?"N/A":df.format(((double)current.getCpuload())/10000);
	}

	public String getCurrentMemload()
	{
		final DecimalFormat df = (DecimalFormat)NumberFormat.getPercentInstance();
		return current==null?"N/A":df.format(((double)current.getMemusage())/10000);
	}

	public String getCurrentIOload()
	{
		return current==null?"N/A":(Kit.bytesScale(current.getIoload0())+"/s,"+Kit.bytesScale(current.getIoload0())+"/s");
	}

	public String getCurrentIOloadI()
	{
		return current==null?"N/A":Kit.bytesScale(current.getIoload0())+"/s";
	}

	public String getCurrentIOloadO()
	{
		return current==null?"N/A":Kit.bytesScale(current.getIoload0())+"/s";
	}
	
	public String getCurrentTemperature()
	{
		return "N/A";
	}

	public String getOnedayNetload()
	{
		return oneday==null?"N/A":(Kit.bytesScale(oneday.getNetload0())+"/s,"+Kit.bytesScale(oneday.getNetload1())+"/s");
	}

	public String getOnedayNetloadI()
	{
		return oneday==null?"N/A":(Kit.bytesScale(oneday.getNetload0())+"/s");
	}

	public String getOnedayNetloadO()
	{
		return oneday==null?"N/A":(Kit.bytesScale(oneday.getNetload1())+"/s");
	}

	public String getOnedayCpuload()
	{
		final DecimalFormat df = (DecimalFormat)NumberFormat.getPercentInstance();
		return oneday==null?"N/A":df.format((double)oneday.getCpuload()/10000);
	}

	public String getOnedayMemload()
	{
		final DecimalFormat df = (DecimalFormat)NumberFormat.getPercentInstance();
		return oneday==null?"N/A":df.format(((double)oneday.getMemusage())/10000);
	}

	public String getOnedayIOload()
	{
		return oneday==null?"N/A":(Kit.bytesScale(oneday.getIoload0())+"/s,"+Kit.bytesScale(oneday.getIoload0())+"/s");
	}

	public String getOnedayIOloadI()
	{
		return oneday==null?"N/A":(Kit.bytesScale(oneday.getIoload0())+"/s");
	}

	public String getOnedayIOloadO()
	{
		return oneday==null?"N/A":(Kit.bytesScale(oneday.getIoload0())+"/s");
	}

	public String getOnedayTemperature()
	{
		return "N/A";
	}

	public String getSevendayNetload()
	{
		return sevenday==null?"N/A":(Kit.bytesScale(sevenday.getNetload0())+"/s,"+Kit.bytesScale(sevenday.getNetload1())+"/s");
	}

	public String getSevendayNetloadI()
	{
		return sevenday==null?"N/A":(Kit.bytesScale(sevenday.getNetload0())+"/s");
	}

	public String getSevendayNetloadO()
	{
		return sevenday==null?"N/A":(Kit.bytesScale(sevenday.getNetload1())+"/s");
	}
	
	public String getSevendayCpuload()
	{
		final DecimalFormat df = (DecimalFormat)NumberFormat.getPercentInstance();
		return sevenday==null?"N/A":df.format((double)sevenday.getCpuload()/10000);
	}

	public String getSevendayMemload()
	{
		final DecimalFormat df = (DecimalFormat)NumberFormat.getPercentInstance();
		return sevenday==null?"N/A":df.format(((double)sevenday.getMemusage())/10000);
	}

	public String getSevendayIOload()
	{
		return sevenday==null?"N/A":(Kit.bytesScale(sevenday.getIoload0())+"/s,"+Kit.bytesScale(sevenday.getIoload0())+"/s");
	}
	
	public String getSevendayIOloadI()
	{
		return sevenday==null?"N/A":(Kit.bytesScale(sevenday.getIoload0())+"/s");
	}
	
	public String getSevendayIOloadO()
	{
		return sevenday==null?"N/A":(Kit.bytesScale(sevenday.getIoload0())+"/s");
	}

	public String getSevendayTemperature()
	{
		return "N/A";
	}

	public String getHost()
	{
		return host;
	}

	public void setHost(String host)
	{
		this.host = host;
	}

	public String getBusyhours()
	{
		return busyhours;
	}

	public void setBusyhours(String busyhours)
	{
		this.busyhours = busyhours;
	}
}
