import com.google.gson.internal.StringMap;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public class MySqlServerCdcTest {
    private static final int INIT_DELAY_IN_SECONDS = 0;
    private static List<AtomicLong> counterList;
    private static LocalDateTime sTime;
    private static LocalDateTime eTime;


    public static void main(String[] args) throws Exception {
        String schemaPath = args[0];
        int terminateDurationInMins = Integer.parseInt(args[1]);
        int batchSize = Integer.parseInt(args[2]);
        double updateFactor = Double.parseDouble(args[3]);
        int chunkSize = Integer.parseInt(args[4]);
        String configPath = args[5];
        int noOfThreads = Integer.parseInt(args[6]);
        int periodInSecs = Integer.parseInt(args[7]);
        double deleteFactor = Double.parseDouble(args[8]);

        Random random = new Random();
        int tableCount = Util.getNumTables(schemaPath);

        HikariConfig cfg = new HikariConfig(configPath);
        HikariDataSource ds = new HikariDataSource(cfg);

        try {
            counterList = new ArrayList<>(tableCount);
            for (int i = 0; i < tableCount; i++) {
                counterList.add(new AtomicLong());
            }

            Map<String, ArrayList<StringMap>> tablesMap  = ParseJson.getSchemaListMap(schemaPath);


            Runnable task = () -> {
                Connection con = null;
                try {
                    con = ds.getConnection();
                    System.out.println("task about to start");
                    List<Future> futures = new ArrayList<>(tableCount);
                    String tableName = null;
                    int lastPeriodPos = 0;
                    int tableIteration = 1;
                    if (tablesMap.size() > 0) {
                        for (Map.Entry<String,ArrayList<StringMap>> entry : tablesMap.entrySet()) {
                            tableName = entry.getKey();
                            ArrayList<StringMap> finalTableMap = entry.getValue();
                            String queryStatement = "INSERT INTO %s (";
                            try {
                                queryStatement += ParseJson.queryStmt(finalTableMap);
                            } catch (IOException ioException) {
                                ioException.printStackTrace();
                            }

                            queryStatement += ") VALUES (";
                            int noOfColumns = 0;
                            try {
                                noOfColumns = ParseJson.getNoColumns(finalTableMap);
                            } catch (IOException ioException) {
                                ioException.printStackTrace();
                            }
                            for (int i = 0; i < noOfColumns; i++) {
                                queryStatement += "?, ";
                            }
                            queryStatement = queryStatement.substring(0, queryStatement.lastIndexOf(","));
                            queryStatement += ")";
                            final String finalQueryStatement = queryStatement;

                            final int tableId = tableIteration;
                            String finalTableName = tableName;
                            long startTime = System.currentTimeMillis();
                            String insert = String.format(finalQueryStatement, finalTableName);
                            try {
                                insertBatch(con, insert, batchSize, finalTableMap, chunkSize);
                            } catch (SQLException throwables) {
                                throwables.printStackTrace();
                            }

                            long count = counterList.get(tableId - 1).incrementAndGet();
                            long endTime = System.currentTimeMillis();
                            String updateStmt = null;
                            try {
                                updateStmt = ParseJson.getUpdateColumn(finalTableMap);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            //String update = String.format("UPDATE %s SET body1 = '" + updateColumn + "', mod_at = CURRENT_TIMESTAMP WHERE id = ?", TABLE_PREFIX + tableId);
                            String update = String.format(updateStmt + ", mod_at = CURRENT_TIMESTAMP() WHERE id = ?", finalTableName);
                            String updMaxId = String.format("SELECT max(id) FROM %s ", finalTableName);
                            PreparedStatement ups = con.prepareStatement(updMaxId);
                            ResultSet urs = ups.executeQuery();
                            //int maximumId = 0;
                            String upMaxId = null;
                            if(urs.next())
                                upMaxId = urs.getString(1);
                            int upMaxResult = Integer.parseInt(upMaxId);
                            int updateBatchSize = Math.max(1, (int) (updateFactor * batchSize));

                            try {
                                //updateBatch(con, update, updateBatchSize, random, count, batchSize);
                                updateBatch(con, update, updateBatchSize, count, finalTableName, upMaxResult);
                            } catch (SQLException throwables) {
                                throwables.printStackTrace();
                            }
                            startTime = System.currentTimeMillis();

                            String delete = String.format("DELETE FROM %s WHERE id = ?", finalTableName);
                            String maxId = String.format("SELECT max(id) FROM %s ", finalTableName);
                            PreparedStatement prepareStatement = con.prepareStatement(maxId);
                            ResultSet rs = prepareStatement.executeQuery();
                            //int maximumId = 0;
                            String maximumId = null;
                            if(rs.next())
                                maximumId = rs.getString(1);
                            int maxResult = Integer.parseInt(maximumId);
                            int deleteBatchSize = Math.max(1, (int) (deleteFactor * batchSize));
                            try {
                                deleteBatch(con, delete, deleteBatchSize, count, finalTableName,maxResult);
                            } catch (SQLException throwables) {
                                throwables.printStackTrace();
                            }

                            endTime = System.currentTimeMillis();
                            /*System.out.println(String.format("Final %d row(s) from table: %s in %fs",
                                    count * batchSize - count, finalTableName, (endTime - startTime) / 1000.0));*/
                            System.out.println(String.format("Inserted Events %d row(s) from table: %s in %fs",
                                    count * batchSize, finalTableName, (endTime - startTime) / 1000.0));
                            /*if ((count * batchSize) >= recordLimit) {
                                System.out.println("record limit execeeded");
                            }*/
                                /*return null;
                            })*/;
                            tableIteration++;
                        }
                    } else {
                        System.out.println("Schemas are missing from resource directory");
                        System.exit(0);
                    }
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                } finally {
                    if (con != null) {
                        try {
                            con.close();
                        } catch (SQLException throwables) {
                            throwables.printStackTrace();
                        }
                    }
                }
            };


            long termination_time_in_millis = System.currentTimeMillis()+(terminateDurationInMins*60000);
            int countThread = 0;
            sTime = LocalDateTime.now();
            do {

                for(int i=0; i<noOfThreads; i++) {
                    Thread thread = new Thread(task);
                    System.out.println("threadName:"+thread.getName());
                    //System.out.println("starttime:" + LocalDateTime.now());
                    thread.start();
                    //System.out.println("endtime:" + LocalDateTime.now());
                }
                Thread.sleep(periodInSecs*1000);
                countThread++;
                System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
                System.out.println("another round of Cdc - thread count : " + countThread);
                System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
            } while(System.currentTimeMillis() < termination_time_in_millis);

            System.out.println("Done!");

            //Thread.sleep(TimeUnit.MINUTES.toMillis(terminateDurationInMins));
            //System.out.println("Time up, shutdown executors!");
        } catch (Exception e) {
            e.printStackTrace();
        }
        eTime = LocalDateTime.now();
        System.out.println("startTime: " + sTime);
        System.out.println("endTime: " + eTime);
        System.out.println("Program has finished");
    }

    /*
    DeleteBatch records
    parameters: connection, delete, random, count, batchsize
     */
    private static void deleteBatch(Connection connection, String delete, int deleteBatchSize,
                                     long count, String finalTableName,int maxResult) throws SQLException {
        // int deleteBatchSize = Math.max(1, (int)(deleteFactor * batchSize));
        //System.out.println("deleteBatchSize: "+deleteBatchSize);
        int deleteCount = 0;
        try (PreparedStatement ps = connection.prepareStatement(delete)) {
            for (int j = 1; j <= deleteBatchSize; j++) {
                System.out.println("deleteThis->Id: "+ maxResult);
                ps.setInt(1, maxResult);
                if (ps.executeUpdate() > 0){
                    deleteCount++;
                    //System.out.println("Record deleted successfully.");
                }
                else {
                    //System.out.println("Record not found.");
                }
                maxResult = maxResult - 1;
                //ps.addBatch();
            }
            long startTime = System.currentTimeMillis();
            ps.executeBatch();
            long endTime = System.currentTimeMillis();
            /*System.out.println(String.format("Deleted %d row(s) from table: %s in %fs",
                    count * deleteCount, finalTableName, (endTime - startTime) / 1000.0));*/
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    /*
    Create UpdateBatch method
    parameters: connection, update, updateBatchSize, random, count, batchSize
     */
    private static void updateBatch(Connection connection, String update, int updateBatchSize,
                                    long count, String finalTableName,int upMaxResult) throws SQLException {
        int updateCount=0;
        try (PreparedStatement ps = connection.prepareStatement(update)) {
                for (int j = 1; j <= updateBatchSize; j++) {
                    //int randId = random.nextInt((int)count * batchSize)
                    //System.out.println("updateThisId->: "+ upMaxResult);
                    ps.setInt(1, upMaxResult);
                    //ps.addBatch();
                    if (ps.executeUpdate() > 0){
                        updateCount++;
                        upMaxResult = upMaxResult - 1;
                        //System.out.println("Record updated successfully.");
                    }
                    else {
                        j = j - 1;
                        upMaxResult = upMaxResult + 1;
                        //System.out.println("Record not found.");
                    }
                }
                long startTime = System.currentTimeMillis();
                ps.executeBatch();
                long endTime = System.currentTimeMillis();
                System.out.println(String.format("Updated %d row(s) from table:%s using %fs",
                        count * updateCount, finalTableName, (endTime - startTime) / 1000.0));
            } catch (SQLException e) {
                e.printStackTrace();
            }
    }

    /*
     Insert execute statement
     parameters: connection, insert, batchSize, tableMap
     */
    private static void insertBatch(Connection connection, String insert, int batchSize, ArrayList<StringMap>tableMap,
                                    int chunkSize)
            throws SQLException {
        Random random = new Random();
        int countInsert  = random.nextInt(10000);
        try (PreparedStatement ps = connection.prepareStatement(insert)) {
            for (int j = 1; j <= batchSize; j++) {
                int index = 1;
                Iterator it = tableMap.iterator();
                while (it.hasNext()) {
                    StringMap d = (StringMap) it.next();
                    String data = null;
                    //columnIndex = ParseJson.countColumns(columnIndex);
                    data = ParseJson.genColumnData(d, chunkSize);
                    ps.setString(index++, data);
                }
                /*ps.setTimestamp(index, Timestamp.valueOf(LocalDateTime.now()));
                ps.setTimestamp(++index, Timestamp.valueOf(LocalDateTime.now()));*/
                ps.addBatch();
            }
            //System.out.println(countInsert+"Insertion started: " + LocalDateTime.now());
            ps.executeBatch();
            //System.out.println(countInsert+"Insertion ended: " + LocalDateTime.now());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
