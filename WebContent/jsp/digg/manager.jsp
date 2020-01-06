<%@ page language="java" pageEncoding="UTF-8"%>
<%@ page import="com.focus.cos.web.common.Kit"%>
<%@ taglib uri="/webwork" prefix="ww" %>
<%Kit.noCache(request,response);  %>
<html>
<head>
<link type="text/css" href="skit/ztree/css/zTreeStyle/zTreeStyle.css" rel="stylesheet"/>
<link type="text/css" href="skit/css/bootstrap-tour.min.css" rel="stylesheet">
<link type="text/css" href="skit/css/costable.css?v=3" rel="stylesheet">
<style type='text/css'>
</style>
<%=Kit.getDwrJsTag(request,"interface/DiggConfigMgr.js")%>
<%=Kit.getDwrJsTag(request,"engine.js")%>
<%=Kit.getDwrJsTag(request,"util.js")%>
<SCRIPT type="text/javascript">
</SCRIPT>
</head>
<body style='overflow-y:hidden;padding-top:0px;padding-left:0px;'>
<form id='form0'>
<input type='hidden' name='editorContent' id='content'>
<input type='hidden' name='gridxml' id='gridxml'>
<input type='hidden' name='gridtext' id='gridtext'>
<input type='hidden' name='version' id='version'>
<input type='hidden' name='oldversion' id='oldversion'>
<input type='hidden' name='remark' id='remark'>
<input type='hidden' name='account' id='account' value="<ww:property value='account'/>">
<input type='hidden' name='id' id='id'>
<input type='hidden' name='sysid' id='sysid' value="<ww:property value='sysid'/>">
<input type='hidden' name='db' id='db' value="<ww:property value='db'/>">
<input type='hidden' name='datatype' id='datatype' value="<ww:property value='datatype'/>">
<input type='hidden' name='filetype' id='filetype' value="<ww:property value='filetype'/>">
<iframe name='iDownload' id='iDownload' style='display:none'></iframe>
</form>
<div class="alert alert-warning" id='divTour' style='display:none'>
    <a href="#" class="close" data-dismiss="alert">&times;</a>
    <span id='spanAlert'><strong>提示！</strong>点击右侧下拉菜单，选择您要查看的【元数据模板】产生的数据、单元格对象定义，以及全局脚本、预处理脚本、渲染器脚本。</span>
</div>
<div class="panel panel-default" style='border: 1px solid #efefef' id='divDiggConfig'>
	<div class="panel-heading"><span id='spanTitle'><i class='skit_fa_btn fa fa-digg'></i>元数据管理(编辑、测试、预览以及发布元数据查询、配置模板，通过模板快速实现对数据的有效管理)</span>
    	<div style='float:right;right:4px;top:0px;display:none' id='divAddQuery'>
 			<button type="button" class='btn btn-outline btn-info btn-xs' onclick='presetQueryTemplate()'>
 				<i class='fa fa-plus-circle'></i> 新增查询模板</button>
        </div>
    	<div style='float:right;padding-right:4px;top:0px;display:none' id='divAddConfig'>
 			<button type="button" class='btn btn-outline btn-success btn-xs' onclick='presetConfigTemplate()'>
 				<i class='fa fa-plus-circle'></i> 新增配置模板</button>
        </div>
    	<div style='float:right;right:4px;top:0px;display:none' id='divSave'>
 			<button type="button" class='btn btn-outline btn-success btn-xs' onclick='presaveTemplate()'>
 				<i class='fa fa-save'></i> 保存</button>
        </div>
        <div style='float:right;padding-right:4px;top:0px;display:none' id='divCancel'>
 			<button type="button" class="btn btn-outline btn-danger btn-xs" onclick='cancelSaveTemplate()'>
 				<i class='fa fa-close'></i> 取消</button>
        </div>
        <div style='float:right;padding-right:4px;top:0px;display:none' id='divUpload'>
 			<button type="button" class="btn btn-outline btn-primary btn-xs" onclick='preuploadTemplate()'>
 				<i class='fa fa-upload'></i> 导入模板</button>
        </div>
        <div style='float:right;padding-right:4px;top:0px;display:none' id='divDebug'>
 			<button type="button" class="btn btn-outline btn-warning btn-xs" onclick='testTemplate()'>
 				<i class='fa fa-bug'></i> 检测</button>
        </div>
        <div style='float:right;padding-right:4px;top:0px;display:none' id='divPreview'>
 			<button type="button" class="btn btn-outline btn-info btn-xs" onclick='previewTemplateEdit()'>
 				<i class='fa fa-bug fa-spin'></i> 调试</button>
        </div>
        <div style='float:right;padding-right:4px;top:0px;display:none' id='divFullscreen'>
 			<button type="button" class="btn btn-outline btn-default btn-xs" onclick='doFullscreen(this)' id='btnFullscreen'>
 				<i class='fa fa-arrows-alt'></i> 全屏</button>
        </div>
    </div>
	<div class="panel-body" style='padding: 0px;'>
	<TABLE>
	<TR class='unline'><TD width='250' valign='top' id='tdTree'>
			<div id='divTree' style='border: 0px solid red'>
				<ul id='myZtree' class='ztree'></ul>
				<div id="rMenu">
					<ul>
						<li onclick="querySql();" id='liQuerySql'><i class='skit_fa_icon_blue fa fa-database'></i> 数据库表查询</li>
						<li onclick="presetQueryTemplate();" id='liAddQueryTemplate'><i class='skit_fa_icon_blue fa fa-plus-circle'></i> 新增查询模板</li>
						<li onclick="presetConfigTemplate();" id='liAddConfigTemplate'><i class='skit_fa_icon fa fa-plus-circle'></i> 新增配置模板</li>
						<li onclick="copyTemplate();" id='liCopyDiggConfig'><i class='skit_fa_icon fa fa-copy'></i> 复制</li>
						<li onclick="delTemplate();" id='liDeleteTemplate'><i class='skit_fa_icon_red fa fa-minus-circle'></i> 删除</li>
						<li onclick="preuploadTemplate();" id='liUploadTemplate'><i class='skit_fa_icon_blue fa fa-upload'></i> 上传模板</li>
						<li onclick="downloadTemplate();" id='liDownloadTemplate'><i class='skit_fa_icon_blue fa fa-download'></i> 下载模板</li>
						<li onclick="preuploadTemplates();" id='liUploadTemplates'><i class='skit_fa_icon fa fa-cloud-upload'></i> 导入模板</li>
						<li onclick="downloadTemplates();" id='liDownloadTemplates'><i class='skit_fa_icon fa fa-cloud-download'></i> 导出模板</li>
						<li class="divider" id='liDivider0'></li>
						<li onclick="addTemplateDir();" id='liAddTemplateDir'><i class='skit_fa_icon_blue fa fa-plus-circle'></i> 新增目录</li>
						<li onclick="publishMenus();" id='liPublishMenus'><i class='skit_fa_icon_blue fa fa-copy'></i> 发布目录菜单组</li>
					</ul>
				</div>
			</div>
		</TD>
		<TD valign='top' valign='top'>
			<div class="well profile" style='margin-top:6px;margin-left:6px;margin-bottom:5px;display:none' id='divDiggProfile'>
			    <div class="col-sm-12">
			        <div class="col-xs-12 col-sm-10">
			        	<h3 id='h2DiggTitle' style='margin-top:0px;'>版权管理</h3>
			            <p><i class="fa fa-info-circle fa-fw text-muted"></i>
			               <span id='spanDiggName'>该查询是用于什么目的，用到了那些数据源。</span></p>
			            <p><strong><i class="fa fa-paw fa-fw text-muted"></i> 版本号:  </strong>
				               <span class="tags" id='spanDiggVersion'
			               	 onclick='viewVersions(true)' title='点击显示版本详情'>1.0.0.0</span>
			               <a onclick="viewVersions()"><i class='fa fa-eye'></i> 查看</a> 
			            </p>
			            <p><strong><i class="fa fa-user fa-fw text-muted"></i> 创建者:  </strong>
			               <span class="tags" id="spanDigger" onclick='mailto(this)'>李佳齐</span>
			               <strong><i class="fa fa-user fa-fw text-muted"></i> 最后修改时间:  </strong>
			               <span class="tags" id="spanTimestamp">2017-06-24 11:14</span>
			            </p>
			            <p><strong><i class="fa fa-link fa-fw text-muted"></i> 配置地址:  </strong>
				               <span id='spanDiggId'></span>
			            </p>
			        </div>
			        <div class="col-xs-12 col-sm-2 text-center">
			            <figure>
			                <i class="fa fa-question" style='font-size:60px;' id='iDiggState'></i>
			                <figcaption class="ratings" style='margin-top:0px;'>
			                	<p id='pDiggState'>N/A</p>
			                </figcaption>
			            </figure>
			        </div>
			    </div>
				<div class="col-xs-12 stext-center">
                   <div class="col-xs-12 col-sm-3">
                       <div class="btn-group dropup btn-block">
                         <button type="button" class="btn btn-outline btn-success" id='btnEdit' style='width:90%;height:34px;'
						onclick='doEdit()'>&nbsp;&nbsp;&nbsp;<span class="fa fa-edit"></span> 编辑 </button>
                         <button type="button" class="btn btn-success dropdown-toggle" style='height:34px;'
                         	title='编辑元数据查询' id='btnEdit1'
                         	data-toggle="dropdown" aria-expanded="false">
                           <span class="fa fa-caret-down"></span>
                         </button>
                         <ul class="dropdown-menu pull-right animated fadeInUp" role="menu" id='ulCompiler'>
                         	<li><a title='' onclick='doEdit(false)'><i class='skit_fa_icon fa fa-code fa-fw'></i>
                         		元数据模板XML脚本编辑</a></li>
                         	<li><a title='' onclick='doEdit(true)'><i class='skit_fa_icon fa fa-desktop fa-fw'></i>
                         		元数据模板可视化编辑</a></li>
                       		<li class="divider"></li>
                         	<li><a title='' onclick='downloadTemplate()'><i class='skit_fa_icon fa fa-download fa-fw'></i>
                         		下载该元数据模板 </a></li>
                         	<li><a title='' onclick='preuploadTemplate()'><i class='skit_fa_icon fa fa-upload fa-fw'></i>
                         		上传该元数据模板 </a></li>
                       		<li class="divider"></li>
                         	<li><a title='' onclick='copyTemplate()'><i class='skit_fa_icon fa fa-copy fa-fw'></i>
                         		复制元数据配置</a></li>
                         	<li><a title='' onclick='delTemplate()'><i class='skit_fa_icon_red fa fa-minus-circle fa-fw'></i>
                         		删除元数据配置</a></li>
                         </ul>
                       </div>
                   </div>
                   <div class="col-xs-12 col-sm-3">
                       <div class="btn-group dropup btn-block">
                         <button type="button" class="btn btn-outline btn-warning" id='btnTest' style='width:90%;height:34px;'
						onclick='doTest()'>&nbsp;&nbsp;&nbsp;<span class="fa fa-bug"></span> 测试 </button>
                         <button type="button" class="btn btn-warning dropdown-toggle" style='height:34px;'
                         	title='' id='btnTest1'
                         	data-toggle="dropdown" aria-expanded="false">
                           <span class="fa fa-caret-down"></span>
                         </button>
                         <ul class="dropdown-menu pull-right animated fadeInUp" role="menu" id='ulPreview'>
                         	<li><a onclick='doTest(0)'><i class='skit_fa_icon fa fa-print fa-fw'></i>
                         		模板测试</a></li>
                         	<li><a onclick='doTest(1)'><i class='skit_fa_icon fa fa-code-fork fa-fw'></i>
                         		数据测试</a></li>
                         </ul>
                       </div>
                   </div>
                   <div class="col-xs-12 col-sm-3">
                       <div class="btn-group dropup btn-block">
                         <button type="button" class="btn btn-outline btn-info" id='btnPreview' style='width:90%;height:34px;'
						onclick='previewTemplateData()'>&nbsp;&nbsp;&nbsp;<span class="fa fa-eye"></span> 预览</button>
                         <button type="button" class="btn btn-info dropdown-toggle" style='height:34px;'
                         	title='元数据配置模板真实数据测试预览' id='btnPreview1'
                         	data-toggle="dropdown" aria-expanded="false">
                           <span class="fa fa-caret-down"></span>
                         </button>
                         <ul class="dropdown-menu pull-right animated fadeInUp" role="menu" id='ulPreview'>
                         	<li><a onclick='previewTemplateStyle(true)'><i class='skit_fa_icon fa fa-th fa-fw'></i>
                         		预览元数据模板样式框架与demo数据</a></li>
                         	<li><a onclick='previewTemplateConfig()'><i class='skit_fa_icon fa fa-laptop fa-fw'></i>
                         		预览元数据模板可视界面</a></li>
                         	<li><a onclick='previewTemplateData()'><i class='skit_fa_icon fa fa-print fa-fw'></i>
                         		预览元数据模板真实数据</a></li>
                         	<li><a onclick='previewTemplateScript()'><i class='skit_fa_icon fa fa-code fa-fw'></i>
                         		查看元数据模板XML脚本</a></li>
                         </ul>
                       </div>
                   </div>
                   <div class="col-xs-12 col-sm-3">
                       <div class="btn-group dropup btn-block">
                         <button type="button" class="btn btn-primary" id='btnPublish' style='width:90%;height:34px;'
						onclick='doPublish()'>&nbsp;&nbsp;&nbsp;<span class="fa fa-cloud-upload"></span> 发布 </button>
                         <button type="button" class="btn btn-primary dropdown-toggle" style='height:34px'
                         	title='发布管理' id='btnPublish1'
                         	data-toggle="dropdown" aria-expanded="false">
                           <span class="fa fa-caret-down"></span>
                         </button>
                         <ul class="dropdown-menu pull-right animated fadeInUp" role="menu" id='ulPublish'>
                         	<li><a title='' onclick='copyUrl()'><i class='skit_fa_icon fa fa-clipboard fa-fw'></i>
                         		复制元数据查询配置模板链接地址</a></li>
                         </ul>
                		</div>
                	</div>
	           	</div>
			</div>
	 		<iframe name='iDigg' id='iDigg' style='border:0px solid #eee;margin-left:3px;margin-top:3px;margin-bottom:3px;'></iframe>
	 		<iframe name='diggXmlEditor' id='diggXmlEditor' scr='editor!xml.action' class='nonicescroll' style='border:0px solid #eee;margin-left:3px;margin-top:3px;margin-bottom:3px;'></iframe>
		</TD>
	</TR>
	</TABLE>
	</div>
</div>
<div class="panel panel-primary" id='divUploadfile' style='top: 100; left: 100; width:600px; display:none; position: absolute; '>
    <div class="panel-heading">
    	<span id='uploadtitle'>从本地磁盘选择上传元数据查询配置模板文件</span>
        <div class="panel-menu">
            <button type="button" onclick='closeuploadfile()' data-action="close" class="btn btn-warning btn-action btn-xs"><i class="fa fa-times"></i></button>
        </div>
    </div>
    <div class="panel-body" style="display: block;">
		<input type="file" name="uploadfile" id="uploadfile" class="file-loading"/>
    </div>
</div>
<div id='divSaveTemplate' style='display:none'>
	<div class="panel panel-default">
		<div class="panel-body" style='padding-bottom:0px;'>
			<div class="form-group">
				<div class="input-group">
					<span class="input-group-addon input-group-addon-font">模板版本<span class='fa fa-paw' style='margin-left:6px'></span></span>
					<input class="form-control form-control-font" type="text" id='#version#'
		             	data-title='请按照0.0.0.0格式输入版本号，版本号是记录你模板开发的记录'
						data-toggle="tooltip" 
						data-placement="bottom"
						data-trigger='manual'
		             	placeholder="1.0.0.0"
					>
					<span class="input-group-addon input-group-addon-font" 
						style='width:160px; color:#afafaf; text-align: left; border-left: 1px solid #eee;border-right: 1px solid #eee;'
						> #lastversion#</span>
				</div>
			</div>
			<div class="form-group">
				<div class="input-group">
					<span class="input-group-addon input-group-addon-font">模板标题<span class='fa fa-paw' style='margin-left:6px'></span></span>
					<input class="form-control form-control-font" type="text" id='#templateTitle#'
		             	data-title='请输入同级目录下模板的唯一标题。'
						data-toggle="tooltip" 
						data-placement="bottom"
						data-trigger='manual'
		             	placeholder=""
					>
				</div>
			</div>
			<div class="form-group">
				<textarea class="form-control form-control-font" rows="3"
					id="#versionremark#" style='color:#66cccc;background-color:#eee;'
				    data-title='元数据模板版本特性描述不少于5个字'
					data-toggle="tooltip" 
					data-placement="bottom"
					data-trigger='manual'
		           	placeholder="请输入当前版本特性说明"></textarea>
			</div>
		</div>
	</div>
</div>
<div id='divCopyTemplate' style='display:none'>
	<div class="panel panel-default">
		<div class="panel-body" style='padding-bottom:0px;'>
			<div class="form-group">
				<div class="input-group">
					<span class="input-group-addon input-group-addon-font">模板标识<span class='fa fa-paw' style='margin-left:6px'></span></span>
					<input class="form-control form-control-font" type="text" id='#templateId#'
		             	data-title='请输入同级目录下模板唯一标识。只能含有字母、数字、下划线;程序标识不少于2个字，不多于64个字'
						data-toggle="tooltip" 
						data-placement="bottom"
						data-trigger='manual'
		             	placeholder=""
					>
				</div>
			</div>
			<div class="form-group">
				<div class="input-group">
					<span class="input-group-addon input-group-addon-font">模板标题<span class='fa fa-paw' style='margin-left:6px'></span></span>
					<input class="form-control form-control-font" type="text" id='#templateTitle#'
		             	data-title='请输入同级目录下模板的唯一标题。'
						data-toggle="tooltip" 
						data-placement="bottom"
						data-trigger='manual'
		             	placeholder=""
					>
				</div>
			</div>
		</div>
	</div>
</div>
<div style='position:absolute;top:1px;left:1px;cursor:pointer;display:none' id='divDebugObjectScript'>
    <div class="btn-group btn-block">
		<button type="button" class="btn btn-outline btn-default btn-xs" style='width:80%;height:26px;font-size:12px;'
			>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="fa fa-eye"></span>查看对象或编辑脚本&nbsp;&nbsp;&nbsp;</button>
	    <button type="button" class="btn btn-outline btn-default btn-xs dropdown-toggle" style='height:26px'
	    	data-toggle="dropdown" aria-expanded="false">
	      <span class="fa fa-caret-down"></span>
	    </button>
	    <ul class="dropdown-menu multi-level" role="menu" id='ulTest'>
	    	<li class="dropdown-submenu" id='liMenuDebugSubgrid' style='display:none'>
	    		<a title='' href='javascript:;'><i class='skit_fa_icon fa fa-share-alt fa-fw'></i>查看与编辑关联表对象与脚本</a>
    			<ul id='uiMenuDebugSubgrid' class="dropdown-menu">
    			</ul>
	    	</li>
			<li class="divider" id='liDividerMenuDebugInnerGrid' style='display:none'></li>
	    	<li><a title='' onclick='editJavascript(0,this)'><i class='skit_fa_icon fa fa-edit fa-fw'></i>
	    		编辑全局用户自定义脚本</a>
	    	<li><a title='' onclick='editJavascript(1,this)'><i class='skit_fa_icon fa fa-edit fa-fw'></i>
	    		编辑主表数据预处理脚本</a></li>
	    	<li id='liMenuDebugRender' class="dropdown-submenu">
	    		<a title='' href='javascript:;'><i class='skit_fa_icon fa fa-edit fa-fw'></i>编辑主表单元格渲染器脚本</a>
    			<ul class="dropdown-menu" id='ulMenuDebugRender'>
    			</ul>
	    	</li>
	    	<li class="divider"></li>
	    	<li><a title='' onclick='viewObject(0,this)'><i class='skit_fa_icon fa fa-info fa-fw'></i>
	    		查看主表配置对象</a></li>
	    	<li><a title='' onclick='viewObject(1,this)'><i class='skit_fa_icon fa fa-code fa-fw'></i>
	    		查看主表数据对象</a></li>
	    	<li><a title='' onclick='viewObject(2,this)'><i class='skit_fa_icon fa fa-th fa-fw'></i>
	    		查看主表单元格配置对象</a></li>
	    	<li><a title='' onclick='viewObject(3,this)'><i class='skit_fa_icon fa fa-database fa-fw'></i>
	    		查看主表数据模型对象</a></li>
	    	<li><a title='主表查询通过该对象数据进行过滤选项预置' onclick='viewObject(4,this)'><i class='skit_fa_icon fa fa-list fa-fw'></i>
	    		查看条件过滤选项映射表</a></li>
	    	<li><a title='主表单元格显示数据某些字段通过该配置转义' onclick='viewObject(5,this)'><i class='skit_fa_icon fa fa-retweet fa-fw'></i>
	    		查看主表数据字段转义映射表</a></li>
	    	<li><a title='让单元格按照配置进行颜色转换' onclick='viewObject(6,this)'><i class='skit_fa_icon fa fa-paint-brush fa-fw'></i>
	    		查看主表单元格样式配置</a></li>
	    </ul>
    </div>
</div>
<div id='divMask' class='skit_mask' onclick='closeprecopy()' style='cursor:pointer;' title='点击关闭弹窗'></div>
</body>
<script type="text/javascript">
var sysid = "<ww:property value='sysid'/>";
var fullscreen;
function resizeWindow()
{
	var	_windowWidth = windowWidth;
	var userAgent = navigator.userAgent; //取得浏览器的userAgent字符串
	var isChrome = userAgent.indexOf("Chrome") > -1;
	var div = null;
	if( document.getElementById( 'tdTree' ).style.display == "" )
	{
		div = document.getElementById( 'divTree' );
		div.style.width = 248;
		div.style.height = windowHeight - 38;
	}

	var h = 0, w = 0;
	if( isChrome )
	{
		document.body.style.marginTop = 3;
		h -= 5;
	}
	div = document.getElementById( 'divDiggProfile' );
	if( div.style.display == "" )
	{
		h += 280;
		div.style.width = _windowWidth - 280; 
		var w = div.clientWidth/4 - 80;
		document.getElementById( 'btnEdit' ).style.width = w;
		document.getElementById( 'btnPreview' ).style.width = w;
		document.getElementById( 'btnPublish' ).style.width = w;
		document.getElementById( 'btnTest' ).style.width = w;
	}
	else
	{
		h = 42;
	}
	var iDigg = document.getElementById( 'iDigg' );
	var diggXmlEditor = document.getElementById( 'diggXmlEditor' );
	h = windowHeight - h;
	if( iDigg.style.display == "" && diggXmlEditor.style.display == "" )
	{
		h /= 2;
	}
	w = fullscreen?_windowWidth:(_windowWidth - 266);
	iDigg.style.width = w;
	diggXmlEditor.style.width = w - 10;
	iDigg.style.height = h;
	diggXmlEditor.style.height = h;
}
</script>
<link href="skin/defone/css/simplemodal.css" rel="stylesheet">
<link href="skit/css/bootstrap.switch.css" rel="stylesheet">
<link href="skit/css/fileinput.min.css" rel="stylesheet"/>
<%@ include file="../../skit/inc/skit_bootstrap.inc"%>
<%@ include file="../../skit/inc/skit_cos.inc"%>
<%@ include file="../../skit/inc/skit_ztree.inc"%>
<script src="skin/defone/js/mootools-core-1.3.1.js"></script>
<script src="skin/defone/js/simple-modal.js?v=3"></script>
<script src="skit/js/fileinput.js"></script>
<script src="skit/js/fileinput_locale_zh.js"></script>
<script src="skit/js/jquery.md5.js"></script>
<script src="skit/js/bootstrap.switch.js"></script>
<script src="skit/js/jquery.inputmask.bundle.min.js"></script>
<script src="skit/js/auto-line-number.js"></script>
<script src="skit/js/bootstrap-tour.min.js"></script>
<style type='text/css'>
body {
}
.btn.btn-outline.btn-primary:hover {
    background-color: #fff;
    border: 1px solid #455a64;
    color: #455a64;
}
.btn.btn-primary:hover {
    background-color: #5a7582;
}
.btn.btn-primary {
    background-color: #455a64;
    border-color: #455a64;
}
.btn-primary:hover {
    color: #fff;
    background-color: #286090;
    border-color: #204d74;
}
.panel.panel-primary .panel-heading {
    background-color: #455a64;
    border-bottom: 2px solid #1b2428;
    color: #fff;
}
.panel .panel-heading {
    font-size: 12px;
    font-family: "微软雅黑",sans-serif;
    padding: 5px 10px;
	border-bottom: 1px solid transparent;
	border-top-left-radius: 3px;
	border-top-right-radius: 3px;
}
.panel-title {
    font-size: 12px;
    font-family: "微软雅黑",sans-serif;
    display:block;
    width:310px;
    word-break:keep-all;
    white-space:nowrap;
    overflow:hidden;
    text-overflow:ellipsis;
}
.panel .panel-menu {
    float: right;
    right: 30px;
    top: 8px;
    font-weight: 100;
}
.profile span.tags {
    background: <%=(String)session.getAttribute("System-Theme")%>;
    border-radius: 2px;
    color: #fff;
    font-weight: 700;
    padding: 2px 4px;
}
.xml_edit
{
	width: 100%;
	font-size: 12px;
	font-family:微软雅黑,Roboto,sans-serif;
	margin:0 0 1px;
	color: #ffffff;
	background-color: #373737;
}
</style>
<SCRIPT type="text/javascript">
$( '#divTree' ).niceScroll({
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
var grant = <ww:property value='grant'/>;//管理程序的权限

//########################################################################
	var setting = {
		//editable: false,
		//fontCss : {color:"red"},
		check: {
			enable: false
		},
		callback: {
			onRightClick: onRightClick,
			onClick: onClick,
			onDblClick: onDblClick,
			beforeRename: beforeRename,
			onRename: onRename,
			beforeDrag: beforeDrag,
			beforeDrop: beforeDrop,
			beforeDragOpen: beforeDragOpen,
			onDrag: onDrag,
			onDrop: onDrop,
		},
		view: {
			addDiyDom: addDiyDom
		},
		edit: {
			drag: {
				autoExpandTrigger: true,
				prev: dropPrev,
				inner: dropInner,
				next: dropNext
			},
			enable: true,
			showRemoveBtn: false,
			showRenameBtn: showRenameBtn
		},
		data: {
			key: {
				title: "id"
			}
		}
	};
	
	function onRightClick(event, treeId, treeNode) 
	{
		document.getElementById( 'liQuerySql' ).style.display = "none";
		document.getElementById( 'liAddConfigTemplate' ).style.display = "none";
		document.getElementById( 'liAddQueryTemplate' ).style.display = "none";
		document.getElementById( 'liCopyDiggConfig' ).style.display = "none";
		document.getElementById( 'liDeleteTemplate' ).style.display = "none";
		document.getElementById( 'liUploadTemplate' ).style.display = "none";
		document.getElementById( 'liDownloadTemplate' ).style.display = "none";
		document.getElementById( 'liUploadTemplates' ).style.display = "none";
		document.getElementById( 'liDownloadTemplates' ).style.display = "none";
		document.getElementById( 'liDivider0' ).style.display = "none";
		document.getElementById( 'liAddTemplateDir' ).style.display = "none";
		document.getElementById( 'liPublishMenus' ).style.display = "none";
		if( treeNode.newTemplate )
		{
			return;
		}
		if( treeNode.rootdir ){
			document.getElementById( 'liUploadTemplates' ).style.display = "";
			document.getElementById( 'liDownloadTemplates' ).style.display = "";
		}
		if( treeNode.type == "dir" )
		{
			if( treeNode.demo ) return;
			document.getElementById( 'liAddQueryTemplate' ).style.display = "";
			document.getElementById( 'liAddConfigTemplate' ).style.display = "";
			document.getElementById( 'liUploadTemplate' ).style.display = "";
			document.getElementById( 'liDivider0' ).style.display = "";
			document.getElementById( 'liAddTemplateDir' ).style.display = "";
			document.getElementById( 'liDeleteTemplate' ).style.display = treeNode.rootdir?"none":"";
		}
		else
		{
			if( !treeNode.type || treeNode.type == "datasource" ) return;
			if( treeNode.type == "table"  )
			{
				document.getElementById( 'liQuerySql' ).style.display = "";
			}
			else
			{
				if( treeNode.demo )
				{
					document.getElementById( 'liCopyDiggConfig' ).style.display = "";
					document.getElementById( 'liDownloadTemplate' ).style.display = "";
				}
				else 
				{
					//document.getElementById( 'liAddQueryTemplate' ).style.display = "";
					document.getElementById( 'liDeleteTemplate' ).style.display = "";
					document.getElementById( 'liCopyDiggConfig' ).style.display = "";
					document.getElementById( 'liDownloadTemplate' ).style.display = "";
				}
			}
		}
	
		if (!treeNode && event.target.tagName.toLowerCase() != "button" && $(event.target).parents("a").length == 0) {
			myZtree.cancelSelectedNode();
			showRMenu("root", event.clientX, event.clientY);
			//alert("0:"+document.body.clientTop);
		} else if (treeNode && !treeNode.noR) {
			myZtree.selectNode(treeNode);
			showRMenu("node", event.clientX, event.clientY);
		}
	}

	function onClick(event, treeId, treeNode)
	{
		//myZtree.expandNode(treeNode, null, null, null, true);
		//myZtree.expandNode(treeNode, true);
		open(treeNode);
		setUserActionMemory("role!set.tree", treeNode.id);
	}
	
	function onDblClick(event, treeId, treeNode){
		if( treeNode.type == "edit" && !treeNode.demo ){
			doEdit();
		}
	}

	function showRenameBtn(treeId, treeNode) {
		return !treeNode.demo&&treeNode.type=="dir"&&treeNode.id!="";
	}
	
	var _oldName;
	function beforeRename(treeId, treeNode, newName, isCancel) {
		if (newName.length == 0) {
			myZtree.cancelEditName();
			skit_alert("目录名称不能为空.");
			return false;
		}
		else if (newName.length > 10) {
			myZtree.cancelEditName();
			skit_alert("目录名称不能超过10个字.");
			return false;
		}
		else if (newName.indexOf('/') != -1 ||
				newName.indexOf(':') != -1 ||
				newName.indexOf('\\') != -1 ||
				newName.indexOf('<') != -1 ||
				newName.indexOf('>') != -1) {
			myZtree.cancelEditName();
			skit_alert("目录名称不能出现特殊字符.");
			return false;
		}
		var parent = treeNode.getParentNode();
		var children = parent.children;
		if( children && children.length > 1 )
			for(var i = 0; i < children.length; i++)
			{
				var child = children[i];
				if( child == treeNode ) continue;
				if( child.name == newName )
				{
					skit_alert("同级目录名称不能相同.");
					if( treeNode.id )
					{
						myZtree.cancelEditName();
					}
					else
					{
						myZtree.removeNode(treeNode);
						myZtree.selectNode(parent, false, false);
						open(parent);
					}
					return false;
				}
			}
		
		_oldName = treeNode.name;
		return true;
	}
	
	function onRename(e, treeId, treeNode, isCancel) 
	{
		if( isCancel )
		{
			return;
		}
		if (_oldName == treeNode.name && treeNode.id != "" ) {
			skit_alert("目录名称没有变化.");
			return;
		}
		var tips = treeNode.id?("您确定要将目录名称更改为"+treeNode.name+"吗？"):("您确定要将目录名称设置为"+treeNode.name+"吗？");
		skit_confirm(tips, function(yes){
			if( yes )
			{
				var parent = treeNode.getParentNode();
				var isAdd = treeNode.id?false:true;
				var zkpath = isAdd?parent.id:treeNode.id;
				DiggConfigMgr.setTemplateDir(sysid, zkpath, treeNode.name, isAdd, {
					callback:function(rsp) {
						if( rsp.succeed ){
							if( isAdd )	treeNode.id = rsp.result;
							open(treeNode);
						}
						else{
							skit_error(rsp.message);
							var parent = treeNode.getParentNode();
							myZtree.removeNode(treeNode);
							myZtree.selectNode(parent, false, false);
							open(parent);
						}
					},
					timeout:30000,
					errorHandler:function(err) {skit_hiddenLoading(); alert(err); }
				});
			}
			else
			{
				treeNode.name = _oldName;
				myZtree.updateNode (treeNode);
			}
		});
	}

	function addDiyDom(treeId, treeNode)
	{
		var treeItem;
		if( treeNode.type == 'dir')
		{
			treeNode.check = new Object();
			treeNode.check.err = 0;
			treeNode.check.war = 0;
			treeItem = $("#" + treeNode.tId + "_a");
			treeItem.before("<i id='ico_abc_"+treeNode.id+"' class='skit_fa_icon_gray fa fa-check-square-o' title='' style='font-size:14px;'></i>");
			return;
		}
		if( !treeNode.template ) return;
		treeItem = $("#" + treeNode.tId + "_a");
		var ico = "skit_fa_icon_gray fa fa-question-circle";
		var title = "该模板";
		if( treeNode.check )
		{
		}
		else
		{
			title = "还没执行自检.";
		}
		treeItem.before("<i id='ico_abc_"+treeNode.id+"' class='"+ico+"' title='"+title+"' style='font-size:14px;'></i>");
	}
	
	function setCheckResult(json)
	{
		var result = jQuery.parseJSON(json);
		var i, treeNode, check;
		for(i=0; i < result.length; i++)
		{
			check = result[i];
			treeNode = myZtree.getNodeByParam("id", check.path);
			if( treeNode )
			{
				treeNode["check"] = check;
				setParentCheckResult(treeNode, check.err, check.war);
				setTemplateCheck(treeNode);
			}
		}
		//open(getNode());
	}

	function setParentCheckResult(treeNode, err, war)
	{
		var parent = treeNode.getParentNode();
		if( parent )
		{
			if( !parent.check )
			{
				parent.check = new Object();
			}
			parent.check.err += err;
			parent.check.war += war;
			setTemplateCheck(parent);
			setParentCheckResult(parent, err, war);
		}
	}
	
	function setTemplateCheck(treeNode)
	{
		var title, ico, check;
		title = "";
		check = treeNode["check"];
		if( !check ) return;
		if( check.war > 0 )
		{
			title += check.war+"个改进提示.<br/>";
			ico = "skit_fa_icon_yellow fa fa-exclamation-circle";
		}
		if( check.err > 0 )
		{
			title += check.err+"个严重错误.";
			ico = "skit_fa_icon_red fa fa-minus-circle";
		}
		if( check.war == 0 && check.err == 0 )
		{
			title = "模板OK";
			ico = "skit_fa_icon_green fa fa-check-circle-o";
		}
		var ic = document.getElementById( 'ico_abc_'+treeNode.id );
		if( ic )
		{
			ic.className = ico;
			ic.title = title;
		}
		myZtree.updateNode(treeNode);
	}

	function beforeDrop(treeId, treeNodes, targetNode, moveType, isCopy) {
		var srcNode = treeNodes[0];
		if( srcNode.demo || targetNode.demo  ){
			skit_alert("DEMO模板或目录不能被移动");
			return false;
		}
		if( targetNode.type != "dir"  ){
			skit_alert("模板只能选择移动到模板目录");
			return false;
		}
		if( !srcNode.template  ){
			skit_alert("只允许用户自定模板移动");
			return false;
		}
		if( srcNode.editmode  ){
			skit_alert("编辑中的模板不能拖拽移动");
			return false;
		}
		return true;
	}

	function onDrop(event, treeId, treeNodes, targetNode, moveType, isCopy)
	{
		if( moveType )
		{
			try
			{
				var node = treeNodes[0];
				var parentSource = treeNodes[0].getParentNode();
				var parentTarget = targetNode.getParentNode();
				if( !parentTarget ) parentTarget == targetNode;

				tIdOper = node.tId;
				//alert(moveType+" source["+parentSource.path+":"+treeNodes[0].path+"]   target["+parentTarget.path+":"+targetNode.path+"]");
				DiggConfigMgr.dragDropTemplate(moveType, node.id, targetNode.id, sysid, {
					callback:function(rsp) {
						skit_hiddenLoading();
						try
						{
							if( rsp.succeed ){
								node.id = rsp.result;
								open(node);
							}
							else{
								skit_error(rsp.message);
								parent.open();
							}
						}
						catch(e)
						{
							skit_alert("拖拽移动模板操作出现异常"+e);
						}
					},
					timeout:30000,
					errorHandler:function(message) {skit_hiddenLoading(); skit_alert(message); }
				});			
			}
			catch(e)
			{
				skit_alert(e);
			}
		}
	}
	$(document).ready(function(){
		try
		{
			var jsonData = '<ww:property value="jsonData" escape="false"/>';
			var zNodes = jQuery.parseJSON(jsonData);
			//var zNodes = json.children;
			$.fn.zTree.init($("#myZtree"), setting, zNodes);
			myZtree = $.fn.zTree.getZTreeObj("myZtree");
			myZtreeMenu = $("#rMenu");
			var id = getUserActionMemory("diggcfg!manager");
			var opened = false;
			document.forms[0].method = "POST";
			document.forms[0].action = "diggcfg!batchtest.action";
			document.forms[0].target = "iDownload";
			document.forms[0].submit();
			if( id )
			{
				var node = myZtree.getNodeByParam("id", id);
				if( node )
				{
					myZtree.selectNode(node);
					myZtree.expandNode(node, true);
					open(node);
					opened = true;
				}
			}
			if( !opened )
			{
				var node = myZtree.getNodeByParam("id", "/cos/config/modules/"+sysid+"/digg");
				if( node )
				{
					myZtree.selectNode(node);
					myZtree.expandNode(node, true);
					open(node);
				}
			}
		}
		catch(e)
		{
			skit_alert("初始化目录树异常"+e.message+", 行数"+e.lineNumber);
		}
	});
//#########################################################################
function open(node)
{
	if( "datasource" != node.type && "table" != node.type )
		setUserActionMemory("diggcfg!manager", node.id);
	//alert(node.type+", "+node.id);
	document.getElementById( 'divFullscreen' ).style.display = "none";
	document.getElementById( 'divAddQuery' ).style.display = "none";
	document.getElementById( 'divAddConfig' ).style.display = "none";
	document.getElementById( 'divSave' ).style.display = "none";
	document.getElementById( 'divCancel' ).style.display = "none";
	document.getElementById( 'divUpload' ).style.display = "";
	document.getElementById( 'divDebug' ).style.display = "none";
	document.getElementById( 'divPreview' ).style.display = "none";
	closePresaveTemplate();
	var iDigg = document.getElementById( 'iDigg' );
	var divDiggProfile = document.getElementById( 'divDiggProfile' );
	divDiggProfile.style.display = "none";
	iDigg.style.display = "";
	
	var spanTitle = document.getElementById( 'spanTitle' );
	spanTitle.innerHTML = "<i class='skit_fa_btn fa fa-digg'></i>元数据管理(编辑、测试、预览以及发布元数据查询、配置模板，通过模板快速实现对数据的有效管理)";
	var diggXmlEditor = document.getElementById( 'diggXmlEditor' );
	if( "datasourcecfgs" == node.id )
	{
		diggXmlEditor.style.display = "none";
		document.getElementById( 'divAddQuery' ).style.display = "";
		document.getElementById( 'divAddConfig' ).style.display = "";
		document.forms[0].method = "POST";
		document.forms[0].action = "diggcfg!datasource.action";
		document.forms[0].target = "iDigg";
		document.forms[0].submit();
		if( !node.init )
		{
			DiggConfigMgr.getModuleDatasources(sysid, {
				callback:function(rsp) {
					if( !rsp ) alert("打开数据源管理失败");
					if( rsp.succeed )
					{
						var newnodes = jQuery.parseJSON(rsp.result);
						for(var i = 0; i < newnodes.length; i++)
						{
							var e = newnodes[i];
							e["id"] = e.name;
							e["type"] = "datasource";
							//if( e.children )
							//{
								e["name"] = e.title+"("+e.dbtype+"@"+e.dbaddr+")";
								e["icon"] = "images/icons/spam.png";
								/*	for(var j = 0; j < e.children.length; j++ )
								{
									var child = e.children[j];
									child["icon"] = "images/icons/text_select.png";
								}
							}
							else
							{
								e["name"] = e.title+"("+e.error?e.error:"未知错误"+")";
								e["icon"] = "images/icons/transfer_size_mismatch.png";
							}*/
						}
						node["init"] = true;
						myZtree.addNodes(node, -1, newnodes);
					}
					else
					{
						skit_error(rsp.message);
					}
					
				},
				timeout:60000,
				errorHandler:function(err) {skit_hiddenLoading(); alert(err); }
			});
		}
	}
	else if( "datasource" == node.type )
	{
		diggXmlEditor.style.display = "none";
		document.getElementById( 'divAddQuery' ).style.display = "";
		document.getElementById( 'divAddConfig' ).style.display = "";
		if( node.dbtype == "mongo" ){
			skit_alert("暂不支持芒果数据库预览");
			return;
		}
		document.getElementById( 'db' ).value = "/cos/config/modules/"+sysid+"/datasource/"+node.id;
		document.forms[0].method = "POST";
		document.forms[0].action = "helper!database.action";
		document.forms[0].target = "iDigg";
		document.forms[0].submit();
		myZtree.expandNode(node, true, false);
		spanTitle.innerHTML = "<i class='skit_fa_icon fa fa-database'></i> 数据源管理【"+node.name+"】查看数据源列表";
	}
	else if( "table" == node.type )
	{
		diggXmlEditor.style.display = "none";
		document.getElementById( 'divAddQuery' ).style.display = "";
		document.getElementById( 'divAddConfig' ).style.display = "";
		var parent = node.getParentNode();
		document.getElementById( 'db' ).value = parent.id;
		document.getElementById( 'datatype' ).value = node.name;
		document.forms[0].method = "POST";
		document.forms[0].action = "diggcfg!remarkcells.action";
		document.forms[0].target = "iDigg";
		document.forms[0].submit();
		spanTitle.innerHTML = "<i class='skit_fa_icon fa fa-database'></i> 数据源管理【"+node.name+"】 查看数据源表结构";
	}
	else if( "dir" == node.type )
	{
		diggXmlEditor.style.display = "none";
		document.getElementById( 'divAddQuery' ).style.display = "";
		document.getElementById( 'divAddConfig' ).style.display = "";
		if( node.demo )
		{
			//myZtree.expandNode(treeNode, null, null, null, true);
			document.forms[0].method = "POST";
			document.forms[0].action = "helper!developer.action";
			document.forms[0].target = "iDigg";
			document.forms[0].submit();
		}
		else
		{
			document.getElementById("id").value = node.id;
			document.forms[0].method = "POST";
			document.forms[0].action = "diggcfg!query.action";
			document.forms[0].target = "iDigg";
			document.forms[0].submit();
		}
		myZtree.expandNode(node, true, false);
	}
	else if( node.editmode )
	{
		spanTitle.innerHTML = "<i class='skit_fa_icon fa fa-table'></i> 元数据模板【"+node.name+" "+node.title+"】 编辑中...";
		spanTitle.title = node.id;
		document.getElementById( 'divFullscreen' ).style.display = "";
		if( "xml" == node.editmode )
		{
			iDigg.style.display = "none";
			diggXmlEditor.style.display = "";
			if( node.xml ) setDiggXmlEditorValue(node.xml);
		}
		else
		{
			diggXmlEditor.style.display = "none";
		}
		document.getElementById( 'divUpload' ).style.display = "";
		document.getElementById( 'divSave' ).style.display = "";
		document.getElementById( 'divCancel' ).style.display = "";
		document.getElementById( 'divDebug' ).style.display = "";
		document.getElementById( 'divPreview' ).style.display = "";
	}
	else
	{
		spanTitle.innerHTML = "<i class='skit_fa_icon fa fa-table'></i> 元数据模板【"+node.name+" "+node.title+"】";
		spanTitle.title = node.id;
		diggXmlEditor.style.display = "none";
		document.getElementById( 'divAddQuery' ).style.display = "";
		document.getElementById( 'divAddConfig' ).style.display = "";
		divDiggProfile.style.display = "";
		document.getElementById( 'h2DiggTitle' ).innerHTML = node.title;
		document.getElementById( 'spanDiggName' ).innerHTML = node.name;
		document.getElementById( 'spanTimestamp' ).innerHTML = node.createtime;
		document.getElementById( 'btnTest' ).disabled = false;
		document.getElementById( 'btnTest1' ).disabled = false;
		document.getElementById( 'btnPreview' ).disabled = false;
		document.getElementById( 'btnPreview1' ).disabled = false;
		document.getElementById( 'gridxml' ).value = node.id;
		if( node.demo )
		{
			document.getElementById( 'iDiggState' ).className = "fa fa-support";
			document.getElementById( 'pDiggState' ).innerHTML = "Demo模板";
			document.getElementById( 'btnEdit' ).disabled = true;
			document.getElementById( 'btnEdit1' ).disabled = true;
			document.getElementById( 'btnPublish' ).disabled = true;
			document.getElementById( 'btnPublish1' ).disabled = true;
			document.getElementById( 'spanDiggVersion' ).innerHTML = "N/A";
			document.getElementById( 'spanDigger' ).innerHTML = "系统内置";
		}
		else
		{
			setTemplateCheck(node);
			var ic = document.getElementById( 'ico_abc_'+node.id );
			if( ic )
			{
				document.getElementById( 'iDiggState' ).className = ic.className;
				document.getElementById( 'pDiggState' ).innerHTML = ic.title;
			}
			document.getElementById( 'btnEdit' ).disabled = false;
			document.getElementById( 'btnEdit1' ).disabled = false;
			document.getElementById( 'btnPublish' ).disabled = false;
			document.getElementById( 'btnPublish1' ).disabled = false;
			document.getElementById( 'spanDiggVersion' ).innerHTML = node.version?node.version:"N/A";
			document.getElementById( 'spanDigger' ).innerHTML = node.developer?node.developer:"N/A";
		}
		document.getElementById( 'spanDiggId' ).innerHTML = node.id;
		viewVersions();
		//previewTemplateStyle();
	}
	resizeWindow();
}

function doFullscreen(btn)
{
	var iDigg = document.getElementById( 'iDigg' );
	iDigg.style.display = "none";
	document.getElementById( 'tdTree' ).style.display = "none";
	fullscreen = true;
	if( btn )
	{
		if( btn.className.indexOf("warning") != -1  ){
			btn.className = "btn btn-outline btn-default btn-xs";
			fullscreen = false;
			document.getElementById( 'tdTree' ).style.display = "";
			resizeWindow();
			return;
		}
		btn.className = "btn btn-outline btn-warning btn-xs";
		resizeWindow();
	}
	else
	{
		btn = document.getElementById( 'btnFullscreen' );
		btn.className = "btn btn-outline btn-warning btn-xs";
	}
}
//预览编辑中的模板
function previewTemplateEdit()
{
	var node = getNode();
	if( !node.check )
	{
		skit_alert("该模板还未完成检测，不能被预览");return;
	}
	if( node.check && node.check.err > 0 )
	{
		skit_alert("该模板检测发现有不可忽略的错误，请完成修改后再预览");return;
	}
	
	try
	{
		var div = document.createElement("div");
		div.id = "div-editdebug";
		div.className = "panel panel-primary";
		div.style.marginTop = 3;
		div.style.marginLeft = 3;
		div.style.position = "absolute";
		div.style.width = windowWidth - 10;
		var div1 = document.createElement("div");
		div1.className = "panel-heading";
		div1.style.height = 28;
		div.appendChild(div1);
		var div10 = document.createElement("div");
		div10.style.float = "left";
		div1.appendChild(div10);
		var span10 = document.createElement("span");
		span10.className = "panel-title";
		span10.innerHTML = "<i class='fa fa-eye'></i> 模板预览";
		span10.style.width = 512;
		div10.appendChild(span10);
		var div11 = document.createElement("div");
		div11.className = "panel-menu";
		div11.innerHTML = 
			"<button type='button' onclick='closeTemplateEdit()' data-action='close' class='btn btn-warning btn-action btn-xs'><i class='fa fa-times'></i></button>";
		div1.appendChild(div11);
		var div2 = document.createElement("div");
		div2.className = "panel-body";
		div2.innerHTML = "<iframe name='i-editdebug' id='i-editdebug' class='nonicescroll' style='width:100%;border:0px solid red;'></iframe>";
		div.appendChild(div2);
		document.forms[0].appendChild(div);
		div.style.left = 3;
		div.style.top = 32;
		div.style.zIndex = zIndex++;
		document.getElementById("divMask").style.visibility = "visible";
		document.getElementById("divMask").style.width = windowWidth;
		document.getElementById("divMask").style.height = windowHeight;
		document.getElementById("divMask").onclick = new Function("closeTemplateEdit();");
		document.getElementById("i-editdebug").style.height = windowHeight - 100;
		document.getElementById("gridtext").value = getDiggXmlEditorValue();

		document.getElementById("divDebugObjectScript").style.display = "";
		document.getElementById("divDebugObjectScript").style.zIndex = zIndex*2;
		tour();	
		document.forms[0].action = "diggcfg!editdebug.action";
		document.forms[0].method = "POST";
		document.forms[0].target = "i-editdebug";
		document.forms[0].submit();
	}
	catch(e)
	{
		skit_alert(e);
	}
}
//设置模版调试器uiMenuDebugSubgrid
function addSubgridDebugMenu(key, label, subgridobj)
{
	try{
		var id = "liSubgridDebugMenu"+key;
		if(document.getElementById(id)){
			return;
		}
		document.getElementById("liMenuDebugSubgrid").style.display = "";
		document.getElementById("liDividerMenuDebugInnerGrid").style.display = "";
		var uiMenuDebugSubgrid = document.getElementById("uiMenuDebugSubgrid");
		var colModel = subgridobj.colModel;
		var li = document.createElement("li");
		li.id = id;
		li.className = "dropdown-submenu";
		var html = "<a title='' href='javascript:;'><i class='skit_fa_icon fa fa-external-link fa-fw'></i>"+label+"</a>"+
			"<ul class='dropdown-menu'>"+
	    	"<li><a title='' onclick='editJavascript(11, this, \""+key+"\", \""+label+"\")'><i class='skit_fa_icon fa fa-edit fa-fw'></i>编辑数据预处理脚本</a></li>";
		var rhtml = "";
	   	if( colModel )
	   	{
	   		var r, i;
	   		//将模板的单元格渲染器添加到菜单
	   		for(i = 0; i < colModel.length; i++){
	   			var col = colModel[i];
   				r = col.render;
   				if( r ){
   					var title = label+"["+col.title+"]";
   					rhtml += "<li><a title='' onclick='editJavascript(12, this, \""+key+"\", "+i+");'><i class='skit_fa_icon fa fa-magic fa-fw'></i>"+title+"</a></li>";
   				}
	   		}
	   	}
	   	if( rhtml ){
	   		html += "<li class='dropdown-submenu'>"+
	   			"<a title='' href='javascript:;'><i class='skit_fa_icon fa fa-edit fa-fw'></i>编辑单元格渲染器脚本</a>"+
				"<ul class='dropdown-menu'>"+rhtml+"</ul></li>";
	   	}
	   	html += "<li class='divider'></li>";
	   	html += "<li><a title='' onclick='viewObject(10, this, \""+key+"\", \""+label+"\")'><i class='skit_fa_icon fa fa-info fa-fw'></i>查看配置对象</a></li>";
	   	html += "<li><a title='' onclick='viewObject(11, this, \""+key+"\", \""+label+"\")'><i class='skit_fa_icon fa fa-code fa-fw'></i>查看数据对象</a></li>";
	   	html += "</ul>";
		li.innerHTML = html;
		uiMenuDebugSubgrid.appendChild(li);
	}
	catch(e){
		alert(e);
	}
}

function addRenderDebugMenu(title, i, j)
{
	var id = "liRenderDebugMenu"+i+"_"+j;
	if( document.getElementById(id) ){
		return;
	}
	var ulMenuDebugRender = document.getElementById("ulMenuDebugRender");
	var li = document.createElement("li");
	li.id = id;
	li.innerHTML = "<a onclick='editJavascript(2,this, "+i+","+j+");'><i class='skit_fa_icon fa fa-magic fa-fw'></i>"+title+"</a>";
	ulMenuDebugRender.appendChild(li);
}

function viewObject(type, li, i, j){
	var ifr = window.frames["i-editdebug"];
	if( ifr && ifr.viewObject )
	{
		ifr.viewObject(type, li.innerHTML, i, j);
	}
}

function editJavascript(type, li, i, j){
	var ifr = window.frames["i-editdebug"];
	if( ifr && ifr.editJavascript )
	{
		ifr.editJavascript(type, li.innerHTML, i, j);
	}
}

function closeTemplateEdit()
{
	try
	{
		document.getElementById("liMenuDebugSubgrid").style.display = "none";
		document.getElementById("liDividerMenuDebugInnerGrid").style.display = "none";
		document.getElementById("divDebugObjectScript").style.display = "none";
		var ulMenuDebugRender = document.getElementById("ulMenuDebugRender");
		ulMenuDebugRender.innerHTML = "";
		var uiMenuDebugSubgrid = document.getElementById("uiMenuDebugSubgrid");
		uiMenuDebugSubgrid.innerHTML = "";
		var panel = document.getElementById("div-editdebug");
		document.forms[0].removeChild(panel);
		document.getElementById("divMask").style.visibility = "hidden";
	}
	catch(e)
	{
		alert("关闭窗口出现异常"+e.message+", 行数"+e.lineNumber);
	}
}

function previewTemplateData()
{
	var node = getNode();
	if( !node.demo )
	{
		if( !node.check )
		{
			skit_alert("该模板还未完成检测，不能被预览");return;
		}
		if( node.check && node.check.err > 0 )
		{
			skit_alert("该模板检测发现有不可忽略的错误，请完成修改后再预览");return;
		}
	}
	var tips = "元数据查询模板真实数据测试预览(数据不可修改，数据不可下载，记录操作日志)";
	if( "edit" == node.type ) tips = "元数据配置模板真实数据测试预览(数据不可修改，数据不可下载，记录操作日志)";
	openView(tips+":【"+node.name+"】"+node.title, "diggcfg!datapreview.action?gridxml="+node.id);
}

function previewTemplateConfig()
{
	skit_alert("该功能暂不开放");return;
}

function previewTemplateScript()
{
	var node = getNode();
	var iDigg = document.getElementById( 'iDigg' );
	var divDiggProfile = document.getElementById( 'divDiggProfile' );
	DiggConfigMgr.getTemplateXml(node.id, {
		callback:function(rsp) {
			if( rsp.succeed ) {
				if( rsp.message ) skit_alert(rsp.message);
				var diggXmlEditor = document.getElementById( 'diggXmlEditor' );
				setDiggXmlEditorValue(rsp.result);
				divDiggProfile.style.display = "none";
				iDigg.style.display = "none";
				diggXmlEditor.style.display = "";
				document.getElementById( 'divPreview' ).style.display = "";
				resizeWindow();
			}
			else skit_error(rsp.message);
		},
		timeout:30000,
		errorHandler:function(err) {skit_hiddenLoading(); alert(err); }
	});
}

function previewTemplateStyle(fullscrren)
{
	if( fullscrren )
	{
		var node = getNode();
		var name = document.getElementById( 'spanDiggName' ).innerHTML;
		openView("元数据查询模板预览: 【"+name+"】"+node.title, "diggcfg!stylepreview.action?gridxml="+node.id);
	}
	else
	{
		document.forms[0].method = "POST";
		document.forms[0].action = "diggcfg!stylepreview.action";
		document.forms[0].target = "iDigg";
		document.forms[0].submit();
	}
}

var newTemplateJson;
function addQueryTemplate(json)
{
	var table = getNode();
	var parent = table.getParentNode();
	newTemplateJson = json;
	document.getElementById( 'db' ).value = parent.id;
	document.getElementById( 'datatype' ).value = table.name;
	openPrecreate("query");
}
//编辑
function doEdit(visualize)
{
	var node = getNode();
	if( visualize )
	{
		var title = "";
		if( node.type == "edit" )
		{
			title = "元数据配置模板编辑器";
		}
		else
		{
			title = "元数据查询模板编辑器";
		}
		title += ": "+node.name+"("+node.id+")";
		openView(title, "diggcfg!preset.action?sysid="+sysid+"&gridxml="+node.id);
	}
	else
	{
		if( node.xml )
		{
			node["editmode"] = "xml";
			doFullscreen();
			open(node);
		}
		else
		{
			DiggConfigMgr.getTemplateXml(node.id, {
				callback:function(rsp) {
					if( rsp.succeed ) {
						if( rsp.message ) skit_alert(rsp.message);
						node["editmode"] = "xml";
						node["xml"] = rsp.result;
						doFullscreen();
						open(node);
					}
					else skit_error(rsp.message);
				},
				timeout:30000,
				errorHandler:function(err) {skit_hiddenLoading(); alert(err); }
			});
		}
	}
}
//创建元数据查询模板配置
function presetQueryTemplate()
{
	hideRMenu();
	document.getElementById( 'db' ).value = "";
	openPrecreate("query");
}

function presetConfigTemplate()
{
	hideRMenu();
	skit_alert("暂不开放创建配置模板");return;
	document.getElementById( 'db' ).value = "";
	openPrecreate("edit");
}

var zIndex = 10000;
function openPrecreate(type)
{
	document.getElementById( 'filetype' ).value = type;
	try
	{
		var div = document.createElement("div");
		div.id = "div-precreate";
		div.className = "panel panel-primary";
		div.style.marginTop = 3;
		div.style.marginLeft = 3;
		div.style.position = "absolute";
		div.style.width = windowWidth - 10;
		var div1 = document.createElement("div");
		div1.className = "panel-heading";
		div1.style.height = 28;
		div.appendChild(div1);
		var div10 = document.createElement("div");
		div10.style.float = "left";
		div1.appendChild(div10);
		var span10 = document.createElement("span");
		span10.className = "panel-title";
		span10.innerHTML = "<i class='fa fa-copy'></i> 创建元数据查询模板";
		if( "edit" == type )
			span10.innerHTML = "<i class='fa fa-copy'></i> 创建元数据配置模板";
		span10.style.width = 512;
		div10.appendChild(span10);
		var div11 = document.createElement("div");
		div11.className = "panel-menu";
		div11.innerHTML = 
			"<button type='button' onclick='savePrecreate()' class='btn btn-success btn-action btn-xs' style='height:20px;margin-right:4px;'><i class='fa fa-save'></i>保存</button>"+
			"<button type='button' onclick='closePrecreate()' data-action='close' class='btn btn-warning btn-action btn-xs'><i class='fa fa-times'></i></button>";
		div1.appendChild(div11);
		var div2 = document.createElement("div");
		div2.className = "panel-body";
		div2.innerHTML = "<iframe name='i-precreate' id='i-precreate' class='nonicescroll' style='width:100%;border:0px solid red;'></iframe>";
		div.appendChild(div2);
		document.forms[0].appendChild(div);
		div.style.left = 3;
		div.style.top = 32;
		div.style.zIndex = zIndex++;
		document.getElementById("divMask").style.visibility = "visible";
		document.getElementById("divMask").style.width = windowWidth;
		document.getElementById("divMask").style.height = windowHeight;
		document.getElementById("i-precreate").style.height = windowHeight - 100;

		document.forms[0].action = "diggcfg!precreate.action";
		document.forms[0].method = "POST";
		document.forms[0].target = "i-precreate";
		document.forms[0].submit();
	}
	catch(e)
	{
		skit_alert(e);
	}	
}

function createTemplate(template)
{
	if( template == null ) return;
	//alert(template.path);
	var parent = myZtree.getNodeByParam("id", template.path); 
	/*var node = getNode();
	if( node.type == "dir" && !node.demo )
	{
		parent = node;
	}
	else if( node.template && !node.demo )
	{
		parent = node.getParentNode();
	}
	if( !parent )
	{
		parent = myZtree.getNodeByParam("id", "/cos/config/moduels/"+sysid+"/digg"); 
	}*/
	try
	{

	    var title = template.title;
		var id = parent.id + "/"+template.id+".xml";
		var children = parent.children;
		if( children && children.length > 1 )
			for(var i = 0; i < children.length; i++)
			{
				var child = children[i];
				if( child.type == "dir" ) continue;
				if( child.title == id )
				{
					skit_alert("同级目录元数据查询配置模板唯一标识("+id+")不能相同.");
					return;
				}
				if( child.name == title )
				{
					skit_alert("同级目录元数据查询配置模板名称("+title+")不能相同.");
					return;
				}
			}
		var json = JSON.stringify(template);
		DiggConfigMgr.createQueryTemplate(json, {
			callback:function(rsp) {
				if( rsp.succeed ) {
					var newNode = new Object();
					newNode["id"] = id;
					//alert(id);
					newNode["name"] = title;
					newNode["title"] = template.id+".xml";
					newNode["type"] = template.type;
					newNode["newTemplate"] = true;
					newNode["editmode"] = "xml";
					newNode["template"] = true;
					newNode["xml"] = rsp.result;
					newNode["datamodel"] = template.datamodel;
					newNode["icon"] = "images/icons/new_item.png";
					//showObject(newNode);
					var nodes = myZtree.addNodes(parent, 0, newNode);
					myZtree.selectNode(nodes[0], false, false);
					open(nodes[0]);
					closePrecreate();
				}
				else skit_error(rsp.message);
			},
			timeout:30000,
			errorHandler:function(err) {skit_hiddenLoading(); alert(err); }
		});
	}
	catch(e)
	{
		skit_alert("创建新编辑的模板出现异常"+e.message+", 行数"+e.lineNumber);
	}
}

function savePrecreate()
{
	var ifr = window.frames["i-precreate"];
	if( ifr && ifr.createTemplate )
	{
		template = ifr.createTemplate();
	}
}

function closePrecreate()
{
	try
	{
		var panel = document.getElementById("div-precreate");
		document.forms[0].removeChild(panel);
		document.getElementById("divMask").style.visibility = "hidden";
	}
	catch(e)
	{
		alert("关闭窗口出现异常"+e.message+", 行数"+e.lineNumber);
	}
}

function cancelSaveTemplate()
{
	document.getElementById( 'tdTree' ).style.display = "";
	var node = getNode();
	var parent = node.getParentNode();
	fullscreen = false;
	if( node.newTemplate )
	{
		myZtree.removeNode(node);
		myZtree.selectNode(parent, false, false);
		open(parent);
	}
	else
	{
		delete node.editmode;
		open(node);
	}
}

function remarkCell(column, remark)
{
	var ds = document.getElementById( 'db' ).value;
	var tablename = document.getElementById( 'datatype' ).value;
//	alert(sysid+"."+ds+"."+tablename+"."+column+"."+remark);
	DiggConfigMgr.remarkDatasourceCell(sysid, ds, tablename, column, remark, {
		callback:function(rsp) {
			skit_hiddenLoading();
			if( !rsp ) skit_alert("备注数据源元数据失败");
		},
		timeout:30000,
		errorHandler:function(err) {skit_hiddenLoading(); alert(err); }
	});
}

function getNode()
{
	var nodes = myZtree.getSelectedNodes();
	return nodes[0];
}

function addTemplateDir()
{
	var node = getNode();
	//var path = node.id;
	hideRMenu();
	var newNode = new Object();
	newNode["id"] = "";
	newNode["name"] = node.name+"子目录";
	newNode["type"] = "dir";
	newNode["isParent"] = true;
	newNode["iconClose"] = "images/icons/folder_closed.png";
	newNode["iconOpen"] = "images/icons/folder_opened.png";
	var nodes = myZtree.addNodes(node, 0, newNode);
	myZtree.editName(nodes[0]);
}

function delTemplate()
{
	var node = getNode();
	hideRMenu();
	var tips = node.type=="dir"?("您确定要删除元数据模板【"+node.name+"】目录吗？"):("您确定要删除元数据模板【"+node.name+"】吗？");
	skit_confirm(tips, function(yes){
		if( yes )
		{
			DiggConfigMgr.delTemplate(sysid, node.id, {
				callback:function(rsp) {
					if( rsp.succeed ){
						var parent = node.getParentNode();
						myZtree.removeNode(node);
						myZtree.selectNode(parent, false, false);
						open(parent);
					}
					else skit_error(rsp.message);
				},
				timeout:30000,
				errorHandler:function(err) {skit_hiddenLoading(); alert(err); }
			});
		}
	});	
}

function copyTemplateDir()
{
	var node = getNode();
	var path = node.path?node.path:"";
	hideRMenu();
//	alert(sysid+"."+ds+"."+tablename+"."+column+"."+remark);
	DiggConfigMgr.copyTemplateDir(sysid, path, {
		callback:function(rsp) {
			if( rsp.succeed ){
				var newNode = jQuery.parseJSON(rsp.result);
				var nodes = myZtree.addNodes(node, newNode);
				myZtree.selectNode(nodes[0], false, false);
				open(nodes[0]);
			}
			else skit_error(rsp.message);
		},
		timeout:30000,
		errorHandler:function(err) {skit_hiddenLoading(); alert(err); }
	});
}

function querySql()
{
	hideRMenu();
	var node = getNode();
	var parent = node.getParentNode();
	var src = "/cos/config/modules/"+sysid+"/datasource/"+parent.id;
	var sql = 'select * from '+node.name;
	openView("SQL查询分析器:【"+node.name+"】"+src, "helper!sqlquery.action?db="+src+"&sql="+sql);
}

function testTemplate()
{
	var node = getNode();
	if( node.editmode )
	{
		presaveTemplate(true);
	}
	else
	{
		doTest();
	}
}

function doTest(mode)
{
	var node = getNode();
	DiggConfigMgr.getTemplateXml(node.id, {
		callback:function(rsp) {
			if( rsp.succeed ) {
				if( rsp.message ) skit_alert(rsp.message);
				setDiggXmlEditorValue(rsp.result);
				var iDigg = document.getElementById( 'iDigg' );
				var divDiggProfile = document.getElementById( 'divDiggProfile' );
				divDiggProfile.style.display = "none";
				iDigg.style.display = "";
				document.getElementById( 'divAddQuery' ).style.display = "none";
				document.getElementById( 'divAddConfig' ).style.display = "none";
				document.getElementById( 'divSave' ).style.display = "none";
				document.getElementById( 'divCancel' ).style.display = "";
				document.getElementById( 'divDebug' ).style.display = "";
				document.getElementById( 'divPreview' ).style.display = "";
				document.forms[0].method = "POST";
				document.forms[0].action = "diggcfg!test.action";
				document.forms[0].target = "iDigg";
				yaoSave = false;
				document.forms[0].submit();
				resizeWindow();
			}
			else skit_error(rsp.message);
		},
		timeout:30000,
		errorHandler:function(err) {skit_hiddenLoading(); alert(err); }
	});
	
}

var yaoSave = false;
function presaveTemplate(showEdit)
{
	/**
	var diggXmlEditor = document.getElementById( 'diggXmlEditor' );
	var iDigg = document.getElementById( 'iDigg' );
	document.getElementById("gridtext").value = getDiggXmlEditorValue();
	diggXmlEditor.style.display = showEdit?"":"none";
	iDigg.style.display = "";
	resizeWindow();
	*/

	try
	{
		var div = document.createElement("div");
		div.id = "div-presavetest";
		div.className = "panel panel-primary";
		div.style.marginTop = 3;
		div.style.marginLeft = 3;
		div.style.position = "absolute";
		div.style.width = windowWidth - 10;
		var div1 = document.createElement("div");
		div1.className = "panel-heading";
		div1.style.height = 28;
		div.appendChild(div1);
		var div10 = document.createElement("div");
		div10.style.float = "left";
		div1.appendChild(div10);
		var span10 = document.createElement("span");
		span10.className = "panel-title";
		span10.innerHTML = "<i class='fa fa-eye'></i> 模板监测";
		span10.style.width = 512;
		div10.appendChild(span10);
		var div11 = document.createElement("div");
		div11.className = "panel-menu";
		div11.innerHTML = 
			"<button type='button' onclick='closePresaveTemplate()' data-action='close' class='btn btn-warning btn-action btn-xs'><i class='fa fa-times'></i></button>";
		div1.appendChild(div11);
		var div2 = document.createElement("div");
		div2.className = "panel-body";
		div2.innerHTML = "<iframe name='i-presavetest' id='i-presavetest' class='nonicescroll' style='width:100%;border:0px solid red;'></iframe>";
		div.appendChild(div2);
		document.forms[0].appendChild(div);
		div.style.left = 3;
		div.style.top = 32;
		div.style.zIndex = zIndex++;
		document.getElementById("divMask").style.visibility = "visible";
		document.getElementById("divMask").style.width = windowWidth;
		document.getElementById("divMask").style.height = windowHeight;
		document.getElementById("divMask").onclick = new Function("closePresaveTemplate();");
		document.getElementById("i-presavetest").style.height = windowHeight - 100;
		document.getElementById("gridtext").value = getDiggXmlEditorValue();

		yaoSave = showEdit?false:true;
		document.forms[0].method = "POST";
		document.forms[0].action = "diggcfg!pretest.action";
		document.forms[0].target = "i-presavetest";
		document.forms[0].submit();
	}
	catch(e)
	{
		skit_alert(e);
	}
}

function closePresaveTemplate()
{
	try
	{
		var panel = document.getElementById("div-presavetest");
		if( panel ){
			document.forms[0].removeChild(panel);
			document.getElementById("divMask").style.visibility = "hidden";
		}
	}
	catch(e)
	{
		alert("关闭窗口出现异常"+e.message+", 行数"+e.lineNumber);
	}
}

function finishTest(tips, err, war)
{
	var node = getNode();
	var old_err = 0, old_war = 0;
	if( !node.check ) node.check = new Object();
	else {
		old_err = node.check.err;
		old_war = node.check.war;
	}
	node.check.err = err;
	node.check.war = war;
	err -= old_err;
	war -= old_war;
	//alert("old:("+old_err+","+old_war+"), "+err+","+war);
	setParentCheckResult(node, err, war);
	setTemplateCheck(node)
	
	if( yaoSave )
	{
		if( err > 0 )
		{
			skit_confirm(tips+"您要继续完善编辑吗？", function(yes){
				if( yes )
				{
					closePresaveTemplate();
					/*var diggXmlEditor = document.getElementById( 'diggXmlEditor' );
					var iDigg = document.getElementById( 'iDigg' );
					document.getElementById("gridtext").value = getDiggXmlEditorValue();
					diggXmlEditor.style.display = "";
					iDigg.style.display = "";
					resizeWindow();*/
				}
				else
				{
					cancelSaveTemplate();
				}
			});
		}
		else
		{
			doSave();
		}
	}
}

var iVersion;
var iRemark;
function doSave()
{
	try
	{
		var node = getNode();
		var lastversion = node.version;
		var template = document.getElementById("divSaveTemplate").innerHTML;
		template = template.replace("#version#", "iVersion");
		template = template.replace("#lastversion#", "模板当前版本: "+lastversion);
		template = template.replace("#templateTitle#", "iTemplateTitle");
		template = template.replace("#versionremark#", "iRemark");
		var SM = new SimpleModal({"btn_ok":"确定","btn_cancel":"取消","width":440});
	    var content = "<div style='height:224px;width:400px;border:0px solid red;overflow-y:hidden;'>"+template+"</div>"
	    SM.show({
	    	"title":"<span class='panel-title' style='width:400px;'><i class='skit_fa_icon_blue fa fa-save'></i> 保存元数据模板［"+node.id+"］</span>",
	        "model":"confirm",
	        "callback": function(){
	        	var version = iVersion.value;
	            if( version == "" )
	            {
	        		$("#iVersion").tooltip("show");
	        		document.getElementById( "iVersion" ).focus();
	            	return false;
	            }
	            var regexp2 = "^(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9]|0)\\."+
	            			   "(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9]|0)\\."+
	            			   "(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9]|0)\\."+
	            			   "(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[0-9])$";
	            var m2 = version.match(new RegExp(regexp2));
	            if( !m2 )
	            {
	        		$("#iVersion").tooltip("show");
	        		iVersion.focus();
			        return false;
	            }
	        	var args1 = version.split(".");
	        	//var args0 = lastversion.split(".");
	        	var v1 = Number(args1[0])*1000000+Number(args1[1])*10000+Number(args1[2])*100+Number(args1[3]);
	        	//var v0 = Number(args0[0])*1000000+Number(args0[1])*10000+Number(args0[2])*100+Number(args0[3]);
	            if( v1 == 0 )
	            {
	        		$("#iVersion").tooltip("show");
	        		iVersion.focus();
	            	return false;
	            }

	            var remark = iRemark.value;
	            if( remark.length < 0 )
	            {
	        		$("#iRemark").tooltip("show");
	        		iRemark.focus();
	            	return false;
	            }
	        	var title = iTemplateTitle.value;
	            if( title == "" )
	            {
	        		$("#iTemplateTitle").tooltip("show");
	        		iTemplateTitle.focus();
	            	return false;
	            }
				try
				{
					var text = getDiggXmlEditorValue();
					var developer = document.getElementById( 'account' ).value;
					document.getElementById( 'version' ).value = version;
					document.getElementById( 'remark' ).value = remark;//特性描述
					document.getElementById( 'gridxml' ).value = node.id;
					node.name = title;
					node.developer = title;
					text = setXml(text, "title", title);
					text = setXml(text, "developer", developer);
					text = setXml(text, "version", version);
					setDiggXmlEditorValue(text);
					document.getElementById("gridtext").value = text;
					document.forms[0].method = "POST";
					document.forms[0].action = "diggcfg!save.action";
					document.forms[0].target = "iDownload";
					document.forms[0].submit();
				}
				catch(e)
				{
					alert(e);
				}
	        	return true;
	        },
	        "cancelback": function(){
				closePresaveTemplate();
				/*var diggXmlEditor = document.getElementById( 'diggXmlEditor' );
				var iDigg = document.getElementById( 'iDigg' );
				document.getElementById("gridtext").value = getDiggXmlEditorValue();
				diggXmlEditor.style.display = "";
				iDigg.style.display = "";
				resizeWindow();*/
	        },
	    	"contents": content
	    });
	    var h = windowHeight;
	    var w = windowWidth;
	    var div = document.getElementById('simple-modal');
	    var top = h/2 - div.scrollHeight/2;
	    SM.options.offsetTop = top;
	    SM._display();
    	iRemark = document.getElementById("iRemark");
    	iRemark.value = node.versionremark?node.versionremark:"";
    	iRemark.onkeydown=function(event){ 
    		$("#iRemark").tooltip("hide");
    	}
    	iVersion = document.getElementById("iVersion");
    	iVersion.value = node.version?node.version:"";
    	document.getElementById("oldversion").value = node.version?node.version:"";
    	iVersion.focus();
    	iVersion.onkeydown=function(event){ 
    		$("#iVersion").tooltip("hide");
    	}

    	var userAgent = navigator.userAgent; //取得浏览器的userAgent字符串
    	var isSafari = userAgent.indexOf("Safari") > -1;
    	if( !isSafari ){
        	$("#iVersion").inputmask("mask", {"mask": "9.9.9.9"});
    	}
	    iTemplateTitle = document.getElementById("iTemplateTitle");
	    iTemplateTitle.value = node.name;
	    iTemplateTitle.onkeydown=function(event){ 
    		$("#iTemplateTitle").tooltip("hide");
    	}
	}
	catch(e)
	{
		alert(e);
	}
}

function finishSave(succeed, id, tips)
{
	var node = getNode();
	if( succeed )
	{
		node.version = document.getElementById( 'version' ).value;
		node.remark = document.getElementById( 'remark' ).value;		
		document.getElementById( 'tdTree' ).style.display = "";
		node.template = true;
		delete node.editmode;
		if( node.xml ) delete node.xml;
		if( node.newTemplate ) delete node.newTemplate;
		if( node.type == "edit" ) node.icon = "images/icons/drafts.png";
		else node.icon = "images/icons/search.png";
		myZtree.updateNode (node);
		fullscreen = false;
		myZtree.updateNode (node);
		open(node);
	}
	else
	{
		skit_alert(tips, "保存模板失败")
		/*var diggXmlEditor = document.getElementById( 'diggXmlEditor' );
		var iDigg = document.getElementById( 'iDigg' );
		diggXmlEditor.style.display = "";
		iDigg.style.display = "";
		resizeWindow();*/
	}
}

function setXml(text, name, value)
{
	var start, end, i, j, c, str0, str1;
	start = text.indexOf("<x");
	end = text.indexOf(">", start);
	var tag = name+"=";
	i = text.lastIndexOf(tag, end);
	var ff = true;
	//alert("start:"+start+",end:"+end+", i="+i);
	if( i > start )
	{
		i += tag.length;
		c = text.charAt(i);
		j = text.indexOf(c, i+1);
		if( i <= j )
		{
			ff = false;
			str0 = text.substring(0, i+1);
			str1 = text.substring(j);
			text = str0+value+str1;
		}
	}
	if( ff )
	{
		attr = " "+name+"='"+value+"'";
		str0 =  text.substring(0, end);
		str1 = text.substring(end);
		text = str0+attr+str1;
	}
	return text;
}
/*
var aaa = "<x type='query' title='版权百科用户搜索日报' developer='admin'>";
aaa = setXml(aaa, "title", "aaa");
alert(aaa);
aaa = setXml(aaa, "remark", "bbb");
alert(aaa);
aaa = setXml(aaa, "version", "1.0.0.0");
alert(aaa);
*/

function doPublish()
{
	var node = getNode();
	if( node.newTemplate )
	{
		skit_alert("新建模板还未不能发布");
		return;
	}
	if( !node.check )
	{
		skit_alert("该元数据查询配置模板还没有测试，请先测试。");
		return;
	}
	if( node.check.err )
	{
		skit_alert("该元数据查询配置模板有"+node.check.err+"个严重错误，请按照测试结果的提示先解决模板中的问题后再发布。");
		return;
	}
	skit_confirm("将该元数据查询配置模板配置到系统后台菜单管理，通过权限控制提供给其它用户使用。您是否打开后台菜单管理", function(yes){
		if( yes )
		{
			var url = "digg!query.action?gridxml="+node.id;
			parent.publishMenu(node.name, url);
		}
	});		
}

function copyUrl()
{
	var node = getNode();
	var url = "digg!query.action?gridxml="+node.id;
	skit_input("模板链接地址，点击确定直接跳转到后台菜单管理进行菜单设置", url, function(yes, url){
		if( yes ){
			window.setTimeout("doPublish()",500);
		}
	});
}

var filename;
function preuploadTemplate()
{
	var node = getNode();
	hideRMenu();
	var divUploadfile = document.getElementById( 'divUploadfile' );
	divUploadfile.style.display = "";
	$("#uploadfile").fileinput({
			language: 'zh', //设置语言
			uploadUrl: "diggcfg!upload.action", //上传的地址
			allowedFileExtensions: ["xml"],//接收的文件后缀
			showUpload: true, //是否显示上传按钮
			showCaption: false,//是否显示标题
			showClose: false,
			showPreview: true,
			browseClass: "btn btn-primary", //按钮样式     
			previewSettings: { image: {width: "auto", height: "260"} },
			//dropZoneEnabled: false,//是否显示拖拽区域
			//maxFileSize: 0,//单位为kb，如果为0表示不限制文件大小
			//minFileCount: 0,
			maxFileCount: 1, //表示允许同时上传的最大文件个数
			enctype: 'multipart/form-data',
			validateInitialCount:true,
			previewFileIcon: "<i class='glyphicon glyphicon-king'></i>",
			msgFilesTooMany: "选择上传的文件数量({n}) 超过允许的最大数值{m}！",
            uploadExtraData: function(previewId, index) {   //额外参数的关键点
                var obj = {};
                obj.path = node.id;
                obj.gridxml = filename;
                return obj;
            }
	});
	document.getElementById( 'uploadtitle' ).innerHTML = "从本地磁盘选择上传元数据查询配置模板文件";
	divUploadfile.style.top = 64;//windowHeight/2 - divUploadfile.clientHeight*2/3;
	divUploadfile.style.left = windowWidth/2 - divUploadfile.clientWidth/2;
	$("#uploadfile").on("fileloaded", function (data, previewId, index) {
		var tips = "您确定要上传元数据查询配置模板["+previewId.name+"]吗？";
		document.getElementById( 'uploadtitle' ).innerHTML = tips;
		filename = previewId.name;
	});
	//导入文件上传完成之后的事件
	$("#uploadfile").on("fileuploaded", function (event, data, previewId, index) {
		if( data.response.alt )
		{
			skit_alert(data.response.alt);
		}
		if( data.response.succeed )
		{
			var template = data.response.template;
			var oldNode = myZtree.getNodeByParam("id", template.id);
			if( oldNode == null )
			{
				var parent = node;
				template["newTemplate"] = true;
				template["icon"] = "images/icons/new_item.png";
				template.editmode = "xml";
				template["xml"] = data.response.xml;
				var nodes = myZtree.addNodes(parent, 0, template);
				myZtree.selectNode(nodes[0], false, false);
				open(nodes[0]);
			}
			else
			{
				skit_alert("您上传了模板【"+template.name+"】的新脚本配置。");
				var diggXmlEditor = document.getElementById( 'diggXmlEditor' );
				setDiggXmlEditorValue(data.response.xml);
				oldNode.editmode = "xml";
				oldNode["xml"] = data.response.xml;
				myZtree.selectNode(oldNode, false, false);
				open(oldNode);
			}
		}
		document.getElementById( 'divUploadfile' ).style.display = "none";
		$("#uploadfile").fileinput("destroy");
	});
}
function closeuploadfile()
{
	document.getElementById( 'divUploadfile' ).style.display = "none";
	$("#uploadfile").fileinput("destroy");
}

function downloadTemplate()
{
	hideRMenu();
	var node = getNode();
	if( node.newTemplate )
	{
		skit_alert("新建模板还未保存不能下载");
		return;
	}
	document.getElementById( 'gridxml' ).value = node.id;
	document.forms[0].method = "POST";
	document.forms[0].action = "diggcfg!download.action";
	document.forms[0].target = "iDigg";
	document.forms[0].submit();
}

function downloadTemplates()
{
	hideRMenu();
	document.forms[0].method = "POST";
	document.forms[0].action = "diggcfg!exporttempaltes.action";
	document.forms[0].target = "iDigg";
	document.forms[0].submit();
}

//上传模版
function preuploadTemplates()
{
	var node = getNode();
	hideRMenu();
	skit_confirm("执行模板导入会直接覆盖现有模版，为了确保不出错，请考虑先执行导入模板操作。<B>请确认是否执行导入？</B>", function(yes){
		if( yes )
		{
			var divUploadfile = document.getElementById( 'divUploadfile' );
			divUploadfile.style.display = "";
			$("#uploadfile").fileinput({
					language: 'zh', //设置语言
					uploadUrl: "diggcfg!importtempaltes.action", //上传的地址
					allowedFileExtensions: ["zip"],//接收的文件后缀
					showUpload: true, //是否显示上传按钮
					showCaption: false,//是否显示标题
					showClose: false,
					showPreview: true,
					browseClass: "btn btn-primary", //按钮样式     
					previewSettings: { image: {width: "auto", height: "260"} },
					//dropZoneEnabled: false,//是否显示拖拽区域
					//maxFileSize: 0,//单位为kb，如果为0表示不限制文件大小
					//minFileCount: 0,
					maxFileCount: 1, //表示允许同时上传的最大文件个数
					enctype: 'multipart/form-data',
					validateInitialCount:true,
					previewFileIcon: "<i class='glyphicon glyphicon-king'></i>",
					msgFilesTooMany: "选择上传的文件数量({n}) 超过允许的最大数值{m}！",
		            uploadExtraData: function(previewId, index) {   //额外参数的关键点
		                var obj = {};
		                obj.sysid = sysid;
		                return obj;
		            }
			});
			document.getElementById( 'uploadtitle' ).innerHTML = "从本地磁盘选择导入元数据模板文件包(*.zip)";
			divUploadfile.style.top = 64;//windowHeight/2 - divUploadfile.clientHeight*2/3;
			divUploadfile.style.left = windowWidth/2 - divUploadfile.clientWidth/2;
			$("#uploadfile").on("fileloaded", function (data, previewId, index) {
				var tips = "您确定要导入元数据模板吗？";
				document.getElementById( 'uploadtitle' ).innerHTML = tips;
				filename = previewId.name;
				//if( node.id.indexOf(".xml") != -1 )	document.getElementById( 'gridxml' ).value = node.id;
				//else document.getElementById( 'gridxml' ).value = node.id+"/"+previewId.name;
			});
			//导入文件上传完成之后的事件
			$("#uploadfile").on("fileuploaded", function (event, data, previewId, index) {
				if( data.response.alt )
				{
					skit_alert(data.response.alt);
				}
				if( data.response.succeed )
				{
					if( parent && parent.open ){
						parent.open();
					}
					else {
						window.location.reload();
					}
				}
				document.getElementById( 'divUploadfile' ).style.display = "none";
				$("#uploadfile").fileinput("destroy");
			});
		}
	});
}

var iTemplateId;
var iTemplateTitle;
function copyTemplate()
{
	hideRMenu();
	try
	{
		var node = getNode();
		var parent = node.getParentNode();
		var template = document.getElementById("divCopyTemplate").innerHTML;
		template = template.replace("#templateId#", "iTemplateId");
		template = template.replace("#templateTitle#", "iTemplateTitle");
		var SM = new SimpleModal({"btn_ok":"确定","btn_cancel":"取消","width":520});
	    var content = "<div style='height:144px;width:480px;border:0px solid red;overflow-y:auto;'>"+template+"</div>"
	    SM.show({
	    	"title":"拷贝元数据模板［"+node.name+"］"+node.title,
	        "model":"confirm",
	        "callback": function(){
	            var id = iTemplateId.value;
	            if( id == "" )
	            {
	        		$("#iTemplateId").tooltip("show");
	        		document.getElementById( "iTemplateId" ).focus();
	            	return false;
	            }
	    		var regexp1 = "^([A-Za-z])|([a-zA-Z0-9])|([a-zA-Z0-9])|([a-zA-Z0-9_])+$";
	            var m2 = id.match(new RegExp(regexp1));
	            if( !m2 )
	            {
	        		$("#iTemplateId").tooltip("show");
	        		document.getElementById( "iTemplateId" ).focus();
			        return false;
	            }
	        	var title = iTemplateTitle.value;
	            if( title == "" )
	            {
	        		$("#iTemplateTitle").tooltip("show");
	        		document.getElementById( "iTemplateTitle" ).focus();
	            	return false;
	            }
	            id = id + ".xml";
	            if( parent.children )
	            {
	            	var children = parent.children;
	            	for(var i = 0; i < children.length; i++)
	            	{
	            		var child = children[i];
	            		if( child.title == id )
	            		{
	    	        		$("#iTemplateId").tooltip("show");
	    	        		document.getElementById( "iTemplateId" ).focus();
			            	return false;
	            		}
	        			if( child.name == title )
	        			{
	    	        		$("#iTemplateTitle").tooltip("show");
	    	        		document.getElementById( "iTemplateTitle" ).focus();
			            	return false;
	        			}
	            	}
	            }
				try
				{
					DiggConfigMgr.getTemplateXml(node.id, {
						callback:function(rsp) {
							if( rsp.succeed ) {
								if( rsp.message ) skit_alert(rsp.message);
								var newNode = new Object();
								newNode["id"] = parent.id+"/"+id;
								newNode["title"] = id;
								newNode["name"] = title;
								newNode["type"] = node.type;
								newNode["newTemplate"] = true;
								newNode["editmode"] = "xml";
								newNode["datamodel"] = node.datamodel;
								newNode["icon"] = "images/icons/new_item.png";
								newNode["xml"] = rsp.result;
								var nodes = myZtree.addNodes(parent, 0, newNode);
								myZtree.selectNode(nodes[0], false, false);
								open(nodes[0]);
							}
							else skit_error(rsp.message);
						},
						timeout:30000,
						errorHandler:function(err) {skit_hiddenLoading(); alert(err); }
					});
				}
				catch(e)
				{
					alert(e);
				}
	        	return true;
	        },
	        "cancelback": function(){
	        },
	    	"contents": content
	    });
	    var h = windowHeight;
	    var w = windowWidth;
	    var div = document.getElementById('simple-modal');
	    var top = h/2 - div.scrollHeight/2;
	    SM.options.offsetTop = top;
	    SM._display();
	    iTemplateTitle = document.getElementById("iTemplateTitle");
	    iTemplateTitle.value = node.name;
	    iTemplateTitle.onkeydown=function(event){ 
    		$("#iTemplateTitle").tooltip("hide");
    	}
	    iTemplateId = document.getElementById("iTemplateId");
	    iTemplateId.value = node.title.substring(0, node.title.length-4);
	    iTemplateId.onkeydown=function(event){ 
    		$("#iTemplateId").tooltip("hide");
    	}
	    iTemplateId.focus();
	}
	catch(e)
	{
		alert(e);
	}
}

function getDiggXmlEditorValue(){
    var ifr = window.frames["diggXmlEditor"];
    if( ifr && ifr.getValue )
    {
    	return ifr.getValue();
    }
    return ""
}
function setDiggXmlEditorValue(xml){
	document.getElementById( 'content' ).value = xml;
	document.forms[0].method = "POST";
	zIndex += 1;
	document.forms[0].action = "editor!xml.action?t="+zIndex;
	document.forms[0].target = "diggXmlEditor";
	document.forms[0].submit();
	/*var ifr = window.frames["diggXmlEditor"];
    if( ifr && ifr.setValue )
    {
    	ifr.setValue(xml);
    }*/
}
function previewBackup(version, xml){

	var divDiggProfile = document.getElementById( 'divDiggProfile' );
	divDiggProfile.style.display = "none";
	var iDigg = document.getElementById( 'iDigg' );
	iDigg.style.display = "none";
	var diggXmlEditor = document.getElementById( 'diggXmlEditor' );
	diggXmlEditor.style.display = "";
	var spanTitle = document.getElementById( 'spanTitle' );
	spanTitle.innerHTML += "版本【"+version+"】上次备份";
	resizeWindow();
	document.getElementById( 'content' ).value = unicode2Chr(xml);
	document.forms[0].method = "POST";
	zIndex += 1;
	document.forms[0].action = "editor!xml.action?t="+zIndex;
	document.forms[0].target = "diggXmlEditor";
	document.forms[0].submit();
}

function viewVersions(timeline)
{
	if( timeline ){

		var dataurl = "diggcfg!versiontimeline.action?id="+getNode().id;
		dataurl = chr2Unicode(dataurl);
		document.getElementById("iDigg").src = "helper!timeline.action?dataurl="+dataurl;
		return;
	}
	document.forms[0].method = "POST";
	document.getElementById("id").value = getNode().id;
	document.forms[0].action = "diggcfg!version.action";
	document.forms[0].target = "iDigg";
	document.forms[0].submit();
}
function tour()
{
	var showTour = getUserActionMemory("tourDiggMgr");
	if( showTour != "1" )
	{
		setUserActionMemory("tourDiggMgr", "1");
		var divTour = document.getElementById("divTour");
		divTour.style.display = "";
		divTour.style.zIndex = zIndex*3;
		divTour.style.left = 1;
		divTour.style.top = 28;
	}
}
</SCRIPT>
</html>