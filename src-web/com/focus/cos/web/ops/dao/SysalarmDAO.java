package com.focus.cos.web.ops.dao;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.LockMode;
import org.springframework.context.ApplicationContext;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import com.focus.cos.web.common.paginate.PageBean;
import com.focus.cos.web.common.paginate.Paginate;
import com.focus.cos.web.ops.vo.Sysalarm;


/**
 * Description:
 * Create Date:Dec 3, 2008
 * @author Nixin
 *
 * @since 1.0
 */
public class SysalarmDAO extends HibernateDaoSupport
{
	private static final Log log = LogFactory.getLog(SysalarmDAO.class);

	// property constants
	public static final String DN = "dn";

	public static final String ORGSEVERITY = "orgseverity";

	public static final String ORGTYPE = "orgtype";

	public static final String PROBABLECAUSE = "probablecause";

	public static final String ACTIVESTATUS = "activestatus";

	public static final String ALARM_TITLE = "alarmTitle";

	public static final String ALARM_TEXT = "alarmText";
	
	public static final String ID = "id";

	private Paginate paginate;

	// 分页实体
	private PageBean pageBean;
	
	protected void initDao()
	{
		// do nothing
	}

	public void save(Sysalarm transientInstance)
	{
		log.debug("saving Sysalarm instance");
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
	
	public void update(Sysalarm alarmInstance)
	{
//		log.debug("updating Sysalarm instance");
		try
		{
			getHibernateTemplate().update(alarmInstance);
		}
		catch (RuntimeException re)
		{
			log.error("save failed", re);
			throw re;
		}
	}

	public void delete(Sysalarm persistentInstance)
	{
		log.debug("deleting Sysalarm instance");
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

	public Sysalarm findById(java.lang.Long id)
	{
		log.debug("getting Sysalarm instance with id: " + id);
		try
		{
			Sysalarm instance = (Sysalarm) getHibernateTemplate()
					.get("com.focus.cos.web.ops.vo.Sysalarm", id);
			return instance;
		}
		catch (RuntimeException re)
		{
			log.error("get failed", re);
			throw re;
		}
	}

	public List<?> findByExample(Sysalarm instance)
	{
		log.debug("finding Sysalarm instance by example");
		try
		{
			List<?> results = getHibernateTemplate().findByExample(instance);
			log.debug("find by example successful, result size: "
					+ results.size());
			return results;
		}
		catch (RuntimeException re)
		{
			log.error("find by example failed", re);
			throw re;
		}
	}
	/**
	 * 取出数据库中告警表中所有DN
	 * @return
	 */
	public List<?> findAllAlarmDN(int type){
		String sql="select distinct dn from Sysalarm where activestatus="+type;
		return getHibernateTemplate().find(sql);
	}
	public List<?> findByProperty(String propertyName, Object value)
	{
		log.debug("finding Sysalarm instance with property: " + propertyName
				+ ", value: " + value);
		try
		{
			String queryString = "from Sysalarm as model where model."
					+ propertyName + "= ?";
			return getHibernateTemplate().find(queryString, value);			
		}
		catch (RuntimeException re)
		{
			log.error("find by property name failed", re);
			throw re;
		}
	}
	
	/**
	 * 查找是否存在重复的告警
	 * @param values
	 * @return
	 */
	public Sysalarm found(Object[] values)
		throws Exception
	{
		StringBuffer query = new StringBuffer("from Sysalarm as model where ");
		query.append("model.dn=?");
		query.append("and model.module=?");
		query.append("and model.orgseverity=?");
		query.append("and model.orgtype=?");
		query.append("and model.alarmTitle=?");
		query.append("and model.id=?");
		query.append("and ( model.activestatus=-1 or ( model.activestatus=0 and model.ackUser='' ) )");
		query.append(" order by eventtime asc");
		List<?> list = getHibernateTemplate().find(query.toString(), values);
		return (Sysalarm)(list!=null&&list.size()>0?list.get(0):null);
	}
	
	/**
	 * 计算条件数据
	 * @param names
	 * @param values
	 * @return
	 */
	public int count(String[] names, Object[] values)
	{
		try
		{			
			StringBuffer query = new StringBuffer("select count(*) from Sysalarm as model where 1=1");
			for(int i = 0; i < (names.length<=values.length?names.length:values.length);i++)
			{
				if(values[i] != null && !values[i].equals(""))
				{
					if(names[i].equals("eventtime"))
					{
						query.append(" and model."+names[i]+" > ?");
					}
					else
					{
						query.append(" and model."+names[i]+" = ?");
					}
				}
			}
			List<?> list = getHibernateTemplate().find(query.toString(), values);
			int count = 0;
			for (int i = 0; i < list.size(); i++)
			{
				Integer ret = ((Long) list.get(0)).intValue();
				if (ret != null)
				{
					count += ret;
				}
			}
			return count;
		}
		catch(RuntimeException e)
		{
			StringBuffer sb = new StringBuffer();
			for( int i = 0; i < values.length; i++)
			{
				sb.append("\r\n\t");
				sb.append(names[i]);
				sb.append("=");
				sb.append(values[i]);
			}
			log.error("Failed to count by properties"+sb.toString(), e);
			throw e;
		}
	}
	/**
	 * 
	 * @param propertyNames
	 * @param values
	 * @param orderby
	 * @return
	 */
	public List<?> query(String[] propertyNames, Object[] values, String orderby)
	{
		try
		{			
			StringBuffer query = new StringBuffer("from Sysalarm as model where 1=1");
			for(int i = 0; i < (propertyNames.length<=values.length?propertyNames.length:values.length);i++)
			{
				if(values[i] != null && !values[i].equals(""))
				{
					if(propertyNames[i].equals("eventtime"))
					{
						query.append(" and model."+propertyNames[i]+" > ?");
					}
					else
					{
						query.append(" and model."+propertyNames[i]+" = ?");
					}
				}
			}
			query.append(" order by eventtime "+orderby);
			return getHibernateTemplate().find(query.toString(), values);
		}
		catch(RuntimeException e)
		{
			StringBuffer sb = new StringBuffer();
			for( int i = 0; i < values.length; i++)
			{
				sb.append("\r\n\t");
				sb.append(propertyNames[i]);
				sb.append("=");
				sb.append(values[i]);
			}
			log.error("Failed to find by properties"+sb.toString(), e);
			throw e;
		}
	}
	
//	public List<?> queryHistoryAlarmList(PageBean pageBean,QueryMeta qMeta)throws Exception
//	{
//		ArrayList<Object> listParameter = new ArrayList<Object>();
//		StringBuffer sql = new StringBuffer("SELECT alarm FROM Sysalarm alarm WHERE 1 = 1 ");
//		StringBuffer sqlc = new StringBuffer("SELECT COUNT(*) FROM Sysalarm alarm WHERE 1 = 1 ");
//		StringBuffer cond = new StringBuffer();
//		sql.append(" and activestatus = 0 ");
//		sqlc.append(" and activestatus = 0 ");
//		String timeColumn = "alarm.eventtime";
//		if(qMeta.getSTime()!= null && !qMeta.getSTime().equals(""))
//		{
//			cond.append(" AND " + timeColumn + " >= ?");
//			listParameter.add(qMeta.getStartDate());
//		}
//		if(qMeta.getETime()!=null && !qMeta.getETime().equals(""))
//		{
//			cond.append(" AND " + timeColumn + " <= ?");
//			listParameter.add(qMeta.getEndDate());
//		}
//		if(qMeta.getOrgseverity()!=null && !qMeta.getOrgseverity().equals(""))
//		{
//			cond.append(" AND alarm.orgseverity = '" +qMeta.getOrgseverity()+ "'");
//		}
//		if(qMeta.getOrgtype()!=null && !qMeta.getOrgtype().equals(""))
//		{
//			cond.append(" AND alarm.orgtype = '" +qMeta.getOrgtype()+"'");
//		}
//		if(null!=qMeta.getDn()&&!"".equals(qMeta.getDn())){
//			cond.append(" AND alarm.dn = '" +qMeta.getDn()+"'");
//		}
//		sql.append(cond);
//		sql.append(" ORDER BY alarm.eventtime DESC");
//		log.debug(sql);
//		sqlc.append(cond);
//		
//		pageBean.setListSQL(sql.toString());
//		pageBean.setTotalCountSQL(sqlc.toString());
//		pageBean.setCount(this.getPaginate().getTotalCount(pageBean,listParameter));
//		this.setPageBean(pageBean);
//		return this.getPaginate().getList(pageBean,listParameter);
//	}
	
	public List<?> findByDn(Object dn)
	{
		return findByProperty(DN, dn);
	}

	public List<?> findByOrgseverity(Object orgseverity)
	{
		return findByProperty(ORGSEVERITY, orgseverity);
	}

	public List<?> findByOrgtype(Object orgtype)
	{
		return findByProperty(ORGTYPE, orgtype);
	}

	public List<?> findByProbablecause(Object probablecause)
	{
		return findByProperty(PROBABLECAUSE, probablecause);
	}

	public List<?> findByActivestatus(Object activestatus)
	{
		return findByProperty(ACTIVESTATUS, activestatus);
	}

	public List<?> findByAlarmTitle(Object alarmTitle)
	{
		return findByProperty(ALARM_TITLE, alarmTitle);
	}

	public List<?> findByAlarmText(Object alarmText)
	{
		return findByProperty(ALARM_TEXT, alarmText);
	}
	
	public List<?> findByID(Object id)
	{
		return findByProperty(ID, id);
	}

	public List<?> findAll()
	{
		log.debug("finding all Sysalarm instances");
		try
		{
			String queryString = "from Sysalarm";
			return getHibernateTemplate().find(queryString);
		}
		catch (RuntimeException re)
		{
			log.error("find all failed", re);
			throw re;
		}
	}

	public Sysalarm merge(Sysalarm detachedInstance)
	{
		log.debug("merging Sysalarm instance");
		try
		{
			Sysalarm result = (Sysalarm) getHibernateTemplate()
					.merge(detachedInstance);
			log.debug("merge successful");
			return result;
		}
		catch (RuntimeException re)
		{
			log.error("merge failed", re);
			throw re;
		}
	}

	public void attachDirty(Sysalarm instance)
	{
//		log.debug("attaching dirty Sysalarm instance");
		try
		{
			getHibernateTemplate().saveOrUpdate(instance);
//			log.debug("attach successful");
		}
		catch (RuntimeException e)
		{
			log.error("attach failed", e);
			throw e;
		}
	}

	public void attachClean(Sysalarm instance)
	{
		log.debug("attaching clean Sysalarm instance");
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

	public static SysalarmDAO getFromApplicationContext(ApplicationContext ctx)
	{
		return (SysalarmDAO) ctx.getBean("SysalarmDAO");
	}
}