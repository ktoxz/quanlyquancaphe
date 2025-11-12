package views;

import dao.DanhMucDAO;
import entity.DanhMuc;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ManHinhDanhMuc extends JPanel {

    private final DanhMucDAO dao = new DanhMucDAO();
    
    // === (THÊM MỚI) Callback để thông báo cho GiaoDienChinh ===
    private Runnable onDanhMucChanged;

    private final DefaultTableModel model =
            new DefaultTableModel(new String[]{"Mã","Tên danh mục","Mô tả"}, 0){
                @Override public boolean isCellEditable(int r,int c){ return false; }
            };
    private final JTable tbl = new JTable(model);

    private final JTextField txtTim  = new JTextField(18);
    private final JTextField txtMa   = new JTextField();
    private final JTextField txtTen  = new JTextField();
    private final JTextArea  txtMoTa = new JTextArea(3, 20);

    // === (SỬA) Thêm constructor mới ===
    public ManHinhDanhMuc(){
        this(null); // Gọi constructor mặc định
    }
    
    public ManHinhDanhMuc(Runnable onDanhMucChangedCallback){
        this.onDanhMucChanged = onDanhMucChangedCallback; // Lưu callback
        
        setLayout(new BorderLayout(8,8));
        setBorder(new EmptyBorder(10,10,10,10));

        add(buildTop(), BorderLayout.NORTH);
        add(buildCenter(), BorderLayout.CENTER);
        add(buildRight(), BorderLayout.EAST);

        tbl.setRowHeight(24);
        tbl.getSelectionModel().addListSelectionListener(e -> pickRow());
        taiLaiBang();
    }
    // === (HẾT SỬA) ===

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

    private JComponent buildCenter(){
        JScrollPane sp = new JScrollPane(tbl);
        return sp;
    }

    private JComponent buildRight(){
        JPanel r = new JPanel(new GridBagLayout());
        r.setOpaque(false);
        r.setBorder(new EmptyBorder(0,12,0,0));
        r.setPreferredSize(new Dimension(380, 0));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6,6,6,6);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx=0; c.gridy=0; c.weightx=1;

        styleField(txtMa); styleField(txtTen);
        styleTextArea(txtMoTa);

        r.add(new JLabel("Mã danh mục:"), c); c.gridy++; r.add(txtMa, c);
        c.gridy++; r.add(new JLabel("Tên danh mục:"), c); c.gridy++; r.add(txtTen, c);
        c.gridy++; r.add(new JLabel("Mô tả:"), c);
        c.gridy++;
        JScrollPane sp = new JScrollPane(txtMoTa);
        sp.setBorder(new LineBorder(new Color(210,200,190), 1, true));
        r.add(sp, c);

        // Nút 2x2
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
            txtMoTa.setText(String.valueOf(model.getValueAt(i,2)));
        }
    }

    private void tim(){
        String kw = txtTim.getText().trim().toLowerCase();
        model.setRowCount(0);
        for (DanhMuc x : dao.getAll()){
            if (kw.isEmpty()
                    || x.getMaDM().toLowerCase().contains(kw)
                    || x.getTenDM().toLowerCase().contains(kw)
                    || (x.getMoTa()!=null && x.getMoTa().toLowerCase().contains(kw))){
                model.addRow(new Object[]{x.getMaDM(), x.getTenDM(), x.getMoTa()});
            }
        }
    }

    private void them(){
        String ma = txtMa.getText().trim();
        String ten = txtTen.getText().trim();
        String mt = txtMoTa.getText().trim();
        if (ma.isEmpty() || ten.isEmpty()){
            JOptionPane.showMessageDialog(this,"Nhập mã & tên danh mục!"); return;
        }
        try {
            dao.insert(new DanhMuc(ma, ten, mt));
            taiLaiBang(); 
            lamMoi();
            // === (THÊM MỚI) Thông báo cho GiaoDienChinh ===
            if (onDanhMucChanged != null) onDanhMucChanged.run();
        } catch (Exception ex){
            JOptionPane.showMessageDialog(this,"Thêm thất bại: "+ex.getMessage());
        }
    }

    private void sua(){
        String ma = txtMa.getText().trim();
        if (ma.isEmpty()){ JOptionPane.showMessageDialog(this,"Chọn dòng để sửa!"); return; }
        String ten = txtTen.getText().trim();
        String mt = txtMoTa.getText().trim();
        try {
            dao.update(new DanhMuc(ma, ten, mt));
            taiLaiBang(); 
            lamMoi();
            // === (THÊM MỚI) Thông báo cho GiaoDienChinh ===
            if (onDanhMucChanged != null) onDanhMucChanged.run();
        } catch (Exception ex){
            JOptionPane.showMessageDialog(this,"Sửa thất bại: "+ex.getMessage());
        }
    }

    private void xoa(){
        String ma = txtMa.getText().trim();
        if (ma.isEmpty()){ JOptionPane.showMessageDialog(this,"Chọn dòng để xóa!"); return; }
        if (JOptionPane.showConfirmDialog(this,"Xóa danh mục "+ma+"?","Xác nhận",
                JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION){
            try { 
                dao.delete(ma); 
                taiLaiBang(); 
                lamMoi(); 
                // === (THÊM MỚI) Thông báo cho GiaoDienChinh ===
                if (onDanhMucChanged != null) onDanhMucChanged.run();
            }
            catch(Exception ex){ JOptionPane.showMessageDialog(this,"Không xóa được: "+ex.getMessage()); }
        }
    }

    private void lamMoi(){
        txtMa.setText(""); txtTen.setText(""); txtMoTa.setText("");
        tbl.clearSelection(); txtMa.requestFocus();
    }

    private void taiLaiBang(){
        model.setRowCount(0);
        List<DanhMuc> ds = dao.getAll();
        for (DanhMuc x : ds){
            model.addRow(new Object[]{x.getMaDM(), x.getTenDM(), x.getMoTa()});
        }
    }

    /* ===== UI helpers (Giữ nguyên) ===== */
    private static void styleField(JTextField f){
        f.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(210,200,190), 1, true),
                new EmptyBorder(6,10,6,10)
        ));
        f.setFont(f.getFont().deriveFont(13f));
    }
    private static void styleTextArea(JTextArea a){
        a.setBorder(new EmptyBorder(6,10,6,10));
        a.setFont(a.getFont().deriveFont(13f));
        a.setLineWrap(true);
        a.setWrapStyleWord(true);
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