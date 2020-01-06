<%@ page language="java" pageEncoding="UTF-8"%>
<%@ page import="com.focus.cos.web.common.Kit"%>
<%@ taglib uri="/webwork" prefix="ww" %>
<%Kit.noCache(request,response);  %>
<html>
<head>
<link type="text/css" href="skit/ztree/css/zTreeStyle/zTreeStyle.css" rel="stylesheet"/>
<link type="text/css" href="skit/css/bootstrap.css" rel="stylesheet">
<link type="text/css" href="skit/css/costable.css?v=3" rel="stylesheet">
<style type='text/css'>
</style>
<%=Kit.getDwrJsTag(request,"interface/MonitorMgr.js")%>
<%=Kit.getDwrJsTag(request,"interface/ProgramMgr.js")%>
<%=Kit.getDwrJsTag(request,"engine.js")%>
<%=Kit.getDwrJsTag(request,"util.js")%>
<SCRIPT type="text/javascript">
function onSelectUser(e, treeId, userNode) {
	var nodes = myZtree.getSelectedNodes();
	if( !nodes || nodes.length == 0)
	{
		hideSelector();
		skit_alert("请先在目录导航树上选择您要操作的容器");
		return;
	}
	var program = nodes[0];
	var name = userNode.rname;
	var account = userNode.id;
	var email = userNode.email;
	hideSelector();
	var tip = "您确定设置系统【"+sysid+"】下程序【"+program.name+"】的负责人为"+userNode.name+"吗？维护邮箱是"+email;
	skit_confirm(tip, function(yes){
		if( yes )
		{
			skit_showLoading();
			ProgramMgr.setProgrammer(sysid, program.id, name, account, email, {
				callback:function(rsp) {
					skit_hiddenLoading();
					try
					{
						skit_alert(rsp.message);
						if( rsp.succeed )
						{
							document.getElementById( 'spanProgramer' ).innerHTML = name;
						}
					}
					catch(e)
					{
						skit_alert("设置系统【"+sysid+"】下程序【"+program.name+"】的负责人出现异常"+e.message+", 行数"+e.lineNumber);
					}
				},
				timeout:30000,
				errorHandler:function(err) {skit_hiddenLoading(); alert(err); }
			});
		}
	});	
}
</SCRIPT>
</head>
<body onResize='resizeWindow()' style='overflow-y:hidden;padding-top:0px;padding-left:0px;'>
<TABLE>
<TR class='unline'><TD width='250' valign='top' id='tdTree'>
        <div class="panel panel-default">
   			<div class="panel-heading" style='font-size:12px;padding:5px 15px'><i class='skit_fa_btn fa fa-bars'></i> 系统程序目录树</div>
   			<div class="panel-body" style='padding: 0px;'>
   				<div id='divTree'>
					<ul id='myZtree' class='ztree'></ul>
					<div id="rMenu">
						<ul>
							<li onclick="presetProgramConfig();" id='liAdd'><i class='skit_fa_icon_blue fa fa-plus-circle'></i> 新增程序</li>
							<li onclick="presetProgramConfig();" id='liPreset'><i class='skit_fa_icon fa fa-cogs'></i> 配置程序</li>
							<li onclick="delProgramConfig();" id='liDelete'><i class='skit_fa_icon_red fa fa-minus-circle'></i> 删除程序</li>
							<li onclick="cancelProgramConfig();" id='liCancel'><i class='skit_fa_icon_yellow fa fa-minus-circle'></i> 撤销程序配置审核</li>
							<li onclick="clearProgramConfig();" id='liClear'><i class='skit_fa_icon_yellow fa fa-minus-circle'></i> 撤销所有程序配置审核</li>
							<li class="divider" id='liDivider1'></li>
							<li onclick="uploadcfg();" id='liUploadcfg'><i class='skit_fa_icon fa fa-upload'></i> 上传程序配置脚本</li>
							<li onclick="downloadcfg();" id='liDownloadcfg'><i class='skit_fa_icon fa fa-download'></i> 下载程序配置脚本</li>
							<li class="divider" id='liDivider'></li>
							<li onclick="uploadxml();" id='liUploadxml'><i class='skit_fa_icon fa fa-upload'></i> 上传程序主控配置文件</li>
							<li onclick="downloadxml();" id='liDownloadxml'><i class='skit_fa_icon fa fa-download'></i> 下载程序主控配置文件</li>
						</ul>
					</div>
				</div>
   			</div>
   		</div>
	</TD>
	<TD valign='top' valign='top'>
        <div class="panel panel-default" id='divMonitor'>
   			<div class="panel-heading" style='font-size:12px;padding:5px 15px'><span><i class='skit_fa_btn fa fa-windows'></i> 程序管理 <ww:property value='viewTitle'/></span>
                <div style='float:right;right:4px;top:0px;display:none' id='divPreset'>
 				<button type="button" class='btn btn-outline btn-primary btn-xs' onclick='presetProgramConfig()' id='btnPreset'>
 					<i class='fa fa-edit'></i> 配置程序</button>
                </div>
                <div style='float:right;padding-right:4px;top:0px;display:none' id='divCloseConfig'>
 				<button type="button" class="btn btn-outline btn-danger btn-xs" onclick='closeProgramConfig();'>
 					<i class='fa fa-close'></i> 取消配置程序</button>
                </div>
                <div style='float:right;padding-right:4px;top:0px;display:none' id='divSaveConfig'>
 				<button type="button" class="btn btn-outline btn-primary btn-xs" onclick='saveProgramConfig();'>
 					<i class='fa fa-save'></i> 保存配置程序</button>
                </div>
	        </div>
   			<div class="panel-body" style='padding: 3px;'>
				<div class="well profile" id='divSystemProgram' style='margin-left:12px;margin-top:12px;margin-bottom:5px;display:none'>
				    <div class="col-sm-12">
				        <div class="col-xs-12 col-sm-10">
				        	<h3 id='h2ProgramId' style='margin-top:0px;'></h3>
				            <p><i class="fa fa-info-circle fa-fw text-muted"></i>
				               <span id='spanProgramTitle'></span>
				               <a onclick="setProgramRemark()" id='aSetProgramRemark'><i class='fa fa-edit'></i> 设置程序描述</a> 
				               </p>
				            <p><strong><i class="fa fa-user fa-fw text-muted"></i> 程序管理员:  </strong>
				               <span class="tags" id="spanProgramer" onclick='mailto(this)'></span>
				               <a onclick="showSelector('spanProgramer')" id='aSetProgramer'><i class='fa fa-user-plus'></i> 设置</a>
				            </p>
				            <p><strong><i class="fa fa-paw fa-fw text-muted"></i> 版本号:  </strong>
					               <span class="tags" id='spanProgramVersion'
				               	 onclick='viewVersions()' title='点击显示版本详情'></span>
				               <a onclick="viewVersions(true)"><i class='fa fa-eye'></i> 查看</a> 
				               <a onclick="presetProgramVersion()" id='sPresetControlVersion'><i class='fa fa-edit'></i> 配置程序版本历史</a> 
				            </p>
				            <p><strong><i class="fa fa-clock-o fa-fw text-muted"></i> 程序工作模式:  </strong>
				               <span id='spanLastConfigTime'>N/A</span> 
				            </p>
				        </div>
				        <div class="col-xs-12 col-sm-2 text-center">
				            <figure>
				                <i class="fa fa-ban" style='font-size:48px;' id='iProgramPublishStat'></i>
				                <figcaption class="ratings" style='margin-top:0px;'>
				                    <p id='pProgramPublishStat'></p>
				                    <p id='pProgramPublishTime' style='color:red'></p>
						            <p><button type="button" class="btn btn-primary" id='btnPublish' onclick='savePublish()'><span class="fa fa-clipboard"></span> 设置发布 </button></p>
				                </figcaption>
				            </figure>
				        </div>
				    </div>
				</div>
	   			<iframe src='' name='iProgram' id='iProgram' class='nonicescroll' style='border:0px solid #eee;margin-left:12px;margin-top:0px;'></iframe>
			</div>
		</div>
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
		<input type="file" name="uploadfile" id="uploadfile" class="file-loading"/>
    </div>
</div>
<form id='form0'>
<input type='hidden' name='command' id='command'>
<input type='hidden' name='id' id='id' value="<ww:property value='id'/>">
<input type='hidden' name='sysid' id='sysid' value="<ww:property value='sysid'/>">
<iframe name='iDownload' id='iDownload' style='display:none'></iframe>
</form>
<%@ include file="../user/user_select.inc"%>
</body>
<SCRIPT type="text/javascript">
/*实现窗口对齐*/
function resizeWindow()
{
	var userAgent = navigator.userAgent; //取得浏览器的userAgent字符串
	var div = document.getElementById( 'tdTree' );
	var w = 15;
	if( div.style.display == "" )
	{
		div = document.getElementById( 'divTree' );
		div.style.width = 248;
		div.style.height = windowHeight - 38;
		w = 286;
	}

	var h = 0;
	if( document.getElementById('divSystemProgram').style.display == "" )
	{
		div = document.getElementById( 'divSystemProgram' );
		div.style.width = windowWidth - 286; 
		h += 249;
	}
	else
	{
		h = 44;
	}

	div = document.getElementById( 'iProgram' );
	if( w == 15 )
	{
		div.style.border = "0px solid #eee";
		div.style.marginLeft = 0;
	}
	else
	{
		div.style.border = "0px solid #eee";
		div.style.marginLeft = 12;	
	}
	div.style.height = windowHeight - h;
	div.style.width = windowWidth - w;
}
</SCRIPT>
<link href="skin/defone/css/simplemodal.css" rel="stylesheet">
<link href="skit/css/bootstrap.switch.css" rel="stylesheet">
<link href="skit/css/fileinput.min.css" rel="stylesheet"/>
<%@ include file="../../skit/inc/skit_bootstrap.inc"%>
<%@ include file="../../skit/inc/skit_cos.inc"%>
<%@ include file="../../skit/inc/skit_ztree.inc"%>
<script src="skin/defone/js/mootools-core-1.3.1.js"></script>
<script src="skin/defone/js/simple-modal.js?v=3"></script>
<script src="skit/js/fileinput.js"></script>
<script src="skit/js/fileinput_locale_zh.js"></script>
<script src="skit/js/jquery.md5.js"></script>
<script src="skit/js/bootstrap.switch.js"></script>
<script src="skit/js/jquery.inputmask.bundle.min.js"></script>
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
</style>
<SCRIPT type="text/javascript">
var iProgramScroll = false;
function buildIframeNiceScroll()
{
	if( iProgramScroll )
	{
		return;
	}
	$( '#iProgram' ).niceScroll({
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
	iProgramScroll = true;
}

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
buildIframeNiceScroll();

var grant = <ww:property value='grant'/>;//管理程序的权限
var sysid = "<ww:property value='sysid'/>";
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
			beforeExpand: beforeExpand,
			onExpand: onExpand
		},
		view: {
			addDiyDom: addDiyDom
		}
	};

	function onRightClick(event, treeId, treeNode) 
	{
		document.getElementById( 'liAdd' ).style.display = "none";
		document.getElementById( 'liDelete' ).style.display = "none";
		document.getElementById( 'liCancel' ).style.display = "none";
		document.getElementById( 'liPreset' ).style.display = "none";
		document.getElementById( 'liClear' ).style.display = "none";
		document.getElementById( 'liDivider' ).style.display = "none";
		document.getElementById( 'liDivider1' ).style.display = "none";
		document.getElementById( 'liUploadxml' ).style.display = "none";
		document.getElementById( 'liDownloadxml' ).style.display = "none";
		document.getElementById( 'liUploadcfg' ).style.display = "none";
		document.getElementById( 'liDownloadcfg' ).style.display = "none";
		if( treeNode.type == 'dir' )
		{
			if( treeNode.id == '-1')
			{
				return;
			}
			document.getElementById( 'liUploadcfg' ).style.display = "";
			//document.getElementById( 'liDownloadcfg' ).style.display = "";
			//document.getElementById( 'liDivider1' ).style.display = "";
			document.getElementById( 'liDivider' ).style.display = "";
			document.getElementById( 'liAdd' ).style.display = "";
			document.getElementById( 'liUploadxml' ).style.display = "";
			document.getElementById( 'liDownloadxml' ).style.display = "";
		}
		else
		{
			document.getElementById( 'liDownloadcfg' ).style.display = "";
			document.getElementById( 'liDelete' ).style.display = "";
			document.getElementById( 'liPreset' ).style.display = "";
		}

		if (!treeNode && event.target.tagName.toLowerCase() != "button" && $(event.target).parents("a").length == 0) {
			myZtree.cancelSelectedNode();
			showRMenu("root", event.clientX, event.clientY);
		} else if (treeNode && !treeNode.noR) {
			myZtree.selectNode(treeNode);
			showRMenu("node", event.clientX, event.clientY);
		}
	}

	function addDiyDom(treeId, treeNode)
	{
		if( treeNode.ip )
		{
			return;
		}
		//if( treeNode.debug ) debugIcon = "<i class='skit_fa_icon_war fa fa-debug' title='程序已开启调试输出请查看log/"+treeNode.id+"/debug_"+treeNode.id+".txt文件' style='font-size:16px;'></i>";
	}

	function onClick(event, treeId, treeNode)
	{
		myZtree.expandNode(treeNode, null, null, null, true);
		myZtree.expandNode(treeNode, true);
		open(treeNode);
	}
	
	$(document).ready(function(){
		var json = '<ww:property value="jsonData" escape="false"/>';
		initialize(json);
	});

function initialize(json)
{
	if( !json ) return;
	if( myZtree ) myZtree.destroy();
	try
	{
		var zNodes = jQuery.parseJSON(json);
		myZtree = $.fn.zTree.init($("#myZtree"), setting, zNodes);
		if( !myZtree ){
			skit_alert("初始化程序管理导航树失败: "+json);
			return;
		}
		//myZtree = $.fn.zTree.getZTreeObj("myZtree");
		myZtreeMenu = $("#rMenu");
		var id = getUserActionMemory("program!open.program-"+sysid);
		if( !id ) id = "0";
		var node = myZtree.getNodeByParam("id", id);
		if( node )
		{
			myZtree.selectNode(node);
			myZtree.expandNode(node, true);
			open(node);
		}
	}
	catch(e)
	{
		skit_alert("初始化程序管理配置导航树异常"+e.message+", 行数"+e.lineNumber);
	}
}
</SCRIPT>
<script type="text/javascript">
var program;
function open(treeNode)
{
	try
	{
		setUserActionMemory("program!open.program-"+sysid, treeNode.id);
		if( treeNode.type == 'dir' ){
			$("#iProgram").getNiceScroll().remove();
			document.getElementById( 'divSystemProgram' ).style.display = "none";
			document.getElementById( 'tdTree' ).style.display = "";
			document.getElementById("divPreset").style.display = "none";
			document.getElementById("divCloseConfig").style.display = "none";
			document.getElementById("divSaveConfig").style.display = "none";
			document.getElementById( 'id' ).value = treeNode.id;
			document.forms[0].action = "program!list.action";
			document.forms[0].target = "iProgram";
			document.forms[0].submit();
			resizeWindow();
			return;
		}
		document.getElementById( 'tdTree' ).style.display = "";
		document.getElementById( 'divSystemProgram' ).style.display = "";
		document.getElementById("divPreset").style.display = "";
		if( treeNode.removed )
		{
			document.getElementById( 'iProgramPublishStat' ).className = "skit_fa_icon_red fa fa-ban";
			document.getElementById( 'pProgramPublishStat' ).innerHTML = "程序删除，"+(treeNode.publishState?treeNode.publishState:"程序未发布过");
			document.getElementById( 'pProgramPublishTime' ).innerHTML = treeNode.publishTime?treeNode.publishTime:"程序发布时间: N/A";
			document.getElementById("btnPublish").style.display = "none";
		}
		else
		{
			document.getElementById( 'iProgramPublishStat' ).className = "fa fa-question";
			document.getElementById( 'pProgramPublishStat' ).innerHTML = treeNode.publishState?treeNode.publishState:"程序还没发布";
			document.getElementById( 'pProgramPublishTime' ).innerHTML = treeNode.publishTime?treeNode.publishTime:"程序发布时间: N/A";
			document.getElementById("btnPublish").style.display = "";
		}
		document.getElementById("aSetProgramRemark").style.display = "none";
		document.getElementById("aSetProgramer").style.display = "none";
		document.getElementById("divCloseConfig").style.display = "none";
		document.getElementById("divSaveConfig").style.display = "none";
		document.getElementById("sPresetControlVersion").style.display = "";
		document.getElementById("spanLastConfigTime").innerHTML = treeNode.mode;
		document.getElementById( 'h2ProgramId' ).innerHTML = treeNode.id;
		document.getElementById( 'spanProgramTitle' ).innerHTML = treeNode.name;
		document.getElementById( 'spanProgramVersion' ).innerHTML = treeNode.version?treeNode.version:"0.0.0.0";
		document.getElementById( 'spanProgramer' ).innerHTML = treeNode.maintenance.programmer;
		document.getElementById( 'id' ).value = treeNode.id;
		programPublish = treeNode.publish?treeNode.publish:new Object();
		program = treeNode;

		document.forms[0].action = "program!publish.action";
		document.forms[0].target = "iProgram";
		document.forms[0].submit();
		resizeWindow();
	}
	catch(e)
	{
		skit_alert("打开系统程序管理视图出现异常"+e.message+", 行数"+e.lineNumber);
	}
}

function presetProgramConfig()
{
	hideRMenu();
	var nodes = myZtree.getSelectedNodes();
	if( !nodes || nodes.length == 0)
	{
		skit_alert("请先在目录导航树上选择您要操作的程序");
		return;
	}
	program = nodes[0];
	buildIframeNiceScroll();
	//TODO:全屏
	document.getElementById( 'divSystemProgram' ).style.display = "none";
	document.getElementById( 'tdTree' ).style.display = "none";
	document.getElementById("divPreset").style.display = "none";
	document.getElementById("divCloseConfig").style.display = "";
	document.getElementById("divSaveConfig").style.display = "";
	resizeWindow();
	document.forms[0].action = "program!preset.action";
	document.forms[0].target = "iProgram";
	document.forms[0].submit();
}

function closeProgramConfig(openVersionConfig)
{
	var nodes = myZtree.getSelectedNodes();
	if( !nodes || nodes.length == 0)
	{
		skit_alert("请先在目录导航树上选择您要操作的程序");
		return;
	}
	open(nodes[0]);
	if( openVersionConfig )
	{
		presetProgramVersion();
	}
}

function saveProgramConfig()
{
	if( window.frames["iProgram"].setProgramConfig )
	{
		window.frames["iProgram"].setProgramConfig();
	}
}

function setProgramConfig(json)
{
	skit_confirm("您确定要配置系统【"+sysid+"】的程序吗？", function(yes){
		if( yes )
		{
			skit_showLoading();
			ProgramMgr.setProgramConfig(sysid, json, {
				callback:function(rsp) {
					skit_hiddenLoading();
					try
					{
						if( rsp.succeed )
						{
							var program = jQuery.parseJSON(rsp.result);
							var program0 = myZtree.getNodeByParam("id", program.id );
							//alert("program0: "+program0.name+" = "+program.name);
							if( program0 )
							{
								if( program.id != program0.id )
								{
									myZtree.removeNode(program0);
									var parentNode = myZtree.getNodeByParam("id", "0" );
									program["icon"] = "images/icons/tile.png";
									program0 = myZtree.addNodes(parentNode, -1, program)[0];
									myZtree.selectNode(program0);
									myZtree.expandNode(program0, true);
									open(program0);
								}
								else
								{
									program0.name = program.name;
									program0.version = program.version;
									program0.description = program.description;
									program0.maintenance.programmer = program.maintenance.programmer;
									myZtree.updateNode(program0);
									open(program0);
								}
							}
							else
							{
								var parentNode = myZtree.getNodeByParam("id", "0" );
								program["icon"] = "images/icons/tile.png";
								program0 = myZtree.addNodes(parentNode, -1, program)[0];
								myZtree.selectNode(program0);
								myZtree.expandNode(program0, true);
								open(program0);
							}
							closeProgramConfig();
						}
						else
						{
							skit_alert(rsp.message);
						}
					}
					catch(e)
					{
						skit_alert("配置系统【"+sysid+"】的程序出现异常"+e.message+", 行数"+e.lineNumber);
					}
				},
				timeout:30000,
				errorHandler:function(err) {skit_hiddenLoading(); alert(err); }
			});
		}
	});
}

function delProgramConfig()
{
	hideRMenu();
	var nodes = myZtree.getSelectedNodes();
	if( !nodes || nodes.length == 0)
	{
		skit_alert("请先在目录导航树上选择您要删除的程序");
		return;
	}
	var program = nodes[0];
	skit_confirm("删除系统【"+sysid+"】程序【"+program.id+" "+program.name+"】将触发所有已发布到伺服器的程序下架，系统管理员审批后执行，您确认删除程序吗？", function(yes){
		if( yes )
		{
			skit_showLoading();
			ProgramMgr.delProgramConfig(sysid, program.id, {
				callback:function(rsp) {
					skit_hiddenLoading();
					try
					{
						if( rsp.succeed )
						{
							var program0 = myZtree.getNodeByParam("id", program.id );
							if( program0 )
							{
								myZtree.removeNode(program0);
							}
							program0 = jQuery.parseJSON(rsp.result);
							var parentNode = myZtree.getNodeByParam("id", "-1" );
							program0["removed"] = true;
							program0["icon"] = "images/icons/tile.png";
							program0 = myZtree.addNodes(parentNode, -1, program0)[0];
							myZtree.selectNode(program0);
							myZtree.expandNode(program0, true);
							open(program0);
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

var programPublish;
function setPublish(serverid, isPublish, ip, port, desc, servertype, cluster)
{
	if( isPublish && !programPublish[serverid] )
	{
		var obj = new Object();
		obj["serverid"] = serverid;
		obj["ip"] = ip;
		obj["port"] = port;
		obj["desc"] = desc;
		obj["cluster"] = cluster;
		obj["servertype"] = servertype;
		programPublish[serverid] = obj;
	}
	else
	{
		delete programPublish[serverid];
	}
}

function getPublish()
{
	var html = "";
	for(var id in programPublish )
	{
		var obj = programPublish[id];
		html += "<br/>&nbsp;&nbsp;&nbsp;伺服器【"+obj.ip+":"+obj.port+"】 "+obj.servertype;
	}
	return html;
}


function getPublishSize()
{
	var i = 0;
	for(var id in programPublish )
	{
		i += 1;
	}
	return i;
}

function savePublish()
{
	if( getPublishSize() == 0  )
	{
		skit_alert("请先从列表中选择你的程序希望发布到的伺服器");
		return;
	}
	var tips = "设置系统【"+sysid+"】程序【"+program.id+" "+program.name+"】发布到指定伺服器，您确认配置该程序吗？"+getPublish();
	skit_confirm(tips, function(yes){
		if( yes )
		{
			skit_showLoading();
			var json = JSON.stringify(programPublish);
			ProgramMgr.setProgramPublish(sysid, program.id, json, {
				callback:function(rsp) {
					skit_hiddenLoading();
					try
					{
						if( rsp.succeed )
						{
							var program0 = myZtree.getNodeByParam("id", program.id );
							open(program0);
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

function closeuploadfile()
{
	document.getElementById( 'divUploadfile' ).style.display = "none";
	$("#uploadfile").fileinput("destroy");
}

function downloadxml()
{
	skit_confirm("您确定要下载系统【"+sysid+"】的主控程序配置吗？", function(yes){
		if( yes )
		{
			document.forms[0].action = "program!downloadxml.action";
			document.forms[0].method = "POST";
			document.forms[0].target = "iDownload";
			document.forms[0].submit();
		}
	});
}

function downloadcfg()
{
	var nodes = myZtree.getSelectedNodes();
	if( !nodes || nodes.length == 0)
	{
		skit_alert("请先在目录导航树上选择您要操作的程序");
		return;
	}
	var program = nodes[0];
	skit_confirm("您确定要下载系统【"+sysid+"】程序【"+program.id+"】的配置吗？", function(yes){
		if( yes )
		{
			document.getElementById( 'id' ).value = program.id;
			document.forms[0].action = "program!downloadcfg.action";
			document.forms[0].method = "POST";
			document.forms[0].target = "iDownload";
			document.forms[0].submit();
		}
	});
}

function uploadxml()
{
	preupload('xml', "program!uploadxml.action", "程序配置脚本");
}

function uploadcfg()
{
	preupload('json', "program!uploadcfg.action", "主控配置文件");
}

function preupload(type, uploadUrl, title)
{
	hideRMenu();
	var divUploadfile = document.getElementById( 'divUploadfile' );
	divUploadfile.style.display = "";
	$("#uploadfile").fileinput({
			language: 'zh', //设置语言
			uploadUrl: uploadUrl, //上传的地址
			allowedFileExtensions: [type],//接收的文件后缀
			showUpload: true, //是否显示上传按钮
			showCaption: false,//是否显示标题
			showClose: false,
			showPreview: true,
			browseClass: "btn btn-primary", //按钮样式     
			previewSettings: { image: {width: "auto", height: "260"} },
			//dropZoneEnabled: false,//是否显示拖拽区域
			//maxFileSize: 0,//单位为kb，如果为0表示不限制文件大小
			//minFileCount: 0,
			maxFileCount: 1, //表示允许同时上传的最大文件个数
			enctype: 'multipart/form-data',
			validateInitialCount:true,
			previewFileIcon: "<i class='glyphicon glyphicon-king'></i>",
			msgFilesTooMany: "选择上传的文件数量({n}) 超过允许的最大数值{m}！",
            uploadExtraData: function(previewId, index) {   //额外参数的关键点
                var obj = {};
                obj.sysid = "<ww:property value='sysid'/>";
                return obj;
            }
	});
	divUploadfile.style.top = 64;//windowHeight/2 - divUploadfile.clientHeight*2/3;
	divUploadfile.style.left = windowWidth/2 - divUploadfile.clientWidth/2;
	$("#uploadfile").on("fileloaded", function (data, previewId, index) {
		var tips = "您确定要上传["+previewId.name+"]到系统【"+sysid+"】替换配置程序吗？";
		if( type == "json" )
		{
			tips = "您确定要上传主控程序配置文件["+previewId.name+"]到系统【"+sysid+"】去替换对应的主控程序配置吗？";
		}
		document.getElementById( 'uploadtitle' ).innerHTML = tips;
		document.getElementById( 'filename' ).value = previewId.name;
	});
	//导入文件上传完成之后的事件
	$("#uploadfile").on("fileuploaded", function (event, data, previewId, index) {
		if( data.response.alt )
		{
			skit_alert(data.response.alt);
		}
		if( data.response.succeed )
		{
			initialize(data.response.result);
		}
		document.getElementById( 'divUploadfile' ).style.display = "none";
		$("#uploadfile").fileinput("destroy");
	});
}

function presetProgramVersion()
{
	var nodes = myZtree.getSelectedNodes();
	if( !nodes || nodes.length == 0)
	{
		skit_alert("请先在目录导航树上选择您要操作的程序");
		return;
	}
	var program = nodes[0];
	$("#iProgram").getNiceScroll().remove();
	iProgramScroll = false;
	document.getElementById( 'id' ).value = program.id;
	document.forms[0].action = "program!presetversion.action";
	document.forms[0].target = "iProgram";
	document.forms[0].submit();
}

function viewVersions(fullscreen)
{
	var nodes = myZtree.getSelectedNodes();
	if( !nodes || nodes.length == 0)
	{
		skit_alert("请先在目录导航树上选择您要操作的程序");
		return;
	}
	var program = nodes[0];
	var dataurl = "program!versiontimeline.action?id="+program.id+"&sysid="+sysid;
	dataurl = chr2Unicode(dataurl);
	buildIframeNiceScroll();
	document.getElementById("iProgram").src = "helper!timeline.action?dataurl="+dataurl;
	if( fullscreen )
	{
		openView("系统【"+sysid+"】程序【"+program.id+" "+program.name+"】版本详情", "helper!timeline.action?dataurl="+dataurl);
	}
}
</script>
</html>