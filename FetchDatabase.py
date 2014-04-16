from fabric.api import run, local, abort
from fabric.contrib.files import exists
from fabric.context_managers import hide, settings
from fabric.operations import get, put, local

DUMP_FOLDER = '/data/devgru_dumps'
DUMP_FILE = DUMP_FOLDER + '/temp_dump.sql'
TEMP_CNF_FILE = DUMP_FOLDER + '/temp.cnf'
CNF_FILE = '/home/addepar/.my.cnf'

## Migrates the database
## If you are specifying to dump the entire database, then run a mysqldump
## If you are selectively choosing a specific firm (or subset of firms) to dump,
## dump 1) the tables with neither a firm_id nor a firmid column, 2) the tables
## with a firmid column, and finally 3) the tables with a firm_id column
## We fetch the list of tables by querying the mysql information_schema database
def fetch(environName, dbFromHost, dbToHost, dbFirmIDsString = None): 
  if dbFromHost == 'demo.addepar.com':
    print 'Fetching demo database from {0} to {1}'.format(dbFromHost, dbToHost)
    checkDumpDirExists(dbToHost)
    copyMyCnf(dbFromHost, dbToHost)
    
    print 'dumping the db for all firms into a temporary dump file'
    run(dumpEntireDBCmd(dbFromHost) + ' > ' + DUMP_FILE)
    deleteAndLoad()
  
  elif not dbFirmIDsString:
    print 'Rocketpants FTW: moving data from {0} to {1}'.format(dbFromHost, dbToHost)
    local('fab -f RocketPants.py --user=root -i /usr/share/jetty/.ssh/id_dsa_jetpants fetch:{0},{1}'.format(
        dbFromHost, dbToHost))
  
  else:
    print 'Fetching data for the firms {0} from {1} to {2}'.format(dbFirmIDsString, dbFromHost, dbToHost)
    checkDumpDirExists(dbToHost)
    copyMyCnf(dbFromHost, dbToHost)

    with(settings(hide('everything'), host_string=dbToHost)):
    # Remove the dump file if it exists (we need to do this since we'll be appending to it, not re-writing it)
      if exists(DUMP_FILE):
        run('rm ' + DUMP_FILE)
  
      if (dbFirmIDsString):
        print 'dumping data for the following firm(s) into a temporary dump file: ' + dbFirmIDsString
        dbFirmIDsString = "".join(dbFirmIDsString.split()) #remove whitespace
        firmIDs = dbFirmIDsString.split(',')
        
        firmidCondition = 'firmid IS NULL'
        firm_idCondition = 'firm_id IS NULL'
        for firmID in firmIDs:
          assert firmID.isdigit(), '"{0}" is not a digit, "{1}" is not a valid comma-delimited list of firmIDs'
          firmidCondition += ' or firmid={0}'.format(firmID)
          firm_idCondition += ' or firm_id={0}'.format(firmID)
          
        print 'dumping the tables with neither \'firmid\' nor \'firm_id\' columns'
        dump(getTablesWithNoFirmIDQuery(), '', dbFromHost)
        
        print 'dumping the tables with \'firmid\' column'
        dump(getTablesWithColumnNameQuery('firmid'), firmidCondition, dbFromHost)
        
        print 'dumping the tables with \'firm_id\' column'
        dump(getTablesWithColumnNameQuery('firm_id'), firm_idCondition, dbFromHost)
        
        deleteAndLoad()


  # Don't use the real portal subdomains
  # The double escape for the inner quotes - \\\\"firm\\\\" - is necessary because of bash weirdness
  with settings(host_string=dbToHost):
    subdomainQuery = 'update pfirm set portalsubdomain = concat(\\\\"{0}\\\\",id);'.format(environName)
    emailQuery = 'update pcredential set email=concat(concat(\\\\"qa+\\\\",id),\\\\"@addepar.com\\\\") where email not like \\\\"%addepar.com\\\\";'
    supportEmailQuery = 'update pfirm set supportEmail=concat(concat(\\\\"qa+\\\\",firmid),\\\\"@addepar.com\\\\");'
    run('mysql -e "use amp; {0}"'.format(subdomainQuery))
    run('mysql -e "use amp; {0}"'.format(emailQuery))
    run('mysql -e "use amp; {0}"'.format(supportEmailQuery))
  
  # If we're copying a new db to staging, we want to update the staging s3 bucket
  # This call should be made on jenkins2, where there are s3 credentials
  if dbToHost.lower() == 'stagingdb.addepar.com':
    print "Syncing the staging s3 bucket with the production s3 bucket"
    local('python S3UpdateScript.py amp_prod amp_staging2')


def checkDumpDirExists(dbToHost):
  with settings(host_string=dbToHost):
    if not exists(DUMP_FOLDER):
      abort('Aborting: ' + DUMP_FOLDER + ' directory on ' + dbToHost + ' does not exist')


## Copy the .my.cnf file from dbFromHost to dbToHost
def copyMyCnf(dbFromHost, dbToHost):
  with settings(host_string=dbFromHost):
    get(CNF_FILE, 'temp.cnf')
  with settings(host_string=dbToHost):
    put('temp.cnf', TEMP_CNF_FILE)
  local('rm temp.cnf')


## Get the names of the tables given by the tableQuery. Then, dump these tables into our temp file
## applying the where condition given by whereClause
def dump(tableQuery, whereClause, host):
    #NOTE: the defaults-file param MUST be the first paramter
  cmd = 'mysql --defaults-file={0} -u addepar --host={1} amp --batch --skip-column-names --execute {2}'.format(TEMP_CNF_FILE, host, tableQuery)
  tables = run(cmd).split()
  if (len(tables) > 0):
    run(dumpTablesCmd(tables, whereClause, host) + ' >> ' + DUMP_FILE)


##  Returns the mysqldump command to dump the *_schema_version tables that are passed in the variable tables
def dumpTablesCmd(tables, whereClause, remoteDBHost):
    #NOTE: the defaults-file param MUST be the first paramter
  cmd = 'mysqldump --defaults-file={0} -u addepar --host={1} --single-transaction --quick amp '.format(TEMP_CNF_FILE, remoteDBHost)
  for table in tables:
    cmd += (table + ' ')
  if (whereClause):
    cmd += '--where="{0}"'.format(whereClause)
  return cmd


## Returns the mysqldump command for migrating the entire db. 
def dumpEntireDBCmd(remoteDBHost):
  return 'mysqldump --defaults-file={0} -u addepar --host={1} --single-transaction --quick amp'.format(TEMP_CNF_FILE, remoteDBHost)


def getTablesWithNoFirmIDQuery():
  query = '"select distinct table_name from information_schema.columns where table_schema=\'amp\' and table_name not in '
  query += '(select distinct table_name from information_schema.columns where table_schema=\'amp\' and (column_name=\'firmid\' or column_name=\'firm_id\'));"'
  return query


def getTablesWithColumnNameQuery(columnName):
  return '"select distinct table_name from information_schema.columns where table_schema=\'amp\' and column_name=\'' + columnName + '\'"'


# delete the old database, load the new one from the dump file
def deleteAndLoad():    
  print 'deleting old database'
  run('mysql -u addepar -e "drop database if exists amp; create database amp;"')
      
  print 'loading from temporary dump file'
  run('mysql -u addepar amp < {0}'.format(DUMP_FILE))
