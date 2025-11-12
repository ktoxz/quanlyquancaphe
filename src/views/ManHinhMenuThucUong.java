package views;

import dao.DanhMucDAO;
import dao.SanPhamDAO;
import entity.DanhMuc;
import entity.SanPham;
import entity.NhanVien;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;

/**
 * ManHinhMenuThucUong (ĐÃ SỬA LỖI KIẾN TRÚC)
 * - Đã XÓA form quản lý danh mục (trả về file ManHinhDanhMuc.java).
 * - Đã THÊM label hiển thị bàn đang chọn.
 * - Đã SỬA napDanhMuc thành public để GiaoDienChinh gọi.
 */
public class ManHinhMenuThucUong extends JPanel {

    /* ===== palette ===== */
    private static final Color COFFEE     = new Color(111, 78, 55);
    private static final Color COFFEE_DK  = new Color(96, 67, 47);
    private static final Color BEIGE      = new Color(248, 245, 240);
    private static final Color BEIGE_LITE = new Color(253, 250, 244);
    private static final Color LINE       = new Color(210, 200, 190);
    private static final Color OK         = new Color(39, 174, 96);
    private static final Color DANGER     = new Color(192, 57, 43);
    private static final Color INFO       = new Color(33,150,243);
    private static final Color INFO_DK    = new Color(25,118,210);
    private static final Color MUTED      = new Color(120,144,156);
    private static final Color MUTED_DK   = new Color(96,125,139);

    /* ===== live set (optional) ===== */
    private static final Set<ManHinhMenuThucUong> LIVE = Collections.newSetFromMap(new WeakHashMap<>());

    private final SanPhamDAO spDAO = new SanPhamDAO();
    private final DanhMucDAO dmDAO = new DanhMucDAO();

    private final JTextField txtTim = new JTextField(22);
    private final JPanel listPanel = new JPanel();
    
    // === (THÊM MỚI) Label hiển thị bàn ===
    private final JLabel lbBanDangChon = new JLabel("Chưa chọn bàn");

    // form bên phải
    private final JTextField txtMa  = new JTextField(16);
    private final JTextField txtTen = new JTextField(24);
    private final JTextField txtGia = new JTextField(14);
    private final JComboBox<DanhMuc> cboDM = new JComboBox<>();
    private final JLabel imgPreview = new JLabel("", SwingConstants.CENTER);

    private byte[] imgBytes = null;
    private String imgFileName = null;
    private String imgContentType = null;
    private File   imgFile = null; // file ảnh đã chọn

    // nút
    private JButton btnThem, btnSua, btnXoa, btnMoi, btnChonAnh, btnTim, btnLM;

    // callback
    private Consumer<SanPham> onAdd;
    private final NumberFormat vn = NumberFormat.getNumberInstance(new Locale("vi", "VN"));

    /* ===== Constructors (Sử dụng constructor gốc của bạn) ===== */
    public ManHinhMenuThucUong(NhanVien nv, Consumer<SanPham> onAdd) {
        this(onAdd);
    }
    public ManHinhMenuThucUong(Consumer<SanPham> onAdd) {
        this.onAdd = onAdd;
        LIVE.add(this);
        buildUI();
        napDanhMuc(null);
        napDanhSach(fetchProducts(null));
    }
    
    // === (THÊM MỚI) Hàm để GiaoDienChinh gọi ===
    public void setCurrentBan(String maBan) {
        if (maBan == null || maBan.isEmpty()) {
            lbBanDangChon.setText("Chưa chọn bàn");
            lbBanDangChon.setForeground(Color.GRAY);
        } else {
            // Giả định maBan là "BAN01"
            lbBanDangChon.setText("Đang phục vụ: BÀN " + maBan.substring(3)); // Cắt "BAN"
            lbBanDangChon.setForeground(DANGER); // Màu đỏ
        }
    }


    /* ================= UI (ĐÃ SỬA) ================= */
    private void buildUI() {
        setLayout(new BorderLayout());
        setBackground(BEIGE);

        // TOP: tìm kiếm
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        top.setBackground(BEIGE);
        styleField(txtTim);
        btnTim = solidBtn(" Tìm", COFFEE, COFFEE_DK, views.Icons.search());
        btnLM  = solidBtn(" Làm mới", MUTED, MUTED_DK, views.Icons.refresh());
        top.add(new JLabel("Tìm món:"));
        top.add(txtTim);
        top.add(btnTim);
        top.add(btnLM);
        
        // === (THÊM MỚI) Thêm Label hiển thị bàn ===
        top.add(Box.createHorizontalStrut(20));
        lbBanDangChon.setFont(lbBanDangChon.getFont().deriveFont(Font.BOLD, 14f));
        setCurrentBan(null); // Set trạng thái mặc định
        top.add(lbBanDangChon);
        
        add(top, BorderLayout.NORTH);

        btnTim.addActionListener(e -> doTim());
        btnLM.addActionListener(e -> { txtTim.setText(""); napDanhSach(fetchProducts(null)); });
        txtTim.addActionListener(e -> doTim());

        // Center: split trái/phải
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(new Color(251, 247, 239));
        JScrollPane scLeft = new JScrollPane(listPanel);
        scLeft.getVerticalScrollBar().setUnitIncrement(16);
        scLeft.setBorder(new EmptyBorder(0,0,0,0));

        // === (ĐÃ SỬA) Trả về kiến trúc gốc ===
        JPanel right = buildRightForm();

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scLeft, right);
        split.setBorder(null);
        split.setDividerLocation(0.64);
        split.setResizeWeight(0.64);
        add(split, BorderLayout.CENTER);
    }

    private JPanel buildRightForm() {
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(BEIGE_LITE);
        wrap.setBorder(new EmptyBorder(12,12,12,12));

        JLabel title = new JLabel("Quản lý Thức uống");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
        title.setForeground(COFFEE);
        title.setBorder(new EmptyBorder(0,0,8,0));
        wrap.add(title, BorderLayout.NORTH);

        JPanel frm = new JPanel(new GridBagLayout());
        frm.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8,8,8,8);
        g.anchor = GridBagConstraints.WEST;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1; int r = 0;

        // hàng 1: Mã
        g.gridy=r; g.gridx=0; frm.add(new JLabel("Mã SP:"), g);
        styleField(txtMa); g.gridx=1; frm.add(txtMa, g); r++;

        // hàng 2: Tên
        g.gridy=r; g.gridx=0; frm.add(new JLabel("Tên SP:"), g);
        styleField(txtTen); g.gridx=1; frm.add(txtTen, g); r++;

        // hàng 3: Giá
        g.gridy=r; g.gridx=0; frm.add(new JLabel("Giá (đ):"), g);
        styleField(txtGia); g.gridx=1; frm.add(txtGia, g); r++;

        // hàng 4: Danh mục
        g.gridy=r; g.gridx=0; frm.add(new JLabel("Danh mục:"), g);
        cboDM.setFocusable(false);
        g.gridx=1; frm.add(cboDM, g); r++;

        // hàng 5: Ảnh
        g.gridy=r; g.gridx=0; frm.add(new JLabel("Ảnh:"), g);
        JPanel imgBox = new JPanel(new BorderLayout());
        imgBox.setOpaque(false);
        imgPreview.setPreferredSize(new Dimension(240, 160));
        imgPreview.setBorder(new LineBorder(LINE, 1, true));
        imgPreview.setOpaque(true);
        imgPreview.setBackground(Color.WHITE);
        btnChonAnh = solidBtn(" Chọn ảnh…", MUTED, MUTED_DK, views.Icons.drink()); 
        btnChonAnh.setPreferredSize(new Dimension(160, 42));
        btnChonAnh.addActionListener(e -> chonAnh());
        JPanel pBtn = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 8));
        pBtn.setOpaque(false);
        pBtn.add(btnChonAnh);
        imgBox.add(imgPreview, BorderLayout.CENTER);
        imgBox.add(pBtn, BorderLayout.SOUTH);
        g.gridx=1; frm.add(imgBox, g); r++;

        // hàng 6: nút hành động 2x2
        JPanel act = new JPanel(new GridLayout(2,2,12,12));
        act.setOpaque(false);
        btnThem = solidBtn(" Thêm",   OK,       OK.darker(),       views.Icons.plus());
        btnSua  = solidBtn(" Sửa",    INFO,     INFO_DK,           views.Icons.edit());
        btnXoa  = solidBtn(" Xóa",    DANGER,   DANGER.darker(),   views.Icons.remove());
        btnMoi  = solidBtn(" Làm mới",MUTED,    MUTED_DK,          views.Icons.refresh());
        btnThem.setPreferredSize(new Dimension(180, 46));
        btnSua.setPreferredSize(new Dimension(180, 46));
        btnXoa.setPreferredSize(new Dimension(180, 46));
        btnMoi.setPreferredSize(new Dimension(180, 46));
        act.add(btnThem); act.add(btnSua); act.add(btnXoa); act.add(btnMoi);

        btnThem.addActionListener(e -> doThem());
        btnSua.addActionListener(e -> doSua());
        btnXoa.addActionListener(e -> doXoa());
        btnMoi.addActionListener(e -> clearForm());

        g.gridy=r; g.gridx=0; g.gridwidth=2; frm.add(act, g);

        wrap.add(frm, BorderLayout.CENTER);
        return wrap;
    }

    /* ============== actions ============== */

    private void doTim() {
        String k = txtTim.getText().trim();
        napDanhSach(fetchProducts(k.isEmpty()?null:k));
    }

    private void doThem() {
        SanPham sp = readForm();
        if (sp == null) return;
        boolean ok = callInsert(sp); // Gọi hàm insert trong SanPhamDAO (Lượt 48)
        if (ok) {
            JOptionPane.showMessageDialog(this, "Đã thêm sản phẩm.");
            txtTim.setText("");
            napDanhSach(fetchProducts(null));
            clearForm();
            fireDanhMucChangedSafe(); // Thông báo cho các instance khác (nếu có)
        } else {
            JOptionPane.showMessageDialog(this, "Không thêm được sản phẩm!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void doSua() {
        SanPham sp = readForm();
        if (sp == null) return;
        boolean ok = callUpdate(sp); // Gọi hàm update trong SanPhamDAO (Lượt 48)
        if (ok) {
            JOptionPane.showMessageDialog(this, "Đã cập nhật.");
            napDanhSach(fetchProducts(null));
            selectInList(sp.getMaDM()); 
        } else {
            JOptionPane.showMessageDialog(this, "Không cập nhật được!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void doXoa() {
        String ma = txtMa.getText().trim();
        if (ma.isEmpty()) { JOptionPane.showMessageDialog(this, "Chưa chọn sản phẩm."); return; }
        if (JOptionPane.showConfirmDialog(this, "Xóa sản phẩm " + ma + " ?", "Xác nhận",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            boolean ok = callDelete(ma); // Gọi hàm delete trong SanPhamDAO (Lượt 48)
            if (ok) {
                JOptionPane.showMessageDialog(this, "Đã xóa.");
                napDanhSach(fetchProducts(null));
                clearForm();
            } else {
                JOptionPane.showMessageDialog(this, "Không xóa được!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void chonAnh() {
        JFileChooser fc = new JFileChooser();
        int r = fc.showOpenDialog(this);
        if (r == JFileChooser.APPROVE_OPTION) {
            imgFile = fc.getSelectedFile();
            try {
                byte[] bytes = java.nio.file.Files.readAllBytes(imgFile.toPath());
                imgBytes = bytes;
                imgFileName = imgFile.getName();
                imgContentType = guessContentType(imgFileName);

                ImageIcon icon = new ImageIcon(bytes);
                Image im = scalePreserveRatio(icon.getImage(), 240, 160);
                imgPreview.setIcon(new ImageIcon(im));
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Không đọc được file ảnh!");
            }
        }
    }

    private void clearForm() {
        txtMa.setText("");
        txtTen.setText("");
        txtGia.setText("");
        if (cboDM.getItemCount() > 0) cboDM.setSelectedIndex(0);
        txtMa.setEditable(true);
        imgPreview.setIcon(null);
        imgBytes = null; imgFileName = null; imgContentType = null; imgFile = null;
        txtMa.requestFocus();
    }

    private SanPham readForm() {
        String ma  = txtMa.getText().trim();
        String ten = txtTen.getText().trim();
        String giaS= txtGia.getText().trim().replace(".", "").replace(",", ""); 
        DanhMuc dm = (DanhMuc) cboDM.getSelectedItem();

        if (ma.isEmpty() || ten.isEmpty() || giaS.isEmpty() || dm == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đủ Mã / Tên / Giá / Danh mục.");
            return null;
        }

        BigDecimal gia;
        try {
            gia = BigDecimal.valueOf(Long.parseLong(giaS)); 
        } catch (Exception ex) {
            try {
                Number n = vn.parse(txtGia.getText().trim());
                gia = BigDecimal.valueOf(n.longValue());
            } catch (Exception e2) {
                JOptionPane.showMessageDialog(this, "Giá không hợp lệ!");
                return null;
            }
        }

        SanPham sp = new SanPham();
        sp.setMaSP(ma);
        sp.setTenSP(ten);
        sp.setDonGia(gia);
        sp.setMaDM(dm.getMaDM());

        if (imgFile != null) {
            setIfExists(sp, "setAnh", String.class, imgFile.getAbsolutePath());
            setIfExists(sp, "setHinhAnh", String.class, imgFile.getAbsolutePath());
        }
        if (imgBytes != null) {
            setIfExists(sp, "setAnhBytes", byte[].class, imgBytes);
            setIfExists(sp, "setAnhFileName", String.class, imgFileName);
            setIfExists(sp, "setAnhContentType", String.class, imgContentType);
        }
        return sp;
    }

    /* ============== danh sách bên trái ============== */

    private void napDanhSach(List<SanPham> ds) {
        listPanel.removeAll();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        if (ds == null || ds.isEmpty()) {
            JLabel empty = new JLabel("Chưa có sản phẩm để hiển thị", SwingConstants.CENTER);
            empty.setForeground(new Color(120,120,120));
            empty.setAlignmentX(Component.LEFT_ALIGNMENT);
            empty.setBorder(new EmptyBorder(24,12,24,12));
            listPanel.add(empty);
        } else {
            for (SanPham sp : ds) {
                JPanel row = buildRow(sp);
                row.setAlignmentX(Component.LEFT_ALIGNMENT); 
                listPanel.add(row);
                listPanel.add(Box.createVerticalStrut(8));
            }
        }
        
        listPanel.revalidate();
        listPanel.repaint();
        
        JScrollPane scLeft = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, listPanel);
        if (scLeft != null) {
            SwingUtilities.invokeLater(() -> {
                scLeft.getVerticalScrollBar().setValue(0);
            });
        }
    }

    private JPanel buildRow(SanPham sp) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(Color.WHITE);
        row.setBorder(BorderFactory.createCompoundBorder(
                new EmptyBorder(6,10,6,10),
                new LineBorder(new Color(240,235,225),1,true)
        ));
        row.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        row.setAlignmentX(Component.LEFT_ALIGNMENT); 
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100)); // Cao 100px

        JLabel lbImg = new JLabel("", SwingConstants.CENTER);
        
        int imgW = 140; int imgH = 100;
        lbImg.setPreferredSize(new Dimension(imgW, imgH)); 
        ImageIcon icon = resolveImage(sp);
        if (icon != null && icon.getIconWidth() > 0) {
            Image scaled = scalePreserveRatio(icon.getImage(), imgW, imgH);
            lbImg.setIcon(new ImageIcon(scaled));
        }
        row.add(lbImg, BorderLayout.WEST);

        JPanel mid = new JPanel();
        mid.setOpaque(false);
        mid.setLayout(new BoxLayout(mid, BoxLayout.Y_AXIS));
        mid.setAlignmentX(Component.LEFT_ALIGNMENT);
        mid.setBorder(new EmptyBorder(0, 12, 0, 12));
        
        JLabel t = new JLabel(sp.getTenSP());
        t.setFont(t.getFont().deriveFont(Font.BOLD, 14f));
        JLabel m = new JLabel("Mã: " + sp.getMaSP());
        JLabel g = new JLabel("Giá: " + vn.format(sp.getDonGia()));
        mid.add(t); mid.add(m); mid.add(g);
        row.add(mid, BorderLayout.CENTER);

        JButton add = solidBtn(" Thêm vào giỏ", OK, OK.darker(), views.Icons.plus());
        add.setPreferredSize(new Dimension(160, 38));
        add.addActionListener(e -> {
            if (onAdd != null) onAdd.accept(sp);
            else JOptionPane.showMessageDialog(this, "Chưa chọn bàn!");
        });
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 24));
        right.setOpaque(false);
        right.add(add);
        row.add(right, BorderLayout.EAST);

        row.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { fillForm(sp); }
        });

        return row;
    }


    private void fillForm(SanPham sp) {
        txtMa.setText(sp.getMaSP());
        txtTen.setText(sp.getTenSP());
        txtMa.setEditable(false);
        txtGia.setText(sp.getDonGia()!=null ? String.valueOf(sp.getDonGia().longValue()) : "");

        for (int i=0;i<cboDM.getItemCount();i++) {
            DanhMuc d = cboDM.getItemAt(i);
            if (d.getMaDM()!=null && d.getMaDM().equals(sp.getMaDM())) { cboDM.setSelectedIndex(i); break; }
        }

        ImageIcon icon = resolveImage(sp);
        imgPreview.setIcon(null);
        if (icon != null && icon.getIconWidth() > 0) {
            Image scaled = scalePreserveRatio(icon.getImage(), 240,160);
            imgPreview.setIcon(new ImageIcon(scaled));
        }

        imgBytes = getIfExists(sp, "getAnhBytes", byte[].class);
        imgFileName = getIfExists(sp, "getAnhFileName", String.class);
        imgContentType = getIfExists(sp, "getAnhContentType", String.class);
        imgFile = null;
    }

    private void selectInList(String ma) { /* preserve compatibility - no-op here */ }

    private ImageIcon resolveImage(SanPham sp) {
        try {
            String p = getFirstString(sp, "getAnh", "getHinh", "getHinhAnh");
            if (p != null && new File(p).exists()) {
                ImageIcon ic = new ImageIcon(p);
                return ic;
            }
        } catch (Exception ignored) {}
        try {
            byte[] b = getIfExists(sp, "getAnhBytes", byte[].class);
            if (b != null && b.length>0) return new ImageIcon(b);
        } catch (Exception ignored) {}
        return null;
    }

    /* ============== DAO helpers (reflection an toàn) ============== */

    @SuppressWarnings("unchecked")
    private List<SanPham> fetchProducts(String keyword) {
        try {
            if (keyword == null || keyword.isEmpty()) { 
                try {
                    Method m = spDAO.getClass().getMethod("getAll");
                    return (List<SanPham>) m.invoke(spDAO);
                } catch (NoSuchMethodException ex) {
                    try {
                        Method m = spDAO.getClass().getMethod("search", String.class);
                        return (List<SanPham>) m.invoke(spDAO, "");
                    } catch (NoSuchMethodException ex2) {
                        Method m = spDAO.getClass().getMethod("searchByName", String.class);
                        return (List<SanPham>) m.invoke(spDAO, "");
                    }
                }
            } else {
                try {
                    Method m = spDAO.getClass().getMethod("search", String.class);
                    return (List<SanPham>) m.invoke(spDAO, keyword);
                } catch (NoSuchMethodException ex) {
                    Method m = spDAO.getClass().getMethod("searchByName", String.class);
                    return (List<SanPham>) m.invoke(spDAO, keyword);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    private boolean callInsert(SanPham sp) {
        try {
            spDAO.insert(sp); // Gọi trực tiếp (vì đã có file Lượt 48)
            return true;
        } catch (Exception ex) { ex.printStackTrace(); return false; }
    }
    private boolean callUpdate(SanPham sp) {
        try {
            spDAO.update(sp); // Gọi trực tiếp
            return true;
        } catch (Exception ex) { ex.printStackTrace(); return false; }
    }
    private boolean callDelete(String ma) {
        try {
            spDAO.delete(ma); // Gọi trực tiếp
            return true;
        } catch (Exception ex) { ex.printStackTrace(); return false; }
    }

    /* ============== Danh mục (SỬA THÀNH PUBLIC) ============== */

    // === (SỬA) Đổi thành public để GiaoDienChinh gọi ===
    @SuppressWarnings("unchecked")
    public void napDanhMuc(String maDMToSelect) {
        try {
            List<DanhMuc> ds;
            try {
                Method m = dmDAO.getClass().getMethod("getAll");
                ds = (List<DanhMuc>) m.invoke(dmDAO);
            } catch (NoSuchMethodException ex) {
                Method m = dmDAO.getClass().getMethod("search", String.class);
                ds = (List<DanhMuc>) m.invoke(dmDAO, "");
            }
            
            Object selected = cboDM.getSelectedItem();
            
            cboDM.removeAllItems();
            for (DanhMuc d : ds) cboDM.addItem(d);

            if (maDMToSelect != null) { 
                for (int i=0;i<cboDM.getItemCount();i++) {
                    if (maDMToSelect.equals(cboDM.getItemAt(i).getMaDM())) { 
                        cboDM.setSelectedIndex(i); 
                        break; 
                    }
                }
            } else if (selected != null) { 
                cboDM.setSelectedItem(selected);
            }
            
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void fireDanhMucChangedSafe() {
        SwingUtilities.invokeLater(() -> {
            for (ManHinhMenuThucUong v : new ArrayList<>(LIVE)) {
                try { v.napDanhMuc(null); } catch (Throwable ignore) {}
            }
        });
    }

    /* ============== helpers (Giữ nguyên) ============== */
    
    private static void styleField(JTextField f) {
        f.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(210,200,190),1,true),
                new EmptyBorder(8,12,8,12)
        ));
        f.setFont(f.getFont().deriveFont(14f));
        f.setOpaque(true);
        f.setBackground(Color.WHITE);
    }

    private JButton solidBtn(String text, Color base, Color hover, Icon icon) { return new SolidButton(text, base, hover, icon); }
    private static class SolidButton extends JButton{
        private final Color base, hover; private boolean hov=false;
        SolidButton(String text, Color base, Color hover, Icon icon){
            super(text, icon); this.base=base; this.hover=hover;
            setFocusPainted(false); setOpaque(false); setContentAreaFilled(false);
            setBorder(BorderFactory.createEmptyBorder(10,18,10,18));
            setFont(getFont().deriveFont(Font.BOLD, 14f)); setForeground(Color.WHITE);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setHorizontalAlignment(LEFT);
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e){ hov=true; repaint(); }
                @Override public void mouseExited (MouseEvent e){ hov=false; repaint(); }
            });
        }
        @Override protected void paintComponent(Graphics g){
            Graphics2D g2=(Graphics2D)g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(hov?hover:base);
            g2.fillRoundRect(0,0,getWidth(),getHeight(),12,12);
            g2.dispose(); super.paintComponent(g);
        }
        @Override public Dimension getPreferredSize(){
            Dimension d = super.getPreferredSize();
            return new Dimension(Math.max(d.width, 140), Math.max(d.height, 42));
        }
    }

    private String guessContentType(String name) {
        String n = name==null? "" : name.toLowerCase(Locale.ROOT);
        if (n.endsWith(".png")) return "image/png";
        if (n.endsWith(".gif")) return "image/gif";
        if (n.endsWith(".bmp")) return "image/bmp";
        return "image/jpeg";
    }

    private static <T> void setIfExists(Object target, String setter, Class<T> type, T val) {
        try { Method m = target.getClass().getMethod(setter, type); m.invoke(target, val); }
        catch (Exception ignored) {}
    }
    @SuppressWarnings("unchecked")
    private static <T> T getIfExists(Object target, String getter, Class<T> type) {
        try { Method m = target.getClass().getMethod(getter); Object v = m.invoke(target); return (T) v; }
        catch (Exception ignored) { return null; }
    }
    private static String getFirstString(Object target, String... getters) {
        for (String g : getters) {
            String v = getIfExists(target, g, String.class);
            if (v != null && !v.isEmpty()) return v;
        }
        return null;
    }

    private static Image scalePreserveRatio(Image src, int maxW, int maxH) {
        if (src == null) return null;
        int w = src.getWidth(null), h = src.getHeight(null);
        if (w <= 0 || h <= 0) return src.getScaledInstance(maxW, maxH, Image.SCALE_SMOOTH);
        double rw = (double) maxW / w, rh = (double) maxH / h;
        double r = Math.min(rw, rh);
        int nw = (int) Math.max(1, Math.round(w * r));
        int nh = (int) Math.max(1, Math.round(h * r));
        return src.getScaledInstance(nw, nh, Image.SCALE_SMOOTH);
    }
}