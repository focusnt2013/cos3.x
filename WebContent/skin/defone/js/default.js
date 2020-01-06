var fullscreens = new Object();
fullscreens["index"] = true;
fullscreens["monitor!navigate.action"] = true;
fullscreens["control!checkupgrades.action"] = true;
fullscreens["control!checkupgrade.action"] = true;
fullscreens["monitor!server.action"] = true;
fullscreens["monitorload!cluster.action"] = true;
fullscreens["monitorload!clusterchart.action"] = true;
fullscreens["monitorload!runnerflow.action"] = true;
fullscreens["monitorload!serverchart.action"] = true;
fullscreens["monitorload!modulememory.action"] = true;
fullscreens["monitorload!modulecpu.action"] = true;
fullscreens["monitorload!modulediskspace.action"] = true;
fullscreens["monitorload!modulediskio.action"] = true;
fullscreens["monitorload!modulenetio.action"] = true;
fullscreens["user!propmgr.action"] = true;
fullscreens["menus!publish.action"] = true;
fullscreens["menus!config.action"] = true;
fullscreens["datacfg!datasource.action"] = true;
fullscreens["datacfg!setquery.action"] = true;
fullscreens["weixin!config.action"] = true;
fullscreens["helper!developer.action"] = true;
fullscreens["helper!pdf.action?filename=cos-data-structure.pdf"] = true;
fullscreens["rpc!navigate.action"] = true;
fullscreens["rpc!open.action"] = true;
fullscreens["rpc!findjars.action"] = true;
fullscreens["rpc!debug.action"] = true;
fullscreens["helper!regexp.action"] = true;
fullscreens["sysalarm!config.action"] = true;
fullscreens["sysalarm!history.action"] = true;
fullscreens["sysalarm!manager.action"] = true;
fullscreens["notify!messenger.action"] = true;
fullscreens["http://fontawesome.dashgame.com/"] = true;
fullscreens["monitorcfg!databases.action"] = true;
fullscreens["helper!sqlquery.action"] = true;
fullscreens["helper!upgrade.action"] = true;
fullscreens["helper!timeline.action"] = true;
fullscreens["helper!colors.action"] = true;
fullscreens["files!edit.action"] = true;
fullscreens["files!preview.action"] = true;
fullscreens["files!previewlog.action"] = true;
fullscreens["files!navigate.action"] = true;
fullscreens["files!open.action"] = true;
fullscreens["control!configxml.action"] = true;
fullscreens["control!preset.action"] = true;
fullscreens["control!navigate.action"] = true;
fullscreens["control!open.action"] = true;
fullscreens["control!publish.action"] = true;
fullscreens["zookeeper!navigate.action"] = true;
fullscreens["zookeeper!open.action"] = true;
fullscreens["modules!navigate.action"] = true;
fullscreens["modules!viewpublish.action"] = true;
fullscreens["diggcfg!debug.action"] = true;
fullscreens["diggcfg!debuglog.action"] = true;
fullscreens["diggcfg!stylepreview.action"] = true;
fullscreens["diggcfg!editpreview.action"] = true;
fullscreens["diggcfg!explorer.action"] = true;
fullscreens["diggcfg!preset.action"] = true;
fullscreens["diggcfg!api.action"] = true;
//fullscreens["program!config.action"] = true;
fullscreens["editor!javascript.action"] = true;
fullscreens["editor!xml.action"] = true;
fullscreens["editor!json.action"] = true;
fullscreens["editor!css.action"] = true;
fullscreens["editor!js.action"] = true;
fullscreens["editor!compare.action"] = true;
fullscreens["security!manager.action"] = true;

function getHrefUri(href)
{
	var i = href.indexOf('?');
	if( i != -1 )
	{
		return href.substring(0, i);
	}
	return href;
}

//打开指定模块的视图
var feview = document.getElementById("feview");
var divViewMenu = document.getElementById("divViewMenu");
var curMenuItemId;//当前菜单项目ID
var curView;//视图的iframe窗口对应的tr
var memoryMenuItem = new Array();//菜单项的队列
Array.prototype.next=function(menuItemId)
{
    if( menuItemId && this.length > 0 )
    {
		for(var i=0; i < this.length; i++)
		{
			if( this[i] == menuItemId )
			{
				return (i-1)>=0?this[i-1]:null;
			}
		}
	}
	return null;
};
Array.prototype.back=function(menuItemId)
{
    if( menuItemId && this.length > 0 )
    {
		for(var i=0; i < this.length; i++)
		{
			if( this[i] == menuItemId )
			{
				return (i+1)<this.length?this[i+1]:null;
			}
		}
	}
	return null;
};
Array.prototype.remove=function(menuItemId)
{
    if( menuItemId && this.length > 0 )
    {
	    var match = false;
		for(var i=0; i < this.length; i++)
		{
			match = !match?this[i]==menuItemId:match;
			if( match && i + 1 < this.length )
			{
				this[i]=this[i+1];
			}
		}
	  	this.length -= match?1:0;
	  	
  	}
};
//在指定位置添加一条记录
Array.prototype.insert=function(curMenuItemId,newMenuItemId)
{
	if( curMenuItemId )
	{
	    if( curMenuItemId == newMenuItemId )
	    {
	    	return false;
	    }
	    var begin = 0;
		for(var i = 0; i < this.length; i++)
		{
			if( this[i] == curMenuItemId )
			{
				begin = i;
				break;
			}
		}
		this.push("");
		for(var i = this.length - 1; i > begin; i-- )
		{
			this[i] = this[i-1];
		}
		this[begin] = newMenuItemId;
	   	return true;
	}
	this.push(newMenuItemId);
	return true;
};
var currentModuleId = ''; 
var mapView = new Object();
var defaultModule = "Cos";
var isOpenModule = "Cos";
var m_fullscreen = "no";
function openView()
{	
	var args = openView.arguments;
	try{
		var closeSidebar = document.getElementById("closeSidebar");
		if( closeSidebar ){
			try{
				closeSidebar.click();
			}
			catch(e){
			}
		}
		/*视图标题*/
		var title = args[0];
		/*打开链接视图*/
		var href = args[1];
		if( href == '#' || href == '' || href.charAt(0) == '#' )
		{
	//skit_alert("<ww:text name='label.ema.module.measurement'/>");
			return null;
		}
		var target = args.length>2?args[2]:null;
		if( target && target != "fullscreen" )
		{
			window.open(href,target,"resizable=no,toolbar=no,location=no,directories=no,menubar=no,scrollbars=no,status=yes");
			return null;
		}
		var disableFullscreen = args.length>3?args[3]:false;
	    skit_showLoading();
		if( switchView(href, true, title, target=="fullscreen", disableFullscreen) )
		{//发生切换不需要再打开一次
			return curMenuItemId;
		}
		var i = href.indexOf("%_windowWidth%");
		if( i > 1 ){
			href = href.replace("%_windowWidth%", _windowWidth);
		}
		var id = href;
		var row = feview.insertRow(0);
		row.id = "tr"+id;
		row.title = title;
		
		var cell = row.insertCell(0);
		cell.id = "td"+id;
		cell.innerHTML = "<iframe id='"+id+"' name='"+id+"' frameborder='0' width='100%' style='margin-left:3px;padding-right:6px;height=100%'></iframe>";
		cell = row.insertCell(1);
		cell.style.display = "none";
		cell.id = "title"+id;
		cell.innerHTML = title;
		if( curView )
		{
			curView.style.display = "none";
		}

		document.getElementById("bookmarkTips").innerHTML = feview.rows.length;
		if( href != "index" )
		{
			document.getElementById("page-header").style.display = "none";
			document.getElementById("showBannerIcon").className = "fa fa-angle-down";
			document.getElementById("btnClose").disabled = false;
		}
		else
		{
			document.getElementById("page-header").style.display = "";
			document.getElementById("showBannerIcon").className = "fa fa-angle-up";
			document.getElementById("btnClose").disabled = true;
		}
		curView = row;
		//执行将打开的视图在视图菜单中添加对应的数据
		var fa = "fa-bookmark-o";
		if( id == "index" ) fa = "fa-desktop";
		var html = '<div class="status-online user-chat">'+
			'<div class="media-left"><i class="fa fa-fw '+fa+'"></i></div>'+
			'<div class="media-body" onclick="switchView(\''+id+'\');">'+
			'<h4 class="media-heading menu_text_overflow" id="menuTitle'+id+'">'+title+'</h4></div>';
		if( id != "index" )
		{
			html += '<div class="media-right" onclick="removeView(\''+id+'\')" title="点击关闭视图"><i class="fa fa-fw fa-close"></i></a></div>';
		}
		html += '</div>';
		if( curMenuItemId )
		{
			var menuTitle = document.getElementById("menuTitle"+curMenuItemId);
			menuTitle.style.fontWeight = "";
			menuTitle.style.color = "";
		}
		var li = document.createElement("li");
		li.id = "menuLi"+id;
		li.className = "media";
		li.innerHTML = html;
		li.style.padding = "7px";
		li.style.cursor = "pointer";
		li.title = title;
		document.getElementById("media-list").appendChild(li);
		var menuTitle = document.getElementById("menuTitle"+id);
		menuTitle.style.fontWeight = "bold";
		menuTitle.style.color = "#000000";
		memoryMenuItem.insert(curMenuItemId, id);//插入菜单项目
		curMenuItemId = id;
		goable();
		resizeWindow(id);
	//alert("href="+href);
		if( href.indexOf("https:") == -1 )
		{
			if( href.indexOf('?') > 0 ) {
				href = href+"&account="+account+"&wh="+document.getElementById(id).clientHeight+"&v="+v;
			}
			else {
				href = href+"?account="+account+"&wh="+document.getElementById(id).clientHeight+"&v="+v;
			}
		}
		if( href.indexOf("diggcfg!debug.action") != -1 ){
			document.getElementById("divDebugObjectScript").style.display = "";
		}
		else{
			document.getElementById("divDebugObjectScript").style.display = "none";
		}
		document.getElementById(id).src = href;
		//var iframe = $("iframe");
		//alert("$('#"+id+"'): ");
		var iframe = $("iframe[id$='"+id+"']");
		//var ifr = window.frames[id];
		//alert("ifr.window.resizeWindow="+ifr.window.resizeWindow);
		if( iframe && id == 'index' ){
			//暂时禁用
			try{
				iframe.niceScroll({
					cursorcolor: '#dddddd',
					railalign: 'right',
					cursorborder: "none", 
					horizrailenabled: false, 
					zindex: 2001, 
					left: '0px', 
					cursoropacitymax: 0.6, 
					cursorborderradius: "0px", 
					spacebarenabled: false });
			}
			catch(e){
				alert("渲染视图窗口的滚动栏异常:"+e);
			}
		}
	}
	catch(e){
		alert(e);
	}
	return curMenuItemId;
}

function executeByView(id, callback){

	var ifr = window.frames[id];
	if( ifr )
	{
		callback(ifr);
	}	
}

function showTriggerSidebar()
{
	document.getElementById("triggerSidebar").click();
}
//得到权限数据
function getPermissions(viewId)
{
	if( viewId )
		return pers[viewId];//指定视图
	return pers[curMenuItemId];
}
//打开子系统
function openSubsystem(module)
{
//alert(module);
	var btnShowMenu = document.getElementById("btnShowMenu");
	btnShowMenu.click();
	var li = switchNavigate(module);
//alert("完成导航切换："+li);
	if( li )
	{
		var href = document.getElementById(module).value;
		if( href ){
			moduleDefaultViews[href] = module;
		}
		openView(li.title, href);
	}
	else 
	{
		var m = document.getElementById(module);
		if( m && m.value != "" )
		{
			if( sysadmin )
			{
				top.skit_confirm("因为系统配置的用户角色权限发生改变不能打开系统菜单，请是否立刻配置角色权限。", function(yes){
					if( yes )
					{
						openView('角色管理', 'role!manager.action');
					}
				});
			}
			else{
				skit_alert("因为系统配置的用户角色权限发生改变不能打开系统菜单，请联系系统管理员重置您的角色权限。", "权限菜单提醒");
				sendSystemNotify(null, "用户投诉", 
						"用户["+account+"]登录后发现因为系统配置的用户角色权限发生改变不能打开系统菜单。",
						"请立刻打开角色权限管理或用户权限管理配置该用户的权限。",
						"配置角色用户权限",
						"role!manager.action");
			}
		}
	}
}

/*切换子系统导航菜单*/
function switchNavigate(module)
{
	var li1 = null;
	if( currentModuleId && currentModuleId != module )
	{
		var li_modules = $("li[id^='"+currentModuleId+" - ']");
//alert("li[id^='"+currentModuleId+" - '] "+li_modules.length);
		for(var i = 0; i < li_modules.length; i+=1 )
		{
			var li = li_modules[i];
			if( li.hide ) li.hide();
			else li.style.display = "none";
		}
	}
	else if( currentModuleId )
	{
		return null;
	}
	
	if( module )
	{
		//document.getElementById("breadcrumb_2").style.display = "none";
		//document.getElementById("breadcrumb_3").style.display = "none";
//alert("li[id^='"+module+" - ']");
		var li_modules = $("li[id^='"+module+" - ']");
		for(var i = 0; i < li_modules.length; i+=1 )
		{
			var li = li_modules[i];
//alert("["+i+"]"+li+","+li.show);
			if( i == 0 )
			{
				li1 = li;
				if( module != "home" )
				{
					document.getElementById("breadcrumb_1").innerHTML = li.title;
					document.getElementById("breadcrumb_1").style.display = "";
					document.getElementById("side-menu-user").style.display = "none";
				}
				else
					document.getElementById("side-menu-user").style.display = "";
			}
			if( li.show ) li.show();
			else li.style.display = "";
		}
	}
	currentModuleId = module;
	return li1;
}
//切换到已经打开的视图中
var switchViewLast;
function switchView(href, reload, viewTitle, fullscreen, disableFullscreen)
{
	try{
//		var menu = document.getElementById("menu");
		//alert($("#menu"));
	    if( !disableFullscreen && (fullscreen || fullscreens[getHrefUri(href)]) )
	    {
	    	//document.getElementById("page-wrapper").style.marginLeft = 0;
			//document.getElementById("showMenuIcon").className = "fa fa-angle-right";
	    	$("#menu").hide();
	    	$("#page-wrapper").css('marginLeft', 0); 
	    	$("#showMenuIcon").attr('class','fa fa-angle-right'); 
	    	//menu.style.visibility = "hidden";
	    	//menu.style.display = "none";
	    	//alert(document.getElementById("page-wrapper").style.marginLeft);
	    }
	    else
		{
	    	//document.getElementById("page-wrapper").style.marginLeft = 250;
			//document.getElementById("showMenuIcon").className = "fa fa-angle-left";
	    	//menu.style.display = "";
	    	//menu.style.visibility = "visible";
	    	//menu.style.top = 0;
	    	$("#menu").show();
	    	$("#page-wrapper").css('marginLeft', 250); 
	    	$("#showMenuIcon").attr('class','fa fa-angle-left'); 
		}
		//alert("switchView:"+href+"("+curMenuItemId+"),reload="+reload);
		document.getElementById("chat").className = "hidden-print";
		if( reload ){}else
		{
			reload = false;
		}
		if( !reload && curMenuItemId == href )
		{//当前视图与被切换视图是同一个视图
			return true;
		}
		if( href.indexOf("diggcfg!debug.action") != -1 ){
			document.getElementById("divDebugObjectScript").style.display = "";
		}
		else{
			document.getElementById("divDebugObjectScript").style.display = "none";
		}
		if( curView )
		{
			curView.style.display = "none";
		}
		curView = document.getElementById("tr"+href);
		if( curView )
		{
			curView.style.display = "";
			var title = document.getElementById("menuTitle"+curMenuItemId);
			if( title )
			{
				title.style.fontWeight = "";
				title.style.color = "";
			}
	//alert("title="+title);
			title = document.getElementById("menuTitle"+href);
			title.style.fontWeight = "bold";
			title.style.color = "#000000";
			//视图切换了导航栏与导航菜单要进行变化
			showNavigate(href, title.innerHTML);
			curMenuItemId = href;
			if( curMenuItemId == "index" )
			{
				document.getElementById("btnClose").disabled = true;
			    document.getElementById("page-header").style.display = "";
			}
			else
			{
				document.getElementById("btnClose").disabled = false;
				document.getElementById("page-header").style.display = "none";
			}
			goable();
			resizeWindow(href);
			skit_hiddenMask();
			return true;
		}
		else
		{
			//alert("switchViewLast("+switchViewLast+"):"+href);
			if( switchViewLast == href ){
				switchViewLast = null;
				return true;
			}
			switchViewLast = href;
			showNavigate(href, viewTitle);
			return false;
		}
	}
	catch(e){
		//showObject(_li);
		alert("切换视图窗口异常:"+e.message+", 行数"+e.lineNumber);
		return false;
	}
}
/*显示导航栏信息*/
function showObject(obj)
{
	if( obj )
	{
		var alt = "";
		for(var key in obj )
			alt += "\r\n\t\t"+key+"="+obj[key];
		alert("展示对象内容:"+alt);
	}
}

function searchFunction(input){
	if( currentModuleId )
	{
		if(!input) input = document.getElementById("iSearchFunction");
		try{
			var li_modules = $("li[title^='"+input.value+"']");
			for(var i = 0; i < li_modules.length; i+=1 )
			{
				var li = li_modules[i];
				var id =  li.id;
				if( id.indexOf(currentModuleId) == 0 ){
					//var ul = li.parentNode;
					var a = document.getElementById(id+"-aa");
					/*if( ul.className.indexOf("nav-third-level") != -1 ){
						//当前是3级菜单
						var li2 = ul.parentNode;
						var ul1 = li2.parentNode;
						var li1 = ul1.parentNode;
						if( li1.className != "active" ){
							li1.childNodes[0].click();
						}
						if( li2.attr("class") != "active" ){
							li2.childNodes[0].click();
						}
					}
					else if( ul.className.indexOf("nav-second-level") != -1 ){
						//当前是2级菜单
						var li1 = ul.parentNode;
						if( li1.className != "active" ){
							showObject(li1.childNodes[0]);
						}
					}
					else{
						//当前是1级菜单
					}*/
					if( a ) a.click();
					break;
				}
			}
		}
		catch(e){
			alert(e);
		}
	}
	
}
var paneltitle;
var menu_a_current_id;
var _li;
function showNavigate(href, title)
{
//alert("showNavigate:"+href);
	document.getElementById("breadcrumb_1").style.display = "none";
	document.getElementById("breadcrumb_2").style.display = "none";
	document.getElementById("breadcrumb_3").style.display = "none";
	document.getElementById("breadcrumb_4").style.display = "none";
	document.getElementById("breadcrumb_5").style.display = "none";
	var suid = href;
	if( href != "index" ) suid = $.md5(href);
	//alert("suid="+suid);
	var li_menus = $("li[id$=' - "+suid+"']");
	var li;
	if( li_menus && li_menus.length > 0 )
	{
		if( li_menus.length==1 ){
			li = li_menus.length==1?li_menus:li_menus[0];
		}
		else{
			li = li_menus.first();
		}
	}
	//alert("li="+li);
	if( li )
	{
		_li = li;
//alert("li: "+li.id);
		var id =  li.attr("id");
		//切换导航菜单为当前子系统
//alert("showNavigate.id:"+id);
		var modules = id.split(" - ");
		var module = modules[0];
		var ul = li.parent();
		var a = document.getElementById(id+"-aa");
		if( ul.attr("class").indexOf("nav-third-level") != -1 ){
			//当前是3级菜单
			var li2 = ul.parent();
			var ul1 = li2.parent();
			var li1 = ul1.parent();
			if( li1.attr("class") != "active" ){
				ul1.prev().trigger("click");//如果是非激活状态就触发点击事件
			}
			if( li2.attr("class") != "active" ){
				ul.prev().trigger("click");//如果是非激活状态就触发点击事件
			}
			if( li.attr("class") == "active" ){
			}
		}
		else if( ul.attr("class").indexOf("nav-second-level") != -1 ){
			//当前是2级菜单
			var li1 = ul.parent();
			if( li1.attr("class") != "active" ){
				ul.prev().trigger("click");//如果是非激活状态就触发点击事件
				if(li.html().indexOf("<ul") == -1 ){
					if( a ) a.click();
				}
			}
		}
		else{
			//当前是1级菜单
			if( li.attr("class") == "active" ){
				if( a ) a.click();
			}
		}
		//alert(menu_a_current_id+":"+a);
		
		if( menu_a_current_id && a ){
			document.getElementById(menu_a_current_id+"-aa").className = "";
		}
		if( a ){
			menu_a_current_id = id;
			a.className = "MenuSelected";//#f3c016
		}
//alert("found the module("+modules.length+") is "+module+" from ["+title+"]"+href);
		if( module && module != currentModuleId )
		{//如果打开视图后发现子系统与当前子系统不一致，则切换导航菜单
			if( switchNavigate(module) )
			{//子系统发生了切换
//alert("发生了导航切换");
			}
		}
//alert("去激活前一个导航li"+currentNavigate);
		currentNavigate = li;
		if( href == "index" ) return;
		document.getElementById("breadcrumb_1").style.display = "";
		if( modules.length > 1 )
		{
			id = modules[0];
			var i = 1;
			var li_bc = document.getElementById("breadcrumb_"+(i));
			var subject = document.getElementById(id+"_label")?document.getElementById(id+"_label").value:"";
			if( li_bc ){
				li_bc.innerHTML = subject;
				li_bc.style.display = "";
			}
			for(i = 1; i < modules.length; i+=1 )
			{
				id += " - " + modules[i];
				li = document.getElementById(id);
				if( li )
				{
					var li_bc = document.getElementById("breadcrumb_"+(i+1));
					if( li_bc && subject != li.title )
					{
						li_bc.innerHTML = li.title;
						li_bc.style.display = "";
					}
					else
					{
						//alert("li_bc is null breadcrumb_"+(i+2));
					}
				}
			}
//alert("showNavigate:i="+i+":"+li_bc.innerHTML);
			//li_bc.innerHTML = "<span class='navigate_text_overflow'>"+li_bc.innerHTML+"</span>"
			//li_bc = document.getElementById("breadcrumb_"+(i+1));
			paneltitle = li_bc;//document.getElementById("breadcrumb_"+(i+1));
		}
	}
	else
	{
//alert("showNavigate:"+title);
		paneltitle = document.getElementById("breadcrumb_1");
		paneltitle.innerHTML = title;
		paneltitle.style.display = "";
		var moduleId = moduleDefaultViews[href];
		if( moduleId ){
			switchNavigate(moduleId);
		}
	}
//alert("showNavigate:end.");
}
var moduleDefaultViews = {};
//从底层视图回调上来的设置视图标题的方法
function setViewTitle(title)
{
	if( paneltitle )
	{
		paneltitle.style.display = "";
		var newTitle = title;
		if( curView.title != title ) newTitle = "<span class='navigate_text_overflow'>"+curView.title+":"+title+"</span>";
		paneltitle.innerHTML = newTitle;
		var titleMenuItem = document.getElementById("menuTitle"+curMenuItemId);
		if( titleMenuItem )
		{
//alert("curView.title="+curView.title);
			titleMenuItem.innerHTML = newTitle;
		}
	}
}
/*触发操作*/
var currentNavigate;
function triggerAction(a)
{
	//alert('triggerAction('+currentNavigate+'): '+a.attr("data-href"));
	if( currentNavigate)
	{
		if( currentNavigate.attr ) currentNavigate.attr("class", "");
	  	else currentNavigate.className = "";
	}
	var href = a.attr("data-href");
	var liid = a.attr("data-li");
	var action = a.attr("data-action");
	if( action && action != "" )
	{
	}
	if( href && href != "#" && href != "" )
	{
		eval(href);
	}
	currentNavigate = a;
}
/*增加视图菜单
<li class="media">
	<a href="javascript:;" class="status-online user-chat">
		<div class="media-left"><i class="fa fa-fw fa-desktop"></i></div>
		<div class="media-body"><h4 class="media-heading">首页</h4></div>
	</a>
</li>*/
function addViewMenu(viewTitle, id)
{
}
//重新刷新视图
var v = 1;
function reloadView()
{
	resizeWindow(curMenuItemId);
	var ifr = window.frames[curMenuItemId];
	if( ifr )
	{
		//ifr.window.location.reload();
		var href = curMenuItemId;
		if( href.indexOf("https:") == -1 )
		{
			if( href.indexOf('?') > 0 )
			{
				href = href+"&account="+account+"&v="+v;
			}
			else
			{
				href = href+"?account="+account+"&v="+v;
			}
		}
		v += 1;
	//alert(document.getElementById(curMenuItemId).src);
		document.getElementById(curMenuItemId).src = href;
	}
}

//关闭视图
function removeView(id)
{
	if( curMenuItemId == id )
	{
		closeView();
		return;
	}
	var removeMenuItemId = id;
	//删除菜单
	var removeMenuItem = document.getElementById("menuLi"+removeMenuItemId);//得到li对象
	if( removeMenuItem ){
		try{
			//alert("memoryMenuItem.remove(removeMenuItemId)");
			memoryMenuItem.remove(removeMenuItemId);//从视图排序列表删除对应的视图标识
			document.getElementById("media-list").removeChild(removeMenuItem);
			//删除视图
			var removeViewTr = document.getElementById("tr"+removeMenuItemId);
			feview.deleteRow(removeViewTr.rowIndex);
			document.getElementById("bookmarkTips").innerHTML = feview.rows.length;
			goable();
		}
		catch(e){
			alert("删除视图("+id+")出现异常"+e);
		}
	}
}
//关闭视图
function doCloseView(viewTitle, viewHref)
{
	try{
		//先把菜单项和视图记录下来
		var removeView = curView;
		var removeMenuItemId = curMenuItemId;
		//alert(removeMenuItemId);
		if( viewTitle && viewHref )
		{//打开指定视图
			openView(viewTitle, viewHref);
		}
		else
		{//打开前一个视图
			var backMenuItemId = memoryMenuItem.back(removeMenuItemId);
			if( backMenuItemId ) switchView(backMenuItemId, false);
		}
		//alert("memoryMenuItem.remove(removeMenuItemId)");
		memoryMenuItem.remove(removeMenuItemId);//从视图排序列表删除对应的视图标识
		//删除菜单
		var removeMenuItem = document.getElementById("menuLi"+removeMenuItemId);//得到li对象
		if( removeMenuItem ) document.getElementById("media-list").removeChild(removeMenuItem);
		//删除视图
		var id = "tr"+removeMenuItemId;
		var removeViewTr = document.getElementById(id);
		if( removeViewTr == removeView ){
			//alert(removeViewTr.rowIndex+":"+feview.innerHTML);
			feview.deleteRow(removeViewTr.rowIndex);
			//alert(removeViewTr.rowIndex+":"+feview.innerHTML);
		}
		document.getElementById("bookmarkTips").innerHTML = feview.rows.length;
		switchViewLast = null;
		goable();
	}
	catch(e){
		alert(e);
	}
}
function closeView(viewTitle, viewHref)
{
	if( curMenuItemId == "index" )
	{
		return;
	}
	if( curMenuItemId.indexOf("diggcfg!explorer.action?mode=1") != -1 ){
		//如果是元数据模板开发视图，要提示确认才能关闭
		var ifr = window.frames[curMenuItemId];
		if( ifr && ifr.checkEdit && ifr.checkEdit() )
		{
			skit_confirm("【元数据模板开发】还有未保存的编辑中模板，关闭当前视图编辑数据将丢失，您确认继续关闭当前视图吗？", function(yes){
				if( yes )
				{
					doCloseView(viewTitle, viewHref);
				}
			});	
			return;
		}
	}
	doCloseView(viewTitle, viewHref);
}

//返回导航树显示视图
function reopenView()
{
	closeView();
}
//关闭当前视图再刷新前一个视图
function closeReloadView(viewId)
{
	closeView();
	if( viewId )
	{
		switchView(viewId, false);
	}
	skit_showLoading();	
	reloadView();
}

function goForward()
{
	var nextMenuItemId = memoryMenuItem.next(curMenuItemId);
	if( nextMenuItemId )
	{
		switchView(nextMenuItemId, false);
	}
	goable();
}
function goBack()
{
	var backMenuItemId = memoryMenuItem.back(curMenuItemId);
	if( backMenuItemId )
	{
		switchView(backMenuItemId, false);
	}
	goable();
}
//显示banner
function showBanner(btn)
{
	if( document.getElementById("showBannerIcon").className == "fa fa-angle-down" )
	{
		document.getElementById("page-header").style.display = "";
		document.getElementById("showBannerIcon").className = "fa fa-angle-up";
		btn.title = "展开显示Banner";
	}
	else
	{
		document.getElementById("page-header").style.display = "none";
		document.getElementById("showBannerIcon").className = "fa fa-angle-down";
		btn.title = "收起隐藏Banner";
	}
	resizeWindow(curMenuItemId);
}

function showMenu(btn)
{
	//var menu = document.getElementById("menu");
	if( document.getElementById("showMenuIcon").className == "fa fa-angle-right" )
	{

    	$("#menu").show();
    	$("#page-wrapper").css('marginLeft', 250); 
    	$("#showMenuIcon").attr('class','fa fa-angle-left'); 
		//menu.style.display = "";
		//menu.style.visibility = "visible";
		//document.getElementById("page-wrapper").style.marginLeft = 250;
		//document.getElementById("showMenuIcon").className = "fa fa-angle-left";
		btn.title = "收起隐藏菜单栏";
	}
	else
	{
    	$("#menu").hide();
    	$("#page-wrapper").css('marginLeft', 0); 
    	$("#showMenuIcon").attr('class','fa fa-angle-right'); 
		//menu.style.visibility = "hidden";
		//menu.style.display = "none";
		//document.getElementById("page-wrapper").style.marginLeft = 0;
		//document.getElementById("showMenuIcon").className = "fa fa-angle-right";
		btn.title = "展开显示菜单栏";
	}
	resizeWindow(curMenuItemId);
}

function resizeFrame()
{
	resizeWindow(curMenuItemId);
}
//检查是否可以前进或后退
function goable()
{
	var nextMenuItemId = memoryMenuItem.next(curMenuItemId);
	if( nextMenuItemId )
	{
		document.getElementById("btnForward").disabled = false;
	}
	else
	{
		document.getElementById("btnForward").disabled = true;
	}
	var backMenuItemId = memoryMenuItem.back(curMenuItemId);
	if( backMenuItemId )
	{
		document.getElementById("btnBack").disabled = false;
	}
	else
	{
		document.getElementById("btnBack").disabled = true;
	}
}

function reload()
{
	skit_showLoading();
	reloadView();
}

function setAlarmTips(count)
{
	document.getElementById("badge-alarms").innerHTML = count;
	if( count > 0 )
	{
		document.getElementById("badge-alarms").style.display = "";
		document.getElementById("dropdown-alarms").style.display = "";
	}
	else
	{
		document.getElementById("badge-alarms").style.display = "none";
		document.getElementById("dropdown-alarms").style.display = "none";
	}
}

function setNotifyTips(count)
{
	document.getElementById("badge-notifies").innerHTML = count;
	if( count > 0 )
	{
		document.getElementById("badge-notifies").style.display = "";
	}
	else
	{
		document.getElementById("badge-notifies").style.display = "none";
	}
}

//向系统管理员发送系统通知消息
function sendSystemNotify(to, filter, title, content, link, action, actionurl)
{
	DescktopMgr.sendSystemNotify(to, filter, title, content, link, action, actionurl, {
		callback:function(){
			if( to )
				skit_alert("成功向用户【"+to+"】发送系统通知消息:<br/>"+title+"<br/>"+content);
			else
				skit_alert("成功向【系统管理组】发送系统通知消息:<br/>"+title+"<br/>"+content);
		},
		timeout:10000,
		errorHandler:function(message) {
			skit_alert("发送系统通知消息出现异常"+message);
		}
	});
}
//发送桌面消息
function sendDescktopMessage()
{
	var content = document.getElementById("inputDescktopMessage").value;
	if( content == null || content == "" )
	{
		skit_alert("请输入要发送消息的内容");
		return;
	}
	DescktopMgr.sendMessage(content, {
		callback:function(msg){
			try
			{
				var divChat = document.getElementById("divChat");
				var node = document.getElementById("chat-list");
				var lastNode = document.getElementById("chat-list-first");
				var li = document.createElement("li");
				li.id = msg.id;
				li.className = "right clearfix";
				var html = "<span class='chat-img pull-right'>";
				html += "<img src='"+msg.head+"' data-src='"+msg.head+"' data-src-retina='"+msg.head+"' alt='User Avatar' class='img-responsive img-circle' height='50' width='50'>";
	            html += "</span>";
	            html += "<div class='chat-body clearfix'><div class='header'><small class=' text-muted'><i class='fa fa-clock-o fa-fw'></i> "+msg.prettytime+"</small><strong class='pull-right primary-font' title='"+msg.account+"'>"+msg.username+"</strong></div>";
	            html += "<p>"+msg.content+"</p></div>";
	            li.innerHTML = html;
	            node.insertBefore(li, lastNode);
	            divChat.scrollTop = divChat.scrollHeight;
	            document.getElementById("inputDescktopMessage").value = "";
			}
			catch(e)
			{
				skit_alert(e);
			}
		},
		timeout:10000,
		errorHandler:function(message) {
			skit_alert("发送桌面消息出现异常"+message);
		}
	});
}
var notifesWaitForTips = new Array();//存放系统消息的队列
var notifesMemory = new Object();
var firstDoNotify = true;
var tsMessage = 0;
function handleDescktopTips(){
	if( account != "" && DescktopMgr ){}else
	{
		return;
	}
	tsNotify = getUserActionMemory("tsNotify");
	if( !tsNotify ) tsNotify = 0;
	DescktopMgr.getDescktopTips(firstDoNotify?lastLogin:tsNotify, tsMessage, {
		callback:function(response){
			if( response ){
				if( response.exception ){
					skit_alert(response.exception, "登录异常", function(){
						logout();
					});
					return;
				}
			} 
			else {
				skit_alert("会话因为超时或者其它原因过期，请尝试重新登录，若还有问题请联系系统管理员。", "重新登录提醒", function(){
					logout();
				});
				return;
			}
			document.getElementById("title_dtime").innerHTML = response.datetime;
			if( disableNotifies == true ){
				return;
			}
			var notifies = response.notifies;
			var messages = response.messages;
			try
			{
				setAlarmTips(response.alarmTips);
				var list = notifies;
				var table = document.getElementById("task-menu-notifies-table");
				for(var i = list.length - 1; i >= 0; i-=1 )
				{
					var n = list[i];
					if( tsNotify < n.timestamp ) notifesWaitForTips.push(n);
					
					var tr = table.insertRow(0);
					tr.onclick = new Function("openNotifyView("+n.nid+");");
					tr.onmouseover = new Function("this.style.backgroundColor='#EEF5FD';");
					tr.onmouseout = new Function("this.style.backgroundColor='';");
					tr.id = n.nid;
					var icon = "<i style='font-size:10px;color:#fbc728;' class='fa fa-envelope'></i>";
					var td = tr.insertCell(0);
					td.className = "notify-tr";
					td.style = "padding-top:10px;padding-bottom:10px;";
					td.innerHTML = "<span class='notify-name'>"+n.title+"</span><span class='notify-summary'>"+n.filter+"["+n.nid+"]</span>";
					
					td = tr.insertCell(1);
					td.width = 50;
					td.align = "center";
					td.className = "notify-tr";
					td.innerHTML = "<span class='notify-time' title='"+n.time+"'>"+n.prettytime+"</span><br/>"+icon;
				}
				countNotifes = response.notifyTips;
				if( response.notifyTips > 0 )
				{
					document.getElementById("badge-mytasks").innerHTML = response.notifyTips;
					document.getElementById("badge-mytasks").style.display = "";
					document.getElementById("badge-notifies").innerHTML = response.notifyTips;
					document.getElementById("badge-notifies").style.display = "";
				}
				else
				{
					document.getElementById("badge-mytasks").style.display = "none";
					document.getElementById("badge-notifies").style.display = "none";
				}
				setUserActionMemory("tsNotify", response.tsNotify);
				var node = document.getElementById("chat-list");
				var lastNode = document.getElementById("chat-list-first");
				//alert("你有新的消息"+messages.length);
				for(var i = 0; i < messages.length; i+=1 )
				{
					var msg = messages[i];
					if( document.getElementById(msg.id) )
					{
						continue;
					}
					var li = document.createElement("li");
					li.id = msg.id;
					var pullhead = "pull-right";
					var pulltime = "";
					var pullname = "";
					if( account == msg.account )
					{
						li.className = "right clearfix";
						pullname = "pull-right ";
					}
					else
					{
						li.className = "left clearfix";
						pullhead = "pull-left";
						pulltime = "pull-right ";
					}
					var html = "<span class='chat-img "+pullhead+"'>";
					html += "<img src='"+msg.head+"' data-src='"+msg.head+"' data-src-retina='"+msg.head+"' alt='User Avatar' class='img-responsive img-circle' height='50' width='50'>";
		            html += "</span>";
		            html += "<div class='chat-body clearfix'><div class='header'><small class='"+pulltime+"text-muted'><i class='fa fa-clock-o fa-fw'></i> "+msg.prettytime+"</small><strong class='"+pullname+"primary-font' title='"+msg.account+"'>"+msg.username+"</strong></div>";
		            html += "<p>"+msg.content+"</p></div>";
		            li.innerHTML = html;
		            node.insertBefore(li, lastNode);
		            lastNode = li;
				}
				tsMessage = response.tsMessage;
				if( messages.length > 0 && getUserActionMemory("allownMessageTrigger") )
				{
					if( firstDoNotify )
					{
						var triggerSidebarIcon = document.getElementById("triggerSidebarIcon");
						triggerSidebarIcon.className = "fa fa-comments fa-spin";
						var bookmarkTips = document.getElementById("bookmarkTips");
						bookmarkTips.style.display = "none";
						var messageTips = document.getElementById("messageTips");
						messageTips.innerHTML = messages.length;
						messageTips.style.display = "";
					}
					else
					{
						var triggerSidebar = document.getElementById("triggerSidebar");
						triggerSidebar.click();
						document.getElementById("activeChat").click();
						var divChat = document.getElementById("divChat");
			            divChat.scrollTop = divChat.scrollHeight;
					}
				}

				var h = document.getElementById("chatHeader").clientHeight;
				h += document.getElementById("chatTabs").clientHeight;
				h += document.getElementById("chatTypeMessage").clientHeight;
				h = _windowHeight - h;
				var divChat = document.getElementById("divChat");
				divChat.style.height = h+"px";
				if( firstDoNotify ) doNotify();//第一次启动通知程序后不再重复启动
				firstDoNotify = false;
				window.setTimeout("handleDescktopTips()",15000);
			}
			catch(e)
			{
				skit_alert(e, "异常提示", function(){
					window.setTimeout("handleDescktopTips()",15000);
				});
			}
		},
		timeout:10000,
		errorHandler:function(message) {
			skit_alert("处理桌面消息出现异常"+message+",可能是因为网络原因，点击确定您的桌面将尝试重连。", "异常提示", function(){
				window.setTimeout("handleDescktopTips()",15000);
			});
		}
	});	
}

function switchBar(a)
{
	if( a.id == "activeView" )
	{
		document.getElementById("chatTitle").innerHTML="已打开视图书签";
	}
	else if( a.id == "activeChat" )
	{
		document.getElementById("chatTitle").innerHTML="系统用户互动区";
	}
	else if( a.id == "activeSwitcher" )
	{
		document.getElementById("chatTitle").innerHTML="系统配置区";
	}
	else if( a.id == "activeDevelopers" )
	{
		document.getElementById("chatTitle").innerHTML="开发者社区";
	}
	setUserActionMemory("triggerBarActive", a.id);
}

function openTriggerBar()
{
	var triggerSidebarIcon = document.getElementById("triggerSidebarIcon");
	if( triggerSidebarIcon.className == "fa fa-comments fa-spin" )
	{
		document.getElementById("activeChat").click();
		triggerSidebarIcon.className = "fa fa-bookmark";
		var bookmarkTips = document.getElementById("bookmarkTips");
		bookmarkTips.style.display = "";
		var messageTips = document.getElementById("messageTips");
		messageTips.style.display = "none";
	}
	else
	{
		var id = getUserActionMemory("triggerBarActive");
		if( id ) document.getElementById(id).click();
	}
}
function switchMessageTrigger(chck)
{
	if(chck.checked)
	{
		setUserActionMemory("allownMessageTrigger", true);
	}
	else
	{
		setUserActionMemory("allownMessageTrigger", false);
	}
}
/*打开通知视图*/
function openNotifyView(id)
{
    document.getElementById("dropdown").className='dropdown';
	openView('查看系统消息', 'notify!messenger.action');
	var ifr = window.frames['notify!messenger.action'];
	if( ifr )
	{
		if( ifr.window && ifr.window.viewNotify )
		{
			ifr.window.viewNotify(id);
		}
		else
		{
			window.setTimeout("openNotifyView('"+id+"');",1000);
		}
	}
	//TODO：调用直接打开指定的系统消息
	//openView('查看系统消息：'+li.title, 'notify!view.action?id='+id);
}


/*从缓存队列中取出对象数据进行tips通知*/
var myNotification;
function doNotify()
{
	if (!Notify.needsPermission) {
		var n = notifesWaitForTips.pop();
//alert("Notify.needsPermission="+Notify.needsPermission+"\r\nnotifesWaitForTips="+n);
		if( n )
		{
			myNotification = new Notify(n.filter+"["+n.time+"] "+notifesWaitForTips.length, {
				icon: n.icon,
			    body: n.title,
			    tag: ""+n.nid,
			    notifyShow:  onNotifyShow,
                notifyClose: onNotifyClosed,
                notifyClick: onNotifyClicked,
                notifyError: onNotifyError,
                timeout: 7
			});
			myNotification.show();
		}
		else
		{
			window.setTimeout("doNotify()",10000);//10秒执行一次
		}
	}
	else if (Notify.isSupported()) {
	    Notify.requestPermission(onPermissionGranted, onPermissionDenied);
	}
}

function setAutoUpgrade(chck)
{
	var tip = "你确定要设置系统不自动升级？";
	if( chck.checked )
	{
		tip = "你确定要设置系统自动升级？";
	}
	skit_confirm(tip, function(yes){
		if( yes )
		{
			HelperMgr.setAutoUpgrade(chck.checked,{
				callback:function(tips){
					skit_alert(tips);
				},
				timeout:10000,
				errorHandler:function(message) {
				}
			});	
		}
	});
}

//执行升级下载
function upgrade()
{
	skit_confirm("你确定要执行升级检测？", function(yes){
		if( yes )
		{
			HelperMgr.doUpgradeDownload({
				callback:function(tips){
					skit_progress(tips);
					window.setTimeout("getUpgradeDownloadResult()",500);
				},
				timeout:10000,
				errorHandler:function(message) {
				}
			});
		}
	});
}
//检查升级下载结果，如果有新版本提示用户升级
function getUpgradeDownloadResult()
{
	HelperMgr.getUpgradeDownloadResult({
		callback:function(response){
			if( response.succeed )
			{//表示正在执行升级
				var ifr = window.frames["iProgress"];
				if( ifr && ifr.setProgress )
				{
					ifr.setProgress(response.result, response.message);
				}
				window.setTimeout("getUpgradeDownloadResult()",500);
			}
			else
			{
				if( response.result == 100 )
				{
					skit_confirm(response.message+"<br/>你是否要立刻升级【主界面框架系统】？", function(yes){
						if( yes )
						{
							HelperMgr.doUpgrade({
								callback:function(response){
									if( response.succeed )
									{//表示正在执行升级
										skit_progress(response.message);
										window.setTimeout("getUpgradeResult()",500);
									}
									else
									{
										skit_alert(response.message);
									}
								},
								timeout:10000,
								errorHandler:function(message) {
								}
							});	
						}
					});
				}
				else
				{
					skit_alert(response.message);
				}
			}
		},
		timeout:10000,
		errorHandler:function(message) {
			window.setTimeout("getUpgradeDownloadResult()",500);
		}
	});	
}

function setProgress(result, message)
{
	var ifr = window.frames["iProgress"];
	if( ifr && ifr.setProgress )
	{
		ifr.setProgress(result, message);
	}
}

//检查升级结果
function getUpgradeResult()
{
	HelperMgr.getUpgradeResult({
		callback:function(response){
			if( response.succeed )
			{//表示正在执行升级
				setProgress(response.result, response.message);
				window.setTimeout("getUpgradeResult()",500);
			}
			else
			{
				if( response.result > 100 )
				{
					skit_confirm(response.message+"<br/>你是否要重启【主界面框架系统】让新版本生效？", function(yes){
						if( yes )
						{
							HelperMgr.doUpgradeRetartup({
								callback:function(tips){
									skit_alert(tips);
								},
								timeout:10000,
								errorHandler:function(message) {
								}
							});	
						}
					});
				}
				else
				{
					skit_alert(response.message);
				}
			}
		},
		timeout:10000,
		errorHandler:function(message) {skit_alert("升级出现异常："+message)}
	});	
}
//
function onNotifyShow() {
//alert('notification is shown!');
}
//当通知被关闭后调用
function onNotifyClosed () {
//alert('notification is closed!');
	window.setTimeout("doNotify()",3000);//3秒后执行下一次
}
//当通知被点击后被调用
function onNotifyClicked () {
	var args = onNotifyClicked.arguments;
    if( myNotification )
    {
//alert(myNotification.options.tag+","+myNotification.options.body);
		var id = myNotification.options.tag;
		openNotifyView(id);
    }
}
//当出现错误的时候被调用
function onNotifyError () {
//alert('Error showing notification. You may need to request permission.');
}
//当权限被授予后被调用
function onPermissionGranted() {
    console.log('Permission has been granted by the user');
    doNotify();
}
//当权限被拒绝后调用
function onPermissionDenied() {
    console.warn('Permission has been denied by the user');
}
//关闭删除指定消息
function closeNotify(id)
{
	var tr = document.getElementById(id);
	if( tr )
	{
		countNotifes -= 1;
		document.getElementById("badge-mytasks").innerHTML = countNotifes;
		document.getElementById("badge-notifies").innerHTML = countNotifes;
		if( countNotifes == 0 )
		{
			document.getElementById("badge-mytasks").style.display = "none";
			document.getElementById("badge-notifies").style.display = "none";
		}
		var table = document.getElementById("task-menu-notifies-table");
		table.deleteRow(tr.rowIndex);
	}
}

//检查所有通知消息
function openMessenger()
{
    document.getElementById("dropdown").className='dropdown';
	openView('查看系统消息', 'notify!messenger.action');
}
/*检查所有通知消息
function checkAllNotifes()
{
	openView('我的系统消息', 'notify!manager.action');
	countNotifes = 0;
	document.getElementById("badge-mytasks").style.display = "none";
	document.getElementById("badge-notifies").style.display = "none";
	var node = document.getElementById("dropdown-menu-notifies");
//alert("dropdown-menu-notifies:"+node.childNodes.length+"\r\n"+node.innerHTML);
	for( var i = 0; i < node.childNodes.length; i+=1 )
	{
		var e = node.childNodes[i];
		if( (e.tagName == "LI" || e.tagName == "li" ) && e.id != "dropdown-menu-notifies-last" )
		{
			node.removeChild(e);
			i -= 1;
		}
	}
//alert("[end]dropdown-menu-notifies:"+node.childNodes.length+"\r\n"+node.innerHTML);
	node = document.getElementById("task-menu-notifies");
//alert("task-menu-notifies:"+node.childNodes.lengthh+"\r\n"+node.innerHTML);
	for( var i = 0; i < node.childNodes.length; i+=1 )
	{
		var e = node.childNodes[i];
		if( e.tagName == "DIV" || e.tagName == "div" )
		{
			node.removeChild(e);
			i -= 1;
		}
	}
}
*/
//登录上后调用该方法得到用户的行为记忆
var userActionMemory;
var memory_key;//记忆标识
var memory_value;//记忆值
var memory_idl = true;
userActionMemory["allownMessageTrigger"] = true;
function setUserActionMemory(key, value)
{
	if( value == userActionMemory[key] ) return;
	if( memory_idl )
	{
		memory_idl = false;
		memory_key = key;
		memory_value = value;
		DescktopMgr.setUserActionMemory(account, key, value, {	
			callback:function() {
				memory_idl = true;
				userActionMemory[memory_key] = memory_value;
		//alert("setUserActionMemory["+memory_key+"]:"+userActionMemory[memory_key]);
			},
			timeout:10000,
			errorHandler:function(message) {
				memory_idl = true;
			}
		});
	}
}
//得到用户行为记忆参数
function getUserActionMemory(key, defval)
{
	var a = userActionMemory[key];
	if( a != null || a ) return a;
	return defval?defval:null;
}
//退出
function logout()
{
	window.location = "login!signout.action";
}