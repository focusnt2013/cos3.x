package com.focus.util;

/**
 * 对一些基于Base64机制进行编码的数据进行穷举解码
 * @author focus
 *
 */
public class Base64Decoder
{

    private static char baseChar[] = {
        'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 
        'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 
        'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 
        'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 
        'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n',
        'o', 'p', 'q', 'r', 's', 't', 'u', 'v',
        'w', 'x', 'y', 'z', '0', '1', '2', '3',
        '4', '5', '6', '7', '8', '9', '+', '/'
    };
    
    private static byte position[] = new byte[128];
    /*{
        -1, -1, -1, -1, -1, -1, -1, -1, //00 ~07
        -1, -1, -1, -1, -1, -1, -1, -1, //08 ~15
        -1, -1, -1, -1, -1, -1, -1, -1, //16 ~23
        -1, -1, -1, -1, -1, -1, -1, -1, //24 ~31
        -1, -1, -1, -1, -1, -1, -1, -1, //32 ~39
        -1, -1, -1, 62, -1, 63, -1, 63, //40 ~47
        52, 53, 54, 55, 56, 57, 58, 59, //48 ~55
        60, 61, -1, -1, -1,  0, -1, -1, //56 ~63
        -1,  0,  1,  2,  3,  4,  5,  6, //64 ~71
         7,  8,  9, 10, 11, 12, 13, 14, //72 ~79
        15, 16, 17, 18, 19, 20, 21, 22, //80 ~87
        23, 24, 25, -1, -1, -1, -1, -1, //88 ~95
        -1, 26, 27, 28, 29, 30, 31, 32, //96 ~103
        33, 34, 35, 36, 37, 38, 39, 40, //104 ~111
        41, 42, 43, 44, 45, 46, 47, 48, //112 ~119
        49, 50, 51, -1, -1, -1, -1, -1  //120 ~127
    };
    //加密表中的ASCII码的值对应解密表的位，加密表的位对应解密表的值
    private static int biao[] = {
        43, 45, 47, 48, 49, 50, 51, 52, 
        53, 54, 55, 56, 57, 65, 66, 67, 
        'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 
        'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 
        'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n',
        'o', 'p', 'q', 'r', 's', 't', 'u', 'v',
        'w', 'x', 'y', 'z', '0', '1', '2', '3',
        '4', '5', '6', '7', '8', '9', '+', '/'
    };*/
    
    static
    {
    	for( byte i = 0; i < baseChar.length; i++ )
    	{
    		int p = baseChar[i];
    		position[p] = i;
    	}
    }

    public static String encode(byte b[])
    {
        int code = 0;
        StringBuffer sb = new StringBuffer((b.length - 1) / 3 << 6);
        for(int i = 0; i < b.length; i++)
        {
            code |= b[i] << 16 - (i % 3) * 8 & 255 << 16 - (i % 3) * 8;
            if(i % 3 == 2 || i == b.length - 1)
            {
                sb.append(baseChar[(code & 0xfc0000) >>> 18]);
                sb.append(baseChar[(code & 0x3f000) >>> 12]);
                sb.append(baseChar[(code & 0xfc0) >>> 6]);
                sb.append(baseChar[code & 0x3f]);
                code = 0;
            }
        }

        if(b.length % 3 > 0)
            sb.setCharAt(sb.length() - 1, '=');
        if(b.length % 3 == 1)
            sb.setCharAt(sb.length() - 2, '=');
        return sb.toString();
    }

    public static byte[] decode(String code)
    {
        if(code == null)
            return null;
        int len = code.length();
        if(len % 4 != 0)
        {
            return code.getBytes(); // new IllegalArgumentException("Base64 string length must be 4*n");
        }
        if(code.length() == 0)
            return new byte[0];
        int pad = 0;
        if(code.charAt(len - 1) == '=')
            pad++;
        if(code.charAt(len - 2) == '=')
            pad++;
        int retLen = (len / 4) * 3 - pad;
        byte ret[] = new byte[retLen];
        for(int i = 0; i < len; i += 4)
        {
            int j = (i / 4) * 3;
            char ch1 = code.charAt(i);
            char ch2 = code.charAt(i + 1);
            char ch3 = code.charAt(i + 2);
            char ch4 = code.charAt(i + 3);
            int tmp = position[ch1] << 18 | position[ch2] << 12 | position[ch3] << 6 | position[ch4];
            ret[j] = (byte)((tmp & 0xff0000) >> 16);
            if(i < len - 4)
            {
                ret[j + 1] = (byte)((tmp & 0xff00) >> 8);
                ret[j + 2] = (byte)(tmp & 0xff);
                continue;
            }
            if(j + 1 < retLen)
                ret[j + 1] = (byte)((tmp & 0xff00) >> 8);
            if(j + 2 < retLen)
                ret[j + 2] = (byte)(tmp & 0xff);
        }

        return ret;
    }
    
    /**
     * 破解Base64编码表
     * @param num
     * @param i
     */
    public static long pcn = 0;//排列组合计数
    public static long total = 0;//总的组合数
    public static void crack(char[] num)
    {
    	
    }
    
    public static void crack(char[] num, int i )
    {
    	if( i == num.length - 1)
    	{
    		pcn += 1;
    		if( pcn % 10240000 == 0)
    		{	for( int j = 1; j < num.length; j++ )
	    		{
	    			System.out.print(num[j]+"");
	    		}
    			System.out.println("\t【"+pcn+"】");
    		}
        	for( byte k = 0; k < baseChar.length; k++ )
        	{
        		int p = baseChar[i];
        		position[p] = k;
        	}
        	final String code = "WTlUNpRv7RH12UYymTbMUA==";
        	String imsi = new String(decode(code));
        	if( imsi.indexOf("46002") != -1 )
        	{
        		System.out.println("################");
        		System.out.println(imsi);
    			for( int j = 1; j < num.length; j++ )
	    		{
	    			System.out.print(num[j]+"");
	    		}
    			System.out.println("\t【"+pcn+"】");
    			System.exit(0);
        	}
    	}
    	else
    	{
    		for(int j = i; j < num.length; j++ )
    		{
    			char tmp = num[j];
    			for( int k = j; k > i; k-- )
    			{
    				num[k] = num[k-1];
    			}
    			num[i] = tmp;
    			crack(num, i+1);
    			for( int k = i; k < j; k++)
    			{
    				num[k] = num[k+1];
    			}
    			num[j]=tmp;
    		}
    	}
    }

    public static void main(String[] args)
    {
//    	char numbers[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
    	crack(baseChar, 0);
//    	for(int i = 0; i < baseChar.length; i++ )
//    	{
//    		System.out.print(baseChar[i]);
//    	}
    }
}
