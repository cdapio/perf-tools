import java.io.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.sql.Timestamp;
import java.util.Random;
import java.util.Date;

public class Util {
    public static void main(String[] args) throws IOException, Exception {
        //tests below function here
        /*tests function here
        File file = new File("D:/IntraGcp/JavaProject/cdapio-testing-scripts-master/resources/Schema.txt");    //creates a new file instance
        FileReader fr = new FileReader(file);   //reads the file
        BufferedReader br = new BufferedReader(fr);  //creates a buffering character input stream
        String line, columnName, dataType;
        while ((line = br.readLine()) != null) {
            //System.out.println("Key: " + line.split(" ")[0]);
            columnName = line.split(" ")[0];
            dataType = line.split(" ")[1];
            int size = 0, scale = 0 ;
            if (dataType.contains("(") && dataType.contains(")") && !dataType.contains(",")) {
                String sizeValue = dataType.substring(dataType.indexOf('(') + 1, dataType.indexOf(')'));
                if(sizeValue.equals("max")) {
                    size = 4000;
                } else {
                    size = Integer.parseInt(sizeValue);
                }
            } else if (dataType.contains("(") && dataType.contains(")") && dataType.contains(",")) {
                String sizeValue = dataType.substring(dataType.indexOf('(') + 1, dataType.indexOf(')'));
                String precisionValue = sizeValue.split(",")[0];
                String scaleValue = sizeValue.split(",")[1];
                size = Integer.parseInt(precisionValue);
                scale = Integer.parseInt(scaleValue);
            } else {
                //do nothing
            }
            genData(columnName, dataType, size, scale);
        }
        fr.close();*/
    }

    /*
    input parameters: column name, data type, size and scale
    function returns data for the above parameters
     */
    public static String genData(String columnName, String dataType, int size, int scale) {
        String dataTypeValue =null;
        String data = null;
        if (dataType.contains("(") && dataType.contains(")")) {
            dataTypeValue = dataType.substring(0, dataType.indexOf('('));
        } else {
            dataTypeValue = dataType;
        }
        //System.out.println("column: " + columnName + "dataType: " + dataTypeValue);
        int min, max;
        double minDec, maxDec;
        long offset, end;
        dataTypeValue = dataTypeValue.toLowerCase();
        if (dataTypeValue.equals("tinyint")) {
            //tinyint
            min=getNumMinRange(size);
            max=getNumMaxRange(size);
            int tinyIntNumber = genNumber(min, max);
            //System.out.println("columnName: "+ columnName+  ", dataType: " + dataType+ ", tinyIntNumber: " + tinyIntNumber);
            data = String.valueOf(tinyIntNumber);

        } else if (dataTypeValue.equals("int")) {
            //int
            min=getNumMinRange(size);
            max=getNumMaxRange(size);
            int IntNumber = genNumber(min, max);
            //System.out.println("columnName: "+ columnName+  ", dataType: " + dataType+  ", intNumber: " + IntNumber);
            data = String.valueOf(IntNumber);

        } else if (dataTypeValue.equals("smallint")) {
            //smallInt
            min=getNumMinRange(size);
            max=getNumMaxRange(size);
            int smallNumber = genNumber(min, max);
            //System.out.println("columnName: "+ columnName+ ", dataType: " + dataType+ ", smallNumber: " + smallNumber);
            data = String.valueOf(smallNumber);
        } else if (dataTypeValue.equals("char")) {
            //char
            String charVal = getAlphaNumericString(size);
            //System.out.println("columnName: "+ columnName+ ", dataType: " + dataType+  ", charVal: " + charVal);
            data = String.valueOf(charVal);
            //varchar
	    } else if (dataTypeValue.equals("varchar") || dataTypeValue.equals("text") || dataTypeValue.equals("mediumtext")) {
            String varcharVal = getAlphaNumericString(size);
            //System.out.println("columnName: "+ columnName+  ", dataType: " + dataType+  ", varChar: " + varcharVal);
            data = String.valueOf(varcharVal);
        } else if (dataTypeValue.equals("nvarchar")) {
            //nvarchar
            String ncharVal = getAlphaNumericString(size);
            //System.out.println("columnName: "+ columnName+  ", dataType: " + dataType+  ", ncharVal: " + ncharVal);
            data = String.valueOf(ncharVal);
        } else if (dataTypeValue.equals("decimal")) {
            //decimal
            int decSize = size;
            int decscale = scale;
            String minDigits = "1";
            String maxDigits = "9";
            for(int i=0;i<(size-scale)-1; i++ ){
                minDigits = minDigits.concat("1");
                maxDigits = maxDigits.concat("9");
            }
            minDigits = minDigits.concat(".");
            maxDigits = maxDigits.concat(".");
            for(int i=(size-scale) + 1;i<=(size-scale)+scale; i++ ){
                minDigits = minDigits.concat("1");
                maxDigits = maxDigits.concat("9");
            }
            /*System.out.println("minDigits: " +minDigits);
            System.out.println("maxDigits: " +maxDigits);*/
            minDec = Double.parseDouble(minDigits);
            maxDec = Double.parseDouble(maxDigits);
            double decVal = getDecimal(minDec, maxDec);
            //System.out.println("columnName: "+ columnName+  ", dataType: " + dataType+  ", DecimalValue: " + decVal);
            //data = String.valueOf(decVal);
            data = new BigDecimal(String.valueOf(decVal)).toPlainString();
        } else if (dataTypeValue.equals("datetime")) {
            //datetime
            offset = Timestamp.valueOf("2012-01-01 00:00:00").getTime();
            end = Timestamp.valueOf("2013-01-01 00:00:00").getTime();
            Date timestamp = getDate(offset, end);
            //System.out.println("columnName: "+ columnName+  ", dataType: " + dataType+  ", DateTime: " + timestamp);
            data = String.valueOf(timestamp);
        } else if (dataTypeValue.equals("timestamp")) {
            //datetime
            offset = Timestamp.valueOf("2012-01-01 00:00:00").getTime();
            end = Timestamp.valueOf("2013-01-01 00:00:00").getTime();
            Date timestamp = getDate(offset, end);
            //System.out.println("columnName: "+ columnName+  ", dataType: " + dataType+  ", DateTime: " + timestamp);
            data = String.valueOf(timestamp);
        } else if (dataTypeValue.equals("datetimeoffset")) {
            //offsetDateTime
            offset = Timestamp.valueOf("2012-01-01 00:00:00").getTime();
            end = Timestamp.valueOf("2013-01-01 00:00:00").getTime();
            OffsetDateTime offsetDateTime = getOffsetDateTime(offset, end);
            //System.out.println("columnName: "+ columnName+  ", dataType: " + dataType+  ", offsetDateTime: " + offsetDateTime);
            data = String.valueOf(offsetDateTime);
        }
        return data;
    }

    /*
    input parameters: size
    function returns data min range
     */
    private static int getNumMinRange(int size) {
        String minDigits = "1";

        for(int i=0;i<size-2; i++ ){
            minDigits = minDigits.concat("1");
        }
        int min = Integer.parseInt(minDigits);
        return min;
    }

    /*
    input parameters: size
    function returns data max range
     */
    private static int getNumMaxRange(int size) {
        String maxDigits = "2";
        for(int i=0;i<size-2; i++ ){
            maxDigits = maxDigits.concat("1");
        }
        int max = Integer.parseInt(maxDigits);
        return max;
    }

    /*
    input parameters: min and max range
    function returns number
     */

    private static int genNumber(int min, int max) {
        Random random = new Random();

        // nextInt is normally exclusive of the top value,
        // so add 1 to make it inclusive
        int randomNum = random.nextInt((max - min) + 1) + min;
        return randomNum;
    }

    /*
    input parameters: size n
    function returns alphanumeric string
     */
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
            int index = (int) (AlphaNumericString.length() * Math.random());

            // add Character one by one in end of sb
            sb.append(AlphaNumericString.charAt(index));
        }
        return sb.toString();
    }

    /*
    input parameters: min and max range
    function returns decimal value
     */
    private static double getDecimal(double min, double max) {
        /*Random r = new Random();
        return (r.nextInt((int) ((max - min) * 10 + 1)) + min * 10) / 10.0;*/
        double decimalVal = (Math.random()*((max-min)+1))+min;
        return decimalVal;
    }

    /*
    input parameters: offset and end range
    function returns date with in specified range
     */
    private static Date getDate(long offset, long end) {
        long diff = end - offset + 1;
        Timestamp rand = new Timestamp(offset + (long) (Math.random() * diff));
        return rand;
    }

    /*
    input parameters: offset and end range
    function returns offsetDateTime with in specified range
     */
    private static OffsetDateTime getOffsetDateTime(long offset, long end) {
        long diff = end - offset + 1;
        OffsetDateTime offsetDateTime = OffsetDateTime.now();
        return offsetDateTime;
    }
    /*
    inputParameters: Schema Path
    output: tableCount
     */
    public static int getNumTables(String schemaPath){
        int tableCount = 0;
        File dir = new File(schemaPath);
        File[] directoryListing = dir.listFiles();
        if (directoryListing != null) {
            for (File child : directoryListing) {
                if (child.isFile()) {
                    tableCount++;
                }
            }
        } else {
            System.out.println("schema 's doesn't exist");
            System.exit(0);
        }
        return tableCount;
    }
}
