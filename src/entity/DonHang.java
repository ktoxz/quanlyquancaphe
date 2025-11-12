package entity;

import java.util.Date;

public class DonHang {
    private String maDH;
    private String maBan;
    private String maNV;       // MÃ NHÂN VIÊN LẬP ĐƠN
    private String trangThai;  // OPEN | PAID
    private Date ngayTao;
    private Date ngayDong;

    public DonHang() {}

    public DonHang(String maDH, String maBan, String maNV, String trangThai, Date ngayTao, Date ngayDong) {
        this.maDH = maDH;
        this.maBan = maBan;
        this.maNV = maNV;
        this.trangThai = trangThai;
        this.ngayTao = ngayTao;
        this.ngayDong = ngayDong;
    }

    public String getMaDH() { return maDH; }
    public void setMaDH(String maDH) { this.maDH = maDH; }

    public String getMaBan() { return maBan; }
    public void setMaBan(String maBan) { this.maBan = maBan; }

    public String getMaNV() { return maNV; }
    public void setMaNV(String maNV) { this.maNV = maNV; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public Date getNgayTao() { return ngayTao; }
    public void setNgayTao(Date ngayTao) { this.ngayTao = ngayTao; }

    public Date getNgayDong() { return ngayDong; }
    public void setNgayDong(Date ngayDong) { this.ngayDong = ngayDong; }
}