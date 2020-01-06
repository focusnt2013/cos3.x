<%@ page language="java" pageEncoding="UTF-8"%>
<%@ page import="com.focus.cos.web.common.Kit"%>
<%@ taglib uri="/webwork" prefix="ww" %>
<%Kit.noCache(request,response);  %>
<html>
<head>
<link type="text/css" href="skit/ztree/css/zTreeStyle/zTreeStyle.css" rel="stylesheet"/>
<link type="text/css" href="skit/css/bootstrap-tour.min.css" rel="stylesheet">
<style type='text/css'>
</style>
<%=Kit.getDwrJsTag(request,"interface/DiggConfigMgr.js")%>
<%=Kit.getDwrJsTag(request,"engine.js")%>
<%=Kit.getDwrJsTag(request,"util.js")%>
<SCRIPT type="text/javascript">
</SCRIPT>
</head>
<body style='overflow-y:hidden;padding-top:0px;padding-left:0px;'>
<div class="alert alert-warning" id='divTour' style='display:none'>
    <a href="#" class="close" data-dismiss="alert">&times;</a>
    <span id='spanAlert'><strong>提示！</strong>点击右侧下拉菜单，选择您要查看的【元数据模板】产生的数据、单元格对象定义，以及全局脚本、预处理脚本、渲染器脚本。</span>
</div>
<div class="panel panel-default" style='border: 1px solid #efefef' id='divDiggConfig'>
	<div class="panel-heading">
		<span id='spanTitle'><i class='skit_fa_btn fa fa-digg'></i>管理系统【<ww:property value='sysname'/>】的模板</span>
    	<div style='float:right;right:4px;top:0px;display:none' id='divAddQuery'>
 			<button type="button" class='btn btn-outline btn-info btn-xs' onclick='presetQueryTemplate()'>
 				<i class='fa fa-plus-circle'></i> 新增查询模板</button>
        </div>
    	<div style='float:right;padding-right:4px;top:0px;display:none' id='divAddConfig'>
 			<button type="button" class='btn btn-outline btn-success btn-xs' onclick='presetConfigTemplate()'>
 				<i class='fa fa-plus-circle'></i> 新增配置模板</button>
        </div>
    	<div style='float:right;right:4px;top:0px;display:none' id='divSave'>
 			<button type="button" class='btn btn-outline btn-success btn-xs' onclick='presaveTemplate()'>
 				<i class='fa fa-save'></i> 保存</button>
        </div>
        <div style='float:right;padding-right:4px;top:0px;display:none' id='divCancel'>
 			<button type="button" class="btn btn-outline btn-danger btn-xs" onclick='cancelSaveTemplate()'>
 				<i class='fa fa-close'></i> 取消</button>
        </div>
        <div style='float:right;padding-right:4px;top:0px;display:none' id='divUpload'>
 			<button type="button" class="btn btn-outline btn-primary btn-xs" onclick='preuploadTemplate()'>
 				<i class='fa fa-upload'></i> 导入模板</button>
        </div>
        <div style='float:right;padding-right:4px;top:0px;display:none' id='divDebug'>
 			<button type="button" class="btn btn-outline btn-warning btn-xs" onclick='testTemplate()'>
 				<i class='fa fa-bug'></i> 检测</button>
        </div>
        <div style='float:right;padding-right:4px;top:0px;display:none' id='divPreview'>
 			<button type="button" class="btn btn-outline btn-info btn-xs" onclick='previewTemplateEdit()'>
 				<i class='fa fa-bug fa-spin'></i> 调测</button>
        </div>
        <div style='float:right;padding-right:4px;top:0px;display:none' id='divPreviewData'>
 			<button type="button" class="btn btn-outline btn-info btn-xs" onclick='previewTemplateEdit(1)'>
 				<i class='fa fa-bug fa-spin'></i> 调测(数据)</button>
        </div>
        <div style='float:right;padding-right:4px;top:0px;display:none' id='divCompare' title='编辑前后对比'>
 			<button type="button" class="btn btn-outline btn-default btn-xs" onclick='doCompareTemplate()'>
 				<i class='fa fa-columns'></i> 比较</button>
        </div>
        <div style='float:right;padding-right:4px;top:0px;display:none' id='divVersions' title='关闭版本查看'>
 			<button type="button" class="btn btn-outline btn-danger btn-xs" onclick='closeVersions()'>
 				<i class='fa fa-paw'></i> 隐藏版本视图</button>
        </div>
        <div style='float:right;padding-right:4px;top:0px;display:none' id='divFullscreen'>
 			<button type="button" class="btn btn-outline btn-default btn-xs" onclick='doFullscreen(this)' id='btnFullscreen'>
 				<i class='fa fa-arrows-alt'></i> 全屏</button>
        </div>
        <div style='float:right;padding-right:4px;top:0px;display:none' id='divDiggLink'>
 			<button type="button" class="btn btn-outline btn-success btn-xs" onclick='getDiggLink(this)' id='btnDiggLink'>
 				<i class='fa fa-digg'></i> 提取DIGG链接</button>
        </div>
        <div style='float:right;padding-right:4px;top:0px;display:none' id='divFormLink'>
        	<div class="btn-group btn-block">
				<button type="button" class="btn btn-outline btn-success btn-xs" style='width:80%;font-size:12px;padding-left:8px;padding-right:16px;'
				  onclick='getFormLink(this)' id='btnFormLink'><span class="fa fa-digg"></span>提取FORM链接</button>
	 			 <button type="button" class="btn btn-outline btn-success btn-xs dropdown-toggle" style='height:22px'
			    	data-toggle="dropdown" aria-expanded="false">
			      <span class="fa fa-caret-down"></span>
			    </button>
			    <ul class="dropdown-menu multi-level" role="menu" id='ulForm'>
		    		<li><a title='' onclick="getFormLink('open')"><i class='skit_fa_icon fa fa-plus fa-fw'></i>新开模式链接</a></li>
		    		<li><a title='' onclick="getFormLink('edit')"><i class='skit_fa_icon fa fa-edit fa-fw'></i>编辑模式链接</a></li>
		    		<li><a title='' onclick="getFormLink('preview')"><i class='skit_fa_icon fa fa-eye fa-fw'></i>预览模式链接</a></li>
			    </ul>
        	</div>
        </div>
    </div>
	<div class="panel-body" style='padding: 0px;'>
	<TABLE>
	<TR class='unline'><TD width='250' valign='top' id='tdTree'>
			<div id='divTree' style='border: 0px solid red'>
				<ul id='myZtree' class='ztree'></ul>
				<div id="rMenu">
					<ul>
						<li onclick="checkAllTemplates(true);" id='liCheckAll'><i class='skit_fa_icon_red fa fa-stethoscope'></i> 检测所有模板</li>
						<li onclick="querySql();" id='liQuerySql'><i class='skit_fa_icon_blue fa fa-database'></i> 数据库表查询</li>
						<li onclick="presetQueryTemplate();" id='liAddQueryTemplate'><i class='skit_fa_icon_blue fa fa-plus-circle'></i> 新增查询模板</li>
						<li onclick="presetConfigTemplate();" id='liAddConfigTemplate'><i class='skit_fa_icon fa fa-plus-circle'></i> 新增配置模板</li>
						<li onclick="copyTemplate();" id='liCopyDiggConfig'><i class='skit_fa_icon fa fa-copy'></i> 复制</li>
						<li onclick="delTemplate();" id='liDeleteTemplate'><i class='skit_fa_icon_red fa fa-minus-circle'></i> 删除</li>
						<li onclick="preuploadTemplate();" id='liUploadTemplate'><i class='skit_fa_icon_blue fa fa-upload'></i> 上传模板</li>
						<li onclick="downloadTemplate();" id='liDownloadTemplate'><i class='skit_fa_icon_blue fa fa-download'></i> 下载模板</li>
						<li onclick="preuploadTemplates();" id='liUploadTemplates'><i class='skit_fa_icon fa fa-cloud-upload'></i> 导入模板包</li>
						<li onclick="downloadTemplates();" id='liDownloadTemplates'><i class='skit_fa_icon fa fa-cloud-download'></i> 导出模板包</li>
						<li class="divider" id='liDivider0'></li>
						<li onclick="addTemplateDir();" id='liAddTemplateDir'><i class='skit_fa_icon_blue fa fa-plus-circle'></i> 新增目录</li>
						<li onclick="openVersions();" id='liOpenVersions'><i class='skit_fa_icon_blue fa fa-paw'></i> 查看版本</li>
						<li onclick="publishMenus();" id='liPublishMenus'><i class='skit_fa_icon_blue fa fa-copy'></i> 发布目录菜单组</li>
					</ul>
				</div>
			</div>
		</TD>
		<TD valign='top' valign='top'>
			<ww:if test='developer'>
			<div id="tabL" style="position:relative;display:">
				<ul id='ulTab'>
				</ul>
			</div>
			</ww:if>
			<ww:else>
			<%@ include file="explorer_well.inc"%>
	 		<iframe name='diggXmlEditor' id='diggXmlEditor' scr='editor!xml.action' class='nonicescroll' style='border:0px solid #eee;margin-left:3px;margin-top:3px;margin-bottom:3px;'></iframe>
			</ww:else>
	 		<iframe name='iDigg' id='iDigg' class='nonicescroll' style='border:0px solid #eee;margin-left:3px;margin-top:3px;margin-bottom:3px;'></iframe>
		</TD>
	</TR>
	</TABLE>
	</div>
</div>
<%@ include file="explorer.inc"%>
<form id='form0'>
<input type='hidden' name='timestamp' id='timestamp'>
<input type='hidden' name='editorContent' id='content'>
<input type='hidden' name='compareLeftTitle' id='compareLeftTitle'>
<input type='hidden' name='compareRightTitle' id='compareRightTitle'>
<input type='hidden' name='compareLeft' id='compareLeft'>
<input type='hidden' name='compareRight' id='compareRight'>
<input type='hidden' name='gridxml' id='gridxml'>
<input type='hidden' name='gridtext' id='gridtext'>
<input type='hidden' name='version' id='version'>
<input type='hidden' name='oldversion' id='oldversion'>
<input type='hidden' name='remark' id='remark'>
<input type='hidden' name='account' id='account' value="<ww:property value='account'/>">
<input type='hidden' name='id' id='id'>
<input type='hidden' name='sysid' id='sysid' value="<ww:property value='sysid'/>">
<input type='hidden' name='db' id='db' value="<ww:property value='db'/>">
<input type='hidden' name='filetype' id='filetype' value="<ww:property value='filetype'/>">
<iframe name='iDownload' id='iDownload' style='display:none'></iframe>
</form>
</body>
<script type="text/javascript">
var grant = <ww:property value='grant'/>;//管理程序的权限
var developer = <ww:property value='developer'/>
var sysid = "<ww:property value='sysid'/>";
var fullscreen, viewversions;
<ww:if test='developer'>
var ifrid;//当前打开的视图
function resizeWindow()
{
	var w = 250, h = 0;
	var div = document.getElementById( 'divTree' );
	if( document.getElementById( 'tdTree' ).style.display == "" )
	{
		div.style.width = 248;
		div.style.height = windowHeight - 38;
	}
	else {
		w = 16;
	}
	w = windowWidth - w;
	div = document.getElementById( 'tabL' );
	div.style.width = w;

	var iDigg = document.getElementById( 'iDigg' );
	if( iDigg.style.display == "" )
	{
		iDigg.style.width = w;
		iDigg.style.height = 256;
		h = 256;
	}
	
	if( ifrid )
	{
		h += 64;
		div = document.getElementById( ifrid );
		div.style.height = windowHeight - h;
	}
	
}
</ww:if>
<ww:else>
function resizeWindow()
{
	var	_windowWidth = windowWidth;
	var userAgent = navigator.userAgent; //取得浏览器的userAgent字符串
	var isChrome = userAgent.indexOf("Chrome") > -1;
	var div = null;
	if( document.getElementById( 'tdTree' ).style.display == "" )
	{
		div = document.getElementById( 'divTree' );
		div.style.width = 248;
		div.style.height = windowHeight - 38;
	}

	var h = 0, w = 0;
	if( isChrome )
	{
		document.body.style.marginTop = 3;
		h -= 5;
	}
	div = document.getElementById( 'divDiggProfile' );
	if( div.style.display == "" )
	{
		h += 280;
		div.style.width = _windowWidth - 280; 
		var w = div.clientWidth/4 - 80;
		document.getElementById( 'btnEdit' ).style.width = w;
		document.getElementById( 'btnPreview' ).style.width = w;
		document.getElementById( 'btnPublish' ).style.width = w;
		document.getElementById( 'btnTest' ).style.width = w;
	}
	else
	{
		h = 42;
	}
	var iDigg = document.getElementById( 'iDigg' );
	var diggXmlEditor = document.getElementById( 'diggXmlEditor' );
	h = windowHeight - h;
	if( iDigg.style.display == "" && diggXmlEditor.style.display == "" )
	{
		h /= 2;
	}
	w = fullscreen?_windowWidth:(_windowWidth - 256);
	iDigg.style.width = w;
	diggXmlEditor.style.width = w;
	iDigg.style.height = h;
	diggXmlEditor.style.height = h;
}
//管理模式下打开节点
function open(node)
{
	if( "datasource" != node.type && "table" != node.type )
		setUserActionMemory("diggcfg!manager."+sysid, node.id);
	//alert(node.type+", "+node.id);
	document.getElementById( 'divFullscreen' ).style.display = "none";
	document.getElementById( 'divDiggLink' ).style.display = "none";
	document.getElementById( 'divFormLink' ).style.display = "none";
	document.getElementById( 'divVersions' ).style.display = "none";
	document.getElementById( 'divAddQuery' ).style.display = "none";
	document.getElementById( 'divAddConfig' ).style.display = "none";
	document.getElementById( 'divSave' ).style.display = "none";
	document.getElementById( 'divCancel' ).style.display = "none";
	document.getElementById( 'divUpload' ).style.display = "none";
	document.getElementById( 'divDebug' ).style.display = "none";
	document.getElementById( 'divPreview' ).style.display = "none";
	document.getElementById( 'divAddQuery' ).style.display = "none";
	document.getElementById( 'divAddConfig' ).style.display = "none";
	closePresaveTemplate();
	var iDigg = document.getElementById( 'iDigg' );
	var divDiggProfile = document.getElementById( 'divDiggProfile' );
	divDiggProfile.style.display = "none";
	iDigg.style.display = "";
	
	var spanTitle = document.getElementById( 'spanTitle' );
	spanTitle.innerHTML = "<i class='skit_fa_btn fa fa-digg'></i>元数据管理(编辑、测试、预览以及发布元数据查询、配置模板，通过模板快速实现对数据的有效管理)";
	var diggXmlEditor = document.getElementById( 'diggXmlEditor' );
	if( "dir" == node.type )
	{
		diggXmlEditor.style.display = "none";
		if( node.demo )
		{
			//myZtree.expandNode(treeNode, null, null, null, true);
			document.forms[0].method = "POST";
			document.forms[0].action = "helper!developer.action";
			document.forms[0].target = "iDigg";
			document.forms[0].submit();
		}
		else
		{
			document.getElementById("id").value = node.id;
			document.forms[0].method = "POST";
			document.forms[0].action = "diggcfg!query.action";
			document.forms[0].target = "iDigg";
			document.forms[0].submit();
		}
		myZtree.expandNode(node, true, false);
	}
	else
	{
		spanTitle.innerHTML = "<i class='skit_fa_icon fa fa-table'></i> 元数据模板【"+node.name+" "+node.title+"】";
		spanTitle.title = node.id;
		diggXmlEditor.style.display = "none";
		divDiggProfile.style.display = "";
		document.getElementById( 'h2DiggTitle' ).innerHTML = node.title;
		document.getElementById( 'spanDiggName' ).innerHTML = node.cname;
		document.getElementById( 'spanTimestamp' ).innerHTML = node.createtime;
		document.getElementById( 'btnTest' ).disabled = false;
		document.getElementById( 'btnTest1' ).disabled = false;
		document.getElementById( 'btnPreview' ).disabled = false;
		document.getElementById( 'btnPreview1' ).disabled = false;
		document.getElementById( 'gridxml' ).value = node.id;
		if( node.demo )
		{
			document.getElementById( 'iDiggState' ).className = "fa fa-support";
			document.getElementById( 'pDiggState' ).innerHTML = "Demo模板";
			document.getElementById( 'btnEdit' ).disabled = true;
			document.getElementById( 'btnEdit1' ).disabled = true;
			document.getElementById( 'btnPublish' ).disabled = true;
			document.getElementById( 'btnPublish1' ).disabled = true;
			document.getElementById( 'spanDiggVersion' ).innerHTML = "N/A";
			document.getElementById( 'spanDigger' ).innerHTML = "系统内置";
		}
		else
		{
			setTemplateCheck(node);
			document.getElementById( 'btnEdit' ).disabled = false;
			document.getElementById( 'btnEdit1' ).disabled = false;
			document.getElementById( 'btnPublish' ).disabled = false;
			document.getElementById( 'btnPublish1' ).disabled = false;
			document.getElementById( 'spanDiggVersion' ).innerHTML = node.version?node.version:"N/A";
			document.getElementById( 'spanDigger' ).innerHTML = node.developer?node.developer:"N/A";
		}
		document.getElementById( 'spanDiggId' ).innerHTML = node.id;
		viewVersions();
		//previewTemplateStyle();
	}
	resizeWindow();
}
</ww:else>
</script>
<link href="skin/defone/css/simplemodal.css" rel="stylesheet">
<link href="skit/css/fileinput.min.css" rel="stylesheet"/>
<%@ include file="../../skit/inc/skit_bootstrap.inc"%>
<%@ include file="../../skit/inc/skit_cos.inc"%>
<%@ include file="../../skit/inc/skit_ztree.inc"%>
<script src="skin/defone/js/mootools-core-1.3.1.js"></script>
<script src="skin/defone/js/simple-modal.js?v=3"></script>
<script src="skit/js/fileinput.js"></script>
<script src="skit/js/fileinput_locale_zh.js"></script>
<script src="skit/js/jquery.md5.js"></script>
<script src="skit/js/jquery.inputmask.bundle.min.js"></script>
<script src="skit/js/bootstrap-tour.min.js"></script>
<style type='text/css'>
body {
}
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
    padding: 5px 10px;
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
    padding: 2px 4px;
}
.xml_edit
{
	width: 100%;
	font-size: 12px;
	font-family:微软雅黑,Roboto,sans-serif;
	margin:0 0 1px;
	color: #ffffff;
	background-color: #373737;
}
.ui-tabs .ui-tabs-nav {
    margin: 0;
    padding-top: 2px;
    padding-right: 1px;
    padding-bottom: 0px;
    padding-left: 1px;
}
.ui-tabs .ui-tabs-nav li a {
    float: left;
    padding-top: 3px;
    padding-right: 5px;
    padding-bottom: 3px;
    padding-left: 5px;
    text-decoration: none;
    font-size: 8pt;
}
.ui-tabs .ui-tabs-panel {
    display: block;
    border-width: 0;
    padding-top: 3px;
    padding-right: 1px;
    padding-bottom: 1px;
    padding-left: 1px;
    background: none;
}
</style>
<SCRIPT type="text/javascript">
function testTemplate()
{
	var node = getNode();
	if( node.editmode )
	{
		presaveTemplate(true);
	}
	else
	{
		doTest();
	}
}
<%@ include file="explorer.js"%>
function previewTemplateScript()
{
	var node = getNode();
	var iDigg = document.getElementById( 'iDigg' );
	var divDiggProfile = document.getElementById( 'divDiggProfile' );
	DiggConfigMgr.getTemplateXml(node.id, {
		callback:function(rsp) {
			if( rsp.succeed ) {
				if( rsp.message ) skit_alert(rsp.message);
				var diggXmlEditor = document.getElementById( 'diggXmlEditor' );
				setDiggXmlEditorValue(rsp.result);
				divDiggProfile.style.display = "none";
				iDigg.style.display = "none";
				diggXmlEditor.style.display = "";
				document.getElementById( 'divPreview' ).style.display = "";
				resizeWindow();
			}
			else skit_error(rsp.message);
		},
		timeout:30000,
		errorHandler:function(err) {skit_hiddenLoading(); alert(err); }
	});
}

function previewTemplateStyle(fullscrren)
{
	if( fullscrren )
	{
		var node = getNode();
		var name = document.getElementById( 'spanDiggName' ).innerHTML;
		openView("元数据查询模板预览: 【"+name+"】"+node.title, "diggcfg!stylepreview.action?gridxml="+node.id);
	}
	else
	{
		document.forms[0].method = "POST";
		document.forms[0].action = "diggcfg!stylepreview.action";
		document.forms[0].target = "iDigg";
		document.forms[0].submit();
	}
}
<ww:if test='developer'>
$('#tabL').tabs({
/*    add: function(event, ui) {
        alert(ui.panel.id);
        alert($tabs);
        $tabs.tabs('select', '#' + ui.panel.id);
    },*/
    remove: function(event, ui) {
//		$("#tabL").tabs('select', '#i-1');
    	var curTab = $('.ui-tabs-active');
    	if( curTab.length > 0 ){
    		ifrid = curTab[0].id.substring(3);
    		var node = myZtree.getNodeByParam("id", ifrid);
    		if( node )
    		{
    			myZtree.selectNode(node);
    		}
    	}
    	else{
    		var node = myZtree.getNodeByParam("rootdir", true);
    		if( node )
    		{
    			myZtree.selectNode(node);
        		open(node);
    		}
    	}
    		
    },
    select: function(event, ui) {
		if( ui.panel.id != ifrid ){
			var node = ifrid?myZtree.getNodeByParam("id", ifrid):null;
			setTemplateEditorStatus(node);//切换选项卡的时候检查原模板是否状态发生变化
			ifrid = ui.panel.id;
			node = myZtree.getNodeByParam("id", ifrid);
			if( node )
			{
				myZtree.selectNode(node);
				open(node);
			}
			if( resizeWindow ) resizeWindow();
		}
    }
});

//设置模板编辑的状态
function setTemplateEditorStatus(node){
	if(!node || node.type == 'help' ) return;
	var ifr = frames[node.id];
	if( ifr && ifr.getValue ){
		var content = ifr.getValue();
		var xmlmd5 = $.md5(content);
		//alert("edit: "+xmlmd5+"\r\nnode:"+node.xmlmd5)
		if( node.xmlmd5 != xmlmd5 ){
			document.getElementById("a-"+node.id).innerHTML = getTabTile(node)+"*";
		}
		else{
			document.getElementById("a-"+node.id).innerHTML = getTabTile(node);
		}
		document.getElementById("content").value = content;
	}
}

function getTabTile(node){

	var title = node.type != "dir" && "help" != node.type && "datasourcecfgs" != node.id ? node.title : node.name;
	title = node.type=="datasource"?node.id:title;
	if( node.template && title.indexOf(".xml") != -1 ){
		title = title.substring(0, title.length -4);
	}
	return title;
}

function openTemplate(id){
	var node = id?myZtree.getNodeByParam("id", id):null;
	if( node ){
		open(node);
	}
	else{
		skit_alert("打开指定模板["+id+"]编辑器失败");
	}
}
function getDiggLink(){
	var node = getNode();
	var url = "digg!query.action?gridxml="+node.id;
	skit_input("模板链接地址，点击确定直接跳转到后台菜单管理进行菜单设置", url, function(yes, url){
		if( yes ){
			openView('我的系统开发管理', 'modules!navigate.action');
		}
	});
}
function getFormLink(mode){
	var node = getNode();
	var url = "form!"+mode+".action?gridxml="+node.id;
	skit_input("模板链接地址，点击确定直接跳转到后台菜单管理进行菜单设置", url, function(yes, url){
		if( yes ){
			openView('我的系统开发管理', 'modules!navigate.action');
		}
	});
}
//开发者模式下打开
function open(node, openTemplateCallback)
{
	if( "datasource" != node.type && "table" != node.type )
		setUserActionMemory("diggcfg!explorer."+sysid, node.id);
	document.getElementById( 'divCompare' ).style.display = "none";
	document.getElementById( 'divVersions' ).style.display = "none";
	document.getElementById( 'divFullscreen' ).style.display = "none";
	document.getElementById( 'divDiggLink' ).style.display = "none";
	document.getElementById( 'divFormLink' ).style.display = "none";
	document.getElementById( 'divAddQuery' ).style.display = "none";
	document.getElementById( 'divAddConfig' ).style.display = "none";
	document.getElementById( 'divSave' ).style.display = "none";
	document.getElementById( 'divCancel' ).style.display = "none";
	document.getElementById( 'divUpload' ).style.display = "none";
	document.getElementById( 'divDebug' ).style.display = "none";
	document.getElementById( 'divPreviewData' ).style.display = "none";
	document.getElementById( 'divPreview' ).style.display = "none";
	var i = document.getElementById( node.id );
	if( i ){
		document.getElementById( 'iDigg' ).style.display = "none";
		$("#tabL").tabs('select', '#'+node.id);
		if( node.template ){
			if( sysid != "local" ){
				document.getElementById( 'divCompare' ).style.display = "";
				document.getElementById( 'divUpload' ).style.display = "";
				document.getElementById( 'divSave' ).style.display = "";
				document.getElementById( 'divFullscreen' ).style.display = "";
				if( node.type == "form" ){
					document.getElementById( 'divFormLink' ).style.display = "";
				}
				else{
					document.getElementById( 'divDiggLink' ).style.display = "";
				}
			}
			if( node.datamodel && "local" != node.datamodel ){
				document.getElementById( 'divPreview' ).style.display = "";
				if( node.type == "form" ){
					document.getElementById( 'divPreviewData' ).style.display = "";
				}
			}
			document.getElementById( 'divDebug' ).style.display = "";
			if( node.newTemplate ){
				document.getElementById( 'divCancel' ).style.display = "";
				document.getElementById( 'divDiggLink' ).style.display = "none";
				document.getElementById( 'divFormLink' ).style.display = "none";
			}
			else{
				setTemplateEditorStatus(node);
				if( node.viewversions ){
					openVersions(node);
				}
				else{
					closeVersions(node);
				}
			}
			document.getElementById( 'gridxml' ).value = node.id;
			return;
		}
		if( node.type == "dir" ){
			if( sysid != "local" ){
				document.getElementById( 'divAddQuery' ).style.display = "";
				document.getElementById( 'divAddConfig' ).style.display = "";
			}
			document.getElementById("id").value = node.id;
			document.forms[0].method = "POST";
			document.forms[0].action = "diggcfg!query.action";
			document.forms[0].target = node.id;
			document.forms[0].submit();
			myZtree.expandNode(node, true, false);
			return;
		}
		if( "datasourcecfgs" == node.id || "datasource" == node.type || "help" == node.type  ){
			return;//数据库视图打开后也不用再管了
		}
	}
	if( "help" == node.type )
	{
		if( !node.url ){
			return;
		}
	}
	addTab(node);
	if( "help" == node.type ) {
		document.forms[0].method = "POST";
		document.forms[0].action = node.url;
		document.forms[0].target = node.id;
		document.forms[0].submit();
	}
	else if( node.template ){
		document.getElementById( 'divCompare' ).style.display = "";
		document.getElementById( 'divUpload' ).style.display = "";
		document.getElementById( 'divSave' ).style.display = "";
		document.getElementById( 'divDebug' ).style.display = "";
		document.getElementById( 'divPreview' ).style.display = "";
		document.getElementById( 'divFullscreen' ).style.display = "";
		if( node.type == "form" ){
			document.getElementById( 'divFormLink' ).style.display = "";
		}
		else{
			document.getElementById( 'divDiggLink' ).style.display = "";
		}
		DiggConfigMgr.getTemplateXml(node.id, {
			callback:function(rsp) {
				if( rsp.succeed ) {
					if( rsp.message ) skit_alert(rsp.message);
					node.timestamp = rsp.timestamp;
					node.xmlmd5 = $.md5(rsp.result);
					node.checked = node.check;
					node.name = node.cname;
					myZtree.updateNode(node);
					if(node.check){
						//delete node.check;
						//var json = JSON.stringify(node.check);
						//node.checked = jQuery.parseJSON(json);
					}
					var ic = document.getElementById( 'ico_abc_'+node.id );
					if( ic )
					{
						ic.className = "skit_fa_icon_blue fa fa-edit";
						ic.title = "模板编辑中...";
					}
					document.getElementById("content").value = rsp.result;
					document.forms[0].action = "editor!xml.action";
					document.forms[0].method = "POST";
					document.forms[0].target = node.id;
					document.forms[0].submit();
					if( openTemplateCallback ){
						openTemplateCallback();
					}
				}
				else skit_error(rsp.message);
			},
			timeout:30000,
			errorHandler:function(err) {skit_hiddenLoading(); alert(err); }
		});
	}
	else if( "datasourcecfgs" == node.id ) {
		document.getElementById( 'divAddQuery' ).style.display = "";
		document.getElementById( 'divAddConfig' ).style.display = "";
		document.forms[0].method = "POST";
		document.forms[0].action = "diggcfg!datasource.action";
		document.forms[0].target = node.id;
		document.forms[0].submit();
		if( !node.init )
		{
			node.init = 1;
			DiggConfigMgr.getModuleDatasources(sysid, {
				callback:function(rsp) {
					if( !rsp ) skit_alert("打开数据源管理失败");
					if( rsp.succeed )
					{
						var newnodes = jQuery.parseJSON(rsp.result);
						for(var i = 0; i < newnodes.length; i++)
						{
							var e = newnodes[i];
							e["id"] = e.name;
							e["type"] = "datasource";
							e["name"] = e.title+"("+e.dbtype+"@"+e.dbaddr+")";
							e["cname"] = e.title+"("+e.dbtype+"@"+e.dbaddr+")";
							//e["icon"] = "images/icons/spam.png";
							e.icon = "images/ico/database.png";
						}
						node.init = 200;
						myZtree.addNodes(node, -1, newnodes);
					}
					else
					{
						node.init = 0;
						skit_error(rsp.message);
					}
				},
				timeout:60000,
				errorHandler:function(err) {skit_hiddenLoading(); alert(err); }
			});
		}
	}
	else if( "datasource" == node.type )
	{
		document.getElementById( 'divAddQuery' ).style.display = "";
		document.getElementById( 'divAddConfig' ).style.display = "";
		if( node.dbtype == "mongo" ){
			skit_alert("暂不支持芒果数据库预览");
			return;
		}
		document.getElementById( 'db' ).value = "/cos/config/modules/"+sysid+"/datasource/"+node.id;
		document.forms[0].method = "POST";
		document.forms[0].action = "helper!database.action";
		document.forms[0].target = node.id;
		document.forms[0].submit();
	}
	else if( node.type == "dir" ){
		document.getElementById( 'divAddQuery' ).style.display = "";
		document.getElementById( 'divAddConfig' ).style.display = "";
		document.getElementById("id").value = node.id;
		document.forms[0].method = "POST";
		document.forms[0].action = "diggcfg!query.action";
		document.forms[0].target = node.id;
		document.forms[0].submit();
		myZtree.expandNode(node, true, false);
	}
	$("#tabL").tabs('select', '#'+node.id);
}
function removeTab(id, force)
{
	var node = myZtree.getNodeByParam("id", id);
	if( !node )
	{
		skit_alert("未知选项卡【"+id+"】");
		return;
	}
	var ifr = frames[node.id];
	if( ifr ){
		if( !ifr.getValue ){
			$("#tabL").tabs('remove', '#'+id);
		}		
		var content = ifr.getValue();
		if( node.newTemplate ){
			skit_confirm("您创建的新元数据模板还没有保存，关闭编辑视图数据将不会保存，您是否保存？", function(yes){
				if( yes ) {
					presaveTemplate();
				}
				else{
					myZtree.removeNode(node);
					$("#tabL").tabs('remove', '#'+id);
				}
			});	
			return;
		}
		var xmlmd5 = $.md5(content);
		//alert("edit: "+xmlmd5+"\r\nnode:"+node.xmlmd5)
		if( node.xmlmd5 != xmlmd5 && !force && sysid != "local" ){
			skit_confirm("该元数据模板【"+node.name+"】 "+node.title+"正在编辑中还没有保存，关闭编辑视图数据将不会保存，您是否保存？", function(yes){
				if( yes ) {
					presaveTemplate();
				}
				else{
					node.check = node.checked;
					setTemplateCheck(node);
					node.name = node.ename;
					myZtree.updateNode(node);
					$("#tabL").tabs('remove', '#'+id);
				}
			});
			return;
		}
		node.check = node.checked;
		setTemplateCheck(node);
		node.name = node.ename;
		myZtree.updateNode(node);
		$("#tabL").tabs('remove', '#'+id);
		return true;
	}
	return false;
}
//检查编辑
function checkEdit(){
	var iframes = $("iframe");
	var template, xml, xmlmd5, ifr, id;
	if( iframes )
	{
		for(var i = 0; i < iframes.length; i++ )
		{
			id = iframes[i].id;
			ifr = frames[id];
			if( ifr && ifr.getValue ){
				xml = ifr.getValue();
				xmlmd5 = $.md5(xml);
				template = myZtree.getNodeByParam("id", id);
				if( template.xmlmd5 != xmlmd5 ){
					setTemplateEditorStatus(template);
					return true;
				}
			}
		}
	}
	return false;
}
</ww:if>
</SCRIPT>
</html>