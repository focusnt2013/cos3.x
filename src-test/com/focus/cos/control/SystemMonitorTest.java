package com.focus.cos.control;

import java.lang.management.ManagementFactory;

import com.focus.util.Log;
import com.focus.util.Tools;
import com.sun.management.OperatingSystemMXBean;

public class SystemMonitorTest {
	public static void main(String args[])
	{
		OperatingSystemMXBean osmxb = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        long totalPhysicalMemorySize = osmxb.getTotalPhysicalMemorySize();
        long freePhysicalMemorySize = osmxb.getFreePhysicalMemorySize();
        System.out.println(Tools.bytesScale(totalPhysicalMemorySize));
        System.out.println(Tools.bytesScale(freePhysicalMemorySize));
        System.out.println("SwapSpaceSize:"+Tools.bytesScale(osmxb.getTotalSwapSpaceSize()));
        System.out.println("SystemLoadAverage():"+Tools.bytesScale(osmxb.getCommittedVirtualMemorySize()));
        System.out.println("SystemLoadAverage():"+Tools.bytesScale(osmxb.getFreeSwapSpaceSize()));

        Log.getInstance().setSubroot("SystemMonitor");
        Log.getInstance().setDebug(true);
        Log.getInstance().setLogable(false);
        Log.getInstance().start();
        
        SystemMonitor instance = new SystemMonitor();
        instance.setCpuLoadForWindows(instance.getSysPerf());
	}
}
