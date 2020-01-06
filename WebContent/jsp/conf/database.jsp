<%@ page language="java" pageEncoding="UTF-8"%>
<%@ page import="com.focus.cos.web.common.Kit"%>
<%@ taglib uri="/webwork" prefix="ww" %>
<%Kit.noCache(request,response);  %>
<html>
<head>
<link type="text/css" href="skit/ztree/css/zTreeStyle/zTreeStyle.css" rel="stylesheet"/>
<link type="text/css" href="skit/css/bootstrap-tour.min.css" rel="stylesheet">
<link href="skin/defone/css/awesome-bootstrap-checkbox.css" rel="stylesheet">
<style type='text/css'>
</style>
<%=Kit.getDwrJsTag(request,"interface/SysCfgMgr.js")%>
<%=Kit.getDwrJsTag(request,"engine.js")%>
<%=Kit.getDwrJsTag(request,"util.js")%>
<SCRIPT type="text/javascript">
function createCluster(type){
	var cfg = dbcfg[type.toLowerCase()];
	if( !cfg ){
		skit_alert("数据库服务["+type+"]不存在，不能执行创建集群操作");
		return;
	}
	var info = "伺服器【"+cfg.ip+":"+cfg.controlport+"】数据库["+type+":"+cfg.port+"]";
	if( cfg.length == 0 ){
		skit_alert(info+"数据文件是空，不能执行创建集群操作。");
		return;
	}
	if( !cfg.cosworking ){
		skit_alert(info+"的主控引擎没有运行，不能执行创建集群操作");
		return;
	}
	if( !cfg.standby ){
		skit_alert(inof+"没有配置备份数据库服务，不能执行创建集群操作");
		return;
	}
	var cluster = cfg.ip+":"+cfg.port+","+cfg.standby;
	var tips = "您要以"+info+"的数据("+cfg.size+")创建数据库主备集群("+cluster+")吗？执行该指令需确保对端服务器数据库服务正常启动，且数据清零。";
	skit_confirm(tips, function(yes){
		if( yes ) {
			skit_showLoading(180000);
			SysCfgMgr.executeDatabaseOper(cfg.ip, cfg.controlport, 2, cfg.standby, {
				callback:function(rsp) {
					skit_alert(rsp.message, "创建主备集群", function(){
						window.top.reloadView();
					});
					skit_hiddenLoading();
					try
					{
						if( rsp.succeed ) {
							var json = rsp.result;
							dbcfg[type.toLowerCase()] = JSON.parse(json);
							//window.top.showJson(dbcfg[type]);
							refresh();
						}
					}
					catch(e)
					{
						skit_alert("配置模板的API接口参数操作出现异常"+e);
					}
				},
				timeout:180000,
				errorHandler:function(message) {skit_hiddenLoading(); skit_alert(message); }
			});
		}
	});
}

function backupDataFiles(type, mode){
	try{
		var iSwitch = document.getElementById("iSwitch"+type);
		var cfg = dbcfg[type.toLowerCase()];
		var info = "伺服器【"+cfg.ip+":"+cfg.controlport+"】数据库["+type+":"+cfg.port+"]";
		if( !cfg.cosworking ){
			skit_alert(info+"的主控引擎，不能执行备份操作");
			return;
		}
		var oper = "拷贝备份";
		if( mode == 4 ){
			if( iSwitch.checked ){
				skit_alert("在数据库服务器运行状态下不能做移除备份");
				return;
			}
			oper = "移除备份";
		}
		if( mode == 3 ){
			if( iSwitch.checked ){
				skit_alert("在数据库服务器运行状态下不能做系统备份");
				return;
			}
			oper = "系统备份";
		}
		var tips = "您要"+oper+info+"的数据文件吗？";
		skit_confirm(tips, function(yes){
			if( yes ) {
				skit_showLoading(180000);
				SysCfgMgr.executeDatabaseOper(cfg.ip, cfg.controlport, mode, "", {
					callback:function(rsp) {
						skit_alert(rsp.message);
						skit_hiddenLoading();
						if( rsp.succeed ) {
							var json = rsp.result;
							dbcfg[type.toLowerCase()] = JSON.parse(json);
							//window.top.showJson(dbcfg[type]);
							refresh();
						}
					},
					timeout:180000,
					errorHandler:function(message) {skit_hiddenLoading(); skit_alert(message); }
				});
			}
		});
	}
	catch(e){
		alert("调用函数[backupDataFiles]出现异常"+e);
	}
}

//激活
function activeStandby(type){
	try{
		var cfg = dbcfg[type.toLowerCase()];
		var info = "伺服器【"+cfg.ip+":"+cfg.controlport+"】数据库["+type+":"+cfg.port+", 状态:"+cfg.status+"]";
		if( !cfg.cosworking ){
			skit_alert(info+"的主控引擎没有运行，不能执行激活操作");
			return;
		}
		if( !cfg.running ){
			skit_alert(info+"没有启动，不能执行激活操作");
			return;
		}
		if( !cfg.length ){
			skit_alert(info+"的数据没有完成构建，不能执行激活操作");
			return;
		}
		var tips = "激活"+info+"吗？数据库将按照配置的JDBC地址执行有效性检查，这将可能导致数据库服务不能正常工作";
		skit_confirm(tips, function(yes){
			if( yes ) {
				skit_showLoading(180000);
				SysCfgMgr.executeDatabaseOper(cfg.ip, cfg.controlport, 6, "", {
					callback:function(rsp) {
						skit_alert(rsp.message, "结果提示", function(){
							window.top.reloadView();
						});
						skit_hiddenLoading();
						if( rsp.succeed ) {
							var json = rsp.result;
							dbcfg[type.toLowerCase()] = JSON.parse(json);
							//window.top.showJson(dbcfg[type]);
							refresh();
						}
					},
					timeout:180000,
					errorHandler:function(message) {skit_hiddenLoading(); skit_alert(message); }
				});
			}
		});
	}
	catch(e){
		alert("调用函数[backupDataFiles]出现异常"+e);
	}
}
//改变数据库状态
function changeDatabaseStatus(type){
	try{
		var status = 0;
		var cfg = dbcfg[type.toLowerCase()];
		if( !cfg.cosworking ){
			skit_alert("数据库服务器所在的主控引擎没有运行");
			autoSwitch = true;
			var r = status==0?true:false;
			$('#divSwitch'+type).bootstrapSwitch('setState', r);
			autoSwitch = false;
			return;
		}
		var iSwitch = document.getElementById("iSwitch"+type);
		var tips = "您确定要停止伺服器【"+cfg.ip+":"+cfg.controlport+"】主数据库["+type+"]运行吗？";
		if( iSwitch.checked ){
			tips = "您确定要启动伺服器【"+cfg.ip+":"+cfg.controlport+"】主数据库["+type+"]运行吗？";
			status = 1;
		}
		skit_confirm(tips, function(yes){
			if( yes ) {
				skit_showLoading(180000);
				SysCfgMgr.executeDatabaseOper(cfg.ip, cfg.controlport, status, "", {
					callback:function(rsp) {
						skit_alert(rsp.message, "操作提示", function(){
							window.top.reloadView();
						});
						skit_hiddenLoading();
						if( rsp.succeed ) {
							var json = rsp.result;
							dbcfg[type.toLowerCase()] = JSON.parse(json);
							refresh();
						}
					},
					timeout:180000,
					errorHandler:function(message) {skit_hiddenLoading(); skit_alert(message); }
				});
			}
			else{
				autoSwitch = true;
				var r = status==0?true:false;
				$('#divSwitch'+type).bootstrapSwitch('setState', r);
				autoSwitch = false;
			}
		});
	}
	catch(e){
		alert("调用函数[changeDatabaseStatus]出现异常"+e);
	}
}
//显示数据恢复操作进度
var _showRestoreDatabaseProgress;
function showRestoreDatabaseProgress(){
	SysCfgMgr.getRestoreDatabaseProgress({
		callback:function(message) {
			if( _showRestoreDatabaseProgress ){
				skit_message(message, "数据恢复操作执行跟踪", 360, 900, function(){
					_showRestoreDatabaseProgress = false;
				});
				window.setTimeout("showRestoreDatabaseProgress()", 3000);
			}
		},
		timeout:300000,
		errorHandler:function(message) {skit_hiddenLoading(); skit_alert(message); }
	});
}
//恢复数据库
function restoreDatabase(){
	try{
		var master = dbcfg["master"];
		var standby = dbcfg["standby"];
		if( !standby ){
			skit_alert("备数据库未启动，不能执行恢复操作。");
			return;
		}
		var info1 = "备份服务器【"+standby.ip+":"+standby.controlport+"】数据库["+standby.port+"]";
		var info2 = "主服务器【"+master.ip+":"+master.controlport+"】数据库["+master.port+"]";
		if( standby.running ){
			skit_alert(info1+"运行中，不能执行恢复操作。");
			return;
		}
		if( standby.length == 0 ){
			skit_alert(info1+"数据文件是空，不能执行恢复操作。");
			return;
		}
		skit_confirm("您确定要将"+info1+"的数据文件("+standby.size+")恢复到"+info2+"？执行数据库恢复首先会停止备份服务器的数据库服务"+
				"，如果服务停止后数据文件消失，那说明备份数据库的数据文件持久化不成功，"+
				"你需要重新建立起正常的主备数据库集群，产生新的备份数据文件，"+
				"直到数据库文件在备份数据库服务停止后也存在为止，才能正确执行数据恢复。", function(yes){
			if( yes ) {
				skit_showLoading(180000);
				skit_message("等待操作执行...", "数据恢复操作执行跟踪", 360, 900, function(){
					_showRestoreDatabaseProgress = false;
				});
				_showRestoreDatabaseProgress = true;
				SysCfgMgr.restoreDatabase(
						master.ip, master.controlport,master.rootdir, 
						standby.ip, standby.controlport, standby.rootdir, standby.standby, {
					callback:function(rsp) {
						_showRestoreDatabaseProgress = false;
						skit_message(rsp.message, "数据恢复操作执行跟踪", 360, 900, function(){
							window.top.reloadView();
						});
						skit_hiddenLoading();
						var json = rsp.result;
						var obj = JSON.parse(json);
						if( obj.master ){
							dbcfg.master = obj.master;
						}
						if( obj.standby ){
							dbcfg.standby = obj.standby;
						}
						refresh();
					},
					timeout:300000,
					errorHandler:function(message) {skit_hiddenLoading(); skit_alert(message); }
				});
				window.setTimeout("showRestoreDatabaseProgress()", 3000);
			}
			else{
				autoSwitch = true;
				var r = status==0?true:false;
				$('#divSwitch'+type).bootstrapSwitch('setState', r);
				autoSwitch = false;
			}
		});
	}
	catch(e){
		alert("调用函数[restoreDatabase]出现异常"+e);
	}
}
//刷新数据库状态
function refreshDatabaseStatus(type){
	try{
		var cfg = dbcfg[type];
		if( cfg ){
			SysCfgMgr.executeDatabaseOper(cfg.ip, cfg.controlport, 10, "", {
				callback:function(rsp) {
					skit_alert(rsp.message);
					skit_hiddenLoading();
					if( rsp.succeed ) {
						var json = rsp.result;
						dbcfg[type] = JSON.parse(json);
						refresh();
					}
				},
				timeout:180000,
				errorHandler:function(message) {skit_hiddenLoading(); skit_alert(message); }
			});
		}
	}
	catch(e){
		alert("调用函数[refreshDatabaseStatus]出现异常"+e);
	}
}
var dbcfg;
function refresh(){
	//window.top.showJson(dbcfg);
	try{
		if( dbcfg.normal){
			dbcfg.master = dbcfg.normal;
			delete dbcfg.normal;
		}
		var status = "<i class='skit_fa_icon_green fa fa-check-circle fa-fw'></i>数据库服务工作正常";
		if( dbcfg.master ){
			if( dbcfg.master.working && dbcfg.master.running ){
				document.getElementById("master").style.background = "";
				document.getElementById("master").innerHTML = "<i class='fa fa-hourglass-start fa-spin'></i> 主数据库("+dbcfg.master.port+")</span>";
			}
			else{
				status = "<i class='skit_fa_icon_red fa fa-warning fa-fw'></i>数据库服务未启动";
				if( dbcfg.master.running ){
					status = "<i class='skit_fa_icon_orange fa fa-warning fa-fw'></i>"+dbcfg.master.status;
					if( "待信号激活" == dbcfg.master.status ){
						status += " [<a onclick='activeStandby(\"Master\");' style='cursor:pointer'>激活</a>]";
					}
				}
				document.getElementById("master").style.background = "lightgray";
				document.getElementById("master").innerHTML = "<i class='fa fa-hourglass-start fa-fw'></i> 主数据库("+dbcfg.master.port+")</span>";
			}
			document.getElementById("master").title = dbcfg.master.status?dbcfg.master.status:"";
			var cosaddr = dbcfg.master.cosaddr?dbcfg.master.cosaddr:"N/A";
			if( cosaddr != "N/A" ){
				if( dbcfg.master.cosworking ){
					cosaddr += " <i class='fa fa-gear fa-spin'></i>";
				}
				else{
					cosaddr += " <i class='skit_fa_icon_red fa fa-minus-circle fa-fw'></i>";
				}
			}
			document.getElementById("master-cos-addr").innerHTML = cosaddr;
			var dbworking = dbcfg.master.type?dbcfg.master.type:"N/A";
			if( dbworking != "N/A" ){
				if( dbcfg.master.running ){
					dbworking += " <i class='fa fa-gear fa-spin' title='数据库服务已启动'></i>";
				}
				else{
					dbworking += " <i class='skit_fa_icon_red fa fa-minus-circle fa-fw' title='数据库服务未启动'></i>";
				}
			}
			document.getElementById("master-cos-type").innerHTML = dbworking;
			document.getElementById("master-cos-addr").title = dbcfg.master.name?dbcfg.master.name:"N/A";
			document.getElementById("master-cos-starttime").innerHTML = dbcfg.master.starttime?dbcfg.master.starttime:"N/A";
			document.getElementById("master-cos-size").innerHTML = dbcfg.master.size?(dbcfg.master.size+
				"<a onclick='backupDataFiles(\"Master\", 5)' title='拷贝备份数据文件' style='cursor:pointer;color:#66cc99'><i class='fa fa-copy fa-fw'></i></a>"+
				"<a onclick='backupDataFiles(\"Master\", 4)' title='移除备份数据文件' style='cursor:pointer;color:#ff0099;'><i class='fa fa-close fa-fw'></i></a>"+
				"<a onclick='backupDataFiles(\"Master\", 3)' title='系统备份数据文件' style='cursor:pointer;color:#0099ff'><i class='fa fa-clone fa-fw'></i></a>"):"N/A";
			autoSwitch = true;
			$('#divSwitchMaster').bootstrapSwitch('setState', dbcfg.master.running);
			autoSwitch = false;

			if( dbcfg.master.cosworking ){
				var uid = "files!open.path-"+dbcfg.master.cid;
				setUserActionMemory(uid, "h2");
				//setUserActionMemory("files!navigate.server", dbcfg.master.rootdir+"h2/");
				//document.getElementById( 'path' ).value = dbcfg.master.rootdir+"h2/";
				document.getElementById( 'id' ).value = dbcfg.master.cid;
				document.forms[0].action = "files!open.action";
				document.forms[0].method = "POST";
				document.forms[0].target = "iMasterFiles";
				document.forms[0].submit();
			}
			else{
				document.getElementById( 'btnRestore' ).disabled = true;
				status = "<i class='skit_fa_icon_red fa fa-warning fa-fw'></i>主控引擎没有启动";
				$('#iSwitchMaster').bootstrapSwitch('setActive', false);
			}
		}
		else{
			document.getElementById( 'btnRestore' ).disabled = true;
			status = "<i class='skit_fa_icon_gray fa fa-warning fa-fw'></i>未读取到数据库配置";
			$('#iSwitchMaster').bootstrapSwitch('setActive', false);
		}
		document.getElementById("master-status").innerHTML = status;
		status = "<i class='skit_fa_icon_green fa fa-check-circle fa-fw'></i>数据库服务工作正常";
		if( dbcfg.standby ){
			if( dbcfg.standby.working && dbcfg.standby.running ){
				document.getElementById("standby").style.background = "";
				document.getElementById("standby").innerHTML = "<i class='fa fa-hourglass-end fa-spin'></i> 备数据库("+dbcfg.standby.port+")</span>";
			}
			else{
				status = "<i class='skit_fa_icon_red fa fa-warning fa-fw'></i>数据库服务未启动";
				if( dbcfg.standby.running ){
					status = "<i class='skit_fa_icon_red fa fa-warning fa-fw'></i>"+dbcfg.standby.status;
					if( "待信号激活" == dbcfg.standby.status ){
						status += " [<a onclick='activeStandby(\"Standby\");' style='cursor:pointer'>激活</a>]";
					}
				}
				document.getElementById("standby").style.background = "lightgray";
				document.getElementById("standby").innerHTML = "<i class='fa fa-hourglass-start fa-fw'></i> 备数据库("+dbcfg.standby.port+")</span>";
			}
			document.getElementById("standby").title = dbcfg.standby.status?dbcfg.standby.status:"";
			var cosaddr = dbcfg.standby.cosaddr?dbcfg.standby.cosaddr:"N/A";
			if( cosaddr != "N/A" ){
				if( dbcfg.standby.cosworking ){
					cosaddr += "<i class='fa fa-gear fa-spin'></i>";
				}
				else{
					cosaddr += "<i class='skit_fa_icon_red fa fa-minus-circle fa-fw'></i>";
				}
			}
			document.getElementById("standby-cos-addr").innerHTML = cosaddr;

			var dbworking = dbcfg.standby.type?dbcfg.standby.type:"N/A";
			if( dbworking != "N/A" ){
				if( dbcfg.standby.running ){
					dbworking += " <i class='fa fa-gear fa-spin' title='数据库服务已启动'></i>";
				}
				else{
					dbworking += " <i class='skit_fa_icon_red fa fa-minus-circle fa-fw' title='数据库服务未启动'></i>";
				}
			}
			document.getElementById("standby-cos-type").innerHTML = dbworking;
			document.getElementById("standby-cos-addr").title = dbcfg.standby.name?dbcfg.standby.name:"N/A";
			document.getElementById("standby-cos-starttime").innerHTML = dbcfg.standby.starttime?dbcfg.standby.starttime:"N/A";
			document.getElementById("standby-cos-size").innerHTML = dbcfg.standby.size?(dbcfg.standby.size+
				"<a onclick='backupDataFiles(\"Standby\", 5)' title='拷贝备份数据文件' style='cursor:pointer;color:#66cc99'><i class='fa fa-copy fa-fw'></i></a>"+
				"<a onclick='backupDataFiles(\"Standby\", 4)' title='移除备份数据文件' style='cursor:pointer;color:#ff0099;'><i class='fa fa-close fa-fw'></i></a>"+
				"<a onclick='backupDataFiles(\"Standby\", 3)' title='系统备份数据文件' style='cursor:pointer;color:#0099ff'><i class='fa fa-clone fa-fw'></i></a>"):"N/A";
			autoSwitch = true;
			$('#divSwitchStandby').bootstrapSwitch('setState', dbcfg.standby.running);
			autoSwitch = false;
			if( dbcfg.standby.cosworking ){
				var uid = "files!open.path-"+dbcfg.standby.cid;
				setUserActionMemory(uid, "h2");
				document.getElementById( 'id' ).value = dbcfg.standby.cid;
				document.forms[0].action = "files!open.action";
				document.forms[0].method = "POST";
				document.forms[0].target = "iStandbyFiles";
				document.forms[0].submit();
			}
			else{
				$('#divSwitchStandby').bootstrapSwitch('toggleActivation');
				$('#divSwitchStandby').bootstrapSwitch('setActive', false);
				status = "<i class='skit_fa_icon_red fa fa-warning fa-fw'></i>主控引擎没有启动";
				document.getElementById( 'btnRestore' ).disabled = true;
			}
		}
		else{
			document.getElementById( 'btnRestore' ).disabled = true;
			$('#divSwitchStandby').bootstrapSwitch('toggleActivation');
			status = "<i class='skit_fa_icon_gray fa fa-warning fa-fw'></i>未读取到数据库配置";
		}
		document.getElementById("standby-status").innerHTML = status;
	}
	catch(e){
		alert(e);
	}
}
</SCRIPT>
</head>
<body style='overflow-y:hidden;padding-top:0px;padding-left:0px;'>
<table style='width:100%' border='0'>
<tr style=''>
	<td width='250' style='padding-top:5px;padding-left:5px;padding-right:5px;' valign='top'>
	<div id='divDashboard'>
		<div class="well profile" style='margin-bottom:10px;'>
		    <div>
		        <p style='cursor:pointer;' onclick='refreshDatabaseStatus("master")'><span class="tags" id='master' style='background:lightgray;'>
		        	<i class="fa fa-hourglass-start fa-spin"></i> 主数据库(N/A)</span><span title='点击刷新数据库状态'
		        	class='tags' style='background:lightgray'><i class="fa fa-refresh fa-spin"></i></span></p>
		        <p id='master-status'></p>
		        <p><strong>主控引擎: </strong> <span id='master-cos-addr'></span></p>
		        <p><strong>工作状态: </strong> <span id='master-cos-type'></span></p>
		        <p><strong>启动时间: </strong> <span id='master-cos-starttime'></span></p>
		        <p><strong>数据文件: </strong> <span id='master-cos-size'></span></p>
		    </div>            
		    <div class="col-xs-12 text-center">
		    	<div class=row>
		    		<div class="switch"
		            	id="divSwitchMaster"
		            	style='height:28px;font-size:12px;' 
		            	data-on="info"
		            	data-off="danger"
		            	data-on-label="<i class='fa fa-check-square-o'></i>启动"
		            	data-off-label="<i class='fa fa-minus-circle'></i>停止"
		            	title='启动停止主数据库'
		            	><input type="checkbox" id='iSwitchMaster'/>
		            </div>
		    	</div>
		    </div>
		</div>
		
		<div class="well profile" style='margin-bottom:10px;'>
		    <div>
		        <p style='cursor:pointer;' onclick='refreshDatabaseStatus("standby")'><span class="tags" id='standby' style='background:lightgray;'>
		        	<i class="fa fa-hourglass-end fa-fw"></i> 备份数据库(N/A)</span><span title='点击刷新数据库状态'
		        	class='tags' style='background:lightgray'><i class="fa fa-refresh fa-spin"></i></span></p>
		        <p id='standby-status'></p>
		        <p><strong>主控引擎: </strong> <span id='standby-cos-addr'></span></p>
		        <p><strong>工作状态: </strong> <span id='standby-cos-type'></span></p>
		        <p><strong>启动时间: </strong> <span id='standby-cos-starttime'></span></p>
		        <p><strong>数据文件: </strong> <span id='standby-cos-size'></span></p>
		    </div>
		    <div class="col-xs-12 text-center">
		    	<div class=row>
		    		<div class="switch"
		            	id="divSwitchStandby"
		            	style='height:28px;font-size:12px;' 
		            	data-on="info"
		            	data-off="danger"
		            	data-on-label="<i class='fa fa-check-square-o'></i>启动"
		            	data-off-label="<i class='fa fa-minus-circle'></i>停止"
		            	title='启动停止主数据库'
		            	><input type="checkbox" id='iSwitchStandby'/>
		    		</div>
		    	</div>
		    </div>
		</div>
		<div class="well profile" style='margin-bottom:10px;'>
		    <div class="col-xs-12 text-center">
		    	<div class=row style='margin-top:5px;margin-bottom:5px;'>
                	<div class="btn-group dropup btn-block">
                         <button type="button" class="btn btn-outline btn-primary" id='btnEdit' style='width:160px;height:34px;font-size:12px;'
							>&nbsp;&nbsp;&nbsp;<span class="fa fa-power-off"></span> 启动数据库集群 </button>
                         <button type="button" class="btn btn-primary dropdown-toggle" style='height:34px;'
                         	title='编辑元数据查询'
                         	data-toggle="dropdown" aria-expanded="false">
                           <span class="fa fa-caret-down"></span>
                         </button>
                         <ul class="dropdown-menu pull-right animated fadeInUp" role="menu" style='cursor:pointer'>
                         	<li><a onclick='createCluster("Master")' style='font-size:12px;'><i class='skit_fa_icon fa fa-hourglass-start fa-fw'></i>
                         		从主服务器创建数据库集群</a></li>
                         	<li><a onclick='createCluster("Standby")' style='font-size:12px;'><i class='skit_fa_icon fa fa-hourglass-end fa-fw'></i>
                         		从备份服务器创建数据库集群</a></li>
                         </ul>
              		</div>
		    	</div>
		    	<div class="row" style='display:none'>
			        <div style='margin-top:10px;'>
		          	  <button onclick='restoreDatabase()' class="btn btn-danger btn-block" style='font-size:12px;width:186px;'
		          	  	id='btnRestore'><span class="fa fa-window-restore"></span> 从备份服务器恢复数据 </button>
			        </div>
		    	</div>
		    </div>
		</div>
   		<div class="panel panel-default">
			<div class="panel-heading" style='background-color:#f5f5f5;font-size:12px;padding:5px 15px'><i class='fa fa-flask'></i> 配置主数据库数据文件自动备份</div>
			<div class="panel-body" style='padding-top:5px;padding-bottom:5px;padding-left:10px;padding-right:10px;'>
	    		<div class="switch"
	            	id="divSwitchBackup"
	            	style='height:28px;font-size:12px;margin-left:48px;' 
	            	data-on="info"
	            	data-off="danger"
	            	data-on-label="<i class='fa fa-check-square-o'></i>开启"
	            	data-off-label="<i class='fa fa-minus-circle'></i>关闭"
	            	title='启动数据库自动备份的开启或关闭'
	            	><input type="checkbox" id='iSwitchBackup'/>
	    		</div>
			</div>
		</div>
	</div>
	</td>
	<td style='padding-top:5px;padding-right:2px;' valign='top'>
		<div id="tabL" style="position:relative;">
			<ul>
				<li><a href="#tabL-master-files"><i class='fa fa-hourglass-start'></i> 主数据库文件</a></li>
				<li><a href="#tabL-standby-files"><i class='fa fa-hourglass-end'></i> 备份数据库文件</a></li>
				<li><a href="#tabL-local-sqlquery"><i class='fa fa-database'></i> 本地数据库查询分析器</a></li>
				<li><a href="#tabL-cluster-sqlquery"><i class='fa fa-window-restore'></i> 主备数据库查询分析器</a></li>
				<li><a href="#tabL-h2-errorcode"><i class='fa fa-window-restore'></i> H2数据库错误码</a></li>
			</ul>
			<iframe name='iMasterFiles' id='tabL-master-files' class='nonicescroll' style='border:0px solid #eee;margin-left:3px;margin-top:3px;margin-bottom:3px;'></iframe>
			<iframe name='iStandbyFiles' id='tabL-standby-files' class='nonicescroll' style='border:0px solid #eee;margin-left:3px;margin-top:3px;margin-bottom:3px;'></iframe>
			<iframe src='' id='tabL-local-sqlquery' class='nonicescroll' style='border:0px solid #eee;margin-left:3px;margin-top:3px;margin-bottom:3px;'></iframe>
			<iframe name='iClusterSqlquery' id='tabL-cluster-sqlquery' class='nonicescroll' style='border:0px solid #eee;margin-left:3px;margin-top:3px;margin-bottom:3px;'></iframe>
			<iframe src='http://www.h2database.com/javadoc/org/h2/api/ErrorCode.html?highlight=unexpected%2Cstatus%2C16777216&search=unexpected%20status%2016777216' 
				id='tabL-h2-errorcode' class='nonicescroll' style='border:0px solid #eee;margin-left:3px;margin-top:3px;margin-bottom:3px;'></iframe>
		</div>
	</td>
</tr>
</table>
<form id='form0' name='form0'>
<input type='hidden' name='id' id='id'/>
<input type='hidden' name='ip' id='ip'/>
<input type='hidden' name='port' id='port'/>
<input type='hidden' name='path' id='path'/>
<input type='hidden' name='jdbcUrl' id='jdbcUrl'>
<input type='hidden' name='jdbcUsername' id='jdbcUsername'>
<input type='hidden' name='jdbcUserpswd' id='jdbcUserpswd'>
<input type='hidden' name='driverClass' id='driverClass'>
</form>
</body>
<script type="text/javascript">
var grant = <ww:property value='grant'/>;//管理程序的权限
var sysid = "<ww:property value='sysid'/>";
function resizeWindow()
{
	var div;
	div = document.getElementById("divDashboard");
	div.style.height = windowHeight - 10;
	
	div = document.getElementById("tabL-master-files");
	div.style.height = windowHeight - 48;
	div.style.width = windowWidth - 256;
	div = document.getElementById("tabL-standby-files");
	div.style.height = windowHeight - 48;
	div.style.width = windowWidth - 256;
	div = document.getElementById("tabL-local-sqlquery");
	div.style.height = windowHeight - 48;
	div.style.width = windowWidth - 256;
	div = document.getElementById("tabL-cluster-sqlquery");
	div.style.height = windowHeight - 48;
	div.style.width = windowWidth - 256;

	div = document.getElementById("tabL-h2-errorcode");
	div.style.height = windowHeight - 48;
	div.style.width = windowWidth - 256;
}
</script>
<link href="skit/css/bootstrap.switch.css" rel="stylesheet">
<%@ include file="../../skit/inc/skit_bootstrap.inc"%>
<%@ include file="../../skit/inc/skit_cos.inc"%>
<%@ include file="../../skit/inc/skit_ztree.inc"%>
<script src="skit/js/jquery.md5.js"></script>
<script src="skit/js/bootstrap-tour.min.js"></script>
<script src="skit/js/jquery.inputmask.bundle.min.js"></script>
<script src="skit/js/bootstrap.switch.js"></script>
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
    padding: 2px 10px;
}
.input-group-addon {
    padding: 6px 12px;
    font-size: 12px;
    font-weight: 400;
    line-height: 1;
    color: #555;
    text-align: center;
    background-color: #eee;
    border: 1px solid #ccc;
        border-right-width: 1px;
        border-right-style: solid;
        border-right-color: rgb(204, 204, 204);
    border-radius: 4px;
        border-top-right-radius: 4px;
        border-bottom-right-radius: 4px;
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
$( '#divDashboard' ).niceScroll({
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

$('#divSwitchBackup').on('switch-change', function (e, data) {
	if( autoSwitch )
	{
		autoSwitch = false;
		return;
	}
	try{
		var cfg = dbcfg["master"];
		if( !cfg ){
			skit_alert("数据库服务[master]不存在，不能配置自动备份");
			return;
		}
		var iSwitch = document.getElementById("iSwitchBackup");
		var info = "伺服器【"+cfg.ip+":"+cfg.controlport+"】数据库["+cfg.type+":"+cfg.port+"]";
		var tips = "您确定要关闭"+info+"数据文件的自动备份吗？";
		var mode = -8;
		if( iSwitch.checked ){
			tips = "您确定要开启"+info+"数据文件的自动备份吗？自动备份将在每天凌晨0点到1点之间执行，备份的数据文件将保存7天。";
			mode = 8;
		}
		autoSwitch = false;
		skit_confirm(tips, function(yes){
			if( yes ) {
				skit_showLoading(180000);
				SysCfgMgr.executeDatabaseOper(cfg.ip, cfg.controlport, mode, "", {
					callback:function(rsp) {
						skit_hiddenLoading();
						try
						{
							if( rsp.succeed ) {
								var json = rsp.result;
								var s = JSON.parse(json);
								skit_alert(s.result);
							}
							else{
								skit_alert(message);
							}
					
						}
						catch(e)
						{
							skit_alert("配置模板的API接口参数操作出现异常"+e);
						}
					},
					timeout:180000,
					errorHandler:function(message) {skit_hiddenLoading(); skit_alert(message); }
				});
			}
			else{
				autoSwitch = true;
				var r = true;
				if( mode == 8){
					r = false;
				}
				$('#divSwitchBackup').bootstrapSwitch('setState', r);
				autoSwitch = false;	
			}
		});
	}
	catch(e){
		alert(e);
	}
});	

var autoSwitch = false;
$('#divSwitchMaster').on('switch-change', function (e, data) {
	if( autoSwitch )
	{
		autoSwitch = false;
		return;
	}
	autoSwitch = false;
	var a = $('#divSwitchMaster').bootstrapSwitch('isActive');
	if( a ){
		changeDatabaseStatus("Master");
	}
	else{
		var iSwitch = document.getElementById("iSwitchMaster");
		autoSwitch = true;
		$('#divSwitchMaster').bootstrapSwitch('setState', !iSwitch.checked);
		autoSwitch = false;		
	}
});	

$('#divSwitchStandby').on('switch-change', function (e, data) {
	if( autoSwitch )
	{
		autoSwitch = false;
		return;
	}
	autoSwitch = false;
	var a = $('#divSwitchStandby').bootstrapSwitch('isActive');
	if(a){
		changeDatabaseStatus("Standby");
	}
	else{
		var iSwitch = document.getElementById("iSwitchStandby");
		autoSwitch = true;
		$('#divSwitchStandby').bootstrapSwitch('setState', !iSwitch.checked);
		autoSwitch = false;		
	}
});

$("#tabL").tabs({
    select: function(event, ui) {
		if( ui.panel.id == "tabL-local-sqlquery" ){
			document.getElementById("tabL-local-sqlquery").src = "helper!sqlquery.action?db=local";
		}
		else if( ui.panel.id == "tabL-cluster-sqlquery" ){
			try{
				var master = dbcfg["master"];
				var standby = dbcfg["standby"];
				var tips = "", jdbcUrl = "";
				if( master && master.type == "master" && master.working ){
					jdbcUrl = master.ip+":"+master.port+","+master.standby;
					tips = "主数据库["+master.ip+":"+master.port+"]工作模式["+master.type+"]正常";
				}
				else if( master ){
					tips = "主数据库["+master.ip+":"+master.port+"]工作模式["+master.type+"]"+master.status;
				}
				else{
					tips = "主数据库未启动";
				}
				if( standby && standby.type == "standby" && standby.working ){
					if( !jdbcUrl ){
						jdbcUrl = standby.ip+":"+standby.port+","+standby.standby;
					}
					tips += "; 备数据库["+master.ip+":"+master.port+"]正常";
				}
				else if( standby ){
					tips += "; 备数据库["+master.ip+":"+master.port+"]"+standby.status;
				}
				else{
					tips += "; 备数据库未启动";
				}
				
				if( jdbcUrl ){
					jdbcUrl = "jdbc:h2:tcp://"+jdbcUrl+"/../h2/cos";
					skit_alert(tips+"\r\n"+jdbcUrl);
					document.getElementById("jdbcUrl").value = jdbcUrl;
					document.getElementById("jdbcUsername").value = "sa";
					document.getElementById("jdbcUserpswd").value = "";
					document.getElementById("driverClass").value = "org.h2.Driver";
					document.forms[0].action = "helper!sqlquery.action";
					document.forms[0].target = "iClusterSqlquery";
					document.forms[0].submit();
				}
				else{
					skit_alert(tips);
				}
			}
			catch(e){
				alert(e);
			}
		}
		setUserActionMemory("syscfg!database.tab", ui.panel.id);
    }
});

$(document).ready(function(){
	try
	{
		dbcfg = <ww:property value="jsonData" escape="false"/>;
		refresh();
		var id = getUserActionMemory("syscfg!database.tab");
		if(id){
			$("#tabL").tabs('select', '#'+id);
		}
		var master = dbcfg.master;
		if( master ){
			SysCfgMgr.executeDatabaseOper(master.ip, master.controlport, 7, "", {
				callback:function(rsp) {
					if( rsp.succeed ){
						var json = rsp.result;
						var e = JSON.parse(json);
						if( e.result ){
							document.getElementById("divSwitchBackup").title = e.result;
							autoSwitch = true;
							$('#divSwitchBackup').bootstrapSwitch('setState', true);
							autoSwitch = false;
						}
					}
				},
				timeout:180000,
				errorHandler:function(message) {skit_hiddenLoading(); skit_alert(message); }
			});
		}
		else{
			$('#divSwitchBackup').bootstrapSwitch('toggleActivation');
			$('#divSwitchBackup').bootstrapSwitch('setActive', false);
		}
	}
	catch(e)
	{
		skit_alert("初始化数据库配置异常"+e.message+", 行数"+e.lineNumber);
	}
});
</SCRIPT>
</html>