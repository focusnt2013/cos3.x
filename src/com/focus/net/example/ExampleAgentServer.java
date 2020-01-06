package com.focus.net.example;

import com.focus.net.Packet;
import com.focus.net.TCPAgent;

public class ExampleAgentServer extends TCPAgent
{
	public static final int MAX_LENGTH_PACKT = 1024;

	public static final int MAX_SIZE_PACKT = 64;

	public ExampleAgentServer()
	{
		super("", 0);
		// super( "", MAX_SIZE_PACKT, MAX_LENGTH_PACKT );
	}

	public void disconnect()
	{
	}

	public int receive(Packet packet)
	{
		// Packet out = new TCPOutPacket( super.conee);
		// super.send()
		return 0;
	}

	public Packet getPacket()
	{
		return null;
	}

	public int invoke(int method, byte[] payload)
	{
		return 0;
	}

	public String getIPAndPort()
	{
		return "";
	}

	public static void main(String args[])
	{
		System.out.println(Packet.MAX_VALUE_16);
	}
}
