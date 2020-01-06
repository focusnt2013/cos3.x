package com.focus.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.zip.ZipFile;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.ar.ArArchiveEntry;
import org.apache.commons.compress.archivers.cpio.CpioArchiveEntry;
import org.apache.commons.compress.archivers.jar.JarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipUtils;
import org.apache.commons.io.FileUtils;

/**
 * 
 * @author focus
 *
 */
public class Decompressor 
{
	/**
	 * 解压
	 * @param cfile
	 */

	public final static Progress execute(File cfile)
		throws Exception
	{
		Progress progress = new Progress(){

			@Override
			public void report(int p) {
				// TODO Auto-generated method stub
			}
		};
		execute(cfile, null, progress);
		return progress;
	}
	
	public static abstract class Progress
	{
		private StringBuffer logtxt = new StringBuffer();
		private File decompressFile = null;
		public void log(String msg)
		{
			logtxt.append(msg);
		}
		public abstract void report(int p);

		public String getLogtxt() {
			return logtxt.toString();
		}
		public String getName() {
			return decompressFile!=null?decompressFile.getName():"";
		}
		public boolean isFile(){
			return decompressFile!=null?decompressFile.isFile():false;
		}
		public long lastModified(){
			return decompressFile!=null?decompressFile.lastModified():0;
		}
		public String getParentName() {
			return decompressFile!=null?decompressFile.getParentFile().getName():"";
		}
		public File getDecompressFile() {
			return decompressFile;
		}
		public void setDecompressFile(File file) {
			this.decompressFile = file;
		}
	}
	
	/**
	 * 得到解压文件的个数
	 * @param cfile
	 * @return
	 */
	public static int getDecompressCount(File cfile) throws Exception
	{
		int count = 0;
		ArchiveInputStream ais = null;
		ZipFile zipFile = null;
		InputStream is = null;
		try 
		{
			String archiverName = null;
			String suffix = null;
			if( cfile.getName().indexOf(".tar") != -1 ){
				archiverName = "tar";
				if( cfile.getName().endsWith(".gz") )
				{
					is = new GzipCompressorInputStream(new FileInputStream(cfile));
				}
				else if( cfile.getName().endsWith(".zip") )
				{
					zipFile = new ZipFile(cfile);
					return zipFile.size();
				}
				else if( cfile.getName().endsWith(".bz2") )
				{
					is = new BZip2CompressorInputStream(new FileInputStream(cfile));
				}
				else
				{
					is = new FileInputStream(cfile);
				}
			}
			else if( cfile.getName().indexOf(".gz") != -1 ) {
				is = new FileInputStream(cfile);
				suffix = "gz";
			}
			else if( cfile.getName().indexOf(".bz2") != -1 ) {
				is = new FileInputStream(cfile);
				suffix = "bz2";
			}
			else if( cfile.getName().indexOf(".jar") != -1 ){
				archiverName = "jar";
				is = new FileInputStream(cfile);
			}
			else if( cfile.getName().indexOf(".zip") != -1 ){
				archiverName = "zip";
				is = new FileInputStream(cfile);
			}
			else if( cfile.getName().indexOf(".ar") != -1 ){
				archiverName = "ar";
				is = new FileInputStream(cfile);
			}
			else if( cfile.getName().indexOf(".cpio") != -1 ){
				archiverName = "cpio";
				is = new FileInputStream(cfile);
			}
			else
			{
				throw new Exception("不支持解压的文件格式("+cfile.getName()+").");
			}
			if( archiverName == null )
			{
				if( "gz".equalsIgnoreCase(suffix) )
				{
					return 1;
				}
				if( "bz2".equalsIgnoreCase(suffix) )
				{
					return 1;
				}
				throw new Exception("不支持解压的文件格式("+cfile.getName()+").");
			}
			ArchiveStreamFactory factory = new ArchiveStreamFactory();
			ais = factory.createArchiveInputStream(archiverName, is);
			while( ais.getNextEntry() != null )
			{
				count += 1;
			}
		}
		catch (Exception e) 
		{
			throw e;
		}
		finally
		{
			if( zipFile != null ) zipFile.close();
			if( ais != null )
				try {
					ais.close();
				} catch (IOException e) {
				}
			if( is != null )
				try {
					is.close();
				} catch (IOException e) {
				}
		}
		return count;
	}
	/**
	 * 解压到指定目录
	 * @param cfile
	 * @param destdir
	 * @return
	 * @throws Exception
	 */
	public final static void execute(File cfile, File destdir, Progress progress)
		throws Exception
	{
		if( progress == null ) throw new Exception("Found the object of progress is null(请您提供进度器)");
		if( !cfile.exists() ) throw new Exception("要执行解压的文件("+cfile.getAbsolutePath()+")不存在");
		ArchiveInputStream ais = null;
		InputStream is = null;
		try 
		{
			String archiverName = null;
			String suffix = null;
			int i = 0;
			if( (i=cfile.getName().indexOf(".tar")) != -1 ){
				archiverName = "tar";
				if( cfile.getName().endsWith(".gz") )
				{
					is = new GzipCompressorInputStream(new FileInputStream(cfile));
				}
				else if( cfile.getName().endsWith(".zip") )
				{
					is = new FileInputStream(cfile);
				}
				else if( cfile.getName().endsWith(".bz2") )
				{
					is = new BZip2CompressorInputStream(new FileInputStream(cfile));
				}
				else
				{
					is = new FileInputStream(cfile);
				}
			}
			else if( (i=cfile.getName().indexOf(".gz")) != -1 ) {
				is = new FileInputStream(cfile);
				suffix = "gz";
			}
			else if( (i=cfile.getName().indexOf(".bz2")) != -1 ) {
				is = new FileInputStream(cfile);
				suffix = "bz2";
			}
			else if( (i=cfile.getName().indexOf(".jar")) != -1 ){
				archiverName = "jar";
				is = new FileInputStream(cfile);
			}
			else if( (i=cfile.getName().indexOf(".zip")) != -1 ){
				archiverName = "zip";
				is = new FileInputStream(cfile);
			}
			else if( (i=cfile.getName().indexOf(".ar")) != -1 ){
				archiverName = "ar";
				is = new FileInputStream(cfile);
			}
			else if( (i=cfile.getName().indexOf(".cpio")) != -1 ){
				archiverName = "cpio";
				is = new FileInputStream(cfile);
			}
			else
			{
				throw new Exception("不支持解压的文件格式("+cfile.getName()+").");
			}
			progress.log("\r\n压缩文件名称:"+cfile.getName());
			progress.log("\r\n压缩文档类别: "+archiverName!=null?archiverName:suffix);
			
			if( destdir == null ) destdir = cfile.getParentFile();
			progress.log("\r\n解压根目录:"+destdir.getAbsolutePath());
			if( archiverName == null )
			{
				if( "gz".equalsIgnoreCase(suffix) )
				{
					GzipCompressorInputStream gis = new GzipCompressorInputStream(is);
					File file = new File(destdir, GzipUtils.getUncompressedFilename(cfile.getName()));
					IOHelper.writeFile(file, gis);
					file.setLastModified(cfile.lastModified());
					progress.report(100);
					progress.setDecompressFile(file);
					return;
				}
				if( "bz2".equalsIgnoreCase(suffix) )
				{
					BZip2CompressorInputStream bis = new BZip2CompressorInputStream(is);
					File file = new File(destdir, GzipUtils.getUncompressedFilename(cfile.getName()));
					IOHelper.writeFile(file, bis);
					progress.report(100);
					progress.setDecompressFile(file);
					return;
				}
				throw new Exception("不支持解压的文件格式("+suffix+").");
			}
//			System.err.println("开始解压: "+cfile.getName());
			String decompressName = cfile.getName().substring(0, i);
			progress.log("\r\n解压名:"+decompressName);
			ArchiveStreamFactory factory = new ArchiveStreamFactory();
			ais = factory.createArchiveInputStream(archiverName, is);
			File newDecDir = null;
			ArchiveEntry entry = null;
	        boolean first = true;
	        boolean hasDecDir = true;
        	byte[] buffer = new byte[64*1024];
        	ArrayList<ArchiveEntry> dirs = null;
        	File fileOrDir = null;
        	int filescount = getDecompressCount(cfile);
//			System.err.print("计算总个数耗时: "+(System.currentTimeMillis()-ts));
        	int step = 1;
			progress.report(step);
        	i = 0;
        	double percent = filescount;
        	percent /= 99;
        	percent = percent<1?1:percent;
        	progress.log("\r\nTotal: "+filescount+", Percent: "+percent);
			while( (entry=ais.getNextEntry()) != null )
			{
//	        	long ts = System.currentTimeMillis();
				if( i >= step*percent )
				{
					if( step < 100 )
						progress.report(step++);
				}
				i += 1;
	            if( first )
	            {
	            	if( !entry.isDirectory() )
	            	{
	            		destdir = new File(destdir, decompressName);
	            		if( !destdir.exists() )
	            		{
	            			destdir.mkdirs();
		            		newDecDir = destdir;
		            		progress.log("\r\n创建解压文件夹:"+destdir.getAbsolutePath());
	            		}
	            		else
	            		{
	            			hasDecDir = true;
		            		newDecDir = destdir;
		            		progress.log("\r\n发现现成文件夹:"+destdir.getAbsolutePath());
	            		}
	            	}
	            	else
	            	{
	            		dirs = new ArrayList<ArchiveEntry>();
	            	}
	            }
	            if( dirs != null && entry.isDirectory() )
	            {
	            	if( Tools.countChar(entry.getName(), '/') == 1 )
	            	{
	            		progress.log("\r\n发现解压根文件夹:"+entry.getName());
	            		dirs.add(entry);
	            	}
	            }

	            first = false;
	            String outPath = entry.getName().replaceAll("\\*", "/"); 
	            fileOrDir = new File(destdir, outPath);
	            if( entry.isDirectory() )
	            {
	            	if( !fileOrDir.exists() )
	            	{
	            		fileOrDir.mkdirs();
	            	}
	            }
	            else
	            {
	            	File parentDir = fileOrDir.getParentFile();
	            	if( !parentDir.exists() )
	            	{
	            		parentDir.mkdirs();
	            	}
	            	FileOutputStream fos = new FileOutputStream(fileOrDir);
	            	int len = 0;
	            	while( (len = ais.read(buffer)) != -1 )
	            	{
	            		fos.write(buffer, 0, len);
	            		fos.flush();
	            	}
	            	fos.close();
	            }
	            if("ar".equalsIgnoreCase(archiverName))
			    {
	            	ArArchiveEntry e = (ArArchiveEntry)entry;
	            	fileOrDir.setLastModified(e.getLastModified());
			    }
	            else if("zip".equalsIgnoreCase(archiverName)) 
				{
	            	ZipArchiveEntry e = (ZipArchiveEntry)entry;
	            	fileOrDir.setLastModified(e.getTime());
				}
	            else if("tar".equalsIgnoreCase(archiverName)) 
				{
	            	TarArchiveEntry e = (TarArchiveEntry)entry;
	            	fileOrDir.setLastModified(e.getModTime().getTime());
				}
	            else if("jar".equalsIgnoreCase(archiverName)) 
	            {
	            	JarArchiveEntry e = (JarArchiveEntry)entry;
	            	fileOrDir.setLastModified(e.getTime());
				}
	            else if("cpio".equalsIgnoreCase(archiverName)) {
	            	CpioArchiveEntry e = (CpioArchiveEntry)entry;
	            	fileOrDir.setLastModified(e.getTime());
				}
            	progress.log("\r\n");
//            	progress.log(Tools.getFormatTime("yy-MM-dd HH:mm:ss", fileOrDir.lastModified()));
//	            progress.log("\t"+step+"%\t");
//	        	ts = System.currentTimeMillis() - ts;
//            	progress.log(ts+"ms\t");
            	progress.log(outPath);
			}
			
			if( dirs != null && dirs.size() > 1 )
			{
    			newDecDir = new File(destdir, decompressName);
    			newDecDir.mkdirs();
				for(ArchiveEntry e: dirs)
				{
					File srcDir = new File(destdir, e.getName());
					File destDir = new File(newDecDir, e.getName());
					FileUtils.copyDirectory(srcDir, destDir);
					FileUtils.deleteDirectory(srcDir);
				}
			}
			if( newDecDir != null )
			{
				if( filescount == 1 )
				{
					File destFile = new File(cfile.getParentFile(), fileOrDir.getName());
					FileUtils.copyFile(fileOrDir, destFile);
					destFile.setLastModified(fileOrDir.lastModified());
					if( !hasDecDir ) FileUtils.deleteDirectory(newDecDir);
					progress.setDecompressFile(destFile);
				}
				else
				{
					newDecDir.setLastModified(cfile.lastModified());
					progress.setDecompressFile(newDecDir);
				}
			}
			if( progress.getDecompressFile() == null  )
			{
				if( filescount == 1 ) progress.setDecompressFile(fileOrDir) ;
				if( dirs != null && dirs.size() == 1 ) progress.setDecompressFile(new File(destdir, dirs.get(0).getName()));
			}
			progress.report(100);
		} 
		catch (Exception e) 
		{
			throw e;
		}
		finally
		{
			if( ais != null )
				try {
					ais.close();
				} catch (IOException e) {
				}
			if( is != null )
				try {
					is.close();
				} catch (IOException e) {
				}
		}
	}
}
