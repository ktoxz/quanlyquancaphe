package entity;

import java.math.BigDecimal;

public class SanPham {
    private String maSP;
    private String tenSP;
    private BigDecimal donGia;
    private String maDM;

    // Ảnh cũ (đường dẫn)
    private String anh;

    // Ảnh BLOB + metadata
    private byte[] anhBytes;
    private String anhFileName;
    private String anhContentType;

    public SanPham() {}

    public String getMaSP() { return maSP; }
    public void setMaSP(String maSP) { this.maSP = maSP; }

    public String getTenSP() { return tenSP; }
    public void setTenSP(String tenSP) { this.tenSP = tenSP; }

    public BigDecimal getDonGia() { return donGia; }
    public void setDonGia(BigDecimal donGia) { this.donGia = donGia; }

    public String getMaDM() { return maDM; }
    public void setMaDM(String maDM) { this.maDM = maDM; }

    public String getAnh() { return anh; }
    public void setAnh(String anh) { this.anh = anh; }

    public byte[] getAnhBytes() { return anhBytes; }
    public void setAnhBytes(byte[] anhBytes) { this.anhBytes = anhBytes; }
    public String getAnhFileName() { return anhFileName; }
    public void setAnhFileName(String anhFileName) { this.anhFileName = anhFileName; }
    public String getAnhContentType() { return anhContentType; }
    public void setAnhContentType(String anhContentType) { this.anhContentType = anhContentType; }
}