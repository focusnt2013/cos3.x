package com.focus.cos.web.dev.action;

import java.io.ByteArrayInputStream;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;

import com.focus.cos.api.LogSeverity;
import com.focus.cos.web.action.TemplateChecker;
import com.focus.cos.web.common.Kit;
import com.focus.cos.web.service.SvrMgr;
import com.focus.util.Tools;

/**
 * 通用表单编辑模块
 * @author focus
 * @version v1.0.0 2019-9-28
 */
public class FormAction extends DiggAction 
{
	private static final long serialVersionUID = -6310292377737618515L;
	private static final Log log = LogFactory.getLog(FormAction.class);

	/**
	 * 表单调测
	 * @return
	 */
	public String debug(){
		long ts = System.currentTimeMillis();
		try 
		{
			HttpServletRequest request = super.getRequest();
			String gridtext = request.getParameter("gridtext");
			this.workmode = TemplateChecker.DEBUG;
			ByteArrayInputStream is = new ByteArrayInputStream(gridtext.getBytes("UTF-8"));
        	this.parse(is);
        	String type = this.filetype;
			log.info("Debug '"+type+"' template of "+gridxml+" spent "+(System.currentTimeMillis()-ts)+" ms.");
	        if( "1".equals(super.getRequest().getParameter("a")) ){
	        	this.loadFormData(super.getRequest());
	        }
			return "grid"+type;
		}
		catch (Exception e) 
		{
			test(1, "解析元数据表单配置模板发生<span style='color:#ff3300;font-size:14px;font-weight:bold;'>【致命】</span>异常， 请检查模板XML格式是否正确。\r\n %s", e.getMessage());
			checker.write(e);
			String tips = "模板【"+id+"】测试没有发现问题。";
			if( checker.warcount()>0||checker.errcount()>0)
			{
				tips = "测试模板发现"+checker.errcount()+"个错误，"+checker.warcount()+"个改进提示。";
			}
			javascript += "document.getElementById('aaa').innerHTML = '"+tips+"';";
			if( checker.errcount() > 0 )
			{
				javascript += "document.getElementById('aaa').style.background = '#ff6633';";
			}
			else if( checker.warcount() > 0 )
			{
				javascript += "document.getElementById('aaa').style.background = '#ffff66';";
			}
			id = "xml";
			return debuglog();
		}
		finally{
    		this.getSession().setAttribute("xml.log", this.checker.payload());
			log.info("Save the log to session spent "+(System.currentTimeMillis()-ts)+" ms.");
		}
	}
	
	/*只做预览表单数据*/
	private boolean onlypreview = false;
	public boolean isOnlypreview() {
		return onlypreview;
	}
	/**
	 * 表单预览
	 * @return
	 */
	public String preview(){
		log.info("Open the form of tempalte "+gridxml);
		onlypreview = true;
//		this.gridxml = Kit.unicode2Chr(gridxml);
		HttpServletRequest request = super.getRequest();
		super.ww = 4121113;
        String result = super.grid(this.gridxml);
//        if (this.gridxml.indexOf("syslogquery") == -1) {
//        	super.logoper(String.format("执行Form模板【%s, 版本v%s】", new Object[] {
//        		this.gridtitle, this.gridversion }), 
//        		"DIGG-QUERY", this.gridxml, "diggcfg!version.action?id=" + this.gridxml);
//        }
        loadFormData(request);
        return result;
	}
	/**
	 * 表单编辑
	 * @return
	 */
	public String edit(){
		log.info("Open the form of tempalte "+gridxml);
//		this.gridxml = Kit.unicode2Chr(gridxml);
		HttpServletRequest request = super.getRequest();
		super.ww = 4121113;
        String result = super.grid(this.gridxml);
//        if (this.gridxml.indexOf("syslogquery") == -1) {
//        	super.logoper(String.format("执行Form模板【%s, 版本v%s】", new Object[] {
//        		this.gridtitle, this.gridversion }), 
//        		"DIGG-QUERY", this.gridxml, "diggcfg!version.action?id=" + this.gridxml);
//        }
        loadFormData(request);
        return result;
	}
	/**
	 * 打开表单
	 * @return
	 */
	public String open(){
//		this.gridxml = Kit.unicode2Chr(gridxml);
		log.info("Open the form of tempalte "+gridxml);
		super.ww = 4121113;
        String result = super.grid(this.gridxml);
//        if (this.gridxml.indexOf("syslogquery") == -1) {
//        	super.logoper(String.format("执行Form模板【%s, 版本v%s】", new Object[] {
//        		this.gridtitle, this.gridversion }), 
//        		"DIGG-QUERY", this.gridxml, "diggcfg!version.action?id=" + this.gridxml);
//        }
        return result;
	}
	
	/**
	 * 加载表单数据
	 * @param request
	 */
	private void loadFormData(HttpServletRequest request){
    	JSONObject dataJSON = new JSONObject();
    	long ts = System.currentTimeMillis();
    	if( "form".equalsIgnoreCase(filetype) ){
    		if( localData != null ){
    			this.localData = Kit.chr2Unicode(this.localData);
    			return;
    		}
    	}
		try
		{
	        referer = request.getHeader("referer");
			diggChecker = new DiggChecker(true);
    		StringBuffer sb = new StringBuffer();
    		sb.append("\r\n----------------\r\nHTTP参数:");
    		sb.append("\r\n\t"+request.getRemoteAddr()+":"+request.getRemotePort()+"\t远端用户: "+request.getRemoteUser());
    		Iterator<Map.Entry<String, String[]>> iterator = request.getParameterMap().entrySet().iterator();
    		while(iterator.hasNext())
    		{
    			Map.Entry<String, String[]> e = iterator.next();
    			sb.append("\r\n\t");
    			sb.append(e.getKey());
    			sb.append("=");
    			for(String value : e.getValue())
    				sb.append(value+"\t");
    		}
    		diggChecker.print(-1, "%s", sb.toString());
    		this.setDiggConfig(request);
			curPage = 1;
			pageSize  = 0;
    		this.digg(dataJSON);
    		if( dataJSON.has("data") ){
    			this.localDataArray = dataJSON.getJSONArray("data");
    			if( this.localDataArray.length() > 0 ){
    				this.localDataObject = this.localDataArray.getJSONObject(0);
    				this.localData = this.localDataObject.toString();
    				this.localData = Kit.chr2Unicode(this.localData);
    				//System.err.println(localData);
    			}
    		}
    		int count = dataJSON.getInt("curPageRecords");
//    			System.err.println(dataJSON.toString(4));
        	long duration = System.currentTimeMillis() - ts;
    		String endtips = String.format("执行元数据模板[%s]数据查询成功，耗时%s毫秒", gridxml, duration);
    		this.diggChecker.print(0, "执行元数据模板[%s]数据查询成功，耗时%s毫秒", gridxml, duration);
    		if(curPage>1 && !gridxml.isEmpty() && this.gridxml.indexOf("syslogquery") == -1) {
	        	super.logoper(String.format("执行Grid表单模板【%s, 版本v%s】视图查询，获取数据成功", new Object[] {
	        		this.gridtitle, this.gridversion, curPage,  count}), 
	        		"DIGG-QUERY", endtips, "diggcfg!version.action?id=" + this.gridxml);
    		}
		}
		catch (Exception e)
		{
			this.diggChecker.print(0, "执行元数据模板DIGG发生异常: %s", e.toString());
			this.checker.write(e);
			dataJSON.put("hasException", true);
			String c1 = e.getMessage();
			if( c1 != null ){
				c1 = c1.replaceAll("\"", "");
				c1 = c1.replaceAll("'", "");
			}
			dataJSON.put("message", "执行元数据模板["+(gridxml!=null?gridxml:"未知")+"]DIGG查询出现异常，"+c1);
    		SvrMgr.sendNotiefiesToSystemadmin(
    				super.getRequest(),
					"数据管理",
					"执行DIGG模板["+gridxml+"]视图查询出现异常",
					"[<i class='fa fa-digg fa-fw'></i>] 查询数据出现异常"+e.getMessage()+"，堆栈如下所示：\r\n"+checker.toString(),
					"helper!previewxml.action?path="+gridxml,
                    "情况确认", "#feedback?to="+super.getUserAccount());
        	super.logoper(LogSeverity.ERROR, String.format("执行DIGG模板[%s]视图查询出现异常: %s", new Object[] {
        		this.gridxml, e.getMessage()}), 
        		"DIGG-QUERY", checker.toString(), "diggcfg!version.action?id=" + this.gridxml);
//        		System.out.println(checker.toString());
		}
        finally
        {
        	long duration = System.currentTimeMillis() - ts;
    		this.diggChecker.print(1, "数据解析处理从时间[%s]开始，一共耗时%s秒", Tools.getFormatTime("HH:mm:ss", ts), duration/1000);
//    		System.err.println(diggChecker.toString());
        	if( referer != null && this.referer.indexOf("!debug") != -1 ){
        		this.getSession().setAttribute("digg.log", this.checker.payload());
        	}
        }
	}
	
	public static void main(String[] args) {
		try {
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
}
