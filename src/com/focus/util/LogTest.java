package com.focus.util;

public class LogTest
{
	public static void main(String args[])
	{
		Thread thread  = new Thread()
		{
			public void run()
			{
				Log.getInstance().setDebug(false);
				Log.getInstance().setLogable(true);
				Log.getInstance().setSubroot("LogTest");
				Log.getInstance().start();
				
				for(int i = 1; i <= 10000; i++ )
				{
					Log.msg("测试["+i+"].");
					if( i%1000 == 0 )
					{
						try
						{
							System.out.println("写了"+i+"条记录。");
							Thread.sleep(1000);
						}
						catch (InterruptedException e)
						{
						}
					}
				}
				
				System.out.println("线程结束，进程将结束，判断日志模块是否自动释放，数据是否全部写完。");
			}
		};
		thread.start();
	}
}
