package views;

import dao.ChiTietDonHangDAO;
import dao.DonHangDAO;
import dao.HoaDonDAO;
import entity.ChiTietDonHang;
import entity.HoaDon;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class PrintHoaDonDiaLog extends JDialog {

    private final String maDH;
    private BigDecimal khachDuaOpt; // có thể null → lấy từ HoaDon

    private final JTable table = new JTable(new DefaultTableModel(
            new String[]{"TT","Tên món","SL","Đ.Giá","T.Tiền"}, 0){
        @Override public boolean isCellEditable(int r,int c){ return false; }
        @Override public Class<?> getColumnClass(int c){
            return switch (c){ case 0,2 -> Integer.class; case 3,4 -> String.class; default -> String.class; };
        }
    });

    private final JLabel lbSoBill = new JLabel();
    private final JLabel lbThuNgan = new JLabel();
    private final JLabel lbSoBan = new JLabel();
    private final JLabel lbTongSL = new JLabel("0");
    private final JLabel lbThanhTien = new JLabel("0 đ");
    private final JLabel lbThanhToan = new JLabel("0 đ");
    private final JLabel lbKhachDua = new JLabel("0 đ");
    private final JLabel lbTienThua = new JLabel("0 đ");

    private final NumberFormat VN = NumberFormat.getNumberInstance(new Locale("vi","VN"));

    private final DonHangDAO dhDAO = new DonHangDAO();
    private final ChiTietDonHangDAO ctDAO = new ChiTietDonHangDAO();
    private final HoaDonDAO hdDAO = new HoaDonDAO();

    public static void open(Component parent, String maDH){
        PrintHoaDonDiaLog d = new PrintHoaDonDiaLog(SwingUtilities.getWindowAncestor(parent), maDH, null);
        d.setVisible(true);
    }
    public static void open(Component parent, String maDH, BigDecimal khachDua){
        PrintHoaDonDiaLog d = new PrintHoaDonDiaLog(SwingUtilities.getWindowAncestor(parent), maDH, khachDua);
        d.setVisible(true);
    }

    public PrintHoaDonDiaLog(Window owner, String maDH, BigDecimal khachDua){
        super(owner, "Hóa đơn", ModalityType.APPLICATION_MODAL);
        this.maDH = maDH;
        this.khachDuaOpt = khachDua;
        setSize(640, 560);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        add(buildHeader(), BorderLayout.NORTH);
        add(buildTable(), BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);
        loadData();
    }

    private JComponent buildHeader(){
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(new EmptyBorder(10,10,4,10));

        JLabel title = new JLabel("ZENTA COFFEE", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 22f));
        p.add(title, BorderLayout.NORTH);

        JPanel info = new JPanel(new GridLayout(2,2,10,2));
        info.add(new JLabel("195/10 Nguyễn Văn Bảo, Phường 4, Gò Vấp, TP.HCM"));
        info.add(new JLabel()); // spacer
        lbSoBill.setText("Số Bill: …");
        info.add(lbSoBill);
        p.add(info, BorderLayout.CENTER);

        JPanel line2 = new JPanel(new GridLayout(1,4,10,2));
        String now = new SimpleDateFormat("dd.MM.yyyy HH.mm").format(new java.util.Date());
        line2.add(new JLabel("Thời gian: " + now));
        lbThuNgan.setText("Thu ngân: ..");
        line2.add(lbThuNgan);
        lbSoBan.setText("Số bàn: ..");
        line2.add(lbSoBan);
        line2.add(new JLabel()); // spacer
        p.add(line2, BorderLayout.SOUTH);

        return p;
    }

    private JComponent buildTable(){
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBorder(new EmptyBorder(8,10,8,10));
        table.setRowHeight(24);
        wrap.add(new JScrollPane(table), BorderLayout.CENTER);
        return wrap;
    }

    private JComponent buildFooter(){
        JPanel foot = new JPanel();
        foot.setLayout(new BoxLayout(foot, BoxLayout.Y_AXIS));
        foot.setBorder(new EmptyBorder(8,10,10,10));

        JPanel totals = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(2,2,2,2);
        c.gridx=0; c.gridy=0; totals.add(new JLabel("Tổng số lượng:"), c);
        c.gridx=1; c.gridy=0; totals.add(lbTongSL, c);
        c.gridx=2; c.gridy=0; totals.add(new JLabel("Thành tiền:"), c);
        c.gridx=3; c.gridy=0; totals.add(lbThanhTien, c);
        c.gridx=2; c.gridy=1; totals.add(new JLabel("Thanh toán:"), c);
        c.gridx=3; c.gridy=1; totals.add(lbThanhToan, c);
        c.gridx=2; c.gridy=2; totals.add(new JLabel("Tiền khách đưa:"), c);
        c.gridx=3; c.gridy=2; totals.add(lbKhachDua, c);
        c.gridx=2; c.gridy=3; totals.add(new JLabel("Tiền thừa:"), c);
        c.gridx=3; c.gridy=3; totals.add(lbTienThua, c);

        foot.add(totals);
        foot.add(Box.createVerticalStrut(8));
        JTextArea note = new JTextArea(
                "Giá sản phẩm đã bao gồm VAT.\n" +
                "Nếu cần xuất hóa đơn GTGT, vui lòng liên hệ quầy thu ngân.\n" +
                "Password Wifi: ZentaCoffee");
        note.setEditable(false);
        note.setBackground(getBackground());
        foot.add(note);
        return foot;
    }

    private void loadData(){
        // Header NV & Bàn
        String tenNV = String.valueOf(dhDAO.getTenNhanVienByMaDH(maDH));
        String soBan = String.valueOf(dhDAO.getMaBanByMaDH(maDH));
        lbThuNgan.setText("Thu ngân: " + (tenNV==null?"..":tenNV));
        lbSoBan.setText("Số bàn: " + (soBan==null?"..":soBan));

        // Lấy hóa đơn mới nhất của đơn hàng
        HoaDon h = new HoaDonDAO().getByMaDH(maDH);
        if (h != null) {
            lbSoBill.setText(String.format("Số Bill: HD%06d", h.getMaHD()));
        }

     // Chi tiết món
        DefaultTableModel m = (DefaultTableModel) table.getModel();
        m.setRowCount(0);
        List<ChiTietDonHang> ds = new ChiTietDonHangDAO().getByMaDH(maDH);
        int tt = 1, tongSL = 0;
        BigDecimal thanhTien = BigDecimal.ZERO;
        for (ChiTietDonHang x : ds){
            BigDecimal row = x.getDonGia().multiply(BigDecimal.valueOf(x.getSoLuong()));
            m.addRow(new Object[]{
                    tt++,
                    (x.getTenSP() != null && !x.getTenSP().isEmpty()) ? x.getTenSP() : x.getMaSP(),
                    x.getSoLuong(),
                    VN.format(x.getDonGia()),
                    VN.format(row)
            });
            tongSL += x.getSoLuong();
            thanhTien = thanhTien.add(row);
        }

        // Cập nhật tổng số lượng và thành tiền
        lbTongSL.setText(String.valueOf(tongSL));
        lbThanhTien.setText(VN.format(thanhTien) + " đ");

        // Khách đưa + tiền thừa
        BigDecimal khachDua = khachDuaOpt;
        BigDecimal tienThua = BigDecimal.ZERO;
        if (khachDua == null && h != null) khachDua = h.getTienKhachDua();
        if (khachDua != null){
            tienThua = khachDua.subtract(thanhTien);
        }
        lbThanhToan.setText(VN.format(thanhTien) + " đ");
        lbKhachDua.setText(khachDua==null? "0 đ" : VN.format(khachDua) + " đ");
        lbTienThua.setText(VN.format(tienThua.max(BigDecimal.ZERO)) + " đ");

    }
}