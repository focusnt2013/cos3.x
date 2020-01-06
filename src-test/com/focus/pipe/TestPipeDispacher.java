package com.focus.pipe;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

import com.focus.util.Log;
import com.focus.util.Tools;

public class TestPipeDispacher extends Thread
{
	//子进程启动指令
	private static ArrayList<String> Commands = new ArrayList<String>();
	private int procSize = 1;
	private ArrayList<Proc> procPool = new ArrayList<Proc>();
	
	public static void main(String args[])
	{
		TestPipeDispacher d = new TestPipeDispacher();
		if( args.length > 0 )
		{
			d.procSize = Integer.parseInt(args[0]);
		}
		d.start();
		for( int i = 1; i < args.length; i++ )
		{
			Commands.add(args[i]);
		}
	}
	
	public void run()
	{
        Log.getInstance().setSubroot( "TestPipe" );
        Log.getInstance().setDebug( true );
        Log.getInstance().setLogable( true );
        Log.getInstance().start();
		Log.msg("Ready to test.");
		for( int i = 0; i < procSize; i++ )
		{
			Proc proc0 = new Proc("TestPipeSubproc"+i);
			proc0.setDaemon(true);
			proc0.start();
			procPool.add(proc0);
		}
		Random random = new Random();
		int i = 0;
		while( true )
		{
			for( int j = 0; j < 10000; j++ )
			{
				TestWritable writable = new TestWritable();
				writable.setP0((byte)i);
				writable.setP1((short)j);
				writable.setP2(i*j);
				writable.setP3(writable.getP2()*random.nextInt(100));
				writable.setP4("PIPE.TEST");
				for( Proc proc : procPool )
				{
					proc.write(writable);
				}				
			}
			try
			{
				i += 1;
				Thread.sleep(1000);
			}
			catch (InterruptedException e)
			{
			}
		}
	}
	
	class Proc extends Thread
	{
		public static final int STATE_SHUTDOWN = 0;//启动中
		public static final int STATE_STARTUP = 1;//已经启动
		public static final int STATE_SHUTDOWNING = 2;//正在关闭
		private Process sh = null;
		private InputStream sh_reader_err = null;
		private InputStream sh_reader = null;
		private DataOutputStream out = null;
		private String moduleId;
		private int state = STATE_SHUTDOWN;
		private LinkedList<Writable> queue = new LinkedList<Writable>();
		
		public Proc(String id)
		{
			this.moduleId = id;
		}
		
		public void write(Writable obj)
		{
			if( queue.size() == 10240 )
			{
				Log.war("Block to pipe("+moduleId+", state="+state+") for quque(size="+queue.size()+").");
				return;
			}
			synchronized(queue)
			{
				queue.addLast(obj);
				queue.notify();
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
			ArrayList<String> commands = new ArrayList<String>();
			if( Commands.isEmpty() )
			{
				commands.add("e:/sdk/jdk/bin/java");
				commands.add("-Xloggc:log/"+moduleId+"/"+Tools.getFormatTime("yyyyMMddHHmmss", System.currentTimeMillis())+".gc");
				commands.add("-Xms32m");
				commands.add("-Xmx512m");
				commands.add("-classpath");
				commands.add("./cos.jar");
			}
			else
			{
				for(String command : Commands )
				{
					if( command.startsWith("-Xloggc:") )
					{
						String dirPath = command.substring(8);
						dirPath += moduleId+"/";
						File dir = new File(dirPath);
						if( !dir.exists() )
						{
							dir.mkdirs();
						}
						command += Tools.getFormatTime("yyyyMMddHHmmss", System.currentTimeMillis())+".gc";
					}
					commands.add(command);
				}
			}
			commands.add("com.focus.pipe.TestPipeSubproc");
			commands.add(moduleId);
	        try
	        {
	            ProcessBuilder pb = new ProcessBuilder( commands );
	            pb.redirectErrorStream( true );
	            sh = pb.start();
	            sh_reader = sh.getInputStream();
	            sh_reader_err = sh.getErrorStream();
	            out = new DataOutputStream(sh.getOutputStream());
	            //从SH读取数据
	            Thread thread = new Thread()
	            {
	            	public void run()
	            	{
	            		try
	            		{
	            			while( sh_reader.read() != -1 )
	            			{
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
	            			while( sh_reader_err.read() != -1 )
	            			{
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
	            					Writable obj = queue.poll();
				    				try
				    				{
				    					obj.writeFields(out);
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
	        	Log.msg(commands);
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
