package com.focus.cos.web.login.service;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.Key;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.crypto.Cipher;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.directwebremoting.WebContextFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Node;

import com.focus.cos.control.WrapperShell;
import com.focus.cos.web.Version;
import com.focus.cos.web.common.AjaxResult;
import com.focus.cos.web.common.Kit;
import com.focus.cos.web.common.PathFactory;
import com.focus.cos.web.common.ZKMgr;
import com.focus.cos.web.dev.service.MenusMgr;
import com.focus.cos.web.dev.service.ModulesMgr;
import com.focus.cos.web.login.vo.Permission;
import com.focus.cos.web.login.vo.PermissionAction;
import com.focus.cos.web.user.dao.UserDAO;
import com.focus.cos.web.user.service.RoleMgr;
import com.focus.cos.web.user.vo.User;
import com.focus.cos.web.util.Tools4i18n;
import com.focus.skit.KComponent;
import com.focus.skit.menu.KMenu;
import com.focus.skit.menu.KMenuItem;
import com.focus.skit.tree.KAction;
import com.focus.skit.tree.KActionItem;
import com.focus.skit.tree.KTree;
import com.focus.skit.tree.KTreeItem;
import com.focus.util.IOHelper;
import com.focus.util.Tools;
import com.focus.util.XMLParser;

public class Login implements java.io.Serializable
{
	private static final long serialVersionUID = 3661781114851498746L;

	private static final Log log = LogFactory.getLog(Login.class);
	
	public static final int ROLE_ROOT = 1;
	
	private UserDAO userDao;
	
	private ArrayList<String> skinIcons = new ArrayList<String>();

	//动态显示top页面的系统名称、软件名称等信息
//	private JSONObject sysConfig;
	//系统软件配置说明
//	private JSONObject softwareConfig;
//	private String systemLogo;//软件LOGO
//	private String systemName;//系统名称
//	private String softwareId;//软件标识
//	private String softwareVersion;//软件标识
	/**
	 * 得到随机的图标
	 * @return
	 */
	public String getIcon()
	{
		if( skinIcons.isEmpty() )
		{
	    	URL url = Login.class.getClassLoader().getResource("/");
	    	if( url != null )
	    	{
	    		File file = new File(url.getFile(), "../../skin/defone/css/font-awesome.css");
	            BufferedReader reader = null;  
	            try 
	            {  
	    	        // 定义BufferedReader输入流来读取URL的响应  
	    	        reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));  
	    	        String line = null;  
	    	        while ((line = reader.readLine()) != null) 
	    	        {  
	    	        	int i = line.indexOf(":before");
	    	        	if( line.startsWith(".fa-") && i != -1 )
	    	        	{
	    	        		skinIcons.add(line.substring(1, i));
	    	        	}
	    	        } 
	    	    }
	            catch (IOException e) 
	            { 
	            	log.error("Failed to list fa", e);
	            } 
	            finally
	            {  
	    	        if(reader!=null)
	    	        {  
	    	        	try
						{
							reader.close();
						}
						catch (IOException e)
						{
						}  
	    	        }  
	            }  
	    	}
		}

		final Random random = new Random();
        return this.skinIcons.get(random.nextInt(skinIcons.size()));
	}
	
	public boolean checkLogin(String pwd,String workId,String dmkxdev)
	{
		if( userDao == null ) return true;
		return userDao.checkLogin(pwd, workId, dmkxdev);
	}

	public void update(User user)
	{
		if( userDao == null )
		{
			return;
		}
		userDao.attachDirty(user);
	}
	
	public User findByAccount(String account)
		throws Exception
	{
		return userDao!=null?(User)userDao.findByAccount(account):null; 
	}

	public void setUserDao(UserDAO userDao)
	{
		this.userDao = userDao;
	}
	
	/**
	 * 对路径进行解析修正
	 * @param src
	 * @return
	 */
	public String src(String src, HttpServletRequest request)
	{
		if( src == null || src.isEmpty() )
		{
			return "";
		}
        int len = src.length();
        StringBuffer sbMatch = null;
        StringBuffer sbCmd = new StringBuffer();
        for( int i = 0; i < len; i++ )
        {
            char c = src.charAt( i );
            if( c == '%' )
            {
                if( sbMatch == null )
                {
                    sbMatch = new StringBuffer();
                }
                else
                {
                    String property = System.getProperty( sbMatch.toString() );
                    if( property == null )
                    {
                    	property = "";
                    }
                    if( !property.startsWith("http://") && request != null )
                    {
                    	sbCmd.append( Kit.URL(request) + property );
                    }
                    else
                    {
                    	sbCmd.append( property );
                    }
                    sbMatch = null;
                }
            }
            else if( sbMatch == null )
            {
                sbCmd.append( c );
            }
            else if( sbMatch != null )
            {
                sbMatch.append( c );
            }
        }
        /*
        String cmd = sbCmd.toString();
        int k = cmd.lastIndexOf('?');
        if( k != -1)
        {
        	String params = cmd.substring(k+1);
        	try
        	{
//        		boolean c2u = false;
	        	String args[] = params.split("&");
	        	if( args != null )
	        	{
	        		for(String arg : args)
	        		{
	        			String kv[] = arg.split("=");
	        			if( kv != null && kv.length == 2)
	        			{
	        				String v = kv[1];
	        				for( int i = 0; i < v.length(); i++ )
	        				{
	        					if( v.charAt(i) > 128 )
	        					{//疑似中文
	        						k = sbCmd.indexOf(v);
	        						sbCmd.replace(k, k+v.length(), com.focus.cos.web.util.Tools.chr2Unicode(v));
	        						sbCmd.insert(k, "~");
//	        						c2u = true;
	        						break;
	        					}
	        				}
	        			}
	        		}
	        	}
//	        	if( c2u )
//	        	{
//	        		System.out.println(sbCmd);
//	        	}
        	}
        	catch(Exception e)
        	{
//            	System.out.println(params);
            	e.printStackTrace();
        	}
        }
        */
		return sbCmd.toString();
	}
	/**
	 * 得到唯一的模块门户参数
	 * @param userName 指定用户
	 * @return
	 */
	public JSONObject getUniqModulePortal(User user, HttpServletRequest request)
		throws Exception
	{
		JSONObject role = null;
        //Modify by liuxue below at 2011-8-31
        if( user != null )
        {
	        //Modify by liuxue below at 2011-3-21
        	//Delete by liuxue below at 2017-2-3
//			File fileRole = new File(PathFactory.getCfgPath(), "role/"+user.getUsername() );
//			if( !fileRole.exists() && user != null )
//			{
//				fileRole = new File(PathFactory.getCfgPath(), "role/"+user.getRoleid() );
//			}
//			if( fileRole.exists() )
//			{
//				role = (Role)IOHelper.readSerializableNoException(fileRole);
//			}
        	role = RoleMgr.getRolePrivileges(user.getRoleid(), user.getUsername());
        }
        JSONObject profile = null;
		List<JSONObject> list = ModulesMgr.getConfigs();
        if(list.size()==1)
        {
        	profile = list.get(0);
        }
        
		if( profile != null )
		{
			boolean isPortalNavigate = profile.has("isPortalNavigate")?profile.getBoolean("isPortalNavigate"):false;
			String portalUrl = profile.has("portalUrl")?profile.getString("portalUrl"):"";
			if( portalUrl.isEmpty() || portalUrl.startsWith("login!open.action") )
			{
//				File conf = new File(PathFactory.getCfgPath(),"config.properties");
//				Configuration config = new PropertiesConfiguration(conf);   
//				String locale = config.getString("emasys.locale");
//				File file = new File(PathFactory.getCfgPath(), "modules.xml");
//				if(locale != null && !locale.isEmpty() )
//				{
//					if(locale.indexOf("en_US") != -1)
//					{
//						File file1 = new File(PathFactory.getCfgPath(), "module_en_US.xml");
//						file = file1.exists()?file1:file;
//					}
//					else if(locale.indexOf("zh_CN") != -1)
//					{
//						File file1 = new File(PathFactory.getCfgPath(), "module_zh_CN.xml");
//						file = file1.exists()?file1:file;
//					}
//				}
//				log.debug("Ready to parse modules.xml:"+file.getPath()+":"+file.exists());
//				if( file.exists() )
//				{
			        String moduleId = profile.getString("id");
			        XMLParser parser = new XMLParser( MenusMgr.getModulesXml() );
			        Node moduleNode = XMLParser.getElementByTag( parser.getRootNode(), "module" );
			        for( ; moduleNode != null; moduleNode = moduleNode.getNextSibling() )
			        {
			            if( !moduleNode.getNodeName().equalsIgnoreCase( "module" ) )
			            {
			                continue;
			            }
			            String id = XMLParser.getElementAttr( moduleNode, "id" );
			            if( id == null || !id.equals( moduleId ) )
			            {
			                continue;
			            }
			            isPortalNavigate = true;
			            Node summaryNode = XMLParser.getElementByTag( moduleNode, "summary" );
			            //if( summaryNode != null && "focus".equals(user.getUsername()) )
			            if( summaryNode != null )
			            {
			            	String str = XMLParser.getElementAttr(summaryNode, "src");
			            	if( str != null && !str.isEmpty() )
			            	{
			        			if( portalUrl.isEmpty() ) portalUrl = src(str, request);
			            	}
			            	else
			            	{
			            		// 如果有summary字段，但是没有指出src
			            		String url = findFirstPrivateUrl(moduleNode, role);
			            		if(url != null)
			            		{
			            			portalUrl = src(url, request);
			            		}
			            	}
			            }
			        }
//				}
			}
			else
			{
				portalUrl = src(portalUrl, request);
			}
			profile.put("portalUrl", portalUrl);
			profile.put("isPortalNavigate", isPortalNavigate);
		}
		return profile;
	}
	
	/**
	 * 根据用户角色信息找到满足权限要求的第一个菜单url
	 * @param moduleNode
	 * @param role
	 * @param id
	 * @return
	 */
	private String findFirstPrivateUrl(Node moduleNode, JSONObject role)
	{
		String id = XMLParser.getElementAttr( moduleNode, "id" );
		String url = null;
		Node navigationNode = XMLParser.getElementByTag( moduleNode, "navigation" );
		Node menuNode = XMLParser.getFirstChildElement(navigationNode);
		while(menuNode != null)
		{
			if(!"menu".equals(menuNode.getNodeName()))
			{
				menuNode = XMLParser.nextSibling(menuNode);
				continue;
			}
			
			String menuId = XMLParser.getElementAttr(menuNode, "id");
			String href = XMLParser.getElementAttr(menuNode, "href");
			if(href != null)
			{
				if(role == null)
				{
					if(href.length() > 1)
					{
						url = href;
						break;
					}
				}
				else if(role.has(id + "." + href))
				{
					url = href;
					break;
				}
				else if(!role.has(id + "." + menuId))
				{
					// 本节点都没有权限，那么下级节点也不会有权限
					menuNode = XMLParser.nextSibling(menuNode);
					continue;
				}
			}
			
			Node subMenuNode = XMLParser.getFirstChildElement(menuNode);
			while(subMenuNode != null)
			{
				if(!"menu".equals(subMenuNode.getNodeName()))
    			{
					subMenuNode = XMLParser.nextSibling(subMenuNode);
    				continue;
    			}
				
				href = XMLParser.getElementAttr(subMenuNode, "href");
    			if(href != null)
    			{
    				if(role == null)
    				{
    					if(href.length() > 1)
    					{
    						url = href;
    						break;
    					}
    				}
    				else if(role.has(id + "." + menuId + "." + href))
    				{
    					url = href;
    					break;
    				}
    			}
    			
				subMenuNode = XMLParser.nextSibling(subMenuNode);
			}
			
			if(url != null)
			{
				break;
			}
			menuNode = XMLParser.nextSibling(menuNode);
		}
		
		return url;
	}

	public UserDAO getUserDao()
	{
		return userDao;
	}
	
	/**
	 *  返回登录相关信息，包括导航菜单
	 */
	@Deprecated
	public AjaxResult<String> getLoginInfo()
	{
		AjaxResult<String> result = new AjaxResult<String>();
		JSONObject response = new JSONObject();
		try
		{
			org.directwebremoting.WebContext web = WebContextFactory.get();   
		    javax.servlet.http.HttpServletRequest request = web.getHttpServletRequest();
		    JSONObject user = (JSONObject)request.getSession().getAttribute("account");
			if( user == null )
			{
				result.setMessage("用户未登录或者会话超时过期，请重新登录。");
				return result;
			}
			response.put("loginTitle", user.getString("realname"));
			KMenu toolbars = new KMenu();
			ArrayList<KTree> trees = new ArrayList<KTree>();
			ArrayList<Permission> permissions = new ArrayList<Permission>();
			JSONObject token = new JSONObject();
			this.loadNavigate(toolbars, trees, permissions, request, user, token, false);
			this.setToken(user.getString("token"), token);
			response.put("version", Version.getValue());
			JSONObject buttons = new JSONObject();//按钮权限
			for(Permission p : permissions)
			{
				for(PermissionAction a : p.getActions())
					buttons.put(a.getActionId(), a.isHasPermission());
			}
			response.put("permissions", buttons);
			//装载导航菜单数据到对象节点
			JSONObject menus = new JSONObject();
			for(KTree tree: trees)
			{
				JSONArray array = new JSONArray();
				this.loadMenu(array, tree);
				menus.put(tree.getId(), array);
				//加载action
				for(KAction a : tree.getActions())
				{
					array = new JSONArray();;
					for (int i = 0; i < a.getComponentCount(); i++)
					{
						KActionItem e = (KActionItem)a.getComponent(i);
						JSONObject action = new JSONObject();
						action.put("id", e.getId());
						action.put("name", e.getLabel());
						action.put("href", e.getViewHref());
						action.put("icon", e.getIcon());
						array.put(action);
					}
					menus.put(a.getSuid(), array);
				}
			}
			response.put("menus", menus);
			StringBuffer toolbar = new StringBuffer();
			toolbar.append("<table cellspacing='0' cellpadding='0'>");
			toolbar.append("<tr><td class='be093'/>");
			for (int i = 0; i < toolbars.getComponentCount(); i++)
			{
				KMenuItem e = (KMenuItem)toolbars.getComponent(i);
				String onclick = e.getHref();
				if( !onclick.equals("#") )
				{
					onclick = "openView(\""+e.getLabel()+"\", \""+onclick+"\")";
				}
				toolbar.append("<td id='"+e.getSuid()+"' class='skit_menu_item' onclick='"+onclick+"' onmouseover='skitModuleOver(this)' onmouseout='skitModuleOut(this)'>");
				toolbar.append("<i class='skit_fa_btn fa "+e.getIcon()+"'></i>"+e.getLabel()+"</td>");
				toolbar.append("<td class='be097'/>");
			}
			toolbar.append("<td id='toolbar.back' class='skit_menu_item' onclick='openStaticPanel(\"back\", this)' onmouseover='skitModuleOver(this)' onmouseout='skitModuleOut(this)'>");
			toolbar.append("<i class='skit_fa_btn fa fa-arrow-circle-left'></i>后退</td>");
			toolbar.append("<td class='be097'/>");
			toolbar.append("<td id='toolbar.forward' class='skit_menu_item' onclick='openStaticPanel(\"go\", this)' onmouseover='skitModuleOver(this)' onmouseout='skitModuleOut(this)'>");
			toolbar.append("<i class='skit_fa_btn fa fa-arrow-circle-right'></i>前进</td>");
			toolbar.append("<td class='be097'/>");
			toolbar.append("<td id='toolbar.reload' class='skit_menu_item' onclick='openStaticPanel(\"reload\", this)' onmouseover='skitModuleOver(this)' onmouseout='skitModuleOut(this)'>");
			toolbar.append("<i class='skit_fa_btn fa fa-refresh'></i>刷新</td>");
			toolbar.append("<td class='be097'/>");
			toolbar.append("<td id='toolbar.notify' class='skit_menu_item' onclick='openView(\"系统通知\", \"notify!manager.action\")' onmouseover='skitModuleOver(this)' onmouseout='skitModuleOut(this)'>");
			toolbar.append("<i class='skit_fa_btn fa fa-inbox'></i>系统通知</td>");
			toolbar.append("<td class='be097'/>");
			toolbar.append("<td id='toolbar.password' class='skit_menu_item' onclick='openStaticPanel(\"password\", this)' onmouseover='skitModuleOver(this)' onmouseout='skitModuleOut(this)'>");
			toolbar.append("<i class='skit_fa_btn fa fa-user-secret'></i>修改密码</td>");
			toolbar.append("<td class='be097'/>");
			toolbar.append("<td id='toolbar.about' class='skit_menu_item' onclick='openStaticPanel(\"about\", this)' onmouseover='skitModuleOver(this)' onmouseout='skitModuleOut(this)'>");
			toolbar.append("<i class='skit_fa_btn fa fa-info-circle'></i>关于</td>");
			toolbar.append("<td class='be097'/>");
			toolbar.append("<td id='toolbar.home' class='skit_menu_item' onclick='openStaticPanel(\"home\", this)' onmouseover='skitModuleOver(this)' onmouseout='skitModuleOut(this)'>");
			toolbar.append("<i class='skit_fa_btn fa fa-home'></i>首页</td>");
			toolbar.append("<td class='be097'/>");
			toolbar.append("<td id='toolbar.navigate' class='skit_menu_item' onclick='openStaticPanel(\"navigate\", this)' onmouseover='skitModuleOver(this)' onmouseout='skitModuleOut(this)'>");
			toolbar.append("<i class='skit_fa_btn fa fa-navicon'></i>导航</td>");
			toolbar.append("<td class='be097'/>");
			toolbar.append("<td id='toolbar.notify' class='skit_menu_item' onclick='openView(\"系统通知\", \""+Kit.URL_PATH(request)+"jsp/developer/start.htm\")' onmouseover='skitModuleOver(this)' onmouseout='skitModuleOut(this)'>");
			toolbar.append("<i class='skit_fa_btn fa fa-support'></i>开发者手册</td>");
			toolbar.append("<td class='be097'/>");
			toolbar.append("<td id='toolbar.logout' class='skit_menu_item' onclick='openStaticPanel(\"exit\", this)' onmouseover='skitModuleOver(this)' onmouseout='skitModuleOut(this)'>");
			toolbar.append("<i class='skit_fa_btn fa fa-sign-out'></i>退出</td>");
			toolbar.append("</tr></table>");
			response.put("toolbars", toolbar.toString());
			result.setResult(response.toString());
			result.setSucceed(true);
		}
		catch(Exception e)
		{
			log.error("Failed to nagavite", e);
			result.setMessage(Tools4i18n.getI18nProperty("label.ema.moduleid.open.exception")+e.toString()+")！");
		}
		return result;
	}
	
	/**
	 * 加载菜单
	 * @param array
	 * @param node
	 */
	private void loadMenu(JSONArray array, KComponent node )
	{
		for(Object o : node)
		{
			KTreeItem e = (KTreeItem)o;
			JSONObject menu = new JSONObject();
			menu.put("id", e.getSuid());
			menu.put("name", e.getLabel());
			menu.put("href", e.getViewHref());
			menu.put("icon", e.getIcon());
			menu.put("iconSkin", "skit_fa_tree fa "+e.getIcon()+"");
			menu.put("pid", node.getSuid());
			if( e.getAction() != null )
			{
				menu.put("action", e.getAction().getSuid());
			}
			array.put(menu);
			this.loadMenu(array, e);
		}
	}

	/**
	 * 加载菜单
	 * @param array
	 * @param node
	private void loadAction(JSONArray array, KComponent node )
	{
		for(Object o : node)
		{
			KTreeItem e = (KTreeItem)o;
			JSONObject menu = new JSONObject();
			menu.put("id", e.getId());
			menu.put("name", e.getLabel());
			menu.put("href", e.getViewHref());
			menu.put("icon", e.getIcon());
			menu.put("pid", node.getId());
			array.put(menu);
			this.loadMenu(array, e);
		}
	}
	 */
	/**
	 * 设置令牌
	 */
	public void setToken(String id, JSONObject token)
	{
		//设置令牌
		ZooKeeper zookeeper = null;
		try
		{
			String path = "/cos/login/token";
			zookeeper = ZKMgr.getZooKeeper();
			Stat stat = zookeeper.exists(path, false); 
			if( stat == null)
			{
				zookeeper.create(path, "记录登录会话令牌的节点".getBytes("UTF-8"), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
			}
			path = "/cos/login/token/"+id;
			stat = zookeeper.exists(path, false); 
			if( stat == null)
				zookeeper.create(path, token.toString().getBytes("UTF-8"), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
			else
				zookeeper.setData(path, token.toString().getBytes("UTF-8"), stat.getVersion());
		}
		catch(Exception e)
		{
			log.error("Failed to set toke of "+id+" for exception"+e);
		}
//		finally
//		{
//			if( zookeeper != null )
//				try
//				{
//					zookeeper.close();
//				}
//				catch (InterruptedException e)
//				{
//				}
//		}
	}
	/**
	 * 加载所有导航数据
	 * @param toolbars
	 * @param trees
	 * @param permissions
	 * @param request
	 * @param user
	 * @param token
	 * @param newversion
	 */
	public void loadNavigate(KMenu toolbars,
							 ArrayList<KTree> trees,
							 ArrayList<Permission> permissions,
							 HttpServletRequest request,
							 JSONObject user,
							 JSONObject token,
							 boolean newversion)
	{
		try
		{
	        if( user == null )
	        {
				return;
	        }
	        user.put("head", "images/role/a1.png");
			JSONObject role = RoleMgr.getRolePrivileges(user.getInt("roleid"), user.getString("username"));
			if( role == null )
			{
				log.warn("Not found the build of role.");
				return;
			}
			user.put("rolename", role.has("name")?role.getString("name"):"");
			String headtag = "b";
			if( role.has("head") )
			{
				headtag = role.getString("head");
			}
			if( user.has("sex") )
			{
				user.put("head", "images/role/"+headtag+user.getInt("sex")+".png");
			}
			InputStream is = MenusMgr.getModulesXml();
			if( is == null ) return;
	        XMLParser parser = new XMLParser( is );
			Node toolbarNode = XMLParser.getElementByTag( parser.getRootNode(), "toolbar" );
			if( toolbarNode != null )
			{
				Node menuNode = XMLParser.getChildElementByTag( toolbarNode, "menu" );
				if( menuNode != null )
				{
					toolbars.setId("toolbar");
					loadToolbar(role, toolbars, menuNode, request, permissions, token);
				}
			}

			Node moduleNode = XMLParser.getChildElementByTag( parser.getRootNode(), "module" );
	        for( ; moduleNode != null; moduleNode = XMLParser.nextSibling(moduleNode))
	        {
	            String id = XMLParser.getElementAttr( moduleNode, "id" );
	            if( id == null ) continue;
	            //判断对应的组件是否存在，如果不存在就不加载对应的菜单
	            JSONObject cfg = ModulesMgr.getConfig(id);
	            if( cfg == null || (cfg.has("Disabled") && cfg.get("Disabled").toString().equalsIgnoreCase("true") ) )
	            {
	            	continue;
	            }
	            if( role != null && !role.has(id) )
	            {
	            	continue;
	            }
	            String icon = "fa-puzzle-piece";//XMLParser.getElementAttr( moduleNode, "fa" );
	            KTree tree = new KTree();
	            tree.setLabel(cfg.getString("SysName"));
	            String defaultView = src(XMLParser.getElementAttr(moduleNode, "default"), request);
	            tree.setDefaultView(defaultView);
		        Node menuNode = XMLParser.getChildElementByTag( moduleNode, "menu" );
		        tree.setIcon(icon);
		        tree.setId(id);
		        trees.add(tree);
		        loadTree(tree, role, tree, menuNode, request, permissions, token, newversion );//装载树形菜单
	        }
		}
		catch(Exception e)
		{
			log.error("Failed to nagavite", e);
		}
	}
	

	/**
	 * 加载工具栏导航
	 * @param role 权限
	 * @param component 组件
	 * @param menuNode 菜单节点
	 * @param toolbarMap 导航映射表
	 * @param request 
	 * @param token 权限令牌
	 */
	private void loadToolbar(JSONObject role,
							 KComponent component,
							 Node menuNode,
							 HttpServletRequest request,
							 ArrayList<Permission> permissions,
							 JSONObject token)
	{
		for( ; menuNode != null; menuNode = XMLParser.nextSibling(menuNode) )
		{
			String href = XMLParser.getElementAttr( menuNode, "href" );
			String id = component.getId()+"."+href;//XMLParser.getElementAttr( menuNode, "id" );
			if( role != null && !role.has(id) )
			{
				continue;
			}
        	href = src( href, request );
			String suid = id;
			//if( !href.equals("#") && !href.isEmpty() ) 
			suid = component.getSuid()+"."+Tools.encodeMD5(href);
			token.put(href, new JSONArray());
			String icon = XMLParser.getElementAttr( menuNode, "icon" );
            if( !icon.startsWith("fa-") )
            {
                icon = XMLParser.getElementAttr( menuNode, "fa" );
                if( icon.isEmpty() ) icon = getIcon();
            }
			String name = XMLParser.getElementAttr( menuNode, "name" );
			String moduleId = XMLParser.getElementAttr( menuNode, "module" );
			if( !href.equals('#') ) href = src(href, request);
			KMenuItem menu = new KMenuItem(name);
			menu.setIcon(icon);
			menu.setHref(href);
			menu.setModule(moduleId);
			menu.setId(id);
			menu.setSuid(suid);
			menu.setTarget(XMLParser.getElementAttr( menuNode, "target" ));
			component.addComponent(menu);
			this.loadButton(role, menuNode, request, permissions, token, href, id);
            Node childMenuNode = XMLParser.getChildElementByTag(menuNode, "menu" );
            if( childMenuNode != null )
            	loadToolbar(role, menu, childMenuNode, request, permissions, token);
		}
	}
	/**
	 * 加载导航树一个指定模块
	 * @param tree
	 * @param role
	 * @param component
	 * @param menuNode
	 * @param mapAction
	 * @param request
	 * @param permissions
	 * @param token
	 * @param newversion 如果是新版本将action放入到菜单中
	 */
	private void loadTree(KTree tree,
						  JSONObject role,
						  KComponent component,
						  Node menuNode,
//						  HashMap<String, Node> mapAction,
						  HttpServletRequest request,
						  ArrayList<Permission> permissions,
						  JSONObject token, 
						  boolean newversion)
	{
        for( ; menuNode != null; menuNode = XMLParser.nextSibling(menuNode) )
        {
//            String id = XMLParser.getElementAttr( menuNode, "id" );
            String icon = XMLParser.getElementAttr( menuNode, "icon" );
            if( !icon.startsWith("fa-") )
            {
                icon = XMLParser.getElementAttr( menuNode, "fa" );
                if( icon.isEmpty() ) icon = getIcon();
            }
            String href = XMLParser.getElementAttr( menuNode, "href" );//src(XMLParser.getElementAttr( menuNode, "href" ), request);
            String name = XMLParser.getElementAttr( menuNode, "name" );
            String id = component.getId()+" - "+href;
            if( role != null && !role.has(id.replaceAll(" - ", ".")) )
            {
            	continue;
            }
        	href = src( href, request );
            String suid = component.getSuid()+" - "+Tools.encodeMD5(href);
            //TODO:加载按钮级权限
            this.loadButton(role, menuNode, request, permissions, token, href, id);
            KTreeItem comp = (KTreeItem)component.addComponent(new KTreeItem(name, src(href, request), icon));
            comp.setId(id);
            comp.setSuid(suid);
			comp.setTarget(XMLParser.getElementAttr( menuNode, "target" ));
            Node childMenuNode = XMLParser.getChildElementByTag(menuNode, "menu" );
            loadTree( tree, role, comp, childMenuNode, request, permissions, token, newversion );
            if( comp.getComponentCount() > 0 )
            {
            	comp.setPaddingLeftSeed(26);
            }
            else
            {
            	comp.setPaddingLeft(22);
            }            
        }
	}
	
	/**
	 * 加载按钮权限
	 * @param role
	 * @param menuNode
	 * @param request
	 * @param permissions
	 * @param token
	 * @param href
	 */
	private void loadButton(JSONObject role, Node menuNode, HttpServletRequest request, ArrayList<Permission> permissions, JSONObject token, String href, String id)
	{
    	JSONArray btnToken = new JSONArray();
    	token.put(href, btnToken);
        //add
    	Node buttonNode = XMLParser.getChildElementByTag( menuNode, "button" );
    	Permission permission = new Permission();
    	permission.setViewId(href);
    	//如果所有按钮的权限都是false，那么打开该视图应该缺省让所有按钮都可以用
    	List<PermissionAction> list = new ArrayList<PermissionAction>();//记录循环的PermissionAction
    	boolean isNeeds = true;//是否需要记录模块下的权限
    	for( ; buttonNode != null; buttonNode = XMLParser.nextSibling(buttonNode) )
        {
    		String actionId = XMLParser.getElementAttr( buttonNode, "id" );
    		PermissionAction action =  new PermissionAction();
    		action.setActionId(actionId);
    		if( role != null )
    		{
    			if( role.has(id.replaceAll(" - ", ".")+"."+actionId) )
    			{
        			btnToken.put(actionId);
    				action.setHasPermission(true);
    			}
//    			如果配置过某个按钮的权限为true
//    			if(action.isHasPermission())
//    			{
//    				isNeeds = true;
//    			}
    		}
    		else
    		{
    			btnToken.put(actionId);
    			action.setHasPermission(true);//如果还没初始化（没配置过）就默认赋予全部权限
    		}
    		list.add(action);
        }
    	//如果不需要权限控制  默认赋值true
    	if(!isNeeds)
    	{
    		for(PermissionAction permissionAction : list)
    		{
    			permissionAction.setHasPermission(true);
    			permission.getActions().add(permissionAction);
    		}
    	}
    	else
    	{
    		permission.getActions().addAll(list);
    	}

    	if( !permission.getActions().isEmpty() )
    	{
    		permissions.add(permission);
    	}
	}
	
	/**
	 * 设置登录cookie
	 * @param user
	 * @param cookie
	 * @param cookies
	 * @return 是否登录
	 */
	public boolean setLoginCookie(JSONObject cookie, Cookie[] cookies)
	{
		if( cookie.has("password") ){
			cookie.remove("password");
		}
		ZooKeeper zookeeper = null;
		String path = "/cos";
		boolean logon = false;
		try
		{
			zookeeper = ZKMgr.getZooKeeper();
			Stat stat = zookeeper.exists(path, false); 
			if( stat == null)
			{
				zookeeper.create(path, "企业级应用支撑平台框架".getBytes("UTF-8"), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
			}
			path = "/cos/login";
			stat = zookeeper.exists(path, false); 
			if( stat == null)
			{
				zookeeper.create(path, "记录登录信息的节点".getBytes("UTF-8"), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
			}
			path = "/cos/login/user";
			stat = zookeeper.exists(path, false); 
			if( stat == null)
			{
				zookeeper.create(path, "记录登录用户节点".getBytes("UTF-8"), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
			}
			path = "/cos/login/user/"+cookie.getString("username");
			stat = zookeeper.exists(path, false);
			if( stat == null)
			{
//				user.setCount(1);
				cookie.put("count", 1);
				zookeeper.create(path, cookie.toString().getBytes("UTF-8"), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
			}
			else
			{
//				user.setCount(stat.getVersion()+2);
				cookie.put("count", stat.getVersion());
				zookeeper.setData(path, cookie.toString().getBytes("UTF-8"), stat.getVersion());
			}
			//记录cookie
			if( cookies != null )
				for( Cookie c : cookies )
				{
//					String _path = c.getPath();
					String name = c.getName();// get the cookie name
					String value = c.getValue(); // get the cookie value
					if( "COSSESSIONID".equalsIgnoreCase(name) )
					{
						path = "/cos/login/cookie";
						stat = zookeeper.exists(path, false); 
						if( stat == null)
						{
							zookeeper.create(path, "记录登录用户的节点".getBytes("UTF-8"), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
						}
						path = "/cos/login/cookie/"+value;
//						user.setCookie(value);
						cookie.put("sessionid", value);
						stat = zookeeper.exists(path, false); 
						if( stat == null)
						{
							zookeeper.create(path, cookie.toString().getBytes("UTF-8"), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
						}
						else
						{
							zookeeper.setData(path, cookie.toString().getBytes("UTF-8"), stat.getVersion());
						}
						logon = true;
						break;
					}
				}
			
			path = "/cos/release";
			stat = zookeeper.exists(path, false); 
			if( stat == null)
			{
				zookeeper.create(path, WrapperShell.version().getBytes("UTF-8"), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
			}
			else
			{
				zookeeper.setData(path, WrapperShell.version().getBytes("UTF-8"), stat.getVersion());
			}
		}
		catch(Exception e)
		{
			log.warn("Failed to set cookie to zookeeper for "+e);
			logon = true;
		}
		return logon;
	}
	
	public String checkLicense(HttpServletRequest request)
	{
		File file = new File(PathFactory.getDataPath(), "cos.l");
		if( !file.exists() )
		{
			return null;
		}
		String licenseTips = null;
		Key privateKey = null;
		DataInputStream dis = null;
		try
		{
			dis = new DataInputStream(new ByteArrayInputStream(IOHelper.readAsByteArray(file)));
			int len = dis.readInt();
			byte[] wrappedKey = new byte[len];
			dis.readFully(wrappedKey);
			Cipher c = Cipher.getInstance("RSA");
			file = new File(PathFactory.getDataPath(), "identity.pk");
			privateKey = (Key)IOHelper.readSerializable(file);
			c.init(Cipher.UNWRAP_MODE, privateKey);
			Key key = c.unwrap(wrappedKey, "AES", Cipher.SECRET_KEY);

			c = Cipher.getInstance("AES");
			c.init(Cipher.DECRYPT_MODE, key);
			len = dis.readInt();
			byte[] value = new byte[len];
			dis.readFully(value);
			licenseTips = new String(c.doFinal(value));
		}
		catch (Exception e)
		{
		}
		finally
		{
			if( dis != null )
				try
				{
					dis.close();
				}
				catch (IOException e)
				{
				}
		}
		if( licenseTips == null || licenseTips.trim().isEmpty() )
		{
			licenseTips = "当前您的软件服务授权已过期，请联系您的服务提供商。";
		}
		request.getSession().setAttribute("license", licenseTips);
		return licenseTips;
	}
}
