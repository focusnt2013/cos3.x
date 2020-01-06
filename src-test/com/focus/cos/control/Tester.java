package com.focus.cos.control;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.Character.UnicodeBlock;
import java.net.ServerSocket;
import java.net.URLDecoder;
import java.net.URLEncoder;

import com.focus.util.Tools;

public class Tester {

	private String status;
	private long status_timestamp;
    public void status(String info, Object... args){
    	status_timestamp = System.currentTimeMillis();
    	status = String.format(info, args);
	}
    
	public static void main(String[] args)
	{
		String subject = "【知标局】测试";
		String nickname = null;
		if( subject.startsWith("【") ){
			int i = subject.indexOf("】");
			if( i != -1 ){
				nickname = subject.substring(1, i);
				subject = subject.substring(i+1);
			}
		}
		System.out.println(nickname+": "+subject);
//		String url = "https://book.douban.com/subject/24872769/, 文件";
//		try {
//			System.out.println(URLDecoder.decode(url, "UTF-8"));
//			url = URLEncoder.encode(url, "UTF-8");
//			System.err.println(url);
//		} catch (UnsupportedEncodingException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		System.out.println(Tools.getFormatTime(1573257600000L));
//		File file = new File("D:\\1542861101");
//		byte[] payload = IOHelper.readAsByteArray(file);
//		JSONObject json = new JSONObject(Base64X.decode2str(new String(payload)));
//		System.out.println(json.toString(4));
//		System.out.println(Base64X.decode2str(json.getString("wx_pay_key")));
		
//		Calendar c = Calendar.getInstance();
//		c.add(Calendar.YEAR, -1);
//		c.set(Calendar.MONTH, Calendar.SEPTEMBER);
//		c.set(Calendar.DAY_OF_MONTH, 22);
//		file.setLastModified(c.getTimeInMillis());
//		Tester t = new Tester();
//		t.status("aaa%s", 1);
//		System.out.println(t.status);
		
////		String a = "6442450944";
////		System.err.println(Integer.parseInt(a));
//		String s = "333/*.*";
//		int i = s.lastIndexOf("/");
//		int k = s.indexOf("*");
//		k += 1;
//		int l = s.lastIndexOf("*");
//		l += 1;
//		String p = k<l?s.substring(k, l-1):(k==s.length()?s.substring(i+1, k-1):s.substring(k));
//		System.err.println(p);
//
////		StackTraceElement[] stes = new Exception().getStackTrace();
////		if( stes != null && stes.length > 0 )
////		{
////			for(int i = 0; i < stes.length; i++)
////			{
////				StackTraceElement e = stes[i];
////				String c = String.format("%s!%s()", e.getClassName(), e.getMethodName());
////				System.out.println(c);
////			}
////		}
//		
////		F fileIdentity = new F("d:\\identity");
//////		byte[] payload = IOHelper.readAsByteArray(fileIdentity);
////		//读取数字证书并初始化
////		try{
////			Key key = (Key)IOHelper.readSerializable(fileIdentity);
////			Cipher c = Cipher.getInstance("DES");
////			c.init(Cipher.WRAP_MODE, key);//再用数字证书构建另外一个DES密码器
////			System.out.println(Base64.encode(c.wrap(key)));
////		}
////		catch(Exception e){
////			e.printStackTrace();
////		}
////		JSONObject pids = new JSONObject();
////		pids.put("1001", "");
////        System.out.println(pids.toString(4));
////		String type = "系统告警";
////		String sysid = "Sys";
////		String str =type+sysid;
////		String md5 = Tools.encodeMD5(str);
////		String s = encodeUnicode("嵌入式版权管理系统");
////		System.err.println(s);
////		System.err.println(decodeUnicode(s));
////		String t = "d:\\test\\a.txt";
////		t = t.replace("\\", "/");
////		System.out.println(t);
	}
	
	static class ControlSocket extends ServerSocket
    {
		public ControlSocket() throws IOException {
			super(10000);
		}
		
		public void close()
		{
			String logtxt = "Close the socket("+super.getLocalPort()+") of control.";
			try
			{
				super.close();
			}
			catch(Exception e)
			{
				logtxt += "\r\n\t"+e.getMessage();
			}
			System.err.println(logtxt);
		}
    }
    /**
     * unicode 转换成 中文
     * 
     * @author leon 2016-3-15
     * @param theString
     * @return
     */
    public static String decodeUnicode(String theString) {
        char aChar;
        int len = theString.length();
        StringBuffer outBuffer = new StringBuffer(len);
        for (int x = 0; x < len;) {
            aChar = theString.charAt(x++);
            if (aChar == '\\') {
                aChar = theString.charAt(x++);
                if (aChar == 'u') {
                    // Read the xxxx
                    int value = 0;
                    for (int i = 0; i < 4; i++) {
                        aChar = theString.charAt(x++);
                        switch (aChar) {
                            case '0':
                            case '1':
                            case '2':
                            case '3':
                            case '4':
                            case '5':
                            case '6':
                            case '7':
                            case '8':
                            case '9':
                                value = (value << 4) + aChar - '0';
                                break;
                            case 'a':
                            case 'b':
                            case 'c':
                            case 'd':
                            case 'e':
                            case 'f':
                                value = (value << 4) + 10 + aChar - 'a';
                                break;
                            case 'A':
                            case 'B':
                            case 'C':
                            case 'D':
                            case 'E':
                            case 'F':
                                value = (value << 4) + 10 + aChar - 'A';
                                break;
                            default:
                                throw new IllegalArgumentException("Malformed   \\uxxxx   encoding.");
                        }
                    }
                    outBuffer.append((char) value);
                } else {
                    if (aChar == 't') aChar = '\t';
                    else if (aChar == 'r') aChar = '\r';
                    else if (aChar == 'n') aChar = '\n';
                    else if (aChar == 'f') aChar = '\f';
                    outBuffer.append(aChar);
                }
            } else outBuffer.append(aChar);
        }
        return outBuffer.toString();
    }
    /**
     * 中文转换成 unicode
     * 
     * @author leon 2016-3-15
     * @param inStr
     * @return
     */
    public static String encodeUnicode(String inStr) {
        char[] myBuffer = inStr.toCharArray();

        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < inStr.length(); i++) {
            char ch = myBuffer[i];
            if (ch < 10) {
                sb.append("\\u000" + (int) ch);
                continue;
            }
            UnicodeBlock ub = UnicodeBlock.of(ch);
            if (ub == UnicodeBlock.BASIC_LATIN) {
                // 英文及数字等
                sb.append(myBuffer[i]);
            } else if (ub == UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {
                // 全角半角字符
                int j = myBuffer[i] - 65248;
                sb.append((char) j);
            } else {
                // 汉字
                int s = myBuffer[i];
                String hexS = Integer.toHexString(Math.abs(s));
                String unicode = "\\u" + hexS;
                sb.append(unicode.toLowerCase());
            }
        }
        return sb.toString();
    }
}
