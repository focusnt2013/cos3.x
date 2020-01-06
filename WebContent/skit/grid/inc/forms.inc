<ww:iterator value="forms" status="loop">
<ww:if test='"text".equals(type)||"password".equals(type)'>
<div class="form-group" id="form_group_<ww:property value='dataIndx'/>">
	<div class="input-group" style='<ww:property value="groupStyle"/>'>
		<ww:if test='!nullable'>
		<div class="input-group-btn">
        	<div id='btnCheck-<ww:property value='dataIndx'/>' class="btn btn-danger form-check"><i class='fa fa-star' style='font-size:12px;margin-top:4px'></i></div>
        </div>
        </ww:if>
		<span class="input-group-addon" style='border-right:0px;'><ww:property value="title"/><span class='fa <ww:property value="icon"/>' style='margin-left:6px'></span></span>
		<input class="form-control form-input" type="<ww:property value='type'/>" <ww:property value="readonly"/>
			style='<ww:property value="style"/>'
			onblur='onblurTrigger(this)'
			onfocus='onfocusTrigger(this)'
			name='<ww:property value='dataIndx'/>' 
			id='<ww:property value='dataIndx'/>' 
			data-title='<ww:property value='placeholder'/>' data-toggle="tooltip" data-placement="bottom" data-trigger='manual'
			placeholder="<ww:property value='placeholder'/>">
		<ww:if test='!label.equals("")'>
		<span class="input-group-addon remark"><ww:property value='label'/></span>
		</ww:if>
	</div>
</div>
<SCRIPT type="text/javascript">
<ww:if test='"datetimepicker".equals(plugin)'>
try{
	var format = '<ww:property value="format" escape="false"/>';
	$.datetimepicker.setLocale('zh');
	var defaultDate = new Date();
	if( formData["<ww:property value='dataIndx'/>"] ){
		defaultDate = formData["<ww:property value='dataIndx'/>"];
	}
	$("#<ww:property value='dataIndx'/>").datetimepicker({
		format: format,
		timepicker: false,
		defaultDate: defaultDate,
		onSelectDate:function(ct,$i){
			var onselect = <ww:property value='onselect' escape='false'/>;
			var i = document.getElementById("<ww:property value='dataIndx'/>");
			onselect(i);
		}
	});
}
catch(e){
	alert("初始化日期组件出现异常"+e.message+", 行数"+e.lineNumber);
}
</ww:if>
if( '<ww:property value="mask"/>' ){
	try{
		var mask = '<ww:property value="mask" escape="false"/>';
		if( mask.charAt(0) == '{'  ){
			var json = jQuery.parseJSON(mask);
			$("#<ww:property value='dataIndx'/>").inputmask();
		}
		else{
			if( mask == "yyyy-mm-dd" ){
				var cfg = {};
				cfg.alias = "datetime";
				cfg.inputFormat = mask;
				$("#<ww:property value='dataIndx'/>").inputmask(cfg);
			}
			else{
				$("#<ww:property value='dataIndx'/>").inputmask(mask);
			}
		}
	}
	catch(e){
		alert("执行表格mask初始化出现异常"+e.message+", 行数"+e.lineNumber);
	}
}
onBlurFunctions["<ww:property value='dataIndx'/>"] = <ww:property value='onblur' escape='false'/>;
onFocusFunctions["<ww:property value='dataIndx'/>"] = <ww:property value='onfocus' escape='false'/>;
</SCRIPT>
</ww:if>
<ww:if test='"textarea".equals(type)'>
<div class="form-group" id="form_group_<ww:property value='dataIndx'/>">
	<div class="input-group" style='margin-bottom:5px;<ww:property value="groupStyle"/>'>
		<ww:if test='!nullable'>
		  <div class="input-group-btn">
        	<div id='btnCheck-<ww:property value='dataIndx'/>' class="btn btn-danger form-check"><i class='fa fa-star' style='font-size:12px;margin-top:4px'></i></div>
          </div>
        </ww:if>
		<span class="input-group-addon" style=''><ww:property value="title"/><span class='fa <ww:property value="icon"/>' style='margin-left:6px'></span></span>
		<ww:if test='!label.equals("")'>
		<span class="input-group-addon remark" style='border-left:1px solid #e0e0e0;'><ww:property value='label'/></span>
		</ww:if>
	</div>
	<textarea class="form-control form-textarea" rows='<ww:property value="rows"/>' <ww:property value="readonly"/>
		style='<ww:property value="style"/>'
		onblur='onblurTrigger(this)'
		onfocus='onfocusTrigger(this);'
		name='<ww:property value="dataIndx"/>' 
		id='<ww:property value="dataIndx"/>' 
		data-title='<ww:property value="placeholder"/>' 
		data-toggle="tooltip" data-placement="bottom" data-trigger='manual'
		placeholder="<ww:property value='placeholder'/>"		
		></textarea>
</div>
</ww:if>
<ww:if test='"select".equals(type)'>
<div class="form-group" id="form_group_<ww:property value='dataIndx'/>">
	<div class="input-group" style='<ww:property value="groupStyle"/>'>
		<ww:if test='!nullable'>
		<div class="input-group-btn">
        	<div id='btnCheck-<ww:property value='dataIndx'/>' class="btn btn-danger form-check"><i class='fa fa-star' style='font-size:12px;margin-top:4px'></i></div>
         </div>
         </ww:if>
		<span class="input-group-addon" style='border-right:0px;'><ww:property value="title"/><span class='fa <ww:property value="icon"/>' style='margin-left:6px'></span></span>
		<select class="form-control" name='<ww:property value="dataIndx"/>' id='<ww:property value="dataIndx"/>' onchange="select_<ww:property value='dataIndxMd5'/>(this)" <ww:property value="readonly"/>>
			<ww:iterator value="options" status="loop">
			<option value='<ww:property value="value"/>' title='<ww:property value="depend"/>'><ww:property value="label"/></option>
			</ww:iterator>
		</select>
		<ww:if test='!label.equals("")'>
		<span class="input-group-addon remark"><ww:property value='label'/></span>
		</ww:if>
	</div>
</div>
<SCRIPT type="text/javascript">
function select_<ww:property value='dataIndxMd5'/>(i){
	selectTrigger(i);
	var onchange = <ww:property value='onchange' escape='false'/>;
	onchange(i);
	var filter = "<ww:property value='filter'/>";
	if( filter ){
		var sel = document.getElementById(filter);
		if( sel ){
			var firstOption;
			var indxSelected = false;
			var options = sel.options;
			for(var j = 0; j < options.length; j++ ){
				var o = options[j];
				var display = o.title&&o.title==i.value?"":"none";
				o.style.display = display;
				if( o.selected && display ){
					indxSelected = j;
					o.selected = false;
				}
				if( !display && !firstOption ){
					firstOption = o;
				}
			}
			if( indxSelected < options.length && options[indxSelected].selected ){
				if( options[indxSelected].style.display == "none" ){
					firstOption.selected = true;
				}
			}
		}
	}
}
if( "<ww:property value='filter'/>" || <ww:property value='init'/> ){
	window.setTimeout(function(){
		select_<ww:property value='dataIndxMd5'/>(document.getElementById("<ww:property value='dataIndx'/>"));
	}, 1000);
}
</SCRIPT>
</ww:if>
<ww:if test='"radio".equals(type)'>
<div class="form-group" id="form_group_<ww:property value='dataIndx'/>">
	<div class="input-group" style='<ww:property value="groupStyle"/>'>
		<ww:if test='!nullable'>
		<div class="input-group-btn">
        	<div id='btnCheck-<ww:property value='dataIndx'/>' class="btn btn-danger form-check"><i class='fa fa-star' style='font-size:12px;margin-top:4px'></i></div>
         </div>
         </ww:if>
		<span class="input-group-addon" style=''><ww:property value="title"/><span class='fa <ww:property value="icon"/>' style='margin-left:6px'></span></span>
		<ww:iterator value="options" status="loop">
		<span class='input-group-addon form-checkbox' title="<ww:property value='tips'/>">
			<div class='checkbox checkbox-info' style='margin-top:0px;margin-bottom:0px;'>
				<input type='checkbox' style='cursor:pointer;'
					name="<ww:property value='dataIndx'/>"
					id="<ww:property value='label'/>"
					value="<ww:property value='value'/>"
					onclick="radioTrigger(this)">
				<label><ww:property value='label'/></label>
			</div>
		</span>
		</ww:iterator>
		<ww:if test='!label.equals("")'>
		<span class="input-group-addon remark" style='border-left:1px solid #e0e0e0;'><ww:property value='label'/></span>
		</ww:if>
		<ww:else>
		<span class="input-group-addon remark" style='border-left:1px solid #e0e0e0;'></span>
		</ww:else>
	</div>
</div>
</ww:if>
<ww:if test='"checkbox".equals(type)'>
<div class="form-group" id="form_group_<ww:property value='dataIndx'/>">
	<div class="input-group" style='<ww:property value="groupStyle"/>margin-bottom:0px;'>
		<ww:if test='!nullable'>
		<div class="input-group-btn" style='border-bottom:0px;border-bottom-left-radius:0px;'>
        	<div id='btnCheck-<ww:property value='dataIndx'/>' class="btn btn-danger form-check"
        		style='border-bottom-left-radius:0px;'><i class='fa fa-star' style='font-size:12px;margin-top:4px'></i></div>
         </div>
         </ww:if>
		<span class="input-group-addon" style='border-bottom:0px;border-bottom-left-radius:0px;'><ww:property value="title"/><span class='fa <ww:property value="icon"/>' style='margin-left:6px'></span></span>
		<ww:if test='!label.equals("")'>
		<span class="input-group-addon remark" style='border-bottom:0px;background-color:#fff;border-bottom-right-radius:0px;'><ww:property value='label'/></span>
		</ww:if>
		<ww:else>
		<span class="input-group-addon remark" style='border-bottom:0px;background-color:#fff;border-bottom-right-radius:0px;'></span>
		</ww:else>
	</div>
	<div style='border:1px solid #e0e0e0;'>
		<ul style="list-style:none;margin-top:16px;width:100%;height:<ww:property value='height'/>px;">
		<ww:iterator value="options" status="loop">
		<li class='li-checkbox' style="width:<ww:property value='width'/>px;" title="<ww:property value='tips'/>">
			<div class='checkbox checkbox-info' style='margin-top:0px;margin-bottom:0px;'>
				<input type='checkbox' style='cursor:pointer;'
					name="<ww:property value='dataIndx'/>"
					id="<ww:property value='label'/>"
					value="<ww:property value='value'/>"
					onclick="checkboxTrigger(this)">
				<label><ww:property value='label'/></label>
			</div>
		</li>
		</ww:iterator>
		</ul>
	</div>
</div>
</ww:if>
<ww:if test='"file".equals(type)'>
<div class='file-loading'>
	<input name="uploadfile" <ww:property value='multiple'/> <ww:property value='webkitdirectory'/> id="file-<ww:property value='dataIndx'/>" type="file">
	<SCRIPT type="text/javascript">
	openFileFunctions["<ww:property value='dataIndx'/>"] = function(){
		var data = formData["<ww:property value='dataIndx'/>"];
		/*
		data = [
	        {caption: "Moon.jpg", downloadUrl: "http://upload.wikimedia.org/wikipedia/commons/thumb/e/e1/FullMoon2010.jpg/631px-FullMoon2010.jpg", size: 930321},
	        {caption: "Earth.jpg", downloadUrl: "http://upload.wikimedia.org/wikipedia/commons/thumb/6/6f/Earth_Eastern_Hemisphere.jpg/600px-Earth_Eastern_Hemisphere.jpg", size: 1218822}
	    ];
		data = "http://upload.wikimedia.org/wikipedia/commons/thumb/e/e1/FullMoon2010.jpg/631px-FullMoon2010.jpg";
		*/
	    var uploadUrl = <ww:property value='uploadUrl' escape='false'/>;
	    var multiFileUpload = <ww:property value='multiFileUpload'/>;
	    var reversePreviewOrder = <ww:property value='reversePreviewOrder' escape='false'/>;
	    var hasUploadUrl = uploadUrl()?true:false;
		var deleteUrl = <ww:property value='deleteUrl' escape='false'/>;
	    var hasDeleteUrl = deleteUrl()?true:false;
		var initialCaption = "<ww:property value='label' escape='false'/>";
		var uploadExtraData = <ww:property value='uploadExtraData' escape='false'/>;
		var deleteExtraData = <ww:property value='deleteExtraData' escape='false'/>;
		var maxFileCount = <ww:property value='maxFileCount'/>;
		var id = "#file-<ww:property value='dataIndx'/>";
		var allowedFileExtensions = <ww:property value='allowedFileExtensions' escape='false'/>;
		var cfg = {
	        language: 'zh',
	        overwriteInitial: false,
		    allowedFileExtensions: allowedFileExtensions,
		    dropZoneEnabled: true,
			showCaption: true, //是否显示标题
			showClose: false,
			showPreview: true,
			showBrowse: false,
			showDrag: reversePreviewOrder,
			reversePreviewOrder: reversePreviewOrder,
			showUploadStats: false,
			showUpload: false,
			uploadAsync: true,
			preferIconicPreview: false,
		    previewFileIconSettings: { // configure your icon file extensions
		        'doc': '<i class="fa fa-file-word-o text-primary"></i>',
		        'xls': '<i class="fa fa-file-excel-o text-success"></i>',
		        'ppt': '<i class="fa fa-file-powerpoint-o text-danger"></i>',
		        'pdf': '<i class="fa fa-file-pdf-o text-danger"></i>',
		        'zip': '<i class="fa fa-file-zip-o text-muted"></i>',
		        'htm': '<i class="fa fa-file-code-o text-info"></i>',
		        'txt': '<i class="fa fa-file-text-o text-info"></i>',
		        'mov': '<i class="fa fa-file-video-o text-warning"></i>',
		        'mp3': '<i class="fa fa-file-audio-o text-warning"></i>',
		        'pem': '<i class="fa fa-file text-danger"></i>',
		        // note for these file types below no extension determination logic 
		        // has been configured (the keys itself will be used as extensions)
		        'jpg': '<i class="fa fa-file-image-o text-danger"></i>', 
		        'gif': '<i class="fa fa-file-image-o text-muted"></i>', 
		        'png': '<i class="fa fa-file-image-o text-primary"></i>'    
		    },
		    previewFileExtSettings: { // configure the logic for determining icon file extensions
		        'doc': function(ext) {
		            return ext.match(/(doc|docx)$/i);
		        },
		        'xls': function(ext) {
		            return ext.match(/(xls|xlsx)$/i);
		        },
		        'ppt': function(ext) {
		            return ext.match(/(ppt|pptx)$/i);
		        },
		        'zip': function(ext) {
		            return ext.match(/(zip|rar|tar|gzip|gz|7z)$/i);
		        },
		        'htm': function(ext) {
		            return ext.match(/(htm|html)$/i);
		        },
		        'txt': function(ext) {
		            return ext.match(/(txt|ini|csv|java|php|js|css)$/i);
		        },
		        'mov': function(ext) {
		            return ext.match(/(avi|mpg|mkv|mov|mp4|3gp|webm|wmv)$/i);
		        },
		        'mp3': function(ext) {
		            return ext.match(/(mp3|wav)$/i);
		        },
		        'pem': function(ext) {
		            return ext.match(/(pem|p12)$/i);
		        }
		    },
			browseClass: "btn btn-success",
			maxFileCount: maxFileCount,
			maxFileSize: <ww:property value='maxFileSize'/>
		};
		var preferIconicPreview = false;
		var initialPreviewAsData = true;
		//initialCaption
		if( data ){
			var initialPreview = <ww:property value='initialPreview' escape='false'/>;
			cfg.initialPreview = [];
			data = initialPreview(data, cfg.initialPreview);
			if( typeof data == 'object' ){
				var array = [], o;
				cfg.initialPreview = [];
				var havePush = cfg.initialPreview.length?true:false;//已经插入预览
				if( data.length ){
					var k = data.length - 1;
					for(var i = 0; i < data.length; i++){
						o = data[i];
						o.key = (i+1);
						if( !o.width ){
							o.width = "120px";
						}
						o.showRemove = hasDeleteUrl?true:false;
						if( o.downloadUrl ){
							cfg.initialPreview[k-i] = o.downloadUrl;
						}
						array[k-i] = o;
						var type = buildPreview(o);
						if( o.caption ){
							if( o.caption.indexOf(type) == -1 ){
								o.caption += type;
							}
						}
						if( o.type == 'text' || o.type == 'html' ){
							initialPreviewAsData = false;
							preferIconicPreview = true;
						}
					}
					for(var i = 0; i < array.length; i++){
						o = array[i];
						if( preferIconicPreview && (o.type == 'text' || o.type == 'html' || o.type == 'pdf') ){
							cfg.initialPreview[i] = "<iframe src='"+o.downloadUrl+"' style='border:0;width:100%;height:100%;'></iframe>";
						}
					}
				}
				else if( data.downloadUrl ){
					o = data;
					o.key = 1;
					if( !o.width ){
						o.width = "120px";
					}
					o.showRemove = hasDeleteUrl?true:false;
					var type = buildPreview(o);
					if( o.caption ){
						if( o.caption.indexOf(type) == -1 ){
							o.caption += type;
						}
					}
					if( o.type == 'text' || o.type == 'html' ){
						initialPreviewAsData = false;
						preferIconicPreview = true;
					}
					if( preferIconicPreview && (o.type == 'text' || o.type == 'html' || o.type == 'pdf') ){
						cfg.initialPreview[0] = "<iframe src='"+o.downloadUrl+"' style='border:0;width:100%;height:100%;'></iframe>";
					}
					else{
						cfg.initialPreview[0] = o.downloadUrl;
					}
					array[0] = o;
				}
				cfg.initialPreviewConfig = array;
			}
			else{
				hasDeleteUrl = cfg.maxFileCount>1?true:false;
				var o = {
					downloadUrl: data,
					size: 0,
					width: "120px",
					showRemove: hasDeleteUrl,
					key: 1
				};
				var i = data.lastIndexOf("/");
				if( i != -1 ){
					o.caption = data.substring(i+1);
				}
				i = data.lastIndexOf(".");
				if( i != -1 ){
					var type = buildPreview(o);
					if( o.type == 'text' || o.type == 'html' ){
						initialPreviewAsData = false;
						preferIconicPreview = true;
					}
					if( preferIconicPreview && (o.type == 'text' || o.type == 'html' || o.type == 'pdf') ){
						cfg.initialPreview[0] = "<iframe src='"+o.downloadUrl+"' style='border:0;width:100%;height:100%;'></iframe>";
					}
					else{
						cfg.initialPreview[0] = o.downloadUrl;
					}
	                o.caption = "<ww:property value='title'/>"+type;
				}
				cfg.initialPreviewConfig = [o];
			}
			//cfg.preferIconicPreview = true;
		}
		cfg.preferIconicPreview = preferIconicPreview;
		cfg.initialPreviewAsData = initialPreviewAsData;
		if( hasUploadUrl ){
			cfg.uploadUrl = uploadUrl;
			cfg.showUploadedThumbs = false;
			cfg.showUpload = cfg.maxFileCount>1?false:true;
			if( multiFileUpload ){
				cfg.showUpload = true;
			}
			cfg.showBrowse = true;
			//cfg.showUploadStats = true;
		}
		if( hasDeleteUrl ){
			cfg.deleteUrl = deleteUrl;
		}
		if( initialCaption ){
			cfg.initialCaption = initialCaption;
		}
		if( multiFileUpload ){
		    cfg.multiFileUpload = multiFileUpload;
			//cfg.allowedPreviewTypes = null;
		}
		cfg.uploadExtraData = uploadExtraData;
		cfg.deleteExtraData = deleteExtraData;
		$(id).fileinput(cfg);
		if( !hasUploadUrl ){
			$(".file-caption-main").hide();
		}

		if( hasUploadUrl ){
			$(id).on('fileloaded', function(event, file, previewId, index) {
				var fileloaded = <ww:property value='fileloaded' escape='false'/>;
				try{
					fileloaded(event, file, previewId, index);
				}
				catch(e){
					alert("文件上传加载异常"+e.message+", 行数"+e.lineNumber);					
				}
			});
			$(id).on('fileuploaderror', function(event, data, msg) {
				//alert(msg);
			});
			$(id).on('filebeforeload', <ww:property value='filebeforeload' escape='false'/>);
			$(id).on('filepreupload', <ww:property value='filepreupload' escape='false'/>);
			$(id).on('filebatchpreupload', <ww:property value='filebatchpreupload' escape='false'/>);
			$(id).on('fileuploaded', function(event, result, previewId, index){
				try{
					var data = result.response;
	    			if(data.errcode){
	    			    skit_alert(data.errmsg);
	    			}else{
	    				var skipfileuploaded = "<ww:property value='skipfileuploaded'/>";
	    				if( maxFileCount == 1 && skipfileuploaded == "yes" ){//单张图片的时候这样处理
	    					formData["<ww:property value='dataIndx'/>"] = data.url;
	    					$(id).fileinput('destroy');
	    					var func = openFileFunctions["<ww:property value='dataIndx'/>"];
	    					if( func ){
	    						window.setTimeout(function(){
	        						func();
	    						}, 500);
	    					}
	    				}
	    			}
					var fileuploaded = <ww:property value='fileuploaded' escape='false'/>;
					fileuploaded(event, result, previewId, index);
				}
				catch(e){
					alert(e);
				}
			});
		}
		if( reversePreviewOrder ){
			$(id).on('filesorted', function(event, param) {
				try{
					var filesorted = <ww:property value='filesorted' escape='false'/>;
					filesorted(event, param);
				}
				catch(e){
					alert(e);
				}
			});
		}
	    $(id).on('fileremoved', function(event, id, index) {
	    });
	    if( hasDeleteUrl ){
	    	$(id).on('filebeforedelete', function(event, key, data) {
	    		//top.showJson(data);
	    		var filebeforedelete = <ww:property value='filebeforedelete' escape='false'/>;
	    		if( filebeforedelete(event, key, data) ){
	    			return new Promise(deleteConfirm);
	    		}
	    		return true;
		    });
//		    $(id).on('filepredelete', function(event, key, jqXHR, data) {
//		    });
//			$(id).on('filedeleteerror', function(event, data, msg) {
//				top.showJson(data);
//			});
		    $(id).on('filedeleted', <ww:property value='filedeleted' escape='false'/>);
	    }
	}
	</SCRIPT>
</div>
</ww:if>
<ww:if test='"ueditor".equals(type)'>
<script id="<ww:property value='dataIndx'/>" type="text/plain" style="height:<ww:property value='height'/>px;margin:0 auto;"></script>
</ww:if>
<ww:if test='"hidden".equals(type)'>
<input type='hidden' 
	name='<ww:property value="dataIndx"/>' 
	id='<ww:property value="dataIndx"/>' 
>
</ww:if>
</ww:iterator> 