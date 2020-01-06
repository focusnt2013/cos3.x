package com.focus.cos.web.dev.action;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.List;
import java.util.Random;
import java.util.zip.CRC32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.json.JSONArray;
import org.json.JSONObject;

import com.focus.cos.web.common.HelperMgr;
import com.focus.cos.web.common.Kit;
import com.focus.cos.web.common.PathFactory;
import com.focus.cos.web.common.ZKMgr;
import com.focus.cos.web.dev.service.MenusMgr;
import com.focus.cos.web.dev.service.ModulesMgr;
import com.focus.cos.web.dev.service.ProgramMgr;
import com.focus.cos.web.ops.service.ControlMgr;
import com.focus.cos.web.user.service.UserMgr;
import com.focus.util.IOHelper;
import com.focus.util.Tools;
import com.focus.util.Zookeeper;

/**
 * 开发者管理
 * @author focus
 *
 */
public class ModulesAction extends DevAction 
{
	private static final long serialVersionUID = -3079427411469239198L;
	private static final Log log = LogFactory.getLog(ModulesAction.class);

	/**
	 * 导入元数据查询配置模板
	 * @return
	 */
	public String importconfig()
	{
		JSONObject rsp = new JSONObject();
		if( this.uploadfile == null || !uploadfile.exists() )
		{
			rsp.put("alt", "导入子系统配置包失败，因为未能收到文件包");
			return response(super.getResponse(), rsp.toString());
		}
		log.info("Import the config of "+sysid+"("+uploadfile.length()+") from "+uploadfile);
		ZipFile zip = null;
        StringBuilder sb = new StringBuilder();
        try
        {
        	Zookeeper zookeeper = ZKMgr.getZookeeper();
	        zip = new ZipFile(uploadfile);
	        Enumeration<?> entries = zip.entries();
	        while(entries.hasMoreElements())
	        {
	            ZipEntry entry = (ZipEntry)entries.nextElement();
	            path = entry.getName();
            	byte[] payload = IOHelper.readAsByteArray(zip.getInputStream(entry));
	            if(path.startsWith("_")){
	            	path = path.substring(1);
	            	payload = null;
	            }
	            path = Kit.unicode2Chr(path);
            	sb.append("\r\n\t"+path);
            	sb.append("\t"+(payload!=null?payload.length:-1));
            	Stat stat = zookeeper.exists(path);
            	if( stat != null ){
            		sb.append("\trecoved.");
            		stat = zookeeper.setData(path, payload, stat.getVersion());
            	}
            	else{
            		sb.append("\tcreated.");
            		zookeeper.create(path, payload);
            	}
	        }
			rsp.put("alt", "导入子系统配置成功。");
			rsp.put("succeed", true);
			log.info(sb.toString());
        }
        catch(Exception e)
        {
        	log.error(sb.toString(), e);
			rsp.put("alt", "导入子系统配置失败，因为出现异常"+e.getMessage());
        }
		finally
		{
			if( zip != null )
				try
				{
					zip.close();
				}
				catch (IOException e) {
				}
		}
		return this.response(super.getResponse(), rsp.toString());
	}
	/**
	 * 导出子系统配置
	 * @return
	 */
	public String exportconfig()
	{
		log.info("Export the config of "+id);
		ZipOutputStream out = null;
		try 
		{
			String filename = sysid+".zip";
			String new_sysid = super.getRequest().getParameter("new_sysid");
			if( new_sysid != null ){
				filename = new_sysid+".zip";
			}
			if( super.getRequest().getParameter("all") != null ){
				filename = "cos_sys_config.zip";
			}
			getResponse().setContentType("application/binary;charset=ISO8859_1");
			getResponse().setHeader("Content-disposition", "attachment; filename*=UTF-8''"+URLEncoder.encode(filename,"UTF-8"));
			out = new ZipOutputStream(new CheckedOutputStream(getResponse().getOutputStream(), new CRC32()));
			Zookeeper zookeeper = ZKMgr.getZookeeper();
			if( super.getRequest().getParameter("all") != null ){
				this.responseMessage = "导出平台全部系统配置成功。";
				modulesMgr.exportZookeeper(zookeeper, "/cos/config/system", out);
				modulesMgr.exportZookeeper(zookeeper, "/cos/config/software", out);
				modulesMgr.exportZookeeper(zookeeper, "/cos/config/userpropcfg", out);
				modulesMgr.exportZookeeper(zookeeper, "/cos/config/role", out);
				modulesMgr.exportZookeeper(zookeeper, "/cos/config/modules", out);
			}
			else{
				this.responseMessage = "导出系统【"+sysid+"】配置成功。";
				if( new_sysid != null ){
					modulesMgr.exportModule(zookeeper, sysid, new_sysid, out);
					responseMessage += ", 系统ID改为了【"+new_sysid+"】";
				}
				else{
					modulesMgr.exportZookeeper(zookeeper, "/cos/config/modules/"+sysid, out);
				}
			}
    		logoper(responseMessage, "开发管理", "", null);
        }
        catch(Exception e)
        {
        	this.responseException ="导出系统【"+sysid+"】配置出现异常 "+e.getMessage();
    		logoper(responseException, "开发管理", null, null, e);
        	return "alert";
        }
        finally
        {
    		try
			{
            	if( out != null ) out.close();
			}
			catch (Exception e)
			{
				log.error("Failed to close the outputstream of zip.", e);
			}
        }
		return null;
	}
	/**
	 * 开发者管理导航
	 * 看到所有模块子系统
	 * @return
	 */
	public String navigate()
	{
		try 
		{
			List<JSONObject> modules = ModulesMgr.getConfigs();
			this.modulesMgr.setNavigates(modules, isSysadmin(), super.getUserAccount());
			JSONArray array = new JSONArray();
			for(JSONObject e : modules)
			{
				array.put(e);
			}
			jsonData = array.toString();
			jsonData = jsonData.replace("\\", "\\\\");
//			System.err.println(array.toString(4));
		}
		catch (Exception e) 
		{
			super.responseException = e.getMessage();
		}
		return "navigate";
	}


	/**
	 * 修改配置
	 * @return
	 */
	public String config()
	{
		String rsp = super.grid("/grid/local/modules.xml");
		if( sysid != null && !sysid.isEmpty() && localDataArray != null )
		{
			for(int i = 0; i < localDataArray.length(); i++)
			{
				JSONObject e = localDataArray.getJSONObject(i);
				if( !sysid.equals(e.getString("id")) )
				{
					localDataArray.remove(i);
					i -= 1;
				}
			}
			this.localData = localDataArray.toString();
		}
		this.showTitle = true;
		return rsp;
	}
	/**
	 * 开发者查询
	 * @return
	 */
	public String developers()
	{
		String rsp = super.grid("/grid/local/modulesdevelopers.xml");
		if( sysid != null && !sysid.isEmpty() && localDataArray != null )
		{
			for(int i = 0; i < localDataArray.length(); i++)
			{
				JSONObject e = localDataArray.getJSONObject(i);
				if( !sysid.equals(e.getString("id")) )
				{
					localDataArray.remove(i);
					i -= 1;
				}
			}
			this.localData = localDataArray.toString();
		}
		return rsp;
	}

	/**
	 * 开发者查询
	 * @return
	 */
	public String pop3()
	{
		String rsp = super.grid("/grid/local/modulespop3.xml");
		if( sysid != null && !sysid.isEmpty() && localDataArray != null )
		{
			for(int i = 0; i < localDataArray.length(); i++)
			{
				JSONObject e = localDataArray.getJSONObject(i);
				if( !sysid.equals(e.getString("id")) )
				{
					localDataArray.remove(i);
					i -= 1;
				}
			}
			this.localData = localDataArray.toString();
		}
		this.showTitle = true;
		return rsp;
	}
	/**
	 * 设置开发者
	 * @return
	 */
	public String presetdevelopers()
	{
		try
		{
			this.jsonData = userMgr.getRoleUsers(super.getUserRole()).toString();
			log.debug(this.jsonData);
		}
		catch(Exception e)
		{
			log.error("Failed to open the data of user for ", e);
			super.setResponseException("打开用户权限管理界面失败，因为"+e);
		}
		return "presetdevelopers";
	}

	/**
	 * 生成的发布记录
	 * @return
	 */
	public String prepublishdata()
	{
		Exception e1 = null;
		try
		{
			this.localDataArray = new JSONArray();
			this.modulesMgr.buildPublish(sysid, localDataArray, false);
		}
		catch (Exception e)
		{
			e1 = e;
			log.error("Failed to prepublish:", e);
		}
		return querydata(this.localDataArray, e1);
	}
	/**
	 * 预发布
	 * @return
	 */
	public String prepublish()
	{
		log.info("Prepublish "+this.sysid);
		String rsp = super.grid("/grid/local/modulesprepublish.xml");
		return rsp;
	}
	/**
	 * 发布
	 * @return
	 */
	public String publish()
	{
		String rsp = super.grid("/grid/local/modulespublish.xml");
		return rsp;
	}
	
	/**
	 * 审批
	 * @return
	 */
	public String approval()
	{
		String version = super.getRequest().getParameter("version");
		String ar = super.getRequest().getParameter("ar");
		String remark = "";
		String zkpath = "/cos/config/modules/"+sysid+"/publish/"+version;
		JSONObject publish = null;
		Zookeeper zookeeper = null;
		try 
		{
			zookeeper = ZKMgr.getZookeeper();
			publish = zookeeper.getUngzipJSONObject(zkpath);
		}
		catch (Exception e) 
		{
			return this.publish();
		}
		if( "2".equals(ar) )
		{
			remark = super.getRequest().getParameter("remark");
			if( remark == null || remark.isEmpty() )
			{
				this.responseException = "拒绝版本【"+version+"】发布需要输入拒绝理由";
			}
			else
			{
				publish.put("status", 2);//取消发布，给出原因
				publish.put("approver", super.getUserAccount());
				publish.put("apptime", Tools.getFormatTime("yyyy-MM-dd HH:mm:ss"));
				publish.put("reason", remark);
				zookeeper.setGzipJSONObject(zkpath, publish);
				responseMessage = "拒绝系统【"+sysid+"】待审批版本【"+version+"】发布";
				String viewpublish = "modules!viewpublish.action?sysid="+sysid+"&id="+version;;
	    		logoper(responseMessage, "开发管理", null, viewpublish);
	    		try {
					this.modulesMgr.sendNotiefieToAccount(
							super.getRequest(),
							publish.getString("publisher"),//给发布申请者发消息
							"开发管理",
							"系统管理员["+super.getUserAccount()+"]"+responseMessage,
							null,
							viewpublish,
					        "开发管理", "modules!navigate.action");
				} catch (Exception e) {
					log.error("", e);
				}
			}
		}
		else
		{
			try
			{

				if( zookeeper.exists("/cos/config/modules/"+sysid+"/menus/current") != null )
				{
					menusMgr.accept(super.getRequest(), super.getUserAccount(), sysid);//接受系统菜单配置
				}
				ArrayList<JSONObject> buffer = new ArrayList<JSONObject>();//临时缓存提交发布的程序配置
				JSONArray acceptdata = new JSONArray();
				if( publish.has("details") )
				{
					JSONArray details = publish.getJSONArray("details");
					for(int i = 0; i < details.length(); i++)
					{
						JSONObject e = details.getJSONObject(i);
						if( e.has("publish") )
						{
							JSONArray publishs = e.getJSONArray("publish");
							for(int j = 0; j < publishs.length(); j++)
							{
								JSONObject cfg = publishs.getJSONObject(j);
								cfg.put("id", e.getString("id"));
								cfg.put("serverkey", Tools.encodeMD5(cfg.getString("serverid")));
								cfg.put("operuser", super.getUserAccount());
								if( e.has("timeline"))
									cfg.put("timeline", e.getJSONArray("timeline"));
								acceptdata.put(cfg);
							}

							JSONObject program = zookeeper.getJSONObject("/cos/config/modules/"+sysid+"/program/"+e.getString("id"));
							buffer.add(program);
						}
					}
				}
				controlMgr.accept(super.getRequest(), super.getUserAccount(), acceptdata);
				for( JSONObject program : buffer)
				{
					JSONArray publishlogs = program.has("publishlogs")?program.getJSONArray("publishlogs"):new JSONArray();
					JSONObject publishlog = null;
					if( program.has("timeline") )
					{
						JSONArray versions = program.getJSONArray("timeline");
						for( int j = 0; j < versions.length(); j++ )
						{
							JSONObject timeline = versions.getJSONObject(j);
							if( program.getString("version").equals(timeline.getString("version")) )
							{
								publishlog = new JSONObject();
								publishlog.put("version", program.getString("version"));
								publishlog.put("time", Tools.getFormatTime("yyyy-MM-dd HH:mm"));
								publishlog.put("text", timeline.getString("text"));
								break;
							}
						}
					}
					if( publishlog != null )
					{
						publishlogs.put(publishlog);
						program.put("publishlogs", publishlogs);
						zookeeper.setJSONObject("/cos/config/modules/"+sysid+"/program/"+program.getString("id"), program);
					}
				}
				String publisher = publish.getString("publisher");
				publish.put("status", 1);
				publish.put("approver", super.getUserAccount());
				publish.put("apptime", Tools.getFormatTime("yyyy-MM-dd HH:mm:ss"));
				zookeeper.setGzipJSONObject(zkpath, publish);
				zkpath = "/cos/config/modules/"+sysid+"/publish";
				publish = new JSONObject();
				publish.put("current", version);
				zookeeper.setJSONObject(zkpath, publish);
				responseMessage = "接受系统【"+sysid+"】待审批版本【"+version+"】发布";
				String viewpublish = "modules!viewpublish.action?sysid="+sysid+"&id="+version;;
	    		logoper(responseMessage, "开发管理", null, viewpublish);
	    		try {
					this.modulesMgr.sendNotiefieToAccount(
							super.getRequest(),
							publisher,//给发布申请者发消息
							"开发管理",
							"系统管理员["+super.getUserAccount()+"]"+responseMessage,
							"系统已完成发布，请等待系统管理员重启系统。",
							viewpublish,
					        "开发管理", "modules!navigate.action");
				} catch (Exception e) {
					log.error("", e);
				}
			}
			catch(Exception e)
			{
				log.error("", e);
				publish.put("status", 3);
				publish.put("approver", super.getUserAccount());
				publish.put("apptime", Tools.getFormatTime("yyyy-MM-dd HH:mm:ss"));
				publish.put("reason", e.getMessage());
				zookeeper.setGzipJSONObject(zkpath, publish);
			}
		}
		log.info("Do approval(sysid="+sysid+", ar="+ar+", version="+version+", remark="+remark+").");
		return publish();
	}
	/**
	 * 查看发布的配置文件
	 * @return
	 */
	public String viewpublish()
	{
		log.info("Previewpublish "+this.sysid);
		String rsp = super.grid("/grid/local/modulespublishhistory.xml");
		return rsp;
	}

	/**
	 * 生成的发布记录
	 * @return
	 */
	public String viewpublishdata()
	{
		Exception e1 = null;
		try
		{

			this.localDataArray = new JSONArray();
    		String zkpath = "/cos/config/modules/"+sysid+"/publish/"+id;//查看历史的发布记录
    		Zookeeper zookeeper = ZKMgr.getZookeeper();
			JSONObject history = zookeeper.getUngzipJSONObject(zkpath);
			if( history != null && history.has("details") )
			{
				localDataArray = history.getJSONArray("details");
			}
			else{
				localDataArray = new JSONArray();
			}
		}
		catch (Exception e)
		{
			e1 = e;
		}
		return querydata(this.localDataArray, e1);
	}
	/**
	 * 上传文件
	 * @return
	 */
	public String uplogo()
	{
		JSONObject rsp = new JSONObject();
		rsp.put("previewid", "uploadlogo");
		try
		{
			if( this.uploadfile == null )
			{
				rsp.put("alt", "上传系统LOGO失败，因为未能收到上传文件");
				return response(super.getResponse(), rsp.toString());
			}
			//id = super.getRequest().getParameter("uploadfile_id");
			if( sysid == null )
			{
				rsp.put("alt", "上传统LOGO失败，因为未能收到系统ID参数");
				return response(super.getResponse(), rsp.toString());
			}
			rsp.put("id", sysid);
			this.modulesMgr.setLogo(sysid, IOHelper.readAsByteArray(uploadfile));
			rsp.put("url", Kit.URL_IMAGEPATH(super.getRequest())+"modulelogo/"+sysid+"?ts="+uploadfile.lastModified());
			rsp.put("succeed", true);
		}
		catch(Exception e)
		{
			rsp.put("alt", "更换系统［"+sysid+"］图标出现异常:"+e.getMessage());
		}
		return this.response(super.getResponse(), rsp.toString());
	}

	/**
	 * 显示组件图标
	 * @return
	 */
	public String logo()
	{
		OutputStream out = null;
		byte[] payload = null;
		try
		{
			path = "/cos/config/modules/"+sysid+"/logo.png";
			ZooKeeper zookeeper = ZKMgr.getZooKeeper();
			Stat stat = zookeeper.exists(path, false); 
			if( stat != null)
			{
				payload = zookeeper.getData(path, false, stat);
			}
		}
		catch(Exception e)
		{
		}
		
		try
		{
	        HttpServletResponse response = super.getResponse();
			response.setContentType("image/png");
			response.setHeader("Content-disposition", "inline; filename="+sysid+".png");
			out = response.getOutputStream();
			if( payload == null)
			{
				File tmp = new File(PathFactory.getWebappPath(), "images/cmp/"+sysid+".png");
				if( !tmp.exists() )
				{//随机创建PNG图片
					Random r = new Random();
					File rp = new File(PathFactory.getWebappPath(), "images/cmp/"+r.nextInt(95)+".png");
					payload = IOHelper.readAsByteArray(rp);
				}
				else
				{
					payload = IOHelper.readAsByteArray(tmp);
				}
			}
			out.write(payload);
			out.flush();
		}
		catch(Exception e)
		{
		}
		return null;
	}
	/**
	 * 版本时间树
	 * @return
	 */
	public String versiontimeline()
	{
		JSONObject data = new JSONObject();
		Zookeeper zookeeper = null;
		String zkpath = "";
		String name = "未知模块子系统名称";
		try
		{
			zookeeper = ZKMgr.getZookeeper();
			zkpath = "/cos/config/modules/"+sysid;
			JSONObject module = zookeeper.getJSONObject(zkpath);
			if( module == null ) return null;
			name = module.getString("SysName");
			zkpath += "/publish";
			JSONArray versions = new JSONArray();
			zookeeper.getJSONArray(zkpath, versions, false, true);
			JSONObject asset = new JSONObject();
			asset.put("media", "images/notes.png");
			asset.put("credit", "");
			asset.put("caption", "");
			JSONObject timeline = new JSONObject();
			timeline.put("asset", asset);
			timeline.put("date", versions);
			
			long fistTs = System.currentTimeMillis();
			int programVersion[] = new int[4];
			long _v = 0;
			for(int i = 0; i < versions.length(); i++)
			{
				JSONObject version = versions.getJSONObject(i);
				if( !version.has("apptime") ){
					versions.remove(i);
					i -= 1;
					continue;
				}
				String v = version.getString("version");
				String[] args = Tools.split(v, ".");
				int v0 = Integer.parseInt(args[0]);
				int v1 = Integer.parseInt(args[1]);
				int v2 = Integer.parseInt(args[2]);
				int v3 = Integer.parseInt(args[3]);
				long v_ = v0*100*100*100+v1*100*100+v2*100+v3;
				if( _v < v_ )
				{
					programVersion[0] = v0;
					programVersion[1] = v1;
					programVersion[2] = v2;
					programVersion[3] = v3;
					_v = v_;
				}
				
				String time = version.getString("apptime");//dd/MM/yyyy
				args = Tools.split(time, "-");
				int j = args[2].indexOf(' ');
				args[2] = args[2].substring(0, j).trim();
				Calendar calendar = Calendar.getInstance();
				calendar.setTimeInMillis(0);
				calendar.set(Integer.parseInt(args[0]), Integer.parseInt(args[1])-1, Integer.parseInt(args[2]));
				version.put("startDate", Tools.getFormatTime("yyyy,M,d", calendar.getTimeInMillis()));
				version.put("headline", "v"+v);
				version.put("text", version.has("remark")?version.getString("remark"):"没有留下版本特性");
				asset = new JSONObject();
				asset.put("media", HelperMgr.getImgWallpaper());
				asset.put("credit", "");
				asset.put("caption", "");
				version.put("asset", asset);
				if( calendar.getTimeInMillis() < fistTs ) fistTs = calendar.getTimeInMillis();
			}
//				if( version.equals("0.0.0.0") )
			String version = "v"+programVersion[0]+"."+programVersion[1]+"."+programVersion[2]+"."+programVersion[3];
			timeline.put("headline", name);
			timeline.put("type", "default");
			timeline.put("startDate", Tools.getFormatTime("yyyy,M", fistTs));
			StringBuffer text = new StringBuffer();
			text.append("<span class='version'>"+version+"</span>");
			text.append("<span class='title'>"+(module.has("id")?module.getString("id"):"N/A")+"</span><br/>");
//			text.insert(0, "<span class='version'>v"+timeline.getString("version")+"</span>");
			text.append(module.has("SysDescr")?module.getString("SysDescr"):"");
			timeline.put("text", text.toString());
			data.put("timeline", timeline);
		}
		catch(Exception e)
		{
			log.error("Failed to get the version from "+zkpath, e);
			return noversion(data, name, "构建程序["+sysid+"]的版本数据出现异常"+e.getMessage());
		}
		return response(super.getResponse(), data.toString());
	}
	/**
	 * 菜单配置
	 * @return
	 */
	public String menucfg()
	{
		return "menucfg";
	}
	
	private ModulesMgr modulesMgr;

	public void setModulesMgr(ModulesMgr modulesMgr) {
		this.modulesMgr = modulesMgr;
	}

	//用户管理器
	private UserMgr userMgr;

	public void setUserMgr(UserMgr userMgr) {
		this.userMgr = userMgr;
	}
	/*远过程调用*/
	private ProgramMgr programMgr;
	
	public void setProgramMgr(ProgramMgr programMgr) {
		this.programMgr = programMgr;
	}
	
	/*主控配置*/
	private ControlMgr controlMgr;
	
	public void setControlMgr(ControlMgr controlMgr) {
		this.controlMgr = controlMgr;
	}
	
	private MenusMgr menusMgr;
	public void setMenusMgr(MenusMgr menusMgr) {
		this.menusMgr = menusMgr;
	}
}
