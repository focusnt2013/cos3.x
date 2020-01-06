<%@ page language="java" pageEncoding="UTF-8"%>
<%@ page import="com.focus.cos.web.common.Kit"%>
<%@ taglib uri="/webwork" prefix="ww" %>
<%Kit.noCache(request,response);  %>
<html style='margin-top:-10px;'>
<head>
<link type="text/css" href="skit/css/bootstrap.css" rel="stylesheet">
</head>
<body style="margin:1px;">
<form>
<input type='hidden' name='command' id='command' value='1'>
<input type='hidden' name='path' id='path' value='<ww:property value='path'/>'>
<input type='hidden' name='rootdir' id='rootdir' value='<ww:property value='rootdir'/>'>
<input type='hidden' name='length' id='length' value='<ww:property value='length'/>'>
<input type='hidden' name='filetype' id='filetype' value='<ww:property value='filetype'/>'>
<input type='hidden' name='ip' id='ip' value="<ww:property value='ip'/>">
<input type='hidden' name='port' id='port' value="<ww:property value='port'/>">
<input type='hidden' name='ww' id='ww'>
<input type='hidden' name='encoding' id='_encoding' value=''>
</form>
<table style='width:100%'>
<tr>
	<td width='220' style='padding-right:10px'>
		<div class="well profile" id='divProfile'>
		    <div>
		        <!-- <h2><ww:property value='localDataObject.getString("filename")'/></h2>
		        <p><strong>文件路径: </strong> <ww:property value='localDataObject.getString("path")'/></p> -->
		        <p><span class="tags">
		         	<i class="fa fa-<ww:property value='localDataObject.getString("icon")'/> fa-fw"></i>
		         	<ww:property value='filetype'/></span></p>
		        <p><strong>文件描述: </strong> <ww:property value='localDataObject.getString("description")'/></p>
		        <p><strong>文件大小: </strong> <ww:property value='localDataObject.getString("size")'/></p>
		        <p><strong>文件长度: </strong> <ww:property value='localDataObject.getLong("length")'/></p>
		        <p><strong>修改时间: </strong> <ww:property value='localDataObject.getString("time")'/></p>
		        <p><strong>文件用户: </strong> </p>
		    </div>            
		    <div class="col-xs-12 text-center">
		    	<div class="row">
			        <div style='margin-top:10px;'>
			            <button onclick='showfile()' class="btn btn-outline btn-primary btn-block"><span class="fa fa-refresh"></span> 刷新预览</button>
			        </div>
		    	</div>
		    	<div class="row">
			        <div style='margin-top:10px;'>
			            <button onclick='download()' class="btn btn-success btn-block"><span class="fa fa-download"></span> 下载文件 </button>
			        </div>
		    	</div>
		    	<div class="row">
			        <div style='margin-top:10px;'>
		          	  <button onclick='showdigit()' class="btn btn-danger btn-block"><span class="fa fa-code"></span> 查看源码 </button>
			        </div>
		    	</div>
		    	<ww:if test='decompressable'>
		    	<div class="row">
			        <div style='margin-top:10px;'>
		          	  <button onclick='uncompress()' class="btn btn-info btn-block"><span class="fa fa-expand"></span> 执行解压 </button>
			        </div>
		    	</div>
		    	</ww:if>
		    	<ww:if test='editable'>
		    	<div class=row style='margin-top:10px;'>
			        <div>
						<select class="form-control" id='encoding'
							style='width:96px;float:left;'>
							<option value='' selected>ISO-8859-1</option>
							<option value='US-ASCII'>US-ASCII</option>
						  	<option value='GBK'>GBK</option>
						  	<option value='UTF-8'>UTF-8</option>
						  	<option value='UTF-16BE'>UTF-16BE</option>
						  	<option value='Unicode'>Unicode</option>
						</select>
			        </div>
			        <div style='float:right;'>
			            <button onclick='textedit(this)' id='btnEdit' class="btn btn-info btn-block"><span class="fa fa-edit"></span> 编辑 </button>
			        </div>
		    	</div>
		    	</ww:if>
		    </div>
		</div>
	</td>
	<td>
        <div class="panel panel-default">
   			<div class="panel-heading" style='background-color: #f5f5f5'>
   				<span id='divPanel'><i class='skit_fa_btn fa fa-globe'></i> 预览</span>
   				<ww:if test='showBottom'>
                <div style='float:right;right:4px;top:0px;' id='btnCloseWindow'>
    			<button type="button" class="btn btn-outline btn-warning btn-xs" onclick='parent.closepreview()'>
 					<i class='fa fa-close'></i> 关闭</button>
   				</div>
                <div style='float:right;padding-right:4px;top:0px;' id='btnMinWindow'>
 				<button type="button" class="btn btn-outline btn-primary btn-xs" onclick='parent.showpreview()'>
  					<i class='fa fa-angle-down'></i> 收起窗口</button>
                </div>
                </ww:if>
                <div style='float:right;padding-right:4px;top:0px;' id='btnCancleEdit'>
 				<button type="button" class="btn btn-danger btn-xs" onclick='showfile()'>
  					<i class='fa fa- sign-out'></i> 取消编辑</button>
                </div>
                <div style='float:right;padding-right:4px;top:0px;' id='btnSaveEdit'>
 				<button type="button" class="btn btn-success btn-xs" onclick='saveEdit()'>
  					<i class='fa fa-save'></i> 保存编辑</button>
                </div>
   			</div>
   			<div class="skit_view_div panel-body" id='divContext'>
   				<iframe name='iContext' id='iContext' class='nonicescroll' style='width:100%;border:0px;'></iframe>
   			</div>
		</div>
	</td>
</tr>
</table>
<div id='divUnpreviewable' style='
	position: absolute;
	display:none;
	width:512px;
	height:465px;
	background-image: url(images/timg.jpg);
	color:#aaa;
	font-size:28px;
	font-weight:bold;
	text-align:center;
	padding-top:160px;'>该文件不可预览请下载</div>
</body>
<script type="text/javascript">
function resizeWindow()
{
	document.getElementById("divProfile").style.height = windowHeight - 8;
	document.getElementById("iContext").style.height = windowHeight - 72;
}

function setTextEncoding(encoding)
{
	$("#btnEdit").attr('disabled',false); 
	$("#encoding").val(encoding);
	$("#encoding").attr('disabled',true); 
	//document.getElementById("encoding").value = encoding;
}

function setTextEncoding(encoding)
{
	$('#encoding').val(encoding);
}

function getTextEncoding()
{
	var encoding = $('#encoding option:selected').val();
	return encoding;
}

function download()
{
	skit_confirm("您是否选择将文件压缩成ZIP文件后下载？", function(yes){
		document.getElementById("command").value = yes?1:0;
		document.forms[0].action = "files!download.action";
		document.forms[0].method = "POST";
		document.forms[0].target = "iContext";
		document.forms[0].submit();
	});
}

function uncompress()
{
	var path = "<ww:property value='path'/>";
	var filename = path.substring(path.lastIndexOf('/')+1);
	var dir = path.substring(0, path.lastIndexOf('/')+1)
	skit_input("您是否确认将该文件"+filename+"解压缩到当前目录？", dir, function(yes, val){
		if( yes ){
			var div = document.getElementById("divUnpreviewable");
			div.style.display = "none";
			document.getElementById("command").value = val;
			document.forms[0].action = "files!uncompress.action";
			document.forms[0].method = "POST";
			document.forms[0].target = "iContext";
			document.forms[0].submit();
		}
	});
}

function showdigit()
{
	var nicescroll = $('div[class^=nicescroll]');
	if( nicescroll ) nicescroll.remove();
	$( '#iContext' ).niceScroll({
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
	var div = document.getElementById("divUnpreviewable");
	div.style.display = "none";
	document.forms[0].action = "files!digit.action";
	document.forms[0].method = "POST";
	document.forms[0].target = "iContext";
	document.forms[0].submit();
}

function textedit()
{
	if( grant )
	{
	    if( snapshotable )
	    {
			$("#btnEdit").attr('disabled',true); 
			$("#encoding").attr('disabled',false);
			var btnMinWindow = document.getElementById("btnMinWindow");
			var btnCloseWindow = document.getElementById("btnCloseWindow");
			if( btnMinWindow) btnMinWindow.style.display = "none";
			if( btnCloseWindow ) btnCloseWindow.style.display = "none";
			
			var btnSaveEdit = document.getElementById("btnSaveEdit");
			var btnCancleEdit = document.getElementById("btnCancleEdit");
			btnSaveEdit.style.display = "";
			btnCancleEdit.style.display = "";

			document.getElementById("divPanel").innerHTML = "<i class='skit_fa_btn fa fa-edit'></i> 编辑";
			
			var nicescroll = $('div[class^=nicescroll]');
			if( nicescroll ) nicescroll.remove();
			document.getElementById("_encoding").value = getTextEncoding();
			document.forms[0].action = "files!textedit.action";
			document.forms[0].method = "POST";
			document.forms[0].target = "iContext";
			document.forms[0].submit();
	    }
	    else{
			skit_alert("该文件不可编辑。");
	    }
	}
	else
	{
		skit_alert("您的权限管理员未给您开放编辑文本文件的权限。");
	}
}

function showfile()
{
	if( editable )
	{
		$("#btnEdit").attr('disabled',false); 
		$("#encoding").attr('disabled',true);
	}

	var btnMinWindow = document.getElementById("btnMinWindow");
	var btnCloseWindow = document.getElementById("btnCloseWindow");
	if( btnMinWindow ) btnMinWindow.style.display = "";
	if( btnCloseWindow ) btnCloseWindow.style.display = "";

	var btnSaveEdit = document.getElementById("btnSaveEdit");
	var btnCancleEdit = document.getElementById("btnCancleEdit");
	
	document.getElementById("divPanel").innerHTML = "<i class='skit_fa_btn fa fa-globe'></i> 预览";
	
	btnSaveEdit.style.display = "none";
	btnCancleEdit.style.display = "none";
	var nicescroll = $('div[class^=nicescroll]');
	if( nicescroll ) nicescroll.remove();
	if( snapshotable )
	{
		if( scrollModelAutoFit )
		{
			$( '#iContext' ).niceScroll({
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
		}
		document.forms[0].action = "files!show.action";
		document.forms[0].method = "POST";
		document.forms[0].target = "iContext";
		document.forms[0].submit();
	}
	else
	{
		var div = document.getElementById("divUnpreviewable");
		div.style.display = "";
		div.style.top = windowHeight/2 - div.clientHeight/2 + 30;
		div.style.left = windowWidth/2 - div.clientWidth/2 + 125;
		/*document.forms[0].action = "http://defthemes.com/assets/images/quant-avatar.png";
		document.forms[0].method = "POST";
		document.forms[0].target = "iContext";
		document.forms[0].submit();*/
	}
}
function saveEdit()
{
	var encoding = getTextEncoding();
	skit_confirm("您确定要 保存该文件？文本编码格式是"+encoding, function(yes){
		if( yes ){
		    var ifr = window.frames["iContext"];
		    if( ifr && ifr.save )
		    {
				ifr.save(encoding);

				var btnMinWindow = document.getElementById("btnMinWindow");
				var btnCloseWindow = document.getElementById("btnCloseWindow");
				if( btnMinWindow) btnMinWindow.style.display = "";
				if( btnCloseWindow ) btnCloseWindow.style.display = "";

				var btnSaveEdit = document.getElementById("btnSaveEdit");
				var btnCancleEdit = document.getElementById("btnCancleEdit");
				btnSaveEdit.style.display = "none";
				btnCancleEdit.style.display = "none";
				
				$("#btnEdit").attr('disabled',false); 
				$("#encoding").attr('disabled',true);

				document.getElementById("divPanel").innerHTML = "<i class='skit_fa_btn fa fa-globe'></i> 预览";
		    }
		}
	});
}
</script>
<%@ include file="../../skit/inc/skit_bootstrap.inc"%>
<style type='text/css'>
.well p {
    font-family: 微软雅黑,Lato,sans-serif;
    font-weight: 300;
    font-size: 12px;
}
.panel .panel-heading {
    font-size: 12px;
    font-family: "微软雅黑",sans-serif;
    padding: 5px 10px;
	border-bottom: 1px solid transparent;
	border-top-left-radius: 3px;
	border-top-right-radius: 3px;
}
</style>
<%@ include file="../../skit/inc/skit_cos.inc"%>
<script type="text/javascript">
var grant = <ww:property value='grant'/>;
var editable = <ww:property value='editable'/>;
var snapshotable = <ww:property value='snapshotable'/>;
var scrollModelAutoFit = <ww:property value='scrollModelAutoFit'/>;
document.getElementById("ww").value = windowWidth - 280;
showfile();
</script>
</html>