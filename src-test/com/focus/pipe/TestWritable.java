package com.focus.pipe;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class TestWritable extends Writable
{
	private byte p0;
	private short p1;
	private int p2;
	private long p3;
	private String p4;
	
	@Override
	public void readFields(DataInput in) throws IOException
	{
		p0 = this.readByte(in);
		p1 = this.readShort(in);
		p2 = this.readInt(in);
		p3 = this.readLong(in);
		p4 = this.readString(in);
	}

	@Override
	public void writeFields(DataOutput out) throws IOException
	{
		this.writeByte(out, p0);
		this.writeShort(out, p1);
		this.writeInt(out, p2);
		this.writeLong(out, p3);
		this.writeString(out, p4);
	}
	
	public String toString()
	{
		return p4+"["+p2+"] "+p3+","+p0+","+p1;
	}

	public byte getP0()
	{
		return p0;
	}

	public void setP0(byte p0)
	{
		this.p0 = p0;
	}

	public short getP1()
	{
		return p1;
	}

	public void setP1(short p1)
	{
		this.p1 = p1;
	}

	public int getP2()
	{
		return p2;
	}

	public void setP2(int p2)
	{
		this.p2 = p2;
	}

	public long getP3()
	{
		return p3;
	}

	public void setP3(long p3)
	{
		this.p3 = p3;
	}

	public String getP4()
	{
		return p4;
	}

	public void setP4(String p4)
	{
		this.p4 = p4;
	}

}
