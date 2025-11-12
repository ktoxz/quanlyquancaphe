package views;

import connectDB.ConnectDB;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.*;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

/** Báo cáo doanh thu: gộp theo Ngày bán + Sản phẩm, tính VAT 10% và doanh thu ròng. */
public class ManHinhThongKe extends JPanel {

    private static final BigDecimal VAT_RATE = new BigDecimal("0.10");
    private static final NumberFormat VNF = NumberFormat.getNumberInstance(new Locale("vi", "VN"));

    private final JTextField txtFrom = new JTextField(10);
    private final JTextField txtTo   = new JTextField(10);

    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"TT","Ngày bán","Mã sản phẩm","Tên sản phẩm","Số lượng bán",
                    "Đơn giá (VND)","Thành tiền (VND)","Thuế VAT (10%)","Tổng doanh thu (VND)"}, 0){
        @Override public boolean isCellEditable(int r, int c){ return false; }
        @Override public Class<?> getColumnClass(int c){
            return switch (c){
                case 0,4 -> Integer.class;
                case 5,6,7,8 -> BigDecimal.class;
                default -> Object.class;
            };
        }
    };
    private final JTable table = new JTable(model);

    // Tổng phía dưới
    private final JLabel lbTongSL   = new JLabel("0");
    private final JLabel lbThanhTien= new JLabel("0");
    private final JLabel lbVAT      = new JLabel("0");
    private final JLabel lbNet      = new JLabel("0");

    public ManHinhThongKe(){
        setLayout(new BorderLayout(8,8));
        setBorder(new EmptyBorder(8,8,8,8));

        add(buildTopBar(), BorderLayout.NORTH);
        add(buildBody(),   BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);

        setDefaultDateToday();
        styleTable();
        loadReport();
    }

    /* ===== UI ===== */

    private JComponent buildTopBar(){
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        styleField(txtFrom); styleField(txtTo);

        JButton btnTim = solid(" Tìm", new Color(0,150,136), new Color(0,137,124), Icons.check());
        JButton btnLM  = solid(" Làm mới", new Color(120,144,156), new Color(96,125,139), Icons.refresh());

        p.add(new JLabel("Từ (yyyy-MM-dd):")); p.add(txtFrom);
        p.add(new JLabel("Đến:"));             p.add(txtTo);
        p.add(btnTim); p.add(btnLM);

        btnTim.addActionListener(e -> loadReport());
        btnLM.addActionListener(e -> { setDefaultDateToday(); clearTable(); });

        return p;
    }

    private JComponent buildBody(){
        JScrollPane sp = new JScrollPane(table);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        return sp;
    }

    private JComponent buildFooter(){
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(new EmptyBorder(8,4,0,4));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4,10,4,10);
        c.gridy = 0;

        JLabel title = new JLabel("TỔNG");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 13f));
        p.add(title, c);

        c.gridx = 1; p.add(new JLabel("Số lượng:"), c);
        c.gridx = 2; lbBold(lbTongSL); p.add(lbTongSL, c);

        c.gridx = 3; p.add(new JLabel("Thành tiền:"), c);
        c.gridx = 4; lbBold(lbThanhTien); p.add(lbThanhTien, c);

        c.gridx = 5; p.add(new JLabel("Thuế VAT (10%):"), c);
        c.gridx = 6; lbBold(lbVAT); p.add(lbVAT, c);

        c.gridx = 7; p.add(new JLabel("Tổng doanh thu:"), c);
        c.gridx = 8; lbBold(lbNet); p.add(lbNet, c);

        return p;
    }

    private static void lbBold(JLabel l){
        l.setFont(l.getFont().deriveFont(Font.BOLD, 13f));
    }

    private void styleTable(){
        table.setRowHeight(24);
        table.setAutoCreateRowSorter(true);

        // canh phải & format tiền
        DefaultTableCellRenderer right = new DefaultTableCellRenderer();
        right.setHorizontalAlignment(SwingConstants.RIGHT);

        for (int c = 4; c <= 8; c++){
            table.getColumnModel().getColumn(c).setCellRenderer(new DefaultTableCellRenderer(){
                @Override protected void setValue(Object v){
                    if (v instanceof Number) setText(VNF.format(((Number)v).longValue()));
                    else if (v instanceof BigDecimal bd) setText(VNF.format(bd.longValue()));
                    else super.setValue(v);
                    setHorizontalAlignment(SwingConstants.RIGHT);
                }
            });
        }

        // Ngày bán canh giữa cho đẹp
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(1).setCellRenderer(center);
    }

    /* ===== Actions ===== */

    private void setDefaultDateToday(){
        String today = new SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date());
        txtFrom.setText(today);
        txtTo.setText(today);
    }

    private void clearTable(){
        model.setRowCount(0);
        lbTongSL.setText("0");
        lbThanhTien.setText("0");
        lbVAT.setText("0");
        lbNet.setText("0");
    }

    private void loadReport(){
        java.util.Date dFrom, dTo;
        try {
            SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
            dFrom = fmt.parse(txtFrom.getText().trim());
            dTo   = fmt.parse(txtTo.getText().trim());
        } catch (Exception ex){
            JOptionPane.showMessageDialog(this, "Ngày không hợp lệ (yyyy-MM-dd)");
            return;
        }

        String sql =
                "SELECT CAST(hd.ngayLap AS date) AS ngayBan, " +
                "       ct.maSP, sp.tenSP, " +
                "       SUM(ct.soLuong) AS soLuong, " +
                "       MAX(ct.donGia)  AS donGia, " +                  // đơn giá tham chiếu
                "       SUM(ct.soLuong * ct.donGia) AS thanhTien " +
                "FROM HoaDon hd " +
                "JOIN DonHang dh       ON dh.maDH = hd.maDH " +
                "JOIN ChiTietDonHang ct ON ct.maDH = dh.maDH " +
                "JOIN SanPham sp       ON sp.maSP = ct.maSP " +
                "WHERE CAST(hd.ngayLap AS date) BETWEEN ? AND ? " +
                "GROUP BY CAST(hd.ngayLap AS date), ct.maSP, sp.tenSP " +
                "ORDER BY ngayBan, ct.maSP";

        clearTable();
        int tt = 1, sumSL = 0;
        BigDecimal sumThanh = BigDecimal.ZERO, sumVAT = BigDecimal.ZERO, sumNet = BigDecimal.ZERO;

        try (Connection c = ConnectDB.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {

            p.setDate(1, new java.sql.Date(dFrom.getTime()));
            p.setDate(2, new java.sql.Date(dTo.getTime()));

            try (ResultSet rs = p.executeQuery()){
                while (rs.next()){
                    java.sql.Date ngay = rs.getDate("ngayBan");
                    String maSP = rs.getString("maSP");
                    String tenSP= rs.getString("tenSP");
                    int soLuong= rs.getInt("soLuong");
                    BigDecimal donGia = BigDecimal.valueOf(rs.getInt("donGia"));
                    BigDecimal thanh  = rs.getBigDecimal("thanhTien");

                    BigDecimal vat = thanh.multiply(VAT_RATE);
                    BigDecimal net = thanh.subtract(vat);

                    model.addRow(new Object[]{
                            tt++,
                            ngay.toString(),
                            maSP, tenSP,
                            soLuong,
                            donGia,
                            thanh,
                            vat,
                            net
                    });

                    sumSL   += soLuong;
                    sumThanh = sumThanh.add(thanh);
                    sumVAT   = sumVAT.add(vat);
                    sumNet   = sumNet.add(net);
                }
            }

        } catch (Exception ex){
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Không tải được báo cáo: " + ex.getMessage());
        }

        // cập nhật dòng tổng
        lbTongSL.setText(VNF.format(sumSL));
        lbThanhTien.setText(VNF.format(sumThanh));
        lbVAT.setText(VNF.format(sumVAT));
        lbNet.setText(VNF.format(sumNet));
    }

    /* ===== helpers UI ===== */
    private static void styleField(JTextField f){
        f.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(210,200,190), 1, true),
                new EmptyBorder(6,10,6,10)
        ));
        f.setFont(f.getFont().deriveFont(13f));
    }

    private static JButton solid(String text, Color base, Color hover, Icon icon){
        return new SolidButton(text, base, hover, icon);
    }

    private static class SolidButton extends JButton{
        private final Color base, hover; private boolean hov=false;
        SolidButton(String text, Color base, Color hover, Icon icon){
            super(text, icon); this.base=base; this.hover=hover;
            setFocusPainted(false); setOpaque(false); setContentAreaFilled(false);
            setBorder(BorderFactory.createEmptyBorder(8,14,8,14));
            setFont(getFont().deriveFont(Font.BOLD, 12f)); setForeground(Color.WHITE);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new java.awt.event.MouseAdapter() {
                @Override public void mouseEntered(java.awt.event.MouseEvent e){ hov=true; repaint(); }
                @Override public void mouseExited (java.awt.event.MouseEvent e){ hov=false; repaint(); }
            });
        }
        @Override protected void paintComponent(Graphics g){
            Graphics2D g2=(Graphics2D)g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(hov?hover:base);
            g2.fillRoundRect(0,0,getWidth(),getHeight(),10,10);
            g2.dispose(); super.paintComponent(g);
        }
    }
}