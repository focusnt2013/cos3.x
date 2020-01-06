package com.focus.cos.control;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;

import com.focus.control.ModuleMemeory;
import com.focus.cos.web.common.Kit;
import com.focus.util.ConfigUtil;
import com.focus.util.Log;
import com.focus.util.QuickSort;
import com.focus.util.Tools;

/**
 * 模块内存历史
 * @author think
 *
 */
public class MonitorGCHistory extends ArrayList<ModuleMemeory> implements java.io.Serializable
{
	private static final long serialVersionUID = -4633773595284549577L;
	private long fileGccode = 0;

	public MonitorGCHistory(String id){
		this(new File(ConfigUtil.getWorkPath(), "log/"+id));
	}
	
	public MonitorGCHistory(File path){
		File gcFiles[] = path.listFiles(new FilenameFilter(){
			public boolean accept(File arg0, String arg1)
			{
				return arg1.toLowerCase().endsWith(".gc");
			}
		});

		if( gcFiles == null )
		{
			return;
		}
		if( gcFiles.length > 1 )
		{//删除10个运行GC日志
	         QuickSort sorter = new QuickSort()
	         {
				public boolean compareTo(Object sortSrc, Object pivot){
					File left = (File)sortSrc;
					File right = (File)pivot;
					return left.lastModified()<right.lastModified();
				}
	         };
	         sorter.sort(gcFiles);
		}
		for( int i = 0; i < gcFiles.length; i++ )
		{
			ArrayList<ModuleMemeory> history = this.loadHistory(gcFiles[i]);
			for(ModuleMemeory e: history){
				this.add(e);
			}
		}
	}
	
	/**
	 * 加载历史的内存数据
	 * @param gcFile
	 */
	private ArrayList<ModuleMemeory> loadHistory(File gcFile){
		ModuleMemeory memory = null; 
		StringBuffer line = new StringBuffer(128);//行缓存队列
		FileInputStream fis = null;
		ArrayList<ModuleMemeory> history = new ArrayList<ModuleMemeory>();
		try
		{
			fis = new FileInputStream(gcFile);
			int ch = 0;
			long lastTime = 0;
//			long count = 0;
			while( (ch=fis.read()) != -1 )
			{
				if( ch == '\n' )
				{
					if( line.length() > 0 )
					{
						//R(n) = T(n): [ <GC> HB->HE(HC), D]
						int k = line.indexOf(": [");
						if( k != -1 )
						{
							long T = (long)(Double.parseDouble(line.substring(0, k))*1000);
							if( lastTime == 0 || T - lastTime > Tools.MILLI_OF_MINUTE )
							{
								memory = new ModuleMemeory("", ModuleManager.LocalIp);
								memory.setBegin(lastTime==0);
								memory.setId(fileGccode++);
								memory.setRuntime(T);
								history.add(memory);
								lastTime = T;
							}
							k = line.indexOf("[",k);
							int j = line.indexOf("GC", k);
							int l = line.indexOf("->", k);
							if( k != -1 && j != -1 && l != -1)
							{
								String GC = line.substring(k+1,j+2);
								memory.plusGccount();//GC次数累加一次
								if( !"GC".equals(GC) )
								{
									memory.plusFullgccount();//GC次数累加一次
								}
								j = line.lastIndexOf(" ", l);
								String HB = line.substring(j+1, l-1);
								int kk = line.charAt(l-1)=='M'?1024:1;
								memory.setHb(kk*Integer.parseInt(HB));
								j = line.indexOf("(", l);
								k = line.indexOf(")", l);
								if( k != -1 && j != -1 && l != -1 )
								{
									String HE = line.substring(l+2, j);
									String HC = line.substring(j+1, k);
									kk = HE.charAt(HE.length()-1)=='M'?1024:1;
									memory.setHe(kk*Integer.parseInt(HE.substring(0, HE.length()-1)));
									kk = HC.charAt(HC.length()-1)=='M'?1024:1;
									memory.setHc(kk*Integer.parseInt(HC.substring(0, HC.length()-1)));
									k = line.indexOf(" ", k);
									j = line.indexOf(" ", k+1);
									if( k != -1 && j != -1 )
									{
										double D = Double.parseDouble(line.substring(k+1, j));
										memory.setDsum(D);
										memory.setDmax(D);
										memory.setDmin(D);
									}
								}
							}
						}
						line.delete(0, line.length());
					}
				}
				else
				{
					line.append((char)ch);
				}
			}
//			System.err.println(count);
		}
		catch (Exception e)
		{
			Log.err("Failed decoe gc of "+gcFile.getAbsolutePath(), e);
		}
		finally
		{
			if( fis != null )
			{
				try
				{
					fis.close();
				}
				catch (IOException e)
				{
				}
			}
		}
		if( !history.isEmpty() ){
			long runstartTime = gcFile.lastModified() - history.get(history.size()-1).getRuntime();
			for( ModuleMemeory e : history ){
				e.setRuntime(e.getRuntime()+runstartTime);
			}
		}
		return history;
	}
	

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		final long k = 1024;
		MonitorGCHistory history = new MonitorGCHistory(new File("D:/focusnt/cos/trunk/CODE/main/test/GCMonitor/"));
		System.out.println(history.size());
		for(ModuleMemeory e : history) {
			System.err.println(String.format("[%s] 堆:%s 开始: %s 结束: %s, GC: %s, Full GC: %s (%s)",
				Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", e.getRuntime()),
				Kit.bytesScale(k*e.getHc()), Kit.bytesScale(k*e.getHb()), Kit.bytesScale(k*e.getHe()),
				e.getGccount(), e.getFullgccount(), e.isBegin()));
		}
	}
}
