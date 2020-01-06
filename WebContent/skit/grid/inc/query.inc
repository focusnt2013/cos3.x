<%@ page language="java" contentType="text/html; charset=UTF-8"  pageEncoding="UTF-8"%>
callbackBeforeGridView();
//define colModel
colModel = <ww:property value='colModel' escape='false'/>;
//define dataModel
dataModel = <ww:property value='dataModel' escape='false'/>;
//defin selectionModel
selectionModel = <ww:property value='selectionModel' escape='false'/>;

gridobj = { 
    title: "<ww:property value='viewTitle'/>",
    showTitle: <ww:property value='showTitle'/>,
	width: w,
	height: h,
    wrap: false,
    hwrap: <ww:property value='sortable'/>,
    sortable: <ww:property value='sortable'/>,
    flexWidth: false,
    flexHeight: false,
    columnBorders: true,
    numberCell:{resizable:true, title: "#", minWidth:20, width:30},
    dataModel: dataModel,
    colModel: colModel,
    freezeCols: <ww:property value='freezeCols'/>,
    selectionModel: selectionModel,
    load: function( event, ui ) {
	    var $cell = ui.$cell, rowData = ui.rowData, dataIndx = ui.dataIndx;
    	 <ww:property value='load' escape='false'/>
    },
    editor: {
        select: true
    },
    editModel: { 
	    clicksToEdit: 1, 
	    saveKey: $.ui.keyCode.ENTER,
	    cellBorderWidth: 1
    },
    cellSave: function( event, ui ) {
	    var $cell = ui.$cell, rowData = ui.rowData, dataIndx = ui.dataIndx;
	    try{
	    	
	    }catch(e){
			if( top && top.skit_alert) top.skit_alert("执行单元格数据保存回调出现异常:"+e.message+", 行数"+e.lineNumber);
			else alert("执行单元格数据保存回调出现异常:"+e.message+", 行数"+e.lineNumber);
	    }
    	<ww:property value='cellSave' escape='false'/>
	    filterRow(rowData);
	    //var rowIndx = $grid.pqGrid("getRowIndx", { rowData: rowData }).rowIndx;
        //$grid.pqGrid("refreshRow", { rowIndx: rowIndx });
    	$("#grid_query").pqGrid("refresh");
    },
    cellBeforeSave: function( event, ui ) {
	    var $cell = ui.$cell, rowData = ui.rowData, dataIndx = ui.dataIndx;
    	<ww:property value='cellBeforeSave' escape='false'/>
    },
    cellClick: function( event, ui ) {
	    var $cell = ui.$cell, rowData = ui.rowData, dataIndx = ui.dataIndx;
    	<ww:property value='cellClick' escape='false'/>
    },
    cellEditKeyDown: function( event, ui ) {
	    var $cell = ui.$cell, rowData = ui.rowData, dataIndx = ui.dataIndx;
    	<ww:property value='cellEditKeyDown' escape='false'/>
    },
    cellKeyDown: function( event, ui ) {
	    var $cell = ui.$cell, rowData = ui.rowData, dataIndx = ui.dataIndx;
    	<ww:property value='cellKeyDown' escape='false'/>
    },
    cellDblClick: function( event, ui ) {
	    var $cell = ui.$cell, rowData = ui.rowData, dataIndx = ui.dataIndx;
    	<ww:property value='cellDblClick' escape='false'/>
    },
    cellRightClick: function( event, ui ) {
	    var $cell = ui.$cell, rowData = ui.rowData, dataIndx = ui.dataIndx;
    	<ww:property value='cellRightClick' escape='false'/>
    },
    cellSelect: function( event, ui ) {
	    var $cell = ui.$cell, rowData = ui.rowData, dataIndx = ui.dataIndx;
        if (ui.rowData) {                
			var rowIndx = ui.rowIndx,
               colIndx = ui.colIndx,
               dataIndx = ui.dataIndx,
               cellData = ui.rowData[dataIndx];
        	<ww:property value='cellSelect' escape='false'/>
        }
    },
    filterModel: { on: <ww:property value="filterModel.on"/>, mode: "AND", header: <ww:property value="filterModel.header"/> },
    showBottom: showBottom
    <ww:if test='pageModel!=null'>
    ,pageModel: <ww:property value='pageModel'/>
    </ww:if>
    ,showToolbar: hasToolbar,
    toolbar: {
        cls: "pq-toolbar-search",
        items: toolbar
    },
	detailModel: {
		init: function (ui) {
			return initDetailModel(ui);
        }
    },
    scrollModel: {autoFit: <ww:property value='scrollModelAutoFit'/>},
	//use refresh event to display jQueryUI buttons and bind events.
    refresh: function (evt, ui) {
        //debugger;
        var $grid = $(this);
        if (!$grid) {
            return;
        }
        if( summary )
        {
            $(this).pqGrid("createTable", { dataType: "JSON", data: [summary], $cont: $summary });
        }
		<ww:iterator value="innerbuttons" status="loop">
        $grid.find("button.<ww:property value='class'/>").button({ icons: { primary: "<ww:property value='icon'/>"} })
        .unbind("click")
        .bind("click", function (evt) {
        	<ww:property value='function' escape='false'/>;
        });
        </ww:iterator>
    },
    rowSelect: function (evt, ui) {
        if (ui.rowData) {                
            var rowIndx = parseInt(ui.rowIndx);
            <ww:property value='rowSelect' escape='false'/>
        }
    },
    render: function (evt, ui) {
    	if( summary )
        {
    		$summary = $("<div class='pq-grid-summary'></div>").prependTo($(".pq-grid-bottom", this));
        }
    },
    create: function (evt, ui) {
    },
    //hoverMode: 'row',
    resizable: false
    <ww:property value="beforeTableView" escape="false"/>
};
$grid = $("#grid_query").pqGrid(gridobj);