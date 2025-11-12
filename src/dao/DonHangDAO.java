package dao;

import connectDB.ConnectDB;
import entity.DonHang;
import entity.NhanVien;

import java.sql.*;
import java.util.Date;

public class DonHangDAO {

    /**
     * Mở đơn cho bàn (nếu đã có OPEN trả về, nếu không tạo mới).
     * maBan example: "BAN05" (đã đồng bộ với ManHinhBanHang)
     */
    public DonHang moDonHang(String maBan, NhanVien nv){
        String find = "SELECT maDH, maBan, maNV, trangThai, ngayTao, ngayDong FROM DonHang WHERE maBan=? AND trangThai='OPEN'";
        try (Connection c = ConnectDB.getConnection();
             PreparedStatement p = c.prepareStatement(find)){
            p.setString(1, maBan);
            try (ResultSet rs = p.executeQuery()){
                if (rs.next()){
                    DonHang d = new DonHang();
                    d.setMaDH(rs.getString("maDH"));
                    d.setMaBan(rs.getString("maBan"));
                    d.setMaNV(rs.getString("maNV"));
                    d.setTrangThai(rs.getString("trangThai"));
                    d.setNgayTao(rs.getTimestamp("ngayTao"));
                    d.setNgayDong(rs.getTimestamp("ngayDong"));
                    return d;
                }
            }

            // tạo mới
            String newMa = "DH" + System.currentTimeMillis(); // đơn giản, unique
            String insert = "INSERT INTO DonHang(maDH, maBan, maNV, trangThai, ngayTao) VALUES(?,?,?,?,GETDATE())";
            try (PreparedStatement pi = c.prepareStatement(insert)) {
                pi.setString(1, newMa);
                pi.setString(2, maBan);
                pi.setString(3, nv==null? null : nv.getMaNV());
                pi.setString(4, "OPEN");
                pi.executeUpdate();
            }
            DonHang d = new DonHang();
            d.setMaDH(newMa);
            d.setMaBan(maBan);
            d.setMaNV(nv==null? null : nv.getMaNV());
            d.setTrangThai("OPEN");
            d.setNgayTao(new Timestamp(new Date().getTime()));
            return d;
        } catch (Exception e){ e.printStackTrace(); return null; }
    }

    public void dongDonHang(DonHang d){
        String sql = "UPDATE DonHang SET trangThai='PAID', ngayDong=GETDATE() WHERE maDH=?";
        try (Connection c = ConnectDB.getConnection();
             PreparedStatement p = c.prepareStatement(sql)){
            p.setString(1, d.getMaDH());
            p.executeUpdate();
        } catch (Exception e){ e.printStackTrace(); }
    }

    public String getTenNhanVienByMaDH(String maDH){
        String sql = "SELECT nv.tenNV FROM DonHang dh LEFT JOIN NhanVien nv ON dh.maNV = nv.maNV WHERE dh.maDH=?";
        try (Connection c = ConnectDB.getConnection();
             PreparedStatement p = c.prepareStatement(sql)){
            p.setString(1, maDH);
            try (ResultSet rs = p.executeQuery()){
                if (rs.next()) return rs.getString(1);
            }
        } catch (Exception e){ e.printStackTrace(); }
        return null;
    }

    public String getMaBanByMaDH(String maDH){
        String sql = "SELECT maBan FROM DonHang WHERE maDH=?";
        try (Connection c = ConnectDB.getConnection();
             PreparedStatement p = c.prepareStatement(sql)){
            p.setString(1, maDH);
            try (ResultSet rs = p.executeQuery()){
                if (rs.next()) return rs.getString(1);
            }
        } catch (Exception e){ e.printStackTrace(); }
        return null;
    }
}