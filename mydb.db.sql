BEGIN TRANSACTION;
CREATE TABLE IF NOT EXISTS "Computer_Preference" (
	"Computer_Id"	integer NOT NULL,
	"Preference_Id"	integer NOT NULL,
	FOREIGN KEY("Computer_Id") REFERENCES "Computers"("Id"),
	FOREIGN KEY("Preference_Id") REFERENCES "Preferences"("Id")
);
CREATE TABLE IF NOT EXISTS "Computers" (
	"Id"	integer,
	"Classroom"	varchar(255) NOT NULL,
	"DisplayedName"	varchar(255) NOT NULL UNIQUE,
	"Host"	varchar(255) NOT NULL UNIQUE,
	"IsSelected"	boolean NOT NULL,
	"LastMaintenance"	datetime NOT NULL,
	"LogExpiration"	bigint NOT NULL,
	"MaintainPeriod"	bigint NOT NULL,
	"RequestInterval"	bigint NOT NULL,
	"SSHConfiguration_Id"	integer,
	PRIMARY KEY("Id"),
	FOREIGN KEY("SSHConfiguration_Id") REFERENCES "SSH_Configurations"("Id")
);
CREATE TABLE IF NOT EXISTS "UsersLogs" (
	"Id"	integer,
	"Timestamp"	datetime,
	"FromWhere"	varchar(255),
	"Idle"	varchar(255),
	"JCPU"	varchar(255),
	"PCPU"	varchar(255),
	"SAT15"	varchar(255),
	"TTY"	varchar(255),
	"User"	varchar(255),
	"What"	varchar(255),
	"Computer_Id"	integer,
	PRIMARY KEY("Id")
);
CREATE TABLE IF NOT EXISTS "SwapLogs" (
	"Id"	integer,
	"Timestamp"	datetime,
	"Free"	bigint NOT NULL,
	"Total"	bigint NOT NULL,
	"Used"	bigint NOT NULL,
	"Computer_Id"	integer,
	PRIMARY KEY("Id")
);
CREATE TABLE IF NOT EXISTS "SSH_Configurations" (
	"Id"	integer,
	"AuthMethod"	varchar(255) NOT NULL,
	"EncryptedPassword"	varchar(255),
	"Name"	varchar(255) UNIQUE,
	"Port"	integer NOT NULL,
	"PrivateKeyPath"	varchar(255),
	"Scope"	varchar(255) NOT NULL,
	"Username"	varchar(255) NOT NULL,
	PRIMARY KEY("Id")
);
CREATE TABLE IF NOT EXISTS "RamLogs" (
	"Id"	integer,
	"Timestamp"	datetime,
	"Buffers"	bigint NOT NULL,
	"Cached"	bigint NOT NULL,
	"Free"	bigint NOT NULL,
	"Shared"	bigint NOT NULL,
	"Total"	bigint NOT NULL,
	"Used"	bigint NOT NULL,
	"Computer_Id"	integer,
	PRIMARY KEY("Id")
);
CREATE TABLE IF NOT EXISTS "ProcessesLogs" (
	"Id"	integer,
	"Timestamp"	datetime,
	"CPU_Percentage"	double precision NOT NULL,
	"Command"	varchar(255),
	"Memory_Percentage"	double precision NOT NULL,
	"PID"	bigint NOT NULL,
	"RSS"	bigint NOT NULL,
	"Start"	varchar(255),
	"Stat"	varchar(255),
	"TTY"	varchar(255),
	"Time"	varchar(255),
	"User"	varchar(255),
	"VSZ"	bigint NOT NULL,
	"Computer_Id"	integer,
	PRIMARY KEY("Id")
);
CREATE TABLE IF NOT EXISTS "Preferences" (
	"Id"	integer,
	"ClassName"	varchar(255) NOT NULL UNIQUE,
	PRIMARY KEY("Id")
);
CREATE TABLE IF NOT EXISTS "DisksLogs" (
	"Id"	integer,
	"Timestamp"	datetime,
	"Available"	bigint NOT NULL,
	"BlocksNumber"	bigint NOT NULL,
	"FileSystem"	varchar(255),
	"MountedOn"	varchar(255),
	"UsePercentage"	integer NOT NULL,
	"Used"	bigint NOT NULL,
	"Computer_Id"	integer,
	PRIMARY KEY("Id")
);
CREATE TABLE IF NOT EXISTS "CpuLogs" (
	"Id"	integer,
	"Timestamp"	datetime,
	"ExecutingKernelSchedulingEntitiesNum"	integer NOT NULL,
	"ExistingKernelSchedulingEntitiesNum"	integer NOT NULL,
	"Last15MinutesAvgCpuUtil"	double precision NOT NULL,
	"Last1MinuteAvgCpuUtil"	double precision NOT NULL,
	"Last5MinutesAvgCpuUtil"	double precision NOT NULL,
	"RecentlyCreatedProcessPID"	integer NOT NULL,
	"Computer_Id"	integer,
	PRIMARY KEY("Id")
);
INSERT INTO "Preferences" VALUES (1,'Healthcheck.Preferences.CpuInfoPreference');
INSERT INTO "Preferences" VALUES (2,'Healthcheck.Preferences.DisksInfoPreference');
INSERT INTO "Preferences" VALUES (3,'Healthcheck.Preferences.ProcessesInfoPreference');
INSERT INTO "Preferences" VALUES (4,'Healthcheck.Preferences.RamInfoPreference');
INSERT INTO "Preferences" VALUES (5,'Healthcheck.Preferences.SwapInfoPreference');
INSERT INTO "Preferences" VALUES (6,'Healthcheck.Preferences.UsersInfoPreference');
COMMIT;
