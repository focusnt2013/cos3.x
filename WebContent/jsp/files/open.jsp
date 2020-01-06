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
   z-index: 5;
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
<TABLE>
<TR class='unline'><TD colspan='2' style='padding-top: 3px;padding-bottom: 3px;'>
	<div class="input-group custom-search-form" id='divPathinput'>
		<div class="input-group-btn">
            <button type="button" class="btn btn-info dropdown-toggle" data-toggle="dropdown" aria-expanded="false"
            	style='display: inline-block;height:28px;' id='btnAddr'><span class="caret"></span></button>
		    <ul class="dropdown-menu pull-left" style='width:300px' id='ulGopathMemory'>
		        <li id='liDefault'><a onclick='selectGopath(this)' style='font-size:12px;'>缺省工作目录</a></li>
		    </ul>
		</div>
		<span class="input-group-addon" style='font-size:12px;border-right:0px solid red;'>地址栏：</span>
	    <input class="form-control" placeholder="输入路径跳转，可输入相对路径在缺省工作目录下跳转，也可输入绝对路径在系统目录下跳转"
	    	 onkeyup='return go(this);' autocomplete='on'
	    	 id='dir' name='dir' type="text" style='font-size: 12px;height:28px;padding-top:3px;padding-bottom:3px;'>
	    <span class="input-group-btn">
	        <button class="btn btn-inverse" type="button" onclick='gopath()' style='width:22px;' title='跳转'>
	            <i class="fa fa-share-square-o"></i>
	        </button>
	        <button class="btn btn-inverse" type="button" onclick='goup()' style='width:36px;' title='返回上一级'>
	            <i class="fa fa-level-up"></i>
	        </button>
	    </span>
	</div>
</TD></TR>
<TR class='unline'><TD width='250'>
        <div class="panel panel-default">
   			<div class="panel-heading" style='font-size:12px;padding:5px 15px'><i class='skit_fa_btn fa fa-bars'></i> 文件目录树</div>
   			<div class="panel-body" style='padding: 0px;'>
   				<div id='divTree'>
					<ul id='myZtree' class='ztree'></ul>
					<div id="rMenu">
						<ul>
							<li onclick="mkdir();" id='liMkdir'><i class='skit_fa_icon fa fa-plus-circle'></i> 创建文件夹</li>
							<li onclick="mkfile();" id='liMkfile'><i class='skit_fa_icon fa fa-plus-circle'></i> 创建文件</li>
							<li onclick="search()" id='liSearch'><i class='skit_fa_icon fa fa-search'></i> 搜索文件</li>
							<li onclick="rmdir();" id='liRmdir'><i class='skit_fa_icon_red fa fa-minus-circle'></i> 删除文件夹</li>
							<li class="divider" id='liDivider'></li>
							<li onclick="mkzip();" id='liMkzip'><i class='skit_fa_icon fa fa-download'></i> 压缩下载文件夹</li>
							<li onclick='preupload("file")' id='liPreuploadfile'><i class='skit_fa_icon fa fa-upload'></i> 上传文件</li>
							<li onclick='preupload("zip")' id='liPreuploadzip'><i class='skit_fa_icon fa fa-file-zip-o'></i> 上传压缩包并自动解压</li>
							<li onclick='precopydir()' id='liPrecopy'><i class='skit_fa_icon fa fa-copy'></i> 同步拷贝文件夹</li>
						</ul>
					</div>
				</div>
   			</div>
   		</div>	
	</TD>
	<TD valign='top'>
        <div class="panel panel-default">
   			<div class="panel-heading" style='font-size:12px;padding:5px 15px' id='divFilesOper'>
   				<span><i class='skit_fa_btn fa fa-folder-open'></i> <span id='headTitle' style='color:#aaa'></span></span>
				<ww:if test='grant'>
				<span class='dropdown' style='float: right;'><a href='#'
					onmouseover='this.style.color="#000000";this.style.fontWeight="bold";'
					onmouseout='this.style.color="#566B52";this.style.fontWeight="normal";' 
					class='dropdown-toggle' 
					data-toggle='dropdown' 
					aria-expanded='false' 
					style='color:#566B52;TEXT-DECORATION: none'>
					<i class='skit_fa_btn fa fa-android'></i>文件操作</a>
					<ul class="dropdown-menu pull-right">
						<li onclick="mkdir()"><a><i class='skit_fa_icon fa fa-plus-circle fa-fw'></i> 创建文件夹</a></li>
						<li onclick="search()"><a><i class='skit_fa_icon fa fa-search fa-fw'></i> 搜索文件或文件夹</a></li>
						<li onclick='predelete()'><a><i class='skit_fa_icon_red fa fa-minus-circle fa-fw'></i> 删除文件或文件夹</a></li>
						<li class="divider"></li>
						<!-- 
						<li onclick='predownload()'><a><i class='skit_fa_icon fa fa-download fa-fw'></i> 下载文件或文件夹</a></li>
						-->
						<li onclick='preupload("file")'><a><i class='skit_fa_icon fa fa-upload fa-fw'></i> 上传文件</a></li>
						<li onclick='preupload("zip")'><a><i class='skit_fa_icon fa fa-file-zip-o fa-fw'></i> 上传压缩包并自动解压</a></li>
						<li onclick='opencopyselect()'><a><i class='skit_fa_icon fa fa-copy fa-fw'></i> 同步拷贝文件</a></li>
					</ul>
				</span>
				</ww:if>
	        </div>
   			<div class="panel-body" style='padding: 3px;' id='divFiles'>
				<table id="tableFiles" class="panel panel-default" style='border-color:#fff;'>
				<tr>
				 	<td id='tdName'><a onclick='sortAlpha("tableFiles", "sortIconName", 0)'>名称<i class='fa skit_fa_icon fa-sort' id='sortIconName'></i></a></td>
					<td width='192'>用户组权限</td>
				 	<td width='144'><a onclick='sortAlpha("tableFiles", "sortIconTime", 1)'>修改时间<i class='fa skit_fa_icon fa-sort-alpha-desc' id='sortIconTime'></i></a></td>
					<td width='192'>类型</td>
					<td width='128'><a onclick='sortAmount("tableFiles", "sortIconSize", 3)'>大小<i class='fa skit_fa_icon fa-sort' id='sortIconSize'></i></a></td>
					<td width='36' align='center' style='padding-left: 0px;'>
						<div class='checkbox checkbox-info checkbox-circle' style='display:none;'>
							<input title='全选或全取消' onclick='checkAll(this)' type='checkbox' 
								style='cursor:pointer;background-color:<%=(String)session.getAttribute("System-Theme")%>'><label></label></div>
					</td>
				</tr>
				<tr class='unline'>
					<td style='font-weight:bold' colspan='3'>当前目录共显示  <span id='spanItemCount'></span> 项目, <span id='spanFileCount'></span> 个文件或文件夹</td>
					<td style='font-weight:bold' id='tdSumSize' colspan='2'></td>
				</tr>
				</table>
			</div>
		</div>
	</TD>
</TR>
</TABLE>
<div style='top: 100; left: 100; width:280px; display:none; position: absolute; border:0px solid red;' id='divDelete'>
	<button type="button" class="btn btn-outline btn-primary btn-block"
		style='width:128px;float:left;padding-right:10px;' onclick='doDelete();'><i class="fa fa-save"></i> 删除 </button>
	<button type="button" class="btn btn-outline btn-danger btn-block"
		style='width:128px;padding-left:10px;float:right;margin-top:0px;' onclick='cancledelete();'><i class="fa fa-sign-out"></i> 取消</button></div>
<div style='top: 100; left: 100; width:280px; display:none; position: absolute; border:0px solid red;' id='divDownload'>
	<button type="button" class="btn btn-outline btn-primary btn-block"
		style='width:128px;float:left;padding-right:10px;' onclick='doDownload();'><i class="fa fa-download"></i> 下载 </button>
	<button type="button" class="btn btn-outline btn-danger btn-block"
		style='width:128px;padding-left:10px;float:right;margin-top:0px;' onclick='cancledownload();'><i class="fa fa-sign-out"></i> 取消</button></div>
<div style='top: 100; left: 100; width:280px; display:none; position: absolute; border:0px solid red;' id='divCopy'>
	<button type="button" class="btn btn-outline btn-primary btn-block"
		style='width:128px;float:left;padding-right:10px;' onclick='precopyfiles();'><i class="fa fa-save"></i> 同步复制 </button>
	<button type="button" class="btn btn-outline btn-danger btn-block"
		style='width:128px;padding-left:10px;float:right;margin-top:0px;' onclick='canclecopy();'><i class="fa fa-sign-out"></i> 取消</button></div>
		
<div class="panel panel-primary" id='divUploadfile' style='z-index:10;top: 100; left: 100; width:600px; display:none; position: absolute; '>
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
<div id='divMask' class='skit_mask' onclick='closeMask();' style='cursor:pointer;' title='点击关闭弹窗'></div>
</body>
<SCRIPT type="text/javascript">
/*实现窗口对齐*/
function resizeWindow()
{
	var div = document.getElementById('divTree');
	div.style.width = 248;
	div.style.height = windowHeight - 68;
	div = document.getElementById( 'divFiles' );
	div.style.height = windowHeight - 68;
	div = document.getElementById( 'dir' );
	div.style.width = windowWidth - 170;
}
</SCRIPT>
<%@ include file="../../skit/inc/skit_bootstrap.inc"%>
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

.filename.ln {
	color: #0099ff;
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
var userAgent = navigator.userAgent; //取得浏览器的userAgent字符串
var isChrome = userAgent.indexOf("Chrome") > -1;
if( isChrome )
{
	document.body.style.marginTop = 3;
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

$( '#divFiles' ).niceScroll({
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
		}
	};

	function onRightClick(event, treeId, treeNode) {
		<ww:if test='grant'>
		if( treeNode.path == "/" )
		{
			return;
		}
		else if( treeNode.path == "" )
		{
			document.getElementById( 'liMkdir' ).style.display = "";
			document.getElementById( 'liMkfile' ).style.display = "";
			document.getElementById( 'liSearch' ).style.display = "";
			document.getElementById( 'liRmdir' ).style.display = "none";
			document.getElementById( 'liDivider' ).style.display = "none";
			document.getElementById( 'liMkzip' ).style.display = "none";
			document.getElementById( 'liPreuploadfile' ).style.display = "";
			document.getElementById( 'liPreuploadzip' ).style.display = "";
			document.getElementById( 'liPrecopy' ).style.display = "none";
			
		}
		else
		{
			document.getElementById( 'liMkdir' ).style.display = "";
			document.getElementById( 'liMkfile' ).style.display = "";
			document.getElementById( 'liSearch' ).style.display = "";
			document.getElementById( 'liRmdir' ).style.display = "";
			document.getElementById( 'liDivider' ).style.display = "";
			document.getElementById( 'liMkzip' ).style.display = "";
			document.getElementById( 'liPreuploadfile' ).style.display = "";
			document.getElementById( 'liPreuploadzip' ).style.display = "";
			document.getElementById( 'liPrecopy' ).style.display = "";
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
		showfiles(treeNode);
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
			var uid = "files!open.path-<ww:property value='id'/>";
			var defpath = getUserActionMemory(uid);
			if( !defpath ) defpath = "";
			var node = myZtree.getNodeByParam("path", defpath);
			if( node && node.path == "" )
			{
				myZtree.selectNode(node);
				showfiles(node);
			}
			else
			{
				document.getElementById( 'dir' ).value = defpath;
				gopath();
			}
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
	        gopath();
		}
	}
	
//#########################################################################
function getfiles(treeNode)
{
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
			myZtree.removeNode(node);
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
		myZtree.addNodes(treeNode, cursor, node);
	}
	treeNode["files"] = json.files;
	treeNode["summary"] = json.summary;
	showfiles(json);
	var uid = "files!open.path-<ww:property value='id'/>";
	//alert(uid+":"+json.path);
	setUserActionMemory(uid, json.path);
}

var str = getUserActionMemory("files!open.gopath-<ww:property value='id'/>");
var ul = document.getElementById( 'ulGopathMemory' );
if( str && str != "" )
{
	var gopathMemory = jQuery.parseJSON(str);
	var liDefault = document.getElementById( 'liDefault' );
	for(var i = 0; i< gopathMemory.length; i++ )
	{
		var li = document.createElement("li");
		li.innerHTML = "<a onclick='selectGopath(this)' style='font-size:12px;' class='liaGopath'>"+gopathMemory[i]+"</a>";
		ul.insertBefore(li, liDefault);
	}
}
ul.style.width = windowWidth - 50;
function saveGopathMemory(dir)
{
	var args = $(".liaGopath");
	var ul = document.getElementById( 'ulGopathMemory' );
	for(var i = 0; i< args.length; i++ )
	{
		var path = args[i].innerHTML;
		if( dir == path ) return;
	}
	if( args.length >= 10 )
	{
		var liDefault = document.getElementById( 'liDefault' );
		var lastChild = liDefault.previousSibling;//ul.childNodes[ul.childNodes.length-2];
		//alert("lastChild="+(lastChild?lastChild.innerHTML:"null"));
		ul.removeChild(lastChild);
	}
	
	var li = document.createElement("li");li.innerHTML = "<a onclick='selectGopath(this)' style='font-size:12px;' class='liaGopath'>"+dir+"</a>";
	ul.insertBefore(li, ul.firstChild);
	
	args = $(".liaGopath");
	var gopathMemory = new Array();
	for(var i = 0; i< args.length; i++ )
	{
		gopathMemory.push(args[i].innerHTML);
	}
	var value = JSON.stringify(gopathMemory);
	//alert("saveGopathMemory:"+value);
	setUserActionMemory("files!open.gopath-<ww:property value='id'/>", value);
}

function selectGopath(a)
{
	var path = a.innerHTML;
	if( path == "缺省工作目录" ) path = "";
	document.getElementById("dir").value = path;
	gopath();
}

function goup()
{
	var nodes = myZtree.getSelectedNodes();
	if( !nodes || nodes.length == 0)
	{
		skit_alert("请先在目录导航树上选择您要操作的文件夹");
		return;
	}
	var dir = nodes[0];
	var parent = dir.getParentNode();
	if( parent )
	{
		myZtree.selectNode(parent);
		showfiles(parent);
		
	}
}

function gopath()
{
	var dir = document.getElementById( 'dir' ).value;
	dir = dir.trim();
	if( dir.indexOf(":") != -1 )
	{
		skit_alert("跳转的目录地址不合法");
		return;
	}
	var node = myZtree.getNodeByParam("path", dir);
	if( node )
	{
		myZtree.selectNode(node);
		myZtree.expandNode(node, true);
		getfiles(node);
		return;
	}
	else
	{
		//判断是绝对路径还是相对路径
		var rootdir = null;
		if( dir.charAt(0) != '/' )
		{
			node = myZtree.getNodeByParam("path", "");
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
							var str = response.objects[i];
							var json = jQuery.parseJSON(str);
							var treeNode = myZtree.getNodeByParam("path", json.path);
							if( treeNode )
							{
								myZtree.selectNode(treeNode);
								myZtree.expandNode(treeNode, true);
								setfiles(treeNode, json);
							}
						}
						saveGopathMemory(dir);
						document.getElementById( 'dir' ).value = response.result;
					}
					catch(e)
					{
						alert("跳转路径"+dir+"出现异常"+e.message+", 行数"+e.lineNumber);
					}
				}
				if( response.message != null )
					skit_alert(response.message);
			},
			timeout:30000,
			errorHandler:function(message) {
				skit_hiddenLoading();
			}
		});	
	}
}

function preview(node)
{
	//alert(node.path);
}

function showfiles(json)
{
	cancledelete();
	var files = json.files;
	var summary = json.summary;
	//showObject(json);
	document.getElementById( 'dir' ).value = json.path;
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
		if( file.ln ){
			td.innerHTML = "<a onclick='openFile(\""+file.path+"\", "+file.isParent+", "+file.length+", \""+filetime+"\")' title='"+atip+
			"' class='tablea filename ln'>"+ico+file.name+" -&gt; "+file.ln+"</a>";
		}
		else{
			td.innerHTML = "<a onclick='openFile(\""+file.path+"\", "+file.isParent+", "+file.length+", \""+filetime+"\")' title='"+atip+
				"' class='tablea filename'>"+ico+file.name+"</a>";
		}
		td.title = file.name;
		
		td = tr.insertCell(1);
		td.className = "weaken";
		if( file.user ){
			if( file.user == file.group ){
				td.innerHTML = file.user+"(=) "+file.privileges;
				td.title = file.privileges;
			}
			else{
				td.innerHTML = file.user+"/"+file.group;
				td.title = file.privileges;
			}
		}
		
		td = tr.insertCell(2);
		td.className = "weaken";
		td.innerHTML = filetime;
		td.title = filetime;
		td = tr.insertCell(3);
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
		td = tr.insertCell(4);
		td.className = "weaken";
		if( file.isParent ){
			if( file.count ){
				td.innerHTML = file.size+(" ("+file.count+")");			
			}
			else{
				td.innerHTML = "";
			}
		}
		else{
			td.innerHTML = file.size;
		}
		td.title = file.length;
		td = tr.insertCell(5);
		td.style.paddingLeft = 0;
		td.align = 'center';
		td.innerHTML = "<div class='checkbox checkbox-danger filecheckbox' style='display:none;'>"+
			"<input name='files' value='"+path+"' title='"+path+"' aria-label='' type='checkbox' style='cursor:pointer;'><label></label></div>";
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
	document.getElementById( 'headTitle' ).innerHTML = json.path?json.path:"缺省工作目录";
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

function openFile(path, isdir, length, filetime)
{
	if( isdir )
	{
		var dir = myZtree.getNodeByParam("path", path);
		if( !dir )
		{
			skit_alert("未找到您要打开的文件或文件夹"+path);
			return;
		}
		myZtree.selectNode(dir);
		myZtree.expandNode(dir, true);
		getfiles(dir);
	}
	else
	{
		var nodes = myZtree.getSelectedNodes();
		if( !nodes || nodes.length == 0)
		{
			skit_alert("请先在目录导航树上选择文件夹");
			return;
		}
		var dir = nodes[0];
		//alert("node.rootdir="+dir.rootdir);
		previewfile(path, length, filetime, dir.rootdir);
	}
}
var zIndex = 10000;
var previewframes = new Object();
function previewfile(path, length, filetime, rootdir)
{
	var id = $.md5(path);
	if( previewframes[id] )
	{
		showpreview(id);
		return;
	}
	var fileico = "<i class='fa fa-file-o'></i>";
	var file = fileTypes[path];
	if( file )
	{
		fileico = "<i class='fa fa-"+file.typeico+"'></i>";
	}
	
	var div = document.createElement("div");
	div.className = "panel panel-primary";
	div.style.marginTop = 3;
	div.style.marginLeft = 3;
	div.style.position = "absolute";
	div.style.width = windowWidth - 24;
	var div1 = document.createElement("div");
	div1.className = "panel-heading";
	div1.style.height = 28;
	div1.title = path;
	div.appendChild(div1);
	var div10 = document.createElement("div");
	div10.style.width = windowWidth - 216;
	div10.style.float = "left";
	div10.id = "span-div-"+id;
	div1.appendChild(div10);
	var span10 = document.createElement("span");
	span10.className = "panel-title";
	var filename = path.substring(path.lastIndexOf('/')+1);
	var dir = path.substring(0, path.lastIndexOf('/')+1)
	span10.innerHTML = fileico+" "+filename+" 目录在："+dir;
	span10.id = "span-"+id;
	span10.style.width = windowWidth - 216;
	div10.appendChild(span10);
	var div11 = document.createElement("div");
	div11.className = "panel-menu";
	div11.innerHTML = 
		"<button type='button' onclick='showpreview(\""+id+"\")' data-action='minimize' id='btnMini"+id+"' class='btn btn-default btn-action btn-xs'><i class='fa fa-angle-down'></i></button>"+
		"<button type='button' onclick='reloadpreview(\""+id+"\",\""+path+"\","+length+")' data-action='reload' class='btn btn-default btn-action btn-xs hidden-xs hidden-sm'><i class='fa fa-refresh'></i></button>"+
		"<button type='button' onclick='closepreview(\""+id+"\")' data-action='close' class='btn btn-warning btn-action btn-xs'><i class='fa fa-times'></i></button>";
	div1.appendChild(div11);
	var div2 = document.createElement("div");
	div2.className = "panel-body";
	div2.id = "panel-body"+id;
	div2.innerHTML = "<iframe name='i-"+id+"' id='i-"+id+"' class='nonicescroll' style='width:100%;border:0px solid red;'></iframe>";
	div.appendChild(div2);
	document.forms[0].appendChild(div);
	div.style.left = 10;
	div.style.top = 10;
	div.style.zIndex = zIndex++;
	previewframes[id] = div;
	showId = id;
	
	document.getElementById("i-"+id).style.height = windowHeight - 88;
	var divMask = document.getElementById("divMask");
	callbackCloseMask = function(){
		closepreview(id);
	}
	divMask.style.visibility = "visible";
	divMask.style.width = windowWidth;
	divMask.style.height = windowHeight;
	
	document.getElementById( 'filetime' ).value = filetime;
	document.getElementById( 'path' ).value = path;
	document.getElementById( 'rootdir' ).value = rootdir?rootdir:"";
	document.getElementById( 'length' ).value = length;
	document.forms[0].action = "files!preview.action";
	document.forms[0].method = "POST";
	document.forms[0].target = "i-"+id;
	document.forms[0].submit();
}

function opencopyselect()
{
	var checkboxes = $(".checkbox");
	for(var i = 0; i < checkboxes.length; i+=1 )
	{
		var div = checkboxes[i];
		div.style.display = "";
	}
	var divCopy = document.getElementById( 'divCopy' );
	divCopy.style.top = windowHeight - 50;
	divCopy.style.left = (windowWidth - 250)/2 + 140;
	divCopy.style.display = "";
}

function canclecopy()
{
	var divCopy = document.getElementById( 'divCopy' );
	divCopy.style.display = "none";
	var checkboxes = $(".checkbox");
	for(var i = 0; i < checkboxes.length; i+=1 )
	{
		var div = checkboxes[i];
		div.style.display = "none";
	}
}

function precopyfiles()
{
	var nodes = myZtree.getSelectedNodes();
	if( !nodes || nodes.length == 0)
	{
		skit_alert("请先在目录导航树上选择您要删除的文件夹");
		return;
	}
	var dir = nodes[0];
	var filenames = "";
	var checkboxes = $("input[name^='files']");
	for(var i = 0; i < checkboxes.length; i+=1 )
	{
		var checkbox = checkboxes[i];
		if( checkbox.checked ){
			var filename = checkbox.value.substring(checkbox.value.lastIndexOf('/')+1);
			filenames += filename+",";
		}
	}
	if( filenames == "" )
	{
		skit_alert("请选择您要同步拷贝的文件");
		return;
	}
	precopy(dir, filenames);
}

function precopydir()
{
	hideRMenu();
	var nodes = myZtree.getSelectedNodes();
	if( !nodes || nodes.length == 0)
	{
		skit_alert("请先在目录导航树上选择您要删除的文件夹");
		return;
	}
	var dir = nodes[0];
	precopy(dir);
}

function precopy(dir, filenames)
{
	if( filenames ){} else{
		filenames = "";
	}
	try
	{
		var div = document.createElement("div");
		div.id = "div-precopy";
		div.className = "panel panel-primary";
		div.style.marginTop = 3;
		div.style.marginLeft = 3;
		div.style.position = "absolute";
		div.style.width = windowWidth - 264;
		var div1 = document.createElement("div");
		div1.className = "panel-heading";
		div1.style.height = 28;
		div1.title = path;
		div.appendChild(div1);
		var div10 = document.createElement("div");
		div10.style.width = windowWidth - 400;
		div10.style.float = "left";
		div1.appendChild(div10);
		var span10 = document.createElement("span");
		span10.className = "panel-title";
		span10.innerHTML = "<i class='fa fa-copy'></i> 拷贝"+dir.path+"/"+filenames;
		span10.style.width = windowWidth - 400;
		div10.appendChild(span10);
		var div11 = document.createElement("div");
		div11.className = "panel-menu";
		div11.innerHTML = 
			"<button type='button' onclick='closeprecopy()' data-action='close' class='btn btn-warning btn-action btn-xs'><i class='fa fa-times'></i></button>";
		div1.appendChild(div11);
		var div2 = document.createElement("div");
		div2.className = "panel-body";
		div2.innerHTML = "<iframe name='i-precopy' id='i-precopy' class='nonicescroll' style='width:100%;border:0px solid red;'></iframe>";
		div.appendChild(div2);
		document.forms[0].appendChild(div);
		div.style.left = 256;
		div.style.top = 34;
		div.style.zIndex = zIndex++;
		document.getElementById("i-precopy").style.height = windowHeight - 108;

		var divMask = document.getElementById("divMask");
		callbackCloseMask = function(){
			closeprecopy();
		}
		divMask.style.visibility = "visible";
		divMask.style.width = windowWidth;
		divMask.style.height = windowHeight;

		document.getElementById( 'filenames' ).value = filenames;
		document.getElementById( 'path' ).value = dir.path;
		document.getElementById( 'rootdir' ).value = dir.rootdir?dir.rootdir:"";
		document.forms[0].action = "files!precopy.action";
		document.forms[0].method = "POST";
		document.forms[0].target = "i-precopy";
		document.forms[0].submit();
	}
	catch(e)
	{
		skit_alert(e);
	}
}

function closeprecopy()
{
	try
	{
		var panel = document.getElementById("div-precopy");
		document.forms[0].removeChild(panel);
		document.getElementById("divMask").style.visibility = "hidden";
	}
	catch(e)
	{
		alert("关闭拷贝窗口出现异常"+e.message+", 行数"+e.lineNumber);
	}
}

function reloadpreview(id, path, length)
{
	var nodes = myZtree.getSelectedNodes();
	if( !nodes || nodes.length == 0)
	{
		skit_alert("请先在目录导航树上选择文件夹");
		return;
	}
	var dir = nodes[0];
	document.getElementById("i-"+id).style.height = windowHeight - 88;

	var divMask = document.getElementById("divMask");
	divMask.onClick = function(){
		closepreview(id);
	};
	divMask.style.visibility = "visible";
	divMask.style.width = windowWidth;
	divMask.style.height = windowHeight;
	
	document.getElementById( 'path' ).value = path;
	document.getElementById( 'rootdir' ).value = dir.rootdir?dir.rootdir:"";
	document.getElementById( 'length' ).value = length;
	document.forms[0].action = "files!preview.action";
	document.forms[0].method = "POST";
	document.forms[0].target = "i-"+id;
	document.forms[0].submit();
}

var showId;//当前正在显示的预览
function showpreview(id)
{
	if( !id ) id = showId;
	try
	{
		var btn = document.getElementById( "btnMini"+id);
		var bodyPanel = document.getElementById( "panel-body"+id);
		var panel = previewframes[id];
		if( btn.innerHTML.indexOf("up") != -1 )
		{
			btn.innerHTML = "<i class='fa fa-angle-down'></i>";
			bodyPanel.style.display = "";
			var panel = previewframes[id];
			var span = document.getElementById( "span-"+id);
			span.style.width = windowWidth - 216;
			var div = document.getElementById( "span-div-"+id);
			div.style.width = windowWidth - 216;
			panel.style.width = windowWidth - 24; 
			panel.style.left = 10;
			panel.style.top = 10;
			panel.style.zIndex = zIndex++;
			document.getElementById("i-"+id).style.height = windowHeight - 88;
			if( showId ){
				showpreview(showId);
			}
			showId = id;
			var divMask = document.getElementById("divMask");
			divMask.style.visibility = "visible";
			divMask.style.width = windowWidth;
			divMask.style.height = windowHeight;
		}
		else
		{
			btn.innerHTML = "<i class='fa fa-angle-up'></i>";
			bodyPanel.style.display = "none";
			panel.style.zIndex = Number(panel.style.zIndex)*10;
			minpreview();
			showId = null;
		}
		//alert("showId="+showId+","+(showId?previewframes[showId].title:"XXX"));
	}
	catch(e)
	{
		alert("异常"+e.message+", 行数"+e.lineNumber);
	}
}

function closepreview(id)
{
	if( !id ) id = showId;
	try
	{
		var panel = previewframes[id];
		document.forms[0].removeChild(panel);
		delete previewframes[id];
		document.getElementById("divMask").style.visibility = "hidden";
	}
	catch(e)
	{
		alert("关闭预览"+id+"出现异常"+e.message+", 行数"+e.lineNumber);
	}
}

function minpreview()
{
	var i = 0;
	for(var id in previewframes)
	{
		var bodyPanel = document.getElementById( "panel-body"+id);
		if( bodyPanel.style.display != "none" )
		{
			continue;
		}
		var panel = previewframes[id];//document.getElementById( path);
		var span = document.getElementById( "span-"+id);
		span.style.width = 144;
		var div = document.getElementById( "span-div-"+id);
		div.style.width = 144;
		panel.style.width = 260; 
		panel.style.left = windowWidth - 270;
		panel.style.top = windowHeight - 48 - i*32;
		i += 1;
	}
	document.getElementById("divMask").style.visibility = "hidden";
}

/*
 <div class="panel panel-primary" id='divPreviewfile' style='top: 100; left: 100; width:360px; display:none; position: absolute; '>
 <div class="panel-heading">
 	<span id='uploadtitle'>预览文件</span>
     <div1 class="panel-menu">
         <button type="button" data-action="minimize" class="btn btn-default btn-action btn-xs"><i class="fa fa-angle-down"></i></button>
         <button type="button" data-action="reload" class="btn btn-default btn-action btn-xs hidden-xs hidden-sm"><i class="fa fa-refresh"></i></button>	
         <button type="button" onclick='closeuploadfile()' data-action="close" class="btn btn-warning btn-action btn-xs"><i class="fa fa-times"></i></button>
     </div>
 </div>
 <div class="panel-body" style="display: block;">
		<iframe src='' name='iPreview' id='iPreview' class='nonicescroll' style='width:100%;border:0px;'></iframe>
 </div>
</div>
 */


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
							document.getElementById( parent.tId + "_ico").className = "button ico_open";
							myZtree.selectNode(parent);
							preview(parent);
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

function checkAll(flag)
{
	var checkboxes = $("input[name^='files']");
	for(var i = 0; i < checkboxes.length; i+=1 )
	{
		var checkbox = checkboxes[i];
		checkbox.checked = flag.checked;
	}
}

function predelete()
{
	var checkboxes = $(".checkbox");
	for(var i = 0; i < checkboxes.length; i+=1 )
	{
		var div = checkboxes[i];
		div.style.display = "";
	}
	var divDelete = document.getElementById( 'divDelete' );
	divDelete.style.top = windowHeight - 50;
	divDelete.style.left = (windowWidth - 250)/2 + 140;
	divDelete.style.display = "";
}

function cancledelete()
{
	var divDelete = document.getElementById( 'divDelete' );
	divDelete.style.display = "none";
	var checkboxes = $(".checkbox");
	for(var i = 0; i < checkboxes.length; i+=1 )
	{
		var div = checkboxes[i];
		div.style.display = "none";
	}
}

function doDelete()
{
	hideRMenu();
	var nodes = myZtree.getSelectedNodes();
	if( !nodes || nodes.length == 0)
	{
		skit_alert("请先在目录导航树上选择您要删除的文件夹");
		return;
	}
	var dir = nodes[0];
	var paths = new Array();
	var checkboxes = $("input[name^='files']");
	for(var i = 0; i < checkboxes.length; i+=1 )
	{
		var checkbox = checkboxes[i];
		if( checkbox.checked ) paths.push(checkbox.value);
	}
	
	skit_confirm("您确定要删除伺服器【"+ip+"】下所勾选的文件或文件夹吗？", function(yes){
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
							cancledelete();
							getfiles(dir);
						}
						else
						{
							skit_alert(rsp.message);
						}
					}
					catch(e)
					{
						alert("删除多个文件或文件夹出现异常"+e.message+", 行数"+e.lineNumber);
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
		//$("#uploadfile").fileinput("destroy");
		closeMask();
	});
	var divMask = document.getElementById("divMask");
	callbackCloseMask = function(){
		closeuploadfile();
	}
	divMask.style.visibility = "visible";
	divMask.style.width = windowWidth;
	divMask.style.height = windowHeight;
}

var callbackCloseMask;
function closeMask(){
	document.getElementById("divMask").style.visibility = "hidden";
	if( callbackCloseMask ){
		callbackCloseMask();
		callbackCloseMask = null;
	}
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
		        element: "#divFilesOper",
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
tour();
</SCRIPT>
</html>