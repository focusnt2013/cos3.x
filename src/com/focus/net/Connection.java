/**
 * <p>Title: ��ʱͨ�����ܲ���</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: </p>
 * @author ��ѧ
 * @version 1.0
 */
package com.focus.net;

public abstract class Connection
{
	// public static int MAX_SIZE_BUFFER; //��������С
	// public static final int MAX_NUM_PAGE = 40; //������ҳ��
	// public static final int MAX_SIZE_PAGE = 0x0200; //û�л����С
	protected int pageNum;

	protected int pageSize;

	protected byte[] buffer; // ������

	protected byte[] pagable;

	// protected int port;
	protected int sequence = 65533;

	protected int count_alloted; // �ѷ������

	protected int peak_value; // ��ֵ

	/* �������� */
	public static final int INT_8 = 1;

	public static final int INT_16 = 2;

	public static final int INT_32 = 4;

	public static final int INT_64 = 8;

	public Connection(int nPageNum, int nPageSize)
	{
		this.pageNum = nPageNum;
		this.pageSize = nPageSize;
		this.buffer = new byte[this.pageNum * this.pageSize];
		pagable = new byte[pageNum];
		for (int i = 0; i < pageNum; i++)
		{
			pagable[i] = 1;
		}
	}

	/**
	 * �����������Ķ���
	 * 
	 * @return
	 */
	public byte[] buffer()
	{
		return buffer;
	}

	/**
	 * ����������
	 */
	public synchronized int malloc()
	{
		for (int i = 0; i < pageNum; i++)
		{
			if (pagable[i] == 1)
			{
				pagable[i] = 0;
				count_alloted += 1;
				peak_value = count_alloted > peak_value ? count_alloted : peak_value;
				// System.out.println("����ҳ��"+i+"��--"+this.toString());
				return i;
			}
		}
		return -1;
	}

	/**
	 * �ͷŻ�������Դ
	 */
	public synchronized void free(int page)
	{
		if (page >= 0 && page < pageNum)
		{
			// System.out.println("����ҳ��"+page+"��--"+this.toString());
			pagable[page] = 1;
			count_alloted -= 1;
		}
	}

	public abstract int onReceiveFrom(Packet packet) throws Exception;

	public abstract int onSendTo(Packet packet) throws Exception;

	public abstract void close();

	/* ����ҳ */
	public int sizeOfPage()
	{
		return pageSize;
	}

	/* ÿ���������Ĵ�С */
	public int sizeOfBuffer()
	{
		return this.pageNum * this.pageSize;
	}

	/**
	 * д�ַ�����������
	 * 
	 * @param buf
	 * @param offset
	 */
	public void writeString(byte[] buf, int offset)
	{
		for (int i = 0; i < buf.length; i++)
		{
			buffer[offset++] = buf[i];
		}
	}

	/**
	 * �����ݴӻ�����
	 * 
	 * @param offset
	 * @param length
	 * @return
	 */
	public byte[] readString(int offset, int length)
	{
		byte[] buf = new byte[length];
		for (int i = 0; i < length; i++)
		{
			buf[i] = buffer[offset++];
		}
		return buf;
	}

	public int read8(int offset)
	{
		return buffer[offset] < 0 ? 0x100 + buffer[offset] : buffer[offset];
	}

	public int read16(int offset)
	{
		return NetUtility.bytesToInt(buffer, offset, INT_16);
	}

	/**
	 * ��4λ�ֽ�
	 * 
	 * @return
	 */
	public int read32(int offset)
	{
		return NetUtility.bytesToInt(buffer, offset, INT_32);
	}

	/**
	 * ��4λ�ֽ�
	 * 
	 * @return
	 */
	public long read64(int offset)
	{
		return NetUtility.bytesToInt(buffer, offset, INT_64);
	}

	public void write8(int i8, int offset)
	{
		buffer[offset] = (byte) i8;
	}

	public void write16(int i16, int offset)
	{
		byte[] buf = NetUtility.intToBytes(i16, INT_16);
		for (int i = 0; i < 2; i++)
		{
			int k = buf.length - 1 - i;
			buffer[offset + 1 - i] = k >= 0 ? buf[k] : 0;
		}
	}

	public void write32(int i32, int offset)
	{
		byte[] buf = NetUtility.intToBytes(i32, INT_32);
		for (int i = 0; i < INT_32; i++)
		{
			int k = buf.length - 1 - i;
			buffer[offset + 3 - i] = k >= 0 ? buf[k] : 0;
		}
	}

	public void write64(long i64, int offset)
	{
		byte[] buf = NetUtility.longToBytes(i64, INT_64);
		for (int i = 0; i < INT_64; i++)
		{
			int k = buf.length - 1 - i;
			buffer[offset + 7 - i] = k >= 0 ? buf[k] : 0;
		}
	}

	/**
	 * �������к�
	 * 
	 * @return
	 */
	public synchronized int getSequence()
	{
		if (sequence == 0x10000)
		{
			sequence = 0;
		}
		return sequence++;
	}

	/**
	 * ��ѯ������ҳ��
	 * 
	 * @return
	 */
	public int getPageNum()
	{
		return this.pageNum;
	}

	/**
	 * �õ�ָ��ҳ��ƫ����
	 */
	protected int getPageOffset(int page)
	{
		return page * this.sizeOfPage();
	}

	public String toString()
	{
		return "�ѷ���Ļ���[" + count_alloted + "]��ֵ[" + peak_value + "]";
	}
}
