package com.focus.cos.web.ops.service;

import java.util.LinkedList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.focus.cos.web.ops.dao.SysalarmDAO;
import com.focus.cos.web.ops.vo.Sysalarm;

public class AlarmSaveMgr extends Thread
{
	private SysalarmDAO sysalarmDao;
	private boolean running = true;
	private static final Log log = LogFactory.getLog(AlarmSaveMgr.class);
	LinkedList<Sysalarm> saveList = new LinkedList<Sysalarm>();
	
	public synchronized void close()
	{
		running = false;
		this.notify();
	}
	
	public synchronized void addSave(Sysalarm alarm)
	{
		saveList.push(alarm);
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
				if(this.saveList.isEmpty())
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
					alarm = saveList.poll();
				}
			}
			try
			{
			if(alarm == null)
			{
				continue;
			}
			sysalarmDao.save(alarm);
//			log.debug("Succeed to save alarm "+alarm.getAlarmTitle()+" from "+alarm.getDn()+" "+alarm.getModule());			
			}
			catch(Exception e)
			{
				log.error("Failed to save alarm: "+e);
			}
		}
		log.warn("AlarmSaveMgr quit");
	}

	public void setSysalarmDao(SysalarmDAO sysalarmDao) {
		this.sysalarmDao = sysalarmDao;
	}	
}
