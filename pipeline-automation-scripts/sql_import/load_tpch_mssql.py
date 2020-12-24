'''Sql Script to load data'''
import os
import subprocess
import time
import multiprocessing
import pyodbc
import constants
import logging
pyodbc.pooling = False  # for closing connection when codes completed

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger()
start_time = time.strftime("%H:%M:%S", time.localtime())
os.system('mkdir -p /home/melburne_rodrigues/formatfiles')
sql_start = time.strftime("%H:%M:%S", time.localtime())
sql_end = time.strftime("%H:%M:%S", time.localtime())
sql_conn = pyodbc.connect('DRIVER={ODBC Driver 17 for SQL Server};SERVER=' + constants.server +
    '; UID=' + constants.uid + ';PWD=' + constants.pwd, autocommit=True)
cursor = sql_conn.cursor()

structure_1 = {
    "DatabaseName": constants.databasenamewillbe,
    "Schemas":
        [
            {
                "SchemaName": constants.schemanamewillbe,
                "Tables":
                    [
                        {
                            "TableName":"customer",
                            "TableColumns":
                            [
                                {
                                    "ColumnName": "C_CUSTKEY",
                                    "ColumnType": "BIGINT",
                                    "PK": True
                                },
                            {
                            "ColumnName": "C_NAME",
                            "ColumnType": "VARCHAR(max)"
                            },
                            {
                                    "ColumnName": "C_ADDRESS",
                                    "ColumnType": "VARCHAR(max)"
                            },
                                {
                                    "ColumnName": "C_NATIONKEY",
                                    "ColumnType": "BIGINT"
                                },
                                {
                                    "ColumnName": "C_PHONE",
                                    "ColumnType": "VARCHAR(max)"
                                },
                                {
                                    "ColumnName": "C_ACCTBAL",
                                    "ColumnType": "FLOAT"
                                },
                                {
                                    "ColumnName": "C_MKTSEGMENT",
                                    "ColumnType": "VARCHAR(max)"
                                },
                                {
                                    "ColumnName": "C_COMMENT",
                                    "ColumnType": "VARCHAR(max)"
                                }
                            ]
                        },

                        {
                            "TableName": "nation",
                            "TableColumns": [{
                                "ColumnName": "N_NATIONKEY",
                                "ColumnType": "INT",
                                "PK": True
                            },
                                {
                                    "ColumnName": "N_NAME",
                                    "ColumnType": "VARCHAR(max)"
                                },
                                {
                                    "ColumnName": "N_REGIONKEY",
                                    "ColumnType": "INT"
                                },
                                {
                                    "ColumnName": "N_COMMENT",
                                    "ColumnType": "VARCHAR(max)"
                                }
                            ]
                        },

                        {
                            "TableName": "region",
                            "TableColumns": [{
                                "ColumnName": "R_REGIONKEY",
                                "ColumnType": "INT",
                                "PK": True
                            },
                                {
                                    "ColumnName": "R_NAME",
                                    "ColumnType": "VARCHAR(max)"
                                },
                                {
                                    "ColumnName": "R_COMMENT",
                                    "ColumnType": "VARCHAR(max)"
                                }
                            ]
                        },

                        {
                            "TableName": "orders",
                            "TableColumns": [{
                                "ColumnName": "O_ORDERKEY",
                                "ColumnType": "BIGINT",
                                "PK": True
                            },
                                {
                                    "ColumnName": "O_CUSTKEY",
                                    "ColumnType": "BIGINT"
                                },
                                {
                                    "ColumnName": "O_ORDERSTATUS",
                                    "ColumnType": "VARCHAR(max)"
                                },
                                {
                                    "ColumnName": "O_TOTALPRICE",
                                    "ColumnType": "FLOAT"
                                },
                                {
                                    "ColumnName": "O_ORDERDATE",
                                    "ColumnType": "VARCHAR(max)"
                                },
                                {
                                    "ColumnName": "O_ORDERPRIORITY",
                                    "ColumnType": "VARCHAR(max)"
                                },
                                {
                                    "ColumnName": "O_CLERK",
                                    "ColumnType": "VARCHAR(max)"
                                },
                                {
                                    "ColumnName": "O_SHIPPRIORITY",
                                    "ColumnType": "INT"
                                },
                                {
                                    "ColumnName": "O_COMMENT",
                                    "ColumnType": "VARCHAR(max)"
                                }
                            ]
                        },

                        {
                            "TableName": "part",
                            "TableColumns": [{
                                "ColumnName": "P_PARTKEY",
                                "ColumnType": "BIGINT",
                                "PK": True
                            },
                                {
                                    "ColumnName": "P_NAME",
                                    "ColumnType": "VARCHAR(max)"
                                },
                                {
                                    "ColumnName": "P_MFGR",
                                    "ColumnType": "VARCHAR(max)"
                                },
                                {
                                    "ColumnName": "P_BRAND",
                                    "ColumnType": "VARCHAR(max)"
                                },
                                {
                                    "ColumnName": "P_TYPE",
                                    "ColumnType": "VARCHAR(max)"
                                },
                                {
                                    "ColumnName": "P_SIZE",
                                    "ColumnType": "INT"
                                },
                                {
                                    "ColumnName": "P_CONTAINER",
                                    "ColumnType": "VARCHAR(max)"
                                },
                                {
                                    "ColumnName": "P_RETAILPRICE",
                                    "ColumnType": "FLOAT"
                                },
                                {
                                    "ColumnName": "P_COMMENT",
                                    "ColumnType": "VARCHAR(max)"
                                }
                            ]
                        },

                        {
                            "TableName": "partsupp",
                            "TableColumns": [{
                                "ColumnName": "PS_PARTKEY",
                                "ColumnType": "BIGINT",
                                "PK": True
                            },
                                {
                                    "ColumnName": "PS_SUPPKEY",
                                    "ColumnType": "BIGINT",
                                    "PK": True
                                },
                                {
                                    "ColumnName": "PS_AVAILQTY",
                                    "ColumnType": "INT"
                                },
                                {
                                    "ColumnName": "PS_SUPPLYCOST",
                                    "ColumnType": "FLOAT"
                                },
                                {
                                    "ColumnName": "PS_COMMENT",
                                    "ColumnType": "VARCHAR(max)"
                                }
                            ]
                        },

                        {
                            "TableName": "lineitem",
                            "TableColumns": [{
                                "ColumnName": "L_ORDERKEY",
                                "ColumnType": "BIGINT",
                                "PK": True
                            },
                                {
                                    "ColumnName": "L_PARTKEY",
                                    "ColumnType": "BIGINT"
                                },
                                {
                                    "ColumnName": "L_SUPPKEY",
                                    "ColumnType": "BIGINT"
                                },
                                {
                                    "ColumnName": "L_LINENUMBER",
                                    "ColumnType": "INT",
                                    "PK": True
                                },
                                {
                                    "ColumnName": "L_QUANTITY",
                                    "ColumnType": "FLOAT"
                                },
                                {
                                    "ColumnName": "L_EXTENDEDPRICE",
                                    "ColumnType": "FLOAT"
                                },
                                {
                                    "ColumnName": "L_DISCOUNT",
                                    "ColumnType": "FLOAT"
                                },
                                {
                                    "ColumnName": "L_TAX",
                                    "ColumnType": "FLOAT"
                                },
                                {
                                    "ColumnName": "L_RETURNFLAG",
                                    "ColumnType": "VARCHAR(max)"
                                },
                                {
                                    "ColumnName": "L_LINESTATUS",
                                    "ColumnType": "VARCHAR(max)"
                                },
                                {
                                    "ColumnName": "L_SHIPDATE",
                                    "ColumnType": "VARCHAR(max)"
                                },
                                {
                                    "ColumnName": "L_COMMITDATE",
                                    "ColumnType": "VARCHAR(max)"
                                },
                                {
                                    "ColumnName": "L_RECEIPTDATE",
                                    "ColumnType": "VARCHAR(max)"
                                },
                                {
                                    "ColumnName": "L_SHIPINSTRUCT",
                                    "ColumnType": "VARCHAR(max)"
                                },
                                {
                                    "ColumnName": "L_SHIPMODE",
                                    "ColumnType": "VARCHAR(max)"
                                },
                                {
                                    "ColumnName": "L_COMMENT",
                                    "ColumnType": "VARCHAR(max)"
                                }
                            ]
                        },
                        {
                            "TableName": "supplier",
                            "TableColumns": [{
                                "ColumnName": "S_SUPPKEY",
                                "ColumnType": "INT",
                                "PK": True
                            },
                                {
                                    "ColumnName": "S_NAME",
                                    "ColumnType": "VARCHAR(max)"
                                },
                                {
                                    "ColumnName": "S_ADDRESS",
                                    "ColumnType": "VARCHAR(max)"
                                },
                                {
                                    "ColumnName": "S_NATIONKEY",
                                    "ColumnType": "INT"
                                },
                                {
                                    "ColumnName": "S_PHONE",
                                    "ColumnType": "VARCHAR(max)"
                                },
                                {
                                    "ColumnName": "S_ACCTBAL",
                                    "ColumnType": "FLOAT"
                                },
                                {
                                    "ColumnName": "S_COMMENT",
                                    "ColumnType": "VARCHAR(max)"
                                }
                            ]
                        }

                    ]

            }

        ]
}


def extraction(database, schema, table, datafiles, uid, server, pwd):
    ''' Loads data in sqlserver using bcp command

        Parameters :

        database(string): Name of database

        schema(string): Name of the schema

        table(string): Name of sql table

        datafiles(string): Datafiles of table

        uid(string): User id of sql server

        server(string): Server name of sql-server

        pwd(string): password of sql-server

    '''

    bcp_cmd = ["/opt/mssql-tools/bin/bcp", "{}.{}.{}".format(database, schema, table), "IN",
               "/home/melburne_rodrigues/tpch_1000gb/{}".format(datafiles), "-f",
               "/home/melburne_rodrigues/formatfiles/{0}_{1}_{2}.fmt"
               .format(database, schema, table),
               "-U", uid, "-S",
               server, "-P", pwd, "-b", "100000", "-e", "/home/melburne_rodrigues/error_file.txt"]
    try:
        logging.info('Table: {0}.{1}.{2} loading for datafile: {3}....'
            .format(database, schema, table, datafiles))
        datafiles_bcp_start = time.strftime("%H:%M:%S", time.localtime())
        subprocess.check_output(bcp_cmd)
        logging.info('Table: {0}.{1}.{2} loaded for datafile: {3}'
            .format(database, schema, table, datafiles))
        datafiles_bcp_end = time.strftime("%H:%M:%S", time.localtime())
        logging.info('Table: {0}.{1}.{2} loading for datafile: {3} started at: {4}'
            .format(database, schema, table, datafiles, datafiles_bcp_start))
        logging.info('Table: {0}.{1}.{2} loading for datafile: {3} ended at {4}'
            .format(database, schema, table, datafiles, datafiles_bcp_end))
        os.system('rm /home/ubuntu/import/{0}/{1}/{2}/{3}'
            .format(database, schema, table, datafiles))
    except subprocess.CalledProcessError as e_1:
        r_c = e_1.returncode
print(extraction.__doc__)

def create_table_pyodbc(structure_1):
    ''' Creates sql-server tables
        Parameters:

        structure_1(string): structure_1 of database,schema,tables and fields

    '''

    y_1 = structure_1
    DatabaseName = y_1['DatabaseName']
    databasestr = """IF NOT EXISTS
       (
         SELECT name FROM master.dbo.sysdatabases 
         WHERE name = N'{0}'
        )
    CREATE DATABASE [{0}]
    """.format(DatabaseName)
    cursor.execute(databasestr)
    print("Database:" + DatabaseName + " created")
    for i in range(len(y_1['Schemas'])):
        schemas = y_1['Schemas'][i]['SchemaName']

        schemastr = """
        IF NOT EXISTS (
            SELECT  schema_name
                FROM    information_schema.schemata
                WHERE   schema_name = '{0}' 
                and CATALOG_NAME='{1}') 


                use {1}
                BEGIN
                EXEC sp_executesql N'CREATE SCHEMA {0}'  
                END
                
            """.format(schemas, DatabaseName)
        cursor.execute(schemastr)
        print("Schema:" + schemas + " created in database: " + DatabaseName)
        for j in range(len(y_1['Schemas'][i]['Tables'])):
            tables = y_1['Schemas'][i]['Tables'][j]['TableName']
            columnslist = []
            pk_cols=[]
            for k in range(len(y_1['Schemas'][i]['Tables'][j]['TableColumns'])):
                columns = y_1['Schemas'][i]['Tables'][j]['TableColumns'][k]['ColumnName']
                columntype = y_1['Schemas'][i]['Tables'][j]['TableColumns'][k]['ColumnType']
                try:
                    pk_bool = y_1['Schemas'][i]['Tables'][j]['TableColumns'][k]['PK']
                    if pk_bool == True:
                        str1 = " " + columns + " " + columntype + " Not Null"
                        columnslist.append(str1)
                        pk_cols.append(columns)
                    else:
                        str1 = " " + columns + " " + columntype
                        columnslist.append(str1)
                except KeyError:
                    str1 = " " + columns + " " + columntype
                    columnslist.append(str1)

            columnstr = ','.join(columnslist)
            pk_colsstr = ','.join(pk_cols)
            print('\n')
            if len(pk_cols) != 0:
                columnstr = columnstr + ", CONSTRAINT PK_{} PRIMARY KEY ({})".format(tables,
                    pk_colsstr)
            tablestr = "IF OBJECT_ID('{0}.{1}.{2}', 'U') IS NULL \
            create table {0}.{1}.{2} ( {3});".format(DatabaseName, schemas, tables, columnstr)
            print(tablestr)
            cursor.execute(tablestr)
            print('Table: ' + tables + ' created in Schema: ' + schemas + ' in database: '
                + DatabaseName)
    sql_conn.commit()
    cursor.close()
    sql_conn.close()
print(create_table_pyodbc.__doc__)

def create_formatfile_bcp(structure_1):
    ''' Creates format files for bcp command

        Parameters:

        structure_1(string): structure_1 of database,schema,tables and fields

    '''
    y_1 = structure_1
    database = y_1['DatabaseName']
    for i in range(len(y_1['Schemas'])):
        schema = y_1['Schemas'][i]['SchemaName']
        for j in range(len(y_1['Schemas'][i]['Tables'])):
            table = y_1['Schemas'][i]['Tables'][j]['TableName']
            bcp_formatfilestr = """/opt/mssql-tools/bin/bcp {0}.{1}.{2} format nul -c -t'|' -r
            "|\n" -f /home/melburne_rodrigues/formatfiles/{0}_{1}_{2}.fmt -U '{3}' -S '{4}' -P '{5}
            '""".format(database, schema, table, constants.uid, constants.server, constants.pwd)
            os.system(bcp_formatfilestr)
            print("formatfile created for {0}.{1}.{2} in folder formatfiles as{0}_{1}_{2}.fmt"
                .format(database, schema, table))
print(create_formatfile_bcp.__doc__)

#Datafiles generated from TPC-H datageneration.
lineitem_datafile = ['lineitem.tbl.1', 'lineitem.tbl.2', 'lineitem.tbl.3', 'lineitem.tbl.4',
                    'lineitem.tbl.5','lineitem.tbl.6']


def import_data_bcp(structure_1):
    '''Imports data for bcp command

        Parameters:

        structure_1(string): structure_1 of database,schema,tables and fields
    '''

    y_1 = structure_1
    processes = []
    database = y_1['DatabaseName']
    for i in range(len(y_1['Schemas'])):
        schema = y_1['Schemas'][i]['SchemaName']
        for j in range(len(y_1['Schemas'][i]['Tables'])):
            table = y_1['Schemas'][i]['Tables'][j]['TableName']
            if table == 'lineitem':
                for lineitem_files in lineitem_datafile:
                    datafiles = lineitem_files

                    p_1 = multiprocessing.Process(target=extraction,
                                                args=(database, schema, table, datafiles,
                                                constants.uid, constants.server, constants.pwd))
                    processes.append(p_1)
                    p_1.start()
            else:
                datafiles = '{}.tbl'.format(table)

                p_1 = multiprocessing.Process(target=extraction,
                                            args=(database, schema, table, datafiles,
                                                constants.uid, constants.server, constants.pwd))
                processes.append(p_1)
                p_1.start()
    for process in processes:
        process.join()
print(import_data_bcp.__doc__)

if __name__ == "__main__":
    '''Main fucntion

        Creates table in sql server using given structure_1.
        Creates bcp format file and runs bcp command to load data into sql-server.

    '''
    create_table_pyodbc(structure_1)
    create_formatfile_bcp(structure_1)
    import_data_bcp(structure_1)
    os.system('TPCH data upload successfully')

    end_time = time.strftime("%H:%M:%S", time.localtime())

    print('Migration started at:{0}'.format(start_time))
    print('Migration ended at:{0}'.format(end_time))
    print('TPCH data upload started at:{0}'.format(sql_start))
    print('TPCH data upload ended at:{0}'.format(sql_end))
print(__name__.__doc__)
