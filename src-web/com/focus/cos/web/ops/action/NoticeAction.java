package com.focus.cos.web.ops.action;

import java.io.File;
import java.util.Iterator;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.focus.cos.web.action.GridAction;
import com.focus.cos.web.common.Kit;
import com.focus.cos.web.common.PathFactory;
import com.focus.cos.web.ops.service.NoticeMgr;
import com.focus.cos.web.ops.vo.Notice;
import com.focus.util.IOHelper;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.opensymphony.xwork.ModelDriven;

public class NoticeAction extends GridAction implements ModelDriven
{
	private static final long serialVersionUID = -3262716038859904417L;

	private static final Log log = LogFactory.getLog(NoticeAction.class);
	
	private NoticeMgr noticeMgr;
	
	private Notice notice;
	//附件
	private File attachmenFile;
	private String attachmentFilename;
	private String ids = "";
	
	public String doQuery()
	{
		try
		{
			db = "cos";
			sql = "select * from TB_NOTICE";
    		StringBuffer colM = new StringBuffer();
    		colM.append("[");
    		colM.append("\r\n\t{ title: '', minWidth: 27, width: 27, type: 'detail', resizable: false, editable: false },");
    		colM.append("\r\n\t{ title: '创建时间', dataIndx: 'ADD_TIME', width: 190");
			colM.append(", filter: {type: 'textbox', condition: 'between', init: pqDatePicker, listeners: ['change']}");
			colM.append("},");
    		colM.append("\r\n\t{ title: '公告标题', dataIndx: 'TITLE', width: 260");
			colM.append(", filter: { type: 'textbox', condition: 'contain', listeners: ['change']}");
			colM.append("},");
    		colM.append("\r\n\t{ title: '状态', dataIndx: 'LOGTYPE', width: 80");
    		colM.append(",dataType: 'integer', filter: {" +
    	            	"type: 'select', condition: 'range', listeners: ['change'], "+
    	            	"init: function () {$(this).pqSelect({ checkbox: true, radio: true, width: '100%' });} " +
                	"}},");
    		colM.append("\r\n\t{ title: '发表时间', dataIndx: 'PUBLISH_TIME', width: 190");
			colM.append(", filter: {type: 'textbox', condition: 'between', init: pqDatePicker, listeners: ['change']}");
			colM.append("},");
    		colM.append("\r\n\t{ title: '附件', dataIndx: 'ATTACH_NAME', width: 120");
			colM.append(", filter: { type: 'textbox', condition: 'contain', listeners: ['change']}");
			colM.append("},");
    		colM.append("\r\n\t{ title: '公告摘要', dataIndx: 'SUMMARY'");
			colM.append(", filter: { type: 'textbox', condition: 'contain', listeners: ['change']}");
			colM.append("}");
    		colM.append("\r\n]");
    		this.colModel = colM.toString();

    		BasicDBObject toolbar = null;
    		toolbar = new BasicDBObject("label", "新增公告");
    		toolbar.put("icon", "ui-icon-plus");
    		toolbar.put("function", "preAdd();");
    		toolbars.add(toolbar);
    		toolbar = new BasicDBObject("label", "删除公告");
    		toolbar.put("icon", "ui-icon-minus");
    		toolbar.put("function", "doDelete();");
    		toolbars.add(toolbar);
    		toolbar = new BasicDBObject("label", "发布公告");
    		toolbar.put("icon", "ui-icon-check");
    		toolbar.put("function", "doPublish();");
    		toolbars.add(toolbar);
    		
    		BasicDBObject dataLights = new BasicDBObject();
    		dataLights.put("STATE", new BasicDBObject("错误", new BasicDBObject("pq_cellcls", "red")));
    		filterModel.put("lights", dataLights.toString());//INFO(1), DEBUG(2), ERROR(3);
    		
    		StringBuffer dataM = new StringBuffer();
    		dataM.append("{");
    		dataM.append("\r\n\tdataType: 'JSON',");
    		dataM.append("\r\n\tlocation: 'remote',");
    		dataM.append("\r\n\tsorting: 'remote',");
    		dataM.append("\r\n\tsortIndx: 'ADD_TIME',");
    		dataM.append("\r\n\tsortDir: 'down',");
    		dataM.append("\r\n\tmethod: 'GET',");
    		dataM.append("\r\n\turl: 'helper!sqldata.action',");
    		dataM.append("\r\n\tgetData: function (dataJSON) {return filterData(dataJSON)}");
    		dataM.append("\r\n}");
    		this.dataModel = dataM.toString();
    		
    		BasicDBObject detail;
    		detail = new BasicDBObject();
    		detail.put("type", 0);
    		detail.put("subject", "公告内容");
    		detail.put("data", "CONTENT");
    		details.add(detail);
    		
    		filterModel.put("on", true);
    		BasicDBList fields = new BasicDBList();
    		fields.add(new BasicDBObject("name", "STATE").append("attr", "multiple").append("condition", "range").append("style", "height:20px;width:128px;"));
    		filterModel.put("fields", fields);
    		BasicDBObject dataOptions = new BasicDBObject();
    		BasicDBList options;
    		options = new BasicDBList();//操作日志(1), 运行日志(2), 用户日志(3), 安全日志(4);
    		options.add(new BasicDBObject("0", "待发布"));
    		options.add(new BasicDBObject("1", "已发布"));
    		dataOptions.put("STATE", options);
    		//构造labels数据
    		BasicDBObject dataLabels = new BasicDBObject();
    		Iterator<String> iterator = dataOptions.keySet().iterator();
    		while(iterator.hasNext())
    		{
    			String name = iterator.next();
    			options = (BasicDBList)dataOptions.get(name);
    			BasicDBObject labels = new BasicDBObject();
    			for(Object o : options)
    			{
    				BasicDBObject e = (BasicDBObject)o;
    				String key = e.keySet().iterator().next();
    				String val = e.getString(key);
    				labels.put(key, val);
    			}
    			dataLabels.put(name, labels);
    		}
    		filterModel.put("labels", dataLabels.toString());
    		
    		BasicDBObject sObject = new BasicDBObject();
    		sObject.put("sql", sql);
    		sObject.put("colModel", colModel);
    		sObject.put("sqlExport", sql);
    		super.getSession().setAttribute(Kit.URLPATH(super.getRequest()), sObject);
			this.pageModel = "{ type: dataModel.location, rPP: 20, strRpp: '{0}', rPPOptions: [1, 10, 20, 30, 40, 50, 100, 500, 1000] }";
    		rowSelect = "";
    		selectionModel = "{ type: 'row', mode: 'single' }";
    		snapshotable = false;
    		hasToolbar = true;
    		File jsFile = new File(PathFactory.getWebappPath(), "jsp/notice/manager.js");
    		javascript = new String(IOHelper.readAsByteArray(jsFile), "UTF-8");
			datatype = "sql";
		}
		catch(Exception e)
		{
			super.setResponseException("打开公告管理页面出现异常"+e);
			log.error("Failed to query", e);
		}
		return "gridquery";
	}
	
	public String doSetNotice()
	{
		try
		{
			if(attachmenFile !=null)
			{
				File attDir = new File(getServletContext().getRealPath(""), "Attachment/");
				if(!attDir.exists())attDir.mkdir();
				
				String subPath = String.valueOf(System.currentTimeMillis());
				attDir = new File(attDir,subPath);
		        if (attDir.mkdir())
		        {
		            File file = new File(attDir, attachmentFilename);
		            FileUtils.copyFile(attachmenFile, file);
		            notice.setAttachUri("Attachment/"+subPath+"/"+attachmentFilename);
		            notice.setAttachName(attachmentFilename);
		        }
			}
			noticeMgr.modifyNotice(notice);
			String s = String.format(this.getText("user.log.0092"), super.getUserAccount(), notice.getId()+"");
			logoper(s, "公告管理", null, null);
		}
		catch(Exception e)
		{
			String s = String.format(this.getText("user.log.1092"), super.getUserAccount());
			logoper(s, "公告管理", null, null, e);
			log.error(e);
		}
		return doQuery();
	}
//	
//	public String downloadNotice()
//	{
//		log.debug("下载公告附件");
//		notice = noticeMgr.findNotice(notice.getId());
//		File f = new File(getServletContext().getRealPath(""),notice.getAttachUri());
//    	FileInputStream fileIn = null;
//    	OutputStream out = null;
//		try
//		{
//	    	fileIn = new FileInputStream(f);
//	    	super.getResponse().reset();
//	    	super.getResponse().setContentType("application/octet-stream; charset=utf-8");
//			String fileName = new String(f.getName().getBytes("GBK"),"ISO8859_1");  
//			super.getResponse().addHeader("Content-Disposition", "attachment;filename="+fileName);        
//			out = super.getResponse().getOutputStream();
//			byte[] b = new byte[32768];
//		    int len;
//		    while ( (len = fileIn.read(b)) > 0) 
//		    {
//		    	out.write(b, 0, len);
//		    }
//		    out.close();
//		}
//		catch (Exception e)
//		{
//			log.error("下载公告附件异常啦");
//		}
//		finally
//		{
//			try
//			{
//				if(fileIn != null)
//				{
//					fileIn.close();
//				}
//			}
//			catch(IOException e)
//			{
//				log.error("下载时发生异常",e);
//			}
//			
//		}
//		return null;
//	}
	
	public String doDelete()
	{
		if(!"".equals(ids))
		{
			String[] arryId = ids.split(",");
			for (int i = 0; i < arryId.length; i++)
			{
				try
				{
					if (arryId[i] != null && !"".equals(arryId[i].trim()))
					{
						notice = noticeMgr.findNotice(Long.parseLong(arryId[i].trim()));
						if (notice != null && notice.getAttachUri() != null
								&& notice.getAttachUri().trim().length() > 0)
						{
							File f = new File(getServletContext().getRealPath(""),
									StringUtils.substringBeforeLast(notice.getAttachUri(), "/"));
							FileUtils.deleteDirectory(f);
						}
					}
				}
				catch (Exception e)
				{
					log.error("", e);
				}
			}
			try
			{
				noticeMgr.deleteNoticeByIds(ids);
				String s = String.format(this.getText("user.log.0094"), getUserAccount(),ids);
				logoper(s, "公告管理", null, null);
			}
			catch (RuntimeException e)
			{
				String s = String.format(this.getText("user.log.1094"), getUserAccount());
				logoper(s, "公告管理", null, null, e);
			}
			super.setMessage(SUCCESS);
		}
		return doQuery();
	}
	
	public String doPublish()
	{
		try
		{
			noticeMgr.releaseNotice(ids);
			String s = String.format(this.getText("user.log.0093"), getUserAccount(),ids);
			logoper(s, "公告管理", null, null);
		}
		catch(Exception e)
		{
			log.error(e);
			String s = String.format(this.getText("user.log.1093"), getUserAccount());
			logoper(s, "公告管理", null, null, e);
		}
		return doQuery();
	}
	
	public Notice getNotice()
	{
		return notice;
	}

	public void setNotice(Notice notice)
	{
		this.notice = notice;
	}

	public String getIds()
	{
		return ids;
	}

	public void setIds(String ids)
	{
		this.ids = ids;
	}

	public void setNoticeMgr(NoticeMgr noticeMgr)
	{
		this.noticeMgr = noticeMgr;
	}

	public File getAttachmenFile() {
		return attachmenFile;
	}

	public void setAttachmenFile(File attachmenFile) {
		this.attachmenFile = attachmenFile;
	}

	public String getAttachmentFilename() {
		return attachmentFilename;
	}

	public void setAttachmentFilename(String attachmentFilename) {
		this.attachmentFilename = attachmentFilename;
	}
}
