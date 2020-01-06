package com.focus.pipe;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public abstract class Writable
{
	/**
	 * ��ȡ���������ָ��ֶ�
	 * @param in
	 * @throws IOException
	 */
	public abstract void readFields(DataInput in) throws IOException;
	/**
	 * д���ݵ���������ָ��ֶ�
	 * @param out
	 * @throws IOException
	 */
	public abstract void writeFields(DataOutput out) throws IOException;
	
	/**
	 * ���·�����ȡ
	 * @param in
	 * @return
	 * @throws IOException
	 */
	protected byte readByte(DataInput in) throws IOException
	{
		return in.readByte();
	}

	protected short readShort(DataInput in) throws IOException
	{
		return in.readShort();
	}
	
	protected int readInt(DataInput in) throws IOException
	{
		return in.readInt();
	}
	
	protected long readLong(DataInput in) throws IOException
	{
		return in.readLong();
	}
	/**
	 * �ַ������Ȳ��ܳ���64K
	 * @param in
	 * @return
	 * @throws IOException
	 */
	protected String readString(DataInput in) throws IOException
	{
	    int length = in.readInt();
	    if (length == 0 ) return "";
	    if (length == -1 ) return null;
	    byte[] buffer = new byte[length];
	    in.readFully(buffer);
	    return new String(buffer,"UTF-8");  
	}

	/**
	 * ���·����ṩд��ֵ�������
	 * @param out
	 * @param v
	 * @throws IOException
	 */
	protected void writeByte(DataOutput out, byte v) throws IOException 
	{
		out.writeByte(v);
	}

	protected void writeShort(DataOutput out, short v) throws IOException 
	{
		out.writeShort(v);
	}
	
	protected void writeInt(DataOutput out, int v) throws IOException 
	{
		out.writeInt(v);
	}
	

	protected void writeLong(DataOutput out, long v) throws IOException 
	{
		out.writeLong(v);
	}
	
	protected void writeString(DataOutput out, String s) throws IOException 
	{
	    if( s != null ) 
	    {
		    if( s.isEmpty()) 
		    {
		    	out.writeInt(0);
		    	return;
		    } 
	    	byte[] buffer = s.getBytes("UTF-8");
	    	int len = buffer.length;
	    	out.writeInt(len);
	    	out.write(buffer, 0, len);
	    } 
	    else 
	    {
	    	out.writeInt(-1);
	    }
	}
}
