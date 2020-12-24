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

public class SqlSerPreLoadTest {
  private static final String TABLE_PREFIX = "mytable";
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
    int numOfBodyColumns = Integer.parseInt(args[7]);
    String DB = args[8];
    Random random = new Random();

    String createStatement = "CREATE TABLE %s (id int PRIMARY KEY IDENTITY(1,1), ";
    String queryStatement = "INSERT INTO %s (";
    for (int i = 0; i < numOfBodyColumns; i++) {
      createStatement += "body" + (i + 1) + " text, ";
      queryStatement += "body" + (i + 1) + ", ";
    }
    queryStatement = queryStatement.substring(0, queryStatement.lastIndexOf(","));
    createStatement += "create_at DATETIME2(0) NOT NULL DEFAULT(GETDATE()), mod_at DATETIME2(0) NOT NULL DEFAULT(GETDATE()))";
    //createStatement = createStatement.substring(0, createStatement.lastIndexOf(","));
    //createStatement += ");";
    queryStatement += ") VALUES (";
    for (int i = 0; i < numOfBodyColumns; i++) {
      queryStatement += "?, ";
    }
    queryStatement = queryStatement.substring(0, queryStatement.lastIndexOf(","));
    queryStatement += ")";
    final String finalQueryStatement = queryStatement;
    /*System.out.println("createStatement: "+createStatement);
    System.out.println("queryStatement: " + queryStatement);*/

    try {
      Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
      String connURL = String.format("jdbc:sqlserver://%s:%d", host, port);;
      Properties properties = new Properties();
      properties.put("user", user);
      properties.put("password", password);
      properties.put("serverTimezone", timezone);

      // create database
      try (Connection connection = DriverManager.getConnection(connURL, properties);
           Statement statement = connection.createStatement()) {
        statement.execute("IF (DB_ID('" + DB + "') IS NOT NULL)\n" +
                "\tBEGIN\n" +
                "\tUSE master;\n" +
                "\tALTER DATABASE " + DB + "\n" +
                "\tSET SINGLE_USER \n" +
                "\tWITH ROLLBACK IMMEDIATE\n" +
                "\tDROP DATABASE " + DB + ";\n" +
                "\tEND");
        statement.execute("CREATE DATABASE " + DB);
        System.out.println(String.format("CREATED DATABASE:%s", DB));
      }

      String finalConnURL = connURL + ";databaseName=" + DB;
      // create tables
      try (Connection connection = DriverManager.getConnection(finalConnURL, properties)) {
        for (int i = 1; i <= numOfTables; i++) {
          try (Statement statement = connection.createStatement()) {
            statement.execute(
                    String.format(createStatement, TABLE_PREFIX + i));
          }
        }
        System.out.println(String.format("CREATED %d TABLE(S)", numOfTables));

        try (Statement statement = connection.createStatement()) {
          statement.execute("EXEC sys.sp_cdc_enable_db");
          System.out.println(String.format("ENABLED CDC FOR DATABASE:%s", DB));
        }

        for (int i = 1; i <= numOfTables; i++) {
          try (Statement statement = connection.createStatement()) {
            statement.execute(
                    String.format("EXEC sys.sp_cdc_enable_table @source_schema = N'dbo', @source_name = N'%s', @role_name = NULL", TABLE_PREFIX + i));
          }
        }
        System.out.println("ENABLED CDC FOR ALL TABLE(S)");
        sTime = LocalDateTime.now();
        for (int i = 1; i <= numOfTables; i++) {
          String insert = String.format(queryStatement, TABLE_PREFIX + i);
          try (PreparedStatement ps = connection.prepareStatement(insert)) {
            int index = 1;
            for (int j = 1; j <= numOfBodyColumns; j++) {
              ps.setString(index++, getAlphaNumericString(chunkSize));
            }
            //ps.setTimestamp(index, Timestamp.valueOf(LocalDateTime.now()));
            ps.execute();
          }
        }
        System.out.println("FINISHED PRELOAD ONE RECORD TO EACH TABLE");
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
