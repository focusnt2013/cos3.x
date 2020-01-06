 package com.focus.cos.web.dev.action;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.CRC32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.zookeeper.data.Stat;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.focus.cos.web.action.TemplateChecker;
import com.focus.cos.web.common.HelperMgr;
import com.focus.cos.web.common.Kit;
import com.focus.cos.web.common.PathFactory;
import com.focus.cos.web.common.ZKMgr;
import com.focus.cos.web.dev.service.DiggConfigMgr;
import com.focus.cos.web.dev.service.DiggMgr;
import com.focus.util.IOHelper;
import com.focus.util.Item;
import com.focus.util.Tools;
import com.focus.util.XMLParser;
import com.focus.util.Zookeeper;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;

public class DiggConfigAction extends DevAction 
{
	private static final long serialVersionUID = -3079427411469239198L;
	private static final Log log = LogFactory.getLog(DiggConfigAction.class);
	private DiggMgr diggMgr;
	private DiggConfigMgr diggConfigMgr;
	/*模板脚本*/
	private String gridtext;
	/*模板开发的选项卡模式开发模式*/
	private boolean developer;
	public boolean isDeveloper() {
		return developer;
	}
	/**
	 * 脚本设置
	 * @return
	 */
	public String scriptset()
	{
		return "scriptset";
	}

	/**
	 * 
	 * @return
	 * @throws Exception
	 */
	public InputStream getGridXmlInputStream()
		throws Exception
	{
		if( this.gridtext != null && !gridtext.isEmpty()){
			return new ByteArrayInputStream(gridtext.getBytes("UTF-8"));
		}
		return super.getGridXmlInputStream();
	}
	/**
	 * 测试
	 * @return
	 */
	public String pretest()
	{
		this.workmode = TemplateChecker.TEST;
    	ServletOutputStream sos = null;
		String javascript = "";
		InputStream is = null;
		try 
		{
			getResponse().setContentType("text/html");
			getResponse().setCharacterEncoding("UTF-8");
			sos = getResponse().getOutputStream();
			sos.print("<html><body style='padding: 1px;font-size:9pt;background:#000;color:#fff;word-break:keep-all;white-space:pre;'>");
			sos.write(("<p id='aaa' style='padding-top:5px;padding-bottom:5px;padding-left:5px;font-size:14px;color:#33ffff;background:'>模板【"+gridxml+"】开始执行检测...</p>").getBytes("UTF-8"));
			sos.flush();
			is = this.getGridXmlInputStream();
			super.parse(is);
		}
		catch (Exception e) 
		{
			checker.err();
			test(1, "解析元数据查询配置模板发生<span style='color:#ff3300;font-size:14px;font-weight:bold;'>【致命】</span>异常， 请检查模板XML格式是否正确。\r\n %s", e.getMessage());
			checker.write(e);
		}
        finally
        {
			if( is != null )
				try {
					is.close();
				} catch (IOException e) {
				}
        	if( sos != null )
        	{
	        	try
	        	{
	    			sos.write(checker.payload());
	    			sos.flush();
    				String tips = "模板【"+id+"】测试没有发现问题。";
	    			if( checker.warcount()>0||checker.errcount()>0)
	    			{
	    				tips = "测试模板发现"+checker.errcount()+"个错误，"+checker.warcount()+"个改进提示。";
	    			}
	    			javascript += "document.getElementById('aaa').innerHTML = '"+tips+"';";
	    			if( checker.errcount() > 0 )
	    			{
	    				javascript += "document.getElementById('aaa').style.background = '#ff6633';";
	    			}
	    			else if( checker.warcount() > 0 )
	    			{
	    				javascript += "document.getElementById('aaa').style.background = '#ffff66';";
	    			}
	    			javascript += "parent.finishTest('"+tips+"', "+checker.errcount()+", "+checker.warcount()+");";
					sos.write(("</body>\r\n\r\n<script type='text/javascript'>"+javascript+"</script></html>").getBytes("UTF-8"));
					sos.flush();
					sos.close();
	        	} catch (IOException e1) {
				}
        	}
        }
		return null;
	}

	/**
	 * 执行模板测试
	 * @return
	 */
	public String test()
	{
		this.gridtext = null;
		return pretest();
	}
	
	public long getTs(){
		return System.currentTimeMillis();
	}
	/**
	 * 保存元数据查询配置模板
	 * @return
	 */
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	public String save()
	{
		long ts = 0;
    	try 
		{
    		log.info("Save the script("+timestamp+") of gridxml to "+gridxml);
			JSONObject timeline = new JSONObject();
			timeline.put("text", remark);
			timeline.put("developer", super.getUserAccount());
			timeline.put("time", Tools.getFormatTime("MM/dd/yyyy"));
			timeline.put("version", version);
    		Zookeeper zookeeper = ZKMgr.getZookeeper();
    		///cos/config/modules/bqbk-yuqing/digg/yuqing_trend_emotion.xml
    		Stat stat = zookeeper.exists(gridxml);
    		if( stat != null && timestamp != stat.getMtime() ) {
    			ts = stat.getMtime();
    			JSONObject obj = this.diggConfigMgr.getTemplate(gridxml, "");
    			this.version = obj.getString("version");
    			throw new Exception("该模板已近于["+Tools.getFormatTime("HH:mm:ss", stat.getMtime())+"]被其他用户修改版本号为["+version+"].");
    		}
    		ts = stat!=null?stat.getMtime():0;
			byte[] backup = zookeeper.getData(gridxml);
			String gridtext0 = null;
			if( backup != null )
			{
				gridtext0 = new String(backup,"UTF-8");
				if( gridtext0.equals(gridtext) )
				{
					javascript = "parent.finishSave(false, '"+gridxml+"', '编辑版本没有发生任何修改', "+ts+", '"+version+"');";
					return null;
				}
				zookeeper.setString(gridxml, gridtext);
			}
			else
			{
				zookeeper.createObject(gridxml, gridtext);
			}
			int i = gridxml.lastIndexOf("digg");
			path = gridxml.substring(i+5);
			String versionPath = gridxml+"/"+version;
			//得到当前版本号的版本对象
			JSONObject timeline1 = zookeeper.getJSONObject(versionPath);
			JSONArray history = new JSONArray();
			JSONObject current = new JSONObject();
			current.put("developer", super.getUserAccount());
			current.put("time", Tools.getFormatTime("yyyy-MM-dd HH:mm:ss"));
			current.put("text", remark);
			history.put(current);
			timeline.put("history", history);
			if( timeline1 != null )
			{
				if( remark==null || remark.isEmpty() )
				{
					timeline.put("text", timeline1.getString("text"));
				}
				
				if(timeline1.has("history"))
				{
					JSONArray history0 = timeline1.getJSONArray("history");
					for( i = 0; i < history0.length(); i++)
					{
						history.put(history0.get(i));
					}
				}
				timeline.put("backup", Kit.chr2Unicode(gridtext0));
				zookeeper.setJSONObject(versionPath, timeline);
			}
			else
			{
				//将老版本的版本号从request中获得
				String oldversion = super.getRequest().getParameter("oldversion");
				if( oldversion != null && !oldversion.isEmpty() ){
					JSONObject timeline0 = zookeeper.getJSONObject(gridxml+"/"+oldversion);
					if( timeline0 != null ){
						//将备份数据写到老版本号中
						timeline0.put("backup", Kit.chr2Unicode(gridtext0));
						zookeeper.setJSONObject(gridxml+"/"+oldversion, timeline0);
					}
				}
				zookeeper.createObject(versionPath, timeline);
			}
			File dir = new File(PathFactory.getDataPath(), "grid/"+sysid);
			File file = new File(dir, path);
			if( !file.getParentFile().exists() )
			{
				file.getParentFile().mkdirs();
			}
			IOHelper.writeFile(file, gridtext.getBytes("UTF-8"));
			stat = zookeeper.exists(gridxml);
			javascript = "parent.finishSave(true, '"+gridxml+"', '', "+stat.getMtime()+");";
		}
		catch (Exception e) 
		{
			log.error("", e);
			javascript = "parent.finishSave(false, '"+gridxml+"', '保存模板失败因为:"+e.getMessage()+"', "+ts+", '"+version+"');";
		}
    	finally{
        	ServletOutputStream out = null;
        	try {
        		out = getResponse().getOutputStream();
        		getResponse().setContentType("text/html");
        		getResponse().setCharacterEncoding("UTF-8");
        		out.write(("<html><script type='text/javascript'>"+javascript+"</script></html>").getBytes("UTF-8"));
        		out.flush();
        		out.close();
        	} catch (IOException e) {
    			log.error("", e);
    		}
    	}
		return null;
	}

	/**
	 * 上传元数据配置模板
	 * @return
	 */
	public String upload()
	{
		JSONObject rsp = new JSONObject();
		if( this.uploadfile == null || !uploadfile.exists() )
		{
			rsp.put("alt", "上传元数据查询配置模板失败，因为未能收到文件包");
			return response(super.getResponse(), rsp.toString());
		}
		log.info("Upload the template of "+gridxml+" from "+uploadfile);
        try
        {
        	if( path.endsWith(".xml") )
        	{//修改
        		int i = path.lastIndexOf("/");
        		String xmlname = path.substring(i+1);
        		rsp.put("template", diggConfigMgr.getTemplate(new FileInputStream(uploadfile), path, xmlname, uploadfile.lastModified(), uploadfile.lastModified()));
        		rsp.put("newTemplate", false);
        	}
        	else
        	{
        		path += "/"+gridxml;
        		rsp.put("template", diggConfigMgr.getTemplate(new FileInputStream(uploadfile), path, gridxml, uploadfile.lastModified(), uploadfile.lastModified()));
        		rsp.put("newTemplate", true);
        	}
        	rsp.put("xml", new String(IOHelper.readAsByteArray(uploadfile), "UTF-8"));
			rsp.put("succeed", true);
        }
        catch(Exception e)
        {
        	log.error("Failed to upload template", e);
			rsp.put("alt", "上传元数据查询配置模板失败，因为出现异常"+e.getMessage());
        }
		return this.response(super.getResponse(), rsp.toString());
	}
	
	/**
	 * 下载元数据查询配置模板
	 * @return
	 */
	public String download()
	{
		log.info("Download template of "+gridxml);
    	ServletOutputStream out = null;
		InputStream is;
		try 
		{
			if( gridxml.startsWith("/grid/local") )
    		{
				is = this.getClass().getResourceAsStream(gridxml);
    		}
			else
			{
				is = new ByteArrayInputStream(ZKMgr.getZookeeper().getData(gridxml));
			}
	        byte[] payload = IOHelper.readAsByteArray(is);
			int i = gridxml.lastIndexOf("/");
			String filename = gridxml.substring(i+1);
			getResponse().setContentType("application/xml;charset=UTF-8");
            getResponse().setHeader("Content-disposition", "attachment; filename*=UTF-8''"+URLEncoder.encode(filename,"UTF-8"));
    		out = getResponse().getOutputStream();
    		out.write(payload);
    		out.flush();
            this.responseMessage = "从系统【"+sysid+"】元数据开发管理下载模板文件成功。";
    		logoper(responseMessage, "开发管理", new String(payload, "UTF-8"), null);
        }
        catch(Exception e)
        {
        	this.responseException ="从系统【"+sysid+"】元数据开发管理下载模板文件出现异常 "+e.getMessage();
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
			}
        }
		return null;
	}

	/**
	 * 导出子系统所有模版
	 * @return
	 */
	public String exporttemplates()
	{
		log.info("Export templates of "+id);
		ZipOutputStream out = null;
		try 
		{
			String filename = sysid+"_griddigg_templates.zip";
			getResponse().setContentType("application/binary;charset=ISO8859_1");
			getResponse().setHeader("Content-disposition", "attachment; filename*=UTF-8''"+URLEncoder.encode(filename,"UTF-8"));
			CheckedOutputStream cos = new CheckedOutputStream(getResponse().getOutputStream(), new CRC32());
			out = new ZipOutputStream(cos);
			Zookeeper zookeeper = ZKMgr.getZookeeper();
			List<String> list = zookeeper.getChildren(id);
			for(String name : list){
				this.diggConfigMgr.makeTemplateZip(zookeeper, id+"/"+name, out, "");
			}
            this.responseMessage = "从系统【"+sysid+"】元数据开发管理导出模板文件成功。";
    		logoper(responseMessage, "开发管理", "", null);
        }
        catch(Exception e)
        {
        	this.responseException ="从系统【"+sysid+"】元数据开发管理导出模板文件出现异常 "+e.getMessage();
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
	 * 导入元数据查询配置模板
	 * @return
	 */
	public String importtemplates()
	{
		JSONObject rsp = new JSONObject();
		if( this.uploadfile == null || !uploadfile.exists() )
		{
			rsp.put("alt", "导入元数据查询配置模板失败，因为未能收到文件包");
			return response(super.getResponse(), rsp.toString());
		}
		log.info("Import the templates of "+sysid+" from "+uploadfile);
		ZipFile zip = null;
        String json = null;
        StringBuilder sb = new StringBuilder();
        try
        {
        	Zookeeper zookeeper = ZKMgr.getZookeeper();
	        zip = new ZipFile(uploadfile, Charset.forName("GBK"));
	        Enumeration<?> entries = zip.entries();
	        ArrayList<ZipEntry> templats = new ArrayList<ZipEntry>();
	        ArrayList<ZipEntry> versions = new ArrayList<ZipEntry>();
	        HashMap<String, String> names = new HashMap<String, String>();
	        while(entries.hasMoreElements())
	        {
	            ZipEntry entry = (ZipEntry)entries.nextElement();  
	            if( entry.getName().endsWith(".xml") )
	            {
	            	templats.add(entry);
	            }
	            else if( entry.getName().endsWith("name.txt") )
	            {
		            String path = entry.getName().replaceAll("\\*", "/");
		            path = path.substring(0, path.lastIndexOf("/"));
		            if( !path.startsWith("/") ){
		            	path = "/"+path;
		            }
	            	InputStream in = zip.getInputStream(entry);
	            	byte[] buf = IOHelper.readAsByteArray(in);
	            	in.close();
	            	names.put(path, new String(buf));
	            }
	            else
	            {
	            	versions.add(entry);
	            }
	        }
	        //记忆模板名称对应的路径
	        int dircount = 0, covercount = 0;
	        HashMap<String, String> map = new HashMap<String, String>();
	        sb.append("Handle the tempaltes:");
	        for(int i = 0; i < templats.size(); i++)
	        {
	        	ZipEntry entry = templats.get(i);
	        	sb.append("\r\n\t"+entry.getName());
	            String path = entry.getName().replaceAll("\\*", "/");
	            String[] args = Tools.split(path, "/");
	            if( args.length < 2 ) continue;
	            String name = args[args.length-2];
	            String zkpath = "/cos/config/modules/"+sysid+"/digg";
	            for(int j = 0; j < args.length - 2; j++){
	        		List<String> list = zookeeper.getChildren(zkpath);
	        		String nodepath = null;
	        		for(String nodename : list )
	        		{
	        			if( nodename.endsWith(".xml") ) continue;
	        			nodepath = zkpath + "/" + nodename;
	        			String name0 = ZKMgr.getZookeeper().getUTF8(nodepath);
	        			if( args[j].equals(name0) ){
	        				break;
	        			}
	        			nodepath = null;
	        		}
	        		
	        		if( nodepath == null )
	        		{
	        			//创建新的目录节点
	        			String _name = "";
	        			nodepath = "/cos/config/modules/"+sysid+"/digg";
	    	            for(int k = 0; k < args.length - 2; k++){
    	            		_name += "/";
	    	            	_name += args[k];
	    	            	if( names.containsKey(_name) ){
	    	            		nodepath += "/" + names.get(_name);
	    	            	}
	    	            	else{
	    	            		nodepath = "/" + Tools.getUniqueValue();
	    	            	}
    	            		Stat stat = zookeeper.exists(nodepath);
    	            		if( stat == null ){
    	    	            	zookeeper.create(nodepath, args[j].getBytes("UTF-8"));
    	            		}
	    	            }
						ZKMgr.getZookeeper().createNode(nodepath, args[j].getBytes("UTF-8"));
						dircount += 1;
			        	sb.append("\tCreate the dir["+args[j]+"]");
	        		}
	        		zkpath = nodepath;
	            }
	            zkpath += "/"+ name+".xml";
	        	sb.append("\r\n\t\t"+zkpath);
	            Stat stat = zookeeper.exists(zkpath);
            	InputStream in = zip.getInputStream(entry);
	            if( stat != null ){
		        	sb.append("\tCover.");
		        	covercount += 1;
	            	zookeeper.setData(zkpath, IOHelper.readAsByteArray(in), stat);
	            }
	            else
	            {
	            	zookeeper.create(zkpath, IOHelper.readAsByteArray(in));
	            }
	            in.close();
	            map.put(name, zkpath);
	        }
	        sb.append("Handle the versions:");
	        for(int i = 0; i < versions.size(); i++)
	        {
	        	ZipEntry entry = versions.get(i);
	        	sb.append("\r\n\t"+entry.getName());
	            String path = entry.getName().replaceAll("\\*", "/");
	            String[] args = Tools.split(path, "/");
	            if( args.length < 3 ) continue;
	            String name = args[args.length-1];
	            String key = args[args.length-3];
	            String zkpath = map.get(key);
	            zkpath += "/"+ name;
	        	sb.append("\r\n\t\t"+zkpath);
            	InputStream in = zip.getInputStream(entry);
            	json = new String(IOHelper.readAsByteArray(in), "UTF-8");
            	sb.append("\t"+json);
            	if( json.isEmpty() ) continue;
            	zookeeper.setJSONObject(zkpath, new JSONObject(json));
	        }
			rsp.put("alt", "导入元数据模板成功，共保存"+templats.size()+"个模板，覆盖"+covercount+"模版, 创建"+dircount+"个模板目录。");
			rsp.put("succeed", true);
			log.info(sb.toString());
        }
        catch(Exception e)
        {
        	log.error(sb.toString(), e);
			rsp.put("alt", "导入元数据模板失败，因为出现异常"+e.getMessage());
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
	 * 使用情况查询
	 * @return
	 */
	public String usage()
	{
	    try
	    {
	      this.localDataArray = new JSONArray();
	      if ("local".equalsIgnoreCase(this.sysid)) {
	        this.diggConfigMgr.loadLocalTemplates("/grid/local", this.localDataArray, false, false);
	      }
	      else {
	        this.diggConfigMgr.loadTemplates("/cos/config/modules/" + this.sysid, this.localDataArray, false);
	      }
	      
	      Zookeeper zookeeper = ZKMgr.getZookeeper();
	      BasicDBList labels = new BasicDBList();
	      BasicDBList labels1 = new BasicDBList();
	      BasicDBList labels2 = new BasicDBList();
	      for (int i = 0; i < this.localDataArray.length(); i++)
	      {
	        JSONObject row = this.localDataArray.getJSONObject(i);
	        HashMap<String, Integer> developers = new HashMap<String, Integer>();
	        int totalcount = 0;
	        if (row.has("developer"))
	          developers.put(row.getString("developer"), Integer.valueOf(0));
	        String name;
	        if (!"local".equalsIgnoreCase(this.sysid)) {
	          JSONArray data = zookeeper.getJSONArray(row.getString("id"), false, "checked.json");
	          row.put("versions", data);
	          for (int j = 0; j < data.length(); j++) {
	            JSONObject version = data.getJSONObject(j);
	            version.put("modifytime", Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", version.getLong("_timestamp")));
	            if (version.has("history")) {
	              JSONArray history = version.getJSONArray("history");
	              for (int k = 0; k < history.length(); k++) {
	                JSONObject v = history.getJSONObject(k);
	                if (v.has("developer")) {
	                  name = v.getString("developer");
	                  Integer count = (Integer)developers.get(name);
	                  count = Integer.valueOf(count == null ? 0 : count.intValue());
	                  developers.put(name, count = Integer.valueOf(count.intValue() + 1));
	                  totalcount++;
	                }
	              }
	            }
	          }
	        }
	        String checkpath = row.getString("id") + "/checked.json";
	        if ("local".equalsIgnoreCase(this.sysid)) {
	          checkpath = "/cos/config" + row.getString("id") + "/checked.json";
	        }
	        BasicDBObject label = new BasicDBObject("value", row.getString("id"));
	        JSONObject check = zookeeper.getJSONObject(checkpath);
	        if (check != null) {
	          row.put("check", check);
	          if (check.getInt("err") > 0) {
	            label.put("label", "3");
	          }
	          else if (check.getInt("war") > 0) {
	            label.put("label", "2");
	          }
	          else {
	            label.put("label", "1");
	          }
	        }
	        else {
	          label.put("label", "0");
	        }
	        labels2.add(label);
	        
	        label = new BasicDBObject("value", row.getString("id"));
	        label.put("label", row.getString("cname"));
	        labels.add(label);
	        
	        StringBuilder d = new StringBuilder();
	        List<Map.Entry<String, Integer>> list = new ArrayList<Map.Entry<String, Integer>>(developers.entrySet());
	        Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
	            public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
	            	return ((Integer)o2.getValue()).intValue() - ((Integer)o1.getValue()).intValue();
	            }
	        });
	        for (Map.Entry<String, Integer> developer : list) {
	          if (d.length() > 0) {
	            d.append(",");
	          }
	          if (totalcount > 0) {
	            d.append(String.format("%s(%s%%)", new Object[] { developer.getKey(), Integer.valueOf(((Integer)developer.getValue()).intValue() * 100 / totalcount) }));
	          }
	          else {
	            d.append((String)developer.getKey());
	          }
	        }
	        label = new BasicDBObject("value", row.getString("id"));
	        label.put("label", d.toString());
	        labels1.add(label);
	      }
	      BasicDBObject  label = new BasicDBObject("value", "");
          label.put("label", "[所有模板]");
	      labels.add(0, label);
	      this.labelsModel.put("TEMPLATE", labels);
	      this.labelsModel.put("developer", labels1);
	      this.labelsModel.put("check", labels2);
	      labels = new BasicDBList();
	      label = new BasicDBObject("value", "local");
	      label.put("label", "内置模板");
	      labels.add(label);
	      this.labelsModel.put("MODULE", labels);
	    }
	    catch (Exception localException) {}
	    String type = grid("/grid/local/diggusage.xml");
	    BasicDBObject labelsFilter = (BasicDBObject)filterModel.get("labels");
	    labelsFilter.put("SPEED_API", diggMgr.getSpeedApi());
	    labelsFilter.put("SPEED_QUERY", diggMgr.getSpeedQuery());
	    labelsFilter.put("SPEED_EXPORT", diggMgr.getSpeedExport());
	    return type;
	}	
	/**
	 * 
	 * @return
	 */
	public String query()
	{
		log.info("Query the digg of "+sysid+",id="+id);
		this.gridtext = null;
		String rspstr = super.grid("/grid/local/diggquery.xml");
		return rspstr;
	}
	/**
	 * 
	 * @return
	 */
	public String queryapi()
	{
		log.info("Query the digg-api of "+sysid+",id="+id);
		this.gridtext = null;
		String rspstr = super.grid("/grid/local/diggapiquery.xml");
		return rspstr;
	}
	/**
	 * 查询数据
	 * @return
	 */
	public String querydata()
	{
	    log.info("Get the digg-cfgs of " + this.path);
	    Exception e1 = null;
	    try
	    {
	    	this.localDataArray = new JSONArray();
	    	boolean local = !this.path.startsWith("/");
	    	if (local) {
	    		this.diggConfigMgr.loadLocalTemplates(this.path, this.localDataArray, false, false);
	    	}
	    	else {
	    		this.diggConfigMgr.loadTemplates(this.path, this.localDataArray, super.getRequest().getParameter("api")!=null);
	    	}
	    	Zookeeper zookeeper = ZKMgr.getZookeeper();
	    	for (int i = 0; i < this.localDataArray.length(); i++) {
		        JSONObject row = this.localDataArray.getJSONObject(i);
		        HashMap<String, Integer> developers = new HashMap<String, Integer>();
		        int totalcount = 0;
		        if (row.has("developer"))
		        	developers.put(row.getString("developer"), Integer.valueOf(0));
		        JSONArray history;
		        if (!local) {
		          JSONArray data = zookeeper.getJSONArray(row.getString("id"), false, "checked.json");
		          row.put("versions", data);
		          for (int j = 0; j < data.length(); j++) {
		            JSONObject version = data.getJSONObject(j);
		            version.put("modifytime", Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", version.getLong("_timestamp")));
		            if (version.has("history")) {
		              history = version.getJSONArray("history");
		              for (int k = 0; k < history.length(); k++) {
		                JSONObject v = history.getJSONObject(k);
		                if (v.has("developer")) {
		                  String name = v.getString("developer");
		                  Integer count = (Integer)developers.get(name);
		                  count = Integer.valueOf(count == null ? 0 : count.intValue());
		                  developers.put(name, count = Integer.valueOf(count.intValue() + 1));
		                  totalcount++;
		                }
		              }
		            }
		          }
		        }
		        JSONArray d = new JSONArray();
		        List<Map.Entry<String, Integer>> list = new ArrayList<Map.Entry<String, Integer>>(developers.entrySet());
		        Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
		          public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
		            return ((Integer)o2.getValue()).intValue() - ((Integer)o1.getValue()).intValue();
		          }
		        });
		        for (Map.Entry<String, Integer> developer : list) {
		          if (totalcount > 0) {
		            d.put(String.format("%s(%s%%)", new Object[] { developer.getKey(), Integer.valueOf(((Integer)developer.getValue()).intValue() * 100 / totalcount) }));
		          }
		          else {
		            d.put(developer.getKey());
		          }
		        }
		        row.put("developer", d);
		        String checkpath = row.getString("id") + "/checked.json";
		        if (local) {
		          checkpath = "/cos/config" + row.getString("id") + "/checked.json";
		        }
		        JSONObject check = zookeeper.getJSONObject(checkpath);
		        if (check != null) {
		          row.put("check", check);
		        }
	      	}
	    }
	    catch (Exception e)
	    {
	    	log.error("", e);
	    	e1 = e;
	    }
	    return querydata(this.localDataArray, e1);
	}
	/**
	 * 查看模版版本
	 * @return
	 */
	public String version()
	{
		this.gridtext = null;
		return super.grid("/grid/local/diggversion.xml");
	}
	
	/**
	 * 版本数据
	 * @return
	 */
	public String versiondata()
	{
		Zookeeper zookeeper = null;
		JSONObject dataJSON = new JSONObject();
		try
		{
			zookeeper = ZKMgr.getZookeeper();
			JSONArray data = zookeeper.getJSONArray(id, false, "checked.json");
			for(int i = 0; i < data.length(); i++){
				JSONObject row = data.getJSONObject(i);
				row.put("modifytime", Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", row.getLong("_timestamp")));
			}
			dataJSON.put("totalRecords", data.length());
			dataJSON.put("curPage", 1);
			dataJSON.put("data", data);
		}
		catch(Exception e)
		{
			dataJSON.put("totalRecords", 0);
			dataJSON.put("curPage", 1);
		}
		return super.response(super.getResponse(), dataJSON.toString());
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
		String name = "未知模版";
		try
		{
			zookeeper = ZKMgr.getZookeeper();
			zkpath = id;
			String filename = id.substring(id.lastIndexOf("/")+1);
			JSONObject template = this.diggConfigMgr.getTemplate(zkpath, filename);
			if( template == null ) return null;
			name = template.getString("name");
			JSONArray versions = new JSONArray();
			zookeeper.getJSONArray(zkpath, versions, false, false);
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
				
				String time = version.getString("time");//dd/MM/yyyy
				args = Tools.split(time, "/");
				Calendar calendar = Calendar.getInstance();
				calendar.setTimeInMillis(0);
				calendar.set(Integer.parseInt(args[2]), Integer.parseInt(args[1])-1, Integer.parseInt(args[0]));
				version.put("startDate", time);
				version.put("headline", "v"+v);
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
			text.append("<span class='title'>"+(template.has("title")?template.getString("title"):"N/A")+"</span><br/>");
//			text.insert(0, "<span class='version'>v"+timeline.getString("version")+"</span>");
			text.append(template.has("remark")?template.getString("remark"):"");
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
	 * 预览grid样式
	 * @return
	 */
	public String demo()
	{
		log.info("preview the template(demo) of "+gridxml);
		this.workmode = TemplateChecker.DEMO;
		String rsp = grid(gridxml);
		this.localData = "[]";
		this.showTitle = true;
		return rsp;
	}
	
	/**
	 * 调测
	 * @return
	 */
	public String debug()
	{
		long ts = System.currentTimeMillis();
		try 
		{
			this.workmode = TemplateChecker.DEBUG;
    		InputStream is = this.getGridXmlInputStream();
        	this.parse(is);
        	String type = this.filetype;
        	if( "report".equalsIgnoreCase(type) ) type = "query";
			log.info("Debug '"+type+"' template of "+gridxml+" spent "+(System.currentTimeMillis()-ts)+" ms.");
	        return "grid"+type;
		}
		catch (Exception e) 
		{
			checker.err();
			test(1, "解析元数据查询配置模板发生<span style='color:#ff3300;font-size:14px;font-weight:bold;'>【致命】</span>异常， 请检查模板XML格式是否正确。\r\n %s", e.getMessage());
			checker.write(e);
			String tips = "模板【"+id+"】测试没有发现问题。";
			if( checker.warcount()>0||checker.errcount()>0)
			{
				tips = "测试模板发现"+checker.errcount()+"个错误，"+checker.warcount()+"个改进提示。";
			}
			javascript += "document.getElementById('aaa').innerHTML = '"+tips+"';";
			if( checker.errcount() > 0 )
			{
				javascript += "document.getElementById('aaa').style.background = '#ff6633';";
			}
			else if( checker.warcount() > 0 )
			{
				javascript += "document.getElementById('aaa').style.background = '#ffff66';";
			}
			id = "xml";
			return debuglog();
		}
		finally{
    		this.getSession().setAttribute("xml.log", this.checker.payload());
			log.info("Save the log to session spent "+(System.currentTimeMillis()-ts)+" ms.");
		}
	}
	/**
	 * 批量测试所有模板
	 * @return
	 */
	public String batchtest()
	{
    	ServletOutputStream sos = null;
		String javascript = "";
        try
        {
        	this.gridtext = null;
			log.info("Batch test the template of "+sysid+".");
	    	ArrayList<String> gridxmls = new ArrayList<String>();
	    	if( sysid.equals("local") ){
	        	URL url = DiggConfigMgr.class.getClassLoader().getResource("/");
	        	File dir = new File(url.getFile(), "grid/local");
	        	File[] files = dir.listFiles();
	        	for(File f : files)
	        	{
	        		if(f.getName().endsWith(".xml")){
	        			gridxmls.add("/grid/local/"+f.getName());
	        		}
	        	}
	    	}
	    	else{
	    		diggConfigMgr.getAllTemplatePath("/cos/config/modules/"+sysid+"/digg", gridxmls);
	    	}
	    	this.workmode = TemplateChecker.TEST;
	    	Zookeeper zookeeper = ZKMgr.getZookeeper();
	    	JSONArray result = new JSONArray();
	    	for(String gridxml : gridxmls)
	    	{
	    		String checkpath = gridxml+"/checked.json";
		    	if( sysid.equals("local") ){
		    		checkpath = "/cos/config"+gridxml+"/checked.json";
		    	}
	    		JSONObject check = zookeeper.getJSONObject(checkpath);
	    		if( check == null || super.getRequest().getParameter("again") != null ){
	    			this.grid(gridxml);
	    			boolean created = check!= null;
	    			check = new JSONObject();
	    			check.put("path", gridxml);
	    			check.put("err", this.checker.errcount());
	    			check.put("war", this.checker.warcount());
	    			check.put("title", gridtitle);
	    			if( created ){
	    				zookeeper.setJSONObject(checkpath, check);
	    			}
	    			else{
	    				zookeeper.createNode(checkpath, check.toString().getBytes("UTF-8"));
	    			}
	    		}
    			result.put(check);
	    	}
	    	gridxmls.clear();
			getResponse().setContentType("text/html");
			getResponse().setCharacterEncoding("UTF-8");
			sos = getResponse().getOutputStream();
			sos.print("<html><body style='padding: 1px;font-size:9pt;background:#000;color:#fff;word-break:keep-all;white-space:pre;'>");
			sos.flush();
			javascript = "\r\nvar json = '"+result.toString()+"';";
//			javascript += "\r\nvar result = jQuery.parseJSON(json);";
			javascript += "\r\nparent.setCheckResult(json);";
//			sos.write(javascript.getBytes("UTF-8"));
        }
        catch(Exception e)
        {
        }
        finally
        {
        	if( sos != null )
        	try {
				sos.write(("</body>\r\n\r\n<script type='text/javascript'>"+javascript+"</script></html>").getBytes("UTF-8"));
				sos.flush();
				sos.close();
        	} catch (IOException e1) {
			}
        }
		return null;
	}
	

	/**
	 * 打开DEMO视图
	 * @return
	 */
	public String local()
	{
		developer = true;
		sysid = "local";
		sysname = "主界面框架系统";
		JSONObject localdir = new JSONObject();
		localDataArray = new JSONArray();
		localdir.put("name", "系统模板(用户可修改)");
		localdir.put("id", "local");
		localdir.put("type", "dir");
		localdir.put("rootdir", true);
		localdir.put("isParent", true);
		localdir.put("local", true);
		localdir.put("icon", "images/icons/wand_disabled.png");
		localdir.put("children", localDataArray);
		this.diggConfigMgr.loadLocalTemplates("", this.localDataArray, true, false);
		this.jsonData = "["+localdir.toString()+"]";
		return "explorer";
	}
	
	/**
	 * 得到查询目标
	 * @return
	 */
	public String api(){
	    try{
	    	this.grant = this.isSysadmin();
	        this.account = super.getUserAccount();
	        String zkpath = "/cos/config/modules/" + this.sysid;
	        JSONObject module = ZKMgr.getZookeeper().getJSONObject(zkpath);
	        if (module == null)
	        {
	            throw new Exception("未发现模块子系统(" + this.sysid + ")");
	        }
	        zkpath = zkpath + "/digg";
	        if (ZKMgr.getZookeeper().exists(zkpath) == null)
	        {
	            ZKMgr.getZookeeper().create(zkpath, "元数据配置管理".getBytes("UTF-8"));
	        }
	        this.sysname = module.getString("SysName");
	        
	        this.localDataArray = new JSONArray();
	        JSONObject diggcfgs = this.diggConfigMgr.getApiTemplates(zkpath);
	        diggcfgs.put("cname", "【"+sysname+"】");
	        localDataArray.put(diggcfgs);
	        if( "Sys".equalsIgnoreCase(sysid)){
	        	localDataArray.put(this.diggConfigMgr.getLocalApiTemplates());
	        }
	        this.jsonData = localDataArray.toString();
//	      log.info(diggcfgs.toString(4));
	    }
	    catch (Exception e)
	    {
	      log.error("Failed to open the explorer of digg for ", e);
	      super.setResponseException("打开元数据模板接口开发界面失败，因为" + e);
	    }
	    return "api";
	}
	/**
	 * DIGG的数据样式
	 * @return
	 */
	public String datastyle(){
		InputStream is = null;
		try 
		{
			is = this.getGridXmlInputStream();
			super.parse(is);
			messageCode = "json";
//			System.err.println(dataStyle.toString(4));
			this.setEditorContent(this.dataStyle.toString(4));
		}
		catch (Exception e) 
		{
			dataStyle = new JSONObject();
			dataStyle.put("error", e.getMessage());
		}
        finally
        {
        	this.setEditorContent(this.dataStyle.toString(4));
			if( is != null )
				try {
					is.close();
				} catch (IOException e) {
				}
        }
		return "xml_json_css_sql";	
	}
	/**
	 * 元数据模板开发的资源管理器
	 * @return
	 */
	public String explorer()
	{
		try
		{
			developer = "1".equals(super.getRequest().getParameter("mode"));//mode参数为1的时候为开发者模式
			this.account = super.getUserAccount();
			String zkpath = "/cos/config/modules/"+sysid;
			JSONObject module = ZKMgr.getZookeeper().getJSONObject(zkpath);
			if( module == null )
			{
				throw new Exception("未发现模块子系统("+sysid+")");
			}
			zkpath += "/digg";
			if( ZKMgr.getZookeeper().exists(zkpath) == null )
			{
				ZKMgr.getZookeeper().create(zkpath, "元数据配置管理".getBytes("UTF-8"));
			}
			this.sysname = module.getString("SysName");
	    	JSONObject diggcfgs = diggConfigMgr.getTemplates(zkpath, true);
			if( developer )
			{
		    	JSONObject datasourcecfgs = new JSONObject();
				datasourcecfgs.put("id", "datasourcecfgs");
				datasourcecfgs.put("name", "数据源查询");
				datasourcecfgs.put("cname", "数据源查询");
				datasourcecfgs.put("isParent", true);
//				datasourcecfgs.put("icon", "images/icons/tile.png");
				datasourcecfgs.put("icon", "images/ico/datasource.png");

		    	JSONObject helpcfgs = new JSONObject();
		    	helpcfgs.put("id", "helpcfgs");
		    	helpcfgs.put("type", "help");
		    	helpcfgs.put("name", "开发帮助");
		    	helpcfgs.put("cname", "开发帮助");
		    	helpcfgs.put("isParent", true);
		    	helpcfgs.put("icon", "images/icons/wand.png");
		    	helpcfgs.put("isParent", true);
				JSONArray children = new JSONArray();
				helpcfgs.put("children", children);

				JSONObject e = new JSONObject();
				e.put("name", "表格组件 ParamQuery Grid");
				e.put("cname", "ParamQuery Grid");
				e.put("id", "ParamQuery");
		    	e.put("icon", "images/icons/tile.png");
				e.put("type", "help");
				e.put("isParent", false);
				e.put("url", "https://paramquery.com/api#method-getRowData");
				children.put(e);
				e = new JSONObject();
				e.put("name", "文件组件 File-Input");
				e.put("cname", "File-Input");
				e.put("id", "FileInput");
				e.put("icon", "images/icons/tile.png");
				e.put("type", "help");
				e.put("isParent", false);
				e.put("url", "https://plugins.krajee.com/file-input");
				children.put(e);
				e = new JSONObject();
				e.put("name", "富文本组件 UEditor");
				e.put("cname", "UEditor");
				e.put("id", "UEditor");
		    	e.put("icon", "images/icons/tile.png");
				e.put("type", "help");
				e.put("isParent", false);
				e.put("url", "http://ueditor.baidu.com/doc/#UE.Editor");
				children.put(e);
				this.jsonData = "["+diggcfgs+", "+datasourcecfgs+", "+helpcfgs+"]";
			}
			else{
				this.jsonData = "["+diggcfgs+"]";
			}
			log.debug(this.jsonData);
		}
		catch(Exception e)
		{
			log.error("Failed to open the explorer of digg for ", e);
			super.setResponseException("打开元数据模板开发界面失败，因为"+e);
		}
		return "explorer";
	}

	/**
	 * 设置创建字段的空返回
	 * @return
	 */
	public String setgridcell()
	{
		StringBuffer sb = new StringBuffer();
		sb.append("Receive the request, the parameters of below ");
		HttpServletRequest req = super.getRequest();
		Iterator<Map.Entry<String, String[]>> iterator = req.getParameterMap().entrySet().iterator();
		while(iterator.hasNext())
		{
			Map.Entry<String, String[]> e = iterator.next();
			sb.append("\r\n\t");
			sb.append(e.getKey());
			sb.append("=");
			for(String value : e.getValue())
				sb.append(value+"\t");
		}
		log.info(sb.toString());
		JSONObject result = new JSONObject();
		result.put("recId", req.getParameter("dataIndx"));
		return response(this.getResponse(), result.toString());
	}
	/**
	 * 设置创建字段的空返回
	 * @return
	 */
	public String setprecreate()
	{
		JSONObject result = new JSONObject();
		result.put("recId", "");
		return response(this.getResponse(), result.toString());
	}
	/**
	 * 打开预加载
	 * @return
	 */
	public String openprecreate()
	{
		this.localDataArray = new JSONArray();
		return grid("/grid/local/diggprecreatequery.xml");
	}
	/**
	 * 创建元数据查询配置
	 * @return
	 */
	public String precreate()
	{
		try
		{
			log.info("Precreate tempalte("+filetype+") from "+id+" or "+gridxml+"(datasource="+db+"."+this.datatype+").");
			String zkpath = "/cos/config/modules/"+sysid;
			JSONObject module = ZKMgr.getZookeeper().getJSONObject(zkpath);
			if( module == null )
			{
				throw new Exception("未发现模块子系统("+sysid+")");
			}
			zkpath += "/digg";
			if( id == null || id.isEmpty() )
			{
				if( gridxml == null || gridxml.isEmpty() )
				{
					id = zkpath;
				}
				else if( gridxml.endsWith(".xml") ) 
				{
	        		int i = gridxml.lastIndexOf("/");
	        		id = gridxml.substring(0, i);
				}
				else
				{
					id = gridxml;
				}
			}
			JSONObject diggcfgs = diggConfigMgr.getTemplates(zkpath, false);
			this.jsonData = "["+diggcfgs+"]";
			ArrayList<Item> list = new ArrayList<Item>();
			zkpath = "/cos/config/modules/"+sysid+"/datasource";
			JSONArray array = ZKMgr.getZookeeper().getJSONArray(zkpath, true);
			for(int i = 0; i < array.length(); i++)
			{
				JSONObject e= array.getJSONObject(i);
				Item item = new Item(e.getString("title"), zkpath+"/"+e.getString("name"));
				list.add(item);
			}
			this.listData = list;
//			System.err.println(diggcfgs.toString(4));
		}
		catch(Exception e)
		{
			log.error("Failed to open the config of digg for ", e);
			super.setResponseException("打开元数据配置管理界面失败，因为"+e);
		}
		return "precreate";
	}
	/**
	 * 列用表字段创建元数据查询
	 * @return
	 */
	public String createdigg()
	{
		log.info("Open the digg-creator to "+sysid+"."+this.db+"."+datatype);
		Zookeeper zookeeper = null;
		Connection connection = null;
		ResultSet rs = null;
		Statement statement = null;
		this.gridtext = null;
		try
		{
			zookeeper = ZKMgr.getZookeeper();
			JSONObject digg = new JSONObject();
			String tablename = datatype;
			presetDatabase("/cos/config/modules/"+sysid+"/datasource/"+this.db, digg);
            Class.forName(driverClass); 
            connection = DriverManager.getConnection(jdbcUrl ,jdbcUsername, jdbcUserpswd);  
			rs = connection.getMetaData().getColumns( null, "%", tablename, "%");

			JSONObject remarks = zookeeper.getJSONObject("/cos/config/modules/"+sysid+"/datasource/"+this.db+"/"+tablename, false);
			if( remarks == null )
			{
				remarks = new JSONObject();
			}
			this.localDataArray = new JSONArray();
			while(rs.next())
			{
            	JSONObject column = new JSONObject();
            	String id = rs.getString("COLUMN_NAME");
            	column.put("COLUMN_NAME", id);
				String TYPE_NAME = rs.getString("TYPE_NAME");
				if( TYPE_NAME != null )
				{
					column.put("TYPE_NAME", TYPE_NAME);
				}
				column.put("DATA_TYPE", rs.getInt("DATA_TYPE"));
				column.put("COLUMN_SIZE", rs.getInt("COLUMN_SIZE"));
				column.put("NULLABLE", rs.getInt("NULLABLE")==1?true:false);
				column.put("IS_AUTOINCREMENT", rs.getString("IS_AUTOINCREMENT"));
				column.put("REMARKS", remarks.has(id)?remarks.getString(id):"");
				String COLUMN_DEF = rs.getString("COLUMN_DEF");
				if( COLUMN_DEF != null )
				{
					column.put("COLUMN_DEF", COLUMN_DEF);
				}
				localDataArray.put(column);
			}
			this.localData = localDataArray.toString();
			return super.grid("/grid/local/diggcreator.xml");
		}
		catch(Exception e)
		{
			super.setResponseException("打开元数据查询创建界面失败，因为异常"+e.getMessage());
			log.error("", e);
			return "alert";
		}
        finally
        {
			if( statement != null )
				try
				{
					statement.close();
				}
				catch (SQLException e1)
				{
				}
        	if( rs != null )
				try
				{
					rs.close();
				}
				catch (SQLException e1)
				{
				}
        	if( connection != null )
				try
				{
					connection.close();
				}
				catch (SQLException e1)
				{
				}
        }
	}
	
	/**
	 * 表字段备注
	 * @return
	 */
	public String remarkcells()
	{
		log.info("Remark the cells to "+sysid+"."+this.db+"."+datatype);
		Zookeeper zookeeper = null;
		Connection connection = null;
		ResultSet rs = null;
		Statement statement = null;
		this.gridtext = null;
		try
		{
			zookeeper = ZKMgr.getZookeeper();
			JSONObject digg = new JSONObject();
			String tablename = datatype;
			presetDatabase("/cos/config/modules/"+sysid+"/datasource/"+this.db, digg);
            Class.forName(driverClass); 
            connection = DriverManager.getConnection(jdbcUrl ,jdbcUsername, jdbcUserpswd);  
			rs = connection.getMetaData().getColumns( null, "%", tablename, "%");

			JSONObject remarks = zookeeper.getJSONObject("/cos/config/modules/"+sysid+"/datasource/"+this.db+"/"+tablename, false);
			if( remarks == null )
			{
				remarks = new JSONObject();
			}
			this.localDataArray = new JSONArray();
			while(rs.next())
			{
            	JSONObject column = new JSONObject();
            	String id = rs.getString("COLUMN_NAME");
            	column.put("COLUMN_NAME", id);
				String TYPE_NAME = rs.getString("TYPE_NAME");
				if( TYPE_NAME != null )
				{
					column.put("TYPE_NAME", TYPE_NAME);
				}
				column.put("DATA_TYPE", rs.getInt("DATA_TYPE"));
				column.put("COLUMN_SIZE", rs.getInt("COLUMN_SIZE"));
				column.put("NULLABLE", rs.getInt("NULLABLE")==1?true:false);
				column.put("IS_AUTOINCREMENT", rs.getString("IS_AUTOINCREMENT"));
				column.put("REMARKS", remarks.has(id)?remarks.getString(id):"");
				String COLUMN_DEF = rs.getString("COLUMN_DEF");
				if( COLUMN_DEF != null )
				{
					column.put("COLUMN_DEF", COLUMN_DEF);
				}
				localDataArray.put(column);
			}
			this.localData = localDataArray.toString();
			return super.grid("/grid/local/diggremarkcells.xml");
		}
		catch(Exception e)
		{
			super.setResponseException("打开元数据源字段备注界面失败，因为异常"+e.getMessage());
			log.error("Failed to open the view of remark-cells for exception ", e);
			return "alert";
		}
        finally
        {
			if( statement != null )
				try
				{
					statement.close();
				}
				catch (SQLException e1)
				{
				}
        	if( rs != null )
				try
				{
					rs.close();
				}
				catch (SQLException e1)
				{
				}
        	if( connection != null )
				try
				{
					connection.close();
				}
				catch (SQLException e1)
				{
				}
        }
	}
	
	/**
	 * 导入DiggCells
	 * @return
	 */
	public String importcells()
	{
		
		return "importcells";
	}
	/**
	 * 配置一个元数据查询
	 * @return
	 */
	public String preset()
	{
		log.info("Open the editor of digg "+gridxml);
		XMLParser xml = null;
		JSONObject x = new JSONObject();
		x.put("id", "x");
		x.put("name", "元数据查询模板");
		x.put("icon", "images/icons/properties.png");
		x.put("open", true);
		x.put("sysid", sysid);
		try
		{
			InputStream is = new ByteArrayInputStream(ZKMgr.getZookeeper().getData(gridxml));
			xml = new XMLParser(is);
			JSONArray templates = this.diggConfigMgr.loadTemplateTree(xml);
			//############################################
			JSONObject javascript = new JSONObject();
			javascript.put("id", "javascript");
			javascript.put("name", "[javascript]用户自定义脚本");
			javascript.put("icon", "images/icons/documents.png");
			templates.put(javascript);
			Element javascriptNode = XMLParser.getChildElementByTag( xml.getRootNode(), "javascript" );
			if( javascriptNode != null )
				javascript.put("cdata", Kit.chr2Unicode(XMLParser.getCData(javascriptNode)));
			//############################################
			JSONObject globalscript = new JSONObject();
			globalscript.put("id", "globalscript");
			globalscript.put("name", "[globalscript]全局自定义脚本");
			globalscript.put("icon", "images/icons/documents.png");
			templates.put(globalscript);
			Element globalscriptNode = XMLParser.getChildElementByTag( xml.getRootNode(), "globalscript" );
			if( globalscriptNode != null )
				globalscript.put("cdata", Kit.chr2Unicode(XMLParser.getCData(globalscriptNode)));

			x.put("children", templates);
			this.jsonData = "["+x.toString()+"]";
			ArrayList<Item> list = new ArrayList<Item>();
			String zkpath = "/cos/config/modules/"+sysid+"/datasource";
//			System.err.print(zkpath);
			JSONArray array = ZKMgr.getZookeeper().getJSONArray(zkpath, true);
			for(int i = 0; i < array.length(); i++)
			{
				JSONObject e= array.getJSONObject(i);
				Item item = new Item(e.getString("title"), zkpath+"/"+e.getString("name"));
				list.add(item);
			}
			this.listData = list;
			return "preset";
		}
		catch(Exception e)
		{
			log.error("", e);
			this.responseException = "打开元数据编辑器出现异常:"+e.getMessage();
			return "close";
		}
	}
	/**
	 * 接收界面传过来的json对象
	 * @return
	 */
	public String previewgrid()
	{
		log.info("Open the preview of digg "+gridxml);
		XMLParser xml = null;
		try
		{
			InputStream is = getGridXmlInputStream();//new ByteArrayInputStream(ZKMgr.getZookeeper().getData(gridxml));
			xml = new XMLParser(is);
			JSONObject grid = new JSONObject();
			grid.put("id", "grid");
			grid.put("name", "[grid]列表字段");
			grid.put("icon", "images/icons/new_item.png");
			Element gridNode = XMLParser.getChildElementByTag( xml.getRootNode(), "grid" );
			diggConfigMgr.loadTemplateGrid(gridNode, grid);
			if( grid.has("children") ){
				this.localDataArray = grid.getJSONArray("children");
			}
			return grid("/grid/local/diggpreviewgrid.xml");
		}
		catch(Exception e)
		{
			log.error("", e);
			this.responseException = "打开元数据编辑器出现异常:"+e.getMessage();
			return "alert";
		}
	}
	/**
	 * 接收界面传过来的json对象
	 * @return
	 */
	public String presetgrid()
	{
		JSONObject grid = new JSONObject(gridtext);
//		System.out.println(grid.toString(4));
		this.localDataArray = grid.getJSONArray("children");
		return grid("/grid/local/diggpresetquery.xml");
	}
	/**
	 * 配置元数据
	 * @return
	 */
	public String datasource()
	{
		try 
		{
			log.info("Open the view of datasource for "+sysid);
			String path = "/cos/config/modules/"+sysid+"/datasource";
			Stat stat = ZKMgr.getZookeeper().exists(path);
			if( stat == null )
			{
				ZKMgr.getZookeeper().create(path, ("系统【"+sysid+"】配置数据源").getBytes("UTF-8"));
			}
			this.gridtext = "";
			String rsp = super.grid("/grid/local/diggdatasource.xml");
			if( this.localDataArray != null )
			{
				for(int i = 0; i < this.localDataArray.length(); i++ )
				{
					JSONObject ds = this.localDataArray.getJSONObject(i);
//					System.err.println(Base64X.decode2str(ds.getString("dbpassword")));
					if( ds.has("test") ) ds.remove("test");
				}
				this.localData = localDataArray.toString();
			}
			return rsp;
		}
		catch (Exception e) 
		{
			super.setResponseException("初始化数据源配置界面失败，因为异常"+e);
			log.info("Failed to initialize the view of datasource for exception ", e);
			return "alert";
		}
	}

	/**
	 * 返回用于Grid显示的数据
	 * @return
	 */
	/**
	 * 
	 * @return
	 */
	public String demodata()
	{
		Exception err = null;
		InputStream is;
		try
		{
            log.info("Get the data of demo for "+gridxml);
			if( gridxml.startsWith("/grid/local") )
    		{
				is = this.getClass().getResourceAsStream(gridxml);
    		}
			else
			{
				is = new ByteArrayInputStream(ZKMgr.getZookeeper().getData(gridxml));
			}
			XMLParser xml = new XMLParser(is);
			Node demodataNode = XMLParser.getChildElementByTag(xml.getRootNode(), "demodata");
			if( demodataNode != null ){
				String demodata = XMLParser.getCData(demodataNode);
				if(!demodata.isEmpty() && demodata.startsWith("[") )
					this.localDataArray = new JSONArray(demodata);
			}
		}
		catch(Exception e)
		{
			log.error("", e);
			err = new Exception("模板配置的DEMO数据错误无法显示该模板的样本数据");
		}
		return querydata(localDataArray.length(), 1, this.localDataArray, err);
	}
	public void setDiggConfigMgr(DiggConfigMgr diggConfigMgr) {
		this.diggConfigMgr = diggConfigMgr;
	}

	public void setDiggMgr(DiggMgr diggMgr) {
		this.diggMgr = diggMgr;
	}
	public void setGridtext(String gridtext) {
		this.gridtext = gridtext;
	}

	public String getGridtext() {
		return gridtext;
	}
}
