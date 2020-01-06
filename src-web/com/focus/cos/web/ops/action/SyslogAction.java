package com.focus.cos.web.ops.action;

import com.focus.cos.web.action.GridAction;
import com.opensymphony.xwork.ModelDriven;

public class SyslogAction extends GridAction implements ModelDriven
{
	private static final long serialVersionUID = 3948073383562114350L;
	/**
	 * 组件子系统菜单配置
	 * @return
	 */
	public String doQuery()
	{
		this.values.put("#type#", "1,2,4");
		String xmlpath = "/grid/local/syslogquery.xml";
        String rsp = super.grid(xmlpath);
        return rsp;
	}
}
