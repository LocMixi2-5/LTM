package com.billiards.client;

import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.*;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

/**
 * Quản lý danh sách Avatar và Màu gậy/Đồ họa cây cơ cho người chơi.
 * 
 * - 16 Avatar phong cách độc đáo với emoji và nền gradient cao cấp.
 * - Hỗ trợ nạp ảnh đại diện tùy ý từ máy tính cá nhân.
 * - 10 Màu sắc và họa tiết gậy bi-a tinh xảo.
 * - Hệ thống đồ họa render cây cơ chân thực (chuôi, tay cầm bọc da/chỉ, vòng khớp ren kim loại, ngọn gỗ phủ màu, đầu tẩy gắn lơ).
 */
public class AvatarManager {

    // ============================================================
    // 16 AVATAR ĐỊNH NGHĨA SẴN: {text, gradientStart, gradientEnd, tên}
    // ============================================================
    private static final String[][] AVATARS = {
        {"🎱", "#1a1a2e", "#e94560", "Bi-a Huyền Thoại"},
        {"🦁", "#d35400", "#f39c12", "Sư Tử Lửa"},
        {"🐉", "#900c3f", "#c70039", "Hỏa Long"},
        {"🦅", "#0f3460", "#16a085", "Đại Bàng Lam"},
        {"🐺", "#2c3e50", "#4ca1af", "Sói Băng Dạ"},
        {"🦊", "#e65c00", "#f9d423", "Cáo Hoàng Hôn"},
        {"🎋", "#134e5e", "#71b280", "Trúc Tiên"},
        {"🦈", "#000046", "#1cb5e0", "Cá Mập Biển Sâu"},
        {"🦉", "#3a1c71", "#d76d77", "Cú Đêm Tri Thức"},
        {"🐯", "#304352", "#d7d2cc", "Bạch Hổ"},
        {"🐍", "#11998e", "#38ef7d", "Hắc Xà"},
        {"👑", "#f7971e", "#ffd200", "Hoàng Đế Bi-a"},
        {"⚡", "#4b6cb7", "#182848", "Lôi Thần"},
        {"💎", "#654ea3", "#eaafc8", "Kim Cương"},
        {"⚔️", "#eb3349", "#f45c43", "Chiến Binh"},
        {"🎯", "#2b5876", "#4e4376", "Thiện Xạ"}
    };

    // ============================================================
    // 10 MÀU GẬY BI-A: {mã màu hex, tên kiểu gậy}
    // ============================================================
    public static final String[][] CUE_COLORS = {
        {"#DEB887", "Gỗ Sồi Cổ Điển"},
        {"#8B4513", "Gỗ Óc Chó Quý"},
        {"#C0392B", "Hỏa Long Đỏ"},
        {"#2980B9", "Lam Ngọc Đại Dương"},
        {"#F39C12", "Hoàng Kim Giáp"},
        {"#2C3E50", "Hắc Ám Huyền Bí"},
        {"#27AE60", "Lục Bảo Thần Mộc"},
        {"#8E44AD", "Tử Tinh Huyền Ảo"},
        {"#E91E63", "Neon Hồng Cyber"},
        {"#BDC3C7", "Bạch Ngân Tuyết"}
    };

    // Ảnh tùy chọn của người chơi từ máy tính (nếu có)
    private static Image customAvatarImage = null;

    public static int getAvatarCount() { return AVATARS.length; }
    public static int getCueColorCount() { return CUE_COLORS.length; }

    public static String getEmoji(int id) {
        if (id < 0 || id >= AVATARS.length) id = 0;
        return AVATARS[id][0];
    }

    public static String getGradientStart(int id) {
        if (id < 0 || id >= AVATARS.length) id = 0;
        return AVATARS[id][1];
    }

    public static String getGradientEnd(int id) {
        if (id < 0 || id >= AVATARS.length) id = 0;
        return AVATARS[id][2];
    }

    public static String getAvatarName(int id) {
        if (id < 0 || id >= AVATARS.length) id = 0;
        return AVATARS[id][3];
    }

    public static String getCueColorHex(int idx) {
        if (idx < 0 || idx >= CUE_COLORS.length) idx = 0;
        return CUE_COLORS[idx][0];
    }

    public static String getCueColorName(int idx) {
        if (idx < 0 || idx >= CUE_COLORS.length) idx = 0;
        return CUE_COLORS[idx][1];
    }

    public static Image getCustomAvatarImage() {
        return customAvatarImage;
    }

    public static void setCustomAvatarImage(Image img) {
        customAvatarImage = img;
    }

    // ============================================================
    // TẠO NODE AVATAR (JavaFX)
    // ============================================================

    public static StackPane createAvatarNode(int id, double size) {
        return createAvatarNode(id, size, null);
    }

    /**
     * Tạo avatar tròn với gradient sắc nét hoặc hình ảnh người dùng tải lên
     */
    public static StackPane createAvatarNode(int id, double size, Image customImg) {
        if (id < 0 || id >= AVATARS.length) id = 0;

        StackPane pane = new StackPane();
        pane.setAlignment(Pos.CENTER);
        pane.setMinSize(size + 4, size + 4);
        pane.setMaxSize(size + 4, size + 4);

        Circle bg = new Circle(size / 2);

        if (customImg != null) {
            bg.setFill(new ImagePattern(customImg));
        } else {
            bg.setFill(new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web(AVATARS[id][1])),
                new Stop(1, Color.web(AVATARS[id][2]))
            ));
        }

        bg.setStroke(Color.web("#ffffff", 0.4));
        bg.setStrokeWidth(Math.max(1.5, size * 0.04));

        DropShadow shadow = new DropShadow();
        shadow.setRadius(size * 0.15);
        shadow.setColor(Color.web(AVATARS[id][2], 0.6));
        bg.setEffect(shadow);

        pane.getChildren().add(bg);

        if (customImg == null) {
            Text emoji = new Text(AVATARS[id][0]);
            emoji.setFont(Font.font("Arial", javafx.scene.text.FontWeight.BOLD, size * 0.5));
            emoji.setFill(Color.WHITE);
            pane.getChildren().add(emoji);
        }

        return pane;
    }

    public static StackPane createSmallAvatar(int id) {
        return createAvatarNode(id, 36);
    }

    public static StackPane createMediumAvatar(int id) {
        return createAvatarNode(id, 56);
    }

    public static StackPane createLargeAvatar(int id) {
        return createAvatarNode(id, 80);
    }

    public static StackPane createSelectableAvatar(int id, double size, boolean selected) {
        StackPane node = createAvatarNode(id, size, null); // Danh sách chọn luôn hiển thị icon gốc
        if (selected) {
            Circle border = new Circle(size / 2 + 4);
            border.setFill(Color.TRANSPARENT);
            border.setStroke(Color.web("#f1c40f"));
            border.setStrokeWidth(3.5);

            DropShadow glow = new DropShadow();
            glow.setRadius(10);
            glow.setColor(Color.web("#f39c12", 0.8));
            border.setEffect(glow);

            node.getChildren().add(0, border);
        }
        return node;
    }

    // ============================================================
    // ĐỒ HỌA CÂY CƠ BI-A (CUE STICK GRAPHICS)
    // ============================================================

    /**
     * Vẽ cây cơ chân thực trên GraphicsContext.
     * 
     * @param gc           GraphicsContext cần vẽ
     * @param tipX, tipY   Tọa độ đầu gậy (tiếp xúc bi)
     * @param nx, ny       Vector đơn vị hướng bóng đi
     * @param cueBack      Khoảng lùi của gậy khi kéo lực
     * @param cueColorHex  Mã màu gậy (mặc định #DEB887)
     */
    public static void drawRealisticCue(GraphicsContext gc,
                                       double cueBallX, double cueBallY,
                                       double nx, double ny,
                                       double cueBack, String cueColorHex) {
        if (cueColorHex == null || cueColorHex.isEmpty()) {
            cueColorHex = "#DEB887";
        }
        Color mainColor;
        try {
            mainColor = Color.web(cueColorHex);
        } catch (Exception e) {
            mainColor = Color.web("#DEB887");
        }

        double totalLen = 220.0;
        // Điểm bắt đầu của đầu cơ (cách tâm bi 1 khoảng Ball.RADIUS + cueBack)
        double startDist = 12.0 + cueBack;
        double endDist   = startDist + totalLen;

        // Vector lùi lại phía sau bi
        double dirX = -nx;
        double dirY = -ny;

        // Vector pháp tuyến vuông góc để tạo độ dày
        double perpX = -ny;
        double perpY = nx;

        // Vẽ bóng đổ (Soft Drop Shadow) dưới cây cơ trên mặt bàn nỉ
        gc.save();
        gc.setStroke(Color.color(0, 0, 0, 0.35));
        gc.setLineWidth(7.0);
        gc.strokeLine(
            cueBallX + dirX * (startDist + 8) + 4, cueBallY + dirY * (startDist + 8) + 4,
            cueBallX + dirX * endDist + 4,         cueBallY + dirY * endDist + 4
        );
        gc.restore();

        // 1. CHUÔI GẬY (Butt Section & Rubber Bumper) - Cuối gậy
        double buttStart = startDist + 140.0;
        double buttEnd   = endDist;
        gc.setStroke(Color.web("#1c1c1c")); // Đệm cao su đen
        gc.setLineWidth(10.0);
        gc.strokeLine(
            cueBallX + dirX * (buttEnd - 8), cueBallY + dirY * (buttEnd - 8),
            cueBallX + dirX * buttEnd,       cueBallY + dirY * buttEnd
        );

        // Vòng nẹp chuôi mạ vàng (Gold accent ring)
        gc.setStroke(Color.web("#f1c40f"));
        gc.setLineWidth(9.0);
        gc.strokeLine(
            cueBallX + dirX * (buttEnd - 12), cueBallY + dirY * (buttEnd - 12),
            cueBallX + dirX * (buttEnd - 8),  cueBallY + dirY * (buttEnd - 8)
        );

        // 2. TAY CẦM BỌC CHỈ / DA (Grip / Wrap Area)
        double gripStart = startDist + 110.0;
        double gripEnd   = buttEnd - 12.0;
        gc.setStroke(Color.web("#262626"));
        gc.setLineWidth(8.5);
        gc.strokeLine(
            cueBallX + dirX * gripStart, cueBallY + dirY * gripStart,
            cueBallX + dirX * gripEnd,   cueBallY + dirY * gripEnd
        );

        // Đường vân caro / chỉ bọc trên tay cầm
        gc.setStroke(Color.web("#3d3d3d"));
        gc.setLineWidth(7.0);
        gc.setLineDashes(3, 4);
        gc.strokeLine(
            cueBallX + dirX * gripStart, cueBallY + dirY * gripStart,
            cueBallX + dirX * gripEnd,   cueBallY + dirY * gripEnd
        );
        gc.setLineDashes();

        // Vòng đai khớp nối kim loại (Metallic Joint Collar)
        gc.setStroke(Color.web("#e67e22"));
        gc.setLineWidth(7.5);
        gc.strokeLine(
            cueBallX + dirX * (gripStart - 6), cueBallY + dirY * (gripStart - 6),
            cueBallX + dirX * gripStart,       cueBallY + dirY * gripStart
        );

        // 3. THÂN GẬY / NGỌN GẬY THEO MÀU LỰA CHỌN (Shaft & Forearm)
        double shaftStart = startDist + 12.0;
        double shaftEnd   = gripStart - 6.0;

        // Thân gậy vát thon (Taper effect)
        gc.setStroke(mainColor);
        gc.setLineWidth(6.5);
        gc.strokeLine(
            cueBallX + dirX * shaftStart, cueBallY + dirY * shaftStart,
            cueBallX + dirX * shaftEnd,   cueBallY + dirY * shaftEnd
        );

        // Vệt sáng highlight dọc thân gậy tạo cảm giác bóng bẩy
        gc.setStroke(Color.color(1, 1, 1, 0.35));
        gc.setLineWidth(2.0);
        gc.strokeLine(
            cueBallX + dirX * (shaftStart + 4) + perpX * 1.5, cueBallY + dirY * (shaftStart + 4) + perpY * 1.5,
            cueBallX + dirX * (shaftEnd - 4)   + perpX * 1.5, cueBallY + dirY * (shaftEnd - 4)   + perpY * 1.5
        );

        // 4. PHÍP GẬY NGÀ VOI (Ferrule - White ivory collar)
        double ferruleStart = startDist + 4.0;
        double ferruleEnd   = startDist + 12.0;
        gc.setStroke(Color.web("#fcfaf2"));
        gc.setLineWidth(5.5);
        gc.strokeLine(
            cueBallX + dirX * ferruleStart, cueBallY + dirY * ferruleStart,
            cueBallX + dirX * ferruleEnd,   cueBallY + dirY * ferruleEnd
        );

        // 5. ĐẦU TẨY XANH LƠ (Chalked Blue Cue Tip)
        double tipStart = startDist;
        double tipEnd   = startDist + 4.0;
        gc.setStroke(Color.web("#2980b9")); // Màu lơ bi-a Master xanh
        gc.setLineWidth(5.0);
        gc.strokeLine(
            cueBallX + dirX * tipStart, cueBallY + dirY * tipStart,
            cueBallX + dirX * tipEnd,   cueBallY + dirY * tipEnd
        );
    }

    /**
     * Vẽ cây gậy hoàn chỉnh để xem trước trong hộp thoại Cài Đặt.
     */
    public static void renderCuePreview(Canvas canvas, String cueColorHex) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        double w = canvas.getWidth();
        double h = canvas.getHeight();

        gc.clearRect(0, 0, w, h);

        // Nền tối thanh lịch cho hộp preview
        gc.setFill(Color.web("#121829"));
        gc.fillRoundRect(0, 0, w, h, 10, 10);
        gc.setStroke(Color.web("#253252"));
        gc.setLineWidth(1.5);
        gc.strokeRoundRect(0, 0, w, h, 10, 10);

        Color mainColor;
        try {
            mainColor = Color.web(cueColorHex);
        } catch (Exception e) {
            mainColor = Color.web("#DEB887");
        }

        double startX = 25;
        double endX   = w - 25;
        double midY   = h / 2.0;

        // Bóng đổ
        gc.setStroke(Color.color(0, 0, 0, 0.45));
        gc.setLineWidth(10);
        gc.strokeLine(startX, midY + 4, endX, midY + 4);

        // 1. Đầu tẩy xanh
        gc.setStroke(Color.web("#2980b9"));
        gc.setLineWidth(6.0);
        gc.strokeLine(startX, midY, startX + 8, midY);

        // 2. Phíp trắng
        gc.setStroke(Color.web("#faf8f2"));
        gc.setLineWidth(7.0);
        gc.strokeLine(startX + 8, midY, startX + 22, midY);

        // 3. Thân gậy phủ màu
        double shaftEnd = startX + (endX - startX) * 0.58;
        gc.setStroke(mainColor);
        gc.setLineWidth(8.5);
        gc.strokeLine(startX + 22, midY, shaftEnd, midY);

        // Highlight bóng bẩy
        gc.setStroke(Color.color(1, 1, 1, 0.4));
        gc.setLineWidth(2.0);
        gc.strokeLine(startX + 24, midY - 2.0, shaftEnd - 4, midY - 2.0);

        // Khớp kim loại vàng
        gc.setStroke(Color.web("#f1c40f"));
        gc.setLineWidth(10.0);
        gc.strokeLine(shaftEnd, midY, shaftEnd + 10, midY);

        // 4. Tay cầm Grip bọc đen
        double gripEnd = endX - 18;
        gc.setStroke(Color.web("#202020"));
        gc.setLineWidth(11.0);
        gc.strokeLine(shaftEnd + 10, midY, gripEnd, midY);

        // Vân tay cầm
        gc.setStroke(Color.web("#383838"));
        gc.setLineWidth(9.0);
        gc.setLineDashes(4, 5);
        gc.strokeLine(shaftEnd + 10, midY, gripEnd, midY);
        gc.setLineDashes();

        // Vành mạ vàng chuôi
        gc.setStroke(Color.web("#f39c12"));
        gc.setLineWidth(11.5);
        gc.strokeLine(gripEnd, midY, gripEnd + 6, midY);

        // Đệm cao su chuôi
        gc.setStroke(Color.web("#0d0d0d"));
        gc.setLineWidth(12.0);
        gc.strokeLine(gripEnd + 6, midY, endX, midY);
    }
}
