package views;

import dao.NhanVienDAO;
import entity.NhanVien;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ManHinhNhanVien extends JPanel {

    private final NhanVienDAO dao = new NhanVienDAO();

    private final DefaultTableModel model =
            new DefaultTableModel(new String[]{"Mã NV","Họ tên","Chức vụ","Lương"}, 0){
                @Override public boolean isCellEditable(int r,int c){ return false; }
                @Override public Class<?> getColumnClass(int c){ return c==3 ? Integer.class : Object.class; }
            };
    private final JTable tbl = new JTable(model);

    private final JTextField txtTim   = new JTextField(18);
    private final JTextField txtMa    = new JTextField();
    private final JTextField txtTen   = new JTextField();
    
    // === (ĐÃ SỬA) Thay JTextField thành JComboBox ===
    private final String[] CHUC_VU_LIST = {"Quản Lý", "Thu Ngân", "Pha Chế", "Phục Vụ"};
    private final JComboBox<String> cboChucVu = new JComboBox<>(CHUC_VU_LIST);
    // === (HẾT SỬA) ===
    
    private final JTextField txtLuong = new JTextField();

    private final NumberFormat VNF = NumberFormat.getNumberInstance(new Locale("vi","VN"));

    public ManHinhNhanVien(){
        setLayout(new BorderLayout(8,8));
        setBorder(new EmptyBorder(10,10,10,10));

        add(buildTop(), BorderLayout.NORTH);
        add(new JScrollPane(tbl), BorderLayout.CENTER);
        add(buildRight(), BorderLayout.EAST);

        tbl.setRowHeight(24);
        // Renderer cột Lương
        DefaultTableCellRenderer money = new DefaultTableCellRenderer(){
            @Override protected void setValue(Object value){
                if (value instanceof Number) setText(VNF.format(((Number)value).longValue()));
                else super.setValue(value);
            }
        };
        tbl.getColumnModel().getColumn(3).setCellRenderer(money);

        tbl.getSelectionModel().addListSelectionListener(e -> pickRow());
        taiLaiBang();
    }

    /* ===== Thanh tìm kiếm (Giữ nguyên) ===== */
    private JComponent buildTop(){
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        styleField(txtTim);
        JButton btnTim = solid(" Tìm", new Color(111,78,55), new Color(96,67,47), Icons.search());
        JButton btnLM  = solid(" Làm mới", new Color(111,78,55), new Color(96,67,47), Icons.refresh());

        p.add(new JLabel("Tìm:"));
        p.add(txtTim);
        p.add(btnTim);
        p.add(btnLM);

        btnTim.addActionListener(e -> tim());
        txtTim.addActionListener(e -> tim());
        btnLM.addActionListener(e -> { txtTim.setText(""); taiLaiBang(); });
        return p;
    }

    /* ===== Form bên phải (ĐÃ SỬA) ===== */
    private JComponent buildRight(){
        JPanel r = new JPanel(new GridBagLayout());
        r.setOpaque(false);
        r.setBorder(new EmptyBorder(0,12,0,0));
        r.setPreferredSize(new Dimension(360, 0));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6,6,6,6);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx=0; c.gridy=0; c.weightx=1;

        // === (ĐÃ SỬA) Bỏ styleField(txtChuc) ===
        styleField(txtMa); styleField(txtTen); styleField(txtLuong);
        Dimension fieldSize = new Dimension(300, 30);
        txtMa.setPreferredSize(fieldSize);
        txtTen.setPreferredSize(fieldSize);
        // === (ĐÃ SỬA) Dùng ComboBox ===
        cboChucVu.setPreferredSize(fieldSize); 
        txtLuong.setPreferredSize(fieldSize);

        // Định dạng lương
        txtLuong.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusLost(java.awt.event.FocusEvent e) {
                int v = parseLuong(txtLuong.getText().trim());
                txtLuong.setText(v>0 ? VNF.format(v) : "");
            }
        });

        r.add(new JLabel("Mã NV:"), c);   c.gridy++; r.add(txtMa, c);
        c.gridy++; r.add(new JLabel("Họ tên:"), c); c.gridy++; r.add(txtTen, c);
        // === (ĐÃ SỬA) Dùng ComboBox ===
        c.gridy++; r.add(new JLabel("Chức vụ:"), c); c.gridy++; r.add(cboChucVu, c);
        c.gridy++; r.add(new JLabel("Lương:"), c);   c.gridy++; r.add(txtLuong, c);

        // Nhóm nút (Giữ nguyên)
        JPanel act = new JPanel(new GridLayout(2,2,10,10));
        JButton btnAdd = solid(" Thêm",   new Color(67,160,71),  new Color(56,142,60),  Icons.plus());
        JButton btnEdit= solid(" Sửa",    new Color(33,150,243), new Color(25,118,210), Icons.edit());
        JButton btnDel = solid(" Xóa",    new Color(192,57,43),  new Color(172,47,37),  Icons.remove());
        JButton btnClr = solid(" Làm mới",new Color(120,144,156),new Color(96,125,139), Icons.refresh());
        act.add(btnAdd); act.add(btnEdit); act.add(btnDel); act.add(btnClr);

        btnAdd.addActionListener(e -> them());
        btnEdit.addActionListener(e -> sua());
        btnDel.addActionListener(e -> xoa());
        btnClr.addActionListener(e -> lamMoi());

        c.gridy++; r.add(act, c);
        return r;
    }

    /* ===== Actions (ĐÃ SỬA) ===== */
    private void pickRow(){
        int i = tbl.getSelectedRow();
        if (i>=0){
            txtMa.setText(String.valueOf(model.getValueAt(i,0)));
            txtTen.setText(String.valueOf(model.getValueAt(i,1)));
            
            // === (ĐÃ SỬA) Dùng ComboBox ===
            cboChucVu.setSelectedItem(String.valueOf(model.getValueAt(i,2)));
            
            int v = (model.getValueAt(i,3) instanceof Number)
                    ? ((Number)model.getValueAt(i,3)).intValue() : parseLuong(String.valueOf(model.getValueAt(i,3)));
            txtLuong.setText(v>0 ? VNF.format(v) : "");
        }
    }

    private void tim(){
        // (Giữ nguyên)
        String kw = txtTim.getText().trim().toLowerCase();
        model.setRowCount(0);
        for (NhanVien x : dao.getAll()){
            String luongStr = String.valueOf(x.getLuong());
            if (kw.isEmpty()
                    || x.getMaNV().toLowerCase().contains(kw)
                    || x.getTenNV().toLowerCase().contains(kw)
                    || luongStr.contains(kw)
                    || (x.getChucVu()!=null && x.getChucVu().toLowerCase().contains(kw))){
                model.addRow(new Object[]{x.getMaNV(),x.getTenNV(),x.getChucVu(),x.getLuong()});
            }
        }
    }

    private void them(){
        String ma = txtMa.getText().trim();
        String ten = txtTen.getText().trim();
        // === (ĐÃ SỬA) Dùng ComboBox ===
        String ch = String.valueOf(cboChucVu.getSelectedItem());
        
        if (ma.isEmpty() || ten.isEmpty()){
            JOptionPane.showMessageDialog(this,"Nhập mã & tên!"); return;
        }
        int luong = parseLuong(txtLuong.getText().trim());
        try {
            dao.insert(new NhanVien(ma, ten, ch, luong));
            taiLaiBang(); lamMoi();
        } catch (Exception ex){
            JOptionPane.showMessageDialog(this,"Thêm thất bại: "+ex.getMessage());
        }
    }

    private void sua(){
        String ma = txtMa.getText().trim();
        if (ma.isEmpty()){ JOptionPane.showMessageDialog(this,"Chọn dòng để sửa!"); return; }
        String ten = txtTen.getText().trim();
        // === (ĐÃ SỬA) Dùng ComboBox ===
        String ch = String.valueOf(cboChucVu.getSelectedItem());
        int luong = parseLuong(txtLuong.getText().trim());
        try {
            dao.update(new NhanVien(ma, ten, ch, luong));
            taiLaiBang(); lamMoi();
        } catch (Exception ex){
            JOptionPane.showMessageDialog(this,"Sửa thất bại: "+ex.getMessage());
        }
    }

    private void xoa(){
        // (Giữ nguyên)
        String ma = txtMa.getText().trim();
        if (ma.isEmpty()){ JOptionPane.showMessageDialog(this,"Chọn dòng để xóa!"); return; }
        if (JOptionPane.showConfirmDialog(this,"Xóa nhân viên "+ma+"?","Xác nhận",
                JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION){
            try { dao.delete(ma); taiLaiBang(); lamMoi(); }
            catch(Exception ex){ JOptionPane.showMessageDialog(this,"Không xóa được: "+ex.getMessage()); }
        }
    }

    private void lamMoi(){
        txtMa.setText(""); txtTen.setText("");
        // === (ĐÃ SỬA) Dùng ComboBox ===
        cboChucVu.setSelectedIndex(0); 
        txtLuong.setText("");
        tbl.clearSelection(); txtMa.requestFocus();
    }

    private void taiLaiBang(){
        // (Giữ nguyên)
        model.setRowCount(0);
        for (NhanVien x : dao.getAll()){
            model.addRow(new Object[]{x.getMaNV(),x.getTenNV(),x.getChucVu(),x.getLuong()});
        }
    }

    /* ===== helpers (Giữ nguyên) ===== */
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
    private static int parseLuong(String s){
        if (s==null) return 0;
        String digits = s.replaceAll("\\D", "");
        if (digits.isEmpty()) return 0;
        try { return Integer.parseInt(digits); } catch(Exception e){ return 0; }
    }
}