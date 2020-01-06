<%@ page language="java" pageEncoding="UTF-8"%>
<%@ page import="com.focus.cos.web.common.Kit"%>
<%@ taglib uri="/webwork" prefix="ww" %>
<%Kit.noCache(request,response); %>
<html>
<head>
<style type='text/css'>
<%@ include file="../../../skit/css/jquery-ui.css"%>
<%@ include file="../../../skit/grid/pqgrid.min.css"%>
<%@ include file="../../../skit/grid/pqselect.min.css"%>
<%@ include file="../../../skit/grid/themes/office/pqgrid.css"%>
<%@ include file="../../../skit/grid/pqgrid-skit.css"%>
body {overflow-y:auto; overflow-x:auto; margin-top:3px; margin-left:0px; margin-bottom:0px; margin-right:0px }
</style>
</head>
<body>
	<div class="pq-grid ui-widget ui-widget-content ui-corner-all" id="grid_data" style="width:<ww:property value='snapshotWidth'/>px;">
		<div class="pq-grid-top ui-widget-header ui-corner-top">
			<div style="" class="pq-grid-title ui-corner-top"><ww:property value='%{viewTitle}' escape='false'/></div>
		</div>
		<div style="" class="pq-grid-inner">
			<div class="pq-grid-right">
				<div style="height: 26px;" class="pq-header-outer ui-widget-header">
					<span class="pq-grid-header ui-state-default">
						<ww:property value='%{colModel}' escape='false'/>
					</span>
				</div>
				<div class="pq-cont-right">
					<div style="" class="pq-cont pq-cont-1">
						<div class="pq-cont-inner">
							<ww:property value='%{dataModel}' escape='false'/>
						</div>
					</div>
				</div>
			</div>
		</div>
		<div class="pq-grid-bottom ui-widget-header ui-corner-bottom">
			<ww:if test='summaryModel!=null'>
			<div class="pq-grid-summary">
				<ww:property value='%{summaryModel}' escape='false'/>
			</div>
			</ww:if>
			<div class="pq-grid-footer pq-pager">
				<span class="pq-pager-msg"><ww:property value='%{bottomInfo}' escape='false'/></span>
			</div>
		</div>
	</div>
</body>
</html>