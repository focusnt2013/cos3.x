package com.focus.cos.control;

public class CpuStat implements java.io.Serializable
{
    private static final long serialVersionUID = 2619672878906831325L;
    public long user; //从系统启动开始累计到当前时刻用户态的CPU时间
    public long nice; //从系统启动开始累计到当前时刻负的进程所占用的CPU时间
    public long system; //从系统启动开始累计到当前时刻,核心时间
    public long idle; //从系统启动开始累计到当前时刻，除硬盘IO等待时间以外其它等待时间
    public long iowait; //从系统启动开始累计到当前时刻，硬盘IO等待时间
    public long irq; //从系统启动开始累计到当前时刻，硬中断时间
    public long softirq; //从系统启动开始累计到当前时刻，软中断时间
//    public long ctxt;//给出了自系统启动以来CPU发生的上下文交换的次数
    public long time; //给出了从系统启动到现在为止的时间
//    public long processes;//自系统启动以来所创建的任务的个数目
//    public long procs_running;//当前运行队列的任务的数目
//    public long procs_blocked;//当前被阻塞的任务的数目
	public long getUser()
	{
		return user;
	}
	public void setUser(long user)
	{
		this.user = user;
	}
	public long getNice()
	{
		return nice;
	}
	public void setNice(long nice)
	{
		this.nice = nice;
	}
	public long getSystem()
	{
		return system;
	}
	public void setSystem(long system)
	{
		this.system = system;
	}
	public long getIdle()
	{
		return idle;
	}
	public void setIdle(long idle)
	{
		this.idle = idle;
	}
	public long getIowait()
	{
		return iowait;
	}
	public void setIowait(long iowait)
	{
		this.iowait = iowait;
	}
	public long getIrq()
	{
		return irq;
	}
	public void setIrq(long irq)
	{
		this.irq = irq;
	}
	public long getSoftirq()
	{
		return softirq;
	}
	public void setSoftirq(long softirq)
	{
		this.softirq = softirq;
	}
	public long getTime()
	{
		return time;
	}
	public void setTime(long time)
	{
		this.time = time;
	}
}
