<%@page language="java" pageEncoding="UTF-8"%>
<%@ page import="com.focus.cos.web.common.Kit"%>
<%@ taglib uri="/webwork" prefix="ww" %>
<%Kit.noCache(request,response);  %>
<html>
<head>
<style type='text/css'>
</style>
<%=Kit.getJSTag(request, "skit/hc/highcharts.js")%>
<%=Kit.getJSTag(request, "skit/hc/exporting.js")%>
<%=Kit.getJSTag(request, "skit/hc/oldie.js")%>
<%=Kit.getJSTag(request, "skit/hc/annotations.js")%>
<script src="skit/hc/<ww:property value='filetype'/>.js"></script>
</head>
<body style='overflow-y: hidden;'>
<div style='cursor:pointer;padding-top:3px;padding-bottom:0px;' id='btnPage'>
	<div class="form-group" style='margin-bottom:3px;'>
		<div class="input-group">
			<span class="input-group-addon" style='font-size:12px;height:26px;'>样式主题</span>
			<select class="form-control" id='theme' style='width:96px;font-size:12px;height:30px;margin-right:3px;'
				onchange='changeTheme(this)'>
			  	<option <ww:if test='filetype.equals("gray")'>selected</ww:if> value='gray'>灰</option>
			  	<option <ww:if test='filetype.equals("dark-unica")'>selected</ww:if> value='dark-unica'>暗黑</option>
			  	<option <ww:if test='filetype.equals("dark-green")'>selected</ww:if> value='dark-green'>暗绿</option>
			  	<option <ww:if test='filetype.equals("dark-blue")'>selected</ww:if> value='dark-blue'>暗蓝</option>
			  	<option <ww:if test='filetype.equals("avocado")'>selected</ww:if> value='avocado'>鳄梨</option>
			  	<option <ww:if test='filetype.equals("grid")'>selected</ww:if> value='grid'>表格</option>
			  	<option <ww:if test='filetype.equals("grid-light")'>selected</ww:if> value='grid-light'>亮格</option>
			  	<option <ww:if test='filetype.equals("sand-signika")'>selected</ww:if> value='sand-signika'>砂砾</option>
			  	<option <ww:if test='filetype.equals("sunset")'>selected</ww:if> value='sunset'>日落</option>
			  	<option <ww:if test='filetype.equals("skies")'>selected</ww:if> value='skies'>天空</option>
			</select>
			<div style='padding-top:0px;'>
			    <button type="button" class="btn btn-default btn-circle" onclick="changePage(-1)" title='前一页' id='btnBackward'>
			   	<i class="fa fa-backward"></i></button>
			    <button type="button" class="btn btn-default btn-circle" onclick="changePage(1)" title='后一页' id='btnForward'>
			   	<i class="fa fa-forward"></i></button>
			   	<span id='spanInfo'></span>
			</div>
		</div>
	</div>
</div>
<div id='container'>
	<div id="div2"></div><br/>
	<div id="div1"></div><br/>
	<div id="div3"></div><br/>
</div>
<form>
<input type='hidden' name='filetype' id='filetype'>
<input type='hidden' name='id' id='id' value="<ww:property value='id'/>">
<input type='hidden' name='ip' id='ip' value="<ww:property value='ip'/>">
<input type='hidden' name='port' id='port' value="<ww:property value='port'/>">
</form>
</body>
<%@ include file="../../skit/inc/skit_cos.inc"%>
<SCRIPT type="text/javascript">
var gc = <ww:property value="command"/>;
var chart1;
var chart2;
var chart3;
//gc = 0;
/*实现窗口对齐*/
function resizeWindow()
{
	var container = document.getElementById( 'container' );
	if( gc != 1 ){
		var div1 = document.getElementById( 'div1' );
		div1.style.display = "none";
		var div3 = document.getElementById( 'div3' );
		div3.style.display = "none";
	}
	var btnPage = document.getElementById( 'btnPage' );
	container.style.height = windowHeight-btnPage.clientHeight- 8;
	var chartHeight = gc==1?400:((windowHeight-btnPage.clientHeight) - 16);
	//chartHeight = windowHeight/3 - 5;
	chart2.setSize(windowWidth -2, chartHeight);
	if( gc == 1 ){
		chart1.setSize(windowWidth - 3, chartHeight);
		chart3.setSize(windowWidth - 3, chartHeight);
	}
	//btnPage.style.paddingLeft = windowWidth/2 - btnPage.clientWidth;
}

$( '#container' ).niceScroll({
	cursorcolor: '<%=(String)session.getAttribute("System-Theme")%>',
	railalign: 'right',
	cursorborder: "none",
	horizrailenabled: true, 
	zindex: 2001,
	left: '245px',
	cursoropacitymax: 0.6,
	cursorborderradius: "0px",
	spacebarenabled: false 
});

function changeTheme(sel){

	setUserActionMemory("monitor!modulememory.theme", sel.value);
	document.getElementById('filetype').value = sel.value;
	document.forms[0].submit();
}

function drawChart(divId, h, title, timeSeries, dataSeries, ytitle, subtitle, unit, tips)
{
	var legend = {};
	if( gc == 1 ){
		legend = {
            layout: 'vertical',
            align: 'right',
            verticalAlign: 'top',
            x: -10,
            y: 100,
            borderWidth: 0
        };
	}
    return Highcharts.chart(divId, {
        chart: {
            type: 'area',
            marginRight: gc==1?128:0,
            marginBottom: gc!=1?72:25,
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
            tickInterval: 5+tick/10
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
               	return this.x +'的'+this.series.name+'是'+this.y;
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

var gcCounter = [<ww:property value="gcCounter"/>];
var fullGcCounter = [<ww:property value="fullGcCounter"/>];
var hb = [<ww:property value="hb"/>];
var he = [<ww:property value="he"/>];
var hc = [<ww:property value="hc"/>];
var maxGcDelay = [<ww:property value="maxGcDelay"/>];
var minGcDelay = [<ww:property value="minGcDelay"/>];
var avgGcDelay = [<ww:property value="avgGcDelay"/>];
var timeSeries = [<ww:property value="timeSeries"/>];
var page = 1;
var btnBackwardBlock = false;
var btnForwardBlock = false;
var tick = 180;
var tips_startup = <ww:property value='jsonData' escape='false'/>;
if( gc != 1 ){
	hb = hc;
	//top.showJson(tips_startup);
}
function changePage(p){
	try{
		var _gcCounter = [];
		var _fullGcCounter = [];
		var _hb = [];
		var _he = [];
		var _hc = [];
		var _maxGcDelay = [];
		var _minGcDelay = [];
		var _avgGcDelay = [];
		var _timeSeries = [];
		var btnForward = document.getElementById( 'btnForward' );
		var btnBackward = document.getElementById( 'btnBackward' );
		page = page-p;
		if( btnBackwardBlock && p == -1 ){
			return;
		}
		if( btnForwardBlock && p == 1 ){
			return;
		}
		if( page <= 1 ){
			btnForward.style.color = "#cfcfcf";
			btnForwardBlock = true;
		}
		else{
			btnForward.style.color = "";
			btnForwardBlock = false;
		}
		var skip = page*tick;
		if( skip > timeSeries.length ) {
			btnBackward.style.color = "#cfcfcf";
			btnBackwardBlock = true;
		}
		else{
			btnBackward.style.color = "";
			btnBackwardBlock = false;
		}
		//alert(skip+":"+timeSeries.length);
		var spanInfo = document.getElementById( 'spanInfo' );
		var i = timeSeries.length-skip;
		var k;
		if( i < 0 ) i = 0;
		if(i == 0&&timeSeries.length<tick){
			var time = timeSeries[0];
			var date = new Date();
		    var year = date.getFullYear();
			var month = "", day = "", minute = "", second = "";
			time = Date.parse(year+"-"+time+":00");
			for(k=0;k<tick-timeSeries.length;k++){
				_gcCounter.push(0);
				_fullGcCounter.push(0);
				_hb.push(0);
				_he.push(0);
				_hc.push(0);
				_maxGcDelay.push(0);
				_minGcDelay.push(0);
				_avgGcDelay.push(0);
				
				time -= 60*1000;
				date.setMilliseconds(time);
			    month = date.getMonth() + 1;
			    day = date.getDate();
			    month = month < 10 ? ("0" + month) : month;
			    day = day < 10 ? ("0" + day) : day;
			    minute = date.getMinutes();
			    second = date.getSeconds();
			    minute = minute < 10 ? ("0" + minute) : minute;
			    second = second < 10 ? ("0" + second) : second;
				_timeSeries.push(month+"-"+day+" "+minute+":"+second);
			}
		}
		var label;
		k = _timeSeries.length;
		var maxGc = 0, _maxGc = 0, maxFullgc = 0, _maxFullgc = 0;
		var maxHb = 0, _maxHb = 0, minHe = 0, _minHe = k;
		var maxDelay = 0, _maxDelay = 0, minDelay = 0, _minDelay = k;
		for(; i < timeSeries.length; i++,k++){
			if( k > tick ){
				break;
			}
			_gcCounter.push(gcCounter[i]);
			_fullGcCounter.push(fullGcCounter[i]);
			_hb.push(hb[i]);
			_he.push(he[i]);
			_hc.push(hc[i]);
			_maxGcDelay.push(maxGcDelay[i]);
			_minGcDelay.push(minGcDelay[i]);
			_avgGcDelay.push(avgGcDelay[i]);
			_timeSeries.push(timeSeries[i]);
			
			if(_hb[k]>=_hb[_maxHb]){
				_maxHb = k;
			}
			if(_he[k]<_he[_minHe]){
				_minHe = k;
			}
			if(_maxGcDelay[k]>=_maxGcDelay[_maxDelay]){
				_maxDelay = k;
			}
			if(_minGcDelay[k]<_minGcDelay[_minDelay]){
				_minDelay = k;
			}
			if(_gcCounter[k]>=_gcCounter[_maxGc]){
				_maxGc = k;
			}
			if(_fullGcCounter[k]>=_fullGcCounter[_maxFullgc]){
				_maxFullgc = k;
			}
			if( tips_startup.labels && tips_startup.labels.length > 0 ){
				for(var j = 0; j < tips_startup.labels.length; j++){
					label = tips_startup.labels[j];
					if( label.startup_time == timeSeries[i] ){
						label.point.x = k;
						label.point.y = gc==1?hc[i]:hb[i];//将启动提示放在堆栈空间上
					}
				}
				//top.showJson(tips_startup);
			}
		}
		for(i = 0; i < timeSeries.length; i++){
			if(hb[i]>hb[maxHb]){
				maxHb = i;
			}
			if(he[i]<he[minHe]){
				minHe = i;
			}
			if(maxGcDelay[i]>maxGcDelay[maxDelay]){
				maxDelay = i;
			}
			if(minGcDelay[i]<minGcDelay[minDelay]){
				minDelay = i;
			}
			if(gcCounter[i]>gcCounter[maxGc]){
				maxGc = i;
			}
			if(fullGcCounter[i]>fullGcCounter[maxFullgc]){
				maxFullgc = i;
			}
		}
		var tips = [];
		var tips_stack = {};
		var tips_gc = {};
		var tips_delay = {};
		if( timeSeries.length > 0 ) {
			var html = "<span style='font-size: 9pt;'>总时间范围"+timeSeries[0]+" ~ "+timeSeries[timeSeries.length-1];
			html += "</span>, [峰值内存]: "+byteScale(hb[maxHb])+"("+timeSeries[maxHb]+")";
			if( gc == 1 ){
				html += ", [垃圾回收]: "+gcCounter[maxGc]+"次("+timeSeries[maxHb]+")";
				html += ", [强制回收]: "+fullGcCounter[maxGc]+"次("+timeSeries[maxHb]+")";
				html += ", [最大时延]: "+maxGcDelay[maxDelay].toFixed(2)+"毫秒("+timeSeries[maxDelay]+")";
			}
			spanInfo.style.fontSize = "8pt";
			spanInfo.style.color = "#8d8d8d";
			spanInfo.innerHTML = html;
			
			tips_stack = {
				labelOptions: {
		            shape: 'connector',
		            align: 'right',
		            justify: false,
		            crop: true,
		            style: {
		                fontSize: '0.98em',
		                textOutline: '1px white'
		            }
		        }
			};
			tips_stack.labels = [];
			
			label = {};
			label.text = "时间点:"+_timeSeries[_maxHb]+"<br/>内存峰值:<span style='font-size:14px;color:#483d8b;font-weight:bold;'>"+
				byteScale(_hb[_maxHb])+"</span><br/>(3小时范围内)";
			label.point = {};
			label.point.x = _maxHb;
			label.point.y = _hb[_maxHb];
			label.point.xAxis = 0;
			label.point.yAxis = 0;
			tips_stack.labels.push(label);
			
			if( _he[_minHe] > 0 ){
				label = {};
				label.text = "时间点:"+_timeSeries[_minHe]+"<br/>内存最小:<span style='font-size:14px;color:#483d8b;font-weight:bold;'>"+
					byteScale(_he[_minHe])+"</span>";
				label.point = {};
				label.point.x = _minHe;
				label.point.y = _he[_minHe];
				label.point.xAxis = 0;
				label.point.yAxis = 0;
				tips_stack.labels.push(label);
			}

			tips_gc = {
				labelOptions: {
		            shape: 'connector',
		            align: 'right',
		            justify: false,
		            crop: true,
		            style: {
		                fontSize: '0.98em',
		                textOutline: '1px white'
		            }
		        }
			};
			tips_gc.labels = [];
			label = {};
			label.text = "时间点:"+_timeSeries[_maxGc]+"<br/>GC次数:<span style='font-size:14px;color:#483d8b;font-weight:bold;'>"+
				_gcCounter[_maxGc]+"</span>";
			label.point = {};
			label.point.x = _maxGc;
			label.point.y = _gcCounter[_maxGc];
			label.point.xAxis = 0;
			label.point.yAxis = 0;
			tips_gc.labels.push(label);
			
			if( _fullGcCounter[_maxFullgc] > 0 ){
				label = {};
				label.text = "时间点:"+_timeSeries[_maxFullgc]+"<br/>FullGC次数:<span style='font-size:14px;color:#483d8b;font-weight:bold;'>"+
					_gcCounter[_maxFullgc]+"</span>";
				label.point = {};
				label.point.x = _maxFullgc;
				label.point.y = _fullGcCounter[_maxFullgc];
				label.point.xAxis = 0;
				label.point.yAxis = 0;
				tips_gc.labels.push(label);
			}
		}
		else{
			spanInfo.innerHTML = "没有任何内存监控数据";
		}

		if( gc == 1 ){
			var data1 = [{
	            name: 'GC次数',
	   	        lineColor: Highcharts.getOptions().colors[1],
	   	        color: "#f0e68c",
	   	        fillOpacity: 0.5,
	   	        marker: {
	   	            enabled: false
	   	        },
	   	        threshold: null,
	            data: _gcCounter
	        }, {
	            name: 'FullGc次数',
	   	        lineColor: Highcharts.getOptions().colors[1],
	   	        color: '#e9967a',
	   	        fillOpacity: 0.5,
	   	        marker: {
	   	            enabled: false
	   	        },
	   	        threshold: null,
	            data: _fullGcCounter
	        }];
	        tips = [];
	        tips.push(tips_gc);
			chart1 = drawChart("div1", 200, "GC分析", _timeSeries, data1, '内存释放次数',
				"根据GC次数的频繁程度可以推断进程内存配置是否合理，如果出现大量FullGC那么进程逻辑需要优化", "", tips);
		}
		var data2 = [];
		if( gc == 1 ){
			data2.push({
	            name: '总堆栈空间',
	   	        lineColor: Highcharts.getOptions().colors[1],
	   	        color: '#d3d3d3',
	   	        fillOpacity: 0.5,
	   	        marker: {
	   	            enabled: false
	   	        },
	   	        threshold: null,
	            data: _hc
	        });
		}
		data2.push({
            name: '开始堆空间',
   	        lineColor: Highcharts.getOptions().colors[1],
   	        color: '#e9967a',
   	        fillOpacity: 0.5,
   	        marker: {
   	            enabled: false
   	        },
   	        threshold: null,
            data: _hb
        });
		data2.push({
            name: '结束堆空间',
   	        lineColor: Highcharts.getOptions().colors[1],
   	        color: "#48d1cc",
   	        fillOpacity: 0.5,
   	        marker: {
   	            enabled: false
   	        },
   	        threshold: null,
            data: _he
        });
        tips = [];
        tips.push(tips_stack);
        tips.push(tips_startup);
        //top.showJson(tips);
		chart2 = drawChart("div2", 200, "堆栈分析", _timeSeries, data2, '堆栈空间',
			"总堆空间是系统分配给进程的内存，结束堆空间是系统当前使用的内存，如果结束堆空间接近总堆空间程序可能随时会内存溢出", "bytes/s", tips);
		if( gc == 1 ){
			var data3 = [{
		            name: '最大延时',
		   	        lineColor: Highcharts.getOptions().colors[1],
		   	        color: '#e9967a',
		   	        fillOpacity: 0.5,
		   	        marker: {
		   	            enabled: false
		   	        },
		   	        threshold: null,
		            data: _maxGcDelay
		        },
		        {
		            name: '平均延时',
		   	        lineColor: Highcharts.getOptions().colors[1],
		   	        color: '#f0e68c',
		   	        fillOpacity: 0.5,
		   	        marker: {
		   	            enabled: false
		   	        },
		   	        threshold: null,
		            data: _avgGcDelay
		        }, 
		        {
		            name: '最小延时',
		   	        lineColor: Highcharts.getOptions().colors[1],
		   	        color: "#d3d3d3",
		   	        fillOpacity: 0.5,
		   	        marker: {
		   	            enabled: false
		   	        },
		   	        threshold: null,
		            data: _minGcDelay
		        }
	        ];
			tips_delay = {
				labelOptions: {
		            shape: 'connector',
		            align: 'right',
		            justify: false,
		            crop: true,
		            style: {
		                fontSize: '0.98em',
		                textOutline: '1px white'
		            }
		        }
			};
			tips_delay.labels = [];
			label = {};
			label.text = "时间点:"+_timeSeries[_maxDelay]+"<br/>释放内存时延:<span style='font-size:14px;color:#483d8b;font-weight:bold;'>"+
				_maxGcDelay[_maxDelay].toFixed(2)+"毫秒</span>";
			label.point = {};
			label.point.x = _maxDelay;
			label.point.y = _maxGcDelay[_maxDelay];
			label.point.xAxis = 0;
			label.point.yAxis = 0;
			tips_delay.labels.push(label);

			if( _minGcDelay[_minDelay] > 0 ){
				label = {};
				label.text = "时间点:"+_timeSeries[_minDelay]+"<br/>释放内存时延:<span style='font-size:14px;color:#483d8b;font-weight:bold;'>"+
					_minGcDelay[_minDelay].toFixed(2)+"毫秒</span>";
				label.point = {};
				label.point.x = _minDelay;
				label.point.y = _minGcDelay[_minDelay];
				label.point.xAxis = 0;
				label.point.yAxis = 0;
				tips_delay.labels.push(label);
			}
			
	        tips = [];
	        tips.push(tips_delay);
	        //top.showJson(tips);
			chart3 = drawChart("div3", 200, "GC延时分析", _timeSeries, data3, 'GC延时(毫秒)', "", "毫秒/次", tips);
		}
		resizeWindow();	
	}
	catch(e){
		alert(e);
	}
}
changePage(0);
</SCRIPT>
</html>