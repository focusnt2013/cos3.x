package com.focus.cos.control;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedList;

import com.focus.control.ModulePerf;
import com.focus.util.Log;
import com.focus.util.Tools;

public abstract class SystemMonitorSyncher implements Runnable
{
	static final int SIZE_BUFFER = 10240;
	protected boolean closed;
	protected ServerSocket ss;
	protected OutputStream out;
	protected Socket socket;
	protected String id = "";
	protected StringBuffer logtxt;
	private HashMap<String,Long> count_module = new HashMap<String,Long>();
	private long count_system = 0;
	private long count_heartbeat = 0;
	private long timestamp = 0;//发送时间戳
	/*监控数据队列，依次发送*/
	protected LinkedList<MonitorData> quene = new LinkedList<MonitorData>();
	
	SystemMonitorSyncher( ServerSocket ss, String id, StringBuffer logtxt )
	{
		this.logtxt = logtxt;
		this.ss = ss;
		this.id = id;
	}
	
	public String getId()
	{
		return id!=null?id:"";
	}
	
	/**
	 * 断开连接
	 */
	public void disconnect()
	{
		Log.war("Disconnect SystemMonitorSyncher("+this+").");
		if( out != null )
		{
			try
			{
				out.close();
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
    	try
		{
			ss.close();
		}
		catch (IOException e)
		{
		}
    	closed = true;
    	remove(this);
	}
	
	public abstract void remove(SystemMonitorSyncher syncher);
	
	/**
	 * 关闭
	 */
	public void close(StringBuffer sb)
	{
		sb.append("\r\n\t\t"+toString()+" close...");
		if( out != null )
		{
			try
			{
				out.close();
			}
			catch (Exception e)
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
    	try
		{
			ss.close();
		}
		catch (IOException e)
		{
		}
    	sb.append("\tClosed.");
	}
	
	/**
	 * 执行
	 * @throws Exception
	 */
	public synchronized void execute() throws Exception{
		//执行排队发送监控数据
		MonitorData e = null;
//		Log.print("Found %s data of monitor need to send.", quene.size());
		while((e=quene.poll())!=null){
			this.send(e);
		}
		this.wait(3000);//没有数据3秒钟后再发送
	}

	/**
	 * 发送心跳
	 * @throws Exception
	 */
	public synchronized void heartbeat(String subject)
	{
		if( out == null ) return;
		MonitorData e = new MonitorData();
		e.serializable = subject;
		e.type = -1;
		quene.push(e);
	}
	
//	private HashMap<String, Long> timestampModuleNotify = new HashMap<String, Long>();
	public synchronized void send(Serializable serializable, int type)
	{
		if( out == null ) return;
		if( serializable == null ) return;
		MonitorData e = new MonitorData();
		e.serializable = serializable;
		e.type = type;
		quene.addLast(e);
	}
	
	/**
	 * 向服务器发送监控数据
	 * @param e
	 * @throws Exception
	 */
	private void send(MonitorData e)
	    throws Exception
    {
		timestamp = System.currentTimeMillis();
		ByteArrayOutputStream baos = null;
        try
        {
//    		Log.print("Send the data(type:%s, %s) of monitor.", e.type, e.serializable.toString());
			if( e.type == -1 ){
	        	count_heartbeat += 1;
				//将服务器的标题发送过去
		        out.write(-1);
		        byte[] bytesLength = new byte[4];
		        byte[] payload = e.serializable!=null?e.serializable.toString().getBytes("UTF-8"):new byte[0];
		        Tools.intToBytes(payload.length, bytesLength, 0, 4);
		        out.write(bytesLength);
		        out.write(payload);
	    		if( count_heartbeat % 10000 == 0 ){
	    			Log.msg(String.format("[%s] Send the heartbeat(%s, length=%s) of system to %s",
						count_heartbeat, e.serializable, payload.length, this.toString()));
	    		}
			}
			else{
				baos = new ByteArrayOutputStream();
	        	byte[] payload = null;
	        	ObjectOutputStream oos = new ObjectOutputStream(baos);
		        oos.writeObject(e.serializable);
        		payload = baos.toByteArray();
	        	if( e.serializable instanceof ModulePerf ){
	        		ModulePerf mp = (ModulePerf)e.serializable;
	        		Long count = count_module.get(mp.getId());
	        		if( count == null ){
	        			count = 0L;
	        		}
	        		if( count % 1000 == 999 ){
	        			Log.msg(String.format("[%s][%s] Send the report(length=%s, memory=%s) to %s",
	        					mp.getId(), count, payload.length, mp.getMemories().size(), this.toString()));
	        		}
	        		count += 1;
	        		count_module.put(mp.getId(), count);
	        	}
	        	else{
	        		if( count_system % 1000 == 999 ){
	        			Log.msg(String.format("[%s] Send the report(length=%s) of system to %s",
	        					count_system, payload.length, this.toString()));
	        		}
	        		count_system += 1;
	        	}
		        out.write(e.type);
		        byte[] bytesLength = new byte[4];
		        Tools.intToBytes(payload.length, bytesLength, 0, 4);
		        out.write(bytesLength);//消息长度
	        	int len = 0;
	        	int off = 0;
	        	while( off < payload.length )//消息分段发送，免得出现溢出
	        	{
	        		len = payload.length - off;
	        		len = len>SIZE_BUFFER?SIZE_BUFFER:len;
	        		out.write(payload, off, len);
	        		off += len;
	        	}
//		        Log.msg("Succeed to send(type="+type+",size="+payload.length+").");
		        out.flush();
	        }
        }
        catch(Exception e1)
        {
        	throw e1;
        }
        finally
        {
    		timestamp = 0;
    		if( baos != null )
	        	try
				{
					baos.close();
				}
				catch (IOException e1)
				{
				}
        }
	}
	
	/**
	 * 是否同步器已经超时
	 * @return
	 */
	public boolean timeout(){
		return timestamp>0&&(System.currentTimeMillis()-timestamp)>7000;//超过7秒没有完成发送就算超时，socket失效
	}
	
	public String timestamp(){
		return Tools.getFormatTime(timestamp);
	}
	
	/**
	 * 监控数据
	 * @author think
	 *
	 */
	class MonitorData{
		Object serializable;
		int type; 
	}
	
	public String toString()
	{
		return String.format("[monitor:%s][%s][connect:%s,quene:%s,closed:%s]",
			socket!=null?(socket.getInetAddress().getHostAddress()+":"+socket.getLocalPort()):"",
			id,
			socket!=null?socket.isConnected():"NULL", quene.size(),closed);
	}
}
