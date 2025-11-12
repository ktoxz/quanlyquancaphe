package dao;

import connectDB.ConnectDB;
import entity.ChiTietDonHang;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ChiTietDonHangDAO {

    /** Thêm mới nếu chưa có; nếu đã có thì tăng số lượng */
    public void insertOrIncrease(ChiTietDonHang x){
        String sqlCheck = "SELECT soLuong FROM ChiTietDonHang WHERE maDH=? AND maSP=?";
        String sqlIns   = "INSERT INTO ChiTietDonHang(maDH, maSP, soLuong, donGia) VALUES(?,?,?,?)";
        String sqlUpd   = "UPDATE ChiTietDonHang SET soLuong = soLuong + ?, donGia = ? WHERE maDH=? AND maSP=?";
        try (Connection c = ConnectDB.getConnection()) {
            int so = 0;
            try (PreparedStatement p = c.prepareStatement(sqlCheck)) {
                p.setString(1, x.getMaDH());
                p.setString(2, x.getMaSP());
                try (ResultSet rs = p.executeQuery()) {
                    if (rs.next()) so = rs.getInt(1);
                }
            }
            if (so > 0) {
                try (PreparedStatement p = c.prepareStatement(sqlUpd)) {
                    p.setInt(1, x.getSoLuong());
                    p.setBigDecimal(2, x.getDonGia());
                    p.setString(3, x.getMaDH());
                    p.setString(4, x.getMaSP());
                    p.executeUpdate();
                }
            } else {
                try (PreparedStatement p = c.prepareStatement(sqlIns)) {
                    p.setString(1, x.getMaDH());
                    p.setString(2, x.getMaSP());
                    p.setInt   (3, x.getSoLuong());
                    p.setBigDecimal(4, x.getDonGia());
                    p.executeUpdate();
                }
            }
        } catch (Exception ignored) {}
    }

    /** Xóa hẳn 1 mặt hàng khỏi đơn (dùng cho nút Xóa dòng) */
    public void deleteItem(String maDH, String maSP){
        String sql = "DELETE FROM ChiTietDonHang WHERE maDH=? AND maSP=?";
        try (Connection c = ConnectDB.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, maDH);
            p.setString(2, maSP);
            p.executeUpdate();
        } catch (Exception ignored) {}
    }

    /** Đếm số món trong đơn → quyết định điều hướng */
    public int demMon(String maDH){
        String sql = "SELECT COUNT(*) FROM ChiTietDonHang WHERE maDH=?";
        try (Connection c = ConnectDB.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, maDH);
            try (ResultSet rs = p.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (Exception ignored) {}
        return 0;
    }

    /** Lấy chi tiết đơn kèm tên sản phẩm để hiển thị/in */
    public List<ChiTietDonHang> getByMaDH(String maDH){
        String sql = "SELECT ct.maDH, ct.maSP, sp.tenSP, ct.soLuong, ct.donGia " +
                     "FROM ChiTietDonHang ct " +
                     "JOIN SanPham sp ON sp.maSP = ct.maSP " +
                     "WHERE ct.maDH=? " +
                     "ORDER BY sp.tenSP";
        List<ChiTietDonHang> ds = new ArrayList<>();
        try (Connection c = ConnectDB.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, maDH);
            try (ResultSet rs = p.executeQuery()) {
                while (rs.next()) {
                    ChiTietDonHang x = new ChiTietDonHang();
                    x.setMaDH(rs.getString("maDH"));
                    x.setMaSP(rs.getString("maSP"));
                    x.setTenSP(rs.getString("tenSP"));
                    x.setSoLuong(rs.getInt("soLuong"));
                    x.setDonGia(rs.getBigDecimal("donGia"));
                    ds.add(x);
                }
            }
        } catch (Exception ignored) {}
        return ds;
    }
}