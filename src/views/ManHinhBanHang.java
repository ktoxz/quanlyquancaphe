package views;

import dao.BanDAO;
import dao.ChiTietDonHangDAO;
import dao.DonHangDAO;
import dao.HoaDonDAO;
import entity.Ban;
import entity.ChiTietDonHang;
import entity.DonHang;
import entity.NhanVien;
import entity.SanPham;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.AbstractCellEditor;
import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

public class ManHinhBanHang extends JPanel {

    /* ===== palette (tông nâu cà phê) ===== */
    private static final Color COFFEE     = new Color(111, 78, 55);
    private static final Color COFFEE_DK  = new Color(96, 67, 47);
    private static final Color BEIGE      = new Color(248, 245, 240);
    private static final Color BEIGE_LITE = new Color(253, 250, 244);
    private static final Color LINE       = new Color(210, 200, 190);
    private static final Color DANGER     = new Color(192, 57, 43);

    private final NhanVien nv;
    private final Consumer<String> goMenu; // có thể null

    private DonHang currentDonHang;
    private String currentBan; // "BAN01", ...

    private final DonHangDAO dhDAO = new DonHangDAO();
    private final ChiTietDonHangDAO ctDAO = new ChiTietDonHangDAO();
    private final HoaDonDAO hdDAO = new HoaDonDAO();
    private final BanDAO banDAO = new BanDAO();

    // Cột bảng
    private static final int COL_MA  = 0;
    private static final int COL_TEN = 1;
    private static final int COL_SL  = 2;
    private static final int COL_DG  = 3;
    private static final int COL_TT  = 4;
    private static final int COL_XOA = 5;

    private final DefaultTableModel modelOrder =
            new DefaultTableModel(new String[]{"Mã","Tên","SL","Đơn giá","Thành tiền","Xóa"},0){
                @Override public boolean isCellEditable(int r,int c){ return c==COL_SL || c==COL_XOA; }
                @Override public Class<?> getColumnClass(int c){
                    return c==COL_SL ? Integer.class : Object.class;
                }
            };

    private final JTable tblOrder = new JTable(modelOrder);
    private final JLabel lbTong = new JLabel("0 đ");
    private final JTextField txtKhachDua = new JTextField(10);
    private final JLabel lblBanDangPhucVu = new JLabel("Bạn đang phục vụ: (chưa chọn bàn)");

    private final Map<String,JButton> banBtns = new LinkedHashMap<>();
    private final TableModelListener tableModelListener = e -> capNhatThanhTien();

    private final NumberFormat VNF = NumberFormat.getNumberInstance(new Locale("vi","VN"));

    public ManHinhBanHang(NhanVien nv, Consumer<String> goMenu){
        this.nv = nv;
        this.goMenu = goMenu;
        buildUI();
        modelOrder.addTableModelListener(tableModelListener);
        loadBanStatus();
    }

    /* ===================== UI ===================== */

    private void buildUI(){
        setLayout(new BorderLayout(8,8));
        setBorder(new EmptyBorder(8,8,8,8));

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, buildTableGrid(), buildOrderPanel());
        split.setDividerLocation(380);
        add(split, BorderLayout.CENTER);
    }

    private JComponent buildTableGrid(){
        JPanel wrap = new JPanel(new BorderLayout());
        JLabel title = new JLabel("SƠ ĐỒ BÀN", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 13f));
        title.setBorder(new EmptyBorder(4,0,8,0));
        wrap.add(title, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(6,4,10,10));
        grid.setBackground(Color.WHITE);

        for(int i=1;i<=23;i++){
            String maBan = String.format("BAN%02d",i);
            String tenBan = "BÀN " + String.format("%02d",i);

            JButton b = new JButton(tenBan, Icons.tableFree());
            b.setHorizontalTextPosition(SwingConstants.CENTER);
            b.setVerticalTextPosition(SwingConstants.BOTTOM);
            b.setFocusPainted(false);
            b.setBackground(BEIGE);
            b.setBorder(new LineBorder(LINE,1,true));

            b.addMouseListener(new MouseAdapter(){
                @Override public void mousePressed(MouseEvent e){
                    if(SwingUtilities.isRightMouseButton(e)){
                        showBanPopup(maBan,b);
                    } else if(SwingUtilities.isLeftMouseButton(e)){
                        chonBan(maBan);
                    }
                }
            });

            grid.add(b);
            banBtns.put(maBan,b);
        }

        wrap.add(new JScrollPane(grid),BorderLayout.CENTER);
        wrap.setPreferredSize(new Dimension(360,0));
        return wrap;
    }

    /* ===== style helpers cho Button (đảm bảo nền/ chữ không bị trắng) ===== */
    private void styleSolidButton(JButton b, Color bg, Color fg){
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setContentAreaFilled(true);
        b.setBackground(bg);
        b.setForeground(fg);
        b.setBorder(new LineBorder(LINE, 1, true));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private void showBanPopup(String maBan, JButton btn){
        JDialog dialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this), "Chọn trạng thái bàn", true);
        dialog.setLayout(new GridLayout(1, 3, 8, 8));
        dialog.setSize(420, 96);
        dialog.setLocationRelativeTo(btn);

        JButton btnPhucVu   = new JButton("Phục vụ");
        JButton btnDatTruoc = new JButton("Đặt trước");
        JButton btnHuy      = new JButton("Hủy");

        // ===== ÁP DỤNG STYLE RÕ NỀN - RÕ CHỮ =====
        styleSolidButton(btnPhucVu,   COFFEE,    Color.WHITE);   // nền nâu, chữ trắng
        styleSolidButton(btnDatTruoc, COFFEE_DK, Color.WHITE);   // nền nâu đậm, chữ trắng
        styleSolidButton(btnHuy,      new Color(245,245,245), COFFEE_DK); // nền xám rất nhạt, chữ nâu đậm

        btnPhucVu.setFont(btnPhucVu.getFont().deriveFont(Font.BOLD, 16f));
        btnDatTruoc.setFont(btnDatTruoc.getFont().deriveFont(Font.BOLD, 16f));
        btnHuy.setFont(btnHuy.getFont().deriveFont(Font.BOLD, 16f));

        btnPhucVu.addActionListener(e -> {
            setBanTrangThai(maBan, "PHUC_VU");
            dialog.dispose();
            chonBan(maBan);
            if(goMenu != null) goMenu.accept(maBan); // chỉ Phục vụ mới chuyển sang Menu (nếu bạn cấu hình vậy)
        });

        btnDatTruoc.addActionListener(e -> {
            setBanTrangThai(maBan,"DAT");
            dialog.dispose();
            chonBan(maBan);
        });

        btnHuy.addActionListener(e -> {
            setBanTrangThai(maBan,"TRONG");
            dialog.dispose();

            // KHÔNG điều hướng sang Menu khi bấm Hủy nữa
            if (maBan.equals(currentBan)) {
                currentBan = null;
                currentDonHang = null;
                lblBanDangPhucVu.setText("Bạn đang phục vụ: (chưa chọn bàn)");
                modelOrder.setRowCount(0);
                capNhatThanhTien();
                // ❌ bỏ hẳn dòng goMenu.accept(null);
            }
        });

        dialog.add(btnPhucVu);
        dialog.add(btnDatTruoc);
        dialog.add(btnHuy);
        dialog.setVisible(true);
    }

    private void setBanTrangThai(String maBan,String trangThai){
        JButton btn = banBtns.get(maBan);
        if(btn!=null){
            String tenBan = "BÀN " + maBan.substring(3);
            switch(trangThai){
                case "PHUC_VU" -> { btn.setIcon(Icons.tableBusy());    btn.setText(tenBan+" • PV");  btn.setToolTipText("Đang phục vụ"); }
                case "DAT"     -> { btn.setIcon(Icons.tableReserved()); btn.setText(tenBan+" • Đặt"); btn.setToolTipText("Đặt trước"); }
                case "TRONG"   -> { btn.setIcon(Icons.tableFree());     btn.setText(tenBan);          btn.setToolTipText("Bàn trống"); }
            }
        }
        banDAO.capNhatTrangThai(maBan,trangThai);
    }

    private void loadBanStatus(){
        List<Ban> list = banDAO.getAllBan();
        for(Ban b : list){
            setBanTrangThai(b.getMaBan(),b.getTrangThai());
        }
    }

    private void chonBan(String maBan){
        currentBan = maBan;
        currentDonHang = dhDAO.moDonHang(maBan,nv);
        if(currentDonHang!=null){
            loadChiTiet(currentDonHang.getMaDH());
        } else {
            modelOrder.setRowCount(0);
        }
        lblBanDangPhucVu.setText("Bạn đang phục vụ: BÀN " + maBan.substring(3));
    }

    private JComponent buildOrderPanel(){
        JPanel p = new JPanel(new BorderLayout(8,8));

        JPanel head = new JPanel(new FlowLayout(FlowLayout.LEFT,8,4));
        JButton btnLamMoi = new JButton("Làm mới", Icons.refresh());
        head.add(new JLabel("Đơn hàng"));
        head.add(Box.createHorizontalStrut(12));
        head.add(lblBanDangPhucVu);
        head.add(Box.createHorizontalStrut(12));
        head.add(btnLamMoi);
        p.add(head,BorderLayout.NORTH);

        tblOrder.setRowHeight(26);
        // Cột nút Xóa
        tblOrder.getColumnModel().getColumn(COL_XOA).setMaxWidth(60);
        tblOrder.getColumnModel().getColumn(COL_XOA).setCellRenderer(new ButtonColumn(Icons.trashSmall(), "delete"));
        tblOrder.getColumnModel().getColumn(COL_XOA).setCellEditor  (new ButtonColumn(Icons.trashSmall(), "delete"));

        p.add(new JScrollPane(tblOrder),BorderLayout.CENTER);

        // ===== KHU THANH TOÁN =====
        JPanel pay = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4,4,4,4);
        c.anchor = GridBagConstraints.WEST;
        lbTong.setFont(lbTong.getFont().deriveFont(Font.BOLD,14f));
        txtKhachDua.setHorizontalAlignment(JTextField.RIGHT);

        // Định dạng nhanh khi gõ 'k' hoặc '.'
        txtKhachDua.addFocusListener(new FocusAdapter() {
            @Override public void focusLost(FocusEvent e) {
                long value = parseMoneyField(txtKhachDua.getText());
                txtKhachDua.setText(value>0 ? VNF.format(value) : "");
            }
        });
        txtKhachDua.addKeyListener(new KeyAdapter() {
            @Override public void keyTyped(KeyEvent e) {
                char ch = e.getKeyChar();
                if (ch == 'k' || ch == 'K' || ch == '.') {
                    e.consume();
                    String text = txtKhachDua.getText().replaceAll("[^\\d]", "");
                    if (!text.isEmpty()) {
                        long value = Long.parseLong(text) * 1000;
                        txtKhachDua.setText(VNF.format(value));
                    }
                }
            }
        });

        int r=0;
        c.gridx=0;c.gridy=r; pay.add(new JLabel("Tổng tiền:"),c);
        c.gridx=1;c.gridy=r; pay.add(lbTong,c); r++;
        c.gridx=0;c.gridy=r; pay.add(new JLabel("Tiền khách đưa:"),c);
        c.gridx=1;c.gridy=r; pay.add(txtKhachDua,c); r++;

        JButton btnPay = new JButton("Thanh toán",Icons.check());
        stylePrimaryButton(btnPay); // nền nâu, chữ trắng rõ ràng
        c.gridx=1;c.gridy=r; pay.add(btnPay,c);

        // ===== HÀNG NÚT NHANH (1k…100k) =====
        JPanel quick = new JPanel(new FlowLayout(FlowLayout.LEFT,10,6));
        quick.setOpaque(false);
        long[] denoms = new long[]{1,2,5,10,20,50,100,200,500};
        for(long d : denoms){
            JButton b = new JButton(d + "k");
            styleCashButton(b);
            b.addActionListener(e -> addDenomination(d * 1000));
            quick.add(b);
        }
        JButton bClr = new JButton("Xóa tiền");
        styleGhostButton(bClr);
        bClr.addActionListener(e -> txtKhachDua.setText(""));
        quick.add(bClr);

        JPanel south = new JPanel(new BorderLayout());
        south.add(pay, BorderLayout.NORTH);
        south.add(quick, BorderLayout.CENTER);
        p.add(south, BorderLayout.SOUTH);

        btnLamMoi.addActionListener(e -> lamMoi());
        btnPay.addActionListener(e -> thanhToan());

        return p;
    }

    /* ===== Style helpers cho dải nút tiền ===== */
    private static class RoundedBorder extends javax.swing.border.AbstractBorder {
        private final int arc; private final Color line;
        RoundedBorder(int arc, Color line){ this.arc = arc; this.line = line; }
        @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(line);
            g2.drawRoundRect(x, y, w-1, h-1, arc, arc);
            g2.dispose();
        }
    }
    private void styleCashButton(JButton b){
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setContentAreaFilled(true);
        b.setBackground(Color.WHITE);
        b.setForeground(COFFEE_DK);
        b.setFont(b.getFont().deriveFont(Font.BOLD, 13f));
        b.setBorder(new RoundedBorder(14, LINE));
        b.setPreferredSize(new Dimension(64, 34));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { b.setBackground(BEIGE_LITE); }
            @Override public void mouseExited (MouseEvent e) { b.setBackground(Color.WHITE); }
            @Override public void mousePressed(MouseEvent e) { b.setBackground(new Color(245,240,232)); }
            @Override public void mouseReleased(MouseEvent e){ b.setBackground(BEIGE_LITE); }
        });
    }
    private void styleGhostButton(JButton b){
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setContentAreaFilled(true);
        b.setBackground(BEIGE);
        b.setForeground(COFFEE_DK);
        b.setFont(b.getFont().deriveFont(Font.PLAIN, 12f));
        b.setBorder(new RoundedBorder(14, LINE));
        b.setPreferredSize(new Dimension(88, 34));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }
    private void stylePrimaryButton(JButton b){
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setContentAreaFilled(true);
        b.setBackground(COFFEE);
        b.setForeground(Color.WHITE);
        b.setFont(b.getFont().deriveFont(Font.BOLD, 13f));
        b.setBorder(new RoundedBorder(16, COFFEE_DK));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    /* ===================== Logic ===================== */

    private void addDenomination(long v){
        long now = parseMoneyField(txtKhachDua.getText());
        now += v;
        txtKhachDua.setText(VNF.format(now));
    }
    private long parseMoneyField(String s){
        if (s==null) return 0L;
        String dd = s.replaceAll("[^\\d]", "");
        if (dd.isEmpty()) return 0L;
        try { return Long.parseLong(dd); } catch (Exception e) { return 0L; }
    }

    private void loadChiTiet(String maDH){
        modelOrder.removeTableModelListener(tableModelListener);
        modelOrder.setRowCount(0);
        for(var x: ctDAO.getByMaDH(maDH)){
            BigDecimal tt = x.getDonGia().multiply(BigDecimal.valueOf(x.getSoLuong()));
            modelOrder.addRow(new Object[]{x.getMaSP(),x.getTenSP(),x.getSoLuong(),x.getDonGia(),tt,null});
        }
        capNhatThanhTien();
        modelOrder.addTableModelListener(tableModelListener);

        var h = hdDAO.getByMaDH(maDH);
        if (h != null && h.getTienKhachDua()!=null) {
            txtKhachDua.setText(VNF.format(h.getTienKhachDua()));
        }
    }

    private void capNhatThanhTien(){
        modelOrder.removeTableModelListener(tableModelListener);
        try{
            BigDecimal tong = BigDecimal.ZERO;
            for(int i=0;i<modelOrder.getRowCount();i++){
                Object dgObj = modelOrder.getValueAt(i,COL_DG);
                Object slObj = modelOrder.getValueAt(i,COL_SL);
                BigDecimal dg = dgObj instanceof BigDecimal ? (BigDecimal) dgObj :
                        dgObj!=null ? new BigDecimal(dgObj.toString()) : BigDecimal.ZERO;
                int sl = slObj instanceof Number ? ((Number) slObj).intValue() :
                        slObj!=null ? Integer.parseInt(slObj.toString()) : 0;
                BigDecimal tt = dg.multiply(BigDecimal.valueOf(sl));
                modelOrder.setValueAt(tt,i,COL_TT);
                tong = tong.add(tt);
            }
            lbTong.setText(VNF.format(tong)+" đ");
        } finally {
            modelOrder.addTableModelListener(tableModelListener);
        }
    }

    public void addFromMenu(SanPham sp){
        if(currentDonHang==null){
            JOptionPane.showMessageDialog(this,"Vui lòng chọn bàn trước!");
            return;
        }
        BigDecimal dg = sp.getDonGia()==null ? BigDecimal.ZERO : sp.getDonGia();
        ctDAO.insertOrIncrease(new ChiTietDonHang(currentDonHang.getMaDH(), sp.getMaSP(),1,dg));
        loadChiTiet(currentDonHang.getMaDH());
    }

    private void xoaDong(int rowModel){
        if(rowModel >= 0){
            String maSP = String.valueOf(modelOrder.getValueAt(rowModel,COL_MA));
            if(currentDonHang!=null){
                ctDAO.deleteItem(currentDonHang.getMaDH(),maSP);
                loadChiTiet(currentDonHang.getMaDH());
            } else {
                modelOrder.removeRow(rowModel);
                capNhatThanhTien();
            }
        }
    }

    private void lamMoi(){
        if(currentDonHang!=null) loadChiTiet(currentDonHang.getMaDH());
        else { modelOrder.setRowCount(0); capNhatThanhTien(); }
    }

    private void thanhToan(){
        if(currentDonHang==null || currentBan==null){
            JOptionPane.showMessageDialog(this,"Chưa chọn bàn / không có đơn.");
            return;
        }

        BigDecimal tong = BigDecimal.ZERO;
        for(int i=0;i<modelOrder.getRowCount();i++){
            Object v = modelOrder.getValueAt(i,COL_TT);
            if (v instanceof BigDecimal bd) tong = tong.add(bd);
            else if (v!=null) try{ tong = tong.add(new BigDecimal(v.toString())); }catch(Exception ignored){}
        }
        if(tong.compareTo(BigDecimal.ZERO)<=0){
            JOptionPane.showMessageDialog(this,"Giỏ hàng trống!"); return;
        }

        long khach = parseMoneyField(txtKhachDua.getText());
        BigDecimal khachDua = BigDecimal.valueOf(khach);
        if(khachDua.compareTo(tong)<0){
            JOptionPane.showMessageDialog(this,"Khách đưa chưa đủ!"); return;
        }

        int idHD = hdDAO.taoHoaDonTuDonHang(currentDonHang.getMaDH(), tong, khachDua);
        String maHD = idHD>0 ? String.format("HD%06d",idHD) : null;

        dhDAO.dongDonHang(currentDonHang);
        setBanTrangThai(currentBan,"TRONG");

        try{ PrintHoaDonDiaLog.open(this,currentDonHang.getMaDH(),khachDua); }catch(Throwable ignored){}
        JOptionPane.showMessageDialog(this,"Thanh toán thành công!\nMã HĐ: "+(maHD==null?"(N/A)":maHD));

        modelOrder.setRowCount(0);
        capNhatThanhTien();
        currentDonHang=null;
        currentBan=null;
        txtKhachDua.setText("");
        lblBanDangPhucVu.setText("Bạn đang phục vụ: (chưa chọn bàn)");
    }

    /* ===== Nút XÓA: Renderer + Editor (ghi nhớ editingRow để tránh -1) ===== */
    class ButtonColumn extends AbstractCellEditor implements TableCellRenderer, TableCellEditor, ActionListener {
        private JTable table;
        private final JButton renderButton;
        private final JButton editButton;
        private final String actionCommand;
        private int editingRow = -1;

        public ButtonColumn(Icon icon, String actionCommand) {
            this.actionCommand = actionCommand;

            renderButton = new JButton(icon);
            renderButton.setToolTipText("Xóa món này");
            renderButton.setBorderPainted(false);
            renderButton.setOpaque(true);
            renderButton.setContentAreaFilled(true);
            renderButton.setBackground(new Color(255,245,245));
            renderButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

            editButton = new JButton(icon);
            editButton.setToolTipText("Xóa món này");
            editButton.setBorderPainted(false);
            editButton.setOpaque(true);
            editButton.setContentAreaFilled(true);
            editButton.setBackground(new Color(255,235,235));
            editButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
            editButton.addActionListener(this);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            this.table = table;
            return renderButton;
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            this.table = table;
            this.editingRow = row; // ghi nhớ row ngay khi editor mở
            return editButton;
        }

        @Override public Object getCellEditorValue() { return null; }

        @Override
        public void actionPerformed(ActionEvent e) {
            int rowModel = table.convertRowIndexToModel(editingRow);
            fireEditingStopped(); // dừng editor SAU khi nhớ row
            if (rowModel >= 0 && rowModel < table.getModel().getRowCount()) {
                xoaDong(rowModel);
            }
        }
    }
}
