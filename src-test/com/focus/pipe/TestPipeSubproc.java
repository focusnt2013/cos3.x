package com.focus.pipe;

import java.io.DataInputStream;
import java.io.IOException;

import com.focus.util.Log;

public class TestPipeSubproc extends Thread
{
	private Log log = null;
	
	public static void main(String args[])
	{
		TestPipeSubproc proc = new TestPipeSubproc(args[0]);
		proc.start();
	}
	
	public TestPipeSubproc(String subroot)
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
        DataInputStream ois = new DataInputStream(System.in);
		try
		{
            TestWritable obj = new TestWritable();
            while(true) 
            {
            	obj.readFields(ois);
            	log.info(obj, obj.toString());
            }  
		}
		catch (Exception e)
		{
			Log.err(e);
		}
		finally
		{
			if( ois != null )
				try
				{
					ois.close();
				}
				catch (IOException e)
				{
				}
		}
		log.warn("Finish to receive data from pipe.");
		log.close();
	}
}
