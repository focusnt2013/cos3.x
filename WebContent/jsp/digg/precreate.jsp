<%@ page language="java" pageEncoding="UTF-8"%>
<%@ page import="com.focus.cos.web.common.Kit"%>
<%@ taglib uri="/webwork" prefix="ww" %>
<%Kit.noCache(request,response);  %>
<html>
<head>
<link type="text/css" href="skit/ztree/css/zTreeStyle/zTreeStyle.css" rel="stylesheet"/>
<link href="skit/css/awesome-bootstrap-checkbox.css" rel="stylesheet">
</head>
<body>
<table style='width:100%'><tr>
<td style='width:250px;'>
<div class="form-group" style='width:250px;'>
	<div class="input-group">
		<span class="input-group-addon input-group-addon-font">标识<span class='fa fa-user-secret' style='margin-left:1px'></span></span>
		<input class="form-control form-control-font" id="id" type="text" value=''
             	data-title='请输入您模板英文标识。只能含有字母、数字、下划线;程序标识不少于2个字，不多于64个字'
				data-toggle="tooltip" 
				data-placement="bottom"
				data-trigger='manual'
				onkeydown='$("#id").tooltip("hide")'
				onblur='$("#id").tooltip("hide")'
             	placeholder="模板XML文件名称"					
		>
	</div>
</div>
<div class="form-group" style='width:250px;'>
	<div class="input-group">
		<span class="input-group-addon input-group-addon-font">标题<span class='fa fa-info-circle' style='margin-left:1px'></span></span>
		<input class="form-control form-control-font" id="title" type="text" value=''
               	data-title='模板标题只能含有汉字、数字、字母、下划线不能以下划线开头和结尾;程序名称不少于2个字，不多于64个字'
				data-toggle="tooltip" 
				data-placement="bottom"
				data-trigger='manual'
				onkeydown='$("#title").tooltip("hide")'
				onblur='$("#title").tooltip("hide")'
             	placeholder="元数据查询模板中文名称"					
		>
	</div>
</div>
<div class="form-group" style='width:250px;' title='模板的数据模型，不同的数据模型驱动查询出数据的方式不一样'>
	<div class="input-group">
		<span class="input-group-addon input-group-addon-font">模型<span class='fa fa-database' style='margin-left:1px'></span></span>
		<select class="form-control form-control-font" style='font-size:12px' id='datamodel' onchange='changeType(this)'>
			<option value='digg'>DIGG挖掘</option>
			<option value='json'>JSON远程调用</option>
			<option value='zookeeper'>ZooKeeper映射</option>
		</select>
	</div>
</div>

<div class="form-group" id='formDatabase' style='display:none;width:250px;'>
	<div class="input-group">
		<span class="input-group-addon input-group-addon-font">数据库</span>
		<select class="form-control form-control-font" style='font-size:12px' id='src'>
			<ww:iterator value="listData" status="loop">
			<option value="<ww:property value='value'/>"><ww:property value='name'/></option>
       		</ww:iterator>
		</select>
	</div>
</div>

<div class="form-group" id='formTable' style='display:none;width:250px;'>
	<div class="input-group">
		<span class="input-group-addon input-group-addon-font">数据表</span>
		<input class="form-control form-control-font" id="from" type="text" value=''
               	data-title='请输入数据源中您想查询的表名'
				data-toggle="tooltip" 
				data-placement="bottom"
				data-trigger='manual'
				onkeydown='$("#from").tooltip("hide")'
				onblur='$("#from").tooltip("hide")'
             	placeholder="请输入数据源表名"					
		>
	</div>
</div>

<div class="form-group" id='formJsonurl' style='display:none;width:250px;'>
	<div class="input-group">
		<span class="input-group-addon input-group-addon-font">地址<span class='fa fa-external-link' style='margin-left:1px'></span></span>
		<input class="form-control form-control-font" id="jsonurl" type="text" value=''
             	data-title='请输入数据的JSON的URL地址'
				data-toggle="tooltip" 
				data-placement="bottom"
				data-trigger='manual'
				onkeydown='$("#jsonurl").tooltip("hide")'
				onblur='$("#jsonurl").tooltip("hide")'
             	placeholder="表格数据来自于URL访问地址返回的JSON数组对象"					
		>
	</div>
</div>

<div class="form-group" id='formRemote' style='display:none;width:250px;'>
	<div class="input-group">
		<select class="form-control form-control-font" style='font-size:12px' id='pagesize'>
			<option value='20'>20条/页</option>
			<option value='10'>10条/页</option>
			<option value='50'>50条/页</option>
			<option value='100'>100条/页</option>
			<option value='0'>不分页</option>
		</select>
		<span class="input-group-addon input-group-addon-font" style='border-left: 1px solid #eee;border-right: 1px solid #eee;'
			><span class='fa fa-list'></span>首列</span>
		<select class="form-control form-control-font" style='font-size:12px' id='firstColumnType'>
			<option value='1'>可展开详情</option>
			<option value='0'>不扩展</option>
			<option value='2'>出复选框</option>
		</select>
	</div>
</div>

<div class="form-group" id='formZkpath' style='display:none;width:250px;'>
	<div class="input-group">
		<span class="input-group-addon input-group-addon-font">地址<span class='fa fa-share-alt' style='margin-left:1px'></span></span>
		<input class="form-control form-control-font" id="zkpath" type="text" value=''
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

<div class="form-group" id='formModeEncrypte' style='display:none;width:250px;'>
	<div class="input-group">
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

<div class="panel panel-default" style='border: 1px solid #aaaaaa;margin-bottom:10px;width:250px;'>
	<div class="panel-heading" style='font-size:12px;'><i class='skit_fa_btn fa fa-server'></i> 选择元数据查询模板保存目录</div>
	<div class="panel-body" style='padding: 0px;'>
		<div id='divTree'><ul id='myZtree' class='ztree'></ul></div>
	</div>
</div>
</td>
<td valign='top'><iframe id='iGrid' src='diggcfg!openprecreate.action' style='width:100%;border:0px solid #eee;margin-left:3px;margin-top:0px;margin-bottom:0px;'></iframe></td>
</tr></table>
</body>
<SCRIPT type="text/javascript">
/*实现窗口对齐*/
function resizeWindow()
{
	var divTree = document.getElementById('divTree');
	var iGrid = document.getElementById('iGrid');
	divTree.style.height = windowHeight - 160;
	iGrid.style.height = windowHeight;
	iGrid.style.width = windowWidth - 250;
}

var filetype = "<ww:property value='filetype'/>";
var src = "<ww:property value='db'/>";
var from = "<ww:property value='datatype'/>";
var srcOptions = new Object();

function changeType(sel)
{
	changeDatamodel(sel.value);
}
function changeDatamodel(type)
{
	var formDatabase = document.getElementById('formDatabase');
	var formTable = document.getElementById('formTable');
	var formJsonurl = document.getElementById('formJsonurl');
	var formRemote = document.getElementById('formRemote');
	var formZkpath = document.getElementById('formZkpath');
	var formModeEncrypte = document.getElementById('formModeEncrypte');
	formZkpath.style.display = "none";
	formModeEncrypte.style.display = "none";
	formRemote.style.display = "none";
	formJsonurl.style.display = "none";
	formTable.style.display = "none";
	formDatabase.style.display = "none";
	var div = document.getElementById('divTree');
	if("zookeeper"==type)
	{
		formZkpath.style.display = "";
		formModeEncrypte.style.display = "";
		div.style.height = windowHeight - 256 + 16;
	}
	else if("json"==type)
	{
		formJsonurl.style.display = "";
		formRemote.style.display = "";
		div.style.height = windowHeight - 256 + 16;
	}
	else if("digg"==type)
	{
		formDatabase.style.display = "";
		formTable.style.display = "";
		formRemote.style.display = "";
		div.style.height = windowHeight - 256 - 22;
	}
	else
	{
		resizeWindow();
	}
}

var gridColumns = new Array();
Array.prototype.remove=function(dx)
{
    if(isNaN(dx)||dx>this.length){return false;}
    for(var i=0,n=0;i<this.length;i++)
    {
        if(this[i]!=this[dx])
        {
            this[n++]=this[i]
        }
    }
    this.length-=1
}
function setGridColumns(rowDatas)
{
	gridColumns = rowDatas;
}
function setGridColumn(rowData)
{
	//alert("setGridColumn:"+rowIndx+","+isDelete);
	//showObject(rowData);
	delete rowData.pq_rowcls;
	delete rowData.pq_rowselect;
	delete rowData.pq_cellcls;
	delete rowData.gridxml;
	delete rowData._id;
	rowData.dataType = rowData["dataType.src"];
	delete rowData["dataType.src"];
	rowData.render = rowData["render.src"];
	delete rowData["render.src"];
	rowData.filter = rowData["filter.src"];
	delete rowData["filter.src"];
	rowData.sort = rowData["sort.src"];
	delete rowData["sort.src"];
	rowData.editor = rowData["editor.src"];
	delete rowData["editor.src"];
	rowData.editable = rowData["editable.src"];
	delete rowData["editable.src"];
	rowData.hidden = rowData.hidden?"true":"false";
	rowData.align = rowData.align?"true":"false";
	var recIndx = rowData.recIndx?rowData.dataIndx:"";
	//showObject(rowData);
	//alert(recIndx);
	return recIndx;
}
//根据当前返回模板配置
function createTemplate()
{
	try
	{
		var nodes = $.fn.zTree.getZTreeObj("myZtree").getNodesByFilter(filter);
		if( nodes == null || nodes.length == 0 ) {
			skit_error("请选择新建元数据查询模板的保存目录");
			return;
		}
		var path = nodes[0].id;
		var id = document.getElementById( "id" ).value;
		var regexp1 = "^([A-Za-z])|([a-zA-Z0-9])|([a-zA-Z0-9])|([a-zA-Z0-9_])+$";
	    var m1 = id.match(new RegExp(regexp1));
	    if( !m1 )
	    {
	    	document.getElementById( "id" ).focus();
			$("#id").tooltip("show");
	        return;
	    }
	    
		if( id.length<2 || id.length>64 )
		{
	    	document.getElementById( "id" ).focus();
			$("#id").tooltip("show");
		    return;
		}
		
		var title = document.getElementById( "title" ).value;
		if( title.length<2 || title.length>64 )
		{
	    	document.getElementById( "title" ).focus();
			$("#title").tooltip("show");
		    return;
		}
	    
		var template = new Object();
		template.path = path;
		var datamodel = document.getElementById( "datamodel" ).value;
		template.id = id;
		template.title = title;
		template.type = filetype;
		template.datamodel = datamodel;
		if("zookeeper"==datamodel)
		{
			var zkpath = document.getElementById( "zkpath" ).value;
			if( zkpath.length < 2 || zkpath.indexOf('/') != 0 )
			{
		    	document.getElementById( "zkpath" ).focus();
				$("#zkpath").tooltip("show");
			    return;
			}
			var zkmode = document.getElementById( "zkmode" ).value;
			var encrypte = document.getElementById( "encrypte" ).checked?"true":"false";
			template.zkpath = zkpath;
			template.zkmode = zkmode;
			template.encrypte = encrypte;
		}
		else if("json"==datamodel)
		{
			var firstColumnType = document.getElementById( "firstColumnType" ).value;
			var jsonurl = document.getElementById( "jsonurl" ).value;
			var pagesize = document.getElementById( "pagesize" ).value;
			template.firstColumnType = firstColumnType;
			template.jsonurl = url;
			template.pagesize = pagesize;
		}
		else if("digg"==datamodel)
		{
			document.getElementById('src').disabled = false;
			document.getElementById('from').disabled = false;
			src = document.getElementById( "src" ).value;
			from = document.getElementById( "from" ).value;
			if( src == "" )
			{
				skit_alert("请选择数据库，如果没有请先在数据源管理配置");
			    return;
			}
			if( from == "" )
			{
		    	document.getElementById( "from" ).focus();
				$("#from").tooltip("show");
			    return;
			}
			var firstColumnType = document.getElementById( "firstColumnType" ).value;
			var pagesize = document.getElementById( "pagesize" ).value;
			template.firstColumnType = firstColumnType;
			template.src = src;
			template.from = from;
			template.pagesize = pagesize;
		}

		var i, rowData, recIndx, recIndxs = "";
		for(i=0;i<gridColumns.length;i++)
		{
			rowData = gridColumns[i];
			recIndx = setGridColumn(rowData);
			if( recIndx )
			{
				if( recIndxs != "" ) recIndxs+= ",";
				recIndxs += recIndx;
			}
		}
		template.recIndx = recIndxs;
		template.grid = gridColumns;
		if( window.parent && window.parent.createTemplate )
		{
			window.parent.createTemplate(template);
		}
		else skit_alert("该页面必须在框架下运行");
	}
	catch(e)
	{
		skit_alert("获取新增的模板数据出现异常"+e.message+", 行数"+e.lineNumber);
	}
}

function filter(node) {
    return node.checked;
}
</SCRIPT>
<%@ include file="../../skit/inc/skit_cos.inc"%>
<%@ include file="../../skit/inc/skit_ztree.inc"%>
<style type='text/css'>
.form-control-font {
	font-size: 12px;
}
.input-group-addon-font{
	font-size: 12px;
}
.form-group {
    margin-bottom: 5px;
}
</style>

<SCRIPT type="text/javascript">
var myZtree;
var setting = {
	view: {
		dblClickExpand: false
	},
	callback: {
		onClick: onClick,
	},
	check: {
		enable: true,
		chkStyle: "radio",
		radioType: "level"
	}
};

function onClick(event, treeId, treeNode)
{
	var nodes = $.fn.zTree.getZTreeObj("myZtree").getNodesByFilter(filter);
	if( nodes )
		for(var i = 0; i < nodes.length; i++)
		{
			myZtree.checkNode(nodes[i], false);
		}
	myZtree.checkNode(treeNode, true);
}

$(document).ready(function(){
	var json = '<ww:property value="jsonData" escape="false"/>';
	$.fn.zTree.init($("#myZtree"), setting, jQuery.parseJSON(json));
	myZtree = $.fn.zTree.getZTreeObj("myZtree");
	expandAll(true);
	var id =  "<ww:property value='id'/>";
	var node = myZtree.getNodeByParam("id", id);
	if( node )
	{
		myZtree.checkNode(node, true);
	}
	if( filetype == "query" )
	{
		changeDatamodel("digg");
	}
	else
	{
		var options = document.getElementById('datamodel').options;
		options.remove(0);
		options.remove(0);
		changeDatamodel("zookeeper");
	}
});
</SCRIPT>
</html>