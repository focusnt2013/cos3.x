<%@ page language="java" contentType="text/html; charset=UTF-8"  pageEncoding="UTF-8"%>
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
			            <p><strong><i class="fa fa-user fa-fw text-muted"></i> 开发者:  </strong>
			               <span class="tags" id="spanDigger" onclick='mailto(this)'>李佳齐</span>
			               <strong><i class="fa fa-clock-o fa-fw text-muted"></i> 最后修改时间:  </strong>
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
                         <ul class="dropdown-menu pull-right animated fadeInUp" role="menu" id='ulCompiler' style='cursor:pointer'>
                         	<li><a title='' onclick='doEdit(false)'><i class='skit_fa_icon fa fa-code fa-fw'></i>
                         		元数据模板XML脚本编辑</a></li>
                         	<li><a title='' onclick='doEdit(true)'><i class='skit_fa_icon fa fa-desktop fa-fw'></i>
                         		元数据模板可视化编辑</a></li>
                       		<li class="divider"></li>
                         	<li><a title='' onclick='downloadTemplate()'><i class='skit_fa_icon fa fa-download fa-fw'></i>
                         		下载该元数据模板 </a></li>
                       		<li class="divider"></li>
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
                         <ul class="dropdown-menu pull-right animated fadeInUp" role="menu" id='ulPreview' style='cursor:pointer'>
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
                         <ul class="dropdown-menu pull-right animated fadeInUp" role="menu" id='ulPreview' style='cursor:pointer'>
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
                         <ul class="dropdown-menu pull-right animated fadeInUp" role="menu" id='ulPublish' style='cursor:pointer'>
                         	<li><a title='' onclick='copyUrl()'><i class='skit_fa_icon fa fa-clipboard fa-fw'></i>
                         		复制元数据查询配置模板链接地址</a></li>
                         </ul>
                		</div>
                	</div>
	           	</div>
			</div>