/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: www.lazybug.com</p>
 * @author ��ѧ
 * @version 1.0
 */
package com.focus.net;

public abstract class Packet
{
    /*���ݰ�����*/
    private int length;

    protected int page; //��������ҳ��
    protected int offset; //���ݰ����ݵ�ƫ����
    protected int buffer_offset; //��������ƫ�����������޸ĵģ�
    protected Connection connection;
    /*��������*/
    public static final int INT_8 = 1;
    public static final int INT_16 = 2;
    public static final int INT_32 = 4;
    public static final int INT_64 = 8;

    public static final long MAX_VALUE_8 = 0xFFL;
    public static final long MAX_VALUE_16 = 0xFFFFL;
    public static final long MAX_VALUE_32 = 0x7FFFFFFFL;
    public static final long MAX_VALUE_64 = Long.MAX_VALUE;

    /*���ݰ���Դ��ַ*/
    private String hostAddress;
    /*���ݰ���Դ�˿�*/
    private int hostPort;

    protected Packet()
    {
        this.page = -1;
        this.offset = 0;
    }

    /*�����ܳ����ֶεĴ�С*/
    public abstract int sizeOfLength();

    /*��ͷ�Ĵ�С*/
    public abstract int sizeOfHeader();

    /*�ַ��������ֶεĴ�С*/
    public abstract int sizeOfStrlen();

    /**
     * ���仺����
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
     * �ͷ�ռ�õĻ���ռ�
     */
    protected void free()
    {
        this.connection.free( page );
        this.page = -1;
    }

    /**
     * �ӻ��������ķ���
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
     * �ӻ�����д�ķ���
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
     * �������ӻ�����
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
     * д������������
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
     * ���ݰ��������ĵ�ǰƫ����
     * @return int
     */
    protected final int offset()
    {
        return offset;
    }

    /**
     * ���ݰ�����������ʼƫ����
     * @return int
     */
    protected final int offsetBuffer()
    {
        return buffer_offset;
    }

    /**
     * �������ݰ�����
     */
//    protected final void setLength()
//    {
//        length = offset - buffer_offset;
//    }

    /**
     * ���ص�ǰƫ���������ݰ��ĳ��ȣ�
     */
//    protected final int getOffset()
//    {
//        return offset - buffer_offset;
//    }
    /**
     * �õ������������ֽ���
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
     * ��ͷ����
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
            //�õ�������ݰ��Ļ�����ƫ����
            if( offset > buffer_offset )
            {
                length = offset - buffer_offset;//�������ݰ��ĳ���
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

                encodeHeader(); //��ͷ����
//                System.out.println( "send(" + offset + "):" + length );
//                Tools.printb( this.getByteStream() );
                //���հ������С�ڰ�ͷ�ĳ��Ȼ��ߴ��ڰ������޶Ⱦ���ֹ���ķ���
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
     * ������������
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
        //���nLength����-1��ô��ʾ��������ʧ��
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
        //�������С�ڰ�ͷ�Ĵ�С�����߳��ȳ����������������󳤶ȣ����߳��Ȳ�������������
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
     * ���ĳ���
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
     * ͨ�����Ӷ���������������������
     */
    protected static Packet malloc( Connection conn, String className )
        throws Exception
    {
        Packet packet = ( Packet )Class.forName( className ).newInstance();
        packet.setConnection( conn );
        packet.malloc(); //�򿪰�
        packet.skipHeader(); //����ͷ�ֶ�
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
