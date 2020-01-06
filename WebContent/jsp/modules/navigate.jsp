<%@ page language="java" pageEncoding="UTF-8"%>
<%@ page import="com.focus.cos.web.common.Kit"%>
<%@ taglib uri="/webwork" prefix="ww" %>
<%Kit.noCache(request,response);  %>
<html>
<head>
<link type="text/css" href="skit/ztree/css/zTreeStyle/zTreeStyle.css" rel="stylesheet"/>
<link type="text/css" href="skit/css/bootstrap.css" rel="stylesheet">
<link type="text/css" href="skit/css/bootstrap-tour.min.css" rel="stylesheet">
<style type='text/css'>
</style>
<%=Kit.getDwrJsTag(request,"interface/ModulesMgr.js")%>
<%=Kit.getDwrJsTag(request,"interface/DiggConfigMgr.js")%>
<%=Kit.getDwrJsTag(request,"interface/WeixinMgr.js")%>
<%=Kit.getDwrJsTag(request,"engine.js")%>
<%=Kit.getDwrJsTag(request,"util.js")%>
</head>
<body>
<TABLE style='width:100%;height:100%;margin-top:3px'>
<TR><TD width='250' id='tdLeftNavigate'>
        <div class="panel panel-default" style='border: 1px solid #aaaaaa'>
   			<div class="panel-heading"><i class='skit_fa_icon fa fa-bars'></i>模块子系统配置导航树
   				<ww:if test='sysadmin'>
                <div class='dropdown' style='float:right;right:4px;top:0px;' id='divAdd'>
                	<a href='#' class='dropdown-toggle' data-toggle='dropdown' aria-expanded='false'>
 						<div class='btn btn-outline btn-primary btn-xs'><i class='fa fa-cogs'></i> 配置</div>
	 					<ul class="dropdown-menu pull-right" style='cursor:pointer' >
							<li onclick='configModules();'><a><i class='skit_fa_icon_blue fa fa-cog fa-fw'></i> 配置模块子系统</a></li>
							<li onclick='configDevelopers();'><a><i class='skit_fa_icon_blue fa fa-group fa-fw'></i> 配置系统开发者</a></li>
							<li onclick='configPop3();'><a><i class='skit_fa_icon_blue fa fa-envelope fa-fw'></i> 配置系统邮件服务</a></li>
							<li class="divider"></li>
							<li onclick='importModule();'><a><i class='skit_fa_icon_blue fa fa-upload fa-fw'></i> 导入系统配置</a></li>
							<li onclick='exportAll();'><a><i class='skit_fa_icon_blue fa fa-download fa-fw'></i> 导出整个系统配置</a></li>
						</ul>
					</a>
                </div>
                </ww:if>
   			</div>
   			<div class="panel-body" style='padding: 0px;'>
                <div id='divTree'>
					<ul id='myZtree' class='ztree'></ul>
					<div id="rMenu">
						<ul>
							<li onclick="exportModule();" id='li0'><i class='skit_fa_icon_blue fa fa-cloud-download'></i> 导出模块子系统配置</li>
							<li id='liOpenNewView' onclick="openNewView();"><i class='skit_fa_icon_blue fa fa-arrows-alt'></i> 在新视图打开</li>
						</ul>
					</div>
				</div>
   			</div>
   		</div>	
	</TD>
	<TD style='width:3px;'/>
	<TD valign='top'>
		<div class="well profile" id='divModuleConfig' style='margin-left:0px;margin-top:0px;margin-bottom:5px;'>
		    <div class="col-sm-12">
		        <div class="col-xs-12 col-sm-10">
		        	<h3 id='h2ModuleTitle' style='margin-top:0px;'>系统管理</h3>
		            <p><i class="fa fa-info-circle fa-fw text-muted"></i>
		               <span id='spanh2ModuleDesc' style='color:#afafaf;'>系统缺省生成的子系统模块用于配置系统基础组件程序配置菜单等</span>
		            </p>
		            <p><strong><i class="fa fa-group fa-fw text-muted"></i> 开发团队:  </strong>
		               <span class="tags" id="spanDeveloper"></span>
		               <ww:if test='sysadmin'>
		               <a onclick="setDevelopers()" style='cursor:pointer'><i class='fa fa-gear'></i> 设置</a>
		               </ww:if>
		            </p>
		            <p><strong><i class="fa fa-user fa-fw text-muted"></i> 系统管理员:  </strong>
		               <span id="spanDeveloperContact" onclick='mailto(this)' title=''></span>
		               <strong style='padding-left:10px;'><i class="fa fa-envelope fa-fw text-muted"></i> 系统邮箱:  </strong>
		               <span id="spanPop3Config" class="tags">未配置</span>
		               <ww:if test='sysadmin'>
		               <a onclick="setPop3()" style='cursor:pointer'><i class='fa fa-gear'></i> 设置</a>
		               </ww:if>
		            </p>
		            <p><strong><i class="fa fa-paw fa-fw text-muted"></i> 版本号:  </strong>
			               <span class="tags" id='spanModuleVersion' onclick='viewVersions()' title='点击显示版本详情'>1.0.0.0</span>
		               <a onclick="viewVersions(true)" style='cursor:pointer'><i class='fa fa-eye'></i> 查看</a> 
		               <span id="spanPublishing" style='padding-left:5px;padding-right:5px;'></span>
		            </p>
		        </div>
		        <div class="col-xs-12 col-sm-2 text-center">
		            <figure id='figure'>
		               <img src="images/cmp/60.png" alt="" class="img-responsive" style='width:96px;' id='iSysLogo'>
		                <p></p>
		                <figcaption class="ratings" style='margin-top:0px;text-align:left;'>
	                        <p id='btnPrograms'><button class="btn btn-outline btn-primary btn-ms" style="width:96px;"
	                        	onclick='preupload()'>
	                        	<span class="fa fa-code-fork"></span> 更换图标 </button></p>
		                </figcaption>
		            </figure>
		        </div>
		    </div>
			<div class="col-xs-12 divider text-center" id='divAutoDeploy' style='display:'>
                   <div class="col-xs-12 col-sm-3 emphasis">
                       <h6><strong id='sModuleDiskload'>0个编译工程</strong></h6>
                       <div class="btn-group dropup btn-block">
                         <button type="button" class="btn btn-default" id='btnCompiler' style='width:90%;color:#cfcfcf'
						onclick='doCompiler()'>&nbsp;&nbsp;&nbsp;<span class="fa fa-code"></span> 编译 </button>
                         <button type="button" class="btn btn-default dropdown-toggle"
                         	onclick='dropdownLogs()' style='height:33px;color:#cfcfcf'
                         	title='编译模块子系统'
                         	data-toggle="dropdown" aria-expanded="false">
                           <span class="fa fa-caret-down"></span>
                         </button>
                         <ul class="dropdown-menu pull-right animated fadeInUp" role="menu" id='ulCompiler'></ul>
                       </div>
                   </div>
                   <div class="col-xs-12 col-sm-3 emphasis">
                       <h6><strong id='sModuleDiskload'>0个自动化测试用例</strong></h6>
                       <div class="btn-group dropup btn-block">
                         <button type="button" class="btn btn-default" id='btnDebug' style='width:90%;color:#cfcfcf'
						onclick='doDebug()'>&nbsp;&nbsp;&nbsp;<span class="fa fa-bug"></span> 测试 </button>
                         <button type="button" class="btn btn-default dropdown-toggle"
                         	onclick='dropdownLogs()' style='height:33px;color:#cfcfcf'
                         	title='自动化测试模块子系统'
                         	data-toggle="dropdown" aria-expanded="false">
                           <span class="fa fa-caret-down"></span>
                         </button>
                         <ul class="dropdown-menu pull-right animated fadeInUp" role="menu" id='ulDebug'></ul>
                       </div>
                   </div>
                   <div class="col-xs-12 col-sm-3 emphasis">
                       <h6><strong id='sModuleDiskload'>1台伺服器</strong></h6>
                       <div class="btn-group dropup btn-block">
                         <button type="button" class="btn btn-default" id='btnDeploy' style='width:90%;color:#cfcfcf'
						onclick='doDeploy()'>&nbsp;&nbsp;&nbsp;<span class="fa fa-copy"></span> 部署 </button>
                         <button type="button" class="btn btn-default dropdown-toggle"
                         	onclick='dropdownLogs()' style='height:33px;color:#cfcfcf'
                         	title='自动化测试模块子系统'
                         	data-toggle="dropdown" aria-expanded="false">
                           <span class="fa fa-caret-down"></span>
                         </button>
                         <ul class="dropdown-menu pull-right animated fadeInUp" role="menu" id='ulDeploy'>
                         	<li><a title='' onclick=''><i class='skit_fa_icon fa fa-institution fa-fw'></i>
                         		查看部署历史</a></li>
                         	<li><a title='' onclick=''><i class='skit_fa_icon fa fa-institution fa-fw'></i>
                         		配置系统部署</a></li>
                         </ul>
                       </div>
                   </div>
                   <div class="col-xs-12 col-sm-3 emphasis">
                       <h6><strong id='sModuleDiskload'>发布次数</strong></h6>
                       <div class="btn-group dropup btn-block">
                         <button type="button" class="btn btn-primary" id='btnPublish' style='width:90%'
						onclick='doPublish()'>&nbsp;&nbsp;&nbsp;<span class="fa fa-clipboard"></span> 发布 </button>
                         <button type="button" class="btn btn-primary dropdown-toggle" style='height:33px'
                         	title='发布管理'
                         	data-toggle="dropdown" aria-expanded="false">
                           <span class="fa fa-caret-down"></span>
                         </button>
                         <ul class="dropdown-menu pull-right animated fadeInUp" role="menu" id='ulPublish'>
                         	<li><a title='' onclick=''><i class='skit_fa_icon fa fa-institution fa-fw'></i>
                         		查看发布历史</a></li>
                         	<!-- <li><a title='' onclick=''><i class='skit_fa_icon_gray fa fa-institution fa-fw'></i>
                         		灰度发布发布</a></li>
                         	<li><a title='' onclick=''><i class='skit_fa_icon_blue fa fa-institution fa-fw'></i>
                         		蓝绿发布</a></li>
                         	<li><a title='' onclick=''><i class='skit_fa_icon_red fa fa-institution fa-fw'></i>
                         		发布回滚</a></li>
                         	<li><a title='' onclick=''><i class='skit_fa_icon fa fa-institution fa-fw'></i>
                         		配置系统发布</a></li>-->
                         </ul>
                	</div>
            	</div>
       		</div>
		</div>
  		<iframe name='iDeveloper' id='iDeveloper' class='nonicescroll' style='border:0px solid #eee;margin-left:0px;margin-top:0px;'></iframe>
	</TD>
</TR>
</TABLE>
<div class="panel panel-primary" id='divUploadfile' style='top: 100; left: 100; width:600px; display:none; position: absolute; '>
    <div class="panel-heading">
    	<span id='uploadtitle'>从本地磁盘选择上传程序配置文件</span>
        <div class="panel-menu">
            <button type="button" onclick='closeuploadfile()' data-action="close" class="btn btn-warning btn-action btn-xs"><i class="fa fa-times"></i></button>
        </div>
    </div>
    <div class="panel-body" style="display: block;">
		<input type="file" name="uploadfile" id="SysLogo" class="file-loading"/>
    </div>
</div>
<form>
<input type='hidden' name='id' id='id' value=''>
<input type='hidden' name='menuName' id='menuName' value=''>
<input type='hidden' name='menuUrl' id='menuUrl' value=''>
<input type='hidden' name='sysid' id='sysid' value=''>
<input type='hidden' name='jsonData' id='jsonData' value=''>
<iframe name='iDownload' id='iDownload' style='display:none'></iframe>
</form>
<div id='divPublishTemplate' style='display:none'>
	<div class="panel panel-default">
		<div class="panel-body" style='padding-bottom:0px;'>
			<div class="form-group">
				<span style='color:#afafaf; text-align: left; border-left: 1px solid #eee;border-right: 1px solid #eee;'> #prepublish#</span>
			</div>
			<div class="form-group">
				<div class="input-group">
					<span class="input-group-addon input-group-addon-font">程序版本<span class='fa fa-paw' style='margin-left:6px'></span></span>
					<input class="form-control form-control-font" type="text" id='#version#'
		             	data-title='请按照1.0.0.0格式输入版本号，新版本号要大于前一个版本'
						data-toggle="tooltip" 
						data-placement="bottom"
						data-trigger='manual'
		             	placeholder="1.0.0.0"
					>
					<span class="input-group-addon input-group-addon-font" 
						style='width:160px; color:#afafaf; text-align: left; border-left: 1px solid #eee;border-right: 1px solid #eee;'
						> #lastversion#</span>
				</div>
			</div>
			<div class="form-group">
				<textarea class="form-control form-control-font" rows="4"
					id="#versionremark#" style='color:#66cccc;background-color:#eee;'
				    data-title='系统版本特性描述不少于4个字'
					data-toggle="tooltip" 
					data-placement="bottom"
					data-trigger='manual'
		           	placeholder="请输入当前版本特性说明"></textarea>
			</div>
		</div>
	</div>
</div>
</body>
<SCRIPT type="text/javascript">
var sysadmin = <ww:property value='sysadmin'/>;
var grant = <ww:property value='grant'/>;
function resizeWindow()
{
	var div = document.getElementById( 'divTree' );
	div.style.width = 248;
	div.style.height = windowHeight - 48;
	div = document.getElementById( 'divModuleConfig' );
	div.style.width = windowWidth - 260; 
	var w = div.clientWidth/4 - 96;
	document.getElementById( 'btnDebug' ).style.width = w;
	document.getElementById( 'btnDeploy' ).style.width = w;
	document.getElementById( 'btnPublish' ).style.width = w;
	document.getElementById( 'btnCompiler' ).style.width = w;
	if( div.style.display == "" )
	{
		div = document.getElementById( 'iDeveloper' );
		div.style.width = windowWidth - 256;
		div.style.height = windowHeight - 204 - document.getElementById( 'divAutoDeploy' ).clientHeight;
	}
	else
	{
		div = document.getElementById( 'iDeveloper' );
		div.style.width = windowWidth - 256;
		div.style.height = windowHeight - 8;
	}
	
}
</SCRIPT>
<%@ include file="../../skit/inc/skit_bootstrap.inc"%>
<%@ include file="../../skit/inc/skit_cos.inc"%>
<%@ include file="../../skit/inc/skit_ztree.inc"%>
<style type='text/css'>
.btn.btn-outline.btn-primary:hover {
    background-color: #fff;
    border: 1px solid #455a64;
    color: #455a64;
}
.btn.btn-primary:hover {
    background-color: #5a7582;
}
.btn.btn-primary {
    background-color: #455a64;
    border-color: #455a64;
}
.btn-primary:hover {
    color: #fff;
    background-color: #286090;
    border-color: #204d74;
}
.panel.panel-primary .panel-heading {
    background-color: #455a64;
    border-bottom: 2px solid #1b2428;
    color: #fff;
}
.panel .panel-heading {
    font-size: 12px;
    font-family: "微软雅黑",sans-serif;
    padding: 10px 10px;
	border-bottom: 1px solid transparent;
	border-top-left-radius: 3px;
	border-top-right-radius: 3px;
}
.panel-title {
    font-size: 12px;
    font-family: "微软雅黑",sans-serif;
    display:block;
    width:310px;
    word-break:keep-all;
    white-space:nowrap;
    overflow:hidden;
    text-overflow:ellipsis;
}
.panel .panel-menu {
    float: right;
    right: 30px;
    top: 8px;
    font-weight: 100;
}
.profile span.tags {
    background: <%=(String)session.getAttribute("System-Theme")%>;
    border-radius: 2px;
    color: #fff;
    font-weight: 700;
    padding-left: 10px;
    padding-right: 10px;
    padding-top: 2px;
    padding-bottom: 2px;
    cursor: pointer;
}
</style>
<SCRIPT type="text/javascript">
$( '#divTree' ).niceScroll({
	cursorcolor: '<%=(String)session.getAttribute("System-Theme")%>',
	railalign: 'right',
	cursorborder: "none",
	horizrailenabled: true, 
	zindex: 2001,
	left: '245px',
	cursoropacitymax: 0.6,
	cursorborderradius: "0px",
	spacebarenabled: false 
});
var timestamp = <ww:property value='timestamp'/>;
//########################################################################
	var setting = {
		//editable: false,
		//fontCss : {color:"red"},
		check: {
			enable: false
		},
		callback: {
			onRightClick: onRightClick,
			onClick: onClick,
			onDblClick: onDblClick,
			beforeRemove: beforeRemove,
			beforeExpand: beforeExpand,
			onExpand: onExpand
		},
		view: {
			addDiyDom: addDiyDom,
		}
	};
		
	function beforeRemove(e, treeId, treeNode)
	{
		var node = myZtree.getSelectedNodes()[0];
		if( node.newMenu )
		{
			return true;
		}
		delMenu();
		return false;
	}
	
	function onRightClick(event, treeId, treeNode) {
		if( treeNode.type == "module" )
		{
			document.getElementById( 'li0' ).style.display = "";
		}
		else
		{
			if( treeNode.type != "programs" && treeNode.type != "digg" && treeNode.type != "api@digg" )
			{
				return;
			}
			
			document.getElementById( 'li0' ).style.display = "none";
		}
		
		if (!treeNode && event.target.tagName.toLowerCase() != "button" && $(event.target).parents("a").length == 0) {
			myZtree.cancelSelectedNode();
			showRMenu("root", event.clientX, event.clientY);
		} else if (treeNode && !treeNode.noR) {
			myZtree.selectNode(treeNode);
			showRMenu("node", event.clientX, event.clientY);
		}
	}

	function onClick(event, treeId, treeNode)
	{
		myZtree.expandNode(treeNode, null, null, null, true);
		open(treeNode);
	}
	
	function onDblClick(event, treeId, treeNode){
		if( treeNode.type == "digg" || treeNode.type == "api@digg" ){
			openNewView();	
		}
	}

	function showRemoveBtn(treeId, treeNode){
		return false;
	}
	
	function addDiyDom(treeId, treeNode)
	{
	}

	$(document).ready(function(){
		var json = '<ww:property value="jsonData" escape="false"/>';
		try
		{
			var zNodes = jQuery.parseJSON(json);
			initZtree(zNodes);
			tour();
		}
		catch(e)
		{
			skit_alert("初始化模块子系统开发管理导航目录出现异常: "+e.message+", 行数"+e.lineNumber);
		}
	});
//#########################################################################
function initZtree(zNodes)
{
	$.fn.zTree.init($("#myZtree"), setting, zNodes);
	myZtree = $.fn.zTree.getZTreeObj("myZtree");
	myZtreeMenu = $("#rMenu");
	var moduleid = getUserActionMemory("modules!navigate.action");
	if( !moduleid )
	{
		moduleid = "Sys";
	}
	var node = myZtree.getNodeByParam("id", moduleid);
	if( node )
	{
		myZtree.selectNode(node);
		open(node);
	}
}
</SCRIPT>
<link href="skit/css/fileinput.min.css" rel="stylesheet"/>
<link href="skin/defone/css/simplemodal.css" rel="stylesheet">
<script src="skit/js/fileinput.js"></script>
<script src="skit/js/fileinput_locale_zh.js"></script>
<script src="skin/defone/js/mootools-core-1.3.1.js"></script>
<script src="skin/defone/js/simple-modal.js?v=3"></script>
<script src="skit/js/jquery.inputmask.bundle.min.js"></script>
<script src="skit/js/auto-line-number.js"></script>
<script src="skit/js/bootstrap-tour.min.js"></script>
<SCRIPT type="text/javascript">
var moduleId = "";
var moduleName = "";
var mDisabled = false;
function openNewView()
{
	var nodes = myZtree.getSelectedNodes();
	var node = nodes[0];
	if( node ){
		var parent = node.getParentNode();
		return openView("我的系统开发管理 >> "+parent.name+" >> "+node.name, node.href+"?mode=1&sysid="+moduleId, "fullscreen");
	}
	return null;
}
function openTemplatesEditor()
{
	var nodes = myZtree.getSelectedNodes();
	var node = nodes[0];
	if( node ){
		var parent = node.getParentNode();
		return openView("我的系统开发管理 >> "+parent.name+" >> 元数据模板开发", "diggcfg!explorer.action?mode=1&sysid="+moduleId);
	}
	return null;
}
//根据导航打开对应视图
function open(node)
{
	if( node ){}else {
		var nodes = myZtree.getSelectedNodes();
		node = nodes[0];
	}

	myZtree.expandNode(node, true);
	if( node.abort )
	{
		skit_alert("【系统开发管理】"+node.name+" 功能服务暂不开放，敬请期待");
		var node = myZtree.getNodeByParam("id", moduleId);
		if( node )
		{
			myZtree.selectNode(node);
			open(node);
		}
		return;
	}
	setUserActionMemory("modules!navigate.action", node.id);
	if( node.type == "module" )
	{
		moduleId = node.id;
		moduleName = node.name;
		var title = node.name;
		mDisabled = node.Disabled?true:false;
		if( node.Disabled ) title += " <a style='font-size:12px;cursor:pointer'><i class='skit_fa_icon_red fa fa-minus-circle'></i> 禁用</a>";
		else if( sysadmin ) title += " <a onclick='configModules(\""+node.id+"\")' style='font-size:12px;cursor:pointer'><i class='fa fa-edit'></i> 修改配置</a>";
		document.getElementById( 'h2ModuleTitle' ).innerHTML = title; 
		document.getElementById( 'spanh2ModuleDesc' ).innerHTML = node.SysDescr?node.SysDescr:"N/A"; 
		document.getElementById( 'spanDeveloper' ).innerHTML = node.SoftwareVendor; 
		document.getElementById( 'spanDeveloperContact' ).innerHTML = node.SysContactName+"("+node.SysContact+")";
		var version = node.version;
		var spanModuleVersion = document.getElementById( 'spanModuleVersion' );
		var spanPublishing = document.getElementById( 'spanPublishing' )
		var publishing = node.publishing;
		var tips = "";
		if( publishing )
		{
			if( publishing.status == 0 )
			{
				spanPublishing.innerHTML = "已提交版本["+publishing.version+"]发布申请,待系统管理员审核";
				spanModuleVersion.style.background = "#ffcccc";
			}
			else if( publishing.status == 2 )
			{
				spanPublishing.innerHTML = "已提交版本["+publishing.version+"]发布申请被系统管理员拒绝，原因是"+publishing.reason;
				spanPublishing.style.background = "#ffcc99";
			}
			else if( publishing.status == 3 )
			{
				spanPublishing.innerHTML = "已提交版本["+publishing.version+"]发布申请审核发生异常，原因是"+publishing.reason;
				spanPublishing.style.background = "#ff6633";
			}
		}
		else if( version == "0.0.0.0")
		{
			version = "系统未发布过";
			spanModuleVersion.style.background = "#cfcfcf";
		}
		else
		{
			spanModuleVersion.style.background = "";
		}
		spanModuleVersion.innerHTML = version; 
		document.getElementById( 'spanPop3Config' ).innerHTML = node.POP3Username?node.POP3Username:"未设置"; 
		document.getElementById( 'iSysLogo' ).src = "modulelogo/"+node.id; 
		document.getElementById( 'divModuleConfig' ).style.display = "";
		document.getElementById( 'btnPrograms' ).style.display = node.Disabled?"none":"";
		document.getElementById( 'divAutoDeploy' ).style.display = node.Disabled?"none":"";
	}
	else
	{
		moduleId = node.sysid;
		document.getElementById( 'divModuleConfig' ).style.display = "none";
	}
	document.getElementById( "sysid" ).value = moduleId;
	resizeWindow();
	document.forms[0].method = "POST";
	document.forms[0].action = node.href;
	document.forms[0].target = "iDeveloper";
	document.forms[0].submit();
	document.getElementById( "jsonData" ).value = "";
}

function openModlue()
{
	var node = myZtree.getNodeByParam("id", moduleId);
	if( node )
	{
		myZtree.selectNode(node);
		open(node);
	}
}

function reloadNavigate(id)
{
	setUserActionMemory("modules!navigate.action", id);
	window.parent.reloadView();
}

function configModules(sysid)
{
	hideRMenu();
	document.getElementById( 'divModuleConfig' ).style.display = "none";
	resizeWindow();
	var url = "modules!config.action";//"dev!preset.action?gridxml="+chr2Unicode("/grid/local/modules.xml");
	if( sysid )
	{
		document.getElementById("sysid").value = sysid;
	}
	else document.getElementById("sysid").value = "";
	document.forms[0].method = "POST";
	document.forms[0].action = url;
	document.forms[0].target = "iDeveloper";
	document.forms[0].submit();
}

function setPop3()
{
	if( mDisabled )
	{
		skit_alert("该模块子系统已经被系统管理员禁用，不能再做设置操作");
		return;
	}
	configPop3(moduleId);
}


function configPop3(sysid)
{
	hideRMenu();
	document.getElementById( 'divModuleConfig' ).style.display = "none";
	resizeWindow();
	var url = "modules!pop3.action";
	if( sysid )
	{
		document.getElementById("sysid").value = sysid;
	}
	else document.getElementById("sysid").value = "";
	document.forms[0].method = "POST";
	document.forms[0].action = url;
	document.forms[0].target = "iDeveloper";
	document.forms[0].submit();
}

function setDevelopers()
{
	if( mDisabled )
	{
		skit_alert("该模块子系统已经被系统管理员禁用，不能再做设置操作");
		return;
	}
	configDevelopers(moduleId);
}

function configDevelopers(sysid)
{
	hideRMenu();
	document.getElementById( 'divModuleConfig' ).style.display = "none";
	resizeWindow();
	var url = "modules!developers.action";
	if( sysid )
	{
		document.getElementById("sysid").value = sysid;
	}
	else document.getElementById("sysid").value = "";
	document.forms[0].method = "POST";
	document.forms[0].action = url;
	document.forms[0].target = "iDeveloper";
	document.forms[0].submit();
}

function openConfigWeixinProgram(config)
{
	var tips = "您确认设置系统【"+moduleId+"】微信公众号【"+config.name+"】回调程序["+config.weixinno+"]配置吗？";
	skit_confirm(tips, function(yes){
		if( yes )
		{
			skit_showLoading();
			WeixinMgr.setWeixinProgram(moduleId, config.weixinno, {
				callback:function(rsp) {
					skit_hiddenLoading();
					try
					{
						if( rsp.succeed )
						{
							var node = myZtree.getNodeByParam("id", moduleId+".programs");
							if( node )
							{
								setUserActionMemory("program!open.program-"+moduleId, rsp.result);
								myZtree.selectNode(node);
								open(node);
							}
						}
						skit_alert(rsp.message);
					}
					catch(e)
					{
					}
				},
				timeout:30000,
				errorHandler:function(err) {skit_hiddenLoading(); alert(err); }
			});
		}
	});	
}

function openConfigMenus(sysid)
{
	var node = myZtree.getNodeByParam("id", sysid+".menus");
	if( node )
	{
		myZtree.selectNode(node);
		open(node);
	}
}

function openAddDatasource(sysid, json)
{
	var node = myZtree.getNodeByParam("id", sysid+".datasource");
	if( node )
	{
		if( json ) document.getElementById( "jsonData" ).value = json; 
		myZtree.selectNode(node);
		open(node);
	}
}

function openAddWeixin(sysid, json)
{
	var node = myZtree.getNodeByParam("id", sysid+".weixin");
	if( node )
	{
		if( json ) document.getElementById( "jsonData" ).value = json; 
		myZtree.selectNode(node);
		open(node);
	}
}

function importModule()
{
	hideRMenu();
	skit_confirm("执行系统配置导入会直接覆盖现有子系统配置，为了确保不出错，请考虑先执行导出备份操作。<B>请确认是否执行导入？</B>", function(yes){
		if( yes )
		{
			try
			{
				document.getElementById( 'uploadtitle' ).innerHTML = "上传系统配置文件(*.zip)";
				var divUploadfile = document.getElementById( 'divUploadfile' );
				divUploadfile.style.display = "";
				$("#SysLogo").fileinput({
						language: 'zh', //设置语言
						uploadUrl: 'modules!importconfig.action', //上传的地址
						allowedFileExtensions: ['zip'],//接收的文件后缀
						showUpload: true, //是否显示上传按钮
						showCaption: false,//是否显示标题
						showClose: false,
						showPreview: true,
						browseClass: "btn btn-primary", //按钮样式     
						previewSettings: { image: {width: "auto", height: "128px"} },
						//dropZoneEnabled: false,//是否显示拖拽区域
						minImageWidth: 128, //图片的最小宽度
						minImageHeight: 128,//图片的最小高度
						maxImageWidth: 128,//图片的最大宽度
						maxImageHeight: 128,//图片的最大高度
						//maxFileSize: 0,//单位为kb，如果为0表示不限制文件大小
						//minFileCount: 0,
						maxFileCount: 1, //表示允许同时上传的最大文件个数
						enctype: 'multipart/form-data',
						validateInitialCount:true,
						previewFileIcon: "<i class='glyphicon glyphicon-king'></i>",
						msgFilesTooMany: "选择上传的文件数量({n}) 超过允许的最大数值{m}！",
			            uploadExtraData: function(previewId, index) {   //额外参数的关键点
			                var obj = {};
			                obj.sysid = moduleId;
			                return obj;
			            }
				});
				divUploadfile.style.top = 64;
				divUploadfile.style.left = windowWidth/2 - divUploadfile.clientWidth/2;
				//$( '.file-preview' ).hide();
				$("#SysLogo").on("fileloaded", function (data, previewId, index) {
					var tips = "您确定要上传["+previewId.name+"]的覆盖系统配置吗？";
					document.getElementById( 'uploadtitle' ).innerHTML = tips;
				});
				//导入文件上传完成之后的事件
				$("#SysLogo").on("fileuploaded", function (event, data, previewId, index) {
					if( data.response.alt )
					{
						skit_alert(data.response.alt);
					}
					if( data.response.succeed )
					{
						document.getElementById( 'divUploadfile' ).style.display = "none";
						$("#SysLogo").fileinput("destroy");
						window.location.reload();
					}
				});
			}
			catch (e)
			{
				alert(e);
			}			
		}
	});
}

function exportAll(){
	hideRMenu();
	var node = myZtree.getNodeByParam("id", moduleId);
	if( node )
	{
		skit_confirm("您确认要导出整个系统配置到本地？", function(yes){
			if( yes )
			{
				document.forms[0].action = "modules!exportconfig.action?all=1";
				document.forms[0].method = "POST";
				document.forms[0].target = "iDownload";
				document.forms[0].submit();
			}
		});
	}
}

function exportModule()
{
	hideRMenu();
	var node = myZtree.getNodeByParam("id", moduleId);
	if( node ) {
		window.top.skit_input("导出模块字系统【"+node.name+"】配置", "", function(yes, val){
			if( yes ){
				var url =  "modules!exportconfig.action";
				if( val && val != node.id )
				{
					url += "?new_sysid="+val;
				}
				document.forms[0].action = url;
				document.forms[0].method = "POST";
				document.forms[0].target = "iDownload";
				document.forms[0].submit();
			}
		}, "如果需要更改模块ID，输入新的ID名称");
	}
}
function addDeveloper()
{
	skit_alert("敬请期待.");
}
function addDeveloper()
{
	skit_alert("敬请期待.");
}
function addDatasource()
{
	skit_alert("敬请期待.");
}
function doCompiler()
{
	skit_alert("功能暂不开放敬请期待.");
}
function doDebug()
{
	skit_alert("功能暂不开放敬请期待.");
}
function doDeploy()
{
	skit_alert("功能暂不开放敬请期待.");
}

function preupload()
{
	try
	{
		document.getElementById( 'uploadtitle' ).innerHTML = "上传系统LOGO图标(*.png)";
		var divUploadfile = document.getElementById( 'divUploadfile' );
		divUploadfile.style.display = "";
		$("#SysLogo").fileinput({
				language: 'zh', //设置语言
				uploadUrl: 'modules!uplogo.action', //上传的地址
				allowedFileExtensions: ['png'],//接收的文件后缀
				showUpload: true, //是否显示上传按钮
				showCaption: false,//是否显示标题
				showClose: false,
				showPreview: true,
				browseClass: "btn btn-primary", //按钮样式     
				previewSettings: { image: {width: "auto", height: "128px"} },
				//dropZoneEnabled: false,//是否显示拖拽区域
				minImageWidth: 128, //图片的最小宽度
				minImageHeight: 128,//图片的最小高度
				maxImageWidth: 128,//图片的最大宽度
				maxImageHeight: 128,//图片的最大高度
				//maxFileSize: 0,//单位为kb，如果为0表示不限制文件大小
				//minFileCount: 0,
				maxFileCount: 1, //表示允许同时上传的最大文件个数
				enctype: 'multipart/form-data',
				validateInitialCount:true,
				previewFileIcon: "<i class='glyphicon glyphicon-king'></i>",
				msgFilesTooMany: "选择上传的文件数量({n}) 超过允许的最大数值{m}！",
	            uploadExtraData: function(previewId, index) {   //额外参数的关键点
	                var obj = {};
	                obj.sysid = moduleId;
	                return obj;
	            }
		});
		divUploadfile.style.top = 64;
		divUploadfile.style.left = windowWidth/2 - divUploadfile.clientWidth/2;
		//$( '.file-preview' ).hide();
		$("#SysLogo").on("fileloaded", function (data, previewId, index) {
			var tips = "您确定要上传["+previewId.name+"]作为系统［"+moduleName+"］的ＬＯＧＯ吗？";
			document.getElementById( 'uploadtitle' ).innerHTML = tips;
		});
		//导入文件上传完成之后的事件
		$("#SysLogo").on("fileuploaded", function (event, data, previewId, index) {
			if( data.response.alt )
			{
				skit_alert(data.response.alt);
			}
			if( data.response.succeed )
			{
				document.getElementById("iSysLogo").src = data.response.url;
				document.getElementById( 'divUploadfile' ).style.display = "none";
				$("#SysLogo").fileinput("destroy");
			}
		});
	}
	catch (e)
	{
		alert(e);
	}
}

function closeuploadfile()
{
	document.getElementById( 'divUploadfile' ).style.display = "none";
	$("#SysLogo").fileinput("destroy");
}

function doPublish()
{
	var nodes = myZtree.getSelectedNodes();
	var node = nodes[0];
	var publishing = node.publishing;
	var tips = "";
	var spanPublishing = document.getElementById( 'spanPublishing' )
	if( publishing )
	{
		tips = spanPublishing.innerHTML +"。您可以相同或不同版号重新提交申请。";
	}
	else
	{
		if( node.version == "0.0.0.0" ) tips = "您的系统［"+moduleName+"］将首次发布，提交发布申请系统管理员审批。";
		else tips = "您的系统［"+moduleName+"］当前版本是[ "+node.version+" ]，您是否提交新的发布申请？";
	}
	skit_confirm(tips, function(yes){
		if( yes )
		{
			document.getElementById( 'divModuleConfig' ).style.display = "none";
			resizeWindow();
			var url = "modules!prepublish.action";
			document.forms[0].method = "POST";
			document.forms[0].action = url;
			document.forms[0].target = "iDeveloper";
			document.forms[0].submit();
		}
		//else confirmPublish();
	});	
}

var iVersion;
var iRemark;
function confirmPublish(m, p)
{
	try
	{
		var nodes = myZtree.getSelectedNodes();
		var node = nodes[0];
		var lastversion = node.version;
		var template = document.getElementById("divPublishTemplate").innerHTML;
		template = template.replace("#version#", "iVersion");
		template = template.replace("#lastversion#", "系统当前版本: "+lastversion);
		template = template.replace("#versionremark#", "iRemark");
		template = template.replace("#prepublish#", "系统待发布菜单配置项"+m+"，程序配置项"+p);
		var SM = new SimpleModal({"btn_ok":"确定","btn_cancel":"取消","width":520});
	    var content = "<div style='height:256px;width:480px;border:0px solid red;overflow-y:auto;'>"+template+"</div>"
	    SM.show({
	    	"title":"发布系统［"+moduleName+"］",
	        "model":"confirm",
	        "callback": function(){
	        	var version = iVersion.value;
	            if( version == "" )
	            {
	        		$("#iVersion").tooltip("show");
	        		document.getElementById( "iVersion" ).focus();
	            	return false;
	            }
	            var regexp2 = "^(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9]|0)\\."+
	            			   "(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9]|0)\\."+
	            			   "(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9]|0)\\."+
	            			   "(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[0-9])$";
	            var m2 = version.match(new RegExp(regexp2));
	            if( !m2 )
	            {
	        		$("#iVersion").tooltip("show");
	        		document.getElementById( "iVersion" ).focus();
			        return false;
	            }
	        	var args1 = version.split(".");
	        	var args0 = lastversion.split(".");
	        	var v1 = Number(args1[0])*1000000+Number(args1[1])*10000+Number(args1[2])*100+Number(args1[3]);
	        	var v0 = Number(args0[0])*1000000+Number(args0[1])*10000+Number(args0[2])*100+Number(args0[3]);
	            if( v1 <= v0 )
	            {
	        		$("#iVersion").tooltip("show");
	        		document.getElementById( "iVersion" ).focus();
	            	return false;
	            }

	            var remark = iRemark.value;
	            if( remark.length < 4 )
	            {
	        		$("#iRemark").tooltip("show");
	        		document.getElementById( "iRemark" ).focus();
	            	return false;
	            }
				skit_showLoading();
				try
				{
					ModulesMgr.publish(moduleId, moduleName, version, lastversion, remark, {
						callback:function(rsp) {
							skit_hiddenLoading();
							skit_alert(rsp.message);
							if( rsp.succeed )
							{
								var publishing = new Object();
								publishing.status = 0;
								publishing.version = version;
								node["publishing"] = publishing;
							}
							myZtree.selectNode(node);
							open(node);
						},
						timeout:30000,
						errorHandler:function(err) {skit_hiddenLoading(); alert(err); }
					});
				}
				catch(e)
				{
					alert(e);
				}
	        	return true;
	        },
	        "cancelback": function(){
	        },
	    	"contents": content
	    });
	    var h = windowHeight;
	    var w = windowWidth;
	    var div = document.getElementById('simple-modal');
	    var top = h/2 - div.scrollHeight/2;
	    SM.options.offsetTop = top;
	    SM._display();
    	iRemark = document.getElementById("iRemark");
    	iRemark.onkeydown=function(event){ 
    		$("#iRemark").tooltip("hide");
    	}
    	iVersion = document.getElementById("iVersion");
    	iVersion.onkeydown=function(event){ 
    		$("#iVersion").tooltip("hide");
    	}
    	$("#iVersion").inputmask('ip');//inputmask("mask", {"mask": "9.9.9.9"});

    	var publishing = node.publishing;
    	if( publishing )
    	{
    		iVersion.value = publishing.version;
    		iRemark.value = publishing.remark;
    	}
    	else
    	{
    		iVersion.value = node.version;
    	}
	}
	catch(e)
	{
		alert(e);
	}
}

function viewVersions(fullscreen)
{
	var dataurl = "modules!versiontimeline.action?sysid="+moduleId;
	dataurl = chr2Unicode(dataurl);
	document.getElementById("iDeveloper").src = "helper!timeline.action?dataurl="+dataurl;
	if( fullscreen )
	{
		openView("系统【"+moduleName+"】版本详情", "helper!timeline.action?dataurl="+dataurl);
	}
}

function cancelFullscreen()
{
	document.getElementById( 'tdLeftNavigate' ).style.display = "none";
}

function doFullscreen()
{
	document.getElementById( 'tdLeftNavigate' ).style.display = "none";
}

function publishMenu(name, url)
{
	var id = moduleId+".menus";
	var node = myZtree.getNodeByParam("id", id);
	if( node )
	{
		document.getElementById( "menuName" ).value = name;
		document.getElementById( "menuUrl" ).value = url;
		myZtree.selectNode(node);
		open(node);
	}
}

function testDatasource(sysid, name, title)
{
	DiggConfigMgr.testDatasource(sysid, name, {
		callback:function(rsp) {
		    var ifr = window.frames["iDeveloper"];
		    if( ifr && ifr.setTestResult )
		    {
				ifr.setTestResult(name, rsp.succeed, rsp.message, title+"("+name+")");
		    }
		},
		timeout:60000,
		errorHandler:function(err) {
		    var ifr = window.frames["iDeveloper"];
		    if( ifr && ifr.setTestResult )
		    {
				ifr.setTestResult(name, false, "执行数据源连接测试出现错误:"+err, title+"("+name+")");
		    }
		}
	});
}

function tour()
{
	var showTour = getUserActionMemory("tourDigg");
	if( showTour != "1" )
	{
		var node = myZtree.getNodeByParam("type", "digg");
		if( node )
		{
			myZtree.selectNode(node);
		}
		setUserActionMemory("tourDigg", "1");
		var tour = new Tour({
		    steps: [{
		        element: "#"+node.tId,
		        title: "元数据模板开发",
		        content: "双击可打开【元数据模板开发】全屏视图，获得更好的编辑体验。"
		    }],
		    container: 'body',
		    backdrop: true,
		    keyboard: true,
		    storage: false
		});
		// Initialize the tour
		tour.init();
		myZtree.cancelSelectedNode(node);
		// Start the tour
		tour.start();
	}
}
</SCRIPT>
</html>