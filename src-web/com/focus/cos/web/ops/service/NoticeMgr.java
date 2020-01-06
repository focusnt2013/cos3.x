package com.focus.cos.web.ops.service;

import java.util.List;

import com.focus.cos.web.common.QueryMeta;
import com.focus.cos.web.common.paginate.PageBean;
import com.focus.cos.web.ops.dao.NoticeDAO;
import com.focus.cos.web.ops.vo.Notice;

public class NoticeMgr
{
	private NoticeDAO noticeDao;

	public List<?> queryNoticeList(PageBean pageBean,QueryMeta queryMeta)throws Exception
	{
		return noticeDao.queryList(pageBean, queryMeta);
	}
	
	public void addNotice(Notice notice)
	{
		noticeDao.save(notice);
	}
	
	public void modifyNotice(Notice notice)
	{
		noticeDao.attachDirty(notice);
	}
	
	public Notice findNotice(long id)
	{
		return noticeDao.findById(id);
	}
	
	public void deleteNoticeByIds(String ids)
	{
		noticeDao.deleteByIds(ids);
	}
	
	public void releaseNotice(String ids)
	{
		noticeDao.release(ids);
	}
	
	public String getPageMenu()
	{
		PageBean p = noticeDao.getPageBean();
		return noticeDao.getPaginate().getPageMenu(p);
	}
	
	public void setNoticeDao(NoticeDAO noticeDao)
	{
		this.noticeDao = noticeDao;
	}

	public NoticeDAO getNoticeDao()
	{
		return noticeDao;
	}
}
