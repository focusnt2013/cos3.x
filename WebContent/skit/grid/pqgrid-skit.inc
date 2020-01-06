<%@ page language="java" contentType="text/html; charset=UTF-8"  pageEncoding="UTF-8"%>
<style type='text/css'>
body {overflow-y:hidden; overflow-x:hidden; margin-top:0px; margin-left:0px; margin-bottom:0px; margin-right:0px }

button.query_btn
{
    height:22px;
}

button.delete_btn
{
    margin:-2px 0px;
}
button.edit_btn
{
    margin:-3px 0px;
}

tr.pq-row-delete
{
    text-decoration:line-through;         
}

tr.pq-row-delete td
{
    background-color:pink;   
}

tr.green td
{
    background: green;
    color: #fff;
}
tr td.green
{
    background: green;
    color: #000;
}
tr.lightgreen td
{
    background: lightgreen;
    color: #fff;
}
tr td.green
{
    background: lightgreen;
    color: #000;
}
tr.red td
{
    background:red;
    color: #fff;    
}
tr td.red
{
    background:red;
    color: #fff;
}
tr.lightred td
{
    color: red;  
}
tr td.lightred
{
    color: red;  
}
tr.black td
{
    background: #000;
    color: red;    
}
tr td.black
{
    background: #000;
    color: #fff;
}
tr.orange td
{
    background: #f9a839;
    color: #fff;    
}
tr td.orange
{
    background: #f9a839;
    color: #fff;
}
tr.blue td
{
    background: #00ccff;
    color: #fff;    
}
tr td.blue
{
    background: #00ccff;
    color: #fff;
}
tr.yellow td
{
    background: yellow;
    color: lightgreen;    
}
tr td.yellow
{
    background: yellow;
    color: lightgreen;
}
tr.gray td
{
    color: #ccc;    
}
tr td.gray
{
    color: #ccc;    
}

tr.lightorange td
{
    background: #fef5da;
    color: #000;    
}
tr td.lightorange
{
    background: #fef5da;
    color: #000;
}
tr.lightblue td
{
    background: #dbf8ff;
    color: #000;    
}
tr td.lightblue
{
    background: #dbf8ff;
    color: #000;
}
img.ui-datepicker-trigger
{
    margin-top:2px;
}
.ui-autocomplete {
    max-height: 200px;
    overflow-y: auto;
    /* prevent horizontal scrollbar */
    overflow-x: hidden;
}    
/*div.pq-grid :focus{
    outline:none;        
}*/
.pq-grid .pq-editor-focus
{
    outline:none;
    border:1px solid #bbb;    
    border-radius:6px;
    background-image: linear-gradient(#e6e6e6, #fefefe);

    filter: progid:DXImageTransform.Microsoft.gradient(startColorstr='#e6e6e6', endColorstr='#fefefe'); 
    background: -webkit-gradient(linear, left top, left bottom, from(#e6e6e6), to(#fefefe)); 
    background: -moz-linear-gradient(top,  #e6e6e6,  #fefefe); /* for firefox 3.6+ */	        
}
input.pq-date-editor
{
    padding:2px;vertical-align:bottom;width:78px;z-index:4;position:relative;
}
input.pq-ac-editor
{
    padding:2px;z-index:4;position:relative;
}

.pq-td-div ul
{
	list-style: none;
}

.pq-td-div ul li
{
	max-width: 180px;
	overflow: hidden;
	float: left;
	margin: 0;
	padding: 0;
	cursor: pointer;
	line-height: 19px;
	padding: 0 9px;
	background: url(skit/grid/images/pq-tag-bg.$5106.png) repeat-x 0 0;
	margin: 0 1px 1px 0;
	-moz-border-radius: 2px;
	-webkit-border-radius: 2px;
	border-radius: 2px;
	color: #eeeeee;
	white-space: nowrap;
	box-shadow: 0 1px 1px #b7b9bc;
}

.pq-td-div ul li span{
	font-size: 8pt;
}

.pq-td-div ul li a{
	text-decoration: none;
	padding-left: 5px;
	color: rgb(255, 255, 255);
}

div.pq-grid-summary{
        height:21px;border-bottom:1px solid #ddd;border-top:1px solid #fff;position:relative;
    }
div.pq-grid-summary table.pq-grid-table{
    position:absolute;top:0;left:-1px;
}

div.pq-grid-top {
    background: <ww:property value="themeColorLight"/>;
    color: #666;
}

.metadata_table {
	border: 1px solid #cfcfcf;
	width: 100%;
}

.metadata_table tr{
	border-bottom: 1px solid #efefef;
}

.metadata_table tr td{
	padding-left:5px;
	padding-top:5px;
	padding-bottom:5px;
}

</style>
<style type='text/css'>
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

.simple-modal .simple-modal-header h1 {
    margin: 0;
    color: #404040;
    font-size: 12px;
    font-weight: bold;
    font-family: "Helvetica Neue", Helvetica, Arial, sans-serif;
    line-height: 20px;
}
.simple-modal a.close, .simple-modal a.previous-image, .simple-modal a.next-image {
    position: absolute;
    top: 10px;
    color: #999;
    font-family: "Helvetica Neue", Helvetica, Arial, sans-serif;
    font-size: 22px;
    font-weight: normal;
    line-height: 10px;
    text-decoration: none;
}
.skit_mask {
   visibility: hidden;
   z-index: 99;
   background-color: #92A3B2;
   left: 0px;
   position: absolute;
   top: 0px;
   background-image: none;
   BORDER-TOP: 1px solid #405B40;
   BORDER-LEFT: 1px solid #405B40;
   BORDER-RIGHT: 1px solid #405B40;
   BORDER-BOTTOM: 1px solid #405B40;
   justify-content:center;
   align-items: center; 
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
</style>