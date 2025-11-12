package entity;

import java.math.BigDecimal;
import java.util.Objects;

public class ChiTietDonHang {
    private String maDH;
    private String maSP;
    private int soLuong;
    private BigDecimal donGia;   // đơn giá 1 món
    private String tenSP;        // <-- thêm để in hóa đơn đẹp

    public ChiTietDonHang() {}

    public ChiTietDonHang(String maDH, String maSP, int soLuong, BigDecimal donGia) {
        this.maDH = maDH;
        this.maSP = maSP;
        this.soLuong = soLuong;
        this.donGia = donGia;
    }

    // Getter/Setter
    public String getMaDH() { return maDH; }
    public void setMaDH(String maDH) { this.maDH = maDH; }

    public String getMaSP() { return maSP; }
    public void setMaSP(String maSP) { this.maSP = maSP; }

    public int getSoLuong() { return soLuong; }
    public void setSoLuong(int soLuong) { this.soLuong = soLuong; }

    public BigDecimal getDonGia() { return donGia; }
    public void setDonGia(BigDecimal donGia) { this.donGia = donGia; }

    public String getTenSP() { return tenSP; }
    public void setTenSP(String tenSP) { this.tenSP = tenSP; }

    // equals/hashCode theo (maDH, maSP)
    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChiTietDonHang)) return false;
        ChiTietDonHang that = (ChiTietDonHang) o;
        return Objects.equals(maDH, that.maDH) && Objects.equals(maSP, that.maSP);
    }
    @Override public int hashCode() { return Objects.hash(maDH, maSP); }

    @Override public String toString() {
        return "CTDH{maDH='" + maDH + "', maSP='" + maSP + "', sl=" + soLuong + ", gia=" + donGia + "}";
    }
}