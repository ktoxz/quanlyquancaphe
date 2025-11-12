package entity;

public class TaiKhoan {
    private String username, mk, maNV, vaiTro, trangThai;

    public TaiKhoan() {}
    public TaiKhoan(String username, String mk, String maNV, String vaiTro, String trangThai){
        this.username=username; this.mk=mk; this.maNV=maNV; this.vaiTro=vaiTro; this.trangThai=trangThai;
    }
    public String getUsername(){ return username; }
    public String getMk(){ return mk; }
    public String getMaNV(){ return maNV; }
    public String getVaiTro(){ return vaiTro; }
    public String getTrangThai(){ return trangThai; }
    public void setUsername(String s){ username=s; } public void setMk(String s){ mk=s; }
    public void setMaNV(String s){ maNV=s; } public void setVaiTro(String s){ vaiTro=s; }
    public void setTrangThai(String s){ trangThai=s; }
}