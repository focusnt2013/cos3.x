package com.focus.cos.web.ops.action;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;

import com.focus.cos.web.common.Kit;
import com.focus.cos.web.common.QueryMeta;
import com.focus.cos.web.common.paginate.PageBean;
import com.focus.cos.web.ops.service.EmailMgr;
import com.focus.cos.web.ops.vo.EmailOutbox;
import com.focus.util.Tools;
import com.opensymphony.xwork.ModelDriven;

public class EmailAction extends OpsAction implements ModelDriven
{
	private static final long serialVersionUID = -2634567304247201547L;
	private static final Log log = LogFactory.getLog(EmailAction.class);
	//复用管理器
	private EmailMgr emailMgr;
	//
	private long eid;
	
	public String suspend()
	{
		return null;
	}
	public String resume()
	{
		return null;
	}
	/**
	 * 查询邮件发件箱
	 * @return
	 */
	public String queryoutbox()
	{
    	ServletOutputStream out = null;
		JSONObject dataJSON = new JSONObject();
		try
		{
			HttpServletResponse response = super.getResponse();
            out = response.getOutputStream();
			response.setContentType("text/json;charset=utf8");
    		response.setHeader("Content-disposition", "inline; filename="+System.currentTimeMillis()+".json");
			JSONObject condition = pq_filter!=null&&pq_filter.startsWith("{")?new JSONObject(pq_filter):new JSONObject();
//			System.err.println(pq_filter);

			int curpage = Integer.parseInt(Tools.getValidStr(super.getRequest().getParameter("pq_curpage"), "1"));
			curpage = curpage<=0?1:curpage;
			int pagesize  = Integer.parseInt(Tools.getValidStr(super.getRequest().getParameter("pq_rpp"), "50"));
			QueryMeta qMeta = new QueryMeta();
			if( condition.has("data") )
			{
				JSONArray data = condition.getJSONArray("data");
				for(int i = 0;i < data.length(); i++)
				{
					JSONObject e = data.getJSONObject(i);
					if( e.has("dataIndx") )
					{
						if( "state".equals(e.getString("dataIndx")) )
						{
							qMeta.setStatus(Integer.parseInt(e.getString("value")));
						}
						else if( "time".equals(e.getString("dataIndx")) )
						{
							if( e.has("value"))	qMeta.setSTime(e.getString("value"));
							if( e.has("value2")) qMeta.setETime(e.getString("value2"));
						}
						else if( "subject".equals(e.getString("dataIndx")) )
						{
							if( e.has("value"))	qMeta.set("subject", e.getString("value"));
						}
						else if( "to".equals(e.getString("dataIndx")) )
						{
							if( e.has("value"))	qMeta.set("to", e.getString("value"));
						}
					}
				}
			}
			PageBean pageBean = new PageBean();
//			System.err.println(curpage+"/"+pagesize);
			pageBean.setPage(curpage);
			pageBean.setPageSize(pagesize);
//			qMeta.setSTime();
			if( condition.has("TITLE") ) qMeta.setKeyword(condition.getString("TITLE"));
			if( condition.has("STATE") ) qMeta.setStatus(condition.getInt("STATE"));
			this.listData = emailMgr.queryOutboxList(pageBean, qMeta);
			JSONArray data = new JSONArray();
			if( listData != null )
				for(int i = 0; i < listData.size(); i++)
				{
					EmailOutbox outbox = (EmailOutbox)listData.get(i);
		    		JSONObject e = new JSONObject();
		    		e.put("eid", outbox.getEid());
		    		e.put("time", Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", outbox.getRequestTime().getTime()));
		    		e.put("to", outbox.getMailTo());
		    		e.put("subject", outbox.getSubject());
		    		e.put("content", outbox.getContent());
		    		e.put("cc", outbox.getCc());
		    		e.put("bcc", outbox.getBcc());
		    		e.put("module", outbox.getModule());
		    		e.put("state", outbox.getState());
		    		JSONArray attachments = new JSONArray();
		    		String[] as = Tools.split(outbox.getAttachment(), ";");
		    		for( String attachment : as)
		    		{
						String args[] = Tools.split(attachment, ", ");
						if( args[0].isEmpty() ) continue;
						String path = args[0];
						String attachmentFilename = null;
						if( args.length == 1 )
						{
							int j = path.lastIndexOf('/');
							if( j == -1 ) continue;
							attachmentFilename = attachment.substring(j);
							i = attachmentFilename.lastIndexOf('.');
							if( i == -1 ) attachmentFilename = "未知文件";
						}
						else
						{
							attachmentFilename = args[1];
						}
						JSONObject a = new JSONObject();
						a.put("name", attachmentFilename);
						a.put("path", path);
						attachments.put(a);
		    		}
		    		System.err.println(attachments.toString(4));
		    		e.put("attachments", attachments);
		    		e.put("previewurl", "email!preview.action?eid="+outbox.getEid());
		    		data.put(e);
				}
			dataJSON.put("totalRecords", pageBean.getCount());
			dataJSON.put("curPage", pageBean.getPage());
			dataJSON.put("data", data);
		}
		catch (Exception e)
		{
			log.error("Failed to query the outbox of email for exception:", e);
		}
        finally
        {
        	if( out != null )
	    		try
				{
	    			String json = dataJSON.toString();
					out.write(json.getBytes("UTF-8"));
	            	out.close();
				}
				catch (IOException e)
				{
					log.error("", e);
				}
        }
		return null;
	}
	/**
	 * 
	 * @return
	 */
	public String outbox()
	{
		String xmlpath = "/grid/local/sysemailoutbox.xml";
        return this.grid(xmlpath);
	}
	

	/**
	 * 预览邮件
	 * @return
	 */
	public String preview()
	{
    	PrintWriter out = null;
        try
        {
        	final String TAG_SNAPSHOT = "snapshot==";
        	final String TAG_IMAGES = "images==";
        	final String TAG_HTML = "html==";
        	EmailOutbox email = emailMgr.doView(eid);
			out = new PrintWriter(new OutputStreamWriter(getResponse().getOutputStream(), "UTF-8"));
        	if( email.getContent() == null )
			{
    			getResponse().setContentType("text/plain");
    			getResponse().setCharacterEncoding("UTF-8");
        		out.print("*该邮件正文为空*");
			}
        	else
        	{
        		if( super.getRequest().getParameter("s") != null )
        		{
        			getResponse().setContentType("text/plain");
        			getResponse().setCharacterEncoding("UTF-8");
            		out.print(email.getContent());
        		}
        		else if( email.getContent().startsWith(TAG_SNAPSHOT+"http") )
				{
	        		String snapshot = email.getContent().substring(TAG_SNAPSHOT.length());
	        		super.getResponse().sendRedirect(Kit.URL_PATH(super.getRequest())+"p/"+Tools.encodeUnicode(snapshot));
				}
	        	else
	        	{
	    			getResponse().setContentType("text/html");
	    			getResponse().setCharacterEncoding("UTF-8");
	    			out = new PrintWriter(new OutputStreamWriter(getResponse().getOutputStream(), "UTF-8"));
		        	if( email.getContent().startsWith(TAG_HTML) )
					{
		        		out.print(email.getContent().substring(TAG_HTML.length()));
					}
		        	else if( email.getContent().startsWith(TAG_IMAGES+"http") )
					{
		        		out.println("<html><body style='img{max-width:600px;}'>");
		        		String images[] = email.getContent().substring(TAG_IMAGES.length()).split(";");
		        		for(String image : images)
		        			out.println("<p><img src='"+image+"'></p>");
		        		out.println("</body>\r\n\r\n<script type='text/javascript' LANGUAGE='JavaScript'>if(top&&top.skit_alert){top.skit_alert('该图片将以邮件附件的形式发送给用户。');}</script></html>");
					}
		        	else
		        	{
		        		out.println(email.getContent());
		        	}
		        	if( email.getAttachment() != null )
		        	{
		        		String attachments[] = email.getAttachment().split(";");
		        		if( attachments.length > 0 ) out.println("<h3>以下是附件</h3>");
		        		for(String attachment : attachments)
		        		{
		        			String[] parmas = attachment.split(", ");
		        			String name = parmas.length>1?parmas[1]:parmas[0];
		        			out.println("<p><a href='"+parmas[0]+"' target='_blank'>"+name+"</a></p>");
		        		}
		        	}
	        	}
        	}
        }
        catch(Exception e)
        {
        	out.println("Failed to preview email "+eid);
        	out.println();
        	log.error("Failed to preview email ", e);
        }
        finally
        {
    		try
			{
            	if( out != null ) out.close();
			}
			catch (Exception e)
			{
			}
        }		
		return null;
	}
	
	public void setEmailMgr(EmailMgr emailMgr) {
		this.emailMgr = emailMgr;
	}

	public long getEid() {
		return eid;
	}
	public void setEid(long eid) {
		this.eid = eid;
	}
}
