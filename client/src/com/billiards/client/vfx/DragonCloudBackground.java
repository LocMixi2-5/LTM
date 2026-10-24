package com.billiards.client.vfx;

import com.billiards.client.theme.Theme;
import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Nền không gian "Modern Billiards Club":
 * - Tông nền tối trầm tĩnh lặng (#111816)
 * - Vệt sáng đèn trần nhẹ rọi từ trên xuống (Overhead Pool Table Ambient Light)
 * - Cây lá đung đưa uyển chuyển tự nhiên (Swaying Foliage)
 * - Con bọ côn trùng bò nhịp nhàng sống động (Crawling Beetle & Ladybug)
 */
public class DragonCloudBackground extends StackPane {

    private final Canvas canvas;
    private final GraphicsContext gc;
    private AnimationTimer timer;
    private final Random rand = new Random();
    private final long startTime = System.currentTimeMillis();

    // Hiệu ứng thiên nhiên cành lá và bọ côn trùng
    private final NatureAmbience natureAmbience = new NatureAmbience();

    // Hạt bụi ánh sáng phòng (Ambient dust motes trong vầng sáng)
    private static class AmbientMote {
        double x, y, radius, alpha, baseAlpha, vy, vx;
    }
    private final List<AmbientMote> motes = new ArrayList<>();

    public DragonCloudBackground() {
        setStyle("-fx-background-color: " + Theme.BG_MAIN + ";");
        canvas = new Canvas(1000, 650);
        canvas.setManaged(false);
        gc = canvas.getGraphicsContext2D();
        getChildren().add(canvas);

        // Khởi tạo các hạt ánh sáng nhẹ
        for (int i = 0; i < 18; i++) {
            AmbientMote m = new AmbientMote();
            m.x = rand.nextDouble() * 1000;
            m.y = rand.nextDouble() * 650;
            m.radius = 1.0 + rand.nextDouble() * 1.6;
            m.baseAlpha = 0.08 + rand.nextDouble() * 0.12;
            m.alpha = m.baseAlpha;
            m.vy = -0.15 - rand.nextDouble() * 0.25;
            m.vx = (rand.nextDouble() - 0.5) * 0.1;
            motes.add(m);
        }

        startLoop();
    }

    @Override
    protected void layoutChildren() {
        super.layoutChildren();
        double w = getWidth();
        double h = getHeight();
        if (w > 0 && h > 0) {
            if (canvas.getWidth() != w || canvas.getHeight() != h) {
                canvas.setWidth(w);
                canvas.setHeight(h);
            }
        }
    }

    private void startLoop() {
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                updateAndRender();
            }
        };
        timer.start();
    }

    public void stop() {
        if (timer != null) timer.stop();
    }

    private void updateAndRender() {
        double w = canvas.getWidth();
        double h = canvas.getHeight();
        if (w <= 0 || h <= 0) return;

        double timeSec = (System.currentTimeMillis() - startTime) / 1000.0;

        // 1. Nền tối CLB
        gc.setFill(Color.web(Theme.BG_MAIN));
        gc.fillRect(0, 0, w, h);

        // 2. Vầng sáng đèn trần nhẹ chiếu xuống giữa phòng (Warm Table Spotlight Vignette)
        RadialGradient spotlight = new RadialGradient(
            0, 0, w * 0.5, h * 0.28, w * 0.65, false, CycleMethod.NO_CYCLE,
            new Stop(0.0, Color.rgb(36, 50, 43, 0.45)), // Xanh rêu nỉ ánh sáng ấm
            new Stop(0.5, Color.rgb(26, 36, 32, 0.28)),
            new Stop(1.0, Color.TRANSPARENT)
        );
        gc.setFill(spotlight);
        gc.fillRect(0, 0, w, h);

        // 3. Khung viền chỉ đồng mờ viền trang trí câu lạc bộ (Subtle Inlay Border)
        double margin = 24.0;
        gc.setStroke(Color.rgb(194, 163, 107, 0.04));
        gc.setLineWidth(1.0);
        gc.strokeRect(margin, margin, Math.max(10, w - margin * 2), Math.max(10, h - margin * 2));

        // 4. Các góc nẹp bạc tinh tế (Corner silver brackets mờ)
        drawCornerBrackets(w, h, margin);

        // 5. Cập nhật và vẽ các hạt ánh sáng êm dịu (Soft ambient motes)
        gc.setFill(Color.rgb(237, 241, 232));
        for (AmbientMote m : motes) {
            m.y += m.vy;
            m.x += m.vx;
            if (m.y < 0) {
                m.y = h + 10;
                m.x = rand.nextDouble() * w;
            }
            if (m.x < 0) m.x = w;
            if (m.x > w) m.x = 0;

            gc.setGlobalAlpha(m.alpha);
            gc.fillOval(m.x - m.radius, m.y - m.radius, m.radius * 2, m.radius * 2);
        }
        gc.setGlobalAlpha(1.0);

        // 6. CÂY LÁ ĐUNG ĐƯA VÀ CON BỌ CÔN TRÙNG DI CHUYỂN
        natureAmbience.render(gc, w, h, timeSec, false);
    }

    private void drawCornerBrackets(double w, double h, double m) {
        double len = 20.0;
        gc.setStroke(Color.rgb(209, 213, 219, 0.08)); // Ánh kim bạc mờ
        gc.setLineWidth(1.5);

        // Góc trên - trái
        gc.strokeLine(m, m, m + len, m);
        gc.strokeLine(m, m, m, m + len);

        // Góc trên - phải
        gc.strokeLine(w - m, m, w - m - len, m);
        gc.strokeLine(w - m, m, w - m, m + len);

        // Góc dưới - trái
        gc.strokeLine(m, h - m, m + len, h - m);
        gc.strokeLine(m, h - m, m, h - m - len);

        // Góc dưới - phải
        gc.strokeLine(w - m, h - m, w - m - len, h - m);
        gc.strokeLine(w - m, h - m, w - m, h - m - len);
    }
}
