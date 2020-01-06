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
<%=Kit.getDwrJsTag(request,"interface/ControlMgr.js")%>
<%=Kit.getDwrJsTag(request,"engine.js")%>
<%=Kit.getDwrJsTag(request,"util.js")%>
<SCRIPT type="text/javascript">
function onSelectUser(e, treeId, userNode) {
	if( !checkPrivileges() ) return;
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
	var tip = "您确定设置伺服器【"+ip+"】下程序【"+program.name+"】的负责人为"+userNode.name+"吗？维护邮箱是"+email;
	skit_confirm(tip, function(yes){
		if( yes )
		{
			skit_showLoading();
			ControlMgr.setProgrammer(ip, port, serverkey, program.id, name, account, email, <ww:property value='id'/>, {
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
						skit_alert("设置伺服器【"+ip+"】下程序【"+program.name+"】的负责人出现异常"+e.message+", 行数"+e.lineNumber);
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
<body style='overflow-y:hidden;padding-top:0px;padding-left:0px;'>
<TABLE>
<TR class='unline'><TD width='250' valign='top' id='tdTree'>
        <div class="panel panel-default">
   			<div class="panel-heading" style='font-size:12px;padding:5px 15px'><i class='skit_fa_btn fa fa-bars'></i> 伺服器程序目录树</div>
   			<div class="panel-body" style='padding: 0px;'>
   				<div id='divTree'>
					<ul id='myZtree' class='ztree'></ul>
					<div id="rMenu">
						<ul>
							<li onclick="presetProgramConfig();" id='liAdd'><i class='skit_fa_icon_blue fa fa-plus-circle'></i> 新增程序</li>
							<li onclick="presetProgramConfig();" id='liPreset'><i class='skit_fa_icon fa fa-cogs'></i> 配置程序</li>
							<li onclick="configProgramFile();" id='liCfgfile'><i class='skit_fa_icon fa fa-cog'></i> 修改配置文件</li>
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
				<ww:if test='grant'>
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
                <div style='float:right;padding-right:4px;top:0px;display:none' id='divCancelConfig'>
 				<button type="button" class="btn btn-outline btn-danger btn-xs" onclick='cancelProgramConfig();' id='btnCancel'>
 					<i class='fa fa-reply'></i> 撤销配置程序</button>
                </div>
				</ww:if>
				<ww:if test='sysadmin'>
                <div style='float:right;padding-right:4px;top:0px;'>
 				<button type="button" class="btn btn-outline btn-success btn-xs" onclick='goPublish();'>
 					<i class='fa fa-cloud-upload'></i> 审核程序配置发布</button>
                </div>
				</ww:if>
	        </div>
   			<div class="panel-body" style='padding: 3px;'>
				<div class="well profile" id='divServerPrograms' style='margin-left:12px;margin-top:12px;margin-bottom:5px;'>
				    <div class="col-sm-12">
				        <div class="col-xs-12 col-sm-10">
				            <h3 style='margin-top:0px;' title="<ww:property value='localDataObject.getString("security-key")'/>"
				            	><ww:property value='localDataObject.getString("ip")'/>
				            	<span style='color:#ccc;font-size:12px;'>(<ww:property value='servertype'/>)</span></h3>
				            <p><strong><i class="fa fa-info-circle fa-fw text-muted"></i></strong>
				            	 <span id='spanControlTitle'><ww:property value='localDataObject.getString("title")'/></span>
				            	 <a onclick="setControlTitle()"><i class='fa fa-edit'></i> 修改描述</a> </p>
				            <p><strong><i class="fa fa-server fa-fw text-muted"></i> 伺服器内存: </strong>
				            	<span><ww:property value='localDataObject.getString("phyMemUsageDetail")'/></span>
				            	<span id='memoryStateCheck'></span>
				            </p>
				            <p><strong><i class="fa fa-server fa-fw text-muted"></i> 伺服器硬盘: </strong>
				            	<span><ww:property value='localDataObject.getString("phyDiskUsageDetail")'/></span>
				            	<span id='diskStateCheck'><a onclick='openPath()'><i class='fa fa-folder-open'></i> 打开文件管理器</a></span>
				            </p>
				            <p><strong><i class="fa fa-users fa-fw text-muted"></i> 程序管理员: </strong>
				            	<ww:iterator value="listData" status="loop">
				            	<span class="tags" title="<ww:property value='value'/>" onclick='mailto(this)'><ww:property value='name'/></span>
				            	</ww:iterator>
				            </p>
				        </div>
				        <div class="col-xs-12 col-sm-2 text-center">
				            <figure id='figure'>
				            <!-- 
				                <img src="images/cmp/<ww:property value='command'/>.png" alt="" class="img-circle img-responsive"
				                	style='width:64px;' id='figureImg'>
				             -->
				             	<h2 id='hRunState' style='margin-top:0px;'></h2>
				                <figcaption class="ratings" style='margin-top:0px;'>
									<p>程序运行/配置</p>
				                    <p id='pHealthy'></p>
				                	<p>健康度</p>
			                        <p id='btnLoad'><button class="btn btn-danger btn-block btn-xs" onclick='viewServerLoadChat(-1)' style='height:22px;'>
			                        	<span class="fa fa-line-chart"></span> 查看伺服负载 </button></p>
			                        <p id='btnPrograms'><button class="btn btn-success btn-block btn-xs" onclick='openServerPrograms()' style='height:22px;'>
			                        	<span class="fa fa-code-fork"></span> 查看程序列表 </button></p>
				                </figcaption>
				            </figure>
				        </div>
				    </div>
					<div class="col-xs-12 divider text-center" style='display:none'>
	                    <div class="col-xs-12 col-sm-3 emphasis">
	                        <h6><strong id='sMemoryUsageInfo'><ww:property value='localDataObject.getString("memoryUsageInfo")'/></strong></h6>                    
	                        <p><a onclick='viewServerLoadChat(1)'><i class="fa fa-exchange"></i> <small
	                        	>内存使用</small> <img src="images/icons/gray.gif" id='memoryState'></a></p>
	                    </div>
	                    <div class="col-xs-12 col-sm-3 emphasis">
	                        <h6><strong><ww:property value='localDataObject.getString("cpuUsageInfo")'/></strong></h6>
	                        <p><a onclick='viewServerLoadChat(0)'><i class="fa fa-dashboard"></i> <small
	                        	>CPU开销</small> <img src="images/icons/gray.gif" id='cpuState'></a></p>
	                    </div>
	                    <div class="col-xs-12 col-sm-3 emphasis">
	                        <h6><strong><ww:property value='localDataObject.getString("netLoadInfo")'/></strong></h6>                    
	                        <p><a onclick='viewServerLoadChat(2)'><i class="fa fa-wifi"></i> <small
	                        	>网络IO</small> <img src="images/icons/gray.gif" id='netState'></a></p>
	                    </div>
	                    <div class="col-xs-12 col-sm-3 emphasis">
	                        <h6><strong><ww:property value='localDataObject.getString("ioLoadInfo")'/></strong></h6>
	                        <p><a onclick='viewServerLoadChat(3)'><i class="fa fa-wifi"></i> <small
	                        	>磁盘IO</small> <img src="images/icons/gray.gif" id='diskState'></a></p>
	                    </div>
               		</div>
				</div>
				<div class="well profile" id='divServerProgram' style='margin-left:12px;margin-top:12px;margin-bottom:5px;display:none'>
				    <div class="col-sm-12">
				        <div class="col-xs-12 col-sm-10">
				        	<h3 id='h2ProgramId' style='margin-top:0px;'></h3>
				            <p><i class="fa fa-info-circle fa-fw text-muted"></i>
				               <span id='spanProgramTitle'></span>
				               <a onclick="setProgramRemark()" id='aSetProgramRemark'><i class='fa fa-edit'></i> 设置程序描述</a>
				               <a onclick="configProgramFile()" id='aConfigProgramFile'><i class='fa fa-cogs'></i> 修改配置文件</a> 
				               </p>
				            <p><strong><i class="fa fa-user fa-fw text-muted"></i> 程序管理员:  </strong>
				               <span class="tags" id="spanProgramer" onclick='mailto(this)'></span>
				               <a onclick="showSelector('spanProgramer')" id='aSetProgramer'><i class='fa fa-user-plus'></i> 设置</a>
				            </p>
				            <p><strong><i class="fa fa-paw fa-fw text-muted"></i> 版本号:  </strong>
					               <span class="tags" id='spanProgramVersion'
				               	 onclick='viewVersions()' title='点击显示版本详情'></span>
				               <a onclick="viewVersions(true)"><i class='fa fa-eye'></i> 查看</a> 
				               <a onclick="presetControlVersion()" id='sPresetControlVersion'><i class='fa fa-edit'></i> 配置程序版本历史</a> 
				            </p>
				            <p id='pRunInfo'>
				               <strong><i class="fa fa-clock-o fa-fw text-muted"></i> 上次启动时间:  </strong>
				               <span id='spanLastRunTime'>N/A</span> 
				               <strong><i class="fa fa-recycle fa-fw text-muted"></i> 累计启动次数:  </strong>
				               <span id="spanRumTimes">N/A</span>
				            </p>
				            <p id='pCfgInfo'>
				               <strong><i class="fa fa-clock-o fa-fw text-muted"></i> 上次配置时间:  </strong>
				               <span id='spanLastConfigTime'>N/A</span> 
				            </p>
				        </div>
				        <div class="col-xs-12 col-sm-2 text-center">
				            <figure>
				                <i class="fa fa-ban" style='font-size:48px;' id='iModuleState'></i>
				                <figcaption class="ratings" style='margin-top:0px;'>
				                	<p id='pModuleState'></p>
				                    <p id='pModuleHealthy'></p>
				                    <p id='pModuleOper'></p>
				                    <p id='pModuleOperResult' style='color:red'></p>
						            <p><div class="switch"
						            	id="divSwitchProgram" 
						            	data-on="info"
						            	data-off="danger"
						            	data-on-label="<i class='fa fa-check-square-o'></i>开启"
						            	data-off-label="<i class='fa fa-minus-circle'></i>禁用"
						            	title='程序运行的总开关，启用或禁用程序'
						            	>
    									<input type="checkbox" id='iSwitchProgram' checked/>
									</div></p>
				                </figcaption>
				            </figure>
				        </div>
				    </div>
					<div class="col-xs-12 divider text-center" id='divModuleMonitor'>
	                    <div class="col-xs-12 col-sm-3 emphasis">
	                        <h6><strong id='sModuleMemory'>6.55M/555.01M, 11.1%</strong></h6>
	                        <button class="btn btn-success btn-block" onclick='viewModuleLoadChat(1)' id="btnProgramMemory" style='font-size:12px;'>
	                        	<span class="fa fa-exchange"></span> 查看内存使用 </button>
	                    </div>
	                    <div class="col-xs-12 col-sm-3 emphasis">
	                        <h6><strong id='sModuleCpu'>15s, 11.1%</strong></h6>                    
	                        <button class="btn btn-warning btn-block" onclick='viewModuleLoadChat(0)' style='font-size:12px;'>
	                        	<span class="fa fa-dashboard"></span> 查看CPU使用 </button>
	                    </div>
	                    <div class="col-xs-12 col-sm-3 emphasis">
	                        <h6><strong id='sModuleNetload'>0.00K/0.11K, 0.00K/0.11K</strong></h6>                    
	                        <button class="btn btn-info btn-block" onclick='viewModuleLoadChat(2)' style='font-size:12px;'>
	                        	<span class="fa fa-wifi"></span> 查看网络使用 </button>
	                    </div>
	                    <div class="col-xs-12 col-sm-3 emphasis">
	                        <h6><strong id='sModuleDiskload'>100.22M</strong></h6>
	                        <div class="btn-group dropup btn-block">
	                          <button type="button" class="btn btn-primary" id='btnProgramDisk' style='width:256px;font-size:12px;'
								onclick='openPath()'>&nbsp;&nbsp;&nbsp;<span class="fa fa-clipboard"></span> 查看磁盘使用 </button>
	                          <button type="button" class="btn btn-primary dropdown-toggle"
	                          	onclick='dropdownLogs()' style='height:31px'
	                          	title='查看日志'
	                          	data-toggle="dropdown" aria-expanded="false">
	                            <span class="fa fa-caret-down"></span>
	                          </button>
	                          <ul class="dropdown-menu pull-right animated fadeInUp" role="menu" id='ulLogs' style='height:220px;'></ul>
	                        </div>
	                    </div>
               		</div>
				</div>
	   			<iframe src='' name='iProgram' id='iProgram' class='nonicescroll' style='border:1px solid #eee;margin-left:12px;margin-top:0px;'></iframe>
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
<input type='hidden' name='serverkey' id='serverkey' value="<ww:property value='serverkey'/>">
<input type='hidden' name='servertype' id='servertype' value='<ww:property value='servertype'/>'>
<input type='hidden' name='ip' id='ip' value="<ww:property value='ip'/>">
<input type='hidden' name='port' id='port' value="<ww:property value='port'/>">
<iframe name='iDownload' id='iDownload' style='display:none'></iframe>
</form>
<%@ include file="../user/user_select.inc"%>
</body>
<SCRIPT type="text/javascript">
/*实现窗口对齐*/
function resizeWindow()
{
	var userAgent = navigator.userAgent; //取得浏览器的userAgent字符串
	var isChrome = userAgent.indexOf("Chrome") > -1;
	var div = null;
	var w = 15;
	if( document.getElementById( 'tdTree' ).style.display == "" )
	{
		div = document.getElementById( 'divTree' );
		div.style.width = 248;
		div.style.height = windowHeight - 38;
		w = 286;
	}

	var h = 0;
	if( isChrome )
	{
		document.body.style.marginTop = 3;
		h -= 5;
	}
	if( document.getElementById( 'divServerPrograms' ).style.display == "" )
	{
		div = document.getElementById( 'divServerPrograms' );
		h += 256;
		div.style.width = windowWidth - 286; 
	}
	else if( document.getElementById('divServerProgram').style.display == "" )
	{
		div = document.getElementById( 'divServerProgram' );
		div.style.width = windowWidth - 286; 
		if( document.getElementById('divModuleMonitor').style.display == "" )
		{
			h += 321;
		}
		else
		{
			h += 249;
		}
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
		div.style.border = "1px solid #eee";
		div.style.marginLeft = 12;	
	}
	div.style.height = windowHeight - h;
	div.style.width = windowWidth - w;
	//div = document.getElementById( 'figure' );
	//var figureImg = document.getElementById( 'figureImg' );
	//figureImg.style.marginLeft = div.clientWidth/2 - 32;

	var btnProgramMemory = document.getElementById( 'btnProgramMemory' );
	var btnProgramDisk = document.getElementById( 'btnProgramDisk' );
	btnProgramDisk.style.width = btnProgramMemory.clientWidth - 36;
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
//var switchProgram = new Switchery($(".js-switch")[0]);
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

$( '#ulLogs' ).niceScroll({
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
var serverkey = "<ww:property value='serverkey'/>";
var ip = "<ww:property value='ip'/>";
var port = <ww:property value='port'/>;
var osname = "<ww:property value='servertype'/>";
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
		},
		data: {
			key: {
				title: "title"
			}
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
		if( treeNode.type )
		{
			if( treeNode.type == "system"  )
			{
				return;
			}
			document.getElementById( 'liUploadcfg' ).style.display = "";
			//document.getElementById( 'liDownloadcfg' ).style.display = "";
			//document.getElementById( 'liDivider1' ).style.display = "";
			document.getElementById( 'liDivider' ).style.display = "";
			document.getElementById( 'liAdd' ).style.display = "";
			document.getElementById( 'liUploadxml' ).style.display = "";
			if( treeNode.type == "config"  )
			{
				document.getElementById( 'liClear' ).style.display = "";
			}
			else
			{
				document.getElementById( 'liDownloadxml' ).style.display = "";
			}
		}
		else
		{
			if( treeNode.getParentNode().type == "system"  )
			{
				return;
			}

			document.getElementById( 'liDownloadcfg' ).style.display = "";
			if( treeNode.editing )
			{
				document.getElementById( 'liCancel' ).style.display = "";
				if( treeNode.oper != 3 ) document.getElementById( 'liPreset' ).style.display = ""
			}
			else
			{
				document.getElementById( 'liDelete' ).style.display = "";
				document.getElementById( 'liPreset' ).style.display = "";
			}
				
			if( treeNode.getParentNode().type == "system" )
			{
				document.getElementById( 'liPreset' ).style.display = "none";
			}
		}
	
		<ww:if test='grant'>
		if (!treeNode && event.target.tagName.toLowerCase() != "button" && $(event.target).parents("a").length == 0) {
			myZtree.cancelSelectedNode();
			showRMenu("root", event.clientX, event.clientY);
		} else if (treeNode && !treeNode.noR) {
			myZtree.selectNode(treeNode);
			showRMenu("node", event.clientX, event.clientY);
		}
		</ww:if>
	}

	function addDiyDom(treeId, treeNode)
	{
		if( treeNode.ip )
		{
			return;
		}
		addStateIco(treeNode);
		//if( treeNode.debug ) debugIcon = "<i class='skit_fa_icon_war fa fa-debug' title='程序已开启调试输出请查看log/"+treeNode.id+"/debug_"+treeNode.id+".txt文件' style='font-size:16px;'></i>";
	}

	function onClick(event, treeId, treeNode)
	{
		myZtree.expandNode(treeNode, null, null, null, true);
		myZtree.expandNode(treeNode, true);
		open(treeNode);
	}
	
	$(document).ready(function(){
		try
		{
			var jsonData = '<ww:property value="jsonData" escape="false"/>';
			var zNodes = jQuery.parseJSON(jsonData);
			//var zNodes = json.children;
			$.fn.zTree.init($("#myZtree"), setting, zNodes);
			myZtree = $.fn.zTree.getZTreeObj("myZtree");
			myZtreeMenu = $("#rMenu");
			var id = getUserActionMemory("control!open.program-<ww:property value='id'/>");
			if( id )
			{
				var node = myZtree.getNodeByParam("id", id);
				if( node )
				{
					myZtree.selectNode(node);
					myZtree.expandNode(node, true);
					open(node);
					//presetProgramConfig();
				}
			}
			else
			{
				var node = myZtree.getNodeByParam("type", "user");
				if( node )
				{
					myZtree.selectNode(node);
					myZtree.expandNode(node, true);
					open(node);
					//presetProgramConfig();
				}
			}
		}
		catch(e)
		{
			skit_alert("初始化目录树异常"+e.message+", 行数"+e.lineNumber);
		}
	});
//#########################################################################
function checkPrivileges(program)
{
	if( grant )	return true;
	
	if( program && program.manager == "<ww:property value='userAccount'/>" )
	{
		return true;
	}
	skit_alert("您没有操作程序管理的权限。");
	return false;	
}
if( $('#divSwitchDebug') )
	$('#divSwitchDebug').on('switch-change', function (e, data) {
	});

var autoSwitchProgram = false;
$('#divSwitchProgram').on('switch-change', function (e, data) {
	if( !checkPrivileges() ) return;
	if( autoSwitchProgram )
	{
		autoSwitchProgram = false;
		return;
	}
	autoSwitchProgram = false;
	var $el = $(data.el), value = data.value;
	//alert('a:'+value);
	var nodes = myZtree.getSelectedNodes();
	if( !nodes || nodes.length == 0)
	{
		skit_alert("请先在目录导航树上选择您要操作的程序");
		return;
	}
	var program = nodes[0];
	var tip = "您确定要禁用伺服器【"+ip+"】下程序【"+program.name+"】吗？";
	if( value )	tip = "您确定要启用伺服器【"+ip+"】下程序【"+program.name+"】吗？";
	skit_confirm(tip, function(yes){
		if( yes )
		{
			skit_showLoading();
			ControlMgr.switchProgram(ip, port, serverkey, program.id, value, <ww:property value='id'/>, {
				callback:function(rsp) {
					skit_hiddenLoading();
					try
					{
						skit_alert(rsp.message);
						if( !rsp.succeed )
						{
							$('#divSwitchProgram').bootstrapSwitch('setState', program["switch"]);
						}
					}
					catch(e)
					{
						skit_alert("启用禁用伺服器【"+ip+"】下程序【"+program.name+"】出现异常"+e.message+", 行数"+e.lineNumber);
					}
				},
				timeout:30000,
				errorHandler:function(err) {skit_hiddenLoading(); alert(err); }
			});
		}
		else
		{
			$('#divSwitchProgram').bootstrapSwitch('setState', program["switch"]);
		}
	});	
});

function switchDebug(active, a)
{
	var nodes = myZtree.getSelectedNodes();
	if( !nodes || nodes.length == 0)
	{
		skit_alert("请先在目录导航树上选择您要操作的程序");
		return;
	}
	var program = nodes[0];
	var tip = "您确定要禁用伺服器【"+ip+"】下程序【"+program.name+"】调试日志输出吗？";
	if( active ) tip = "您确定要启用伺服器【"+ip+"】下程序【"+program.name+"】调试日志输出吗？";
	skit_confirm(tip, function(yes){
		if( yes )
		{
			skit_showLoading();
			ControlMgr.switchDebug(ip, port, serverkey, program.id, active, <ww:property value='id'/>, {
				callback:function(rsp) {
					skit_hiddenLoading();
					try
					{
						skit_alert(rsp.message);
						if( rsp.succeed )
						{
						}
					}
					catch(e)
					{
						skit_alert("启用禁用伺服器【"+ip+"】下程序【"+program.name+"】调试日志输出出现异常"+e.message+", 行数"+e.lineNumber);
					}
				},
				timeout:30000,
				errorHandler:function(err) {skit_hiddenLoading(); alert(err); }
			});
		}
	});	
}

function dropdownLogs()
{
	var nodes = myZtree.getSelectedNodes();
	if( !nodes || nodes.length == 0)
	{
		skit_alert("请先在目录导航树上选择您要操作的程序");
		return;
	}
	var program = nodes[0];
	MonitorMgr.getModuleLogs(ip, port, program.id, {
		callback:function(logs){
			if( logs )
			{
				var ul = document.getElementById("ulLogs");
				ul.innerHTML = "";
				var html = "";
				for(var i = 0; i < logs.length; i++)
				{
					var rows = logs[i];
					var func = 'previewlog("'+rows.path+'","'+rows.name+'","'+rows.length+'","'+rows.scale+'","'+rows.size+'")';
					html += "<li><a title='"+rows.path+"' onclick='"+func+"' style='font-size:9pt;padding:2px 10px;'>" + rows.name + "</a></li>";
				}
				ul.innerHTML = html;
			}
			else
			{
				skit_alert("获取模块日志超时，请联系系统管理员检查服务器主控程序是否正常工作，或者网络配置是否正常。");
			}
		},
		timeout:30000,
		errorHandler:function(message) {skit_alert(message);}
	});
}
//预览日志
function previewlog(path, name, length, scale, size)
{
	var url = "files!previewlog.action?path="+path+"&ip="+ip+"&port="+port+"&length="+length;
	if( scale != 'K' )
	{
		skit_confirm("需要预览的日志"+name+"有"+size+"，您是否压缩下载？", function(yes){
			if( yes )
			{
				document.forms[0].path.value = path;
				document.forms[0].action = "files!download.action";
				document.forms[0].method = "POST";
				document.forms[0].target = "iDownload";
				document.forms[0].submit();
			}
			else
			{
				openView(ip+":"+path+"("+size+")", url);
			}
		});
	}
	else
	{
		openView(ip+":"+path+"("+size+")", url);
	}
}

function openServerPrograms()
{
	var nodes = myZtree.getSelectedNodes();
	if( !nodes || nodes.length == 0)
	{
		skit_alert("请先在目录导航树上选择您要操作的容器");
		return;
	}
	var server = nodes[0];
	document.getElementById( 'btnLoad' ).style.display = "";
	document.getElementById( 'btnPrograms' ).style.display = "none";
	document.getElementById( 'command' ).value = server.inner?'sysPrograms':'userPrograms';
	//alert(document.getElementById( 'command' ).value);
	buildIframeNiceScroll();
	document.forms[0].action = "monitor!server.action";
	document.forms[0].target = "iProgram";
	document.forms[0].submit();
}

function viewModuleLoadChat(c)
{
	if( c == 1 )
	{
		var nodes = myZtree.getSelectedNodes();
		if( !nodes || nodes.length == 0)
		{
			skit_alert("请先在目录导航树上选择您要操作的程序");
			return;
		}
		var program = nodes[0];
		document.getElementById( 'id' ).value = program.id;
		document.getElementById( 'command' ).value = c;
		buildIframeNiceScroll();
		document.forms[0].action = "monitorload!modulememory.action";
		document.forms[0].target = "iProgram";
		document.forms[0].submit();
	}
	else
	{
		document.getElementById( 'id' ).value = <ww:property value='id'/>;
		//alert(document.getElementById( 'id' ).value);
		viewServerLoadChat(c);
		//openView("伺服器【"+ip+"】程序系统监控", "monitor!server.action?id=<ww:property value='id'/>");
	}
}

function viewServerLoadChat(c)
{
	document.getElementById( 'btnLoad' ).style.display = "none";
	document.getElementById( 'btnPrograms' ).style.display = "";
	document.getElementById( 'command' ).value = c;
	buildIframeNiceScroll();
	document.forms[0].action = "monitorload!serverchart.action";
	document.forms[0].target = "iProgram";
	document.forms[0].submit();
}

function setControlTitle()
{
	if( !checkPrivileges() ) return;
	var nodes = myZtree.getSelectedNodes();
	if( !nodes || nodes.length == 0)
	{
		skit_alert("请先在目录导航树上选择您要操作的容器");
		return;
	}
	var server = nodes[0];
	window.top.skit_input("请填写伺服器程序容器主控描述", server.title, function(yes, val){
		if( yes ){
			if( val == "" )
			{
				window.top.skit_alert("请填写伺服器程序容器主控描述。");
				return;
			}
			skit_showLoading();
			ControlMgr.setControlTitle(ip, port, serverkey, val, <ww:property value='id'/>, {
				callback:function(rsp) {
					skit_hiddenLoading();
					try
					{
						if( rsp.succeed )
						{
							document.getElementById( 'spanControlTitle' ).innerHTML = val;
						}
						else skit_alert(rsp.message);
					}
					catch(e)
					{
						skit_alert("设置伺服器【"+ip+"】程序容器的主控描述出现异常"+e.message+", 行数"+e.lineNumber);
					}
		        	$("#ulVersions").getNiceScroll().remove();
				},
				timeout:30000,
				errorHandler:function(err) {skit_hiddenLoading(); alert(err); }
			});
		}
	});
}
//设置程序配置文件
function configProgramFile(){
	hideRMenu();
	if( !checkPrivileges() ) return;
	var nodes = myZtree.getSelectedNodes();
	if( !nodes || nodes.length == 0)
	{
		skit_alert("请先在目录导航树上选择您要操作的程序");
		return;
	}
	var program = nodes[0];
	if( program.control && program.control.cfgfile) {
		
		var title = "配置伺服器【"+ip+":"+port+"】程序: "+program.name+"["+program.id+"]";
		var url = "files!edit.action?ip="+ip+"&port="+port+"&id=<ww:property value='id'/>&path="+program.control.cfgfile;
		window.setTimeout("openView('"+title+"','"+url+"');", 500);	
	}
	else{
		skit_alert("伺服器【"+ip+":"+port+"】程序: "+program.name+"["+program.id+"] 没有设置配置文件路径");	
	}
}

var inputProgramTitle = document.createElement("input");
inputProgramTitle.id = "inputProgramTitle";
var inputProgramVersion = document.createElement("input");
inputProgramVersion.id = "inputProgramVersion";
var textareaProgramDesc = document.createElement("textarea");
textareaProgramDesc.id = "textareaProgramDesc";
function setProgramRemark()
{
	hideRMenu();
	if( !checkPrivileges() ) return;
	var nodes = myZtree.getSelectedNodes();
	if( !nodes || nodes.length == 0)
	{
		skit_alert("请先在目录导航树上选择您要操作的程序");
		return;
	}
	var program = nodes[0];
	try
	{
		var div0 = document.createElement("div");
		div0.className = "panel panel-default";
		var div1 = document.createElement("div");
		div1.className = "panel-body";
		div1.style.paddingBottom = 1;
		div0.appendChild(div1);
		
		var div2 = document.createElement("div");
		div2.className = "form-group";
		div1.appendChild(div2);
		var div3 = document.createElement("div");
		div3.className = "input-group";
		div2.appendChild(div3);
		var span = document.createElement("span");
		span.className = "input-group-addon";
		span.innerHTML = "<i class='fa fa-info-circle'></i> 程序名称";
		div3.appendChild(span);
		inputProgramTitle.className = "form-control";
		inputProgramTitle.type = "text";
		inputProgramTitle.placeholder = "只能含有汉字、数字、字母、不能以下划线开头和结尾，不超过20个字";
		div3.appendChild(inputProgramTitle);
		
		div2 = document.createElement("div");
		div2.className = "form-group";
		div1.appendChild(div2);
		div3 = document.createElement("div");
		div3.className = "input-group";
		div2.appendChild(div3);
		span = document.createElement("span");
		span.className = "input-group-addon";
		span.innerHTML = "<i class='fa fa-paw'></i> 程序版本";
		div3.appendChild(span);
		inputProgramVersion.className = "form-control";
		inputProgramVersion.type = "text";
		inputProgramVersion.readOnly = true;
		div3.appendChild(inputProgramVersion);
		var div4 = document.createElement("div");
		div4.className = "input-group-btn";
		div4.innerHTML = "<button type='button' class='btn btn-info dropdown-toggle' data-toggle='dropdown' aria-expanded='false' style='display: inline-block;height:34px;'><span class='caret'></span></button>"+
	    	"<ul class='dropdown-menu pull-right animated fadeInUp' id='ulVersions' style='height:128px;cursor:pointer;'></ul>";
		div3.appendChild(div4);

		div2 = document.createElement("div");
		div2.className = "form-group";
		div1.appendChild(div2);
		span = document.createElement("span");
		span.className = "input-group-addon";
		span.innerHTML = "<i class='fa fa-pencil-square'></i> 程序描述";
		div2.appendChild(span);
		textareaProgramDesc.className = "form-control";
		textareaProgramDesc.rows = 7;
		//style='color:#66cccc;background-color:#eee;'
		textareaProgramDesc.style.color = "#66cccc";
		textareaProgramDesc.style.backgroundColor = "#eee";
		textareaProgramDesc.placeholder = "请输入关于该程序的简单介绍，简洁准确的名称和详细的描述将提升程序管理的效率。";
		div2.appendChild(textareaProgramDesc);

		var tip = "修改伺服器【"+ip+"】下程序【"+program.id+"】的名称和描述吗？";
		var SM = new SimpleModal({"btn_ok":"确定","btn_cancel":"取消","width":520});
	    var content = "<div style='height:358px;width:480px;border:0px solid red;overflow-y:auto;'>"+div0.outerHTML+"</div>"
	    SM.show({
	    	"title":tip,
	        "model":"confirm",
	        "callback": function(){
	        	inputProgramTitle = document.getElementById(inputProgramTitle.id);
	        	var programTitle = inputProgramTitle.value;
				if( programTitle == "" )
				{
					skit_errtip("程序名称不允许为空.", inputProgramTitle);
			        return false;
				}
		        if( !programTitle.match(new RegExp("^[a-zA-Z0-9_.\u4e00-\u9fa5]+$")) )
		        {
		        	skit_errtip("程序名称只能含有汉字、数字、字母、下划线不能以下划线开头和结尾", inputProgramTitle);
			        return false;
		        }
				if( programTitle.length<2||programTitle.length>32 )
		        {
					skit_errtip("程序名称不少于2个字不超过32个字", inputProgramTitle);
			        return false;
		        }

	        	inputProgramVersion = document.getElementById(inputProgramVersion.id);
	        	var programVersion = inputProgramVersion.value;
				if( programVersion == "" || programVersion == "0.0.0.0" )
				{
					skit_errtip("程序版本必须选择，如果还未配置版本号请先配置。", programVersion);
			        return false;
				}
				
				textareaProgramDesc = document.getElementById(textareaProgramDesc.id);
	        	var programRemark = textareaProgramDesc.value;
				if( programRemark.length<16||programRemark.length>1024 )
		        {
					skit_errtip("程序描述不少于16个字不超过140个字", programRemark);
			        return false;
		        }
				
				skit_showLoading();
				ControlMgr.setProgramRemark(ip, port, serverkey, program.id, programTitle, programVersion, programRemark, <ww:property value='id'/>, {
					callback:function(rsp) {
						skit_hiddenLoading();
						try
						{
							if( rsp.succeed )
							{
								program.name = programTitle;
								program.title = programRemark;
								program.version = programVersion;
								document.getElementById( 'spanProgramTitle' ).innerHTML = program.name;
								document.getElementById( 'spanProgramVersion' ).innerHTML = program.version;
								myZtree.updateNode(program);
							}
							else skit_alert(rsp.message);
						}
						catch(e)
						{
							skit_alert("设置伺服器【"+ip+"】下程序【"+program.name+"】的名称与描述出现异常"+e.message+", 行数"+e.lineNumber);
						}
			        	$("#ulVersions").getNiceScroll().remove();
					},
					timeout:30000,
					errorHandler:function(err) {skit_hiddenLoading(); alert(err); }
				});
				return true;
	        },
	        "cancelback": function(){
	        	$("#ulVersions").getNiceScroll().remove();
	        },
	    	"contents": content
	    });
	    document.getElementById(inputProgramTitle.id).value = program.name;
	    document.getElementById(textareaProgramDesc.id).value = program.description?program.description:"";
	    document.getElementById(inputProgramVersion.id).value = program.version;
	    $( '#ulVersions' ).niceScroll({
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
		ControlMgr.getVersionSelection(ip, port, serverkey, program.id, <ww:property value='id'/>, {
			callback:function(rsp) {
				skit_hiddenLoading();
				try
				{
					if( rsp.succeed )
					{
						var versions = rsp.objects;
						if( versions.length == 0 )
						{
							skit_confirm("该伺服器【"+ip+"】程序【"+program.id+"】还未设置版本列表数据，您离开配置吗？", function(yes){
								if( yes )
								{
									SM.hide();
									presetControlVersion();
								}
							});
							return;
						}
						var ul = document.getElementById("ulVersions");
						ul.innerHTML = "";
						var html = "";
						for(var i = 0; i < versions.length; i++)
						{
							var version = versions[i];
							var func = "document.getElementById(\"inputProgramVersion\").value=\""+version+"\";";
							html += "<li><a onclick='"+func+"' style='font-size:9pt;padding:2px 10px;'>" + version + "</a></li>";
						}
						ul.innerHTML = html;
					}
					else skit_alert(rsp.message);
				}
				catch(e)
				{
					skit_alert("选择伺服器【"+ip+"】下程序【"+program.name+"】的版本号出现异常"+e.message+", 行数"+e.lineNumber);
				}
			},
			timeout:30000,
			errorHandler:function(err) {skit_hiddenLoading(); alert(err); }
		});
	}
	catch(e)
	{
		skit_alert("设置程序备注出现异常"+e.message+", 行数"+e.lineNumber);
	}
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
	var dataurl = "control!versiontimeline.action?id="+program.id+"&ip="+ip+"&port="+port+"&serverkey="+serverkey;
	dataurl = chr2Unicode(dataurl);
	buildIframeNiceScroll();
	document.getElementById("iProgram").src = "helper!timeline.action?dataurl="+dataurl;
	if( fullscreen )
	{
		openView("伺服器【"+ip+"】程序【"+program.id+" "+program.name+"】版本详情", "helper!timeline.action?dataurl="+dataurl);
	}
}

function goPublish()
{
	openView('审核集群程序发布管理', 'control!publish.action');
}

function presetControlVersion()
{
	if( !checkPrivileges() ) return;
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
	document.forms[0].action = "control!presetversion.action";
	document.forms[0].target = "iProgram";
	document.forms[0].submit();
}

function addStateIco(treeNode)
{
	var treeItem = $("#" + treeNode.tId + "_a");
	var ico = "skit_fa_icon_gray fa fa-ban";
	var title = "";
	if( treeNode.editing )
	{
		switch( treeNode.oper )
		{
		case 0:
			title = "新增配置中";
			ico = "skit_fa_icon fa fa-plus-square";
			break; 
		case 1:
			title = "修改配置中";
			ico = "skit_fa_icon_blue fa fa-edit";
			break;
		case 3:
			title = "删除配置中";
			ico = "skit_fa_icon_red fa fa-minus-square";
			break;
		case 5:
			title = "配置已拒绝";
			ico = "skit_fa_icon_yellow fa fa-exclamation-triangle";
			break;
		default:
			title = "未知状态";
			ico = "skit_fa_icon_gray fa fa-chain-broken";
			break; 
		}
		treeItem.before("<i class='"+ico+"' title='"+title+"' style='font-size:14px;'></i>");
	}
	else if( treeNode.state >= 0 )
	{
		var state = treeNode.state;
		switch( state )
		{
		case 0:
			title = "未运行";
			ico = "skit_fa_icon_gray fa fa-ban";
			break;
		case 1:
			title = "运行中";
			ico = "skit_fa_icon_blue fa fa-play-circle-o";
			break;
		case 2:
			title = "关闭中";
			ico = "skit_fa_icon_red fa fa-ban";
			break;
		case 3:
			title = "已关闭";
			ico = "skit_fa_icon_gray fa fa-minus-circle";
			break;
		case 5:
			title = "暂停中";
			ico = "skit_fa_icon_red fa fa-minus-circle";
			break;
		case 6:
			title = "已暂停";
			ico = "skit_fa_icon_war fa fa-minus-circle";
			break;
		case 7:
			title = "已关闭";
			ico = "skit_fa_icon_red fa fa-ban";
			break; 
		default:
			title = "未知状态";
			ico = "skit_fa_icon_red fa fa-chain-broken";
			break; 
		}
		treeItem.before("<i class='"+ico+"' title='"+title+"' style='font-size:14px;'></i>");
	}
}

function saveProgramConfig()
{
	if( window.frames["iProgram"].setProgramConfig )
	{
		window.frames["iProgram"].setProgramConfig();
	}
}

function setProgramConfig(oldid, json)
{
	skit_confirm("您确定要配置伺服器【"+ip+"】的程序吗？", function(yes){
		if( yes )
		{
			skit_showLoading();
			ControlMgr.setProgramConfig(oldid, json, <ww:property value='id'/>, {
				callback:function(rsp) {
					skit_hiddenLoading();
					try
					{
						if( rsp.succeed )
						{
							var program = jQuery.parseJSON(rsp.result);
							if( oldid && program.id != oldid )
							{
								var rm = myZtree.getNodeByParam("id", oldid+"*" );
								if( rm ) myZtree.removeNode(rm);
							}
							var program0 = myZtree.getNodeByParam("id", program.id );
							//alert("program0: "+program0.name+" = "+program.name);
							if( program0 )
							{
								program0.name = program.name;
								myZtree.updateNode(program0);
							}
							else
							{
								var parentNode = myZtree.getNodeByParam("type", "config" );
								program["ip"] = "";
								program["editing"] = true;
								program["icon"] = "images/icons/tile.png";
								program0 = myZtree.addNodes(parentNode, -1, program)[0];
								myZtree.selectNode(program0);
								myZtree.expandNode(program0, true);
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
						skit_alert("配置伺服器【"+ip+"】的程序出现异常"+e.message+", 行数"+e.lineNumber);
					}
				},
				timeout:30000,
				errorHandler:function(err) {skit_hiddenLoading(); alert(err); }
			});
		}
	});
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
	if( osname == "N/A" )
	{
		skit_alert("伺服器【"+ip+"】的操作系统类型未知，不能为其配置程序，请联系您的系统管理员。");
		return;
	}
	var program = nodes[0];
	if( !program.editing )
	{
		var program1 = myZtree.getNodeByParam("id", program.id+"*" );
		if( program1 )
		{
			myZtree.selectNode(program1);
			myZtree.expandNode(program1, true);
			open(program1);
			skit_alert("您要配置的伺服器【"+ip+"】程序【"+program.id+" "+program.name+"】目前正处于审核中，请重新选择您的操作。");
			return;
		}
	}
	if( program.ip )
	{
		document.getElementById( 'id' ).value = ""
	}
	else
	{
		document.getElementById( 'id' ).value = program.id;
	}
	buildIframeNiceScroll();
	//TODO:全屏
	document.getElementById( 'divServerPrograms' ).style.display = "none";
	document.getElementById( 'divServerProgram' ).style.display = "none";
	document.getElementById( 'tdTree' ).style.display = "none";
	if( grant )
	{
		document.getElementById("divPreset").style.display = "none";
		document.getElementById("divCancelConfig").style.display = "none";
		document.getElementById("divCloseConfig").style.display = "";
		document.getElementById("divSaveConfig").style.display = "";
	}
	resizeWindow();
	skit_showLoading();
	document.forms[0].action = "control!preset.action";
	document.forms[0].target = "iProgram";
	document.forms[0].submit();
}

function delProgramConfig()
{
	hideRMenu();
	if( !checkPrivileges() ) return;
	var nodes = myZtree.getSelectedNodes();
	if( !nodes || nodes.length == 0)
	{
		skit_alert("请先在目录导航树上选择您要删除的程序");
		return;
	}
	var program = nodes[0];
	if( program.editing )
	{
		cancelProgramConfig();
		return;
	}

	var program1 = myZtree.getNodeByParam("id", program.id+"*" );
	if( program1 )
	{
		myZtree.selectNode(program1);
		myZtree.expandNode(program1, true);
		open(program1);
		skit_alert("您要删除的伺服器【"+ip+"】程序【"+program.id+" "+program.name+"】目前正处于配置审核中，请重新选择您的操作。");
		return;
	}
		
	skit_confirm("您确定要删除伺服器【"+ip+"】程序【"+program.id+" "+program.name+"】吗？程序删除需要系统管理员审核。", function(yes){
		if( yes )
		{
			skit_showLoading();
			ControlMgr.delProgramConfig(ip, port, serverkey, program.id, <ww:property value='id'/>, {
				callback:function(rsp) {
					skit_hiddenLoading();
					try
					{
						if( rsp.succeed )
						{
							var program0 = myZtree.getNodeByParam("id", program.id+"*" );
							if( program0 )
							{
								myZtree.removeNode(program0);
							}
							var program0 = jQuery.parseJSON(rsp.result);
							var parentNode = myZtree.getNodeByParam("type", "config" );
							program0["editing"] = true;
							program0["id"] = program.id+"*";
							program0["icon"] = "images/icons/tile.png";
							program0 = myZtree.addNodes(parentNode, -1, program0)[0];
							//addStateIco(program0);
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

function clearProgramConfig()
{
	hideRMenu();
	skit_confirm("您确定要清除伺服器【"+ip+"】下所有待配置程序？", function(yes){
		if( yes )
		{
			skit_showLoading();
			ControlMgr.clearProgramConfig(ip, port, serverkey, <ww:property value='id'/>, {
				callback:function(rsp) {
					skit_hiddenLoading();
					try
					{
						if( rsp.succeed )
						{
							var parentNode = myZtree.getNodeByParam("type", "config" );
							myZtree.removeChildNodes(parentNode);
							parentNode["isParent"] = true;
							myZtree.updateNode(parentNode);
							myZtree.selectNode(parentNode);
							myZtree.expandNode(parentNode, true);
							open(parentNode);
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

function cancelProgramConfig()
{
	hideRMenu();
	if( !checkPrivileges() ) return;
	var nodes = myZtree.getSelectedNodes();
	if( !nodes || nodes.length == 0)
	{
		skit_alert("请先在目录导航树上选择您要撤销配置的程序");
		return;
	}
	var program = nodes[0];
	if( !program.editing )
	{
		delProgramConfig();
		return;
	}
	skit_confirm("您确定要撤销配置伺服器【"+ip+"】程序【"+program.id+" "+program.name+"】吗？", function(yes){
		if( yes )
		{
			skit_showLoading();
			ControlMgr.cancelProgramConfig(ip, port, serverkey, program.id, <ww:property value='id'/>, {
				callback:function(rsp) {
					skit_hiddenLoading();
					try
					{
						if( rsp.succeed )
						{
							var parentNode = program.getParentNode();
							myZtree.removeNode(program);
							var treeNode = myZtree.getNodeByParam("id", rsp.result );
							if( treeNode )
							{
								myZtree.selectNode(treeNode);
								myZtree.expandNode(treeNode, true);
								open(treeNode);
							}
							else
							{
								parentNode["isParent"] = true;
								myZtree.updateNode(parentNode);
								myZtree.selectNode(parentNode);
								myZtree.expandNode(parentNode, true);
								open(parentNode);
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
		presetControlVersion();
	}
}

var v = 0;
function open(treeNode)
{
	try
	{
		document.getElementById( 'tdTree' ).style.display = "";
		if( treeNode.type && treeNode.type != "programs" )
		{
			if( grant )
			{
				document.getElementById("divPreset").style.display = treeNode.inner?"none":"";
				document.getElementById("divCloseConfig").style.display = "none";
				document.getElementById("divSaveConfig").style.display = "none";
				document.getElementById("divCancelConfig").style.display = "none";
				document.getElementById("btnPreset").innerHTML = "<i class='fa fa-plus-circle'></i> 新增程序配置</button>";
			}
			document.getElementById( 'divServerPrograms' ).style.display = "";
			document.getElementById( 'divServerProgram' ).style.display = "none";
			document.getElementById( 'memoryStateCheck' ).style.display = "";
			document.getElementById("memoryStateCheck").innerHTML = 
				"<a onclick='viewRunStateInfo(\"memoryStateCheckDesc\")' style='color:#ffd800'>"+
				"<i class='fa fa-warning'></i> 异常</a>";
			document.getElementById("hRunState").innerHTML = treeNode.runStateInfo;
			document.getElementById("sMemoryUsageInfo").innerHTML = treeNode.memoryUsageInfo;
			document.getElementById("cpuState").src = getStateIcon(treeNode.cpuState);
			document.getElementById("memoryState").src = getStateIcon(treeNode.memoryState);
			document.getElementById("netState").src = getStateIcon(treeNode.netState);
			document.getElementById("diskState").src = getStateIcon(treeNode.diskState);
			document.getElementById("pHealthy").innerHTML = getHealthyHtml(treeNode.healthy);
			document.getElementById("id").value = treeNode.id;
			setUserActionMemory("control!open.program-<ww:property value='id'/>", "");
			resizeWindow();
			openServerPrograms();
			//viewServerLoadChat(-1);
		}
		else if( treeNode.id )
		{
			document.getElementById( 'divServerPrograms' ).style.display = "none";
			document.getElementById( 'divServerProgram' ).style.display = "";
			setModuleState(treeNode.state, document.getElementById( 'iModuleState' ), document.getElementById( 'pModuleState' ));
			if( grant ) 
			{
				document.getElementById("divPreset").style.display = "";
				document.getElementById("btnPreset").innerHTML = "<i class='fa fa-edit'></i> 配置程序</button>";
			}
			if( treeNode.editing )
			{
				if( grant ) 
				{
					document.getElementById("divCancelConfig").style.display = "";
					document.getElementById("btnCancel").innerHTML = "<i class='fa fa-reply'></i> 撤销配置程序</button>";
				}
				document.getElementById( 'divModuleMonitor' ).style.display = "none";
				document.getElementById( 'pModuleHealthy' ).style.display = "none";
				document.getElementById( 'pModuleOper' ).style.display = "";
				document.getElementById( 'pModuleOperResult' ).style.display = "";
				document.getElementById( 'pModuleOper' ).innerHTML = "操作者: "+treeNode.operuser;
				document.getElementById( 'pModuleOperResult' ).innerHTML = treeNode.operlog?treeNode.operlog:"";
				document.getElementById( 'pRunInfo' ).style.display = "none";
				document.getElementById( 'pCfgInfo' ).style.display = "";

				document.getElementById("divSwitchProgram").style.display = "none";
				document.getElementById("divSwitchProgram").style.display = "none";
				document.getElementById("aSetProgramRemark").style.display = "none";
				document.getElementById("aConfigProgramFile").style.display = "none";
				document.getElementById("aSetProgramer").style.display = "none";
				document.getElementById("divCloseConfig").style.display = "none";
				document.getElementById("divSaveConfig").style.display = "none";
				document.getElementById("sPresetControlVersion").style.display = "";
				document.getElementById("spanLastConfigTime").innerHTML = treeNode.opertime;
				document.getElementById("pModuleState").style.display = "";
				if( treeNode.oper == 0 )
				{
					document.getElementById( 'h2ProgramId' ).innerHTML = treeNode.id+ " <span style='color:#888;font-size:12px;'>"+
					"<i class='skit_fa_icon_green fa fa-edit' style='font-size:16px;'></i>"+
					" 新增程序配置待系统管理员审批生效</span>";
					document.getElementById( 'iModuleState' ).className = "skit_fa_icon fa fa-plus-square";
					document.getElementById( 'pModuleState' ).innerHTML = "新增程序配置";
				}
				else if( treeNode.oper == 1 )
				{
					document.getElementById( 'h2ProgramId' ).innerHTML = treeNode.id+ " <span style='color:#888;font-size:12px;'>"+
					"<i class='skit_fa_icon_green fa fa-edit' style='font-size:16px;'></i>"+
					" 修改程序配置待系统管理员审批生效</span>";
					document.getElementById( 'iModuleState' ).className = "skit_fa_icon_blue fa fa-edit";
					document.getElementById( 'pModuleState' ).innerHTML = "修改程序配置";
				}
				else if( treeNode.oper == 3 )
				{
					document.getElementById( 'h2ProgramId' ).innerHTML = treeNode.id+" <span style='color:#888;font-size:12px;'>"+
						"<i class='skit_fa_icon_red fa fa-remove' style='font-size:16px;'></i>"+
						" 删除程序配置待系统管理员审批生效可撤销</span>";
					if( grant )
					{
						document.getElementById("divPreset").style.display = "none";
					}
					document.getElementById( 'iModuleState' ).className = "skit_fa_icon_red fa fa-minus-square";
					document.getElementById( 'pModuleState' ).innerHTML = "删除程序配置";
					document.getElementById("sPresetControlVersion").style.display = "none";
				}
				else if( treeNode.oper == 5 )
				{
					document.getElementById( 'h2ProgramId' ).innerHTML = treeNode.id+" <span style='color:#888;font-size:12px;'>"+
						"<i class='skit_fa_icon_yellow fa fa-exclamation-triangle' style='font-size:16px;'></i>"+
						" 程序配置请求被系统管理员拒绝</span>";
					document.getElementById( 'iModuleState' ).className = "skit_fa_icon_yellow fa fa-exclamation-triangle";
					document.getElementById("pModuleState").style.display = "none";
					document.getElementById("sPresetControlVersion").style.display = "none";
				}
			}
			else
			{
				if( treeNode.debug )
				{
					document.getElementById( 'h2ProgramId' ).innerHTML = treeNode.id+ 
					" <a style='color:#888;font-size:12px;' onclick='switchDebug(false, this)' title='程序日志调试输出是配置程序初期检查问题的重要方法'>"+
					"<i class='skit_fa_icon_green fa fa-bug' style='font-size:16px;'></i>"+
					" 调试日志输出中</a>";
				}
				else
				{
					document.getElementById( 'h2ProgramId' ).innerHTML = treeNode.id+ 
					" <a style='color:#888;font-size:12px;' onclick='switchDebug(true, this)' title='程序日志调试输出是配置程序初期检查问题的重要方法'>"+
					"<i class='skit_fa_icon_gray fa fa-bug' style='font-size:16px;'></i>"+
					" 调试日志输出已关闭</a>";
				}
				
				document.getElementById( 'divModuleMonitor' ).style.display = "";
				document.getElementById( 'sModuleMemory' ).innerHTML = treeNode.memoryInfo?treeNode.memoryInfo:"N/A";
				document.getElementById( 'sModuleCpu' ).innerHTML = treeNode.cpuInfo?treeNode.cpuInfo:"N/A";
				document.getElementById( 'sModuleNetload' ).innerHTML = treeNode.netloadInfo?treeNode.netloadInfo:"N/A";
				document.getElementById( 'sModuleDiskload' ).innerHTML = treeNode.diskloadInfo?treeNode.diskloadInfo:"N/A";
				document.getElementById( 'pModuleHealthy' ).style.display = "";
				document.getElementById( 'pModuleHealthy' ).innerHTML = "健康度"+getHealthyHtml(treeNode.healthy);
				document.getElementById( 'pModuleOper' ).style.display = "none";
				document.getElementById( 'pModuleOperResult' ).style.display = "none";
				document.getElementById( 'pRunInfo' ).style.display = "";
				document.getElementById( 'pCfgInfo' ).style.display = "none";

				if( grant )
				{
					document.getElementById("divCancelConfig").style.display = "";
					document.getElementById("btnCancel").innerHTML = "<i class='fa fa-remove'></i> 删除程序</button>";
					document.getElementById("divPreset").style.display = "none";
					document.getElementById("divCloseConfig").style.display = "none";
					document.getElementById("divSaveConfig").style.display = "none";
				}
				if( treeNode.getParentNode().type == "system" )
				{
					document.getElementById("divCancelConfig").style.display = "none";
					document.getElementById("divSwitchProgram").style.display = "none";
					document.getElementById("aSetProgramRemark").style.display = "none";
					document.getElementById("aConfigProgramFile").style.display = "none";
					document.getElementById("aSetProgramer").style.display = "none";
					document.getElementById("sPresetControlVersion").style.display = "none";
				}
				else
				{
					if( grant ) document.getElementById("divPreset").style.display = "";
					autoSwitchProgram = true;
					$('#divSwitchProgram').bootstrapSwitch('setState', treeNode["switch"]);
					window.setTimeout("autoSwitchProgram=false;",1000);
					document.getElementById("divSwitchProgram").style.display = "";
					document.getElementById("aSetProgramRemark").style.display = "";
					document.getElementById("aConfigProgramFile").style.display = "";
					document.getElementById("aSetProgramer").style.display = "";
					document.getElementById("sPresetControlVersion").style.display = "";
				}
			}
			document.getElementById( 'spanProgramTitle' ).innerHTML = treeNode.name;
			document.getElementById( 'spanProgramVersion' ).innerHTML = treeNode.version;
			document.getElementById( 'spanProgramer' ).innerHTML = treeNode.programmer;
			document.getElementById( 'spanLastRunTime' ).innerHTML = treeNode.startupTime?treeNode.startupTime:"N/A";
			
			setUserActionMemory("control!open.program-<ww:property value='id'/>", treeNode.id);
			//openView("查看伺服器【"+ip+":"+port+"】程序内存使用情况: "+trProgramTitle+"["+trProgramId+"]", "monitorload!modulememory.action?id="+trProgramId+"&ip="+ip+"&port="+port);
			document.getElementById( 'id' ).value = treeNode.id;
			resizeWindow();
			viewVersions();
		}
		else if( treeNode.path )
		{
			//alert(treeNode.path);
			setUserActionMemory("control!open.program-<ww:property value='id'/>", treeNode.path);
			openView("伺服器【"+ip+"】文件管理器", "files!open.action?id=<ww:property value='id'/>");
		}
		else
		{
			myZtree.cancelSelectedNode(treeNode);
		}		
	}
	catch(e)
	{
		skit_alert("打开集群程序视图出现异常"+e.message+", 行数"+e.lineNumber);
	}
}

/**
 * 中文转unicode编码
 */
function chr2Unicode(str)
{
	if ('' != str) 
	{
		var st, t, i;
		st = '';
		for (i = 1; i <= str.length; i++)
		{
			t = str.charCodeAt(i - 1).toString(16);
			if (t.length < 4)
			while(t.length <4)
				t = '0'.concat(t);
			t = t.slice(2, 4).concat(t.slice(0, 2))
			st = st.concat(t);
		}
		return(st.toUpperCase());
	}
	else
	{
		return('');
	}
}

function setModuleState(state, i, p)
{
	if( state >= 0 )
	switch( state )
	{
	case 0:
		p.innerHTML = "未运行";
		i.className = "skit_fa_icon_gray fa fa-ban";
		break;
	case 1:
		p.innerHTML = "运行中";
		i.className = "skit_fa_icon_blue fa fa-play-circle-o";
		break;
	case 2:
		p.innerHTML = "关闭中";
		i.className = "skit_fa_icon_red fa fa-ban";
		break;
	case 3:
		p.innerHTML = "已关闭";
		i.className = "skit_fa_icon_gray fa fa-minus-circle";
		break;
	case 5:
		p.innerHTML = "暂停中";
		i.className = "skit_fa_icon_red fa fa-minus-circle";
		break;
	case 6:
		p.innerHTML = "已暂停";
		i.className = "skit_fa_icon_war fa fa-minus-circle";
		break;
	case 7:
		p.innerHTML = "已关闭";
		i.className = "skit_fa_icon_red fa fa-ban";
		break; 
	default:
		p.innerHTML = "未知状态";
		i.className = "skit_fa_icon_red fa fa-question-circle";
		break; 
	}
}

function getHealthyHtml(healthy)
{
	if( !healthy )
	{
		healthy = 0;
	}
	var html = "";
	for(var i = 0; i < 5; i++ )
	{
		var ico = "skit_fa_icon_gray fa fa-star-o";
		if( healthy > i ) ico = "skit_fa_icon_yellow fa fa-star";
		html += "<a href='#'><span class='"+ico+"'></span></a>";
	}
	return html;
}

function openPath()
{
	var nodes = myZtree.getSelectedNodes();
	if( !nodes || nodes.length == 0)
	{
		skit_alert("请先在目录导航树上选择您要操作的程序");
		return;
	}
	var program = nodes[0];
	if( program.ip )
	{
		setUserActionMemory("control!open.program-<ww:property value='id'/>", "");
	}
	else
	{
		setUserActionMemory("control!open.program-<ww:property value='id'/>", "log/"+program.id);
	}
	openView("伺服器【"+ip+"】程序日志查看", "files!open.action?id=<ww:property value='id'/>");
}

function getStateIcon(state)
{
	if( state >= 0 )
		switch(state)
		{
		case 0:
			return "images/icons/gray.gif";
		case 1:
			return "images/icons/green.gif";
		case 2:
			return "images/icons/yellow.gif";
		case 3:
			return "images/icons/red.gif";
		}
	return "images/icons/gray.gif"
}

function viewRunStateInfo(key)
{
	var node = myZtree.getNodeByParam("ip", ip);
	if( node )
	{
		skit_alert(node[key]);
		//document.getElementById("runState").src = getStateIcon(node.runState);
	}
}

function downloadxml()
{
	skit_confirm("您确定要下载伺服器【"+ip+"】的主控程序配置吗？", function(yes){
		if( yes )
		{
			document.forms[0].action = "control!downloadxml.action";
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
	skit_confirm("您确定要下载伺服器【"+ip+"】程序【"+program.id+"】的配置吗？", function(yes){
		if( yes )
		{
			document.getElementById( 'id' ).value = program.id;
			document.forms[0].action = "control!downloadcfg.action";
			document.forms[0].method = "POST";
			document.forms[0].target = "iDownload";
			document.forms[0].submit();
		}
	});
}

function uploadxml()
{
	preupload('xml', "control!uploadxml.action", "程序配置脚本");
}

function uploadcfg()
{
	preupload('json', "control!uploadcfg.action", "主控配置文件");
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
                obj.id = "<ww:property value='id'/>";
                obj.ip = "<ww:property value='ip'/>";
                obj.port = <ww:property value='port'/>;
                obj.serverkey = "<ww:property value='serverkey'/>";
                return obj;
            }
	});
	divUploadfile.style.top = 64;//windowHeight/2 - divUploadfile.clientHeight*2/3;
	divUploadfile.style.left = windowWidth/2 - divUploadfile.clientWidth/2;
	$("#uploadfile").on("fileloaded", function (data, previewId, index) {
		var tips = "您确定要上传"+title+"["+previewId.name+"]到伺服器提交审核替换配置程序吗？";
		if( type == "json" )
		{
			tips = "您确定要上传主控程序配置文件["+previewId.name+"]到伺服器【"+ip+"】去替换对应的主控程序配置吗？";
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
			try
			{
				var parentNode = myZtree.getNodeByParam("type", "config" );
				var result = jQuery.parseJSON(data.response.result);
				for(var i = 0; i < result.length; i++)
				{
					var program = result[i];
					var node = myZtree.getNodeByParam("id", program.id+"*" );
					if( node )
					{
						node.name = program.name;
						node.children = program.children;
						myZtree.updateNode(node);
					}
					else
					{
						program["editing"] = true;
						if( myZtree.getNodeByParam("id", program.id) )
						{
							program["oper"] = 1;
						}
						program["id"] = program.id+"*";
						program["icon"] = "images/icons/tile.png";
						program = myZtree.addNodes(parentNode, -1, program)[0];
						//addStateIco(program);
					}
				}
				myZtree.selectNode(parentNode);
				myZtree.expandNode(parentNode, true);
				open(parentNode);
			}
			catch(e)
			{
				alert("通过上传主控配置文件添加程序配置到集群伺服器【<ww:property value='ip'/>】出现异常"+e.message+", 行数"+e.lineNumber);
			}
		}
		document.getElementById( 'divUploadfile' ).style.display = "none";
		$("#uploadfile").fileinput("destroy");
	});
}

function closeuploadfile()
{
	document.getElementById( 'divUploadfile' ).style.display = "none";
	$("#uploadfile").fileinput("destroy");
}
</SCRIPT>
</html>