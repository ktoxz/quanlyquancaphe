package entity;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class HoaDon {

    private int maHD;                // mã hóa đơn (INT trong SQL)
    private String maDH;             // mã đơn hàng (VARCHAR trong SQL)
    private String maNV;             // mã nhân viên
    private Timestamp ngayLap;       // ngày lập (DATETIME / TIMESTAMP trong SQL)
    private BigDecimal tongTien;     // tổng tiền
    private BigDecimal tienKhachDua; // tiền khách đưa
    private BigDecimal tienThoi;     // tiền thối lại

    // ======= Getter / Setter =======

    public int getMaHD() {
        return maHD;
    }

    public void setMaHD(int maHD) {
        this.maHD = maHD;
    }

    public String getMaDH() {
        return maDH;
    }

    public void setMaDH(String maDH) {
        this.maDH = maDH;
    }

    public String getMaNV() {
        return maNV;
    }

    public void setMaNV(String maNV) {
        this.maNV = maNV;
    }

    public Timestamp getNgayLap() {
        return ngayLap;
    }

    public void setNgayLap(Timestamp ngayLap) {
        this.ngayLap = ngayLap;
    }

    public BigDecimal getTongTien() {
        return tongTien;
    }

    public void setTongTien(BigDecimal tongTien) {
        this.tongTien = tongTien;
    }

    public BigDecimal getTienKhachDua() {
        return tienKhachDua;
    }

    public void setTienKhachDua(BigDecimal tienKhachDua) {
        this.tienKhachDua = tienKhachDua;
    }

    public BigDecimal getTienThoi() {
        return tienThoi;
    }

    public void setTienThoi(BigDecimal tienThoi) {
        this.tienThoi = tienThoi;
    }

    @Override
    public String toString() {
        return "HoaDon{" +
                "maHD=" + maHD +
                ", maDH='" + maDH + '\'' +
                ", maNV='" + maNV + '\'' +
                ", ngayLap=" + ngayLap +
                ", tongTien=" + tongTien +
                ", tienKhachDua=" + tienKhachDua +
                ", tienThoi=" + tienThoi +
                '}';
    }
}