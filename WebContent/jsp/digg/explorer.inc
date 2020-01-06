<%@ page language="java" contentType="text/html; charset=UTF-8"  pageEncoding="UTF-8"%>
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
<div style='position:absolute;top:1px;left:5px;cursor:pointer;display:none' id='divDebugObjectScript'>
    <div class="btn-group btn-block">
		<button type="button" class="btn btn-outline btn-default btn-xs" style='width:80%;height:32px;font-size:12px;padding-left:16px;padding-right:64px;'
			><span class="fa fa-eye"></span>点击右侧下拉菜单对模板进行调测</button>
	    <button type="button" class="btn btn-outline btn-default btn-xs dropdown-toggle" style='height:32px'
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
	    		编辑全局用户自定义脚本</a></li>
	    	<li><a title='' onclick='editJavascript(1,this)'><i class='skit_fa_icon fa fa-edit fa-fw'></i>
	    		编辑主表数据预处理用户自定义脚本</a></li>
	    	<li><a title='' onclick='editJavascript(3,this)'><i class='skit_fa_icon fa fa-edit fa-fw'></i>
	    		编辑表格初始化前回调用户自定义脚本</a>
	    	<li id='liMenuDebugRender' class="dropdown-submenu" style='display:none'>
	    		<a title='' href='javascript:;'><i class='skit_fa_icon fa fa-edit fa-fw'></i>编辑主表单元格渲染器脚本</a>
    			<ul class="dropdown-menu" id='ulMenuDebugRender'>
    			</ul>
	    	</li>
	    	<li id='liMenuDebugEditor' class="dropdown-submenu" style='display:none'>
	    		<a title='' href='javascript:;'><i class='skit_fa_icon fa fa-edit fa-fw'></i>编辑主表单元格自定义编辑器脚本</a>
    			<ul class="dropdown-menu" id='ulMenuDebugEditor'>
    			</ul>
	    	</li>
	    	<li id='liMenuDebugEditorInit' class="dropdown-submenu" style='display:none'>
	    		<a title='' href='javascript:;'><i class='skit_fa_icon fa fa-edit fa-fw'></i>编辑主表单元格编辑器初始化脚本</a>
    			<ul class="dropdown-menu" id='ulMenuDebugEditorInit'>
    			</ul>
	    	</li>
	    	<li id='liMenuDebugEditable' class="dropdown-submenu" style='display:none'>
	    		<a title='' href='javascript:;'><i class='skit_fa_icon fa fa-edit fa-fw'></i>编辑主表单元格是否可编辑判断脚本</a>
    			<ul class="dropdown-menu" id='ulMenuDebugEditable'>
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
	    	<li class="divider" id='liDividerDebuglog'></li>
	    	<li id='liParserlog'><a title='' onclick='top.openView("查看模板解析日志(XML)", "diggcfg!debuglog.action?id=xml")'><i class='skit_fa_icon_red fa fa-bug fa-fw'></i>
	    		查看模板解析日志(XML)</a></li>
	    	<li id='liDigglog'><a title='' onclick='top.openView("查看数据引擎日志(DIGG)", "diggcfg!debuglog.action?id=digg")'><i class='skit_fa_icon_red fa fa-digg fa-fw'></i>
	    		查看数据引擎日志(DIGG)</a></li>
	    	<li id='liDiggexport'><a title='' onclick='top.openView("查看数据导出日志(EXPORT)", "diggcfg!debuglog.action?id=export")'><i class='skit_fa_icon_red fa fa-cloud-download fa-fw'></i>
	    		查看数据导出日志(EXPORT)</a></li>
	    </ul>
    </div>
</div>
<div id='divMask' class='skit_mask' onclick='closeprecopy()' style='cursor:pointer;' title='点击关闭弹窗'></div>