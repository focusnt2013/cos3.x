package com.focus.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

public class DecompressorTest {
	@Before
	public void setUp() throws Exception 
	{
	}

	@Test
	public void testJustYouCan() 
	{
		try 
		{
			long ts = System.currentTimeMillis();
			File file = new File("test/Decompression/mysql-5.6.26-winx64.zip");
			int size = Decompressor.getDecompressCount(file);
			ts = System.currentTimeMillis() - ts;
			System.out.println(file.getName()+": "+size+", 耗时: "+ts);
			assertEquals(size, 10053);
			
			ts = System.currentTimeMillis();
			file = new File("test/Decompression/nginx-1.8.0.tar.gz");
			size = Decompressor.getDecompressCount(file);
			ts = System.currentTimeMillis() - ts;
			System.out.println(file.getName()+": "+size+", 耗时: "+ts);
			assertEquals(size, 401);
		}
		catch(Exception e) 
		{
			e.printStackTrace();
			assertNull(e);
		}
		Decompressor.Progress progress = new Decompressor.Progress() {
			private int step = 0;
			@Override
			public void report(int p) {
            	for(; step < p/2; step++)
            		System.out.print('+');
			}
		};
		try 
		{
			System.out.println("|--------20%-------40%-------60%-------80%----------|100%");
			System.out.print(" ");
			File file = new File("test/Decompression/mysql-5.6.26-winx64.zip");
			Decompressor.execute(file, null , progress);
    		System.out.print('+');
			assertEquals(progress.getDecompressFile().getName(), "mysql-5.6.26-winx64");
			assertEquals(true, progress.getDecompressFile().isDirectory());
			assertEquals(progress.getDecompressFile().getParentFile().getName(), "Decompression");
			File caseout = new File("test/Decompression/case1.txt");
			IOHelper.writeFile(caseout, progress.getLogtxt().getBytes("UTF-8"));
			File gzipFile = IOHelper.gzip(caseout);
			assertEquals(gzipFile.length()<64*1024, true);
		}
		catch(Exception e) 
		{
			System.err.println(progress.getLogtxt());
			e.printStackTrace();
			assertNull(e);
		}
		try 
		{
			File file = new File("test/Decompression/config.zip");
			progress = Decompressor.execute(file);
			assertEquals(progress.getName(), "control.xml");
			assertEquals(true, progress.isFile());
			System.out.println(progress.getParentName());
			assertEquals(progress.getParentName(), "Decompression");
			File cfgdir = new File(progress.getDecompressFile().getParentFile(), "config");
			assertEquals(true, cfgdir.exists());
		}
		catch(Exception e) 
		{
			System.err.println(progress.getLogtxt());
			e.printStackTrace();
			assertNull(e);
		}
		try 
		{
			File file = new File("test/Decompression/ab.gz");
			progress = Decompressor.execute(file);
			assertEquals(progress.getName(), "ab");
			assertEquals(progress.getParentName(), "Decompression");
			assertEquals(progress.lastModified(), file.lastModified());
		}
		catch(Exception e) 
		{
			System.err.println(progress.getLogtxt());
			e.printStackTrace();
			assertNull(e);
		}
		try 
		{
			File file = new File("test/Decompression/pcre-8.38.zip");
			progress = Decompressor.execute(file);
			assertEquals(progress.getName(), "pcre-8.38");
			assertEquals(progress.getParentName(), "Decompression");
		}
		catch(Exception e) 
		{
			System.err.println(progress.getLogtxt());
			e.printStackTrace();
			assertNull(e);
		}
		try 
		{
			File file = new File("test/Decompression/pcre-8.38.0.zip");
			progress = Decompressor.execute(file);
			assertEquals(progress.getName(), "pcre-8.38.0");
			assertEquals(progress.lastModified(), file.lastModified());
		}
		catch(Exception e) 
		{
			System.err.println(progress.getLogtxt());
			e.printStackTrace();
			assertNull(e);
		}
		try 
		{
			File file = new File("test/Decompression/commons-compress-1.0.jar");
			progress = Decompressor.execute(file);
			assertEquals(progress.getName(), "commons-compress-1.0");
			assertEquals(progress.lastModified(), file.lastModified());
		}
		catch(Exception e) 
		{
			System.err.println(progress.getLogtxt());
			e.printStackTrace();
			assertNull(e);
		}
		try 
		{
			File file = new File("test/Decompression/test.tar");
			progress = Decompressor.execute(file);
			assertEquals(progress.getName(), "test.txt");
			assertEquals(progress.getParentName(), "Decompression");
		}
		catch(Exception e) 
		{
			System.err.println(progress.getLogtxt());
			e.printStackTrace();
			assertNull(e);
		}
	}
}
