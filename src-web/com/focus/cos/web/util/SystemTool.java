package com.focus.cos.web.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class SystemTool
{
	public static String getOSName()
	{
		return System.getProperty("os.name");
	}

	public static String getUnixMACAddress()
	{
		String mac = null;
		BufferedReader bufferedReader = null;
		Process process = null;
		try
		{
			process = Runtime.getRuntime().exec("ifconfig eth0");// Linux下的命令，一般取eth0作为本地主网卡
			// 显示信息中包含有MAC地址信息
			bufferedReader = new BufferedReader(new InputStreamReader(process
					.getInputStream()));
			String line = null;
			int index = -1;
			while ((line = bufferedReader.readLine()) != null)
			{
				index = line.toLowerCase().indexOf("hwaddr");// 寻找标示字符串[hwaddr]
				if (index >= 0)// 找到了
				{
					mac = line.substring(index + "hwaddr".length() + 1).trim();// 取出MAC地址并去除2边空格
					break;
				}
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if (bufferedReader != null)
				{
					bufferedReader.close();
				}
			}
			catch (IOException e1)
			{
				e1.printStackTrace();
			}
			bufferedReader = null;
			process = null;
		}

		return mac;
	}

	/**
	 * 获取Windows网卡的MAC地址.
	 * 
	 * @return MAC地址
	 */
	public static String getWindowsMACAddress()
	{
		String mac = null;
		BufferedReader bufferedReader = null;
		Process process = null;
		try
		{
			process = Runtime.getRuntime().exec("ipconfig /all");// windows下的命令，显示信息中包含有MAC地址信息
			bufferedReader = new BufferedReader(new InputStreamReader(process
					.getInputStream()));
			String line = null;
			int index = -1;
			while ((line = bufferedReader.readLine()) != null)
			{
				index = line.toLowerCase().indexOf("physical address");// 寻找标示字符串[physical
				// address]
				if (index >= 0)//找到了
				{
					index = line.indexOf(":");// 寻找":"的位置
					if (index >= 0)
					{
						mac = line.substring(index + 1).trim();// 取出MAC地址并去除2边空格
					}
					break;
				}
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if (bufferedReader != null)
				{
					bufferedReader.close();
				}
			}
			catch (IOException e1)
			{
				e1.printStackTrace();
			}
			bufferedReader = null;
			process = null;
		}

		return mac;
	}

	public static void main(String[] argc)
	{
		String os = getOSName();
		System.out.println(os);
		if (os.startsWith("Windows"))
		{
			// 本地是windows
			String mac = getWindowsMACAddress();
			System.out.println(mac);
		}
		else
		{
			// 本地是非windows系统 一般就是Unix
			String mac = getUnixMACAddress();
			System.out.println(mac);
		}
	}
}
