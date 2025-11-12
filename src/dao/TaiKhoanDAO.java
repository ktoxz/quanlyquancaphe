package dao;

import connectDB.ConnectDB;
import entity.NhanVien;

import java.sql.*;

public class TaiKhoanDAO {

    /**
     * Đăng nhập theo schema hiện tại:
     * TaiKhoan(username, mk, maNV, vaiTro, trangThai)
     * JOIN NhanVien(maNV, tenNV, chucVu) bằng maNV.
     */
    public NhanVien login(String user, String pass) {
        if (user == null) user = "";
        if (pass == null) pass = "";
        user = user.trim();
        pass = pass.trim();

        // Ưu tiên schema có cột mk + username + maNV (đúng với DB của bạn)
        String sqlMkUser =
            "SELECT nv.maNV, nv.tenNV, " +
            "       COALESCE(nv.chucVu, tk.vaiTro, 'NHANVIEN') AS chucVu " +
            "FROM TaiKhoan tk " +
            "LEFT JOIN NhanVien nv ON nv.maNV = tk.maNV " +
            "WHERE tk.username = ? " +
            "  AND (tk.mk = ? OR LTRIM(RTRIM(tk.mk)) = ?) " +
            "  AND (tk.trangThai IS NULL OR tk.trangThai = 'ACTIVE')";

        // Dự phòng nếu ai đó đặt tên cột khác
        String sqlMatKhauUser =
            "SELECT nv.maNV, nv.tenNV, COALESCE(nv.chucVu, tk.vaiTro, 'NHANVIEN') AS chucVu " +
            "FROM TaiKhoan tk " +
            "LEFT JOIN NhanVien nv ON nv.maNV = tk.maNV " +
            "WHERE tk.username = ? " +
            "  AND (tk.matKhau = ? OR LTRIM(RTRIM(tk.matKhau)) = ?) " +
            "  AND (tk.trangThai IS NULL OR tk.trangThai = 'ACTIVE')";

        String sqlMkTaiKhoan =
            "SELECT nv.maNV, nv.tenNV, COALESCE(nv.chucVu, tk.vaiTro, 'NHANVIEN') AS chucVu " +
            "FROM TaiKhoan tk " +
            "LEFT JOIN NhanVien nv ON nv.maNV = tk.maNV " +
            "WHERE tk.taiKhoan = ? " +
            "  AND (tk.mk = ? OR LTRIM(RTRIM(tk.mk)) = ?) " +
            "  AND (tk.trangThai IS NULL OR tk.trangThai = 'ACTIVE')";

        NhanVien nv = tryLogin(sqlMkUser, user, pass);
        if (nv == null) nv = tryLogin(sqlMatKhauUser, user, pass);
        if (nv == null) nv = tryLogin(sqlMkTaiKhoan, user, pass);
        return nv;
    }

    private NhanVien tryLogin(String sql, String u, String p) {
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, u);
            ps.setString(2, p);
            ps.setString(3, p);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    NhanVien nv = new NhanVien();
                    nv.setMaNV(rs.getString("maNV"));
                    nv.setTenNV(rs.getString("tenNV"));
                    nv.setChucVu(rs.getString("chucVu"));
                    return nv;
                }
            }
        } catch (SQLException ignore) {}
        return null;
    }
}