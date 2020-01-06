<%@ page language="java" pageEncoding="UTF-8"%>
<%@ page import="com.focus.cos.web.common.Kit"%>
<%@ taglib uri="/webwork" prefix="ww" %>
<%Kit.noCache(request,response);  %>
<html>
<head>
<link type="text/css" href="skit/ztree/css/zTreeStyle/zTreeStyle.css" rel="stylesheet"/>
<link href="skit/css/awesome-bootstrap-checkbox.css" rel="stylesheet">
<link type="text/css" href="skit/css/costable.css" rel="stylesheet">
<style type='text/css'>
.form-control:hover{
	cursor: pointer;
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
</style>
<SCRIPT type="text/javascript">
</SCRIPT>
</head>
<body>
<form>
<input type='hidden' name='filenames' id='filenames' value='<ww:property value='filenames'/>'>
<input type='hidden' name='ip' id='ip' value='<ww:property value='ip'/>'>
<input type='hidden' name='port' id='port' value='<ww:property value='port'/>'>
<input type='hidden' name='path' id='path' value='<ww:property value='path'/>'>
<input type='hidden' name='rootdir' id='rootdir' value='<ww:property value='rootdir'/>'>
<input type='hidden' name='srcpath' id='srcpath'>
<input type='hidden' name='destserver' id='destserver'>
<TABLE style='width:100%;'>
<TR class='unline'><TD style='padding-top: 3px;padding-bottom: 3px;'>
	<div class="input-group custom-search-form">
		<div class="input-group-btn">
            <button type="button" class="btn btn-info dropdown-toggle" data-toggle="dropdown" aria-expanded="false"
            	style='display: inline-block;height:28px;' id='btnAddr'><span class="caret"></span></button>
		    <ul class="dropdown-menu pull-left" style='width:300px' id='ulGopathMemory'>
		        <li id='liDefault'><a onclick='document.getElementById("path").value="";' style='font-size:12px;'>缺省工作目录</a></li>
		    </ul>
		</div>
		<span class="input-group-addon" style='font-size:12px;border-right:0px solid red;'>拷贝路径：</span>
	    <input class="form-control" id='destpath' placeholder="缺省工作目录<ww:property value='rootdir'/>的相对路径"
	    	 name='destpath' type="text" style='font-size: 12px;height:28px;padding-top:3px;padding-bottom:3px;'>
	</div>
</TD></TR>
<TR class='unline'><TD>
    <div class="panel panel-default" style='border: 1px solid #aaaaaa'>
		<div class="panel-heading"><i class='skit_fa_btn fa fa-server'></i> 你可以将拷贝文件或文件夹复制到多台伺服器。拷贝的文件或文件夹只允许拷贝到【缺省工作目录】</div>
		<div class="panel-body" style='padding: 0px;'>
			<div id='divTree'><ul id='myZtree' class='ztree'></ul></div>
		</div>
	</div>
</TD></TR>
<TR class='unline'><TD>
<div style='width:280px;' id='divSet'>
	<button type="button" class="btn btn-outline btn-success btn-block"
		style='width:128px;float:left;padding-right:10px;' onclick='doCopy();'><i class="fa fa-save"></i> 确定</button>
	<button type="button" class="btn btn-outline btn-danger btn-block"
		style='width:128px;padding-left:10px;float:right;margin-top:0px;' onclick='parent.closeprecopy();'><i class="fa fa-sign-out"></i> 取消</button>
</div>
</TD></TR>
</TABLE>
</form>
</body>
<SCRIPT type="text/javascript">
/*实现窗口对齐*/
function resizeWindow()
{
	var div = document.getElementById('divTree');
	div.style.height = windowHeight - 128;
}
function doCopy()
{
	try
	{
		var path = document.getElementById('path');
		var pathname = path.value.substring(path.value.lastIndexOf('/')+1);
		var rootdir = document.getElementById('rootdir');
		var destpath = document.getElementById('destpath').value;
		if( destpath.charAt(0) == '/' )
		{
			skit_alert("文件或文件夹拷贝只能到【缺省工作目录】，请输入【缺省工作目录】的相对路径");
			return;
		}
		var srcpath = rootdir.value+path.value;
		destpath = rootdir.value+destpath;
		if( destpath.charAt(destpath.length-1) != '/' ) destpath += "/";
		if( srcpath == destpath+pathname )
		{
			skit_alert("输入的拷贝路径与拷贝文件或文件夹路径一致，不允许覆盖拷贝。");
			return;
		}
		skit_confirm("您确定要拷贝文件或文件夹'"+path.value+"'到选择的伺服器【缺省工作目录】'"+document.getElementById('destpath').value+"'吗？", function(yes){
			if( yes )
			{
				var nodes = $.fn.zTree.getZTreeObj("myZtree").getNodesByFilter(filter);
				var destserver = "";
				for(var i = 0; i < nodes.length; i++ )
				{
					var node = nodes[i];
					destserver += node.id+",";
				}
				document.getElementById('destpath').value = destpath;
				document.getElementById("srcpath").value = srcpath;
				document.getElementById("destserver").value = destserver;
				//alert(srcpath+":"+destserver);
				document.forms[0].action = "files!copy.action";
				document.forms[0].method = "POST";
				document.forms[0].submit();
			}
		});
	}
	catch(e)
	{
		skit_alert(e);
	}
}
function filter(node) {
    return node.checked;
}
</SCRIPT>
<%@ include file="../../skit/inc/skit_cos.inc"%>
<%@ include file="../../skit/inc/skit_ztree.inc"%>
<SCRIPT type="text/javascript">
var divSet = document.getElementById( 'divSet' );
divSet.style.marginLeft = windowWidth/2 - 140;

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

var myZtree;
var setting = {
	view: {
		dblClickExpand: false
	},
	check: {
		enable: true,
		chkStyle: "checkbox",
		radioType: "level"
	},
	callback: {
		onClick: onSelectServer,
		onCheck: onSelectServer
	}
};

function onSelectServer(e, treeId, node) {
}

$(document).ready(function(){
	var json = '<ww:property value="jsonData" escape="false"/>';
	$.fn.zTree.init($("#myZtree"), setting, jQuery.parseJSON(json));
	myZtree = $.fn.zTree.getZTreeObj("myZtree");
	expandAll(true);
});
</SCRIPT>
</html>