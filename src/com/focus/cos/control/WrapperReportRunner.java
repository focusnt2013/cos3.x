package com.focus.cos.control;

import com.focus.util.ConfigUtil;
import com.focus.util.Tools;

public class WrapperReportRunner extends SystemRunner
{
	public static final String Name = "主控报告程序"; 
	public static final String Remark = "主控相关情况监控报告；执行主控引擎以及关联程序升级。";
	private boolean suspendWrapper = false;
    public WrapperReportRunner(ModuleManager manager)
    {
    	super( "WrapperReport", manager );
    	super.className = "com.focus.cos.wrapper.WrapperReport";
        this.setName(Name);
        this.setRemark(Remark);
        this.setDelayedStartInterval(5000);
    }

	public boolean isSuspendWrapper()
	{
		return suspendWrapper;
	}

	@Override
	public void initliaize() throws Exception
	{
    	this.frequency = Tools.MILLI_OF_DAY;
    	super.setDebug("true".equalsIgnoreCase(ConfigUtil.getString("wrapper.report.debug", "false")));
	}

	@Override
	public void onFinish() {
		// TODO Auto-generated method stub
		
	}
}
