package com.focus.control;

import java.util.Date;

public class ModuleMemeory implements java.io.Serializable
{
	private static final long serialVersionUID = -5348228880181502149L;
	private long id;//ID
	private long runtime;//采集时间点
	private String host;
	private String module;//模块
	private int gccount;//GC的次数
	private int fullgccount;//全GC的次数
	private int hb;//GC 之前堆的数量 
	private int he;//GC 之后使用的堆数量 
	private int hc;//堆空间的总量
	private double dsum;//累加的持续时间
	private double davg;//平均GC时间
	private double dmax;//最大GC时间
	private double dmin;//最小GC时间
	private boolean begin;//进程开始
	
	public ModuleMemeory(String module, String host)
	{
		this.module = module;
		this.host = host;
	}
	public ModuleMemeory(ModuleMemeory old)
	{
		if( old != null ){
			this.module = old.module;
			this.host = old.host;
			this.hc = old.hc;
			this.he = old.he;
		}
	}
	public long getId()
	{
		return id;
	}
	public void setId(long id)
	{
		this.id = id;
	}
	public Date getGctime()
	{
		return new Date(runtime);
	}
	public void setRuntime(long runtime)
	{
		this.runtime = runtime;
	}
	public String getHost()
	{
		return host;
	}
	public void setHost(String host)
	{
		this.host = host;
	}
	public String getModule()
	{
		return module;
	}
	public void setModule(String module)
	{
		this.module = module;
	}
	public int getGccount()
	{
		return gccount;
	}
	public void plusGccount()
	{
		this.gccount += 1;
	}
	public int getFullgccount()
	{
		return fullgccount;
	}
	public void plusFullgccount()
	{
		this.fullgccount += 1;
	}
	public int getHb()
	{
		return hb;
	}
	public void setHb(int hb)
	{
		if( this.hb == 0 )
		{
			this.hb = hb;
		}
	}
	public int getHe()
	{
		return he;
	}
	public void setHe(int he)
	{
		this.he = he;
	}
	public int getHc()
	{
		return hc;
	}
	public void setHc(int hc)
	{
		this.hc = hc;
	}
	public double getDmax()
	{
		return dmax;
	}
	public void setDmax(double dmax)
	{
		if( dmax > this.dmax )
		{
			this.dmax = dmax;
		}
	}
	public double getDmin()
	{
		return dmin;
	}
	public void setDmin(double dmin)
	{
		if( dmin < this.dmin || this.dmin == 0 )
		{
			this.dmin = dmin;
		}
	}
	public long getRuntime()
	{
		return runtime;
	}
	public double getDsum()
	{
		return dsum;
	}
	public void setDsum(double dsum)
	{
		this.dsum += dsum;
		this.davg = this.dsum/this.gccount;
	}
	public double getDavg()
	{
		return davg;
	}

	public boolean isBegin() {
		return begin;
	}
	public void setBegin(boolean begin) {
		this.begin = begin;
	}
	
	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		sb.append("GC(");
		sb.append(module);
		sb.append(",code:");
		sb.append(id);
		sb.append(",gc:");
		sb.append(this.gccount);
		sb.append(",fullgc:");
		sb.append(this.fullgccount);
		sb.append(",hb:");
		sb.append(this.hb);
		sb.append(",he:");
		sb.append(this.he);
		sb.append(",hc:");
		sb.append(this.hc);
		sb.append(",dsum:");
		sb.append(this.dsum);
		sb.append(",davg:");
		sb.append(this.davg);
		sb.append(",dmax:");
		sb.append(this.dmax);
		sb.append(",dmin:");
		sb.append(this.dmin);
		sb.append(").");
		return sb.toString();
	}
}
