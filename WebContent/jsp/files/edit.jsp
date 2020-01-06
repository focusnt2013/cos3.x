<%@ page language="java" pageEncoding="UTF-8"%>
<%@ page import="com.focus.cos.web.common.Kit"%>
<%@ taglib uri="/webwork" prefix="ww" %>
<%Kit.noCache(request,response);  %>
<html>
<head>
<link type="text/css" href="skit/ztree/css/zTreeStyle/zTreeStyle.css" rel="stylesheet"/>
<link type="text/css" href="skit/css/bootstrap.css" rel="stylesheet">
<link type="text/css" href="skit/css/costable.css" rel="stylesheet">
<link type="text/css" href="skit/css/bootstrap-tour.min.css" rel="stylesheet">
<style type='text/css'>
.skit_mask {
   visibility: hidden;
   background-color: #92A3B2;
   left: 0px;
   position: absolute;
   top: 0px;
   background-image: none;
   BORDER-TOP: 1px solid #405B40;
   BORDER-LEFT: 1px solid #405B40;
   BORDER-RIGHT: 1px solid #405B40;
   BORDER-BOTTOM: 1px solid #405B40;
   /* older safari/Chrome browsers */  
   -webkit-opacity: 0.5;  
   /* Netscape and Older than Firefox 0.9 */  
   -moz-opacity: 0.5;  
   /* Safari 1.x (pre WebKit!) 老式khtml内核的Safari浏览器*/  
   -khtml-opacity: 0.5;  
   /* IE9 + etc...modern browsers */  
   opacity: .5;  
   /* IE 4-9 */  
   filter:alpha(opacity=50);  
   /*This works in IE 8 & 9 too*/  
   -ms-filter:"progid:DXImageTransform.Microsoft.Alpha(Opacity=50)";  
   /*IE4-IE9*/  
   filter:progid:DXImageTransform.Microsoft.Alpha(Opacity=50);  
}
</style>
<%=Kit.getDwrJsTag(request,"interface/FilesMgr.js")%>
<%=Kit.getDwrJsTag(request,"engine.js")%>
<%=Kit.getDwrJsTag(request,"util.js")%>
</head>
<body style='overflow-y:hidden;padding-top:0px;padding-left:0px;'>
<TABLE style='margin-top: 3px;'>
<TR class='unline'><TD width='250'>
        <div class="panel panel-default">
   			<div class="panel-heading" style='font-size:12px;padding:5px 10px'><i class='skit_fa_btn fa fa-bars'></i> 文件树</div>
   			<div class="panel-body" style='padding: 0px;'>
   				<div id='divTree'>
					<ul id='myZtree' class='ztree'></ul>
					<div id="rMenu">
						<ul style='font-size:12px;'>
							<li onclick="mkdir();" id='liMkdir'><i class='skit_fa_icon fa fa-plus-circle'></i> 创建文件夹</li>
							<li onclick="mkfile();" id='liMkfile'><i class='skit_fa_icon fa fa-plus-circle'></i> 创建文件</li>
							<li onclick="rmdir();" id='liRmdir'><i class='skit_fa_icon_red fa fa-minus-circle'></i> 删除文件夹</li>
							<li onclick="rmfile();" id='liRmfile'><i class='skit_fa_icon_red fa fa-minus-circle'></i> 删除文件</li>
							<li class="divider" id='liDivider'></li>
							<li onclick='preupload("file")' id='liPreuploadfile'><i class='skit_fa_icon fa fa-upload'></i> 上传文件</li>
							<li onclick='preupload("zip")' id='liPreuploadzip'><i class='skit_fa_icon fa fa-file-zip-o'></i> 上传压缩包并自动解压</li>
							<li onclick="mkzip();" id='liMkzip'><i class='skit_fa_icon fa fa-download'></i> 压缩下载文件夹</li>
						</ul>
					</div>
				</div>
   			</div>
   		</div>	
	</TD>
	<TD valign='top'>
        <div class="panel panel-default">
   			<div class="panel-heading" style='font-size:12px;padding:5px 10px' id='divPanelHead'>
   				<span><i class='skit_fa_btn fa fa-folder-open'></i> <spanstyle='color:#aaa'></span></span>
	        </div>
   			<div class="panel-body" style='padding: 3px;' id='divFiles' >
				<iframe name='iFileEdit' id='iFileEdit' class='nonicescroll' style='width:100%;height:100%;border:0px;display:none'></iframe>
				<table id="tableFiles" class="panel panel-default" style='border-color:#fff;'>
				<tr>
				 	<td id='tdName'><a onclick='sortAlpha("tableFiles", "sortIconName", 0)'>名称<i class='fa skit_fa_icon fa-sort' id='sortIconName'></i></a></td>
				 	<td width='128'><a onclick='sortAlpha("tableFiles", "sortIconTime", 1)'>修改时间<i class='fa skit_fa_icon fa-sort-alpha-desc' id='sortIconTime'></i></a></td>
					<td width='192'>类型</td>
					<td width='128'><a onclick='sortAmount("tableFiles", "sortIconSize", 3)'>大小<i class='fa skit_fa_icon fa-sort' id='sortIconSize'></i></a></td>
				</tr>
				<tr class='unline'>
					<td style='font-weight:bold' colspan='3'>当前目录共  <span id='spanItemCount'></span> 项目, <span id='spanFileCount'></span> 个文件</td>
					<td style='font-weight:bold' id='tdSumSize' colspan='2'></td>
				</tr>
				</table>
			</div>
		</div>
	</TD>
</TR>
</TABLE>

<div class="panel panel-primary" id='divUploadfile' style='top: 100; left: 100; width:600px; display:none; position: absolute; '>
    <div class="panel-heading">
    	<span id='uploadtitle'>从本地磁盘选择上传文件</span>
        <div class="panel-menu">
            <button type="button" onclick='closeuploadfile()' data-action="close" class="btn btn-warning btn-action btn-xs"><i class="fa fa-times"></i></button>
        </div>
    </div>
    <div class="panel-body" style="display: block;">
		<input type="file" name="uploadfile" id="uploadfile" class="file-loading"/>
    </div>
</div>

<form id='form0'>
<input type='hidden' name='filename' id='filename'>
<input type='hidden' name='filenames' id='filenames'>
<input type='hidden' name='path' id='path'>
<input type='hidden' name='rootdir' id='rootdir'>
<input type='hidden' name='filetime' id='filetime'>
<input type='hidden' name='length' id='length'>
<input type='hidden' name='id' id='id' value="<ww:property value='id'/>">
<input type='hidden' name='ip' id='ip' value="<ww:property value='ip'/>">
<input type='hidden' name='port' id='port' value="<ww:property value='port'/>">
<iframe name='iFiles' id='iFiles' style='display:none'></iframe>
</form>
<div id='divMask' class='skit_mask' onclick='closeprecopy()' style='cursor:pointer;' title='点击关闭弹窗'></div>
</body>
<SCRIPT type="text/javascript">
/*实现窗口对齐*/
function resizeWindow()
{
	var div = document.getElementById('divTree');
	div.style.width = 248;
	div.style.height = windowHeight - 36;
	
	div = document.getElementById( 'divFiles' );
	div.style.height = windowHeight - 36;
	
	div = document.getElementById( 'iFileEdit' );
	div.style.height = windowHeight - 42;
}
</SCRIPT>
<%@ include file="../../skit/inc/skit_cos.inc"%>
<%@ include file="../../skit/inc/skit_ztree.inc"%>
<link href="skit/css/awesome-bootstrap-checkbox.css" rel="stylesheet">
<link href="skit/css/fileinput.min.css" rel="stylesheet"/>
<script src="skit/js/fileinput.js"></script>
<script src="skit/js/fileinput_locale_zh.js"></script>
<script src="skit/js/jquery.md5.js"></script>
<style type='text/css'>
.checkbox, .radio {
    position: relative;
    display: block;
    margin-top: 3px;
    margin-bottom: 0px;
}
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

var ip = "<ww:property value='ip'/>";
var port = <ww:property value='port'/>;
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
				title: "path"
			}
		}
	};

	function onRightClick(event, treeId, treeNode) {
		<ww:if test='grant'>
		if( treeNode.isParent )
		{
			document.getElementById( 'liMkdir' ).style.display = "";
			document.getElementById( 'liMkfile' ).style.display = "";
			document.getElementById( 'liRmdir' ).style.display = "";
			document.getElementById( 'liRmfile' ).style.display = "none";
			document.getElementById( 'liDivider' ).style.display = "";
			document.getElementById( 'liMkzip' ).style.display = "";
			document.getElementById( 'liPreuploadfile' ).style.display = "";
			document.getElementById( 'liPreuploadzip' ).style.display = "";
			
		}
		else
		{
			document.getElementById( 'liMkdir' ).style.display = "none";
			document.getElementById( 'liMkfile' ).style.display = "none";
			document.getElementById( 'liRmdir' ).style.display = "none";
			document.getElementById( 'liRmfile' ).style.display = "";
			document.getElementById( 'liDivider' ).style.display = "none";
			document.getElementById( 'liMkzip' ).style.display = "none";
			document.getElementById( 'liPreuploadfile' ).style.display = "none";
			document.getElementById( 'liPreuploadzip' ).style.display = "none";
		}
		if (!treeNode && event.target.tagName.toLowerCase() != "button" && $(event.target).parents("a").length == 0) {
			myZtree.cancelSelectedNode();
			showRMenu("root", event.clientX, event.clientY);
		} else if (treeNode && !treeNode.noR) {
			myZtree.selectNode(treeNode);
			showRMenu("node", event.clientX, event.clientY);
		}
		</ww:if>
	}
	
	function onClick(event, treeId, treeNode)
	{
		if( !treeNode.open ) myZtree.expandNode(treeNode, true, null, null, true);
		open(treeNode);
	}
	
	function addDiyDom(treeId, treeNode) {
		var editStr = "";
		if( treeNode.ico )
		{
			editStr = "<i class='fa "+treeNode.ico+"' id='ico_fa_menu_" + treeNode.tId + "'></i>";
			
		}
		if( treeNode.warn )
		{
			editStr += "<i class='skit_fa_icon_war fa fa-exclamation-triangle' id='ico_fa_menu_warn_" + treeNode.tId + "'></i>";
		}
		if( editStr )
		{
			var aObj = $("#" + treeNode.tId + "_a");
			if( aObj ) aObj.after(editStr);
		}
	}
	
	function onExpand(event, treeId, treeNode) {
		curExpandNode = treeNode;
		myZtree.selectNode(treeNode);
		getfiles(treeNode);
	}
	$(document).ready(function(){
		try
		{
			var zNodes = <ww:property value="jsonData" escape="false"/>;
			//var zNodes = json.children;
			$.fn.zTree.init($("#myZtree"), setting, zNodes);
			myZtree = $.fn.zTree.getZTreeObj("myZtree");
			myZtreeMenu = $("#rMenu");
			var dir = myZtree.getNodeByParam("id", "root");
			getfiles(dir);
		}
		catch(e)
		{
			alert("初始化目录树异常"+e.message+", 行数"+e.lineNumber);
		}
	});

	document.onkeydown = function(event) 
	{ 
		var e = event ? event :(window.event ? window.event : null); 
		if(e.keyCode==13){ 
			//执行的方法 
		}
	}
	
//#########################################################################
function open(node){
	if( node.isParent ){
		document.getElementById( 'iFileEdit' ).style.display = "none";
		document.getElementById( 'tableFiles' ).style.display = "";
		
		document.getElementById( 'divPanelHead' ).innerHTML = "<span><i class='skit_fa_ico fa fa-folder-open'></i> <span style='color:#aaa'>"+
			node.rootdir+node.path+"</span></span>";
		showfiles(node);
	}
	else{
		document.getElementById( 'iFileEdit' ).style.display = "";
		document.getElementById( 'tableFiles' ).style.display = "none";
		
		FilesMgr.getFileType(node.path,{
			callback:function(response){
				var json = jQuery.parseJSON(response);
				document.getElementById( 'divPanelHead' ).innerHTML = "<span><i class='skit_fa_ico fa fa-"+json.icon+"'></i> <span style='color:#aaa'>"+
					node.rootdir+node.path+"</span></span>";
				node.title = json.description;
			},
			timeout:10000,
			errorHandler:function(message) {
				document.getElementById( 'divPanelHead' ).innerHTML = "<span><i class='skit_fa_ico fa fa-file'></i> <span style='color:#aaa'>"+
					node.rootdir+node.path+"</span></span>";
			}
		});

		var file = node;
		document.getElementById( 'filetime' ).value = file.time?file.time:"";
		document.getElementById( 'path' ).value = file.path;
		document.getElementById( 'rootdir' ).value = file.rootdir?file.rootdir:"";
		document.getElementById( 'length' ).value = file.length;
		document.forms[0].action = "files!preview.action?noclose=1";
		document.forms[0].method = "POST";
		document.forms[0].target = "iFileEdit";
		document.forms[0].submit();
		resizeWindow();
	}
}

function openFile(path){
	var file = myZtree.getNodeByParam("path", path);
	myZtree.selectNode(file);
	open(file);
}

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
		if( file.isParent )
		{
			continue;
		}
		var path = file.path;
		if( file.rootdir ) path = file.rootdir+path;
		var tr = table.insertRow(i+1);
		tr.onmouseover = new Function("this.style.backgroundColor='<ww:property value='themeColorLight'/>';");
		tr.onmouseout = new Function("this.style.backgroundColor='';");
		var td = tr.insertCell(0);
		var ico = file.isParent?"<i class='fa fa-folder'></i> ":"<i class='fa fa-file-text'></i> ";
		var filetime = file.time?file.time:"";
		td.innerHTML = "<a onclick='openFile(\""+file.path+"\")' title='点击查看' class='tablea filename'>"+ico+file.name+"</a>";
		td.title = file.name;
		td = tr.insertCell(1);
		td.className = "weaken";
		td.innerHTML = filetime;
		td.title = filetime;
		td = tr.insertCell(2);
		td.className = "weaken";
		td.title = path;
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
	getContentType(fetchArray, tdArray, fileArray);
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

function getfiles(dir)
{
	FilesMgr.getDirs(ip, port, dir.path, dir.rootdir,{
		callback:function(response){
			if( response.succeed )
			{
				var json = jQuery.parseJSON(response.result);
				dir.files = json.files;
				defaultFileNode = null;
				dir.summary = json.summary;
				setfiles(dir, json.children, true);
				setfiles(dir, json.files, false);
				if( defaultFileNode ){
					myZtree.selectNode(defaultFileNode);
					open(defaultFileNode);
				}
				else{
					myZtree.selectNode(dir);
					open(dir);
				}
			}
			else skit_alert(response.message);
		},
		timeout:10000,
		errorHandler:function(message) {
			skit_hiddenLoading();
		}
	});	
}

var defaultFileNode;
function setfiles(treeNode, files, isdir)
{
	var i, node;
	try{
		var tmpMap1 = new Object();
		var tmpMap2 = new Object();
		for( i = 0; i < files.length; i++ )
		{
			tmpMap1[files[i].name] = true;
		}
		
		var children = treeNode.children;
		if( children != null )
		{
			for( i = 0; i < children.length; i++ )
			{
				node = children[i];
				if( isdir ){
					if( !node.isParent ){
						continue;//只检查目录
					}				
				}
				else{
					if( node.isParent ){
						continue;//只检查文件
					}
				}
				if( tmpMap1[node.name] ){
					tmpMap2[node.name] = i;
					continue;
				}
				myZtree.removeNode(node);
				i -= 1;
			}
		}
		var cursor = -1;
		var count = 0;
		for( i = 0; i < files.length; i++ )
		{
			node = files[i];
			if( tmpMap2[node.name] >= 0 ){
				cursor = count + tmpMap2[node.name];
				continue;
			}
			//node.isParent = isdir;
			if( isdir && node.isParent ){
				count += 1;
				cursor += 1;
				myZtree.addNodes(treeNode, cursor, node);
			}
			else if( !isdir && !node.isParent ){
				count += 1;
				cursor += 1;
				var filenames = "<ww:property value='filenames'/>";
				if( filenames ){
					filenames.replace("*", "");
				}
				if( filenames && node.name.indexOf(filenames) != -1 ){
					node.icon = "images/icons/bookmarks.png";
					node.mark = true;
				}
				var nodes = myZtree.addNodes(treeNode, cursor, node);
				if( nodes[0].mark ){
					defaultFileNode = nodes[0];
				}
			}
		}
	}
	catch(e){
		skit_alert(e);
	}
}

function mkdir()
{
	hideRMenu();
	var nodes = myZtree.getSelectedNodes();
	if( !nodes || nodes.length == 0)
	{
		skit_alert("请先在目录导航树上选择您要创建目录的所属文件夹");
		return;
	}
	var dir = nodes[0];
	var path = dir.path;
	if( dir.rootdir ) path = dir.rootdir+path;
	skit_input("请输入您要创建的目录名称", "", function(yes, val){
		if( yes ){
			FilesMgr.mkDir(ip, port, path+"/"+val, {
				callback:function(rsp) {
					skit_hiddenLoading();
					try
					{
						if( rsp.succeed )
						{
							getfiles(dir);
						}
						else
						{
							skit_alert(rsp.message);
						}
					}
					catch(e)
					{
						alert("创建文件夹"+path+"出现异常"+e.message+", 行数"+e.lineNumber);
					}
				},
				timeout:30000,
				errorHandler:function(err) {skit_hiddenLoading(); alert(err); }
			});
		}
	});
}

function mkfile()
{
	hideRMenu();
	var nodes = myZtree.getSelectedNodes();
	if( !nodes || nodes.length == 0)
	{
		skit_alert("请先在目录导航树上选择您要创建文件的所属文件夹");
		return;
	}
	var dir = nodes[0];
	var path = dir.path;
	if( dir.rootdir ) path = dir.rootdir+path;
	skit_input("请输入您要创建的文件", "", function(yes, val){
		if( yes ){
			FilesMgr.mkFile(ip, port, path+"/"+val, {
				callback:function(rsp) {
					skit_hiddenLoading();
					try
					{
						if( rsp.succeed )
						{
							getfiles(dir);
						}
						else
						{
							skit_alert(rsp.message);
						}
					}
					catch(e)
					{
						alert("创建文件"+path+"出现异常"+e.message+", 行数"+e.lineNumber);
					}
				},
				timeout:30000,
				errorHandler:function(err) {skit_hiddenLoading(); alert(err); }
			});
		}
	});
}
 
function search()
{
	hideRMenu();
	skit_alert("伺服器【"+ip+"】主控版本不支持搜索文件");
}

function rmdir()
{
	hideRMenu();
	var nodes = myZtree.getSelectedNodes();
	if( !nodes || nodes.length == 0)
	{
		skit_alert("请先在目录导航树上选择您要删除的文件夹");
		return;
	}
	var dir = nodes[0];
	var path = dir.path;
	if( dir.rootdir ) path = dir.rootdir+path;
	skit_confirm("您确定要删除伺服器【"+ip+"】下文件夹["+path+"]吗？", function(yes){
		if( yes )
		{
			skit_showLoading();
			FilesMgr.deleteDir(ip, port, path, {
				callback:function(rsp) {
					skit_hiddenLoading();
					try
					{
						if( rsp.succeed )
						{
							var parent = dir.getParentNode();
							myZtree.removeNode(dir);
							parent["isParent"] = true;
							myZtree.selectNode(parent);
							open(parent);
						}
						else
						{
							skit_alert(rsp.message);
						}
					}
					catch(e)
					{
						alert("删除文件夹"+path+"出现异常"+e.message+", 行数"+e.lineNumber);
					}
				},
				timeout:30000,
				errorHandler:function(err) {skit_hiddenLoading(); alert(err); }
			});
		}
	});
}

function rmfile()
{
	hideRMenu();
	var nodes = myZtree.getSelectedNodes();
	if( !nodes || nodes.length == 0)
	{
		skit_alert("请先在目录导航树上选择您要删除的文件夹");
		return;
	}
	var paths = new Array();
	var file = nodes[0];
	if( file.rootdir ){
		paths.push(file.rootdir+file.path);
	}
	else{
		paths.push(file.path);
	}
	var dir = file.getParentNode();
	skit_confirm("您确定要删除该文件【"+file.name+"】吗？", function(yes){
		if( yes )
		{
			skit_showLoading();
			FilesMgr.deleteFiles(ip, port, paths, {
				callback:function(rsp) {
					skit_hiddenLoading();
					try
					{
						if( rsp.succeed )
						{
							getfiles(dir);
						}
						else
						{
							skit_alert(rsp.message);
						}
					}
					catch(e)
					{
						alert("删除文件出现异常"+e.message+", 行数"+e.lineNumber);
					}
				},
				timeout:30000,
				errorHandler:function(err) {skit_hiddenLoading(); alert(err); }
			});
		}
	});
}

function predownload()
{
	var checkboxes = $(".checkbox");
	for(var i = 0; i < checkboxes.length; i+=1 )
	{
		var div = checkboxes[i];
		div.style.display = "";
	}
	var divDelete = document.getElementById( 'divDownload' );
	divDelete.style.top = windowHeight - 50;
	divDelete.style.left = (windowWidth - 250)/2 + 140;
	divDelete.style.display = "";
}

function cancledownload()
{
	var divDelete = document.getElementById( 'divDownload' );
	divDelete.style.display = "none";
	var checkboxes = $(".checkbox");
	for(var i = 0; i < checkboxes.length; i+=1 )
	{
		var div = checkboxes[i];
		div.style.display = "none";
	}
}

function doDownload()
{
	hideRMenu();
	skit_confirm("您确定要压缩下载伺服器【"+ip+"】下所勾选的文件或文件夹吗？", function(yes){
		if( yes )
		{
			var nodes = myZtree.getSelectedNodes();
			if( !nodes || nodes.length == 0)
			{
				skit_alert("请先在目录导航树上选择文件夹目录");
				return;
			}
			var dir = nodes[0];
			document.getElementById( 'path' ).value = dir.path;
			document.getElementById( 'rootdir' ).value = dir.rootdir?dir.rootdir:"";
			var checkboxes = $("input[name^='files']");
			var filenames = "";
			for(var i = 0; i < checkboxes.length; i+=1 )
			{
				var checkbox = checkboxes[i];
				if( checkbox.checked ){
					if( filenames != "" ) filenames += ",";
					filenames += checkbox.value;
				}
			}
			cancledownload();
			document.getElementById( 'filename' ).value = filenames;
			document.forms[0].action = "files!downloads.action";
			document.forms[0].method = "POST";
			document.forms[0].target = "iFiles";
			document.forms[0].submit();
		}
	});
}

function mkzip()
{
	hideRMenu();
	var nodes = myZtree.getSelectedNodes();
	if( !nodes || nodes.length == 0)
	{
		skit_alert("请先在目录导航树上选择文件夹目录");
		return;
	}
	var dir = nodes[0];
	skit_confirm("您确定要压缩下载伺服器【"+ip+"】的文件夹("+dir.path+")吗？", function(yes){
		if( yes )
		{
			document.getElementById( 'path' ).value = dir.path;
			document.getElementById( 'rootdir' ).value = dir.rootdir?dir.rootdir:"";
			cancledownload();
			document.forms[0].action = "files!downloads.action";
			document.forms[0].method = "POST";
			document.forms[0].target = "iFiles";
			document.forms[0].submit();
		}
	});
}

function preupload(type)
{
	hideRMenu();
	var nodes = myZtree.getSelectedNodes();
	if( !nodes || nodes.length == 0)
	{
		skit_alert("请先在目录导航树上选择文件夹目录");
		return;
	}
	var dir = nodes[0];
	document.getElementById( 'uploadtitle' ).innerHTML = type=="zip"?"上传ZIP|GZ压缩包，伺服器主控将自动帮您解压":"从本地磁盘选择上传文件";
	var divUploadfile = document.getElementById( 'divUploadfile' );
	divUploadfile.style.display = "";
	var uploadUrl = type=="zip"?"files!importzip.action":"files!importfile.action";
	var allowedFileExtensions = type=="zip"?['zip', 'gz']:null;
	var maxFileCount = type=="zip"?1:10;
	$("#uploadfile").fileinput({
			language: 'zh', //设置语言
			uploadUrl: uploadUrl, //上传的地址
			allowedFileExtensions: allowedFileExtensions,//接收的文件后缀
			showUpload: true, //是否显示上传按钮
			showCaption: false,//是否显示标题
			showClose: false,
			showPreview: true,
			browseClass: "btn btn-primary", //按钮样式     
			previewSettings: { image: {width: "auto", height: "260"} },
			//dropZoneEnabled: false,//是否显示拖拽区域
			//maxFileSize: 0,//单位为kb，如果为0表示不限制文件大小
			//minFileCount: 0,
			maxFileCount: maxFileCount, //表示允许同时上传的最大文件个数
			enctype: 'multipart/form-data',
			validateInitialCount:true,
			previewFileIcon: "<i class='glyphicon glyphicon-king'></i>",
			msgFilesTooMany: "选择上传的文件数量({n}) 超过允许的最大数值{m}！",
            uploadExtraData: function(previewId, index) {
				//额外参数的关键点
                var obj = {};
                obj.path = dir.path;
                obj.rootdir = dir.rootdir?dir.rootdir:"";
                obj.filename = document.getElementById( 'filename' ).value;
                obj.ip = document.getElementById( 'ip' ).value;
                obj.port = document.getElementById( 'port' ).value;
                return obj;
            }
	});
	divUploadfile.style.top = 64;//windowHeight/2 - divUploadfile.clientHeight*2/3;
	divUploadfile.style.left = windowWidth/2 - divUploadfile.clientWidth/2;
	$("#uploadfile").on("fileloaded", function (data, previewId, index) {
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
			getfiles(dir);
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

<script src="skit/js/bootstrap-tour.min.js"></script>
<SCRIPT type="text/javascript">
function tour()
{
	var showTour = getUserActionMemory("tourFilesmgr");
	if( showTour != "1" )
	{
		setUserActionMemory("tourFilesmgr", "1");
		var tour = new Tour({
		    steps: [{
		        element: "#divTree",
		        title: "浏览、下载、上传、拷贝、删除目录",
		        content: "支持浏览容器【缺省工作目录】相对路径文件结构，以及【系统目录】完整目录结构；支持下载文件夹，通过压缩包上传文件夹然后自动解压；同步拷贝文件夹到集群任何一台伺服器；可删除【缺省工作目录】下的子目录。",
		        placement: "right"
		    },{
		        element: "#divPathinput",
		        title: "地址栏",
		        content: "显示当前文件夹路径；记忆10条常用地址；用户直接输入路径跳转；点击右端按钮返回上级路径。",
		        placement: "bottom"
		    },{
		        element: "#divPanelHead",
		        title: "文件操作:预览、上传、删除",
		        content: "点击任何文件预览文件，显示文件各项信息，支持单文件直接或压缩下载、文本文件在线编辑、压缩文件在线解压；支持上传文件到当前目录，或者通过压缩包上传文件并自动解压；同步拷贝多个文件到集群任何一台伺服器；可选择删除当前目录多个文件或子文件夹。",
		        placement: "bottom"
		    }],
		    container: 'body',
		    backdrop: true,
		    keyboard: true,
		    storage: false
		});
		// Initialize the tour
		tour.init();
		// Start the tour
		tour.start();
	}
}
</SCRIPT>
</html>