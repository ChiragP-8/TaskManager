package database;

import java.sql.*;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/task_db?useSSL=false";
    private static final String USER = "root";
    private static final String PASS = "Chirag@813";
    private Connection conn;

    public DatabaseManager() throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        conn = DriverManager.getConnection(DB_URL, USER, PASS);
    }

    public void addTask(String username, String task, String dueDate,
                        String priority, String status) throws SQLException {
        String sql = "INSERT INTO tasks VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, task);
            stmt.setString(3, dueDate);
            stmt.setString(4, priority);
            stmt.setString(5, status);
            stmt.executeUpdate();
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new SQLException("Task '" + task + "' already exists!", e);
        }
    }

    public String getTasks(String username) throws SQLException {
        StringBuilder result = new StringBuilder();
        String sql = "SELECT task, due_date, priority, status FROM tasks WHERE user = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                result.append(rs.getString("task")).append(";")
                        .append(rs.getString("due_date")).append(";")
                        .append(rs.getString("priority")).append(";")
                        .append(rs.getString("status")).append("\n");
            }
        }
        return result.toString().trim();
    }

    public void deleteTask(String username, String task, String dueDate,
                           String priority, String status) throws SQLException {
        String sql = "DELETE FROM tasks WHERE user=? AND task=? AND due_date=? AND priority=? AND status=?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, task);
            stmt.setString(3, dueDate);
            stmt.setString(4, priority);
            stmt.setString(5, status);
            stmt.executeUpdate();
        }
    }

    public void close() throws SQLException {
        if (conn != null) {
            conn.close();
        }
    }
}