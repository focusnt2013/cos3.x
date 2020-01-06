<%@ page language="java" contentType="text/html; charset=UTF-8"  pageEncoding="UTF-8"%>
function initDetailModel(ui)
{
	var rowData = ui.rowData;
	var data0 = "";
	var tmphtml = "", htmlTabs = "", html = "", metadatahtml = "";
	var detailDataKey;
	try
	{
		detailGrids = {};
	<ww:iterator value="details" status="loop">
		<ww:if test='type==1'><!-- 是metadata-->
		if(showDetail(rowData, "<ww:property value='data'/>") )
		{
			var key = "<ww:property value='data'/>";
			var metadataSize = "<ww:property value='metadata.size'/>";
			detailDataKey = key;
			if( !key ) key = "metadata<ww:property value='#loop.index'/>";
			tmphtml = "<table class='metadata_table'><tr style='background:#efefef;'><td style='width:160px;'>名称</td><td>值</td></tr>";
			if( detailDataKey && metadataSize == 0 )
			{
				htmlTabs += "<li><a href='#tabs-<ww:property value='tab_key'/>'><ww:property value='subject'/></a></li>";
				data0 = rowData[key]; 
				for(var key1 in data0)
				{
					var value = data0[key1];
					if( value == "N/A" ){
						tmphtml += "<tr><td>"+key1+"</td><td style='color:#c0c0c0'>"+value+"</td></tr>";
					}
					else{
						tmphtml += "<tr><td>"+key1+"</td><td>"+value+"</td></tr>";
					}
				}
				tmphtml += "</table>";
				metadatahtml += "<div id='tabs-<ww:property value='tab_key'/>'>"+tmphtml+"</div>";
			}
			else
			{
				htmlTabs += "<li><a href='#tabs-metadata-<ww:property value='#loop.index'/>'><ww:property value='subject'/></a></li>";
				<ww:iterator value="metadata" status="loop">
					data0 = getRowObject(rowData, "<ww:property value='value'/>");
					<ww:if test='nullable'>
					if( data0 == "N/A" ){
						tmphtml += "<tr><td><ww:property value='name'/></td><td style='color:#c0c0c0'>"+data0+"</td></tr>";
					}
					else{
						tmphtml += "<tr><td><ww:property value='name'/></td><td>"+data0+"</td></tr>";
					}
					</ww:if>
					<ww:else>

					if( value == "N/A" ){
						tmphtml += "<tr style='display:"+(data0&&data0!="N/A"?"":"none")+"'><td><ww:property value='name'/></td><td style='color:#c0c0c0'>"+value+"</td></tr>";
					}
					else{
						tmphtml += "<tr style='display:"+(data0&&data0!="N/A"?"":"none")+"'><td><ww:property value='name'/></td><td>"+value+"</td></tr>";
					}
					</ww:else>
				</ww:iterator>
				tmphtml += "</table>";
				metadatahtml += "<div id='tabs-metadata-<ww:property value='#loop.index'/>'>"+tmphtml+"</div>";
			}
		}
		</ww:if>
		<ww:if test='type==3'><!-- 是地图-->
		if(showDetail(rowData, "<ww:property value='data'/>") )
		{
			data0 = getRowObject(rowData, "<ww:property value='data'/>");
			htmlTabs += "<li><a href='#tabs-<ww:property value='tab_key'/>'><ww:property value='subject'/></a></li>";
			if( data0["Longitude"] || data0.latitude )
			{
				var x, y, z;
				try
				{
					if( data0.Longitude ){
						y = data0.Longitude;
					}
					else{
						y = data0.longitude;
					}
					if( data0.Latitude ){
						x = data0.Latitude;
					}
					else{
						x = data0.latitude;
					}
					
					if( data0.Precision ){
						z = data0.Precision;
					}
					else{
						z = data0.precision;
					}
					if( x && y ){
						if( !z ){
							z = 15;
						}
						//alert("Longitude="+y+", Latitude="+x+", Precision="+z+", "+document.getElementById("BaiduMap"));
						var url = "<%=Kit.URL_PATH(request)%>helper!baidumap.action?longitude="+y+"&latitude="+x+"&precision="+z;
						html += "<iframe id='tabs-<ww:property value='tab_key'/>' src='"+url+"' style='width:97%;height:480px;overflow:hidden;margin:0;'></iframe>";

					}
				}
				catch(e)
				{
					data0 = "渲染地图异常"+e;
					html += "<textarea id='tabs-<ww:property value='tab_key'/>' class='details.div' " +
							"style='text-align:center;width:100%;height:128px;border:solid #7F9DB9 1px;background:#000;color:red;'" +
							">"+data0+"</textarea>";
				}
			}
			else
			{
				data0 = "没有经纬度信息";
				html += "<textarea id='tabs-<ww:property value='tab_key'/>' class='details.div' " +
						"style='text-align:center;width:100%;height:128px;border:solid #7F9DB9 1px;background:#000;color:red;'" +
						">"+data0+"</textarea>";
			}
		}
		</ww:if>
		<ww:if test='type==0'>
		if(showDetail(rowData, "<ww:property value='data'/>") )
		{
			data0 = getRowObject(rowData, "<ww:property value='data'/>");
			htmlTabs += "<li><a href='#tabs-<ww:property value='tab_key'/>'><ww:property value='subject'/></a></li>";
			if( data0 )
			{
				html += "<textarea id='tabs-<ww:property value='tab_key'/>' class='details.div' " +
				"style='overflow:auto;white-space:pre-wrap;width:100%;height:200px;border:solid #7F9DB9 1px;background:#fffce9'" +
				">"+data0+"</textarea>";
			}
			else
			{
				data0 = "N/A";
				html += "<textarea id='tabs-<ww:property value='tab_key'/>' " +
						"style='text-align:center;width:100%;height:128px;border:solid #7F9DB9 1px;background:#fffce9;color:#666;'" +
						">"+data0+"</textarea>";
			}
		}
		</ww:if>
		<ww:if test='type==4'><!-- 是媒体消息-->
		if(showDetail(rowData, "<ww:property value='data'/>") )
		{
			htmlTabs += "<li><a href='#tabs-<ww:property value='tab_key'/>'><ww:property value='subject'/></a></li>";
			if( rowData["<ww:property value='data'/>"] )
			{
				data0 = getRowObject(rowData, "<ww:property value='data'/>");
				if( data0 && data0.charAt(0) == 'z' )
				{
					data0 = "<%=Kit.URL_PATH(request)%>"+data0;
				}
				//alert("<ww:property value='data'/>: "+data0);
				html += "<iframe id='tabs-<ww:property value='tab_key'/>' src='"+data0+"' style='width:97%;height:480px;overflow:hidden;margin:0;background:#000'></iframe>"
			}
			else
			{
				data0 = "没有对应的链接URL";
				html += "<textarea id='tabs-<ww:property value='tab_key'/>' class='details.div' " +
						"style='text-align:center;width:100%;height:128px;border:solid #7F9DB9 1px;background:#000;color:red;'" +
						">"+data0+"</textarea>";
			}
		}
		</ww:if>
		<ww:if test='type==2'>
		if(showDetail(rowData, "<ww:property value='data'/>") )
		{
			htmlTabs += "<li><a href='#tabs-<ww:property value='tab_key'/>'><ww:property value='subject'/></a></li>";
			html += "<div id='tabs-<ww:property value='tab_key'/>'></div>";
		}
		</ww:if>
		<ww:if test='type==5'>
		if(showDetail(rowData, "<ww:property value='data'/>") )
		{		
			data0 = getRowObject(rowData, "<ww:property value='data'/>");
			htmlTabs += "<li><a href='#tabs-<ww:property value='tab_key'/>'><ww:property value='subject'/></a></li>";
			html += "<div id='tabs-<ww:property value='tab_key'/>'>"+data0+"</div>";
		}
		</ww:if>
		<ww:if test='type==6'>
		if(showDetail(rowData, "<ww:property value='data'/>") )
		{	
			var left = "<ww:property value='left'/>";
			var right = "<ww:property value='right'/>";
			var key = "<ww:property value='data'/>";
			var lval = rowData[key+".left"];
			var rval = rowData[key+".right"];
			lval = lval?lval:"N/A";
			rval = rval?rval:"N/A";
			var lthml = "<div class='panel panel-default'>"+
				"<div class='panel-heading' style='font-size:12px;padding:5px 15px'>"+left+"</div>"+
				"<div class='panel-body' style='padding-bottom:0px;padding:5px 5px;'>"+lval+"</div></div>";
			var rthml = "<div class='panel panel-default'>"+
			"<div class='panel-heading' style='font-size:12px;padding:5px 15px'>"+right+"</div>"+
			"<div class='panel-body' style='padding-bottom:0px;padding:5px 5px;'>"+rval+"</div></div>";
			
			htmlTabs += "<li><a href='#tabs-<ww:property value='tab_key'/>'><ww:property value='subject'/></a></li>";
			html += "<div id='tabs-<ww:property value='tab_key'/>'><table style='width:100%'><tr>"+
				"<td style='width:50%;font-size:12px;' valign='top'>"+lthml+"</td><td style='padding-left:5px;width:50%;font-size:12px;' valign='top'>"+rthml+"</td><tr></tr>"+
				"</tr></table></div>";
		}
		</ww:if>
	</ww:iterator>
		html += metadatahtml;
		html = "<div class='pq-tabs' style='width:100%;'><ul>"+htmlTabs+"</ul>"+html+"</div><div>";
	}
	catch(e)
	{
		alert(e);
	}
	//create detail place holder
	var $detail = $("<div></div>");
	$detail.html(html);
	$detail.find(".pq-tabs").tabs().on("tabsactivate", function (evt, ui) {
		ui.newPanel.find(".pq-grid").pqGrid("refresh");
	});
	<ww:iterator value="details" status="loop">
	<ww:if test='type==2'>
	detailDataKey = "<ww:property value='data'/>";
	if(showDetail(rowData, detailDataKey) )
	{
		var indx = "<ww:property value='indx'/>";
		var obj1;
		var dataM;
		var remote = <ww:property value="remote" escape='false'/>;
		if( remote ){
			dataM = <ww:property value="dataModel" escape='false'/>;
			var data = {};
			var args = detailDataKey.split(",");
			var args1 = indx.split(",");
			for(var i = 0; i < args.length; i++){
				var arg = args[i].trim();
				var j = arg;
				if(args1 && args1.length){
					j = args1[i].trim();
				}
				data[arg] = getRowObject(rowData, j);
			}
			var url = dataM.url;
			var i, j, field, key, val;
			do{
				i = url.indexOf("%");
				if( i == -1 ) break;
				j = url.indexOf("&", i);
				if( j > 0 ){
					field = url.substring(i, j);
				}
				else{
					field = url.substring(i);
				}
				key = field.substring(1);
				val = data[key];
				url = url.replace(field, val);
				//alert("url("+detailDataKey+")="+url);
			}
			while(true);
			dataM.url = url;
		}
		else{
			dataLocalPlus[detailDataKey] = getRowObject(rowData, detailDataKey);
			dataM = <ww:property value="dataModel" escape='false'/>;
		}
		var colM = <ww:property value="colModel" escape='false'/>;
		obj1 = {
		    dataModel: dataM,
		    colModel: colM,
		    width: "95%",
		    flexWidth: true,
		    flexHeight: true,
		    numberCell: { show: false },
		    showTop: false,
		    showBottom: false
		    <ww:property value="subPageModel" escape='false'/>
		};
		//top.showJson(obj1);
		//showObject(obj.colModel);
		var a = "#tabs-<ww:property value='tab_key'/>";
		$("<div></div>").appendTo($(a, $detail)).pqGrid(obj1);
		detailGrids[detailDataKey] = (obj1);
		if( dodebug && parent && parent.addSubgridDebugMenu ){
			parent.addSubgridDebugMenu(detailDataKey, "<ww:property value='subject'/>", obj1);
		}
		var pq_pager = $detail.find(".pq-pager");
		if( pq_pager ){
			pq_pager.pqPager("option", $.paramquery.pqPager.regional["zh"]);
		}
	}
	</ww:if>
	</ww:iterator>
	return $detail;
}