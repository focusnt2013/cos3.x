<%@ page language="java" contentType="text/html; charset=UTF-8"  pageEncoding="UTF-8"%>
//打开弹出对话框
var popupRowId;//行ID
var popupRowIndx;//行号
function openPopupDialog(id, title, width, height) {
	//var $frm = $("form#crud-form");
	//$frm.find("input").val("");
	if( nopopup[id] && nopopup[id]() )
	{
		return;
	}
	$("#popup-dialog-crud").dialog({ 
		title: title,
	    width: width,
		height: height,
	    open: function(){
	    	if( openpopup[id] )
		    {
	    		openpopup[id]();
		    }
	    },
	    close: function(){
	    	if( closepopup[id] )
		    {
	    		closepopup[id]();
		    }
	    }
	});
	$("#popup-dialog-crud").dialog("open");
}
//打开弹出的触发函数脚本
var openPopupEditor = function( ui ) {
    //debugger;
    var $cell = ui.$cell,
        rowData = ui.rowData,
        dataIndx = ui.dataIndx,
        width = ui.column.width,
        cls = ui.cls;
    var dc = $.trim(rowData[dataIndx]);
    var $inp = $("<input type='text' name='" + dataIndx + "' class='" + cls + " pq-ac-editor' />")
        .width(width - 6)
        .appendTo($cell)
        .val(dc);
    if( openEditorPopup(dataIndx, rowData, $inp) )
    {
        $inp.autocomplete({
            minLength: 0
        }).focus(function () {
            //open the autocomplete upon focus
            $(this).autocomplete("search", "");
        });
    }
    else
    {
    	$inp.val("");
    }
}
/*#####################################################################################################################################*/
/*打开编辑对话框*/
var popupObjs = {}, popupColModel = {}, popupDataModel = {};
var closeCallback = {}, openCallback = {};
var callbackRowData;
<ww:iterator value="editorPopups" status="loop">
popupColModel['<ww:property value="name"/>'] = <ww:property value="colModel" escape="false"/>;
popupDataModel['<ww:property value="name"/>'] = <ww:property value="dataModel" escape="false"/>;
popupObjs['<ww:property value="name"/>'] = <ww:property value="popupObj" escape="false"/>;
closeCallback['<ww:property value="name"/>'] = <ww:property value="closecallback" escape="false"/>;
openCallback['<ww:property value="name"/>'] = <ww:property value="opencallback" escape="false"/>;
</ww:iterator>
function openEditorPopup(dataIndx, rowData, input)
{
	if( popupObjs && popupObjs[dataIndx]);
    {
    	var dataModel = popupDataModel[dataIndx];
    	var objPopup = popupObjs[dataIndx];
		$("#pq-grid-in-popup-dialog").attr("title", objPopup.title);
	    $("#pq-grid-in-popup-dialog").dialog({
	        height: 400,
	        width:  objPopup.width,
	        open: function (evt, ui) {
	            var $grid = $("#grid_in_popup_grid");
	            var ht = $grid.parent().height() - 2;
	            var wd = $grid.parent().width() - 2;

	            if ($grid.hasClass('pq-grid')) {
	                $grid.pqGrid("option", { height: ht, width: wd });
	            }
	            else {
	                objPopup.width = wd;
	                objPopup.height = ht;
	                callbackRowData = rowData;
		        	var callback = openCallback[dataIndx];
		            if(callback){
			            var popupData = dataLocalPlus[dataIndx];
		            	callback(rowData, popupData, input);
		            }
	                $grid.pqGrid(objPopup);
	            }
	        },
	        close: function () {
	            var $grid = $("#grid_in_popup_grid");
	            var popupData = dataLocalPlus[dataIndx];
	        	var callback = closeCallback[dataIndx];
	            if(callback){
	            	callback(rowData, popupData, input);
	            }
	            $grid.pqGrid('destroy');
	        },
	        resizeStop: function (evt, ui) {
	            var $grid = $("#grid_in_popup_grid");
	            var ht = $grid.parent().height();
	            var wd = $grid.parent().width();
	            $grid.pqGrid("option", { height: ht - 2, width: wd - 2 });
	            $grid.pqGrid("refresh");
	        },
	        show: {
	            effect: "blind",
	            duration: 500
	        },
	        hide: {
	            effect: "explode",
	            duration: 500
	        }
	    });
    	return false;
    }
   	return true;
}