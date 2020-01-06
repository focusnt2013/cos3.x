package com.focus.cos.web.ops.dao;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.LockMode;
import org.hibernate.Session;
import org.springframework.context.ApplicationContext;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import com.focus.cos.web.common.QueryMeta;
import com.focus.cos.web.common.paginate.PageBean;
import com.focus.cos.web.common.paginate.Paginate;
import com.focus.cos.web.ops.vo.Notice;
import com.focus.cos.web.util.Tools;

public class NoticeDAO extends HibernateDaoSupport
{
	private static final Log log = LogFactory.getLog(NoticeDAO.class);
	
    // 分页处理接口
    private Paginate paginate;

    // 分页实体
    private PageBean pageBean;
	protected void initDao()
	{
		// do nothing
	}

	public void save(Notice transientInstance)
	{
		log.debug("saving Notice instance");
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

	public void delete(Notice persistentInstance)
	{
		log.debug("deleting Notice instance");
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

	public Notice findById(java.lang.Long id)
	{
		log.debug("getting Notice instance with id: " + id);
		try
		{
			Notice instance = (Notice) getHibernateTemplate().get("com.focus.cos.web.ops.vo.Notice", id);
			return instance;
		}
		catch (RuntimeException re)
		{
			log.error("get failed", re);
			throw re;
		}
	}

	public List<?> findByExample(Notice instance)
	{
		log.debug("finding Notice instance by example");
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
		log.debug("finding Notice instance with property: " + propertyName + ", value: " + value);
		try
		{
			String queryString = "from Notice as model where model." + propertyName + "= ?";
			return getHibernateTemplate().find(queryString, value);
		}
		catch (RuntimeException re)
		{
			log.error("find by property name failed", re);
			throw re;
		}
	}

	public List<?> queryList(PageBean pageBean,QueryMeta queryMeta)throws Exception
	{
		List<?> list = new ArrayList<Notice>();
        StringBuffer sql = new StringBuffer("SELECT n FROM Notice n WHERE 1 = 1");
        StringBuffer sqlc = new StringBuffer("SELECT COUNT(*) FROM Notice n WHERE 1 = 1");
        
        //查询条件
        StringBuffer cond = new StringBuffer();
        
        ArrayList<Object> listParameter = new ArrayList<Object>();
        if( queryMeta.exist("state") )
        {
        	cond.append(" AND n.state="+queryMeta.get("state"));   	
        }
        if(queryMeta.exist("startDate"))
    	{
        	cond.append(" AND n.addTime >= ?");
        	listParameter.add(queryMeta.getStartDate());
    	}
        if(queryMeta.exist("endDate"))
    	{
        	cond.append(" AND n.addTime <= ?");
        	listParameter.add(queryMeta.getEndDate());
    	}
        if(queryMeta.exist("keyword"))
    	{
        	//截取前后空格
    		String keyword = Tools.replaceStr(queryMeta.getKeyword()).trim();
    		cond.append(" AND (n.title LIKE '%"+keyword+"%'");
    		cond.append(" OR");
    		cond.append(" n.content LIKE '%"+keyword+"%'");
    		cond.append(" )");
    	}
        
        sql.append(cond);
        sqlc.append(cond);
        
        sql.append(" ORDER BY id desc");
        
        pageBean.setListSQL(sql.toString());
        pageBean.setTotalCountSQL(sqlc.toString());
        pageBean.setCount(this.getPaginate().getTotalCount(pageBean));
        
        this.setPageBean(pageBean);
        list = this.getPaginate().getList(pageBean,listParameter);
        return list;
	}
	
	public boolean deleteByIds(String ids)
	{
		Session session = this.getSession();
		int c = session.createQuery("DELETE Notice WHERE id IN ("+ids+")").executeUpdate();
		releaseSession(session);
		return c > 0;
	}
	
	public boolean release(String ids)
	{
		Session session = this.getSession();
		int c = session.createQuery("UPDATE Notice SET state =1 WHERE id IN ("+ids+")").executeUpdate();
		releaseSession(session);
		return c > 0;
	}
	
	public List<?> findAll()
	{
		log.debug("finding all Notice instances");
		try
		{
			String queryString = "from Notice";
			return getHibernateTemplate().find(queryString);
		}
		catch (RuntimeException re)
		{
			log.error("find all failed", re);
			throw re;
		}
	}

	public Notice merge(Notice detachedInstance)
	{
		log.debug("merging Notice instance");
		try
		{
			Notice result = (Notice) getHibernateTemplate().merge(detachedInstance);
			log.debug("merge successful");
			return result;
		}
		catch (RuntimeException re)
		{
			log.error("merge failed", re);
			throw re;
		}
	}

	public void attachDirty(Notice instance)
	{
		log.debug("attaching dirty Notice instance");
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

	public void attachClean(Notice instance)
	{
		log.debug("attaching clean Notice instance");
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

	public static NoticeDAO getFromApplicationContext(ApplicationContext ctx)
	{
		return (NoticeDAO) ctx.getBean("NoticeDAO");
	}
}