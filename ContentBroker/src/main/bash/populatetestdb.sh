#!/bin/bash

# author: Daniel M. de Oliveira

HIER=`pwd`
#cd /tmp

if [ "$1" = "create" ] 
then

	DATABASE_PROC_ID=`ps -aef | grep hsqldb.jar | grep -v grep | awk '{print $2}'`
	if [ "$DATABASE_PROC_ID" != "" ]
	then
	        echo Killing hsql database process $DATABASE_PROC_ID.
	        kill -9 $DATABASE_PROC_ID
	fi
	
	sleep 2
	
	echo Recreating da-nrw schema in hsql database.
	rm -r mydb.tmp 2> /dev/null
	rm mydb.*      2> /dev/null
	
	cd $HIER
	java -cp ../3rdParty/hsqldb/lib/hsqldb.jar org.hsqldb.server.Server --database.0 file:mydb --dbname.0 xdb &
	
	sleep 2

	sqls=(
		"CREATE MEMORY TABLE PUBLIC.CONTRACTORS(ID INTEGER GENERATED BY DEFAULT AS IDENTITY(START WITH 1) NOT NULL PRIMARY KEY,ADMIN INTEGER,EMAIL_CONTACT VARCHAR(255),FORBIDDEN_NODES VARCHAR(255),SHORT_NAME VARCHAR(255));"
		"CREATE MEMORY TABLE PUBLIC.CONVERSION_POLICIES(ID INTEGER GENERATED BY DEFAULT AS IDENTITY(START WITH 1) NOT NULL PRIMARY KEY,SOURCE_FORMAT VARCHAR(255),CONTRACTOR_ID INTEGER,CONVERSION_ROUTINE_ID INTEGER,CONSTRAINT FK68223659BDCEDD53 FOREIGN KEY(CONTRACTOR_ID) REFERENCES PUBLIC.CONTRACTORS(ID));"
		"CREATE MEMORY TABLE PUBLIC.CONVERSION_QUEUE(ID INTEGER GENERATED BY DEFAULT AS IDENTITY(START WITH 1) NOT NULL PRIMARY KEY,ADDITIONAL_PARAMS VARCHAR(255),DATE TIMESTAMP,NODE VARCHAR(255),TARGET_FOLDER VARCHAR(255),CONVERSION_ROUTINE_ID INTEGER,SOURCE_FILE_ID INTEGER,JOB_ID INTEGER);"
		"CREATE MEMORY TABLE PUBLIC.CONVERSION_ROUTINES(ID INTEGER GENERATED BY DEFAULT AS IDENTITY(START WITH 1) NOT NULL PRIMARY KEY,NAME VARCHAR(255),PARAMS VARCHAR(255),TARGET_SUFFIX VARCHAR(255),TYPE VARCHAR(255));"
		"CREATE MEMORY TABLE PUBLIC.CONVERSION_ROUTINES_NODES(CONVERSION_ROUTINES_ID INTEGER NOT NULL,NODES_ID INTEGER NOT NULL,PRIMARY KEY(CONVERSION_ROUTINES_ID,NODES_ID),CONSTRAINT FKF167A8EAC31E4A77 FOREIGN KEY(CONVERSION_ROUTINES_ID) REFERENCES PUBLIC.CONVERSION_ROUTINES(ID));"
		"CREATE MEMORY TABLE PUBLIC.DAFILES(ID INTEGER GENERATED BY DEFAULT AS IDENTITY(START WITH 1) NOT NULL PRIMARY KEY,CONVERSION_INSTRUCTION_ID INTEGER NOT NULL,FILE_FORMAT VARCHAR(255),FORMAT_SECOND_ATTRIBUTE VARCHAR(255),PATH_TO_JHOVE_OUTPUT VARCHAR(255),RELATIVE_PATH VARCHAR(255),REP_NAME VARCHAR(255),PKG_ID INTEGER);"
		"CREATE MEMORY TABLE PUBLIC.EVENTS(ID INTEGER GENERATED BY DEFAULT AS IDENTITY(START WITH 1) NOT NULL PRIMARY KEY,AGENT_NAME VARCHAR(255),AGENT_TYPE VARCHAR(255),DATE TIMESTAMP,DETAIL VARCHAR(2048),TYPE VARCHAR(255),SOURCE_FILE_ID INTEGER,TARGET_FILE_ID INTEGER,PKG_ID INTEGER,CONSTRAINT FKB307E1197273F1C2 FOREIGN KEY(TARGET_FILE_ID) REFERENCES PUBLIC.DAFILES(ID),CONSTRAINT FKB307E1198013BE4C FOREIGN KEY(SOURCE_FILE_ID) REFERENCES PUBLIC.DAFILES(ID));"
		"CREATE MEMORY TABLE PUBLIC.NODES(ID INTEGER GENERATED BY DEFAULT AS IDENTITY(START WITH 1) NOT NULL PRIMARY KEY,NAME VARCHAR(255),URN_INDEX INTEGER NOT NULL);"
		"CREATE MEMORY TABLE PUBLIC.OBJECTS(DATA_PK INTEGER GENERATED BY DEFAULT AS IDENTITY(START WITH 1) NOT NULL PRIMARY KEY,DATE_CREATED VARCHAR(255),DATE_MODIFIED VARCHAR(255),DDB_EXCLUSION BIT(1),DYNAMIC_NONDISCLOSURE_LIMIT VARCHAR(255),IDENTIFIER VARCHAR(255),INITIAL_NODE VARCHAR(255),LAST_CHECKED TIMESTAMP,MOST_RECENT_FORMATS VARCHAR(255),MOST_RECENT_SECONDARY_ATTRIBUTES VARCHAR(255),OBJECT_STATE INTEGER NOT NULL,ORIG_NAME VARCHAR(255),ORIGINAL_FORMATS VARCHAR(255),PUBLISHED_FLAG INTEGER NOT NULL,STATIC_NONDISCLOSURE_LIMIT TIMESTAMP,URN VARCHAR(255),ZONE VARCHAR(255),CONTRACTOR_ID INTEGER,CONSTRAINT FK9D13C514BDCEDD53 FOREIGN KEY(CONTRACTOR_ID) REFERENCES PUBLIC.CONTRACTORS(ID));"
		"CREATE MEMORY TABLE PUBLIC.OBJECTS_PACKAGES(OBJECTS_DATA_PK INTEGER NOT NULL,PACKAGES_ID INTEGER NOT NULL,UNIQUE(PACKAGES_ID),CONSTRAINT FK343D37B88BAD65FD FOREIGN KEY(OBJECTS_DATA_PK) REFERENCES PUBLIC.OBJECTS(DATA_PK));"
		"CREATE MEMORY TABLE PUBLIC.PACKAGES(ID INTEGER GENERATED BY DEFAULT AS IDENTITY(START WITH 1) NOT NULL PRIMARY KEY,CHECKSUM VARCHAR(255),CONTAINER_NAME VARCHAR(255),LAST_CHECKED TIMESTAMP,NAME VARCHAR(255),STATUS VARCHAR(255));"
		"CREATE MEMORY TABLE PUBLIC.QUEUE(ID INTEGER GENERATED BY DEFAULT AS IDENTITY(START WITH 1) NOT NULL PRIMARY KEY,DATE_CREATED VARCHAR(255),DATE_MODIFIED VARCHAR(255),INITIAL_NODE VARCHAR(255),PARENT_ID INTEGER,REP_NAME VARCHAR(255),REPL_DESTINATIONS VARCHAR(255),STATUS VARCHAR(255),CONTRACTOR_ID INTEGER,OBJECTS_ID INTEGER,CONSTRAINT FK66F1911CFE5879E FOREIGN KEY(OBJECTS_ID) REFERENCES PUBLIC.OBJECTS(DATA_PK),CONSTRAINT FK66F1911BDCEDD53 FOREIGN KEY(CONTRACTOR_ID) REFERENCES PUBLIC.CONTRACTORS(ID),CONSTRAINT FK66F1911DF7D9EF4 FOREIGN KEY(PARENT_ID) REFERENCES PUBLIC.QUEUE(ID));"
		"CREATE MEMORY TABLE PUBLIC.SECOND_STAGE_SCANS(ID INTEGER GENERATED BY DEFAULT AS IDENTITY(START WITH 1) NOT NULL PRIMARY KEY,PUID VARCHAR(255),ALLOWED_VALUES VARCHAR(255),FORMAT_IDENTIFIER_SCRIPT_NAME VARCHAR(255));"
	)
fi
if [ "$1" = "clean" ] 
then
	sqls=(
		"DELETE FROM second_stage_scans;"
		"DELETE FROM queue;"
		"DELETE FROM events;"
		"DELETE FROM dafiles;"
		"DELETE FROM objects_packages;"
		"DELETE FROM packages;"
		"DELETE FROM objects;"
		"DELETE FROM conversion_routines_nodes;"
		"DELETE FROM nodes;"
		"DELETE FROM conversion_policies;"
		"DELETE FROM conversion_routines;"
		"DELETE FROM conversion_queue;"
		"DELETE FROM contractors;"
	) 
fi

if [ "$1" = "populate" ]
then
	sqls=(
		"INSERT INTO contractors (id,short_name,admin) values (1,'TEST',0);"
        "INSERT INTO contractors (id,short_name,admin) values (2,'DEFAULT',0);"
        "INSERT INTO contractors (id,short_name,admin) values (3,'PRESENTER',0);"
        "INSERT INTO contractors (id,short_name,admin) values (4,'rods',1);"
        "INSERT INTO nodes (id,urn_index) values (1,0);"
        "INSERT INTO conversion_routines (id,name,target_suffix,type) VALUES (1,'TIFF',null,'de.uzk.hki.da.convert.TiffConversionStrategy');"
        "INSERT INTO conversion_routines (id,name,target_suffix,type) VALUES (2,'PIMG','jpg','de.uzk.hki.da.convert.PublishImageConversionStrategy');"
        "INSERT INTO conversion_routines (id,name,target_suffix,type,params) VALUES (3,'CLITIF','tif','de.uzk.hki.da.convert.CLIConversionStrategy','convert input output');"
        "INSERT INTO conversion_routines (id,name,target_suffix,type,params) VALUES (4,'CLICOPY','*','de.uzk.hki.da.convert.PublishCLIConversionStrategy','cp input output');"
        "INSERT INTO conversion_routines_nodes (conversion_routines_id,nodes_id) VALUES (1,1);"
        "INSERT INTO conversion_routines_nodes (conversion_routines_id,nodes_id) VALUES (2,1);"
        "INSERT INTO conversion_routines_nodes (conversion_routines_id,nodes_id) VALUES (3,1);"
        "INSERT INTO conversion_policies (id,contractor_id,source_format,conversion_routine_id) VALUES (1,2,'fmt/353',1);"
        "INSERT INTO conversion_policies (id,contractor_id,source_format,conversion_routine_id) VALUES (2,3,'fmt/353',2);"
        "INSERT INTO conversion_policies (id,contractor_id,source_format,conversion_routine_id) VALUES (3,2,'fmt/116',3);"
        "INSERT INTO conversion_policies (id,contractor_id,source_format,conversion_routine_id) VALUES (4,3,'x-fmt/392',2);"
        "INSERT INTO conversion_policies (id,contractor_id,source_format,conversion_routine_id) VALUES (5,3,'fmt/4',2);"
        "INSERT INTO conversion_policies (id,contractor_id,source_format,conversion_routine_id) VALUES (6,3,'fmt/16',4);"
        "INSERT INTO conversion_policies (id,contractor_id,source_format,conversion_routine_id) VALUES (7,3,'fmt/354',4);"
        "INSERT INTO second_stage_scans (id,puid,allowed_values,format_identifier_script_name) VALUES (1,'x-fmt/384','svq1','ffmpeg.sh');"
        "INSERT INTO second_stage_scans (id,puid,allowed_values,format_identifier_script_name) VALUES (2,'fmt/200','dvvideo','ffmpeg.sh');"
        "INSERT INTO second_stage_scans (id,puid,allowed_values,format_identifier_script_name) VALUES (3,'fmt/5','cinepak','ffmpeg.sh');"
	)
fi

for i in "${sqls[@]}"
do
	echo "$i"
	cd $HIER
	
	if [ "$2" = "ci" ]
	then
	    psql -U cb_usr -d CI-CB -c "$i"
	fi
	if [ "$2" = "dev" ]
	then
	    java -jar ../3rdParty/hsqldb/lib/sqltool.jar --autoCommit --sql "$i" xdb 
	fi
done




