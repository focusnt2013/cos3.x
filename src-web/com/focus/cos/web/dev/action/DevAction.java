package com.focus.cos.web.dev.action;

import org.json.JSONArray;
import org.json.JSONObject;

import com.focus.cos.web.common.HelperMgr;
import com.focus.cos.web.common.Kit;
import com.focus.cos.web.ops.action.OpsAction;
import com.focus.util.Tools;

/**
 * ZK监控action
 * @author focus
 *
 */
public class DevAction extends OpsAction
{
	private static final long serialVersionUID = 595771782637311121L;
	/*所属的系统ID*/
	protected String sysid;
	protected String sysname;
	protected String version;
	protected String remark;
	/**
	 * 完全配置
	 * @return
	 */
	public String doPreset()
	{
		this.gridxml = Kit.unicode2Chr(gridxml);
        return super.grid(this.gridxml);
	}
	
	public String getSysid() {
		return sysid;
	}

	public void setSysid(String sysid) {
		this.sysid = sysid;
	}

	public String getSysname() {
		return sysname;
	}
	/**
	 * 返回没版本号时候的数据结构
	 * @param data
	 * @param text
	 * @return
	 */
	public String noversion(JSONObject data, String name, String text)
	{
		String startDate = Tools.getFormatTime("yyyy,M", System.currentTimeMillis()-Tools.MILLI_OF_DAY);
		JSONObject timeline = new JSONObject();
		timeline = new JSONObject();
		timeline.put("headline", name+"["+sysid+"]");
		timeline.put("type", "default");
		timeline.put("startDate", startDate);
		timeline.put("text", "<span class='version'>N/A</span><span class='title'>N/A</span><br/>"+text);
		data.put("timeline", timeline);
		JSONObject asset = new JSONObject();
		asset.put("media", "images/notes.png");
		asset.put("credit", "");
		asset.put("caption", "");
		timeline.put("asset", asset);

		JSONArray date = new JSONArray();
		JSONObject timeline0 = new JSONObject();
		timeline0.put("headline", "N/A");
		timeline0.put("type", "default");
		timeline0.put("startDate", startDate);
		timeline0.put("text", text);
		asset = new JSONObject();
		asset.put("media", HelperMgr.getImgWallpaper());
		asset.put("credit", "");
		asset.put("caption", "");
		timeline0.put("asset", asset);
		date.put(timeline0);
		timeline.put("date", date);
		
		return response(super.getResponse(), data.toString());
	}
	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}
}
