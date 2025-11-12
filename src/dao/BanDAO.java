package dao;

import connectDB.ConnectDB;
import entity.Ban;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BanDAO {

    /** Lấy tất cả bàn */
    public List<Ban> getAllBan() {
        List<Ban> list = new ArrayList<>();
        String sql = "SELECT maBan, tenBan, trangThai FROM Ban ORDER BY maBan";
        try (Connection cn = ConnectDB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String maBan = rs.getString("maBan"); // Lấy String
                String tenBan = rs.getString("tenBan");
                String trangThai = rs.getString("trangThai");
                list.add(new Ban(maBan, tenBan, trangThai));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /** Lấy bàn theo mã */
    public Ban getBanTheoMa(String maBan) { // Dùng String
        String sql = "SELECT maBan, tenBan, trangThai FROM Ban WHERE maBan = ?";
        try (Connection cn = ConnectDB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, maBan); // Dùng setString
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String tenBan = rs.getString("tenBan");
                    String trangThai = rs.getString("trangThai");
                    return new Ban(maBan, tenBan, trangThai);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /** Cập nhật trạng thái bàn: "TRONG" | "PHUC_VU" | "DAT" */
    public void capNhatTrangThai(String maBan, String trangThai) { // Dùng String
        String sql = "UPDATE Ban SET trangThai = ? WHERE maBan = ?";
        try (Connection cn = ConnectDB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, trangThai);
            ps.setString(2, maBan); // Dùng setString
            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}