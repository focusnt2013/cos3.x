package com.focus.cos.web.ops.dao;

import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.sql.Blob;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Hibernate;
import org.hibernate.LockMode;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.lob.SerializableBlob;
import org.springframework.context.ApplicationContext;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import com.focus.cos.web.common.QueryMeta;
import com.focus.cos.web.common.paginate.PageBean;
import com.focus.cos.web.common.paginate.Paginate;
import com.focus.cos.web.ops.vo.EmailOutbox;
import com.focus.cos.web.util.Tools;

import oracle.sql.BLOB;

public class EmailOutboxDAO extends HibernateDaoSupport
{
	private static final Log log = LogFactory.getLog(EmailOutboxDAO.class);
	
	private Paginate paginate;
	private Properties dbPropMap;

	// 分页实体
	private PageBean pageBean;

	protected void initDao()
	{
		// do nothing
	}

	public void save(EmailOutbox transientInstance)
	{
		log.debug("saving EmailOutbox instance");
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
		
	public void saveB(EmailOutbox transientInstance) throws Exception
	{
		log.debug("saving Email instance:"+transientInstance.getEid());
		Session session = super.getSessionFactory().openSession();
		Transaction transaction = session.beginTransaction();
		try
		{
			transaction.begin();
			if( dbPropMap.getProperty("database").toLowerCase().indexOf("oracle")!=-1)
			{   
				byte nullByte[] = new byte[1];
				nullByte[0] = 0;
				transientInstance.setData(Hibernate.createBlob(nullByte));
				session.saveOrUpdate(transientInstance);
				session.flush();
				session.refresh(transientInstance,LockMode.UPGRADE);
				SerializableBlob serializableBlob = (SerializableBlob)transientInstance.getData();
				if( serializableBlob.getWrappedBlob() instanceof oracle.sql.BLOB )
				{
					BLOB blob = (BLOB)serializableBlob.getWrappedBlob();
		            OutputStream out = blob.getBinaryOutputStream();
		            out.write( transientInstance.getPayload());
		            out.flush();
		            out.close();
		            session.flush();
				}
//				byte nullByte[] = new byte[1];
//				nullByte[0] = 0;
//				transientInstance.setData(Hibernate.createBlob(nullByte));
//				getHibernateTemplate().saveOrUpdate(transientInstance);
//				SerializableBlob serializableBlob = (SerializableBlob)transientInstance.getData();
//				if( serializableBlob.getWrappedBlob() instanceof oracle.sql.BLOB )
//				{
//					BLOB blob = (BLOB)serializableBlob.getWrappedBlob();
//		            OutputStream out = blob.getBinaryOutputStream();
//		            out.write( transientInstance.getPayload() );
//		            out.flush();
//		            out.close();
//				}				
			}
			else
			{
				Blob blob = Hibernate.createBlob(new ByteArrayInputStream(transientInstance.getPayload()));					
				transientInstance.setData(blob);
				session.saveOrUpdate(transientInstance);
//				getHibernateTemplate().saveOrUpdate(transientInstance);				
			}

			log.debug("save successful");
			transaction.commit();
		}
		catch (Exception re)
		{
			log.error("save failed:"+transientInstance.getEid(), re);
			transaction.rollback();
			throw re;
		}
		finally
		{
			session.close();
		}			
	}

	public void delete(EmailOutbox persistentInstance)
	{
		log.debug("deleting EmailOutbox instance");
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
	

	public void deleteById(long eid)
	{
		log.debug("deleting EmailOutbox instance");
		try
		{
			EmailOutbox e = new EmailOutbox();
			e.setEid(eid);
			getHibernateTemplate().delete(e);
			log.debug("delete successful");
		}
		catch (RuntimeException re)
		{
			log.error("delete failed", re);
			throw re;
		}
	}

	public EmailOutbox findById(java.lang.Long id)
	{
		log.debug("getting EmailOutbox instance with id: " + id);
		try
		{
			EmailOutbox instance = (EmailOutbox) getHibernateTemplate().get("com.focus.cos.web.ops.vo.EmailOutbox", id);
			return instance;
		}
		catch (RuntimeException re)
		{
			log.error("get failed", re);
			throw re;
		}
	}

	public List<?> findByExample(EmailOutbox instance)
	{
		log.debug("finding EmailOutbox instance by example");
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
		log.debug("finding EmailOutbox instance with property: " + propertyName + ", value: " + value);
		try
		{
			String queryString = "from EmailOutbox as model where model." + propertyName + "= ?";
			return getHibernateTemplate().find(queryString, value);
		}
		catch (RuntimeException re)
		{
			log.error("find by property name failed", re);
			throw re;
		}
	}

	public List<?> findAll()
	{
		log.debug("finding all EmailOutbox instances");
		try
		{
			String queryString = "from EmailOutbox";
			return getHibernateTemplate().find(queryString);
		}
		catch (RuntimeException re)
		{
			log.error("find all failed", re);
			throw re;
		}
	}

	public EmailOutbox merge(EmailOutbox detachedInstance)
	{
		log.debug("merging EmailOutbox instance");
		try
		{
			EmailOutbox result = (EmailOutbox) getHibernateTemplate().merge(detachedInstance);
			log.debug("merge successful");
			return result;
		}
		catch (RuntimeException re)
		{
			log.error("merge failed", re);
			throw re;
		}
	}

	public void attachDirty(EmailOutbox instance)
	{
		log.debug("attaching dirty EmailOutbox instance");
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

	public void attachClean(EmailOutbox instance)
	{
		log.debug("attaching clean EmailOutbox instance");
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

	public static EmailOutboxDAO getFromApplicationContext(ApplicationContext ctx)
	{
		return (EmailOutboxDAO) ctx.getBean("EmailOutboxDAO");
	}
	
	public List<?> queryOutboxList(PageBean p,QueryMeta qMeta)throws Exception
	{
		List<?> ibList = new ArrayList<EmailOutbox>();
        ArrayList<Object> listParameter = new ArrayList<Object>();
        StringBuffer sql = new StringBuffer("SELECT a FROM EmailOutbox a WHERE 1 = 1");
        StringBuffer sqlc = new StringBuffer("SELECT COUNT(*) FROM EmailOutbox a WHERE 1 = 1");
        //查询条件
        StringBuffer cond = new StringBuffer();
    	if(qMeta.getSTime()!= null && !qMeta.getSTime().equals(""))
    	{
    		cond.append(" AND a.requestTime >= ?");
        	listParameter.add(qMeta.getStartDate());
    	}
    	if(qMeta.getETime() != null && !qMeta.getETime().equals(""))
    	{
    		cond.append(" AND a.requestTime <= ?");
        	listParameter.add(qMeta.getEndDate());
    	}
    	if(qMeta.getKeyword()!= null && !qMeta.getKeyword().equals("") && !qMeta.getKeyword().equals("请输入关键字"))
    	{
    		String keyword = Tools.replaceStr(qMeta.getKeyword());
    		cond.append(" AND ( a.subject LIKE '%"+keyword+"%' or a.mailTo LIKE '%"+keyword+"%')");
    	}
    	if(qMeta.getStatus()!=-1)
    	{
    		cond.append(" AND a.state = ?");
    		listParameter.add((short)qMeta.getStatus());
    	}
    	if(qMeta.exist("subject"))
    	{
    		cond.append(" AND a.subject LIKE '%"+qMeta.getString("subject")+"%'");
    	}
    	if(qMeta.exist("to"))
    	{
    		cond.append(" AND a.mailTo LIKE '%"+qMeta.getString("to")+"%'");
    	}
    	if(qMeta.exist("module"))
    	{
    		cond.append(" AND a.module = '"+qMeta.getString("module")+"'");
    	}
    	if(qMeta.exist("states"))
    	{
    		cond.append(" AND a.state IN("+qMeta.getString("states")+")");
    	}
        sql.append(cond);
        sqlc.append(cond);
        sql.append(" ORDER BY a.requestTime DESC");
        
        p.setListSQL(sql.toString());
        p.setTotalCountSQL(sqlc.toString());
        p.setCount(this.getPaginate().getTotalCount(p,listParameter));
        
        this.setPageBean(p);
        ibList = this.getPaginate().getList(p,listParameter);
        return ibList;
	}

	public Paginate getPaginate() {
		return paginate;
	}

	public void setPaginate(Paginate paginate) {
		this.paginate = paginate;
	}

	public PageBean getPageBean() {
		return pageBean;
	}

	public void setPageBean(PageBean pageBean) {
		this.pageBean = pageBean;
	}

	public Properties getDbPropMap()
	{
		return dbPropMap;
	}

	public void setDbPropMap(Properties dbPropMap)
	{
		this.dbPropMap = dbPropMap;
	}
}