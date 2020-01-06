<%@ page language="java" pageEncoding="UTF-8"%>
<%@ page import="com.focus.cos.web.common.Kit"%>
<%@ taglib uri="/webwork" prefix="ww" %>
<%Kit.noCache(request,response);  %>
<html>
<head>
<link type="text/css" href="skit/ztree/css/zTreeStyle/zTreeStyle.css" rel="stylesheet"/>
<link type="text/css" href="skit/css/bootstrap-tour.min.css" rel="stylesheet">
<link href="skit/css/awesome-bootstrap-checkbox.css" rel="stylesheet">
<style type='text/css'>
ul.ztree {
    margin-top: 10px;
    border: 1px solid #617775;
    background: #f0f6e4;
    width: 220px;
    height: 360px;
    overflow-y: scroll;
    overflow-x: auto;
}

.switchery {
    background-color: #fff;
    border: 1px solid #dfdfdf;
    border-radius: 20px;
    cursor: pointer;
    display: inline-block;
    height: 26px;
    position: relative;
    vertical-align: middle;
    width: 50px;
    -moz-user-select: none;
    -khtml-user-select: none;
    -webkit-user-select: none;
    -ms-user-select: none;
    user-select: none;
    box-sizing: content-box;
    background-clip: content-box;
}

.switchery.switchery-default {
    width: 40px;
    height: 25px;
    -webkit-border-radius: 15px;
    -moz-border-radius: 15px;
    border-radius: 15px;
}

.switchery.switchery-default > small {
    width: 25px;
    height: 25px;
}
.switchery > small {
    background: #fff;
    border-radius: 100%;
    box-shadow: 0 1px 3px rgba(0,0,0,0.4);
    height: 26px;
    position: absolute;
    top: 0;
    width: 30px;
}

.form-control:hover{
	cursor: pointer;
}
.input-checkbox-addon {
    font-size: 14px;
    font-weight: 400;
    line-height: 1;
    color: #555;
    text-align: center;
    background-color: #eee;
    border: 1px solid #ccc;
    border-radius: 4px;
    height:34px;
}
.input-group-addon table {
	width:100%;
	height:34px;
}
.input-group-addon table tr {
	height:34px;
}
.input-group-addon table tr td {
	border:1px solid blue;
	font-size:14px;
	padding-top:-10px;
	padding-bottom:0px;
	margin-top:-10px;
}

.heading {
    font-family: Lato,sans-serif;
    font-weight: 300;
    font-size: 16px;
    text-transform: uppercase;
    margin-bottom: 5px;
    margin-top: 0px;
    color: #333;
    display: inline-block;
}
.heading .sub-heading {
    display: block;
    color: #888;
    font-size: 12px;
    text-transform: initial !important;
}
</style>
<%=Kit.getDwrJsTag(request,"interface/ControlMgr.js")%>
<%=Kit.getDwrJsTag(request,"interface/FilesMgr.js")%>
<%=Kit.getDwrJsTag(request,"engine.js")%>
<%=Kit.getDwrJsTag(request,"util.js")%>
<SCRIPT type="text/javascript">
</SCRIPT>
</head>
<body>
<div class='ui-tabs .ui-tabs-panel' style='padding:10px 10px'>
	
	<table style='width:100%;margin-bottom:5px;border:0px solid red'><tr>
	<td id='tdInfo' valign='top'>
	</td>
	<!-- 
	<td align='right'>
		<div class="switch" style='height:34px;' 
           	id="divSwitchProgram" 
           	data-on="info"
           	data-off="danger"
           	data-on-label="<i class='fa fa-check-square-o'></i>开启"
           	data-off-label="<i class='fa fa-minus-circle'></i>禁用"
           	title='程序运行的总开关，启用或禁用程序'
           	>
			<input type="checkbox" id='switch'/>
		</div>
		<div class="switch" style='height:34px;display:none' 
           	id="divSwitchDebug" 
           	data-on="danger"
           	data-off="success"
           	data-on-label="<i class='fa fa-check-square-o'></i>开调试"
           	data-off-label="<i class='fa fa-minus-circle'></i>不调试"
           	title='输出OS底层信息，方便程序运行初期调试，输出信息在debug_<ww:property value="id"/>.txt'
           	>
			<input type="checkbox" id='debug'/>
		</div>
	</td>
	-->
	</tr></table>
	
	<div class="panel panel-default">
		<div class="panel-heading" style='font-size:12px;padding:5px 15px'><i class='skit_fa_btn fa fa-info'></i> 程序基本信息</div>
		<div class="panel-body" style='padding-bottom:0px;'>
			<div class="form-group">
				<div class="input-group">
					<span class="input-group-addon">程序标识<span class='fa fa-user-secret' style='margin-left:6px'></span></span>
					<input class="form-control" id="programid" type="text"
                	data-title="请输入您程序的软件英文标识。只能含有字母、数字、下划线、'-'减号、'.'句号字符;程序标识不少于2个字，不多于64个字"
					data-toggle="tooltip" 
					data-placement="bottom"
					data-trigger='manual'
					onkeydown='$("#programid").tooltip("hide")'
                	placeholder="请输入您程序的软件英文标识"					
					>
				</div>
			</div>
			<div class="form-group">
				<div class="input-group">
					<span class="input-group-addon">程序名称<span class='fa fa-info-circle' style='margin-left:6px'></span></span>
					<input class="form-control" type="text" id='name'
                	data-title='程序名称只能含有汉字、数字、字母、下划线不能以下划线开头和结尾;程序名称不少于2个字，不多于20个字'
					data-toggle="tooltip" 
					data-placement="bottom"
					data-trigger='manual'
					onkeydown='$("#name").tooltip("hide")'
                	placeholder="请输入您程序的名称"							
					>
				</div>
			</div>
			<div class="form-group">
				<div class="input-group" style='width:240px'>
					<span class="input-group-addon">程序版本<span class='fa fa-paw' style='margin-left:6px'></span></span>
					<input class="form-control" type="text" readonly id='version'
                	data-title='请按照1.0.0.0格式输入版本号'
					data-toggle="tooltip" 
					data-placement="bottom"
					data-trigger='manual'
					onblur='this.readOnly=true;var val=this.value;$("#version").inputmask("remove");this.value=val;'
					onkeydown='$("#version").tooltip("hide");'
					onfocus='this.readOnly=false;$("#version").inputmask("mask", {"mask": "9.9.9.9"});'
                	placeholder="1.0.0.0"
					>
      				<div class="input-group-btn">
      					<button type='button' class='btn btn-info dropdown-toggle' data-toggle='dropdown' aria-expanded='false'
      						<ww:if test='!editable'>readonly</ww:if> style='display: inline-block;height:34px;'><span class='caret'></span></button>
      					<ul class='dropdown-menu pull-right animated fadeInUp' id='ulVersions' style='height:128px;cursor:pointer;'></ul>
      				</div>
				</div>
			</div>
			<div class="form-group">
				<textarea class="form-control" rows="2"	id="description" style='color:#66cccc;background-color:#eee;'
				    data-title='程序的介绍说明描述不少于16个字，不多于500个字'
					data-toggle="tooltip" 
					data-placement="bottom"
					data-trigger='manual'
					onkeydown='$("#description").tooltip("hide")'
                	placeholder="请输入您程序的介绍说明描述"></textarea>
			</div>
			<div class="form-group">
				<div class="input-group" style='width:440px'>
					<span class="input-group-addon">程序管理员<span class='fa fa-user-secret' style='margin-left:6px'></span></span>
					<input class="form-control" type="text"	id='programmer' readonly style='width:128px;height:34px;'
  						data-title='点击右侧下来按钮选择系统的用户作为程序管理员'
						data-toggle="tooltip" 
						data-placement="bottom"
						data-trigger='manual'
						onblur='$("#programmer").tooltip("hide")'>
      				<input type="hidden" id='manager'>
					<span class="input-group-addon" style='border-left: 0px solid #eee;border-right: 0px solid #eee;'
						>邮箱<span class='fa fa-at' style='margin-left:6px'></span></span>
					<input class="form-control" type="text"	style='width:256px;height:34px;' id='email' readonly>
      				<div class="input-group-btn">
      					<button type='button' <ww:if test='!editable'>readonly</ww:if>
      						onclick='showUserSelector("programmer", "manager", 406)'
      						class='btn btn-info dropdown-toggle'
      						data-toggle='dropdown' aria-expanded='false'
      						style='display: inline-block;height:34px;'
      						><span class='caret'></span></button>
      				</div>
				</div>
			</div>
		</div>
	</div>

	<div class="panel panel-default">
		<div class="panel-heading" style='font-size:12px;padding:5px 15px'><i class='skit_fa_btn fa fa-cogs'></i> 程序运行控制
		</div>
		<div class="panel-body" style='padding-bottom:0px;'>
			<div class="form-group">
     			<div class="input-group" style='display:'>
					<span class="input-group-addon" id='spanMode'>主控启动<i class='fa fa-globe' style='margin-left:6px'></i></span>
					<select class="form-control" style='font-size:12px' onchange='changeMode()' id='mode'>
						<option value='0' <ww:if test='serviceConfig.mode==0'>selected</ww:if> >0:主控服务启动后自动启动进程，进程关闭根据重启间隔时延自动重启，主控服务停止关闭进程</option>
						<option value='1' <ww:if test='serviceConfig.mode==1'>selected</ww:if> >1:主控服务启动后不启动进程，用户通过界面启动服务，进程关闭根据重启间隔时延自动重启，主控服务停止关闭进程</option>
						<option value='3' <ww:if test='serviceConfig.mode==3'>selected</ww:if> >3:主控服务启动后先检查PID对应进程是否存在，如果进程不存在就启动该进程，主控不会重复启动该进程，进程关闭根据重启间隔时延自动重启，主控服务停止不会关闭该进程</option>
						<option value='4' <ww:if test='serviceConfig.mode==4'>selected</ww:if> >4:主控服务启动后不启动进程，用户通过前台启动服务，服务启动后先检查PID对应进程是否存在，如果进程不存在就启动该进程，主控不会重复启动该进程，进程关闭根据重启间隔时延自动重启，主控服务停止不会关闭该进程</option>
					  	<option value='2' <ww:if test='serviceConfig.mode==2'>selected</ww:if> >2:主控服务启动后不启动进程，用户通过前台启动服务，进程关闭后不根据重启间隔时延自动重启，再次启动需要用户通过前台操作，主控服务停止关闭进程。可用于单步调试程序</option>
					</select>
				</div>
			</div>
			<div class="form-group" id='div-logfile'>
				<div class="input-group">
					<span class="input-group-addon">日志路径<i class='fa fa-file-text' style='margin-left:6px'></i></span>
					<input class="form-control" type="text" id='logfile' readonly
   						data-title='守护运行模式的程序需要配置日志目录，点击右侧下来按钮选择系统的日志目录'
						data-toggle="tooltip" 
						data-placement="bottom"
						data-trigger='manual'
						onblur='$("#logfile").tooltip("hide")'	
	                	placeholder="非守护运行模式的程序缺省日志目录是log/[程序id]/"			
					>
      				<div class="input-group-btn">
      					<button type='button' <ww:if test='!editable'>readonly</ww:if> 
      						onclick='showFilesSelector("logfile")'
      						class='btn btn-info dropdown-toggle'
      						data-toggle='dropdown' 
      						aria-expanded='false'
      						style='display: inline-block;height:34px;'><span class='caret'></span></button>
      				</div>
				</div>
			</div>
			<div class="form-group" id='div-pidfile'>
				<div class="input-group">
					<span class="input-group-addon">PID文件<span class='fa fa-credit-card' style='margin-left:6px'></span></span>
					<input class="form-control" type="text" id='pidfile' readonly
   						data-title='守护运行模式的程序需要配置程序的PID文件，点击右侧下拉按钮选择PID文件'
						data-toggle="tooltip" 
						data-placement="bottom"
						data-trigger='manual'
						onblur='$("#pidfile").tooltip("hide")'	
	                	placeholder="Linux守护运行模式的程序配置PID文件">
      				<div class="input-group-btn">
      					<button type='button'
      						class='btn btn-info dropdown-toggle' 
      						data-toggle='dropdown' 
      						aria-expanded='false'
      						<ww:if test='!editable'>readonly</ww:if>
      						style='display: inline-block;height:34px;'
      						onclick='showFilesSelector("pidfile")'><span class='caret'></span></button>
      				</div>
				</div>
			</div>
			<div class="form-group" id='div-cfgfile'>
				<div class="input-group">
					<span class="input-group-addon">配置文件<span class='fa fa-cog' style='margin-left:6px'></span></span>
					<input class="form-control" type="text" id='cfgfile' readonly
   						data-title='该程序的配置目录，可通过系统监控程序管理打开配置'
						data-toggle="tooltip" 
						data-placement="bottom"
						data-trigger='manual'
						onblur='$("#cfgfile").tooltip("hide")'	
	                	placeholder="该程序的配置文件目录或文件">
      				<div class="input-group-btn">
      					<button type='button'
      						class='btn btn-info dropdown-toggle' 
      						data-toggle='dropdown' 
      						aria-expanded='false'
      						<ww:if test='!editable'>readonly</ww:if>
      						style='display: inline-block;height:34px;'
      						onclick='showFilesSelector("cfgfile")'><span class='caret'></span></button>
      				</div>
				</div>
			</div>
			<div class="form-group" id='div-control'>
   				<div class="input-group" style='width:222px;float:left;' title='如果服务中断间隔多少时间被主控重启。缺省不等候马上重启'>
					<span class='input-group-addon'>重启间隔<span class='fa fa-group' style='margin-left:6px'></span></span></span>
					<input class="form-control" type="text" id='restartup' readonly
   						data-title='程序运行结束后间隔多少秒自动重启运行程序'
						data-toggle="tooltip" 
						data-placement="bottom"
						data-trigger='manual'
						onblur='$("#restartup").tooltip("hide")'	
	                	placeholder="">
					<span class="input-group-addon" style='border-left: 1px solid #eee;border-right: 1px solid #eee;'>秒</span>
					<span class='input-group-addon' style='border-left: 1px solid #eee;'>
						<div class='checkbox checkbox-info' style='padding-left: 0px'>
							<input type='checkbox' id='iActiveRestartup' style='cursor:pointer;'
								onclick='activeRestartup(this)' title="">
							<label></label>
						</div>
					</span>
				</div>
				<div class="input-group" style='width:222px;padding-left:10px;' title='程序将在主控启动后指定时间后启动'>
					<span class="input-group-addon">延迟启动<span class='fa fa-group' style='margin-left:6px'></span></span>
					<input class="form-control" type="text" id='delayed' readonly
   						data-title='主控引擎启动后延迟多少秒再启动运行该程序'
						data-toggle="tooltip" 
						data-placement="bottom"
						data-trigger='manual'
						onblur='$("#delayed").tooltip("hide")'	
	                	placeholder="">
					<span class="input-group-addon" style='border-left: 1px solid #eee;border-right: 1px solid #eee;'>秒</span>
					<span class='input-group-addon' style='border-left: 1px solid #eee;'>
						<div class='checkbox checkbox-info' style='padding-left: 0px'>
							<input type='checkbox' id='iActiveDelayed' style='cursor:pointer;'
								onclick='activeDelayed(this)' title='激活参数' >
							<label></label>
						</div>
					</span>
				</div>
			</div>
			<div class="form-group">
     			<div class="input-group">
					<span class="input-group-addon">启动依赖<span class='fa fa-credit-card' style='margin-left:6px'></span></span>
					<select class="form-control" id='dependence'>
					<option value=''></option>
					<ww:iterator value="listData" status="loop">
					<option value="<ww:property value='value'/>" <ww:if test='value.equals(localDataObject.getString("dependence"))'>selected</ww:if> ><ww:property value='name'/></option>
					</ww:iterator>
					</select>
				</div>
			</div>
			<div class="form-group" id='div-forcereboot' style='display:none'>
   				<div class="input-group" style='width:202px;'>
  					<div class="input-group-btn" style='display:none'
  						id="divFrMode">
	                    <button type="button" class="btn btn-info dropdown-toggle" data-toggle="dropdown" aria-expanded="false"
	                    	<ww:if test='!editable'>readonly</ww:if> style="display: inline-block;height:34px;"><span class="caret"></span></button>
	                    <ul class="dropdown-menu pull-left animated fadeInUp" style="width:60px;cursor:pointer;">
	                        <li><a onclick="changeForcereboot('');">每x秒</a></li>
	                        <li><a onclick="changeForcereboot('h');">每x小时</a></li>
	                        <li><a onclick="changeForcereboot('d');">每x天</a></li>
	                        <li><a onclick="changeForcereboot('w');">每周星期</a></li>
	                        <li><a onclick="changeForcereboot('m');">每月x日</a></li>
	                    </ul>
                    </div>
					<span class="input-group-addon" style='border-right: 1px solid #eee;'
						id="frBeforeTips"></span>
					<input class="form-control" type="text"
						id='frval' name='serviceConfig.frval' style='width:64px;height:34px;display:none;border-right: 1px solid #eee;'>
					<span class="input-group-addon"
						id='frAfterTips' style='border-right: 1px solid #eee;'></span><input type='hidden'
						id='frmode' name='serviceConfig.frmode'>
					<input class="form-control" type="text"
						placeholder="09:00" onkeyup='validFrHour(this)'
						id='frtime' name='serviceConfig.frtime' 
						style='width:64px;height:34px;display:none;border-right: 1px solid #eee;'
   						data-title='请输入强制重启正确的时钟时间'
						data-toggle="tooltip" 
						data-placement="bottom"
						data-trigger='manual'
						onblur='$("#frtime").tooltip("hide")'>
					<span class="input-group-addon" style='border-right: 1px solid #eee;'>强制重启程序开关<span class='fa fa-toggle-on' style='margin-left:6px'></span></span>
					<span class='input-group-addon' style='border-left: 1px solid #eee;'>
						<div class='checkbox checkbox-info' style='padding-left: 0px'>
							<input type='checkbox' id='iActiveForcereboot' style='cursor:pointer;'
								title='激活参数' onclick='activeForcereboot(this)'>
							<label></label>
						</div>
					</span>
				</div>
			</div>
			<div class="form-group" id='div-timerange'>
				<div class="input-group" style='width:360px' title='该参数可用于某些服务进程每天只在指定时间内运行的程序，例如08:00~09:00。同时配置在时间到期是否强制关闭'>
					<span class="input-group-addon"><i class='fa fa-at' style='margin-right:6px'></i>每天从</span>
					<input class="form-control" type="text"
						placeholder="00:00"
						id='starttime' readonly
						style='width:64px;border-right: 1px solid #eee;'
   						data-title='请输入运行起始正确的时钟时间'
						data-toggle="tooltip" 
						data-placement="bottom"
						data-trigger='manual'
						onblur='$("#starttime").tooltip("hide")'>
					<span class="input-group-addon"> 到 </span>
					<input class="form-control" type="text"
						placeholder="23:59" 
						id='endtime' readonly
						style='width:64px;border-left: 1px solid #eee;;border-right: 1px solid #eee;'
   						data-title='请输入运行结束正确的时钟时间'
						data-toggle="tooltip" 
						data-placement="bottom"
						data-trigger='manual'
						onblur='$("#endtime").tooltip("hide")'>
					<span class="input-group-addon" style='border-right: 1px solid #eee;'>允许运行程序，在此时间范围之外强制关闭程序<span class='fa fa-toggle-on' style='margin-left:6px'></span></span>
					<span class='input-group-addon' style='border-left: 1px solid #eee;'>
						<div class='checkbox checkbox-info' style='cursor:pointer;padding-left: 0px'>
							<input type='checkbox' id="iActiveTimerange" onclick='activeTimerange(this)' title='激活参数'>
							<label></label>
						</div>
					</span>
				</div>
			</div>
		</div>
	</div>
	
	<div class="panel panel-default">
		<div class="panel-heading" style='font-size:12px;padding:5px 15px'><i class='skit_fa_btn fa fa-indent'></i> 启动指令配置</div>
		<div class="panel-body" style='padding-bottom:0px;'>
			<table style='width:100%'><tr>
			<td width="60%">
			<textarea class="form-control" rows="2" 
				id='startupCommands' style='color:#fff;background-color:#000;padding-right:0px;'
				<ww:if test='!editable'>readonly</ww:if>
				onblur="changeCommands(this)"><ww:property value='localDataObject.getString("startupCommands")'/></textarea>
			</td>
			<td width="40%">
			<textarea class="form-control" rows="2" placeholder='请按行输入命令备注'
				id='startupCommandsRemark' style='color:#66cccc;background-color:#eee;'
				<ww:if test='!editable'>readonly</ww:if>
				><ww:property value='localDataObject.getString("startupCommandsRemark")'/></textarea>
			</td>
			</tr></table>			
		</div>
	</div>
	
	<div class="panel panel-default">
		<div class="panel-heading" style='font-size:12px;padding:5px 15px'><i class='skit_fa_btn fa fa-dedent'></i> 停止指令配置</div>
		<div class="panel-body" style='padding-bottom:0px;'>
			<table style='width:100%'><tr>
			<td width="60%">
			<textarea class="form-control" rows="2"
				id='shutdownCommands' style='color:#fff;background-color:#000;'
				<ww:if test='!editable'>readonly</ww:if>
				onblur="changeCommands(this)"><ww:property value='localDataObject.getString("shutdownCommands")'/></textarea>
			</td>
			<td width="40%">
			<textarea class="form-control" rows="2" placeholder='请按行输入命令备注'
				id='shutdownCommandsRemark' style='color:#66cccc;background-color:#eee;'
				<ww:if test='!editable'>readonly</ww:if>
				><ww:property value='localDataObject.getString("shutdownCommandsRemark")'/></textarea>
			</td>
			</tr></table>
		</div>
	</div>
    <ww:if test='editable'>
	<div style='width:280px;' id='divSet'>
		<button type="button" class="btn btn-outline btn-success btn-block"
			style='width:128px;float:left;padding-right:10px;' onclick='setProgramConfig();'><i class="fa fa-save"></i> 保存 </button>
		<button type="button" class="btn btn-outline btn-danger btn-block"
			style='width:128px;padding-left:10px;float:right;margin-top:0px;' onclick='parent.closeProgramConfig();'><i class="fa fa-sign-out"></i> 取消</button>
	</div>
	</ww:if>
</div>
<div id="divUserSelector" class="divUserSelector" style="display:none; position: absolute; z-index: 1000000; cursor: pointer;">
	<ul id="myZtreeUser" class="ztree" style="margin-top:0; border: 1px solid #617775; background: #eee; color: #fff; width: 220px; height: 360px;"></ul>
</div>
<div id="divFilesSelector" class="divFilesSelector" style="display:none; position: absolute; z-index: 11000000; cursor: pointer;">
	<ul id="myZtreeFiles" class="ztree" style="margin-top:0; border: 1px solid #617775; background: #eee; color: #fff; width: 220px; height: 360px;"></ul>
</div>
</body>
<%@ include file="../../skit/inc/skit_cos.inc"%>
<%@ include file="../../skit/inc/skit_ztree.inc"%>
<script src="skit/js/bootstrap-tour.min.js"></script>
<script src="skit/js/jquery.inputmask.bundle.min.js"></script>
<script src="skit/js/auto-line-number.js"></script>
<ww:if test='editable'>
<SCRIPT type="text/javascript">
var divSet = document.getElementById( 'divSet' );
divSet.style.marginLeft = windowWidth/2 - 140;

var myZtreeUser;
var settingUser = {
	view: {
		dblClickExpand: false
	},
	check: {
		enable: true,
		chkStyle: "radio",
		radioType: "level"
	},
	callback: {
		onClick: onSelectUser,
		onCheck: onSelectUser
	}
};

var inputFiles;
var myZtreeFiles;
var curFilesExpandNode = curExpandNode;
var settingFiles = {
	view: {
		dblClickExpand: false
	},
	check: {
		enable: true,
		chkStyle: "radio",
		radioType: "level"
	},
	callback: {
		onCheck: onSelectFile,
		onClick: openFiles,
		beforeExpand: beforeExpand,
		onExpand: openFiles
	}
};


function showUserSelector(id, keyid, width) 
{
	var div = $("#"+id);
	var offset = $("#"+id).offset();
	if( width ){}else{
		width = 300;
	}
	document.getElementById("myZtreeUser").style.width = width;
	$("#divUserSelector").css({left:offset.left + "px", top:offset.top + div.outerHeight() + "px"}).slideDown("fast");
	$("body").bind("mousedown", onUserBodyDown);

	var value = document.getElementById(keyid).value;
	var node = myZtreeUser.getNodeByParam("id", value);
	if( node )
	{
		myZtreeUser.checkNode(node, true, false);
	}
}

function onUserBodyDown(event) 
{
	if( !(event.target.id == "divUserSelector" || $(event.target).parents("#divUserSelector").length>0) )
	{
		hideUserSelector();
	}
}

function hideUserSelector() 
{
	$("#divUserSelector").fadeOut("fast");
	$("body").unbind("mousedown", onUserBodyDown);
}

function showFilesSelector(id) 
{
	var div = $("#"+id);
	var offset = $("#"+id).offset();
	inputFiles = document.getElementById(id);
	document.getElementById("myZtreeFiles").style.width = inputFiles.clientWidth;
	$("#divFilesSelector").css({left:offset.left + "px", top:offset.top + div.outerHeight() + "px"}).slideDown("fast");
	$("body").bind("mousedown", onFilesBodyDown);
	
	dir = inputFiles.value;
	if( dir == "" ){
		var programid = document.getElementById("programid").value;
		if( id == "logfile" ){
			dir = "log/"+programid;
		}
	}
	//alert(dir);
	gopath(dir);
}

function onFilesBodyDown(event) 
{
	if (!(event.target.id == "divFilesSelector" || 
		  $(event.target).parents("#divFilesSelector").length>0 )
	   )
	{
		hideFielsSelector();
	}
}

function hideFielsSelector() 
{
	$("#divFilesSelector").fadeOut("fast");
	$("body").unbind("mousedown", onBodyDown);
}

function onSelectUser(e, treeId, userNode) {
	document.getElementById("programmer").value = userNode.rname;
	document.getElementById("manager").value = userNode.id;
	document.getElementById("email").value = userNode.email;
	hideUserSelector();
}

function onSelectFile(event, treeId, treeNode)
{
	if( treeNode.checked ){
		if( inputFiles.id == "logfile" )
		{
			skit_input("请设置日志文件类型(例如*.log)，默认是文本文件", "*.txt", function(yes, val){
				if( yes ){
					val = val.trim();
					if( val ) inputFiles.value = treeNode.path + "/" + val;
					else inputFiles.value = treeNode.path;
				}
			});
		}
		else if( inputFiles.id == "cfgfile" ){
			skit_input("请设置配置文件类型(例如*.xml,.conf,.ini等)，默认是文本文件", "*.conf", function(yes, val){
				if( yes ){
					val = val.trim();
					if( val ) inputFiles.value = treeNode.path + "/" + val;
					else inputFiles.value = treeNode.path;
				}
			});
		}
		else {
			skit_input("请输入程序PID文件名称，默认是后缀为.pid的第一个文件", "*.pid", function(yes, val){
				if( yes ){
					if( val ) inputFiles.value = treeNode.path + "/" + val;
					else inputFiles.value = treeNode.path;
				}
			});
		}
	}
	else inputFiles.value = "";
	hideFielsSelector();
}


$(document).ready(function(){
	var json = '<ww:property value="localData" escape="false"/>';
	$.fn.zTree.init($("#myZtreeUser"), settingUser, jQuery.parseJSON(json));
	myZtreeUser = $.fn.zTree.getZTreeObj("myZtreeUser");
	json = '<ww:property value="rowSelect" escape="false"/>';
	$.fn.zTree.init($("#myZtreeFiles"), settingFiles, jQuery.parseJSON(json));
	myZtreeFiles = $.fn.zTree.getZTreeObj("myZtreeFiles");
	myZtree = myZtreeFiles;
});

function openFiles(event, treeId, treeNode) {
	myZtreeFiles.selectNode(treeNode);
	myZtreeFiles.expandNode(treeNode, true);
	getfiles(treeNode);
}

function getfiles(treeNode)
{
	//alert(treeNode.path+", "+treeNode.rootdir);
	FilesMgr.getDirs(ip, port, treeNode.path, treeNode.rootdir,{
		callback:function(response){
			if( response.succeed )
			{
				var json = jQuery.parseJSON(response.result);
				setfiles(treeNode, json);
			}
			else skit_alert(response.message);
		},
		timeout:10000,
		errorHandler:function(message) {
			skit_hiddenLoading();
		}
	});	
}

function setfiles(treeNode, json)
{
	var newnodes = json.children;
	var tmpMap1 = new Object();
	var tmpMap2 = new Object();
	for( var i = 0; i < newnodes.length; i++ )
	{
		tmpMap1[newnodes[i].name] = true;
	}
	var children = treeNode.children;
	if( children != null )
	{
		for( var i = 0; i < children.length; i++ )
		{
			var node = children[i];
			if( tmpMap1[node.name] ){
				tmpMap2[node.name] = i;
				continue;
			}
			myZtreeFiles.removeNode(node);
			i -= 1;
		}
	}
	var cursor = -1;
	var count = 0;
	for( var i = 0; i < newnodes.length; i++ )
	{
		var node = newnodes[i];
		if( tmpMap2[node.name] >= 0 ){
			cursor = count + tmpMap2[node.name];
			continue;
		}
		count += 1;
		cursor += 1;
		myZtreeFiles.addNodes(treeNode, cursor, node);
	}
	treeNode["files"] = json.files;
	treeNode["summary"] = json.summary;
}

function gopath(dir)
{
	var node = myZtreeFiles.getNodeByParam("path", dir);
	if( node )
	{
		myZtreeFiles.selectNode(node);
		myZtreeFiles.expandNode(node, true);
		getfiles(node);
		return;
	}
	else
	{
		//判断是绝对路径还是相对路径
		var rootdir = null;
		if( dir.charAt(0) != '/' )
		{
			node = myZtreeFiles.getNodeByParam("path", "");
			rootdir = node.rootdir;
		}
		skit_showLoading();
		FilesMgr.getAllDirs(ip, port, dir, rootdir, {
			callback:function(response){
				skit_hiddenLoading();
				if( response.succeed )
				{
					try
					{
						for(var i = 0; i < response.objects.length; i++)
						{
							var json = jQuery.parseJSON(response.objects[i]);
							var treeNode = myZtreeFiles.getNodeByParam("path", json.path);
							if( treeNode )
							{
								myZtreeFiles.expandNode(treeNode, true);
								setfiles(treeNode, json);
								if( response.objects.length == i+1)
								{
									myZtreeFiles.selectNode(treeNode);
									//myZtreeFiles.checkNode(treeNode, true, false);
								}
							}
						}
					}
					catch(e)
					{
						alert("跳转路径"+dir+"出现异常"+e.message+", 行数"+e.lineNumber);
					}
				}
				else skit_alert(response.message);
			},
			timeout:30000,
			errorHandler:function(message) {
				skit_hiddenLoading();
			}
		});	
	}
}
</SCRIPT>
</ww:if>
<ww:else>

<style type='text/css'>
body {overflow-y:auto;overflow-x:hidden; margin-top:0px; margin-left:0px; margin-bottom:0px; margin-right:0px }
</style>
</ww:else>
<SCRIPT type="text/javascript">
//#########################################################################
var serverkey = "<ww:property value='serverkey'/>";
var servertype = "<ww:property value='servertype'/>";
var ip = "<ww:property value='ip'/>";
var port = <ww:property value='port'/>;
var id = "<ww:property value='id'/>";
var editable = <ww:property value='editable'/>;

var startupCommands = $("#startupCommands").setTextareaCount({
	width: "30px",
	bgColor: "#373737",
	color: "#FFF",
	display: "inline-block",
	id: "startupCommands"
});
var shutdownCommands = $("#shutdownCommands").setTextareaCount({
	width: "30px",
	bgColor: "#373737",
	color: "#FFF",
	display: "inline-block",
	id: "shutdownCommands"
});
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

function checkVersionSelection()
{
	ControlMgr.getVersionSelection(ip, port, serverkey, id, 0, {
		callback:function(rsp) {
			skit_hiddenLoading();
			try
			{
				if( rsp.succeed )
				{
					var versions = rsp.objects;
					if( versions.length == 0 )
					{
						skit_confirm("该伺服器【"+ip+"】程序【"+id+"】还未设置版本列表数据，您要马上跳转到版本配置界面吗？", function(yes){
							if( yes )
							{
								parent.closeProgramConfig(true);
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
						var func = "document.getElementById(\"version\").value=\""+version+"\";";
						html += "<li><a onclick='"+func+"' style='font-size:9pt;padding:2px 10px;'>" + version + "</a></li>";
					}
					ul.innerHTML = html;
				}
				else skit_alert(rsp.message);
			}
			catch(e)
			{
				alert("选择伺服器【"+ip+"】下程序【"+program.name+"】的版本号出现异常"+e.message+", 行数"+e.lineNumber);
			}
		},
		timeout:30000,
		errorHandler:function(err) {skit_hiddenLoading(); alert(err); }
	});
}

var modeindex = 0;
function changeMode(init)
{
	var selector = document.getElementById("mode");
	var mode = Number(selector.value);
	if( !init )
		if( mode == 3 || mode == 4 )
		{
			if( servertype.indexOf("linux") == -1 )
			{
				skit_alert("守护模式只支持Linux操作系统。");
				selector.options[modeindex].selected = true;
				return;
			}
		}
	switch(Number(selector.value))
	{
	case 0:
		modeindex = 0;
		document.getElementById( "spanMode" ).innerHTML = "主控启动<i class='fa fa-globe' style='margin-left:6px'></i>";
		document.getElementById( "div-control" ).style.display = "";
		document.getElementById( "div-forcereboot" ).style.display = "";
		document.getElementById( "div-timerange" ).style.display = "";
		document.getElementById( "div-logfile" ).style.display = "";
		document.getElementById( "div-pidfile" ).style.display = "";
		break;
	case 1:
		modeindex = 1;
		document.getElementById( "spanMode" ).innerHTML = "界面启动<i class='fa fa-globe' style='margin-left:6px'></i>";
		document.getElementById( "div-control" ).style.display = "";
		document.getElementById( "div-forcereboot" ).style.display = "";
		document.getElementById( "div-timerange" ).style.display = "";
		document.getElementById( "div-logfile" ).style.display = "";
		document.getElementById( "div-pidfile" ).style.display = "";
		break;
	case 2:
		modeindex = 4;
		document.getElementById( "spanMode" ).innerHTML = "单次执行<i class='fa fa-globe' style='margin-left:6px'></i>";
		document.getElementById( "div-control" ).style.display = "none";
		document.getElementById( "div-forcereboot" ).style.display = "";
		document.getElementById( "div-timerange" ).style.display = "";
		document.getElementById( "div-logfile" ).style.display = "";
		document.getElementById( "div-pidfile" ).style.display = "";
		break;
	case 3:
		modeindex = 2;
		document.getElementById( "spanMode" ).innerHTML = "守护自启<i class='fa fa-globe' style='margin-left:6px'></i>";
		document.getElementById( "div-control" ).style.display = "none";
		document.getElementById( "div-forcereboot" ).style.display = "none";
		document.getElementById( "div-timerange" ).style.display = "none";
		document.getElementById( "div-logfile" ).style.display = "";
		document.getElementById( "div-pidfile" ).style.display = "";
		break
	case 4:
		modeindex = 3;
		document.getElementById( "spanMode" ).innerHTML = "守护人启<i class='fa fa-globe' style='margin-left:6px'></i>";
		document.getElementById( "div-control" ).style.display = "none";
		document.getElementById( "div-forcereboot" ).style.display = "none";
		document.getElementById( "div-timerange" ).style.display = "none";
		document.getElementById( "div-logfile" ).style.display = "";
		document.getElementById( "div-pidfile" ).style.display = "";
		break;
	}
	changeForcereboot(frmode);
}

function setCommands(id)
{
	var textarea = document.getElementById(id);
	changeCommands(textarea);
}

function changeCommands(textarea)
{
	var id = textarea.id;
	var a = 0;
	if( id == "shutdownCommands" ) a = 1;
	var val = textarea.value;
	var rows = 0;
	val = val.replace(/[\n]/g, ' '); //去掉回车换行
	var args = val.split(" ");
	val = "";
	for(var i = 0; i < args.length; i++)
	{
		var line = args[i].trim();
		if(line.length == 0) continue;
		if( rows > 0 ) val += "\n";
		val += line;
		rows += 1;
	}
	textarea.value = val;
	if( rows > 0 ){
		textarea.rows = rows;
		$('#textarea-rows-'+id).css({"height" : textarea.clientHeight});
		$('#textarea-wrap-'+id).css({"height" : textarea.clientHeight});
	}
	textarea = document.getElementById(id+"Remark");
	if( rows > 0 ) textarea.rows = rows;
}
//var switchery = new Switchery($(".js-switch")[0]);
function activeDelayed(input)
{
	document.getElementById("delayed").readOnly = !input.checked;
}
function activeRestartup(input)
{
	document.getElementById("restartup").readOnly = !input.checked;
}

function activeTimerange(input)
{
	document.getElementById("starttime").readOnly = !input.checked;
	document.getElementById("endtime").readOnly = !input.checked;
}

function activeForcereboot(input)
{
	if( !input.checked )
	{
		document.getElementById( "divFrMode" ).style.display = "none";
		document.getElementById( "frBeforeTips" ).style.display = "none";
		document.getElementById( "frAfterTips" ).style.display = "none";
		document.getElementById( "frtime" ).style.display = "none";
		document.getElementById( "frval" ).style.display = "none";
		return;
	}
	document.getElementById( "frval" ).value = 600;//默认10分钟重启一次
	changeForcereboot('');
}

function validFrHour(input)
{
	if( document.getElementById( "frmode" ).value == "h" )
	{
		var text = input.value;
		if( text.length > 0 && text.charAt(0) > '5' )
			input.value = "";
	}
}

var frmode = '';
function changeForcereboot(mode)
{
	if( !document.getElementById("iActiveForcereboot").checked)
	{
		document.getElementById( "divFrMode" ).style.display = "none";
		document.getElementById( "frBeforeTips" ).style.display = "none";
		document.getElementById( "frAfterTips" ).style.display = "none";
		document.getElementById( "frtime" ).style.display = "none";
		document.getElementById( "frval" ).style.display = "none";
		return;
	}
	frmode = mode;
	document.getElementById( "divFrMode" ).style.display = "";
	document.getElementById( "frBeforeTips" ).style.display = "";
	document.getElementById( "frAfterTips" ).style.display = "";
	document.getElementById( "frval" ).style.display = "";
	document.getElementById( "frval" ).style.width = 64;
	if( "" == mode )
	{
		document.getElementById( "frBeforeTips" ).innerHTML = "每";
		document.getElementById( "frmode" ).value = "";
		document.getElementById( "frAfterTips" ).style.display = "";
		document.getElementById( "frAfterTips" ).innerText = "秒";
		document.getElementById( "frtime" ).style.display = "none";
		document.getElementById( "frval" ).maxLength = 3;
	    $('#frval').inputmask('remove');
	    $('#frval').inputmask('integer');
		document.getElementById( "frval" ).style.width = 56;
	}
	else if( "h" == mode )
	{
		document.getElementById( "frBeforeTips" ).innerHTML = "每";
		document.getElementById( "frmode" ).value = "h";
		document.getElementById( "frAfterTips" ).innerText = "小时的";
		document.getElementById( "frtime" ).style.display = "";
		document.getElementById( "frval" ).maxLength = 2;
	    $('#frval').inputmask('remove');
	    $('#frval').inputmask('integer');
		document.getElementById( "frval" ).style.width = 48;
		$("#frtime").inputmask("remove");
		document.getElementById( "frtime" ).placeholder = "59:00";
		$("#frtime").inputmask("mask", {"mask": "99:00"});
	}
	else if( "d" == mode )
	{
		document.getElementById( "frBeforeTips" ).innerHTML = "每";
		document.getElementById( "frmode" ).value = "d";
		document.getElementById( "frAfterTips" ).innerText = "天的";
		document.getElementById( "frtime" ).style.display = "";
		document.getElementById( "frval" ).maxLength = 1;
	    $('#frval').inputmask('remove');
	    $('#frval').inputmask('integer');
		document.getElementById( "frval" ).style.width = 36;
		$("#frtime").inputmask("remove");
		$("#frtime").inputmask("hh:mm");
		document.getElementById( "frtime" ).placeholder = "23:59";
		//$("#frval").inputmask("decimal", { skipRadixDance: true, digits:0, rightAlignNumerics: true, allowMinus: false });
	}
	else if( "w" == mode )
	{
		document.getElementById( "frBeforeTips" ).innerHTML = "每周星期";
		document.getElementById( "frmode" ).value = "w";
		document.getElementById( "frAfterTips" ).innerText = "的";
		document.getElementById( "frtime" ).style.display = "";
		document.getElementById( "frval" ).maxLength = 1;
	    $('#frval').inputmask('remove');
		$("#frval").inputmask("mask", {"mask": "9"});
		document.getElementById( "frval" ).style.width = 36;
		$("#frtime").inputmask("remove");
		$("#frtime").inputmask("hh:mm");
		document.getElementById( "frtime" ).placeholder = "23:59";
	}
	else if( "m" == mode)
	{
		document.getElementById( "frBeforeTips" ).innerHTML = "每月";
		document.getElementById( "frmode" ).value = "m";
		document.getElementById( "frAfterTips" ).innerText = "的";
		document.getElementById( "frtime" ).style.display = "";
		document.getElementById( "frval" ).maxLength = 5;
	    $('#frval').inputmask('remove');
		$("#frval").inputmask("d \\日");
		$("#frtime").inputmask("remove");
		$("#frtime").inputmask("hh:mm");
		document.getElementById( "frtime" ).placeholder = "23:59";
	}
}
var config = null;
try
{
	var json = '<ww:property value="jsonData" escape="false"/>';
	document.getElementById( "tdInfo" ).innerHTML = "<h3 class='heading pull-left'><i class='fa fa-plus-circle'></i> 程序配置 "+
		"<span class='sub-heading'>程序配置相关参数如下所示</span></h3>";
	if( json != "" )
	{
		config = jQuery.parseJSON(json);
		if( config.oper == 0 )
		{
			document.getElementById( "tdInfo" ).innerHTML = "<h3 class='heading pull-left'><i class='fa fa-plus-circle'></i> 新增程序配置 "+
			"<span class='sub-heading'>该程序配置在"+config.opertime+"被"+config.operuser+"提交新增，通过填写选择输入以下参数完成程序新增配置</span></h3>";
		}
		else if( config.oper == 1 )
		{
			document.getElementById( "tdInfo" ).innerHTML = "<h3 class='heading pull-left'><i class='fa fa-edit'></i> 修改程序配置 "+
				"<span class='sub-heading'>该程序配置在"+config.opertime+"被"+config.operuser+"提交修改，通过填写选择输入以下参数完成程序修改配置</span></h3>";
		}
		else if( config.oper == 3 )
		{
			document.getElementById( "tdInfo" ).innerHTML = "<h3 class='heading pull-left'><i class='fa fa-edit'></i> 待删除程序配置 "+
				"<span class='sub-heading'>该程序配置在"+config.opertime+"被"+config.operuser+"提交删除，等待系统管理员审批</span></h3>";
		}
		else if( config.oper == 4 )
		{
			document.getElementById( "tdInfo" ).innerHTML = "<h3 class='heading pull-left'><i class='fa fa-edit'></i> 已删除程序配置 "+
				"<span class='sub-heading'>该程序配置已经在"+config.opertime+"被"+config.operuser+"删除</span></h3>";
		}
		document.getElementById( "programid" ).value = config.id?config["id"]:"";
		document.getElementById( "programid" ).readOnly = !editable;
		document.getElementById( "name" ).value = config.name?config["name"]:"";
		document.getElementById( "name" ).readOnly = !editable;
		document.getElementById( "version" ).value = config.version?config["version"]:"";
		document.getElementById( "description" ).value = config.description?config.description:"";
		document.getElementById( "description" ).readOnly = !editable;
		
		if( config.maintenance ){
			document.getElementById( "programmer" ).value = config.maintenance["programmer"];
			document.getElementById( "manager" ).value = config.maintenance["manager"];
			document.getElementById( "email" ).value = config.maintenance["email"];
		}

		var control = config["control"];
		if( config.control ){}else{
			control = new Object();
		}
		document.getElementById( "mode" ).value = control.mode?control["mode"]:"0";
		document.getElementById( "mode" ).disabled = !editable;
		document.getElementById( "logfile" ).value = control.logfile?control.logfile:"";
		document.getElementById( "pidfile" ).value = control.pidfile?control.pidfile:"";
		document.getElementById( "cfgfile" ).value = control.cfgfile?control.cfgfile:"";
		if( control.restartup )
		{
			document.getElementById( "restartup" ).value = control["restartup"];
			document.getElementById( "restartup" ).readOnly = !editable;
			document.getElementById( "iActiveRestartup" ).checked = control.restartup?true:false;
			document.getElementById( "iActiveRestartup" ).readOnly = !editable;
		}
		if( control.delayed )
		{
			document.getElementById( "delayed" ).value = control["delayed"];
			document.getElementById( "delayed" ).readOnly = !editable;
			document.getElementById( "iActiveDelayed" ).checked = control.delayed?true:false;
			document.getElementById( "iActiveDelayed" ).readOnly = !editable;
		}
		document.getElementById( "dependence" ).value = control.dependence?control["dependence"]:"";
		document.getElementById( "dependence" ).disabled = !editable;

		var forcereboot = control["forcereboot"];
		if( forcereboot ){}else{
			forcereboot = new Object();
		}
		if( forcereboot.val )
		{
			document.getElementById( "frval" ).value = forcereboot["val"];
			document.getElementById( "frval" ).readOnly = !editable;
			document.getElementById( "frmode" ).value = forcereboot["mode"];
			document.getElementById( "frtime" ).value = forcereboot["time"];
			document.getElementById( "frtime" ).readOnly = !editable;
			document.getElementById( "iActiveForcereboot" ).checked = forcereboot.val?true:false;
			document.getElementById( "iActiveForcereboot" ).readOnly = !editable;
		}
		if( control.starttime && control.endtime )
		{
			document.getElementById( "starttime" ).value = control["starttime"];
			document.getElementById( "starttime" ).readOnly = control.starttime&&!editable?true:false;
			document.getElementById( "endtime" ).value = control["endtime"];
			document.getElementById( "endtime" ).readOnly = control.endtime&&!editable?true:false;
			document.getElementById( "iActiveTimerange" ).checked = control.starttime?true:false;
			document.getElementById( "iActiveTimerange" ).readOnly = !editable;
		}
		
		document.getElementById( "startupCommands" ).readOnly = !editable;
		document.getElementById( "startupCommandsRemark" ).readOnly = !editable;
		document.getElementById( "shutdownCommands" ).readOnly = !editable;
		document.getElementById( "shutdownCommandsRemark" ).readOnly = !editable;
		document.getElementById( "startupCommands" ).value = unicode2Chr(config.startupCommands);
		document.getElementById( "startupCommandsRemark" ).value = unicode2Chr(config.startupCommandsRemark);
		document.getElementById( "shutdownCommands" ).value = unicode2Chr(config.shutdownCommands);
		document.getElementById( "shutdownCommandsRemark" ).value = unicode2Chr(config.shutdownCommandsRemark);
		config["ip"] = ip;
		config["port"] = port;
		config["serverkey"] = serverkey;
		changeForcereboot(forcereboot.mode);
		changeMode(true);
		setCommands("startupCommands");
		setCommands("shutdownCommands");
	}
	$("#restartup").inputmask("decimal", {digits:0});
	$("#delayed").inputmask("decimal", {digits:0});
	$("#starttime").inputmask("hh:mm");
	$("#endtime").inputmask("hh:mm");
	
	if( editable && id )
	{
		foundModeInvalid(true);
	}
	else
	{
		if( parent && parent.openNicescroll )
		{
			parent.openNicescroll();
		}
	}
}
catch(e)
{
	alert(e);
}

function foundModeInvalid(chkVersion)
{
	var selector = document.getElementById("mode");
	var mode = Number(selector.value);
	if( (mode == 3 || mode == 4) && servertype.indexOf("linux") == -1 )
	{
		skit_alert("该程序配置的守护模式只支持Linux操作系统，但是您配置的伺服器是"+servertype+"。", "配置错误提示");
		return true;
	}
	else
	{
		if( chkVersion ) checkVersionSelection();
	}
	return false;
}

function setProgramConfig()
{
	if( foundModeInvalid() ) return;
	var oper = config?config.oper:0;
	config = new Object();
	if( oper ) config["oper"] = oper;
	config["id"] = document.getElementById( "programid" ).value;
	var regexp1 = "^[0-9a-zA-Z_\.\-]{2,}$";
	try{
	    var m1 = config.id.match(new RegExp(regexp1));
	    if( !m1 )
	    {
	    	document.getElementById( "programid" ).focus();
			$("#programid").tooltip("show");
	        return;
	    }
	}
	catch(e){
		alert(e);
	}
	if( config.id.length<2 || config.id.length>64 )
	{
    	document.getElementById( "programid" ).focus();
		$("#programid").tooltip("show");
	    return;
	}
	
	var options = document.getElementById( "dependence" ).options;
	for(var i = 0; i < options.length; i++ )
	{
		if( config.id == options[i].value )
		{
	    	skit_alert("您设定的程序唯一标识与已发布程序【"+options[i].text+"】的唯一标识重复");
			return;
		}
	}
	
	config["name"] = document.getElementById( "name" ).value;
    if( config.name.length<4 || config.name.length>32 )
	{
    	skit_alert("程序名称不能少于4个字多于32个字");
	    return;
	}
    
	config["version"] = document.getElementById( "version" ).value;
    if( config.version == "" )
	{
		$("#version").tooltip("show");
		document.getElementById( "version" ).focus();
	    return;
	}
	config["description"] = document.getElementById( "description" ).value;
	if( config.description.length<16 || config.description.length>500 )
	{
		$("#description").tooltip("show");
		document.getElementById( "description" ).focus();
	    return;
	}
	
	var maintenance = new Object();
	config["maintenance"] = maintenance;
	
	maintenance["programmer"] = document.getElementById( "programmer" ).value;
    if( maintenance.programmer == "" )
	{
		$("#programmer").tooltip("show");
		document.getElementById( "programmer" ).focus();
	    return;
	}
    maintenance["manager"] = document.getElementById( "manager" ).value;
    maintenance["email"] = document.getElementById( "email" ).value;

	var control = new Object();
	config["control"] = control;
	control["mode"] = Number(document.getElementById( "mode" ).value);
	if( document.getElementById( "logfile" ).value != "" )
		control["logfile"] = document.getElementById( "logfile" ).value;
    control["pidfile"] = document.getElementById( "pidfile" ).value;
    control["cfgfile"] = document.getElementById( "cfgfile" ).value;
	if( control.mode == 3 || control.mode == 4 )
	{
		if( servertype.indexOf("linux") == -1 )
		{
			skit_alert("守护模式只支持Linux操作系统。");
			return;
		}
	    if( control.logfile == "" )
		{
			$("#logfile").tooltip("show");
			document.getElementById( "logfile" ).focus();
		    return;
		}
	    if( control.pidfile == "" )
		{
			$("#pidfile").tooltip("show");
			document.getElementById( "pidfile" ).focus();
		    return;
		}
	}
	
	if( document.getElementById( "iActiveRestartup" ).checked )
	{
	    if( document.getElementById( "restartup" ).value == "" )
		{
			$("#restartup").tooltip("show");
			document.getElementById( "restartup" ).focus();
		    return;
		}
		control["restartup"] = Number(document.getElementById( "restartup" ).value);
	}
	if( document.getElementById( "iActiveDelayed" ).checked )
	{
	    if( document.getElementById( "delayed" ).value == "" )
		{
			$("#delayed").tooltip("show");
			document.getElementById( "delayed" ).focus();
		    return;
		}
		control["delayed"] = Number(document.getElementById( "delayed" ).value);
	}
	control["dependence"] = document.getElementById( "dependence" ).value;
	var regexpHour = new RegExp("^([0-1][0-9]|[2][0-3]):([0-5][0-9])$");
	var regexpMinute = new RegExp("^([0-5][0-9]):([0-5][0-9])$");
	if( document.getElementById( "iActiveForcereboot" ).checked )
	{
		var forcereboot = new Object();
		control["forcereboot"] = forcereboot;
		forcereboot["val"] = document.getElementById( "frval" ).value;
		forcereboot["mode"] = document.getElementById( "frmode" ).value;
		forcereboot["time"] = document.getElementById( "frtime" ).value;
		if( forcereboot.mode != "" )
		{
		    var regexp = regexpHour;
			if( forcereboot["mode"] == 'h') regexp = regexpMinute;
			if( !forcereboot.time.match(regexp) )
		    {
		    	document.getElementById( "frtime" ).focus();
				$("#frtime").tooltip("show");
		        return;
		    }
		}
	}
	if( document.getElementById( "iActiveTimerange" ).checked )
	{
		control["starttime"] = document.getElementById( "starttime" ).value;
	    if( !control.starttime.match(regexpHour) )
	    {
	    	document.getElementById( "starttime" ).focus();
			$("#starttime").tooltip("show");
	        return;
	    }
	    control["endtime"] = document.getElementById( "endtime" ).value;
	    if( !control.endtime.match(regexpHour) )
	    {
	    	document.getElementById( "endtime" ).focus();
			$("#endtime").tooltip("show");
	        return;
	    }

	}

	if( control.mode != 3 && control.mode != 4 )
	{
		if( document.getElementById( "startupCommands" ).value == "" )
		{
			$("#startupCommands").tooltip("show");
			document.getElementById( "startupCommands" ).focus();
		    return;
		}
	}
	config["startupCommands"] = chr2Unicode(document.getElementById( "startupCommands" ).value);
	config["shutdownCommands"] = chr2Unicode(document.getElementById( "shutdownCommands" ).value);
	config["startupCommandsRemark"] = chr2Unicode(document.getElementById( "startupCommandsRemark" ).value);
	config["shutdownCommandsRemark"] = chr2Unicode(document.getElementById( "shutdownCommandsRemark" ).value);
	config["ip"] = ip;
	config["port"] = port;
	config["serverkey"] = serverkey;
	var json = JSON.stringify(config);
	//skit_alert(json);
	parent.setProgramConfig("<ww:property value='id'/>", json);
}

/**
 * unicode编码转中文
 */
function unicode2Chr(str) 
{
	if (str)
	{
		var st, t, i
		st = '';
		for (i = 1; i <= str.length/4; i++)
		{
			t = str.slice(4*i-4, 4*i-2);
			t = str.slice(4*i-2, 4*i).concat(t);
			st = st.concat('%u').concat(t);
		}
		st = unescape(st);
		return st;
	}
	else
		return "";
}


/**
 * 中文转unicode编码
 */
function chr2Unicode(str)
{
	if( str ) 
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
		return "";
}
</SCRIPT>
</html>