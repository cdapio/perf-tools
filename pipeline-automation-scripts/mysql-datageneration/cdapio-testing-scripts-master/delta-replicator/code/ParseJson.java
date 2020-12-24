import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import com.google.gson.Gson;
import com.google.gson.internal.StringMap;


public class ParseJson {
    public static void main(String[] args) throws Exception
    {

        String schemaPath = "D:/IntraGcp/JavaProject/cdapio-testing-scripts-master/resources/inputschema";
        String tableName = null;

        tableName = "mytable1";

        Map<String, ArrayList<StringMap>> tablesMap  = getSchemaListMap(schemaPath);
        for (Map.Entry<String,ArrayList<StringMap>> entry : tablesMap.entrySet()) {
            System.out.println(entry.getKey() + "----" + entry.getValue());
    }

    //ArrayList<ArrayList<StringMap>> tables = ParseJson.getSchemaListMap(schemaPath);
        //String getGenData = getData(schemaPath);
        /*Test any functions from here Example
        ArrayList<StringMap> tableMap = getSchemaMap(schemaPath);

            Iterator it = tableMap.iterator();
            int index = 0;
            while(it.hasNext()) {
                StringMap d = (StringMap) it.next();
                String data = null;
                data = ParseJson.parseArgs(d);
                index = ParseJson.countColumns(index);
                System.out.println("columnName: " + d.get("name").toString() + ", dataType: " +d.get("type").toString()+ ", index: " + index + ", data: "+ data);
            }
        System.exit(0);*/
        }
    /*
    input parameters: schemaPath
    function returns SchemaMap
     */
    public static ArrayList<StringMap> getSchemaMap(String schemaPath) throws IOException {
        Reader reader = Files.newBufferedReader(Paths.get(String.valueOf(schemaPath)));
        Gson gson = new Gson();
        //Creating a map object from the json data in the file
        Map<?, ?> map = gson.fromJson(reader, Map.class);
        //Extracting the json array from json object and creating an arraylist from it
        ArrayList<StringMap> mytable = (ArrayList<StringMap>) map.get("fields");
        return mytable;
    }

    /*
    input parameters: schemaPath
    function returns list of SchemaMap
     */
    public static Map<String, ArrayList<StringMap>> getSchemaListMap(String schemaPath) throws IOException {
        File dir = new File(schemaPath);
        File[] directoryListing = dir.listFiles();
        ArrayList<StringMap>  mytable  = new ArrayList<StringMap>();
        //ArrayList<ArrayList<StringMap>> tables = new ArrayList<ArrayList<StringMap>>();
        HashMap<String, ArrayList<StringMap>> tables = new HashMap<>();
        for (File filePath : directoryListing) {
            System.out.println("filePath: "+filePath);
            String tableName = filePath.getName();
            //System.out.println("tableName: "+tableName);
            int lastPeriodPos = tableName.lastIndexOf('.');
            if (lastPeriodPos > 0)
                tableName = tableName.substring(0, lastPeriodPos);
            Reader reader = Files.newBufferedReader(Paths.get(String.valueOf(filePath)));
            Gson gson = new Gson();
            //Creating a map object from the json data in the file
            Map<?, ?> map = gson.fromJson(reader, Map.class);
            //Extracting the json array from json object and creating an arraylist from it
            mytable = (ArrayList<StringMap>) map.get("fields");
            tables.put(tableName,mytable);
        }
        return tables;
    }


    /*
    input parameters: schema Map
    returns schema column count
     */

    public static int getNoColumns(ArrayList<StringMap> tableMap) throws IOException {
        Iterator it = tableMap.iterator();
        int columnsCount = 0;
        columnsCount = tableMap.size();
        return columnsCount;
        }

    /*
      input parameters: schema Map
      returns query statement
     */
    public static String queryStmt(ArrayList<StringMap> tableMap) throws IOException {
        String queryStatement = "";
        Iterator it = tableMap.iterator();
        while(it.hasNext()) {
            StringMap d = (StringMap) it.next();
            String data = null;
            queryStatement +=d.get("name").toString() + ", ";;
        }
        return queryStatement.substring(0,queryStatement.lastIndexOf(","));
    }

    /*
      input parameters: schema Map
      returns create statement for the given size
     */
    public static String createStmt(ArrayList<StringMap> tableMap, int chunkSize ) throws IOException {
        String createStatement = "";
        Iterator it = tableMap.iterator();
            while (it.hasNext()) {
                StringMap d = (StringMap) it.next();
                int size = 0, precision = 0, scale = 0;
                String dataVal = null;
                if (d.get("args").toString().contains("size")) {
                    if(d.get("type").toString().contains("char")) {
                        if (d.get("args").toString().split("=")[1].contains("max")) {
                            size = chunkSize;
                            //createStatement += d.get("name").toString() + " " + d.get("type").toString() + ", ";
                            createStatement += d.get("name").toString() + " " + d.get("type").toString() + "("+ size +"), ";
                        } else {
                            //System.out.println("value: "+ d.get("args").toString().split("=")[1]);
                            String value = d.get("args").toString().split("=")[1].replaceAll(".0}", "");
                            //String value = d.get("args").toString().split("=")[1].split(".")[0];
                            //System.out.println("value: " + value);
                            size = Integer.parseInt(value);
                            createStatement += d.get("name").toString() + " " + d.get("type").toString() + "("+ size +"), ";
                        }
                    } else {
                        createStatement += d.get("name").toString() + " " + d.get("type").toString() + ", ";
                    }
                } else if (d.get("args").toString().contains("precision") &&
                        d.get("args").toString().contains("scale")) {
                    String precisionStr = d.get("args").toString().split(",")[0];
                    String scaleStr = d.get("args").toString().split(",")[1];
                    String precisionVal = precisionStr.split("=")[1].replaceAll(".0", "").replaceAll("}", "");
                    String scaleVal = scaleStr.split("=")[1].replaceAll(".0", "").replaceAll("}", "");
                    precision = Integer.parseInt(precisionVal);
                    scale = Integer.parseInt(scaleVal);
                    size = precision;
                    createStatement += d.get("name").toString() + " " + d.get("type").toString() + "("+ size +","+ scale+"), ";
                } else {
                    createStatement += d.get("name").toString() + " " + d.get("type").toString() + ", ";
                }
                //System.out.println("size: " +size+  ",precision: " +precision+  ",scale = " + scale);
            }

        return createStatement.substring(0, createStatement.lastIndexOf(","));
    }

    /*
      input parameters: schema Map iterator (each column from iterator)
      chunkSize only applicable for max size i.e., for payload column
      returns query column Data for each column
     */

    public static String genColumnData(StringMap d, int chunkSize) {
        int size = 0, precision = 0, scale = 0;
        String dataVal = null;
        if (d.get("args").toString().contains("size")) {
            if (d.get("args").toString().split("=")[1].contains("max")) {
                size = chunkSize;
            } else {
                //System.out.println("value: "+ d.get("args").toString().split("=")[1]);
                String value = d.get("args").toString().split("=")[1].replaceAll(".0}", "");
                //String value = d.get("args").toString().split("=")[1].split(".")[0];
                //System.out.println("value: " + value);
                size = Integer.parseInt(value);
            }
        } else if (d.get("args").toString().contains("precision") &&
                d.get("args").toString().contains("scale")) {
            String precisionStr = d.get("args").toString().split(",")[0];
            String scaleStr = d.get("args").toString().split(",")[1];
            String precisionVal = precisionStr.split("=")[1].replaceAll(".0", "").replaceAll("}", "");
            String scaleVal = scaleStr.split("=")[1].replaceAll(".0", "").replaceAll("}", "");
            precision = Integer.parseInt(precisionVal);
            scale = Integer.parseInt(scaleVal);
            size = precision;
        }
        //System.out.println("size: " +size+  ",precision: " +precision+  ",scale = " + scale);
        dataVal = Util.genData(d.get("name").toString(), d.get("type").toString(), size, scale);
        return dataVal;
    }

    /*
      input parameters: schema Map
      returns updated statement for the given size
     */
    public static String getUpdateColumn(ArrayList<StringMap> tableMap) throws IOException {
        String updateStmt = null;
        String dataVal = null;
        int size = 0;
        int scale = 0;
        Iterator it = tableMap.iterator();
        while (it.hasNext()) {
            StringMap d = (StringMap) it.next();
            if (d.get("type").toString().equals("varchar") || d.get("type").toString().equals("text")
                    ||d.get("type").toString().equals("mediumtext") ) {
                if (d.get("args").toString().contains("size")) {
                    if (d.get("args").toString().split("=")[1].contains("max")) {
                        size = 10;
                    } else {
                        String value = d.get("args").toString().split("=")[1].replaceAll(".0}", "");
                        size = Integer.parseInt(value);
                    }
                }
                dataVal = Util.genData(d.get("name").toString(), d.get("type").toString(), size, scale);
                updateStmt = "UPDATE %s SET " + d.get("name").toString() + " = '" + dataVal + "'";
                break;
            }
        }
        return updateStmt;
    }
}
