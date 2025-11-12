package entity;

public class DanhMuc {
    private String maDM, tenDM, moTa;
    
    public DanhMuc() {}
    
    public DanhMuc(String maDM, String tenDM, String moTa){ 
        this.maDM=maDM; 
        this.tenDM=tenDM; 
        this.moTa=moTa; 
    }
    
    public String getMaDM(){return maDM;} 
    public void setMaDM(String s){maDM=s;}
    
    public String getTenDM(){return tenDM;} 
    public void setTenDM(String s){tenDM=s;}
    
    public String getMoTa(){return moTa;} 
    public void setMoTa(String s){moTa=s;}
    
    // Dùng để hiển thị tên trong JComboBox của ManHinhMenuThucUong
    @Override 
    public String toString(){ 
        return tenDM; 
    }
}