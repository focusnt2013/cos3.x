<%@ page language="java" pageEncoding="UTF-8"%>
<%@ page import="com.focus.cos.web.common.Kit"%>
<%@ taglib uri="/webwork" prefix="ww" %>
<%Kit.noCache(request,response);  %>
<html>
<head>
<link type="text/css" href="skit/css/costable.css" rel="stylesheet">
<link type="text/css" href="jsp/monitor/server.css?v=4" rel="stylesheet">
<style type='text/css'>
.log {
	padding-top: 5px;
}
.programname {
	font-size: 12px;
	font-family: "微软雅黑",sans-serif;
	display:block;
	width:310px;
	word-break:keep-all;
	white-space:nowrap;
	overflow:hidden;
	text-overflow:ellipsis;
	line-height: 26px;
}
</style>
<%=Kit.getDwrJsTag(request,"interface/MonitorMgr.js")%>
<%=Kit.getDwrJsTag(request,"engine.js")%>
<%=Kit.getDwrJsTag(request,"util.js")%>
</head>
<body>
<form action="#" method="post">
<input type='hidden' name='command'/>
<input type='hidden' name='path' id='path'>
<input type='hidden' name='id' id='id' value="<ww:property value='id'/>">
<input type='hidden' name='ip' id='ip' value="<ww:property value='ip'/>">
<input type='hidden' name='port' id='port' value="<ww:property value='port'/>">
<div class="row">
       <!-- /.col -->
       <div class="col-md-3 col-sm-6 col-xs-12" title='点击查看内存负载图表'>
         <div class="info-box" onclick='viewServerChart(1);'>
           <span class="info-box-icon bg-red"><i class="fa fa-meetup"></i></span>
           <div class="info-box-content">
             <span class="info-box-text">内存 负载</span>
             <span class="info-box-number" id='dashM'>0<small>%</small></span>
             <span class="info-box-number" id='dashM0' style='font-weight:normal;font-size:10px;color:gray;'>22</span>
           </div>
           <!-- /.info-box-content -->
         </div>
         <!-- /.info-box -->
       </div>
       <!-- /.col -->
	   <div class="col-md-3 col-sm-6 col-xs-12" title='点击查看CPU负载图表'>
         <div class="info-box" onclick='viewServerChart(0);'>
           <span class="info-box-icon bg-aqua"><i class="fa fa-microchip"></i></span>
           <div class="info-box-content">
             <span class="info-box-text">CPU 负载</span>
             <span class="info-box-number" id='dashC'>0<small>%</small></span>
             <span class="info-box-number" id='dashC0' style='font-weight:normal;font-size:10px;color:gray;'>22</span>
           </div>
           <!-- /.info-box-content -->
         </div>
         <!-- /.info-box -->
       </div>

       <!-- fix for small devices only -->
       <div class="clearfix visible-sm-block"></div>

       <div class="col-md-3 col-sm-6 col-xs-12" title='点击查看网络吞吐负载图表'>
         <div class="info-box" onclick='viewServerChart(2);'>
           <span class="info-box-icon bg-green"><i class="fa fa-wifi"></i></span>
           <div class="info-box-content">
             <span class="info-box-text">网络流量 负载</span>
             <span class="info-box-number" id='dashN' style='font-size:14px;'>0</span>
             <span class="info-box-number" id='dashN0' style='font-weight:normal;font-size:10px;color:gray;'></span>
           </div>
           <!-- /.info-box-content -->
         </div>
         <!-- /.info-box -->
       </div>
       <!-- /.col -->
       <div class="col-md-3 col-sm-6 col-xs-12" title='点击查看所有监控指标图表'>
         <div class="info-box" onclick='viewServerChart(-1);'>
           <span class="info-box-icon bg-yellow"><i class="fa fa-th-large"></i></span>
           <div class="info-box-content">
             <span class="info-box-text">运行程序</span>
             <span class="info-box-number" id='dashP'>0</span>
             <span class="info-box-number" id='dashP0' style='font-weight:normal;font-size:10px;color:gray;'></span>
           </div>
           <!-- /.info-box-content -->
         </div>
         <!-- /.info-box -->
       </div>
       <!-- /.col -->
</div>

<div class="row" style='margin-left:5px;margin-right:5px;'>
	<div class="box box-success" id='boxMonitor'>
    	<div class="box-header with-border">
              <h3 class="box-title">伺服器负载监控(CPU、内存、网络上下行、磁盘IO)</h3>
              <div class="box-tools pull-right">
              	<button type="button" class="btn btn-box-tool" data-widget="remove"><i class="fa fa-times"></i></button>
                <button type="button" class="btn btn-box-tool" data-widget="collapse" id='btnMonitor'
                 onclick='collapseBox(this, "monitor");'><i class="fa fa-window-minimize"></i></button>
              </div>
            </div>
            <!-- /.box-header -->
            <div class="box-body" style="padding-top:0px;padding-bottom:0px;padding-left:1px;padding-right:1px;" id='bodyMonitor'>
				<iframe name='iMonitor' id='iMonitor' style='width:100%;height:280px;border:0px;' src=''></iframe>
    	</div>
	</div>
</div>

<div class="row" style='margin-left:5px;margin-right:5px;<ww:if test="selectionModel!=null&&selectionModel.equals('inner')">display:none</ww:if>'>
	<div class="box box-warning">
  		<div class="box-header with-border">
             <h3 class="box-title">用户程序列表</h3>
             <div class="box-tools pull-right">
               <button type="button" class="btn btn-box-tool" data-widget="collapse" id='btnOuter' onclick='collapseBox(this, "outer");'
               	><i class="fa fa-window-minimize"></i></button>
             </div>
           </div>
           <!-- /.box-header -->
           <div class="box-body" style="padding-top:0px;padding-bottom:0px;padding-left:1px;padding-right:1px;">
			<table id="tbOuter" style='margin-bottom: 5px;border:0px solid red;' border='0'>
			<tr><td class='head' style='font-weight:bold;width:50px;'>状态</td>
				<td class='head' style='font-weight:bold'><a onclick='sortAlpha("tbOuter", "sortIconNameOuter", 1)'>用户服务引擎<i class='fa skit_fa_icon fa-sort-alpha-desc' id='sortIconNameOuter'></i></a></td>
				<td class='head' style='width:18px'>&nbsp;</td>
				<td class='head' style='font-weight:bold;width:108px;'>版本</td>
				<td class='head' style='font-weight:bold;width:80px;'>程序管理员</td>
				<td class='head' style='font-weight:bold;width:108px;'>上次启动</td>
				<td class='head' style='font-weight:bold;width:128px;'><a onclick='sortAmount("tbOuter", "sortIconMemoryOuter", 6)'>内存占用<i class='fa skit_fa_icon fa-sort' id='sortIconMemoryOuter'></i></a></td>
				<td class='head' style='font-weight:bold;width:108px;display:none'>持续运行</td>
				<td class='head' align='right' style='font-weight:bold;width:80px;padding-right:10px;'>&nbsp;</td></tr>
			<tr class='unline'><td align='center' colspan='6' style='font-weight:bold'>使用内存合计</td>
				<td colspan='3' style='font-weight:bold' id='TotalMemeory'></td></tr>
			</table>            
         </div>
	</div>
</div>

<div class="row" style='margin-left:5px;margin-right:5px;<ww:if test="selectionModel!=null&&selectionModel.equals('outer')">display:none</ww:if>'>
	<div class="box box-success collapsed-box" id='boxInner'>
            <div class="box-header with-border">
              <h3 class="box-title">内置程序列表</h3>
              <div class="box-tools pull-right">
                <button type="button" class="btn btn-box-tool" data-widget="collapse" id='btnInner' onclick='collapseBox(this, "inner");'><i class="fa fa-window-maximize"></i></button>
              </div>
            </div>
            <!-- /.box-header -->
            <div class="box-body" style="padding-top:0px;padding-bottom:0px;padding-left:1px;padding-right:1px;" id='bodyInner'>
				<table id='tbInner' class="panel panel-default" style='margin-bottom: 5px;border:0px solid red;' border='0'>
				<tr><td class='head' style='font-weight:bold;width:50px;'>状态</td>
					<td class='head' style='font-weight:bold'><a onclick='sortAlpha("tbInner", "sortIconNameInner", 1)'>内置服务引擎<i class='fa skit_fa_icon fa-sort-alpha-desc' id='sortIconNameInner'></i></a></td>
					<td class='head' style='width:18px'>&nbsp;</td>
					<td class='head' style='font-weight:bold;width:108px;'>版本</td>
					<td class='head' style='font-weight:bold;width:80px;'>程序管理员</td>
					<td class='head' style='font-weight:bold;width:108px;'>上次启动</td>
					<td class='head' style='font-weight:bold;width:128px;'><a onclick='sortAmount("tbInner", "sortIconMemoryInner", 6)'>内存占用<i class='fa skit_fa_icon fa-sort' id='sortIconMemoryInner'></i></a></td>
					<td class='head' style='font-weight:bold;width:108px;display:none;'>持续运行</td>
					<td class='head' align='right' style='font-weight:bold;width:80px;padding-right:10px;'>&nbsp;</td></tr>
				<tr class='unline'><td align='center' colspan='6' style='font-weight:bold'>合计</td>
					<td colspan='3' style='font-weight:bold' id='TotalMemeory0'></td></tr>
				</table>
            </div>
	</div>
</div>

<div class="row" style='margin-left:5px;margin-right:5px;'>
	<div class="box box-info">
            <div class="box-header with-border">
              <h3 class="box-title"><i class='skit_fa_icon fa fa-server'></i> 伺服器状态</h3>
              <div class="box-tools pull-right">
                <button type="button" class="btn btn-box-tool" data-widget="collapse"><i class="fa fa-window-minimize"></i></button>
              </div>
            </div>
            <!-- /.box-header -->
            <div class="box-body" style="padding-top:0px;padding-bottom:0px;padding-left:1px;padding-right:1px;">
			<table id='tbServerStatus' class="panel panel-default" style='margin-bottom: 5px;<ww:if test="selectionModel!=null">display:none</ww:if>'>
			<tr>
				<td colspan='2' style='padding-top:10px;padding-bottom:10px;'>
				<div class="inner-t inner-b text-center clearfix">
				       <div class="col-xs-3 border right" style="width:12.5%">
				           <div><img src="images/icons/gray.gif" id='programState'></div>
				           <small class="text-muted">程序</small>
				       </div>
				       <div class="col-xs-3 border right" style="width:12.5%">
				           <div><img src="images/icons/gray.gif" id='cpuState'></div>
				           <small class="text-muted">CPU</small>
				       </div>
				       <div class="col-xs-3" style="width:12.5%">
				           <div><img src="images/icons/gray.gif" id='memoryState'></div>
				           <small class="text-muted">内存</small>
				       </div>
				       <div class="col-xs-3" style="width:12.5%">
				           <div><img src="images/icons/gray.gif" id='diskState'></div>
				           <small class="text-muted">硬盘</small>
				       </div>
				       <div class="col-xs-3" style="width:12.5%">
				           <div><img src="images/icons/gray.gif" id='netState'></div>
				           <small class="text-muted">网络</small>
				       </div>
				       <div class="col-xs-3" style="width:12.5%">
				           <div><img src="images/icons/gray.gif" id='databaseState'></div>
				           <small class="text-muted">数据库</small>
				       </div>
				       <div class="col-xs-3" style="width:12.5%">
				           <div><img src="images/icons/gray.gif" id='zookeeperState'></div>
				           <small class="text-muted">Zookeeper</small>
				       </div>
				       <div class="col-xs-3 border left" style="width:12.5%">
				           <div><img src="images/icons/gray.gif" id='coswsState'></div>
				           <small class="text-muted">接口</small>
				       </div>
				   </div>
				</td>
			</tr>
			<tr>
				<td width='320'><i class='skit_fa_icon fa fa-info-circle'></i>程序情况</td>
				<td><ww:property value="systemPerf.programInfo"/></td></tr>
			<tr>
				<td><i class='skit_fa_icon fa fa-info-circle'></i>CPU负载</td>
				<td><ww:property value="systemPerf.cpuLoadInfo"/></td></tr>
			<tr>
				<td><i class='skit_fa_icon fa fa-info-circle'></i><ww:text name="label.ema.omt.cpu.occupation"/></td>
				<td><ww:property value="systemPerf.phyMemUsageInof"/>&nbsp;&nbsp;<ww:property value="systemPerf.phyMemUsageDetail"/></td></tr>
			<tr>
				<td><i class='skit_fa_icon fa fa-info-circle'></i>网络负载</td>
				<td><ww:property value="systemPerf.netLoad"/></td></tr>
			<tr>
				<td><i class='skit_fa_icon fa fa-info-circle'></i>磁盘IO</td>
				<td><ww:property value="systemPerf.iOLoad"/></td></tr>
			<tr>
				<td><i class='skit_fa_icon fa fa-info-circle'></i><ww:text name="label.ema.omt.disk.occupation"/></td>
				<td>
				<ww:property value="systemPerf.diskUsageInfo"/>&nbsp;&nbsp;<ww:property value="systemPerf.diskUsageDetail"/>
				&nbsp;&nbsp;<ww:property value="systemPerf.storagesInfo"/>
				</td>
			</tr>
			
			<tr class='unline'>
				<td><i class='skit_fa_icon fa fa-info-circle'></i>监控状态</td>
				<td id='perfTime'></td>
			</tr>
			
			</table>            
            </div>
	</div>
</div>

<div class="row" style='margin-left:5px;margin-right:5px;'>
	<div class="box box-danger">
            <div class="box-header with-border">
              <h3 class="box-title"><i class='skit_fa_icon fa fa-database'></i> 数据库管理</h3>
              <div class="box-tools pull-right">
                <button type="button" class="btn btn-box-tool" data-widget="collapse"><i class="fa fa-window-minimize"></i></button>
              </div>
            </div>
            <!-- /.box-header -->
            <div class="box-body" style="padding-top:0px;padding-bottom:0px;padding-left:1px;padding-right:1px;">
				<table class="panel panel-default" style='margin-bottom: 5px;<ww:if test="selectionModel!=null">display:none</ww:if>'>
				<tr><td id='tdDatabaseStatus'></td></tr>
				</table>
            </div>
	</div>
</div>

<div class="row" style='margin-left:5px;margin-right:5px;'>
	<div class="box box-info">
    	<div class="box-header with-border">
              <h3 class="box-title"><i class='skit_fa_icon fa fa-wifi'></i>网络状况监测 </h3>
              <div class="box-tools pull-right">
                <button type="button" class="btn btn-box-tool" data-widget="collapse"><i class="fa fa-window-minimize"></i></button>
              </div>
            </div>
            <!-- /.box-header -->
            <div class="box-body" style="padding-top:0px;padding-bottom:0px;padding-left:1px;padding-right:1px;">
				<table id='tbNetInfo' class="panel panel-default" style='margin-bottom: 5px;<ww:if test="selectionModel!=null">display:none</ww:if>'>
				<tr class='unline'>
					<td width='120'><i class='skit_fa_icon fa fa-info-circle'></i>
						<ww:text name="label.ema.omt.host.ip.info"></ww:text> </td>
					<td>
				<pre id='preIP'>
				<ww:property value="systemPerf.getPropertyValue('IPInfo')"/>
				</pre>
					</td>
				</tr>
				</table>
    	</div>
	</div>
</div>

<div class="row" style='margin-left:5px;margin-right:5px;'>
	<div class="box box-success">
            <div class="box-header with-border">
              <h3 class="box-title"><i class='skit_fa_icon fa fa-windows'></i>操作系统信息</h3>
              <div class="box-tools pull-right">
                <button type="button" class="btn btn-box-tool" data-widget="collapse"><i class="fa fa-window-minimize"></i></button>
              </div>
            </div>
            <!-- /.box-header -->
            <div class="box-body" style="padding-top:0px;padding-bottom:0px;padding-left:1px;padding-right:1px;">
				<table id='tbOS' class="panel panel-default" style='margin-bottom: 5px;<ww:if test="selectionModel!=null">display:none</ww:if>'>
				<tr>
					<td width='320'><i class='skit_fa_icon fa fa-info-circle'></i>主机名</td>
					<td id='hostName'><ww:property value="systemPerf.hostName"/></td></tr>
				<tr>
					<td><i class='skit_fa_icon fa fa-info-circle'></i>上次启动时间</td>
					<td id='systemUpTime'><ww:property value="systemPerf.systemUpTime"/></td></tr>
				<tr>
					<td><i class='skit_fa_icon fa fa-info-circle'></i>操作系统类型</td>
					<td id='OSName'><ww:property value='systemPerf.getPropertyValue("OSName")'/></td></tr>
				<tr>
					<td><i class='skit_fa_icon fa fa-info-circle'></i>操作系统版本</td>
					<td id='OSVersion'><ww:property value='systemPerf.getPropertyValue("OSVersion")'/></td></tr>
				<tr>
					<td><i class='skit_fa_icon fa fa-info-circle'></i>物理内存总量</td>
					<td id='PhysicalMemoryInfo'><ww:property value='systemPerf.getPropertyValue("PhysicalMemoryInfo")'/></td></tr>
				<tr>
					<td><i class='skit_fa_icon fa fa-info-circle'></i>磁盘分区情况</td>
					<td id='DiskInfo'><ww:property value='systemPerf.getPropertyValue("DiskInfo")'/></td></tr>
				<tr>
					<td><i class='skit_fa_icon fa fa-info-circle'></i>安全密钥</td>
					<td id='securityKey'><ww:property value="systemPerf.securityKey"/></td></tr>
				<tr>
					<td><i class='skit_fa_icon fa fa-info-circle'></i>COS版本</td>
					<td id='cosVersion' title="<ww:property value='systemPerf.getPropertyValue("cosVersion")'/>"><ww:property value='systemPerf.getPropertyValue("cos.version")'/></td></tr>
				<tr>
					<td><i class='skit_fa_icon fa fa-info-circle'></i>主机其它情况</td>
					<td>
				<pre id='HostInfo'>
				<ww:property value="systemPerf.getPropertyValue('HostInfo')"/>
				</pre></td>
				</tr>
				</table>
            </div>
	</div>
</div>

<iframe name='downloadFrame' style='display:none;visibility: hidden'></iframe>
</form>
<div style='display: none' id='programomt'>
<ww:if test='grant'>
<li><a onclick='restartup();'><i class='skit_fa_icon fa fa-rotate-left fa-spin'></i> 重启程序</a></li>
<li><a onclick='suspend();'><i class='skit_fa_icon fa fa-pause'></i> 暂停程序</a></li>
<li><a onclick='clearlogs();'><i class='skit_fa_icon fa fa-trash'></i> 清除日志</a></li>
<li><a onclick='debug();'><i class='skit_fa_icon fa fa-bug'></i> 管道调测</a></li>
<li class='divider'></li>
</ww:if>
<li><a onclick='configProgram();'><i class='skit_fa_icon fa fa-cogs'></i> 程序配置文件</a></li>
<li><a onclick='viewPid();'><i class='skit_fa_icon_orange fa fa-heartbeat'></i> 查看进程详情</a></li>
<li><a onclick='viewNetstat();'><i class='skit_fa_icon_red fa fa-snowflake-o'></i> 查看进程端口使用情况</a></li>
<li><a onclick='viewMemory();'><i class='skit_fa_icon_orange fa fa-microchip'></i> 查看程序内存</a></li>
</div>
</body>
<%@ include file="../../skit/inc/skit_bootstrap.inc"%>
<%@ include file="../../skit/inc/skit_cos.inc"%>
<style type='text/css'>
body {
	margin-top:1px;
	margin-bottom:1px;
	margin-left:1px;
	margin-right:1px;
    background: #efefef;
}

.row {
    margin-right: -10px;
    margin-left: -10px;
    background: #efefef;
}
.info-box-icon i {
	margin-top: 20px;
}

.pull-right {
    float: right !important;
}
.btn {
    display: inline-block;
    padding: 6px 4px;
    margin-bottom: 0;
    font-size: 14px;
    font-weight: 400;
    line-height: 1.42857143;
    text-align: center;
    white-space: nowrap;
    vertical-align: middle;
    -ms-touch-action: manipulation;
    touch-action: manipulation;
    cursor: pointer;
    -webkit-user-select: none;
    -moz-user-select: none;
    -ms-user-select: none;
    user-select: none;
    background-image: none;
    border: 1px solid transparent;
    border-radius: 4px;
}
button, input, select, textarea {
    font-family: inherit;
    font-size: inherit;
    line-height: inherit;
}
button, html input[type="button"], input[type="reset"], input[type="submit"] {
    -webkit-appearance: button;
    cursor: pointer;
}
button, select {
    text-transform: none;
}
button {
    overflow: visible;
}
button, input, optgroup, select, textarea {
    margin: 0;
        margin-bottom: 0px;
    font: inherit;
        font-family: inherit;
        font-weight: inherit;
        font-size: inherit;
        line-height: inherit;
    color: inherit;
}
</style>
<script src="skin/defone/js/bootstrap.js"></script>
<script type="text/javascript">
function resizeWindow()
{
	var args = $(".programname");
	var w = windowWidth - 720; 
	for(var i = 0; i < args.length; i++ )
	{
		args[i].style.width = w;
	}
}

function viewServerChart(type){
	var btn = document.getElementById("btnMonitor");
	var c = btn.parentNode.parentNode.parentNode.className;
	if( c.indexOf("collapsed-box") != -1 ){
		btn.click();
	}
	if( type >= 0 ){
		document.getElementById("iMonitor").src = 'monitorload!serverchart.action?id=<ww:property value="id"/>&command='+type;
	}
	else{
		document.getElementById("iMonitor").src = 'monitorload!serverchart.action?id=<ww:property value="id"/>';
	}
}

var ip = "<ww:property value='ip'/>";
var port = <ww:property value='port'/>;
//定时刷新界面
var updating = false;
var count_error = 0;
function updateInfo()
{
	if( updating ) return;
	updating = true;
	var begintime = nowtime();//更新集群状态变化信息出现异常
	MonitorMgr.getSystemPerf("<ww:property value='ip'/>", <ww:property value='port'/>, <ww:property value='id'/>, {
		callback:function(systemPerf) {
			if( systemPerf )
			{
				try
				{
					var tdDatabaseStatus = document.getElementById("tdDatabaseStatus");
					tdDatabaseStatus.innerHTML = systemPerf.databaseStates;
					var table = document.getElementById("tbServerStatus");
					var i = 1;
					document.getElementById("dashC").innerHTML = systemPerf.cpuLoadInfo;
					var cpuLoadInfo = systemPerf.properties["CpuLoadInfo"];
					if( cpuLoadInfo ){
						cpuLoadInfo = cpuLoadInfo.replace(",", "<br/>");
					}
					document.getElementById("dashC0").innerHTML = getStateIcon(systemPerf.cpuState)+cpuLoadInfo;
					
					document.getElementById("dashM").innerHTML = systemPerf.phyMemUsageInof;
					var memLoadInfo = systemPerf.properties["MemLoadInfo"];
					if( memLoadInfo ){
						memLoadInfo = memLoadInfo.replace(",", "<br/>");
					}
					document.getElementById("dashM0").innerHTML = getStateIcon(systemPerf.memoryState)+memLoadInfo;
					
					var ioload = systemPerf.netLoad;
					ioload = ioload.replace("/", "<br/>");
					ioload = ioload.replace("I", "输入");
					ioload = ioload.replace("O", "输出");
					document.getElementById("dashN").innerHTML = ioload;
					document.getElementById("dashN0").innerHTML = getStateIcon(systemPerf.netState)+systemPerf.pingState;
					
					table.rows[i++].cells[1].innerHTML = systemPerf.programInfo;
					table.rows[i++].cells[1].innerHTML = systemPerf.cpuLoadInfo;
					table.rows[i++].cells[1].innerHTML = systemPerf.phyMemUsageInof+"&nbsp;&nbsp;"+
					                                   systemPerf.phyMemUsageDetail;
					table.rows[i++].cells[1].innerHTML = systemPerf.netLoad;
					table.rows[i++].cells[1].innerHTML = systemPerf.IOLoad;
					table.rows[i++].cells[1].innerHTML = systemPerf.diskUsageInfo +"&nbsp;&nbsp;"+
													   systemPerf.diskUsageDetail +"&nbsp;&nbsp;"+
													   systemPerf.storagesInfo;
					document.getElementById("PhysicalMemoryInfo").innerHTML = systemPerf.properties["PhysicalMemoryInfo"];
					document.getElementById("DiskInfo").innerHTML = systemPerf.properties["DiskInfo"]?systemPerf.properties["DiskInfo"]:"";
					document.getElementById("securityKey").innerHTML = systemPerf.securityKey;
					document.getElementById("cosVersion").innerHTML = systemPerf.properties["cos.version"];
					document.getElementById("cosVersion").title = systemPerf.properties["cosVersion"];
					document.getElementById("OSVersion").innerHTML = systemPerf.properties["OSVersion"];
					document.getElementById("OSName").innerHTML = systemPerf.properties["OSName"];
					document.getElementById("systemUpTime").innerHTML = systemPerf.systemUpTime;
					document.getElementById("hostName").innerHTML = systemPerf.hostName;
					document.getElementById("perfTime").innerHTML = "监控连接["+systemPerf.properties["tracking"]+"] 上次监控心跳: "+systemPerf.properties["HeartbeatTime"];
					document.getElementById("HostInfo").innerHTML = systemPerf.properties["HostInfo"]?systemPerf.properties["HostInfo"]:"";
					document.getElementById("programState").src = getClusterStateIcon(systemPerf.programState, systemPerf.properties["Running"]);
					document.getElementById("cpuState").src = getClusterStateIcon(systemPerf.cpuState, systemPerf.properties["Running"]);
					document.getElementById("memoryState").src = getClusterStateIcon(systemPerf.memoryState, systemPerf.properties["Running"]);
					document.getElementById("diskState").src = getClusterStateIcon(systemPerf.diskState, systemPerf.properties["Running"]);
					document.getElementById("netState").src = getClusterStateIcon(systemPerf.netState, systemPerf.properties["Running"]);
					document.getElementById("databaseState").src = getClusterStateIcon(systemPerf.databaseState, systemPerf.properties["Running"]);
					document.getElementById("zookeeperState").src = getClusterStateIcon(systemPerf.zookeeperState, systemPerf.properties["Running"]);
					document.getElementById("coswsState").src = getClusterStateIcon(systemPerf.coswsState, systemPerf.properties["Running"]);
					//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
					var modules = systemPerf.properties["ModuleTrack"];
					var totalMemory = systemPerf.properties["TotalMemeory"];
					document.getElementById("TotalMemeory").innerHTML = totalMemory;
					totalMemory = systemPerf.properties["TotalMemeory0"];
					if( totalMemory ) document.getElementById("TotalMemeory0").innerHTML = totalMemory;
					if( modules )
					{
						var a = setModuleTracker("tbOuter", modules, true);
						document.getElementById("dashP").innerHTML = a+"<span style='font-weight:normal;color:gray;font-size:12px;'>  用户程序</span>";
						setModuleTracker("tbInner", modules, false);
						var programInfo = systemPerf.programInfo;
						programInfo = programInfo.replace(",", "<br/>");
						document.getElementById("dashP0").innerHTML = getStateIcon(systemPerf.programState)+programInfo;
					}
					//----------------------------------------------------------------------------
					table = document.getElementById("tbNetInfo");
					document.getElementById("preIP").innerHTML = systemPerf.properties["IPInfo"];
					for( var i = 1; i < table.rows.length - 1; i++ )
					{//从第一行开始计算
						table.deleteRow(i);
						i -= 1;
					}
					var pings = systemPerf.properties["PING"];
					if( pings )
					{
						for( var i = 0; i < pings.length; i++ )
						{
							var ping = pings[i];
							var newRow = table.insertRow(0);
							cell = newRow.insertCell(0);
							cell.colSpan = 2;
							cell.innerHTML = "<i class='skit_fa_icon fa fa-info-circle'></i>"+ping;
						}
					}
					window.setTimeout("updateInfo()",7000);
					resizeWindow();
				}
				catch(e)
				{
					top.window.skit_alert("解析伺服器监控信息出现异常"+e, "异常提示", function(){
						window.setTimeout("updateInfo()",7000);
					});
				}
			}
			else
			{
				top.window.skit_alert("伺服器未启动或未能正常监控，请检查该服务器工作状态", "异常提示", function(){
					window.setTimeout("updateInfo()",7000);
					updating = false;
				});
			}
			updating = false;
		},
		timeout:24000,
		errorHandler:function(message){
			if( count_error == 10 ){

				top.window.skit_alert("自"+begintime+"更新伺服器监控信息出现异常"+message, "异常提示", function(){
					window.setTimeout("updateInfo()",7000);
					updating = false;
				});
				count_error = 0;
			}
			else{
				if( count_error == 0 ){
					time_error = nowtime();
				}
				count_error += 1;
				window.setTimeout("getClusterStates()",7000);
			}
		}
	});
}
updateInfo();
//设置模块跟踪
var pids = {};
var netstats = {};
function setModuleTracker(tableid, modules, outer)
{
	var runprogram = 0;
	var cfgprogram = 0;
	var table = document.getElementById(tableid);
	for( var i = 1; i < table.rows.length -1; i++ )
	{//从第一行开始计算
		var id = table.rows[i].id;
		var j = -1;
		for( var m in modules )
		{
			var module = modules[m];
			if( outer && module.programmer == "超级管理员" ) continue;
			else if( !outer && module.programmer != "超级管理员" ) continue;
			
			if( module && id == module.id )
			{
				j = m;
				break;
			}
		}
		if( j == -1 )
		{//TR上的数据已经可以删除了，一次删除一个
			table.deleteRow(i);
			i -= 1;
		}
	}
	var html;
	for( var i = 0; i < modules.length; i++ )
	{
		var module = modules[i];
		if( outer && module.programmer == "超级管理员" ) continue;
		else if( !outer && module.programmer != "超级管理员" ) continue;
		if( module.pid ){
			pids[module.id] = JSON.parse(module.pid);
		}
		else{
			pids[module.id] = {};
		}
		if( module.netstat ){
			netstats[module.id] = JSON.parse(module.netstat);
		}
		else{
			netstats[module.id] = {};
		}
		cfgprogram += 1;
		var nn = 0;
		var pid_size = 0, moudle_pids = "";
		var processor = pids[module.id];
		if( processor ){
			for(var pid in processor )
			{
				moudle_pids += ","+pid;
				pid_size += 1;
			}
			if( moudle_pids ){
				moudle_pids = moudle_pids.substring(1);
			}
		}
		var tips = module.remark+"(程序ID:"+module.id+" 进程ID:"+moudle_pids+")";
		var tr = document.getElementById(module.id);
		if( tr )
		{
			tr.cells[nn++].title = module.state;
			var cell = tr.cells[0];
			runprogram += setModuleState(module, cell);
			//tr.cells[nn++].innerHTML = "<span style='width:80px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;list-style-type:none;'>"+module.monitorInfo+"</span>";
			if( pid_size > 1) {
				document.getElementById("iProgramMgr_"+module.id).className = "skit_fa_btn_red fa fa-exclamation-circle";
				if( module.dead ){
					html = "<a onclick='showDeadModuleLog(\""+module.id+"\")' style='cursor:pointer;color:#f1410e'><img src='images/icons/spam.png'>"+module.name+"</a>";
				}
				else{
					html = "<a onclick='viewPid(\""+module.id+"\")' style='cursor:pointer;color:#f1410e'><img src='images/icons/spam.png'>"+module.name+"</a>";
				}
				tips += " 该程序出现"+pid_size+"个进程实例，请联系系统管理员尽快处理.";
			}
			else if( pid_size == 0 && module.dead ){
				if( module.daemon ){
					document.getElementById("iProgramMgr_"+module.id).className = "skit_fa_btn_red fa fa-exclamation-circle";
					html = "<a onclick='showDeadModuleLog(\""+module.id+"\")' style='cursor:pointer;color:#f1410e'><img src='images/icons/spam.png'>"+module.name+"</a>";
					tips += " 该程序是守护进程可能启动出错，请查看相关日志分析并处理.";
				}
				else{
					document.getElementById("iProgramMgr_"+module.id).className = "skit_fa_icon_orange fa fa-warning";
					html = "<a onclick='showDeadModuleLog(\""+module.id+"\")' style='cursor:pointer;color:#fec667'><img src='images/icons/tab_spam.png'>"+module.name+"</a>";
					tips += " 该程序可能没有启动，请查看相关日志分析并处理.";
				}
			}
			else if( pid_size == 1 && module.dead ){
				document.getElementById("iProgramMgr_"+module.id).className = "skit_fa_icon_orange fa fa-warning";
				html = "<a onclick='showDeadModuleLog(\""+module.id+"\")' style='cursor:pointer;color:#fec667'><img src='images/icons/tab_spam.png'>"+module.name+"</a>";
				tips += " 该程序通过正常的关闭信号无法退出被主控引擎强制kill，程序可能没有设计资源释放逻辑，有可能会造成该程序的数据异常，需要开发人员优化.";
			}
			else{
				document.getElementById("iProgramMgr_"+module.id).className = "skit_fa_btn fa fa-info-circle";
				html = "<img src='images/icons/tile.png'>"+module.name;
			}

			if( module.countException > 8 ){
				if( module.expiredException ){
					html += "<a onclick='showExceptionModuleLog(\""+module.id+"\")' style='cursor:pointer;color:#c0c0c0'>"+
						"[<img src='images/icons/gray.png' height='12'>异常]</a>";
					tips += " 该程序一天前出现"+module.countException+"疑似异常日志，请查看相关日志分析并处理.";
				}
				else{
					var ic = "warning";
					if( document.getElementById("iProgramMgr_"+module.id).className.indexOf("_red") != -1 || module.countException > 256 ){
						ic = "danger";
					}
					html += "<a onclick='showExceptionModuleLog(\""+module.id+"\")' style='cursor:pointer;color:#a0a0a0'>"+
						"[<img src='images/icons/"+ic+".gif' height='12'>异常]</a>";
					tips += " 该程序连续出现"+module.countException+"疑似异常日志，请查看相关日志分析并处理.";
				}
			}
			
			tr.cells[nn].innerHTML = html;
			tr.cells[nn++].title = tips;
			tr.cells[nn++].title = module.name+"<ww:text name='label.ema.omt.log'/>";
			tr.cells[nn].innerHTML = module.version;
			tr.cells[nn++].title = module.remark;
			tr.cells[nn++].innerHTML = module.programmer;
			tr.cells[nn++].innerHTML = module.startupTime;
			tr.cells[nn].innerHTML = module.usageMemory;
			tr.cells[nn++].title = module.res;
			//tr.cells[nn++].innerHTML = module.runtime;
			tr.cells[nn++].innerHTML = module.dead?module.dead:"";
			tr.cells[nn++].innerHTML = module.printException?module.printException:"";
		}
		else
		{
			var newRow = table.insertRow(table.rows.length-1);
			newRow.id = module.id;
			newRow.onmouseover = new Function("this.style.backgroundColor='<ww:property value='themeColorLight'/>';");
			newRow.onmouseout = new Function("this.style.backgroundColor='';");
			newRow.onclick = new Function("trProgramId='"+module.id+"';trProgramTitle='"+module.name+"';trProgramCfgfile='"+module.cfgfile+"';");
			var cell = newRow.insertCell(nn++);
			runprogram += setModuleState(module, cell);

			cell = newRow.insertCell(nn++);
			cell.innerHTML = module.name;
			var ico = "skit_fa_btn fa fa-info-circle";
			//if("Nginx-FastDFS-Group2-8000" == module.id){
			//	alert("size_pid="+pid_size+",dead="+module.dead+","+module.countException+",module.daemon="+module.daemon);
			//}
			if( pid_size > 1) {
				ico = "skit_fa_btn_red fa fa-exclamation-circle";
				if( module.dead ){
					html = "<a onclick='showDeadModuleLog(\""+module.id+"\")' style='cursor:pointer;color:#f1410e'><img src='images/icons/spam.png'>"+module.name+"</a>";
				}
				else{
					html = "<a onclick='viewPid(\""+module.id+"\")' style='cursor:pointer;color:#f1410e'><img src='images/icons/spam.png'>"+module.name+"</a>";
				}
				tips += " 该程序出现"+pid_size+"个进程实例，请联系系统管理员尽快处理.";
			}
			else if( pid_size == 0 && module.dead ){
				if( module.daemon ){
					ico = "skit_fa_btn_red fa fa-exclamation-circle";
					html = "<a onclick='showDeadModuleLog(\""+module.id+"\")' style='cursor:pointer;color:#f1410e'><img src='images/icons/spam.png'>"+module.name+"</a>";
					tips += " 该程序是守护进程可能启动出错，请查看相关日志分析并处理.";
				}
				else{
					ico = "skit_fa_icon_orange fa fa-warning";
					html = "<a onclick='showDeadModuleLog(\""+module.id+"\")' style='cursor:pointer;color:#fec667'><img src='images/icons/tab_spam.png'>"+module.name+"</a>";
					tips += " 该程序可能没有启动，请查看相关日志分析并处理.";
				}
			}
			else if( pid_size == 1 && module.dead ){
				ico = "skit_fa_icon_orange fa fa-warning";
				html = "<a onclick='showDeadModuleLog(\""+module.id+"\")' style='cursor:pointer;color:#fec667'><img src='images/icons/tab_spam.png'>"+module.name+"</a>";
				tips += " 该程序通过正常的关闭信号无法退出被主控引擎强制kill，程序可能没有设计资源释放逻辑，有可能会造成该程序的数据异常，需要开发人员优化.";
			}
			else{
				ico = "skit_fa_btn fa fa-info-circle";
				html = "<img src='images/icons/tile.png'>"+module.name;
			}

			if( module.countException > 8 ){
				if( module.expiredException ){
					html += "<a onclick='showExceptionModuleLog(\""+module.id+"\")' style='cursor:pointer;color:#c0c0c0'>"+
						"[<img src='images/icons/gray.png' height='12'>异常]</a>";
					tips += " 该程序一天前出现"+module.countException+"疑似异常日志，请查看相关日志分析并处理.";
				}
				else{
					var ic = "warning";
					if( ico.indexOf("_red") != -1 || module.countException > 256 ){
						ic = "danger";
					}
					html += "<a onclick='showExceptionModuleLog(\""+module.id+"\")' style='cursor:pointer;color:#a0a0a0'>"+
						"[<img src='images/icons/"+ic+".gif' height='12'>异常]</a>";
					tips += " 该程序连续出现"+module.countException+"疑似异常日志，请查看相关日志分析并处理.";
				}
			}
			
			cell.innerHTML = html;
			cell.className = 'programname';
			cell.title = tips;

			cell = newRow.insertCell(nn++);
			cell.innerHTML = "<span class='dropdown'><a onclick='log(this, \""+module.id+"\");' "+
			"class='dropdown-toggle' data-toggle='dropdown' aria-expanded='false'>"+
			"<i class='skit_fa_btn fa fa-file-text'></i></a>"+
			"<ul class='dropdown-menu atl' id='ul_log_"+module.id+"'></ul></span>";
			cell.title = module.name+"<ww:text name='label.ema.omt.log'/>";
			cell.className = 'log';

			cell = newRow.insertCell(nn++);
			cell.innerHTML = module.version;
			cell.title = module.remark;
			cell = newRow.insertCell(nn++);
			
			cell.innerHTML = module.programmer;
			cell.title = "";
			cell = newRow.insertCell(nn++);
			
			cell.innerHTML = module.startupTime;
			cell = newRow.insertCell(nn++);
			
			cell.innerHTML = module.usageMemory;
			cell.title = module.res;
			
			//cell = newRow.insertCell(nn++);
			//cell.innerHTML = module.runtime;

			cell = newRow.insertCell(nn++);
			cell.style.display = "none"
			cell.id = "dead_"+module.id;
			cell.innerHTML = module.dead?module.dead:"";

			cell = newRow.insertCell(nn++);
			cell.style.display = "none"
			cell.id = "exception_"+module.id;
			cell.innerHTML = module.printException?module.printException:"";
			
			cell = newRow.insertCell(nn++);
			cell.align = 'right';
			cell.style.paddingRight = 10;
			var menuhtml = document.getElementById("programomt").innerHTML;
			if( tableid != "tbInner" && <ww:property value='grant'/>)
			{
//				menuhtml = menuhtml+"<li><a onclick='configService();'><i class='skit_fa_icon fa fa-cog'></i> 配置启动指令</a></li>";
			}
			//cell.title = module.id;
			//var html = "<button type='button' class='skit_btn60 dropdown-toggle' data-toggle='dropdown' aria-expanded='false'>"+
			//"<i class='skit_fa_btn fa fa-user'></i><ww:text name='label.ema.sysmsg.operation'/></button>";
			if(module.id=="COSControl"){
				module.type = 1;
				//alert(module.type+":"+document.getElementById("cosVersion").title);
			}
			if( module.type == 1 && document.getElementById("cosVersion").title>="03.18.06.26" ){
				menuhtml += "<li><a onclick='gc();'><i class='skit_fa_icon_red fa fa-stethoscope'></i> 查看JAVA程序GC历史</a></li>";
			}
			if( module.type == 1 && document.getElementById("cosVersion").title>="03.19.07.04" ){
				menuhtml += "<li><a onclick='jstack();'><i class='skit_fa_icon_yellow fa fa-bug'></i> JSTACK(dump线程与内存信息)</a></li>";
				menuhtml += "<li><a onclick='jmap();'><i class='skit_fa_icon_red fa fa-bug'></i> JMAP(dump线程信息)</a></li>";
			}
			menuhtml = "<ul class='dropdown-menu pull-right'>"+menuhtml+"</ul>";
			cell.innerHTML = "<span class='dropdown'><a class='dropdown-toggle' data-toggle='dropdown' aria-expanded='false' style='color:#566B52;TEXT-DECORATION: none'>"+
			"<i class='"+ico+"' id='iProgramMgr_"+module.id+"'></i>程序管理</a>"+menuhtml+"</span>";
		}
	}
	return runprogram+"/"+cfgprogram;
}

function setModuleState(module , cell)
{
	cell.title = module.monitorInfo;
	switch( module.state )
	{
	case 0:
		cell.innerHTML = "<i class='skit_fa_icon_gray fa fa-minus-circle'></i>待机";
		cell.title = module.monitorInfo+"(程序没有启动运行开关或者等待用户进行操作)";
		return 0;
	case 1:
		cell.innerHTML = "<i class='skit_fa_icon fa fa-play-circle-o'>运行";
		cell.title = module.monitorInfo+"(程序运行中)";
		return 1;
	case 2:
		cell.innerHTML = "<i class='skit_fa_icon_red fa fa-stop-circle-o'></i>停止";
		cell.title = module.monitorInfo+"(程序停止中...)";
		return 0;
	case 3:
		cell.innerHTML = "<i class='skit_fa_icon_gray fa fa-stop-circle-o'></i>停止";
		cell.title = module.monitorInfo+"(程序停止运行)";
		return 0;
	case 5:
		cell.innerHTML = "<i class='skit_fa_icon_red fa fa-pause-circle-o'></i>暂停";
		cell.title = module.monitorInfo+"(程序暂停中...)";
		return 0;
	case 6:
		cell.innerHTML = "<i class='skit_fa_icon_war fa fa-pause-circle-o'></i>暂停";
		cell.title = module.monitorInfo+"(程序暂停运行...)";
		return 0;
	case 7:
		cell.innerHTML = "<i class='skit_fa_icon_war fa fa-times-circle-o'></i>关闭";
		cell.title = module.monitorInfo+"(程序模块被关闭)";
		return 0;
	default:
		cell.innerHTML = "<i class='skit_fa_icon_red fa fa-question-circle'></i>未知";
		cell.title = module.monitorInfo+"(程序模块未知情况联系系统管理员)";
		return 0;
	}
}

function getClusterStateIcon(state, running)
{
	if( running )
	{
		switch(state)
		{
		case 0:
			return "images/icons/gray.png";
		case 1:
			return "images/icons/green.png";
		case 2:
			return "images/icons/yellow.png";
		case 3:
			return "images/icons/danger.gif";
		}
	}
	return "images/icons/gray.png"
}

function getStateIcon(state)
{
	switch(state)
	{
	case 1:
		return "<i class='skit_fa_icon fa fa-info-circle'></i>";
	case 2:
		return "<i class='skit_fa_icon_orange fa fa-warning'></i>";
	case 3:
		return "<i class='skit_fa_icon_red fa fa-exclamation-circle'></i>";
	}
	return "<i class='skit_fa_icon_gray fa fa-info-circle'></i>";
}
//#############################################################################
var trProgramId;//操作ID
var trProgramTitle;
var trProgramCfgfile;
function restartup()
{
	if( trProgramId == "COSControl" ){
		window.setTimeout(function(){
			parent.getClusterStates();
		}, 500);
	}
	//document.forms[0].host.value = host;//"127.0.0.1";
	document.getElementById("id").value = trProgramId;
	document.forms[0].command.value = 0;
	document.forms[0].action = "control!program.action";
	document.forms[0].method = "POST";
	document.forms[0].target = "downloadFrame";
	document.forms[0].submit();
}

function suspend()
{
	if( trProgramId == "COSControl" )
	{
		skit_alert("主控引擎不能被暂停。");
		return;
	}
	//document.forms[0].host.value = host;//"127.0.0.1";
	document.getElementById("id").value = trProgramId;
	document.forms[0].command.value = 1;
	document.forms[0].action = "control!program.action";
	document.forms[0].method = "POST";
	document.forms[0].target = "downloadFrame";
	document.forms[0].submit();
}

function clearlogs()
{
	//document.forms[0].host.value = host;//"127.0.0.1";
	document.getElementById("id").value = trProgramId;
	document.forms[0].command.value = 17;
	document.forms[0].action = "control!program.action";
	document.forms[0].method = "POST";
	document.forms[0].target = "downloadFrame";
	document.forms[0].submit();
}

function log(btna, id)
{
	MonitorMgr.getModuleLogs(ip, port, id, {
		callback:function(logs){
			if( logs )
			{
				var ul = document.getElementById("ul_log_"+id);
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
				document.forms[0].target = "downloadFrame";
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

function configService()
{
	if( trProgramId == "COSControl" )
	{
		skit_alert("主控引擎不能被配置。");
		return;
	}
	openView("配置伺服器【"+ip+":"+port+"】程序: "+trProgramTitle+"["+trProgramId+"]", "control!open.action?ip="+ip+"&port="+port+"&id="+trProgramId);	
}

function configProgram(){
	var uid = "files!open.path-<ww:property value='id'/>";
	if( trProgramCfgfile ){
		//alert(trProgramCfgfile);
		//if( i != -1 ){
		//	path = trProgramCfgfile.substring(0, i);
		//}
		//alert(path);
		//setUserActionMemory(uid, path);
		var title = "配置伺服器【"+ip+":"+port+"】程序: "+trProgramTitle+"["+trProgramId+"]";
		var url = "files!edit.action?ip="+ip+"&port="+port+"&id=<ww:property value='id'/>&path="+trProgramCfgfile;
		window.setTimeout("openView('"+title+"','"+url+"');", 500);
	}
	else{
		skit_alert("伺服器【"+ip+":"+port+"】程序: "+trProgramTitle+"["+trProgramId+"] 没有设置配置文件路径");		
	}
}

function debug()
{
	if( trProgramId == "COSControl" )
	{
		skit_alert("主控引擎不能被调测。");
		return;
	}
	openView("调测伺服器【"+ip+":"+port+"】程序: "+trProgramTitle+"["+trProgramId+"]", "rpc!debug.action?ip="+ip+"&port="+port+"&id="+trProgramId);
}

function viewMemory()
{
	var href = "monitorload!modulememory.action?id="+trProgramId+"&ip="+ip+"&port="+port;
	var theme = getUserActionMemory("monitor!modulememory.theme");
	if( !theme ){
		theme = "gray";
	}
	href += "&filetype="+theme;
	openView("查看伺服器【"+ip+":"+port+"】程序内存使用情况: "+trProgramTitle+"["+trProgramId+"]", href);
}

function gc()
{
	var href = "monitorload!gc.action?id="+trProgramId+"&ip="+ip+"&port="+port;
	var theme = getUserActionMemory("monitor!modulememory.theme");
	if( !theme ){
		theme = "dark-unica";
	}
	href += "&filetype="+theme;
	openView("查看伺服器【"+ip+":"+port+"】JAVA程序历史内存GC情况: "+trProgramTitle+"["+trProgramId+"]", href);
}

function viewNetstat(){
	if( netstats[trProgramId] ){
		var netstat = netstats[trProgramId];
		var size = 0;
		for(var pid in netstat )
		{
			size += 1;
		}
		if( size > 1 ){
			window.top.showJson(netstat, "检查伺服器【"+ip+":"+port+"】程序占用端口: "+trProgramTitle+"["+trProgramId+"]存在僵尸进程");
		}
		else if( size > 0 ){
			window.top.showJson(netstat, "检查伺服器【"+ip+":"+port+"】程序占用端口: "+trProgramTitle+"["+trProgramId+"]端口情况");
		}
		else{
			skit_alert("程序["+trProgramId+"]没有网络状态信息数据");
		}
	}
	else{
		skit_alert("程序["+trProgramId+"]没有网络状态信息数据");
	}
}

function viewPid(id){
	if(id){
		trProgramId = id;
	}
	if( pids[trProgramId] ){
		var processor = pids[trProgramId];
		var size = 0;
		for(var pid in processor )
		{
			size += 1;
		}
		if( size > 1 ){
			window.top.showJson(processor, "发现伺服器【"+ip+":"+port+"】程序: "+trProgramTitle+"["+trProgramId+"]存在僵尸进程");
		}
		else if( size > 0 ){
			window.top.showJson(processor, "查看伺服器【"+ip+":"+port+"】程序: "+trProgramTitle+"["+trProgramId+"]进程实例情况");
		}
		else{
			skit_alert("程序["+trProgramId+"]没有进程实例");
		}
	}
	else{
		skit_alert("程序["+trProgramId+"]没有进程信息数据");
	}
}

function jmap()
{
	window.top.skit_frame("control!jmap.action?command=29&ip="+ip+"&port="+port+"&id="+trProgramId, "JMAP["+trProgramTitle+"] 请报仇下载...", 256, 512, true);
}

function jstack()
{
	window.top.skit_frame("control!jstack.action?command=30&ip="+ip+"&port="+port+"&id="+trProgramId, "Jstack["+trProgramTitle+"]", null, null, true);
}

function showExceptionModuleLog(id){
	var data = document.getElementById("exception_"+id).innerHTML;
	window.top.showText(data, "程序["+id+"]被捕获的疑似异常日志", windowWidth -8, windowHeight - 16);
}

function showDeadModuleLog(id)
{
	var data = document.getElementById("dead_"+id).innerHTML;
	window.top.showText(data, "程序["+id+"]接收关闭信号前后日志", windowWidth -8, windowHeight - 16);
}

function showDatabaseStatus(i, title)
{
	var json = document.getElementById("database"+i).innerHTML;
	var data = JSON.parse(json);
	var html = "";
	for(var key in data )
	{
		var val = data[key];
		html += key + ": "+ val + "<br/>";
	}
	skit_message(html, title, 320, 480);
}

function openDatabase(i, title)
{
	var json = document.getElementById("database"+i).innerHTML;
	var data = JSON.parse(json);
	var jurl = data["jdbc.url"];
	var i = jurl.lastIndexOf("?");
	if( i != -1 ) jurl = jurl.substring(0, i);
	var url = "helper!sqlquery.action?driverClass=";
	url += data["jdbc.driver"];
	url += "&jdbcUsername=";
	url += data["jdbc.username"];
	url += "&jdbcUserpswd=";
	url += data["jdbc.password"];
	url += "&jdbcUrl=";
	url += jurl;
	openView(title, url);
}

if("undefined"==typeof jQuery)throw new Error("AdminLTE requires jQuery");+function(a){"use strict";function b(b){return this.each(function(){var e=a(this),f=e.data(c);if(!f){var h=a.extend({},d,e.data(),"object"==typeof b&&b);e.data(c,f=new g(h))}if("string"==typeof b){if(void 0===f[b])throw new Error("No method named "+b);f[b]()}})}var c="lte.layout",d={slimscroll:!0,resetHeight:!0},e={wrapper:".wrapper",contentWrapper:".content-wrapper",layoutBoxed:".layout-boxed",mainFooter:".main-footer",mainHeader:".main-header",sidebar:".sidebar",controlSidebar:".control-sidebar",fixed:".fixed",sidebarMenu:".sidebar-menu",logo:".main-header .logo"},f={fixed:"fixed",holdTransition:"hold-transition"},g=function(a){this.options=a,this.bindedResize=!1,this.activate()};g.prototype.activate=function(){this.fix(),this.fixSidebar(),a("body").removeClass(f.holdTransition),this.options.resetHeight&&a("body, html, "+e.wrapper).css({height:"auto","min-height":"100%"}),this.bindedResize||(a(window).resize(function(){this.fix(),this.fixSidebar(),a(e.logo+", "+e.sidebar).one("webkitTransitionEnd otransitionend oTransitionEnd msTransitionEnd transitionend",function(){this.fix(),this.fixSidebar()}.bind(this))}.bind(this)),this.bindedResize=!0),a(e.sidebarMenu).on("expanded.tree",function(){this.fix(),this.fixSidebar()}.bind(this)),a(e.sidebarMenu).on("collapsed.tree",function(){this.fix(),this.fixSidebar()}.bind(this))},g.prototype.fix=function(){a(e.layoutBoxed+" > "+e.wrapper).css("overflow","hidden");var b=a(e.mainFooter).outerHeight()||0,c=a(e.mainHeader).outerHeight()+b,d=a(window).height(),g=a(e.sidebar).height()||0;if(a("body").hasClass(f.fixed))a(e.contentWrapper).css("min-height",d-b);else{var h;d>=g?(a(e.contentWrapper).css("min-height",d-c),h=d-c):(a(e.contentWrapper).css("min-height",g),h=g);var i=a(e.controlSidebar);void 0!==i&&i.height()>h&&a(e.contentWrapper).css("min-height",i.height())}},g.prototype.fixSidebar=function(){if(!a("body").hasClass(f.fixed))return void(void 0!==a.fn.slimScroll&&a(e.sidebar).slimScroll({destroy:!0}).height("auto"));this.options.slimscroll&&void 0!==a.fn.slimScroll&&(a(e.sidebar).slimScroll({destroy:!0}).height("auto"),a(e.sidebar).slimScroll({height:a(window).height()-a(e.mainHeader).height()+"px",color:"rgba(0,0,0,0.2)",size:"3px"}))};var h=a.fn.layout;a.fn.layout=b,a.fn.layout.Constuctor=g,a.fn.layout.noConflict=function(){return a.fn.layout=h,this},a(window).on("load",function(){b.call(a("body"))})}(jQuery),function(a){"use strict";function b(b){return this.each(function(){var e=a(this),f=e.data(c);if(!f){var g=a.extend({},d,e.data(),"object"==typeof b&&b);e.data(c,f=new h(g))}"toggle"==b&&f.toggle()})}var c="lte.pushmenu",d={collapseScreenSize:767,expandOnHover:!1,expandTransitionDelay:200},e={collapsed:".sidebar-collapse",open:".sidebar-open",mainSidebar:".main-sidebar",contentWrapper:".content-wrapper",searchInput:".sidebar-form .form-control",button:'[data-toggle="push-menu"]',mini:".sidebar-mini",expanded:".sidebar-expanded-on-hover",layoutFixed:".fixed"},f={collapsed:"sidebar-collapse",open:"sidebar-open",mini:"sidebar-mini",expanded:"sidebar-expanded-on-hover",expandFeature:"sidebar-mini-expand-feature",layoutFixed:"fixed"},g={expanded:"expanded.pushMenu",collapsed:"collapsed.pushMenu"},h=function(a){this.options=a,this.init()};h.prototype.init=function(){(this.options.expandOnHover||a("body").is(e.mini+e.layoutFixed))&&(this.expandOnHover(),a("body").addClass(f.expandFeature)),a(e.contentWrapper).click(function(){a(window).width()<=this.options.collapseScreenSize&&a("body").hasClass(f.open)&&this.close()}.bind(this)),a(e.searchInput).click(function(a){a.stopPropagation()})},h.prototype.toggle=function(){var b=a(window).width(),c=!a("body").hasClass(f.collapsed);b<=this.options.collapseScreenSize&&(c=a("body").hasClass(f.open)),c?this.close():this.open()},h.prototype.open=function(){a(window).width()>this.options.collapseScreenSize?a("body").removeClass(f.collapsed).trigger(a.Event(g.expanded)):a("body").addClass(f.open).trigger(a.Event(g.expanded))},h.prototype.close=function(){a(window).width()>this.options.collapseScreenSize?a("body").addClass(f.collapsed).trigger(a.Event(g.collapsed)):a("body").removeClass(f.open+" "+f.collapsed).trigger(a.Event(g.collapsed))},h.prototype.expandOnHover=function(){a(e.mainSidebar).hover(function(){a("body").is(e.mini+e.collapsed)&&a(window).width()>this.options.collapseScreenSize&&this.expand()}.bind(this),function(){a("body").is(e.expanded)&&this.collapse()}.bind(this))},h.prototype.expand=function(){setTimeout(function(){a("body").removeClass(f.collapsed).addClass(f.expanded)},this.options.expandTransitionDelay)},h.prototype.collapse=function(){setTimeout(function(){a("body").removeClass(f.expanded).addClass(f.collapsed)},this.options.expandTransitionDelay)};var i=a.fn.pushMenu;a.fn.pushMenu=b,a.fn.pushMenu.Constructor=h,a.fn.pushMenu.noConflict=function(){return a.fn.pushMenu=i,this},a(document).on("click",e.button,function(c){c.preventDefault(),b.call(a(this),"toggle")}),a(window).on("load",function(){b.call(a(e.button))})}(jQuery),function(a){"use strict";function b(b){return this.each(function(){var e=a(this);if(!e.data(c)){var f=a.extend({},d,e.data(),"object"==typeof b&&b);e.data(c,new h(e,f))}})}var c="lte.tree",d={animationSpeed:500,accordion:!0,followLink:!1,trigger:".treeview a"},e={tree:".tree",treeview:".treeview",treeviewMenu:".treeview-menu",open:".menu-open, .active",li:"li",data:'[data-widget="tree"]',active:".active"},f={open:"menu-open",tree:"tree"},g={collapsed:"collapsed.tree",expanded:"expanded.tree"},h=function(b,c){this.element=b,this.options=c,a(this.element).addClass(f.tree),a(e.treeview+e.active,this.element).addClass(f.open),this._setUpListeners()};h.prototype.toggle=function(a,b){var c=a.next(e.treeviewMenu),d=a.parent(),g=d.hasClass(f.open);d.is(e.treeview)&&(this.options.followLink&&"#"!=a.attr("href")||b.preventDefault(),g?this.collapse(c,d):this.expand(c,d))},h.prototype.expand=function(b,c){var d=a.Event(g.expanded);if(this.options.accordion){var h=c.siblings(e.open),i=h.children(e.treeviewMenu);this.collapse(i,h)}c.addClass(f.open),b.slideDown(this.options.animationSpeed,function(){a(this.element).trigger(d)}.bind(this))},h.prototype.collapse=function(b,c){var d=a.Event(g.collapsed);b.find(e.open).removeClass(f.open),c.removeClass(f.open),b.slideUp(this.options.animationSpeed,function(){b.find(e.open+" > "+e.treeview).slideUp(),a(this.element).trigger(d)}.bind(this))},h.prototype._setUpListeners=function(){var b=this;a(this.element).on("click",this.options.trigger,function(c){b.toggle(a(this),c)})};var i=a.fn.tree;a.fn.tree=b,a.fn.tree.Constructor=h,a.fn.tree.noConflict=function(){return a.fn.tree=i,this},a(window).on("load",function(){a(e.data).each(function(){b.call(a(this))})})}(jQuery),function(a){"use strict";function b(b){return this.each(function(){var e=a(this),f=e.data(c);if(!f){var g=a.extend({},d,e.data(),"object"==typeof b&&b);e.data(c,f=new h(e,g))}"string"==typeof b&&f.toggle()})}var c="lte.controlsidebar",d={slide:!0},e={sidebar:".control-sidebar",data:'[data-toggle="control-sidebar"]',open:".control-sidebar-open",bg:".control-sidebar-bg",wrapper:".wrapper",content:".content-wrapper",boxed:".layout-boxed"},f={open:"control-sidebar-open",fixed:"fixed"},g={collapsed:"collapsed.controlsidebar",expanded:"expanded.controlsidebar"},h=function(a,b){this.element=a,this.options=b,this.hasBindedResize=!1,this.init()};h.prototype.init=function(){a(this.element).is(e.data)||a(this).on("click",this.toggle),this.fix(),a(window).resize(function(){this.fix()}.bind(this))},h.prototype.toggle=function(b){b&&b.preventDefault(),this.fix(),a(e.sidebar).is(e.open)||a("body").is(e.open)?this.collapse():this.expand()},h.prototype.expand=function(){this.options.slide?a(e.sidebar).addClass(f.open):a("body").addClass(f.open),a(this.element).trigger(a.Event(g.expanded))},h.prototype.collapse=function(){a("body, "+e.sidebar).removeClass(f.open),a(this.element).trigger(a.Event(g.collapsed))},h.prototype.fix=function(){a("body").is(e.boxed)&&this._fixForBoxed(a(e.bg))},h.prototype._fixForBoxed=function(b){b.css({position:"absolute",height:a(e.wrapper).height()})};var i=a.fn.controlSidebar;a.fn.controlSidebar=b,a.fn.controlSidebar.Constructor=h,a.fn.controlSidebar.noConflict=function(){return a.fn.controlSidebar=i,this},a(document).on("click",e.data,function(c){c&&c.preventDefault(),b.call(a(this),"toggle")})}(jQuery),function(a){"use strict";function b(b){return this.each(function(){var e=a(this),f=e.data(c);if(!f){var g=a.extend({},d,e.data(),"object"==typeof b&&b);e.data(c,f=new h(e,g))}if("string"==typeof b){if(void 0===f[b])throw new Error("No method named "+b);f[b]()}})}var c="lte.boxwidget",d={animationSpeed:500,collapseTrigger:'[data-widget="collapse"]',removeTrigger:'[data-widget="remove"]',collapseIcon:"fa-window-minimize",expandIcon:"fa-window-maximize",removeIcon:"fa-times"},e={data:".box",collapsed:".collapsed-box",body:".box-body",footer:".box-footer",tools:".box-tools"},f={collapsed:"collapsed-box"},g={collapsed:"collapsed.boxwidget",expanded:"expanded.boxwidget",removed:"removed.boxwidget"},h=function(a,b){this.element=a,this.options=b,this._setUpListeners()};h.prototype.toggle=function(){a(this.element).is(e.collapsed)?this.expand():this.collapse()},h.prototype.expand=function(){var b=a.Event(g.expanded),c=this.options.collapseIcon,d=this.options.expandIcon;a(this.element).removeClass(f.collapsed),a(this.element).find(e.tools).find("."+d).removeClass(d).addClass(c),a(this.element).find(e.body+", "+e.footer).slideDown(this.options.animationSpeed,function(){a(this.element).trigger(b)}.bind(this))},h.prototype.collapse=function(){var b=a.Event(g.collapsed),c=this.options.collapseIcon,d=this.options.expandIcon;a(this.element).find(e.tools).find("."+c).removeClass(c).addClass(d),a(this.element).find(e.body+", "+e.footer).slideUp(this.options.animationSpeed,function(){a(this.element).addClass(f.collapsed),a(this.element).trigger(b)}.bind(this))},h.prototype.remove=function(){var b=a.Event(g.removed);a(this.element).slideUp(this.options.animationSpeed,function(){a(this.element).trigger(b),a(this.element).remove()}.bind(this))},h.prototype._setUpListeners=function(){var b=this;a(this.element).on("click",this.options.collapseTrigger,function(a){a&&a.preventDefault(),b.toggle()}),a(this.element).on("click",this.options.removeTrigger,function(a){a&&a.preventDefault(),b.remove()})};var i=a.fn.boxWidget;a.fn.boxWidget=b,a.fn.boxWidget.Constructor=h,a.fn.boxWidget.noConflict=function(){return a.fn.boxWidget=i,this},a(window).on("load",function(){a(e.data).each(function(){b.call(a(this))})})}(jQuery),function(a){"use strict";function b(b){return this.each(function(){var e=a(this),f=e.data(c);if(!f){var h=a.extend({},d,e.data(),"object"==typeof b&&b);e.data(c,f=new g(e,h))}if("string"==typeof f){if(void 0===f[b])throw new Error("No method named "+b);f[b]()}})}var c="lte.todolist",d={iCheck:!1,onCheck:function(){},onUnCheck:function(){}},e={data:'[data-widget="todo-list"]'},f={done:"done"},g=function(a,b){this.element=a,this.options=b,this._setUpListeners()};g.prototype.toggle=function(a){if(a.parents(e.li).first().toggleClass(f.done),!a.prop("checked"))return void this.unCheck(a);this.check(a)},g.prototype.check=function(a){this.options.onCheck.call(a)},g.prototype.unCheck=function(a){this.options.onUnCheck.call(a)},g.prototype._setUpListeners=function(){var b=this;a(this.element).on("change ifChanged","input:checkbox",function(){b.toggle(a(this))})};var h=a.fn.todoList;a.fn.todoList=b,a.fn.todoList.Constructor=g,a.fn.todoList.noConflict=function(){return a.fn.todoList=h,this},a(window).on("load",function(){a(e.data).each(function(){b.call(a(this))})})}(jQuery),function(a){"use strict";function b(b){return this.each(function(){var d=a(this),e=d.data(c);e||d.data(c,e=new f(d)),"string"==typeof b&&e.toggle(d)})}var c="lte.directchat",d={data:'[data-widget="chat-pane-toggle"]',box:".direct-chat"},e={open:"direct-chat-contacts-open"},f=function(a){this.element=a};f.prototype.toggle=function(a){a.parents(d.box).first().toggleClass(e.open)};var g=a.fn.directChat;a.fn.directChat=b,a.fn.directChat.Constructor=f,a.fn.directChat.noConflict=function(){return a.fn.directChat=g,this},a(document).on("click",d.data,function(c){c&&c.preventDefault(),b.call(a(this),"toggle")})}(jQuery);

function collapseBox(btn, type){
	try{
		var c = btn.parentNode.parentNode.parentNode.className;
		if( c.indexOf("collapsed-box") == -1 ){
			c = true;
		}
		else{
			c = false;
		}
		var flag = "monitor!server.collapsed."+type;
		setUserActionMemory(flag, c);
	}
	catch(e){
		alert(e);
	}
}

$(document).ready(function(){
	try
	{
		var collapsed = getUserActionMemory("monitor!server.collapsed.monitor", true);
		if( collapsed ){
			document.getElementById("btnMonitor").innerHTML = '<i class="fa fa-window-maximize"></i>';
			document.getElementById("bodyMonitor").style.display = "none";
			document.getElementById("boxMonitor").className = "box box-success collapsed-box";
		}
		else{
			document.getElementById("btnMonitor").innerHTML = '<i class="fa fa-window-minimize"></i>';
			document.getElementById("bodyMonitor").style.display = "";
			document.getElementById("boxMonitor").className = "box box-success";
			viewServerChart(1);
		}
		
		collapsed = getUserActionMemory("monitor!server.collapsed.inner", true);
		if( collapsed ){
			document.getElementById("btnInner").innerHTML = '<i class="fa fa-window-maximize"></i>';
			document.getElementById("bodyInner").style.display = "none";
			document.getElementById("boxInner").className = "box box-success collapsed-box";
		}
		else{
			document.getElementById("btnInner").innerHTML = '<i class="fa fa-window-minimize"></i>';
			document.getElementById("bodyInner").style.display = "";
			document.getElementById("boxInner").className = "box box-success";
		}
	}
	catch(e)
	{
		alert(e);
	}
});
</script>