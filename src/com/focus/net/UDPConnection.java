package com.focus.net;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPConnection
    extends Connection
{
    private DatagramSocket ds;

    protected UDPConnection( DatagramSocket ds, int num, int size )
        throws Exception
    {
        super( num, size );
        this.ds = ds;
    }

    public int onReceiveFrom( Packet packet )
        throws Exception
    {
        DatagramPacket dp = new DatagramPacket(
            buffer, packet.offset(), super.pageSize );
        ds.receive( dp );
        packet.setHostAddress( dp.getAddress().getHostAddress() );
        packet.setHostPort( dp.getPort() );

        return dp.getLength();
    }

    public int onSendTo( Packet packet )
        throws Exception
    {
        DatagramPacket dp = new DatagramPacket(
            buffer, packet.offsetBuffer(), packet.length(),
            InetAddress.getByName( packet.getHostAddress() ),
            packet.getHostPort() );
        ds.send( dp );
        return 0;
    }

    public void close()
    {
        if( ds != null )
        {
            this.ds.close();
        }
    }
}
