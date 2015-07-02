#A working wordcount with oozie workflow

A working example showing how to perform Word Count, Word Count with keyword filtering using the YARN Distributed Cache, and orchestration as an Oozie Workflow. 



##Sample Dataset: MusixMatch (~13.5mb)
http://labrosa.ee.columbia.edu/millionsong/sites/default/files/AdditionalFiles/mxm_dataset_test.txt.zip
http://labrosa.ee.columbia.edu/millionsong/musixmatch


#Running as Normal Word Count

##Instructions For Standalone MR Usage:

######1) Make directory for user if not already exists

> hdfs dfs -mkdir /user/$USERNAME

######2) Make raw data folder for musixmatch dataset

> hdfs dfs -mkdir rawmusixmatch

######3) Upload raw dataset to HDFS

> hdfs dfs -put mxm_dataset_test.txt rawmusixmatch/

######4) Execute MR Job

> yarn jar hdp2wordcount-0.0.1-SNAPSHOT.jar hdp2wordcount.WordCount rawmusixmatch $OUTPUTDIR




##Instructions For Oozie  Usage:

######1) Make directory for user if not already exists

> hdfs dfs -mkdir /user/$USERNAME

######2) Make raw data folder for musixmatch dataset

> hdfs dfs -mkdir rawmusixmatch

######3) Upload raw dataset to HDFS

> hdfs dfs -put mxm_dataset_test.txt rawmusixmatch/

######4) Upload ooziewc folder to HDFS

> hdfs dfs -put ooziewc/ .

######5) Delete the keywords.txt from ooziewc/lib

> hdfs dfs -rm ooziewc/lib/keywords.txt

######6) Execute the Oozie Workflow!

> oozie job -oozie http://sandbox.hortonworks.com:11000/oozie -config job.properties  -run


#Filtering with keywords file from YARN Distributed Cache

***Note*** - Any file can be provided for filtering but the assumptions are:

1. All Keywords are Comma ‘,’ delimited 
2. All Keywords are on a single line in the keyword file


##Running as StandAlone MR with Keyword Filtering

######1) Upload keywords.txt file to HDFS

> hdfs dfs -put keywords.txt /user/$USERNAME/$SOMEFILE

######2) Follow Steps 1-3 above of the Normal Word Count Standalone MR Usage. 

######3) Execute MR Job

> yarn jar hdp2wordcount-0.0.1-SNAPSHOT.jar hdp2wordcount.WordCount rawmusixmatch $OUTPUTDIR $SOMEFILE


##Running as Oozie with Keyword Filtering

######1) Perform steps for Normal Oozie Word Count Usage above skipping step #5 
