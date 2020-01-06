create table if not exists TB_USER 
(
	id					int					not null		PRIMARY KEY,
	roleid				tinyint				not null,
	username			varchar(256)		not null,
	password			varchar(64)			not null,
	realname			varchar(32)			not null,
	email				varchar(32),
	status				tinyint,
	sex					tinyint,
	lastLogin			datetime,
	lastChangePassword	datetime,
	errorCount			int(4)		default 0,
	historyPassword		varchar(2048),
	lastLoginIp			varchar(64),
	lastLoginRegion		varchar(256),
	creator				varchar(256),
	primary key (id)
)ENGINE=INNODB DEFAULT CHARSET=utf8;

create table if not exists TB_SYSLOG 
(
	logid			bigint unsigned		not null		PRIMARY KEY,
	logtype			int					not null,
	logseverity		int					not null,
	logtime			datetime			not null,
	logtext			varchar(2048)		not null,
	category		varchar(128),
	account			varchar(64),
	contextlink		varchar(1024),
	context			text,
	primary key (logid)
)ENGINE=INNODB DEFAULT CHARSET=utf8;

create table if not exists TB_SYSALARM 
(
	alarmid 		bigint unsigned		not null                PRIMARY KEY,
	dn				varchar(32)			not null,
	orgseverity		varchar(32)			not null,
	orgtype			varchar(32)			not null,
	probablecause	varchar(2048)		not null,
	eventtime		datetime			not null,
	acktime			datetime,
	cleartime		datetime,
	activestatus	int					not null,
	alarmtitle		varchar(256)		not null,
	alarmtext		text,
	ackuser			varchar(256),
	ackremark		varchar(256),
	module          varchar(256)		not null,
	id				varchar(256),
	frequency		int,
	responser		varchar(64),
	contact 		varchar(64),
	serverkey		varchar(64),
	primary key (alarmid)
)ENGINE=INNODB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS TB_EMAIL_OUTBOX(
  EID			bigint(8)		NOT NULL        PRIMARY KEY,
  REQUEST_TIME	datetime,
  MAIL_TO		varchar(2048),
  CC			varchar(2048),
  BCC			varchar(2048),
  MODULE		varchar(64),
  MAIL_TYPE		int(4)		default 0,
  SUBJECT		varchar(512),
  CONTENT		text,
  ATTACHMENTS   text,
  STATE			tinyint		default 0,
  RESULT		varchar(1024),
  PRIMARY KEY ( EID ))ENGINE=INNODB DEFAULT CHARSET=utf8;

create table if not exists TB_NOTIFIES 
(
	nid		bigint unsigned			not null		PRIMARY KEY,
	useraccount	varchar(64)			not null,
	title		varchar(256)			not null,
	filter		varchar(32)			not null,
	notifytime	datetime			not null,
	context		text,
	contextlink	varchar(1024),
	contextimg varchar(1024),
	action		varchar(8),
	actionlink	varchar(1024),
	state		int				not null		DEFAULT 0,
	priority	int				not null		DEFAULT 0,
	primary key (nid),
)ENGINE=INNODB DEFAULT CHARSET=utf8;

create table if not exists TB_NOTICE 
(
	ID		bigint unsigned			not null		PRIMARY KEY,
	TITLE		varchar(64)			not null,
	CONTENT		varchar(2048)			not null,
	ADD_TIME	datetime			NOT NULL,
	PUBLISH_TIME	datetime,
	ATTACH_NAME	varchar(128),
	ATTACH_URI	varchar(512),
	STATE		tinyint,
	primary key (ID)
)ENGINE=INNODB DEFAULT CHARSET=utf8;

INSERT INTO TB_USER( id, roleid, username, password, realname, email, status, sex) VALUES(1, 1, 'admin','e10adc3949ba59abbe56e057f20f883e', '超级管理员','',1,1);