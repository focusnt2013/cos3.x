package com.focus.net;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import com.focus.util.Log;

public abstract class PortSpyServer extends Thread
{
	private int port;
	public PortSpyServer(int port)
	{
		this.port = port;
	}
	public static Integer countPortSpyServer;
	public static int soTimeout = 300000;
	public abstract void count();
	
	public static void main(String args[])
	{
		int beginPort = Integer.parseInt(args[0]);
		int endPort = Integer.parseInt(args[1]);
		soTimeout = Integer.parseInt(args[2]);
        Log.getInstance().setSubroot("PortSpyServer");
        Log.getInstance().setDebug(true);
        Log.getInstance().setLogable(true);
        Log.getInstance().start();
        
        Log.msg("Start spy.");
        countPortSpyServer = endPort - beginPort;
		for( int port = beginPort; port < endPort; port ++  )
		{
	        PortSpyServer portSpyServer = new PortSpyServer(port){
				public void count()
				{
					synchronized( countPortSpyServer )
					{
						countPortSpyServer -= 1;
					}
				}
	        };
	        portSpyServer.start();
		}
		Log.msg("Finish start spyer("+countPortSpyServer+")");
		if( countPortSpyServer == 0 )
		{
			Log.msg("Finish spy.");
		}
	}
	
	public void run()
	{
		Socket socket = null;
		ServerSocket ss = null;
		try
		{
			byte[] paylaod = new byte[256];
			ss = new ServerSocket(port);
			ss.setSoTimeout(soTimeout);
			socket = ss.accept();
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
			if( ss != null )
				try
				{
					ss.close();
				}
				catch (IOException e)
				{
				}
		}
	}
}
