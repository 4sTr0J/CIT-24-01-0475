package bmt;

import org.springframework.stereotype.Service;

import java.sql.*;

@Service
public class BankService {

    private final String url =
            "jdbc:mysql://database:3306/bank";

    private final String user = "root";

    private final String password = "password";

    public double getBalance() {

        try {

            Connection con =
                    DriverManager.getConnection(url, user, password);

            Statement st =
                    con.createStatement();

            ResultSet rs =
                    st.executeQuery(
                            "SELECT balance FROM account WHERE id=1");

            if (rs.next()) {

                return rs.getDouble("balance");

            }

        } catch (Exception e) {

            e.printStackTrace();

        }

        return 0;

    }

    public void deposit(double amount) {

        try {

            Connection con =
                    DriverManager.getConnection(url, user, password);

            PreparedStatement ps =
                    con.prepareStatement(
                            "UPDATE account SET balance = balance + ? WHERE id=1");

            ps.setDouble(1, amount);

            ps.executeUpdate();

        } catch (Exception e) {

            e.printStackTrace();

        }

    }

    public void withdraw(double amount) {

        try {

            Connection con =
                    DriverManager.getConnection(url, user, password);

            PreparedStatement ps =
                    con.prepareStatement(
                            "UPDATE account SET balance = balance - ? WHERE id=1");

            ps.setDouble(1, amount);

            ps.executeUpdate();

        } catch (Exception e) {

            e.printStackTrace();

        }

    }

}