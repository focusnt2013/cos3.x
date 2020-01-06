package com.focus.cos.web.common;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.focus.cos.api.LogSeverity;
import com.focus.cos.api.LogType;
import com.focus.cos.api.Status;
import com.focus.cos.api.Syslog;
import com.focus.cos.api.SyslogClient;
import com.focus.cos.api.Sysnotify;
import com.focus.cos.api.SysnotifyClient;
import com.focus.cos.api.Sysuser;
import com.focus.cos.api.SysuserClient;
import com.focus.cos.web.Version;
import com.focus.cos.web.common.AjaxResult;
import com.focus.cos.web.common.COSConfig;
import com.focus.cos.web.common.Kit;
import com.focus.cos.web.common.PathFactory;
import com.focus.cos.wrapper.WrapperUpgrade;
import com.focus.util.HttpUtils;
import com.focus.util.IOHelper;
import com.focus.util.Tools;
import com.mongodb.BasicDBObject;

/**
 * 系统
 * @author focus
 *
 */
public class HelperMgr
{
	private static final Log log = LogFactory.getLog(HelperMgr.class);
	/*为了拒绝连续多次重复下载请求而创建的cookie对象，当下载完成时删除关闭*/
	private HashMap<String, Long> cookie = new HashMap<String, Long>();
	private boolean upgrading;//升级状态
	private String upgradeTips = null;//升级的提示信息。
	/**
	 * 设置SQL的数据模型
	 * @param rs
	 * @param fetchData
	 * @param colM
	 * @param dataM
	 * @param details
	 * @param filterModel
	 * @return
	 * @throws SQLException
	 */
	public JSONArray setModelOfSql(
			ResultSet rs,
			String sqlurl,
			StringBuffer colM, 
			StringBuffer dataM, 
			ArrayList<BasicDBObject> details, 
			BasicDBObject filterModel,
			JSONObject sObject)
		throws SQLException
	{
		ArrayList<String> sorting = new ArrayList<String>();
		//生成表头
        ResultSetMetaData meta = rs.getMetaData();
		colM.append("[");
		JSONObject dataColumns = new JSONObject();
		JSONArray titleColumns = new JSONArray();
        for(int i = 1; i <= meta.getColumnCount(); i++ )
        {
        	if( i > 1 ) colM.append(",");
    		colM.append("\r\n\t");
    		this.setSqlGridColumn(meta, i, colM, sorting, details, filterModel, dataColumns, titleColumns);
//    		if( i == meta.getColumnCount() )
//    			dataColumns.put("scrollModelAutoFit", meta.getColumnType(i) != java.sql.Types.VARCHAR);
        }
		colM.append("\r\n]");

		dataM.append("{");
		dataM.append("\r\n\tdataType: 'JSON',");
		if( !sorting.isEmpty() )
		{
			dataM.append("\r\n\tsortIndx: [");
			for(int i = 0; i < sorting.size(); i++)
			{
				if( i > 0 ) dataM.append(',');
				dataM.append('\'');
				dataM.append(sorting.get(i));
				dataM.append('\'');
			}
			dataM.append("],");
			dataM.append("\r\n\tsortDir: ['down','up',''],");
		}
		if( details.size() > 0 )
    		colM.insert(1, "\r\n\t{title: '', minWidth: 27, width: 27, type: 'detail', resizable: false, editable: false},");
		dataM.append("\r\n\tlocation: 'remote',");
		dataM.append("\r\n\tsorting: 'remote',");
		//dataM.append("\r\n\tsortDir: 'up',");
		dataM.append("\r\n\tcontentType: 'application/json; charset=UTF-8',");
		dataM.append("\r\n\tmethod: 'GET',"); 
		dataM.append("\r\n\turl: '"+sqlurl+"',");
		dataM.append("\r\n\tgetData: function (remoteData) {return callbackAfterReceiveRemoteData(remoteData)}");
		dataM.append("\r\n}");
    	sObject.put("dataColumns", dataColumns);
    	return titleColumns;
	}
	/**
	 * 得到SQL的列
	 * @param meta
	 * @param i
	 * @param colM
	 * @return
	 * @throws SQLException
	 */
	public void setSqlGridData(ResultSet rs, StringBuffer dataM) throws SQLException
	{
        ResultSetMetaData meta = rs.getMetaData();
    	dataM.append("{rank: "+rs.getRow()+"");
        for(int i = 1; i <= meta.getColumnCount(); i++ )
	    	switch( meta.getColumnType(i) )
	    	{
	    		case java.sql.Types.CLOB:
	    		case java.sql.Types.BLOB:
	    			dataM.append(", cell"+i+":");
	    			dataM.append("'展开查看详情', ");
	    			dataM.append(meta.getColumnName(i)+":'"+ rs.getString(i)+"'");
	    			break;
	    		case java.sql.Types.TIMESTAMP:
	    		case java.sql.Types.DATE:
	    		case java.sql.Types.VARCHAR:
	    			dataM.append(", cell"+i+":");
	    			dataM.append("'"+rs.getString(i)+"'");
	    			break;
	    		case java.sql.Types.BOOLEAN:
	    		case java.sql.Types.BIGINT:
	    		case java.sql.Types.CHAR:
	    		case java.sql.Types.DECIMAL:
	    		case java.sql.Types.DOUBLE:
	    		case java.sql.Types.FLOAT:
	    		case java.sql.Types.INTEGER:
	    		case java.sql.Types.NULL:
	    		case java.sql.Types.NUMERIC:
	    			dataM.append(",cell"+i+":");
	    			dataM.append(rs.getString(i));
	    			break;
	    		default:// java.sql.Types.VARCHAR:
	    			dataM.append(", cell"+i+":");
    				dataM.append("'"+rs.getString(i)+"'");
	    			break;
	    	}
    	dataM.append("}");
	}	
	/**
	 * 得到SQL的列
	 * @param meta
	 * @param i
	 * @param colM
	 * @param dataM
	 * @param details
	 * @param filterModel
	 * @return
	 * @throws SQLException
	 */
	public void setSqlGridColumn(
			ResultSetMetaData meta,
			int i,
			StringBuffer colM,
			ArrayList<String> sortIndx,
			ArrayList<BasicDBObject> details, 
			BasicDBObject filterModel,
			JSONObject dataColumns,
			JSONArray titleColumns)
		throws SQLException
	{
    	int type = meta.getColumnType(i);
    	int size = meta.getColumnDisplaySize(i);
    	String title = meta.getColumnLabel(i);
    	title = title.replace("'", "");
    	title = title.replace("\"", "");
    	String dataIndx = meta.getColumnLabel(i);
		if( meta.isAutoIncrement(i) )
		{
			sortIndx.add(dataIndx);
		}
		JSONObject column = new JSONObject();
		column.put("title", title);
		column.put("column", title);
		colM.append("{title: '"+title+"', dataIndx: '"+dataIndx+"'");
//		colM.append(", sqlcolumn: '"+meta.getColumnName(i)+"'");
		colM.append(", sqlsize: "+size);
		colM.append(", sqltypename: '"+meta.getColumnTypeName(i)+"'");
		colM.append(", sqltype: "+type);
		if( meta.isAutoIncrement(i) ) colM.append(", pk: true");
		BasicDBObject detail = null;
    	switch( type )
    	{
    		case java.sql.Types.CLOB:
    		case java.sql.Types.BLOB:
    			colM.append(", width: 100");
    			colM.append(", lob: true");
    			log.debug("Found the lob "+meta.getColumnName(i));
	    		detail = new BasicDBObject();
	    		detail.put("type", 0);
	    		detail.put("subject", title);
	    		detail.put("data", meta.getColumnName(i));
	    		details.add(detail);
    			colM.append(", dataType: 'string'");
    			break;
    		case java.sql.Types.TIMESTAMP:
    		case java.sql.Types.DATE:
    		case java.sql.Types.TIME:
    			column.put("dataType", "time");
    			colM.append(", minWidth: 190");
    			colM.append(", dataType: 'string'");
    			colM.append(", filter: {type: 'textbox', condition: 'between', init: pqDatePicker, listeners: ['change']}");
    			filterModel.put("on", true);
//    			dataM.append("'"+dataIndx+"',");
    			break;
    		case java.sql.Types.BOOLEAN:
    			column.put("dataType", "boolean");
    			colM.append(", minWidth: 30");
    			colM.append(", dataType: 'boolean'");
    			colM.append(", align: 'center'");
    			colM.append(", filter: { type: 'checkbox', subtype: 'triple', condition: 'equal', listeners: ['click'] }");
    			filterModel.put("on", true);
    			break;
    		case java.sql.Types.BIGINT:
    		case java.sql.Types.CHAR:
    		case java.sql.Types.DECIMAL:
    		case java.sql.Types.DOUBLE:
    		case java.sql.Types.FLOAT:
    		case java.sql.Types.INTEGER:
    		case java.sql.Types.NULL:
    		case java.sql.Types.NUMERIC:
    			column.put("dataType", "number");
    			colM.append(", minWidth: 40");
    			colM.append(", maxWidth: 120");
    			colM.append(", dataType: 'number'");
//    			colM.append(", filter: {type: 'textbox', condition: 'between', init: pqDatePicker, listeners: ['change']}");
    			break;
    		case java.sql.Types.VARCHAR:
    			column.put("dataType", "string");
    			filterModel.put("on", true);
    			if( size <= 64 ) colM.append(", width: 100, maxWidth: 128");
    			else if( size > 200 )
		    	{
        			colM.append(", width: 200");
        			colM.append(", minWidth: 200");
    				colM.append(", maxWidth: "+size);
		    	}
    			else colM.append(", width: 160, minWidth: 160");
//    			dataM.append("'"+dataIndx+"',");
    			colM.append(", dataType: 'string'");
//    			colM.append(", filter: { type: 'textbox', condition: 'begin', listeners: [{'change':filtersql}]}");
    			colM.append(", filter: { type: 'textbox', condition: 'contain', listeners: ['change']}");
    			if( size == 2048 )
    			{
        			log.debug("Found the lob "+meta.getColumnName(i));
    	    		detail = new BasicDBObject();
    	    		detail.put("type", 0);
    	    		detail.put("subject", title);
    	    		detail.put("data", meta.getColumnName(i));
    	    		details.add(detail);
    			}
    			break;
    		default:// java.sql.Types.VARCHAR:
    			column.put("dataType", "string");
    			colM.append(", dataType: 'string'");
//				dataM.append("'"+dataIndx+"',");
    			//colM.append(", filter: { type: 'textbox', condition: 'begin', cls: filtersql, listeners: [{'change':filtersql}]}");
    			break;
    	}
    	colM.append("}");
		dataColumns.put(dataIndx, column);
		if( column.has("dataType") )
		{
			column.put("dataIndx", dataIndx);
			titleColumns.put(column);
		}
	}

	/**
	 * 升级COS.WEB软件版本
	 * @return 返回新版本号
	 */
	private File upgradeTempDir = new File(PathFactory.getAppPath(), "temp/upgrade/web");
	private WrapperUpgrade wrapper = null;
	public String doUpgradeDownload()
	{
		if( upgrading ) return upgradeTips;
		/**
		 * 以当前版本作为基线进行升级，每次打包cos.ide的时候都对该版本号做修改。
		 */
		Thread thread = new Thread()
		{
			public void run()
			{
				try
				{
					upgrading = true;
					wrapper = new WrapperUpgrade(PathFactory.getAppPath(), PathFactory.getWebappPath()){
						public void notifyDownloadResult(File upgradedir, boolean succeed, boolean newvresion, String version, Exception e, boolean needReboot, String release, String logcontext)
						{
							setUpgradeDownloadResult(succeed, newvresion, version, e, needReboot, release, logcontext);
						}
						@Override
						public void notifyDownloadProgress(int arg0) {
							// TODO Auto-generated method stub
							
						}
					};
					if( !upgradeTempDir.exists() ) upgradeTempDir.mkdirs();
					wrapper.download("web", Version.getValue(), upgradeTempDir, true);
				}
				catch(Exception e)
				{
					log.error("Failed to upgrade for exception:", e);
					upgrading = false;
				}
			}
		};
		thread.start();
		upgradeTips = "正在下载升级文件，请耐心等待……";
		return upgradeTips;
	}
	/**
	 * 升级COS.WEB软件版本
	 * @return 返回新版本号
	 */
	public String setAutoUpgrade(boolean yes)
	{
		File upgradeDir = new File(PathFactory.getWebappPath(), "upgrade.aot");
		if( yes )
		{
			upgradeDir.delete();
		}
		else if( !upgradeDir.exists() )
		{
			try {
				upgradeDir.createNewFile();
			} catch (IOException e) {
			}
		}
		return Version.getVersionUpgradeInfo();
		
	}
	/**
	 * 升级结果
	 * @return
	 */
	public synchronized AjaxResult<Integer> getUpgradeDownloadResult()
	{
		AjaxResult<Integer> result = new AjaxResult<Integer>();
		result.setSucceed(upgrading);
		result.setResult(this.wrapper!=null?wrapper.getUpgradeProgress():0);
		result.setMessage(upgrading?this.wrapper.getUpgradeTips():upgradeTips);
		String tag = PathFactory.getWebappPath().getAbsolutePath().toLowerCase();
		if( tag.startsWith("d:\\focusnt\\cos\\trunk\\ide"))
		{
			log.info("Response the result of upgrade, upgrading is "+upgrading+", progress is "+this.wrapper.getUpgradeProgress()+", upgradeTips is "+upgradeTips);
		}
		return result;
	}
	
	/**
	 * 设置升级结果
	 * @param succeed
	 * @param newvresion
	 * @param version
	 * @param e
	 */
	private synchronized void setUpgradeDownloadResult(
			boolean succeed,
			boolean newvresion,
			String version,
			Exception e,
			boolean needReboot,
			String release,
			String logcontext)
	{
		String tag = PathFactory.getWebappPath().getAbsolutePath().toLowerCase();
		if( tag.startsWith("d:\\focusnt\\cos\\trunk\\ide"))
		{
			log.info("Receive the result of upgrade, succeed is "+succeed+", newvresion is "+newvresion+", version is "+version+", exception is "+e);
		}
		upgrading = false;
		Syslog syslog = new Syslog();
		syslog.setCategory("COSPortal");
		syslog.setAccount(Version.getCOSSecurityKey());
		syslog.setLogseverity(LogSeverity.INFO.getValue());
		syslog.setLogtype(LogType.运行日志.getValue());
		syslog.setContext(logcontext);
		if( succeed )
		{
//			File file = new File(upgradeTempDir, "version.txt");
			if( newvresion )
			{
//				try {
//					IOHelper.writeFile(file, (version+"\r\n"+release).getBytes("UTF-8"));
//				} catch (UnsupportedEncodingException e1) {
//				}
//				reboot = needReboot;//只有在成功升级，同时版本有更新的情况设为true
				upgradeTips = "成功下载了【主界面框架系统】的新版本["+version+"]。版本信息如下："+release;
				ArrayList<Sysuser> users = SysuserClient.listUser(1, -1, Status.Enable.getValue());
				Sysnotify notify = new Sysnotify();
				notify.setFilter("系统升级");
				notify.setTitle("成功下载了【主界面框架系统】的新版本["+version+"]请点击【立刻升级】");
				notify.setNotifytime(new Date());
				notify.setPriority(0);
				notify.setContext(release);
				notify.setAction("立刻升级");
				notify.setActionlink("helper!upgrade.action");
				for(Sysuser u : users )
				{
					notify.setUseraccount(u.getUsername());
					SysnotifyClient.submit(notify);
				}
				syslog.setLogtext("成功下载了【主界面框架系统】的新版本["+version+"]需系统管理员确认升级，当前版本是["+Version.getValue()+"]。");
				SyslogClient.submit(syslog);
			}
			else
			{
				upgradeTips = "【主界面框架系统】当前版本["+version+"]就是最新版本。版本信息如下："+release;
				File[] files = upgradeTempDir.listFiles();
				if( files.length > 0 )
				{
					upgradeTips = "【主界面框架系统】当前版本["+version+"]。版本信息如下："+release;
					this.wrapper.setUpgradeProgress(100);
				}
			}
		}
		else {
			upgradeTips = "升级失败。";
			if( e != null )
			{
				log.error("Failed to upgrade for exception:", e);
				upgradeTips += "原因是"+e.getMessage();
				syslog.setLogtext("【主界面框架系统】版本["+Version.getValue()+"]升级出现异常"+e.getMessage());
				ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
				PrintStream ps = new PrintStream(out);
				e.printStackTrace(ps);
				syslog.setContext(out.toString());
				SyslogClient.submit(syslog);
			}
		}
	}
	
	/**
	 * 加载升级文件
	 * @param dir
	 * @param list
	 */
	public void loadUpgradeFiles(File dir, ArrayList<String> upgrades, int substring, HashMap<String, Boolean> filter)
	{
		boolean exclude = false;
		if( dir == upgradeTempDir ) exclude = true;
		File[] files = dir.listFiles();
		if( files == null ) return;
		ArrayList<File> dirs = new ArrayList<File>();
		for(File file : files)
		{
			if( file.isDirectory() )
			{
				dirs.add(file);
				continue;
			}
			if( exclude && file.getName().endsWith(".json") ) continue;
			String path = file.getAbsolutePath();
			path = path.substring(substring);
			upgrades.add(path);
			filter.put(path, true);
		}
		for(File subdir : dirs)
		{
			String path = subdir.getAbsolutePath();
			path = path.substring(substring)+"/";
			upgrades.add(path);
			loadUpgradeFiles(subdir, upgrades, substring, filter);
		}
		dirs.clear();
	}
	/**
	 * 正式执行升级
	 * @return
	 */
	public synchronized AjaxResult<String> doUpgrade()
	{
		AjaxResult<String> result = new AjaxResult<String>();
		result.setMessage("升级中...");
		result.setSucceed(upgrading);
		if( upgrading ) return result;
		try
		{
			log.info("Upgrade from "+upgradeTempDir.getAbsolutePath());
			File versionfile = new File(upgradeTempDir, "version.txt");
			if( upgradeTempDir.exists() )
			{
				String version = IOHelper.readFirstLine(versionfile);
				String path = upgradeTempDir.getAbsolutePath();
				int substring = path.endsWith("/")?path.length():path.length()+1;
				ArrayList<String> updates = new ArrayList<String>();
				HashMap<String, Boolean> filter = new HashMap<String, Boolean>();
				this.loadUpgradeFiles(upgradeTempDir, updates, substring, filter);
				
				File[] files = upgradeTempDir.listFiles(new FilenameFilter() {
					@Override
					public boolean accept(File dir, String name) {
						return name.endsWith(".json");
					}
				});
		
				ArrayList<String> deletes = new ArrayList<String>();
				for(File jsonFile : files)
				{
					JSONObject json = new JSONObject(new String(IOHelper.readAsByteArray(jsonFile)));
					if( json.has("Upgrade") )
					{
						JSONArray array = json.getJSONArray("Upgrade");
						for(int i = 0; i < array.length(); i++)
						{
							JSONObject f = array.getJSONObject(i);
							path = f.getString("path");
							if( f.has("action") && 
								f.getString("action").equals("d") &&
								!filter.containsKey(path) )
							{
								deletes.add(path);
							}
						}
					}
				}
				upgrading = true;
				upgradeTips = "升级版本["+version+"], 共"+updates.size()+"文件需要更新, 共"+deletes.size()+"文件需要删除";
				Upgrader thread = new Upgrader(version, updates, deletes);
				thread.start();
			}
			else
			{
				upgradeTips = "升级文件已经被删除，不能继续执行升级";
			}
			result.setSucceed(upgrading);
			result.setMessage(upgradeTips);
		}
		catch(Exception e)
		{
			result.setMessage("执行升级出现异常"+e.getMessage());
			log.error("Failed to upgrade from "+upgradeTempDir.getAbsolutePath(), e);
		}
		return result;
	}
	
	/**
	 * 执行升级
	 * @author focus
	 *
	 */
	class Upgrader extends Thread
	{
		private String version;
		private ArrayList<String> updates;
		private ArrayList<String> deletes;
		
		public Upgrader(String version, ArrayList<String> updates, ArrayList<String> deletes)
		{
			this.version = version;
			this.updates = updates;
			this.deletes = deletes;
		}

		public void run()
		{
			upgradeprogress = 0;
			upgradeTips = "";
			boolean needReboot = false;
			try
			{
				File upgradepath = PathFactory.getWebappPath();
				String oldversion = "unknown";
				File oldversionFile = new File(upgradepath, "version.txt");
				if( oldversionFile.exists() )
					oldversion = IOHelper.readFirstLine(oldversionFile);
				int size = deletes.size() + updates.size();
				int count = 0;
				StringBuffer logcontext = new StringBuffer();
				for(String path : updates)
				{

        			if( path.endsWith(".class") || path.endsWith(".xml") || path.endsWith(".jar") ) needReboot = true;
        			
					File fileSrc = new File(upgradeTempDir, path);
					File fileTarget = new File(upgradepath, path);
					if( fileTarget.exists() )
					{
						if( fileTarget.isFile() )
						{
//							fileTarget.delete();
							long lm = fileSrc.lastModified();
							FileUtils.copyFile(fileSrc, fileTarget);
//							boolean b = fileSrc.renameTo(fileTarget);
							fileTarget.setLastModified(lm);
							upgradeTips = "覆盖文件"+path;
							logcontext.append("\r\n"+upgradeTips+",文件更新于"+Tools.getFormatTime("yy-MM-dd HH:mm:ss", fileTarget.lastModified())+",大小"+fileTarget.length());
						}
					}
					else
					{
						if( path.endsWith("/") )
						{
							fileTarget.mkdirs();
							upgradeTips = "创建目录"+path;
							logcontext.append("\r\n"+upgradeTips);
						}
						else
						{
							long lm = fileSrc.lastModified();
//							fileSrc.renameTo(fileTarget);
							FileUtils.copyFile(fileSrc, fileTarget);
							fileTarget.setLastModified(lm);
							upgradeTips = "新增文件"+path;
							logcontext.append("\r\n"+upgradeTips+",文件更新于"+Tools.getFormatTime("yy-MM-dd HH:mm:ss", fileTarget.lastModified())+",大小"+fileTarget.length());
						}
					}
					count += 1;
					upgradeprogress = count*100/size;
				}
				Thread.sleep(100);
	
				for(String path : deletes)
				{
					File fileTarget = new File(upgradepath, path);
					int dc = IOHelper.deleteFile(fileTarget);
					upgradeTips = "删除文件"+path;
					logcontext.append("\r\n"+upgradeTips+", 共"+dc+"文件");
					count += 1;
					upgradeprogress = count*100/size;
//					Thread.sleep(50);
				}
//				File file = new File(upgradeTempDir, "version.txt");
//				File fileVersion = new File(PathFactory.getWebappPath(), "version.txt");
//				fileVersion.delete();
//				file.renameTo(fileVersion);

				upgradeTips = "删除升级临时目...";
				FileUtils.deleteDirectory(upgradeTempDir);
//				IOHelper.deleteFile(upgradeTempDir);
				ArrayList<Sysuser> users = SysuserClient.listUser(1, -1, Status.Enable.getValue());
				Sysnotify notify = new Sysnotify();
				notify.setFilter("系统升级");
				upgradeTips = "成功将【主界面框架系统】从["+oldversion+"]升级到新版本["+version+"]";
				notify.setTitle(upgradeTips);
				notify.setNotifytime(new Date());
				notify.setPriority(0);
				String dataurl = "control!costimeline.action?id=COSPortal&ip=127.0.0.1&port="+COSConfig.getLocalControlPort();
	    		notify.setContextlink("helper!timeline.action?dataurl="+Kit.chr2Unicode(dataurl));
				for(Sysuser u : users )
				{
					notify.setUseraccount(u.getUsername());
					SysnotifyClient.send(notify);
				}
	
				Syslog syslog = new Syslog();
				syslog.setCategory("COSPortal");
				syslog.setAccount(Version.getCOSSecurityKey());
				syslog.setLogseverity(LogSeverity.INFO.getValue());
				syslog.setLogtype(LogType.运行日志.getValue());
				syslog.setContext(logcontext.toString());
				syslog.setLogtext(notify.getTitle());
				SyslogClient.write(syslog);
				upgradeprogress = 100;
				if( needReboot ) upgradeprogress = 101;
			}
			catch(Exception e)
			{
				upgradeTips = "将【主界面框架系统】从["+Version.getValue()+"]升级到新版本["+version+"]出现异常"+e.getMessage();
			}
			upgrading = false;
		}
	}

	/**
	 * 正式执行升级
	 * @return
	 */
	private int upgradeprogress = 0;//升级进度
	public synchronized AjaxResult<Integer> getUpgradeResult()
	{
		AjaxResult<Integer> result = new AjaxResult<Integer>();
		result.setSucceed(upgrading);
		result.setResult(upgradeprogress);
		result.setMessage(upgradeTips);
		String tag = PathFactory.getWebappPath().getAbsolutePath().toLowerCase();
		if( tag.startsWith("d:\\focusnt\\cos\\trunk\\ide"))
		{
			log.info("Response the result of upgrade, upgrading is "+upgrading+", upgradeprogress is "+upgradeprogress+", upgradeTips is "+upgradeTips);
		}
		return result;
	}
	
	/**
	 * 升级重启操作
	 * @return
	 */
	public String doUpgradeRetartup()
	{
		Thread thread = new Thread()
		{
			public void run()
			{
				try
				{
					sleep(3000);
				}
				catch (InterruptedException e)
				{
				}
				System.exit(0);//停止程序运行并重启，这回触发一些程序关闭
			}
		};
		thread.start();
		return "系统将在三秒后关闭";
	}
	/**
	 * 清除Cookie
	 * @param id
	 */
	public synchronized void clearCookie(String id)
	{
		cookie.remove(id);
	}

	/**
	 * 关闭
	 */
	public void close()
	{
		cookie.clear();
	}

	/**
	 * 设置会话
	 * @param id
	 * @param req
	 */
	public synchronized boolean setCookie(String id)
	{
		if( cookie.containsKey(id) )
		{
			if( System.currentTimeMillis() - cookie.get(id) > 1000*7 )
			{
				cookie.put(id, System.currentTimeMillis());
				return true;
			}
			return false;
		}
		cookie.put(id, System.currentTimeMillis());
		return true;
	}


	/**
	 * 获得动态墙纸"imageUrl"
	 */
	private static ArrayList<String> ArryWallpaper = new ArrayList<String>();
	private static void loadWallpaper()
	{
		synchronized( ArryWallpaper )
		{
			if( !ArryWallpaper.isEmpty() )
			{
				return;
			}
			Thread thread = new Thread()
			{
				public void run()
				{
					final Random random = new Random();
					String html = (random.nextInt(354)+1)+".html";
					String link = "http://desk.zol.com.cn/nb/"+html;
					//"http://www.win4000.com/wallpaper_detail_92057.html";
					//"http://image.baidu.com/data/imgs?pn=0&rn=30&col=%E5%A3%81%E7%BA%B8&tag=%E5%85%A8%E9%83%A8&tag3=&width=1600&height=900&ic=0&ie=utf8&oe=utf-8&image_id=&fr=channel&p=channel&from=1&app=img.browse.channel.wallpaper&t=0.8941218816879991";
					Document doc;
					try
					{
						StringBuffer sb = new StringBuffer();
						sb.append("loadWallpaper");
						if( ArryWallpaper.isEmpty() )
						{
							doc = HttpUtils.crwal(link);
							Elements ul = doc.getElementsByClass("photo-list-padding");
							for(Element e : ul )
							{
								Element img = HttpUtils.getElementByTag(e, "img");
								if( img != null )
								{
									String src = img.attr("src");
									if( src.endsWith(".jpg") && src.indexOf("208x130") != -1 )
									{
										src = src.replaceAll("208x130", "1600x900");
										ArryWallpaper.add(src);
										sb.append("\r\n\t"+src);
									}
								}
							}
						}
						log.debug(sb.toString());
					}
					catch (Exception e)
					{
						log.error("Failed to load the images of wallpaper from "+link+" for exception "+e);
					}
				}
			};
			thread.start();
		}
	}
	/**
	 * 得到图片墙纸
	 * @return
	 */
	public static String getImgWallpaper()
	{
		String imgWallpaper = "images/home.jpg";
		try
		{
			synchronized( ArryWallpaper )
			{
				if( !ArryWallpaper.isEmpty() )
				{
					Random r = new Random();
					int i = r.nextInt(ArryWallpaper.size());
					imgWallpaper = ArryWallpaper.get(i);
					ArryWallpaper.remove(i);
				}
				else
				{
					loadWallpaper();
				}
			}
		}
		catch (Exception e)
		{
			log.error("Failed to load the images of wallpaper.", e);
		}
		return imgWallpaper;
	}
}
