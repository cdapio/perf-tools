import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class SqlSerCdcTest {
  private static final String TABLE_PREFIX = "mytable";
  private static final int INIT_DELAY_IN_SECONDS = 0;
  private static final int PERIOD_IN_SECONDS = 60;
  private static List<AtomicLong> counterList;
  private static LocalDateTime sTime;
  private static LocalDateTime eTime;

  public static void main(String[] args) {
    String host = args[0];
    String user = args[1];
    String password = args[2];
    String timezone = args[3];
    int port = Integer.parseInt(args[4]);
    int numOfTables = Integer.parseInt(args[5]);
    int chunkSize = Integer.parseInt(args[6]);
    int terminateDurationInMins = Integer.parseInt(args[7]);
    int batchSize = Integer.parseInt(args[8]);
    double updateFactor = Double.parseDouble(args[9]);
    double deleteFactor = Double.parseDouble(args[10]);
    int numOfBodyColumns = Integer.parseInt(args[11]);
    String DB = args[12];
    //int numOfBodyColumns = remain == 0 ? rowSizeInBytes / chunkSize : rowSizeInBytes / chunkSize + 1;
    Random random = new Random();

    String queryStatement = "INSERT INTO %s (";
    for (int i = 0; i < numOfBodyColumns; i++) {
      queryStatement += "body" + (i + 1) + ", ";
    }
    queryStatement = queryStatement.substring(0, queryStatement.lastIndexOf(","));
    queryStatement += ") VALUES (";
    for (int i = 0; i < numOfBodyColumns; i++) {
      queryStatement += "?, ";
    }
    queryStatement = queryStatement.substring(0, queryStatement.lastIndexOf(","));
    queryStatement += ")";
    final String finalQueryStatement = queryStatement;
    System.out.println("queryStatement: " + queryStatement);

    try {
      Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
      String connURL = String.format("jdbc:sqlserver://%s:%d", host, port);;
      Properties properties = new Properties();
      properties.put("user", user);
      properties.put("password", password);
      properties.put("serverTimezone", timezone);

      // use database
      try (Connection connection = DriverManager.getConnection(connURL, properties);
           Statement statement = connection.createStatement()) {
        statement.execute("USE " + DB);
      }

      String finalConnURL = connURL + ";databaseName=" + DB;

      ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
      final ThreadFactory threadFactory = runnable -> {
        final Thread thread = new Thread(runnable, "load-test-daemon");
        thread.setDaemon(true);
        return thread;
      };
      // create a fixed thread pool, size is equal to num of tables
      ExecutorService executorService = Executors.newFixedThreadPool(numOfTables, threadFactory);
      counterList = new ArrayList<>(numOfTables);
      for (int i = 0; i < numOfTables; i++) {
        counterList.add(new AtomicLong());
      }

      // insert sample data
      // String insert = String.format("INSERT INTO %s VALUES (?, ?, ?)", TABLE);
      // update sample data
      // String update = String.format("UPDATE %s SET name = 'product' WHERE id = ?", TABLE);
      // delete sample data
      // String delete = String.format("DELETE FROM %s WHERE id = ?", TABLE;
      sTime = LocalDateTime.now();
      Runnable task = () -> {
        List<Future> futures = new ArrayList<>(numOfTables);
        try {
          Connection connection = DriverManager.getConnection(finalConnURL, properties);
          for (int i = 1; i <= numOfTables; i++) {
            final int tableId = i;
            futures.add(executorService.submit((Callable<Void>) () -> {
              long startTime = System.currentTimeMillis();
              String insert = String.format(finalQueryStatement, TABLE_PREFIX + tableId);
              try (PreparedStatement ps = connection.prepareStatement(insert)) {
                for (int j = 1; j <= batchSize; j++) {
                  int index = 1;
                  for (int k = 1; k <= numOfBodyColumns; k++) {
                    ps.setString(index++, getAlphaNumericString(chunkSize));
                  }
                  ps.addBatch();
                }
                ps.executeBatch();
              }
              long count = counterList.get(tableId - 1).incrementAndGet();
              long endTime = System.currentTimeMillis();
              System.out.println(String.format("Inserted %d row(s) with size %d bytes to table:%s using %fs", count * batchSize,
                      chunkSize, TABLE_PREFIX + tableId, (endTime - startTime) / 1000.0));
              startTime = System.currentTimeMillis();
              String updateColumn = getAlphaNumericString(chunkSize);
              String update = String.format("UPDATE %s SET body1 = '" + updateColumn + "', mod_at = GETDATE() WHERE id = ?", TABLE_PREFIX + tableId);
              String updMaxId = String.format("SELECT max(id) FROM %s ", TABLE_PREFIX + tableId);
              PreparedStatement ups = connection.prepareStatement(updMaxId);
              ResultSet urs = ups.executeQuery();
              //int maximumId = 0;
              String upMaxId = null;
              if(urs.next())
                upMaxId = urs.getString(1);
              int upMaxResult = Integer.parseInt(upMaxId);
              int updateBatchSize = Math.max(1, (int)(updateFactor * batchSize));
              int updateCount=0;
              //System.out.println("updatedBatchSize: "+updateBatchSize);
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
                ps.executeBatch();
              }
              /*endTime = System.currentTimeMillis();
              System.out.println(String.format("Updated %d row(s) with 'body1' field to table:%s using %fs",
                      updatedCount, TABLE_PREFIX + tableId, (endTime - startTime) / 1000.0));*/
              endTime = System.currentTimeMillis();
              System.out.println(String.format("Updated %d row(s) with 'body1' field to table:%s using %fs",
                      count * updateCount, TABLE_PREFIX + tableId, (endTime - startTime) / 1000.0));
              String delete = String.format("DELETE FROM %s WHERE id = ?", TABLE_PREFIX + tableId);
              String maxId = String.format("SELECT max(id) FROM %s ", TABLE_PREFIX + tableId);
              PreparedStatement prepareStatement = connection.prepareStatement(maxId);
              ResultSet rs = prepareStatement.executeQuery();
              //int maximumId = 0;
              String maximumId = null;
              if(rs.next())
                maximumId = rs.getString(1);
              int maxResult = Integer.parseInt(maximumId);
              int deleteBatchSize = Math.max(1, (int) (deleteFactor * batchSize));
              int deleteCount =0 ;
              try (PreparedStatement ps = connection.prepareStatement(delete)) {
                for (int j = 1; j <= deleteBatchSize; j++) {
                  //int low = (int) (count * batchSize);
                  //int randId = low;
                  //System.out.println("deleteThis->Id: "+ maxResult);
                  //int randId = random.nextInt((int) count * batchSize);
                  ps.setInt(1, maxResult);
                  if (ps.executeUpdate() > 0) {
                    deleteCount++;
                    //System.out.println("Record deleted successfully.");
                  }
                  else{
                    //System.out.println("Record not found.");
                  }
                  maxResult = maxResult - 1;
                  //ps.addBatch();
                }
                //System.out.println("deleting no of records --->" +deleteBatchSize);
                ps.executeBatch();
                //System.out.println(" deleting  of records --->" +deleteBatchSize);
              } catch (SQLException e) {
                e.printStackTrace();
              }

              System.out.println(String.format("Deleted %d row(s) from table: %s in %fs",
                      count*deleteCount, TABLE_PREFIX + tableId, (endTime - startTime) / 1000.0));
              return null;
            }));
          }
        } catch (SQLException e) {
          System.out.println("exception in task");
          e.printStackTrace();
        }

        Exception exception = null;
        for (Future future : futures) {
          try {
            future.get();
          } catch (Exception e) {
            if (exception != null) {
              exception.addSuppressed(e);
            } else {
              exception = e;
            }
          }
        }

        if (exception != null) {
          exception.printStackTrace();
        }
      };

      ScheduledFuture<?> future =
              scheduledExecutorService.scheduleAtFixedRate(task, INIT_DELAY_IN_SECONDS, PERIOD_IN_SECONDS, TimeUnit.SECONDS);

      Thread.sleep(TimeUnit.MINUTES.toMillis(terminateDurationInMins));
      System.out.println("Time up, shutdown executors!");
      future.cancel(true);

      scheduledExecutorService.shutdown();
      System.out.println(scheduledExecutorService.isTerminated());

      executorService.shutdown();
      System.out.println(executorService.isTerminated());

      try {
        System.out.println("attempt to shutdown executor");
        scheduledExecutorService.awaitTermination(10, TimeUnit.SECONDS);
        executorService.awaitTermination(10, TimeUnit.SECONDS);
      } catch (InterruptedException e) {
        System.err.println("tasks interrupted");
        e.printStackTrace();
      } finally {
        System.out.println(scheduledExecutorService.isTerminated());
        System.out.println(executorService.isTerminated());
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
    eTime = LocalDateTime.now();
    System.out.println("startTime: " + sTime);
    System.out.println("endTime: " + eTime);
    System.out.println("Program has finished");
  }

  // function to generate a random string of length n
  private static String getAlphaNumericString(int n) {
    // chose a Character random from this String
    String AlphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
            + "0123456789"
            + "abcdefghijklmnopqrstuvxyz";

    // create StringBuffer size of AlphaNumericString
    StringBuilder sb = new StringBuilder(n);

    for (int i = 0; i < n; i++) {
      // generate a random number between
      // 0 to AlphaNumericString variable length
      int index = (int)(AlphaNumericString.length() * Math.random());

      // add Character one by one in end of sb
      sb.append(AlphaNumericString.charAt(index));
    }

    return sb.toString();
  }
}
