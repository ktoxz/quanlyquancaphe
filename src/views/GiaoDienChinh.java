package views;

import entity.NhanVien;
// === (ĐÃ THÊM) Import SanPham ===
import entity.SanPham;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

public class GiaoDienChinh extends JFrame {

    private final NhanVien nguoiDung;

    private final CardLayout cards = new CardLayout();
    private final JPanel content = new JPanel(cards);

    // Các màn hình
    private ManHinhBanHang pnlBanHang;
    private ManHinhMenuThucUong pnlMenu;
    private ManHinhDanhMuc pnlDanhMuc;
    private ManHinhHoaDon pnlHoaDon;
    private ManHinhNhanVien pnlNhanVien;
    private ManHinhThongKe pnlThongKe;

    // ====== Màu cho sidebar (Đã fix lỗi thiếu biến) ======
    private static final Color SB_BG_NORMAL = new Color(111,78,55);   // nâu
    private static final Color SB_BG_HOVER  = new Color(96,67,47);    // nâu đậm
    private static final Color SB_BG_ACTIVE = new Color(240,232,219); // beige
    private static final Color SB_FG_LIGHT  = Color.WHITE;
    private static final Color SB_FG_DARK   = new Color(84,54,38);

    private final Map<String, NavButton> navBtns = new LinkedHashMap<>();
    private String activeKey = null;

    public GiaoDienChinh(NhanVien nv){
        this.nguoiDung = nv;
        setTitle("Zenta Coffee – Quản lý bán hàng");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1280, 720);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(buildTopBar(), BorderLayout.NORTH);
        add(buildLeft(), BorderLayout.WEST);
        add(buildScreens(), BorderLayout.CENTER);
        setActive("BANHANG"); // mặc định
    }

    /* ===== Topbar ===== */
    private JComponent buildTopBar(){
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(new Color(88, 52, 35));
        top.setBorder(new EmptyBorder(6,10,6,10));

        JLabel title = new JLabel("Zenta Coffee");
        title.setForeground(Color.WHITE);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        title.setIcon(icon("logo", "ZC"));
        title.setIconTextGap(10);
        top.add(title, BorderLayout.WEST);

        String nguoi = safeStr(invokeStr(nguoiDung, "getTenNV"), "");
        String role  = firstNonEmpty(
                invokeStr(nguoiDung, "getChucVu"),
                invokeStr(nguoiDung, "getVaiTro"),
                ""
        );
        JLabel info = new JLabel("Người dùng: " + nguoi + "   |   Vai trò: " + role);
        info.setForeground(Color.WHITE);
        top.add(info, BorderLayout.EAST);
        return top;
    }

    /* ===== Sidebar ===== */
    private JComponent buildLeft(){
        Box left = Box.createVerticalBox();
        left.setBorder(new EmptyBorder(8, 8, 8, 8));
        left.setBackground(new Color(245, 236, 223));
        left.setOpaque(true);

        left.add(navBtn("BANHANG",  "  BÁN HÀNG",        icon("order", "\uD83D\uDCCB"))); left.add(Box.createVerticalStrut(8));
        left.add(navBtn("HOADON",   "  HÓA ĐƠN",         icon("invoice", "\uD83D\uDCC4"))); left.add(Box.createVerticalStrut(8));
        left.add(navBtn("DANHMUC",  "  DANH MỤC",        icon("category", "\uD83D\uDCC2"))); left.add(Box.createVerticalStrut(8));
        left.add(navBtn("MENU",     "  MENU THỨC UỐNG",  icon("menu", "\u2615"))); left.add(Box.createVerticalStrut(8));
        left.add(navBtn("NHANVIEN", "  NHÂN VIÊN",       icon("users", "\uD83D\uDC65"))); left.add(Box.createVerticalStrut(8));
        left.add(navBtn("THONGKE",  "  THỐNG KÊ",        icon("stats", "\uD83D\uDCCA")));

        left.add(Box.createVerticalGlue());

        NavButton btnLogout = navBtn("LOGOUT", "  ĐĂNG XUẤT", icon("logout", "\u21AA"));
        btnLogout.setCustomColors(new Color(192,57,43), new Color(172,47,37), new Color(192,57,43), SB_FG_LIGHT, SB_FG_LIGHT);
        btnLogout.addActionListener(e -> {
            dispose();
            SwingUtilities.invokeLater(() -> new DangNhap().setVisible(true));
        });
        left.add(Box.createVerticalStrut(8));
        left.add(btnLogout);
        return left;
    }

    private NavButton navBtn(String key, String text, Icon icon){
        NavButton b = new NavButton(text, icon);
        b.setCustomColors(SB_BG_NORMAL, SB_BG_HOVER, SB_BG_ACTIVE, SB_FG_LIGHT, SB_FG_DARK);
        b.addActionListener(e -> setActive(key));
        navBtns.put(key, b);
        return b;
    }

    private void setActive(String key){
        cards.show(content, key);
        if (activeKey != null && navBtns.containsKey(activeKey)) {
            navBtns.get(activeKey).setActive(false);
        }
        activeKey = key;
        if (navBtns.containsKey(key)) {
            navBtns.get(key).setActive(true);
        }
    }

    /* ===== buildScreens (ĐÃ SỬA) ===== */
    private JComponent buildScreens() {
        
        // --- NỐI DÂY SỰ KIỆN ---

        // 1. Callback (Sự kiện) khi Bàn hàng (BanHang) muốn chuyển sang Menu
        //    (Truyền mã bàn "BAN01" sang Menu)
        Consumer<String> evtChonBanSangMenu = (maBan) -> {
            if (pnlMenu != null) {
                // Gọi hàm public của pnlMenu để set text
                pnlMenu.setCurrentBan(maBan);  
            }
            setActive("MENU"); // Chuyển tab
        };

        // 2. Callback (Sự kiện) khi Menu muốn thêm món vào Bàn hàng
        Consumer<SanPham> evtThemMonVaoDon = (sanPham) -> {
            if (pnlBanHang != null) {
                // Gọi hàm public của pnlBanHang để thêm món
                pnlBanHang.addFromMenu(sanPham);
            }
        };
        
        // 3. Callback (Sự kiện) khi Danh mục (DanhMuc) thay đổi
        //    (Báo cho Menu biết để tải lại ComboBox)
        Runnable evtDanhMucThayDoi = () -> {
            if (pnlMenu != null) {
                // Gọi hàm public của pnlMenu để tải lại CBO
                pnlMenu.napDanhMuc(null); 
            }
        };

        // --- KHỞI TẠO CÁC PANEL VỚI CALLBACK ---
        
        // Khởi tạo BanHang, truyền callback (1)
        pnlBanHang = new ManHinhBanHang(nguoiDung, evtChonBanSangMenu);

        // Khởi tạo Menu, truyền callback (2)
        // (Sử dụng constructor ManHinhMenuThucUong(NhanVien, Consumer) của bạn)
        pnlMenu = new ManHinhMenuThucUong(nguoiDung, evtThemMonVaoDon); 

        // Khởi tạo DanhMuc, truyền callback (3)
        pnlDanhMuc = new ManHinhDanhMuc(evtDanhMucThayDoi);
        
        // Khởi tạo các panel còn lại (dùng try-catch cho an toàn)
        try { pnlHoaDon   = new ManHinhHoaDon(); } catch (Throwable ignore){}
        try { pnlNhanVien = new ManHinhNhanVien(); } catch (Throwable ignore){}
        try { pnlThongKe  = new ManHinhThongKe(); } catch (Throwable ignore){}

        // Add vào CardLayout
        content.add(pnlBanHang, "BANHANG");
        content.add(pnlMenu,    "MENU");
        content.add(pnlDanhMuc, "DANHMUC");
        if (pnlHoaDon   != null) content.add(pnlHoaDon,   "HOADON");
        if (pnlNhanVien != null) content.add(pnlNhanVien, "NHANVIEN");
        if (pnlThongKe  != null) content.add(pnlThongKe,  "THONGKE");

        return content;
    }

    /* ===== Icon helper ===== */
    private static Icon icon(String method, String glyph){
        try {
            Class<?> ic = Class.forName("views.Icons");
            Method m = ic.getMethod(method);
            Object v = m.invoke(null);
            if (v instanceof Icon) return (Icon) v;
        } catch (Exception ignore){}
        int s = 24;
        BufferedImage img = new BufferedImage(s, s, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(SB_BG_NORMAL);
        g.fillRoundRect(0,0,s-1,s-1, s/3, s/3);
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 14));
        FontMetrics fm = g.getFontMetrics();
        int x = (s - fm.stringWidth(glyph))/2;
        int y = (s - fm.getHeight())/2 + fm.getAscent();
        g.drawString(glyph, x, y);
        g.dispose();
        return new ImageIcon(img);
    }

    /* ===== NavButton: vẽ nền thủ công để màu luôn đúng ===== */
    private static class NavButton extends JButton {
        private boolean active = false;
        private Color bgNormal, bgHover, bgActive, fgNormal, fgActive;
        private boolean hover = false;

        NavButton(String text, Icon icon) {
            super(text, icon);
            setHorizontalAlignment(SwingConstants.LEFT);
            setPreferredSize(new Dimension(220, 64));
            setMinimumSize(new Dimension(220, 64));
            setMaximumSize(new Dimension(Short.MAX_VALUE, 64));
            setFocusPainted(false);
            setBorder(BorderFactory.createEmptyBorder(14,16,14,16));
            setOpaque(false);
            setContentAreaFilled(false);
            setRolloverEnabled(true);

            addMouseListener(new java.awt.event.MouseAdapter() {
                @Override public void mouseEntered(java.awt.event.MouseEvent e) { hover = true;  repaint(); }
                @Override public void mouseExited (java.awt.event.MouseEvent e) { hover = false; repaint(); }
            });
        }

        void setCustomColors(Color normal, Color hover, Color active, Color fgNormal, Color fgActive){
            this.bgNormal = normal; this.bgHover = hover; this.bgActive = active;
            this.fgNormal = fgNormal; this.fgActive = fgActive;
            repaint();
        }

        void setActive(boolean a){ this.active = a; repaint(); }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Color bg = active ? bgActive : (hover ? bgHover : bgNormal);
            Color fg = active ? fgActive : fgNormal;

            g2.setColor(bg);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            g2.dispose();

            setForeground(fg);
            super.paintComponent(g);
        }
    }

    /* ===== Helpers (Giữ nguyên) ===== */
    private static String safeStr(Object o, String d){ return o==null?d:o.toString(); }
    private static String invokeStr(Object obj, String method){
        if (obj == null) return null;
        try { return String.valueOf(obj.getClass().getMethod(method).invoke(obj)); }
        catch (Exception e){ return null; }
    }
    private static String firstNonEmpty(String a, String b, String d){
        if (a!=null && !a.isEmpty()) return a;
        if (b!=null && !b.isEmpty()) return b;
        return d;
    }
}