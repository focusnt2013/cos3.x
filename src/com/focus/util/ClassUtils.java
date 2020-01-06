package com.focus.util;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ClassUtils
{
	public static void main(String args[])
	{
		try
		{
			String mode = "findjar";//"findjar"/"findConflict"
			int index = 0;
			if(args.length > index)
			{
				mode = args[index];
			}
			index++;
			
			if(mode.equalsIgnoreCase("findjar"))
			{
				
				String classCompare = "org.apache.commons.codec.DecoderException";
				classCompare = Tools.replaceStr(classCompare, ".", "/");
				String libPath = "D:/focusnt/cos/trunk/CODE";
				
				if(args.length > index)
				{
					classCompare = args[index];
				}
				index++;
				
				if(args.length > index)
				{
					libPath = args[index];
				}
				index++;
				
				ArrayList<String> buffer = new ArrayList<String>();
				ClassUtils.findJars(buffer, classCompare, new File(libPath), true);
				for(String jar : buffer )
					System.out.println(jar );
			}
			else if(mode.equalsIgnoreCase("findConflict"))
			{
				String pathList = "D:/myProject/xdata/trunk/CODE/xdata/lib;D:/myProject/xdata/trunk/CODE/xdata/WebRoot/WEB-INF/lib";
				if(args.length > index)
				{
					pathList = args[index];
				}
				index++;
				
				String jarPathArray[] = null;
				if(pathList.indexOf(";") != -1)
				{
					jarPathArray = pathList.split(";");
				}
				else if(pathList.indexOf(":") != -1)
				{
					jarPathArray = pathList.split(":");
				}
				else
				{
					return;
				}
				
				List<String> jarPathList = new ArrayList<String>();
				for(int i=0;i<jarPathArray.length;i++)
				{
					jarPathList.add(jarPathArray[i]);
				}
				printConflictClasses(jarPathList, true);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * 给一个类，返回继承这个类的所有类
	 * @param c 基类
	 * @param packages 从那些包中查找
	 * @return
	 */
	public static void findClassesByExctends(Class<?> c, String packages[], ArrayList<Class<?>> classes)
	{
		for(String packageName : packages)
		{
			findClassesByExctends(c, packageName, classes);
		}
	}
	
	/**
	 * 
	 * @param c
	 * @param packageName
	 * @param classFounds 找的的类
	 */
	public static void findClassesByExctends(Class<?> c, String packageName, ArrayList<Class<?>> classFounds)
	{
		try
		{
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			String name = packageName.replace('.', '/');
			System.out.println("Todo find "+name);
			Enumeration<URL> resources = classLoader.getResources(name);
			while (resources.hasMoreElements())
			{
				URL resource = resources.nextElement();
				String path = resource.toString();
				if( path.startsWith("jar:") )
				{
					path = path.substring(4).substring(5);
					int i = path.lastIndexOf('!');
					path = path.substring(0, i);
					System.out.println("/t"+path);
					ZipFile zipFile = new ZipFile(path);
					Enumeration<?> en = zipFile.entries();
					while( en.hasMoreElements() )
					{
						ZipEntry entry = (ZipEntry)en.nextElement();
						String classFile = entry.toString();
						if(!entry.isDirectory() && 
						   classFile.endsWith(".class") && 
						   classFile.lastIndexOf('$') == -1)
						{
							Class<?> c1 = isAssignableFrom(c, classFile);
							if( c1 != null )
							{
//								System.out.println("/t/t"+classFile);
								classFounds.add(c1);
							}
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
		
	// 给一个接口，返回这个接口的所有实现类
	public static void findClassesByExctends(Class<?> c, List<Class<?>> classes)
	{
		String packageName = c.getPackage().getName(); // 获得当前的包名
//		System.out.println(packageName);
		try
		{
			List<Class<?>> allClass = getClasses(packageName); // 获得当前包下以及子包下的所有类
			// 判断是否是同一个接口
			for (int i = 0; i < allClass.size(); i++)
			{
				if (c.isAssignableFrom(allClass.get(i)))
				{ // 判断是不是一个接口
					if (!c.equals(allClass.get(i)))
					{ // 本身不加进去
						classes.add(allClass.get(i));
					}
				}
			}
		}
		catch (ClassNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	// 给一个接口，返回这个接口的所有实现类
	public static List<Class<?>> getAllClassExctends(Class<?> c)
	{
		List<Class<?>> classes = new ArrayList<Class<?>>(); // 返回结果
		findClassesByExctends(c, classes);
		return classes;
	}
	// 给一个接口，返回这个接口的所有实现类
	public static List<Class<?>> getAllClassByInterface(Class<?> c)
	{
		List<Class<?>> returnClassList = new ArrayList<Class<?>>(); // 返回结果
		// 如果不是一个接口，则不做处理
		if (c.isInterface())
		{
			String packageName = c.getPackage().getName(); // 获得当前的包名
			try
			{
				List<Class<?>> allClass = getClasses(packageName); // 获得当前包下以及子包下的所有类
				// 判断是否是同一个接口
				for (int i = 0; i < allClass.size(); i++)
				{
					if (c.isAssignableFrom(allClass.get(i)))
					{ // 判断是不是一个接口
						if (!c.equals(allClass.get(i)))
						{ // 本身不加进去
							returnClassList.add(allClass.get(i));
						}
					}
				}
			}
			catch (ClassNotFoundException e)
			{
				e.printStackTrace();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		return returnClassList;
	}

	// 从一个包中查找出所有的类，在jar包中不能查找
	private static List<Class<?>> getClasses(String packageName) throws ClassNotFoundException,
		IOException
	{
		System.out.println("getClasses:"+packageName);
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		String path = packageName.replace('.', '/');
		Enumeration<URL> resources = classLoader.getResources(path);
		List<File> dirs = new ArrayList<File>();
		while (resources.hasMoreElements())
		{
			URL resource = resources.nextElement();
			System.out.println("/t"+resource.getFile());
			dirs.add(new File(resource.getFile()));
		}
		ArrayList<Class<?>> classes = new ArrayList<Class<?>>();
		for (File directory : dirs)
		{
			classes.addAll(findClasses(directory, packageName));
		}
		return classes;
	}

	private static List<Class<?>> findClasses(File directory, String packageName) 
		throws ClassNotFoundException
	{
		List<Class<?>> classes = new ArrayList<Class<?>>();
		if (!directory.exists())
		{
			return classes;
		}
		System.out.println("findClasses:"+directory);
		File[] files = directory.listFiles();
		for (File file : files)
		{
			if (file.isDirectory())
			{
				assert !file.getName().contains(".");
				classes.addAll(findClasses(file, packageName + "." + file.getName()));
			}
			else if (file.getName().endsWith(".class"))
			{
				String classpath = packageName + '.' + file.getName().substring(0, file.getName().length() - 6);
				System.out.println("/t"+classpath);
				classes.add(Class.forName(classpath));
			}
		}
		return classes;
	}
	

	private static Class<?> isAssignableFrom(Class<?> c, String className )
	{
		String classpath = className.replace('/', '.');
		classpath = classpath.substring(0, classpath.length()-6);
//		System.out.println("/t"+classpath);
		Class<?> c1 = null;
		try
		{
			c1 = Class.forName(classpath);
			if( c.isAssignableFrom(c1) && !c.equals(c1) )
			{
				return c1;
			}
		}
		catch (Throwable e)
		{
//			System.out.println(e.getClass().getSimpleName()+":"+className);
		}
		return null;
	}
	
	/**
	 * 
	 * @param classCompare 匹配的类
	 * @param libPath 包路径
	 * @param findSubpath 是否查找子目录
	 */
	public static void findJars(ArrayList<String> buffer, String classCompare, File libPath, boolean findSubpath)
	    throws Exception
	{
		classCompare = classCompare.replace('/', '.');
		File jars[] = libPath.listFiles();
		for( File jar : jars )
		{
			if( jar.isDirectory() )
			{
				if( !jar.getName().startsWith(".") && findSubpath )
				{//子目录是隐藏目录同时允许向下搜索
					findJars(buffer, classCompare, jar, findSubpath);
				}
				continue;
			}
			else if(!jar.getName().endsWith(".jar"))
			{//文件不是jar
				continue;
			}
//				System.out.println(jar.getPath());
			ZipFile zipFile = new ZipFile(jar);
			Enumeration<?> en = zipFile.entries();
			while( en.hasMoreElements() )
			{
				ZipEntry entry = (ZipEntry)en.nextElement();
				String classFile = entry.toString();
				if(!entry.isDirectory() && 
				   classFile.endsWith(".class") && 
				   classFile.lastIndexOf('$') == -1)
				{
					String classpath = classFile.replace('/', '.');
//						System.out.println("/t"+classpath);
					classpath = classpath.substring(0, classpath.length()-6);
					if( classpath.equals(classCompare) )
					{
						System.out.println(classpath);
						buffer.add(jar.getPath());
					}
				}
			}
		}
	}
	
	public static void printConflictClasses(List<String> pathList, boolean findSubpath) throws Exception
	{
		Map<String, List<String>> classToJarsMap = new HashMap<String, List<String>>();
		for(String strPath:pathList)
		{
			File path = new File(strPath);
			analysisClassesFrom(classToJarsMap, path, findSubpath);
		}
		
		Map<String, List<String>> jarsToConflictClassMap = new HashMap<String, List<String>>();
		QuickSort qs = new QuickSort()
		{
			public boolean compareTo(Object sortSrc, Object pivot) {
				return ((String)sortSrc).compareTo((String)pivot) >=0 ;
			}
		};
		
		for(String classPath:classToJarsMap.keySet())
		{
			List<String> jarPathList = classToJarsMap.get(classPath);
			if(jarPathList.size() > 1)
			{
				qs.sort(jarPathList);
//				Log.msg("ConflictClasses " + classPath);
//				System.out.println("ConflictClasses " + classPath);
//				for(int i=0;i<jarPathList.size();i++)
//				{
//					Log.msg("            jar-"+i +":"+ jarPathList.get(i));
//					System.out.println("            jar-"+i +":"+ jarPathList.get(i));
//				}
				
				
				String strJars = jarPathList.get(0);
				for(int i=1;i<jarPathList.size();i++)
				{
					strJars += "," + jarPathList.get(i);
				}
				
				List<String> conflictClassList = jarsToConflictClassMap.get(strJars);
				if(conflictClassList == null)
				{
					conflictClassList = new ArrayList<String>();
					jarsToConflictClassMap.put(strJars, conflictClassList);
				}
				
				conflictClassList.add(classPath);
			}
		}
		
		
		for(String jars:jarsToConflictClassMap.keySet())
		{
			List<String> conflictClasses = jarsToConflictClassMap.get(jars);
			Log.msg("ConflictJars " + jars);
			System.out.println("ConflictJars " + jars);
			for(String conflictClass:conflictClasses)
			{
				Log.msg("           class:" + conflictClass);
				System.out.println("           class:" + conflictClass);
			}
			
		}
	}
	public static void analysisClassesFrom(Map<String, List<String>> classToJarsMap , File path, boolean findSubpath) throws Exception
	{
		if(path.exists())
		{
			if(path.isDirectory())
			{
				File jars[] = path.listFiles();
				for( File jar : jars )
				{
					if( jar.isDirectory() )
					{
						if( !jar.getName().startsWith(".") && findSubpath )
						{//子目录是隐藏目录同时允许向下搜索
							analysisClassesFrom(classToJarsMap , jar, findSubpath);
						}
						continue;
					}
					else if(!jar.getName().endsWith(".jar"))
					{
						//文件不是jar
						continue;
					}
//							System.out.println(jar.getPath());
					ZipFile zipFile = new ZipFile(jar);
					Enumeration<?> en = zipFile.entries();
					while( en.hasMoreElements() )
					{
						ZipEntry entry = (ZipEntry)en.nextElement();
						String classFile = entry.toString();
						if(!entry.isDirectory() && 
						   classFile.endsWith(".class") && 
						   classFile.lastIndexOf('$') == -1)
						{
							String classpath = classFile.replace('/', '.');
//									System.out.println("/t"+classpath);
							classpath = classpath.substring(0, classpath.length()-6);
							List<String> jarPathList = classToJarsMap.get(classpath);
							if(jarPathList == null)
							{
								jarPathList = new ArrayList<String>();
								classToJarsMap.put(classpath, jarPathList);
							}
							
							jarPathList.add(jar.getAbsolutePath());
						}
					}
				}
			}
			else if(path.isFile())
			{
				if(path.getName().endsWith(".jar"))
				{
					ZipFile zipFile = new ZipFile(path);
					Enumeration<?> en = zipFile.entries();
					while( en.hasMoreElements() )
					{
						ZipEntry entry = (ZipEntry)en.nextElement();
						String classFile = entry.toString();
						if(!entry.isDirectory() && 
						   classFile.endsWith(".class") && 
						   classFile.lastIndexOf('$') == -1)
						{
							String classpath = classFile.replace('/', '.');
//									System.out.println("/t"+classpath);
							classpath = classpath.substring(0, classpath.length()-6);
							List<String> jarPathList = classToJarsMap.get(classpath);
							if(jarPathList == null)
							{
								jarPathList = new ArrayList<String>();
								classToJarsMap.put(classpath, jarPathList);
							}
							
							jarPathList.add(path.getAbsolutePath());
						}
					}
				}
			}
		}
		
	}
}
