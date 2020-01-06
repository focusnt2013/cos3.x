package com.focus.util;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: Kehaoinfo</p>
 *
 * @author David Lau
 * @version 1.0
 */
import java.util.HashMap;
import java.util.Map;

public class LZW
{
    private static boolean xmlsafe = false;

    public static String compress( String str )
    {
        Map dico = new HashMap();
        int skipnum = xmlsafe ? 5 : 0;
        for( char i = 0; i < 256; ++i )
        {
            dico.put( Character.toString( i ), new Character( i ) );
        }
        if( xmlsafe )
        {
            dico.put( "<", new Character( ( char ) 256 ) );
            dico.put( ">", new Character( ( char ) 257 ) );
            dico.put( "&", new Character( ( char ) 258 ) );
            dico.put( "\"", new Character( ( char ) 259 ) );
            dico.put( "\"", new Character( ( char ) 260 ) );

        }
        String res = "";
        String txt2encode = str;
        String[] splitStr =
            txt2encode.split( "" );
        int len = splitStr.length;
        int nbChar =
            256 + skipnum;
        String buffer = "";
        for( int i = 1; i <= len;
                     i++ )
        {
            String current;
            if( i <= len - 1 )
            {
                current = splitStr[i];
            }
            else
            {
                current
                    = null;
            }
            if( dico.get( buffer + current ) != null )
            {
                buffer += current;
            }
            else
            {
                res += ( ( Character ) dico.get( buffer ) ).toString();
                dico.put( buffer + current, new Character( ( char ) nbChar ) );
                nbChar++;
                buffer = current;
            }

        }
        return res;
    }

    public static String decompress( String str )
    {
        Map dico = new HashMap();
        int skipnum = xmlsafe ? 5 : 0;
        for( int
             i = 0; i < 256; ++i )
        {
            dico.put( Integer.toString( i ), Character.toString( ( char ) i ) );
        }
        if( xmlsafe )
        {
            dico.put( "256", "<" );
            dico.put( "257", ">" );
            dico.put( "258", "&" );
            dico.put( "259", "\"" );
            dico.put( "260", "\"" );
        }
        String txt2encode = str;
        String[] splitStr = txt2encode.split( "" );
        int len = splitStr
                  .length;
        int nbChar = 256 + skipnum;
        String buffer =
            "";
        String chaine = "";
        String res = "";
        for( int
             i = 1;
                 i < len; i++ )
        {
            int code = txt2encode.charAt( i - 1 );
            String current = (
                String ) dico.get( Integer.toString( code ) );
            if( buffer ==
                "" )
            {
                buffer = current;
                res += current;
            }
            else
            {
                if( code <= 255 + skipnum )
                {
                    res += current;
                    chaine = buffer + current;
                    dico.put(
                        "" + nbChar, chaine );
                    nbChar++;
                    buffer = current;
                }
                else
                {
                    chaine = ( String ) dico.get( "" + code );
                    if( chaine == null )
                    {
                        chaine = buffer + buffer.substring( 0, 1 );
                    }
                    res += chaine;
                    dico.put( "" + nbChar,
                              buffer + chaine.substring
                              ( 0, 1 ) );
                    nbChar++;
                    buffer = chaine;
                }
            }

        }
        return res;
    }

    public static void main(String[] args)
    {
        //19647819_4480852997
        //19647819_4480850304
        //System.out.print(Long.toHexString(18788761>>8));
        //System.out.println(Long.toHexString(System.currentTimeMillis()>>8));
        String s = LZW.compress("24056908_4480822924");
        System.out.println(s);
        String s1 = LZW.decompress(s);
        System.out.println(s1);
    }
}
