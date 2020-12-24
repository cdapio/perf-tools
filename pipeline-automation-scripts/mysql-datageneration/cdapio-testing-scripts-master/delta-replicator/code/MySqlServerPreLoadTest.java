import com.google.gson.internal.StringMap;

import java.io.File;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class MySqlServerPreLoadTest {
    //private static final String DB = "mysql_db";

    public static void main(String[] args) throws Exception {
        //String schemaPath = "D:/IntraGcp/JavaProject/cdapio-testing-scripts-master/resources/inputschema/mytable1.json";
        String host = args[0];
        String user = args[1];
        String password = args[2];
        String timezone = args[3];
        int port = Integer.parseInt(args[4]);
        String schemaPath = args[5];
        int chunkSize = Integer.parseInt(args[6]);
        String DB = args[7];

        try {
            Class.forName("com.mysql.jdbc.Driver");
            String connURL = String.format("jdbc:mysql://%s:%d", host, port);
            ;
            Properties properties = new Properties();
            properties.put("user", user);
            properties.put("password", password);
            properties.put("serverTimezone", timezone);

            // create database
            try (Connection connection = DriverManager.getConnection(connURL, properties);
                 Statement statement = connection.createStatement()) {
                statement.execute("DROP DATABASE IF EXISTS " + DB);
                System.out.println("db dropped");
                statement.execute("CREATE DATABASE " + DB);
                System.out.println(String.format("CREATED DATABASE:%s", DB));
            }

            String finalConnURL = connURL + "/" + DB;
            String tableName = null;
            int lastPeriodPos = 0;
            int tableCount =0;
            File dir = new File(schemaPath);
            File[] directoryListing = dir.listFiles();
            if (directoryListing != null) {
                for (File filePath : directoryListing) {
                    if (filePath.isFile()) {
                        tableName = filePath.getName();
                        lastPeriodPos = tableName.lastIndexOf('.');
                        if (lastPeriodPos > 0)
                            tableName = tableName.substring(0, lastPeriodPos);

                        ArrayList<StringMap> tableMap = ParseJson.getSchemaMap(filePath.toString());

                        String createStatement = "CREATE TABLE %s (id int NOT NULL AUTO_INCREMENT, ";
                        String queryStatement = "INSERT INTO %s (";
                        createStatement += ParseJson.createStmt(tableMap, chunkSize);
                        createStatement += ", create_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP(), mod_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP(), PRIMARY KEY (id))";
                        queryStatement += ParseJson.queryStmt(tableMap);
                        queryStatement += ") VALUES (";
                        int noOfColumns = ParseJson.getNoColumns(tableMap);
                        for (int i = 0; i < noOfColumns; i++) {
                            queryStatement += "?, ";
                        }
                        queryStatement = queryStatement.substring(0, queryStatement.lastIndexOf(","));
                        queryStatement += " )";

                        // create tables
                        try (Connection connection = DriverManager.getConnection(finalConnURL, properties)) {
                            try (Statement statement = connection.createStatement()) {
                                statement.execute(
                                        String.format(createStatement, tableName));
                                tableCount++;
                            }
                            System.out.println(String.format("CREATED %d TABLE(S)", tableCount));


                            String insert = String.format(queryStatement, tableName);
                            try (PreparedStatement ps = connection.prepareStatement(insert)) {
                                int index = 1;
                                Iterator it = tableMap.iterator();
                                while (it.hasNext()) {
                                    StringMap d = (StringMap) it.next();
                                    String data = null;
                                    data = ParseJson.genColumnData(d, chunkSize);
                                    ps.setString(index++, data);
                                }
                                /*ps.setTimestamp(index, Timestamp.valueOf(LocalDateTime.now()));
                                ps.setTimestamp(++index, Timestamp.valueOf(LocalDateTime.now()));*/
                                ps.execute();
                            }
                        }
                        System.out.println("FINISHED PRELOAD ONE RECORD TO EACH TABLE");
                    } else {
                        System.out.println("schema 's doesn't exist");
                        System.exit(0);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Program has finished");
    }

}
