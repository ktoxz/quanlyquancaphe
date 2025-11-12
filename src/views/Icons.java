package views;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public final class Icons {
    private Icons(){}

    /* ===== core factory ===== */
    private static Icon make(int size, Color bg, String glyph) {
        int s = Math.max(16, size);
        BufferedImage img = new BufferedImage(s, s, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.setColor(bg);
        g.fillRoundRect(0, 0, s-1, s-1, s/3, s/3);

        g.setColor(Color.WHITE);
        Font f = new Font("SansSerif", Font.BOLD, (int)(s*0.60));
        g.setFont(f);
        FontMetrics fm = g.getFontMetrics();
        int x = (s - fm.stringWidth(glyph)) / 2;
        int y = (s - fm.getHeight()) / 2 + fm.getAscent();
        g.drawString(glyph, x, y);

        g.dispose();
        return new ImageIcon(img);
    }

    private static Icon small(Color bg, String glyph){ return make(20, bg, glyph); }
    private static Icon big(Color bg, String glyph){ return make(24, bg, glyph); }
    private static Icon circle(int size, Color bg, String glyph){ return make(size, bg, glyph); }

    /* ===== app icons ===== */
    public static Icon order()    { return big(new Color(121, 85, 72), "\uD83D\uDCCB"); }
    public static Icon invoice()  { return big(new Color(96, 125, 139), "\uD83D\uDCC4"); }
    public static Icon category() { return big(new Color(92, 107, 192), "\uD83D\uDCC2"); }
    public static Icon menu()     { return big(new Color(67, 160, 71), "\u2615"); }
    public static Icon users()    { return big(new Color(3, 155, 229), "\uD83D\uDC65"); }
    public static Icon stats()    { return big(new Color(0, 150, 136), "\uD83D\uDCCA"); }
    public static Icon logout()   { return big(new Color(192, 57, 43),  "\u21AA"); }

    /* ===== actions / controls ===== */
    public static Icon trash()   { return small(new Color(211, 47, 47), "\uD83D\uDDD1"); }
    public static Icon check()   { return small(new Color(0, 150, 136), "\u2714"); }
    public static Icon search()  { return small(new Color(97, 97, 97),  "\uD83D\uDD0D"); }
    public static Icon refresh() { return small(new Color(120, 144, 156), "\u21BB"); }
    public static Icon plus()    { return small(new Color(76, 175, 80),  "+"); }
    public static Icon edit()    { return small(new Color(33, 150, 243), "\u270E"); }
    public static Icon remove()  { return trash(); }
    
    // === (THÊM MỚI) Icon 'X' nhỏ cho nút xóa nội tuyến ===
    public static Icon trashSmall(){ return make(18, new Color(211, 47, 47), "X"); } 

    /* ===== bàn theo trạng thái ===== */
    public static Icon table()        { return small(new Color(121, 85, 72), "B"); }
    public static Icon tableFree()    { return small(new Color(56, 142, 60), "T"); }
    public static Icon tableBusy()    { return small(new Color(255, 143, 0), "PV"); }
    public static Icon tableReserved(){ return small(new Color(233, 30, 99), "ĐT"); }

    /* ===== logo / dùng lại ===== */
    public static Icon logo()  { return circle(28, new Color(111, 78, 55), "ZC"); }
    public static Icon drink() { return menu(); }
    public static Icon add()   { return plus(); }
}