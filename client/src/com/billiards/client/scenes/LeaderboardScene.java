package com.billiards.client.scenes;

import com.billiards.client.NetworkClient;
import com.billiards.client.audio.SoundManager;
import com.billiards.client.protocol.PacketType;
import com.billiards.client.theme.SilverCornerPane;
import com.billiards.client.theme.Theme;
import com.billiards.client.vfx.DragonCloudBackground;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.io.IOException;

/**
 * Màn hình Bảng Xếp Hạng - Phong cách Modern Billiards Club:
 * Khung hình vuông không bo tròn, tráng bạc 4 góc.
 */
public class LeaderboardScene {

    private final NetworkClient network;
    private final SceneManager  sceneManager;
    private Scene scene;

    private VBox tableBox;
    private final String myUsername;
    private final int myExp, myLevel, myWins, myLosses;

    public LeaderboardScene(NetworkClient network, SceneManager sceneManager,
                            String myUsername, int myExp, int myLevel,
                            int myWins, int myLosses) {
        this.network      = network;
        this.sceneManager = sceneManager;
        this.myUsername   = myUsername;
        this.myExp        = myExp;
        this.myLevel      = myLevel;
        this.myWins       = myWins;
        this.myLosses     = myLosses;
        build();
        setupNetworkCallback();

        // 1. Hiển thị ngay lập tức dữ liệu đã nạp ngầm nếu có (0 giây chờ, không bị đơ)
        String cached = (network != null) ? network.getCachedLeaderboardData() : null;
        if (cached != null && !cached.trim().isEmpty()) {
            populateTable(cached);
        }

        // 2. Tự động gửi yêu cầu cập nhật mới nhất từ Server ngầm
        refreshData();
    }

    private void refreshData() {
        if (network != null) {
            network.sendLeaderboardRequest();
        }
    }

    private void build() {
        StackPane root = new StackPane();

        // 1. Nền phòng CLB
        DragonCloudBackground bg = new DragonCloudBackground();

        // 2. Card bảng xếp hạng hình vuông tráng bạc
        VBox cardContent = new VBox(16);
        cardContent.setAlignment(Pos.CENTER);
        cardContent.setPadding(new Insets(26, 36, 24, 36));

        SilverCornerPane card = new SilverCornerPane(cardContent);
        card.setMaxWidth(840);
        card.setMaxHeight(580);

        // Header Tiêu đề & thanh điều hướng
        HBox topRow = new HBox(12);
        topRow.setAlignment(Pos.CENTER_LEFT);

        Button backBtn = Theme.btnSecondary("← Về sảnh chờ", 130, 36);
        backBtn.setOnAction(e -> {
            SoundManager.playButtonClick();
            if (network != null) {
                network.setOnLeaderboardUpdated(null);
            }
            sceneManager.showLobby(myUsername, myExp, myLevel, myWins, myLosses);
        });

        Label title = new Label("Bảng xếp hạng");
        title.setFont(Font.font(Theme.FONT_FAMILY, FontWeight.BOLD, 22));
        title.setTextFill(Color.web(Theme.HEX_TEXT));

        Region topSpacer = new Region();
        HBox.setHgrow(topSpacer, Priority.ALWAYS);

        Button refreshBtn = Theme.btnSecondary("🔄 Làm mới", 110, 36);
        refreshBtn.setOnAction(e -> {
            SoundManager.playButtonClick();
            refreshData();
        });

        topRow.getChildren().addAll(backBtn, topSpacer, title, refreshBtn);

        // Header cột bảng
        HBox tableHeader = buildRow("HẠNG", "CƠ THỦ", "CẤP ĐỘ", "EXP", "THẮNG", "THUA", true, false, 0);

        // Danh sách dữ liệu
        tableBox = new VBox(4);
        Label loadingLbl = new Label("Đang tải dữ liệu...");
        loadingLbl.setFont(Font.font(Theme.FONT_FAMILY, 13));
        loadingLbl.setTextFill(Color.web(Theme.HEX_TEXT_MUTED));
        loadingLbl.setPadding(new Insets(20));
        tableBox.getChildren().add(loadingLbl);

        ScrollPane scrollPane = new ScrollPane(tableBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent;");
        scrollPane.setPrefHeight(380);

        cardContent.getChildren().addAll(topRow, tableHeader, scrollPane);
        root.getChildren().addAll(bg, card);

        scene = new Scene(root, 950, 640);
    }

    private void setupNetworkCallback() {
        if (network == null) return;

        // Lắng nghe cập nhật từ bộ đệm của NetworkClient
        network.setOnLeaderboardUpdated(data -> {
            Platform.runLater(() -> populateTable(data));
        });

        network.setOnPacketReceived(pkt -> {
            if (pkt.type == PacketType.LEADERBOARD_RESPONSE) {
                try {
                    String data = pkt.readString();
                    Platform.runLater(() -> populateTable(data));
                } catch (IOException e) {
                    System.err.println("[LeaderboardScene] Lỗi: " + e.getMessage());
                }
            }
        });
    }

    private void populateTable(String data) {
        tableBox.getChildren().clear();
        if (data == null || data.isEmpty()) {
            Label emptyLbl = new Label("Chưa có dữ liệu bảng xếp hạng.");
            emptyLbl.setTextFill(Color.web(Theme.HEX_TEXT_MUTED));
            emptyLbl.setFont(Font.font(Theme.FONT_FAMILY, 13));
            emptyLbl.setPadding(new Insets(20));
            tableBox.getChildren().add(emptyLbl);
            return;
        }

        String[] rows = data.split(";");
        int rankNum = 1;
        for (String row : rows) {
            String[] parts = row.split("\\|");
            if (parts.length < 6) continue;

            String rank     = parts[0];
            String uname    = parts[1];
            String level    = parts[2];
            String exp      = parts[3];
            String wins     = parts[4];
            String losses   = parts[5];

            boolean isMe = uname.equals(myUsername);

            int rankInt = rankNum;
            try { rankInt = Integer.parseInt(rank); } catch (Exception ignored) {}

            String rankText = (rankInt <= 3) ? ("#" + rankInt) : String.valueOf(rankInt);

            HBox rowBox = buildRow(rankText, uname, "Lv." + level,
                                   exp, wins, losses,
                                   false, isMe, rankInt);
            tableBox.getChildren().add(rowBox);
            rankNum++;
        }
    }

    private HBox buildRow(String rank, String name, String level,
                          String exp, String wins, String losses,
                          boolean isHeader, boolean isMe, int rankInt) {
        HBox row = new HBox();
        row.setPadding(new Insets(8, 14, 8, 14));
        row.setAlignment(Pos.CENTER_LEFT);

        if (isHeader) {
            row.setStyle(
                "-fx-background-color: #141D1A;" +
                "-fx-background-radius: 0;" +
                "-fx-border-color: " + Theme.HEX_BORDER + ";" +
                "-fx-border-width: 0 0 1 0;"
            );
        } else if (isMe) {
            row.setStyle(
                "-fx-background-color: #1D2D25;" +
                "-fx-background-radius: 0;" +
                "-fx-border-color: " + Theme.HEX_PRIMARY + ";" +
                "-fx-border-radius: 0;" +
                "-fx-border-width: 1.0;"
            );
        } else if (rankInt == 1) {
            row.setStyle(
                "-fx-background-color: " + Theme.HEX_SURFACE_HOVER + ";" +
                "-fx-background-radius: 0;" +
                "-fx-border-color: " + Theme.HEX_BRONZE + ";" +
                "-fx-border-radius: 0;" +
                "-fx-border-width: 1.0;"
            );
        } else {
            row.setStyle(
                "-fx-background-color: " + (rankInt % 2 == 0 ? "#17201D" : Theme.HEX_SURFACE) + ";" +
                "-fx-background-radius: 0;"
            );
        }

        Color rankColor;
        Color nameColor = Color.web(Theme.HEX_TEXT);
        Color statColor = Color.web(Theme.HEX_TEXT_MUTED);

        if (isHeader) {
            rankColor = Color.web(Theme.HEX_TEXT_MUTED);
            nameColor = Color.web(Theme.HEX_TEXT_MUTED);
        } else if (rankInt == 1) {
            rankColor = Color.web(Theme.HEX_BRONZE);
        } else if (rankInt == 2) {
            rankColor = Color.web("#D0D7DE");
        } else if (rankInt == 3) {
            rankColor = Color.web("#B89368");
        } else {
            rankColor = Color.web(Theme.HEX_TEXT_MUTED);
        }

        FontWeight weight = (isHeader || isMe || rankInt <= 3) ? FontWeight.BOLD : FontWeight.NORMAL;

        row.getChildren().addAll(
            makeCell(rank,   70,  rankColor, weight),
            makeCell(name,   230, isMe ? Color.web(Theme.HEX_PRIMARY) : nameColor, weight),
            makeCell(level,  80,  statColor, FontWeight.NORMAL),
            makeCell(exp,    120, statColor, FontWeight.NORMAL),
            makeCell(wins,   80,  isHeader ? statColor : Color.web(Theme.HEX_PRIMARY), FontWeight.NORMAL),
            makeCell(losses, 80,  isHeader ? statColor : Color.web("#C95555"), FontWeight.NORMAL)
        );

        if (isMe && !isHeader) {
            Label youLabel = new Label("(Bạn)");
            youLabel.setTextFill(Color.web(Theme.HEX_PRIMARY));
            youLabel.setFont(Font.font(Theme.FONT_FAMILY, FontWeight.BOLD, 12));
            row.getChildren().add(youLabel);
        }

        return row;
    }

    private Label makeCell(String text, double width, Color color, FontWeight weight) {
        Label lbl = new Label(text);
        lbl.setMinWidth(width);
        lbl.setPrefWidth(width);
        lbl.setTextFill(color);
        lbl.setFont(Font.font(Theme.FONT_FAMILY, weight, 13));
        return lbl;
    }

    public Scene getScene() { return scene; }
}
