package connectDB;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectDB {
    public static Connection con = null;             // GIỮ y như bài cũ
    private static ConnectDB instance = new ConnectDB();

    public static ConnectDB getInstance() { return instance; }

    public void connect() throws SQLException {
        try { Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver"); } catch (ClassNotFoundException ignored) {}
        if (con == null || con.isClosed()) {
            String url = "jdbc:sqlserver://localhost:1433;databaseName=Coffee;encrypt=true;trustServerCertificate=true";
            String user = "NguyenTuanDat_Server";
            String password = "03102005";
            con = DriverManager.getConnection(url, user, password);
        }
        System.out.println("Hello world");

    }

    public void disconnect() {
        try { if (con != null && !con.isClosed()) con.close(); } catch (SQLException ignored) {}
        con = null;
    }

    public static Connection getConnection() {
        try {
            if (con == null || con.isClosed()) {
                instance.connect();                  // <<< quan trọng: tự mở nếu chưa mở
            }
        } catch (SQLException e) {
            throw new RuntimeException("Không kết nối CSDL: " + e.getMessage(), e);
        }
        return con;
    }
}
