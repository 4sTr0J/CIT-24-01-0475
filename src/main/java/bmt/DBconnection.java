package bmt;

import java.sql.*;

public class DBconnection {
    static String url = "#################";

    static String user = "root";

    static String pass = "password";

    public static Connection connect() throws Exception {
        return DriverManager.getConnection(url, user, pass);

    }
       
}
