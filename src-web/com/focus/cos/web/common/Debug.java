package com.focus.cos.web.common;

import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.focus.cos.control.Command;
import com.focus.cos.web.common.SSH;
import com.focus.util.Tools;

/**
 * 调用cos后台的Debug功能
 * @author Focus
 *
 */
public abstract class Debug extends Thread
{
	public static final Log log = LogFactory.getLog(SSH.class);
	public static final int SATE_CONNECTING = 0;
	public static final int SATE_CONNECT = 1;
	public static final int SATE_DISCONNECT = 2;
	private LinkedList<String> buffer = new LinkedList<String>();
	private String cosHost;
	private int cosPort;
//	private OutputStream out = null;
	private InputStream is = null; 
	private ServerSocket ss = null;
	private Socket socket = null;
	private byte[] payload = new byte[64*1024];
	int offset = 0;
	private String module = null;
	private int status;//0等待连接、1连接、2断开
	private int maxBufferSize = 100;
	
	public Debug( String host, int port, String id)
	{
		this.module = id;
		this.cosHost = host;
		this.cosPort = port;
		this.start();
	}
	
	public abstract void connect();
	public abstract void disconnect(Exception e);
	public abstract void receive(String line);
	public abstract void executed();//执行完一个指令后调用
	
	public synchronized void waitDisconnect()
	{
		while( status != SATE_DISCONNECT )
		try
		{
			this.wait(300);
		}
		catch (InterruptedException e)
		{
		}
	}
	
	public synchronized void waitConnect()
	{
		while( status == SATE_CONNECTING )
		try
		{
			this.wait(300);
		}
		catch (InterruptedException e)
		{
		}
	}
	
	public void run()
	{
    	DatagramSocket datagramSocket = null;
    	payload[offset++] = Command.CONTROL_DEBUG;//CONTROL_SSH;
    	payload[offset++] = (byte)this.module.length();//文件路径
    	offset = Tools.copyByteArray(this.module.getBytes(), payload, offset);
    	try
    	{
        	datagramSocket = new DatagramSocket();
            datagramSocket.setSoTimeout(3000);
            DatagramPacket request = new DatagramPacket(payload, 0, offset, InetAddress.getByName( cosHost ), cosPort);
            datagramSocket.send( request );
            int port = datagramSocket.getLocalPort();
            ss = new ServerSocket(port);
            ss.setSoTimeout(10000);
            socket = ss.accept();
            log.info("Succeed to open debug from port "+port);
            socket.getInputStream();
//            out = socket.getOutputStream();
            is = socket.getInputStream();
        	synchronized(this)
        	{
        		status = 1;
				this.notifyAll();
				this.connect();
        	}
			String line;
			int ch = 0;
			offset = 0;
    		while( (ch = is.read()) != -1  )
    		{
    			if( ch == '\n' )
    			{
    				if( offset > 0 )
    				{
	    				line = new String(payload, 0, offset);
	    				if(!line.trim().isEmpty())
	    				{
	    					receive(line);//接收上条命令返回结果
			            	buffer.add(line);
			            	if (buffer.size() > maxBufferSize) {
			            		buffer.poll();
			            	}
	    				}
		            	offset = 0;
    				}
    			}
    			else if( ch != '\r' && ch != '\n' )
    			{
    				payload[offset++] = (byte)ch;
    			}
    		}
    		is.close();
        }
        catch(Exception e)
        {
        	log.error("Disconnect debug "+this.toString()+":"+ e);
            disconnect(e);
        }
        finally
        {
        	synchronized(this)
        	{
	    		try
				{
	    			if( datagramSocket != null ) datagramSocket.close();
	            	if( ss != null ) ss.close();
	            	if( socket != null ) socket.close();
				}
				catch (Exception e)
				{
				}
				status = 2;
				this.notifyAll();
			}
        }
	}
	
	public synchronized void close()
	{
		try
		{
        	if( socket != null ) socket.close();
        	buffer.clear();
        	socket = null;
		}
		catch (IOException e)
		{
		}
	}


	public void receive(StringBuffer result)
	{
		for(String line : buffer )
		{
			result.append(line);
			result.append("<br/>");
		}
		if( offset > 0 )
		{
			result.append(new String(payload, 0, offset));
			result.append("<br/>");
		}
	}
	
	public void receive(DebugResponse response)
	{
		response.setModule(module);
		response.setHost(cosHost);
//		System.out.println(response.getOffset()+":"+buffer.size());
		for( int i = response.getOffset(); i < buffer.size(); i++ )
		{
			String line = buffer.get(i);
			response.getMessages().add(line);
		}
		response.setOffset(buffer.size());
	}
	
	public String getHost()
	{
		return cosHost;
	}

	public int getPort()
	{
		return cosPort;
	}

	public String toString()
	{
		return cosHost+":"+cosPort+" "+this.module;
	}
	
	public int getStatus()
	{
		return status;
	}

	public void setMaxBufferSize(int maxBufferSize) {
		this.maxBufferSize = maxBufferSize;
	}
}
