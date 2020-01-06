package com.focus.cos.web.dev.service;

import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.directwebremoting.WebContextFactory;
import org.json.JSONObject;
import org.w3c.dom.Node;

import com.focus.cos.web.common.AjaxResult;
import com.focus.cos.web.service.SvrMgr;
import com.focus.util.XMLParser;

/**
 * 表单管理器
 * @author focus
 *
 */
public class FormMgr extends SvrMgr
{
	private static final Log log = LogFactory.getLog(FormMgr.class);
	
	
	/**
	 * 保存表单
	 * @param mode 0新增，1修改
	 * @param gridxml 模板路径
	 * @param formJson 表单json
	 * @param columnJson 字段json
	 * @return
	 */
	public AjaxResult<Integer> save(int mode, String gridxml, String formJson, String columnJson)
	{
		AjaxResult<Integer> result = new AjaxResult<Integer>();
		try
		{
			if( gridxml == null || gridxml.isEmpty() ){
				result.setMessage("中止表单保存执行，因为异常没有提供表单模板");
				return result;
			}
			JSONObject formData = new JSONObject(formJson);
			JSONObject columns = new JSONObject(columnJson);
			FormSetter setter = new FormSetter(mode, gridxml, formData, columns){
				@Override
				public JSONObject set() throws Exception
				{
					Node formNode = XMLParser.getFirstChildElement(filednode);
					if( formNode == null )
					{
						throw new Exception("不正确的form("+gridtitle+")格式");
					}
					setFormParameters(formData, formNode);
					String recIndx = getRecIndx();
					myid = formData.has(recIndx)?formData.get(recIndx).toString():null;
					log.info("Todo set data("+myid+") of update:"+formData.toString(4));
					return formData;
				}
				@Override
				public void update(JSONObject old, JSONObject set) throws Exception {
					if( old != null )
					{
						Iterator<?> iterator = old.keys();
						while(iterator.hasNext())
						{
							String key = iterator.next().toString();
							Object val = old.get(key);
							if( !set.has(key) )	set.put(key, val);
							JSONObject column = columns.has(key)?columns.getJSONObject(key):null;
							if( column != null ){
								String dataType = column.has("dataType")?column.getString("dataType" ):"";
								if( !"password".equalsIgnoreCase(dataType) ) continue;
								if( !set.has(key) || set.getString(key).equals("******") )
								{
									set.put(key, old.getString(key));
								}
							}
						}
					}
				}

			};
			org.directwebremoting.WebContext web = WebContextFactory.get(); 
			HttpServletRequest request = web.getHttpServletRequest();
	        JSONObject account = (JSONObject)request.getSession().getAttribute("account");
			JSONObject response = setter.execute(account);
			result.setMessage(response.has("message")?response.getString("message"):"");
			result.setSucceed(response.has("hasException")&&response.getBoolean("hasException")?false:true);
		}
		catch(Exception e)
		{
			log.error("Failed to save the forms \r\n\t"+formJson+"\r\n\tby tempalte "+gridxml, e);
			result.setMessage("中止表单保存执行，因为异常"+e.getMessage());
		}
		return result;
	}
	
	/**
	 * 删除表单数据
	 * @param gridxml
	 * @param formJson
	 * @return
	 */
	public AjaxResult<Integer> delete(String gridxml, String formJson, String columnJson)
	{
		AjaxResult<Integer> result = new AjaxResult<Integer>();
		try
		{
			JSONObject formData = new JSONObject(formJson);
			JSONObject columns = new JSONObject(columnJson);
			FormSetter setter = new FormSetter(FormSetter.DELETE, gridxml, formData, columns){
				@Override
				public JSONObject set() throws Exception
				{
					String recIndx = getRecIndx();
					myid = (String)formData.get(recIndx);
					log.info("Todo delete the data("+myid+") of grid from "+path);
					return null;
				}
				@Override
				public void update(JSONObject old, JSONObject set) throws Exception {
				}

			};
			org.directwebremoting.WebContext web = WebContextFactory.get(); 
			HttpServletRequest request = web.getHttpServletRequest();
	        JSONObject account = (JSONObject)request.getSession().getAttribute("account");
			JSONObject response = setter.execute(account);
			result.setMessage(response.has("message")?response.getString("message"):"");
			result.setSucceed(response.has("hasException")&&response.getBoolean("hasException")?false:true);
		}
		catch(Exception e)
		{
			log.error("", e);
			result.setMessage("中止导出数据指令异常"+e.getMessage());
		}
		return result;
	}
}
