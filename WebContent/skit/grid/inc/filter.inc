<%@ page language="java" contentType="text/html; charset=UTF-8"  pageEncoding="UTF-8"%>
function buildHeaderFilter()
{
	<ww:if test='filterModel.fields.size()>0'>
	//load shipregion and shipvia dropdowns in first load event.
    var column, filter, options, tmpjson;
    var count = 0;
	<ww:iterator value="filterModel.fields" status="loop">
	<ww:if test='!filter'>
	tmpjson = '<ww:property value="options" escape="false"/>';
	options = null;
	if( tmpjson ) options = jQuery.parseJSON(tmpjson);
	if( options )
	{
		var attr = "<ww:if test='attr!=null'><ww:property value="attr"/></ww:if>";
   	 	column = $grid.pqGrid("getColumn", { dataIndx: '<ww:property value="name"/>' });
       	filter = column.filter;
        filter.cache = null;
        filter.options = options;
        $.extend(filter, {
            attr: attr,
            condition: '<ww:property value="condition"/>',
            prepend: { '': '[ 空 ]' },
            value: [],
            cache: null,
            style: '<ww:property value="style"/>'
        });
        
	}
	count += 1;
	</ww:if>
	</ww:iterator>
    if( count > 0 ) $grid.pqGrid("refreshHeader");
    </ww:if>    
}
<ww:if test='asyncDigg'>
if( !remoteDataReceived ){
	//alert("remote:"+dataModel.url+", remoteDataReceived="+remoteDataReceived);
	$grid.pqGrid("showLoading");//如果是直接返回数据，就不显示加载提示
}
$.ajax({ 
	url: "digg!data.action",
    cache: false,
    async: true,
    dataType: "JSON",
    success: function (remoteData) {
    	$grid.pqGrid("hideLoading");
    	var data = handleRemoteData(remoteData);
        $grid.pqGrid("option", "dataModel.data", data.data);
        $grid.pqGrid("refreshDataAndView");
    }
});
</ww:if>
//load all data at once
if( dataModel.location == 'remote' )
{
//	alert("remote:"+dataModel.url+", remoteDataReceived="+remoteDataReceived);
    $grid.one("pqgridload", function (evt, ui) {
    	buildHeaderFilter();
    	$grid.pqGrid("hideLoading");
    });
    if( dataModel.url == "digg!data.action" ){
    	var d = 10;
    	if( parent.showDebuglog ){
    		parent.showDebuglog(true);
    	}
    	if(document.referrer && document.referrer.indexOf("diggcfg!explorer.action") == -1){
    		//5秒后如果没有出结果就展示
    		d = 5000;
    	}
		setTimeout(function(){
			startProgress();
		}, d);
    }
}
else
{
	buildHeaderFilter();
}
$grid.pqGrid("option", $.paramquery.pqGrid.regional["zh"]); 
$grid.find(".pq-pager").pqPager("option", $.paramquery.pqPager.regional["zh"]);
//bind the select list.
var grid_sorting_select = "#grid_"+dataModel.location+"_sorting_select";
$(grid_sorting_select).change(function(evt){            
    var val=$(this).val();
    var DM = $grid.pqGrid("option", "dataModel");
    if(val=="multiple"){                        
        DM.sortIndx = [dataModel.sortIndx];
        DM.sortDir = [dataModel.sortDir];                
    }
    else{
        DM.sortIndx = dataModel.sortIndx[0];
        DM.sortDir = dataModel.sortDir[0];
    }
    $grid.pqGrid("option", "dataModel", DM);
    $grid.pqGrid("refreshDataAndView");
});

if( dodebug && colModel && parent && parent.addRenderDebugMenu )
{
	var r, i, j;
	//将模板的单元格渲染器添加到菜单
	for(i = 0; i < colModel.length; i++){
		var col = colModel[i];
		if( col.colModel ){
			for(j = 0; j < col.colModel.length; j++){
				r = col.colModel[j].render;
				if( r ){
					parent.addRenderDebugMenu(col.title+"_"+col.colModel[j].title, i, j);
				}
				r = col.colModel[j].editor;
				if( r && r.init ){
					parent.addEditorInitDebugMenu(col.title+"_"+col.colModel[j].title, i, -1);
				}
				else if( r && r.type ){
					parent.addEditorDebugMenu(col.title+"_"+col.colModel[j].title, i, -1);
				}
				r = col.colModel[j].editable;
				if( r && r != true ){
					parent.addEditableDebugMenu(col.title+"_"+col.colModel[j].title, i, -1);
				}
			}
		}
		else{
			r = col.render;
			if( r ){
				parent.addRenderDebugMenu(col.title, i, -1);
			}
			r = col.editor;
			if( r && r.init ){
				parent.addEditorInitDebugMenu(col.title, i, -1);
			}
			else if( r && typeof r.type == "function" ){
				parent.addEditorDebugMenu(col.title, i, -1);
			}
			r = col.editable;
			if( r && r != true ){
				parent.addEditableDebugMenu(col.title, i, -1);
			}
		}
	}
}
//检查工具栏中日期范围选择进行在输入框上构建日期空间
try{
	var datepicker;
<ww:iterator value="toolbars" status="loop">
<ww:if test='filter&&calendar'>
	datepicker = $(".ui-corner-all.<ww:property value='dataIndx'/>").datepicker({
		dateFormat: "yy-mm-dd",
	    yearRange: "-100:+100", //20 years prior to present.
	    changeYear: true,
	    changeMonth: true,//dateFormat.indexOf("mm")!=-1,
	    currentText: '今天',
	    nextText: '下月>',  
	    prevText: '<上月',  
	    closeText: '关闭',  
	    weekHeader: '周',  
	    monthNamesShort: ['一月','二月','三月','四月','五月','六月','七月','八月','九月','十月','十一月','十二月'],  //且让月份显示为中文 
	    dayNames: ['周日','周一','周二','周三','周四','周五','周六'], 
	    dayNamesShort: ['日','一','二','三','四','五','六']
	});
</ww:if>
</ww:iterator>
}
catch(e){
	alert(e);
}