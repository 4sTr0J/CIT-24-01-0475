package bmt;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class BankService {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String STATS_CACHE_KEY = "banking:stats";
    private static final String OP_COUNT_KEY = "banking:operation_count";

    private Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    private void incrementOperationCount() {
        try {
            redisTemplate.opsForValue().increment(OP_COUNT_KEY);
            // Evict stats cache so it recalculates
            redisTemplate.delete(STATS_CACHE_KEY);
        } catch (Exception e) {
            System.err.println("Redis is unavailable or failed to increment: " + e.getMessage());
        }
    }

    public List<Map<String, Object>> listAccounts() {
        List<Map<String, Object>> accounts = new ArrayList<>();
        String sql = "SELECT * FROM account";
        try (Connection con = getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Map<String, Object> acc = new HashMap<>();
                acc.put("id", rs.getInt("id"));
                acc.put("name", rs.getString("name"));
                acc.put("accountNumber", rs.getString("account_number"));
                acc.put("balance", rs.getDouble("balance"));
                accounts.add(acc);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return accounts;
    }

    public Map<String, Object> createAccount(String name, String accountNumber, double initialBalance) {
        String sql = "INSERT INTO account (name, account_number, balance) VALUES (?, ?, ?)";
        incrementOperationCount();
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setString(2, accountNumber);
            ps.setDouble(3, initialBalance);
            ps.executeUpdate();
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int id = generatedKeys.getInt(1);
                    logTransaction(con, id, "CREATE_ACCOUNT", initialBalance, "Account created with initial balance");
                    Map<String, Object> acc = new HashMap<>();
                    acc.put("id", id);
                    acc.put("name", name);
                    acc.put("accountNumber", accountNumber);
                    acc.put("balance", initialBalance);
                    return acc;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void deposit(int accountId, double amount, String description) {
        String updateSql = "UPDATE account SET balance = balance + ? WHERE id = ?";
        incrementOperationCount();
        try (Connection con = getConnection()) {
            con.setAutoCommit(false);
            try (PreparedStatement ps = con.prepareStatement(updateSql)) {
                ps.setDouble(1, amount);
                ps.setInt(2, accountId);
                ps.executeUpdate();
                logTransaction(con, accountId, "DEPOSIT", amount, description);
                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void withdraw(int accountId, double amount, String description) {
        String updateSql = "UPDATE account SET balance = balance - ? WHERE id = ? AND balance >= ?";
        incrementOperationCount();
        try (Connection con = getConnection()) {
            con.setAutoCommit(false);
            try (PreparedStatement ps = con.prepareStatement(updateSql)) {
                ps.setDouble(1, amount);
                ps.setInt(2, accountId);
                ps.setDouble(3, amount);
                int rowsUpdated = ps.executeUpdate();
                if (rowsUpdated == 0) {
                    throw new SQLException("Insufficient funds or invalid account ID");
                }
                logTransaction(con, accountId, "WITHDRAWAL", amount, description);
                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    public void transfer(int fromAccountId, int toAccountId, double amount) {
        if (fromAccountId == toAccountId) {
            throw new IllegalArgumentException("Cannot transfer to the same account");
        }
        incrementOperationCount();
        try (Connection con = getConnection()) {
            con.setAutoCommit(false);
            try {
                // Withdraw from sender
                String withdrawSql = "UPDATE account SET balance = balance - ? WHERE id = ? AND balance >= ?";
                try (PreparedStatement ps = con.prepareStatement(withdrawSql)) {
                    ps.setDouble(1, amount);
                    ps.setInt(2, fromAccountId);
                    ps.setDouble(3, amount);
                    int rows = ps.executeUpdate();
                    if (rows == 0) {
                        throw new SQLException("Insufficient funds for transfer");
                    }
                }
                // Deposit to receiver
                String depositSql = "UPDATE account SET balance = balance + ? WHERE id = ?";
                try (PreparedStatement ps = con.prepareStatement(depositSql)) {
                    ps.setDouble(1, amount);
                    ps.setInt(2, toAccountId);
                    ps.executeUpdate();
                }
                logTransaction(con, fromAccountId, "TRANSFER_OUT", amount, "Transferred to account ID " + toAccountId);
                logTransaction(con, toAccountId, "TRANSFER_IN", amount, "Transferred from account ID " + fromAccountId);
                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    public List<Map<String, Object>> getTransactions(int accountId) {
        List<Map<String, Object>> transactions = new ArrayList<>();
        String sql = "SELECT * FROM transaction_log WHERE account_id = ? ORDER BY timestamp DESC";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> t = new HashMap<>();
                    t.put("id", rs.getInt("id"));
                    t.put("accountId", rs.getInt("account_id"));
                    t.put("type", rs.getString("type"));
                    t.put("amount", rs.getDouble("amount"));
                    t.put("timestamp", rs.getTimestamp("timestamp").toString());
                    t.put("description", rs.getString("description"));
                    transactions.add(t);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return transactions;
    }

    public Map<String, Object> getSystemStats() {
        // Try reading stats from Redis cache
        try {
            String cachedTotal = redisTemplate.opsForValue().get(STATS_CACHE_KEY);
            String cachedOps = redisTemplate.opsForValue().get(OP_COUNT_KEY);
            if (cachedTotal != null) {
                Map<String, Object> stats = new HashMap<>();
                stats.put("totalDeposits", Double.parseDouble(cachedTotal));
                stats.put("totalOperations", cachedOps != null ? Integer.parseInt(cachedOps) : 0);
                stats.put("cached", true);
                return stats;
            }
        } catch (Exception e) {
            System.err.println("Redis read failed: " + e.getMessage());
        }

        // Fallback or Cache Miss: Query Database
        double totalBalance = 0;
        int totalOps = 0;
        String balanceSql = "SELECT SUM(balance) AS total FROM account";
        try (Connection con = getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(balanceSql)) {
            if (rs.next()) {
                totalBalance = rs.getDouble("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try {
            String opsStr = redisTemplate.opsForValue().get(OP_COUNT_KEY);
            if (opsStr != null) {
                totalOps = Integer.parseInt(opsStr);
            }
        } catch (Exception e) {
            // Ignore redis errors
        }

        // Cache the result in Redis with a 30 seconds expiration
        try {
            redisTemplate.opsForValue().set(STATS_CACHE_KEY, String.valueOf(totalBalance), 30, TimeUnit.SECONDS);
        } catch (Exception e) {
            // Ignore redis errors
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalDeposits", totalBalance);
        stats.put("totalOperations", totalOps);
        stats.put("cached", false);
        return stats;
    }

    private void logTransaction(Connection con, int accountId, String type, double amount, String description) throws SQLException {
        String logSql = "INSERT INTO transaction_log (account_id, type, amount, description) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(logSql)) {
            ps.setInt(1, accountId);
            ps.setString(2, type);
            ps.setDouble(3, amount);
            ps.setString(4, description);
            ps.executeUpdate();
        }
    }
}