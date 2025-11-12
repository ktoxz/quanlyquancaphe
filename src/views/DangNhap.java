package views;

import dao.TaiKhoanDAO;
import entity.NhanVien;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.util.prefs.Preferences;

public class DangNhap extends JDialog {
    // ===== Fields =====
    private final JTextField     txtUser = new JTextField();
    private final JPasswordField txtPass = new JPasswordField();
    private final JCheckBox      chkShow = new JCheckBox("Hiện mật khẩu");
    private final JCheckBox      chkRemember = new JCheckBox("Nhớ tôi");
    private final SolidButton    btnLogin = new SolidButton("Đăng nhập", new Color(162,116,72), new Color(140,100,60));
    private final SolidButton    btnExit  = new SolidButton("Thoát",      new Color(94,94,94),   new Color(78,78,78));

    private final TaiKhoanDAO tkDAO = new TaiKhoanDAO();
    private final Preferences prefs = Preferences.userRoot().node("ZentaCoffee/Login");
    private char defaultEchoChar;

    public DangNhap() {
        setTitle("Đăng nhập");
        setModal(true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(500, 500);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(new Color(245, 241, 235));
        root.setBorder(new EmptyBorder(10,10,10,10));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(92,52,37));
        header.setBorder(new EmptyBorder(12,16,12,16));
        header.add(new JLabel(paintLogo(64, 40)), BorderLayout.WEST);
        JLabel brand = new JLabel("Zenta Coffee");
        brand.setForeground(Color.WHITE);
        brand.setFont(brand.getFont().deriveFont(Font.BOLD, 30f));
        header.add(brand, BorderLayout.CENTER);
        root.add(header, BorderLayout.NORTH);

        // Card
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(new Color(251,249,245));
        card.setBorder(new EmptyBorder(16,18,18,18));

        JLabel title = new JLabel("Đăng nhập hệ thống");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 22f));
        title.setBorder(new EmptyBorder(0,0,10,0));
        card.add(title, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8,12,8,12);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0; c.gridy = 0;

        form.add(new JLabel("Tài khoản"), c);
        c.gridy++; styleField(txtUser); form.add(txtUser, c);

        c.gridy++; form.add(new JLabel("Mật khẩu"), c);
        c.gridy++; styleField(txtPass); form.add(txtPass, c);

        JPanel opts = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        opts.setOpaque(false);
        defaultEchoChar = txtPass.getEchoChar();
        chkShow.setOpaque(false);
        chkShow.addActionListener(e -> txtPass.setEchoChar(
                chkShow.isSelected() ? (char)0 : defaultEchoChar
        ));
        chkRemember.setOpaque(false);
        opts.add(chkShow); opts.add(chkRemember);

        c.gridy++; form.add(opts, c);

        JPanel actions = new JPanel(new GridLayout(1,2,12,0));
        actions.setOpaque(false);
        actions.add(btnLogin);
        actions.add(btnExit);

        btnExit.addActionListener(e -> dispose());
        btnLogin.addActionListener(e -> xuLyDangNhap());

        // Enable/disable nút đăng nhập theo input
        DocumentListener dl = new DocumentListener() {
            void on() {
                boolean ok = !txtUser.getText().trim().isEmpty()
                        && txtPass.getPassword().length > 0;
                btnLogin.setEnabled(ok);
                btnLogin.setTextColor(Color.WHITE);
            }
            public void insertUpdate(DocumentEvent e){ on(); }
            public void removeUpdate(DocumentEvent e){ on(); }
            public void changedUpdate(DocumentEvent e){ on(); }
        };
        txtUser.getDocument().addDocumentListener(dl);
        txtPass.getDocument().addDocumentListener(dl);
        btnLogin.setEnabled(false);

        c.gridy++; form.add(actions, c);

        card.add(form, BorderLayout.CENTER);
        root.add(card, BorderLayout.CENTER);

        JLabel foot = new JLabel("© Zenta Coffee – Quản lý bán hàng", SwingConstants.CENTER);
        foot.setBorder(new EmptyBorder(8,0,0,0));
        root.add(foot, BorderLayout.SOUTH);

        setContentPane(root);
        getRootPane().setDefaultButton(btnLogin);

        loadRemember();
    }

    /* ===== Button vẽ nền thủ công ===== */
    private static class SolidButton extends JButton {
        private final Color base, hover;
        private boolean isHover = false;
        private Color textColor = Color.WHITE;

        SolidButton(String text, Color base, Color hover){
            super(text);
            this.base = base; this.hover = hover;
            setFocusPainted(false);
            setOpaque(false);
            setContentAreaFilled(false);
            setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
            setFont(getFont().deriveFont(Font.BOLD, 14f));
            setForeground(textColor);

            addMouseListener(new java.awt.event.MouseAdapter() {
                @Override public void mouseEntered(java.awt.event.MouseEvent e) { isHover = true;  repaint(); }
                @Override public void mouseExited (java.awt.event.MouseEvent e) { isHover = false; repaint(); }
            });
        }

        void setTextColor(Color c){ this.textColor = c; setForeground(c); }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(isHover ? hover : base);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            g2.dispose();

            setForeground(textColor); // chữ luôn rõ
            super.paintComponent(g);
        }
    }

    /* ===== Helper UI ===== */
    private static void styleField(JTextComponent f){
        f.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(210,200,190), 1, true),
                new EmptyBorder(10, 12, 10, 12)
        ));
        f.setFont(f.getFont().deriveFont(14f));
    }

    private static Icon paintLogo(int w, int h){
        return new Icon() {
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.translate(x, y);
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255,255,255,220));
                g2.fillRoundRect(0, h/3, w-14, h/2, 10, 10);
                g2.setColor(new Color(210,190,170));
                g2.drawRoundRect(0, h/3, w-14, h/2, 10, 10);
                g2.drawOval(w-18, h/2-6, 12, 12);
                g2.setColor(new Color(110, 70, 50));
                g2.fillRoundRect(4, h/3+4, w-22, 8, 8, 8);
                g2.dispose();
            }
            public int getIconWidth(){ return w; }
            public int getIconHeight(){ return h; }
        };
    }

    /* ===== Logic ===== */
    private void xuLyDangNhap() {
        String u = txtUser.getText().trim();
        String p = new String(txtPass.getPassword());

        try {
            btnLogin.setEnabled(false);

            NhanVien nv = tkDAO.login(u, p);
            if (nv == null) {
                JOptionPane.showMessageDialog(this, "Tài khoản hoặc mật khẩu không đúng!", "Đăng nhập thất bại", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (chkRemember.isSelected()) {
                prefs.put("username", u);
                prefs.putBoolean("remember", true);
            } else {
                prefs.remove("username");
                prefs.putBoolean("remember", false);
            }

            dispose();
            new GiaoDienChinh(nv).setVisible(true);

        } finally {
            btnLogin.setEnabled(true);
        }
    }

    private void loadRemember() {
        boolean remember = prefs.getBoolean("remember", false);
        chkRemember.setSelected(remember);
        if (remember) {
            String u = prefs.get("username", "");
            txtUser.setText(u);
            if (!u.isEmpty()) txtPass.requestFocusInWindow();
        }
    }
}