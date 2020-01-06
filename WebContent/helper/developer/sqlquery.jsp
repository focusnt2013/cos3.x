<%@ page language="java" pageEncoding="UTF-8"%>
<%@ page import="com.focus.cos.web.common.Kit"%>
<%@ taglib uri="/webwork" prefix="ww" %>
<%Kit.noCache(request,response); %>
<html>
<head>
<style type='text/css'>
</style>
<script type="text/javascript">
function executeSql(sql)
{
	if( sql ){
		document.getElementById("sql").value = sql;
	}
	else
	{
	    var ifr = window.frames["iSql"];
	    if( ifr && ifr.getValue )
	    {
	    	document.getElementById("sql").value = ifr.getValue();
	    }
		sql = document.getElementById("sql").value;
	}
	if( sql == "" )
	{
		previewDatabase();
		return;
	}
	document.forms[0].action = "helper!sql.action";
	document.forms[0].target = "iConnection";
	document.forms[0].submit();
}

function previewDatabase()
{
	document.forms[0].action = "helper!database.action";
	document.forms[0].target = "iConnection";
	document.forms[0].submit();
}

function executeSnapshot()
{
	var sql = document.getElementById("sql");
	if( sql.value == "" )
	{
		skit_alert("请输入你要快照的SQL语句，快照只支持Select。");
		return;
	}
	document.forms[0].action = "helper!snapshot.action?datatype=sql";
	document.forms[0].target = "iConnection";
	document.forms[0].submit();
}
</script>
</head>
<body style="overflow-x:hidden">
<form action="#" method="post" enctype='application/x-www-form-urlencoded'>
<input type='hidden' name='jdbcUrl' value="<ww:property value='jdbcUrl'/>">
<input type='hidden' name='jdbcUsername' value="<ww:property value='jdbcUsername'/>">
<input type='hidden' name='jdbcUserpswd' value="<ww:property value='jdbcUserpswd'/>">
<input type='hidden' name='driverClass' value="<ww:property value='driverClass'/>">
<input type='hidden' name='db' id='db' value="<ww:property value='db'/>">
<TABLE style='width:100%'>
<TR><TD>
     <div class="panel panel-default" style='border: 1px solid #aaaaaa;margin-bottom:3px;'>
		<div class="panel-heading"><i class='skit_fa_btn fa fa-bars'></i>请输入SQL语句在本地数据库执行
               <div style='float:right;right:4px;top:0px;display:'>
				<button type="button" class='btn btn-outline btn-primary btn-xs' onclick='executeSql();'>
					<i class='fa fa-save'></i> 执行SQL</button>
               </div>
               <div style='float:right;padding-right:4px;top:0px;display:'>
				<button type="button" class="btn btn-outline btn-info btn-xs" onclick='previewDatabase();'>
					<i class='fa fa-eye'></i> 看数据库</button>
               </div>
		</div>
 		<div class="panel-body" style='padding: 0px;'>
 			<iframe src='editor!sql.action' name='iSql' id='iSql' class='nonicescroll' style='width:100%;height:80px;border:0px solid #ccc;'></iframe>
			<textarea name='sql' id='sql' style='display:none'><ww:property value='sql'/></textarea>
		</div>
	</div>
	</TD>
</TR>
<TR><TD><iframe name='iConnection' id='iConnection' class='nonicescroll' style='width:100%;border:0px solid #ccc;'></iframe></TD></TR>
</TABLE>
</form>
</body>
<SCRIPT type="text/javascript">
/*实现窗口对齐*/
function resizeWindow()
{
	var div = document.getElementById( 'iConnection' );
	div.style.height = windowHeight - 128;
}
</SCRIPT>
<%@ include file="../../skit/inc/skit_bootstrap.inc"%>
<%@ include file="../../skit/inc/skit_cos.inc"%>
<SCRIPT type="text/javascript">
executeSql();
document.onkeydown = function(event) { 
	var e = event ? event :(window.event ? window.event : null); 
	if(e.keyCode==13){ 
		//执行的方法 
        executeSql();
	} 
};
</SCRIPT>
</html>