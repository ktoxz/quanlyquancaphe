package entity;

import java.util.Date;

public class ThongKeNgay {
    private Date ngay; private int doanhThu;
    public ThongKeNgay(Date ngay,int doanhThu){ this.ngay=ngay; this.doanhThu=doanhThu; }
    public Date getNgay(){ return ngay; } public int getDoanhThu(){ return doanhThu; }
    public Object[] toRow(){ return new Object[]{ ngay, doanhThu }; }
}