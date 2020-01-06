package com.focus.cos.control;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;

import com.focus.util.Log;
import com.focus.util.Tools;

public class SSH extends Thread
{
	private InetAddress addr;
	private int port;
	private OutputStream out = null;
	private InputStream in = null;
	private Socket socket = null;
	private JSH jsh = null;
	
	SSH( InetAddress addr, int port )
	{
		this.addr = addr;
		this.port = port;
	}
	
	public void run()
	{
		try
		{
			byte[] payload = new byte[10240];
			Thread.sleep(700);
			socket = new Socket(addr, port);
			out = socket.getOutputStream();
			in = socket.getInputStream();
			Log.msg("Succeed to ssh "+addr+":"+port);
    		int ch, offset = 0;
    		while( (ch = in.read()) != -1  )
    		{
    			if( (ch == '\n' || ch == '\r') && offset > 0 )
    			{
    				String command = new String(payload, 0, offset);
    				offset = 0;
    				if( jsh == null )
    				{
    			    	String args[] = command.split(" ");
    			    	StringBuffer sb = new StringBuffer();
    					for( int j = 0; j < args.length; j++ )
    					{
    						if( j > 0 )
    							sb.append(' ');
    						sb.append(args[j]);
    					}
		                Log.msg("Todo exec "+sb);
						jsh = new JSH(out, command){
							public void finish()
							{
				                Log.msg("Finish to exec "+command+".");
				                try
				                {
				        			out.write(0x1B);
				                	out.flush();
				                }
				                catch (Exception e1)
				                {
				                }
				                jsh = null;
							}
							
						};
						jsh.start();
    				}
    				else
    				{
		                Log.msg("Input("+command+").");
    					jsh.write(command);
    				}
    			}
    			else if( ch != '\n' && ch != '\r' )
    			{
    				payload[offset++] = (byte)ch;
    			}
    		}
		}
		catch( Exception e )
		{
			Log.err("Failed to ssh from "+addr+":"+port+" for exception:"+e);
		}
		finally
		{
			Log.msg("Finish to ssh from "+addr+":"+port+".");
    		if( out != null )
    		{
    			try
				{
    				out.close();
    				out = null;
				}
				catch (IOException e)
				{
				}
    		}
    		if( in != null )
    		{
    			try
				{
    				in.close();
    				in = null;
				}
				catch (IOException e)
				{
				}
    		}
        	if( socket != null )
        	{
        		try
        		{
        			socket.close();
    			}
    			catch (Exception e)
    			{
    			}
        	}
		}
	}

    public static void main(String args[])
    {
    	Log.getInstance().setSubroot( "ssh" );
        Log.getInstance().setDebug( true );
        Log.getInstance().setLogable( true );
        Log.getInstance().start();
    	int port = 9081;
    	DatagramSocket datagramSocket = null;
        try
        {
        	byte[] payload = new byte[64*1024];
        	String controlPort = System.getProperty("control.port");
        	if( Tools.isNumeric(controlPort) )
        	{
        		port = Integer.parseInt(controlPort);
        	}
        	datagramSocket = new DatagramSocket( port );
        	Log.msg( "Start control listener by "+port+"." );
            while( true )
            {
            	datagramSocket.setSoTimeout(0);
                DatagramPacket getPacket = new DatagramPacket( payload, payload.length );
                datagramSocket.receive( getPacket );
                byte command = payload[0];
                InetAddress addr0 = getPacket.getAddress();
                int port0 = getPacket.getPort();
                if( command == 22 )
                {
                	SSH ssh = new SSH(addr0, port0);
                	ssh.start();
                }
            }
        }
        catch( Exception e )
        {
            Log.err( e );
        }
        finally
        {
        	if( datagramSocket != null )
        	{
        		datagramSocket.close();
        	}
        }
    }
}
