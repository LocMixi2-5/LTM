package com.billiards.client.scenes;

import com.billiards.client.NetworkClient;
import com.billiards.client.audio.SoundManager;
import com.billiards.client.theme.SilverCornerPane;
import com.billiards.client.theme.Theme;
import com.billiards.client.vfx.DragonCloudBackground;
import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

/**
 * Màn hình Kết Quả trận đấu - Phong cách Modern Billiards Club:
 * Khung hình vuông không bo tròn, tráng bạc 4 góc.
 */
public class ResultScene {

    private final NetworkClient network;
    private final SceneManager  sceneManager;
    private Scene scene;

    public ResultScene(NetworkClient network, SceneManager sceneManager,
                       boolean youWin, int expGained, String myName, String opponentName) {
        this.network      = network;
        this.sceneManager = sceneManager;
        build(youWin, expGained, myName, opponentName);
    }

    private void build(boolean youWin, int expGained, String myName, String opponentName) {
        StackPane root = new StackPane();

        // 1. Nền phòng CLB bi-a hiện đại
        DragonCloudBackground bg = new DragonCloudBackground();

        // 2. Thẻ kết quả hình vuông tráng bạc 4 góc
        VBox cardContent = new VBox(20);
        cardContent.setAlignment(Pos.CENTER);
        cardContent.setPadding(new Insets(36, 44, 36, 44));

        SilverCornerPane card = new SilverCornerPane(cardContent);
        card.setMaxWidth(460);
        card.setMaxHeight(480);
        if (youWin) {
            card.setCustomBackground(Theme.HEX_SURFACE, Theme.HEX_BRONZE);
        }

        // Biểu tượng trạng thái kết quả
        Label statusIcon = new Label(youWin ? "🏆" : "🎱");
        statusIcon.setFont(Font.font(44));

        // Tiêu đề ngắn gọn: "Bạn thắng!" hoặc "Bạn thua"
        Label resultLabel = new Label(youWin ? "Bạn thắng!" : "Bạn thua");
        resultLabel.setFont(Font.font(Theme.FONT_FAMILY, FontWeight.BOLD, 26));
        resultLabel.setTextFill(youWin ? Color.web(Theme.HEX_BRONZE) : Color.web(Theme.HEX_TEXT_MUTED));

        // Thông tin trận đấu: myName vs opponentName
        HBox matchBox = new HBox(8);
        matchBox.setAlignment(Pos.CENTER);
        Label myLabel = new Label(myName);
        myLabel.setFont(Font.font(Theme.FONT_FAMILY, FontWeight.BOLD, 14));
        myLabel.setTextFill(Color.web(Theme.HEX_TEXT));

        Label vsLabel = new Label("vs");
        vsLabel.setFont(Font.font(Theme.FONT_FAMILY, 13));
        vsLabel.setTextFill(Color.web(Theme.HEX_TEXT_MUTED));

        Label oppLabel = new Label(opponentName);
        oppLabel.setFont(Font.font(Theme.FONT_FAMILY, FontWeight.BOLD, 14));
        oppLabel.setTextFill(Color.web(Theme.HEX_TEXT));
        matchBox.getChildren().addAll(myLabel, vsLabel, oppLabel);

        // Khung EXP hình vuông tráng bạc
        VBox expContent = new VBox(6);
        expContent.setAlignment(Pos.CENTER);
        expContent.setPadding(new Insets(14, 24, 14, 24));

        Label expTitle = new Label("Kinh nghiệm nhận được");
        expTitle.setFont(Font.font(Theme.FONT_FAMILY, 12));
        expTitle.setTextFill(Color.web(Theme.HEX_TEXT_MUTED));

        Label expLabel = new Label(youWin ? ("+" + expGained + " EXP") : "0 EXP");
        expLabel.setFont(Font.font(Theme.FONT_FAMILY, FontWeight.BOLD, 22));
        expLabel.setTextFill(youWin ? Color.web(Theme.HEX_PRIMARY) : Color.web(Theme.HEX_TEXT_MUTED));

        expContent.getChildren().addAll(expTitle, expLabel);

        SilverCornerPane expBox = new SilverCornerPane(10, 1.8, expContent);
        expBox.setCustomBackground("#141D1A", Theme.HEX_BORDER);
        expBox.setPrefWidth(320);

        // Nút về sảnh chờ hình vuông
        Button lobbyBtn = Theme.btnPrimary("Về sảnh chờ", 220, 42);
        lobbyBtn.setOnAction(e -> {
            SoundManager.playButtonClick();
            NetworkClient.PlayerSession sess = network.getSession();
            if (sess != null) {
                if (youWin) {
                    sess.exp   += expGained;
                    sess.wins  += 1;
                    sess.level = (sess.exp / 200) + 1;
                } else {
                    sess.losses += 1;
                }
                sceneManager.showLobby(sess.username, sess.exp, sess.level, sess.wins, sess.losses);
            } else {
                sceneManager.showLobby(myName, 0, 1, 0, 0);
            }
        });

        // Kiểm tra xem có skin gậy nào vừa mở khóa sau trận đấu không
        NetworkClient.PlayerSession currentSess = (network != null) ? network.getSession() : null;
        int oldLevel = (currentSess != null) ? currentSess.level : 1;
        int newExp = (currentSess != null) ? (currentSess.exp + (youWin ? expGained : 0)) : expGained;
        int newLevel = (newExp / 200) + 1;

        java.util.List<com.billiards.client.game.CueSkin> newSkins = new java.util.ArrayList<>();
        if (newLevel > oldLevel) {
            for (com.billiards.client.game.CueSkin skin : com.billiards.client.game.CueSkinManager.getAllSkins()) {
                if (skin.requiredLevel > oldLevel && skin.requiredLevel <= newLevel) {
                    newSkins.add(skin);
                }
            }
        }

        // Hộp thông báo mở khóa skin mới (nếu có)
        if (!newSkins.isEmpty()) {
            VBox unlockContent = new VBox(6);
            unlockContent.setAlignment(Pos.CENTER);
            unlockContent.setPadding(new Insets(10, 16, 10, 16));

            Label unlockTitle = new Label("🎉 MỞ KHÓA SKIN GẬY MỚI (CẤP " + newLevel + ")!");
            unlockTitle.setFont(Font.font(Theme.FONT_FAMILY, FontWeight.BOLD, 12));
            unlockTitle.setTextFill(Color.web(Theme.HEX_BRONZE));

            unlockContent.getChildren().add(unlockTitle);
            for (com.billiards.client.game.CueSkin s : newSkins) {
                Label skinNameLbl = new Label("• [" + s.tierName + "] " + s.name + " (" + s.beetleName + ")");
                skinNameLbl.setFont(Font.font(Theme.FONT_FAMILY, FontWeight.BOLD, 13));
                skinNameLbl.setTextFill(Color.web(s.tierColorHex));
                unlockContent.getChildren().add(skinNameLbl);
            }

            Button viewColBtn = Theme.btnSecondary("Xem bộ sưu tập", 160, 30);
            viewColBtn.setOnAction(e -> {
                SettingsDialog dialog = new SettingsDialog(sceneManager.getPrimaryStage(), network, (newAv, newCue) -> {});
                dialog.show();
            });
            unlockContent.getChildren().add(viewColBtn);

            SilverCornerPane unlockBox = new SilverCornerPane(8, 1.5, unlockContent);
            unlockBox.setCustomBackground("#182520", Theme.HEX_BRONZE);
            unlockBox.setPrefWidth(320);
            cardContent.getChildren().addAll(statusIcon, resultLabel, matchBox, expBox, unlockBox, lobbyBtn);
        } else {
            cardContent.getChildren().addAll(statusIcon, resultLabel, matchBox, expBox, lobbyBtn);
        }

        root.getChildren().addAll(bg, card);

        // Hiệu ứng Fade in xuất hiện ngắn gọn 180ms
        FadeTransition fadeIn = new FadeTransition(Duration.millis(180), card);
        fadeIn.setFromValue(0.2);
        fadeIn.setToValue(1.0);
        fadeIn.play();

        scene = new Scene(root, 900, 600);
    }

    public Scene getScene() { return scene; }
}
