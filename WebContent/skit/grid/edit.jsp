<%@ page language="java" pageEncoding="UTF-8"%>
<%@ page import="com.focus.cos.web.common.Kit"%>
<%@ taglib uri="/webwork" prefix="ww" %>
<%Kit.noCache(request,response); %>
<html>
<head>
<!--JQUERY UI files-->
<link href="<%=Kit.URL_PATH(request)%>skit/css/jquery-ui.css" rel="stylesheet">
<link href="<%=Kit.URL_PATH(request)%>skit/css/font-awesome.min.css" rel="stylesheet"/>
<!--PQ Grid files-->
<link rel="stylesheet" href="<%=Kit.URL_PATH(request)%>skit/grid/pqgrid.min.css?v=2.0.4" />
<link rel="stylesheet" href="<%=Kit.URL_PATH(request)%>skit/grid/pqselect.min.css" />
<!--PQ Grid Office theme-->
<link rel="stylesheet" href="<%=Kit.URL_PATH(request)%>skit/grid/themes/office/pqgrid.css" />
<!--上传文件-->
<link href="<%=Kit.URL_PATH(request)%>skit/css/bootstrap.min.css" rel="stylesheet"/>
<link href="<%=Kit.URL_PATH(request)%>skit/css/fileinput.min.css" rel="stylesheet"/>
<!--  -->
<link href="<%=Kit.URL_PATH(request)%>skin/defone/css/simplemodal.css" rel="stylesheet"/>
<!--All js-->
<script src="<%=Kit.URL_PATH(request)%>skit/grid/jquery.min.js"></script>
<script src="<%=Kit.URL_PATH(request)%>skit/grid/jquery-ui.min.js"></script>
<script src="<%=Kit.URL_PATH(request)%>skit/grid/pqselect.min.js"></script>
<script src="<%=Kit.URL_PATH(request)%>skit/grid/pqgrid.min.js?v=2.0.4"></script>
<script src="<%=Kit.URL_PATH(request)%>skit/grid/touch-punch.js"></script>
<script src="<%=Kit.URL_PATH(request)%>skit/grid/pq-localize-zh.js"></script>
<script src="<%=Kit.URL_PATH(request)%>skit/js/fileinput.js"></script>
<script src="<%=Kit.URL_PATH(request)%>skit/js/fileinput_locale_zh.js"></script>
<script src="<%=Kit.URL_PATH(request)%>skit/js/jsrsasign-rsa-min.js"></script>
<script src="<%=Kit.URL_PATH(request)%>skit/js/jquery.md5.js"></script>
<script src="<%=Kit.URL_PATH(request)%>skin/defone/js/mootools-core-1.3.1.js"></script>
<script src="<%=Kit.URL_PATH(request)%>skin/defone/js/simple-modal.js"></script>
<script src="<%=Kit.URL_PATH(request)%>skin/defone/js/jquery.nicescroll.min.js"></script> 
<%=Kit.getDwrJsTag(request,"interface/DiggMgr.js")%>
<%=Kit.getDwrJsTag(request,"engine.js")%>
<%=Kit.getDwrJsTag(request,"util.js")%>
<%@ include file="pqgrid-skit.inc"%>
<style type='text/css'>
<ww:property value='pq_cellcls' escape='false'/>
</style>
<SCRIPT TYPE="text/javascript" id='beforeGridView'>
function callbackBeforeGridView(){
	try{
		/* 全局对象包含gridobj、colModel、dataModel、filterFields、fieldOptions、valueLabels、filterRowcls、filterCellcls、summary
		 * gridobj: 完整的结构定义，可通过修改其中的值改变表格样式
		 * colModel: 单元格模型，映射gridobj.colModel，控制表格样式显示
		 * dataModel: 数据模型，映射gridobj.dataModel，控制取数据方式
		 * filterFields: 表格条件查询字段列表，控制条件查询的参数
		 * fieldOptions: 表格条件查询选项列表，控制条件查询的用户下拉选择
		 * valueLabels: 表格值名称映射表，控制表格中数据的值转义为其它可视名称
		 * filterRowcls: 行样式过滤器，控制单元行的显示样式
		 * filterCellcls: 列样式过滤器，控制单元格的显示样式
		 * summary: 汇总配置，控制汇总参数的(如果配置了汇总)
		 */
		/*用户自定义表格初始化前回调脚本区域below*/
	    <ww:property value="beforeGridView" escape="false"/>
	    /*用户自定义表格初始化前回调脚本区域above*/
	}
	catch(e){
   		if( top && top.skit_alert) top.skit_alert("执行表格初始化前回调出现异常"+e.message+", 行数"+e.lineNumber);
   		else alert("执行表格初始化前回调出现异常"+e.message+", 行数"+e.lineNumber);
	}
}
</SCRIPT>
</head>
<body>
<form target='downloadframe'>
<input type='hidden' name='id' id='id' value="<ww:property value='id'/>">
<input type='hidden' name='gridxml' id='gridxml' value="<ww:property value='gridxml'/>">
<div id="grid_edit" style="margin-top:0px auto;"></div>
<div id="pq-dialog-cont">
<div id="pq-grid-in-popup-dialog" title="Resizable Grid in Dialog" style="overflow:hidden;padding:0px;">
    <div id="grid_in_popup_grid"></div>
</div>
</div>
</form>

<div id="popup-dialog-crud" style="display:none;">
<form id="crud-form">
<!-- 遍历所有弹出对话框属性，生成对话框内界面 -->
<ww:iterator value="dialogPopups" status="loop">
<!-- 文件导入上传对话框 below -->
<ww:if test='type=="fileinput"'>
<input type="file" name="<ww:property value='id'/>" id="<ww:property value='id'/>" class="file-loading"/>
</ww:if>
<!-- 文件导入上传对话框 end -->
</ww:iterator>
</form>
</div>
<iframe name="downloadframe" id="downloadframe" style="display:none;border:1px solid red"></iframe>
<div class="panel panel-primary" id='divUploadfile' style='z-index: 9527; top: 100; left: 100; width:600px; display:none; position: absolute; '>
    <div class="panel-heading">
    	<span id='spanUploadtitle'>从本地磁盘选择上传文件</span>
        <div class="panel-menu">
            <button type="button" onclick='closeuploadfile()' data-action="close" class="btn btn-warning btn-action btn-xs"><i class="fa fa-times"></i></button>
        </div>
    </div>
    <div class="panel-body" style="display: block;">
		<input type="file" name="uploadfile" id="uploadfile" class="file-loading"/>
    </div>
</div>
<input type='hidden' id='uploadfilename'>
<div id='divDialogMask' onclick='closeDialogMask(this)' class='skit_mask' style='cursor:pointer;' title='点击关闭弹窗'></div>
</body>
<SCRIPT TYPE="text/javascript">
var grid_type = "grid_edit";
var openpopup = {};
var closepopup = {};
var nopopup = {};
<%@ include file="inc/init.inc"%>
<%@ include file="inc/common.inc"%>
<%@ include file="inc/toolbar.inc"%>
var globalts = 0;//全局增值ID，用于某种用途
callbackBeforeGridView();
$(function () {
	try{
		<%@ include file="inc/edit.inc"%>
		<%@ include file="inc/filter.inc"%>
	}
	catch(e)
	{
		if( top && top.skit_alert) top.skit_alert("初始化表格视图出现异常"+e.message+", 行数"+e.lineNumber);
		else alert("初始化表格视图出现异常"+e.message+", 行数"+e.lineNumber);
	}
});
<%@ include file="inc/popup.inc"%>
//to check whether any row is currently being edited.
var ajaxObj = {
    dataType: "JSON",
    beforeSend: function () {
        this.pqGrid("showLoading");
    },
    complete: function () {
        this.pqGrid("hideLoading");
    },
    error: function () {
        this.pqGrid("rollback");
    }
};

function rowIndxEditing($grid) {
    var rows = $grid.pqGrid("getRowsByClass", { cls: 'pq-row-edit' });
    if (rows.length > 0) {
        //focus on editor if any 
        $grid.find(".pq-editor-focus").focus();
        return rows[0].rowIndx;
    }
    return -1;
}

function isEditing() {
	if( $grid ){
	    var rows = $grid.pqGrid("getRowsByClass", { cls: 'pq-row-edit' });
	    if (rows.length > 0) {
	        //focus on editor if any 
	        $grid.find(".pq-editor-focus").focus();
	        return true;
	    }
	}
    return false;
}
//called by add button in toolbar.
function addRow($grid,newRow, newRowIndx) {
    if (isEditing($grid)) {
        return false;
    }
    //append empty row in the first row.
    var rowData = {};
    if( newRow ) {
    	rowData = newRow;
    };
    if( !newRowIndx )
    {
    	newRowIndx = 0;
    }
    $grid.pqGrid("addRow", { rowIndxPage: newRowIndx, rowData: rowData });
    var $tr = $grid.pqGrid("getRow", { rowIndxPage: newRowIndx });
    if ($tr) {
        //simulate click on edit button.
        var $btn = $tr.find("button.edit_btn");
        if( $btn ){
        	$btn.click();
        }
        else
        {
			$btn.button("option", { label: "保存", "icons": { primary: "ui-icon-check"} })
	            .unbind("click")
	            .click(function (evt) {
	                evt.preventDefault();
	                $grid.pqGrid("quitEditMode");
	                return update(rowIndx, $grid);
	            });
	        $btn.next().button("option", { label: "取消", "icons": { primary: "ui-icon-cancel"} })
	            .unbind("click")
	            .click(function (evt) {
	                $grid.pqGrid("quitEditMode");
	                $grid.pqGrid("removeClass", { rowIndx: rowIndx, cls: 'pq-row-edit' });
	                $grid.pqGrid("rollback");
	        		var rowData = $grid.pqGrid("getRowData", { rowIndx: rowIndx });
	        		filterRow(rowData);
	                $grid.pqGrid("refreshRow", { rowIndx: rowIndx });
	            });
	        return rowData;
        }
    }
}
//在调试模式下，显示编辑模板
function showEditDebug(title, rowIndx){
	if( !dodebug ){
		return false;
	}
	var i, j, js, globalscript, jscbc, jscs;
	try{
		js = "";
		jscbc = "";
		jscs = "";
		globalscript = document.getElementById( 'globalscript' ).innerHTML;
		i = globalscript.indexOf("callbackBeforeCommit");
		if( i != -1 ){
			i = globalscript.indexOf("{", i);
			if( i != -1 ){
				jscbc = globalscript.substring(i+1);
				j = jscbc.indexOf("function");
				if( j == -1 ){
					j = jscbc.lastIndexOf("}");
				}
				else{
					jscbc = jscbc.substring(0, j);
					j = jscbc.lastIndexOf("}", j);
				}
				jscbc = jscbc.substring(0, j-1);
			}
			if(jscbc){
				i = jscbc.lastIndexOf(";");
				if( i != -1 ){
					jscbc = jscbc.substring(0, i+1);
				}
			}
		}
		if( jscbc.indexOf("return ") == -1 ){
			jscbc = "return true";
		}
		i = globalscript.indexOf("callbackSave");
		if( i != -1 ){
			i = globalscript.indexOf("{", i);
			if( i != -1 ){
				jscs = globalscript.substring(i+1);
				j = jscs.indexOf("function");
				if( j == -1 ){
					j = jscs.lastIndexOf("}");
				}
				else{
					jscs = jscs.substring(0, j);
					j = jscs.lastIndexOf("}", j); 
				}
				jscs = jscs.substring(0, j-1);
			}
		}
		js += "function callbackBeforeCommit(rowData){";
		if( jscbc.indexOf("/*用户自定义提交前回调脚本区域below*/") == -1 ){
			js += "\r\n\t/*用户自定义提交前回调脚本区域below*/";
		}
		var _ajaxUpdateUrl = "\r\n\tajaxUpdateUrl = '"+ajaxUpdateUrl+"';//该参数是处理数据保存提交的业务Action";
		if( jscbc.indexOf("ajaxUpdateUrl = ") == -1 ){
			js += _ajaxUpdateUrl;
		}
		var _ajaxDeleteUrl = "\r\n\tajaxDeleteUrl = '"+ajaxDeleteUrl+"';//该参数是处理数据删除提交的业务Action";
		if( jscbc.indexOf("ajaxDeleteUrl = ") == -1 ){
			js += _ajaxDeleteUrl;
		}
		js += jscbc;
		if( jscbc.indexOf("\r\n\t/*用户自定义提交前回调脚本区域above*/") == -1 ){
			js += "\r\n\t/*用户自定义提交前回调脚本区域above*/";
		}
		js += "\r\n}";
		js += "\r\nfunction callbackSave(rowData, rowIndx, isDelete){";
		if( jscs.indexOf("/*用户自定义保存后回调脚本区域below*/") == -1 ){
			js += "\r\n\t/*用户自定义保存后回调脚本区域below*/";
		}
		js += jscs;
		if( jscs.indexOf("/*用户自定义保存后回调脚本区域above*/") == -1 ){
			js += "\r\n\t/*用户自定义保存后回调脚本区域above*/";
		}
		js += "\r\n}";
		top.editJavascript(js, "编辑模板【"+title+"】按钮脚本编辑", w-64, h-64, function(editjs){
			if( parent && parent.setJavascriptEditCallback ){
				parent.setJavascriptEditCallback(editjs);
			}
		}, function(){
			if( stylepreview ){
      		top.skit_alert("当前是元数据模板样式预览状态，所有操作不可用");
				return;
			}
			setTimeout(function() {
     			try	{
     				dodebug = false;
     				if("删除" == title ){
     					deleteRow(rowIndx, $grid);
     				}
     				else{
     					update(rowIndx, $grid);
     				}
     				dodebug = true;
     			}
     			catch(e) {
	          		var err = "执行【"+title+"】按钮脚本出现异常"+e.message+", 行数"+e.lineNumber;
	          		if( top && top.skit_alert) top.skit_alert(err);
	          		else alert(err);
     			}
			},500);
		});//第二个函数是回调脚本
	}
	catch(e) {
		var err = "打开编辑调测脚本出现异常"+e.message+", 行数"+e.lineNumber;
		if( top && top.skit_alert) top.skit_alert(err);
		else alert(err);
	}
	return true;
}
var ajaxAddUrl = "<ww:property value='%{ajaxAdd}' escape='false'/>";
var ajaxUpdateUrl = "<ww:property value='%{ajaxUpdate}' escape='false'/>";
var ajaxDeleteUrl = "<ww:property value='%{ajaxDelete}' escape='false'/>";
//called by delete button.
function deleteRow(rowIndx, $grid) {
	if( showEditDebug("删除", rowIndx) ){
		return;
	}
    var rowData = $grid.pqGrid("getRowData", { rowIndx: rowIndx });
    if( !callbackBeforeCommit(rowData, true) )
    {
    	return;
    }
    $grid.pqGrid("addClass", { rowIndx: rowIndx, cls: 'pq-row-delete' });
	//top.showJson(rowData, "deleteRow", w, h-128);
    top.skit_confirm("您确定要删除该行记录吗？",function(yes){
		if( yes ){
            $grid.pqGrid("deleteRow", { rowIndx: rowIndx, effect: true });
            var recId = $grid.pqGrid("getRecId", { rowIndx: rowIndx });
            var recIndx = $grid.pqGrid("option", "dataModel.recIndx");
            //alert(recIndx+": "+recId);
            //alert("recId="+recId+",url=<ww:property value='%{ajaxDelete}' escape='false'/>");
            if( recId ){
            	rowData["gridxml"] = document.getElementById("gridxml").value;
            	for(var key in rowData ){
            		var val = rowData[key];
            		if( val ){
	            		if( typeof val == 'object' ){
	            			delete rowData[key];
	            		}
            		}
            	}
                if( rowData.pq_cellcls ){
                	delete rowData.pq_cellcls;
                }
                if( rowData.pq_rowcls ){
                	delete rowData.pq_rowcls;
                }
                $.ajax($.extend({}, ajaxObj, {
                    context: $grid,
                    url: ajaxDeleteUrl,
                    //data: { id: recId, data: rowData, gridxml:document.getElementById("gridxml").value,_id:document.getElementById("id").value },
                    data: rowData,
                    success: function (response) {
    			    	if(response.hasException){
    	                    $grid.pqGrid("removeClass", { rowIndx: rowIndx, cls: 'pq-row-delete' });
    	                    $grid.pqGrid("rollback");
    			    		if( top && top.skit_alert )
    			    			top.skit_alert(response.message);
    			    		else  alert(response.message);
    			    	}
    			    	else
    			    	{
    	                    $grid.pqGrid("commit");
    		                if( callbackSave )
    			    		{
    		                	callbackSave(rowData, rowIndx, true);
    			    		}
                        }
                    },
                    error: function (rsp) {
                    	alert("["+rsp.status+"]"+rsp.statusText);
                        //debugger;
                        $grid.pqGrid("removeClass", { rowData: rowData, cls: 'pq-row-delete' });
                        $grid.pqGrid("rollback");
                    }
                }));
            }
            else{
                $grid.pqGrid("removeClass", { rowData: rowData, cls: 'pq-row-delete' });
                $grid.pqGrid("rollback");
            	top.skit_alert("因为没有配置主键，所以不能执行删除记录的操作。");
            }
		}
		else{
        	$grid.pqGrid("removeClass", { rowIndx: rowIndx, cls: 'pq-row-delete' });
   		}
		
	});        
}
//called by edit button.
function editRow(rowIndx, $grid) {
    var rowData = $grid.pqGrid("getRowData", { rowIndx: rowIndx });
    try
    {
		for( var name in rowData )
		{
			var i = name.indexOf(".src");
			var val = rowData[name];
			if( i != -1 )
			{
				var key = name.substring(0,i);
				if( typeof val == 'object' )
				{
					val = JSON.stringify(val);
				}
				rowData[key] = val;
				$grid.pqGrid( "refreshCell", { rowIndx: rowIndx, dataIndx: key } );
			}
			else{
				if( val == "<span class=\"ui-widget-content ui-icon ui-icon-check\" style=\"margin-left:24px;\"></span>" ){
					rowData[name] = "true";
					$grid.pqGrid( "refreshCell", { rowIndx: rowIndx, dataIndx: name } );
				}
			}
		}
    }
    catch(e){
    	alert(e);
    }
	$grid.pqGrid("addClass", { rowIndx: rowIndx, cls: 'pq-row-edit' });
    $grid.pqGrid("editFirstCellInRow", { rowIndx: rowIndx });
    //change edit button to update button and delete to cancel.
    var $tr = $grid.pqGrid("getRow", { rowIndx: rowIndx }),
        $btn = $tr.find("button.edit_btn");
    $btn.button("option", { label: "保存", "icons": { primary: "ui-icon-check"} })
    	.unbind("click").click(function (evt) {
            evt.preventDefault();
            return update(rowIndx, $grid);
    });
    var btn = $btn.next();
    if( btn )
    {
        btn.button("option", { label: "取消", "icons": { primary: "ui-icon-cancel"} })
            .unbind("click").click(function (evt) {
            	quiteEdit(rowIndx);
            });
    }

	if( callbackBeginEdit )
	{
		callbackBeginEdit(rowIndx, rowData);
	}
} 

function quiteEdit(rowIndx){
    $grid.pqGrid("quitEditMode");
    $grid.pqGrid("removeClass", { rowIndx: rowIndx, cls: 'pq-row-edit' });
    $grid.pqGrid("rollback");
	var rowData = $grid.pqGrid("getRowData", { rowIndx: rowIndx });
	for( var name in rowData )
	{
		var i = name.indexOf(".obj");
		if( i != -1 )
		{
			var oldkey = name.substring(0, i);
			if( rowData[name] ) rowData[oldkey] = rowData[name];
		}
	}
  	filterRow(rowData);
    $grid.pqGrid("refreshRow", { rowIndx: rowIndx });
}
//called by update button.
function update(rowIndx, $grid) {
	if( showEditDebug("保存", rowIndx) ){
		return;
	}
	if( !rowIndx ){
		rowIndx = 0;
	}
    if (!$grid.pqGrid("saveEditCell")) {
        return false;
    }
	    var rowData = $grid.pqGrid("getRowData", { rowIndx: rowIndx });
	try{
	    var isValid = $grid.pqGrid("isValid", { rowData: rowData });
	    if (isValid && !isValid.valid) {
	        return false;
	    }
    }
    catch(e)
    {
    	alert("校验编辑数据项出现异常:"+e.message+", 行数"+e.lineNumber);
        return false;
    }
    var isDirty = $grid.pqGrid("isDirty");
    if (isDirty) {
        var recIndx = $grid.pqGrid("option", "dataModel.recIndx");
        $grid.pqGrid("removeClass", { rowIndx: rowIndx, cls: 'pq-row-edit' });
        rowData["gridxml"] = document.getElementById("gridxml").value;
        try
        {
    		for( var name in rowData )
    		{
    			var i = name.indexOf(".obj");
    			if( i != -1 )
    			{
    				var srckey = name.substring(0, i);
    				if( typeof rowData[srckey] == 'string' )
    				{
    					if( rowData[srckey].charAt(0) != '{' )
    					{
        					rowData[srckey] = JSON.stringify(rowData[name]);
    					}
    				}
    				continue;
    			}
    			i = name.indexOf(".src");
    			if( i != -1 )
    			{
    				var srckey = name.substring(0, i);
    				var options = fieldOptions[srckey];
    				if( options )
    				{
    					for( i = 0; i < options.length; i++ )
    					{
    						var option = options[i];
    						if( option[rowData[srckey]] )
    						{
    							break;
    						}
    					}
    					if( i == options.length )
    					{
    						rowData[srckey] = rowData[name];
    					}
    				}
    				continue;
    			}
    			var content = rowData[name];
    			if( content && typeof content == 'string' ){
    				html2text(rowData, name);
    			}
    		}
            if( !callbackBeforeCommit(rowData) )
            {
            	return;
            }
        }
        catch(e)
        {
        	alert(e);
        	return;
        }
        $.ajax($.extend({}, ajaxObj, {
            context: $grid,
            url: ajaxUpdateUrl,
            data: rowData,
            success: function (response) {
                var recIndx = $grid.pqGrid("option", "dataModel.recIndx");
                try
                {
                    if (rowData[recIndx] == null) {
                        rowData[recIndx] = response.recId;
                    }
			    	if(response.hasException){
			    		if( top && top.skit_alert ) top.skit_alert(response.message);
			    		else  alert(response.message);
		                $grid.pqGrid("removeClass", { rowIndx: rowIndx, cls: 'pq-row-edit' });
		                $grid.pqGrid("rollback");
	                    filterRow(rowData);
		                $grid.pqGrid("refreshRow", { rowIndx: rowIndx });
			    	}
			    	else
			    	{
	                    $grid.pqGrid("removeClass", { rowIndx: rowIndx, cls: 'pq-row-edit' });
	                    $grid.pqGrid("commit");
		    			for(var name in response)
		    			{
		    				if( name == "recId" ) continue;
		    				rowData[name] = response[name];
		    			}
		                if( callbackSave )
			    		{
		                	callbackSave(rowData, rowIndx, false);
			    		}
	                    filterRow(rowData);
		                $grid.pqGrid("refreshRow", { rowIndx: rowIndx });
			    	}
                }
                catch(e)
                {
            		if( top && top.skit_alert) top.skit_alert("更新数据出现异常"+e.message+", 行数"+e.lineNumber);
            		else alert("更新数据出现异常"+e.message+", 行数"+e.lineNumber);
                }
            },
            error: function(response){
                $grid.pqGrid("quitEditMode");
	            $grid.pqGrid( "hideLoading" );
            	if( response.status && response.responseText ){
                    alert(response.responseText+"("+response.status+")");
                }
	            //{
	            //    "readyState": 4,
	            //    "responseText": "",
	            //    "status": 500,
	            //    "statusText": "Internal Server Error"
	            //}
            }
        }));
    }
    else {
        $grid.pqGrid("quitEditMode");
        $grid.pqGrid("removeClass", { rowIndx: rowIndx, cls: 'pq-row-edit' });
        $grid.pqGrid("refreshRow", { rowIndx: rowIndx });
        $grid.pqGrid("rollback");
        if( top && top.skit_alert) top.skit_alert("没有需要保存的数据");
    }
}

//当提交前执行回调
function callbackBeforeCommit(rowData)
{
	return true;
}
//当完成保存的时候回调,返回true标识执行保存
function callbackSave(rowData, rowIndx, isDelete)
{
}
//
function callbackBeginEdit(rowIndx, rowData){
}
</SCRIPT>
<SCRIPT TYPE="text/javascript">
window.onresize = function(){
	resizeWindow();
}
document.onkeydown = function(event) { 
	//alert(event);
	var e = event ? event :(window.event ? window.event : null); 
	if(e.keyCode==122){ 
		resizeWindow();
	} 
}
var abortResize = false;
function resizeWindow()
{
	if( abortResize )
	{
		return;
	}
	w = window.innerWidth || document.documentElement.clientWidth || window.document.body.clientWidth; 
	h = window.innerHeight || document.documentElement.clientHeight || window.document.body.clientHeight;
	h -= 2;
	w -= 2;
	$("#grid_edit").pqGrid( "option", "flexWidth", false );
	$("#grid_edit").pqGrid( "option", "flexHeight", false );
	$("#grid_edit").pqGrid( "option", "width", w );
	$("#grid_edit").pqGrid( "option", "height", h );
	$("#grid_edit").pqGrid("refresh");
}
</SCRIPT>
<%@ include file="inc/globalscript.inc"%>
<SCRIPT TYPE="text/javascript" id='globalscript'>
<ww:property value='javascript' escape='false'/>
</SCRIPT>
</html>