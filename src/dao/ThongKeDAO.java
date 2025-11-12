package dao;

import connectDB.ConnectDB;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Thống kê chi tiết “giống ảnh 2”.
 * Trả về từng dòng: Ngày bán, Mã SP, Tên SP, Số lượng bán, Đơn giá, Thành tiền, VAT 10%, Tổng doanh thu (Thành tiền - VAT)
 */
public class ThongKeDAO {

    public static class Row {
        public Date ngayBan;
        public String maSP, tenSP;
        public int soLuong;
        public BigDecimal donGia, thanhTien, vat, tongDoanhThu;
    }

    public List<Row> baoCaoChiTiet(Date from, Date to){
        String sql =
            "SELECT CAST(hd.ngayLap AS date) AS NgayBan, sp.maSP, sp.tenSP, " +
            "       SUM(ct.soLuong) AS SoLuongBan, sp.donGia AS DonGiaHang, " + // Giả định donGia trong SanPham là giá gốc
            "       SUM(ct.soLuong * ct.donGia) AS ThanhTien " + // Thành tiền là giá bán (ct.donGia)
            "FROM HoaDon hd " +
            "JOIN DonHang dh  ON dh.maDH = hd.maDH " +
            "JOIN ChiTietDonHang ct ON ct.maDH = dh.maDH " +
            "JOIN SanPham sp ON sp.maSP = ct.maSP " +
            "WHERE CAST(hd.ngayLap AS date) BETWEEN ? AND ? " +
            "GROUP BY CAST(hd.ngayLap AS date), sp.maSP, sp.tenSP, sp.donGia " +
            "ORDER BY NgayBan, sp.maSP";
        List<Row> ds = new ArrayList<>();
        try (Connection c = ConnectDB.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setDate(1, new java.sql.Date(from.getTime()));
            p.setDate(2, new java.sql.Date(to.getTime()));
            try (ResultSet rs = p.executeQuery()) {
                while (rs.next()) {
                    Row r = new Row();
                    r.ngayBan = rs.getDate("NgayBan");
                    r.maSP = rs.getString("maSP");
                    r.tenSP = rs.getString("tenSP");
                    r.soLuong = rs.getInt("SoLuongBan");
                    r.donGia = rs.getBigDecimal("DonGiaHang"); // Giá gốc
                    r.thanhTien = rs.getBigDecimal("ThanhTien"); // Giá bán
                    // Tính VAT (10% của Thành tiền)
                    r.vat = r.thanhTien.multiply(new BigDecimal("0.10")).setScale(0, BigDecimal.ROUND_HALF_UP);
                    // Doanh thu = Thành tiền - VAT
                    r.tongDoanhThu = r.thanhTien.subtract(r.vat); 
                    ds.add(r);
                }
            }
        } catch (Exception ignored) {}
        return ds;
    }

    public BigDecimal tongDoanhThu(Date from, Date to){
        String sql = "SELECT COALESCE(SUM(tongTien),0) FROM HoaDon WHERE CAST(ngayLap AS date) BETWEEN ? AND ?";
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