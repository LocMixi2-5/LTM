package com.billiards.client.game;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.Stop;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Quản lý toàn bộ hệ thống Skin Gậy Bida:
 * - Định nghĩa 5 mốc cấp độ và 4 loài bọ sừng quý hiếm.
 * - Khối hình 3D thon dần khí động học (Tapered 3D Cylinder), chất liệu chân thực (sơn bóng gương, khảm xà cừ, hổ phách, vàng 24K).
 * - Hiệu ứng phân tầng rõ rệt: Level càng cao, hào quang (Aura), hạt năng lượng (Particles) và cú đánh (Strike VFX) càng lộng lẫy và hoành tráng.
 * - Vệt đuôi bi (Ball Trail) và vòng cổ ngữ hoàng gia (Royal Runes) cho các cấp cao.
 */
public class CueSkinManager {

    public static final String ID_DEFAULT  = "cue_default";
    public static final String ID_KABUTO   = "cue_kabuto";
    public static final String ID_ATLAS    = "cue_atlas";
    public static final String ID_LUCANUS  = "cue_lucanus";
    public static final String ID_HERCULES = "cue_hercules";

    private static final List<CueSkin> SKINS = new ArrayList<>();
    private static boolean vfxEnabled = true;

    static {
        // Cấp 1: Mộc Nguyên Bản [CỔ ĐIỂN]
        SKINS.add(new CueSkin(
            ID_DEFAULT,
            "Mộc Nguyên Bản",
            "Cơ Bida Gỗ Phong Tự Nhiên",
            1,
            "Gỗ phong mộc mạc • Vòng nẹp đồng cổ điển",
            "Cây gậy khởi đầu kinh điển với thân gỗ phong Bắc Mỹ tự nhiên, tay cầm bọc chỉ đan lanh ấm áp và vòng đệm đồng thau mộc mạc.",
            "#DEB887",
            "#C2A36B",
            true,
            "CỔ ĐIỂN",
            "#A0A8A0",
            "#D2B48C"
        ));

        // Cấp 5: Kabutomushi — Mộc Giáp [QUÝ HIẾM]
        SKINS.add(new CueSkin(
            ID_KABUTO,
            "Kabutomushi — Mộc Giáp",
            "Allomyrina dichotoma",
            5,
            "Sừng chữ Y hai tầng • Gỗ mahogany & Lá ngọc bích",
            "Lấy cảm hứng từ võ sĩ Kabutomushi. Chất liệu gỗ đỏ hạt dẻ mahogany ấm áp, chuôi khảm sừng chữ Y hai tầng sắc nét và chiếc lá ngọc bích tỏa sáng thiền tịnh, giải phóng phấn hoa ngọc lục bảo khi đánh.",
            "#87301B",
            "#2ECC71",
            false,
            "QUÝ HIẾM",
            "#2ECC71",
            "#4EFA8B"
        ));

        // Cấp 10: Atlas — Hắc Diệu [SỬ THI]
        SKINS.add(new CueSkin(
            ID_ATLAS,
            "Atlas — Hắc Diệu",
            "Chalcosoma atlas",
            10,
            "Ba sừng dũng mãnh • Hắc diệu thạch & Lôi quang tím",
            "Lấy cảm hứng từ chiến binh ba sừng Atlas. Thân gậy phủ đá hắc diệu thạch obsidian bóng loáng, chuôi khảm huy hiệu ba sừng titan tím than, rãnh năng lượng phóng điện tím sắc sảo.",
            "#1B2633",
            "#9B59B6",
            false,
            "SỬ THI",
            "#9B59B6",
            "#B066FF"
        ));

        // Cấp 15: Lucanus — Xích Giác [HUYỀN THOẠI]
        SKINS.add(new CueSkin(
            ID_LUCANUS,
            "Lucanus — Xích Giác",
            "Lucanus cervus",
            15,
            "Gạc kìm răng cưa • Huyết mộc & Hỏa ngọc ruby",
            "Biểu tượng cặp hàm gạc hươu hùng vĩ. Thân gậy kết hợp huyết mộc đỏ thẫm và gỗ mun quý phái, khảm hoa văn vảy giáp ruby giác cắt kim cương phát quang và tàn lửa ruby rực cháy.",
            "#782929",
            "#E74C3C",
            false,
            "HUYỀN THOẠI",
            "#E74C3C",
            "#FF3838"
        ));

        // Cấp 20: Hercules — Hoàng Phách [THẦN THOẠI]
        SKINS.add(new CueSkin(
            ID_HERCULES,
            "Hercules — Hoàng Phách",
            "Dynastes hercules",
            20,
            "Hoàng đế bọ sừng • Hổ phách hoàng kim 24K & Bụi sao vương giả",
            "Tuyệt phẩm tối thượng của câu lạc bộ. Thân gậy hổ phách hoàng gia trong suốt nạm vàng ròng 24K chạm nổi sừng kìm Hercules, tỏa hào quang thần thánh và mưa bụi sao vàng lộng lẫy bao trùm bàn đấu.",
            "#D1A938",
            "#F1C40F",
            false,
            "THẦN THOẠI",
            "#F1C40F",
            "#FFD700"
        ));
    }

    public static List<CueSkin> getAllSkins() {
        return Collections.unmodifiableList(SKINS);
    }

    public static CueSkin getSkin(String skinIdOrHex) {
        if (skinIdOrHex == null || skinIdOrHex.trim().isEmpty()) {
            return SKINS.get(0);
        }
        String query = skinIdOrHex.trim().toLowerCase();
        for (CueSkin s : SKINS) {
            if (s.id.equalsIgnoreCase(query)) return s;
        }
        return SKINS.get(0);
    }

    public static boolean isVfxEnabled() { return vfxEnabled; }
    public static void setVfxEnabled(boolean enabled) { vfxEnabled = enabled; }

    // ============================================================
    // VẼ CÂY CƠ 3D THỐNG NHẤT (DÙNG CHUNG PREVIEW & IN-GAME)
    // ============================================================
    /**
     * Vẽ cây cơ chân thực hình khối 3D thon dần (Tapered Cylinder), hoa văn khảm,
     * huy hiệu loài bọ tương ứng và hệ thống hiệu ứng phát quang phân tầng theo cấp độ.
     */
    public static void drawCue(GraphicsContext gc,
                               CueSkin skin,
                               double cueBallX, double cueBallY,
                               double nx, double ny,
                               double cueBack,
                               double animTimeSec,
                               boolean isShotStrike,
                               double strikeAgeSec) {
        if (skin == null) skin = SKINS.get(0);

        double totalLen = 245.0;
        double startDist = 12.0 + cueBack;
        double endDist   = startDist + totalLen;

        // Vector lùi lại phía sau bi dọc theo trục gậy
        double dirX = -nx;
        double dirY = -ny;

        // Vector pháp tuyến vuông góc để tạo độ dày 3D và ánh sáng
        double perpX = -ny;
        double perpY = nx;

        // 1. ĐỔ BÓNG MỊN DƯỚI CÂY GẬY (SOFT DROP SHADOW 3D)
        gc.save();
        gc.setStroke(Color.rgb(0, 0, 0, 0.42));
        gc.setLineWidth(9.0);
        gc.strokeLine(
            cueBallX + dirX * (startDist + 12) + perpX * 3.0 + 3.0, cueBallY + dirY * (startDist + 12) + perpY * 3.0 + 3.0,
            cueBallX + dirX * (endDist)        + perpX * 4.0 + 4.0, cueBallY + dirY * (endDist)        + perpY * 4.0 + 4.0
        );
        gc.restore();

        // 2. HÀO QUANG TOÀN THÂN CHO SKIN CẤP CAO (AMBIENT AURA)
        if (vfxEnabled) {
            drawCueAmbientAura(gc, skin, cueBallX, cueBallY, dirX, dirY, perpX, perpY, startDist, endDist, animTimeSec);
        }

        // CÁC MỐC KÍCH THƯỚC DỌC THÂN GẬY:
        // d0: Tip              startDist         r = 2.4 (w = 4.8)
        // d1: Ferrule          startDist + 10    r = 2.7 (w = 5.4)
        // d2: Forearm Spliced  startDist + 65    r = 3.6 (w = 7.2)
        // d3: Joint Collar     startDist + 115   r = 4.3 (w = 8.6)
        // d4: Joint End        startDist + 122   r = 4.5 (w = 9.0)
        // d5: Grip Wrap Start  startDist + 122
        // d6: Grip Wrap End    startDist + 175   r = 5.3 (w = 10.6)
        // d7: Butt Sleeve      startDist + 237   r = 6.2 (w = 12.4)
        // d8: Butt Cap         startDist + 242   r = 6.2
        // d9: Rubber Bumper    endDist           r = 5.9 (w = 11.8)

        double dTipEnd       = startDist + 3.5;
        double dFerruleEnd   = startDist + 11.0;
        double dForearmStart = startDist + 65.0;
        double dJointStart   = startDist + 115.0;
        double dJointEnd     = startDist + 122.0;
        double dGripEnd      = startDist + 175.0;
        double dButtEnd      = startDist + 237.0;
        double dCapEnd       = startDist + 242.0;

        // 3. ĐỆM CAO SU GIẢM CHẤN CUỐI CHUÔI (RUBBER BUMPER)
        drawTaperedCylinder(gc, cueBallX, cueBallY, dirX, dirY, perpX, perpY,
            dCapEnd, 6.0, endDist, 5.7,
            Color.web("#141715"), Color.web("#2C3430"), Color.web("#0A0D0B"));

        // 4. NẮP CHUÔI GẬY (BUTT CAP)
        Color capColor = switch (skin.id) {
            case ID_HERCULES -> Color.web("#F1C40F");
            case ID_ATLAS    -> Color.web("#8E44AD");
            case ID_LUCANUS  -> Color.web("#C0392B");
            case ID_KABUTO   -> Color.web("#D35400");
            default          -> Color.web("#C2A36B");
        };
        drawTaperedCylinder(gc, cueBallX, cueBallY, dirX, dirY, perpX, perpY,
            dButtEnd, 6.2, dCapEnd, 6.0,
            capColor, Color.rgb(255, 255, 255, 0.4), Color.rgb(0, 0, 0, 0.5));

        // 5. CHUÔI GẬY VÀ KHẢM HUY HIỆU BỌ SỪNG (BUTT SLEEVE & EMBLEM)
        drawCustomButtSleeve(gc, skin, cueBallX, cueBallY, dirX, dirY, perpX, perpY, dGripEnd, dButtEnd, animTimeSec);

        // 6. TAY CẦM BỌC DA / CHỈ CAO CẤP (GRIP / WRAP)
        drawCustomGripWrap(gc, skin, cueBallX, cueBallY, dirX, dirY, perpX, perpY, dJointEnd, dGripEnd, animTimeSec);

        // 7. VÒNG KHỚP NỐI KIM LOẠI (METALLIC JOINT COLLAR)
        drawTaperedCylinder(gc, cueBallX, cueBallY, dirX, dirY, perpX, perpY,
            dJointStart, 4.3, dJointEnd, 4.5,
            Color.web(skin.accentHex), Color.rgb(255, 255, 255, 0.55), Color.rgb(0, 0, 0, 0.45));
        // Viền chỉ đen trang trí khớp nối
        gc.setStroke(Color.web("#101412"));
        gc.setLineWidth(1.2);
        gc.strokeLine(
            cueBallX + dirX * (dJointStart + 3.5) - perpX * 4.3, cueBallY + dirY * (dJointStart + 3.5) - perpY * 4.3,
            cueBallX + dirX * (dJointStart + 3.5) + perpX * 4.3, cueBallY + dirY * (dJointStart + 3.5) + perpY * 4.3
        );

        // 8. THÂN TRƯỚC VÀ NGỌN GẬY (FOREARM & SHAFT)
        drawCustomShaft(gc, skin, cueBallX, cueBallY, dirX, dirY, perpX, perpY, dFerruleEnd, dJointStart, animTimeSec);

        // 9. PHÍP GẬY NGÀ VOI / TITAN (FERRULE)
        Color ferruleBase = skin.id.equals(ID_ATLAS) ? Color.web("#D1D8E0") : Color.web("#FDFBF7");
        drawTaperedCylinder(gc, cueBallX, cueBallY, dirX, dirY, perpX, perpY,
            dTipEnd, 2.5, dFerruleEnd, 2.7,
            ferruleBase, Color.rgb(255, 255, 255, 0.7), Color.rgb(0, 0, 0, 0.25));

        // 10. ĐẦU TẨY XANH LƠ MASTER / DA BÒ NÉN (CUE TIP)
        Color tipColor = switch (skin.id) {
            case ID_HERCULES -> Color.web("#F39C12");
            case ID_LUCANUS  -> Color.web("#C0392B");
            case ID_ATLAS    -> Color.web("#5B2C6F");
            default          -> Color.web("#2980B9");
        };
        drawTaperedCylinder(gc, cueBallX, cueBallY, dirX, dirY, perpX, perpY,
            startDist, 2.3, dTipEnd, 2.5,
            tipColor, Color.rgb(255, 255, 255, 0.4), Color.rgb(0, 0, 0, 0.4));

        // 11. HIỆU ỨNG HẠT LƠ LỬNG QUANH CƠ CHO CẤP CAO (IDLE PARTICLES)
        if (vfxEnabled) {
            drawCueParticles(gc, skin, cueBallX, cueBallY, dirX, dirY, perpX, perpY, startDist, endDist, animTimeSec);
        }

        // 12. HIỆU ỨNG KHI ĐÁNH (STRIKE VFX)
        if (vfxEnabled && isShotStrike && strikeAgeSec >= 0 && strikeAgeSec < 0.52) {
            drawStrikeVFX(gc, skin, cueBallX + dirX * startDist, cueBallY + dirY * startDist, nx, ny, strikeAgeSec);
        }
    }

    // ============================================================
    // VẼ KHỐI TRỤ 3D THON DẦN (TAPERED 3D CYLINDER SHADING)
    // ============================================================
    private static void drawTaperedCylinder(GraphicsContext gc,
                                            double cx, double cy,
                                            double dirX, double dirY,
                                            double perpX, double perpY,
                                            double d1, double r1,
                                            double d2, double r2,
                                            Color baseColor, Color highlightColor, Color shadowColor) {
        // Tọa độ 4 đỉnh đa giác
        double x0 = cx + dirX * d1 + perpX * r1;
        double y0 = cy + dirY * d1 + perpY * r1;
        double x1 = cx + dirX * d2 + perpX * r2;
        double y1 = cy + dirY * d2 + perpY * r2;
        double x2 = cx + dirX * d2 - perpX * r2;
        double y2 = cy + dirY * d2 - perpY * r2;
        double x3 = cx + dirX * d1 - perpX * r1;
        double y3 = cy + dirY * d1 - perpY * r1;

        // 1. Thân chính
        gc.setFill(baseColor);
        gc.fillPolygon(new double[]{x0, x1, x2, x3}, new double[]{y0, y1, y2, y3}, 4);

        // 2. Vệt bóng đổ dưới (Core Shadow)
        double sx0 = cx + dirX * d1 - perpX * (r1 * 0.35);
        double sy0 = cy + dirY * d1 - perpY * (r1 * 0.35);
        double sx1 = cx + dirX * d2 - perpX * (r2 * 0.35);
        double sy1 = cy + dirY * d2 - perpY * (r2 * 0.35);
        gc.setFill(shadowColor);
        gc.fillPolygon(new double[]{sx0, sx1, x2, x3}, new double[]{sy0, sy1, y2, y3}, 4);

        // 3. Vệt sáng phản chiếu mép trên (Specular Highlight)
        double hx0 = cx + dirX * d1 + perpX * (r1 * 0.25);
        double hy0 = cy + dirY * d1 + perpY * (r1 * 0.25);
        double hx1 = cx + dirX * d2 + perpX * (r2 * 0.25);
        double hy1 = cy + dirY * d2 + perpY * (r2 * 0.25);
        double hx2 = cx + dirX * d2 + perpX * (r2 * 0.70);
        double hy2 = cy + dirY * d2 + perpY * (r2 * 0.70);
        double hx3 = cx + dirX * d1 + perpX * (r1 * 0.70);
        double hy3 = cy + dirY * d1 + perpY * (r1 * 0.70);
        gc.setFill(highlightColor);
        gc.fillPolygon(new double[]{hx0, hx1, hx2, hx3}, new double[]{hy0, hy1, hy2, hy3}, 4);
    }

    // ============================================================
    // HÀO QUANG TOÀN THÂN CƠ (AMBIENT AURA)
    // ============================================================
    private static void drawCueAmbientAura(GraphicsContext gc, CueSkin skin,
                                          double cx, double cy,
                                          double dirX, double dirY,
                                          double perpX, double perpY,
                                          double start, double end, double animTimeSec) {
        if (skin.id.equals(ID_DEFAULT)) return; // Mộc nguyên bản không có aura viễn tưởng

        gc.save();
        switch (skin.id) {
            case ID_HERCULES -> {
                // Hào quang Thần Thoại Hoàng Kim (Divine Golden Aura): Tỏa sáng rực rỡ toàn thân gậy
                double pulse = 0.22 + 0.08 * Math.sin(animTimeSec * 3.5);
                gc.setStroke(Color.rgb(255, 215, 0, pulse));
                gc.setLineWidth(16.0);
                gc.strokeLine(cx + dirX * (start + 6), cy + dirY * (start + 6), cx + dirX * end, cy + dirY * end);

                // Dải ánh sáng hoàng gia lướt dọc thân gậy (Imperial Light Sweep)
                double sweep = (Math.sin(animTimeSec * 3.0) + 1.0) * 0.5;
                double sweepDist = start + 30 + sweep * (end - start - 50);
                gc.setStroke(Color.rgb(255, 245, 200, 0.75));
                gc.setLineWidth(8.5);
                gc.strokeLine(cx + dirX * (sweepDist - 12), cy + dirY * (sweepDist - 12),
                              cx + dirX * (sweepDist + 12), cy + dirY * (sweepDist + 12));
            }
            case ID_LUCANUS -> {
                // Hào quang Huyết Mộc Ruby: Nhịp thở đỏ thắm
                double breath = 0.20 + 0.09 * Math.sin(animTimeSec * 4.0);
                gc.setStroke(Color.rgb(255, 40, 40, breath));
                gc.setLineWidth(14.0);
                gc.strokeLine(cx + dirX * (start + 12), cy + dirY * (start + 12), cx + dirX * end, cy + dirY * end);
            }
            case ID_ATLAS -> {
                // Hào quang Tím Hắc Diệu Thạch: Lôi điện tím sắc lạnh
                double pulse = 0.18 + 0.07 * Math.sin(animTimeSec * 2.8);
                gc.setStroke(Color.rgb(160, 80, 240, pulse));
                gc.setLineWidth(13.0);
                gc.strokeLine(cx + dirX * (start + 18), cy + dirY * (start + 18), cx + dirX * end, cy + dirY * end);
            }
            case ID_KABUTO -> {
                // Hào quang Ngọc Lục Bảo & Hổ Phách dịu dàng
                double breath = 0.14 + 0.05 * Math.sin(animTimeSec * 2.2);
                gc.setStroke(Color.rgb(46, 204, 113, breath));
                gc.setLineWidth(11.0);
                gc.strokeLine(cx + dirX * (start + 30), cy + dirY * (start + 30), cx + dirX * end, cy + dirY * end);
            }
        }
        gc.restore();
    }

    // ============================================================
    // CHUÔI GẬY & KHẢM HUY HIỆU BỌ SỪNG CHI TIẾT
    // ============================================================
    private static void drawCustomButtSleeve(GraphicsContext gc, CueSkin skin,
                                             double cx, double cy,
                                             double dirX, double dirY,
                                             double perpX, double perpY,
                                             double start, double end, double animTimeSec) {
        Color mainBg = switch (skin.id) {
            case ID_HERCULES -> Color.web("#805A18"); // Hổ phách hoàng gia
            case ID_LUCANUS  -> Color.web("#421010"); // Huyết mộc sâu
            case ID_ATLAS    -> Color.web("#0F1722"); // Obsidian đen tím
            case ID_KABUTO   -> Color.web("#541E0F"); // Mahogany đỏ hạt dẻ
            default          -> Color.web("#C89D66"); // Gỗ phong mộc
        };

        // Vẽ thân chuôi 3D
        drawTaperedCylinder(gc, cx, cy, dirX, dirY, perpX, perpY,
            start, 5.3, end, 6.2,
            mainBg, Color.rgb(255, 255, 255, 0.35), Color.rgb(0, 0, 0, 0.5));

        double midDist = (start + end) * 0.5;
        double emblemX = cx + dirX * midDist;
        double emblemY = cy + dirY * midDist;

        switch (skin.id) {
            case ID_HERCULES -> {
                // Khảm Hổ Phách Hoàng Kim với bọt khí & đốm đen maculae huyền thoại
                double inlayStart = midDist - 22;
                double inlayEnd   = midDist + 22;
                drawTaperedCylinder(gc, cx, cy, dirX, dirY, perpX, perpY,
                    inlayStart, 5.6, inlayEnd, 6.0,
                    Color.web("#E5B83B"), Color.rgb(255, 248, 220, 0.65), Color.web("#7A5410"));

                // Các đốm đen hổ phách maculae tự nhiên
                gc.setFill(Color.web("#140E06", 0.95));
                gc.fillOval(emblemX + perpX * 2.8 - 1.4, emblemY + perpY * 2.8 - 1.4, 2.8, 2.8);
                gc.fillOval(emblemX - perpX * 2.4 - 1.2, emblemY - perpY * 2.4 - 1.2, 2.4, 2.4);
                gc.fillOval(emblemX + dirX * 10 - 1.0,   emblemY + dirY * 10 - 1.0,   2.0, 2.0);

                // Phù điêu Sừng Hercules 24K khảm nổi oai vệ
                gc.setStroke(Color.web("#FFEAA7"));
                gc.setLineWidth(2.2);
                gc.strokeLine(emblemX - dirX * 12, emblemY - dirY * 12, emblemX + dirX * 12, emblemY + dirY * 12);
                // Răng cưa sừng kìm hoàng gia
                gc.setLineWidth(1.4);
                gc.strokeLine(emblemX - dirX * 2, emblemY - dirY * 2, emblemX - dirX * 2 + perpX * 2.4, emblemY - dirY * 2 + perpY * 2.4);
                gc.strokeLine(emblemX + dirX * 4, emblemY + dirY * 4, emblemX + dirX * 4 + perpX * 2.2, emblemY + dirY * 4 + perpY * 2.2);

                // Viền nhẫn vàng 24K ở hai đầu khảm
                gc.setStroke(Color.web("#F1C40F"));
                gc.setLineWidth(2.0);
                gc.strokeLine(cx + dirX * inlayStart - perpX * 5.6, cy + dirY * inlayStart - perpY * 5.6,
                              cx + dirX * inlayStart + perpX * 5.6, cy + dirY * inlayStart + perpY * 5.6);
                gc.strokeLine(cx + dirX * inlayEnd - perpX * 6.0,   cy + dirY * inlayEnd - perpY * 6.0,
                              cx + dirX * inlayEnd + perpX * 6.0,   cy + dirY * inlayEnd + perpY * 6.0);
            }
            case ID_LUCANUS -> {
                // Khảm Huyết Ngọc Ruby giác cắt kim cương
                double inlayStart = midDist - 20;
                double inlayEnd   = midDist + 20;
                drawTaperedCylinder(gc, cx, cy, dirX, dirY, perpX, perpY,
                    inlayStart, 5.6, inlayEnd, 6.0,
                    Color.web("#8E1515"), Color.rgb(255, 120, 120, 0.55), Color.web("#3A0808"));

                // Huy hiệu Cặp gạc hươu Lucanus mạ vàng hồng
                gc.setStroke(Color.web("#FF7675"));
                gc.setLineWidth(2.0);
                gc.strokeLine(emblemX, emblemY, emblemX + dirX * 10 + perpX * 3.8, emblemY + dirY * 10 + perpY * 3.8);
                gc.strokeLine(emblemX, emblemY, emblemX + dirX * 10 - perpX * 3.8, emblemY + dirY * 10 - perpY * 3.8);
                // Các nhánh răng cưa sắc nhọn
                gc.setLineWidth(1.2);
                gc.strokeLine(emblemX + dirX * 5 + perpX * 1.9, emblemY + dirY * 5 + perpY * 1.9,
                              emblemX + dirX * 7 + perpX * 4.2, emblemY + dirY * 7 + perpY * 4.2);
                gc.strokeLine(emblemX + dirX * 5 - perpX * 1.9, emblemY + dirY * 5 - perpY * 1.9,
                              emblemX + dirX * 7 - perpX * 4.2, emblemY + dirY * 7 - perpY * 4.2);

                // Đính 2 viên ruby phát sáng
                gc.setFill(Color.web("#FF3838"));
                gc.fillOval(emblemX - 1.5, emblemY - 1.5, 3.0, 3.0);
            }
            case ID_ATLAS -> {
                // Khảm Hợp kim Titanium tím bóng mờ
                double inlayStart = midDist - 18;
                double inlayEnd   = midDist + 18;
                drawTaperedCylinder(gc, cx, cy, dirX, dirY, perpX, perpY,
                    inlayStart, 5.6, inlayEnd, 6.0,
                    Color.web("#221733"), Color.rgb(200, 150, 255, 0.5), Color.web("#0A0710"));

                // Huy hiệu 3 Sừng Atlas sắc lạnh
                gc.setStroke(Color.web("#BB6BD9"));
                gc.setLineWidth(1.8);
                gc.strokeLine(emblemX, emblemY, emblemX + dirX * 11, emblemY + dirY * 11); // Sừng chính
                gc.setLineWidth(1.4);
                gc.strokeLine(emblemX, emblemY, emblemX + dirX * 8 + perpX * 4.2, emblemY + dirY * 8 + perpY * 4.2);
                gc.strokeLine(emblemX, emblemY, emblemX + dirX * 8 - perpX * 4.2, emblemY + dirY * 8 - perpY * 4.2);

                // Mắt ngọc tím phát quang
                gc.setFill(Color.web("#E056FD"));
                gc.fillOval(emblemX - 1.2, emblemY - 1.2, 2.4, 2.4);
            }
            case ID_KABUTO -> {
                // Khảm Gỗ Đỏ Khảm Đồng Thau & Lá Ngọc Bích
                double inlayStart = midDist - 18;
                double inlayEnd   = midDist + 18;
                drawTaperedCylinder(gc, cx, cy, dirX, dirY, perpX, perpY,
                    inlayStart, 5.6, inlayEnd, 6.0,
                    Color.web("#6E2310"), Color.rgb(255, 200, 150, 0.45), Color.web("#300E06"));

                // Huy hiệu sừng chữ Y hai tầng
                gc.setStroke(Color.web("#F39C12"));
                gc.setLineWidth(2.0);
                gc.strokeLine(emblemX - dirX * 6, emblemY - dirY * 6, emblemX + dirX * 4, emblemY + dirY * 4);
                gc.strokeLine(emblemX + dirX * 4, emblemY + dirY * 4, emblemX + dirX * 9 + perpX * 3.4, emblemY + dirY * 9 + perpY * 3.4);
                gc.strokeLine(emblemX + dirX * 4, emblemY + dirY * 4, emblemX + dirX * 9 - perpX * 3.4, emblemY + dirY * 9 - perpY * 3.4);

                // Chiếc lá ngọc bích đung đưa thanh tịnh
                double leafSway = vfxEnabled ? Math.sin(animTimeSec * 2.5) * 1.5 : 0;
                gc.setFill(Color.web("#2ECC71"));
                gc.fillOval(emblemX - perpX * 4.0 - 1.8, emblemY - perpY * 4.0 - 1.8 + leafSway, 3.8, 2.8);
            }
            default -> {
                // Mộc: Vòng nẹp đồng thau truyền thống
                gc.setStroke(Color.web("#C2A36B"));
                gc.setLineWidth(2.2);
                gc.strokeLine(cx + dirX * (midDist - 5) - perpX * 5.8, cy + dirY * (midDist - 5) - perpY * 5.8,
                              cx + dirX * (midDist - 5) + perpX * 5.8, cy + dirY * (midDist - 5) + perpY * 5.8);
                gc.strokeLine(cx + dirX * (midDist + 5) - perpX * 6.0, cy + dirY * (midDist + 5) - perpY * 6.0,
                              cx + dirX * (midDist + 5) + perpX * 6.0, cy + dirY * (midDist + 5) + perpY * 6.0);
            }
        }
    }

    // ============================================================
    // TAY CẦM BỌC DA / CHỈ CAO CẤP (GRIP / WRAP)
    // ============================================================
    private static void drawCustomGripWrap(GraphicsContext gc, CueSkin skin,
                                           double cx, double cy,
                                           double dirX, double dirY,
                                           double perpX, double perpY,
                                           double start, double end, double animTimeSec) {
        Color gripBg = switch (skin.id) {
            case ID_HERCULES -> Color.web("#1F160C"); // Da hoàng gia đen tuyền dát bụi vàng
            case ID_LUCANUS  -> Color.web("#1E0A0A"); // Da cá đuối huyết đỏ
            case ID_ATLAS    -> Color.web("#0A0E14"); // Sợi carbon dập vân
            case ID_KABUTO   -> Color.web("#1F0E06"); // Da bò thuộc hạt dẻ
            default          -> Color.web("#2B251F"); // Chỉ đan lanh Ireland
        };

        drawTaperedCylinder(gc, cx, cy, dirX, dirY, perpX, perpY,
            start, 4.5, end, 5.3,
            gripBg, Color.rgb(255, 255, 255, 0.25), Color.rgb(0, 0, 0, 0.6));

        // Họa tiết đan chéo / kim cương / đường khâu tỉ mỉ
        gc.save();
        Color stitch = Color.web(skin.accentHex, 0.55);
        gc.setStroke(stitch);
        gc.setLineWidth(1.0);
        gc.setLineDashes(4, 5);
        gc.strokeLine(
            cx + dirX * start + perpX * 1.5, cy + dirY * start + perpY * 1.5,
            cx + dirX * end   + perpX * 1.5, cy + dirY * end   + perpY * 1.5
        );
        gc.strokeLine(
            cx + dirX * start - perpX * 1.5, cy + dirY * start - perpY * 1.5,
            cx + dirX * end   - perpX * 1.5, cy + dirY * end   - perpY * 1.5
        );
        gc.restore();
    }

    // ============================================================
    // THÂN TRƯỚC VÀ NGỌN GẬY (FOREARM & SHAFT)
    // ============================================================
    private static void drawCustomShaft(GraphicsContext gc, CueSkin skin,
                                        double cx, double cy,
                                        double dirX, double dirY,
                                        double perpX, double perpY,
                                        double start, double end, double animTimeSec) {
        Color shaftWood = switch (skin.id) {
            case ID_HERCULES -> Color.web("#E8C76B"); // Hổ phách hoàng kim sáng bóng
            case ID_LUCANUS  -> Color.web("#802222"); // Huyết mộc vân sâu
            case ID_ATLAS    -> Color.web("#202B38"); // Titan hắc ngọc
            case ID_KABUTO   -> Color.web("#A04D2E"); // Gỗ anh đào đỏ ấm
            default          -> Color.web("#E5CCA8"); // Phong Bắc Mỹ mộc
        };

        drawTaperedCylinder(gc, cx, cy, dirX, dirY, perpX, perpY,
            start, 2.7, end, 4.3,
            shaftWood, Color.rgb(255, 255, 255, 0.45), Color.rgb(0, 0, 0, 0.35));

        // 1. HOA VĂN MŨI NHỌN KHẢM FOREARM (SPLICED INLAYS)
        double spliceStart = end - 45.0;
        double spliceEnd   = end;
        double spliceMid   = (spliceStart + spliceEnd) * 0.5;

        Color inlayColor = Color.web(skin.accentHex);
        gc.setFill(inlayColor);

        // Mũi nhọn khảm phía trên
        double[] ipx = {
            cx + dirX * spliceStart,
            cx + dirX * spliceEnd + perpX * 2.8,
            cx + dirX * spliceEnd - perpX * 2.8
        };
        double[] ipy = {
            cy + dirY * spliceStart,
            cy + dirY * spliceEnd + perpY * 2.8,
            cy + dirY * spliceEnd - perpY * 2.8
        };
        gc.fillPolygon(ipx, ipy, 3);

        // 2. RÃNH DẪN NĂNG LƯỢNG / DẢI SÁNG ĐẶC BIỆT THEO SKIN
        if (vfxEnabled) {
            switch (skin.id) {
                case ID_HERCULES -> {
                    // Dòng chảy năng lượng vàng 24K lấp lánh dọc thân ngọn gậy
                    double pulsePos = (animTimeSec * 1.8) % 1.0;
                    double flareDist = start + pulsePos * (end - start);
                    gc.setStroke(Color.rgb(255, 250, 210, 0.85));
                    gc.setLineWidth(1.8);
                    gc.strokeLine(cx + dirX * (flareDist - 8), cy + dirY * (flareDist - 8),
                                  cx + dirX * (flareDist + 8), cy + dirY * (flareDist + 8));
                }
                case ID_ATLAS -> {
                    // 3 Rãnh dẫn năng lượng plasma tím chạy dọc thân gậy
                    double pulsePos = (animTimeSec * 2.2) % 1.0;
                    double sparkDist = start + pulsePos * (end - start);
                    gc.setStroke(Color.rgb(187, 107, 255, 0.8));
                    gc.setLineWidth(1.4);
                    gc.strokeLine(cx + dirX * (sparkDist - 6), cy + dirY * (sparkDist - 6),
                                  cx + dirX * (sparkDist + 6), cy + dirY * (sparkDist + 6));
                }
                case ID_LUCANUS -> {
                    // Vệt nhiệt đỏ ruby bốc cháy dịu êm
                    double pulsePos = (animTimeSec * 1.6 + 0.5) % 1.0;
                    double emberDist = start + pulsePos * (end - start);
                    gc.setStroke(Color.rgb(255, 80, 80, 0.75));
                    gc.setLineWidth(1.5);
                    gc.strokeLine(cx + dirX * (emberDist - 7), cy + dirY * (emberDist - 7),
                                  cx + dirX * (emberDist + 7), cy + dirY * (emberDist + 7));
                }
                case ID_KABUTO -> {
                    // Vệt sáng ngọc lục bảo mơn man thớ gỗ
                    double pulsePos = (animTimeSec * 1.4) % 1.0;
                    double jadeDist = start + pulsePos * (end - start);
                    gc.setStroke(Color.rgb(80, 255, 140, 0.65));
                    gc.setLineWidth(1.2);
                    gc.strokeLine(cx + dirX * (jadeDist - 5), cy + dirY * (jadeDist - 5),
                                  cx + dirX * (jadeDist + 5), cy + dirY * (jadeDist + 5));
                }
            }
        }
    }

    // ============================================================
    // HIỆU ỨNG HẠT LƠ LỬNG QUANH GẬY (IDLE PARTICLES)
    // ============================================================
    private static void drawCueParticles(GraphicsContext gc, CueSkin skin,
                                         double cx, double cy,
                                         double dirX, double dirY,
                                         double perpX, double perpY,
                                         double start, double end, double animTimeSec) {
        if (skin.id.equals(ID_DEFAULT)) return;

        gc.save();
        switch (skin.id) {
            case ID_HERCULES -> {
                // Vòng hạt bụi sao vàng hoàng gia xoay 3D quanh trục gậy
                for (int i = 0; i < 8; i++) {
                    double angle = animTimeSec * 2.8 + i * (Math.PI / 4.0);
                    double distRatio = 0.35 + (i * 0.08);
                    double particleDist = start + distRatio * (end - start);
                    double orbitRadius = 9.0 + Math.sin(animTimeSec * 2.0 + i) * 3.0;
                    double offPerp = Math.cos(angle) * orbitRadius;
                    double alpha = 0.45 + 0.45 * Math.sin(angle);

                    if (alpha > 0.1) {
                        double px = cx + dirX * particleDist + perpX * offPerp;
                        double py = cy + dirY * particleDist + perpY * offPerp;
                        gc.setFill(Color.rgb(255, 230, 100, alpha));
                        gc.fillOval(px - 1.6, py - 1.6, 3.2, 3.2);

                        // Ngôi sao 4 cánh lấp lánh nhỏ
                        if (i % 2 == 0) {
                            gc.setStroke(Color.rgb(255, 255, 255, alpha * 0.8));
                            gc.setLineWidth(0.8);
                            gc.strokeLine(px - 3, py, px + 3, py);
                            gc.strokeLine(px, py - 3, px, py + 3);
                        }
                    }
                }
            }
            case ID_LUCANUS -> {
                // Tàn lửa đỏ ruby bốc lên từ thân gậy
                for (int i = 0; i < 6; i++) {
                    double t = (animTimeSec * 1.5 + i * 0.35) % 1.0;
                    double particleDist = start + 80.0 + (i * 20.0);
                    double drift = t * 14.0;
                    double alpha = (1.0 - t) * 0.7;

                    double px = cx + dirX * particleDist - perpX * (drift + 3.0);
                    double py = cy + dirY * particleDist - perpY * (drift + 3.0);
                    gc.setFill(Color.rgb(255, 50 + (int)(t * 100), 50, alpha));
                    gc.fillOval(px - 1.3, py - 1.3, 2.6, 2.6);
                }
            }
            case ID_ATLAS -> {
                // Tinh thể hắc ngọc tím lơ lửng quanh chuôi gậy
                for (int i = 0; i < 5; i++) {
                    double angle = animTimeSec * 2.0 + i * 1.25;
                    double px = cx + dirX * (end - 35) + Math.cos(angle) * 12.0;
                    double py = cy + dirY * (end - 35) + Math.sin(angle) * 12.0;
                    gc.setFill(Color.rgb(180, 100, 255, 0.65));
                    gc.fillRect(px - 1.2, py - 1.2, 2.4, 2.4);
                }
            }
            case ID_KABUTO -> {
                // Hạt phấn hoa ngọc lục bảo bay dịu nhẹ
                for (int i = 0; i < 4; i++) {
                    double t = (animTimeSec * 1.0 + i * 0.4) % 1.0;
                    double px = cx + dirX * (start + 60 + i * 30) + Math.sin(animTimeSec * 2.0 + i) * 8.0;
                    double py = cy + dirY * (start + 60 + i * 30) - t * 10.0;
                    gc.setFill(Color.rgb(78, 250, 139, (1.0 - t) * 0.6));
                    gc.fillOval(px - 1.2, py - 1.2, 2.4, 2.4);
                }
            }
        }
        gc.restore();
    }

    // ============================================================
    // HÀO QUANG ĐIỂM NGẮM & BI CÁI (AIM AURA & RUNE RING)
    // ============================================================
    /**
     * Vẽ hào quang và ma pháp trận / vòng cổ ngữ hoàng gia quanh bi cái khi đang ngắm
     */
    public static void drawAimAura(GraphicsContext gc, CueSkin skin,
                                   double ballX, double ballY,
                                   double nx, double ny,
                                   double animTimeSec) {
        if (!vfxEnabled || skin == null || skin.id.equals(ID_DEFAULT)) return;

        gc.save();
        switch (skin.id) {
            case ID_HERCULES -> {
                // Vòng cổ ngữ hoàng gia xoay tròn quanh bi cái
                double rotAngle = animTimeSec * 45.0; // Quay 45 độ mỗi giây
                double pulse = 0.40 + 0.15 * Math.sin(animTimeSec * 3.5);
                gc.translate(ballX, ballY);
                gc.rotate(rotAngle);

                // Vòng tròn ánh sáng 24K
                gc.setStroke(Color.rgb(255, 215, 0, pulse));
                gc.setLineWidth(1.4);
                gc.strokeOval(-16, -16, 32, 32);

                // 4 Cổ ngữ / Ký hiệu vương giả tại 4 góc
                gc.setStroke(Color.rgb(255, 240, 180, pulse * 1.2));
                gc.setLineWidth(1.6);
                for (int i = 0; i < 4; i++) {
                    gc.rotate(90);
                    gc.strokeLine(13, 0, 17, 0);
                    gc.strokeLine(15, -2, 15, 2);
                }

                // Hạt bụi sao vàng óng ánh ở tâm bi
                gc.setFill(Color.rgb(255, 255, 200, 0.8));
                gc.fillOval(-1.8, -1.8, 3.6, 3.6);
            }
            case ID_LUCANUS -> {
                // Vầng nhiệt đỏ ruby thở theo nhịp tim
                double pulse = 0.35 + 0.15 * Math.sin(animTimeSec * 4.0);
                gc.setStroke(Color.rgb(255, 40, 40, pulse));
                gc.setLineWidth(1.6);
                gc.strokeOval(ballX - 15, ballY - 15, 30, 30);
                // Tia lửa nhỏ ở tâm
                gc.setFill(Color.rgb(255, 100, 100, 0.7));
                gc.fillOval(ballX - 1.5, ballY - 1.5, 3.0, 3.0);
            }
            case ID_ATLAS -> {
                // Vòng năng lượng tím điện xung kích
                double pulse = 0.30 + 0.12 * Math.sin(animTimeSec * 3.0);
                gc.setStroke(Color.rgb(175, 82, 222, pulse));
                gc.setLineWidth(1.4);
                gc.strokeOval(ballX - 14, ballY - 14, 28, 28);
            }
            case ID_KABUTO -> {
                // 3 Hạt phấn hoa ngọc bích xoay nhẹ quanh bi
                for (int i = 0; i < 3; i++) {
                    double angle = animTimeSec * 2.2 + i * (2.0 * Math.PI / 3.0);
                    double px = ballX + Math.cos(angle) * 14.5;
                    double py = ballY + Math.sin(angle) * 14.5;
                    gc.setFill(Color.rgb(46, 204, 113, 0.65));
                    gc.fillOval(px - 1.4, py - 1.4, 2.8, 2.8);
                }
            }
        }
        gc.restore();
    }

    // ============================================================
    // HIỆU ỨNG CÚ ĐÁNH BÙNG NỔ (EPIC STRIKE VFX)
    // ============================================================
    /**
     * Bùng nổ hiệu ứng siêu cấp tại điểm chạm bi, phân tầng sức mạnh theo cấp độ gậy.
     * Kéo dài ~0.48s với ease-out mượt mà, lens flare, sóng xung kích và chùm hạt nguyên tố.
     */
    public static void drawStrikeVFX(GraphicsContext gc, CueSkin skin,
                                     double tipX, double tipY,
                                     double dirX, double dirY,
                                     double ageSec) {
        if (skin == null) skin = SKINS.get(0);

        double duration = 0.48; // Thời lượng cú đánh hoành tráng
        double t = Math.min(1.0, Math.max(0.0, ageSec / duration));
        double ease = 1.0 - Math.pow(1.0 - t, 3.0); // Ease-out tự nhiên
        double alpha = Math.max(0.0, 1.0 - t);

        gc.save();

        switch (skin.id) {
            case ID_HERCULES -> {
                // ====================================================
                // LEVEL 20: SIÊU TÂN TINH HOÀNG KIM (GOLDEN SUPERNOVA)
                // ====================================================
                // 1. Sóng xung kích kép Hoàng Kim 24K
                double rOuter = 16.0 + ease * 68.0;
                gc.setStroke(Color.rgb(255, 215, 0, alpha * 0.95));
                gc.setLineWidth(3.0 * alpha);
                gc.strokeOval(tipX - rOuter, tipY - rOuter, rOuter * 2, rOuter * 2);

                double rInner = 10.0 + ease * 42.0;
                gc.setStroke(Color.rgb(255, 245, 180, alpha * 0.85));
                gc.setLineWidth(2.0 * alpha);
                gc.strokeOval(tipX - rInner, tipY - rInner, rInner * 2, rInner * 2);

                // 2. Tia sáng Lens Flare Anamorphic hình sao 4 cánh vươn dài
                double flareLen = 35.0 + ease * 65.0;
                gc.setStroke(Color.rgb(255, 255, 230, alpha * 0.90));
                gc.setLineWidth(2.2 * alpha);
                gc.strokeLine(tipX - flareLen * dirX, tipY - flareLen * dirY,
                              tipX + flareLen * dirX, tipY + flareLen * dirY);
                // Tia ngang vuông góc
                gc.setLineWidth(1.5 * alpha);
                gc.strokeLine(tipX - flareLen * 0.5 * (-dirY), tipY - flareLen * 0.5 * dirX,
                              tipX + flareLen * 0.5 * (-dirY), tipY + flareLen * 0.5 * dirX);

                // 3. Mưa bụi sao vàng 24K & kim cương phát nổ theo hình nón hướng đánh
                for (int i = 0; i < 28; i++) {
                    double spread = (i - 14) * 0.08;
                    double pDist = ease * (45.0 + (i % 6) * 10.0);
                    double px = tipX + (dirX + spread * (-dirY)) * pDist;
                    double py = tipY + (dirY + spread * dirX) * pDist;
                    double pAlpha = alpha * (0.5 + (i % 3) * 0.25);

                    gc.setFill(Color.rgb(255, 220 + (i % 4) * 10, 80 + (i % 3) * 40, pAlpha));
                    gc.fillOval(px - 1.8, py - 1.8, 3.6, 3.6);
                }

                // 4. Lóe sáng tâm trắng chói lòa
                if (t < 0.25) {
                    double coreAlpha = (1.0 - t / 0.25) * 0.95;
                    gc.setFill(Color.rgb(255, 255, 255, coreAlpha));
                    gc.fillOval(tipX - 6.0, tipY - 6.0, 12.0, 12.0);
                }
            }
            case ID_LUCANUS -> {
                // ====================================================
                // LEVEL 15: VẾT CHÉM HUYẾT NGẠC (CRIMSON ANTLER SLASH)
                // ====================================================
                // 1. Sóng xung kích đỏ ruby rực lửa
                double r = 14.0 + ease * 58.0;
                gc.setStroke(Color.rgb(255, 30, 30, alpha * 0.90));
                gc.setLineWidth(2.6 * alpha);
                gc.strokeOval(tipX - r, tipY - r, r * 2, r * 2);

                // 2. Hai vệt chém năng lượng hình gạc hươu xé toạc không khí
                double slashLen = 22.0 + ease * 45.0;
                gc.setStroke(Color.rgb(255, 120, 120, alpha * 0.85));
                gc.setLineWidth(2.2 * alpha);
                gc.strokeLine(tipX, tipY, tipX + (dirX * 0.8 - dirY * 0.6) * slashLen, tipY + (dirY * 0.8 + dirX * 0.6) * slashLen);
                gc.strokeLine(tipX, tipY, tipX + (dirX * 0.8 + dirY * 0.6) * slashLen, tipY + (dirY * 0.8 - dirX * 0.6) * slashLen);

                // 3. 20 Tàn lửa đỏ ruby bùng nổ
                for (int i = 0; i < 20; i++) {
                    double spread = (i - 10) * 0.10;
                    double pDist = ease * (35.0 + (i % 5) * 8.0);
                    double px = tipX + (dirX + spread * (-dirY)) * pDist;
                    double py = tipY + (dirY + spread * dirX) * pDist;
                    gc.setFill(Color.rgb(255, 50, 50, alpha * 0.8));
                    gc.fillOval(px - 1.5, py - 1.5, 3.0, 3.0);
                }

                // 4. Tâm đỏ rực chói
                if (t < 0.22) {
                    gc.setFill(Color.rgb(255, 220, 220, (1.0 - t / 0.22) * 0.9));
                    gc.fillOval(tipX - 4.5, tipY - 4.5, 9.0, 9.0);
                }
            }
            case ID_ATLAS -> {
                // ====================================================
                // LEVEL 10: XUNG KÍCH HẮC DIỆU (OBSIDIAN PLASMA BLAST)
                // ====================================================
                // 1. Sóng xung kích tím điện
                double r = 12.0 + ease * 48.0;
                gc.setStroke(Color.rgb(180, 80, 255, alpha * 0.85));
                gc.setLineWidth(2.4 * alpha);
                gc.strokeOval(tipX - r, tipY - r, r * 2, r * 2);

                // 2. 4 Tia sét tím chớp giật
                gc.setStroke(Color.rgb(220, 160, 255, alpha * 0.9));
                gc.setLineWidth(1.6 * alpha);
                double lightningLen = 18.0 + ease * 36.0;
                gc.strokeLine(tipX, tipY, tipX + (dirX + 0.4 * (-dirY)) * lightningLen, tipY + (dirY + 0.4 * dirX) * lightningLen);
                gc.strokeLine(tipX, tipY, tipX + (dirX - 0.4 * (-dirY)) * lightningLen, tipY + (dirY - 0.4 * dirX) * lightningLen);

                // 3. 14 Mảnh vỡ tinh thể hắc ngọc tím
                for (int i = 0; i < 14; i++) {
                    double spread = (i - 7) * 0.12;
                    double pDist = ease * (30.0 + (i % 4) * 8.0);
                    double px = tipX + (dirX + spread * (-dirY)) * pDist;
                    double py = tipY + (dirY + spread * dirX) * pDist;
                    gc.setFill(Color.rgb(190, 110, 255, alpha * 0.8));
                    gc.fillRect(px - 1.4, py - 1.4, 2.8, 2.8);
                }

                if (t < 0.20) {
                    gc.setFill(Color.rgb(255, 255, 255, (1.0 - t / 0.20) * 0.85));
                    gc.fillOval(tipX - 4.0, tipY - 4.0, 8.0, 8.0);
                }
            }
            case ID_KABUTO -> {
                // ====================================================
                // LEVEL 5: CHẤN KÍCH MỘC GIÁP (JADE & AMBER BURST)
                // ====================================================
                // 1. Sóng xung kích hổ phách & ngọc bích
                double r = 10.0 + ease * 38.0;
                gc.setStroke(Color.rgb(230, 126, 34, alpha * 0.80));
                gc.setLineWidth(2.2 * alpha);
                gc.strokeOval(tipX - r, tipY - r, r * 2, r * 2);

                // 2. 12 Hạt phấn hoa ngọc lục bảo bung tỏa
                for (int i = 0; i < 12; i++) {
                    double spread = (i - 6) * 0.14;
                    double pDist = ease * (26.0 + (i % 4) * 6.0);
                    double px = tipX + (dirX + spread * (-dirY)) * pDist;
                    double py = tipY + (dirY + spread * dirX) * pDist;
                    gc.setFill(Color.rgb(46, 204, 113, alpha * 0.85));
                    gc.fillOval(px - 1.6, py - 1.6, 3.2, 3.2);
                }

                if (t < 0.20) {
                    gc.setFill(Color.rgb(255, 230, 180, (1.0 - t / 0.20) * 0.8));
                    gc.fillOval(tipX - 3.5, tipY - 3.5, 7.0, 7.0);
                }
            }
            default -> {
                // ====================================================
                // LEVEL 1: CHẤN ĐỘNG MỘC NGUYÊN BẢN (CLASSIC IMPACT)
                // ====================================================
                double r = 8.0 + ease * 26.0;
                gc.setStroke(Color.rgb(240, 235, 220, alpha * 0.65));
                gc.setLineWidth(1.8 * alpha);
                gc.strokeOval(tipX - r, tipY - r, r * 2, r * 2);

                for (int i = 0; i < 8; i++) {
                    double spread = (i - 4) * 0.16;
                    double pDist = ease * (20.0 + (i % 3) * 5.0);
                    double px = tipX + (dirX + spread * (-dirY)) * pDist;
                    double py = tipY + (dirY + spread * dirX) * pDist;
                    gc.setFill(Color.rgb(222, 184, 135, alpha * 0.7));
                    gc.fillOval(px - 1.2, py - 1.2, 2.4, 2.4);
                }
            }
        }

        gc.restore();
    }

    // ============================================================
    // VẼ KHU VỰC XEM TRƯỚC BỘ SƯU TẬP (COLLECTION PREVIEW STAGE)
    // ============================================================
    /**
     * Trưng bày cây gậy trên giá đỡ cành gỗ cổ thụ, kèm hình minh họa bọ sừng uy nghiêm,
     * hiệu ứng hạt nguyên tố lơ lửng và animation thử cú đánh cực kỳ trực quan.
     */
    public static void renderCollectionPreview(GraphicsContext gc, double w, double h,
                                               CueSkin skin, double animTimeSec,
                                               boolean testStrikeActive, double strikeAgeSec) {
        if (skin == null) skin = SKINS.get(0);

        // 1. Nền phòng trưng bày CLB thượng lưu với ánh sáng spotlight trung tâm
        RadialGradient bgSpotlight = new RadialGradient(
            0, 0, w * 0.5, h * 0.45, w * 0.6, false, CycleMethod.NO_CYCLE,
            new Stop(0.0, Color.web("#182520")),
            new Stop(0.7, Color.web("#0F1714")),
            new Stop(1.0, Color.web("#090E0C"))
        );
        gc.setFill(bgSpotlight);
        gc.fillRect(0, 0, w, h);

        // Khung viền chỉ đồng thanh lịch
        gc.setStroke(Color.web("#26382F"));
        gc.setLineWidth(1.2);
        gc.strokeRect(1, 1, w - 2, h - 2);

        // 2. GIÁ ĐỠ CÀNH GỖ CỔ THỤ ÍT LÁ (RUSTIC WOOD RACK WITH SPARSE LEAVES)
        drawWoodenPreviewRack(gc, w, h, animTimeSec);

        // 3. VẼ CÂY CƠ NẰM NGANG TRÊN GIÁ ĐỠ
        double cueY = h * 0.44;
        double cueStartX = w * 0.12;  // Đầu tẩy bên trái
        // Lực thụt lùi khi thử đánh: Lùi lại trong 0.12s đầu, sau đó lao nhanh về trước
        double cueBack = 0.0;
        if (testStrikeActive) {
            if (strikeAgeSec < 0.12) {
                cueBack = (strikeAgeSec / 0.12) * 35.0; // Lùi đà
            } else if (strikeAgeSec < 0.20) {
                cueBack = 35.0 - ((strikeAgeSec - 0.12) / 0.08) * 35.0; // Lao tới
            } else {
                cueBack = 0.0;
            }
        }

        // nx = -1.0, ny = 0.0 (gậy hướng sang trái, chuôi sang phải)
        drawCue(gc, skin, cueStartX, cueY, -1.0, 0.0, cueBack, animTimeSec, testStrikeActive, strikeAgeSec - 0.15);

        // Khi thử đánh: Bùng nổ Strike VFX ngay tại đầu tẩy của cây gậy trong preview
        if (testStrikeActive && strikeAgeSec >= 0.15 && strikeAgeSec < 0.65) {
            drawStrikeVFX(gc, skin, cueStartX - 4, cueY, -1.0, 0.0, strikeAgeSec - 0.15);
        }

        // 4. HÌNH MINH HỌA BỌ SỪNG QUÝ HIẾM ĐẬU TRÊN KHÚC GỖ (RARE BEETLE EXHIBIT)
        drawBeetleIllustration(gc, skin, w * 0.82, h * 0.72, animTimeSec);
    }

    /**
     * Vẽ cành gỗ cổ thụ ít lá làm giá đỡ gậy cơ trong bản xem trước
     */
    private static void drawWoodenPreviewRack(GraphicsContext gc, double w, double h, double animTimeSec) {
        gc.save();

        // Cành gỗ đỡ bên trái (gốc cành vươn lên đỡ thân gậy)
        gc.setStroke(Color.web("#2A1C12"));
        gc.setLineWidth(6.0);
        gc.beginPath();
        gc.moveTo(w * 0.16, h * 0.75);
        gc.quadraticCurveTo(w * 0.22, h * 0.52, w * 0.28, h * 0.48);
        gc.stroke();

        // Mấu đỡ gậy bằng gỗ
        gc.setStroke(Color.web("#422C1D"));
        gc.setLineWidth(3.0);
        gc.strokeLine(w * 0.27, h * 0.44, w * 0.29, h * 0.50);

        // Chiếc lá ngọc bích điểm xuyết ở mấu cành bên trái
        drawPreviewLeaf(gc, w * 0.22, h * 0.55, -35 + Math.sin(animTimeSec * 2.0) * 3.5, 15);
        drawPreviewLeaf(gc, w * 0.28, h * 0.52, 25 - Math.sin(animTimeSec * 2.2) * 4.0, 14);

        // Cành gỗ đỡ bên phải (nâng đỡ chuôi gậy)
        gc.setStroke(Color.web("#26170E"));
        gc.setLineWidth(8.5);
        gc.beginPath();
        gc.moveTo(w * 0.68, h * 0.88);
        gc.quadraticCurveTo(w * 0.76, h * 0.55, w * 0.84, h * 0.50);
        gc.stroke();

        // Vân gỗ nổi khối trên cành phải
        gc.setStroke(Color.web("#543622", 0.65));
        gc.setLineWidth(2.2);
        gc.strokeLine(w * 0.70, h * 0.84, w * 0.80, h * 0.54);

        // Chiếc lá ngọc bích điểm xuyết ở cành phải
        drawPreviewLeaf(gc, w * 0.72, h * 0.68, -40 + Math.sin(animTimeSec * 1.8 + 1.0) * 3.5, 16);
        drawPreviewLeaf(gc, w * 0.84, h * 0.52, 20 + Math.sin(animTimeSec * 2.1) * 3.0, 15);

        gc.restore();
    }

    private static void drawPreviewLeaf(GraphicsContext gc, double x, double y, double angleDeg, double len) {
        gc.save();
        gc.translate(x, y);
        gc.rotate(angleDeg);

        double w = len * 0.40;
        LinearGradient grad = new LinearGradient(
            0, 0, len, 0, false, CycleMethod.NO_CYCLE,
            new Stop(0.0, Color.web("#183D26")),
            new Stop(0.6, Color.web("#2E6C45")),
            new Stop(1.0, Color.web("#4FA66C"))
        );
        gc.setFill(grad);
        gc.beginPath();
        gc.moveTo(0, 0);
        gc.bezierCurveTo(len * 0.35, -w, len * 0.75, -w * 0.75, len, 0);
        gc.bezierCurveTo(len * 0.75, w * 0.75, len * 0.35, w, 0, 0);
        gc.closePath();
        gc.fill();

        gc.setStroke(Color.web("#68BD84", 0.7));
        gc.setLineWidth(0.7);
        gc.strokeLine(0, 0, len * 0.85, 0);

        gc.restore();
    }

    // ============================================================
    // VẼ HÌNH MINH HỌA BỌ SỪNG SỐNG ĐỘNG
    // ============================================================
    private static void drawBeetleIllustration(GraphicsContext gc, CueSkin skin, double x, double y, double animTimeSec) {
        gc.save();
        gc.translate(x, y);

        // Đổ bóng của bọ
        gc.setFill(Color.rgb(0, 0, 0, 0.45));
        gc.fillOval(-16, -9, 34, 18);

        double hornBob = Math.sin(animTimeSec * 2.2) * 1.8;

        // 6 Chân bám vững chãi
        gc.setStroke(Color.web("#141715"));
        gc.setLineWidth(1.6);
        gc.strokeLine(4, -5, 9, -13);   gc.strokeLine(9, -13, 14, -17);
        gc.strokeLine(0, -5, 1, -14);   gc.strokeLine(1, -14, 4, -18);
        gc.strokeLine(-5, -5, -9, -12); gc.strokeLine(-9, -12, -12, -16);
        gc.strokeLine(4, 5, 9, 13);     gc.strokeLine(9, 13, 14, 17);
        gc.strokeLine(0, 5, 1, 14);     gc.strokeLine(1, 14, 4, 18);
        gc.strokeLine(-5, 5, -9, 12);   gc.strokeLine(-9, 12, -12, 16);

        // Thân cánh (Elytra)
        switch (skin.id) {
            case ID_HERCULES -> {
                // Dynastes hercules: Cánh vàng hổ phách đốm đen huyền thoại
                LinearGradient wingGrad = new LinearGradient(
                    -14, 0, 10, 0, false, CycleMethod.NO_CYCLE,
                    new Stop(0.0, Color.web("#B8860B")),
                    new Stop(0.5, Color.web("#F1C40F")),
                    new Stop(1.0, Color.web("#F9E79F"))
                );
                gc.setFill(wingGrad);
                gc.fillOval(-14, -8.5, 23, 17);

                // Các đốm đen đặc trưng maculae
                gc.setFill(Color.web("#1A140B", 0.95));
                gc.fillOval(-8, -4.5, 2.8, 2.4);
                gc.fillOval(-2, -5.0, 3.0, 2.6);
                gc.fillOval(-6, 2.5, 2.8, 2.4);
                gc.fillOval(2, 1.5, 2.4, 2.2);
            }
            case ID_ATLAS -> {
                // Chalcosoma atlas: Đen nhánh tím than obsidian bóng gương
                LinearGradient wingGrad = new LinearGradient(
                    -14, 0, 10, 0, false, CycleMethod.NO_CYCLE,
                    new Stop(0.0, Color.web("#0B0E14")),
                    new Stop(0.5, Color.web("#1B2633")),
                    new Stop(1.0, Color.web("#34495E"))
                );
                gc.setFill(wingGrad);
                gc.fillOval(-14, -9.0, 23.5, 18);
            }
            case ID_LUCANUS -> {
                // Lucanus cervus: Đỏ ruby mận thẫm quý phái
                LinearGradient wingGrad = new LinearGradient(
                    -14, 0, 10, 0, false, CycleMethod.NO_CYCLE,
                    new Stop(0.0, Color.web("#3A0E0E")),
                    new Stop(0.5, Color.web("#661818")),
                    new Stop(1.0, Color.web("#962D2D"))
                );
                gc.setFill(wingGrad);
                gc.fillOval(-14, -8.5, 23, 17);
            }
            case ID_KABUTO -> {
                // Allomyrina dichotoma: Đỏ hạt dẻ mahogany ấm
                LinearGradient wingGrad = new LinearGradient(
                    -14, 0, 10, 0, false, CycleMethod.NO_CYCLE,
                    new Stop(0.0, Color.web("#3B140A")),
                    new Stop(0.5, Color.web("#662514")),
                    new Stop(1.0, Color.web("#9E3E24"))
                );
                gc.setFill(wingGrad);
                gc.fillOval(-14, -8.8, 23, 17.6);
            }
            default -> {
                // Mộc: Bọ cánh cứng rừng sồi màu đồng mộc
                LinearGradient wingGrad = new LinearGradient(
                    -14, 0, 10, 0, false, CycleMethod.NO_CYCLE,
                    new Stop(0.0, Color.web("#4A3720")),
                    new Stop(0.5, Color.web("#785934")),
                    new Stop(1.0, Color.web("#A88352"))
                );
                gc.setFill(wingGrad);
                gc.fillOval(-14, -8.0, 22, 16);
            }
        }

        gc.setStroke(Color.web("#0D110F"));
        gc.setLineWidth(0.9);
        gc.strokeOval(-14, -8.5, 23, 17);
        gc.strokeLine(-14, 0, 9, 0); // Rãnh chia 2 cánh

        // Ánh bóng specular bóng gương
        gc.setFill(Color.rgb(255, 255, 255, 0.45));
        gc.fillOval(-6, -6.5, 9, 3.2);

        // Ngực & Đầu
        gc.setFill(Color.web("#141816"));
        gc.fillOval(6, -6.0, 7, 12);  // Ngực
        gc.fillOval(10, -4.0, 6, 8);  // Đầu
        gc.setFill(Color.web("#E2E8DE"));
        gc.fillOval(13, -3.2, 1.5, 1.5);
        gc.fillOval(13, 1.8, 1.5, 1.5);

        // Sừng theo từng loài
        gc.save();
        gc.rotate(hornBob);
        switch (skin.id) {
            case ID_HERCULES -> {
                // Cặp kìm sừng dài Hercules oai vệ
                gc.setStroke(Color.web("#111513"));
                gc.setLineWidth(2.8);
                gc.strokeLine(11, -0.6, 32, 0.5); // Sừng ngực dài
                // Lớp lông nhung vàng viền sừng
                gc.setStroke(Color.web("#F1C40F"));
                gc.setLineWidth(1.0);
                gc.strokeLine(16, 0.6, 28, 0.6);
                // Sừng đầu cong lên
                gc.setStroke(Color.web("#141816"));
                gc.setLineWidth(2.2);
                gc.strokeLine(13, 1.4, 27, 0.3);
            }
            case ID_ATLAS -> {
                // 3 Sừng Atlas
                gc.setStroke(Color.web("#111922"));
                gc.setLineWidth(2.4);
                gc.strokeLine(11, -5.0, 25, -3.0);
                gc.strokeLine(11, 5.0, 25, 3.0);
                gc.setStroke(Color.web("#1B2A38"));
                gc.setLineWidth(2.8);
                gc.strokeLine(13, 0, 27, -4.0);
            }
            case ID_KABUTO -> {
                // Sừng chữ Y hai tầng
                gc.setStroke(Color.web("#260C06"));
                gc.setLineWidth(2.8);
                gc.strokeLine(13, 0, 23, 0);
                gc.setLineWidth(2.2);
                gc.strokeLine(23, 0, 29, -4.2);
                gc.strokeLine(23, 0, 29, 4.2);
                gc.setLineWidth(1.4);
                gc.strokeLine(29, -4.2, 32, -5.8);
                gc.strokeLine(29, -4.2, 32, -2.6);
                gc.strokeLine(29, 4.2, 32, 5.8);
                gc.strokeLine(29, 4.2, 32, 2.6);
            }
            case ID_LUCANUS -> {
                // Cặp gạc kìm hươu răng cưa
                gc.setStroke(Color.web("#2B0C0C"));
                gc.setLineWidth(2.4);
                gc.strokeLine(13, -2.8, 27, -1.2);
                gc.strokeLine(13, 2.8, 27, 1.2);
                // Răng cưa
                gc.setLineWidth(1.3);
                gc.strokeLine(19, -2.0, 22, -4.2);
                gc.strokeLine(19, 2.0, 22, 4.2);
            }
            default -> {
                // Sừng ngắn mộc mạc
                gc.setStroke(Color.web("#3A2B18"));
                gc.setLineWidth(2.2);
                gc.strokeLine(13, 0, 20, 0);
            }
        }
        gc.restore();

        gc.restore();
    }
}
