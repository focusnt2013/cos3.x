package com.focus.cos.web.common;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

/**
 * Description:加载类
 * Create Date:Oct 17, 2008
 * @author Focus
 *
 * @since 1.0
 */
public class ClassLoaderUtil
{
    /**
     * 加载Java类。 使用全限定类名     * @return Class
     */
    public static Class<?> loadClass(String className)
    {
        try
        {
            return getClassLoader().loadClass(className);
        }
        catch(ClassNotFoundException e)
        {
            throw new RuntimeException("class not found '" + className + "'", e);
        }
    }

    /**
     * 得到类加载器
     * @return ClassLoader
     */
    public static ClassLoader getClassLoader()
    {
        return ClassLoaderUtil.class.getClassLoader();
    }

    /**
     * 提供相对于CLASSPATH的资源路径，返回文件的输入流
     * 如果需要查找CLASSPATH外部的资源，需要使用../来查找     * @param relativePath必须传递资源的相对路径。是相对于CLASSPATH的路径。     * 
     * @return 文件输入流     * @throws IOException
     * @throws MalformedURLException
     */
    public static InputStream getStream(String relativePath)
            throws MalformedURLException, IOException
    {
        if (!relativePath.contains("../"))
        {
            return getClassLoader().getResourceAsStream(relativePath);
        }
        else
        {
            return ClassLoaderUtil.getStreamByExtendResource(relativePath);
        }
    }

    /**
     * @param url
     * @return InputStream
     * @throwsIOException
     */
    public static InputStream getStream(URL url) throws IOException
    {
        if (url != null)
        {
            return url.openStream();
        }
        else
        {
            return null;
        }
    }

    /**
     * 如果需要查找CLASSPATH外部的资源，需要使用../来查找     * @param relativePath必须传递资源的相对路径。是相对于CLASSPATH的路径。     * @return
     * @throws MalformedURLException
     * @throws IOException
     */
    public static InputStream getStreamByExtendResource(String relativePath)
            throws MalformedURLException, IOException
    {
        return ClassLoaderUtil.getStream(ClassLoaderUtil.getExtendResource(relativePath));
    }

    /**
     * 提供相对于CLASSPATH的资源路径，返回属性对象，它是一个散列表 
     * @param resource 
     * @return Properties
     */
    public static Properties getProperties(String resource)
    {
        Properties properties = new Properties();
        try
        {
            properties.load(getStream(resource));
        }
        catch(IOException e)
        {
            throw new RuntimeException("couldn't load properties file '"
                    + resource + "'", e);
        }
        return properties;
    }

    /**
     * 得到本Class所在的ClassLoader的CLASSPATH的绝对路径。URL形式的 
     * @return String
     */
    public static String getAbsolutePathOfClassLoaderClassPath()
    {
        return ClassLoaderUtil.getClassLoader().getResource("").toString();
    }

    /**
     * 必须传递资源的相对路径。是相对于CLASSPATH的路径。如果需要查找CLASSPATH外部的资源，需要使 用../来查找     * @param relativePath
     * @return资源的绝对URL 
     * @throwsMalformedURLException
     */
    public static URL getExtendResource(String relativePath)
            throws MalformedURLException
    {
        if (!relativePath.contains("../"))
        {
            return ClassLoaderUtil.getResource(relativePath);
        }
        String classPathAbsolutePath = ClassLoaderUtil.getAbsolutePathOfClassLoaderClassPath();
        
        if (relativePath.substring(0, 1).equals("/"))
        {
            relativePath = relativePath.substring(1);
        }
        String wildcardString = relativePath.substring(0, relativePath.lastIndexOf("../") + 3);
        relativePath = relativePath.substring(relativePath.lastIndexOf("../") + 3);
        
        int containSum = ClassLoaderUtil.containSum(wildcardString, "../");
        classPathAbsolutePath = ClassLoaderUtil.cutLastString(classPathAbsolutePath, "/", containSum);
        String resourceAbsolutePath = classPathAbsolutePath + relativePath;
        URL resourceAbsoluteURL = new URL(resourceAbsolutePath);
        return resourceAbsoluteURL;
    }

    /**
     * @param String
     * @param String
     * @return int
     */
    private static int containSum(String source, String dest)
    {
        int containSum = 0;
        int destLength = dest.length();
        while (source.contains(dest))
        {
            containSum = containSum + 1;
            source = source.substring(destLength);
        }
        return containSum;
    }

    /**
     * @param String
     * @param String
     * @param int
     * @return String
     */
    private static String cutLastString(String source, String dest, int num)
    {
        // String cutSource=null;
        for (int i = 0; i < num; i++)
        {
            source = source.substring(0, source.lastIndexOf(dest, source.length() - 2) + 1);
        }
        return source;
    }

    /**
     * @param String
     * @return URL etc: file:/D:/workspace/my_depms/WebRoot/WEB-INF/doc
     */
    public static URL getResource(String resource)
    {
        return ClassLoaderUtil.getClassLoader().getResource(resource);
    }

    /**
     * @param String
     * @return String etc:D:/workspace/my_depms/WebRoot/WEB-INF/doc
     */
    public static String getSystemPath(String resource) throws Exception
    {
        String path = getExtendResource(resource).toString();
        String temp = path;
        if(path.startsWith("file:/"))
        {
        	temp = temp.substring(7);
        	if(!temp.startsWith(":"))return path.substring(path.indexOf("/"));
        }
        
        return path.substring(path.indexOf("/") + 1);
    }

    /**
     * @param args
     * @throws MalformedURLException
     */
    public static void main(String[] args) throws Exception
    {
        // ClassLoaderUtil.getExtendResource("../spring/dao.xml");
        // ClassLoaderUtil.getExtendResource("../../../src/log4j.properties");
        ClassLoaderUtil.getExtendResource("log4j.properties");
        System.out.println(ClassLoaderUtil.getSystemPath("../../doc"));
    }

}
