package com.focus.util;

/**
 * @author focus
 *
 */
public class Base64X
{

    private static char baseChar[] = {
        '9', 'j',  'S', 'K', 'z', 'c', '+', '4',
        'd', 'f', 'a', 'Z', 'h', '8', 'e', 'Y',
        'V', 'X', '6', 'R', 'T', 'U', 'W', 'Q',
        'l', 'n', 'i', 'b', '/', 'k', 'm', 'g',
        'F', 'u', 'C', '3', 'D', 'E', 'G', 'A',
        '1', 'B', 'y', 'x', '7', '0', '2', 'P',
        't', 'v', 'q', 'p', 'r', 'M', 'H', 'o',
        'N', 'w', '5', 'J', 'L', 's', 'O', 'I',
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

    /**
     * @param b
     * @return
     */
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

    public static String decode2str(String code)
    {
    	return new String(decode(code));
    }
    /**
     * @param code
     * @return
     */
    public static byte[] decode(String code)
    {
        if(code == null || code.isEmpty())
            return new byte[0];
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
        for(int i = 0; i < len; i += 4){
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
    public static void main(String[] args)
    {
    	System.err.println(Base64X.decode2str("kou38pTwl3EEhRhMnR8u8RFp"));
//    	String code = encode("/cos/data/program/publish/history/20170402/190416".getBytes());
//    	System.out.println(code);
//    	System.out.println(new String(decode(code)));
    }
}
