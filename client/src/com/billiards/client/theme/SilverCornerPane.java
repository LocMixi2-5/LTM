package com.billiards.client.theme;

import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.Stop;

/**
 * Khung hình vuông không bo tròn với 4 góc tráng bạc (Silver Corner Accents).
 * Tái tạo cảm giác nẹp kim loại góc bàn bi-a cao cấp cổ điển.
 * 
 * Thiết kế an toàn 100%: 4 nẹp góc là các node Canvas unmanaged (setManaged(false))
 * có kích thước cố định, KHÔNG BAO GIỜ can thiệp vào preferred size của Pane và
 * KHÔNG BAO GIỜ gây ra hiện tượng tự phình to/mở rộng (infinite resize loop).
 */
public class SilverCornerPane extends StackPane {

    private final Canvas tlCanvas = new Canvas();
    private final Canvas trCanvas = new Canvas();
    private final Canvas blCanvas = new Canvas();
    private final Canvas brCanvas = new Canvas();

    private double cornerLength = 16.0;
    private double cornerThickness = 2.5;

    public SilverCornerPane() {
        init();
    }

    public SilverCornerPane(Node... children) {
        super(children);
        init();
    }

    public SilverCornerPane(double cornerLength, double cornerThickness, Node... children) {
        super(children);
        this.cornerLength = cornerLength;
        this.cornerThickness = cornerThickness;
        init();
    }

    private void init() {
        // Khung hình vuông tuyệt đối, không bo tròn
        setStyle(
            "-fx-background-color: " + Theme.HEX_SURFACE + ";" +
            "-fx-background-radius: 0;" +
            "-fx-border-color: " + Theme.HEX_BORDER + ";" +
            "-fx-border-radius: 0;" +
            "-fx-border-width: 1.0;"
        );
        setEffect(new DropShadow(18, Color.rgb(0, 0, 0, 0.5)));

        // 4 góc tráng bạc là unmanaged node để KHÔNG BAO GIỜ ảnh hưởng tính toán kích thước
        setupCornerCanvas(tlCanvas);
        setupCornerCanvas(trCanvas);
        setupCornerCanvas(blCanvas);
        setupCornerCanvas(brCanvas);

        getChildren().addAll(tlCanvas, trCanvas, blCanvas, brCanvas);
        redrawMiniCorners();
    }

    private void setupCornerCanvas(Canvas c) {
        c.setManaged(false);
        c.setMouseTransparent(true);
    }

    public void setCustomBackground(String bgHex, String borderHex) {
        setStyle(
            "-fx-background-color: " + bgHex + ";" +
            "-fx-background-radius: 0;" +
            "-fx-border-color: " + borderHex + ";" +
            "-fx-border-radius: 0;" +
            "-fx-border-width: 1.0;"
        );
    }

    @Override
    protected void layoutChildren() {
        super.layoutChildren();
        double w = getWidth();
        double h = getHeight();
        if (w >= cornerLength * 2 && h >= cornerLength * 2) {
            tlCanvas.setVisible(true);
            trCanvas.setVisible(true);
            blCanvas.setVisible(true);
            brCanvas.setVisible(true);

            tlCanvas.relocate(0, 0);
            trCanvas.relocate(w - cornerLength, 0);
            blCanvas.relocate(0, h - cornerLength);
            brCanvas.relocate(w - cornerLength, h - cornerLength);

            tlCanvas.toFront();
            trCanvas.toFront();
            blCanvas.toFront();
            brCanvas.toFront();
        } else {
            tlCanvas.setVisible(false);
            trCanvas.setVisible(false);
            blCanvas.setVisible(false);
            brCanvas.setVisible(false);
        }
    }

    private void redrawMiniCorners() {
        double len = cornerLength;
        double thick = cornerThickness;

        tlCanvas.setWidth(len);
        tlCanvas.setHeight(len);
        trCanvas.setWidth(len);
        trCanvas.setHeight(len);
        blCanvas.setWidth(len);
        blCanvas.setHeight(len);
        brCanvas.setWidth(len);
        brCanvas.setHeight(len);

        drawSingleCorner(tlCanvas.getGraphicsContext2D(), 0, len, thick);
        drawSingleCorner(trCanvas.getGraphicsContext2D(), 1, len, thick);
        drawSingleCorner(blCanvas.getGraphicsContext2D(), 2, len, thick);
        drawSingleCorner(brCanvas.getGraphicsContext2D(), 3, len, thick);
    }

    /**
     * Vẽ từng góc riêng biệt (0: TL, 1: TR, 2: BL, 3: BR)
     */
    private void drawSingleCorner(GraphicsContext gc, int cornerType, double len, double thick) {
        gc.clearRect(0, 0, len, len);

        LinearGradient silverGrad = new LinearGradient(
            0, 0, len, len, false, CycleMethod.NO_CYCLE,
            new Stop(0.0, Color.web("#FFFFFF")),
            new Stop(0.5, Color.web("#D1D5DB")),
            new Stop(1.0, Color.web("#9CA3AF"))
        );

        gc.setFill(silverGrad);
        switch (cornerType) {
            case 0 -> { // Top-Left
                gc.fillRect(0, 0, len, thick);
                gc.fillRect(0, 0, thick, len);
                drawRivet(gc, 3.5, 3.5);
            }
            case 1 -> { // Top-Right
                gc.fillRect(0, 0, len, thick);
                gc.fillRect(len - thick, 0, thick, len);
                drawRivet(gc, len - 6.5, 3.5);
            }
            case 2 -> { // Bottom-Left
                gc.fillRect(0, len - thick, len, thick);
                gc.fillRect(0, 0, thick, len);
                drawRivet(gc, 3.5, len - 6.5);
            }
            case 3 -> { // Bottom-Right
                gc.fillRect(0, len - thick, len, thick);
                gc.fillRect(len - thick, 0, thick, len);
                drawRivet(gc, len - 6.5, len - 6.5);
            }
        }
    }

    private void drawRivet(GraphicsContext gc, double rx, double ry) {
        gc.setFill(Color.web("#E5E7EB"));
        gc.fillOval(rx, ry, 3, 3);
        gc.setStroke(Color.web("#6B7280"));
        gc.setLineWidth(0.6);
        gc.strokeOval(rx, ry, 3, 3);
    }

    /**
     * Phương thức tĩnh hỗ trợ vẽ 4 góc tráng bạc trên bất kỳ Canvas nào (như bàn bi-a, toast...)
     */
    public static void drawSilverCorners(GraphicsContext gc, double x, double y, double w, double h, double len, double thick) {
        if (w < len * 2 || h < len * 2) return;

        LinearGradient silverHoriz = new LinearGradient(
            0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
            new Stop(0.0, Color.web("#FFFFFF")),
            new Stop(0.5, Color.web("#D1D5DB")),
            new Stop(1.0, Color.web("#9CA3AF"))
        );

        LinearGradient silverVert = new LinearGradient(
            0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
            new Stop(0.0, Color.web("#FFFFFF")),
            new Stop(0.5, Color.web("#D1D5DB")),
            new Stop(1.0, Color.web("#9CA3AF"))
        );

        gc.save();

        // 1. GÓC TRÊN - TRÁI
        gc.setFill(silverHoriz);
        gc.fillRect(x, y, len, thick);
        gc.setFill(silverVert);
        gc.fillRect(x, y, thick, len);
        gc.setFill(Color.web("#E5E7EB"));
        gc.fillOval(x + 4, y + 4, 3, 3);
        gc.setStroke(Color.web("#6B7280"));
        gc.setLineWidth(0.6);
        gc.strokeOval(x + 4, y + 4, 3, 3);

        // 2. GÓC TRÊN - PHẢI
        gc.setFill(silverHoriz);
        gc.fillRect(x + w - len, y, len, thick);
        gc.setFill(silverVert);
        gc.fillRect(x + w - thick, y, thick, len);
        gc.setFill(Color.web("#E5E7EB"));
        gc.fillOval(x + w - 7, y + 4, 3, 3);
        gc.setStroke(Color.web("#6B7280"));
        gc.setLineWidth(0.6);
        gc.strokeOval(x + w - 7, y + 4, 3, 3);

        // 3. GÓC DƯỚI - TRÁI
        gc.setFill(silverHoriz);
        gc.fillRect(x, y + h - thick, len, thick);
        gc.setFill(silverVert);
        gc.fillRect(x, y + h - len, thick, len);
        gc.setFill(Color.web("#E5E7EB"));
        gc.fillOval(x + 4, y + h - 7, 3, 3);
        gc.setStroke(Color.web("#6B7280"));
        gc.setLineWidth(0.6);
        gc.strokeOval(x + 4, y + h - 7, 3, 3);

        // 4. GÓC DƯỚI - PHẢI
        gc.setFill(silverHoriz);
        gc.fillRect(x + w - len, y + h - thick, len, thick);
        gc.setFill(silverVert);
        gc.fillRect(x + w - thick, y + h - len, thick, len);
        gc.setFill(Color.web("#E5E7EB"));
        gc.fillOval(x + w - 7, y + h - 7, 3, 3);
        gc.setStroke(Color.web("#6B7280"));
        gc.setLineWidth(0.6);
        gc.strokeOval(x + w - 7, y + h - 7, 3, 3);

        gc.restore();
    }
}
