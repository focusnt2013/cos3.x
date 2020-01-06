<%@ page language="java" pageEncoding="UTF-8"%>
<%@ page import="com.focus.cos.web.common.Kit"%>
<%@ taglib uri="/webwork" prefix="ww" %>
<%Kit.noCache(request,response);  %>
<html>
<head>
<link type="text/css" href="skit/css/bootstrap.css" rel="stylesheet">
<style type='text/css'>
</style>
<%=Kit.getDwrJsTag(request,"interface/UserMgr.js")%>
<%=Kit.getDwrJsTag(request,"engine.js")%>
<%=Kit.getDwrJsTag(request,"util.js")%>
</head>
<body>
<div class="well profile" id='divModuleProfile' style='display:'>
    <div class="col-sm-12">
        <div class="col-xs-12 col-sm-10">
            <h2 id='h2ModuleName' style='margin-top:0px;'><ww:property: value=''/></h2>
            <p><strong><i class="fa fa-connectdevelop fa-fw text-muted"></i>开发商: </strong> <span id='tdCmpDevelopers'>N/A</span> </p>
            <p><strong><i class="fa fa-user fa-fw text-muted"></i>联系方式: </strong> <span id='tdCmpDevelopersContact'>N/A</span> </p>
            <p><strong><i class="fa fa-envelope fa-fw text-muted"></i>子系统邮箱: </strong> <span id='tdPOP3Username'>N/A</span> </p>
        </div>
        <div class="col-xs-12 col-sm-2 text-center">
            <figure>
                <img src="images/cmp/8.png" id='imgModuleLogo' alt="" class="img-circle img-responsive">
                <figcaption class="ratings" style='margin-top:0px;'>
                	<p id='pModuleId'>N/A</p>
                    <p style='display:none'>程序健康度评分
                    <a href="#"><span class="fa fa-star"></span></a>
                    <a href="#"><span class="fa fa-star"></span></a>
                    <a href="#"><span class="fa fa-star"></span></a>
                    <a href="#"><span class="fa fa-star"></span></a>
                    <a href="#"><span class="fa fa-star-o"></span></a> 
                    </p>
                </figcaption>
            </figure>
        </div>
    </div>
    <div class="col-xs-12 text-center">
        <div class="col-xs-12 col-sm-10 emphasis" style='width:100%;border:0px solid red'>
    		<button type="button" class="btn btn-outline btn-primary btn-block" onclick='doSave();'><i class="fa fa-save"></i> 保存 </button>
        </div>
    </div>
</div>
</body>
<%@ include file="../../skit/inc/skit_cos.inc"%>
<%@ include file="../../skit/inc/skit_bootstrap.inc"%>
<SCRIPT type="text/javascript">
</SCRIPT>
</html>