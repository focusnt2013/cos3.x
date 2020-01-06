package com.focus.net;

import java.net.InetAddress;

import com.focus.util.Tools;

/**
 * <p>
 * Title: Geniux
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2006
 * </p>
 * <p>
 * Company:
 * </p>
 * 
 * @author David Lau
 * @version 1.0
 */
public class NetUtility
{

	/**
	 * �и��ֽڴ�
	 * 
	 * @param buffer
	 * @param off
	 * @param length
	 * @return
	 */
	public static final byte[] truncate(byte[] buffer, int off, int length)
	{
		int nSize = 0;
		if (buffer.length < length + off)
		{
			nSize = buffer.length - off;
		}
		else
		{
			nSize = length;
		}

		byte[] buf = new byte[nSize];
		for (int i = 0; i < nSize; i++)
		{
			buf[i] = buffer[off + i];
		}
		return buf;
	}

	/**
	 * ���ֽ���ת����������
	 * 
	 * @param b
	 * @return
	 */
	public static final int bytesToInt(byte[] b)
	{
		int s = 0;
		for (int pos = 0; pos < b.length; pos++)
		{
			int iTemp = b[pos] < 0 ? 0x100 + b[pos] : b[pos];
			s += iTemp << (8 * (b.length - pos - 1));
		}

		return s;
	}

	/**
	 * ���ֽ���ת�����޷��ŵĶ�����
	 * 
	 * @param b
	 * @return
	 */
	public static final short bytesToShort(byte[] b)
	{
		return (short) bytesToInt(b);
	}

	/**
	 * @param b���ݻ���
	 * @param offset�����ݵ��Ǹ�λ�ÿ�ʼ����
	 * @param length�������ݵĳ���
	 * @return
	 */
	public static final int bytesToInt(byte[] b, int offset, int length)
	{
		int s = 0;
		for (int i = 0; i < length; i++)
		{
			int iTemp = b[offset + i] < 0 ? 0x0100 + b[offset + i] : b[offset + i];
			s += iTemp << (8 * (length - i - 1));
		}
		return s;
	}

	/**
	 * @param b���ݻ���
	 * @param offset�����ݵ��Ǹ�λ�ÿ�ʼ����
	 * @param length�������ݵĳ���
	 * @return
	 */
	public static final long bytesToLong(byte[] b, int offset, int length)
	{
		long s = 0;
		for (int i = 0; i < length; i++)
		{
			int iTemp = b[offset + i] < 0 ? 0x0100 + b[offset + i] : b[offset + i];
			s += iTemp << (8 * (length - i - 1));
		}
		return s;
	}

	/*
	 * Copy byte array to a file
	 */
	public static boolean copyByteArray(byte[] source, byte[] target)
	{
		if (source == null || target == null)
			return false;

		if (source.length > target.length)
		{
			for (int i = 0; i < target.length; i++)
				target[i] = source[i];
			return true;
		}

		for (int i = 0; i < source.length; i++)
			target[i] = source[i];

		return true;
	}

	public static final void intToBytes(int s, byte[] buf, int offset, int length)
	{
		for (int i = length - 1; i >= 0; i--)
		{
			// ��λ����
			if (s != 0)
			{
				buf[offset + i] = (byte) (s & 0xFF);
				s >>= 8;
			}
			else
			{
				buf[offset + i] = 0;
			}
		}
	}

	public static final void intToBytes(int s, byte[] buf)
	{
		for (int i = buf.length - 1; i >= 0; i--)
		{
			buf[i] = (byte) (s & 0xFF);
			s >>= 8;
			if (s == 0)
			{
				break;
			}
		}
	}

	/**
	 * ��������ת����Ϊ�ֽ���
	 * 
	 * @param s
	 * @return
	 */
	public static final byte[] intToBytes(int s, int size)
	{
		byte[] buf = new byte[size];
		for (int i = size - 1; i >= 0; i--)
		{
			buf[i] = (byte) (s & 0xFF);
			s >>= 8;
			if (s == 0)
			{
				break;
			}
		}
		return buf;
	}

	public static final byte[] intToBytes(int s)
	{
		return intToBytes(s, 4);
	}

	public static final byte[] longToBytes(long len, int size)
	{
		byte[] buf = new byte[size];
		for (int i = size - 1; i >= 0; i--)
		{
			buf[i] = (byte) (len & 0xFF);
			len >>= 8;
			if (len == 0)
			{
				break;
			}
		}
		return buf;
	}

	public static final String getIP(int ip)
	{
		return getIP(Tools.intToBytes(ip, 4));
	}

	public static final String getIP(byte[] buf)
	{
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < buf.length; i++)
		{
			buffer.append(buf[i] < 0 ? (0x0100 + buf[i]) : buf[i]);
			if (i + 1 < buf.length)
			{
				buffer.append(".");
			}
		}

		return buffer.toString();
	}

	public static final String toHexString(byte[] buffer)
	{
		return toHexString(buffer, 0, buffer.length);
	}

	public static final String toHexString(byte[] buffer, int offset, int length)
	{
		StringBuffer sb = new StringBuffer();
		for (int i = offset; i < buffer.length && i < length; i++)
		{
			// System.out.println("if( i + 1("+(i+1)+") ==
			// buffer.length("+buffer.length+") ) ");
			sb.append("0x");
			int k = buffer[i] < 0 ? 256 + buffer[i] : buffer[i];
			if (k < 0x10)
			{
				sb.append('0');
			}
			sb.append(Integer.toHexString(k));
			sb.append(" ");

			if (i % 8 == 7)
			{
				sb.append(" ");
			}
			if (i % 16 == 15)
			{
				sb.append('\n');
			}

			if (i + 1 == buffer.length)
			{
				if (i % 16 != 0)
				{
					sb.append('\n');
				}
				System.out.println(sb.toString());
				return sb.toString();
			}
		}
		return sb.toString();
	}

	public static String getLocalHostIP()
	{
		String ip;
		try
		{
			InetAddress addr = InetAddress.getLocalHost();
			ip = addr.getHostAddress();
		}
		catch (Exception ex)
		{
			ip = "";
		}
		return ip;
	}

	public static String getLocalHostName()
	{
		String hostName;
		try
		{
			InetAddress addr = InetAddress.getLocalHost();
			hostName = addr.getHostName();
		}
		catch (Exception ex)
		{
			hostName = "";
		}
		return hostName;
	}

	public static String[] getAllLocalHostIP()
	{
		String[] ret = null;
		try
		{
			String hostName = getLocalHostName();
			if (hostName.length() > 0)
			{
				InetAddress[] addrs = InetAddress.getAllByName(hostName);
				if (addrs.length > 0)
				{
					ret = new String[addrs.length];
					for (int i = 0; i < addrs.length; i++)
					{
						ret[i] = addrs[i].getHostAddress();
					}
				}
			}

		}
		catch (Exception ex)
		{
			ret = null;
		}
		return ret;
	}

	public static void main(String[] args)
	{
		// System.out.println(getLocalHostIP());
		System.out.println("��������" + getLocalHostIP());
		System.out.println("��������" + getLocalHostName());

		String[] localIP = getAllLocalHostIP();
		for (int i = 0; i < localIP.length; i++)
		{
			System.out.println(localIP[i]);
		}
	}
}
