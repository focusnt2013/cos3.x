<%@ page language="java" contentType="text/html; charset=UTF-8"  pageEncoding="UTF-8"%>
toolbar = [];
//################构建按钮工具栏 below##################
<ww:iterator value="toolbars" status="loop">
<ww:if test='button'>
toolbar.push({
    type: "button", label: "<ww:property value='label'/>", icon: '<ww:property value='icon'/>', style: 'margin:0px 3px; ',listeners: [{
        "click": function (evt, ui) {
        	//#below
        	var i, j;
        	if( dodebug && "addRow" != "<ww:property value='type'/>" ){
        		var indx = <ww:property value='#loop.index'/>;
        		var js = ""+gridobj.toolbar.items[indx].listeners[0].click;
        		i = js.indexOf("//#below");
        		var js0 = js.substring(0,i);
        		j = js.lastIndexOf("//#above");
        		j += 8;
        		var js1 = js.substring(j);
        		js = js0+js1;
        		top.editJavascript(js, "元数据模板工具栏按钮【<ww:property value='label'/>】点击事件脚本编辑", w-64, h-64, function(editjs){
        			i = editjs.indexOf("/*用户自定义脚本区域below*/");
        			j = editjs.indexOf("/*用户自定义脚本区域above*/");
        			if( i == -1 || j == -1 ){
        				setTimeout("top.skit_alert('请在代码区/*用户自定义脚本区域below*/与/*用户自定义脚本区域above*/之间修改脚本');", 500);
        			}
        			else{
        				i += "/*用户自定义脚本区域below*/".length;
        				editjs = editjs.substring(i, j);
        				if(parnet && parent.setJavascriptToolbar ){
        					parnt.setJavascriptToolbar(editjs, "<ww:property value='label'/>");
        				}
        			}
        		}, function(){
        			if( stylepreview ){
                		top.skit_alert("当前是元数据查询模板样式预览状态，所有操作不可用");
        				return;
        			}
        			setTimeout(function() {
               			try
               			{
                        	<ww:property value='javascript' escape='false'/>
               			}
                    	catch(e)
                    	{
                    		var err = "执行工具栏按钮【<ww:property value='label'/>】脚本出现异常"+e.message+", 行数"+e.lineNumber;
                    		if( top && top.skit_alert) top.skit_alert(err);
                    		else alert(err);
                    	}
        			},500);
        		});//第二个函数是回调脚本
        	}
        	else
        	//#above
   			try
   			{
   				/* 可通过$grid全局表格对象对表格的选项进行操作
   				 * 例子1：获取选择的行记录 
   				 *  var select_row = $grid.pqGrid( "selection",{ type:'row', method:'getSelection' } );
					for(var i = 0; i < select_row.length; i++ )
					{
						var row = select_row[i]["rowData"];
					}
   				 */
   				/*用户自定义脚本区域below*/
            	<ww:property value='javascript' escape='false'/>
   				/*用户自定义脚本区域above*/
   			}
        	catch(e)
        	{
        		var err = "执行工具栏按钮【<ww:property value='label'/>】脚本出现异常"+e.message+", 行数"+e.lineNumber;
        		if( top && top.skit_alert) top.skit_alert(err);
        		else alert(err);
        	}
        }
    }]
});
</ww:if>
</ww:iterator>
<ww:if test='exportable'>
toolbar.push({
	type: 'button',label: "导出Excel",icon: 'ui-icon-document', style: 'margin:0px 3px;', listeners: [{
        "click": function (evt) {
        	if( stylepreview ) {
        		top.skit_alert("当前是元数据查询模板样式预览状态，所有操作不可用");
        		return;
        	}
        	var tips = "您确定要下载数据到excel文件吗？";
        	if( dodebug ) tips = "您确定要下载数据到excel文件吗？(预览测试模式下只会导出首页数据)";
            //$("#grid_query").pqGrid("exportExcel", { url: "<ww:property value='exportUrl'/>?type=xls", sheetName: "sheet1" });
        	top.skit_confirm(tips,function(yes){
				if( yes ){
		        	document.getElementById("id").value = "";
					var url = "digg!export.action?filetype=xls&dodebug="+dodebug;
					//alert(document.getElementById("pq_filter").value);
					document.forms[0].action = url;
					document.forms[0].method = "POST";
					document.forms[0].target = "downloadframe";
					document.forms[0].submit();
					if( getExportProgress && grid_progress )
					{
						grid_progress("导出表格数据到xls文件", function(){
							$grid.pqGrid("hideLoading");
							stopExport();
						});
						$grid.pqGrid("showLoading");
						window.setTimeout("getExportProgress()",500);
					}
				}
			});
        }
    }]
});
toolbar.push({
    type: 'button', label: "导出Csv",icon: 'ui-icon-document', style: 'margin:0px 3px;',listeners: [{
        "click": function (evt) {
        	var tips = "您确定要下载数据到csv文件吗？";
        	if( dodebug ) tips = "您确定要下载数据到csv文件吗？(预览测试模式下只会导出首页数据)";
            //$("#grid_query").pqGrid("exportCsv", { url: "helper!exportCsv.action" });
            //$("#grid_query").pqGrid("exportExcel", { url: "<ww:property value='exportUrl'/>?type=csv", sheetName: "sheet1" });
        	top.skit_confirm("您确定要下载数据到csv文件吗？",function(yes){
				if( yes ){
		        	if( stylepreview ) {
		        		top.skit_alert("当前是元数据查询模板样式预览状态，所有操作不可用");
		        		return;
		        	}
		        	document.getElementById("id").value = "";
					var url = "digg!export.action?filetype=csv&dodebug="+dodebug;
					document.forms[0].action = url;
					document.forms[0].method = "POST";
					document.forms[0].target = "downloadframe";
					document.forms[0].submit();
					if( getExportProgress && grid_progress )
					{
						grid_progress("导出表格数据到csv文件，为了提高导出效率我们将csv文件压缩", function(){
							$grid.pqGrid("hideLoading");
							stopExport();
						});
						$grid.pqGrid("showLoading");
						window.setTimeout("getExportProgress()",500);
					}
				}
			});
        }
    }]
});
</ww:if>
//################构建按钮工具栏 above##################
//################构建过滤工具栏 below##################
//执行工具栏条件过滤
var toolbarfilterhandler = function() {
    var $toolbar = $grid.find('.pq-toolbar-search'),$value, value, condition, dataIndx, filterObject = [];
    try{
        <ww:iterator value="toolbars" status="loop">
        <ww:if test='filter'>
        dataIndx = "<ww:property value='dataIndx'/>";
        $value = $toolbar.find("."+dataIndx);
        value = $value.val();
        if( value ){
        	if( dataModel.location == 'local' )
        	{
        		var options = fieldOptions[dataIndx];
        		for(var i = 0; i < options.length; i++){
        			var e = options[i];
        			if(e[value]){
        				value = e[value];
        			}
        		}
        		//showObject(options);
        	}
        	condition = "<ww:property value='condition'/>";
        	filterObject.push({ dataIndx: dataIndx, condition: condition, value: value });
        }
        </ww:if>
        </ww:iterator>
        $grid.pqGrid("filter", {
            oper: 'replace',
            data: filterObject
        });
    }
    catch(e)
    {
		if( top && top.skit_alert) top.skit_alert("执行工具栏("+dataIndx+" "+condition+" "+value+")过滤出现异常"+e.message+", 行数"+e.lineNumber);
		else alert("执行工具栏("+dataIndx+" "+condition+" "+value+")过滤出现异常"+e.message+", 行数"+e.lineNumber);    	
    }
};
<ww:iterator value="toolbars" status="loop">
<ww:if test='filter'>
<ww:if test='label!=null'>
toolbar.push({ type: "<span style='margin:5px;'><ww:property value='label'/></span>" });
</ww:if>
toolbar.push({
	type: "<ww:property value='type'/>",
	style: "margin:0px 5px;height:22px;<ww:property value='style'/>",
	attr: "placeholder='<ww:property value="placeholder"/>'",
	listeners: [
	    {'change': <ww:property value='change'/>}
	],
	<ww:if test='"select".equals(type)'>
	options: function (ui) {
		return fieldOptions["<ww:property value='dataIndx'/>"];
	},
	</ww:if>
	cls: "<ww:property value='dataIndx'/>"
});
</ww:if>
</ww:iterator>
//################构建过滤工具栏 above##################