package app;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import views.DangNhap;

/**
 * Điểm vào chương trình.
 * - Thiết lập Look & Feel hệ điều hành
 * - Mở hộp thoại Đăng nhập (DangNhap)
 *   (DangNhap của bạn tự mở GiaoDienChinh nếu đăng nhập thành công)
 */
public class MainApp {

    public static void main(String[] args) {
        // Thiết lập giao diện hệ điều hành (không bắt buộc, nhưng đẹp hơn)
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException
                 | IllegalAccessException | UnsupportedLookAndFeelException ignored) {
        }
        System.out.println("Hello Tài");

        System.out.println("Hello world");
        // Chạy UI trên EDT
        SwingUtilities.invokeLater(() -> {
            // Mở màn hình đăng nhập (modal)
            new DangNhap().setVisible(true);

            // Lưu ý:
            // - Nếu DangNhap của bạn tự mở GiaoDienChinh khi login OK (đã đúng như mình sửa),
            //   thì ở đây không cần làm gì thêm.
            // - Nếu bạn đổi DangNhap sang kiểu "return NhanVien" (không tự mở),
            //   có thể thay bằng:
            //     DangNhap dlg = new DangNhap();
            //     dlg.setVisible(true);
            //     NhanVien nv = dlg.getNguoiDung(); // ví dụ nếu bạn có getter
            //     if (nv != null) new GiaoDienChinh(nv).setVisible(true);
        });
    }
}
