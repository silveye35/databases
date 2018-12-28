import java.io.*;
import java.sql.*;
import java.util.*;

/*
Edgar Silveyra
Ahmed 300
 */


public class assignment2 {
    static Connection conn = null;
    static Connection conn2 = null;
    static ArrayList<String[]> tickerIntervalLimits = new ArrayList<String[]>();
    static ArrayList<String> fstTckrIntvs = new ArrayList<String>();
    static ArrayList<String[]> fstTckrIntervals = new ArrayList<String[]>();
    static ArrayList<String[]> tickerIntervals = new ArrayList<String[]>();
    static ArrayList<String[]> tickerArray = new ArrayList<String[]>();
    static HashMap<String, String[]> pricesMaps = new HashMap<String, String[]>();
    static ArrayList<String[]> priceArray = new ArrayList<String[]>();

    static final String dropPerformanceTable= "drop table if exists Performance;";
    public static void main(String[] args) throws Exception {
        String readerFile = "readerparams.txt";
        String writerFile = "writerparams.txt";
        if (args.length >= 1){
            readerFile = args[0];
        }
        Properties connectprops = new Properties();
        Properties connectprops2 = new Properties();
        connectprops.load(new FileInputStream(readerFile));
        connectprops2.load(new FileInputStream(writerFile));

        //connects to johnson330 database
        try{
            Class.forName("com.mysql.jdbc.Driver");
            String dburl = connectprops.getProperty("dburl");
            String username = connectprops.getProperty("user");
            conn = DriverManager.getConnection(dburl, connectprops);
            System.out.println("Reader connection established.");
        } catch(Exception ex){
            System.out.printf("Error: %s\n\n", ex.getMessage());
        }

        //connects to silveye database
        try{
            Class.forName("com.mysql.jdbc.Driver");
            String dburl = connectprops2.getProperty("dburl");
            String username = connectprops2.getProperty("user");
            conn2 = DriverManager.getConnection(dburl, connectprops);
            System.out.println("Writer connection established");
        } catch(Exception ex){
            System.out.printf("Error: %s\n\n", ex.getMessage());
        }

        //tries to run the program
        try{
            executeProgram();
            dropPerformanceTable();
        }catch (Exception e){
            System.out.println("An error has occurred.");
        }

        conn.close();
    }

    //executes the program
    static void executeProgram() throws SQLException{
        showIndustryCount();
//        dropPerformanceTable();
        industryExamination();
        getIntervals();

    }


    //gets the name of the company when given a ticker
    static String getCompanies(String ticker) throws SQLException{
        String statement = "";
        Statement stmt = conn.createStatement();
        ResultSet results = stmt.executeQuery("select Ticker, Name from Company");

        while(results.next()){
            if(results.getString("Ticker").equals(ticker)){
                statement = results.getString("Name");
                break;
            }else{
                statement = "Results not found";
            }
        }
        stmt.close();
        return statement;
    }

    //shows the industry and how many tickers it contains
    static void showIndustryCount() throws SQLException{
        Statement stmt = conn.createStatement();
        int rowcount = 0;
        ResultSet results = stmt.executeQuery("select Industry, count(distinct Ticker) as TickerCnt " +
                " from Company natural join PriceVolume " +
                " group by Industry " +
                " order by Industry ");
        if (results.last()) {
            rowcount = results.getRow();
            results.beforeFirst();
        }
        System.out.println(rowcount + " industries found.");
        while (results.next()){
            System.out.println(results.getString("Industry") + " ");
        }
        stmt.close();
    }

    //examines each ticker and their information
    static void industryExamination() throws SQLException{
        tickerIntervalLimits.clear();
        String ticker;
        String minTransDate;
        String maxTransDate;
        String transDateCount;
        String maxDate;
        String minDate;
        Statement stmt = conn.createStatement();
        ResultSet results = stmt.executeQuery("select max(minTransDate) as max, min(maxTransDate) as min " +
                " from(select Ticker, min(TransDate) as minTransDate, max(TransDate) as maxTransDate, count(distinct TransDate) as TradingDays " +
                " from Company natural join PriceVolume " +
                " where Industry = 'Telecommunications Services' "+
                " group by Ticker "+
                " having TradingDays >= 150 "+
                " order by Ticker) as alias;");
        results.next();
        maxDate = results.getString("max");
        minDate = results.getString("min");

        PreparedStatement pstm = conn.prepareStatement("select Ticker, min(TransDate), max(TransDate),count(distinct TransDate) as TradingDays "+
                " from Company natural join PriceVolume " +
                " where Industry = 'Telecommunications Services' and TransDate >= ? and TransDate <= ? "+
                " group by Ticker " +
                " having TradingDays >= 150 " +
                " order by Ticker ");
        pstm.setString(1, maxDate);
        pstm.setString(2, minDate);
        ResultSet rs = pstm.executeQuery();

        while(rs.next()){
            String[] tickerHelper = new String[4];
            tickerHelper[0] = rs.getString("Ticker").replaceAll("\\s","");
            tickerHelper[1] = rs.getString("min(TransDate)").replaceAll("\\s","");
            tickerHelper[2] = rs.getString("max(TransDate)").replaceAll("\\s","");
            tickerHelper[3] = rs.getString("TradingDays").replaceAll("\\s","");
            tickerIntervalLimits.add(tickerHelper);
        }

//        for(int i = 0; i < tickerIntervalLimits.size(); i++){
//            System.out.println(Arrays.toString(tickerIntervalLimits.get(i)));
//        }

        stmt.close();
    }

    //gets the intervals for the other tickers
    static void getIntervals() throws SQLException{
        fstTckrIntervals.clear();
        String start;
        String end;
        Double openD;
        Double closeD;
        String openAdjusted;
        String closeAdjusted;
        String date;
        int dayNumber = 0;
        String[] helperA = new String[6];
        String[] firstTickerHelper = new String[2];
        String[] allTickerHelper = new String[3];
        String[] tickerRangePrices = new String[3];
        String[] tickerDatePrices = new String[2];
        ResultSet varTickerIntervalsSet;
        ResultSet varTickerPriceSet;
        ResultSet varTickerPriceSet2;
        ResultSet tickerDataSet;
        Statement stmt = conn.createStatement();

        String ticker = tickerIntervalLimits.get(0)[0];
        String minDate = tickerIntervalLimits.get(0)[1];
        String maxDate = tickerIntervalLimits.get(0)[2];
        //queries the first ticker and displays all the trading days
        PreparedStatement firstDatesPS = conn.prepareStatement("select P.TransDate " +
                "from PriceVolume P " +
                "where Ticker = ? and TransDate >= ? and TransDate <= ? ");
        firstDatesPS.setString(1, ticker);
        firstDatesPS.setString(2, minDate);
        firstDatesPS.setString(3, maxDate);
        ResultSet firstDates = firstDatesPS.executeQuery();


        while(firstDates.next()){
            dayNumber++;
            if((dayNumber % 60) == 1) {
                fstTckrIntvs.add(firstDates.getString("P.TransDate"));
            }else if((dayNumber % 60) == 0){
                fstTckrIntvs.add(firstDates.getString("P.TransDate"));
            }
        }
        fstTckrIntvs.remove(fstTckrIntvs.size() - 1);
        for(int k = 0; k < fstTckrIntvs.size(); k += 2) {
            firstTickerHelper[0] = fstTckrIntvs.get(k);
            firstTickerHelper[1] = fstTckrIntvs.get(k + 1);
            fstTckrIntervals.add(firstTickerHelper);
            firstTickerHelper = new String[2];
        }

        //queries and stores all the data from AMT to be used later for split adjustment
        PreparedStatement tickerDataStatement = conn.prepareStatement("select P.TransDate as Date, P.openPrice as Open, P.closePrice as Close, Ticker\n" +
                "from PriceVolume P natural join Company\n" +
                "where Industry = 'Telecommunications Services'and TransDate BETWEEN ? and ? and Ticker = 'WIN'\n" +
                "order by TransDate DESC, Ticker;");
        tickerDataStatement.setString(1, "2005.02.09");
        tickerDataStatement.setString(2, "2014.08.18");
        tickerDataSet = tickerDataStatement.executeQuery();


        //stores open, close, date data into a tickerarray
        while(tickerDataSet.next()){
            allTickerHelper[0] = tickerDataSet.getString("Date");
            allTickerHelper[1] = tickerDataSet.getString("Open").replaceAll("\\s","");
            allTickerHelper[2] = tickerDataSet.getString("Close").replaceAll("\\s","");
            tickerArray.add(allTickerHelper);
            allTickerHelper = new String[3];
        }

//        for(int i = 0; i < tickerArray.size(); i++){
//            System.out.println(Arrays.toString(tickerArray.get(i)));
//        }


        //prints the array containing the intervals for the first ticker
//        for(int i = 0; i < fstTckrIntervals.size(); i++){
//            System.out.println(Arrays.toString(fstTckrIntervals.get(i)));
//        }

        //prepares an sql statement to allow us to get the intervals for the other tickers when given a date range
        PreparedStatement pstm = conn.prepareStatement("select min(TransDate) as Start, max(TransDate) as End " +
                "from( select P.TransDate, Ticker " +
                "from PriceVolume P " +
                "where Ticker = 'AMT' and TransDate >= ? and TransDate <= ?) as alias");

        PreparedStatement priceStatement = conn.prepareStatement("(select P.openPrice as Open, P.closePrice as Close" +
                " from PriceVolume P " +
                "where TransDate = ? and Ticker = 'AMT')");
        PreparedStatement priceStatement2 = conn.prepareStatement("(select M.openPrice as Open, M.closePrice as Close" +
                " from PriceVolume M " +
                "where TransDate = ? and Ticker = 'AMT')");



        Double ratio = 1.0;
        for (int i = 0; i < tickerArray.size() - 1; i++){
            String[] tickerHelper = new String[3];
            openD = new Double((tickerArray.get(i)[1]));
            closeD = new Double((tickerArray.get(i+1)[2]));
            date = (tickerArray.get(i+1)[0]);
            if(hasSplit(closeD, openD)) {
//                System.out.println(splitRatio(closeD, openD) + " split on: " + date);
                tickerHelper[0] = tickerArray.get(i)[0];
                tickerHelper[1] = Double.toString(new Double(tickerArray.get(i)[1])/ratio);
                tickerHelper[2] = Double.toString(new Double(tickerArray.get(i)[2])/ratio);
                ratio = getRatio(splitRatio(closeD,openD));
                priceArray.add(tickerHelper);
            }else{
                tickerHelper[0] = tickerArray.get(i)[0];
                tickerHelper[1] = Double.toString(new Double(tickerArray.get(i)[1])/ratio);
                tickerHelper[2] = Double.toString(new Double(tickerArray.get(i)[2])/ratio);
                priceArray.add(tickerHelper);
            }
        }
        String[] tickerHelper = new String[3];
        tickerHelper[0] = tickerArray.get(tickerArray.size()-1)[0];
        tickerHelper[1] = Double.toString(new Double(tickerArray.get(tickerArray.size()-1)[1])/ratio);
        tickerHelper[2] = Double.toString(new Double(tickerArray.get(tickerArray.size()-1)[2])/ratio);
        priceArray.add(tickerHelper);

        String mapDate;
        for(int i = 0; i < priceArray.size(); i++){
            mapDate = priceArray.get(i)[0];
            openAdjusted = priceArray.get(i)[1];
            closeAdjusted = priceArray.get(i)[2];
            tickerDatePrices[0] = openAdjusted;
            tickerDatePrices[1] = closeAdjusted;
            pricesMaps.put(mapDate, tickerDatePrices);
            tickerDatePrices = new String[2];
        }



        //gets the intervals for other companies and adds it to tickerInterVals
        for(int i = 0; i < fstTckrIntervals.size(); i++){
            start = fstTckrIntervals.get(i)[0];
            end = fstTckrIntervals.get(i)[1];
            pstm.setString(1, start);
            pstm.setString(2, end);
            priceStatement.setString(1, start);
            priceStatement2.setString(1, end);
            varTickerIntervalsSet = pstm.executeQuery();
            varTickerPriceSet = priceStatement.executeQuery();
            varTickerPriceSet2 = priceStatement2.executeQuery();
            varTickerIntervalsSet.next();
            varTickerPriceSet.next();
            varTickerPriceSet2.next();
            helperA[0] = varTickerIntervalsSet.getString("Start");
            helperA[1] = pricesMaps.get(helperA[0])[0].replaceAll("\\s","");
            helperA[2] = pricesMaps.get(helperA[0])[1].replaceAll("\\s","");
            helperA[3] = varTickerIntervalsSet.getString("End");
            helperA[4] = pricesMaps.get(helperA[3])[0].replaceAll("\\s","");
            helperA[5] = pricesMaps.get(helperA[3])[1].replaceAll("\\s","");
            tickerIntervals.add(helperA);
            helperA = new String[6];
        }


        Double tickerReturn;
        Double openPrice;
        Double closePrice;
        ArrayList<String[]> tickerInformation = new ArrayList<String[]>();
        for(int i = 0; i < tickerIntervals.size(); i++){
            openPrice = new Double(tickerIntervals.get(i)[1]);
            closePrice = new Double(tickerIntervals.get(i)[5]);
            tickerReturn = (closePrice/openPrice) - 1;
            String[] tickerInfoHelper = new String[3];
            tickerInfoHelper[0] = tickerIntervals.get(i)[0];
            tickerInfoHelper[1] = tickerIntervals.get(i)[3];
            tickerInfoHelper[2] = Double.toString(tickerReturn);
            tickerInformation.add(tickerInfoHelper);
        }
        System.out.println("Start date    End Date      Ticker Return");
        for(int i = 0; i < tickerInformation.size(); i++){
            System.out.println(Arrays.toString(tickerInformation.get(i)));
        }


        //places information into database table
//        Statement tblstmt = conn2.createStatement();
//        tblstmt.executeUpdate("create table Performance (Industry char(30), Ticker char(6), StartDate char(10), EndDate char(10), TickerReturn char(12), IndustryReturn char(12));”;");
//
//        for(int i = 0; i < tickerInformation.size(); i++){
//            tblstmt.executeUpdate("insert into Performance(Industry, Ticker, StartDate, EndDate, TickerReturn, IndustryReturn) values(?, ?, ?, ?, ?, ?);”;");
//        }








        stmt.close();
        pstm.close();
        priceStatement.close();

    }

    //get ratio from a string
    static Double getRatio(String ratio){
        Double ratioR;
        if(ratio.equals("2:1")){
            ratioR = 2.0;
        } else if(ratio.equals("3:1")){
            ratioR = 3.0;
        } else{
            ratioR = 1.5;
        }
        return ratioR;
    }

    //returns the ratio of the split
    static String splitRatio(double closing, double opening){
        if(Math.abs((closing/opening)-2.0) < 0.20){
            return ("2:1");
        } else if (Math.abs((closing/opening)-3.0) < 0.30){
            return ("3:1");
        } else if(Math.abs((closing/opening)-1.5) < 0.15){
            return ("3:2");
        } else{
            return ("No split. Check logic");
        }
    }

    //checks if split has occured
    static boolean hasSplit(double closing, double opening){
        if(Math.abs((closing/opening)-2.0) < 0.20){
            return true;
        } else if (Math.abs((closing/opening)-3.0) < 0.30){
            return true;
        } else if(Math.abs((closing/opening)-1.5) < 0.15){
            return true;
        } else{
            return false;
        }
    }




    //drops the Performance table if it exists
    static void dropPerformanceTable() throws SQLException{
        Statement stmt = conn2.createStatement();
        stmt.executeUpdate(dropPerformanceTable);
        stmt.close();
    }
}
