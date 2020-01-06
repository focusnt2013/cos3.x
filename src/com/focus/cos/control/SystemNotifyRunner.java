package com.focus.cos.control;

import com.focus.util.ConfigUtil;

public class SystemNotifyRunner extends SystemRunner
{
	public static final String Name = "系统消息通知程序"; 
	public static final String Remark = "根据系统消息通知配置向系统消息通知的收件用户发送邮件或短信提醒；暂不支持发送短信，未来计划逐步支持微信消息等。";
 
    public SystemNotifyRunner(ModuleManager manager)
    {
    	super( "SystemNotify", manager );
    	super.className = "com.focus.cos.ops.service.MessageNotify";
        this.setName( Name );
        this.setRemark(Remark);
    }

	@Override
	public void initliaize() throws Exception
	{
    	this.frequency = 30000;
    	this.timeout = 300000;
    	super.setDebug("true".equalsIgnoreCase(ConfigUtil.getString("runner.notify.debug", "false")));
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
