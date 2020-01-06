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
					Log.msg("����["+i+"].");
					if( i%1000 == 0 )
					{
						try
						{
							System.out.println("д��"+i+"����¼��");
							Thread.sleep(1000);
						}
						catch (InterruptedException e)
						{
						}
					}
				}
				
				System.out.println("�߳̽��������̽��������ж���־ģ���Ƿ��Զ��ͷţ������Ƿ�ȫ��д�ꡣ");
			}
		};
		thread.start();
	}
}
