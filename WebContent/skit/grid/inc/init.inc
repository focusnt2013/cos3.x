<%@ page language="java" contentType="text/html; charset=UTF-8"  pageEncoding="UTF-8"%>
var dodebug = <ww:property value='dodebug'/>;
var stylepreview = <ww:property value="stylepreview"/>;
var showBottom = <ww:property value='showBottom'/>;
var hasToolbar = <ww:property value='hasToolbar'/>;
var showTitle = <ww:property value="showTitle"/>;
var	w = window.innerWidth || document.documentElement.clientWidth || window.document.body.clientWidth; 
var	h = window.innerHeight || document.documentElement.clientHeight || window.document.body.clientHeight;
h -= showTitle?2:2;
//h -= hasToolbar?38:0;
w -= 2;
var $grid = null;
var gridobj;
var colModel;
var dataModel;
var selectionModel;
var detailGrids = {};
var filterFields, fieldOptions, valueLabels, filterRowcls, filterCellcls, dataRemote, dataLocal, dataLocalPlus, dataTemp, summary, toolbar, tmpjson;

try{

	tmpjson = '<ww:property value="filterModel.options" escape="false"/>';
	if( tmpjson ) fieldOptions = jQuery.parseJSON(tmpjson);

	tmpjson = '<ww:property value="filterModel.pq_rowcls" escape="false"/>';
	if( tmpjson ) filterRowcls = jQuery.parseJSON(tmpjson);

	tmpjson = '<ww:property value="filterModel.pq_cellcls" escape="false"/>';
	if( tmpjson ) filterCellcls = jQuery.parseJSON(tmpjson);

	tmpjson = '<ww:property value="filterModel.labels" escape="false"/>';
	if( tmpjson ) valueLabels = jQuery.parseJSON(tmpjson);

	tmpjson = '<ww:property value="localData" escape="false"/>';
	if( tmpjson )  dataLocal = jQuery.parseJSON(tmpjson);

	tmpjson = '<ww:property value="localDataPlus" escape="false"/>';
	if( tmpjson )  dataLocalPlus = jQuery.parseJSON(tmpjson);
	else dataLocalPlus = new Object();

	tmpjson = '<ww:property value="jsonData" escape="false"/>';
	if( tmpjson )  dataTemp = jQuery.parseJSON(tmpjson);

	tmpjson = '<ww:property value="summary" escape="false"/>';
	if( tmpjson ){
		summary = jQuery.parseJSON(tmpjson);
		for(var key in summary)
		{
			summary["type_"+key] = summary[key];
		}
	}

	showBottom = summary?true:showBottom;

	filterFields = {};
	<ww:iterator value="filterModel.fields" status="loop">
	if( '<ww:property value="format"/>' ){
		filterFields['<ww:property value="name"/>'] = {};
		filterFields['<ww:property value="name"/>']["format"] = '<ww:property value="format"/>';
	}
	</ww:iterator>
}
catch(e){
	alert(e);
}

<ww:iterator value="autoCompleteEditors" status="loop">
var <ww:property value="function"/> = function(ui){
	<ww:property value="script" escape="false"/>
};
</ww:iterator>