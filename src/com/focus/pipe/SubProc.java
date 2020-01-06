package com.focus.pipe;

import java.io.ObjectInputStream;

import com.focus.util.Log;

public class SubProc extends Thread
{
	private Log log = null;
	
	public static void main(String args[])
	{
		SubProc proc = new SubProc(args[0]);
		proc.start();
	}
	
	public SubProc(String subroot)
	{
		log = Log.createInstance();
		log.setDebug(false);
		log.setSubroot(subroot);
		log.setLogable(true);
		log.start();
	}
	
	public void run()
	{
		log.info("Begin to receive data from pipe.");
		try
		{
            ObjectInputStream ois = new ObjectInputStream(System.in);
            Object obj = null;
            while((obj=ois.readObject())!=null) 
            {
            	log.info(obj, obj.toString());
            }  
            ois.close();  			
		}
		catch (Exception e)
		{
			Log.err(e);
		}
		log.warn("Finish to receive data from pipe.");
		log.close();
	}
}
