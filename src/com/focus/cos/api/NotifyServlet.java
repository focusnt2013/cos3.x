package com.focus.cos.api;

import java.io.OutputStream;
import java.io.Serializable;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import com.focus.cos.control.COSApi;
import com.focus.cos.ops.dao.NotifyDao;
import com.focus.sql.Dao;
import com.focus.util.IOHelper;
import com.focus.util.Log;

public class NotifyServlet extends AbaseServlet
{
	private static final long serialVersionUID = -6082791442929441606L;
	private NotifyDao dao = null;
	public NotifyServlet(COSApi server) throws Exception
	{
		super(server);
		if( !this.server.isProxy() )
		{
			dao = new NotifyDao();
			dao.prepareInsert();
		}
	}

	@Override
	public void query(HttpServletRequest request, HttpServletResponse response, StringBuffer log) throws Exception 
	{
		response.setStatus(404);
		OutputStream out = response.getOutputStream();
		out.write("请求的服务并不存在".getBytes());
		out.flush();
		out.close();
		log.append("\r\n\t404 not found service.");
	}

	@Override
	public synchronized void save(HttpServletRequest request, byte[] payload, JSONObject response) throws Exception  
	{
		Serializable s = IOHelper.readSerializableNoException(payload);
		if( s != null && s instanceof Sysnotify )
		{
			Sysnotify notify = (Sysnotify)s;
			
			if( notify.getTitle() == null || notify.getTitle().isEmpty() )
			{
				throw new Exception("没有设置系统通知的标题(title)");
			}
			if( notify.getFilter() == null || notify.getFilter().isEmpty() )
			{
				throw new Exception("没有设置系统通知的分类标签(filter)");
			}
			if( notify.getUseraccount() == null || notify.getUseraccount().isEmpty() )
			{
				throw new Exception("没有设置系统通知的接收用户(useraccount)");
			}
			long id = Dao.getMaxId("TB_NOTIFIES", "NID") + 1;
			notify.setNid(id);
//			dao.get(notify.getFilter(), notify.getTitle(), notify.getUseraccount());
			dao.save(notify);
			int r = dao.execute();
			if( r == 1 )
			{
				response.put("id", id);
			}
			else
			{
				response.put("error", "发送系统通知没生效");
			}
		}
		else
		{
			response.put("error", "Unknown notify("+s+").");
		}
	}

	/**
	 * 直接写数据库操作写系统通知
	 * @param notify
	 */
	public synchronized void write(Sysnotify notify)
	{
		try 
		{
			long id = Dao.getMaxId("TB_NOTIFIES", "NID") + 1;
			notify.setNid(id);
			dao.save(notify);
			dao.execute();
		}
		catch (SQLException e) 
		{
			Log.err("Failed to write notify.", e);
		}
	}
}
