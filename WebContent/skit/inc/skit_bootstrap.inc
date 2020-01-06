<%@ page language="java" contentType="text/html; charset=UTF-8"  pageEncoding="UTF-8"%> 
<script src="skin/defone/js/bootstrap.min.js"></script>
<style type='text/css'>
.jumbotron {
    background-color: #fff;
    border: 1px solid #efefef;
}
.jumbotron {
    padding: 30px;
}
.jumbotron h1, .jumbotron p {
    font-family: Lato,sans-serif;
    font-weight: 300;
}
.well {
    -webkit-border-radius: 0;
    -moz-border-radius: 0;
    border-radius: 0;
}
.well h3 {
    font-family: 微软雅黑,Lato,sans-serif;
    font-weight: bold;
}
.well p {
    font-family: 微软雅黑,Lato,sans-serif;
    font-weight: 300;
	font-size: 12px;
}

.user-panel {
    margin-top: 10px;
    padding: 20px;
    box-shadow: 0 1px 3px 0 rgba(0,0,0,.14);
    background-color: #fff;
    border: 1px solid #efefef;
    -webkit-border-radius: 6px;
    -moz-border-radius: 6px;
    border-radius: 6px;
}
.user-panel img {
    text-align: center;
    box-shadow: 0 0 5px rgba(0,0,0,.5);
    -webkit-border-radius: 50%;
    -moz-border-radius: 50%;
    border-radius: 50%;
    padding: 1px;
    width: 64px;
    height: 64px;
}
.user-panel span {
    margin-top: 15px;
    display: block;
    font-size: 18px;
}
.user-panel .user-job {
    margin: 30px -20px;
    padding: 15px;
    text-transform: uppercase;
    text-align: center;
    background-color: #efefef;
}
.user-panel .user-stats {
    font-family: Lato,sans-serif;
    font-size: 16px;
    font-weight: 300;
    color: #999;
}
.user-panel .tip {
    margin: 5px 0;
    font-weight: 400;
    line-height: 20px;
    color:#afafaf;
    width:128px;
}
.well {
    -webkit-border-radius: 0;
    -moz-border-radius: 0;
    border-radius: 0;
}
.profile {
    display: inline-block;
    width: 100%;
}
.profile figcaption.ratings {
    margin-top: 20px;
}
.profile span.tags {
    background: #1abc9c;
    border-radius: 2px;
    color: #f5f5f5;
    font-weight: 700;
    padding: 2px 4px;
}
.panel.panel-default {
    border: 1px solid #efefef;
}
.panel {
    -webkit-border-radius: 0;
    -moz-border-radius: 0;
    border-radius: 0;
    box-shadow: 0 1px 3px 0 rgba(0,0,0,.14);
}
.panel.panel-default .panel-heading {
    color: #606060;
    background-color: #fff;
    border-bottom: 1px solid #efefef;
}
.panel .panel-heading {
    font-size: 12px;
	padding: 8px 5px;
    font-family: "Source Sans Pro",sans-serif;
    text-transform: uppercase;
    cursor: pointer;
}
.panel-body {
    padding: 15px;
}
.panel {
    margin-bottom: 3px;
    background-color: #fff;
    border: 1px solid transparent;
    border-radius: 4px;
    -webkit-box-shadow: 0 1px 1px rgba(0,0,0,.05);
    box-shadow: 0 1px 1px rgba(0,0,0,.05);
}

.btn {
    -webkit-border-radius: 20px;
    -moz-border-radius: 20px;
    border-radius: 20px;
    border: none;
    outline: 0 !important;
}
.btn, a {
    -webkit-transition: all .25s ease-out;
    -moz-transition: all .25s ease-out;
    -o-transition: all .25s ease-out;
    transition: all .25s ease-out;
}

.panel-icon ul {
	list-style-type:none
}
.panel-icon ul li {
	float:left;
	width:32px;
	font-size:10pt;
}
.icon-sel {
	width:32px;
	font-size:10pt;
	padding: .7em 0;
	float: left; 
	position: relative;
	text-align: left;
}
.icon-sel [class^="ti-"], 
.icon-sel [class*=" ti-"],
.icon-sel [class^="fa"], 
.icon-sel [class*=" fa"],
.icon-sel [class^="ion-"], 
.icon-sel [class*=" ion-"] {
	color: #3498db;
	position: absolute;
	margin-top: 3px;
	transition: .3s;
}
.icon-sel:hover [class^="ti-"],
.icon-sel:hover [class*=" ti-"],
.icon-sel:hover [class^="fa"],
.icon-sel:hover [class*=" fa"],
.icon-sel:hover [class^="ion-"],
.icon-sel:hover [class*=" ion-"] {
	font-size: 2.2em;
	margin-top: -5px;
	color: #25a25a;
}
.icon-name{
	display:none;
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
.skit_fa_toolbar{
	font-size: 14px;
	color:#18bc9c;
	width:16px;
	height:16px;
	border:1px solid #dae3ec;
	cursor:pointer;
}
.skit_fa_toolbar:hover {
	font-size: 15px;
	color:#18bc9c;
	width:16px;
	height:16px;
	border:1px solid #dae3ec;
	cursor:pointer;
}
.skit_fa_toolbar_light{
	font-size: 14px;
	color:#316cce;
	width:16px;
	height:16px;
	cursor:pointer;
}
.skit_fa_toolbar_light:hover {
	font-size: 15px;
	color:#316cce;
	width:16px;
	height:16px;
	cursor:pointer;
}
.skit_fa_toolbar_red{
	font-size: 14px;
	color:#ed3312;
	width:16px;
	height:16px;
	cursor:pointer;
}
.skit_fa_toolbar_red:hover {
	font-size: 15px;
	color:#ed3312;
	width:16px;
	height:16px;
	cursor:pointer;
}
.skit_fa_toolbar_blue{
	font-size: 14px;
	color:#0f85f0;
	width:16px;
	height:16px;
	cursor:pointer;
}
.skit_fa_toolbar_blue:hover {
	font-size: 15px;
	color:#0f85f0;
	width:16px;
	height:16px;
	cursor:pointer;
}
.skit_fa_toolbar_gray{
	font-size: 14px;
	color:#aab1b7;
	width:16px;
	height:16px;
	cursor:pointer;
}
.skit_fa_toolbar_gray:hover {
	font-size: 15px;
	color:#aab1b7;
	width:16px;
	height:16px;
	cursor:pointer;
}
.skit_fa_toolbar_war{
	font-size: 14px;
	color:#fabd05;
	width:16px;
	height:16px;
	cursor:pointer;
}
.skit_fa_toolbar_war:hover {
	font-size: 15px;
	color:#fabd05;
	width:16px;
	height:16px;
	cursor:pointer;
}

.skit_fa_btn{
	font-size: 14px;
	color:#18bc9c;
	width:16px;
	height:16px;
	cursor:pointer;
}
.skit_fa_btn:hover {
	font-size: 14px;
	color:#1ddcb6;
	width:16px;
	height:16px;
	cursor:pointer;
}
.skit_fa_btn_red{
	font-size: 14px;
	color:#ed3312;
	width:16px;
	height:16px;
	cursor:pointer;
}
.skit_fa_menu{
	font-size: 14px;
	color:#ffffff;
	width:16px;
	height:16px;
	cursor:pointer;
}
.skit_fa_icon{
	font-size: 14px;
	color:#18bc9c;
	cursor:pointer;
}
.skit_fa_icon_light{
	font-size: 14px;
	color:#316cce;
	cursor:pointer;
}
.skit_fa_icon_red{
	font-size: 14px;
	color:#ed3312;
	cursor:pointer;
}
.skit_fa_icon_blue{
	font-size: 14px;
	color:#00e6e6;
	cursor:pointer;
}
.skit_fa_icon_gray{
	font-size: 14px;
	color:#aab1b7;
	cursor:pointer;
}
.skit_fa_icon_war{
	font-size: 14px;
	color:#fabd05;
	cursor:pointer;
}
.skit_fa_icon_orange{
	font-size: 14px;
	color:#fbc728;
	cursor:pointer;
}
.skit_fa_icon_yellow{
	font-size: 14px;
	color:#fcda72;
	cursor:pointer;
}

.input-group-addon {
    padding: 6px 12px;
    font-size: 12px;
    font-weight: 400;
    line-height: 1;
    color: #555;
    text-align: center;
    background-color: #eee;
    border: 1px solid #ccc;
    border-radius: 4px;
}
.form-control {
    display: block;
    width: 100%;
    height: 34px;
    padding: 6px 12px;
    font-size: 12px;
    line-height: 1.42857143;
    color: #555;
    background-color: #fff;
    background-image: none;
    border: 1px solid #ccc;
    border-radius: 4px;
    -webkit-box-shadow: inset 0 1px 1px rgba(0,0,0,.075);
    box-shadow: inset 0 1px 1px rgba(0,0,0,.075);
    -webkit-transition: border-color ease-in-out .15s,-webkit-box-shadow ease-in-out .15s;
    -o-transition: border-color ease-in-out .15s,box-shadow ease-in-out .15s;
    transition: border-color ease-in-out .15s,box-shadow ease-in-out .15s;
}
.form-group {
    margin-bottom: 10px;
}
.panel-body {
    padding-bottom: 10px;
}
.profile .emphasis:hover {
    border-top: 4px solid #1abc9c;
}
.profile .emphasis {
    border-top: 4px solid transparent;
}
.profile .divider {
    border-top: 1px solid rgba(0,0,0,.1);
}
.media.media-info {
	margin-top: 0px;
	margin-bottom: 15px;
	width: 128px;
	min-width: 120px;
	float: left;
	position: relative;
	text-align: left;
}
.media.media-info .media-object.fleet-icon {
    border: 1px solid rgba(0,0,0,.1);
}
.media .media-object.fleet-icon {
    width: 32px;
    height: 32px;
    -webkit-border-radius: 3px;
    -moz-border-radius: 3px;
    border-radius: 3px;
    text-align: center;
    color: #fff;
    line-height: 32px;
}
.media .media-object.fleet-icon i {
    font-size: 16px;
	padding-top: 7px;
}
.media .media-object.fleet-icon img {
	padding-top: 7px;
}
.media .media-heading {
    font-size: 12px;
    font-weight: 800;
    margin-bottom: 0;
}
.media .caption {
    color: #888;
	font-size: 12px;
}

.dropdown-menu {
    font-size: 12px;
    text-align: left;
    list-style: none;
}

.dropdown-submenu {
	position: relative;
}

.dropdown-submenu > .dropdown-menu {
	top: 0;
	left: 100%;
	margin-top: -6px;
	margin-left: -1px;
	-webkit-border-radius: 0 6px 6px 6px;
	-moz-border-radius: 0 6px 6px;
	border-radius: 0 6px 6px 6px;
}

.dropdown-submenu:hover > .dropdown-menu {
	display: block;
}

.dropdown-submenu > a:after {
	display: block;
	content: " ";
	float: right;
	width: 0;
	height: 0;
	border-color: transparent;
	border-style: solid;
	border-width: 5px 0 5px 5px;
	border-left-color: #ccc;
	margin-top: 5px;
	margin-right: -10px;
}

.dropdown-submenu:hover > a:after {
	border-left-color: #fff;
}

.dropdown-submenu.pull-left {
	float: none;
}

.dropdown-submenu.pull-left > .dropdown-menu {
	left: -100%;
	margin-left: 10px;
	-webkit-border-radius: 6px 0 6px 6px;
	-moz-border-radius: 6px 0 6px 6px;
	border-radius: 6px 0 6px 6px;
}

.switchery {
    background-color: #fff;
    border: 1px solid #dfdfdf;
    border-radius: 20px;
    cursor: pointer;
    display: inline-block;
    height: 22px;
    position: relative;
    vertical-align: middle;
    width: 50px;
    -moz-user-select: none;
    -khtml-user-select: none;
    -webkit-user-select: none;
    -ms-user-select: none;
    user-select: none;
    box-sizing: content-box;
    background-clip: content-box;
}

.switchery.switchery-default {
    width: 40px;
    height: 22px;
    -webkit-border-radius: 15px;
    -moz-border-radius: 15px;
    border-radius: 15px;
}

.switchery.switchery-default > small {
    width: 25px;
    height: 22px;
}
.switchery > small {
    background: #fff;
    border-radius: 100%;
    box-shadow: 0 1px 3px rgba(0,0,0,0.4);
    height: 22px;
    position: absolute;
    top: 0;
    width: 30px;
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
.skit_mask {
   visibility: hidden;
   background-color: #92A3B2;
   left: 0px;
   position: absolute;
   top: 0px;
   background-image: none;
   BORDER-TOP: 1px solid #405B40;
   BORDER-LEFT: 1px solid #405B40;
   BORDER-RIGHT: 1px solid #405B40;
   BORDER-BOTTOM: 1px solid #405B40;
   /* older safari/Chrome browsers */  
   -webkit-opacity: 0.5;  
   /* Netscape and Older than Firefox 0.9 */  
   -moz-opacity: 0.5;  
   /* Safari 1.x (pre WebKit!) 老式khtml内核的Safari浏览器*/  
   -khtml-opacity: 0.5;  
   /* IE9 + etc...modern browsers */  
   opacity: .5;  
   /* IE 4-9 */  
   filter:alpha(opacity=50);  
   /*This works in IE 8 & 9 too*/  
   -ms-filter:"progid:DXImageTransform.Microsoft.Alpha(Opacity=50)";  
   /*IE4-IE9*/  
   filter:progid:DXImageTransform.Microsoft.Alpha(Opacity=50);  
}
.alert-warning {
    color: #8a6d3b;
    background-color: #fcf8e3;
    border-color: #faebcc;
}
.alert {
    padding: 15px;
    margin-bottom: 0px;
    border: 1px solid transparent;
        border-top-color: transparent;
        border-right-color: transparent;
        border-bottom-color: transparent;
        border-left-color: transparent;
    border-radius: 4px;
    position: absolute;
   /* older safari/Chrome browsers */  
   -webkit-opacity: 0.1;  
   /* Netscape and Older than Firefox 0.9 */  
   -moz-opacity: 0.8;  
   /* Safari 1.x (pre WebKit!) 老式khtml内核的Safari浏览器*/  
   -khtml-opacity: 0.8;  
   /* IE9 + etc...modern browsers */  
   opacity: .8;  
   /* IE 4-9 */  
   filter:alpha(opacity=80);  
   /*This works in IE 8 & 9 too*/  
   -ms-filter:"progid:DXImageTransform.Microsoft.Alpha(Opacity=80)";  
   /*IE4-IE9*/  
   filter:progid:DXImageTransform.Microsoft.Alpha(Opacity=80);  
}
.alert span {
    color: red;
}
</style>