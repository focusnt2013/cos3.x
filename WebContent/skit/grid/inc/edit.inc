<%@ page language="java" contentType="text/html; charset=UTF-8"  pageEncoding="UTF-8"%>
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
    track: true, //to turn on the track changes.
    wrap: false,
    hwrap: false,
    sortable: true,
    flexWidth: false,
    flexHeight: false,
    columnBorders: true,
    numberCell:{resizable:<ww:property value='numberCell'/>, title: "#", minWidth:16, width:32},
    dataModel: dataModel,
    colModel: colModel,
    freezeCols: <ww:property value='freezeCols'/>,
    hoverMode: 'cell',
    selectionModel: selectionModel, 
    cellSave: function( event, ui ) {
	    var $cell = ui.$cell, rowData = ui.rowData, dataIndx = ui.dataIndx;
	    try{
	    	<ww:property value='cellSave' escape='false'/>
	    	//$("#grid_edit").pqGrid("refreshCell", { rowIndx: ui.rowIndx, dataIndx: ui.dataIndx });
	    }catch(e){
			if( top && top.skit_alert) top.skit_alert("执行单元格数据保存回调出现异常:"+e.message+", 行数"+e.lineNumber);
			else alert("执行单元格数据保存回调出现异常:"+e.message+", 行数"+e.lineNumber);
	    }
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
    filterModel: { on: <ww:property value="filterModel.on"/>, mode: "AND", header: <ww:property value="filterModel.on"/> },
    showToolbar: <ww:property value='hasToolbar'/>,
    <ww:if test='pageModel!=null'>
    pageModel: <ww:property value='pageModel'/>,
    </ww:if>
    toolbar: {
        cls: "pq-toolbar-search",
        items: toolbar
    },
    //save the cell when cell loses focus.
     quitEditMode: function (evt, ui) {
         var $grid = $(this);
         if (evt.keyCode != $.ui.keyCode.ESCAPE) {
             $grid.pqGrid("saveEditCell");
         }
     },
     //make rows editable selectively.
     editable: function (ui) {
         var $grid = $(this);
         var rowIndx = ui.rowIndx;
         if ($grid.pqGrid("hasClass", { rowIndx: rowIndx, cls: 'pq-row-edit' }) == true) {
             return true;
         }
         else {
             return false;
         }
     },
     load: function( event, ui ) {
    	 <ww:property value='load' escape='false'/>
     },
     //use refresh event to display jQueryUI buttons and bind events.
     refresh: function () {
         //debugger;
         var $grid = $(this);
         if (!$grid) {
             return;
         }
         if( summary )
         {
             $(this).pqGrid("createTable", { dataType: "JSON", data: [summary], $cont: $summary });
         }
         //delete button
         $grid.find("button.delete_btn").button({ icons: { primary: 'ui-icon-close'} })
         .unbind("click").bind("click", function (evt) {
             if (isEditing($grid)) {
                 return false;
             }
             var $tr = $(this).closest("tr"),
                 rowIndx = $grid.pqGrid("getRowIndx", { $tr: $tr }).rowIndx;
             deleteRow(rowIndx, $grid);
         });
         //edit button
         $grid.find("button.edit_btn").button({ icons: { primary: 'ui-icon-pencil'} })
         .unbind("click").bind("click", function (evt) {
             if (isEditing($grid)) {
                 return false;
             }
             var $tr = $(this).closest("tr"),
                 rowIndx = $grid.pqGrid("getRowIndx", { $tr: $tr }).rowIndx;
             editRow(rowIndx, $grid);
             return false;
         });

        //rows which were in edit mode before refresh, put them in edit mode again.
        var rows = $grid.pqGrid("getRowsByClass", { cls: 'pq-row-edit' });
        if (rows.length > 0) {
             var rowIndx = rows[0].rowIndx;
             editRow(rowIndx, $grid);
        }

		<ww:iterator value="innerbuttons" status="loop">
        $grid.find("button.<ww:property value='class'/>").button({ icons: { primary: "<ww:property value='icon'/>"} })
        .unbind("click")
        .bind("click", function (evt) {
            var $tr = $(this).closest('tr'), rowIndx = $grid.pqGrid('getRowIndx', { $tr: $tr }).rowIndx;
            var rowData = $grid.pqGrid('getRowData', {rowIndx: rowIndx});
        	<ww:property value='function' escape='false'/>;
        });
        </ww:iterator>
    },
    render: function (evt, ui) {
    	if( summary )
        {
    		$summary = $("<div class='pq-grid-summary'></div>").prependTo($(".pq-grid-bottom", this));
        }
    },
    showBottom: showBottom,
    resizable: false
    <ww:property value="beforeTableView" escape="false"/>
};
$grid = $("#grid_edit").pqGrid(gridobj);
