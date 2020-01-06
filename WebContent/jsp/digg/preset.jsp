<%@ page language="java" pageEncoding="UTF-8"%>
<%@ page import="com.focus.cos.web.common.Kit"%>
<%@ taglib uri="/webwork" prefix="ww" %>
<%Kit.noCache(request,response);  %>
<html>
<head>
<link type="text/css" href="skit/ztree/css/zTreeStyle/zTreeStyle.css" rel="stylesheet"/>
<link type="text/css" href="skit/css/bootstrap.css" rel="stylesheet">
<link type="text/css" href="skit/css/costable.css?v=3" rel="stylesheet">
<link type="text/css" href="skit/css/bootstrap-tour.min.css" rel="stylesheet">
<link href="skit/css/awesome-bootstrap-checkbox.css" rel="stylesheet">
<style type='text/css'>
</style>
<%=Kit.getDwrJsTag(request,"interface/DiggConfigMgr.js")%>
<%=Kit.getDwrJsTag(request,"engine.js")%>
<%=Kit.getDwrJsTag(request,"util.js")%>
<SCRIPT type="text/javascript">

function changeDatamodelType(sel)
{
	changeDatamodel(sel.value);
}
function changeDatamodel(type)
{
	var divDatamodelType = document.getElementById('divDatamodelType');
    var formDatamodelValue = document.getElementById('formDatamodelValue');
    var formModeEncrypte = document.getElementById('formModeEncrypte');
	var jsonurl = document.getElementById('jsonurl');
	var zkpath = document.getElementById('zkpath');
	var spanSorting = document.getElementById('spanSorting');
	var spanPagesize = document.getElementById('spanPagesize');
	var sorting = document.getElementById('sorting');
	var pagesize = document.getElementById('pagesize');
	jsonurl.style.display = "none";
	zkpath.style.display = "none";
	spanPagesize.style.display = "";
	spanSorting.style.display = "";
	pagesize.style.display = "";
	sorting.style.display = "";
	formDatamodelValue.style.display = "";
	formModeEncrypte.style.display = "none";
	if("zookeeper"==type)
	{
		jsonurl.style.display = "none";
		zkpath.style.display = "";
		spanPagesize.style.display = "none";
		spanSorting.style.display = "none";
		pagesize.style.display = "none";
		sorting.style.display = "none";
		formModeEncrypte.style.display = "";
	}
	else if("json"==type)
	{
		jsonurl.style.display = "";
		zkpath.style.display = "none";
	}
	else if("digg"==type)
	{
		divDatamodelType.style.width = 532;
		formDatamodelValue.style.display = "none";
	}
}
</SCRIPT>
</head>
<body style='overflow-y:hidden;padding-top:0px;padding-left:0px;'>
<TABLE>
<TR class='unline'><TD width='250' valign='top' id='tdTree' align='center'>
		<div id='divTree'>
			<ul id='myZtree' class='ztree'></ul>
			<div id="rMenu">
				<ul>
					<li onclick="importCells();" id='liImportCell'><i class='skit_fa_icon_blue fa fa-cloud-upload'></i> 从数据源导入查询字段</li>
					<li onclick="addCell();" id='liAddCell'><i class='skit_fa_icon_blue fa fa-plus-circle'></i> 新增查询字段</li>
					<li onclick="addCellGroup();" id='liAddCellgroup'><i class='skit_fa_icon_blue fa fa-plus-circle'></i> 新增字段分组</li>
					<li onclick="addFilter();" id='liAddFilter'><i class='skit_fa_icon_red fa fa-minus-circle'></i> 新增查询过滤器</li>
					<li onclick="addLabel();" id='liAddLabel'><i class='skit_fa_icon_red fa fa-minus-circle'></i> 新增字段数据转换</li>
					<li onclick="addCellstyle();" id='liAddCellstyle'><i class='skit_fa_icon_red fa fa-minus-circle'></i> 新增列样式</li>
					<li onclick="addRowstyle();" id='liAddRowstyle'><i class='skit_fa_icon_red fa fa-minus-circle'></i> 新增行样式</li>
					<li onclick="addRender();" id='liAddRender'><i class='skit_fa_icon_red fa fa-minus-circle'></i> 新增字段渲染器</li>
					<li onclick="addDigg();" id='liAddDigg'><i class='skit_fa_icon fa fa-upload'></i> 新增数据映射规则</li>
					<li onclick="addToolbar();" id='liAddToolbar'><i class='skit_fa_icon fa fa-download'></i> 新增工具栏按钮</li>
					<li onclick="addDetail();" id='liAddDetail'><i class='skit_fa_icon fa fa-download'></i> 新增扩展详情</li>
					<li onclick="remove();" id='liRemove'><i class='skit_fa_icon_red fa fa-minus-circle'></i> 删除</li>
				</ul>
			</div>
		</div>
		<div style='width:220px;margin-top:10px' id='divSet'>
			<button type="button" class="btn btn-outline btn-success btn-block"
				style='width:96px;float:left;padding-right:10px;' onclick='saveDiggConfig();'><i class="fa fa-save"></i> 保存</button>
			<button type="button" class="btn btn-outline btn-info btn-block"
				style='width:96px;padding-left:10px;float:right;margin-top:0px;' onclick='previewDiggConfig();'><i class="fa fa-bug"></i> 预览调测 </button>
		</div>
	</TD>
	<TD valign='top' valign='top' style='padding-left:0px;'>
		<div class="alert alert-warning" id='divAlert' style='display:none'>
		    <a href="#" class="close" data-dismiss="alert">&times;</a>
		    <span id='spanAlert'><strong>警告！</strong>您的网络连接有问题。</span>
		</div>
		<div class="panel panel-default" id='cellgroup' style='display:none'>
			<div class="panel-heading" style='font-size:12px;padding:5px 15px'><i class='skit_fa_btn fa fa-user'></i>字段分组</div>
			<div class="panel-body">
				<div class="form-group">
					<div class="input-group">
						<span class="input-group-addon">字段标题<span class='fa fa-align-center' style='margin-left:6px'></span></span>
						<input class="form-control" type="text" id='cellgroup.title'
	                	data-title='元数据查询名称只能含有汉字、数字、字母、下划线不能以下划线开头和结尾;程序名称不少于2个字，不多于20个字'
						data-toggle="tooltip" 
						data-placement="bottom"
						data-trigger='manual'
						onkeydown='$("#title").tooltip("hide")'
	                	placeholder="请输入您元数据查询的名称"							
						>
	   					<div class="input-group-btn">
	 						<button type="button" class="btn btn-info dropdown-toggle" data-toggle="dropdown" aria-expanded="false" style='display: inline-block;height:34px;'><span class="caret"></span></button>
	                        <ul class="dropdown-menu pull-right">
	                            <li><a onclick='changeAlign("left")'>居左</a></li>
	                            <li><a onclick='changeAlign("center")'>居中</a></li>
	                            <li><a onclick='changeAlign("right")'>居右</a></li>
	                        </ul>	
						</div>
					</div>
				</div>
			</div>
		</div>
		<div class="panel panel-default" id='cell' style='display:none'>
			<div class="panel-heading" style='font-size:12px;padding:5px 15px'><i class='skit_fa_btn fa fa-user'></i>查询列表字段</div>
			<div class="panel-body">
				<div class="form-group">
	     			<div class="input-group" style='display:'>
						<span class="input-group-addon" id='spanMode'>数据类型<i class='fa fa-globe' style='margin-left:6px'></i></span>
						<select class="form-control" style='font-size:12px' onchange='changeCellType()' id='cell.dataType'>
							<option value='string'>string: 字符串类型</option>
							<option value='int'>int: 整形</option>
							<option value='long'>long: 长整形</option>
							<option value='long'>bool: 布尔型</option>
							<option value='checkBoxSelection'>checkbox: 复选框</option>
							<option value='tag'>tag: 以标签表现</option>
						</select>
					</div>
				</div>
				<div class="form-group">
					<div class="input-group">
						<span class="input-group-addon">字段标识<span class='fa fa-info-circle' style='margin-left:6px'></span></span>
						<input class="form-control" id="cell.id" type="text"
	                	data-title='请输入您程序的软件英文标识。只能含有字母、数字、下划线;程序标识不少于2个字，不多于15个字'
						data-toggle="tooltip" 
						data-placement="bottom"
						data-trigger='manual'
						onkeydown='$("#programid").tooltip("hide")'
	                	placeholder="请输入新增字段的英文标识，默认与您的数据源字段定义一致"					
						>
					</div>
				</div>
				<div class="form-group">
					<div class="input-group">
						<span class="input-group-addon">字段标题<span class='fa fa-align-left' style='margin-left:6px'></span></span>
						<input class="form-control" type="text" id='cell.title'
	                	data-title='元数据查询名称只能含有汉字、数字、字母、下划线不能以下划线开头和结尾;程序名称不少于2个字，不多于20个字'
						data-toggle="tooltip" 
						data-placement="bottom"
						data-trigger='manual'
						onkeydown='$("#title").tooltip("hide")'
	                	placeholder="请输入您元数据查询的名称"							
						>
	   					<div class="input-group-btn">
	 						<button type="button" class="btn btn-info dropdown-toggle" data-toggle="dropdown" aria-expanded="false" style='display: inline-block;height:34px;'><span class="caret"></span></button>
	                        <ul class="dropdown-menu pull-right">
	                            <li><a onclick='changeAlign("left")'>居左</a></li>
	                            <li><a onclick='changeAlign("center")'>居中</a></li>
	                            <li><a onclick='changeAlign("right")'>居右</a></li>
	                        </ul>	
						</div>
					</div>
				</div>
				<div class="form-group" id='div-timerange'>
					<div class="input-group" style='width:360px' title='设置该字段在列表中宽度'>
						<span class="input-group-addon"><i class='fa fa-text-width' style='margin-right:6px'></i>固定列宽</span>
						<input class="form-control" type="text"
							placeholder="100"
							id='cell.minxWidth' readonly
							style='width:64px;border-right: 1px solid #eee;'
	   						data-title='请输入运行起始正确的时钟时间'
							data-toggle="tooltip" 
							data-placement="bottom"
							data-trigger='manual'
							onblur='$("#starttime").tooltip("hide")'>
						<span class="input-group-addon"> 最长列宽 </span>
						<input class="form-control" type="text"
							placeholder="100" 
							id='cell.maxWidth' readonly
							style='width:64px;border-left: 1px solid #eee;;border-right: 1px solid #eee;'
	   						data-title='最长列宽必须大于最小列宽'
							data-toggle="tooltip" 
							data-placement="bottom"
							data-trigger='manual'
							onblur='$("#endtime").tooltip("hide")'>
						<span class="input-group-addon" style='border-right: 1px solid #eee;'>列宽可自适应<span class='fa fa-toggle-on' style='margin-left:6px'></span></span>
						<span class='input-group-addon' style='border-left: 1px solid #eee;'>
							<div class='checkbox checkbox-info' style='cursor:pointer;padding-left: 0px'>
								<input type='checkbox' id="cell.resizable" onclick='activeResizable(this)' title='激活参数'>
								<label></label>
							</div>
						</span>					
					</div>
				</div>
				<div class="form-group">
					<div class="input-group" style='width:160px' title=''>
						<span class="input-group-addon">字段可编辑<span class='fa fa-edit' style='margin-left:6px'></span></span>
						<span class='input-group-addon' style='border-left: 1px solid #eee;'>
							<div class='checkbox checkbox-info' style='cursor:pointer;padding-left: 0px'>
								<input type='checkbox' id="cell.editable" title=''>
								<label></label>
							</div>
						</span>
					</div>
				</div>
				<div class="form-group">
					<div class="input-group" style='width:160px' title=''>
						<span class="input-group-addon">字段可排序<span class='fa fa-sort' style='margin-left:6px'></span></span>
						<span class='input-group-addon' style='border-left: 1px solid #eee;'>
							<div class='checkbox checkbox-info' style='cursor:pointer;padding-left: 0px'>
								<input type='checkbox' id="cell.sortable" title=''>
								<label></label>
							</div>
						</span>
					</div>
				</div>
			</div>
		</div>
		<div class="panel panel-default" id='datamodel' style='display:none'>
			<div class="panel-heading" style='font-size:12px;padding:5px 15px'><i class='skit_fa_icon fa fa-database'></i> 数据模型配置
		        <div style='float:right;padding-right:4px;top:0px;display:;' id='divUpload'>
		 			<button type="button" class="btn btn-outline btn-primary btn-xs" onclick='beautifyScriptset()'>
		 				<i class='fa fa-outdent'></i> 格式化</button>
		        </div></div>
			<div class="panel-body">
				<div class="form-group">
	     			<div class="input-group" id='divDatamodelType'>
						<span class="input-group-addon">模型类型<i class='fa fa-globe' style='margin-left:6px'></i></span>
						<select class="form-control form-control-font" style='font-size:12px;width:128px;' id='datamodeltype' onchange='changeDatamodelType(this)'>
							<option value='digg'>数据源映射</option>
							<option value='json'>JSON远程调用</option>
							<option value='zookeeper'>ZooKeeper映射</option>
						</select>
						<span class="input-group-addon input-group-addon-font" id='spanSorting' style='border-left: 1px solid #eee;border-right: 1px solid #eee;'
							><span class='fa fa-list-ol'></span>排序</span>
						<select class="form-control form-control-font" style='font-size:12px' id='sorting'>
							<option value='local'>本地处理</option>
							<option value='remote'>远程处理</option>
						</select>
						<span class="input-group-addon" id='spanPagesize'>分页 <i class='fa fa-bookmark'></i></span>
						<select class="form-control form-control-font" style='font-size:12px;width:96px' id='pagesize'>
							<option value='20'>20条/页</option>
							<option value='10'>10条/页</option>
							<option value='50'>50条/页</option>
							<option value='100'>100条/页</option>
							<option value='0'>不分页</option>
						</select>
					</div>
				</div>
				<div class="form-group" id='formDatamodelValue' style='display:none'>
	     			<div class="input-group">
						<span class="input-group-addon">数据地址 <i class='fa fa-share-alt'></i></span>
						<input class="form-control form-control-font" id="jsonurl" type="text" value=''
								style='display:none'
				             	data-title='请输入数据的JSON的URL地址'
								data-toggle="tooltip" 
								data-placement="bottom"
								data-trigger='manual'
								onkeydown='$("#jsonurl").tooltip("hide")'
								onblur='$("#jsonurl").tooltip("hide")'
				             	placeholder="表格数据来自于URL访问地址返回的JSON数组对象"					
						>
						<input class="form-control form-control-font" id="zkpath" type="text" value=''
								style='display:none'
				             	data-title='请输入正确的ZooKeeper节点地址，例如/cos/config/modules'
								data-toggle="tooltip" 
								data-placement="bottom"
								data-trigger='manual'
								onkeydown='$("#zkpath").tooltip("hide")'
								onblur='$("#zkpath").tooltip("hide")'
				             	placeholder="表格数据来自于本系统ZooKeeper路径地址中的数据"					
						>
					</div>
				</div>
				<div class="form-group" id='formModeEncrypte' style='display:none;width:480px;'>
					<div class="input-group">
						<span class="input-group-addon">节点类型 <i class='fa fa-cogs'></i></span>
						<select class="form-control form-control-font" style='font-size:12px' id='zkmode'>
							<option value='0'>配置数据在子节点</option>
							<option value='1'>配置数据在当前节点</option>
							<option value='2'>配置数据在当前节点中某个字段</option>
						</select>
						<span class="input-group-addon input-group-addon-font" style='border-left: 1px solid #eee;border-right: 1px solid #eee;'
							><span class='fa fa-lock'></span>数据加密</span>
						<span class='input-group-addon' style='border-left: 1px solid #eee;'>
							<div class='checkbox checkbox-info' style='padding-left: 0px'>
								<input type='checkbox' id='encrypte' style='cursor:pointer;' title="是否加密">
								<label></label>
							</div>
						</span>
					</div>
				</div>
				<iframe id='scriptDatamodel' name='iScriptDatamodel' class='nonicescroll' style='width:100%;border:0px solid #eee;margin-left:0px;margin-top:0px;margin-bottom:0px;'></iframe>
			</div>
		</div>
		<iframe id='x' name='iPreview' class='nonicescroll' style='display:none;width:100%;border:0px solid #eee;margin-left:0px;margin-top:0px;margin-bottom:0px;'></iframe>
		<iframe id='grid' name='iGrid' class='nonicescroll' style='display:none;width:100%;border:0px solid #eee;margin-left:0px;margin-top:0px;margin-bottom:0px;'></iframe>
		<div class="panel panel-default" id='scriptset' style='display:none'>
			<div class="panel-heading" style='font-size:12px;padding:5px 15px'>
				<span id='spanScriptsetTitle'><i class='skit_fa_icon fa fa-edit'></i>设置脚本</span>
		        <div style='float:right;padding-right:4px;top:0px;display:;' id='divUpload'>
		 			<button type="button" class="btn btn-outline btn-primary btn-xs" onclick='beautifyScriptset()'>
		 				<i class='fa fa-outdent'></i> 格式化</button>
		        </div>
			</div>
			<div class="panel-body">
				<iframe id='script' name='iScript' class='nonicescroll' style='width:100%;border:0px solid #eee;margin-left:0px;margin-top:0px;margin-bottom:0px;'></iframe>
			</div>
		</div>
	</TD>
</TR>
</TABLE>
<form id='form0'>
<input type='hidden' name='gridxml' id='gridxml' value="<ww:property value='gridxml'/>">
<input type='hidden' name='gridtext' id='gridtext'>
<input type='hidden' name='id' id='id'>
<iframe name='iDownload' id='iDownload' style='display:none'></iframe>
</form>
<div id='divMask' class='skit_mask' onclick='minScriptset(true)' style='cursor:pointer;' title='点击关闭弹窗'></div>
</body>
<SCRIPT type="text/javascript">
/*实现窗口对齐*/
function resizeWindow()
{
	var div = null;
	if( document.getElementById( 'tdTree' ).style.display == "" )
	{
		div = document.getElementById( 'divTree' );
		div.style.width = 250;
		div.style.height = windowHeight - 48;
	}

	if( document.getElementById('x').style.display == "" )
	{
		div = document.getElementById( 'x' );
		div.style.height = windowHeight - 5;
	}
	if( document.getElementById('grid').style.display == "" )
	{
		div = document.getElementById( 'grid' );
		div.style.height = windowHeight - 5;
	}
	if( document.getElementById('scriptset').style.display == "" )
	{
		div = document.getElementById( 'script' );
		div.style.height = windowHeight - 64;
		document.getElementById('scriptset').style.width = windowWidth - 256;
	}
	if( document.getElementById('cell').style.display == "" )
	{
		document.getElementById('cell').style.width = windowWidth - 256;
	}
	if( document.getElementById('datamodel').style.display == "" )
	{
		document.getElementById('datamodel').style.width = windowWidth - 256;
		div = document.getElementById( 'scriptDatamodel' );
		div.style.height = windowHeight - 108;
	}
	if( document.getElementById( 'script' ).src == "" )
	{
		document.getElementById( 'script' ).src = "diggcfg!scriptset.action?ww="+(windowWidth-280);
		document.getElementById( 'scriptDatamodel' ).src = "diggcfg!scriptset.action?ww="+(windowWidth-280);
		//alert(document.getElementById( 'script' ).src);
	}
}
//#########################################################################
var displayPanel;
function open(node)
{
	if( displayPanel )
	{
		displayPanel.style.display = "none";
	}
	var div = document.getElementById(node.id);
	if( div )
	{
		if( node.id == "x" )
		{
			document.forms[0].method = "POST";
			document.forms[0].action = "diggcfg!stylepreview.action";
			document.forms[0].target = "iPreview";
			document.forms[0].submit();
		}
		else
		{
			setUserActionMemory(document.referrer, node.id);
		}
	}
	else
	{
		if( node.id == "render" || node.id == "javascript" || node.id == "globalscript" )
		{
			document.getElementById("spanScriptsetTitle").innerHTML = "<i class='skit_fa_icon fa fa-edit'></i> "+node.name;
			if(node.id == "javascript" || node.id == "globalscript") setUserActionMemory(document.referrer, node.id);
			div = document.getElementById("scriptset");
		    var ifr = window.frames["iScript"];
		    if( ifr && ifr.setScript )
		    {
		    	ifr.setScript(unicode2Chr(node.cdata));
		    }
		}
		
	}
	displayPanel = div;
	displayPanel.style.display = "";
	if( node.id == "datamodel" )
	{
		changeDatamodel(node.type);
		var ifr = window.frames["iScriptDatamodel"];
	    if( ifr && ifr.setScript )
	    {
	    	ifr.setScript(unicode2Chr(node.cdata));
	    }
	}
	resizeWindow();
}

function openGridScriptset(type, tId)
{
	var node = myZtree.getNodeByParam("tId", tId);
	if( node )
	{
		node = myZtree.getNodeByParam("id", type, node);
		//alert(unicode2Chr(node.cdata));
		myZtree.selectNode(node);
		myZtree.expandNode(node, true);
		open(node);
	}
	else
	{
		skit_alert("未知脚本节点");
	}
}

function beautifyScriptset()
{
	var nodes = myZtree.getSelectedNodes();
	if( !nodes || nodes.length == 0)
	{
		skit_alert("请先在目录导航树上选择您要设置的配置项");
		return;
	}
	var node = nodes[0];
    var ifr = node.id=="datamodel"?window.frames["iScriptDatamodel"]:window.frames["iScript"];
    if( ifr && ifr.beautifyScript )
    {
        ifr.beautifyScript();
        node.cdata = chr2Unicode(ifr.getScript());
		var ico = document.getElementById("ico_abc_" + node.tId);
		ico.className = "skit_fa_icon_orange fa fa-edit";
    }
}

function changeScriptset(script)
{
	var nodes = myZtree.getSelectedNodes();
	if( !nodes || nodes.length == 0)
	{
		skit_alert("请先在目录导航树上选择您要设置的配置项");
		return;
	}
	var node = nodes[0];
    node.cdata = chr2Unicode(script);
	var ico = document.getElementById("ico_abc_" + node.tId);
	ico.className = "skit_fa_icon_orange fa fa-edit";
}

function getScriptset()
{
	var nodes = myZtree.getSelectedNodes();
	if( !nodes || nodes.length == 0)
	{
		skit_alert("请先在目录导航树上选择您要删除的配置项");
		return;
	}
	var node = nodes[0];
	if( node.cdata )
	{
	    var ifr = node.id=="datamodel"?window.frames["iScriptDatamodel"]:window.frames["iScript"];
	    if( ifr && ifr.getScript )
	    {
	    	ifr.setScript(unicode2Chr(node.cdata));
	    }
	}
}

</SCRIPT>
<%@ include file="../../skit/inc/skit_cos.inc"%>
<%@ include file="../../skit/inc/skit_ztree.inc"%>
<%@ include file="../../skit/inc/skit_bootstrap.inc"%>
<script src="skit/js/bootstrap-tour.min.js"></script>
<script src="skit/js/jquery.inputmask.bundle.min.js"></script>
<style type='text/css'>
body {
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
var grant = <ww:property value='grant'/>;//管理程序的权限

//########################################################################
	var setting = {
		//editable: false,
		//fontCss : {color:"red"},
		callback: {
			onRightClick: onRightClick,
			onClick: onClick,
			beforeExpand: beforeExpand,
			onExpand: onExpand
		},
		view: {
			addHoverDom: addHoverDom,
			removeHoverDom: removeHoverDom,
			addDiyDom: addDiyDom
		},
		edit: {
			drag: {
				autoExpandTrigger: true,
				prev: dropPrev,
				inner: dropInner,
				next: dropNext
			},
			enable: true,
			showRemoveBtn: showRemoveBtn,
			showRenameBtn: false
		},
	};
	
	function showRemoveBtn(treeId, treeNode)
	{
		return treeNode.id == "cell" || treeNode.id == "cellgroup" || treeNode.id == "digg";
	}

	function removeHoverDom(treeId, treeNode) {
		$("#addBtn_"+treeNode.tId).unbind().remove();
	};

	function addHoverDom(treeId, treeNode) 
	{
		if( treeNode.id != "grid" &&
			treeNode.id != "data" &&
			treeNode.id != "cellgroup" &&
			treeNode.id != "toolbar" &&
			treeNode.id != "details" ) return;
		var sObj = $("#" + treeNode.tId + "_span");
		if ( $("#addBtn_"+treeNode.tId).length>0) return;
		var tips = "";
		tips = treeNode.id=="grid"?"新增查询字段":tips;
		tips = treeNode.id=="data"?"新增数据映射":tips;
		tips = treeNode.id=="cellgroup"?"新增分组查询字段":tips;
		tips = treeNode.id=="toolbar"?"新增工具栏按钮":tips;
		tips = treeNode.id=="details"?"新增扩展详情":tips;
		var addStr = "<span class='button add' id='addBtn_" + treeNode.tId+ "' title='"+tips+"' onfocus='this.blur();'></span>";
		sObj.after(addStr);
		var btn = $("#addBtn_"+treeNode.tId);
		if (btn) btn.bind("click", function(){
			if( treeNode.id=="grid" || treeNode.id=="cellgroup" ) addCell();
			else if( treeNode.id=="toolbar" ) addToolbar();
			else if( treeNode.id=="details" ) addDetail();
			else if( treeNode.id=="data" ) addDigg();
		});
	};

	function onRightClick(event, treeId, treeNode) 
	{
		document.getElementById( 'liImportCell' ).style.display = "none";
		document.getElementById( 'liAddCell' ).style.display = "none";
		document.getElementById( 'liAddCellgroup' ).style.display = "none";
		document.getElementById( 'liAddFilter' ).style.display = "none";
		document.getElementById( 'liAddLabel' ).style.display = "none";
		document.getElementById( 'liAddCellstyle' ).style.display = "none";
		document.getElementById( 'liAddRowstyle' ).style.display = "none";
		document.getElementById( 'liAddRender' ).style.display = "none";
		document.getElementById( 'liAddDigg' ).style.display = "none";
		document.getElementById( 'liAddToolbar' ).style.display = "none";
		document.getElementById( 'liAddDetail' ).style.display = "none";
		document.getElementById( 'liRemove' ).style.display = "none";
		if( treeNode.id == "x" )
		{
			document.getElementById( 'liImportCell' ).style.display = "";
		}
		else if( treeNode.id == "grid" )
		{
			document.getElementById( 'liImportCell' ).style.display = "";
			document.getElementById( 'liAddCell' ).style.display = "";
			document.getElementById( 'liAddCellgroup' ).style.display = "";
		}
		else if( treeNode.id == "cellgroup" )
		{
			document.getElementById( 'liAddCell' ).style.display = "";
		}
		else if( treeNode.id == "cell" )
		{
			document.getElementById( 'liAddFilter' ).style.display = "";
			document.getElementById( 'liAddLabel' ).style.display = "";
			document.getElementById( 'liAddCellstyle' ).style.display = "";
			document.getElementById( 'liAddRowstyle' ).style.display = "";
			document.getElementById( 'liAddRender' ).style.display = "";
			document.getElementById( 'liRemove' ).style.display = "";
		}
		else if( treeNode.id == "data" )
		{
			document.getElementById( 'liAddDigg' ).style.display = "";
		}
		else if( treeNode.id == "toolbar" )
		{
			document.getElementById( 'liAddToolbar' ).style.display = "";
		}
		else if( treeNode.id == "details" )
		{
			document.getElementById( 'liAddDetail' ).style.display = "";
		}
		else
		{
			document.getElementById( 'liRemove' ).style.display = "";
			return;
		}
	
		if (!treeNode && event.target.tagName.toLowerCase() != "button" && $(event.target).parents("a").length == 0) {
			myZtree.cancelSelectedNode();
			showRMenu("root", event.clientX, event.clientY);
		} else if (treeNode && !treeNode.noR) {
			myZtree.selectNode(treeNode);
			showRMenu("node", event.clientX, event.clientY);
		}
	}

	function addDiyDom(treeId, treeNode)
	{
		var treeItem = $("#" + treeNode.tId + "_a");
		var ico = "skit_fa_icon fa fa-check-circle";
		var title = "模板自检未执行";
		treeItem.before("<i id='ico_abc_"+treeNode.tId+"' class='"+ico+"' title='"+title+"' style='font-size:14px;'></i>");
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
			//expandAll(myZtree);

			var grid = myZtree.getNodeByParam("id", "grid");
			if( grid )
			{
				var json = JSON.stringify(grid);
				document.getElementById( 'gridtext' ).value = json;
				document.forms[0].method = "POST";
				document.forms[0].action = "diggcfg!presetgrid.action";
				document.forms[0].target = "iGrid";
				document.forms[0].submit();
			}
			var id = getUserActionMemory(document.referrer);
			var node = myZtree.getNodeByParam("id", id?id:"grid");
			if( !node ){
				node = myZtree.getNodeByParam("id", "x");
			}
			myZtree.setChkDisabled(node, true, true, true);
			myZtree.selectNode(node);
			open(node);
		}
		catch(e)
		{
			skit_alert("初始化目录树异常"+e.message+", 行数"+e.lineNumber);
		}
	});

function addDigg()
{
	
}

function addToolbar()
{
}

function addDetail()
{
}

function remove()
{
	hideRMenu();
	var nodes = myZtree.getSelectedNodes();
	if( !nodes || nodes.length == 0)
	{
		skit_alert("请先在目录导航树上选择您要删除的配置项");
		return;
	}
	var node = nodes[0];
	if( node.id == "cell" )
	{
		//showObject(node);
		open(node.getParentNode());
		var iGrid = window.frames["iGrid"];
		if( iGrid && iGrid.removeCell )
			iGrid.removeCell(node.order-1);
		var divAlert = document.getElementById("divAlert");
		divAlert.style.display = "";
		divAlert.style.left = windowWidth - divAlert.clientWidth - 128;
		document.getElementById("spanAlert").innerHTML = "删除单元格配置节点，请点击列表中指定列删除按钮";
	}
	else if( node.id == "render" )
	{
		myZtree.removeNode(render);
	}
}

function delGridCell(rowData, rowInx)
{
	//showObject(rowData);
	var grid = myZtree.getNodeByParam("id", "grid");
	var node = myZtree.getNodeByParam("dataIndx", rowData.dataIndx, grid);
	//alert("delGridCell:"+node);
		document.getElementById("divAlert").style.display = "none";
	if( node )
	{
		myZtree.removeNode(node);
	}
	else
	{
		skit_alert("未找到您要删除的单元格配置【"+rowData.title+"】["+rowData.dataIndx+"]");
	}
}

function setGridCell(rowData, rowIndx)
{
	var render = null;
	var grid = myZtree.getNodeByParam("id", "grid");
	try
	{
		var node = myZtree.getNodeByParam("dataIndx", rowData.dataIndx, grid);
		if( node )
		{
			if( rowData.render && rowData.render == "javascript" )
			{
				for(var i = 0; i < node.children.length; i++)
				{
					if(node.children[i].id == "render" ){
						render = node.children[i];
					}
				}
				if( !render ){
					render = new Object();
					render["name"] = "[render]单元格渲染器";
					render["icon"] = "images/icons/wand.png";
					render["cdata"] = chr2Unicode("function(ui){\r\n\treturn \"\";\r\n}");
					var nodes = myZtree.addNodes(node, -1, render);
					render = nodes[0];
					var ico = document.getElementById("ico_abc_" + render.tId);
					ico.className = "skit_fa_icon_gray fa fa-plus-circle";
					ico.title = "新增的单元格渲染器";
				}
			}
			else
			{
				for(var i = 0; i < node.children.length; i++)
				{
					if(node.children[i].id == "render" ){
						render = node.children[i];
					}
				}
				if( render ){
					myZtree.removeNode(render);
				}
			}
		}
		else
		{
			
		}
		myZtree.expandNode(node, true);
	}
	catch(e)
	{
		alert(e);
	}
}
</SCRIPT>
</html>