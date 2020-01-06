/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */
package com.focus.net;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class TCPConnection extends Connection
{
	public Socket socket;

	private DataInputStream in;

	private DataOutputStream out;

	protected TCPConnection(Socket s, int nPageNum, int nPageSize) throws IOException
	{
		super(nPageNum, nPageSize);
		this.socket = s;
		this.in = new DataInputStream(this.socket.getInputStream());
		this.out = new DataOutputStream(this.socket.getOutputStream());
	}

	/**
	 * 实现了Connection的接口，接收数据包
	 * 
	 * @param packet
	 * @return
	 * @throws java.io.IOException
	 */
	public int onReceiveFrom(Packet packet) throws Exception
	{
		if (in.read(buffer, packet.offset(), packet.sizeOfLength()) == -1)
		{
			throw new java.io.IOException("IO-read/write is closed!");
		}
		int length = NetUtility.bytesToInt(buffer, packet.offset(), packet.sizeOfLength());
		// 如果读取的长度超出了
		if (length > pageSize || length < packet.sizeOfHeader())
		{
			throw new UnknowPacketException();
		}

		int size = length - packet.sizeOfLength();
		int offset = packet.offset() + packet.sizeOfLength();
		while (size > 0)
		{
			int l = in.read(buffer, offset, size);
			if (l == -1)
			{
				throw new java.io.IOException("IO-read/write is closed!");
			}

			if (l > 0)
			{
				size -= l;
				offset += l;
			}
		}
		return length;
	}

	/**
	 * 通过输出流发送数据包
	 * 
	 * @param packet
	 * @return
	 * @throws java.io.IOException
	 */
	public int onSendTo(Packet packet) throws Exception
	{
		out.write(buffer, packet.offsetBuffer(), packet.length());
		return packet.length();
	}

	public void close()
	{
		try
		{
			this.out.close();
			this.in.close();
			this.socket.close();
		}
		catch (IOException e)
		{
		}
	}

	/**
	 * 得到地址（包括端口与IP地址）
	 * 
	 * @return String
	 */
	protected String getAddress()
	{
		return this.socket.getInetAddress().getHostAddress() + ":" + this.socket.getPort();
	}

	protected String getHostAddress()
	{
		return this.socket.getInetAddress().getHostAddress();
	}

	protected int getPort()
	{
		return this.socket.getPort();
	}
}
