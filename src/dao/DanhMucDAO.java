package dao;

import connectDB.ConnectDB;
import entity.DanhMuc;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DanhMucDAO {

    public List<DanhMuc> getAll(){
        List<DanhMuc> ds = new ArrayList<>();
        String sql = "SELECT maDM, tenDM, moTa FROM DanhMuc ORDER BY tenDM";
        try (Connection c = ConnectDB.getConnection();
             PreparedStatement p = c.prepareStatement(sql);
             ResultSet rs = p.executeQuery()){
            while (rs.next()){
                ds.add(new DanhMuc(rs.getString("maDM"), rs.getString("tenDM"), rs.getString("moTa")));
            }
        } catch (Exception e){ e.printStackTrace(); }
        return ds;
    }

    public void insert(DanhMuc d) throws Exception {
        String sql = "INSERT INTO DanhMuc(maDM, tenDM, moTa) VALUES(?,?,?)";
        try (Connection c = ConnectDB.getConnection();
             PreparedStatement p = c.prepareStatement(sql)){
            p.setString(1, d.getMaDM());
            p.setString(2, d.getTenDM());
            p.setString(3, d.getMoTa());
            p.executeUpdate();
        }
    }

    public void update(DanhMuc d) throws Exception {
        String sql = "UPDATE DanhMuc SET tenDM=?, moTa=? WHERE maDM=?";
        try (Connection c = ConnectDB.getConnection();
             PreparedStatement p = c.prepareStatement(sql)){
            p.setString(1, d.getTenDM());
            p.setString(2, d.getMoTa());
            p.setString(3, d.getMaDM());
            p.executeUpdate();
        }
    }

    public void delete(String ma) throws Exception {
        String sql = "DELETE FROM DanhMuc WHERE maDM=?";
        try (Connection c = ConnectDB.getConnection();
             PreparedStatement p = c.prepareStatement(sql)){
            p.setString(1, ma);
            p.executeUpdate();
        }
    }
}