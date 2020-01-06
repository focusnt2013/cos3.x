package com.focus.pipe;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.util.LinkedList;
import java.util.Random;

import com.focus.util.Log;

public class Dispatcher extends Thread
{
	public static void main(String args[])
	{
		Dispatcher d = new Dispatcher();
		d.start();
	}
	
	public void run()
	{
        Log.getInstance().setSubroot( "Pipe/Dispatcher" );
        Log.getInstance().setDebug( true );
        Log.getInstance().setLogable( true );
        Log.getInstance().start();
		Log.msg("Ready to test.");
		Proc proc1 = new Proc("Pipe/TEST1");
		proc1.start();
		Proc proc2 = new Proc("Pipe/TEST2");
		proc2.start();
		Random random = new Random();
		for( int i = 0; i < 100; i++ )
		{
			for( int j = 0; j < 10000; j++ )
			{
				PipeObject obj = new PipeObject();
				obj.id = "PIPE";
				obj.index = i*j;
				obj.cell = i*random.nextInt(10000);
				proc1.write(obj);
				proc2.write(obj);
			}
			try
			{
				Thread.sleep(1000);
			}
			catch (InterruptedException e)
			{
			}
		}
		while(true)
		{
			if(!proc1.queue.isEmpty() )
			{
				synchronized(proc1.queue)
				{
					try
					{
						proc1.queue.wait(1000);
					}
					catch (InterruptedException e)
					{
					}
				}
			}
			else if( !proc2.queue.isEmpty() )
			{
				synchronized(proc2.queue)
				{
					try
					{
						proc2.queue.wait(1000);
					}
					catch (InterruptedException e)
					{
					}
				}
			}
			else
			{
				break;
			}
		}
		proc1.close();
		proc2.close();
		Log.msg("Finish to test.");
		Log.getInstance().close();
	}
	
	class Proc extends Thread
	{
		public static final int STATE_SHUTDOWN = 0;//启动中
		public static final int STATE_STARTUP = 1;//已经启动
		public static final int STATE_SHUTDOWNING = 2;//正在关闭
		private Process sh = null;
		private InputStream sh_reader_err = null;
		private InputStream sh_reader = null;
		private ObjectOutputStream out = null;
		private String moduleId;
		private int state = STATE_SHUTDOWN;
		private LinkedList<Object> queue = new LinkedList<Object>();
		
		public Proc(String id)
		{
			this.moduleId = id;
		}
		
		public void write(Object obj)
		{
			synchronized(queue)
			{
				queue.addLast(obj);
				queue.notify();
			}
			if( queue.size() == 10240 )
			{
				Log.war("Block to pipe("+moduleId+", state="+state+") for quque(size="+queue.size()+").");
			}
		}
		
		/**
		 * 关闭子进程执行
		 */
		public synchronized void close()
		{
			shutdown();
		}
		/**
		 * 关闭子进程执行
		 */
		private void shutdown()
		{
			Log.war("Shutdown pipe("+moduleId+", state="+state+") for quque(size="+queue.size()+").");
			if( sh != null )
			{
				state = STATE_SHUTDOWNING;
				sh.destroy();
			}
			else
			{
				state = STATE_SHUTDOWN;
				Log.err("Failed to shutdown pipe("+this.moduleId+") for proc is null.");
			}
		}
		
		public void run()
		{
			String args[] = new String[5];
			args[0] = "e:/sdk/jdk/bin/java";
			args[1] = "-classpath";
			args[2] = "./cos.jar";
			args[3] = "com.focus.pipe.SubProc";
			args[4] = moduleId;                 
	        try
	        {
	            ProcessBuilder pb = new ProcessBuilder( args );
	            pb.redirectErrorStream( true );
	            sh = pb.start();
	            sh_reader = sh.getInputStream();
	            sh_reader_err = sh.getErrorStream();;
	            out = new ObjectOutputStream(sh.getOutputStream());
	            //从SH读取数据
	            Thread thread = new Thread()
	            {
	            	public void run()
	            	{
	            		try
	            		{
//	            			System.out.println("read sh inputstream:"+sh_reader.available());
	            			int b = 0;
	            			//out.write(sh_reader.available());
	            			while( (b = sh_reader.read() ) != -1 )
	            			{
	            				System.out.write(b);
	            			}
	            		}
	            		catch(Exception e)
	            		{
	            			e.printStackTrace();
	            		}
	            	}
	            };
	            thread.start();
	            //读取SH错误数据
	            thread = new Thread()
	            {
	            	public void run()
	            	{
	            		try
	            		{
	            			int b = 0;
//	            			System.out.println("read sh error inputstream:"+sh_reader_err.available());
	            			while( (b = sh_reader_err.read() ) != -1 )
	            			{
	            				System.out.write(b);
	            			}
	            		}
	            		catch(Exception e)
	            		{
	            			e.printStackTrace();
	            		}
	            	}
	            };
	            thread.start();
	            this.state = STATE_STARTUP;
	            thread = new Thread()
	            {
	            	public void run()
	            	{
	            		Log.msg("Start to write object to pipe("+moduleId+").");
	            		while( state == STATE_STARTUP)
	            		{
	            			if( !queue.isEmpty() )
	            			{
	            				synchronized(queue)
	            				{
		            				Object obj = queue.poll();
				    				try
				    				{
				    					out.writeObject(obj);
				    				}
				    				catch (IOException e)
				    				{
				    					Log.err("Failed to write pipe("+moduleId+") for exception:");
				    					Log.err(e);
				    					shutdown();
				    				}
	            				}
	            			}
	            			else
	            			{
	            				synchronized(queue)
	            				{
	            					try
									{
	            						queue.wait(1000);
									}
									catch (InterruptedException e){
									}
	            				}
	            			}
	            		}
        				synchronized(queue)
        				{
        					queue.notify();
        				}
	            		Log.msg("Finish to write object to pipe("+moduleId+").");
	            	}
	            };
	            thread.start();
	        	Log.msg("Succeed to setup proc-pipe("+this.moduleId+",quque="+queue.size()+").");
	    		int status = sh.waitFor(); //等待执行完成
	    		Log.msg("waitFor() = "+status);
	        }
	        catch (Exception e)
	        {
	        	Log.err("Failed to execute proc:");
	        	Log.err(e);
	        }
	        finally
	        {
	            this.state = STATE_SHUTDOWN;
	        	if( sh_reader != null )
	                try
	                {
	                	sh_reader.close();
	                }
	                catch (Exception e)
	                {
	                }

	        	if( sh_reader_err != null )
	                try
	                {
	                	sh_reader_err.close();
	                }
	                catch (Exception e)
	                {
	                }
	        	if( sh != null )
	                try
	                {
	                	sh.destroy();
	                }
	                catch (Exception e)
	                {
	                }
	                sh = null;
	        }
        	Log.msg("Finish to execute proc("+this.moduleId+").");
		}		
	}
}
