package com.focus.cos.web.config.action;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;

import com.focus.cos.web.action.GridAction;
import com.focus.cos.web.config.service.SftCfgMgr;

public class SftCfgAction extends GridAction 
{
	private static final long serialVersionUID = 2004977066813171910L;
	private static final Log log = LogFactory.getLog(SftCfgAction.class);
	private SftCfgMgr sftCfgMgr;
	private JSONObject profile;
	
	/**
	 * 打开配置界面
	 * @return
	 */
	public String preset()
	{
		log.debug("Open the view of software-config.");
		this.profile = SftCfgMgr.getConfig();
		return "preset";
	}
	/**
	 * 关于的信息
	 * @return
	 */
	public String about()
	{
		profile = SftCfgMgr.getConfig();
		return "about";
	}

	public void setSftCfgMgr(SftCfgMgr sftCfgMgr) {
		this.sftCfgMgr = sftCfgMgr;
	}
	
	public JSONObject getProfile() {
		return profile;
	}
}
