package com.focus.util;

import java.io.File;
import java.util.ArrayList;

public class ClassUtilsTest
{
	public static void main(String args[])
	{
		try
		{
//			Class<?> jobDriver = Class.forName("com.focus.hadoop.mapreduce.JobDriver");
//			List<Class<?>> classes = ClassUtils.getAllClassExctends(jobDriver);
//			for(Class<?> c : classes)
//			{
//				System.out.print(c.getSimpleName());
//				System.out.print('/t');
//				System.out.println(c.getName());
//			}
			ArrayList<String> buffer = new ArrayList<String>();
			ClassUtils.findJars(buffer, "org.apache.zookeeper.proto.SetWatches",
                new File("D:/focusnt/cos/lib"), true);
			for( String s : buffer )
			{
				System.out.println(s);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
