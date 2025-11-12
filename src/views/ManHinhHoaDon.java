package views;

import dao.HoaDonDAO;
import entity.HoaDon;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ManHinhHoaDon extends JPanel {

    private final JTextField txtFrom = new JTextField(10);
    private final JTextField txtTo   = new JTextField(10);
    private final DefaultTableModel model =
            new DefaultTableModel(new String[]{"Mã HĐ","Mã ĐH","Ngày lập","Tổng tiền","Khách đưa","Tiền thối"}, 0){
                @Override public boolean isCellEditable(int r,int c){ return false; }
            };
    private final JTable tbl = new JTable(model);
    private final HoaDonDAO dao = new HoaDonDAO();

    public ManHinhHoaDon(){
        setLayout(new BorderLayout(8,8));
        setBorder(new EmptyBorder(8,8,8,8));
        add(buildFilter(), BorderLayout.NORTH);
        tbl.setRowHeight(24);
        add(new JScrollPane(tbl), BorderLayout.CENTER);
        add(buildActions(), BorderLayout.SOUTH);
        setDefaultDateToday();
        loadData();
    }

    private JComponent buildFilter(){
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8,4));
        styleField(txtFrom); styleField(txtTo);
        JButton btn = solid("Tải", new Color(111,78,55), new Color(96,67,47), Icons.search());
        JButton lm  = solid("Làm mới", new Color(120,144,156), new Color(96,125,139), Icons.refresh());
        p.add(new JLabel("Từ (yyyy-MM-dd):")); p.add(txtFrom);
        p.add(new JLabel("Đến:")); p.add(txtTo);
        btn.addActionListener(e -> loadData());
        lm.addActionListener(e -> { setDefaultDateToday(); loadData(); });
        p.add(btn); p.add(lm);
        return p;
    }

    private JComponent buildActions(){
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8,4));
        JButton xem = solid("Xem hóa đơn", new Color(0,150,136), new Color(0,137,124), Icons.check());
        xem.addActionListener(e -> openHoaDon());
        p.add(xem);
        return p;
    }

    private void setDefaultDateToday(){
        String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        txtFrom.setText(today);
        txtTo.setText(today);
    }

    private void loadData(){
        try {
            Date f = new SimpleDateFormat("yyyy-MM-dd").parse(txtFrom.getText().trim());
            Date t = new SimpleDateFormat("yyyy-MM-dd").parse(txtTo.getText().trim());
            List<HoaDon> ds = dao.getByDateRange(f, t);
            model.setRowCount(0);
            SimpleDateFormat sdfdt = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            for (HoaDon h : ds){
                model.addRow(new Object[]{
                        h.getMaHD(), h.getMaDH(), sdfdt.format(h.getNgayLap()),
                        h.getTongTien(), h.getTienKhachDua(), h.getTienThoi()
                });
            }
        } catch (Exception ex){
            JOptionPane.showMessageDialog(this, "Ngày không hợp lệ (yyyy-MM-dd)");
        }
    }

    private void openHoaDon(){
        int r = tbl.getSelectedRow();
        if (r<0){ JOptionPane.showMessageDialog(this, "Chọn 1 hóa đơn!"); return; }
        String maDH = String.valueOf(model.getValueAt(r,1));
        PrintHoaDonDiaLog.open(this, maDH);
    }

    /* helpers */
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
    private static class SolidButton extends JButton {
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