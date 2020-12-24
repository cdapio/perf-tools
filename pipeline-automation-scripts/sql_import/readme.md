# Upload TPCH Data in sql server
This script used to upload data generated from TPCH tool into sql server using Bulk Copy Program Utility[(BCP)](https://docs.microsoft.com/en-us/sql/tools/bcp-utility?view=sql-server-ver15)

**Python scripts operations :**

* Creates database and empty table with given database name and schema mentioned in constants.py file.
* Creates format file for particular table.
* Loads datafiles of TPC-H data using BCP command.

In this case,TPCH data is stored in 1000gb folder in home directory of VM instance.Provide this path in "create_table_pyodbc", "create_formatfile_bcp", "import_data_bcp" given functions to simply load data in sql server.


    Note : Change TPCH data path directory to the directory where your data is stored/generated.Also change the list of datafiles depending on the size of data.

Run Python file using:

    $python3 load_tpch_mssql.py
