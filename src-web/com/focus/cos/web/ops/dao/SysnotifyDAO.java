package com.focus.cos.web.ops.dao;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.LockMode;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import com.focus.cos.web.common.QueryMeta;
import com.focus.cos.web.common.paginate.PageBean;
import com.focus.cos.web.common.paginate.Paginate;
import com.focus.cos.web.ops.vo.Sysnotify;
import com.focus.cos.web.util.Tools;

public class SysnotifyDAO extends HibernateDaoSupport
{
	private static final Log log = LogFactory.getLog(SysnotifyDAO.class);
	
	private Paginate paginate;
	// 分页实体
	private PageBean pageBean;
	protected void initDao()
	{
		// do nothing
	}

	public void save(Sysnotify transientInstance)
	{
		log.debug("saving Sysnotify instance");
		try
		{
			getHibernateTemplate().save(transientInstance);
			log.debug("save successful");
		}
		catch (RuntimeException re)
		{
			log.error("save failed", re);
			throw re;
		}
	}

	public void delete(Sysnotify persistentInstance)
	{
		log.debug("deleting Sysnotify instance");
		try
		{
			getHibernateTemplate().delete(persistentInstance);
			log.debug("delete successful");
		}
		catch (RuntimeException re)
		{
			log.error("delete failed", re);
			throw re;
		}
	}

	public Sysnotify findById(java.lang.Long id)
	{
		log.debug("getting Sysnotify instance with id: " + id);
		try
		{
			Sysnotify instance = (Sysnotify) getHibernateTemplate().get("com.focus.cos.web.ops.vo.Sysnotify", id);
			return instance;
		}
		catch (RuntimeException re)
		{
			log.error("get failed", re);
			throw re;
		}
	}

	public List<?> findByExample(Sysnotify instance)
	{
		log.debug("finding Sysnotify instance by example");
		try
		{
			List<?> results = getHibernateTemplate().findByExample(instance);
			log.debug("find by example successful, result size: " + results.size());
			return results;
		}
		catch (RuntimeException re)
		{
			log.error("find by example failed", re);
			throw re;
		}
	}

	public List<?> findByProperty(String propertyName, Object value)
	{
		log.debug("finding Sysnotify instance with property: " + propertyName + ", value: " + value);
		try
		{
			String queryString = "from Sysnotify as model where model." + propertyName + "= ?";
			return getHibernateTemplate().find(queryString, value);
		}
		catch (RuntimeException re)
		{
			log.error("find by property name failed", re);
			throw re;
		}
	}

	public int count(QueryMeta queryMeta)
	{
        StringBuffer sql = new StringBuffer("SELECT COUNT(*) FROM Sysnotify a WHERE 1 = 1");   
        ArrayList<Object> listParameter = new ArrayList<Object>();
        if( queryMeta.exist("<state") )
        {
        	sql.append(" AND a.state<?");   	
        	listParameter.add(queryMeta.get("<state"));
        }
        if( queryMeta.exist("state") )
        {
        	sql.append(" AND a.state=?");   	
        	listParameter.add(queryMeta.get("state"));
        }
        if( queryMeta.exist("title") )
        {
        	sql.append(" AND a.title=?");   
        	listParameter.add(queryMeta.get("title"));	        	
        }
        if( queryMeta.exist("filter") )
        {
        	sql.append(" AND a.filter=?");   
        	listParameter.add(queryMeta.get("filter"));	        	
        }
        if( queryMeta.exist("useraccount") )
        {
        	sql.append(" AND a.useraccount=?"); 
        	listParameter.add(queryMeta.get("useraccount"));	      	        	
        }
        if( queryMeta.exist("priority") )
        {
        	sql.append(" AND a.priority=?");   
        	listParameter.add(queryMeta.get("priority"));		        	
        }
        if(queryMeta.exist("startDate"))
    	{
        	sql.append(" AND a.notifytime >= ?");
        	listParameter.add(queryMeta.getStartDate());
    	}
        if(queryMeta.exist("endDate"))
    	{
        	sql.append(" AND a.notifytime <= ?");
        	listParameter.add(queryMeta.getEndDate());
    	}        
        if(queryMeta.exist("keyword"))
    	{
    		String keyword = Tools.replaceStr(queryMeta.getKeyword());
    		sql.append(" AND (a.title LIKE '%"+keyword+"%'");
    		sql.append(" OR");
    		sql.append(" a.context LIKE '%"+keyword+"%')");
    	}
        List<?> list = getHibernateTemplate().find(sql.toString(), listParameter.toArray());
        int count = 0;
        if (list.size() > 0)
        {
            count = ((Long) list.get(0)).intValue();
        }
        return count;
	}
	/**
	 * 按查询条件查询
	 * @param queryMeta
	 * @return
	 */
	public List<?> find(PageBean p, QueryMeta queryMeta)throws Exception
	{
        StringBuffer sql = new StringBuffer("SELECT a FROM Sysnotify a WHERE 1 = 1");   
        ArrayList<Object> listParameter = new ArrayList<Object>();
        if( queryMeta.exist("state") )
        {
        	sql.append(" AND a.state=?");   	
        	listParameter.add(queryMeta.get("state"));
        }
        if( queryMeta.exist("title") )
        {
        	sql.append(" AND a.title=?");   
        	listParameter.add(queryMeta.get("title"));	        	
        }
        if( queryMeta.exist("filter") )
        {
        	sql.append(" AND a.filter=?");   
        	listParameter.add(queryMeta.get("filter"));	        	
        }
        if( queryMeta.exist("useraccount") )
        {
        	sql.append(" AND a.useraccount=?"); 
        	listParameter.add(queryMeta.get("useraccount"));	      	        	
        }
        if( queryMeta.exist("priority") )
        {
        	sql.append(" AND a.priority>=?");   
        	listParameter.add(queryMeta.get("priority"));		        	
        }
        if(queryMeta.exist("startDate"))
    	{
        	sql.append(" AND a.notifytime >= ?");
        	listParameter.add(queryMeta.getStartDate());
    	}
        if(queryMeta.exist("endDate"))
    	{
        	sql.append(" AND a.notifytime <= ?");
        	listParameter.add(queryMeta.getEndDate());
    	}        
        if(queryMeta.exist("keyword"))
    	{
    		String keyword = Tools.replaceStr(queryMeta.getKeyword());
    		sql.append(" AND (a.title LIKE '%"+keyword+"%'");
    		sql.append(" OR");
    		sql.append(" a.content LIKE '%"+keyword+"%')");
    	}
        sql.append(" ORDER BY a.notifytime DESC");
        if( p != null )
        {
	        p.setListSQL(sql.toString());
	        p.setTotalCountSQL(sql.toString());
	        p.setCount(this.getPaginate().getTotalCount(p,listParameter));
	        
	        this.setPageBean(p);
	        return this.getPaginate().getList(p,listParameter);
        }
        else
        {
        	return getHibernateTemplate().find(sql.toString(),listParameter.toArray());
        }
	}
	
	public List<?> find(QueryMeta queryMeta)
	{
		try
		{
			return find(null, queryMeta);
		}
		catch (Exception e)
		{
			return new ArrayList<Object>();
		}
	}
	
	/**
	 * 
	 * @param filter
	 */
	public void deleteByFilter(String filter, String before)
	{
		Session session = null;
		try
		{
			String sql = "DELETE Sysnotify WHERE filter='" + filter + "' AND notifytime<'"+before+"'";
			session = this.getSession();
			Transaction tx = session.beginTransaction();
			int results = session.createQuery(sql).executeUpdate();
			if( results > 0 ) log.debug("Succeed to delete "+results+" sysnotifies.");
			tx.commit();
		}
		catch (RuntimeException re)
		{
			log.error("delete failed", re);
			throw re;
		}
		finally
		{
			releaseSession(session);
		}
	}
	
	public void deleteByIds(String rids)
	{
		log.debug("deleting Sysnotify instance");
		Session session = null;
		try
		{
			String sql = "DELETE Sysnotify WHERE rid in ( " + rids + ")";
			session = this.getSession();
			Transaction tx = session.beginTransaction();
			int results = session.createQuery(sql).executeUpdate();
			tx.commit();
			log.debug("删除了" + results + "条记录！");
			log.debug("delete successful");
		}
		catch (RuntimeException re)
		{
			log.error("delete failed", re);
			throw re;
		}
		finally
		{
			releaseSession(session);
		}
	}
	
	public List<?> findAll()
	{
		log.debug("finding all Sysnotify instances");
		try
		{
			String queryString = "from Sysnotify";
			return getHibernateTemplate().find(queryString);
		}
		catch (RuntimeException re)
		{
			log.error("find all failed", re);
			throw re;
		}
	}

	public Sysnotify merge(Sysnotify detachedInstance)
	{
		log.debug("merging Sysnotify instance");
		try
		{
			Sysnotify result = (Sysnotify) getHibernateTemplate().merge(detachedInstance);
			log.debug("merge successful");
			return result;
		}
		catch (RuntimeException re)
		{
			log.error("merge failed", re);
			throw re;
		}
	}

	public void attachDirty(Sysnotify instance)
	{
		log.debug("attaching dirty Sysnotify instance");
		try
		{
			getHibernateTemplate().saveOrUpdate(instance);
			log.debug("attach successful");
		}
		catch (RuntimeException re)
		{
			log.error("attach failed", re);
			throw re;
		}
	}

	public void attachClean(Sysnotify instance)
	{
		log.debug("attaching clean Sysnotify instance");
		try
		{
			getHibernateTemplate().lock(instance, LockMode.NONE);
			log.debug("attach successful");
		}
		catch (RuntimeException re)
		{
			log.error("attach failed", re);
			throw re;
		}
	}

	public Paginate getPaginate()
	{
		return paginate;
	}

	public void setPaginate(Paginate paginate)
	{
		this.paginate = paginate;
	}

	public PageBean getPageBean()
	{
		return pageBean;
	}

	public void setPageBean(PageBean pageBean)
	{
		this.pageBean = pageBean;
	}
}