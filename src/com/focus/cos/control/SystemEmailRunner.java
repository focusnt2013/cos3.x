package com.focus.cos.control;

import com.focus.util.ConfigUtil;


public class SystemEmailRunner extends SystemRunner
{
	public static final String Name = "系统邮件程序"; 
	public static final String Remark = "基于系统参数配置与模块子系统配置的SMTP等邮件参数，将系统邮件发件箱中待发邮件发送出去。";
 
    public SystemEmailRunner(ModuleManager manager)
    {
    	super("SystemEmail", manager);
    	super.className = "com.focus.cos.ops.service.EmailSender";
        this.setName( Name );
        this.setRemark(Remark);
    }

	@Override
	public void initliaize() throws Exception
	{
    	this.frequency = 30000;
    	this.timeout = 300000;
    	super.setDebug("true".equalsIgnoreCase(ConfigUtil.getString("runner.email.debug", "false")));
		if( !manager.isDatabaseStandby() )
		{
			throw new Exception("主数据库未启动不能启动[系统消息通知程序]");
		}
	}

	@Override
	public void onFinish() {
		// TODO Auto-generated method stub
		
	}
}
