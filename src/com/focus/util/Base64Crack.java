package com.focus.util;

/**
 * 对一些基于Base64机制进行编码的数据进行穷举解码
 * @author focus
 *
 */
public class Base64Crack
{

    private static char base64[] = {
//        'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 
//        'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 
//        'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 
//        'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 
//        'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n',
//        'o', 'p', 'q', 'r', 's', 't', 'u', 'v',
//        '*', '*', '*', '*', '0', '1', '2', '3',
        '4', '5', '6', '7', '8', '9', '*', '*'
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
    		if( pcn % 1024 == 0)
    		{	for( int j = 0; j < num.length; j++ )
	    		{
	    			System.out.print(num[j]+"");
	    		}
    			System.out.println("\t【"+pcn+"】");
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
    	crack(base64, 0);
//    	for(int i = 0; i < baseChar.length; i++ )
//    	{
//    		System.out.print(baseChar[i]);
//    	}
		System.out.println("【"+pcn+"】");
    }
}
