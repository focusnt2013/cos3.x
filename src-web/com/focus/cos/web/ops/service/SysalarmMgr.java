package com.focus.cos.web.ops.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.json.JSONObject;

import com.focus.cos.api.AlarmType;
import com.focus.cos.api.Sysuser;
import com.focus.cos.api.SysuserClient;
import com.focus.cos.web.common.ZKMgr;
import com.focus.cos.web.common.jdbc.JDBCBaseDAO;
import com.focus.cos.web.common.paginate.PageBean;
import com.focus.cos.web.ops.dao.SysalarmDAO;
import com.focus.cos.web.ops.vo.Sysalarm;

public class SysalarmMgr
{
	private static final Log log = LogFactory.getLog(SysalarmMgr.class);
	private SysalarmDAO sysalarmDao;
	private AlarmSaveMgr alarmSaveMgr;
	private AlarmConfirmMgr alarmConfirmMgr;
	private JDBCBaseDAO jdbcDao;
	
	/**
	 * 
	 * @param zookeeper
	 * @param type
	 * @param cmpid
	 * @return
	 */
	public JSONObject getAlarmConfig(ZooKeeper zookeeper, AlarmType type, String cmpid) throws Exception
	{
		String id = AlarmType.getLabel(type.getValue())+"_"+cmpid;
		JSONObject e = null;
		String path = "/cos/config/alarm/"+id;
		Stat stat = zookeeper.exists(path, false); 
		if( stat != null)
		{
			byte[] payload = zookeeper.getData(path, false, stat);
			String json = new String(payload, "UTF-8");
			e = new JSONObject(json);
			e.put("id", id);
			if( e.has("SUBSCRIBER") ) e.put("SUBSCRIBER.obj", e.getJSONObject("SUBSCRIBER"));
		}
		else
		{
			e = new JSONObject();
			e.put("id", id);
			e.put("ORGTYPE", type.getValue());
			e.put("SVRSYSTEM", cmpid);
			if( "Sys".equalsIgnoreCase(cmpid) )
			{
				JSONObject subscriber = new JSONObject();
				subscriber.put("1", "[系统管理员组]");
				e.put("SUBSCRIBER", subscriber);
				e.put("SUBSCRIBER.obj", e.getJSONObject("SUBSCRIBER"));
			}
		}
		return e;
	}
	/**
	 * 根据指定用户的实时告警的个数
	 * @param user
	 * @param time
	 * @return
	 */
	public int countInstantAlarms(JSONObject user, long time)
	{
		if( user == null ) return 0;
		int count = 0;
		StringBuffer query = new StringBuffer("select count(*) from Sysalarm as model where model.activestatus=? and model.eventtime>?");
		try
		{
			ArrayList<Object> conditions = new ArrayList<Object>();
			conditions.add(-1);
			conditions.add(new Date(time+1));
			if( user.getInt("roleid") == 1 )
			{
				query.append(" and (model.orgtype=? or model.orgtype=? or model.orgtype=?)");
				conditions.add(AlarmType.S.getValue());
				conditions.add(AlarmType.E.getValue());
				conditions.add(AlarmType.D.getValue());
			}
			else
			{
				StringBuffer logs = new StringBuffer();
				ZooKeeper zookeeper = ZKMgr.getZooKeeper();
				loadAlarmConfig(zookeeper, -1, alarmConfig, logs);
//				log.debug(logs.toString());
				Iterator<JSONObject> iterator = alarmConfig.values().iterator();
				while( iterator.hasNext() )
				{
					JSONObject config = iterator.next();
//					if( config.getString("ORGTYPE").equals(AlarmType.S.getValue()) ||
//							config.getString("ORGTYPE").equals(AlarmType.E.getValue()) ||
//							config.getString("ORGTYPE").equals(AlarmType.D.getValue()) )
//							continue;
					if( !config.has("SUBSCRIBER") ) continue;
					JSONObject subscribers = (JSONObject)config.get("SUBSCRIBER");
					if( subscribers != null && subscribers.has(user.getString("username")) )
					{
						conditions.add(config.getString("ORGTYPE"));
					}
				}
				query.append(" and (");
				if( conditions.size() > 2 )
				{
					for( int i = 2; i < conditions.size(); i++ )
					{
						if( i > 2 ) query.append(" or ");
						query.append(" model.orgtype=? ");
					}
					query.append(" or ");
				}
				query.append(" model.responser=? or model.responser=? or model.contact=?");
				conditions.add(user.getString("username"));
				conditions.add(user.getString("realname"));
				conditions.add(user.getString("email"));
				
				ArrayList<Sysuser> children = SysuserClient.listUser(user.getString("username"));
				for(Sysuser child : children){
					query.append(" or model.responser=? or model.responser=? or model.contact=?");
					conditions.add(child.getUsername());
					conditions.add(child.getRealname());
					conditions.add(child.getEmail());
				}
				query.append(")");
			}
//			log.debug("Count the alarms instant from sql "+query+" "+conditions.toString());
			Object values[] = new Object[conditions.size()];
			conditions.toArray(values);
			List<?> list = sysalarmDao.getHibernateTemplate().find(query.toString(), values);
			for (int i = 0; i < list.size(); i++)
			{
				Integer ret = ((Long) list.get(0)).intValue();
				if (ret != null)
				{
					count += ret;
				}
			}
		}
		catch (Exception e)
		{
			log.error("Failed to count alarms from sql "+query+" for exception: ", e);
		}
		return count;
	}
	/**
	 * 得到所有实时告警数据
	 * @param useraccount
	 * @param time
	 * @return
	 */
	private HashMap<String, JSONObject> alarmConfig = new HashMap<String, JSONObject>();
	public List<?> getInstantAlarms(int userRole, String userAccount, String userName, String userEmail, long time)
	{
		List<?> alarms = null;
		StringBuffer query = new StringBuffer("from Sysalarm as model where model.activestatus=? and model.eventtime>?");
		try
		{
			ArrayList<Object> conditions = new ArrayList<Object>();
			conditions.add(-1);
			conditions.add(new Date(time+1));
			if( userRole == 1 )
			{
				query.append(" and (model.orgtype=? or model.orgtype=? or model.orgtype=?)");
				conditions.add(AlarmType.S.getValue());
				conditions.add(AlarmType.E.getValue());
				conditions.add(AlarmType.D.getValue());
			}
			else
			{
				StringBuffer logs = new StringBuffer();
				ZooKeeper zookeeper = ZKMgr.getZooKeeper();
				loadAlarmConfig(zookeeper, -1, alarmConfig, logs);
//				log.debug(logs.toString());
				Iterator<JSONObject> iterator = alarmConfig.values().iterator();
				while( iterator.hasNext() )
				{
					JSONObject config = iterator.next();
//					if( config.getString("ORGTYPE").equals(AlarmType.S.getValue()) ||
//						config.getString("ORGTYPE").equals(AlarmType.E.getValue()) ||
//						config.getString("ORGTYPE").equals(AlarmType.D.getValue()) )
//						continue;
					if( !config.has("SUBSCRIBER") ) continue;
					JSONObject subscribers = (JSONObject)config.get("SUBSCRIBER");
					if( subscribers != null && subscribers.has(userAccount) )
					{
						conditions.add(config.getString("ORGTYPE"));
					}
				}

				query.append(" and (");
				if( conditions.size() > 2 )
				{
					for( int i = 2; i < conditions.size(); i++ )
					{
						if( i > 2 ) query.append(" or ");
						query.append(" model.orgtype=? ");
					}
					query.append(" or ");
				}
				query.append(" model.responser=? or model.responser=? or model.contact=?");
				conditions.add(userAccount);
				conditions.add(userName);
				conditions.add(userEmail);
				
				ArrayList<Sysuser> children = SysuserClient.listUser(userAccount);
				for(Sysuser child : children){
					query.append(" or model.responser=? or model.responser=? or model.contact=?");
					conditions.add(child.getUsername());
					conditions.add(child.getRealname());
					conditions.add(child.getEmail());
				}
				query.append(")");
			}
			query.append(" order by eventtime desc" );
			Object values[] = new Object[conditions.size()];
			conditions.toArray(values);
			alarms = sysalarmDao.getHibernateTemplate().find(query.toString(), values);
			log.debug(query.toString()+"\r\n\t"+(alarms!=null?alarms.size():-1));
		}
		catch (Exception e)
		{
			log.error("Failed to query alarms from sql "+query+" for exception: ", e);
		}
		return alarms;
	}
	
	/**
	 * 清理指定编号的告警
	 * @param ids
	 */
	public void doClear(String[] ids)
	{
		for(String id:ids)
		{
			Sysalarm instance = sysalarmDao.findById(Long.valueOf(id));
			if(instance != null)
			{
				instance.setCleartime(new Date());
				instance.setActivestatus(1);
				sysalarmDao.update(instance);
			}
		}
	}

	public List<?> findAllAlarmDN(int type){
		return sysalarmDao.findAllAlarmDN(type);
	}
	

	/**
	 * 自动确认告警
	 * @param module
	 * @param dn
	 * @param id
	 * @param remark
	 */
	public void doConfirm(String module,String dn,String id, String remark)
	{
		String[] propertyNames = {"activestatus","module","dn","id"};
		Object[] values = {-1,module,dn,id};
		List<?> alarmlist = sysalarmDao.query(propertyNames, values, "desc");
		if(alarmlist != null)
		{
			for(int i = 0;i<alarmlist.size();i++)
			{
				Sysalarm instance = (Sysalarm)alarmlist.get(i);
				instance.setAcktime(new Date());
				instance.setActivestatus(0);
				instance.setAckRemark((remark!=null&&!remark.isEmpty())?remark:"因为故障恢复告警被自动确认！");
				instance.setAckUser("");
//				sysalarmDao.update(instance);
//				addConfirm(instance);
				alarmConfirmMgr.addConfirm(instance);
			}
		}
	}
	
	/**
	 * 
	 * @param status
	 * @param orgSeverity
	 * @param orgType
	 * @param dn
	 * @param id
	 * @return
	 */
	public List<?> query(int status,String orgSeverity, String orgType,String dn,String id)
	{
		String[] propertyNames = {"activestatus","orgseverity", "orgtype","dn","id"};	
		Object[] values = {status,orgSeverity,orgType,dn,id};
		return sysalarmDao.query(propertyNames, values, "desc");
	}
	/**
	 * 确认指定编号的告警
	 * @param ids
	 * @param ackuser
	 * @param ackremark
	 */
	public void confirmAlarm(String ids[],String ackuser,String ackremark)
	{
		for(String id:ids)
		{
			Sysalarm instance = sysalarmDao.findById(Long.valueOf(id));
			if(instance != null)
			{
				instance.setAckUser(ackuser);
				instance.setAckRemark(ackremark);
				instance.setAcktime(new Date());
				instance.setActivestatus(0);
				sysalarmDao.update(instance);
			}
		}
	}
	
	/**
	 * 保持
	 * @param alarm
	 * @return
	 */
	public long save(Sysalarm alarm)
	{
		Object[] values = {
		    alarm.getDn(),
		    alarm.getModule(), 
		    alarm.getOrgseverity(),
		    alarm.getOrgtype(),
		    alarm.getAlarmTitle(),
		    alarm.getId()};
		try
		{
			Sysalarm found = sysalarmDao.found(values);
			// 为了兼容sybase的hibernate不允许字段为空的情况
			if( found == null )
			{
				alarm.setAcktime(new Date(0));
				alarm.setCleartime(new Date(0));
				alarm.setAckUser("");
				alarm.setAckRemark("");
				this.sysalarmDao.save(alarm);
				return alarm.getAlarmid();
			}
			else
			{
				found.setAcktime(new Date(0));
//				found.setCleartime(new Date(0));
				found.setAckRemark("");
				found.setAckUser("");
				found.setActivestatus(-1);
				found.setEventtime(alarm.getEventtime());
				found.setAlarmText(alarm.getAlarmText());
				this.sysalarmDao.attachDirty(found);
				return found.getAlarmid();
			}
		}
		catch (Exception e)
		{
			log.error("Failed to save alarm("+alarm.getAlarmid()+", "+alarm.getAlarmTitle()+")", e);
		}
		return 0;
	}			

	/**
	 * 加载告警配置
	 * @param zookeeper
	 * @param users
	 * @param config1
	 * @param config2
	 * @param logs
	 * @return 配置的版本号
	 * @throws Exception
	 */
	public static int loadAlarmConfig(ZooKeeper zookeeper, int version, HashMap<String, JSONObject> config1, StringBuffer logs) throws Exception
	{
		String path = "/cos/config/alarm";
		Stat stat = zookeeper.exists(path, false); 
		if( stat != null && version != stat.getVersion() )
		{
			List<String> list = zookeeper.getChildren(path, false);
			logs.append("Succeed to setup "+list.size()+" configs from "+path);
			for( String nodepath : list )
			{
				nodepath = path+"/"+nodepath;
				stat = zookeeper.exists(nodepath, false);
				if( stat == null )
				{
					continue;
				}
				JSONObject c = null;
				if( config1 != null )
				{
					c = config1.get(nodepath);
					if( c!= null && c.has("version") && c.getInt("version") == stat.getVersion() )
						continue;
				}
				JSONObject subscribers = null;
				String json = new String(zookeeper.getData(nodepath, false, stat), "UTF-8");
				c = new JSONObject(json);
				if( config1 != null ) config1.put(nodepath, c);
				c.put("version", stat.getVersion());
				c.put("isAdmin", false);
				if( !c.has("SUBSCRIBER") ){
					if( !c.getString("ORGTYPE").equals(AlarmType.S.getValue()) &&
						!c.getString("ORGTYPE").equals(AlarmType.E.getValue()) &&
						!c.getString("ORGTYPE").equals(AlarmType.D.getValue()))
					{
						logs.append("\r\n\t["+c.getString("SVRSYSTEM")+"]["+c.getString("ORGTYPE")+"] discard for not found subscribers.");
						continue;
					}
					c.put("isAdmin", true);
					logs.append("\r\n\t["+c.getString("SVRSYSTEM")+"]["+c.getString("ORGTYPE")+"] subscribers are all system administrators.");
				}
				else
				{
					subscribers = (JSONObject)c.get("SUBSCRIBER");
					if( subscribers == null || subscribers.length() == 0 ) continue;
					logs.append("\r\n\t["+c.getString("SVRSYSTEM")+"]["+c.getString("ORGTYPE")+"] subscribers are "+subscribers.length());
				}
			}
			return stat.getVersion();
		}
		else
		{
			logs.append("Not found config("+stat+") from "+path);
			return stat!=null?stat.getVersion():-1;
		}
	}
	
//	public List<?> queryHistoryAlarm(PageBean pageBean, QueryMeta qMeta) throws Exception
//	{
//		return sysalarmDao.queryHistoryAlarmList(pageBean, qMeta);
//	}

	public String getPageMenu()
	{
		PageBean p = sysalarmDao.getPageBean();
		return sysalarmDao.getPaginate().getPageMenu(p);
	}
	
	public void setSysalarmDao(SysalarmDAO sysalarmDao)
	{
		this.sysalarmDao = sysalarmDao;
	}

	public AlarmSaveMgr getAlarmSaveMgr() {
		return alarmSaveMgr;
	}

	public void setAlarmSaveMgr(AlarmSaveMgr alarmSaveMgr) {
		this.alarmSaveMgr = alarmSaveMgr;
	}

	public AlarmConfirmMgr getAlarmConfirmMgr() {
		return alarmConfirmMgr;
	}

	public void setAlarmConfirmMgr(AlarmConfirmMgr alarmConfirmMgr) {
		this.alarmConfirmMgr = alarmConfirmMgr;
	}

	public SysalarmDAO getSysalarmDao() {
		return sysalarmDao;
	}

	public JDBCBaseDAO getJdbcDao()
	{
		return jdbcDao;
	}

	public void setJdbcDao(JDBCBaseDAO jdbcDao)
	{
		this.jdbcDao = jdbcDao;
	}
}
