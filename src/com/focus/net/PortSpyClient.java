package com.focus.net;

import java.io.IOException;
import java.net.Socket;

import com.focus.util.Log;

public abstract class PortSpyClient extends Thread
{
	private String ip;
	private int port;
	public PortSpyClient(String ip, int port)
	{
		this.ip = ip;
		this.port = port;
	}
	public static Integer countPortSpyClient;
	public static int soTimeout = 300000;
	public abstract void count();
	
	public static void main(String args[])
	{
		String ip = args[0];
		int beginPort = Integer.parseInt(args[1]);
		int endPort = Integer.parseInt(args[2]);
        Log.getInstance().setSubroot("PortSpyClient");
        Log.getInstance().setDebug(true);
        Log.getInstance().setLogable(true);
        Log.getInstance().start();
        
        Log.msg("Start spy.");
        countPortSpyClient = endPort - beginPort;
		for( int port = beginPort; port < endPort; port ++  )
		{
			PortSpyClient portSpyClient = new PortSpyClient(ip, port){
				public void count()
				{
					synchronized( countPortSpyClient )
					{
						countPortSpyClient -= 1;
					}
				}
	        };
	        portSpyClient.start();
		}
		Log.msg("Finish start spyer("+countPortSpyClient+")");
		if( countPortSpyClient == 0 )
		{
			Log.msg("Finish spy.");
		}
	}
	
	public void run()
	{
		Socket socket = null;
		try
		{
			byte[] paylaod = new byte[256];
			socket = new Socket(ip, port);
			socket.getOutputStream().write(("The port("+port+") is valid.").getBytes());
			int len = socket.getInputStream().read(paylaod);
			Log.msg(new String(paylaod, 0, len));
		}
		catch(Exception e)
		{
			Log.msg("The port("+port+") is invalid.");
		}
		finally
		{
			count();
			if( socket != null )
				try
				{
					socket.close();
				}
				catch (IOException e)
				{
				}
		}
	}
}
