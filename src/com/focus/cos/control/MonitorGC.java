package com.focus.cos.control;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Calendar;

import com.focus.control.ModuleMemeory;
import com.focus.control.ModulePerf;
import com.focus.cos.web.common.Kit;
import com.focus.util.Log;
import com.focus.util.QuickSort;
import com.focus.util.Tools;

/**
 * 执行GC日志文件的监控
 * @author focus
 */
public class MonitorGC
{
	private long fileGcSkip = 0;
	//当前模块的GC文件
	private File fileGc = null;
	//编号
	private long fileGccode = 0;
	//上次时间
	private long lastTime = 0;
	/*模块监控*/
	protected ModulePerf modulePerf;
	 
	public MonitorGC(ModulePerf modulePerf)
	{
		this.modulePerf = modulePerf;
		
	}
	
	
	/**
	 * 执行监控
	 * @return 数据有变化返回true
	 */
 	public boolean execute()
 	{
 		if( modulePerf.getStartupDate() == null ) return false;
		File gcFiles[] = modulePerf.getModuleLogPath().listFiles(new FilenameFilter(){
			public boolean accept(File arg0, String arg1)
			{
				return arg1.toLowerCase().endsWith(".gc");
			}
		});
		if( gcFiles == null )
		{
			return false;
		}
		if( gcFiles.length > 1 )
		{//删除10个运行GC日志
	         QuickSort sorter = new QuickSort()
	         {
				public boolean compareTo(Object sortSrc, Object pivot){
					File left = (File)sortSrc;
					File right = (File)pivot;
					return left.lastModified()>right.lastModified();
				}
	         };
	         sorter.sort(gcFiles);
	 		//将超过10个的GC日志删除
	 		for( int i = 10; i < gcFiles.length; i++ )
	 		{
 				gcFiles[i].delete();//删除不需要的日志
	 		}
		}
		else if( gcFiles.length == 0 )
		{
			return false;
		}
		if( fileGc != null )
		{
			if( !fileGc.getName().equals(gcFiles[0].getName()) )
			{
				fileGc = gcFiles[0];
				fileGcSkip = 0;
				fileGccode = 0;
			}
		}
		else
		{
			fileGc = gcFiles[0];
		}
		long runstartTime = modulePerf.getStartupDate().getTime();
		/*执行GC日志跟踪
		if( runstartTime > fileGc.lastModified() )
		{
//			Log.msg("["+id+"] GC file not come on.");
			return;
		}*/
		ModuleMemeory memory = null; 
//		final int PERIOD = 60000;//采集周期为10秒钟一次
		StringBuffer line = new StringBuffer(128);//行缓存队列
		FileInputStream fis = null;
		boolean newGc = false;
		try
		{
//			Log.msg("["+id+"]["+Tools.getFormatTime("MM-dd HH:mm:ss", runstartTime)+" "+runstartTime+"] "+fileGcSkip+" "+fileGc.getAbsolutePath());
//			System.out.println("["+id+"]["+Tools.getFormatTime("MM-dd HH:mm:ss", runstartTime)+" "+runstartTime+"] "+fileGcSkip+" "+fileGc.getAbsolutePath());
			fis = new FileInputStream(fileGc);
			fis.skip(fileGcSkip);
			int ch = 0;
			while( (ch=fis.read()) != -1 )
			{
				fileGcSkip += 1;
				if( ch == '\n' )
				{
					if( line.length() > 0 )
					{
						//R(n) = T(n): [ <GC> HB->HE(HC), D]
						int k = line.indexOf(": [");
						if( k != -1 )
						{
							long T = (long)(Double.parseDouble(line.substring(0, k))*1000);
							modulePerf.setRuntime(T);
							if( memory == null && !modulePerf.getMemories().isEmpty() )
							{
								memory = modulePerf.getMemories().get(modulePerf.getMemories().size()-1);
							}
							newGc = true;
							if( memory == null || T - lastTime > Tools.MILLI_OF_MINUTE )
							{
								memory = new ModuleMemeory(memory);
								memory.setId(fileGccode++);
								memory.setRuntime(T+runstartTime);
								modulePerf.addModuleMemeory(memory);
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
								memory.setHb(Integer.parseInt(HB)*kk);
								j = line.indexOf("(", l);
								k = line.indexOf(")", l);
								if( k != -1 && j != -1 && l != -1 )
								{
									String HE = line.substring(l+2, j);
									String HC = line.substring(j+1, k);
									modulePerf.setUsageMemory(HE+"/"+HC);
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
		}
		catch (Exception e)
		{
			Log.err("Failed decoe gc of "+fileGc.getAbsolutePath(), e);
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
		return newGc;
	}
 	
 	/**
 	 * 重置
 	 */
 	public void reset(){
 		modulePerf.getMemories().clear();
 		fileGccode = 0;
 		fileGcSkip = 0;
 		lastTime = 0;
 	}

	public ModulePerf getModulePerf()
	{
		return modulePerf;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		Calendar c = Calendar.getInstance();
		c.set(Calendar.HOUR_OF_DAY, 20);
		c.set(Calendar.MINUTE, 16);
		c.set(Calendar.SECOND, 0);
		System.out.println(Tools.getFormatTime("MM-dd HH:mm:ss", c.getTimeInMillis()));
		ModulePerf perf = new ModulePerf();
		perf.setStartupTime(c.getTime());
		perf.setModuleLogPath(new File("D:/focusnt/cos/trunk/CODE/main/test/GCMonitor/"));
		MonitorGC monitorGC = new MonitorGC(perf);
		if( monitorGC.execute() )
		{
			final long k = 1024;
			for(ModuleMemeory e : monitorGC.getModulePerf().getMemories())
				System.err.println(String.format("[%s] 堆:%s 开始: %s 结束: %s, GC: %s, Full GC: %s",
					Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", e.getRuntime()),
					Kit.bytesScale(k*e.getHc()), Kit.bytesScale(k*e.getHb()), Kit.bytesScale(k*e.getHe()),
					e.getGccount(), e.getFullgccount()));
		}
//		ArrayList<ModuleMemeory> history = new ArrayList<ModuleMemeory>();
//		handleHistory(new File("D:/focusnt/cos/trunk/CODE/main/test/GCMonitor/20180625201441.gc"), "", 133, history);
	}
}
