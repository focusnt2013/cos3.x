<%@ page language="java" pageEncoding="UTF-8"%>
<%@ page import="com.focus.cos.web.common.Kit"%>
<%@ taglib uri="/webwork" prefix="ww" %>
<%Kit.noCache(request,response);  %>
<html>
<head>
<link type="text/css" href="skit/css/bootstrap.css" rel="stylesheet">
<link type="text/css" href="skit/css/costable.css" rel="stylesheet">
<style type='text/css'>
</style>
<%=Kit.getDwrJsTag(request,"interface/FilesMgr.js")%>
<%=Kit.getDwrJsTag(request,"engine.js")%>
<%=Kit.getDwrJsTag(request,"util.js")%>
</head>
<body style='overflow-y:hidden;padding-top:0px;padding-left:0px;'>
</body>
<%@ include file="../../skit/inc/skit_cos.inc"%>
<style type='text/css'>
table {
	width: 100%;
	border: 0xp;
}
.filename {
 font-size: 12px;
 font-family: "微软雅黑",sans-serif;
 display:block;
 width:310px;
 word-break:keep-all;
 white-space:nowrap;
 overflow:hidden;
 text-overflow:ellipsis;
}
</style>
<SCRIPT type="text/javascript">
var ip = "<ww:property value='ip'/>";
var port = <ww:property value='port'/>;
$(document).ready(function(){
	try
	{
		var data = <ww:property value="jsonData" escape="false"/>;
		showfiles(data);
	}
	catch(e)
	{
		alert("初始化文件列表异常"+e.message+", 行数"+e.lineNumber);
	}
});
function showfiles(json)
{
	var files = json.files;
	var summary = json.summary;
	//showObject(json);
	var table = document.getElementById( 'tableFiles' );
	while( table.rows.length > 2 )
	{
		table.deleteRow(1);
	}
	var fetchArray = new Array();
	var tdArray = new Array();
	var fileArray = new Array();
	for(var i = 0; i < files.length; i++)
	{
		var file = files[i];
		var path = file.path;
		if( file.rootdir ) path = file.rootdir+path;
		var tr = table.insertRow(i+1);
		tr.onmouseover = new Function("this.style.backgroundColor='<ww:property value='themeColorLight'/>';");
		tr.onmouseout = new Function("this.style.backgroundColor='';");
		var td = tr.insertCell(0);
		var atip = file.isParent?"点击打开文件夹":"点击预览或下载";
		var ico = file.isParent?"<i class='fa fa-folder'></i> ":"<i class='fa fa-file-text'></i> ";
		var filetime = file.time?file.time:"";
		td.innerHTML = "<a onclick='openFile(\""+file.path+"\", "+file.isParent+", "+file.length+", \""+filetime+"\")' title='"+atip+"' class='tablea filename'>"+ico+file.name+"</a>";
		td.title = file.name;
		td = tr.insertCell(1);
		td.className = "weaken";
		td.innerHTML = filetime;
		td.title = filetime;
		td = tr.insertCell(2);
		td.className = "weaken";
		td.title = path;
		if( file.isParent )
		{
			td.innerHTML = "<i class='fa fa-folder-o'></i> 文件夹";
		}
		else
		{
			if( file.typeico )
			{
				td.innerHTML = "<i class='fa fa-"+file.typeico+"'></i> "+file.type;
			}
			else
			{
				td.innerHTML = "<i class='fa fa-file-o'></i>";
				tdArray.push(td);
				fetchArray.push(path);
				fileArray.push(file);
			}
		}
		td = tr.insertCell(3);
		td.className = "weaken";
		td.innerHTML = file.isParent?"":file.size;
		td.title = file.length;
	}
	var args = $(".filename");
	var w = document.getElementById( 'tdName' ).clientWidth; 
	for(var i = 0; i < args.length; i++ )
	{
		args[i].style.width = w - 30;
	}
	document.getElementById( 'spanFileCount' ).innerHTML = summary.filecount;
	document.getElementById( 'spanItemCount' ).innerHTML = summary.itemcount;
	document.getElementById( 'tdSumSize' ).innerHTML = summary.size;
}

var fileTypes = new Object();
function getContentType(fetchArray, tdArray, fileArray)
{
	if( fetchArray.length == 0 ) return;
	FilesMgr.getContentType(ip, port, fetchArray, {
		callback:function(rsp) {
			skit_hiddenLoading();
			try
			{
				if( rsp.succeed )
				{
					var types = jQuery.parseJSON(rsp.result);
					for(var i = 0; i < types.length; i++)
					{
						var type = types[i];
						var file = fileArray[i];
						var td = tdArray[i];
						file["type"] = type.description;
						file["typeico"] = type.icon;
						fileTypes[file.path] = file;
						td.innerHTML = "<i class='fa fa-"+file.typeico+"'></i> "+file.type;
					}
				}
				else
				{
					skit_alert(rsp.message);
				}
			}
			catch(e)
			{
				alert("获取文件的类型出现异常"+e.message+", 行数"+e.lineNumber);
			}
		},
		timeout:120000,
		errorHandler:function(err) {skit_hiddenLoading(); alert(err); }
	});
}
</SCRIPT>
</html>