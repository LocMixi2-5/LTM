package com.billiards.client.theme;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Hệ thống giao diện "Modern Billiards Club" chuẩn mực cho toàn bộ ứng dụng:
 * - Bảng màu: Nền chính #111816, Panel #1A2420, Hover #24322B, Viền #34443B
 * - Khung hình vuông tuyệt đối (radius: 0), 4 góc tráng bạc (Silver Accents)
 * - Chữ: Chính #EDF1E8, Phụ #A5B1A8
 * - Nút chính: #287A57, Điểm nhấn đồng: #C2A36B, Nút nguy hiểm: #A83A3A
 * - Typography: Segoe UI / Arial sans-serif hỗ trợ tiếng Việt đầy đủ.
 */
public final class Theme {

    private Theme() {}

    // ============================================================
    // BẢNG MÀU THỐNG NHẤT (COLOR PALETTE)
    // ============================================================
    public static final String BG_MAIN            = "#111816";
    public static final String SURFACE            = "#1A2420";
    public static final String SURFACE_HOVER      = "#24322B";
    public static final String SURFACE_INPUT      = "#141D1A";
    public static final String BORDER             = "#34443B";
    public static final String BORDER_LIGHT       = "#43564B";

    public static final String TEXT_PRIMARY       = "#EDF1E8";
    public static final String TEXT_MUTED         = "#A5B1A8";
    public static final String TEXT_FAINT         = "#6D7B72";

    public static final String ACCENT_GREEN       = "#287A57";
    public static final String ACCENT_GREEN_HOVER = "#32936A";
    public static final String ACCENT_BRASS       = "#C2A36B";
    public static final String ACCENT_BRASS_HOVER = "#D4B67E";
    public static final String ACCENT_SILVER      = "#D1D5DB";

    public static final String DANGER             = "#A83A3A";
    public static final String DANGER_HOVER       = "#BF4545";
    public static final String SUCCESS            = "#2E8A57";

    // Aliases để tiện sử dụng ở các Scene
    public static final String HEX_BG             = BG_MAIN;
    public static final String HEX_SURFACE        = SURFACE;
    public static final String HEX_SURFACE_HOVER  = SURFACE_HOVER;
    public static final String HEX_SURFACE_INPUT  = SURFACE_INPUT;
    public static final String HEX_BORDER         = BORDER;
    public static final String HEX_TEXT           = TEXT_PRIMARY;
    public static final String HEX_TEXT_MUTED     = TEXT_MUTED;
    public static final String HEX_PRIMARY        = ACCENT_GREEN;
    public static final String HEX_PRIMARY_HOVER  = ACCENT_GREEN_HOVER;
    public static final String HEX_BRONZE         = ACCENT_BRASS;
    public static final String HEX_BRONZE_HOVER   = ACCENT_BRASS_HOVER;
    public static final String HEX_SILVER         = ACCENT_SILVER;
    public static final String HEX_DANGER         = DANGER;
    public static final String HEX_DANGER_HOVER   = DANGER_HOVER;

    // JavaFX Color Objects
    public static final Color C_BG_MAIN       = Color.web(BG_MAIN);
    public static final Color C_SURFACE       = Color.web(SURFACE);
    public static final Color C_SURFACE_HOVER = Color.web(SURFACE_HOVER);
    public static final Color C_BORDER        = Color.web(BORDER);
    public static final Color C_TEXT_PRIMARY  = Color.web(TEXT_PRIMARY);
    public static final Color C_TEXT_MUTED    = Color.web(TEXT_MUTED);
    public static final Color C_ACCENT_GREEN  = Color.web(ACCENT_GREEN);
    public static final Color C_ACCENT_BRASS  = Color.web(ACCENT_BRASS);
    public static final Color C_ACCENT_SILVER = Color.web(ACCENT_SILVER);
    public static final Color C_DANGER        = Color.web(DANGER);
    public static final Color C_SUCCESS       = Color.web(SUCCESS);

    // ============================================================
    // TYPOGRAPHY (FONT FAMILY & SIZES)
    // ============================================================
    public static final String FONT_FAMILY = "Segoe UI";

    public static Font fontTitle(double size) {
        return Font.font(FONT_FAMILY, FontWeight.BOLD, size);
    }

    public static Font fontMedium(double size) {
        return Font.font(FONT_FAMILY, FontWeight.SEMI_BOLD, size);
    }

    public static Font fontRegular(double size) {
        return Font.font(FONT_FAMILY, FontWeight.NORMAL, size);
    }

    // ============================================================
    // STYLING HELPERS (HÌNH VUÔNG GÓC CẠNH, KHÔNG BO TRÒN)
    // ============================================================

    /** Tạo bóng đổ nhẹ tinh tế cho card/panel */
    public static DropShadow panelShadow() {
        return new DropShadow(18, Color.rgb(0, 0, 0, 0.5));
    }

    /** Tạo bóng đổ cực nhẹ cho nút */
    public static DropShadow buttonShadow() {
        return new DropShadow(8, Color.rgb(0, 0, 0, 0.3));
    }

    /** Áp dụng style panel cho VBox hoặc Region (vuông góc 0 radius) */
    public static void applyCardStyle(Region region) {
        region.setStyle(
            "-fx-background-color: " + SURFACE + ";" +
            "-fx-background-radius: 0;" +
            "-fx-border-color: " + BORDER + ";" +
            "-fx-border-radius: 0;" +
            "-fx-border-width: 1.0px;"
        );
        region.setEffect(panelShadow());
    }

    public static String cardStyle() {
        return "-fx-background-color: " + SURFACE + ";" +
               "-fx-background-radius: 0;" +
               "-fx-border-color: " + BORDER + ";" +
               "-fx-border-radius: 0;" +
               "-fx-border-width: 1.0;";
    }

    public static String inputStyle() {
        return "-fx-background-color: " + SURFACE_INPUT + ";" +
               "-fx-text-fill: " + TEXT_PRIMARY + ";" +
               "-fx-prompt-text-fill: " + TEXT_FAINT + ";" +
               "-fx-background-radius: 0;" +
               "-fx-border-color: " + BORDER + ";" +
               "-fx-border-radius: 0;" +
               "-fx-border-width: 1.0px;" +
               "-fx-padding: 8px 12px;" +
               "-fx-font-family: '" + FONT_FAMILY + "';" +
               "-fx-font-size: 13px;";
    }

    /** Nút chính (Primary Action) - hình vuông không bo tròn */
    public static void stylePrimaryButton(Button btn) {
        btn.setFont(fontMedium(14));
        btn.setStyle(
            "-fx-background-color: " + ACCENT_GREEN + ";" +
            "-fx-text-fill: " + TEXT_PRIMARY + ";" +
            "-fx-background-radius: 0;" +
            "-fx-border-radius: 0;" +
            "-fx-padding: 10px 20px;" +
            "-fx-cursor: hand;"
        );
        btn.setOnMouseEntered(e -> btn.setStyle(
            "-fx-background-color: " + ACCENT_GREEN_HOVER + ";" +
            "-fx-text-fill: #FFFFFF;" +
            "-fx-background-radius: 0;" +
            "-fx-border-radius: 0;" +
            "-fx-padding: 10px 20px;" +
            "-fx-cursor: hand;"
        ));
        btn.setOnMouseExited(e -> btn.setStyle(
            "-fx-background-color: " + ACCENT_GREEN + ";" +
            "-fx-text-fill: " + TEXT_PRIMARY + ";" +
            "-fx-background-radius: 0;" +
            "-fx-border-radius: 0;" +
            "-fx-padding: 10px 20px;" +
            "-fx-cursor: hand;"
        ));
    }

    public static Button btnPrimary(String text, double w, double h) {
        Button btn = new Button(text);
        btn.setPrefSize(w, h);
        btn.setFont(fontMedium(13));
        btn.setStyle(
            "-fx-background-color: " + ACCENT_GREEN + ";" +
            "-fx-text-fill: " + TEXT_PRIMARY + ";" +
            "-fx-background-radius: 0;" +
            "-fx-border-radius: 0;" +
            "-fx-cursor: hand;"
        );
        btn.setOnMouseEntered(e -> btn.setStyle(
            "-fx-background-color: " + ACCENT_GREEN_HOVER + ";" +
            "-fx-text-fill: #FFFFFF;" +
            "-fx-background-radius: 0;" +
            "-fx-border-radius: 0;" +
            "-fx-cursor: hand;"
        ));
        btn.setOnMouseExited(e -> btn.setStyle(
            "-fx-background-color: " + ACCENT_GREEN + ";" +
            "-fx-text-fill: " + TEXT_PRIMARY + ";" +
            "-fx-background-radius: 0;" +
            "-fx-border-radius: 0;" +
            "-fx-cursor: hand;"
        ));
        return btn;
    }

    /** Nút phụ (Secondary Action) - hình vuông không bo tròn */
    public static void styleSecondaryButton(Button btn) {
        btn.setFont(fontMedium(13));
        btn.setStyle(
            "-fx-background-color: " + SURFACE_HOVER + ";" +
            "-fx-text-fill: " + TEXT_PRIMARY + ";" +
            "-fx-border-color: " + BORDER + ";" +
            "-fx-border-radius: 0;" +
            "-fx-background-radius: 0;" +
            "-fx-padding: 8px 16px;" +
            "-fx-cursor: hand;"
        );
        btn.setOnMouseEntered(e -> btn.setStyle(
            "-fx-background-color: " + BORDER + ";" +
            "-fx-text-fill: #FFFFFF;" +
            "-fx-border-color: " + BORDER_LIGHT + ";" +
            "-fx-border-radius: 0;" +
            "-fx-background-radius: 0;" +
            "-fx-padding: 8px 16px;" +
            "-fx-cursor: hand;"
        ));
        btn.setOnMouseExited(e -> btn.setStyle(
            "-fx-background-color: " + SURFACE_HOVER + ";" +
            "-fx-text-fill: " + TEXT_PRIMARY + ";" +
            "-fx-border-color: " + BORDER + ";" +
            "-fx-border-radius: 0;" +
            "-fx-background-radius: 0;" +
            "-fx-padding: 8px 16px;" +
            "-fx-cursor: hand;"
        ));
    }

    public static Button btnSecondary(String text, double w, double h) {
        Button btn = new Button(text);
        btn.setPrefSize(w, h);
        btn.setFont(fontMedium(13));
        btn.setStyle(
            "-fx-background-color: " + SURFACE_HOVER + ";" +
            "-fx-text-fill: " + TEXT_PRIMARY + ";" +
            "-fx-border-color: " + BORDER + ";" +
            "-fx-border-radius: 0;" +
            "-fx-background-radius: 0;" +
            "-fx-cursor: hand;"
        );
        btn.setOnMouseEntered(e -> btn.setStyle(
            "-fx-background-color: " + BORDER + ";" +
            "-fx-text-fill: #FFFFFF;" +
            "-fx-border-color: " + BORDER_LIGHT + ";" +
            "-fx-border-radius: 0;" +
            "-fx-background-radius: 0;" +
            "-fx-cursor: hand;"
        ));
        btn.setOnMouseExited(e -> btn.setStyle(
            "-fx-background-color: " + SURFACE_HOVER + ";" +
            "-fx-text-fill: " + TEXT_PRIMARY + ";" +
            "-fx-border-color: " + BORDER + ";" +
            "-fx-border-radius: 0;" +
            "-fx-background-radius: 0;" +
            "-fx-cursor: hand;"
        ));
        return btn;
    }

    /** Nút cảnh báo / Thoát / Đăng xuất (Danger Action) - hình vuông không bo tròn */
    public static void styleDangerButton(Button btn) {
        btn.setFont(fontMedium(13));
        btn.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: " + DANGER + ";" +
            "-fx-border-color: " + DANGER + ";" +
            "-fx-border-radius: 0;" +
            "-fx-background-radius: 0;" +
            "-fx-padding: 8px 16px;" +
            "-fx-cursor: hand;"
        );
        btn.setOnMouseEntered(e -> btn.setStyle(
            "-fx-background-color: " + DANGER + ";" +
            "-fx-text-fill: #FFFFFF;" +
            "-fx-border-color: " + DANGER + ";" +
            "-fx-border-radius: 0;" +
            "-fx-background-radius: 0;" +
            "-fx-padding: 8px 16px;" +
            "-fx-cursor: hand;"
        ));
        btn.setOnMouseExited(e -> btn.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: " + DANGER + ";" +
            "-fx-border-color: " + DANGER + ";" +
            "-fx-border-radius: 0;" +
            "-fx-background-radius: 0;" +
            "-fx-padding: 8px 16px;" +
            "-fx-cursor: hand;"
        ));
    }

    /** Nút cảnh báo / Thoát / Đăng xuất (Danger Action) - hình vuông không bo tròn */
    public static Button btnDanger(String text, double w, double h) {
        Button btn = new Button(text);
        btn.setPrefSize(w, h);
        btn.setFont(fontMedium(13));
        btn.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: " + DANGER + ";" +
            "-fx-border-color: " + DANGER + ";" +
            "-fx-border-radius: 0;" +
            "-fx-background-radius: 0;" +
            "-fx-cursor: hand;"
        );
        btn.setOnMouseEntered(e -> btn.setStyle(
            "-fx-background-color: " + DANGER + ";" +
            "-fx-text-fill: #FFFFFF;" +
            "-fx-border-color: " + DANGER + ";" +
            "-fx-border-radius: 0;" +
            "-fx-background-radius: 0;" +
            "-fx-cursor: hand;"
        ));
        btn.setOnMouseExited(e -> btn.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: " + DANGER + ";" +
            "-fx-border-color: " + DANGER + ";" +
            "-fx-border-radius: 0;" +
            "-fx-background-radius: 0;" +
            "-fx-cursor: hand;"
        ));
        return btn;
    }

    /** Style ô nhập liệu TextInputControl - hình vuông không bo tròn */
    public static void styleTextInput(TextInputControl input) {
        input.setFont(fontRegular(14));
        input.setStyle(
            "-fx-background-color: " + SURFACE_INPUT + ";" +
            "-fx-text-fill: " + TEXT_PRIMARY + ";" +
            "-fx-prompt-text-fill: " + TEXT_FAINT + ";" +
            "-fx-background-radius: 0;" +
            "-fx-border-color: " + BORDER + ";" +
            "-fx-border-radius: 0;" +
            "-fx-border-width: 1.0px;" +
            "-fx-padding: 10px 14px;"
        );
        input.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                input.setStyle(
                    "-fx-background-color: " + SURFACE_INPUT + ";" +
                    "-fx-text-fill: " + TEXT_PRIMARY + ";" +
                    "-fx-prompt-text-fill: " + TEXT_FAINT + ";" +
                    "-fx-background-radius: 0;" +
                    "-fx-border-color: " + ACCENT_BRASS + ";" +
                    "-fx-border-radius: 0;" +
                    "-fx-border-width: 1.0px;" +
                    "-fx-padding: 10px 14px;"
                );
            } else {
                input.setStyle(
                    "-fx-background-color: " + SURFACE_INPUT + ";" +
                    "-fx-text-fill: " + TEXT_PRIMARY + ";" +
                    "-fx-prompt-text-fill: " + TEXT_FAINT + ";" +
                    "-fx-background-radius: 0;" +
                    "-fx-border-color: " + BORDER + ";" +
                    "-fx-border-radius: 0;" +
                    "-fx-border-width: 1.0px;" +
                    "-fx-padding: 10px 14px;"
                );
            }
        });
    }

    /** Style ScrollPane phẳng, trong suốt */
    public static void styleScrollPane(ScrollPane sp) {
        sp.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-background: transparent;" +
            "-fx-border-color: transparent;" +
            "-fx-padding: 0;"
        );
    }
}
