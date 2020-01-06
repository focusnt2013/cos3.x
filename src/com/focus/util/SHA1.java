package com.focus.util;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * 
 * @author think
 *
 */
public class SHA1 
{
   public static String getHmacSHA1(String loginname, String password, String algorithm){
        byte[] keyBytes = password.getBytes();
        Key key = new SecretKeySpec(keyBytes, 0, keyBytes.length, algorithm);
        Mac mac=null;
        try {
            mac = Mac.getInstance(algorithm);
            mac.init(key);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        return byteArrayToHex(mac.doFinal(loginname.getBytes()));
    }

   /**
     * 16进制加密
     * @param a
     * @return
     */
    protected static String byteArrayToHex(byte [] a) {
        int hn, ln, cx;
        String hexDigitChars = "0123456789abcdef";
        StringBuffer buf = new StringBuffer(a.length * 2);
        for(cx = 0; cx < a.length; cx++) {
            hn = ((int)(a[cx]) & 0x00ff) /16 ;
            ln = ((int)(a[cx]) & 0x000f);
            buf.append(hexDigitChars.charAt(hn));
            buf.append(hexDigitChars.charAt(ln));
        }
        return buf.toString();

    }
    
    public static void main(String[] args){
        String loginKey= getHmacSHA1("密码", "用户名", "HmacSHA1");
        System.out.println(loginKey);//53b3a8413cf49e939d8711a0ba34916b2ec2db75
        String loginKey2= getHmacSHA1("NTc2YmI5MmRlNjI5NGJmYzgyMzU1ZjNlYzQ3ZTFjNGYzYbqj1523105650526", "YzQ4MDcxMGNkMTdlNDVhYzgzMTdlNzI2MWU3YWFmOTk2N", "HmacSHA1");
        System.out.println(loginKey2);//c29d294ea11dce4c9e2ed298298016cf631983e3
        
    }
}
