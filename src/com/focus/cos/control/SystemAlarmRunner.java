package com.focus.cos.control;

import com.focus.util.ConfigUtil;

public class SystemAlarmRunner extends SystemRunner
{
	public static final String Name = "系统告警程序"; 
	public static final String Remark = "基于系统告警参数配置，按照告警的等级与类型，将系统实时告警中待确认的告警转换成告警邮件，提交到邮件发件箱，发送给告警接收用户。";
	
    public SystemAlarmRunner(ModuleManager manager)
    {
    	super( "SystemAlarm", manager );
    	super.className = "com.focus.cos.ops.service.AlarmNotifier";
        this.setName( Name );
        this.setRemark(Remark);
    }

	@Override
	public void initliaize() throws Exception
	{
		
    	this.frequency = 30000;
    	this.timeout = 300000;
    	super.setDebug("true".equalsIgnoreCase(ConfigUtil.getString("runner.alarm.debug", "false")));
		if( !manager.isDatabaseStandby() )
		{
			throw new Exception("主数据库未启动不能启动[系统告警程序]");
		}
	}

	@Override
	public void onFinish() {
		// TODO Auto-generated method stub
		
	}
}
