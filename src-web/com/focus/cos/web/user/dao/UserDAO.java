package com.focus.cos.web.user.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.LockMode;
import org.hibernate.Session;
import org.springframework.context.ApplicationContext;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import com.focus.cos.web.common.paginate.PageBean;
import com.focus.cos.web.common.paginate.Paginate;
import com.focus.cos.web.user.vo.User;
import com.focus.cos.web.util.Tools;

public class UserDAO extends HibernateDaoSupport
{
	private static final Log log = LogFactory.getLog(UserDAO.class);
	// property constants
	public static final String ROLEID = "role";
	public static final String USERNAME = "username";
	public static final String PASSWORD = "password";
	public static final String REALNAME = "realname";
	public static final String CORP = "CORP";
	public static final String DEPT = "DEPT";
	public static final String DUTY = "DUTY";
	public static final String PHONE = "phone";
	public static final String EMAIL = "email";
	public static final String STATUS = "status";
	public static final String MEMO = "memo";
	public static final String SEX = "sex";
	public static final String MOBILE = "mobile";
	public static final String FAX = "fax";
	public static final String ADDRESS = "address";
	public static final String POST = "post";
	public static final String SERVICECODE = "serviceCode";
	public static final String ACCESSCMP = "accessCmp";
	private String url;
	private String username;
	private String password;
	private Paginate paginate;
	// 分页实体
	private PageBean pageBean;

	protected void initDao()
	{
	}

	// 获得jdbc连接 验证单点登录
	public boolean checkLogin(String pwd, String workId, String dmkxdev)
	{
		Connection connection = null;
		ResultSet rs = null;
		// Object[] result = null;
		String sql = "sp_user_pwd ?,?,?,?,?,? ";
		try
		{
			// ConnectionProvider cp =
			// ConnectionProviderFactory.newConnectionProvider();
			// connection = cp.getConnection();//获得jdbc连接
			connection = DriverManager.getConnection(url, username, password);
			CallableStatement callableStatement = connection.prepareCall(sql);

			callableStatement.setString(1, pwd);
			callableStatement.setString(2, "");
			callableStatement.setString(3, dmkxdev);
			callableStatement.setString(4, "");
			callableStatement.setString(5, workId);
			callableStatement.setInt(6, 1);

			// callableStatement.execute();
			rs = callableStatement.executeQuery();
			if (rs != null)
			{
				if (rs.next())
				{
					int flag = rs.getInt(1);
					if (flag == 1)
					{
						return true;
					}
				}

			}

			return false;
		}
		catch (Exception e)
		{
			// e.printStackTrace();
			return false;
		}
		finally
		{
			if (rs != null)
				try
				{
					rs.close();
				}
				catch (SQLException e)
				{
				}
				finally
				{
					try
					{
						connection.close();
					}
					catch (SQLException e)
					{
						e.printStackTrace();
					}
				}
		}
	}

	/**
	 * 根据workid查询user对象
	 * 
	 * @param workId
	 * @return
	public User findUserByWorkId(String workId)
	{
		String sql = "select * from TB_USER where workid='" + workId + "'";
		Connection conn = null;
		Statement statement = null;

		try
		{
			conn = DriverManager.getConnection(url, username, password);
			statement = conn.createStatement();
			ResultSet resultSet = statement.executeQuery(sql);
			if (resultSet.next())
			{
				User user = new User();

			}

		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}

		return null;
	}
	 */

	public ArrayList<User> listUser(int roleid, int status, int sex)
	{
		ArrayList<User> users = new ArrayList<User>();
		StringBuffer sql = new StringBuffer();
		sql.append("select * from TB_USER ");
		sql.append(" where ");
		sql.append("status="+status);
		if( roleid != -1 ) sql.append(" and roleid="+roleid);
		if( sex != -1 ) sql.append(" and sex="+sex);
		Connection conn = null;
		Statement statement = null;

		try
		{
			conn = DriverManager.getConnection(url, username, password);
			statement = conn.createStatement();
			ResultSet resultSet = statement.executeQuery(sql.toString());
			User user = null;
			while (resultSet.next())
			{
				user = new User();
				user.setRoleid(Byte.parseByte(resultSet.getString("roleid")));
				user.setUsername(resultSet.getString("username"));
//				user.setPassword(resultSet.getString("password"));
				user.setRealname(resultSet.getString("realname"));
				user.setEmail(resultSet.getString("email"));
				user.setStatus(Byte.parseByte(resultSet.getString("status")));
				user.setSex(Byte.parseByte(resultSet.getString("sex")));
				if( user.getUsername().equals("admin") ) continue;
				users.add(user);
			}
			statement.close();
		}
		catch (SQLException e)
		{
		}
		finally {
			if( conn != null )
				try {
					conn.close();
				} catch (SQLException e) {
				}
		}

		return users;
	}

	public User findByAccount(String account)
		throws Exception
	{
		if( account == null || account.isEmpty() ){
			return null;
		}
		List<?> result = this.findByUsername(account);
		if (result != null && !result.isEmpty())
		{
			return (User) result.get(0);
		}
		return null;
	}

	public void save(User transientInstance)
	{
		log.debug("saving User instance");
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

	public void delete(User persistentInstance)
	{
		log.debug("deleting User instance");
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

	public User findById(java.lang.Integer id)
	{
		log.debug("getting User instance with id: " + id);
		try
		{
			User instance = (User) getHibernateTemplate().get("com.focus.cos.web.user.vo.User", id);
			return instance;
		}
		catch (RuntimeException re)
		{
			log.error("get failed", re);
			throw re;
		}
	}

	public List<?> findByExample(User instance)
	{
		log.debug("finding User instance by example");
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

	// add 2011.1.5 by pingxl
	public List<?> findAllUsers()
	{
		log.debug("finding User instance by example");
		try
		{
			List<?> results = getHibernateTemplate().find("from User");
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
		log.debug("finding User instance with property: " + propertyName + ", value: " + value);
		try
		{
			String queryString = "from User as model where model." + propertyName + "= ?";
			return getHibernateTemplate().find(queryString, value);
		}
		catch (RuntimeException re)
		{
			log.error("find by property name failed", re);
			throw re;
		}
	}

	/**
	 * 根据用户名和密码查询用户
	 * 
	 * @param userName
	 * @param password
	 * @return List
	 */
	public User findUser(String userName, String password)
	{
		String hql = "FROM User u WHERE u.username='" + Tools.replaceStr(userName) + "' AND u.password='" + password + "'";
		List<?> result = getHibernateTemplate().find(hql);
		if (result != null && !result.isEmpty())
		{
			return (User) result.get(0);
		}
		return null;
	}

	/**
	 * 修改用户密码
	 * 
	 * @param userId
	 * @param pwd
	 * @return
	 */
	public boolean modifyPwd(Integer userId, String pwd)
	{
		Session session = this.getSession();
		int c = session.createQuery("UPDATE User set password = '" + pwd + "', LASTCHANGEPASSWORD='"+Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", System.currentTimeMillis())+"' WHERE id = " + userId).executeUpdate();
		releaseSession(session);
		return c > 0;
	}
	
	/**
	 * 禁用指定角色的所有用户
	 * @param userId
	 * @param roleid
	 * @return
	 */
	public boolean disableRole(int roleid)
	{
		Session session = this.getSession();
		int c = session.createQuery("UPDATE User set status = 2 WHERE roleid = " + roleid).executeUpdate();
		releaseSession(session);
		return c > 0;
	}

	public boolean disable(String userName, Byte status)
	{
		Session session = this.getSession();
		int c = session.createQuery("UPDATE User set status = " + status + " WHERE userName = '" + userName + "'").executeUpdate();
		releaseSession(session);

		return c > 0;
	}

	/**
	 * 查询用户列表分页
	 * 
	 * @param pageBean
	 * @param keyword
	 * @return List
	 * @throws Exception
	 */
	public List<?> queryUserList(PageBean pageBean, String keyword,String other) throws Exception
	{
		StringBuffer sql = new StringBuffer("SELECT user FROM User user WHERE user.id > 1");
		StringBuffer sqlc = new StringBuffer("SELECT COUNT(*) FROM User user WHERE user.id > 1");
		StringBuffer cond = new StringBuffer();
		if (keyword != null && !keyword.equals("") && !keyword.equals("请输入关键字"))
		{
			cond.append(" AND (user.username Like '%" + Tools.replaceStr(keyword.trim()) + "%'");
			cond.append(" or user.realname Like '%" + Tools.replaceStr(keyword.trim()) + "%')");
		}
		if(other!=null && other.trim().length()>0){
			cond.append(other);
		}
		sql.append(cond);
		sqlc.append(cond);

		pageBean.setListSQL(sql.toString());
		pageBean.setTotalCountSQL(sqlc.toString());
		pageBean.setCount(this.getPaginate().getTotalCount(pageBean));
		this.setPageBean(pageBean);
		return this.getPaginate().getList(pageBean);
	}

	/**
	 * 删除用户
	 * 
	 * @param ids
	 */
	public void deleteByIds(String ids)
	{
		String sql = "delete User where id in (" + ids + ")";
		Session session = this.getSession();
		int results = session.createQuery(sql).executeUpdate();
		log.debug("删除了" + results + "个用户");
		releaseSession(session);
	}

	
	/**
	 * 查询所有的部门
	 * @return
	 */
	@SuppressWarnings("unchecked") 
	public List<String> queryAllDepts(){
		String hql = "select distinct dept from User u";
		Session session = null;
		try{
			session = this.getSession();
			List<String> depts = session.createQuery(hql).list();
			return  depts;
		}catch (Exception e) {
			return new ArrayList<String>();
		}finally{
			if(session != null){
				releaseSession(session);
			}
		}
	}
	
	/**
	 * 查询所有的地区
	 * @return
	 */
	@SuppressWarnings("unchecked") 
	public List<String> queryAllAddresses(){
		String hql = "select distinct serviceCode from User u";
		Session session = null;
		try{
			session = this.getSession();
			List<String> serviceCodes = session.createQuery(hql).list();
			return  serviceCodes;
		}catch (Exception e) {
			return new ArrayList<String>();
		}finally{
			if(session != null){
				releaseSession(session);
			}
		}
	}
	
	public void update(User transientInstance)
	{
		log.debug("updating User instance");
		try
		{
			getHibernateTemplate().update(transientInstance);
			log.debug("update successful");
		}
		catch (RuntimeException re)
		{
			log.error("update failed", re);
			throw re;
		}
	}

	public List<?> findByRole(Object roleid)
	{
		return findByProperty(ROLEID, roleid);
	}

	private List<?> findByUsername(Object username)
	{
		return findByProperty(USERNAME, username);
	}

	public List<?> findByPassword(Object password)
	{
		return findByProperty(PASSWORD, password);
	}

	public List<?> findByRealname(Object realname)
	{
		return findByProperty(REALNAME, realname);
	}

	public List<?> findByPhone(Object phone)
	{
		return findByProperty(PHONE, phone);
	}

	public List<?> findByEmail(Object email)
	{
		return findByProperty(EMAIL, email);
	}

	public List<?> findByStatus(Object status)
	{
		return findByProperty(STATUS, status);
	}

	public List<?> findByMemo(Object memo)
	{
		return findByProperty(MEMO, memo);
	}

	public List<?> findBySex(Object sex)
	{
		return findByProperty(SEX, sex);
	}

	public List<?> findByMobile(Object mobile)
	{
		return findByProperty(MOBILE, mobile);
	}

	public List<?> findByFax(Object fax)
	{
		return findByProperty(FAX, fax);
	}

	public List<?> findByAddress(Object address)
	{
		return findByProperty(ADDRESS, address);
	}

	public List<?> findByPost(Object post)
	{
		return findByProperty(POST, post);
	}

	public List<?> findByServiceCode(Object serviceCode)
	{
		return findByProperty(SERVICECODE, serviceCode);
	}

	public List<?> findAll()
	{
		log.debug("finding all User instances");
		try
		{
			String queryString = "from User";
			return getHibernateTemplate().find(queryString);
		}
		catch (RuntimeException re)
		{
			log.error("find all failed", re);
			throw re;
		}
	}

	public User merge(User detachedInstance)
	{
		log.debug("merging User instance");
		try
		{
			User result = (User) getHibernateTemplate().merge(detachedInstance);
			log.debug("merge successful");
			return result;
		}
		catch (RuntimeException re)
		{
			log.error("merge failed", re);
			throw re;
		}
	}

	public void attachDirty(User instance)
	{
		log.debug("attaching dirty User instance");
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

	public void attachClean(User instance)
	{
		log.debug("attaching clean User instance");
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

	public String getUrl()
	{
		return url;
	}

	public void setUrl(String url)
	{
		this.url = url;
	}

	public String getUsername()
	{
		return username;
	}

	public void setUsername(String username)
	{
		this.username = username;
	}

	public String getPassword()
	{
		return password;
	}

	public void setPassword(String password)
	{
		this.password = password;
	}

	public static UserDAO getFromApplicationContext(ApplicationContext ctx)
	{
		return (UserDAO) ctx.getBean("UserDAO");
	}
}