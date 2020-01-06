package com.focus.cos.web.common;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import com.focus.util.IOHelper;

public class Formater 
{
	public static void main(String args[])
	{
		try {
			String result = Formater.code(new FileInputStream("D:/focusnt/cos/trunk/CODE/main/WebContent/jsp/digg/scriptset.js"), 4, 0);
			IOHelper.writeFile(new File("D:/focusnt/cos/trunk/CODE/main/WebContent/jsp/digg/scriptset(format).js"), result.getBytes("UTF-8"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 格式化处理指定字符串
	 * @param codestr
	 * @return
	 */
	public static String code(String codestr, int indent, int i)
	{
		try {
			return code(new ByteArrayInputStream(codestr.getBytes("UTF-8")), indent, i);
		} catch (UnsupportedEncodingException e) {
			return codestr;
		}
	}
	
	/**
	 * 处理得到格式化的程序代码
	 * @param is
	 * @param indent 缩进
	 * @return
	 */
	public static String code(InputStream is, int indent, int i)
	{
		StringBuffer result = new StringBuffer();
		int c = -1, eof = 0;
		char ch = 0;
		boolean trim = false;
		if( indent > 0 )
		{
			indent(result, indent);
		}
		BufferedReader reader = null;
		String line = null;
		try
		{
			reader = new BufferedReader( new InputStreamReader(is, "UTF-8") );
			while( (line = reader.readLine()) != null )
			{
				line = line.trim();
				result.append("\r\n");
				indent(result, i);
				result.append(line.trim());
			}
			/*
			while( (c=is.read()) !=-1 )
			{
				if( c == '{' )
				{
					indent += 4;
					result.append("{\r\n");
					indent(result, indent);
					trim = true;
				}
				else if( c == '}' )
				{
					if( eof > 0)
					{
						result.delete(eof, result.length());
					}
					trim = true;
					indent -= 4;
					result.append("\r\n");
					indent(result, indent);
					result.append("}");
					eof = result.length();
					result.append("\r\n");
					indent(result, indent);
				}
				else if( c == ';' )
				{
					result.append(";\r\n");
					indent(result, indent);
					trim = true;
				}
				else
				{
					ch = (char)c;
					if( trim && (ch == ' ' || ch == '\r' || ch == '\n' || ch == '\t') )
					{
						continue;
					}
					trim = false;
					if( eof > 0 && ch == ')' )
					{
						result.delete(eof, result.length());
					}
					eof = 0;
					result.append(ch);
				}
			}
			*/
		} 
		catch (Exception e) {
		}
		finally {
			try {
				is.close();
			} catch (IOException e) {
			}
		}
		return result.toString();
	}
	
	private static void indent(StringBuffer sb, int i)
	{
		while( i-- > 0 ){
			sb.append(" ");
		}
	}
	public static String json(String jsonstr)
	{
		return jsonstr;
	}
}
