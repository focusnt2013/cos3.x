/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: www.lazybug.com</p>
 * @author 刘学
 * @version 1.0
 */
package com.focus.net;

public abstract class Packet
{
    /*数据包长度*/
    private int length;

    protected int page; //缓冲区的页数
    protected int offset; //数据包数据的偏移量
    protected int buffer_offset; //缓冲区的偏移量（不会修改的）
    protected Connection connection;
    /*数据类型*/
    public static final int INT_8 = 1;
    public static final int INT_16 = 2;
    public static final int INT_32 = 4;
    public static final int INT_64 = 8;

    public static final long MAX_VALUE_8 = 0xFFL;
    public static final long MAX_VALUE_16 = 0xFFFFL;
    public static final long MAX_VALUE_32 = 0x7FFFFFFFL;
    public static final long MAX_VALUE_64 = Long.MAX_VALUE;

    /*数据包的源地址*/
    private String hostAddress;
    /*数据包的源端口*/
    private int hostPort;

    protected Packet()
    {
        this.page = -1;
        this.offset = 0;
    }

    /*命令总长度字段的大小*/
    public abstract int sizeOfLength();

    /*包头的大小*/
    public abstract int sizeOfHeader();

    /*字符串类型字段的大小*/
    public abstract int sizeOfStrlen();

    /**
     * 分配缓冲区
     */
    protected void malloc()
        throws BufferExhaustedException
    {
        free();
        page = this.connection.malloc();
        if( page < 0 )
        {
            throw new BufferExhaustedException();
        }
        buffer_offset = connection.getPageOffset( page );
    }

    /**
     * 释放占用的缓存空间
     */
    protected void free()
    {
        this.connection.free( page );
        this.page = -1;
    }

    /**
     * 从缓存区读的方法
     */
    public int read8()
    {
        return this.connection.read8( this.offset++ );
    }

    public int read16()
    {
        int i16 = this.connection.read16( this.offset );
        this.offset += INT_16;
        return i16;
    }

    public int read32()
    {
        int i32 = this.connection.read32( this.offset );
        this.offset += INT_32;
        return i32;
    }

    public long read64()
    {
        long i64 = this.connection.read64( this.offset );
        this.offset += INT_64;
        return i64;
    }

    /**
     * 从缓存区写的方法
     */
    public void write8( int i8 )
    {
        this.connection.write8( i8, offset );
        offset += INT_8;
    }

    public void write16( int i16 )
    {
        this.connection.write16( i16, offset );
        offset += INT_16;
    }

    public void write32( int i32 )
    {
        this.connection.write32( i32, offset );
        offset += INT_32;

    }

    public void write64( long i64 )
    {
        this.connection.write64( i64, offset );
        offset += INT_64;
    }

    /**
     * 读码流从缓冲区
     * @return
     */
    public byte[] readString()
    {
        int size = 0;
        switch( sizeOfStrlen() )
        {
        case INT_8:
            size = read8();
            break;
        case INT_16:
            size = read16();
            break;
        case INT_32:
            size = read32();
            break;
        case INT_64:
            size = ( int ) read64();
            break;
        default:
            size = read16();
        }

        if( size > this.connection.sizeOfBuffer() - this.offset )
        {
            size = this.connection.sizeOfBuffer() - offset;
        }

        byte buf[] = this.connection.readString( this.offset, size );
        this.offset += size;
        return buf;
    }

    /**
     * 写码流到缓冲区
     * @param buf byte[]
     */
    public void writeString( byte[] buf )
    {
        int len = buf.length;
        switch( sizeOfStrlen() )
        {
        case INT_8:
            this.write8( len );
            break;
        case INT_16:
            if( len <= MAX_VALUE_16 )
            {
                this.write16( len );
            }
            break;
        case INT_32:
            if( len <= MAX_VALUE_32 )
            {
                this.write32( len );
            }
            break;
        case INT_64:
            if( len <= MAX_VALUE_64 )
            {
                this.write64( len );
            }
            break;
        default:
            if( len <= MAX_VALUE_16 )
            {
                this.write16( len );
            }
        }
        if( buf.length == 0 )
        {
            return;
        }

        this.connection.writeString( buf, this.offset );
        this.offset += len;
    }

    /**
     * 数据包缓冲区的当前偏移量
     * @return int
     */
    protected final int offset()
    {
        return offset;
    }

    /**
     * 数据包缓冲区的起始偏移量
     * @return int
     */
    protected final int offsetBuffer()
    {
        return buffer_offset;
    }

    /**
     * 设置数据包长度
     */
//    protected final void setLength()
//    {
//        length = offset - buffer_offset;
//    }

    /**
     * 返回当前偏移量（数据包的长度）
     */
//    protected final int getOffset()
//    {
//        return offset - buffer_offset;
//    }
    /**
     * 得到包缓冲区的字节流
     */
    public final byte[] getByteStream()
    {

        byte[] buffer = new byte[this.length() ];
        for( int i = 0; i < this.length(); i++ )
        {
            buffer[ i ] = this.connection.buffer()[ buffer_offset + i ];
        }
        return buffer;
    }

    protected void setConnection( Connection connection )
    {
        this.connection = connection;
    }

    public void setHostAddress( String hostAddress )
    {
        this.hostAddress = hostAddress;
    }

    public void setHostPort( int hostPort )
    {
        this.hostPort = hostPort;
    }

    /**
     * 包头解码
     * @throws Exception
     */
    public abstract void decodeHeader()
        throws Exception;

    public abstract void encodeHeader()
        throws Exception;

    protected boolean send()
        throws Exception
    {
        boolean isSendOk = false;
        if( page >= 0 && page < connection.getPageNum() )
        {
            //得到输出数据包的缓冲区偏移量
            if( offset > buffer_offset )
            {
                length = offset - buffer_offset;//设置数据包的长度
                offset = buffer_offset;
                switch( sizeOfLength() )
                {
                case INT_8:
                    write8( ( byte ) length );
                    break;
                case INT_16:
                    if( length <= MAX_VALUE_16 )
                    {
                        write16( length );
                    }
                    break;
                case INT_32:
                    if( length <= MAX_VALUE_32 )
                    {
                        write32( length );
                    }
                    break;
                case INT_64:
                    if( length <= MAX_VALUE_64 )
                    {
                        write64( length );
                    }
                    break;
                }

                encodeHeader(); //包头编码
//                System.out.println( "send(" + offset + "):" + length );
//                Tools.printb( this.getByteStream() );
                //最终包长如果小于包头的长度或者大于包最大的限度旧阻止包的发送
                if( length() >= sizeOfHeader() &&
                    length() <= connection.sizeOfPage() )
                {
                    isSendOk = ( length() == this.connection.onSendTo( this ) );
                    //Log.logMessage(this, "["+this.getCmd()+"]length="+this.getLength());
                }
                else
                {
                    throw new UnknowPacketException();
                }
            }
        }
        else
        {
            throw new BufferExhaustedException();
        }
        return isSendOk;
    }

    /**
     * 接收网络数据
     * @return
     */
    protected boolean receive()
        throws Exception
    {
        free();
        malloc();

        buffer_offset = connection.getPageOffset( page );
        offset = buffer_offset;

        int len = connection.onReceiveFrom( this );
        //如果nLength等于-1那么表示接收数据失败
        if( len == -1 )
        {
            free();
            return false;
        }

        switch( sizeOfLength() )
        {
        case INT_8:
            length = read8();
            break;
        case INT_16:
            length = read16();
            break;
        case INT_32:
            length = read32();
            break;
        case INT_64:
            length = ( int ) read64();
            break;
        }
        //如果长度小于包头的大小，或者长度超过了连接允许的最大长度，或者长度不等于码流长度
        if( length() < sizeOfHeader() ||
            length() > connection.sizeOfPage() ||
            length() != len )
        {
            free();
            throw new UnknowPacketException();
        }
        this.decodeHeader();
        return true;
    }

    /**
     * 包的长度
     * @return int
     */
    public int length()
    {
        return length;
    }

    /**/
    protected void skipHeader()
    {
        offset = sizeOfHeader() + buffer_offset;
    }

    /**
     * 通过连接对象与包的类名创建输出包
     */
    protected static Packet malloc( Connection conn, String className )
        throws Exception
    {
        Packet packet = ( Packet )Class.forName( className ).newInstance();
        packet.setConnection( conn );
        packet.malloc(); //打开包
        packet.skipHeader(); //跳过头字段
        return packet;
    }

    public int getHostPort()
    {
        return hostPort;
    }

    public String getHostAddress()
    {
        return hostAddress;
    }
}
