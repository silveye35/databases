/**
 * Created by silveye on 7/9/17.
 */
import java.util.Properties;
import java.util.Scanner;
import java.io.FileInputStream;
import java.sql.*;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.util.*;
public class assignment1 {
    static Connection conn = null;

    //main method
    public static void main(String[] args) throws Exception {
        String paramsFile = "ConnectionParameters.txt";
        if (args.length >= 1){
            paramsFile = args[0];
        }
        Properties connectprops = new Properties();
        connectprops.load(new FileInputStream(paramsFile));

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
        String[] uInput = getInput();


        conn.close();
    }


    //shows the ticker day
    static void showTickerDay(String ticker, String date) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(
                "select OpenPrice, ClosePrice, HighPrice, LowPrice " +
                        "  from PriceVolume " +
                        "  where Ticker = ? and TransDate = ?");
        pstmt.setString(1, ticker);
        pstmt.setString(2, date);
        ResultSet rs = pstmt.executeQuery();

        if (rs.next()){
            System.out.printf("Open: %.2f, High: %.2f, Low: %.2f, Close: %.2f%n",
                    rs.getDouble(1), rs.getDouble(2), rs.getDouble(3), rs.getDouble(4));
        }
        else {
            System.out.printf("Ticker %s, Date %s not found.5n", ticker, date);
        }
        pstmt.close();
    }

    //shows the companies
    // static void showCompanies() throws SQLException{
    //   Statement stmt = conn.createStatement();
    //   ResultSet results = stmt.executeQuery("select Ticker, Name from Company");
    //
    //   while (results.next()){
    //     System.out.printf("%5s %s%n", results.getString("Ticker"), results.getString("Name"));
    //   }
    //   stmt.close();
    // }

    static String[] getInput(){
        System.out.print("Enter a ticker symbol[start/end dates]:");
        Scanner userInput = new Scanner(System.in);
        String command = userInput.nextLine();
        String[] inputList = command.split(" ");
        return inputList;
    }


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
        return statement;
    }
}
