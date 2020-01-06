package com.focus.cos.web;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.security.Key;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.crypto.Cipher;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.zookeeper.data.Stat;
import org.json.JSONArray;
import org.json.JSONObject;

import com.focus.cos.api.LogSeverity;
import com.focus.cos.api.LogType;
import com.focus.cos.api.Status;
import com.focus.cos.api.Syslog;
import com.focus.cos.api.SyslogClient;
import com.focus.cos.api.Sysnotify;
import com.focus.cos.api.SysnotifyClient;
import com.focus.cos.api.Sysuser;
import com.focus.cos.api.SysuserClient;
import com.focus.cos.web.common.COSConfig;
import com.focus.cos.web.common.Kit;
import com.focus.cos.web.common.PathFactory;
import com.focus.cos.web.common.ZKMgr;
import com.focus.cos.wrapper.SystemReport;
import com.focus.cos.wrapper.WrapperUpgrade;
import com.focus.util.Base64;
import com.focus.util.IOHelper;
import com.focus.util.Tools;
import com.focus.util.Zookeeper;

/**
 * 记录程序初始化版本
 * @author focus
 *
 */
public class Version implements Runnable
{
	private static final Log log = LogFactory.getLog(Version.class);
	public static String Remark = "【COS】云架构开放式应用服务框架，是一套基于DevOps框架思想能够用于开发与运维各种类型应用服务系统的软件框架容器。"+
			"该软件框架容器由前台【主界面框架系统】与后台【主控引擎】组成。"+
			"【主控引擎】具备跨平台、多进程、分布式等特性，能够在各种硬件服务器环境下运行，"+
			"支持包括JAVA、C语言程序、服务器脚本多种程序集成，实现多集群的网络、硬件、系统程序统一管理；"+
			"【主界面框架系统】提供全面的包括程序管理、系统管理、系统权限等界面功能，"+
			"业务系统开发者和维护人员通过登录该系统进行开发、集成与维护工作，"+
			"业务系统用户通过登录该系统操作开发集成的各项业务系统功能。";
	/*当前版本 */
	public static String[][] Versions = {
	{"0.7.1.1",		"产品概念建立，以实现技术积累，以及业务系统快速开发为目标，实现各种系统基础功能复用。"},
	{"1.8.10.31",	"1.0版本。根据电信EMA信息机需求进行了开发，实现初始的业务管控。"},
	{"1.9.1.1",		"基于移动研究院业务接入需求功能迭代。"},
	{"1.9.3.1",		"基于手机阅读业务项目需求功能迭代。"},
	{"1.9.9.1",		"基于江西电信手机阅读业务项目需求功能迭代，实现第一代系统监控。"},
	{"1.9.12.1",	"基于重庆电信手机阅读业务项目需求功能迭代，集成短信彩信WAP收发功能。"},
	{"2.10.8.1",	"2.0版本，首次引入主控后台，初代权限管理实现。"},
	{"2.10.8.1",	"基于巴基斯坦数据BI项目需求功能迭代，首次引入主控后台。"},
	{"2.10.10.23",	"基于联通信令监测总部数据平台功能迭代。"},
	{"2.10.12.5",	"基于上海联通数据报表系统功能迭代。"},
	{"2.11.3.3",	"基于通用报表系统产品功能迭代。"},
	{"2.11.9.1",	"基于Hadoop云计算产品功能迭代，强化系统监控功能。"},
	{"2.12.6.27",	"基于联通数据共享平台产品功能迭代，强化Hadoop、Hbase等第三方产品监控管理。"},
	{"3.13.9.9",	"3.0版本。积累了云计算相关产品支撑进行了重构，优化了系统监控权限管理等功能，申请了软件著作权。"},
	{"3.13.11.11",	"实现了初代网站类产品的后台支撑。"},
	{"3.14.2.7",	"新的系统监控权限控制架构。"},
	{"3.14.10.9",	"支持淘标人系列网站的支持。"},
	{"3.15.4.28",	"修复新的应用场景下各种适配问题。"},
	{"3.15.11.3",	"支撑综合业务平台，首次尝试在DevOps容器管理。"},
	{"3.16.7.28",	"重构h2数据库的前台启动逻辑，为了适配report+系统的启动，添加tcp启动模式。"},
	{"3.16.7.29",	"修改各项权限配置的错误。"},
	{"3.16.7.31",	"强化用户权限管理功能，修改通知内容字段存储类型。"},
	{"3.16.8.4",	"大重构告警、消息配置。"},
	{"3.16.8.9",	"告警配置与系统消息配置。"},
	{"3.16.8.10",	"告警配置优化与短信发件箱界面重构。"},
	{"3.16.8.24",	"增加数据库监控配置功能，重构WS服务架构，删除WAPPUSH功能模块。"},
	{"3.16.8.26",	"解决ZK不能写大数据的问题;修正工具栏菜单产品设计。"},
	{"3.16.8.28",	"解决监控中的一些BUG"},
	{"3.16.9.13",	"解决部分监控问题，优化系统告警界面，增加数据库管理界面入口"},
	{"3.16.9.21",	"监控优化;增加程序引擎调测监控功能"},
	{"3.16.9.22",	"实时告警历史告警页面重构，主界面框架消息通知重构"},
	{"3.16.9.24",	"增加监控模块的数据流量的监控功能特性。"},
	{"3.16.9.25",	"告警通知功能优化;监控图表单主机页面。"},
	{"3.16.9.26",	"告警邮件模板重构，告警单页面;系统通知页面重构。"},
	{"3.16.9.28",	"告警生成的问题;解决告警tips通知频繁的问题。"},
	{"3.16.10.3",	"增加新的告警——ZK程序和COS接口。"},
	{"3.16.10.6",	"Zookeeper集群监控改造。"},
	{"3.16.10.13",	"新的告警邮件模板。"},
	{"3.16.10.15",	"监控集群管理展示与配置。"},
	{"3.16.10.21",	"重构系统消息模块;系统监控界面重构，增加内存排序功能，增加内存汇总统计功能，调整菜单;修复系统告警确认功能的BUG。"},
	{"3.16.11.7",	"微信公众号配置管理。"},
	{"3.16.11.8",	"服务器管控权限配置。"},
	{"3.16.11.21",	"角色权限管理重构，增加二级以上权限功能，系统管理员组可以调整任意子角色所属父角色。"},
	{"3.16.11.27",	"升级Zookeeper的时候cos-web保持正常工作;集群按名称排序。"},
	{"3.16.12.1",	"增加新集群伺服器，自动添加到系统管理员组;解决非系统管理员组成员看告警的功能。"},
	{"3.16.12.3",	"适配第三方应用弹出新窗口。"},
	{"3.17.1.20",	"大重构全面升级框架技术。配置数据挪移到ZK中。"},
	{"3.17.1.24",	"实现菜单配置分级管理，系统管理员审核发布。"},
	{"3.17.2.6",	"用户管理角色管理以及COS架构大重构，删除原EMA项目内容。"},
	{"3.17.2.7",	"系统监控内置引擎与用户引擎监控分开。"},
	{"3.17.2.8",	"系统邮箱功能重构，数据源管理重构。解决新版本的几个BUG;在Zookeeper失效的情况下也能登录。"},
	{"3.17.2.9",	"干净程序初始安装后的逐步引导;系统邮件发件箱查询功能条件选择;解决几个初始化数据的问题;初始化的时候选择色彩样式和系统联系人。"},
	{"3.17.3.6",	"删除重构原换肤模版;"+
					"zookeeper管理重构，实现删除，与数据的预览功能;"+
					"zookeeper失效后的处理;"+
					"系统监控大改造：全屏显示;监控节点导航数据，管理员可新增删除与编辑集群目录;"+
					"节点操作二级节点记忆：总集群视图、集群视图、伺服器视图;集群视图包含服务器数量、程序数量等统计。"+
					"集群下服务器工作状况表;"+
					"删除伺服器;"+
					"自动迁移老版本伺服务器数据;"+
					"初始化的时候选择色彩样式和系统联系人;"+
					"重构文件资源管理器功能，删除文件和文件夹;"+
					"上传拷贝文件;文件路径输入快速定位;历史操作路径记忆功能;"+
					"实现各种文件预览以及编辑修改功能, jmimemagic集成;"+
					"初始化的时候输入联系人和选择风格样式，记忆样式选择，系统管理员可以选择修改全系统缺省"+
					"升级优化系统监控，将系统程序单独列表放;"+
					"重构401、404、403、500错误页面"+
					"权限树用户操作记忆功能，权限支持新的集群监控架构;"+
					"优化模块菜单配置。"},
	{"3.17.3.15",	"增加集群程序管理功能，实现主控可视化程序配置查询与监控功能，搭建基础的架构为后期扩容做准备;"+
					"增加集群程序管理版本管理功能，实现版本可配置并时间轴查询;"+
					"解决缺省的127.0.0.1伺服器根据配置监控自动隐藏功能。"},	
	{"3.17.4.10",	"集群程序管理，系统管理员和分配了权限的用户（同时要是开发者）可以看见该菜单，集群导航入口，显示该服务器下程序配置，每个程序可配置。"+
					"集群程序管理显示每个程序的工作状态，显示程序的基本信息和监控信息cpu、内存、硬盘、网络，可在这里入口日志，可发起程序配置版本时间树。实现每次版本的更新可见;"+
					"程序版本时间轴。确定每个程序数据版本可管理，cos由主界面框架系统和主控引擎组成，cos内置程序版本数据管理;"+
					"集群程序管理提供伺服器描述功能。可配置程序描述、标题、版本。版本选择通过版本管理配置数据获得;"+		
					"程序引擎配置与发布审核，为组件化做准备。待审数据序列化对象，在zk。已审数据在数据库。实现分级管理与审核。系统集群管理员有权限审核程序配置发布。配置实现树级管理。可真对节点进行单项配置;"+
					"开发者管理。配置用户成为指定模块子系统开发者。系统用户变成开发者后在开发者社区打开，用户可使用开发者社区的相关功能;"+
					"配置了伺服器权限的用户，在用户菜单栏出现系统监控入口。重新规划缺省用户菜单权限功能。"+
					"系统模块子系统管理可配置管理员。管理员从开发者中选择。去掉模块子系统缺省组件中某部分内容。"+
					"实现前后台确认升级，有开关控制升级，缺省是打开自动升级。实现版本特性同步。实现系统消息通知要升级和已升级。"+
					"首页重构增加各个缺省模块组件入口；实现模块导航介绍。增加各个重要的ops模块入口。"+
					"升级日志管理，本地用户可以看到自己用户相关的日志。日志表增加用户字段、增加上下文链接。实现运行日志功能。"
		},
	{"3.17.5.10",	"解决前个版本的某些BUG，主要包括本地接口访问使用127.0.0.1避免网络循环错误;"+
			"cos-server命令行终端，支持windows和linux，实现cos初始化配置与启动，支持COSPortal的自动下载与安装。"+
			"linux下安装脚本，自动配置服务，输入服务名称以及端口、以及相关参数如主控端口、服务程序配置。"	},
	{"3.17.5.11",	"解决升级程序第一次不成功的BUG;优化系统该消息通知频繁太多的问题;解决集群程序管理因为服务器标识路径不识别问题。"	},
	{"3.17.6.14",	"集群文件管理模块升级：支持文件夹压缩下载，压缩文件上传并自动在指定目录解压，支持文件或文件夹同步拷贝；实现对老的COS接口的兼容支持；解决一些遗留程序缺陷，包括数据库查询、Grid等问题。"	},
	{"3.17.7.19",	"程序开发管理模块上线，实现模块子系统开发管理;重构开发管理架构下的程序管理、菜单管理、公众号开发，以及系统发布。元数据查询配置框架优化，解决前以版本所涉及的多个问题。"	},
	{"3.17.8.4",	"元数据查询配置框架优化题，增加可视化创建与管理模板的;优化完善新架构下告警处理。"	},
	{"3.17.8.5",	"完善用户登录认证控制，实现登录错误计数，超过的限制其不能在指定时间内再次登录；登录认证的配置在系统参数配置管理中可以设置；长时间用户没有修改密码，提示用户修改密码。"	},
	{"3.17.8.17",	"解决元数据配置中h2的配置问题"},
	{"3.17.9.29",	"解决grid模块所引起的各种配置显示问题;解决微信公众号菜单配置问题."},
	{"3.17.10.12",	"新增grid模块显示大数字的特性;解决某些视图页面按钮样式问题."},
	{"3.17.10.13",	"新增grid模块自动聚合统计特性功能."},
	{"3.17.10.17",	"调整系统开发权限规则."},
	{"3.17.10.18",	"解决SQL保留字兼容问题;解决Grid扩展详情默认显示问题;解决角色给管理配置不能显示配置集群视图问题."},
	{"3.17.10.19",	"解决初始化缺失的系统管理的问题；解决下载请求不能记录的问题."},
	{"3.17.10.26",	"增加专业级的XML|JSON|JAVASCRIPT|CSS编辑器."},
	{"3.17.10.28",	"数据源管理数据测试;支持芒果的表预览;元数据模版配置版本管理;元数据模板导入导出管理."},
	{"3.17.10.30",	"增加在同一时间一个账号只允许一个人在登录状态的特性;增加元数据查询芒果count数据特性以及grid设置颜色特性."},
	{"3.17.10.31",	"增加元数据模块新特性：模板关联查询;模板全屏预览;模板预览时增加查看数据对象的功能"},
	{"3.17.11.1",	"增加元数据模块编辑JSON对象的特性；弃用原有的globalscript定义，老版本兼容。"},
	{"3.17.11.3",	"元数据模版编辑器增加查看对象与脚本的功能，可动态打开各个单元格的渲染器;根据条件设置单元格样式。"},
	{"3.17.11.4",	"元数据模版芒果聚合功能特性新增;解决模板编辑器的BUG。"},
	{"3.17.11.6",	"解决join的more属性配置不生效的问题;增加元数据查询模版beforeTableView属性"},
	{"3.17.11.10",	"系统参数配置增加禁用系统消息通知开关和主界面框架系统HTTPS开关;元数据模板开发改为默认改为全屏编辑模式"},
	{"3.17.11.14",	"同时多个人编辑开发元数据模版做版本合并提示；增加元数据模板tag的显示特性;增加DIGG进度条以及查看DIGG报告的特性;解决关闭元数据模板开发视图检查是否有修改的问题;重构元数据查询引擎，增加报表引擎特性；增加调试日志视图可查看功能；解决前一个版本的各种异常问题."},
	{"3.17.11.15",	"增加日期可以只选择年月的特性;解决日期选择的各种问题"},
	{"3.17.11.17",	"用户管理升级，子账号可以创建归属自己的子账号"},
	{"3.17.11.18",	"增加GridAction的Zookeeper跨域访问特性;增加用户管理新增用户组件模块;增加Grid组件打开Frame对话框特性"},
	{"3.17.11.19",	"增加GridAction的SQL标签特性;除了系统管理员用户查询只能查自己或自己创建的用户"},
	{"3.17.11.20",	"解决Digg模板导出功能存在的几个问题"},
	{"3.17.11.21",	"解决null值传参问题;解决聚合查询多个维度数据合并的问题;解决聚合查询中column传参问题;用户扩展参数继承特性新增，创建用户直接状态有效"},
	{"3.17.11.22",	"解决Grid导出模块问题;解决Join查询取最近一条记录;解决Group结合查询数据关联的因为Grid定义中没有无法关联上的问题"},
	{"3.17.11.23",	"解决模板编辑开发模块的比较器和排序问题，以及删除模板的对应tab窗口关闭问题."},
	{"3.17.11.24",	"解决导航窗口视图切换的时候不能回归导航点的问题;解决视图切换时回到模块子系统无法显示导航的问题;增加搜索菜单功能回车后直接打开;解决聚合查询条件错误关联处理;解决数据源预览因为传参架构引起的错误问题;解决移除模板编辑器中不可用问题;增加模板解析日志预览添加某些变量容错"},
	{"3.17.11.25",	"添加主界面框架系统的模板查看调测功能;备注与说明所有系统模板;模板查询视图优化，标签化显示所有开发者，调整模板字段顺序与显示渲染;新增Font-Awesome4.7版本"},
    { "3.17.12.1", "Grid-Digg新增工具栏条件过滤特性;增加系统安全接口管理，通过安全接口，第三方应用可以直接访问系统能力；新增模板使用情况查询，掌握近期模板使用状况;解决Grid-Digg的SQL语句匹配下的各种问题。" }, 
    { "3.17.12.5", "工具栏按钮重构添加对话框特性;解决接口安全访问跨域问题;改进模板编辑提示信息" }, 
    { "3.17.12.7", "解决安全接口访问新增用户回调问题；优化模板开发者显示" },
    { "3.17.12.27", "GRID-DIGG的接口框架，为开发者提供基于元数据模板的接口开发管理。" },
    { "3.17.12.28", "GRID-DIGG的接口管理统计使用情况。" },
    { "3.18.1.6", "集群主数据库主备管理，可视化管理COS的h2数据库。" },
    { "3.18.1.15", "集群主数据库管理增加数据备份的功能特性;解决操作系统字体库不支持引起的主界面框架系统验证码登录问题。" },
    { "3.18.1.18", "开放系统集群监控管理能力的接口。" },
    { "3.18.1.28", "解决集群监控管理出现的软件问题。" },
    { "3.18.2.1", "增加系统监控大数据支持。" },
    { "3.18.2.24", "解决模板目录导出导入改变path名称的问题。" },
    { "3.18.2.28", "重构系统监控伺服器监控界面，增加DashBoard;实现系统监控模块节点大数据存储支撑。" },
    { "3.18.3.1", "菜单配置唯一表示或URL不能够重复，避免菜单项出问题；解决菜单配置设置成功后菜单图标正确性问题。" },
    { "3.18.3.11", "支持堡垒机GateOne功能集成；解决初始化时候引导开发系统;界面实现完整的堡垒机远程控制管理。" },
    { "3.18.3.12", "解决新版本BUG;增加系统监控打开程序配置文件界面的功能。" },
    { "3.18.3.14", "文件管理的文本编辑功能优化，自动检测文件的字符编码；解决DIGG-API调测界面传参存在的一个换行错误。" },
    { "3.18.3.18", "重构我的系统接口管理，增加系统接口测试模块;解决系统参数配置中设置邮件密码的BUG;解决邮件发件箱的BUG；解决监控线程启心跳时间初始赋值的问题." },
    { "3.18.3.23", "系统配置文件编辑修改功能优化完善;增加系统监控主控和程序配置文件修改入口;增加系统程序管理配置文件修改入口;创建文件夹、创建文件" },
    { "3.18.3.27", "系统监控异常抛出定位;系统菜单配置重名检查;系统监控配置重名检查;解决JSON接口上报监控采集历史数据。" },
    { "3.18.3.28", "系统配置导出以及导入，以及自动恢复系统配置" },
    { "3.18.3.30", "解决ZK管理器在使用前初始化的问题，因为某些环境下会有延迟初始化的情况;解决关闭ZIP输出流的问题;ZK集群管理增加导入导出功能;增加ZK自动镜像备份功能;解决GridDigg解析inner中涉及%字符的问题" }
    ,{ "3.18.4.8", "GateOne堡垒机插件安全封装，系统管理员才能给打开GateOne工具栏界面，可以监控到所有用户的会话以及回播其操作日志，非系统管理员只能在角色权限授权下操作指定的服务器远程界面，非系统管理员【SSH远程控制】的入口在【系统监控】界面，系统管理员可以设置登录密码;僵尸进程管理系统监控每个伺服器每个程序的进程实例情况，通过重启可杀死僵尸进程，出现僵尸进程会致命告警。" }
    ,{ "3.18.4.9", "GridDigg模板配置支持动态数据加载的脚本编写;解决编辑器本地化问题;系统接口调用独立日志文件存储日志数据，错误情况记录系统日志数据库。" }
    ,{ "3.18.4.10", "系统监控视图展示死进程，同时可以查看变成死进程前后最后的日志。" }
    ,{ "3.18.4.11", "系统监控导航告警灯展示最严重的那一级，同时tips显示伺服器状况。" }
    ,{ "3.18.4.12", "GridDigg扩展数据ZK取值时出现问题;容错GridDIGG的Label构建的场景。" }
    ,{ "3.18.4.16", "GridDigg扩展数据不能提取所有数据的问题修复。" }
    ,{ "3.18.4.17", "GridDigg扩展Dashboard数据挖掘类型，支持SQL和Zookeeper。" }
    ,{ "3.18.4.20", "GridDigg界面模板增加图片上传控制，以及skit_message对话框优化。" }
    ,{ "3.18.4.24", "解决GridDigg两个问题，时间类型cell正确的时间范围查询，以及过滤搜索不在首页的BUG。" }
    ,{ "3.18.5.2", "解决相对路径所引起的伺服器不能访问自己的外网域名问题导致读取数据失败。" }
    ,{ "3.18.5.3", "解决GridDigg的左连接聚合解析引擎将clause和as写反了的问题。" }
    ,{ "3.18.5.9", "解决GridDigg的多层数据挖掘数字类型传参问题。" }
    ,{ "3.18.5.11", "增加GridDigg的日期变量函数的特性;解决数据库字段空字符串与空都有的情况兼容性问题。" }
    ,{ "3.18.5.15", "增加GridDigg的sqlwhere传参，扩展SQL模式下digg的特性，增加模板中对于条件参数为null的情况;解决微信公众组件一些BUG问题;解决左连接读取数据源数据库类型错误的问题;解决主表数据是芒果的左连接配置没有加载where的问题" }
    ,{ "3.18.5.18", "极大改进增强GridDigg对芒果对象数据库的支持；增加微信组件的用户统计和群发统计;解决不能通过界面创建模板的问题" }
    ,{ "3.18.5.22", "修改GridDigg中左连接SQL传参处理的缺陷问题，同时解决芒果数据库左连接问题，以及详情视图脚本关于data的控制问题;解决菜单配置模块添加菜单因为页面区域不够不能显示完的问题;dynamicParameter动态参数多个参数以';'分割" }
    ,{ "3.18.5.31", "修改GridDigg的数据模型类型为json的情况下构建本地URL的问题;解决IP地址映射实际地址问题后留存原IP信息的问题" }
    ,{ "3.18.6.12", "解决系统监控视图集群分组目录消失的问题" }
    ,{ "3.18.7.18", "优化资源管理器文件列表显示;Gateone节点新增错误;优化文件管理;优化告警管理，实现用户所属账号变更;实现CPU内存程序的监控展示，修复某些小BUG;升级系统监控图表和程序模块内存图表模块;解决备份的ZK文件不能清除的问题;解决主界面框架升级确认操作不成功问题;增加网络端口状态查询监控功能模块;集群SSH功能增加分组功能;增加集群SSH管理功能菜单添加;不需要赋予用户指定服务器的权限也可以打开堡垒机;调整点击查看监控Dashboard展示图表的功能" }
    ,{ "3.18.8.8", "解决SSH集群管理的入口初始化因为脏数据加载不成功的问题;监控初始化异常容错" }
    ,{ "3.18.9.6", "解决因为发送系统默认首封邮件超时造成的注册异常问题;解决Digg引擎通过URL获取json数据失败造成系统通知与系统告警界面打不开的问题。" }
    ,{ "3.18.11.12", "解决GateOne堡垒系统权限配置问题;系统管理员可以看到系统工作目录" }
    ,{ "3.19.1.14", "完善COS代理HTTP模块；GridDigg支持Redis数据库查询;解决GridDigg的编辑模式下保存Zookeeper对象中主键是非字符串类型支持的问题。" }
    ,{ "3.19.1.24", "增加http网页链接信息查询功能。实现post代理功能特殊处理广电总局网站;Digg添加join特性支持Reids数据源。" }
    ,{ "3.19.2.14", "增加GridDigg编辑修改sql数据库的特性;改进GridDigg的标签tag特性适配数据编辑模式;修正Digg引擎左连接加载多条数据条件映射的问题。" }
    ,{ "3.19.3.26", "数据库连接异常提示;解决对象Grid输入br或div特殊标签问题以及兼容保存json对象的功能;解决保存对象数据的问题;解决芒果数据库预置条件问题；解决微信公众号用户关注与取消关注的显示问题;增加Grid对经纬度翻译的支持，以及优化重构字段翻译功能，目前支持IP和经纬度;增加对芒果数据库的Grid数据编辑删除功能;修正微信公众号服务回调地址预览；移动已删除用户到专门目录，解决ID重复的菜单造成的异常问题;修复GridDigg异常处理产生的空指针问题;增加上传文件到指定FastDfs的特性功能;增加向指定ZK路径设置RSA的功能特性。" }
    ,{ "3.19.4.23", "增加GridDigg导出接口功能;增加GridDigg的数据Date循环特性;解决Digg的数据库密码加密问题;提升GridDigg的根据数据源数据产生动态单元格以及动态Digg的功能，优化芒果数据库的聚合功能。添加GridDigg的Label的芒果排序特性;解决GridDigg兼容性问题，当API调用的时候不加载details" }
    ,{ "3.19.5.10", "解决芒果数据库映射表构建问题；解决数据编辑中存在子对象、子列表对象无法保存到芒果数据库的问题。" }
    ,{ "3.19.6.26", "解决GridDigg中join条件动态参数问题；增加强制隐藏toolbar的特性；解决控制工具栏显示的问题;解决Join动态传参数问题." }
    ,{ "3.19.7.4", "增加JMAP和JStack分析JAVA进程的功能." }
    ,{ "3.19.7.29", "解决发送富文本邮件编码问题；解决Grid的$in条件的问题；增加Join的排序特性；增加开放digg特性;DiggApi增加值映射表特性;重要升级支持芒果数据库or条件配置; 增加DiggJoin的setcol特性; 颜色表参数可回调获取; 修复增长or特性引起的bug" }
    ,{ "3.19.8.24", "解决条件查询以及脚本问题，扩展按月纬度统计；弹窗模块支持非本地的Zookeeper数据源；获取数据源配置路径;系统安全接口查询用户的扩展属性。" }
    ,{ "3.19.9.27", "可以删除角色权限组配置;增加密码传参数创建账号功能; 解决Digg中芒果数据库查询条件嵌套传参问题。 { schedule_date : { $lt : %done_date%}}; 解决保存数据密码问题。" }
    ,{ "3.19.10.28", "单点登录特性实现; 优化条件中#变量#取值方法; 增加GridDigg表单模板特性支持用户自定义模板开发表单功能，支持ueditor组件,支持checkbox特性，支持嵌入模板，支持表单提价确认，实现批量文件上传与删除;解决h2数据库查询的问题;解决跨域创建用户的问题;修复一些细节问题。" }
    ,{ "3.19.11.28", "Form表单支持textarea; 解决邮件接口发送附件的URL地址问题; 解决添加系统用户默认权限组问题; 解决DIGG导出接口问题; 支持聚合字段转义; 添加并优化详情特性; 增加值映射表检测日志和解决过滤选择表单问题; 增加Grid变量隐藏字段配置; 解决芒果聚合排序问题; 解决动态条件传参问题；表单模板ZK数据模型编辑配置模板实现; 实现文件预览功能; 优化模板数据修改隐藏字段可以跳过; 模块子系统配置导出导入功能优化，直线模块子系统ID变化。" }
    ,{ "3.19.12.12", "增加GridDigg条件match特性，实现对动态场景状态适配; 为GridDigg模块表单增加onblur事件; 解决字段不一致的不同表同时聚合;实现Grid表单字段动态隐藏功能; 实现多层DIGG的聚合关联; 实现动态数据Join聚合查询" }
    ,{ "3.19.12.17", "细化文件预览组件；解决安全检查高位漏洞 URL重定向 弱点描述： Struts2在2.3.15.1之前版本对redirect或redirectAction后的参数过滤不严，存在多个开放重定向漏洞，攻击者通过构建特制的URL并诱使用户点击，利用这些漏洞将用户" }
    ,{ "3.19.12.24", "增加文件操作组件模块批量上传文件的功能。" }
	};
	/*上次升级的时间*/
	public static long LastUpgrade = 0;
	/*下次升级的时间*/
	public static long NextUpgrade = 0;
	/*上次升级可能产生的异常*/
	public static Exception LastUpgradeExccption;
	/*自动升级标识*/
	private static boolean AutoReboot = false;
	/**
	 * 构建版本的时间轴
	 * @param ip
	 * @param port
	 * @param sk
	 */
	private static String COSSecurityKey = null;
	public static String getCOSSecurityKey() {
		return COSSecurityKey;
	}
	public static void buildTimeline()
	{
		if( SyslogInitialized != null )
		{
			SyslogInitialized.setCategory("COSPortal");
			SyslogInitialized.setAccount(COSSecurityKey);
			SyslogInitialized.setLogseverity(LogSeverity.INFO.getValue());
			SyslogInitialized.setLogtype(LogType.运行日志.getValue());
			SyslogInitialized.setLogtext("主机界面框架系统初始化。");
			String dataurl = "control!costimeline.action?id=COSPortal&ip=127.0.0.1&port="+COSConfig.getLocalControlPort();
			SyslogInitialized.setContextlink("helper!timeline.action?dataurl="+Kit.chr2Unicode(dataurl));
			SyslogClient.submit(SyslogInitialized);
			SyslogInitialized = null;
		}
		Zookeeper zookeeper = null;
		try
		{
			File fileIdentity = new File(PathFactory.getDataPath(), "identity"); 
			//读取数字证书并初始化
			Key identity = (Key)IOHelper.readSerializable(fileIdentity);
	    	Cipher c = Cipher.getInstance("DES");
	        c.init(Cipher.WRAP_MODE, identity);//再用数字证书构建另外一个DES密码器
	        COSSecurityKey = Base64.encode(c.wrap(identity));
//			sk = Tools.encodeMD5(sk);
			zookeeper = ZKMgr.getZookeeper();
			String zkpath = "/cos/config/program/"+Tools.encodeMD5(COSSecurityKey)+"/version/COSPortal";
			Stat stat = zookeeper.exists(zkpath, false);
			JSONObject timeline = new JSONObject();
			JSONArray date = new JSONArray();
			timeline.put("date", date);
			timeline.put("name", "主界面框架系统");
			timeline.put("remark", Remark);
			for(String versions[] : Versions )
			{
				String version = versions[0];
				String text = versions[1];
				String args[] = Tools.split(version, ".");
				int year = Integer.parseInt(args[1]);
				int month = Integer.parseInt(args[2]);
				int day = Integer.parseInt(args[3]);
				Calendar calendar = Calendar.getInstance();
				calendar.setTimeInMillis(0);
				calendar.set(2000+year, month-1, day);
				JSONObject timeline0 = new JSONObject();
				timeline0.put("version", version);
				timeline0.put("time", Tools.getFormatTime("MM/dd/yyyy", calendar.getTimeInMillis()));
				timeline0.put("text", text);
				date.put(timeline0);
			}
			if( stat == null )
			{
				String[] args = zkpath.split("/");
				String path = "";
				for(String arg : args)
				{
					if( arg.isEmpty() ) continue;
					path += ("/"+arg);
					stat = zookeeper.exists(path, false);
					if( stat == null )
					{
						if( path.equals(zkpath) )
							zookeeper.create(zkpath, timeline.toString().getBytes("UTF-8"));
						else
							zookeeper.create(path, new byte[0]);
					}
				}
				log.info("Succeed to add the timeline of version for program CosPortal.");
			}
			else
			{
				log.info("Succeed to update the timeline of version for program CosPortal.");
				zookeeper.setData(zkpath, timeline.toString().getBytes("UTF-8"), stat);
			}
		}
		catch (Exception e)
		{
			COSSecurityKey = null;
			log.error("Failed to set the timeline of version for program CosPortal.", e);
		}
	}

	public String toString()
	{
		File vFile = new File(PathFactory.getWebappPath(), "version.txt");
		if( vFile.exists() )
		{
			String v = IOHelper.readFirstLine(vFile);
			if( v != null && v.startsWith("v") ){
				v = v.substring(1);
				return Tools.replaceStr(v, "_", ".");
			}
		}
		return Versions[Versions.length-1][0];
	}

	public static String getValue()
	{
		File vFile = new File(PathFactory.getWebappPath(), "version.txt");
		if( vFile.exists() )
		{
			String v = IOHelper.readFirstLine(vFile);
			if( v != null && v.startsWith("v") ) return v;
		}
		return "v"+Tools.replaceStr(Versions[Versions.length-1][0], ".", "_");
	}
	/*主界面框架系统的URL地址*/
	private static String Localhost = "";
	public static Syslog SyslogInitialized = new Syslog();
	public static String getLocalhost(){
		return Localhost;
	}
	
	/**
	 * 初始化完成
	 * @param url
	 */
	public static void initialized(String url)
	{
		Localhost = url;
	}
	
	/**
	 * 初始化
	 * @param context
	 */
	public static void check(String context)
	{
		Calendar c = Calendar.getInstance();
		c.set(Calendar.HOUR_OF_DAY, 4);
		c.add(Calendar.DAY_OF_MONTH, 1);
		NextUpgrade = c.getTimeInMillis();
		if( SyslogInitialized != null )
		{
			SyslogInitialized.setContext(context);
		}
	}

	public void run()
	{
      	System.out.println("#Version:"+toString());//修改
      	/**
		 * 以当前版本作为基线进行升级，每次打包cos.ide的时候都对该版本号做修改。
		 */
		try
		{
			if( Localhost == null || Localhost.isEmpty() ) return;//用户还没有访问过，不执行升级
			log.info("Execute report.");
			//TODO:上报系统信息
			systemReport();
			log.info("Upgrade to execute.");
			WrapperUpgrade wrapper = new WrapperUpgrade(PathFactory.getAppPath(), PathFactory.getWebappPath()){
				@Override
				public void notifyDownloadResult(
						File upgradedir,
						boolean succeed,
						boolean newvresion,
						String version,
						Exception e,
						boolean needReboot,
						String release,
						String logcontext)
				{
					Syslog syslog = new Syslog();
					syslog.setCategory("COSPortal");
					syslog.setAccount(COSSecurityKey);
					syslog.setLogseverity(LogSeverity.INFO.getValue());
					syslog.setLogtype(LogType.运行日志.getValue());
					syslog.setContext(logcontext);
					if( succeed && newvresion )
					{
						syslog.setContext("新特性包括："+release);
						log.info("Succeed to upgrade to "+version+" from "+getValue());
						File file = new File(upgradedir, "version.txt");
						IOHelper.writeFile(file, version.getBytes());

						ArrayList<Sysuser> users = SysuserClient.listUser(1, -1, Status.Enable.getValue());
						Sysnotify notify = new Sysnotify();
						notify.setFilter("系统升级");
						notify.setNotifytime(new Date());
						notify.setPriority(0);
						notify.setContext(release);
			    		String dataurl = "control!versiontimeline.action?id=COSPortal&ip=127.0.0.1&port="+COSConfig.getLocalControlPort();
						notify.setContextlink("helper!timeline.action?dataurl="+Kit.chr2Unicode(dataurl));

						boolean coside = false;
						String tag = PathFactory.getWebappPath().getAbsolutePath().toLowerCase();
						if( !tag.startsWith("d:\\focusnt\\cos\\trunk\\ide") &&
							!tag.startsWith("d:\\focusnt\\report\\trunk\\ide") )
						{
							coside = true;
						}
						File upgradeFlag = new File(PathFactory.getWebappPath(), "upgrade.aot");
						if( upgradeFlag.exists() || coside )
						{//自动升级被禁止发系统消息给管理员
							notify.setAction("立刻升级");
							notify.setActionlink("helper!upgrade.action");
							for(Sysuser u : users )
							{
								notify.setUseraccount(u.getUsername());
								notify.setTitle("成功下载了【主界面框架系统】的新版本["+version+"]请点击【立刻升级】，当前版本是["+getValue()+"]");
								SysnotifyClient.submit(notify);
							}
							syslog.setLogtext("成功下载了【主界面框架系统】的新版本["+version+"]需系统管理员确认执行升级后生效，当前版本是["+getValue()+"]。");
							SyslogClient.submit(syslog);
						}
						else
						{
							for(Sysuser u : users )
							{
								notify.setUseraccount(u.getUsername());
								notify.setTitle("【主界面框架系统】有新版本["+version+"]已自动升级，当前版本是["+getValue()+"]。");
								SysnotifyClient.submit(notify);
							}
							syslog.setLogtext("【主界面框架系统】有新版本["+version+"]已自动升级并重启生效，当前版本是["+getValue()+"]。");
							SyslogClient.submit(syslog);
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e1) {
							}
							if( AutoReboot && needReboot )
							{
								System.exit(0);//停止程序运行并重启，这回触发一些程序关闭
							}
						}
					}
					LastUpgrade = System.currentTimeMillis();//设置上次升级时间
					if( !newvresion )
					{
						log.info("No new version.");
					}
					if( e != null )
					{
						syslog.setLogtext("【主界面框架系统】版本["+getValue()+"]升级出现异常"+e.getMessage());

						ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
						PrintStream ps = new PrintStream(out);
						e.printStackTrace(ps);
						syslog.setContext(out.toString());
						SyslogClient.submit(syslog);
						log.info("Failed to upgrade to for exception ", e);
					}
					else
					{
						LastUpgradeExccption = null;
					}
				}

				@Override
				public void notifyDownloadProgress(int arg0) {
				}
			};
			wrapper.report();
			File upgradeFlag = new File(PathFactory.getWebappPath(), "upgrade.aot");
			File upgradePath = PathFactory.getWebappPath();
			if( upgradeFlag.exists() )
			{
				upgradePath = new File(PathFactory.getAppPath(), "temp/upgrade/web");
				if( !upgradePath.exists() ) upgradePath.mkdirs();
				AutoReboot = false;
			}
			else AutoReboot = true;
			wrapper.download("web", getValue(), upgradePath, false);
			LastUpgrade = System.currentTimeMillis();//设置上次升级时间
		}
		catch(Exception e)
		{
			log.debug("Failed to start up upgrade for exception ", e);
			LastUpgradeExccption = e;
		}
	}
	
	/**
	 * 版本升级提示信息
	 * @return
	 */
	public static String getVersionUpgradeInfo()
	{
		StringBuffer sb = new StringBuffer();
		if( AutoReboot ) sb.append("[COS]");
		else sb.append("[IDE]");
		
		File upgradeFlag = new File(PathFactory.getWebappPath(), "upgrade.aot");
		if( upgradeFlag.exists() )
		{
			sb.append("自动升级已关闭");
		}
		else
		{
			sb.append("自动升级已打开");
		}
		sb.append(";下次自动升级检测时间"+Tools.getFormatTime("yyyy-MM-dd HH:mm", NextUpgrade));
		
		if( LastUpgrade > 0 )
		{
			sb.append(";上次升级时间"+Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", LastUpgrade));
			if( LastUpgradeExccption != null )
			{
				sb.append("，出现异常"+LastUpgradeExccption);
			}
		}
		return sb.toString();
	}
	
	/**
	 * 比较两个版本号如果当前版本大于或等于基线版本这匹配
	 * @param current
	 * @param baseline
	 * @return current > baseline
	 */
	public static boolean match(String current, String baseline)
	{
		if( current == null ) return false;
		if( baseline == null ) return true;
		byte[] b = new byte[4];
		String args[] = Tools.split(current, ".");
		b[0] = args.length>0?Byte.valueOf(args[0]):0;
		b[1] = args.length>1?Byte.valueOf(args[1]):0;
		b[2] = args.length>2?Byte.valueOf(args[2]):0;
		b[3] = args.length>3?Byte.valueOf(args[3]):0;
		int l = Tools.bytesToInt(b);
		args = Tools.split(baseline, ".");
		b[0] = args.length>0?Byte.valueOf(args[0]):0;
		b[1] = args.length>1?Byte.valueOf(args[1]):0;
		b[2] = args.length>2?Byte.valueOf(args[2]):0;
		b[3] = args.length>3?Byte.valueOf(args[3]):0;
		int r = Tools.bytesToInt(b);
		return l >= r;
	}
	
	/**
	 * 执行系统报告
	 */
	public void systemReport()
	{
    	Zookeeper zookeeper = null;
    	JSONObject request = new JSONObject();
		try
		{
//	    	if( name != null && !name.isEmpty() ) request.put("Name", name);
//	    	if( description != null && !description.isEmpty() ) request.put("Description", description);
			zookeeper = ZKMgr.getZookeeper();
			JSONObject sysCfg = zookeeper.getJSONObject("/cos/config/system");
			if( sysCfg != null )
			{
				request.put("Version", getValue());
				request.put("SysName", sysCfg.getString("SysName"));
				request.put("SysDescr", sysCfg.getString("SysDescr"));
				request.put("License", sysCfg.getString("SysObjectID"));
				request.put("POP3Username", sysCfg.getString("POP3Username"));
				request.put("SysMailName", sysCfg.getString("SysMailName"));
				request.put("SysContact", sysCfg.has("SysContact")?sysCfg.getString("SysContact"):"");
				request.put("SysContactName", sysCfg.has("SysContactName")?sysCfg.getString("SysContactName"):"");
				JSONObject sftCfg = zookeeper.getJSONObject("/cos/config/software");
				if( sftCfg != null )
				{
					request.put("SoftwareName", sftCfg.getString("SoftwareName"));
					request.put("SoftwareVersion", sftCfg.getString("SoftwareVersion"));
					request.put("SoftwareVendor", sftCfg.getString("SoftwareVendor"));
					request.put("Copyright", sftCfg.getString("Copyright"));
				}
				List<JSONObject> nodes = zookeeper.getJSONObjects("/cos/data/apiproxy");
				SystemReport systemReport = new SystemReport(PathFactory.getAppPath(), request, nodes);
				systemReport.report();
			}
			log.info("Succeed to report the system.");
		}
		catch (Exception e)
		{
			log.error("Failed to report the system for "+e);
		}
	}
}
