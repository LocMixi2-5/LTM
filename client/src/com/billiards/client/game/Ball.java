package com.billiards.client.game;

/**
 * Đại diện cho một viên bi trên bàn chơi.
 * Hỗ trợ hiệu ứng hình ảnh 3D và hoạt ảnh rơi vào lỗ mượt mà (sinking animation).
 */
public class Ball {
    // Chỉ số bi (0 = cue ball trắng, 1–7 = solid, 8 = đen, 9–15 = stripe)
    public final int index;

    // Vị trí (pixel)
    public double x, y;

    // Vận tốc (pixel/frame)
    public double vx, vy;

    // Bán kính
    public static final double RADIUS = 14.0;

    // Trạng thái hoạt động & lỗ
    public boolean inPocket = false;  // Đã hoàn toàn rơi vào lỗ
    public boolean active   = true;   // Đang tồn tại trên bàn

    // Hiệu ứng hoạt ảnh rơi vào lỗ
    public boolean isSinking = false;
    public double  sinkProgress = 0.0; // 0.0 -> 1.0
    public double  targetPocketX, targetPocketY;
    public double  scale = 1.0;
    public double  opacity = 1.0;

    // Độ xoáy bi cái: spinY > 0 là Trô bi (Draw shot/Backspin), spinY < 0 là Cule (Follow shot), spinX là Ép-phê (Side spin)
    public double spinX = 0.0;
    public double spinY = 0.0;

    // Màu của bi (theo chỉ số)
    public final String color;

    public Ball(int index, double x, double y) {
        this.index = index;
        this.x     = x;
        this.y     = y;
        this.vx    = 0;
        this.vy    = 0;
        this.spinX = 0;
        this.spinY = 0;
        this.color = getColorForIndex(index);
        this.scale = 1.0;
        this.opacity = 1.0;
        this.isSinking = false;
        this.sinkProgress = 0.0;
    }

    /** Trả về màu theo chỉ số bi (chuẩn 8-ball pool) */
    public static String getColorForIndex(int i) {
        return switch (i) {
            case 0  -> "#FFFFFF"; // Cue ball – trắng
            case 1  -> "#F5C518"; // 1 - vàng
            case 2  -> "#1E88E5"; // 2 - xanh dương
            case 3  -> "#E53935"; // 3 - đỏ
            case 4  -> "#8E24AA"; // 4 - tím
            case 5  -> "#FB8C00"; // 5 - cam
            case 6  -> "#2E7D32"; // 6 - xanh lá
            case 7  -> "#6D4C41"; // 7 - nâu
            case 8  -> "#1E1E24"; // 8 - đen huyền bí
            case 9  -> "#F5C518"; // 9–15: stripe
            case 10 -> "#1E88E5";
            case 11 -> "#E53935";
            case 12 -> "#8E24AA";
            case 13 -> "#FB8C00";
            case 14 -> "#2E7D32";
            case 15 -> "#6D4C41";
            default -> "#FFFFFF";
        };
    }

    /** Bắt đầu hiệu ứng hút vào lỗ */
    public void startSinking(double px, double py) {
        this.isSinking = true;
        this.targetPocketX = px;
        this.targetPocketY = py;
        this.sinkProgress = 0.0;
        this.vx = (px - x) * 0.15;
        this.vy = (py - y) * 0.15;
    }

    /** Cập nhật hoạt ảnh rơi vào lỗ */
    public void updateSinking() {
        if (!isSinking) return;
        sinkProgress += 0.08; // Khoảng 12 frames (~0.2s)
        x += (targetPocketX - x) * 0.25;
        y += (targetPocketY - y) * 0.25;
        scale = Math.max(0.05, 1.0 - sinkProgress * 0.9);
        opacity = Math.max(0.0, 1.0 - sinkProgress);

        if (sinkProgress >= 1.0) {
            isSinking = false;
            active = false;
            inPocket = true;
            vx = 0;
            vy = 0;
            scale = 1.0;
            opacity = 1.0;
        }
    }

    /** Di chuyển theo vận tốc & cập nhật hoạt ảnh rơi */
    public void update() {
        if (!active && !isSinking) return;

        if (isSinking) {
            updateSinking();
            return;
        }

        x += vx;
        y += vy;

        // Ma sát mặt vải nỉ: giảm vận tốc mỗi frame
        vx *= PhysicsEngine.FRICTION;
        vy *= PhysicsEngine.FRICTION;

        // Giảm dần độ xoáy khi lăn
        spinX *= 0.985;
        spinY *= 0.985;
        if (Math.abs(spinX) < 0.01) spinX = 0;
        if (Math.abs(spinY) < 0.01) spinY = 0;

        // Nếu vận tốc rất nhỏ → dừng hẳn
        if (Math.abs(vx) < 0.04) { vx = 0; spinX = 0; spinY = 0; }
        if (Math.abs(vy) < 0.04) { vy = 0; spinX = 0; spinY = 0; }
    }

    /** Kiểm tra bi có đang chuyển động hoặc đang rơi không */
    public boolean isMoving() {
        if (isSinking) return true;
        return active && (Math.abs(vx) > 0.04 || Math.abs(vy) > 0.04);
    }

    /** Đặt vị trí bi */
    public void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
        this.spinX = 0.0;
        this.spinY = 0.0;
        this.scale = 1.0;
        this.opacity = 1.0;
        this.isSinking = false;
        this.sinkProgress = 0.0;
    }

    /** Đặt vận tốc bi */
    public void setVelocity(double vx, double vy) {
        this.vx = vx;
        this.vy = vy;
    }

    /** Đặt độ xoáy bi cái */
    public void setSpin(double sx, double sy) {
        this.spinX = sx;
        this.spinY = sy;
    }

    /** Khoảng cách từ bi này tới điểm (px, py) */
    public double distanceTo(double px, double py) {
        double dx = x - px;
        double dy = y - py;
        return Math.sqrt(dx * dx + dy * dy);
    }

    /** Khoảng cách tới bi khác */
    public double distanceTo(Ball other) {
        return distanceTo(other.x, other.y);
    }

    /** Kiểm tra bi có phải solid (1–7) không */
    public boolean isSolid()  { return index >= 1 && index <= 7; }

    /** Kiểm tra bi có phải stripe (9–15) không */
    public boolean isStripe() { return index >= 9 && index <= 15; }

    /** Kiểm tra bi có phải cue ball không */
    public boolean isCue()    { return index == 0; }

    /** Kiểm tra bi có phải bi 8 đen không */
    public boolean isEightBall() { return index == 8; }
}
