package com.focus.cos;

import java.io.File;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;

import javax.crypto.Cipher;

import com.focus.util.Base64;
import com.focus.util.IOHelper;
import com.focus.util.Tools;

public class IdendityTest
{
	public static void main(String[] args)
	{
		try 
		{
			Key identity = (Key)IOHelper.readSerializable(new File("data/identity"));
			Cipher c0 = Cipher.getInstance("DES");  
			c0.init(Cipher.WRAP_MODE, identity);//再用数字证书构建另外一个DES密码器
            String cosId = Base64.encode(c0.wrap(identity));
            System.out.println(cosId);
            c0.init(Cipher.ENCRYPT_MODE, identity);
			byte[] s = c0.doFinal("刘问章".getBytes("UTF-8"));
			Tools.printb(s);
			
    		Cipher c1 = Cipher.getInstance("DES");
			c1.init(Cipher.DECRYPT_MODE, identity);

    		s = c1.doFinal(s);
			System.out.println(new String(s, "UTF-8"));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		try 
		{
			KeyPairGenerator keyPair = KeyPairGenerator.getInstance("RSA");
            SecureRandom random = new SecureRandom();
            keyPair.initialize(512, random);
            KeyPair keyP = keyPair.generateKeyPair();
            Key privateKey = keyP.getPrivate();
            Key publicKey = keyP.getPublic();

    		//利用加密KEY生成加密模式
    		Cipher c = Cipher.getInstance("RSA");
    		c.init(Cipher.ENCRYPT_MODE, publicKey);
    		byte[] text = c.doFinal("刘问章".getBytes("UTF-8"));
    		
    		c = Cipher.getInstance("RSA");
    		c.init(Cipher.DECRYPT_MODE, privateKey);
			String result = new String(c.doFinal(text), "UTF-8");
            System.out.println("result:"+result);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

}
