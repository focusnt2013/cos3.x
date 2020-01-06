<%@ page language="java" pageEncoding="UTF-8"%>
<%@ taglib uri="/webwork" prefix="ww" %>
<%@ page import="com.focus.cos.web.common.Kit"%>
<%Kit.noCache(request,response);%>
<html>
<style>
* {
        box-sizing: border-box;
}
html {
        height: 160px;
}
body { background-color: #fff; font-family: "HelveticaNeue-Light", "Helvetica Neue Light", "Helvetica Neue", Helvetica, Arial, "Lucida Grande", sans-serif; text-align: center; }
h1, p {
        padding:0; margin:0;
}
.wrapper {
        width: 480px;
        margin: 64px auto;
}
.wrapper p a {color:#757575; text-decoration:none;}
.wrapper .load-bar {
	width: 100%;
	height: 10px;
	border-radius: 30px;
	background-color: <ww:property value='themeColorLight'/>; 
	position: relative;
	box-shadow: 0 1px 0 rgba(255, 255, 255, 0.8),  inset 0 2px 3px rgba(0, 0, 0, 0.2); }
.wrapper .load-bar:hover .load-bar-inner, .wrapper .load-bar:hover #counter {
        animation-play-state: paused;
        -moz-animation-play-state: paused;
        -o-animation-play-state: paused;
        -webkit-animation-play-state: paused;
}
.wrapper .load-bar-inner { height: 99%;
	width: 0%;
	border-radius: inherit;
	position: relative;background-color: <ww:property value='themeColor'/>; 
	animation: loader 10s linear infinite; 
	-moz-animation: loader 10s linear infinite; 
	-webkit-animation: loader 10s linear infinite;
	-o-animation: loader 10s linear infinite; 
}
.wrapper #counter {
        position: absolute;
		background-color: #E0E0E0;
        padding: 5px 10px;
        border-radius: 0.4em;
        box-shadow: inset 0 1px 0 rgba(255, 255, 255, 1),  0 2px 4px 1px rgba(0, 0, 0, 0.2),  0 1px 3px 1px rgba(0, 0, 0, 0.1);
        left: -25px;
        top: -35px;
        font-size: 12px;
        font-weight: bold;
        width: 44px;
        animation: counter 10s linear infinite;
        -moz-animation: counter 10s linear infinite;
        -webkit-animation: counter 10s linear infinite;
        -o-animation: counter 10s linear infinite;
}
.wrapper #counter:after { content: ""; position: absolute; width: 8px; height: 8px; background-color: #E7E6E3; transform: rotate(45deg); -moz-transform: rotate(45deg); -webkit-transform: rotate(45deg); -o-transform: rotate(45deg); left: 50%; margin-left: -4px; bottom: -4px; box-shadow: 3px 3px 4px rgba(0, 0, 0, 0.2),  1px 1px 1px 1px rgba(0, 0, 0, 0.1); border-radius: 0 0 3px 0; }
.wrapper h1 {
        font-size: 16px;
        padding: 20px 0 8px 0;
}
.wrapper p {
        font-size: 13px;
}
@keyframes loader {
 from {
width: 0%;
}
to {
        width: 100%;
}
}
 @-moz-keyframes loader {
 from {
width: 0%;
}
to {
        width: 100%;
}
}
 @-webkit-keyframes loader {
 from {
width: 0%;
}
to {
        width: 100%;
}
}
 @-o-keyframes loader {
 from {
width: 0%;
}
to {
        width: 100%;
}
}

 @keyframes counter {
 from {
left: -25px;
}
to {
        left: 453px;
}
}
 @-moz-keyframes counter {
 from {
left: -25px;
}
to {
        left: 453px;
}
}
 @-webkit-keyframes counter {
 from {
left: -25px;
}
to {
        left: 453px;
}
}
 @-o-keyframes counter {
 from {
left: -25px;
}
to {
        left: 453px;
}
}
@keyframes loader {
 from {
width: 0%;
}
to {
        width: 100%;
}
}
.load-bar-inner {
        height: 99%;
        width: 0%;
        border-radius: inherit;
        position: relative;
        background: #c2d7ac;
        animation: loader 10s linear infinite;
}
</style>
<script src="skit/js/jquery-2.1.4.min.js"></script>
<body>
<div class="wrapper">
	<div class="load-bar">   
		<div class="load-bar-inner" data-loading="0"> <span id="counter">0%</span> </div> 
	</div>
	<p id='pProgressTips'>请等待... </p>
</div>
</body>
<SCRIPT type="text/javascript">
$('.load-bar-inner').css("animation-play-state", "paused");
$('.load-bar-inner').css("-moz-animation-play-state", "paused");
$('.load-bar-inner').css("-o-animation-play-state", "paused");
$('.load-bar-inner').css("-webkit-animation-play-state", "paused");
$('#counter').css("animation-play-state", "paused");
$('#counter').css("-moz-animation-play-state", "paused");
$('#counter').css("-o-animation-play-state", "paused");
$('#counter').css("-webkit-animation-play-state", "paused");
var progress = 0;
function setProgress(p, tips)
{
	progress = p;
    document.getElementById("pProgressTips").innerHTML = tips;
	if( current < progress )
	{
	    $('.load-bar-inner').css("animation-play-state", "running");
	    $('.load-bar-inner').css("-moz-animation-play-state", "running");
	    $('.load-bar-inner').css("-o-animation-play-state", "running");
	    $('.load-bar-inner').css("-webkit-animation-play-state", "running");
	    $('#counter').css("animation-play-state", "running");
	    $('#counter').css("-moz-animation-play-state", "running");
	    $('#counter').css("-o-animation-play-state", "running");
	    $('#counter').css("-webkit-animation-play-state", "running");
	}
}
var current = 0;
function check()
{
	if( current >= progress )
	{
		$('.load-bar-inner').css("animation-play-state", "paused");
		$('.load-bar-inner').css("-moz-animation-play-state", "paused");
		$('.load-bar-inner').css("-o-animation-play-state", "paused");
		$('.load-bar-inner').css("-webkit-animation-play-state", "paused");
		$('#counter').css("animation-play-state", "paused");
		$('#counter').css("-moz-animation-play-state", "paused");
		$('#counter').css("-o-animation-play-state", "paused");
		$('#counter').css("-webkit-animation-play-state", "paused");
		return;
	}
	current += 1;
    $('#counter').html(current+'%'); 
}
setInterval(check, 70);
</SCRIPT>
</html>