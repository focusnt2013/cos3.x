<%@page language="java" pageEncoding="UTF-8"%>
<%@ page import="com.focus.cos.web.common.Kit"%>
<%@ taglib uri="/webwork" prefix="ww" %>
<%Kit.noCache(request,response);  %>
<html>
<head>
<%=Kit.getJSTag(request, "skit/hc/highcharts.js")%>
<%=Kit.getJSTag(request, "skit/hc/exporting.js")%>
<%=Kit.getJSTag(request, "skit/hc/oldie.js")%>
<%=Kit.getJSTag(request, "skit/hc/annotations.js")%>
<script src="skit/hc/<ww:property value='filetype'/>.js"></script>
</head>
<body style='overflow-y: auto;'>
<ww:iterator value="chartDatasets" status="loop">
<div id='<ww:property value="id"/>'></div><br/>
</ww:iterator>
</body>
<%@ include file="../../skit/inc/skit_cos.inc"%>
<SCRIPT type="text/javascript">
/*实现窗口对齐*/
var charts = new Object();
var freezeCols = <ww:property value='freezeCols'/>;
function resizeWindow()
{
	var freezeRows = 3;
	if( windowHeight < 360 ) freezeRows = 1;
	else if( windowHeight < 480 ) freezeRows = 2;
	var chartHeight = windowHeight;
	if( freezeCols > 1 )
	{
		chartHeight = windowHeight/freezeRows - (freezeRows>1?50:0);
	}
	<ww:iterator value="chartDatasets" status="loop">
	chart = charts['<ww:property value="id"/>'];
	chart.setSize(windowWidth, chartHeight - 10);
	</ww:iterator>
}

function drawChart(divId, h, title, timeSeries, dataSeries, ytitle, subtitle, unit, tips)
{
	var legend = {
        layout: 'vertical',
        align: 'right',
        verticalAlign: 'top',
        x: -10,
        y: 100,
        borderWidth: 0
    };
    return Highcharts.chart(divId, {
        chart: {
            type: 'area',
            marginRight: 128,
            marginBottom: 48,
            zoomType: 'x',
            panning: true,
            panKey: 'shift'
        },
        title: {
            text: title,
            x: -20 //center
        },
        annotations: tips,
        subtitle: {
            text: subtitle,
            color: '#FFFFFF',
            x: -20
        },
        xAxis: {
            categories: timeSeries,
            tickInterval: 60
        },
        yAxis: {
            startOnTick: true,
            endOnTick: false,
            maxPadding: 0.2,
            min:0,
            allowDecimals:false,
            title: {
                text: ytitle
            },
        },
        legend: legend,
        tooltip: {
            formatter: function() {
            	if( unit == "bytes/s" )
            	{
            		return "在"+this.x +'的'+this.series.name+'是'+byteScale(this.y);
            	}
            	else if( unit == "毫秒/次" )
            	{
            		var v = parseFloat(this.y);
            		if( this.series.name == "平均延时" ){
	                   	return "在"+this.x +'的'+this.series.name+'是'+v.toFixed(2)+unit;
            		}
            		else{
            			return "在"+this.x +'的'+this.series.name+'是'+v.toFixed(2)+"毫秒";
            		}
            	}
                //return this.x +'的'+ytitle+'是'+this.y+unit;
               	return this.x +'的'+this.series.name+'是'+this.y+unit;
            },
        },
        series: dataSeries
    });
}

function byteScale(y){
	var v = parseFloat(y);
	var u = "";
	var v1 = v/1024;
	if( parseInt(v1) > 0 )
	{
		v = v1;
		v1 = v/1024;
		if( parseInt(v1) > 0 )
		{
    		v = v1;
    		v1 = v/1024;
    		if( parseInt(v1) == 0 )
    		{
    			u = 'M' + u;
			}
    		else {
    			v = v1;
    			u = 'G' + u;
    		}
		}
		else
		{
			u = 'K'+u;
		}
	}
	return v.toFixed(2)+u; 
}

var freezeRows = 0;
var id, dataSeries, timeSeries, title, ytitle, unit, tips;
try{
	tips = [{"labels":[{"text":"程序在2018-06-28 11:34启动","point":{"yAxis":0,"xAxis":0,"x":814,"y":0}}]}];
	<ww:iterator value="chartDatasets" status="loop">
	id = '<ww:property value="id"/>';
	dataSeries = [<ww:property value="dataSeries" escape='false'/>];
	timeSeries = <ww:property value="timeSeries" escape='false'/>;
	title = '<ww:property value="title"/>';
	ytitle = '<ww:property value="ytitle"/>';
	unit = '<ww:property value="unit"/>';
	tips = <ww:property value="tips" escape='false'/>;
	//top.showJson(timeSeries);
	freezeRows += 1;
	charts[id] = drawChart( id, 220,  title,  timeSeries,  dataSeries,  ytitle, "", unit, tips);
	</ww:iterator>
}
catch(e){
	alert(e);	
}
resizeWindow();
</SCRIPT>
</html>