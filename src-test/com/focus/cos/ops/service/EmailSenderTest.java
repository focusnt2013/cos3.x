package com.focus.cos.ops.service;

import com.focus.cos.ops.service.EmailSender;
import com.focus.cos.web.ops.vo.EmailOutbox;

public class EmailSenderTest
{

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
//		File sysConfigFile = new File("D:/focusnt/efida/project/idc/deploy/10.10.10.92/cos/config/sys/SysConfig");
//		Profile sysConfig = null;
		EmailOutbox outbox = new EmailOutbox();
		outbox.setMailTo("liu3xue@163.com");
		outbox.setSubject("[安卓客户端问题]联系人功能操作事件处理失败:");
//		outbox.setAttachments("http://test.my75.cn:18076/group1/M00/00/02/cx3zZFLRCs-AGG7ZAAATgw95iwM246.zip");
//		outbox.setContent("申先锋:/n/t某用户手机（型号xxxx)发现异常信息， 请依据附件中用户客户端日志文件分析定位故障原因，并尽快解决该问题。");
//		outbox.setContent(EmailSender.TAG_HTML+"<html><head></head><body><img src='cid:http://www.baidu.com/img/bdlogo.gif'><br/>欢迎使用COS邮件功能</body></html>");
//		outbox.setContent(EmailSender.TAG_HTML+"<html><head></head><body>"+
//		                  "<img src='cid:http://www.baidu.com/img/bdlogo.gif'><br/>"+
//		                  "<img src='cid:http://test.my75.cn:18076/group1/M00/00/00/cx3zZFLGeR6ALJhTAAA2dQh1z7Q739.png'><br/>欢迎使用COS邮件功能</body></html>");
		outbox.setContent(EmailSender.TAG_SNAPSHOT+"http://admin.banquanjia.com.cn/report/report!preview.action?id=195&by=grid");
//		outbox.setContent(EmailSender.TAG_SNAPSHOT+"http://www.baidu.com");
//		outbox.setContent(EmailSender.TAG_SNAPSHOT+"http://www.baidu.com/img/bdlogo.gif");
//		outbox.setContent(EmailSender.TAG_IMAGES+"http://www.baidu.com/img/bdlogo.gif;http://test.my75.cn:18076/group1/M00/00/00/cx3zZFLGeR6ALJhTAAA2dQh1z7Q739.png?time=1389435632336");

//		File file = new File(EmailSenderTest.class.getResource("").getFile(), "test.html");
		try
		{
//			String template = new String(new String(IOHelper.readAsByteArray(file), "UTF-8"));
//        	sysConfig = (Profile)IOHelper.readSerializable(sysConfigFile);
//			EmailSender.sendMail(outbox, sysConfig);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
