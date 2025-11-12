package entity;

public class Ban {
    private String maBan;       // Mã bàn, String (vd: "BAN01")
    private String tenBan;      // Tên bàn, ví dụ "Bàn 01"
    private String trangThai;   // TRONG | PHUC_VU | DAT

    public Ban() {}

    public Ban(String maBan, String tenBan, String trangThai){
        this.maBan = maBan;
        this.tenBan = tenBan;
        this.trangThai = trangThai;
    }

    public String getMaBan() { return maBan; }
    public String getTenBan() { return tenBan; }
    public String getTrangThai() { return trangThai; }

    public void setMaBan(String maBan) { this.maBan = maBan; }
    public void setTenBan(String tenBan) { this.tenBan = tenBan; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    @Override
    public String toString() {
        return tenBan; // Dùng để hiển thị tên bàn ở đâu đó nếu cần
    }
}