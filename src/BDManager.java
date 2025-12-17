import java.sql.*;


public class BDManager {
    private static final String URL = "jdbc:mysql://localhost:3306/jackalf";
    private static final String USER = "root";
    private static final String PASSWORD = "";


    public static Connection getConnection() throws Exception {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}