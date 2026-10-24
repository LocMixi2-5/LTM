package com.billiards.client.scenes;

import com.billiards.client.AvatarManager;
import com.billiards.client.NetworkClient;
import com.billiards.client.audio.SoundManager;
import com.billiards.client.protocol.PacketType;
import com.billiards.client.theme.SilverCornerPane;
import com.billiards.client.theme.Theme;
import com.billiards.client.vfx.DragonCloudBackground;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
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
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Màn hình Chuẩn Bị Đối Đầu (Pre-Game Scene) - Phong cách Modern Billiards Club:
 * Khung hình vuông không bo tròn, tráng bạc 4 góc.
 */
public class PreGameScene {

    private final NetworkClient network;
    private final SceneManager  sceneManager;
    private Scene scene;

    private final String myName,     opponentName;
    private final int    myLevel,    opponentLevel;
    private final int    myAvatarId, opponentAvatarId;
    private final String myCueColor, opponentCueColor;

    private Label     countdownLabel;
    private Button    readyBtn;
    private Timeline  countdownTimer;
    private final AtomicInteger secondsLeft = new AtomicInteger(13);
    private boolean readySent = false;

    public PreGameScene(NetworkClient network, SceneManager sceneManager,
                        String myName, int myLevel, String opponentName, int opponentLevel) {
        this(network, sceneManager, myName, myLevel, 0, "#DEB887", opponentName, opponentLevel, 0, "#DEB887");
    }

    public PreGameScene(NetworkClient network, SceneManager sceneManager,
                        String myName, int myLevel, int myAvatarId, String myCueColor,
                        String opponentName, int opponentLevel, int opponentAvatarId, String opponentCueColor) {
        this.network           = network;
        this.sceneManager      = sceneManager;
        this.myName            = myName;
        this.myLevel           = myLevel;
        this.myAvatarId        = myAvatarId;
        this.myCueColor        = (myCueColor != null && !myCueColor.isEmpty()) ? myCueColor : "#DEB887";
        this.opponentName      = opponentName;
        this.opponentLevel     = opponentLevel;
        this.opponentAvatarId  = opponentAvatarId;
        this.opponentCueColor  = (opponentCueColor != null && !opponentCueColor.isEmpty()) ? opponentCueColor : "#DEB887";
        build();
        startCountdown();
        setupNetworkCallback();
    }

    private void build() {
        StackPane root = new StackPane();

        // 1. Nền phòng CLB
        DragonCloudBackground bg = new DragonCloudBackground();

        // 2. Thẻ đối đầu hình vuông tráng bạc
        VBox cardContent = new VBox(22);
        cardContent.setAlignment(Pos.CENTER);
        cardContent.setPadding(new Insets(32, 40, 32, 40));

        SilverCornerPane card = new SilverCornerPane(cardContent);
        card.setMaxWidth(680);
        card.setMaxHeight(520);

        Label title = new Label("Chuẩn bị trận đấu");
        title.setFont(Font.font(Theme.FONT_FAMILY, FontWeight.BOLD, 22));
        title.setTextFill(Color.web(Theme.HEX_TEXT));

        // Thẻ 2 người chơi
        HBox players = new HBox(28);
        players.setAlignment(Pos.CENTER);

        SilverCornerPane myCard = buildPlayerCard(myName, myLevel, myAvatarId, true);

        VBox vsBox = new VBox(2);
        vsBox.setAlignment(Pos.CENTER);
        Label vsLabel = new Label("VS");
        vsLabel.setFont(Font.font(Theme.FONT_FAMILY, FontWeight.BOLD, 24));
        vsLabel.setTextFill(Color.web(Theme.HEX_BRONZE));
        vsBox.getChildren().add(vsLabel);

        SilverCornerPane opponentCard = buildPlayerCard(opponentName, opponentLevel, opponentAvatarId, false);

        players.getChildren().addAll(myCard, vsBox, opponentCard);

        // Đếm ngược
        VBox countBox = new VBox(2);
        countBox.setAlignment(Pos.CENTER);
        countdownLabel = new Label("13");
        countdownLabel.setFont(Font.font(Theme.FONT_FAMILY, FontWeight.BOLD, 42));
        countdownLabel.setTextFill(Color.web(Theme.HEX_TEXT));

        Label countdownHint = new Label("Trận đấu sẽ tự động bắt đầu sau giây đếm ngược...");
        countdownHint.setTextFill(Color.web(Theme.HEX_TEXT_MUTED));
        countdownHint.setFont(Font.font(Theme.FONT_FAMILY, 12));
        countBox.getChildren().addAll(countdownLabel, countdownHint);

        // Nút sẵn sàng (hình vuông)
        readyBtn = Theme.btnPrimary("Bắt đầu ngay", 220, 42);
        readyBtn.setOnAction(e -> {
            SoundManager.playButtonClick();
            sendReady();
        });

        cardContent.getChildren().addAll(title, players, countBox, readyBtn);
        root.getChildren().addAll(bg, card);

        scene = new Scene(root, 900, 620);
    }

    private SilverCornerPane buildPlayerCard(String name, int level, int avatarId, boolean isMe) {
        VBox card = new VBox(8);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(16, 20, 16, 20));

        StackPane avatar = AvatarManager.createAvatarNode(avatarId, 54);

        Label nameLabel = new Label(name);
        nameLabel.setFont(Font.font(Theme.FONT_FAMILY, FontWeight.BOLD, 15));
        nameLabel.setTextFill(Color.web(Theme.HEX_TEXT));

        Label lvLabel = new Label("Cấp độ " + level);
        lvLabel.setFont(Font.font(Theme.FONT_FAMILY, 12));
        lvLabel.setTextFill(Color.web(Theme.HEX_TEXT_MUTED));

        Label roleLabel = new Label(isMe ? "(Bạn)" : "(Đối thủ)");
        roleLabel.setTextFill(isMe ? Color.web(Theme.HEX_PRIMARY) : Color.web(Theme.HEX_TEXT_MUTED));
        roleLabel.setFont(Font.font(Theme.FONT_FAMILY, FontWeight.BOLD, 12));

        card.getChildren().addAll(avatar, nameLabel, lvLabel, roleLabel);

        SilverCornerPane cornerPane = new SilverCornerPane(10, 1.8, card);
        cornerPane.setCustomBackground(Theme.HEX_SURFACE_HOVER, isMe ? Theme.HEX_PRIMARY : Theme.HEX_BORDER);
        cornerPane.setMinWidth(200);
        return cornerPane;
    }

    private void startCountdown() {
        countdownTimer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            int left = secondsLeft.decrementAndGet();
            countdownLabel.setText(String.valueOf(left));
            if (left <= 4) {
                countdownLabel.setTextFill(Color.web(Theme.HEX_DANGER));
                SoundManager.playTimerTick();
            }
            if (left <= 0) {
                countdownTimer.stop();
                sendReady();
            }
        }));
        countdownTimer.setCycleCount(13);
        countdownTimer.play();
    }

    private synchronized void sendReady() {
        if (readySent) return;
        readySent = true;
        readyBtn.setDisable(true);
        readyBtn.setText("Đã sẵn sàng! Chờ đối thủ...");
        countdownTimer.stop();
        network.sendPregameReady();
    }

    private void setupNetworkCallback() {
        network.setOnPacketReceived(pkt -> {
            try {
                if (pkt.type == PacketType.GAME_START) {
                    int     playerIndex  = pkt.readInt();
                    boolean isFirstTurn  = pkt.readBoolean();
                    long    matchSeed;
                    try {
                        matchSeed = pkt.readLong();
                    } catch (Exception ignored) {
                        matchSeed = System.nanoTime();
                    }
                    long finalSeed = matchSeed;
                    Platform.runLater(() ->
                        sceneManager.showGame(myName, myAvatarId, myCueColor,
                                              opponentName, opponentAvatarId, opponentCueColor,
                                              playerIndex, isFirstTurn, finalSeed));
                }
            } catch (Exception e) {
                System.err.println("[PreGameScene] Lỗi: " + e.getMessage());
            }
        });
    }

    public Scene getScene() { return scene; }
}
