package com.focus.cos.web.ops.service;

import java.util.List;

import com.focus.cos.web.common.QueryMeta;
import com.focus.cos.web.common.paginate.PageBean;
import com.focus.cos.web.ops.dao.EmailOutboxDAO;
import com.focus.cos.web.ops.vo.EmailOutbox;

public class EmailMgr
{
	private EmailOutboxDAO emailOutboxDao;

	public long addEmailOutbox(EmailOutbox outbox)
	{
		emailOutboxDao.save(outbox);
		return outbox.getEid();
	}

	public void delete(long eid) throws Exception
	{
		EmailOutbox e = new EmailOutbox();
		e.setEid(eid);
		emailOutboxDao.delete(e);
	}
	
	public long save(EmailOutbox outbox) throws Exception
	{
		emailOutboxDao.saveB(outbox);
		return outbox.getEid();
	}
	
	public void setEmailOutboxDao(EmailOutboxDAO emailOutboxDao)
	{
		this.emailOutboxDao = emailOutboxDao;
	}
	
	public EmailOutbox doView(long eid)
	{
		return emailOutboxDao.findById(eid);
	}
	
	public List<?> queryOutboxList(PageBean pageBean, QueryMeta qMeta)throws Exception
	{
		return emailOutboxDao.queryOutboxList(pageBean, qMeta);
	}
	
	/**
	 * 取分页菜单
	 * @return String
	 */
	public String getPageMenu()
	{
		PageBean p = emailOutboxDao.getPageBean();
		return emailOutboxDao.getPaginate().getPageMenu(p);
	}
}
