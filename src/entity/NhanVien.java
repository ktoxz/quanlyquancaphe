package entity;

public class NhanVien {
    private String maNV, tenNV, chucVu; 
    private double luong;
    
    public NhanVien(){}
    
    public NhanVien(String maNV,String tenNV,String chucVu,double luong){
        this.maNV=maNV;
        this.tenNV=tenNV;
        this.chucVu=chucVu;
        this.luong=luong;
    }
    
    public String getMaNV(){return maNV;} 
    public void setMaNV(String s){maNV=s;}
    
    public String getTenNV(){return tenNV;} 
    public void setTenNV(String s){tenNV=s;}
    
    public String getChucVu(){return chucVu;} 
    public void setChucVu(String s){chucVu=s;}
    
    public double getLuong(){return luong;} 
    public void setLuong(double v){luong=v;}
}