/*
Edgar Silveyra
Ahmed 330
Assignment 1
*/





import java.util.Properties;
import java.util.Scanner;
import java.io.FileInputStream;
import java.sql.*;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.util.*;

class assignment1{
  static String[] uInputLIne;
  static Connection conn = null;
  static ArrayList dateAr = new ArrayList();
  static ArrayList<Double> openAr = new ArrayList<Double>();
  static ArrayList<Double> highAr = new ArrayList<Double>();
  static ArrayList<Double> lowAr = new ArrayList<Double>();
  static ArrayList<Double> closeAr = new ArrayList<Double>();
  static ArrayList<Double> openAdjusted = new ArrayList<Double>();
  static ArrayList<Double> highAdjusted = new ArrayList<Double>();
  static ArrayList<Double> lowAdjusted = new ArrayList<Double>();
  static ArrayList<Double> closeAdjusted = new ArrayList<Double>();

  /*-----------Main-------------*/
  public static void main(String[] args) throws Exception {
    String paramsFile = "ConnectionParameters.txt";
    if (args.length >= 1){
      paramsFile = args[0];
    }
    Properties connectprops = new Properties();
    connectprops.load(new FileInputStream(paramsFile));

    //connects to database
    try{
      Class.forName("com.mysql.jdbc.Driver");
      String dburl = connectprops.getProperty("dburl");
      String username = connectprops.getProperty("user");
      conn = DriverManager.getConnection(dburl, connectprops);
      System.out.printf("Database connection %s %s established.%n",dburl,username);
    }
    catch(Exception ex){
      System.out.printf("Error: %s\n\n", ex.getMessage());
    }

    //runs the program if the conditions have been met
    try{
      boolean bool = true;
      while (bool == true){
        uInputLIne = getInput();
        if(uInputLIne[0] == "" || uInputLIne.length > 3){
          bool = false;
          System.out.println("Exiting program....");
        }else{
          executeProgram(uInputLIne);
        }
      }
    }catch(Exception e){
      System.out.println("Error.");
    }
    conn.close();
  }

  /*-----------executeProgram----------*/
  static void executeProgram(String[] inputLine) throws SQLException{
    System.out.println(getCompanies(inputLine[0]));
    retrievePriceVolume(inputLine);
    adjustPrices();
    stockStrategy();
  }

  static void stockStrategy() throws SQLException{
    if(closeAdjusted.size() < 51){
      System.out.println("Net gain: 0");
    }else{
      System.out.println("Show me your moves!");
    }
  }

  //adjust prices in database if a split occurs
  static void adjustPrices(){
    int index = 0;
    Double openD;
    Double closeD;
    String date;
    Double divider = 1.0;
    for (int i = 0; i < openAr.size() - 1; i++){
      index++;
      openD = new Double((openAr.get(i)).toString());
      closeD = new Double((closeAr.get(i+1)).toString());
      date = (dateAr.get(i+1)).toString();
      if(hasSplit(closeD, openD) == true){
        openAdjusted.add(openAr.get(i) / divider);
        highAdjusted.add(openAr.get(i) / divider);
        lowAdjusted.add(openAr.get(i) / divider);
        closeAdjusted.add(openAr.get(i) / divider);

        System.out.println(splitRatio(closeD, openD) + " split on: " + date);
        divider = divider * getRatio(splitRatio(closeD, openD));
        // System.out.println("Divider: " + divider);
        i++;
      }
      openAdjusted.add(openAr.get(i) / divider);
      // System.out.println("Adjusted openAr is: " + openAr.get(i) + " when divider is: " + divider + " and index is: " + (i));
      highAdjusted.add(highAr.get(i) / divider);
      lowAdjusted.add(lowAr.get(i) / divider);
      closeAdjusted.add(closeAr.get(i) / divider);
    }
    openAdjusted.add(openAr.get(index) / divider);
    highAdjusted.add(highAr.get(index) / divider);
    lowAdjusted.add(lowAr.get(index) / divider);
    closeAdjusted.add(closeAr.get(index) / divider);
    // System.out.println("Size of openAr is : " + openAr.size() + " and size of adjustedAr: " + openAdjusted.size());
    // for(int j = 0; j < openAr.size(); j++){
    //       System.out.println("openAr is: " + openAr.get(j));
    //       System.out.println("openAdjusted is: " + openAdjusted.get(j));
    //     }

  }
  //succesfully retrieves the price volume of a ticker with/without dates
  static void retrievePriceVolume(String[] inputLine) throws SQLException{
    if(inputLine.length == 1){
      showAllTickerDay(inputLine[0]);
    }else{
      showRangeTickerDay(inputLine[0], inputLine[1], inputLine[2]);
    }
  }

  //shows the ticker days for a range of dates and adds them to an arraylist
  static void showRangeTickerDay(String ticker, String start, String end) throws SQLException{
    dateAr.clear();
    openAr.clear();
    highAr.clear();
    lowAr.clear();
    closeAr.clear();
    openAdjusted.clear();
    highAdjusted.clear();
    lowAdjusted.clear();
    closeAdjusted.clear();
    PreparedStatement pstmt = conn.prepareStatement(
    "select TransDate, OpenPrice, HighPrice, LowPrice, ClosePrice " +
    " from PriceVolume " +
    " where Ticker = ? and TransDate BETWEEN ? and ?"+
    "order by TransDate DESC");
    pstmt.setString(1, ticker);
    pstmt.setString(2, start);
    pstmt.setString(3, end);
    ResultSet rs = pstmt.executeQuery();

    while (rs.next()){
      // System.out.printf("Date: %s, Open: %.2f, High: %.2f, Low: %.2f, Close: %.2f%n",
      // rs.getString(1), rs.getDouble(2), rs.getDouble(3), rs.getDouble(4), rs.getDouble(5));
      dateAr.add(rs.getString(1));
      openAr.add(rs.getDouble(2));
      highAr.add(rs.getDouble(3));
      lowAr.add(rs.getDouble(4));
      closeAr.add(rs.getDouble(5));
    }

    pstmt.close();

  }
  //shows the ticker day and adds the info into an arraylist
  static void showAllTickerDay(String ticker) throws SQLException {
    dateAr.clear();
    openAr.clear();
    highAr.clear();
    lowAr.clear();
    closeAr.clear();
    openAdjusted.clear();
    highAdjusted.clear();
    lowAdjusted.clear();
    closeAdjusted.clear();
    PreparedStatement pstmt = conn.prepareStatement(
    "select TransDate, OpenPrice, HighPrice, LowPrice, ClosePrice " +
    "  from PriceVolume " +
    "  where Ticker = ?" +
    "order by TransDate DESC");
    pstmt.setString(1, ticker);
    ResultSet rs = pstmt.executeQuery();

    while (rs.next()){
      // System.out.printf("Date: %s, Open: %.2f, High: %.2f, Low: %.2f, Close: %.2f%n",
      // rs.getString(1), rs.getDouble(2), rs.getDouble(3), rs.getDouble(4), rs.getDouble(5));
      dateAr.add(rs.getString(1));
      openAr.add(rs.getDouble(2));
      highAr.add(rs.getDouble(3));
      lowAr.add(rs.getDouble(4));
      closeAr.add(rs.getDouble(5));
    }
    pstmt.close();
  }

  //gets input from the user and stores it into an arrayString
  static String[] getInput(){
    System.out.print("Enter a ticker symbol[start/end dates]:");
    Scanner userInput = new Scanner(System.in);
    String command = userInput.nextLine();
    String[] inputList = command.split(" ");
    return inputList;
  }

  //gets the name of the company when given a ticker title
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

}
