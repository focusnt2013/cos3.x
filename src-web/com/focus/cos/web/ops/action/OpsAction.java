package com.focus.cos.web.ops.action;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.util.zip.GZIPInputStream;

import javax.servlet.ServletOutputStream;

import com.focus.cos.web.action.GridAction;
import com.focus.cos.web.ops.service.MonitorMgr;
import com.focus.util.Tools;

/**
 * ZK监控action
 * @author focus
 *
 */
public class OpsAction extends GridAction
{
	private static final long serialVersionUID = 4622850076165405498L;
	/*Zookeeper访问地址*/	
	protected String ip = "127.0.0.1";
	/*访问端口*/
	protected int port = 0;
	/*存放预览用的路径*/
	protected String path;
	/*是否只显示堡垒机*/
	protected boolean onlyGateone;
	
	public boolean isOnlyGateone() {
		return onlyGateone;
	}

	public MonitorMgr getMonitorMgr() 
	{
		return MonitorMgr.getInstance();
	}

	/**
	 * 
	 * @param sos
	 * @param step
	 * @param progress
	 * @throws IOException
	 */
	public int writeProgress(ServletOutputStream sos, int step, int progress) throws IOException
	{
		if( progress > 100 ) progress = 100;
		if( step >= 50 ) return step;
    	for(; step < progress/2; step++)
    	{
    		switch( step )
    		{
    		case 8: sos.write('2');break;
    		case 18: sos.write('4');break;
    		case 28: sos.write('6');break;
    		case 38: sos.write('8');break;
    		case 9:
    		case 19:
    		case 29:
    		case 39:
    			sos.write('0');
    			break;
    		case 10:
    		case 20:
    		case 30:
    		case 40:
    			sos.write('%');
    			break;
    		default: sos.write('+');break;
    		}
    		sos.flush();
    	}
    	if( step == 50 )
    	{
			sos.write("|100%".getBytes());
			sos.flush();
    	}
    	return step;
	}

	/**
	 * 拷贝报告
	 * @param t
	 * @param sos
	 * @param tips
	 * @param logtxt
	 */
	protected StringBuffer pr = new StringBuffer();
	protected void pagereport(ServletOutputStream sos, int t, boolean time, String tips)
	{
		if( sos != null )
			synchronized (sos)
			{
				try 
				{
					if( t >= 0 ){
						sos.println();
						pr.append("\r\n");
					}
					while( t-- > 0 ){
						sos.print("\t");
						pr.append('\t');
					}
					if( time ){
						sos.print("["+Tools.getFormatTime("HH:mm:ss", System.currentTimeMillis())+"] ");
						pr.append("["+Tools.getFormatTime("HH:mm:ss", System.currentTimeMillis())+"] ");
					}
					sos.write(tips.getBytes("UTF-8"));
					pr.append(tips);
					sos.flush();
				}
				catch (Exception e) 
				{
				}
			}
	}
	/**
	 * 从UDP报文中解压GZIP数据
	 * @param response
	 * @return
	 * @throws Exception
	 */
	protected byte[] getGZIPResult(DatagramPacket response) throws Exception
	{
		int size = Tools.bytesToInt(response.getData(), 0, 4);
		byte[] buffer = new byte[size];
		GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(response.getData(), 4, response.getLength()));
		int len;
		int offset = 0;
		size = buffer.length;
		while( (len = gis.read(buffer, offset, size)) != -1  )
		{
			size -= len;
			offset += len;
			if( size == 0 ) break;
		}
		gis.close();
//		log.info("Succeed to decode the result of decompress("+offset+").");
		return buffer;
	}
	/**
	 * 
	 * @return
	 */
	public int getServerId()
	{
		if( Tools.isNumeric(id) ) return Integer.parseInt(id);
		return 0;
	}
	
	public String getIp()
	{
		return ip;
	}

	public void setIp(String ip)
	{
		this.ip = ip;
	}

	public int getPort()
	{
		return port;
	}

	public void setPort(int port)
	{
		this.port = port;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

}
