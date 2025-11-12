package dao;

import connectDB.ConnectDB;
import entity.SanPham;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SanPhamDAO {

    private SanPham readOne(ResultSet rs) throws Exception {
        SanPham sp = new SanPham();
        sp.setMaSP(rs.getString("maSP"));
        sp.setTenSP(rs.getString("tenSP"));
        sp.setDonGia(BigDecimal.valueOf(rs.getInt("donGia")));
        sp.setMaDM(rs.getString("maDM"));

        byte[] blob = null;
        try { blob = rs.getBytes("anh"); } catch (SQLException ignore){}
        String fname = null;
        try { fname = rs.getString("anhFileName"); } catch (SQLException ignore){}
        if (blob != null && blob.length > 0) {
            sp.setAnhBytes(blob);
            sp.setAnhFileName(fname);
        } else {
            // fallback: một số CSDL cũ lưu đường dẫn (path) trong cột 'anh'
            try {
                String maybePath = rs.getString("anh");
                if (maybePath != null && !maybePath.isEmpty()) sp.setAnh(maybePath);
            } catch (SQLException ignore){}
        }
        return sp;
    }

    public List<SanPham> getAll(){
        List<SanPham> ds = new ArrayList<>();
        String sql = "SELECT maSP, tenSP, donGia, maDM, anh, anhFileName, anhContentType FROM SanPham ORDER BY tenSP";
        try (Connection c = ConnectDB.getConnection();
             PreparedStatement p = c.prepareStatement(sql);
             ResultSet rs = p.executeQuery()){
            while (rs.next()) ds.add(readOne(rs));
        } catch (Exception e){ e.printStackTrace(); }
        return ds;
    }

    public List<SanPham> getByDanhMuc(String maDM){
        List<SanPham> ds = new ArrayList<>();
        String sql = "SELECT maSP, tenSP, donGia, maDM, anh, anhFileName, anhContentType FROM SanPham WHERE maDM=? ORDER BY tenSP";
        try (Connection c = ConnectDB.getConnection();
             PreparedStatement p = c.prepareStatement(sql)){
            p.setString(1, maDM);
            try (ResultSet rs = p.executeQuery()){
                while (rs.next()) ds.add(readOne(rs));
            }
        } catch (Exception e){ e.printStackTrace(); }
        return ds;
    }

    public List<SanPham> searchByName(String keyword){
        List<SanPham> ds = new ArrayList<>();
        String sql = "SELECT maSP, tenSP, donGia, maDM, anh, anhFileName, anhContentType FROM SanPham WHERE tenSP LIKE ? OR maSP LIKE ? ORDER BY tenSP";
        try (Connection c = ConnectDB.getConnection();
             PreparedStatement p = c.prepareStatement(sql)){
            String k = "%" + (keyword==null? "" : keyword.trim()) + "%";
            p.setString(1, k); p.setString(2, k);
            try (ResultSet rs = p.executeQuery()){
                while (rs.next()) ds.add(readOne(rs));
            }
        } catch (Exception e){ e.printStackTrace(); }
        return ds;
    }

    public void insert(SanPham sp) throws Exception {
        String sql = "INSERT INTO SanPham(maSP, tenSP, donGia, maDM, anh, anhFileName, anhContentType) VALUES(?,?,?,?,?,?,?)";
        try (Connection c = ConnectDB.getConnection();
             PreparedStatement p = c.prepareStatement(sql)){
            p.setString(1, sp.getMaSP());
            p.setString(2, sp.getTenSP());
            p.setInt(3, sp.getDonGia()==null?0:sp.getDonGia().intValue());
            p.setString(4, sp.getMaDM());

            byte[] bytes = sp.getAnhBytes();
            // Nếu không có bytes, thử đọc từ đường dẫn (getAnh())
            if ((bytes == null || bytes.length == 0) && sp.getAnh() != null) {
                File f = new File(sp.getAnh());
                if (f.exists()) {
                    try (FileInputStream in = new FileInputStream(f)) {
                        bytes = in.readAllBytes();
                    }
                }
            }

            if (bytes != null && bytes.length > 0) p.setBytes(5, bytes);
            else p.setNull(5, Types.VARBINARY);

            if (sp.getAnhFileName() != null) p.setString(6, sp.getAnhFileName()); else p.setNull(6, Types.VARCHAR);
            if (sp.getAnhContentType() != null) p.setString(7, sp.getAnhContentType()); else p.setNull(7, Types.VARCHAR);

            p.executeUpdate();
        }
    }

    public void update(SanPham sp) throws Exception {
        String sql = "UPDATE SanPham SET tenSP=?, donGia=?, maDM=?, anh=?, anhFileName=?, anhContentType=? WHERE maSP=?";
        try (Connection c = ConnectDB.getConnection();
             PreparedStatement p = c.prepareStatement(sql)){
            p.setString(1, sp.getTenSP());
            p.setInt(2, sp.getDonGia()==null?0:sp.getDonGia().intValue());
            p.setString(3, sp.getMaDM());

            byte[] bytes = sp.getAnhBytes();
            if ((bytes == null || bytes.length == 0) && sp.getAnh() != null) {
                File f = new File(sp.getAnh());
                if (f.exists()) try (FileInputStream in = new FileInputStream(f)) { bytes = in.readAllBytes(); }
            }

            if (bytes != null && bytes.length > 0) p.setBytes(4, bytes); else p.setNull(4, Types.VARBINARY);
            if (sp.getAnhFileName() != null) p.setString(5, sp.getAnhFileName()); else p.setNull(5, Types.VARCHAR);
            if (sp.getAnhContentType() != null) p.setString(6, sp.getAnhContentType()); else p.setNull(6, Types.VARCHAR);
            p.setString(7, sp.getMaSP());

            p.executeUpdate();
        }
    }

    public void delete(String maSP) throws Exception {
        String sql = "DELETE FROM SanPham WHERE maSP=?";
        try (Connection c = ConnectDB.getConnection();
             PreparedStatement p = c.prepareStatement(sql)){
            p.setString(1, maSP);
            p.executeUpdate();
        }
    }
}