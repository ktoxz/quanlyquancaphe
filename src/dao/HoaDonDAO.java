package dao;

import connectDB.ConnectDB;
import entity.HoaDon;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class HoaDonDAO {

    /** * Tạo HĐ từ Đơn hàng.
     * Nhận maDH dạng String (vd: "DH123..."), trả về mã HĐ (int identity).
     */
    public int taoHoaDonTuDonHang(String maDH, BigDecimal tong, BigDecimal khachDua) {
        String sql = "INSERT INTO HoaDon(maDH, ngayLap, tongTien, tienKhachDua, tienThoi) " +
                     "VALUES(?, GETDATE(), ?, ?, ?); SELECT SCOPE_IDENTITY();";
        try (Connection c = ConnectDB.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            BigDecimal thoi = khachDua.subtract(tong);
            p.setString(1, maDH);
            p.setBigDecimal(2, tong);
            p.setBigDecimal(3, khachDua);
            p.setBigDecimal(4, thoi);
            try (ResultSet rs = p.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (Exception ignored) {}
        return -1;
    }

    /**
     * Lấy Hóa đơn (đã thanh toán) theo mã Đơn hàng.
     * Dùng cho ManHinhBanHang (để check tiền khách đưa) và PrintHoaDon.
     */
    public HoaDon getByMaDH(String maDH){
        String sql = "SELECT maHD, maDH, ngayLap, tongTien, tienKhachDua, tienThoi " +
                     "FROM HoaDon WHERE maDH=? ORDER BY maHD DESC";
        try (Connection c = ConnectDB.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, maDH);
            try (ResultSet rs = p.executeQuery()) {
                if (rs.next()) {
                    HoaDon h = new HoaDon();
                    h.setMaHD(rs.getInt("maHD"));
                    h.setMaDH(rs.getString("maDH"));
                    h.setNgayLap(rs.getTimestamp("ngayLap"));
                    h.setTongTien(rs.getBigDecimal("tongTien"));
                    h.setTienKhachDua(rs.getBigDecimal("tienKhachDua"));
                    h.setTienThoi(rs.getBigDecimal("tienThoi"));
                    return h;
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    /**
     * Lấy Hóa đơn theo khoảng ngày (cho ManHinhHoaDon)
     */
    public List<HoaDon> getByDateRange(Date from, Date to){
        String sql = "SELECT maHD, maDH, ngayLap, tongTien, tienKhachDua, tienThoi " +
                     "FROM HoaDon WHERE CAST(ngayLap AS date) BETWEEN ? AND ? ORDER BY ngayLap DESC";
        List<HoaDon> ds = new ArrayList<>();
        try (Connection c = ConnectDB.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setDate(1, new java.sql.Date(from.getTime()));
            p.setDate(2, new java.sql.Date(to.getTime()));
            try (ResultSet rs = p.executeQuery()) {
                while (rs.next()) {
                    HoaDon h = new HoaDon();
                    h.setMaHD(rs.getInt("maHD"));
                    h.setMaDH(rs.getString("maDH"));
                    h.setNgayLap(rs.getTimestamp("ngayLap"));
                    h.setTongTien(rs.getBigDecimal("tongTien"));
                    h.setTienKhachDua(rs.getBigDecimal("tienKhachDua"));
                    h.setTienThoi(rs.getBigDecimal("tienThoi"));
                    ds.add(h);
                }
            }
        } catch (Exception ignored) {}
        return ds;
    }

    public BigDecimal tongDoanhThu(Date from, Date to){
        String sql = "SELECT COALESCE(SUM(tongTien),0) AS s " +
                     "FROM HoaDon WHERE CAST(ngayLap AS date) BETWEEN ? AND ?";
        try (Connection c = ConnectDB.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setDate(1, new java.sql.Date(from.getTime()));
            p.setDate(2, new java.sql.Date(to.getTime()));
            try (ResultSet rs = p.executeQuery()) {
                if (rs.next()) return rs.getBigDecimal(1);
            }
        } catch (Exception ignored) {}
        return BigDecimal.ZERO;
    }
}