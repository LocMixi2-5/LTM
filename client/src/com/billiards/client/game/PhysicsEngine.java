package com.billiards.client.game;

import com.billiards.client.audio.SoundManager;
import java.util.List;

/**
 * Engine vật lý 2D nâng cao cho game bi-a:
 * 1. Va chạm đàn hồi bi - bi với tính toán lực chuẩn xác
 * 2. Va chạm băng cao su thành bàn (Cushion) kèm âm thanh nảy băng
 * 3. Bi hút vào lỗ (Sinking) với hiệu ứng thị giác và âm thanh
 * 4. Hệ số ma sát chân thực
 * 5. Hỗ trợ CollisionListener để phát hiệu ứng hình ảnh (shockwaves, particles)
 */
public class PhysicsEngine {

    /** Hệ số ma sát (0.985 = giảm 1.5% mỗi frame) */
    public static final double FRICTION = 0.985;

    /** Hệ số phục hồi va chạm tường (băng nảy) */
    private static final double WALL_RESTITUTION = 0.78;

    /** Hệ số phục hồi va chạm bi-bi */
    private static final double BALL_RESTITUTION = 0.96;

    // Vùng chơi của bàn bi-a
    private final double tableX;
    private final double tableY;
    private final double tableW;
    private final double tableH;

    // Vị trí 6 lỗ
    private final double[][] pockets;

    /** Callback khi bi rơi vào lỗ */
    private PocketCallback pocketCallback;

    /** Callback khi xảy ra va chạm (để vẽ hiệu ứng xung kích) */
    private CollisionListener collisionListener;

    public interface PocketCallback {
        void onBallPocketed(Ball ball);
    }

    public interface CollisionListener {
        void onCollision(double x, double y, double forceRatio);
    }

    public PhysicsEngine(double tableX, double tableY, double tableW, double tableH) {
        this.tableX  = tableX;
        this.tableY  = tableY;
        this.tableW  = tableW;
        this.tableH  = tableH;

        // 6 lỗ: 4 góc + 2 giữa cạnh dài (tọa độ tâm lỗ)
        this.pockets = new double[][] {
            {tableX,              tableY},             // góc trên-trái
            {tableX + tableW / 2, tableY - 4},         // giữa trên
            {tableX + tableW,     tableY},             // góc trên-phải
            {tableX,              tableY + tableH},    // góc dưới-trái
            {tableX + tableW / 2, tableY + tableH + 4},// giữa dưới
            {tableX + tableW,     tableY + tableH}     // góc dưới-phải
        };
    }

    public void setPocketCallback(PocketCallback cb) { this.pocketCallback = cb; }
    public void setCollisionListener(CollisionListener listener) { this.collisionListener = listener; }

    /** Số bước tính toán vật lý con mỗi frame (ngăn chặn xuyên thấu và chuẩn hóa góc nảy 100%) */
    public static final int SUB_STEPS = 10;

    // ============================================================
    // CẬP NHẬT VẬT LÝ (gọi mỗi frame)
    // ============================================================

    public void update(List<Ball> balls) {
        double subFriction = Math.pow(FRICTION, 1.0 / SUB_STEPS);
        double dtStep = 1.0 / SUB_STEPS;

        for (int step = 0; step < SUB_STEPS; step++) {
            // 1. Di chuyển các bi một bước nhỏ dt = 1.0 / SUB_STEPS
            for (Ball ball : balls) {
                if (ball.active && !ball.isSinking) {
                    ball.x += ball.vx * dtStep;
                    ball.y += ball.vy * dtStep;
                    ball.vx *= subFriction;
                    ball.vy *= subFriction;
                }
            }

            // 2. Va chạm tường
            for (Ball ball : balls) {
                if (ball.active && !ball.isSinking) resolveWallCollision(ball);
            }

            // 3. Va chạm bi - bi với Time-of-Impact chính xác 100%
            for (int i = 0; i < balls.size(); i++) {
                for (int j = i + 1; j < balls.size(); j++) {
                    Ball a = balls.get(i);
                    Ball b = balls.get(j);
                    if (a.active && !a.isSinking && b.active && !b.isSinking) {
                        resolveBallCollision(a, b, dtStep);
                    }
                }
            }

            // 4. Kiểm tra bi vào lỗ
            for (Ball ball : balls) {
                if (ball.active && !ball.isSinking) checkPockets(ball);
            }
        }

        // Cập nhật hoạt ảnh rơi vào lỗ & triệt tiêu vận tốc rất nhỏ ở cuối frame
        for (Ball ball : balls) {
            if (ball.isSinking) {
                ball.updateSinking();
            } else if (ball.active) {
                ball.spinX *= 0.985;
                ball.spinY *= 0.985;
                if (Math.abs(ball.spinX) < 0.01) ball.spinX = 0;
                if (Math.abs(ball.spinY) < 0.01) ball.spinY = 0;

                if (Math.abs(ball.vx) < 0.04) { ball.vx = 0; ball.spinX = 0; }
                if (Math.abs(ball.vy) < 0.04) { ball.vy = 0; ball.spinY = 0; }
            }
        }
    }

    /** Kiểm tra xem tất cả bi có dừng hẳn chưa */
    public boolean allBallsStopped(List<Ball> balls) {
        for (Ball ball : balls) {
            if (ball.isMoving()) return false;
        }
        return true;
    }

    // ============================================================
    // VA CHẠM TƯỜNG (Cushion bounce)
    // ============================================================
    private void resolveWallCollision(Ball ball) {
        double r = Ball.RADIUS;
        boolean hitWall = false;
        double speed = Math.sqrt(ball.vx * ball.vx + ball.vy * ball.vy);

        // Cạnh trái
        if (ball.x - r < tableX) {
            ball.x  = tableX + r;
            ball.vx = -ball.vx * WALL_RESTITUTION;
            if (ball.isCue() && ball.spinX != 0) {
                ball.vy += ball.spinX * 1.5;
                ball.spinX *= 0.5;
            }
            hitWall = true;
        }
        // Cạnh phải
        else if (ball.x + r > tableX + tableW) {
            ball.x  = tableX + tableW - r;
            ball.vx = -ball.vx * WALL_RESTITUTION;
            if (ball.isCue() && ball.spinX != 0) {
                ball.vy -= ball.spinX * 1.5;
                ball.spinX *= 0.5;
            }
            hitWall = true;
        }
        // Cạnh trên
        if (ball.y - r < tableY) {
            ball.y  = tableY + r;
            ball.vy = -ball.vy * WALL_RESTITUTION;
            if (ball.isCue() && ball.spinX != 0) {
                ball.vx -= ball.spinX * 1.5;
                ball.spinX *= 0.5;
            }
            hitWall = true;
        }
        // Cạnh dưới
        else if (ball.y + r > tableY + tableH) {
            ball.y  = tableY + tableH - r;
            ball.vy = -ball.vy * WALL_RESTITUTION;
            if (ball.isCue() && ball.spinX != 0) {
                ball.vx -= ball.spinX * 1.5;
                ball.spinX *= 0.5;
            }
            hitWall = true;
        }

        if (hitWall && speed > 0.4) {
            double speedRatio = Math.min(1.0, speed / 10.0);
            SoundManager.playCushion(speedRatio);
        }
    }

    // ============================================================
    // VA CHẠM BI - BI (Time-of-Impact Exact Collision & Trô Bi)
    // ============================================================
    private void resolveBallCollision(Ball a, Ball b, double dtStep) {
        double dx = b.x - a.x;
        double dy = b.y - a.y;
        double distSq = dx * dx + dy * dy;
        double minDist = Ball.RADIUS * 2;
        double minDistSq = minDist * minDist;

        if (distSq >= minDistSq || distSq == 0) return; // Không va chạm

        double dist = Math.sqrt(distSq);

        // Vector pháp tuyến mặc định
        double nx = dx / dist;
        double ny = dy / dist;

        // Vận tốc tương đối
        double relVx = a.vx - b.vx;
        double relVy = a.vy - b.vy;
        double dotProduct = relVx * nx + relVy * ny;

        if (dotProduct <= 0) return; // Đang rời xa nhau

        // Time-of-Impact (TOI): Tính toán pháp tuyến va chạm đúng thời điểm tiếp xúc mép bi
        // Loại bỏ hoàn toàn sai số góc do bi đâm sâu (tunneling) giữa các frame
        double relSpeedSq = relVx * relVx + relVy * relVy;
        if (relSpeedSq > 0.001) {
            // Tọa độ tương đối ngay trước bước sub-step
            double p0x = dx + relVx * dtStep;
            double p0y = dy + relVy * dtStep;
            double p0Sq = p0x * p0x + p0y * p0y;

            if (p0Sq >= minDistSq) {
                double B = -2.0 * (p0x * relVx + p0y * relVy);
                double C = p0Sq - minDistSq;
                double disc = B * B - 4.0 * relSpeedSq * C;
                if (disc >= 0) {
                    double tImpact = (-B - Math.sqrt(disc)) / (2.0 * relSpeedSq);
                    if (tImpact >= 0.0 && tImpact <= dtStep) {
                        double contactDx = p0x - relVx * tImpact;
                        double contactDy = p0y - relVy * tImpact;
                        double cDist = Math.hypot(contactDx, contactDy);
                        if (cDist > 0.001) {
                            nx = contactDx / cDist;
                            ny = contactDy / cDist;
                            dotProduct = relVx * nx + relVy * ny;
                            if (dotProduct <= 0) return;
                        }
                    }
                }
            }
        }

        // Tách 2 bi ra (chống xuyên thấu) dọc theo pháp tuyến va chạm thực
        double overlap = (minDist - dist) / 2.0;
        a.x -= nx * overlap;
        a.y -= ny * overlap;
        b.x += nx * overlap;
        b.y += ny * overlap;

        // Xung lượng truyền lực đàn hồi
        double impulse = dotProduct * BALL_RESTITUTION;
        a.vx -= impulse * nx;
        a.vy -= impulse * ny;
        b.vx += impulse * nx;
        b.vy += impulse * ny;

        // Hiệu ứng Trô bi (Draw shot / Backspin) & Cule (Follow shot)
        if (a.isCue() && a.spinY != 0) {
            double drawImpulse = a.spinY * 3.6;
            a.vx -= nx * drawImpulse;
            a.vy -= ny * drawImpulse;
            a.spinY *= 0.15; // Giảm xoáy sau cú chạm chính
        } else if (b.isCue() && b.spinY != 0) {
            double drawImpulse = b.spinY * 3.6;
            b.vx += nx * drawImpulse;
            b.vy += ny * drawImpulse;
            b.spinY *= 0.15;
        }

        double forceRatio = Math.min(1.0, impulse / 5.5);
        SoundManager.playCollision(forceRatio);

        if (collisionListener != null) {
            double colX = (a.x + b.x) / 2.0;
            double colY = (a.y + b.y) / 2.0;
            collisionListener.onCollision(colX, colY, forceRatio);
        }
    }

    // ============================================================
    // KIỂM TRA BI VÀO LỖ
    // ============================================================
    private void checkPockets(Ball ball) {
        double pocketRadius = 30.0; // Bán kính kích hoạt lỗ

        for (double[] pocket : pockets) {
            double dx = ball.x - pocket[0];
            double dy = ball.y - pocket[1];
            double dist = Math.sqrt(dx * dx + dy * dy);

            if (dist < pocketRadius) {
                SoundManager.playPocket();
                ball.startSinking(pocket[0], pocket[1]);

                if (pocketCallback != null) {
                    pocketCallback.onBallPocketed(ball);
                }
                break;
            }
        }
    }

    /** Getter cho vị trí lỗ (để vẽ) */
    public double[][] getPockets() { return pockets; }
}
