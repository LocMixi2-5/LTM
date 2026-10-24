package com.billiards.client.vfx;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.Stop;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Hiệu ứng thiên nhiên sinh động mang đậm phong cách Modern Billiards Club:
 * 1. Cành cây gỗ cổ thụ khẳng khiu, ít lá điểm xuyết đung đưa êm ái (Rustic Wood Branches with Sparse Leaves).
 * 2. Các loài bọ cánh cứng sừng to quý hiếm (Rare Giant Horned Beetles):
 *    - Dynastes hercules (Bọ sừng Hercules khổng lồ - sừng kẹp dài oai vệ, cánh hổ phách đốm đen)
 *    - Chalcosoma atlas (Bọ ba sừng Atlas đen tuyền ánh kim tím than)
 *    - Allomyrina dichotoma (Bọ tê giác Kabutomushi sừng chữ Y hai tầng, vỏ đỏ mahogany)
 *    - Lucanus cervus (Bọ kẹp kìm sừng hươu khổng lồ)
 */
public class NatureAmbience {

    private final List<InsectBug> bugs = new ArrayList<>();
    private final Random rand = new Random();
    private double lastTimeSec = 0;
    private boolean initialized = false;

    public NatureAmbience() {
        initBugs();
    }

    private void initBugs() {
        bugs.clear();
        // 1. Dynastes hercules (Bọ sừng Hercules hoàng đế - cánh vàng hổ phách đốm đen)
        bugs.add(new InsectBug(130, 160, 0, 0.42));
        // 2. Chalcosoma atlas (Bọ 3 sừng Atlas dũng mãnh - đen ánh kim obsidian)
        bugs.add(new InsectBug(880, 210, 1, 0.46));
        // 3. Allomyrina dichotoma (Bọ tê giác Kabutomushi - sừng chữ Y, vỏ đỏ mahogany)
        bugs.add(new InsectBug(310, 560, 2, 0.40));
        // 4. Lucanus cervus (Bọ kẹp kìm sừng hươu khổng lồ - gạc kìm răng cưa)
        bugs.add(new InsectBug(720, 580, 3, 0.44));
    }

    /**
     * Render hiệu ứng cành gỗ ít lá và các loài bọ sừng to quý hiếm
     * @param inGame true nếu đang trong trận đấu bi-a (bọ né tránh mặt nỉ xanh, bò ở viền ngoài)
     */
    public void render(GraphicsContext gc, double width, double height, double timeSec, boolean inGame) {
        if (!initialized) {
            initialized = true;
            lastTimeSec = timeSec;
        }
        double dt = timeSec - lastTimeSec;
        if (dt < 0 || dt > 0.1) dt = 0.016;
        lastTimeSec = timeSec;

        // 1. VẼ CÀNH CÂY GỖ ÍT LÁ (RUSTIC WOOD BRANCHES WITH SPARSE LEAVES)
        renderWoodenBranches(gc, width, height, timeSec, inGame);

        // 2. CẬP NHẬT & VẼ BỌ SỪNG TO HIẾM (RARE GIANT HORNED BEETLES)
        for (InsectBug bug : bugs) {
            bug.update(dt, width, height, inGame);
            bug.draw(gc, timeSec);
        }
    }

    // ============================================================
    // 1. CÀNH CÂY GỖ ÍT LÁ (RUSTIC WOOD WITH SPARSE LEAVES)
    // ============================================================
    private void renderWoodenBranches(GraphicsContext gc, double width, double height, double timeSec, boolean inGame) {
        gc.save();

        // Cành gỗ chính góc trên - trái (vươn dài, khẳng khiu, ít lá điểm xuyết)
        double swayTL = Math.sin(timeSec * 0.75) * 0.025 + Math.sin(timeSec * 0.4) * 0.015;
        drawRusticWoodBranch(gc, -10, -5, 38 + Math.toDegrees(swayTL), 200, 10.0, timeSec, 0.0);

        // Cành gỗ góc trên - phải (vươn nhẹ sang trái, rủ nhẹ)
        double swayTR = Math.sin(timeSec * 0.85 + 1.8) * 0.022;
        drawRusticWoodBranch(gc, width + 10, -5, 142 + Math.toDegrees(swayTR), 160, 8.5, timeSec, 1.8);

        // Ở sảnh chờ (khi !inGame): Thêm cành gỗ mộc mạc ở góc dưới - phải
        if (!inGame) {
            double swayBR = Math.sin(timeSec * 0.70 + 3.2) * 0.020;
            drawRusticWoodBranch(gc, width + 5, height - 10, 218 + Math.toDegrees(swayBR), 170, 9.0, timeSec, 3.2);
        }

        gc.restore();
    }

    /**
     * Vẽ cành gỗ cổ thụ với vân gỗ, mắt gỗ và một vài chùm lá nhỏ điểm xuyết
     */
    private void drawRusticWoodBranch(GraphicsContext gc, double startX, double startY, double angleDeg,
                                      double length, double baseThick, double timeSec, double phase) {
        gc.save();
        gc.translate(startX, startY);
        gc.rotate(angleDeg);

        double midX = length * 0.52;
        double midY = 16.0;
        double endX = length;
        double endY = 4.0;

        // 1. Thân gỗ chính: Đổ bóng thân gỗ
        gc.setStroke(Color.rgb(0, 0, 0, 0.35));
        gc.setLineWidth(baseThick + 3.0);
        gc.beginPath();
        gc.moveTo(0, 2);
        gc.quadraticCurveTo(midX, midY + 3, endX, endY + 2);
        gc.stroke();

        // 2. Lớp vỏ gỗ màu nâu sẫm ấm cúng (Dark Walnut Wood)
        LinearGradient woodGrad = new LinearGradient(
            0, 0, length, 0, false, CycleMethod.NO_CYCLE,
            new Stop(0.0, Color.web("#26170E")),
            new Stop(0.5, Color.web("#3A2518")),
            new Stop(1.0, Color.web("#1F130B"))
        );
        gc.setStroke(woodGrad);
        gc.setLineWidth(baseThick);
        gc.beginPath();
        gc.moveTo(0, 0);
        gc.quadraticCurveTo(midX, midY, endX, endY);
        gc.stroke();

        // 3. Vân thớ gỗ và sống sáng (Wood Bark Grain Highlights)
        gc.setStroke(Color.web("#543622", 0.65));
        gc.setLineWidth(baseThick * 0.28);
        gc.beginPath();
        gc.moveTo(4, -1);
        gc.quadraticCurveTo(midX - 10, midY - 2, endX * 0.85, endY - 1);
        gc.stroke();

        gc.setStroke(Color.web("#140C07", 0.75));
        gc.setLineWidth(baseThick * 0.22);
        gc.beginPath();
        gc.moveTo(8, 2);
        gc.quadraticCurveTo(midX + 15, midY + 2.5, endX * 0.9, endY + 1.5);
        gc.stroke();

        // Mắt gỗ cổ thụ (Wood knot)
        gc.setFill(Color.web("#160D07"));
        gc.fillOval(midX * 0.45, midY * 0.35, 4.5, 3.2);

        // 4. Nhánh phụ 1 (Side twig 1): Khẳng khiu vươn sang bên trái
        double twig1StartX = length * 0.38;
        double twig1StartY = midY * 0.65;
        double twig1Len = length * 0.42;
        gc.save();
        gc.translate(twig1StartX, twig1StartY);
        gc.rotate(-32 + Math.sin(timeSec * 1.2 + phase) * 2.5);
        gc.setStroke(Color.web("#332014"));
        gc.setLineWidth(baseThick * 0.42);
        gc.beginPath();
        gc.moveTo(0, 0);
        gc.quadraticCurveTo(twig1Len * 0.5, -8, twig1Len, -2);
        gc.stroke();

        // Trên nhánh phụ 1 chỉ điểm xuyết đúng 2 chiếc lá nhỏ thanh thoát
        drawSparseLeaf(gc, twig1Len * 0.55, -5.5, -28 + Math.sin(timeSec * 2.0 + phase) * 4.0, 16);
        drawSparseLeaf(gc, twig1Len, -2, 10 + Math.sin(timeSec * 2.2 + phase + 0.5) * 4.5, 17);
        gc.restore();

        // 5. Nhánh phụ 2 (Side twig 2): Mảnh dẻ rủ sang bên phải
        double twig2StartX = length * 0.72;
        double twig2StartY = midY * 0.85;
        double twig2Len = length * 0.30;
        gc.save();
        gc.translate(twig2StartX, twig2StartY);
        gc.rotate(28 + Math.sin(timeSec * 1.3 + phase + 1.0) * 2.2);
        gc.setStroke(Color.web("#2E1C12"));
        gc.setLineWidth(baseThick * 0.35);
        gc.beginPath();
        gc.moveTo(0, 0);
        gc.quadraticCurveTo(twig2Len * 0.5, 6, twig2Len, 1);
        gc.stroke();

        // Đúng 1 chiếc lá ở đầu nhánh phụ 2
        drawSparseLeaf(gc, twig2Len, 1, 35 - Math.sin(timeSec * 2.1 + phase) * 4.0, 15);
        gc.restore();

        // 6. Đầu cành chính chỉ có đúng 2 chiếc lá nhẹ nhàng (Sparse leaves)
        drawSparseLeaf(gc, endX * 0.88, endY + 1.0, -18 + Math.sin(timeSec * 1.9 + phase + 1.2) * 4.0, 18);
        drawSparseLeaf(gc, endX, endY, 15 + Math.sin(timeSec * 2.3 + phase) * 5.0, 19);

        gc.restore();
    }

    /**
     * Vẽ một chiếc lá ngọc bích đơn lẻ tinh tế (Sparse Emerald Leaf)
     */
    private void drawSparseLeaf(GraphicsContext gc, double x, double y, double leafAngleDeg, double len) {
        gc.save();
        gc.translate(x, y);
        gc.rotate(leafAngleDeg);

        double w = len * 0.38;

        // Cuống lá gỗ mảnh
        gc.setStroke(Color.web("#422A1B"));
        gc.setLineWidth(1.0);
        gc.strokeLine(-3, 0, 0, 0);

        // Thân lá hình giọt nước thanh mảnh: Chuyển sắc lục bảo trầm sang phớt vàng ngọc
        LinearGradient leafGrad = new LinearGradient(
            0, 0, len, 0, false, CycleMethod.NO_CYCLE,
            new Stop(0.0, Color.web("#183D26", 0.92)),
            new Stop(0.65, Color.web("#2B6B44", 0.95)),
            new Stop(1.0, Color.web("#4FA66C", 0.90))
        );

        gc.setFill(leafGrad);
        gc.beginPath();
        gc.moveTo(0, 0);
        gc.bezierCurveTo(len * 0.32, -w, len * 0.75, -w * 0.75, len, 0);
        gc.bezierCurveTo(len * 0.75, w * 0.75, len * 0.32, w, 0, 0);
        gc.closePath();
        gc.fill();

        // Viền lá sắc sảo
        gc.setStroke(Color.web("#143220", 0.6));
        gc.setLineWidth(0.6);
        gc.stroke();

        // Gân lá thanh nhã ở giữa
        gc.setStroke(Color.web("#68BD84", 0.75));
        gc.setLineWidth(0.75);
        gc.strokeLine(0, 0, len * 0.88, 0);

        // Ánh phản chiếu nhẹ trên mặt lá
        gc.setFill(Color.rgb(255, 255, 255, 0.22));
        gc.fillOval(len * 0.25, -w * 0.45, len * 0.35, w * 0.35);

        gc.restore();
    }

    // ============================================================
    // 2. BỌ SỪNG TO HIẾM (RARE GIANT HORNED BEETLES)
    // ============================================================
    private static class InsectBug {
        double x, y;
        double angle;
        double targetAngle;
        double speed;
        int bugType;         // 0: Hercules, 1: Atlas, 2: Kabutomushi, 3: Stag Beetle
        boolean isWalking = true;
        double stateTimer = 0;
        double walkPhase = 0;
        Random rng = new Random();

        InsectBug(double startX, double startY, int type, double spd) {
            this.x = startX;
            this.y = startY;
            this.bugType = type;
            this.speed = spd;
            this.angle = rng.nextDouble() * 360;
            this.targetAngle = this.angle;
            this.stateTimer = 2.5 + rng.nextDouble() * 4.0;
        }

        void update(double dt, double maxW, double maxH, boolean inGame) {
            stateTimer -= dt;
            if (stateTimer <= 0) {
                isWalking = rng.nextDouble() > 0.32;
                stateTimer = isWalking ? (3.0 + rng.nextDouble() * 4.5) : (1.2 + rng.nextDouble() * 2.2);
                if (isWalking) {
                    targetAngle = angle + (rng.nextDouble() * 110 - 55);
                }
            }

            // Xoay dần về góc mục tiêu mượt mà
            double diff = targetAngle - angle;
            while (diff > 180) diff -= 360;
            while (diff < -180) diff += 360;
            angle += diff * Math.min(1.0, dt * 3.2);

            if (isWalking) {
                double rad = Math.toRadians(angle);
                x += Math.cos(rad) * speed * 60 * dt;
                y += Math.sin(rad) * speed * 60 * dt;
                walkPhase += dt * 11.5; // Nhịp bước chân của bọ lớn chậm rãi, oai phong
            }

            // Khi inGame: né tránh khu vực lòng nỉ bàn bi-a để không làm vướng mắt người chơi
            if (inGame) {
                if (x > 210 && x < 850 && y > 160 && y < 520) {
                    targetAngle = (x < 530) ? 180 : 0;
                    angle = targetAngle;
                }
            }

            // Đổi hướng khi chạm mép màn hình
            if (x < 35)  { x = 35;  targetAngle = rng.nextDouble() * 90 - 45; }
            if (x > maxW - 35) { x = maxW - 35; targetAngle = 180 + rng.nextDouble() * 90 - 45; }
            if (y < 35)  { y = 35;  targetAngle = 90 + rng.nextDouble() * 90 - 45; }
            if (y > maxH - 35) { y = maxH - 35; targetAngle = 270 + rng.nextDouble() * 90 - 45; }
        }

        void draw(GraphicsContext gc, double timeSec) {
            gc.save();
            gc.translate(x, y);
            gc.rotate(angle);

            // Bóng đổ dưới thân và sừng con bọ tạo độ nổi khối 3D
            gc.setFill(Color.rgb(0, 0, 0, 0.42));
            gc.fillOval(-11, -6, 26, 12);
            gc.fillOval(10, -3, 14, 6); // Bóng của sừng vươn phía trước

            // Nhịp cử động bước chân (Tripod gait)
            double step = isWalking ? Math.sin(walkPhase) * 4.2 : 0;
            double hornBob = isWalking ? Math.sin(walkPhase) * 1.8 : Math.sin(timeSec * 2.5) * 1.0;

            // 6 CHÂN KHỎE CỦA BỌ CÁNH CỨNG SỪNG TO (Chân có đốt và móng vuốt)
            gc.setStroke(Color.web("#141715"));
            gc.setLineWidth(1.35);

            // Chân trái (3 chân)
            drawJointLeg(gc, 4, -5, 8 + step, -12, 11 + step, -18);
            drawJointLeg(gc, 0, -5.5, 2 - step, -13, 3 - step, -19);
            drawJointLeg(gc, -5, -5, -9 + step, -11, -12 + step, -17);

            // Chân phải (3 chân)
            drawJointLeg(gc, 4, 5, 8 - step, 12, 11 - step, 18);
            drawJointLeg(gc, 0, 5.5, 2 + step, 13, 3 + step, 19);
            drawJointLeg(gc, -5, 5, -9 - step, 11, -12 - step, 17);

            // 1. THÂN VỎ CÁNH CỨNG (ELYTRA)
            drawElytra(gc);

            // 2. NGỰC TRƯỚC (PRONOTUM)
            drawPronotum(gc);

            // 3. ĐẦU & MẮT (HEAD & EYES)
            gc.setFill(Color.web("#111513"));
            gc.fillOval(8, -3.2, 5.0, 6.4);
            // 2 Mắt lồi sắc sảo
            gc.setFill(Color.web("#E2E8DE"));
            gc.fillOval(10.5, -3.0, 1.4, 1.4);
            gc.fillOval(10.5, 1.6, 1.4, 1.4);

            // 4. BỘ SỪNG TO KHỦNG BỐ THEO TỪNG LOÀI HIẾM (GIANT HORNS)
            drawGiantHorns(gc, hornBob, timeSec);

            // 5. CẶP RÂU BỌ RUNG RINH (ANTENNAE)
            double antWiggle = Math.sin(timeSec * 8.0 + x * 0.1) * 2.0;
            gc.setStroke(Color.web("#1C231F"));
            gc.setLineWidth(0.9);
            gc.beginPath();
            gc.moveTo(11, -1.5);
            gc.quadraticCurveTo(15, -4.5 + antWiggle, 18, -6.5 + antWiggle);
            gc.moveTo(11, 1.5);
            gc.quadraticCurveTo(15, 4.5 - antWiggle, 18, 6.5 - antWiggle);
            gc.stroke();

            gc.restore();
        }

        /**
         * Vẽ khớp chân bọ 3 đoạn với móng bám
         */
        private void drawJointLeg(GraphicsContext gc, double x0, double y0, double x1, double y1, double x2, double y2) {
            gc.beginPath();
            gc.moveTo(x0, y0);
            gc.lineTo(x1, y1);
            gc.lineTo(x2, y2);
            gc.stroke();
            // Móng vuốt nhỏ ở đầu chân
            gc.strokeLine(x2, y2, x2 + (x2 > x1 ? 1.5 : -1.5), y2 + (y2 > y1 ? 1.5 : -1.5));
        }

        /**
         * Vẽ vỏ cánh cứng (Elytra) theo từng loài bọ
         */
        private void drawElytra(GraphicsContext gc) {
            switch (bugType) {
                case 0 -> {
                    // Dynastes hercules: Cánh vàng hổ phách ánh kim với các đốm đen huyền ảo
                    LinearGradient herculesWing = new LinearGradient(
                        -12, 0, 7, 0, false, CycleMethod.NO_CYCLE,
                        new Stop(0.0, Color.web("#997321")),
                        new Stop(0.45, Color.web("#D1A938")),
                        new Stop(1.0, Color.web("#E8C65A"))
                    );
                    gc.setFill(herculesWing);
                    gc.fillOval(-11, -6.5, 18.5, 13.0);

                    // Các đốm đen đặc trưng trên cánh bọ Hercules
                    gc.setFill(Color.web("#1A140B", 0.9));
                    gc.fillOval(-6.5, -4.0, 2.4, 2.2);
                    gc.fillOval(-1.5, -4.5, 2.8, 2.4);
                    gc.fillOval(-4.0, -1.2, 2.2, 2.0);
                    gc.fillOval(-6.0, 2.0, 2.6, 2.2);
                    gc.fillOval(-1.0, 2.4, 2.5, 2.3);
                    gc.fillOval(3.0, -2.5, 1.8, 1.8);
                }
                case 1 -> {
                    // Chalcosoma atlas: Đen nhánh ánh kim tím than obsidian (Metallic Obsidian Black)
                    LinearGradient atlasWing = new LinearGradient(
                        -12, 0, 7, 0, false, CycleMethod.NO_CYCLE,
                        new Stop(0.0, Color.web("#0C1014")),
                        new Stop(0.5, Color.web("#1B2633")),
                        new Stop(1.0, Color.web("#283D52"))
                    );
                    gc.setFill(atlasWing);
                    gc.fillOval(-11, -7.0, 19.0, 14.0);
                }
                case 2 -> {
                    // Allomyrina dichotoma (Kabutomushi): Màu nâu đỏ hạt dẻ mahogany bóng lộn
                    LinearGradient kabutoWing = new LinearGradient(
                        -12, 0, 7, 0, false, CycleMethod.NO_CYCLE,
                        new Stop(0.0, Color.web("#361108")),
                        new Stop(0.5, Color.web("#5C1F10")),
                        new Stop(1.0, Color.web("#87301B"))
                    );
                    gc.setFill(kabutoWing);
                    gc.fillOval(-11, -6.8, 18.5, 13.6);
                }
                default -> {
                    // Lucanus cervus: Đỏ cánh gián ruby sẫm bóng bẩy
                    LinearGradient stagWing = new LinearGradient(
                        -12, 0, 7, 0, false, CycleMethod.NO_CYCLE,
                        new Stop(0.0, Color.web("#2B0C0C")),
                        new Stop(0.5, Color.web("#4F1A1A")),
                        new Stop(1.0, Color.web("#782929"))
                    );
                    gc.setFill(stagWing);
                    gc.fillOval(-11, -6.5, 18.5, 13.0);
                }
            }

            // Đường viền thân bọ sắc gọn
            gc.setStroke(Color.web("#0D110F"));
            gc.setLineWidth(0.8);
            gc.strokeOval(-11, -6.5, 18.5, 13.0);

            // Khe rãnh giữa 2 cánh
            gc.setStroke(Color.web("#0A0D0B"));
            gc.setLineWidth(1.0);
            gc.strokeLine(-11, 0, 7.5, 0);

            // Ánh phản chiếu specular bóng lộn sang trọng chạy dọc sống cánh
            gc.setFill(Color.rgb(255, 255, 255, 0.48));
            gc.fillOval(-4, -4.5, 7.5, 2.6);
        }

        /**
         * Ngực trước của bọ (Pronotum)
         */
        private void drawPronotum(GraphicsContext gc) {
            // Giáp ngực đen bóng
            LinearGradient chestGrad = new LinearGradient(
                4, 0, 9, 0, false, CycleMethod.NO_CYCLE,
                new Stop(0.0, Color.web("#141816")),
                new Stop(0.7, Color.web("#222A26")),
                new Stop(1.0, Color.web("#111513"))
            );
            gc.setFill(chestGrad);
            gc.fillOval(5.0, -5.0, 5.5, 10.0);

            // Viền ngực và ánh sáng bóng
            gc.setStroke(Color.web("#0D110F"));
            gc.setLineWidth(0.8);
            gc.strokeOval(5.0, -5.0, 5.5, 10.0);

            gc.setFill(Color.rgb(255, 255, 255, 0.35));
            gc.fillOval(6.0, -3.2, 3.2, 1.6);
        }

        /**
         * Vẽ các cấu trúc sừng khổng lồ độc đáo của 4 loài bọ hiếm
         */
        private void drawGiantHorns(GraphicsContext gc, double hornBob, double timeSec) {
            gc.save();
            gc.rotate(hornBob);

            switch (bugType) {
                case 0 -> {
                    // ==========================================
                    // 1. DYNASTES HERCULES (Bọ sừng Hercules)
                    // Cặp sừng kẹp dài nhất thế giới: Sừng ngực dài thẳng, sừng đầu cong lên
                    // ==========================================
                    // Sừng ngực (Thoracic horn): Vươn dài thẳng tắp, mũi cụp xuống
                    gc.setStroke(Color.web("#111513"));
                    gc.setLineWidth(2.4);
                    gc.beginPath();
                    gc.moveTo(8.5, -0.6);
                    gc.bezierCurveTo(16, -1.2, 24, -1.0, 31, 1.2);
                    gc.stroke();

                    // Răng cưa nhọn bên dưới sừng ngực
                    gc.setFill(Color.web("#0E110F"));
                    gc.fillPolygon(new double[]{16, 17.5, 16.5}, new double[]{-1.0, 1.2, -1.0}, 3);
                    gc.fillPolygon(new double[]{21, 22.5, 21.5}, new double[]{-1.0, 1.0, -1.0}, 3);

                    // Lông tơ nhung vàng đồng mặt dưới sừng
                    gc.setStroke(Color.web("#C2A36B", 0.85));
                    gc.setLineWidth(0.7);
                    gc.strokeLine(13, 0.4, 25, 0.4);

                    // Sừng đầu (Cephalic horn): Bên dưới cong vút lên tạo thành kìm kẹp khổng lồ
                    gc.setStroke(Color.web("#141816"));
                    gc.setLineWidth(2.0);
                    gc.beginPath();
                    gc.moveTo(11, 0.8);
                    gc.bezierCurveTo(17, 3.8, 23, 3.2, 27, 0.5);
                    gc.stroke();

                    // Vệt sáng bóng phản chiếu trên sống sừng
                    gc.setStroke(Color.rgb(255, 255, 255, 0.55));
                    gc.setLineWidth(0.7);
                    gc.strokeLine(12, -0.9, 26, -0.9);
                }
                case 1 -> {
                    // ==========================================
                    // 2. CHALCOSOMA ATLAS (Bọ 3 sừng Atlas)
                    // 2 sừng ngực hai bên uốn cong vĩ đại, 1 sừng đầu ở giữa cong vút lên
                    // ==========================================
                    gc.setStroke(Color.web("#111922"));
                    gc.setLineWidth(2.2);

                    // Sừng ngực trái cong vút sang bên
                    gc.beginPath();
                    gc.moveTo(8.5, -4.0);
                    gc.bezierCurveTo(15, -9.0, 22, -6.5, 25, -2.5);
                    gc.stroke();

                    // Sừng ngực phải cong vút đối xứng
                    gc.beginPath();
                    gc.moveTo(8.5, 4.0);
                    gc.bezierCurveTo(15, 9.0, 22, 6.5, 25, 2.5);
                    gc.stroke();

                    // Sừng đầu ở giữa dài nhọn cong ngược lên
                    gc.setStroke(Color.web("#1B2A38"));
                    gc.setLineWidth(2.4);
                    gc.beginPath();
                    gc.moveTo(11.5, 0);
                    gc.bezierCurveTo(18, 0, 23, -2.5, 26, -4.5);
                    gc.stroke();

                    // Ánh kim tím than sáng loáng trên 2 sừng lớn
                    gc.setStroke(Color.rgb(180, 210, 255, 0.6));
                    gc.setLineWidth(0.7);
                    gc.strokeLine(10, -4.5, 21, -6.5);
                    gc.strokeLine(10, 4.5, 21, 6.5);
                }
                case 2 -> {
                    // ==========================================
                    // 3. ALLOMYRINA DICHOTOMA (Kabutomushi - Bọ tê giác Nhật Bản)
                    // Sừng đầu khổng lồ phân nhánh chữ Y đôi độc đáo
                    // ==========================================
                    // Thân sừng đầu chính vươn dài
                    gc.setStroke(Color.web("#260C06"));
                    gc.setLineWidth(2.6);
                    gc.beginPath();
                    gc.moveTo(11.5, 0);
                    gc.lineTo(21.5, 0);
                    gc.stroke();

                    // Chạc chữ Y phân nhánh tầng 1
                    gc.setLineWidth(2.0);
                    gc.strokeLine(21.5, 0, 26.5, -3.8);
                    gc.strokeLine(21.5, 0, 26.5, 3.8);

                    // Chóp nhánh chữ Y chẻ đôi lần hai (Double-forked tips)
                    gc.setLineWidth(1.3);
                    gc.strokeLine(26.5, -3.8, 29.5, -5.2);
                    gc.strokeLine(26.5, -3.8, 29.2, -2.2);

                    gc.strokeLine(26.5, 3.8, 29.5, 5.2);
                    gc.strokeLine(26.5, 3.8, 29.2, 2.2);

                    // Sừng ngực nhỏ hơn phía trên cũng chẻ đôi chữ Y
                    gc.setStroke(Color.web("#38130A"));
                    gc.setLineWidth(1.8);
                    gc.strokeLine(7.5, 0, 14.5, 0);
                    gc.strokeLine(14.5, 0, 17.5, -2.0);
                    gc.strokeLine(14.5, 0, 17.5, 2.0);

                    // Highlight phản quang đỏ hạt dẻ trên sừng
                    gc.setStroke(Color.web("#BD4C32", 0.7));
                    gc.setLineWidth(0.7);
                    gc.strokeLine(13, -0.6, 21, -0.6);
                }
                default -> {
                    // ==========================================
                    // 4. LUCANUS CERVUS (Bọ kẹp kìm sừng hươu khổng lồ)
                    // Cặp kìm gạc hươu đồ sộ đối xứng với nhiều gai răng cưa
                    // ==========================================
                    gc.setStroke(Color.web("#260B0B"));
                    gc.setLineWidth(2.2);

                    // Gạc kìm trái cong vào trong
                    gc.beginPath();
                    gc.moveTo(11.5, -2.0);
                    gc.bezierCurveTo(18, -6.5, 24, -4.5, 27, -1.0);
                    gc.stroke();

                    // Gạc kìm phải cong đối xứng
                    gc.beginPath();
                    gc.moveTo(11.5, 2.0);
                    gc.bezierCurveTo(18, 6.5, 24, 4.5, 27, 1.0);
                    gc.stroke();

                    // Các răng cưa sắc nhọn bên trong cặp kìm
                    gc.setFill(Color.web("#1E0808"));
                    gc.fillPolygon(new double[]{17, 18.5, 17.5}, new double[]{-3.5, -1.5, -3.5}, 3);
                    gc.fillPolygon(new double[]{22, 23.2, 22.5}, new double[]{-3.2, -1.2, -3.2}, 3);
                    gc.fillPolygon(new double[]{17, 18.5, 17.5}, new double[]{3.5, 1.5, 3.5}, 3);
                    gc.fillPolygon(new double[]{22, 23.2, 22.5}, new double[]{3.2, 1.2, 3.2}, 3);

                    // Sống bóng ruby sang trọng trên gạc kìm
                    gc.setStroke(Color.web("#A83B3B", 0.75));
                    gc.setLineWidth(0.7);
                    gc.strokeLine(13, -3.2, 24, -3.2);
                    gc.strokeLine(13, 3.2, 24, 3.2);
                }
            }

            gc.restore();
        }
    }
}
