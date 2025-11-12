package dao;

import connectDB.ConnectDB;
import entity.NhanVien;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NhanVienDAO {

    /* Ép mọi kiểu số về Integer để set vào cột INT */
    private static Integer toIntSQL(Object v) {
        if (v == null) return 0;
        if (v instanceof Number) return ((Number) v).intValue();
        // Xóa ký tự (vd: "1.000.000")
        try { return Integer.parseInt(String.valueOf(v).replaceAll("\\D","")); }
        catch (Exception e) { return 0; }
    }

    public List<NhanVien> getAll(){
        List<NhanVien> ds = new ArrayList<>();
        String sql = "SELECT maNV, tenNV, chucVu, luong FROM NhanVien ORDER BY tenNV";
        try (Connection c = ConnectDB.getConnection();
             PreparedStatement p = c.prepareStatement(sql);
             ResultSet rs = p.executeQuery()){
            while (rs.next()){
                NhanVien x = new NhanVien(
                        rs.getString("maNV"),
                        rs.getString("tenNV"),
                        rs.getString("chucVu"),
                        rs.getInt("luong") // Lấy INT từ DB
                );
                ds.add(x);
            }
        } catch (Exception ignored){}
        return ds;
    }

    public NhanVien findById(String ma){
        String sql = "SELECT maNV, tenNV, chucVu, luong FROM NhanVien WHERE maNV=?";
        try (Connection c = ConnectDB.getConnection();
             PreparedStatement p = c.prepareStatement(sql)){
            p.setString(1, ma);
            try(ResultSet rs = p.executeQuery()){
                if (rs.next()){
                    return new NhanVien(
                            rs.getString("maNV"),
                            rs.getString("tenNV"),
                            rs.getString("chucVu"),
                            rs.getInt("luong")
                    );
                }
            }
        } catch (Exception ignored){}
        return null;
    }

    public void insert(NhanVien x) throws Exception {
        String sql = "INSERT INTO NhanVien(maNV, tenNV, chucVu, luong) VALUES(?,?,?,?)";
        try (Connection c = ConnectDB.getConnection();
             PreparedStatement p = c.prepareStatement(sql)){
            p.setString(1, x.getMaNV());
            p.setString(2, x.getTenNV());
            p.setString(3, x.getChucVu());
            // Dùng toIntSQL để chuyển double (từ NhanVien.java) sang INT
            p.setObject(4, toIntSQL(x.getLuong()), Types.INTEGER);
            p.executeUpdate();
        }
    }

    public void update(NhanVien x) throws Exception {
        String sql = "UPDATE NhanVien SET tenNV=?, chucVu=?, luong=? WHERE maNV=?";
        try (Connection c = ConnectDB.getConnection();
             PreparedStatement p = c.prepareStatement(sql)){
            p.setString(1, x.getTenNV());
            p.setString(2, x.getChucVu());
            // Dùng toIntSQL để chuyển double (từ NhanVien.java) sang INT
            p.setObject(3, toIntSQL(x.getLuong()), Types.INTEGER);
            p.setString(4, x.getMaNV());
            p.executeUpdate();
        }
    }

    public void delete(String ma) throws Exception {
        String sql = "DELETE FROM NhanVien WHERE maNV=?";
        try (Connection c = ConnectDB.getConnection();
             PreparedStatement p = c.prepareStatement(sql)){
            p.setString(1, ma);
            p.executeUpdate();
        }
    }
}