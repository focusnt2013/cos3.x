package com.focus.net;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import com.focus.util.Log;

public abstract class TCPAgent implements Agent
{
	/* ����״̬��ǩ */
	public static final String PROXY_STATUS_LABEL[] = { "Connection", "Disconnection", "Unknown Host", "Exceptoin", };

	/* ÿ�����Ļ�������С */
	private int sizeOfPagebuf = 1024;

	/* һ����ʼ�����ٸ��� */
	private int sizeOfPages = 64;

	/* ����������� */
	private String className = null;

	/* ����״̬ */
	protected int status = DISCONNECTION;

	/* ƽ̨���ӵ��׽��� */
	private TCPConnection tcpConnection = null;

	/* �첽���� */
	private Consumer consumer;

	/* ����ID */
	// public static int tid = 1;
	/* ���ӹرշ� */
	private boolean connectionClosed = false;

	/* ���һ���쳣 */
	public Exception lastException = null;

	/* ��ַ */
	protected String ip;

	/* �˿� */
	protected int port;

	/**/
	protected Socket socket;

	public TCPAgent(String ip, int port)
	{
		this.ip = ip;
		this.port = port;
	}

	public TCPAgent(Socket socket) throws Exception
	{
		this.socket = socket;
		this.ip = socket.getInetAddress().getHostAddress();
		this.port = socket.getPort();
	}

	/**
	 * ������ϵ��
	 * 
	 * @return int ����״̬
	 */
	public int connect()
	{
		if (getStatus() != CONNECTION)
		{
			try
			{
				if (socket != null)
				{
					initialize(socket);
				}
				else
				{
					initialize(ip, port);
				}
			}
			catch (UnknownHostException e)
			{
				setStatus(UNKNOWN_HOST);
			}
			catch (IOException e)
			{
				setStatus(DISCONNECTION);
			}
			catch (Exception ex)
			{
				setStatus(DISCONNECTION);
			}

		}
		return this.getStatus();
	}

	/* ��ʼ������ */
	private void initialize(String ip, int port) throws Exception
	{
		Socket socket = new Socket(ip, port);
		initialize(socket);
	}

	private void initialize(Socket socket) throws Exception
	{
		if (this.tcpConnection != null)
		{
			tcpConnection.close();
		}
		// ����TCP����
		this.tcpConnection = new TCPConnection(socket, sizeOfPages, sizeOfPagebuf);

		// ����������ģ��
		this.consumer = new Consumer();
		this.consumer.start();
		Thread thread = new Thread(this);
		thread.start();
		this.status = CONNECTION;
	}

	public String getAddress()
	{
		if (this.tcpConnection != null)
		{
			return this.tcpConnection.getAddress();
		}
		return "";
	}

	/**
	 * When an object implementing interface <code>Runnable</code> is used to
	 * create a thread, starting the thread causes the object's <code>run</code>
	 * method to be called in that separately executing thread.
	 * 
	 * @todo Implement this java.lang.Runnable method
	 */
	public void run()
	{
		// System.out.println( "����(" + status() + ")����." );
		// System.out.println( "��������(" + this.tcpConnection.getIPAndPort() +
		// ")." );
		while (status == CONNECTION)
		{
			if (this.className == null || sizeOfPagebuf == 0 || sizeOfPages == 0)
			{
				try
				{
					Thread.sleep(100);
				}
				catch (InterruptedException ex)
				{
				}
				continue;
			}

			Packet packet = null;
			try
			{
				packet = malloc();// �����
				if (status != CONNECTION)
				{
					break;
				}

				if (packet.receive())
				{
					this.receive(packet);
				}
				// System.out.println( "packet.receive()" );
			}
			catch (UnknowPacketException e)
			{
				// �յ�δ֪�İ��Ͷ���
				// e.printStackTrace();
				lastException = e;
			}
			catch (BufferExhaustedException e)
			{
				// e.printStackTrace();
				status = EXCEPTION;
				lastException = e;
			}
			catch (IOException e)
			{
				// e.printStackTrace();
				status = DISCONNECTION;
				lastException = e;
			}
			catch (Exception e)
			{
				e.printStackTrace();
				status = DISCONNECTION;
				lastException = e;
			}
			finally
			{
				if (packet != null)
				{
					packet.free();
				}
			}
		}
		// System.out.println( "����(" + status() + ")�Ͽ�." );

		if (this.consumer != null)
		{
			this.consumer.close();
			consumer = null;
		}

		synchronized (this)
		{
			if (this.tcpConnection != null)
			{
				if (!connectionClosed)
				{
					tcpConnection.close();
					connectionClosed = true;
					Log.war("Agent quite(" + tcpConnection.getAddress() + ").");

				}
				else
				{
					Log.war("Agent exit(" + tcpConnection.getAddress() + ").");
				}
			}
			tcpConnection = null;
			this.notifyAll();
		}
		disconnect();
		connectionClosed = false;
	}

	/**
	 * �������
	 */
	public synchronized void close()
	{
		if (this.tcpConnection != null)
		{
			status = DISCONNECTION;
			if (!connectionClosed)
			{
				Log.war("Close agent(" + tcpConnection.getAddress() + ").");
				// System.out.println( "Close agent." );
				tcpConnection.close();
				connectionClosed = true;
			}
			else
			{
				Log.war("Abort agent(" + tcpConnection.getAddress() + ").");
			}
		}
	}

	/**
	 * @param packet TCPPacket
	 * @return int
	 */
	public int send(Packet out)
	{
		Packet packet = (Packet) out;
		if (getStatus() == CONNECTION)
		{
			this.consumer.post(packet);
		}
		return getStatus();
	}

	/**
	 * @return int
	 */
	public int getStatus()
	{
		return status;
	}

	/**
	 * �õ����Ͱ�
	 * 
	 * @return TCPPacket
	 */
	public Packet malloc() throws Exception
	{
		return Packet.malloc(this.tcpConnection, this.className);
	}

	public void free(Packet packet)
	{
		packet.free();
	}

	public String getIp()
	{
		return ip;
	}

	public int getPort()
	{
		return port;
	}

	public void setPacketClassName(String className)
	{
		this.className = className;
	}

	public void setSizeOfPagebuf(int sizeOfPagebuf)
	{
		this.sizeOfPagebuf = sizeOfPagebuf;
	}

	public void setSizeOfPages(int sizeOfPages)
	{
		this.sizeOfPages = sizeOfPages;
	}

	public String status()
	{
		if (status < PROXY_STATUS_LABEL.length)
		{
			return PROXY_STATUS_LABEL[status];
		}
		return "Unknown";
	}

	private void setStatus(int status)
	{
		this.status = status;
	}
}
