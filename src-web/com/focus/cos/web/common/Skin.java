package com.focus.cos.web.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.focus.cos.web.common.PathFactory;
import com.focus.util.IOHelper;
import com.focus.util.Tools;

public class Skin implements java.io.Serializable
{
	private static final Log log = LogFactory.getLog(Skin.class);
	private static final long serialVersionUID = -1539507145543843120L;
	private static Skin instance;
	private String name;//皮肤名称
	private String version;//版本
	private String author;//作者
	private String color;//皮肤色调
	
	/**
	 * 得到皮肤对象的唯一实例
	 * @return
	 */
	public static Skin getInstance()
	{
		if( instance != null ) return instance;
		try
		{
			File skinFile = new File(PathFactory.getDataPath(), "skin");
			if( skinFile.exists() )
			{
				instance = (Skin)IOHelper.readSerializableNoException(skinFile);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return instance;
	}
	
	public String getName()
	{
		return name;
	}
	public void setName(String name)
	{
		this.name = name;
	}
	public String getVersion()
	{
		return version;
	}
	public void setVersion(String version)
	{
		this.version = version;
	}
	public String getAuthor()
	{
		return author;
	}
	public void setAuthor(String author)
	{
		this.author = author;
	}
	public String getColor()
	{
		return color;
	}
	public void setColor(String color)
	{
		this.color = color;
	}
	
	public static boolean changeSkin(String name)
		throws Exception
	{
		File webappDir = PathFactory.getWebappPath();
		File skinsDir = new File(webappDir, "skin/");
		File skinDir = new File( skinsDir, name);
		if( !skinsDir.exists() )
		{//皮肤目录找不到，说明是war 加载
			log.warn("Failed to find skin path("+skinsDir.getPath()+").");
			return false;
		}
		if( !skinsDir.exists() || !skinDir.exists() )
		{//皮肤主目录存在但是，name对应的目录不存在则说明系统配置出问题了。
			log.error("Failed to setup the program for not found the environment of skin("+skinDir.getPath()+").");
			System.err.println("Failed to setup the program for not found the environment of skin("+skinDir.getPath()+").");
			System.exit(-1);
			return false;
		}
		File readmeFile = new File(skinDir, "readme.txt");
		if( !readmeFile.exists() )
		{//从readme文件读取皮肤的配置信息
			log.warn("Failed to setup the program for not found the environment of skin("+readmeFile.getPath()+").");
			System.err.println("Failed to setup the program for not found the environment of skin("+readmeFile.getPath()+").");
			System.exit(-1);
			return false;
		}
		instance = new Skin();
		instance.setName(name);
		Properties properites = new Properties();
		java.io.BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(readmeFile)));
		properites.load(reader);
		instance.setAuthor(properites.getProperty("author"));
		instance.setColor(properites.getProperty("color"));
		instance.setVersion(properites.getProperty("version"));
		//写皮肤配置到序列化文件保存
		IOHelper.writeSerializable(new File(PathFactory.getDataPath(), "skin"), instance);
//		File war = new File(webappDir.getParentFile(), "cos.war");
//		if( war.exists() )
//		{//如果是cos.war版本，那么不需要处理css各级样式
//			log.warn("Not need to handle the skin.");
//			return true;
//		}
//		long ts = System.currentTimeMillis();
		/*delete by liux for not need.
		File cssDir = new File(webappDir, "css/");
		if( !cssDir.exists() )
		{
			cssDir.mkdirs();
		}
		File cssSkinDir = new File(skinDir, "css/");
		//列表皮肤目录下所有的css文件
		File cssfiles[] = cssSkinDir.listFiles();
		//将皮肤中的css拷贝到css目录
		for(File file : cssfiles )
		{
			if( file.isFile() )
			{
				File copyFile = new File(cssDir, file.getName());
				Tools.copyFile(file, copyFile);
			}
		}*/
		File cssSkinDir = new File(skinDir, "css/");
		File cssfiles[] = cssSkinDir.listFiles();
		//将样式文件拷贝到其他应用目录
		File webappsDir = webappDir.getParentFile();
		File appDirs[] = webappsDir.listFiles();
		StringBuffer sb = new StringBuffer();
		sb.append("The skin("+webappsDir+") need to config("+appDirs.length+").");
		String skinExclude = System.getProperty("skin.exclude");
		for( File appDir : appDirs )
		{
			if( appDir.getName().equals("cos") ||
				appDir.getName().equals("ROOT") ||
				appDir.isFile() ||
				appDir.getName().startsWith(".") ||
				(skinExclude!=null && skinExclude.indexOf(appDir.getName()) != -1))
			{
				continue;
			}
			File cssDir = new File(appDir, "css/");
			sb.append("\r\n\tConfig skin.css to "+cssDir.getPath()+" from "+cssSkinDir.getPath()+"("+cssfiles.length+")");
			if( !cssDir.exists() )
			{
				cssDir.mkdir();
			}
			//将皮肤中的css拷贝到css目录
			for(File file : cssfiles )
			{
				if( file.isFile() )
				{
					File copyFile = new File(cssDir, file.getName());
					Tools.copyFile(file, copyFile);
					sb.append("\r\n\t\tCopy file "+file.getPath()+" to "+copyFile.getPath());
				}
				else
				{
					File copyDir = new File(cssDir, file.getName());
					if( !copyDir.exists() )
						copyDir.mkdirs();
					File cssfiles1[] = file.listFiles();
					sb.append("\r\n\t\tCopy dir "+file.getPath()+" to "+copyDir.getPath());
					for( File file1 : cssfiles1 )
					{
						if( file1.isFile() )
						{
							File copyFile = new File(copyDir, file1.getName());
							Tools.copyFile(file1, copyFile);
							sb.append("\r\n\t\t\tCopy file "+file.getPath()+" to "+copyFile.getPath());
						}
					}
				}
			}
			//将皮肤中手机样式图片拷贝到指定应用中
			File skinMobileDir = new File(skinDir, "mobile/");
			if( skinMobileDir.exists() )
			{
				File targetSkinMobileDir = new File(appDir, "skin/"+skinDir.getName()+"/mobile");
				if( !targetSkinMobileDir.equals(skinMobileDir) )
				{
					if( !targetSkinMobileDir.exists() )
					{//如果目标的皮肤按钮目录不存在，则创建目录
						sb.append("\r\n\tCreate skin mobile path "+targetSkinMobileDir.getPath()+".");
						targetSkinMobileDir.mkdirs();
					}
					sb.append("\r\n\tConfig skin.mobile to "+targetSkinMobileDir.getPath()+" from "+skinMobileDir.getPath());
					for( File file : skinMobileDir.listFiles() )
					{
						File copyFile = new File(targetSkinMobileDir, file.getName());
						if( file.isFile() )
						{
							Tools.copyFile(file, copyFile);
							sb.append("\r\n\t\tCopy file "+file.getPath()+" to "+copyFile.getPath());
						}
					}
				}
			}
			else
			{
				sb.append("\r\n\tFailed to find the mobile of skin from "+skinMobileDir.getPath()+".");
			}
			//将皮肤中按钮图片拷贝到指定应用中
			File skinDialogDir = new File(skinDir, "boxes/dialog");
			if( skinDialogDir.exists() )
			{
				File targetSkinDialogDir = new File(appDir, "skin/"+skinDir.getName()+"/boxes/dialog");
				if( targetSkinDialogDir.equals(skinDialogDir) )
				{
					continue;
				}						
				if( !targetSkinDialogDir.exists() )
				{//如果目标的皮肤按钮目录不存在，则创建目录
					sb.append("\r\n\tCreate skin dialog path "+targetSkinDialogDir.getPath()+".");
					targetSkinDialogDir.mkdirs();
				}
				sb.append("\r\n\tConfig skin.dialog to "+targetSkinDialogDir.getPath()+" from "+skinDialogDir.getPath());
				for( File file : skinDialogDir.listFiles() )
				{
					File copyFile = new File(targetSkinDialogDir, file.getName());
					if( file.isFile() )
					{
						Tools.copyFile(file, copyFile);
						sb.append("\r\n\t\tCopy file "+file.getPath()+" to "+copyFile.getPath());
					}
				}
			}
			else
			{
				sb.append("\r\n\tFailed to find the dialog of skin from "+skinDialogDir.getPath()+".");
			}
			//将皮肤中按钮图片拷贝到指定应用中
			File skinTitleDir = new File(skinDir, "boxes/titled");
			if( skinTitleDir.exists() )
			{
				File targetSkinDialogDir = new File(appDir, "skin/"+skinDir.getName()+"/boxes/titled");
				if( targetSkinDialogDir.equals(skinTitleDir) )
				{
					continue;
				}						
				if( !targetSkinDialogDir.exists() )
				{//如果目标的皮肤按钮目录不存在，则创建目录
					sb.append("\r\n\tCreate skin titled path "+targetSkinDialogDir.getPath()+".");
					targetSkinDialogDir.mkdirs();
				}
				sb.append("\r\n\tConfig skin.titled to "+targetSkinDialogDir.getPath()+" from "+skinTitleDir.getPath());
				for( File file : skinTitleDir.listFiles() )
				{
					File copyFile = new File(targetSkinDialogDir, file.getName());
					if( file.isFile() )
					{
						Tools.copyFile(file, copyFile);
						sb.append("\r\n\t\tCopy file "+file.getPath()+" to "+copyFile.getPath());
					}
				}
			}
			else
			{
				sb.append("\r\n\tFailed to find the buttons of skin from "+skinTitleDir.getPath()+".");
			}
			//将皮肤中按钮图片拷贝到指定应用中
			File skinButtonsDir = new File(skinDir, "buttons/");
			if( skinButtonsDir.exists() )
			{
				File targetSkinButtonsDir = new File(appDir, "skin/"+skinDir.getName()+"/buttons");
				if( targetSkinButtonsDir.equals(skinButtonsDir) )
				{
					continue;
				}						
				if( !targetSkinButtonsDir.exists() )
				{//如果目标的皮肤按钮目录不存在，则创建目录
					log.info("Create skin buttons path "+targetSkinButtonsDir.getPath()+".");
					targetSkinButtonsDir.mkdirs();
				}
				sb.append("\r\n\tConfig skin.button to "+targetSkinButtonsDir.getPath()+" from "+skinButtonsDir.getPath());
				for( File file : skinButtonsDir.listFiles() )
				{
					File copyFile = new File(targetSkinButtonsDir, file.getName());
					if( file.isFile() )
					{
						Tools.copyFile(file, copyFile);
						sb.append("\r\n\t\tCopy file "+file.getPath()+" to "+copyFile.getPath());
					}
				}
			}
			else
			{
				sb.append("\r\n\tFailed to find the buttons of skin from "+skinButtonsDir.getPath()+".");
			}
			sb.append("\r\n");
		}
		log.debug(sb.toString());
		return true;
	}
}
