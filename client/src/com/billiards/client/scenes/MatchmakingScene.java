package com.billiards.client.scenes;

import com.billiards.client.NetworkClient;
import com.billiards.client.audio.SoundManager;
import com.billiards.client.protocol.PacketType;
import com.billiards.client.theme.SilverCornerPane;
import com.billiards.client.theme.Theme;
import com.billiards.client.vfx.DragonCloudBackground;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

import java.io.IOException;

/**
 * Màn hình Tìm Trận (Matchmaking) - Phong cách Modern Billiards Club:
 * Khung hình vuông không bo tròn, tráng bạc 4 góc.
 */
public class MatchmakingScene {

    private final NetworkClient network;
    private final SceneManager  sceneManager;
    private Scene scene;
    private final String username;
    private Timeline dotsAnimation;

    public MatchmakingScene(NetworkClient network, SceneManager sceneManager, String username) {
        this.network      = network;
        this.sceneManager = sceneManager;
        this.username     = username;
        build();
        setupNetworkCallback();
    }

    private void build() {
        StackPane root = new StackPane();

        // 1. Nền phòng CLB
        DragonCloudBackground bg = new DragonCloudBackground();

        // 2. Thẻ tìm trận hình vuông tráng bạc
        VBox cardContent = new VBox(22);
        cardContent.setAlignment(Pos.CENTER);
        cardContent.setPadding(new Insets(38, 44, 38, 44));

        SilverCornerPane card = new SilverCornerPane(cardContent);
        card.setMaxWidth(440);
        card.setMaxHeight(460);

        // Hiệu ứng sóng lan tỏa dịu nhẹ
        StackPane radarPane = new StackPane();
        radarPane.setPrefSize(130, 130);

        for (int i = 0; i < 3; i++) {
            Circle wave = new Circle(24, Color.TRANSPARENT);
            wave.setStroke(Color.web(Theme.HEX_BRONZE, 0.5));
            wave.setStrokeWidth(1.5);

            ScaleTransition scale = new ScaleTransition(Duration.seconds(2.0), wave);
            scale.setFromX(0.9);
            scale.setFromY(0.9);
            scale.setToX(2.6);
            scale.setToY(2.6);
            scale.setCycleCount(Animation.INDEFINITE);

            FadeTransition fade = new FadeTransition(Duration.seconds(2.0), wave);
            fade.setFromValue(0.6);
            fade.setToValue(0.0);
            fade.setCycleCount(Animation.INDEFINITE);

            ParallelTransition pt = new ParallelTransition(scale, fade);
            pt.setDelay(Duration.millis(i * 650));
            pt.play();

            radarPane.getChildren().add(wave);
        }

        // Bi số 8 trung tâm
        StackPane ballCenter = new StackPane();
        Circle ballBase = new Circle(28, Color.web("#141414"));
        ballBase.setStroke(Color.web(Theme.HEX_SILVER, 0.7));
        ballBase.setStrokeWidth(1.5);

        Circle ballInner = new Circle(12, Color.web("#EDF1E8"));
        Label num8 = new Label("8");
        num8.setFont(Font.font(Theme.FONT_FAMILY, FontWeight.BOLD, 13));
        num8.setTextFill(Color.web("#141414"));

        ballCenter.getChildren().addAll(ballBase, ballInner, num8);
        radarPane.getChildren().add(ballCenter);

        // Trạng thái tìm kiếm
        Label searchingLabel = new Label("Đang tìm đối thủ");
        searchingLabel.setFont(Font.font(Theme.FONT_FAMILY, FontWeight.BOLD, 18));
        searchingLabel.setTextFill(Color.web(Theme.HEX_TEXT));

        Label dotsLabel = new Label("...");
        dotsLabel.setFont(Font.font(Theme.FONT_FAMILY, FontWeight.BOLD, 18));
        dotsLabel.setTextFill(Color.web(Theme.HEX_BRONZE));

        dotsAnimation = new Timeline(
            new KeyFrame(Duration.ZERO,        e -> dotsLabel.setText(".")),
            new KeyFrame(Duration.millis(400), e -> dotsLabel.setText("..")),
            new KeyFrame(Duration.millis(800), e -> dotsLabel.setText("..."))
        );
        dotsAnimation.setCycleCount(Animation.INDEFINITE);
        dotsAnimation.play();

        HBox searchingBox = new HBox(4, searchingLabel, dotsLabel);
        searchingBox.setAlignment(Pos.CENTER);

        Label hint = new Label("Hệ thống đang ghép phòng thi đấu trực tuyến...");
        hint.setTextFill(Color.web(Theme.HEX_TEXT_MUTED));
        hint.setFont(Font.font(Theme.FONT_FAMILY, 13));

        // Nút Hủy (hình vuông không bo tròn)
        Button cancelBtn = Theme.btnDanger("Hủy tìm trận", 180, 40);
        cancelBtn.setOnAction(e -> {
            SoundManager.playButtonClick();
            dotsAnimation.stop();
            network.sendCancelFind();
            NetworkClient.PlayerSession sess = network.getSession();
            if (sess != null) {
                sceneManager.showLobby(sess.username, sess.exp, sess.level, sess.wins, sess.losses);
            } else {
                sceneManager.showLobby(username, 0, 1, 0, 0);
            }
        });

        cardContent.getChildren().addAll(radarPane, searchingBox, hint, cancelBtn);
        root.getChildren().addAll(bg, card);

        scene = new Scene(root, 900, 600);
    }

    private void setupNetworkCallback() {
        network.setOnPacketReceived(pkt -> {
            try {
                if (pkt.type == PacketType.MATCH_FOUND) {
                    String opponentName     = pkt.readString();
                    int    opponentLevel    = pkt.readInt();
                    int    opponentAvatarId = pkt.readInt();
                    String opponentCueColor = pkt.readString();

                    Platform.runLater(() -> {
                        dotsAnimation.stop();
                        var sess = network.getSession();
                        int myLevel = (sess != null) ? sess.level : 1;
                        int myAvatarId = (sess != null) ? sess.avatarId : 0;
                        String myCueColor = (sess != null) ? sess.cueColor : "#DEB887";

                        sceneManager.showPreGame(username, myLevel, myAvatarId, myCueColor,
                                                 opponentName, opponentLevel, opponentAvatarId, opponentCueColor);
                    });
                }
            } catch (IOException e) {
                System.err.println("[MatchmakingScene] Lỗi: " + e.getMessage());
            }
        });
    }

    public Scene getScene() { return scene; }
}
