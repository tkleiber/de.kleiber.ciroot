print ''
print '--- start script IntegratedWebLogicServerSettings.py ---' 
print ''
# #####################################################################################################################
# connect the RUNNING server
# #####################################################################################################################

connect('weblogic','weblogic1','t3://localhost:7101')
edit()
startEdit()

# #####################################################################################################################
print ''
print '--- create and configure datasource ---' 
print ''
# #####################################################################################################################

# variables
dsName = 'summit_adf'

cd('/')
try:
  delete(dsName,'JDBCSystemResource')
except WLSTException:
  print dsName + " does not exist!"  

cmo.createJDBCSystemResource(dsName)

cd('/JDBCSystemResources/summit_adf/JDBCResource/summit_adf')
cmo.setName(dsName)

# set data source database url
cd('/JDBCSystemResources/summit_adf/JDBCResource/summit_adf/JDBCDriverParams/summit_adf')
cmo.setUrl('jdbc:oracle:thin:@localhost:1521:xe')
cmo.setDriverName('oracle.jdbc.OracleDriver')
set('Password', 'summit_adf')

cd('/JDBCSystemResources/summit_adf/JDBCResource/summit_adf/JDBCDataSourceParams/summit_adf')
set('JNDINames',jarray.array([String('jdbc/summit_adfDS')], String))
cmo.setGlobalTransactionsProtocol('OnePhaseCommit')

# create data source database user
cd('/JDBCSystemResources/summit_adf/JDBCResource/summit_adf/JDBCDriverParams/summit_adf/Properties/summit_adf')
cmo.createProperty('user')
# set data source database user name
cd('/JDBCSystemResources/summit_adf/JDBCResource/summit_adf/JDBCDriverParams/summit_adf/Properties/summit_adf/Properties/user')
cmo.setValue('summit_adf')

cd('/JDBCSystemResources/summit_adf/JDBCResource/summit_adf/JDBCConnectionPoolParams/summit_adf')
cmo.setTestTableName('SQL SELECT 1 FROM DUAL\r\n\r\n')
cmo.setMaxCapacity(200)

cd('/SystemResources/summit_adf')
set('Targets',jarray.array([ObjectName('com.bea:Name=DefaultServer,Type=Server')], ObjectName))

print "datasource: " + dsName + " (re)created and configured!"
# #####################################################################################################################
print ''
print '-- activate the changes --'
print ''
# #####################################################################################################################

activate()
print "restart IntegratedWebLogicServer is required"
print ''
print '--- end script IntegratedWebLogicServerSettings.py ---' 
print ''