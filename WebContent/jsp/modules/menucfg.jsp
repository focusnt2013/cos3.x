<%@ page language="java" pageEncoding="UTF-8"%>
<%@ page import="com.focus.cos.web.common.Kit"%>
<%@ taglib uri="/webwork" prefix="ww" %>
<%Kit.noCache(request,response);  %>
<html>
<head>
<link type="text/css" href="skit/ztree/css/zTreeStyle/zTreeStyle.css" rel="stylesheet"/>
<link type="text/css" href="skit/css/bootstrap-tour.min.css" rel="stylesheet">
<style type='text/css'>
/* ------------- 右键菜单 -----------------  */
.xml_edit
{
	width: 100%;
	font-size: 12px;
	font-family:微软雅黑,Roboto,sans-serif;
	margin:0 0 1px;
	color: #ffffff;
	background-color: #373737;
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
</style>
<%=Kit.getDwrJsTag(request,"interface/MenusMgr.js")%>
<%=Kit.getDwrJsTag(request,"engine.js")%>
<%=Kit.getDwrJsTag(request,"util.js")%>
</head>
<body onResize='resizeWindow()' style='overflow-y:hidden;'>
<TABLE style='width:100%'>
<TR><TD width='360'>
        <div class="panel panel-default" style='border: 1px solid #aaaaaa'>
   			<div class="panel-heading"><i class='skit_fa_btn fa fa-bars'></i>菜单导航树
            <div style='float:right;right:4px;top:0px;'>
 				<button type="button" class='btn btn-outline btn-primary btn-xs' onclick='downloadXml()'>
 					<i class='fa fa-cloud-download'></i> 导出</button>
            </div>
            <div style='float:right;padding-right:4px;top:0px;'>
 				<button type="button" class="btn btn-outline btn-danger btn-xs" onclick='uploadXml()'>
 					<i class='fa fa-cloud-upload'></i> 导入</button>
            </div>
			<ww:if test='listData.size>0'>
            <div class="btn-group btn-block" style='float:right;padding-right:4px;top:0px;width:128px;'>
 				<button type="button" class="btn btn-outline btn-success btn-xs" style='width:80%;height:22px;font-size:12px;'>
 					<i class='fa fa-weixin'></i> 微信组件菜单</button>
 				<button type="button" class="btn btn-outline btn-success btn-xs dropdown-toggle" style='height:22px'
			    	data-toggle="dropdown" aria-expanded="false">
			      <span class="fa fa-caret-down"></span>
			    </button>
    			<ul class="dropdown-menu" style='font-size:12px;cusor:pointer;'>
					<ww:iterator value="listData" status="loop">
					<li><a onclick="addWeixinCfgMenu('<ww:property value='getString("weixinno")'/>');"
						style='padding: 3px 10px;'>
						<i class='skit_fa_icon fa fa-list'></i> 添加公众号【<ww:property value='getString("name")'/>】管理菜单组
						</a>
					</li>
					</ww:iterator>
    			</ul>
            </div>
			</ww:if>
   			</div>
   			<div class="panel-body" style='padding: 0px;' id='divTree'>
				<ul id='myZtree' class='ztree'></ul>
				<div id="rMenu">
					<ul style='font-size:12px;'>
						<li onclick="addMenu();"><i class='skit_fa_icon fa fa-plus-circle'></i> 新增菜单</li>
						<li onclick="delMenu();"><i class='skit_fa_icon_red fa fa-remove'></i> 删除菜单</li>
						<li onclick="chkMenu();"><i class='skit_fa_icon_orange fa fa-check'></i> 检查菜单</li>
						<ww:if test='userRole==1'>
						<li class="divider"></li>
						<li onclick="addRoleMgrMenu();"><i class='skit_fa_btn fa fa-group'></i> 添加权限管理菜单组</li>
						<!-- <li onclick="addCmpCfgMenu();"><i class='skit_fa_btn fa fa-plus-circle'></i> 添加模块菜单配置组</li>-->
						<!-- <li onclick="addDiggCfgMenu();"><i class='skit_fa_btn fa fa-digg'></i> 添加元数据管理菜单组</li>-->
						<li onclick="addClusterCfgMenu();"><i class='skit_fa_btn fa fa-server'></i> 添加系统维护菜单组</li>
						<li onclick="addSysCfgMenu();"><i class='skit_fa_btn fa fa-cogs'></i> 添加系统配置菜单组</li>
						<li onclick="addDefaultMenu('系统接口菜单组');"><i class='skit_fa_btn fa fa-assistive-listening-systems'></i> 添加系统接口菜单组</li>
						<li onclick="addDefaultMenu('集群SSH管理菜单');"><i class='skit_fa_btn fa fa-terminal'></i> 添加集群SSH管理菜单</li>
						</ww:if>
					</ul>
				</div>
   			</div>
   		</div>	
	</TD>
	<TD style='width:3px;'/>
	<TD valign='top'>
		<textarea id="xml" style='display:none'>
		</textarea>
		<div id="tabL" style="position:relative;display:none">
			<ul>
				<li><a href="#tabL-editor">菜单可视编辑</a></li>
				<li style='display:none'><a href="#tabL-xml">菜单XML预览</a></li>
				<li><a href="#tabL-xmleditor">菜单XML编辑</a></li>
				<li style='display:none'><a href="#tabL-icons">可选图标</a></li>
			</ul>
			<!-- 
			<div style='position:absolute;right:8px;top:10px;'>
				<button type="button" class="btn btn-outline btn-primary btn-xs" onclick='openView("模块子系统配置", "cmpcfg!query.action");'>
   					<i class='fa fa-plus-circle'></i> 打开模块子系统配置</button>
			</div>
			 -->
			<div id="tabL-editor" class='skit_view_div'>
	            <div class="user-panel" id='divToolbarProfile' style='display:none;margin-bottom:20px;'>
	                <div class="text-center">
	                    <img src="images/cmp/83.png" alt="">
	                    <span>系统工具栏</span>
	                </div>
	            </div>
	            <div class="panel panel-default" id='moduleProfile' style='display:none'>
	    			<div class="panel-heading">配置模块子系统缺省入口URL</div>
	    			<div class="panel-body">
	    				<div class="form-group">
	        				<div class="input-group">
	        					<div class="input-group-btn">
	                                <button type="button" class="btn btn-info dropdown-toggle" data-toggle="dropdown" aria-expanded="false"
	                                	style='display: inline-block;height:34px;'><span class="caret"></span></button>
	                                <ul class="dropdown-menu pull-left" style='width:60px'>
	                                    <li><a onclick='changePrefix("http://", "defaultHrefPrefix");'>http://</a></li>
	                                    <li><a onclick='changePrefix("https://", "defaultHrefPrefix");'>https://</a></li>
	                                    <li><a onclick='changePrefix("", "defaultHrefPrefix");'>n/a</a></li>
	                                </ul>
	                            </div>
								<span class="input-group-addon" id='defaultHrefPrefix' style='display:none'></span>
								<input class="form-control" placeholder="请输入模块子系统缺省入口访问URL地址" type="text" id='defaultHref'>
	        					<div class="input-group-btn">
	        						<button type="button" class="btn btn-info dropdown-toggle" data-toggle="dropdown" aria-expanded="false" style='display: inline-block;height:34px;'><span class="caret"></span></button>
	                                <ul class="dropdown-menu pull-right">
	                                    <li><a onclick='document.getElementById("defaultHref").value="";'>取消模块子系统缺省URL</a></li>
	                                    <li><a onclick='previewURL()'>预览URL</a></li>
	                                </ul>	
	        					</div>
							</div>
						</div>
	    				<div class="form-group">
	        				<div class="input-group">
								<span class="input-group-addon">视图打开方式</span>
								<select class="form-control" id='defaultHrefTarget'>
									<option value='' selected>缺省  上表URL在主界面框架下打开</option>
								  	<option value='_blank'>上表URL在浏览器新视图窗口打开</option>
								</select>
							</div>
						</div>
	    			</div>
	    		</div>
	            <div class="panel panel-default" id='menuProfile' style='display:none'>
	    			<div class="panel-heading">配置模块子系统菜单
	                    <div style='float:right;right:4px;top:0px;' id='divCancelAddMenu'>
		    				<button type="button" class="btn btn-outline btn-danger btn-xs" onclick='cancelAddMenu()'>
		    					<i class='fa fa-plus-circle'></i> 取消新增</button>
	                    </div>
	    			</div>
	    			<div class="panel-body">
	    				<div class="form-group" id='menuNameForm'>
	        				<div class="input-group">
								<span class="input-group-addon">菜单名称<span style='margin-right:16px;border: 0px solid red;'>*</span></span>
								<input class="form-control" placeholder="请输入菜单名称不超过16个字" type="text" id='menuName'>
							</div>
						</div>
	    				<div class="form-group" id='menuIconForm'>
	        				<div class="input-group">
								<span class="input-group-addon">菜单图标<span id='menuIconPreview' class='fa fa-photo' style='margin-left:12px'></span></span>
								<input class="form-control" placeholder="请点击右侧按钮选择菜单图标" type="text" id='menuIcon' readonly='true'>
	        					<div class="input-group-btn">
	        						<button type="button" class="btn btn-info" onclick='selectIcon()' 
	        							style='display: inline-block;height:34px;'><span class="caret"></span></button>
	        					</div>
							</div>
						</div>
	    				<div class="form-group" id='menuHrefForm'>
	        				<div class="input-group">
	        					<div class="input-group-btn">
	                                <button type="button" class="btn btn-info dropdown-toggle" data-toggle="dropdown" aria-expanded="false"
	                                	style='display: inline-block;height:34px;'><span class="caret"></span></button>
	                                <ul class="dropdown-menu pull-left" style='width:60px'>
	                                    <li><a onclick='changePrefix("http://");'>http://</a></li>
	                                    <li><a onclick='changePrefix("https://");'>https://</a></li>
	                                    <li><a onclick='changePrefix("");'>n/a</a></li>
	                                </ul>
	                            </div>
								<span class="input-group-addon" id='menuHrefPrefix'>http://</span>
								<input class="form-control"
									data-title='请输入正确的菜单URL地址, 不允许为空; 不输入或者输入#加字母数字则表示配置菜单目录'
									data-toggle="tooltip" 
									data-placement="top"
									data-trigger='manual'
									onkeydown='$("#menuHref").tooltip("hide")'
									placeholder="请输入菜单URL地址；不输入或者输入#加字母数字则表示配置菜单目录"
									type="text"
									id='menuHref'
									onblur='return checkMenuURL(this);'>
	        					<div class="input-group-btn">
	        						<button type="button" class="btn btn-info dropdown-toggle" data-toggle="dropdown" aria-expanded="false" style='display: inline-block;height:34px;'><span class="caret"></span></button>
	                                <ul class="dropdown-menu pull-right">
	                                    <li><a onclick='previewURL()'>预览URL</a></li>
	                                    <li><a onclick='deleteURL()'>清空URL</a></li>
	                                </ul>	
	        					</div>
							</div>
						</div>
	    				<div class="form-group" id='menuTargetForm'>
	        				<div class="input-group">
								<span class="input-group-addon">视图打开方式</span>
								<select class="form-control" id='menuTarget'>
									<option value='' selected>缺省  上表URL在主界面框架下打开</option>
								  	<option value='_blank'>上表URL在浏览器新视图窗口打开</option>
								</select>
								<!-- 
								<select style="width:100%" id="select2" tabindex="-1" class="select2-hidden-accessible" aria-hidden="true">
			                       <optgroup label="Alaskan/Hawaiian Time Zone">
			                           <option value="AK">Alaska</option>
			                           <option value="HI">Hawaii</option>
			                       </optgroup>
			                       <optgroup label="Pacific Time Zone">
			                           <option value="CA">California</option>
			                       </optgroup>
			                       <optgroup label="Mountain Time Zone">
			                           <option value="AZ">Arizona</option>
			                       </optgroup>
			                    </select>
								<span class="select2 select2-container select2-container--default select2-container--below" dir="ltr" style="width: 100%;"><span class="selection"><span class="select2-selection select2-selection--single" role="combobox" aria-haspopup="true" aria-expanded="false" tabindex="0" aria-labelledby="select2-select2-container"><span class="select2-selection__rendered" id="select2-select2-container" title="Alaska">Alaska</span><span class="select2-selection__arrow" role="presentation"><b role="presentation"></b></span></span></span><span class="dropdown-wrapper" aria-hidden="true"></span></span>
								 -->
							</div>
						</div>
	    			</div>
	    		</div>
	            <div class="panel panel-default" id='buttonProfile' style='display:none'>
	    			<div class="panel-heading">
	    				配置菜单页面权限按钮(主界面框架可控制页面按钮权限)
	    				<div style='float:right;right:4px;top:0px;'>
		    				<button type="button" class="btn btn-outline btn-primary btn-xs" onclick='addMenuButton()'>
		    					<i class='fa fa-plus-circle'></i> 新增权限按钮</button>
	    				</div>
    				</div>
	    			<div class="panel-body" id='menuButtonPanel'>
	    			<!-- 
	    				<div class="form-group">
	        				<div class="input-group">
								<span class="input-group-addon"><i class='fa fa-key'></i>菜单权限按钮</span>
								<input class="form-control" type="text" value=''>
	        					<div class="input-group-btn">
	        						<button type="button" class="btn btn-danger" onclick='delMenuButton()' 
	        							style='display: inline-block;height:34px;'><span class="caret"></span></button>
	        					</div>
							</div>
						</div>
	    			 -->
		    		</div>
	    		</div>
                <div class="col-xs-12 text-center">
                    <div class="col-xs-12 col-sm-10 emphasis" style='width:100%;border:0px solid red' id='divSave'>
                		<button type="button" class="btn btn-outline btn-primary btn-block" onclick='doSave();'><i class="fa fa-save"></i> 保存 </button>
                    </div>
                </div>
				<div class="user-panel" style='display:none'>
	                <div class="text-center">
	                    <img src="images/cmp/8.png" alt="">
	                    <span id='spanModuleName'>未知</span>
	                </div>
	            </div>
			</div>
			<div id="tabL-xml">
				<div>
					<div>
				<iframe name='xmlPreview' id='xmlPreview' frameborder='0' style='width:100%'></iframe>
					</div>
				</div>
			</div>
			<div id="tabL-xmleditor">
				<div>
					<div>
					<iframe name='xmlEdit' id='xmlEdit' frameborder='0' style='width:100%'></iframe>
					</div>
				</div>
			</div>
			<div id="tabL-icons" class='skit_view_div' style='dispaly:none'>
	            <div class="panel panel-default">
	    			<div class="panel-heading">Web应用图标</div>
	                <div class="panel-body">
	               		<div class='panel-icon'><%@ include file="../../skit/inc/skit_iconfa_webapp.inc"%></div>
	                </div>
	    		</div>
	            <div class="panel panel-default">
	    			<div class="panel-heading">交通工具图标</div>
	                <div class="panel-body">
						<div class='panel-icon'><%@ include file="../../skit/inc/skit_iconfa_transportation.inc"%></div>
	                </div>
	    		</div>
	            <div class="panel panel-default">
	    			<div class="panel-heading">性别图标</div>
	                <div class="panel-body">
						<div class='panel-icon'><%@ include file="../../skit/inc/skit_iconfa_gender.inc"%></div>
	                </div>
	    		</div>
	            <div class="panel panel-default">
	    			<div class="panel-heading">Spinner图标</div>
	                <div class="panel-body">
						<div class='panel-icon'><%@ include file="../../skit/inc/skit_iconfa_spinner.inc"%></div>
	                </div>
	    		</div>
	            <div class="panel panel-default">
	    			<div class="panel-heading">表单控件图标</div>
	                <div class="panel-body">
						<div class='panel-icon'><%@ include file="../../skit/inc/skit_iconfa_formcontrol.inc"%></div>
	                </div>
	    		</div>
	            <div class="panel panel-default">
	    			<div class="panel-heading">支付图标</div>
	                <div class="panel-body">
						<div class='panel-icon'><%@ include file="../../skit/inc/skit_iconfa_payment.inc"%></div>
	                </div>
	    		</div>
	            <div class="panel panel-default">
	    			<div class="panel-heading">对话图标</div>
	                <div class="panel-body">
						<div class='panel-icon'><%@ include file="../../skit/inc/skit_iconfa_chart.inc"%></div>
	                </div>
	    		</div>
	            <div class="panel panel-default">
	    			<div class="panel-heading">货币图标</div>
	                <div class="panel-body">
						<div class='panel-icon'><%@ include file="../../skit/inc/skit_iconfa_currency.inc"%></div>
	                </div>
	    		</div>
	            <div class="panel panel-default">
	    			<div class="panel-heading">文本编辑图标</div>
	                <div class="panel-body">
						<div class='panel-icon'><%@ include file="../../skit/inc/skit_iconfa_texteditor.inc"%></div>
	                </div>
	    		</div>
	            <div class="panel panel-default">
	    			<div class="panel-heading">方向图标</div>
	                <div class="panel-body">
						<div class='panel-icon'><%@ include file="../../skit/inc/skit_iconfa_directional.inc"%></div>
	                </div>
	    		</div>
	            <div class="panel panel-default">
	    			<div class="panel-heading">视频播放图标</div>
	                <div class="panel-body">
						<div class='panel-icon'><%@ include file="../../skit/inc/skit_iconfa_videoplayer.inc"%></div>
	                </div>
	    		</div>
	            <div class="panel panel-default">
	    			<div class="panel-heading">公司图标</div>
	                <div class="panel-body">
						<div class='panel-icon'><%@ include file="../../skit/inc/skit_iconfa_brand.inc"%></div>
	                </div>
	    		</div>
	            <div class="panel panel-default">
	    			<div class="panel-heading">医学图标</div>
	                <div class="panel-body">
						<div class='panel-icon'><%@ include file="../../skit/inc/skit_iconfa_medical.inc"%></div>
	                </div>
	    		</div>
			</div>
		</div>
	</TD>
</TR>
</TABLE>
<form id='form0'>
<input type='hidden' name='sysid' value="<ww:property value='sysid'/>"/>
<iframe name='iDownload' id='iDownload' style='display:none'></iframe>
<textarea name='editorContent' style='display:none' ><ww:property value="xmlEdit" escape='false'/></textarea>
</form>
<div class="panel panel-primary" id='divUploadfile' style='z-index:1000; top: 100; left: 100; width:600px; display:none; position: absolute; '>
    <div class="panel-heading">
    	<span>从本地磁盘选择上传菜单配置的XML文件</span>
        <div class="panel-menu">
            <button type="button" onclick='closeuploadfile()' data-action="close" class="btn btn-warning btn-action btn-xs"><i class="fa fa-times"></i></button>
        </div>
    </div>
    <div class="panel-body" style="display: block;">
		<input type="file" name="uploadfile" id="uploadfile" class="file-loading"/>
    </div>
</div>
</body>
<SCRIPT type="text/javascript">
/*实现窗口对齐*/
function resizeWindow()
{
	var div = document.getElementById( 'divTree' );
	div.style.height = windowHeight - 32;
	div = document.getElementById( 'tabL' );
	div.style.height = windowHeight - 2;
	div.style.width = windowWidth - 366;
	div = document.getElementById( 'tabL-editor' );
	div.style.height = windowHeight - 60;
	div = document.getElementById( 'xmlEdit' );
	div.style.height = windowHeight - 60;
	div.style.width = windowWidth - 400;
	//div = document.getElementById( 'tabL-xml' );
	//div.style.height = windowHeight - 100;
}
</SCRIPT>
<%@ include file="../../skit/inc/skit_bootstrap.inc"%>
<%@ include file="../../skit/inc/skit_cos.inc"%>
<%@ include file="../../skit/inc/skit_ztree.inc"%>
<SCRIPT type="text/javascript">
$("#tabL").tabs({
    select: function(event, ui) {
		if( ui.panel.id == "tabL-xmleditor" )
		{
		}
    }
});
document.getElementById( 'tabL' ).style.display = "";
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
var timestamp = <ww:property value='timestamp'/>;
var sysid = "<ww:property value='sysid'/>";
var sysname = "<ww:property value='sysname'/>";
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
			beforeRemove: beforeRemove,
			beforeExpand: beforeExpand,
			onExpand: onExpand,
			beforeDrag: beforeDrag,
			beforeDrop: beforeDrop,
			beforeDragOpen: beforeDragOpen,
			onDrag: onDrag,
			onDrop: onDrop,
		},
		edit: {
			drag: {
				autoExpandTrigger: true,
				prev: dropPrev,
				inner: dropInner,
				next: dropNext
			},
			enable: true,
			showRemoveBtn: true,
			showRenameBtn: false
		},
		view: {
			addHoverDom: addHoverDom,
			removeHoverDom: removeHoverDom,
			addDiyDom: addDiyDom
		}
	};

	function beforeDrop(treeId, treeNodes, targetNode, moveType, isCopy) {
		if( targetNode.nomenu && moveType != "inner"  ) return false;
		return true;
	}
	
	function beforeRemove(e, treeId, treeNode)
	{
		var node = myZtree.getSelectedNodes()[0];
		if( node.newMenu )
		{
			return true;
		}
		delMenu();
		return false;
	}
	
	function onRightClick(event, treeId, treeNode) {
		if( addModulesNode(treeNode) )
		{
			return;
		}
		if (!treeNode && event.target.tagName.toLowerCase() != "button" && $(event.target).parents("a").length == 0) {
			myZtree.cancelSelectedNode();
			showRMenu("root", event.clientX, event.clientY);
		} else if (treeNode && !treeNode.noR) {
			myZtree.selectNode(treeNode);
			showRMenu("node", event.clientX, event.clientY);
		}
	}
	
	function onClick(event, treeId, treeNode)
	{
		if( addModulesNode(treeNode) )
		{
			return;
		}
		//myZtree.expandNode(treeNode, null, null, null, true);
		selectTreeNode(treeNode);//myZtree.getSelectedNodes()[0];
		myZtree.expandNode(treeNode, true, false);
		setUserActionMemory("menu!config", treeNode.id);
	}

	function addHoverDom(treeId, treeNode) {
		if( treeNode.newMenu || !treeNode.path ) return;
		var sObj = $("#" + treeNode.tId + "_span");
		if ( $("#addBtn_"+treeNode.tId).length>0) return;
		var addStr = "<span class='button add' id='addBtn_" + treeNode.tId+ "' title='新增菜单' onfocus='this.blur();'></span>";
		sObj.after(addStr);
		var btn = $("#addBtn_"+treeNode.tId);
		if (btn) btn.bind("click", function(){
			addMenu();
		});
	};
	function removeHoverDom(treeId, treeNode) {
		$("#addBtn_"+treeNode.tId).unbind().remove();
	};
	
	function addDiyDom(treeId, treeNode) {
		var editStr = "";
		if( treeNode.ico )
		{
			editStr = "<i class='fa "+treeNode.ico+"' id='ico_fa_menu_" + treeNode.tId + "'></i>";
			
		}
		if( treeNode.warn )
		{
			editStr += "<i class='skit_fa_icon_war fa fa-exclamation-triangle' id='ico_fa_menu_warn_" + treeNode.tId + "'></i>";
		}
		if( editStr )
		{
			var aObj = $("#" + treeNode.tId + "_a");
			if( aObj ) aObj.after(editStr);
		}
	}
	
	$(document).ready(function(){
		try
		{
			var json = <ww:property value="jsonData" escape="false"/>;
			initSelectDefaultTreeNode(JSON.stringify(json));
			var id = getUserActionMemory("menu!config");
			if( id )
			{
				var node = myZtree.getNodeByParam("id", id);
				if( node )
				{
					myZtree.selectNode(node);
					myZtree.expandNode(node, true);
				}
			}
		}
		catch(e)
		{
			skit_alert("初始化后台菜单导航树异常"+e.message+", 行数"+e.lineNumber);
		}
	});

	function initSelectDefaultTreeNode(json)
	{
		if( !json ) return;
		if( myZtree ) myZtree.destroy();
		var zNodes = jQuery.parseJSON(json);
		myZtree = $.fn.zTree.init($("#myZtree"), setting, zNodes);
		if( !myZtree ){
			skit_alert("初始化后台菜单导航树失败: "+json);
			return;
		}
		myZtreeMenu = $("#rMenu");
		var nodes = myZtree.getNodes();
		if( treeIdAddModulesNode ) {
			for(var i = 0; i < nodes.length; i++ ) {
				if( nodes[i].id == treeIdAddModulesNode ) {
					myZtree.selectNode(nodes[i]);
					selectTreeNode(nodes[i]);
					if( nodes[i].id == "Sys" )
					{
						addDefaultMenu("系统管理缺省权限菜单");
					}
					return;
				}
			}
			treeIdAddModulesNode = "";
		}
		else if( tIdOper )
		{
			var node = myZtree.getNodeByTId(tIdOper);
			myZtree.selectNode(node);
			selectTreeNode(node);
			tIdOper = "";
		}
		else
		{
			var node = myZtree.getNodeByParam("id", sysid);
			myZtree.selectNode(node);
			selectTreeNode(node);
			tIdOper = "";
		}
		var nodeNoInit;
		for(var i = 0; i < nodes.length; i++ )
		{
			if( !nodes[i].path )
			{
				if( nodes[i].id == "Sys" )
				{
					addModulesNode(nodes[i]);
					return;
				}
				nodeNoInit = nodes[i];
			}
			else if( !nodes[i]["children"] )
			{
				if( nodes[i].id == "Sys" )
				{
					myZtree.selectNode(nodes[i]);
					selectTreeNode(nodes[i]);
					addDefaultMenu("系统管理缺省权限菜单");
					return;
				}
			}
		}
		if( nodeNoInit )
		{
			skit_confirm("模块子系统【"+nodeNoInit.name+"】菜单组还未初始化，是否立刻初始化？", function(yes){
				if( yes )
				{
					addModulesNode(nodeNoInit);
				}
			});
		}
		else
		{
			<ww:if test='editable'>
			skit_confirm("模块子系统菜单还未正式发布，是否立刻发布？", function(yes){
				if( yes )
				{
					top.openView("模块子系统菜单配置发布", "menus!publish.action");
				}
			});
			</ww:if>
			var newMenuUrl = "<ww:property value='menuUrl' escape='false'/>";
			var newMenuName = "<ww:property value='menuName' escape='false'/>";
			if( newMenuUrl )
			{
				skit_confirm("有新的菜单【"+newMenuName+"】可发布，访问地址是["+newMenuUrl+"]，您是否同意发布？", function(yes){
					if( yes )
					{
						
					}
				});
			}
		}
	}	
	//#########################################################################
	function addMenu()
	{
		hideRMenu();
		var node = myZtree.getSelectedNodes()[0];
		var newNode = new Object();
		for( var key in node )
		{
			if( key == "children" ||
				key == "level" ||
				key == "tId" ||
				key == "nomenu" ||
				key == "parentTId" ||
				key.indexOf("check") == 0 ||
				key.indexOf("chk") == 0 ||
				key.indexOf("get") == 0 ||
				key == "editNameStatus" ||
				key == "isAjaxing" ||
				key == "isFirstNode" ||
				key == "isLastNode" ||
				key == "open" ||
				key == "isParent" ||
				key == "halfCheck" ||
				key == "zAsync" ||
				key == "editNameFlag" ||
				key == "nocheck" ||
				key == "chkDisabled" ||
				key == "chkDisabled" ||
				key == "isParent" ||
				key == "isHover" 
				) continue;
			newNode[key] = node[key];
		}
		//showObject(newNode);
		newNode.ico = "skit_fa_icon_blue fa fa-plus-circle";
		newNode.name = newNode.name+"的子菜单";
		newNode.href = "";
		newNode["newMenu"] = true;
		newNode["path"] = node.path;
		document.getElementById( "menuHref" ).value = "";
		document.getElementById( "menuName" ).focus();
		var nodes = myZtree.addNodes(node, newNode);
		myZtree.selectNode(nodes[0], false, false);
		selectTreeNode(newNode);
	}
	
	function cancelAddMenu()
	{
		var node = myZtree.getSelectedNodes()[0];
		var parent = node.getParentNode();
		myZtree.removeNode(node);
		parent["isParent"] = true;
		document.getElementById( parent.tId + "_ico").className = "button ico_open";
		myZtree.selectNode(parent);
		selectTreeNode(parent);
	}
	
	function changePrefix(prefix, span)
	{
		if( !span ) span = "menuHrefPrefix";
		if( prefix == "http://" )
		{
			document.getElementById( span ).innerHTML = "http://";
			document.getElementById( span ).style.display = "";
		}
		else if( prefix == "https://" )
		{
			document.getElementById( span ).innerHTML = "https://";
			document.getElementById( span ).style.display = "";
		}
		else
		{
			document.getElementById( span ).innerHTML = "";
			document.getElementById( span ).style.display = "none";
		}
	}
	
	function delMenuButton(id)
	{
		var div = document.getElementById(id);
		if( div )
		{
			document.getElementById( "menuButtonPanel" ).removeChild(div);
		}
	}
	
	function deleteURL()
	{
		document.getElementById( "menuTargetForm" ).style.display = "none";
		document.getElementById( "buttonProfile" ).style.display = "none";
		document.getElementById( "menuHref" ).value = "";
	}
	
	function previewURL()
	{
		var name = document.getElementById( "menuName" ).value
		var href = document.getElementById( "menuHref" ).value;
		if( href != "" && href.charAt(0) == '#' )
		{
			skit_alert("该菜单配置为了菜单分组目录，因此不能被预览");
			return;
		}

		var prefix = document.getElementById( "menuHrefPrefix" ).innerHTML;
		if( prefix != "" )
		{
			href = prefix + href;
		}
		if( href.indexOf("://") == -1)
		{
			href = "<%=Kit.URL(request)%>"+href;
		}
		var target = document.getElementById( "menuTarget" ).value;
		openView("预览菜单配置【"+name+"】", href, target);
	}
	
	function selectIcon()
	{
		var SM = new SimpleModal({"btn_cancel":"关闭","width":660});
	    var content = "<div id='divMenuIcons' style='height:400px;width:620px;border:0px solid red;overflow-y:auto;'>"+document.getElementById( 'tabL-icons' ).innerHTML+"</div>"
	    SM.show({
	    	"title":"选择菜单图标",
	    	"contents": content
	    });
	    $( '#divMenuIcons' ).niceScroll({cursorcolor: '#fff',railalign: 'right', cursorborder: "none", horizrailenabled: false, zindex: 2001, left: '245px', cursoropacitymax: 0.6, cursorborderradius: "0px", spacebarenabled: false });
	    $(".icon-sel").bind("click", function (event) {
	    	//showObject(evt);
	    	var obj = event.srcElement ? event.srcElement : event.target; 
	    	if( obj.tagName == "A" )
	    	{
	    		obj = obj.firstChild;
	    		var cl = obj.className;
	    		document.getElementById( "menuIconPreview" ).className = "skit_fa_icon_blue "+cl;
	    		document.getElementById( "menuIconPreview" ).style.marginLeft = 12;
	    		document.getElementById( "menuIcon" ).value = cl.substring(3);
	    		/*var cl = obj.innerHTML;
	    		var i = cl.indexOf("class");
	    		cl = cl.substring(i+2);*/
	    	}
	    	else if( obj.tagName == "I" )
	    	{
	    		var cl = obj.className;
	    		document.getElementById( "menuIconPreview" ).className = "skit_fa_icon_blue "+cl;
	    		document.getElementById( "menuIconPreview" ).style.marginLeft = 12;
	    		document.getElementById( "menuIcon" ).value = cl.substring(3);
	    	}
	    	if( SM )
	    	{
	    		SM.hide();
	    	}
	    });
	}
	
	var inputMenuButtonName = document.createElement("input");
	inputMenuButtonName.id = "inputMenuButtonName";
	var inputMenuButtonId = document.createElement("input");
	inputMenuButtonId.id = "inputMenuButtonId";
	function addMenuButton()
	{
		var div0 = document.createElement("div");
		div0.className = "panel panel-default";
		var div1 = document.createElement("div");
		div1.className = "panel-body";
		div0.appendChild(div1);
		
		var div2 = document.createElement("div");
		div2.className = "form-group";
		div1.appendChild(div2);
		var div3 = document.createElement("div");
		div3.className = "input-group";
		div2.appendChild(div3);
		var span = document.createElement("span");
		span.className = "input-group-addon";
		span.innerHTML = "<i class='fa fa-key'></i>按钮名称";
		div3.appendChild(span);
		inputMenuButtonName.className = "form-control";
		inputMenuButtonName.type = "text";
		inputMenuButtonName.placeholder = "请输入按钮名称不超过6个字";
		div3.appendChild(inputMenuButtonName);

		div2 = document.createElement("div");
		div2.className = "form-group";
		div1.appendChild(div2);
		div3 = document.createElement("div");
		div3.className = "input-group";
		div2.appendChild(div3);
		span = document.createElement("span");
		span.className = "input-group-addon";
		span.innerHTML = "<i class='fa fa-key'></i>按钮标识";
		div3.appendChild(span);
		inputMenuButtonId.className = "form-control";
		inputMenuButtonId.type = "text";
		inputMenuButtonId.placeholder = "请输入按钮唯一标识必须是字母数字以及符号";
		div3.appendChild(inputMenuButtonId);

		var SM = new SimpleModal({"btn_ok":"添加按钮","btn_cancel":"取消","width":480});
	    var content = "<div style='height:160px;width:440px;border:0px solid red;overflow-y:auto;'>"+div0.outerHTML+"</div>"
	    SM.show({
	    	"title":"添加菜单权限按钮",
	        "model":"confirm",
	        "callback": function(){
	        	inputMenuButtonName = document.getElementById(inputMenuButtonName.id)
	        	var name = inputMenuButtonName.value;
	        	inputMenuButtonId = document.getElementById(inputMenuButtonId.id);
	        	var id = inputMenuButtonId.value;
	        	if( name == "" )
				{
	        		skit_errtip("必须设置菜单权限按钮的名称.", inputMenuButtonName);
			        return;
				}
	        	if( !name.match(new RegExp("^[a-zA-Z0-9_\u4e00-\u9fa5]+$")) )
	            {
	        		skit_errtip("菜单权限按钮名称只能含有汉字、数字、字母、下划线不能以下划线开头和结尾", inputMenuButtonName);
	    	        return false;
	            }
	    		if( name.length > 16 )
	            {
	    			skit_errtip("菜单权限按钮名称不超过16个字", inputMenuButtonName);
	    	        return false;
	            }
				if( id == "" )
				{
					skit_errtip("必须设置菜单权限按钮的唯一标识.", inputMenuButtonId);
			        return false;
				}
		        if( !id.match(new RegExp("^([A-Za-z])|([a-zA-Z0-9])|([a-zA-Z0-9])|([a-zA-Z0-9_])+$")) )
		        {
		        	skit_errtip("菜单权限按钮标识必须是字母数字以及下划线符号", inputMenuButtonId);
			        return false;
		        }
				if( id.length > 32 )
		        {
					skit_errtip("菜单权限按钮标识不超过32个字", inputMenuButtonId);
			        return false;
		        }
	        	appendMenuButton(name, id);
	        	return true;
	        },
	        "cancelback": function(){
	        },
	    	"contents": content
	    });
	}

	function selectTreeNode(node)
	{
		if( node.type == "module" )
		{
			document.getElementById( "divToolbarProfile" ).style.display = "none";

			document.getElementById( "menuProfile" ).style.display = "none";
			document.getElementById( "buttonProfile" ).style.display = "none";
			document.getElementById( "moduleProfile" ).style.display = "";
			var href = node["default"];
			if( href )
			{
				var prefix = "";
				var i = href.indexOf("http://");
				if( i == -1 ) i = href.indexOf("https://");
				if( i == 0 ) 
				{
					i = href.indexOf("://");
					prefix = href.substring(0, i+3);
					href = href.substring(i+3);
				}
				document.getElementById( "defaultHref" ).value = href;
				changePrefix(prefix, "defaultHrefPrefix");
			}
			else
			{
				document.getElementById( "defaultHref" ).value = "";
				changePrefix("", "defaultHrefPrefix");	
			}
		}
		else if( node.type == "toolbar" )
		{
			document.getElementById( "divSave" ).style.display = "none";
			document.getElementById( "divToolbarProfile" ).style.display = "";
			document.getElementById( "menuProfile" ).style.display = "none";
			document.getElementById( "moduleProfile" ).style.display = "none";
			document.getElementById( "buttonProfile" ).style.display = "none";
		}
		if( node.nomenu )
		{
			return;
		}
		document.getElementById( "divSave" ).style.display = "";
		document.getElementById( "moduleProfile" ).style.display = "none";
		document.getElementById( "menuProfile" ).style.display = "";
		document.getElementById( "menuNameForm" ).style.display = "";
		document.getElementById( "menuIconForm" ).style.display = "";
		//var id = node.id;
		//if( !id ) id = "";
		var i = 0;//id.lastIndexOf(' ');
		//id = id.substring(i+1);
		document.getElementById( "menuName" ).value = node.name;
		var href = node.href;
		var prefix = "";
		i = href.indexOf("http://");
		if( i == -1 ) i = href.indexOf("https://");
		if( i == 0 ) 
		{
			i = href.indexOf("://");
			prefix = href.substring(0, i+3);
			href = href.substring(i+3);
		}
		changePrefix(prefix);
		document.getElementById( "menuHref" ).value = href;
		document.getElementById( "menuTargetForm" ).style.display = "";
		if( prefix != "" )
		{
			var target = "";
			if( node.target )
			{
				target = node.target;
			}
			var options = document.getElementById( "menuTarget" ).options;
			for( var i = 0; i < options.length; i++ )
			{
				if( options[i].value == target )
				{
					options[i].selected = true;
					break;
				} 
			}
		}
		else
		{
			if( href != "" && href.charAt(0) != "#" )	document.getElementById( "buttonProfile" ).style.display = "";
			else
			{
				document.getElementById( "menuTargetForm" ).style.display = "none";		
			}
		}
		if( node.newMenu )
		{
			document.getElementById( "divCancelAddMenu" ).style.display = "";
		}
		else
		{
			document.getElementById( "divCancelAddMenu" ).style.display = "none";
		}
		document.getElementById( "menuButtonPanel" ).innerHTML = "";
		if( node.buttons )
		{
			for( var i = 0; i < node.buttons.length; i++ )
			{
				appendMenuButton(node.buttons[i].name, node.buttons[i].id);
			}
		}
		var icon = node.ico;
		if( node.newMenu )
		{
			document.getElementById( "menuIconPreview" ).className = "skit_fa_icon_blue fa fa-plus-circle";
			document.getElementById( "menuIconPreview" ).style.marginLeft = 12;
			document.getElementById( "menuIcon" ).value = "";
		}
		else
		{
			if( icon )
			{
				document.getElementById( "menuIconPreview" ).className = "fa "+icon;
				document.getElementById( "menuIconPreview" ).style.marginLeft = 12;
				document.getElementById( "menuIcon" ).value = icon;
			}
			else
			{
				document.getElementById( "menuIcon" ).value = "";
				document.getElementById( "menuIconPreview" ).className = "skit_fa_icon_gray fa fa-plus-circle";
				document.getElementById( "menuIconPreview" ).style.marginLeft = 12;
			}
		}
	}
	
	function appendMenuButton(name, id)
	{
		var div = document.createElement("div");
		var divid = "menubtn-"+id;
		div.id = divid
		div.className = "form-group";
		var div1 = document.createElement("div");
		div1.className = "input-group";
		div.appendChild(div1);
		var span = document.createElement("span");
		span.className = "input-group-addon";
		span.innerHTML = "<i class='fa fa-key'></i>菜单权限按钮";
		div1.appendChild(span);
		var input = document.createElement("input");
		input.className = "form-control";
		input.type = "text";
		input.id = "menubtninput-"+id;
		input.readOnly = true;
		input.value = name+" "+id;
		div1.appendChild(input);
		var div2 = document.createElement("div");
		div2.className = "input-group-btn";
		div1.appendChild(div2);
		div2.innerHTML = "<button type='button' class='btn btn-danger dropdown-toggle' data-toggle='dropdown' aria-expanded='false' style='display: inline-block;height:34px;'><span class='caret'></span></button><ul class='dropdown-menu pull-right'><li><a onclick='delMenuButton(\""+divid+"\")'>点击删除权限按钮</a></li></ul>";
		document.getElementById( "menuButtonPanel" ).appendChild(div);
	}

	function doSave()
	{
		var node = myZtree.getSelectedNodes()[0];
		if( node.nomenu )
		{
			setModuleDefault(node)
		}
		else
		{
			setModulesMenu(node);
		}
	}

	function setModuleDefault(node)
	{
		var href = document.getElementById( "defaultHref" ).value;href = href.trim();
		var target = document.getElementById( "defaultHrefTarget" ).value;
		if( href == "" && href.charAt(0) == "#" )
		{
        	skit_errtip("请输入正确的模块子系统缺省URL地址 '"+href+"'", document.getElementById( "defaultHref" ));
			return;
		}
		var prefix = document.getElementById( "defaultHrefPrefix" ).innerHTML;
		var url = href;
		if( prefix != "" )
		{
			href = prefix + href;
			url = href;
		}
		else if( href.indexOf("http") != 0 )
		{
			url = "<%=Kit.URL(request)%>"+href;
		}
        if( node["default"] == href && node.target == target )
		{
			skit_alert("您未做任何修改，无需保存。");
			return;
		}
	  //var regexp = "^(http|https|ftp)\://([a-zA-Z0-9\.\-]+(\:[a-zA-Z0-9\.&amp;%\$\-]+)*@)*((25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9])\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9]|0)\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9]|0)\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[0-9])|localhost|([a-zA-Z0-9\-]+\.)*[a-zA-Z0-9\-]+\.(com|edu|gov|int|mil|net|org|biz|arpa|info|name|pro|aero|coop|museum|[a-zA-Z]{2}))(\:[0-9]+)*(/($|[a-zA-Z0-9\.\,\?\'\\\+&amp;%\$#\=~_\-]+))*$";
        var regexp = "^(http|https|ftp)\://([a-zA-Z0-9\.\-]+(\:[a-zA-Z0-9\.&amp;%\$\-]+)*@)*((25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9])\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9]|0)\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9]|0)\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[0-9])|localhost|([a-zA-Z0-9\-]+\.)*[a-zA-Z0-9\-]+\.(com|edu|gov|int|mil|net|org|biz|arpa|info|name|pro|aero|coop|museum|[a-zA-Z]{2}))(\:[0-9]+)*(/($|[a-zA-Z0-9\.\,\?\!\'\\\+&amp;%\$#\=~_\-]+))*$";
		var tip = "您将设置模块子系统【"+sysname+"】的缺省入口网址【"+url+"】";
		if( !url.match(new RegExp(regexp) ) )
        {
			tip = "您设置的模块子系统【"+sysname+"】的缺省入口网址【"+url+"】可能不正确，您确定要这样设置吗？";
        }
		else
		{
			if( target == "_blank" )
			{
				tip += "，打开方式是在浏览器新视图窗口打开。";
			}
			tip += "，您是否确认修改该配置？";
		}
		skit_confirm(tip, function(yes){
			if( yes )
			{
				skit_showLoading();

				tIdOper = node.tId;
				MenusMgr.setModuleDefault(href, target, timestamp, sysid,sysname, {
					callback:function(rsp) {
						skit_hiddenLoading();
						skit_alert(rsp.message);
						try
						{
							timestamp = rsp.timestamp;
							if( rsp.succeed )
							{
								node["default"] = href;
								node["target"] = target;
								//myZtree.reAsyncChildNodes(node, "refresh");
								myZtree.updateNode(node);
							}
							else if( rsp.result )
							{
								initSelectDefaultTreeNode(rsp.result);
							}
						}
						catch(e)
						{
							alert(e);
						}
					},
					timeout:30000,
					errorHandler:function(err) {skit_hiddenLoading(); alert(err); }
				});
			}
		});
	}

	function checkMenuURL()
	{
		var href = document.getElementById( "menuHref" ).value;
		href = href.trim();
		if( href == "" )
		{
			document.getElementById( "menuTargetForm" ).style.display = "none";
			document.getElementById( "menuHref" ).value = "";
			return;
		}
		if( href != "" && href.charAt(0) == '#' )
		{
			document.getElementById( "menuTargetForm" ).style.display = "none";
			document.getElementById( "menuHrefPrefix" ).innerHTML = "";
			document.getElementById( "menuHrefPrefix" ).style.display = "none";
		}
		else
		{
			document.getElementById( "menuTargetForm" ).style.display = "";
			//document.getElementById( "menuHrefPrefix" ).style.display = "";
		}
	}

	function setModulesMenu(node)
	{
		var name = document.getElementById( "menuName" ).value;name = name.trim();
		var href = document.getElementById( "menuHref" ).value;href = href.trim();
		var icon = document.getElementById( "menuIcon" ).value;
		var target = document.getElementById( "menuTarget" ).value;
		
        if( !name.match(new RegExp("^[a-zA-Z0-9_.@【】\u4e00-\u9fa5]+$")) )
        {
        	skit_errtip("菜单名称只能含有汉字、数字、字母、下划线等特殊符号不能以下划线开头和结尾", document.getElementById( "menuName" ));
	        return;
        }
        
		if( name.length > 16 )
        {
			skit_errtip("菜单名称不超过16个字", document.getElementById( "menuName" ));
	        return;
        }
		var tip = "";
		var prefix = document.getElementById( "menuHrefPrefix" ).innerHTML;
		if( href != "" && href.charAt(0) != "#" && href.charAt(0) != "%" )
		{
			var url = "<%=Kit.URL(request)%>"+href;
			if( prefix != "" )
			{
				href = prefix + href;
				url = href;
			}
		  //var regexp = "^(http|https|ftp)\://([a-zA-Z0-9\.\-]+(\:[a-zA-Z0-9\.&amp;%\$\-]+)*@)*((25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9])\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9]|0)\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9]|0)\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[0-9])|localhost|([a-zA-Z0-9\-]+\.)*[a-zA-Z0-9\-]+\.(com|edu|gov|int|mil|net|org|biz|arpa|info|name|pro|aero|coop|museum|[a-zA-Z]{2}))(\:[0-9]+)*(/($|[a-zA-Z0-9\.\,\?\'\\\+&amp;%\$#\=~_\-]+))*$";
	        var regexp = "^(http|https|ftp)\://([a-zA-Z0-9\.\-]+(\:[a-zA-Z0-9\.&amp;%\$\-]+)*@)*((25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9])\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9]|0)\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9]|0)\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[0-9])|localhost|([a-zA-Z0-9\-]+\.)*[a-zA-Z0-9\-]+\.(com|edu|gov|int|mil|net|org|biz|arpa|info|name|pro|aero|coop|museum|[a-zA-Z]{2}))(\:[0-9]+)*(/($|[a-zA-Z0-9\.\,\?\!\'\\\+&amp;%\$#\=~_\-]+))*$";
			if( !url.match(new RegExp(regexp) ) )
	        {
				tip = "您设置的菜单项入口URL地址【"+url+"】可能不正确，您确定要这样设置吗？";
	        }
		}
		else if( href == "" && prefix != "")
		{
    		document.getElementById( "menuHref" ).focus();
    		$("#menuHref").tooltip("show");
			//skit_errtip("请输入菜单URL地址 ，不允许为空", document.getElementById( "menuHref" ));
	        return;
		}
		else if( href != "" && href.charAt(0) == "#"  )
		{
			if( href.length > 16 )
			{
				skit_errtip("菜单唯一标识长度不能超过16个字节", document.getElementById( "menuHref" ));
				return;
			}
		}
		var menubtninputs = $("input[id^='menubtninput-']");
		var buttons = new Array();
		for(var i = 0; i < menubtninputs.length; i+=1 )
		{
			buttons.push(menubtninputs[i].value);
		}
//alert(name+"("+node.name+"),"+href+"("+node.href+"),"+icon+"("+node.ico+"),"+target+"("+node.target+")");
		if( node.name == name && node.href == href && node.ico == icon && node.target == target && buttons.length == 0 )
		{
			skit_alert("您未做任何修改，无需保存。");
			return;
		}
		if( !tip )
		{
			tip = "您设置的菜单项是【系统工具栏】，菜单名称【"+name+"】"
			
			if( node.type == "module" )
			{
				tip = "您设置的菜单项是模块子系统【"+sysname+"】，菜单名称【"+name+"】";
			}
			if( icon != "" )
			{
				tip += "，选择配置的图标是<i class='fa "+icon+"'></i>";
			}
			if( href.charAt(0) == '#' )
			{
				tip += "，配置的是菜单目录唯一标识是"+href;
			}
			else if( href == "" )
			{
				tip += "，配置的是菜单目录将自动创建唯一标识";
			}
			else
			{
				tip += "，菜单URL地址【"+href+"】";
			}
			if( buttons.length > 0 )
			{
				tip += "，该菜单页面配置了"+buttons.length+"个按钮";
			}
			if( node.newMenu )
			{
				tip += "，您是否确认新增该配置？";
			}
			else
			{
				tip += "，您是否确认修改该配置？";
			}
		}
		skit_confirm(tip, function(yes){
			if( yes )
			{
				skit_showLoading();
				MenusMgr.setModulesMenu(
						node.newMenu?true:false,
						node.path,
						name,
						href,
						icon, 
						target,
						buttons,
						timestamp,
						sysid,
						sysname, {
					callback:function(rsp) {
						skit_hiddenLoading();
						try
						{
							tIdOper = node.tId;
							timestamp = rsp.timestamp;
							if( rsp.succeed )
							{
								var ico_fa_menu = document.getElementById( "ico_fa_menu_" + node.tId);
								if( icon != "" )
								{
									node["ico"] = "fa "+icon;
									document.getElementById( "menuIconPreview" ).className = "fa "+node.ico;
									if( ico_fa_menu )
										document.getElementById( "ico_fa_menu_" + node.tId + "" ).className = node.ico;
									else
									{
										var aObj = $("#" + node.tId + "_a");
										if( aObj ) aObj.after("<i class='fa "+node.ico+"' id='ico_fa_menu_" + node.tId + "'></i>");
									}
								}
								else
								{
									node["ico"] = "";
									document.getElementById( "menuIconPreview" ).className = "skit_fa_icon_gray fa fa-plus-circle";
									if( ico_fa_menu ) ico_fa_menu.parentNode.removeChild(ico_fa_menu);
								}
								href = rsp.result;
								/*if( node.href != href && href.charAt(0) != "#" )
								{
									skit_confirm("模块菜单URL地址改变需要重新配置系统权限才能让相关用户使用，您要马上配置权限吗？", function(yes){
										if( yes )
										{
											//TODO:没有权限就向系统管理员发送一条系统通知
											openView("系统权限配置", "role!manager.action");
										}
									});
								}*/
								if( href.charAt(0) == "#" ){
									document.getElementById( "menuHref" ).value = href;
									document.getElementById( node.tId + "_ico").className = "button ico_open";
									node.isParent = true;
								}
								else{
									node.icon = "images/icons/bookmarks.png";									
								}
								node.name = name;
								node.href = href;
								node["target"] = target;
								if( href.charAt(0) == "#") parent["isParent"] = true;
								myZtree.updateNode(node);
								if( node.newMenu )
								{
									node["newMenu"] = false;
									node.path.push(node.getIndex());//jQuery.parseJSON(rsp.result);
								}
								//alert(node.tId+"_span:"+document.getElementById( node.tId + "_span")+" "+name);
								document.getElementById( node.tId + "_span").innerHTML = name;
								skit_alert(rsp.message);
							}
							else
							{
								skit_alert(rsp.message);
								initSelectDefaultTreeNode(rsp.result);
							}
						}
						catch(e)
						{
							skit_alert("处理设置模块子系统菜单操作出现异常"+e);
						}
					},
					timeout:30000,
					errorHandler:function(err) {skit_hiddenLoading(); alert(err); }
				});
			}
		});
	}

	function delModule(node)
	{
		var tip = "";
		if( node.type == "module" )
		{
			tip = "您要删除的是模块子系统【"+sysname+"】配置节点,该操作将清除工具栏下所有菜单配置，您还可以选择通过【模块子系统配置管理】暂时禁用指定模块工作，您要继续删除吗？";
		}
		else
		{
			tip = "您要删除的是系统工具栏，该操作将清除工具栏下所有菜单配置，您确定操作吗？";
		}
		skit_confirm(tip, function(yes){
			if( yes )
			{
				try
				{
					MenusMgr.delModulesNode(timestamp, sysid, sysname, {
						callback:function(rsp) {
							skit_hiddenLoading();
							try
							{
								timestamp = rsp.timestamp;
								if( rsp.succeed )
								{
									skit_alert(rsp.message);
								}
								initSelectDefaultTreeNode(rsp.result);
							}
							catch(e)
							{
								skit_alert("删除模块菜单组操作出现异常"+e);
							}
						},
						timeout:30000,
						errorHandler:function(err) {skit_hiddenLoading(); skit_alert(err); }
					});
				}
				catch(e)
				{
					skit_alert("执行Ajax调用出现异常 "+e.message+", 行数"+e.lineNumber);
				}
			}
		});
	}
	
	//检查菜单配置中唯一标识重复的情况
	function chkMenu(){
		hideRMenu();
		MenusMgr.chkModulesMenu(sysid, {
			callback:function(rsp) {
				skit_alert(rsp.message);
				try
				{
					timestamp = rsp.timestamp;
					if( rsp.succeed )
					{
						var json = jQuery.parseJSON(rsp.result);
						for(var i = 0; i < json.length; i++){
							var e = json[i];
							var nodes = myZtree.getNodesByParamFuzzy("href", e.href, null);
							if( nodes != null ){
								for(var j = 0; j < nodes.length; j++)
								{
									myZtree.selectNode(nodes[j]);
									myZtree.expandNode(nodes[j], true, false);
								}
							}
						}
					}
				}
				catch(e)
				{
					skit_alert("检查模块子系统菜单配置唯一标识重复操作出现异常"+e);
				}
			},
			timeout:30000,
			errorHandler:function(message) {skit_hiddenLoading(); skit_alert(message); }
		});
	}

	function delMenu()
	{
		hideRMenu();
		var node = myZtree.getSelectedNodes()[0];
		if( node.nomenu && ( node.type == "module" || node.type == "toolbar" ) )
		{
			delModule(node);
			return;
		}
		var msg = "您要删除的是【系统工具栏】的菜单【"+node.name+"】";
		if( node.type == "module" )
		{
			msg = "您要删除的是模块子系统【"+sysname+"】的菜单【"+node.name+"】";
		}
		if( node.href != "" && node.href.charAt(0) == "#" )
		{
			msg += "，菜单组ID是【"+node.href+"】";
		}
		else
		{
			msg += "，菜单组URL是【"+node.href+"】";
		}
		msg += "。删除该目录该菜单下所有的子菜单也将被同时删除，请确认！";
		skit_confirm(msg, function(yes){
			if( yes )
			{
				skit_showLoading();
				MenusMgr.delModulesMenu(node.path, timestamp, sysid, sysname, {
					callback:function(rsp) {
						skit_hiddenLoading();
						skit_alert(rsp.message);
						try
						{
							tIdOper = node.getParentNode()?node.getParentNode().tId:"";
							timestamp = rsp.timestamp;
							if( rsp.succeed )
							{
								var parent = node.getParentNode();
								myZtree.removeNode(node);
								parent["isParent"] = true;
								document.getElementById( parent.tId + "_ico").className = "button ico_open";
								myZtree.selectNode(parent);
								selectTreeNode(parent);
							}
							else
							{
								initSelectDefaultTreeNode(rsp.result);
							}
						}
						catch(e)
						{
							skit_alert("删除模块子系统菜单操作出现异常"+e);
						}
					},
					timeout:30000,
					errorHandler:function(message) {skit_hiddenLoading(); skit_alert(message); }
				});
			}
		});
	}

	var treeIdAddModulesNode = "";
	function addModulesNode(treeNode)
	{
		if( treeNode.nomenu && !treeNode.path )
		{
			treeIdAddModulesNode = treeNode.id;
			MenusMgr.addModulesNode(treeNode.type, timestamp, sysid, sysname, {
				callback:function(rsp) {
					if( !rsp.succeed )
					{
						skit_alert(rsp.message);
					}
					timestamp = rsp.timestamp;
					if( rsp.result )
					{
						initSelectDefaultTreeNode(rsp.result);
					}
				},
				timeout:30000,
				errorHandler:function(err) {skit_hiddenLoading(); alert(err); }
			});
			return true;
		}
		else return false;
	}
	
	function addRoleMgrMenu()
	{
		addDefaultMenu("权限管理菜单组");
	}
	/*function addCmpCfgMenu()
	{
		addDefaultMenu("模块菜单配置组");
	}*/
	function addWeixinCfgMenu(weixinno)
	{
		addDefaultMenu("微信管理菜单组:"+weixinno);
	}
	function addClusterCfgMenu()
	{
		addDefaultMenu("系统维护菜单组");
	}
	function addSysCfgMenu()
	{
		addDefaultMenu("系统配置菜单组");
	}
	//function addDiggCfgMenu()
	//{
	//	addDefaultMenu("元数据管理菜单组");
	//}
	var tIdOper = "";
	function addDefaultMenu(title)
	{
		hideRMenu();
		var node = myZtree.getSelectedNodes()[0];
		var appendmenu = node.nomenu?"":("【"+node.name+"】");
		var msg = "您要在【系统工具栏】的菜单"+appendmenu+"下添加【"+title+"】，您是否自动创建分组目录？";
		if( node.type == "module" )
		{
			msg = "您要在【"+sysname+"】的菜单"+appendmenu+"下添加【"+title+"】，您是否自动创建分组目录？";
		}
		if( node.nomenu && sysid == "Sys" )
		{
			msg = "您要在【"+sysname+"】子系统下添加【"+title+"】，您是否同意自动创建？";
		}
		skit_confirm(msg, function(hasdir){
			if( !hasdir )
			{
				/*if( node.nomenu  )
				{
					if( title != "系统管理缺省权限菜单" )
					{
						msg = "不允许在根菜单目录下添加"+title+"。";
						window.setTimeout('skit_alert(\"'+msg+'\");',100);//1秒执行第一次系统消息检查
					}
				}*/
				return;
			}
			MenusMgr.addDefaultMenu(title, hasdir, node.path, timestamp, sysid, sysname, {
				callback:function(rsp) {
					skit_hiddenLoading();
					try
					{
						tIdOper = node.tId;
						timestamp = rsp.timestamp;
						if( !rsp.succeed )
						{
							skit_alert(rsp.message);
						}
						if( rsp.result )
						{
							treeIdAddModulesNode = "";
							initSelectDefaultTreeNode(rsp.result);
						}
					}
					catch(e)
					{
						skit_alert("添加缺省的模块子系统菜单【"+title+"】操作出现异常"+e);
					}
				},
				timeout:30000,
				errorHandler:function(message) {skit_hiddenLoading(); skit_alert(message); }
			});
		});
	}

	//设置正确路径
	function setPath(node)
	{
		if( node && node["children"] )
		{
			for(var i = 0; i < node.children.length;)
			{
				var child = node.children[i];
				if( child )
				{
					var path = new Array();
					for(var j = 0; j < node.path.length; j++)
						path.push(node.path[j]);
					path.push(i++);
					child.path = path;
					setPath(child);
				}
			}
		}
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
				MenusMgr.dragDropMenu(moveType, node.path, targetNode.path, timestamp, sysid, sysname, {
					callback:function(rsp) {
						skit_hiddenLoading();
						try
						{
							timestamp = rsp.timestamp;
							if( rsp.succeed )
							{
								setPath(parentSource);
								if( parentSource != parentTarget )
								{
									setPath(parentTarget);
								}
							}
							else
							{
								skit_alert(rsp.message);
								initSelectDefaultTreeNode(rsp.result);
							}
						}
						catch(e)
						{
							skit_alert("拖拽子系统菜单操作出现异常"+e);
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
	
	function downloadXml()
	{
		skit_confirm("您确认要下载模块子系统菜单配置到本地？", function(yes){
			if( yes )
			{
				document.forms[0].action = "menus!downloadxml.action";
				document.forms[0].method = "POST";
				document.forms[0].target = "iDownload";
				document.forms[0].submit();
			}
		});
	}
	
	function uploadXml()
	{
		var divUploadfile = document.getElementById( 'divUploadfile' );
		divUploadfile.style.display = "";
		var uploadUrl = "menus!uploadxml.action";
		$("#uploadfile").fileinput({
				language: 'zh', //设置语言
				uploadUrl: uploadUrl, //上传的地址
				allowedFileExtensions: ['xml'],//接收的文件后缀
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
	            	obj["sysid"] = sysid;
	                return obj;
	            }
		});
		divUploadfile.style.top = 64;//windowHeight/2 - divUploadfile.clientHeight*2/3;
		divUploadfile.style.left = windowWidth/2 - divUploadfile.clientWidth/2;
		$("#uploadfile").on("fileloaded", function (data, previewId, index) {
		});
		//导入文件上传完成之后的事件
		$("#uploadfile").on("fileuploaded", function (event, data, previewId, index) {
			if( data.response.succeed )
			{
				skit_alert(data.response.alt, "后台提示", function(){
					parent.open();
				});
			}
			else
			{
				skit_alert(data.response.alt);
				document.getElementById( 'divUploadfile' ).style.display = "none";
				$("#uploadfile").fileinput("destroy");
			}
		});
	}

	function closeuploadfile()
	{
		document.getElementById( 'divUploadfile' ).style.display = "none";
		$("#uploadfile").fileinput("destroy");
	}
</SCRIPT>
<link href="skin/defone/css/simplemodal.css" rel="stylesheet">
<script src="skin/defone/js/mootools-core-1.3.1.js"></script>
<script src="skin/defone/js/simple-modal.js?v=3"></script>
<script src="skit/js/bootstrap-tour.min.js"></script>
<link href="skit/css/fileinput.min.css" rel="stylesheet"/>
<script src="skit/js/fileinput.js"></script>
<script src="skit/js/fileinput_locale_zh.js"></script>
<SCRIPT type="text/javascript">
function tour()
{
	var showTour = getUserActionMemory("tourModules");
	if( showTour != "1" )
	{
		setUserActionMemory("tourModules", "1");
		var tour = new Tour({
		    steps: [{
		        element: "#myZtree",
		        title: "模块菜单导航显示",
		        content: "可通过该导航树查找、查看、新增、删除菜单。"
		    },{
		        element: "#tabL",
		        title: "菜单可视化编辑",
		        content: "可对导航树上显示的菜单进行可是化编辑，设置菜单的名称、URL地址，以及图标等。",
		        placement: "left"
		    }],
		    container: 'body',
		    backdrop: true,
		    keyboard: true,
		    storage: false
		});
		// Initialize the tour
		tour.init();
		// Start the tour
		tour.start();
	}
}
tour();
document.forms[0].action = "editor!xml.action";
document.forms[0].method = "POST";
document.forms[0].target = "xmlEdit";
document.forms[0].submit();
</SCRIPT>
</html>