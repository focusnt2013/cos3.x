<%@ page language="java" pageEncoding="UTF-8"%>
<%@ page import="com.focus.cos.web.common.Kit"%>
<%@ taglib uri="/webwork" prefix="ww" %>
<%Kit.noCache(request,response); %>
<html>
<head>
<link type="text/css" href="skit/css/timeline.css" rel="stylesheet">
<style type='text/css'>
/*-------------------------
	Simple reset
--------------------------*/
*{
	margin:0;
	padding:0;
}
/*-------------------------
	General Styles
--------------------------*/
html{
	background:url('skit/css/images/page_bg.jpg') repeat #191919;
}
body{
	min-height: 700px;
    padding: 0 0 50px;
	font:14px/1.3 'Segoe UI',Arial, sans-serif;
	overflow:hidden;
}
a, a:visited {
	text-decoration:none;
	outline:none;
	color:#54a6de;
}
a:hover{
	text-decoration:underline;
}
section, footer, header{
	display: block;
}
/*----------------------------
	Styling the timeline
-----------------------------*/


#timeline{
	background:none;
}

.slider .slider-container-mask .slider-container{
	background:none;
}

#timeline div.navigation{
    background: url('skit/css/images/timeline_bg.jpg') repeat;
    border-top:none;
}

/* Creating the 3D effect for the timeline navigation */

#timeline div.navigation:before{
	position:absolute;
	content:'';
	height:40px;
	width:100%;
	left:0;
	top:-40px;
	background: url('skit/css/images/timeline_bg.jpg') repeat;
}

#timeline div.navigation:after{
	position:absolute;
	content:'';
	height:10px;
	width:100%;
	left:0;
	top:-40px;
	background:repeat-x;
	
	background-image: linear-gradient(bottom, #434446 0%, #363839 100%);
	background-image: -o-linear-gradient(bottom, #434446 0%, #363839 100%);
	background-image: -moz-linear-gradient(bottom, #434446 0%, #363839 100%);
	background-image: -webkit-linear-gradient(bottom, #434446 0%, #363839 100%);
	background-image: -ms-linear-gradient(bottom, #434446 0%, #363839 100%);
}


/* Adding a darker background to the timeline navigation */

#timeline div.timenav-background{
	background-color:#222 !important;
 	background-color:rgba(0,0,0,0.4) !important;
 	
}

#timeline .navigation .timenav-background .timenav-interval-background{
	background:none;
}

#timeline .top-highlight{
	background-color:transparent !important;
}

/* The zoom-in / zoom-out toolbar */

#timeline .toolbar{
	border:none !important;
	background-color: #202222 !important;
}

#timeline .toolbar div{
	border:none !important;
}

/* Styling the timeline numbers on the bottom */

#timeline .navigation .timenav .time .time-interval-minor .minor{
	margin-left:-1px;
}

#timeline .navigation .timenav .time .time-interval div{
	color: #CCCCCC;
}

/* The vertical lines */

#timeline .navigation .timenav .content .marker .line {
	background: #242828;
	box-shadow: 1px 0 0 rgba(255, 255, 255, 0.05);
}

#timeline .navigation .timenav .content .marker .dot {
	background: none repeat scroll 0 0 #FFFFFF;
	border-radius: 3px 3px 3px 3px;
}

/* The previous / next arrows & labels */

.slider .nav-previous .icon {
	background: url("skit/css/images/timeline.png") no-repeat scroll 0 -293px transparent;
}

.slider .nav-previous,.slider .nav-next{
	font-family:'Segoe UI',sans-serif;
}

.slider .nav-next .icon {
	background: url("skit/css/images/timeline.png") no-repeat scroll 72px -221px transparent;
	width: 70px !important;
}

.slider .nav-next:hover .icon{
	position:relative;
	right:-5px;
}

.slider .nav-previous:hover, .slider .nav-next:hover {
    color: #666;
    cursor: pointer;
}

#timeline .thumbnail {
	border: medium none;
}

/* Color of the headings */

#timeline h1, #timeline h2, #timeline h3, #timeline h4, #timeline h5, #timeline h6{
	color:#ddd;
}

/* The loading screen */

#timeline .feedback {
	background-color: #222222;
	box-shadow: 0 0 30px rgba(0, 0, 0, 0.2) inset;
	border:none;
}

#timeline .feedback div{
	color: #AAAAAA;
	font-size: 14px !important;
	font-weight: normal;
}


/*----------------------------
	The Slides
-----------------------------*/


#timeline .slider-item h2,
#timeline .slider-item h3{
	font-family:'Antic Slab','Segoe UI',sans-serif;
}

#timeline .slider-item h2{
	color:#fff;
}

#timeline .slider-item p{
	font-family:'Segoe UI',sans-serif;
}


#timeline .slider-item img,
#timeline .slider-item iframe{
	border:none;
}

/* Customizing the first slide - the cover */

#timeline .slider-item:nth-child(1) h2{
	font:normal 16px/1 'Antic Slab','Segoe UI',sans-serif;
	white-space: nowrap;
    font-weight: bold;
	padding-top:5px;
	padding-left:0px;
	padding-right: 5px;
	padding-bottom: 10px;
	position:relative;
	right:0px;
	z-index:10;
}

#timeline .slider-item:nth-child(1) p i{
	font:normal normal 40px 'Dancing Script','Segoe UI',sans-serif;
	background:rgba(0,0,0,0.3);
	white-space: nowrap;
	padding:5px 20px;
	position:relative;
	right:-60px;
	z-index:10;
}

#timeline .slider-item:nth-child(1) p .c1{
	color:#1bdff0;
}

#timeline .slider-item:nth-child(1) p .c2{
	color:#c92fe6;
}

#timeline .slider-item:nth-child(1) .media-container {
	left: -30px;
	position: relative;
	z-index: 1;
}
#timeline .slider-item .media-container {
    border: 10px solid #fff;
}
#timeline .slider-item .media-container .credit {
    height: 80px;
    background: #fff;
    line-height: 80px;
}
#timeline .slider-item:first-child .media-container .credit{
    display: none;
}
#timeline .slider-item:first-child .media-container {
    border:0;
}
#timeline .slider-item:nth-child(1) .credit{
	text-align: center;
}
#timeline h2 {
    margin-bottom: 0px;
    font-size: 16px;
    line-height: 20px;
}
#timeline .slider-item h3 {
    font-family: 'KaiTi','Antic Slab','Segoe UI',sans-serif;
}
#timeline h3 {
    font-size: 16px;
    font-family: 'KaiTi','Antic Slab','Segoe UI',sans-serif;
    background: #ffd800;
    border-radius: 2px 2px 2px 2px;
    color: #fff;
    font-weight: bold;
    padding: 1px 8px;
    width:96px;
}
#timeline .feature h3 {
    margin-bottom: 0px;
}
#timeline .slider-item p {
    font-family: 'Microsoft YaHei','Segoe UI',sans-serif;
}
#timeline .feature p {
    color: #aaa;
}
#timeline p {
    font-size: 14px;
    font-weight: normal;
    line-height: 20px;
    margin-bottom: 20px;
    color: #666666;
}
span.version {
    font-size: 14px;
    font-family: 'KaiTi','Antic Slab','Segoe UI',sans-serif;
    background: #ffd800;
    border-radius: 2px 0px 0px 2px;
    color: #fff;
    font-weight: bold;
    padding: 1px 8px;
}
span.title {
    background: #eee;
    font-size: 14px;
	white-space: nowrap;
    font-family: 'KaiTi','Antic Slab','Segoe UI',sans-serif;
    height:22px;
    border-radius: 0px 2px 2px 0px;
    color: #000;
    font-weight: bold;
    padding: 1px 4px;
}
</style>
<script src="skit/js/jquery-timeline.js"></script>
<script src="skit/js/timeline-min.js"></script>
</head>
<body>
<div id="timeline">
	<!-- Timeline.js will genereate the markup here -->
</div>
</body>
<SCRIPT type="text/javascript">
var url = "<%=Kit.URL_PATH(request)%><ww:property value='jsonData' escape='false'/>";
//url = "http://localhost:8080/data.json";
//alert(url);
var timeline = new VMM.Timeline();
timeline.init(url);
</SCRIPT>
</html>