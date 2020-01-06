<%@ page language="java" pageEncoding="UTF-8"%>
<%@ page import="com.focus.cos.web.common.Kit"%>
<%@ taglib uri="/webwork" prefix="ww" %>
<%Kit.noCache(request,response);  %>
<html>
<head>
<link type="text/css" href="skit/ztree/css/zTreeStyle/zTreeStyle.css" rel="stylesheet"/>
<link type="text/css" href="skit/css/bootstrap.css" rel="stylesheet">
<style type='text/css'>
.node-prop {
    margin: 10px;
    box-shadow: 0 1px 3px 0 rgba(0,0,0,.14);
    background-color: #fff;
    border: 1px solid #dfdfdf;
    -webkit-border-radius: 6px;
    -moz-border-radius: 6px;
    border-radius: 6px;
}
.node-prop table {
	width: 100%;
	margin-left: 10px;
	margin-right: 10px;
}
.node-prop table tr {
    border-bottom: 1px solid #efefef;
}
.node-prop table td {
	line-height: 32px;
    font-size: 12px;
    font-family: 微软雅黑;
}
.node-prop .a{
	color: #dfdfdf;
	width: 100px;
}
</style>
<%=Kit.getDwrJsTag(request,"interface/ZookeeperMgr.js")%>
<%=Kit.getDwrJsTag(request,"engine.js")%>
<%=Kit.getDwrJsTag(request,"util.js")%>
</head>
<body style='overflow-y:hidden;'>
<form>
<input type='hidden' name='path' id='path' value=''>
<input type='hidden' name='ip' id='ip' value="<ww:property value='ip'/>">
<input type='hidden' name='port' id='port' value="<ww:property value='port'/>">
<iframe name='iDownload' id='iDownload' style='display:none'></iframe>
</form>
<TABLE style='width:100%;height:100%'>
<TR><TD width='50%'>
        <div class="panel panel-default">
   			<div class="panel-heading" style='font-size:12px;padding:5px 15px'><i class='fa fa-bars'></i> 节点导航</div>
   			<div class="panel-body" style='padding: 0px;'>
   				<div id='divTree'>
					<ul id='myZtree' class='ztree'></ul>
					<div id="rMenu">
						<ul>
							<!-- <li onclick="mkdir();"><i class='fa fa-plus-circle'></i> 创建节点</li> -->
							<li onclick="deleteNode();" id='liRemove'><i class='fa fa-remove'></i> 删除节点</li>
							<li onclick="downloadNode();" id='liDownload'><i class='fa fa-download'></i> 下载</li>
							<li onclick='exportNodes();' id='liExport'><i class='fa fa-cloud-download'></i> 导出</li>
							<li onclick='importNodes();' id='liImport'><i class='fa fa-cloud-upload'></i> 导入</li>
							<li class="divider"></li>
							<li onclick="setBackup();" id='liSetBackup'><i class='fa fa-copy'></i> 启动自动镜像备份</li>
							<li onclick="cancelBackup();" id='liCancelBackup'><i class='fa fa-unlink'></i> 取消自动镜像备份</li>
							<li onclick="openBackupDir();" id='liOpenBackupDir'><i class='fa fa-eye'></i> 查看镜像备份文件</li>
							<li onclick="createJSONNode();"><i class='fa fa-plus-circle'></i> 创建JSON节点</li>
						</ul>
					</div>
				</div>
   			</div>
   		</div>	
	</TD>
	<TD width='50%' valign='top' style='padding-left:3px;'>
        <div class="panel panel-default">
   			<div class="panel-heading" style='font-size:12px;padding:5px 15px'>
   				<i class='fa fa-check-square-o'></i> <span id='headTitle'>节点预览</span>
                <div style='float:right;right:0px;top:-5px;' id='divSave'>
 				<button type="button" class='btn btn-outline btn-primary btn-xs' onclick='setData()'>
 					<i class='fa fa-edit'></i> 设置</button>
                </div>	
   			</div>
   			<div class="panel-body" style='padding: 0px;'>
				<div class='node-prop'>
					<table>
					<tr><td class='a'>创建时间</td><td id='tdCtime'></td>
						<td class='a'>数据大小</td><td id='tdSize'></td></tr>
					<tr><td class='a'>修改时间</td><td id='tdMtime'></td>
						<td class='a'>版本</td><td id='tdVersion'></td></tr>
		            </table>
				</div>
				<div class='node-prop'>
					<iframe src='' name='iZookeeper' id='iZookeeper' class='nonicescroll' style='width:100%;border:0px;'></iframe>
				</div>
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
		<input type="file" name="uploadfile" id="SysLogo" class="file-loading"/>
    </div>
</div>
</body>
<SCRIPT type="text/javascript">
/*实现窗口对齐*/
function resizeWindow()
{
	var userAgent = navigator.userAgent; //取得浏览器的userAgent字符串
	var isChrome = userAgent.indexOf("Chrome") > -1;
	var h = 38;
	if( isChrome )
	{
		h = 32;
		document.body.style.marginTop = 3;
	}
	
	var div = document.getElementById( 'divTree' );
	div.style.height = windowHeight - h;
	div = document.getElementById( 'iZookeeper' );
	div.style.height = windowHeight - h - 100;
	
}
</SCRIPT>
<%@ include file="../../skit/inc/skit_cos.inc"%>
<%@ include file="../../skit/inc/skit_ztree.inc"%>
<style type='text/css'>
body {
	margin-top:-10px;
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
    padding: 10px 10px;
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
    padding-left: 10px;
    padding-right: 10px;
    padding-top: 2px;
    padding-bottom: 2px;
    cursor: pointer;
}
</style>
<link href="skit/css/fileinput.min.css" rel="stylesheet"/>
<script src="skit/js/fileinput.js"></script>
<script src="skit/js/fileinput_locale_zh.js"></script>
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

var timestamp = <ww:property value='timestamp'/>;
var ip = "<ww:property value='ip'/>";
var port = <ww:property value='port'/>;
//########################################################################
	var setting = {
		check: {
			enable: false
		},
		callback: {
			onRightClick: onRightClick,
			onClick: onClick,
			beforeExpand: beforeExpand,
			onExpand: onExpand,
		},
		view: {
			addDiyDom: addDiyDom
		}
	};

	function onRightClick(event, treeId, treeNode) {
		<ww:if test='grant'>
		document.getElementById( 'liSetBackup' ).style.display = "none";
		document.getElementById( 'liCancelBackup' ).style.display = "none";
		document.getElementById( 'liOpenBackupDir' ).style.display = "none";
		document.getElementById( 'liImport' ).style.display = "none";
		document.getElementById( 'liDownload' ).style.display = "";
		document.getElementById( 'liRemove' ).style.display = "";
		document.getElementById( 'liExport' ).style.display = "";
		if( treeNode.path == "/" || treeNode.path == "/zookeeper"){
			if( treeNode.path == "/" ){
				if( document.getElementById("ico_fa_bak_"+treeNode.tId) ){
					document.getElementById( 'liCancelBackup' ).style.display = "";
					document.getElementById( 'liOpenBackupDir' ).style.display = "";
				}
				else{
					document.getElementById( 'liSetBackup' ).style.display = "";
				}
				document.getElementById( 'liImport' ).style.display = "";
			}
			document.getElementById( 'liRemove' ).style.display = "none";
			document.getElementById( 'liDownload' ).style.display = "none";
		}
		else if( "/cos"== treeNode.path ){
			document.getElementById( 'liRemove' ).style.display = "none";
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
		myZtree.expandNode(treeNode, null, null, null, true);
		//selectTreeNode(treeNode);//myZtree.getSelectedNodes()[0];
		preview(treeNode);
	}
	
	function addDiyDom(treeId, treeNode) {
		var editStr = "";
		if( treeNode.ico )
		{
			editStr = "<i class='fa "+treeNode.ico+"' id='ico_fa_ico_" + treeNode.tId + "'></i>";
		}
		if( treeNode.backup )
		{
			editStr += "<img src='images/icons/images_cached.png' id='ico_fa_bak_" + treeNode.tId + "' title='该伺服器ZooKeeper已设置镜像备份'></i>";
		}
		if( treeNode.warn )
		{
			editStr += "<i class='fa fa-exclamation-triangle' id='ico_fa_warn_" + treeNode.tId + "'></i>";
		}
		if( editStr )
		{
			var aObj = $("#" + treeNode.tId + "_a");
			if( aObj ) aObj.after(editStr);
		}
	}
	
	function onExpand(event, treeId, treeNode) {
		curExpandNode = treeNode;
		ZookeeperMgr.getNodes(treeNode.path, ip, port,{
			callback:function(response){
				if( response.succeed )
				{
					var newnodes = jQuery.parseJSON(response.result);
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
				}
				else skit_alert(response.message);
			},
			timeout:10000,
			errorHandler:function(message) {
				skit_hiddenLoading();
			}
		});	
	}
	$(document).ready(function(){
		var json = '<ww:property value="jsonData" escape="false"/>';
		try
		{
			var zNodes = jQuery.parseJSON(json);
			$.fn.zTree.init($("#myZtree"), setting, zNodes);
			myZtree = $.fn.zTree.getZTreeObj("myZtree");
			myZtreeMenu = $("#rMenu");
			var nodes = myZtree.getNodes();
			myZtree.selectNode(nodes[nodes.length-1]);
			preview(nodes[nodes.length-1]);
		}
		catch(e)
		{
			alert("初始化异常"+e);
		}
	});
//#########################################################################
function openBackupDir(){
	var title = "查看伺服器【"+ip+":"+port+"】ZooKeeper镜像备份文件";
	var url = "files!edit.action?ip=127.0.0.1&port=0&path=data/zkbak/"+ip+"_"+port+"/*.zip";
	window.setTimeout("openView('"+title+"','"+url+"');", 500);
}

function preview(node)
{
	//document.getElementById( 'tdName' ).innerHTML = node.name;
	document.getElementById( 'tdCtime' ).innerHTML = node.ctime?node.ctime:"";
	document.getElementById( 'tdMtime' ).innerHTML = node.mtime?node.mtime:"";
	document.getElementById( 'tdVersion' ).innerHTML = node.version?node.version:"";
	document.getElementById( 'tdSize' ).innerHTML = node.size?node.size:"";
	document.getElementById( 'headTitle' ).innerHTML = "节点预览 "+node.path; 
	document.getElementById( 'path' ).value = node.path;
	document.forms[0].action = "zookeeper!preview.action";
	document.forms[0].method = "POST";
	document.forms[0].target = "iZookeeper";
	document.forms[0].submit();
}
function createJSONNode(){
	hideRMenu();
	var nodes = myZtree.getSelectedNodes();
	if( nodes == null || nodes.length == 0 ) return;
	var node = nodes[0];
	skit_input("请输入您要添加的JSON节点名称", "", function(yes, name){
		if( yes ){
			ZookeeperMgr.createJSONNode(ip, port, node.path+"/"+name,{
				callback:function(response){
					skit_alert(response.message);
				},
				timeout:10000,
				errorHandler:function(message) {
					skit_hiddenLoading();
				}
			});	
		}
	});
}
//设置数据
function setData(){
    var ifr = window.frames["iZookeeper"];
    if( ifr && ifr.getValue )
    {
    	var nodes = myZtree.getSelectedNodes();
    	if( nodes == null || nodes.length == 0 ) return;
    	var node = nodes[0];
    	var text = ifr.getValue();
		skit_confirm("你确定要设置节点【"+node.path+"】数据？", function(yes){
			if( yes )
			{
				ZookeeperMgr.setData(ip, port, node.path, text,{
					callback:function(response){
						skit_alert(response.message);
					},
					timeout:10000,
					errorHandler:function(message) {
						skit_hiddenLoading();
					}
				});	
			}
		});
    }
    else{
		skit_alert("该节点数据类型不支持设置数据");
    }
}
//升级
function setBackup()
{
	hideRMenu();
	var nodes = myZtree.getSelectedNodes();
	if( nodes == null || nodes.length == 0 ) return;
	var node = nodes[0];
	skit_confirm("你确定要启动该伺服器ZooKeeper自动镜像备份？<br/>启动后每小时会镜像备份【"+node.path+"】以下整个ZooKeeper节点数据", function(yes){
		if( yes )
		{
			ZookeeperMgr.setBackup(ip, port, node.path,{
				callback:function(response){
					skit_alert(response.message);
					if( response.succeed )
					{
						node["backup"] = true;
						var aObj = $("#" + node.tId + "_a");
						if( aObj ) aObj.after("<img src='images/icons/images_cached.png' id='ico_fa_bak_" + node.tId + "' title='该伺服器ZooKeeper已设置镜像备份'></i>");
					}
				},
				timeout:10000,
				errorHandler:function(message) {
					skit_hiddenLoading();
				}
			});	
		}
	});
}

function cancelBackup()
{
	hideRMenu();
	var nodes = myZtree.getSelectedNodes();
	if( nodes == null || nodes.length == 0 ) return;
	var node = nodes[0];
	skit_confirm("你确定要取消该伺服器ZooKeeper自动镜像备份？<br/>取消后将停止备份并删除已有的镜像数据文件", function(yes){
		if( yes )
		{
			ZookeeperMgr.cancelBackup(ip, port, node.path,{
				callback:function(response){
					skit_alert(response.message);
					if( response.succeed )
					{
						node["backup"] = false;
						var ico_fa_bak = document.getElementById( "ico_fa_bak_" + node.tId);
						if( ico_fa_bak ) ico_fa_bak.parentNode.removeChild(ico_fa_bak);
					}
				},
				timeout:10000,
				errorHandler:function(message) {
					skit_hiddenLoading();
				}
			});	
		}
	});
}

//删除节点
function deleteNode()
{
	hideRMenu();
	var nodes = myZtree.getSelectedNodes();
	if( nodes == null || nodes.length == 0 ) return;
	if( nodes[0].path == "/" ) return;
	skit_confirm("你确定要删除节点【"+nodes[0].path+"】？", function(yes){
		if( yes )
		{
			nodeDeleter = nodes[0];
			if( nodeDeleter.path == "/cos" )
			{
				window.setTimeout("confirmDeleteNode()",1000);
				return;
			}
			ZookeeperMgr.deleteNode(ip, port, nodeDeleter.path,{
				callback:function(response){
					if( response.succeed )
					{
					    skit_showLoading();
						idDeleter = response.result;
						document.getElementById( 'headTitle' ).innerHTML = "节点删除中…… "+nodeDeleter.path+" "+idDeleter; 
						window.setTimeout("checkDeleteResult()",1000);
					}
					else skit_alert(response.message);
				},
				timeout:10000,
				errorHandler:function(message) {
					skit_hiddenLoading();
				}
			});	
		}
	});
}
//删除重要节点
function confirmDeleteNode()
{
	skit_confirm("该节点【"+nodeDeleter.path+"】是系统重要节点，你确定要删除？", function(yes){
		if( yes )
		{
			ZookeeperMgr.deleteNode(ip, port, nodeDeleter.path,{
				callback:function(response){
					if( response.succeed )
					{
					    skit_showLoading();
						idDeleter = response.result;
						window.setTimeout("checkDeleteResult()",1000);
					}
					else skit_alert(response.message);
				},
				timeout:10000,
				errorHandler:function(message) {
					skit_hiddenLoading();
				}
			});	
		}
	});
}
var idDeleter;
var nodeDeleter;
//检查删除结果
function checkDeleteResult()
{
	ZookeeperMgr.getDeleteResult(idDeleter, {
		callback:function(response){
			if( response.succeed )
			{//表示正在执行升级
				skit_alert("执行进度"+response.result+"%");
				window.setTimeout("checkDeleteResult()",1000);//30秒执行一次系统消息检查
			}
			else
			{
				skit_hiddenLoading();
				if( response.result == 100 )
				{
					var nodeParent = nodeDeleter.getParentNode();
					myZtree.removeNode(nodeDeleter);
					myZtree.selectNode(nodeParent);
					preview(nodeParent);
				}
				else
				{
					skit_alert(response.message);
				}
			}
		},
		timeout:10000,
		errorHandler:function(message) {skit_alert("检查删除进度出现异常："+message)}
	});	
}

function exportNodes(){
	hideRMenu();
	document.forms[0].action = "zookeeper!export.action";
	document.forms[0].method = "POST";
	document.forms[0].target = "iDownload";
	document.forms[0].submit();
}

function closeuploadfile()
{
	document.getElementById( 'divUploadfile' ).style.display = "none";
	$("#SysLogo").fileinput("destroy");
}

function importNodes()
{
	hideRMenu();
	skit_confirm("执行数据导入会直接覆盖现有数据，为了确保不出错，请考虑先执行导出备份操作。<B>请确认是否执行导入？</B>", function(yes){
		if( yes )
		{
			try
			{
				document.getElementById( 'uploadtitle' ).innerHTML = "上传ZooKeeper数据文件(*.zip)";
				var divUploadfile = document.getElementById( 'divUploadfile' );
				divUploadfile.style.display = "";
				$("#SysLogo").fileinput({
						language: 'zh', //设置语言
						uploadUrl: 'zookeeper!importdata.action', //上传的地址
						allowedFileExtensions: ['zip'],//接收的文件后缀
						showUpload: true, //是否显示上传按钮
						showCaption: false,//是否显示标题
						showClose: false,
						showPreview: true,
						browseClass: "btn btn-primary", //按钮样式     
						previewSettings: { image: {width: "auto", height: "128px"} },
						//dropZoneEnabled: false,//是否显示拖拽区域
						minImageWidth: 128, //图片的最小宽度
						minImageHeight: 128,//图片的最小高度
						maxImageWidth: 128,//图片的最大宽度
						maxImageHeight: 128,//图片的最大高度
						//maxFileSize: 0,//单位为kb，如果为0表示不限制文件大小
						//minFileCount: 0,
						maxFileCount: 1, //表示允许同时上传的最大文件个数
						enctype: 'multipart/form-data',
						validateInitialCount:true,
						previewFileIcon: "<i class='glyphicon glyphicon-king'></i>",
						msgFilesTooMany: "选择上传的文件数量({n}) 超过允许的最大数值{m}！",
			            uploadExtraData: function(previewId, index) {   //额外参数的关键点
			                var obj = {};
			                return obj;
			            }
				});
				divUploadfile.style.top = 64;
				divUploadfile.style.left = windowWidth/2 - divUploadfile.clientWidth/2;
				//$( '.file-preview' ).hide();
				$("#SysLogo").on("fileloaded", function (data, previewId, index) {
					var tips = "您确定要上传["+previewId.name+"]的覆盖ZooKeeper吗？";
					document.getElementById( 'uploadtitle' ).innerHTML = tips;
				});
				//导入文件上传完成之后的事件
				$("#SysLogo").on("fileuploaded", function (event, data, previewId, index) {
					if( data.response.succeed )
					{
						skit_alert(data.response.alt, "成功提示", function(){
							parent.open();
						});
					}
					else{
						skit_alert(data.response.alt, "错误提示");
					}
				});
			}
			catch (e)
			{
				alert(e);
			}			
		}
	});
}

function downloadNode()
{
	hideRMenu();
	document.forms[0].method = "POST";
	document.forms[0].action = "zookeeper!download.action";
	document.forms[0].target = "iDownload";
	document.forms[0].submit();
}
</SCRIPT>
</html>