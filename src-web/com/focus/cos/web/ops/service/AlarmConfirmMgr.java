package com.focus.cos.web.ops.service;

import java.util.LinkedList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.focus.cos.web.ops.dao.SysalarmDAO;
import com.focus.cos.web.ops.vo.Sysalarm;

public class AlarmConfirmMgr extends Thread
{
	private SysalarmDAO sysalarmDao;
	private boolean running = true;
	private static final Log log = LogFactory.getLog(AlarmConfirmMgr.class);
	LinkedList<Sysalarm> confirmList = new LinkedList<Sysalarm>();
	
	public synchronized void close()
	{
		running = false;
		this.notify();
	}	
	
	public synchronized void addConfirm(Sysalarm alarm)
	{
		confirmList.push(alarm);
		if(!this.isAlive())
		{
			this.start();
		}
		this.notify();
	}	
	
	public void run()
	{
		Sysalarm alarm  = null;
		while(running)
		{
			synchronized(this)
			{
				if(this.confirmList.isEmpty())
				{
					try
			        {
			            this.wait();
			            continue;
			        }
			        catch( InterruptedException e )
			        {}
				}
				else
				{
					alarm = confirmList.poll();
				}
			}
			try
			{
			if(alarm == null)
			{
				continue;
			}
			sysalarmDao.update(alarm);
//			log.info("Succeed to confirm alarm "+alarm.getAlarmTitle()+" from "+alarm.getDn()+" "+alarm.getModule());			
			}
			catch(Exception e)
			{
				log.error("Failed to confirm alarm: "+e);
			}
		}
		log.error("AlarmConfirmMgr quit");
	}

	public SysalarmDAO getSysalarmDao() {
		return sysalarmDao;
	}

	public void setSysalarmDao(SysalarmDAO sysalarmDao) {
		this.sysalarmDao = sysalarmDao;
	}	
}
