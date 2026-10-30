package com.billiards.client.scenes;

import com.billiards.client.AvatarManager;
import com.billiards.client.NetworkClient;
import com.billiards.client.audio.SoundManager;
import com.billiards.client.protocol.PacketType;
import com.billiards.client.theme.SilverCornerPane;
import com.billiards.client.theme.Theme;
import com.billiards.client.vfx.DragonCloudBackground;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.scene.effect.DropShadow;
import javafx.util.Duration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Màn hình Sảnh Chờ (Lobby Scene) phong cách "Modern Billiards Club":
 * - Bố cục cân đối: Top bar gọn, Center tập trung vào Chơi trực tuyến & Luyện tập
 * - Khung Chat được tích hợp ngay trong cột bên phải (thay thế hoặc trượt cùng Danh sách bạn bè)
 *   giúp giải quyết triệt để lỗi mở chat làm bóp nghẹt Center và cắt chữ thống kê.
 * - Sửa lỗi ScrollPane chat không cho phép cuộn ngược lên đọc tin cũ.
 */
public class LobbyScene {

    private final NetworkClient network;
    private final SceneManager  sceneManager;
    private Scene scene;

    private final String username;
    private int exp;
    private int level;
    private int wins;
    private int losses;
    private int avatarId = 0;
    private String cueColor = "#DEB887";

    // UI Nodes
    private StackPane   root;
    private StackPane   avatarContainer;
    private Label       levelLabel;
    private ProgressBar expBar;
    private Label       musicTrackLabel;
    private Label       winsLabel, lossLabel, rateLabel;

    // Cột bên phải (Right Container) chứa cả Bạn bè & Chat
    private StackPane   rightContainer;
    private VBox        friendListPanel;
    private VBox        chatPanel;
    private VBox        friendListBox;
    private VBox        chatMessagesBox;
    private ScrollPane  chatScrollPane;
    private TextField   chatInput;
    private Label       chatTitleLabel;
    private String      currentChatFriend = null;
    private Timeline    friendRefreshTimeline;
    private String      lastFriendListData = null;
    private String      pendingFriendListData = null;
    private ContextMenu activeFriendMenu = null;

    // Quản lý lời mời thách đấu (Hiển thị đếm ngược 10s)
    private Stage activeChallengeDialog = null;
    private Timeline activeChallengeTimeline = null;
    private Label currentToast = null;
    private SequentialTransition currentToastAnim = null;

    public LobbyScene(NetworkClient network, SceneManager sceneManager,
                      String username, int exp, int level, int wins, int losses) {
        var session = (network != null) ? network.getSession() : null;
        this.network      = network;
        this.sceneManager = sceneManager;
        this.username     = (session != null && session.username != null && !session.username.isEmpty()) ? session.username : username;
        this.exp          = (session != null) ? session.exp : exp;
        this.level        = (session != null) ? session.level : level;
        this.wins         = (session != null) ? session.wins : wins;
        this.losses       = (session != null) ? session.losses : losses;

        if (session != null) {
            this.avatarId = session.avatarId;
            this.cueColor = (session.cueColor != null) ? session.cueColor : "#DEB887";
        }

        build();
        setupNetworkCallback();
        startFriendAutoRefresh();

        SoundManager.playBGM();

        if (network != null) {
            network.sendFriendListRequest();
            network.sendLeaderboardRequest(); // Tự động load ngầm bảng xếp hạng ngay khi vào sảnh chờ!
        }
    }

    private void startFriendAutoRefresh() {
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(this::startFriendAutoRefresh);
            return;
        }
        stopFriendAutoRefresh();
        friendRefreshTimeline = new Timeline(new KeyFrame(Duration.seconds(3.5), e -> {
            if (network != null && network.isConnected() && friendListPanel != null && friendListPanel.isVisible()) {
                network.sendFriendListRequest();
            }
        }));
        friendRefreshTimeline.setCycleCount(Timeline.INDEFINITE);
        friendRefreshTimeline.play();
    }

    private void stopFriendAutoRefresh() {
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(this::stopFriendAutoRefresh);
            return;
        }
        if (friendRefreshTimeline != null) {
            friendRefreshTimeline.stop();
            friendRefreshTimeline = null;
        }
    }

    private void build() {
        this.root = new StackPane();
        root.setStyle("-fx-background-color: " + Theme.BG_MAIN + ";");

        // 1. Nền không gian CLB
        DragonCloudBackground bg = new DragonCloudBackground();

        // 2. Bố cục chính
        BorderPane mainLayout = new BorderPane();
        mainLayout.setStyle("-fx-background-color: transparent;");

        // TOP: Thanh thông tin cá nhân & Thao tác
        HBox topBar = buildTopBar();
        mainLayout.setTop(topBar);

        // CENTER: Khu vực chính (Chơi trực tuyến, Luyện tập, Thống kê)
        VBox center = buildCenterArea();
        mainLayout.setCenter(center);

        // RIGHT: Sidebar tích hợp Danh sách Bạn bè & Chat
        rightContainer = new StackPane();
        rightContainer.setPrefWidth(300);
        rightContainer.setStyle(
            "-fx-background-color: " + Theme.SURFACE + ";" +
            "-fx-border-color: " + Theme.BORDER + ";" +
            "-fx-border-width: 0 0 0 1px;"
        );

        buildFriendListPanel();
        buildChatPanel();

        rightContainer.getChildren().addAll(chatPanel, friendListPanel);
        showFriendListView();

        mainLayout.setRight(rightContainer);

        root.getChildren().addAll(bg, mainLayout);
        scene = new Scene(root, 1060, 680);
        scene.setFill(Theme.C_BG_MAIN);

        // Áp dụng phong cách tối sang trọng cho ContextMenu và MenuItem
        try {
            String menuCss = "data:text/css," + java.net.URLEncoder.encode(
                ".context-menu { -fx-background-color: #1A2420; -fx-border-color: #34443B; -fx-border-width: 1px; -fx-border-radius: 6px; -fx-background-radius: 6px; -fx-padding: 4px; }" +
                ".menu-item { -fx-background-color: transparent; -fx-padding: 6px 12px; -fx-background-radius: 4px; }" +
                ".menu-item:hover, .menu-item:focused { -fx-background-color: #24322B; }" +
                ".menu-item .label { -fx-text-fill: #EDF1E8; -fx-font-size: 12px; }" +
                ".menu-item:disabled .label { -fx-opacity: 0.4; }",
                java.nio.charset.StandardCharsets.UTF_8
            );
            scene.getStylesheets().add(menuCss);
        } catch (Exception ignored) {}
    }

    // ============================================================
    // TOP BAR
    // ============================================================
    private HBox buildTopBar() {
        HBox bar = new HBox(16);
        bar.setPadding(new Insets(10, 20, 10, 20));
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setStyle(
            "-fx-background-color: " + Theme.SURFACE + ";" +
            "-fx-border-color: " + Theme.BORDER + ";" +
            "-fx-border-width: 0 0 1px 0;"
        );

        // Góc trái: Avatar & Cấp độ
        HBox leftInfo = new HBox(12);
        leftInfo.setAlignment(Pos.CENTER_LEFT);

        avatarContainer = new StackPane();
        refreshAvatarNode();
        avatarContainer.setStyle("-fx-cursor: hand;");
        Tooltip.install(avatarContainer, new Tooltip("Nhấp để chỉnh Avatar & Cây cơ"));
        avatarContainer.setOnMouseClicked(e -> openSettingsDialog());

        VBox userDetails = new VBox(3);
        Label nameLabel = new Label(username);
        nameLabel.setFont(Theme.fontMedium(15));
        nameLabel.setTextFill(Theme.C_TEXT_PRIMARY);

        HBox expRow = new HBox(6);
        expRow.setAlignment(Pos.CENTER_LEFT);

        levelLabel = new Label("Lv." + level);
        levelLabel.setFont(Theme.fontMedium(11));
        levelLabel.setTextFill(Theme.C_ACCENT_BRASS);

        expBar = new ProgressBar((double)(exp % 200) / 200.0);
        expBar.setPrefWidth(100);
        expBar.setPrefHeight(6);
        expBar.setStyle("-fx-accent: " + Theme.ACCENT_BRASS + "; -fx-control-inner-background: #141D1A;");

        expRow.getChildren().addAll(levelLabel, expBar);
        userDetails.getChildren().addAll(nameLabel, expRow);
        leftInfo.getChildren().addAll(avatarContainer, userDetails);

        Region spacer1 = new Region();
        HBox.setHgrow(spacer1, Priority.ALWAYS);

        // Giữa: Điều khiển nhạc nhỏ gọn
        HBox musicWidget = buildMusicPlayerWidget();

        Region spacer2 = new Region();
        HBox.setHgrow(spacer2, Priority.ALWAYS);

        // Góc phải: Các nút thao tác
        HBox rightButtons = new HBox(8);
        rightButtons.setAlignment(Pos.CENTER_RIGHT);

        Button findFriendBtn = new Button("Tìm bạn");
        Theme.styleSecondaryButton(findFriendBtn);
        findFriendBtn.setOnAction(e -> {
            SoundManager.playButtonClick();
            openFriendSearchDialog();
        });

        Button settingsBtn = new Button("Cài đặt");
        Theme.styleSecondaryButton(settingsBtn);
        settingsBtn.setOnAction(e -> {
            SoundManager.playButtonClick();
            openSettingsDialog();
        });

        Button logoutBtn = new Button("Đăng xuất");
        Theme.styleDangerButton(logoutBtn);
        logoutBtn.setOnAction(e -> {
            SoundManager.playButtonClick();
            handleLogout();
        });

        rightButtons.getChildren().addAll(findFriendBtn, settingsBtn, logoutBtn);
        bar.getChildren().addAll(leftInfo, spacer1, musicWidget, spacer2, rightButtons);
        return bar;
    }

    private HBox buildMusicPlayerWidget() {
        HBox widget = new HBox(8);
        widget.setAlignment(Pos.CENTER);
        widget.setPadding(new Insets(4, 12, 4, 12));
        widget.setStyle(
            "-fx-background-color: " + Theme.SURFACE_INPUT + ";" +
            "-fx-background-radius: 0;" +
            "-fx-border-color: " + Theme.BORDER + ";" +
            "-fx-border-radius: 0;"
        );

        Button playPauseBtn = new Button("🎵");
        styleMiniMusicBtn(playPauseBtn);
        playPauseBtn.setOnAction(e -> {
            if (SoundManager.isMuted()) {
                SoundManager.setMuted(false);
            } else {
                SoundManager.playBGM();
            }
            updateMusicLabel();
        });

        musicTrackLabel = new Label("🎵 " + SoundManager.getCurrentBgmName());
        musicTrackLabel.setFont(Theme.fontRegular(11));
        musicTrackLabel.setTextFill(Theme.C_TEXT_MUTED);
        musicTrackLabel.setMaxWidth(200);

        widget.getChildren().addAll(playPauseBtn, musicTrackLabel);
        return widget;
    }

    private void styleMiniMusicBtn(Button btn) {
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + Theme.TEXT_MUTED + "; -fx-font-size: 11px; -fx-cursor: hand; -fx-padding: 2px 4px;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-size: 11px; -fx-cursor: hand; -fx-padding: 2px 4px;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + Theme.TEXT_MUTED + "; -fx-font-size: 11px; -fx-cursor: hand; -fx-padding: 2px 4px;"));
    }

    private void updateMusicLabel() {
        if (musicTrackLabel != null) {
            musicTrackLabel.setText(SoundManager.getCurrentBgmName());
        }
    }

    // ============================================================
    // CENTER AREA (Tiêu đề, Minh họa, Nút hành động & Thống kê)
    // ============================================================
    private VBox buildCenterArea() {
        VBox center = new VBox(20);
        center.setAlignment(Pos.CENTER);
        center.setPadding(new Insets(24, 40, 24, 40));

        // Minh họa CLB: Canvas vẽ quả bi số 8 tinh tế
        Canvas emblemCanvas = new Canvas(100, 100);
        drawClubEmblem(emblemCanvas.getGraphicsContext2D(), 100, 100);

        // Tiêu đề CLB trang nhã
        Label clubTitle = new Label("CÂU LẠC BỘ BI-A");
        clubTitle.setFont(Theme.fontTitle(24));
        clubTitle.setTextFill(Theme.C_TEXT_PRIMARY);

        Label clubSub = new Label("8-Ball Pool Multiplayer • Phòng chờ thi đấu");
        clubSub.setFont(Theme.fontRegular(13));
        clubSub.setTextFill(Theme.C_TEXT_MUTED);

        VBox titleBox = new VBox(4, emblemCanvas, clubTitle, clubSub);
        titleBox.setAlignment(Pos.CENTER);

        // Cụm thẻ trung tâm bọc SilverCornerPane
        VBox cardContent = new VBox(18);
        cardContent.setAlignment(Pos.CENTER);
        cardContent.setPadding(new Insets(24, 32, 24, 32));

        // Thanh Thống kê nhỏ gọn hình vuông tráng bạc
        HBox statsContent = new HBox(18);
        statsContent.setAlignment(Pos.CENTER);
        statsContent.setPadding(new Insets(8, 16, 8, 16));

        winsLabel = new Label("Thắng: " + wins);
        winsLabel.setFont(Theme.fontMedium(13));
        winsLabel.setTextFill(Theme.C_ACCENT_BRASS);

        lossLabel = new Label("Thua: " + losses);
        lossLabel.setFont(Theme.fontMedium(13));
        lossLabel.setTextFill(Theme.C_TEXT_MUTED);

        int total = wins + losses;
        double pct = total > 0 ? (double) wins / total * 100.0 : 0.0;
        rateLabel = new Label(String.format("Tỷ lệ: %.1f%%", pct));
        rateLabel.setFont(Theme.fontMedium(13));
        rateLabel.setTextFill(Theme.C_TEXT_PRIMARY);

        statsContent.getChildren().addAll(winsLabel, new Separator(javafx.geometry.Orientation.VERTICAL),
                                          lossLabel, new Separator(javafx.geometry.Orientation.VERTICAL),
                                          rateLabel);

        com.billiards.client.theme.SilverCornerPane statsBox = new com.billiards.client.theme.SilverCornerPane(10, 1.8, statsContent);
        statsBox.setCustomBackground(Theme.SURFACE_INPUT, Theme.BORDER);
        statsBox.setMaxWidth(360);

        // Cụm nút hành động chính (Phân cấp rõ ràng, hình vuông góc cạnh)
        VBox actionBox = new VBox(10);
        actionBox.setAlignment(Pos.CENTER);

        // 1. Chơi trực tuyến (Nút chính lớn nhất)
        Button playBtn = new Button("Chơi trực tuyến");
        playBtn.setPrefSize(280, 44);
        playBtn.setFont(Theme.fontMedium(15));
        Theme.stylePrimaryButton(playBtn);
        playBtn.setOnAction(e -> {
            SoundManager.playButtonClick();
            closeActiveChallengeDialog();
            dismissCurrentToast();
            if (network != null) network.sendFindMatch();
            sceneManager.showMatchmaking(username);
        });

        // 2. Luyện tập (Offline)
        Button practiceBtn = new Button("Luyện tập");
        practiceBtn.setPrefSize(280, 40);
        practiceBtn.setFont(Theme.fontMedium(14));
        Theme.styleSecondaryButton(practiceBtn);
        practiceBtn.setOnAction(e -> {
            SoundManager.playButtonClick();
            closeActiveChallengeDialog();
            dismissCurrentToast();
            sceneManager.showPractice(username);
        });

        // 3. Bảng xếp hạng (Hành động phụ)
        Button lbBtn = new Button("Bảng xếp hạng");
        lbBtn.setPrefSize(280, 40);
        lbBtn.setFont(Theme.fontMedium(14));
        Theme.styleSecondaryButton(lbBtn);
        lbBtn.setOnAction(e -> {
            SoundManager.playButtonClick();
            closeActiveChallengeDialog();
            dismissCurrentToast();
            if (network != null) network.sendLeaderboardRequest();
            sceneManager.showLeaderboard(username, exp, level, wins, losses);
        });

        actionBox.getChildren().addAll(playBtn, practiceBtn, lbBtn);

        cardContent.getChildren().addAll(titleBox, statsBox, actionBox);

        com.billiards.client.theme.SilverCornerPane centerCard = new com.billiards.client.theme.SilverCornerPane(18, 2.5, cardContent);
        centerCard.setMaxWidth(460);

        center.getChildren().add(centerCard);
        return center;
    }

    /** Vẽ biểu tượng quả bi 8 phong cách Vintage Club */
    private void drawClubEmblem(GraphicsContext gc, double w, double h) {
        double cx = w / 2;
        double cy = h / 2;
        double r = 32.0;

        // Vòng viền đồng ngoài
        gc.setStroke(Theme.C_ACCENT_BRASS);
        gc.setLineWidth(2.0);
        gc.strokeOval(cx - r - 6, cy - r - 6, (r + 6) * 2, (r + 6) * 2);

        // Bóng bi số 8
        gc.setFill(Color.web("#141D1A"));
        gc.fillOval(cx - r, cy - r, r * 2, r * 2);

        // Tâm trắng
        gc.setFill(Theme.C_TEXT_PRIMARY);
        gc.fillOval(cx - 14, cy - 14, 28, 28);

        // Số 8
        gc.setFill(Color.web("#111816"));
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        gc.setTextAlign(javafx.scene.text.TextAlignment.CENTER);
        gc.fillText("8", cx, cy + 6);
    }

    // ============================================================
    // RIGHT PANEL: DANH SÁCH BẠN BÈ & CHAT TÍCH HỢP
    // ============================================================
    private void buildFriendListPanel() {
        friendListPanel = new VBox(10);
        friendListPanel.setPadding(new Insets(16, 14, 16, 14));
        friendListPanel.setStyle("-fx-background-color: transparent;");

        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Bạn bè");
        title.setFont(Theme.fontTitle(15));
        title.setTextFill(Theme.C_TEXT_PRIMARY);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button refreshBtn = new Button("🔄");
        refreshBtn.setTooltip(new Tooltip("Làm mới danh sách & trạng thái online"));
        refreshBtn.setStyle(
            "-fx-background-color: " + Theme.SURFACE_HOVER + ";" +
            "-fx-text-fill: " + Theme.TEXT_MUTED + ";" +
            "-fx-font-size: 11px;" +
            "-fx-padding: 3px 7px;" +
            "-fx-background-radius: 4px;" +
            "-fx-cursor: hand;"
        );
        refreshBtn.setOnAction(e -> {
            SoundManager.playButtonClick();
            lastFriendListData = null;
            if (network != null) {
                network.sendFriendListRequest();
            }
        });

        header.getChildren().addAll(title, spacer, refreshBtn);

        friendListBox = new VBox(6);
        Label loading = new Label("Đang tải danh sách...");
        loading.setTextFill(Theme.C_TEXT_MUTED);
        loading.setFont(Theme.fontRegular(12));
        friendListBox.getChildren().add(loading);

        ScrollPane scrollPane = new ScrollPane(friendListBox);
        scrollPane.setFitToWidth(true);
        Theme.styleScrollPane(scrollPane);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        friendListPanel.getChildren().addAll(header, scrollPane);
    }

    private void buildChatPanel() {
        chatPanel = new VBox(8);
        chatPanel.setPadding(new Insets(14, 12, 14, 12));
        chatPanel.setStyle("-fx-background-color: transparent;");

        // Header Chat
        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);

        Button backToFriendsBtn = new Button("←");
        backToFriendsBtn.setFont(Theme.fontMedium(14));
        backToFriendsBtn.setStyle(
            "-fx-background-color: " + Theme.SURFACE_HOVER + ";" +
            "-fx-text-fill: " + Theme.TEXT_PRIMARY + ";" +
            "-fx-border-color: " + Theme.BORDER + ";" +
            "-fx-border-radius: 0;" +
            "-fx-background-radius: 0;" +
            "-fx-padding: 4px 10px;" +
            "-fx-cursor: hand;"
        );
        backToFriendsBtn.setOnAction(e -> showFriendListView());

        chatTitleLabel = new Label("Chat");
        chatTitleLabel.setFont(Theme.fontMedium(14));
        chatTitleLabel.setTextFill(Theme.C_TEXT_PRIMARY);

        header.getChildren().addAll(backToFriendsBtn, chatTitleLabel);

        // Vùng tin nhắn
        chatMessagesBox = new VBox(8);
        chatMessagesBox.setPadding(new Insets(6));

        chatScrollPane = new ScrollPane(chatMessagesBox);
        chatScrollPane.setFitToWidth(true);
        Theme.styleScrollPane(chatScrollPane);
        VBox.setVgrow(chatScrollPane, Priority.ALWAYS);

        // Vùng nhập tin nhắn
        HBox inputArea = new HBox(6);
        inputArea.setAlignment(Pos.CENTER);

        chatInput = new TextField();
        chatInput.setPromptText("Nhập tin nhắn...");
        Theme.styleTextInput(chatInput);
        HBox.setHgrow(chatInput, Priority.ALWAYS);
        chatInput.setOnAction(e -> sendMessage());

        Button sendBtn = new Button("Gửi");
        Theme.stylePrimaryButton(sendBtn);
        sendBtn.setOnAction(e -> sendMessage());

        inputArea.getChildren().addAll(chatInput, sendBtn);
        chatPanel.getChildren().addAll(header, chatScrollPane, inputArea);
    }

    private void showFriendListView() {
        friendListPanel.setVisible(true);
        chatPanel.setVisible(false);
        currentChatFriend = null;
    }

    private void showChatView(String friendName) {
        if (!friendName.equals(currentChatFriend)) {
            chatMessagesBox.getChildren().clear();
            currentChatFriend = friendName;
            chatTitleLabel.setText("Chat: " + friendName);
            if (network != null) {
                network.sendChatHistoryRequest(friendName);
            }
        }
        friendListPanel.setVisible(false);
        chatPanel.setVisible(true);
        Platform.runLater(() -> chatScrollPane.setVvalue(1.0));
    }

    private void sendMessage() {
        String msg = chatInput.getText().trim();
        if (!msg.isEmpty() && currentChatFriend != null) {
            network.sendChatMessage(currentChatFriend, msg);
            String time = new java.text.SimpleDateFormat("HH:mm").format(new java.util.Date());
            appendChatMessage("Bạn", time, msg, true);
            chatInput.clear();
        }
    }

    private void appendChatMessage(String sender, String time, String message, boolean isMe) {
        HBox wrapper = new HBox();
        wrapper.setUserData(sender + "|" + time + "|" + message);
        wrapper.setAlignment(isMe ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

        VBox bubble = new VBox(2);
        bubble.setPadding(new Insets(6, 10, 6, 10));
        bubble.setStyle(
            "-fx-background-color: " + (isMe ? Theme.ACCENT_GREEN : Theme.SURFACE_HOVER) + ";" +
            "-fx-background-radius: 8px;" +
            "-fx-border-color: " + (isMe ? "transparent" : Theme.BORDER) + ";" +
            "-fx-border-radius: 8px;"
        );
        bubble.setMaxWidth(220);

        HBox timeRow = new HBox(4);
        timeRow.setAlignment(isMe ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        Label timeLabel = new Label(time);
        timeLabel.setFont(Theme.fontRegular(9));
        timeLabel.setTextFill(Theme.C_TEXT_MUTED);
        timeRow.getChildren().add(timeLabel);

        Label msgLabel = new Label(message);
        msgLabel.setFont(Theme.fontRegular(13));
        msgLabel.setTextFill(Theme.C_TEXT_PRIMARY);
        msgLabel.setWrapText(true);

        bubble.getChildren().addAll(msgLabel, timeRow);
        wrapper.getChildren().add(bubble);
        chatMessagesBox.getChildren().add(wrapper);

        // Cuộn xuống nhẹ nhàng khi có tin nhắn mới
        Platform.runLater(() -> chatScrollPane.setVvalue(1.0));
    }

    private static class FriendItemData {
        String name;
        int level;
        boolean isOnline;
        FriendItemData(String name, int level, boolean isOnline) {
            this.name = name;
            this.level = level;
            this.isOnline = isOnline;
        }
    }

    private void updateFriendList(String data) {
        Platform.runLater(() -> {
            // Nếu người dùng đang mở menu thao tác của bạn bè, hoãn vẽ lại để không làm mất menu (L07)
            if (activeFriendMenu != null && activeFriendMenu.isShowing()) {
                if (data != null && data.equals(lastFriendListData)) {
                    // Dữ liệu mới nhất lại trùng với dữ liệu đang hiển thị -> không cần cập nhật gì khi đóng menu
                    pendingFriendListData = null;
                } else {
                    pendingFriendListData = data;
                }
                return;
            }

            // Không vẽ lại nếu dữ liệu giống hệt lần cập nhật trước
            if (data != null && data.equals(lastFriendListData)) {
                return;
            }

            pendingFriendListData = null;
            lastFriendListData = data;
            friendListBox.getChildren().clear();

            if (data == null || data.isEmpty()) {
                Label noFriends = new Label("Chưa có bạn bè.\nNhấn 'Tìm bạn' để kết nối.");
                noFriends.setTextFill(Theme.C_TEXT_MUTED);
                noFriends.setFont(Theme.fontRegular(12));
                noFriends.setWrapText(true);
                noFriends.setPadding(new Insets(12));
                friendListBox.getChildren().add(noFriends);
                return;
            }

            String[] friends = data.split(";");
            List<FriendItemData> friendDataList = new ArrayList<>();
            for (String friendData : friends) {
                String[] parts = friendData.split("\\|");
                if (parts.length >= 3) {
                    String fName = parts[0];
                    int fLevel = 1;
                    try { fLevel = Integer.parseInt(parts[1]); } catch (Exception ignored) {}
                    boolean isOnline = false;
                    if (parts.length >= 4) {
                        isOnline = Boolean.parseBoolean(parts[3]);
                    } else {
                        isOnline = Boolean.parseBoolean(parts[2]);
                    }

                    friendDataList.add(new FriendItemData(fName, fLevel, isOnline));
                }
            }

            // Sắp xếp: Bạn bè trực tuyến lên đầu, sau đó theo bảng chữ cái
            friendDataList.sort((a, b) -> {
                if (a.isOnline != b.isOnline) {
                    return a.isOnline ? -1 : 1;
                }
                return a.name.compareToIgnoreCase(b.name);
            });

            for (FriendItemData f : friendDataList) {
                friendListBox.getChildren().add(createFriendItem(f.name, f.level, f.isOnline));
            }
        });
    }

    private HBox createFriendItem(String name, int fLevel, boolean isOnline) {
        HBox item = new HBox(8);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setPadding(new Insets(6, 10, 6, 10));
        item.setStyle(
            "-fx-background-color: " + (isOnline ? Theme.SURFACE_HOVER : Theme.SURFACE) + ";" +
            "-fx-background-radius: 6px;" +
            "-fx-border-color: " + (isOnline ? "rgba(34, 197, 94, 0.35)" : Theme.BORDER) + ";" +
            "-fx-border-radius: 6px;"
        );

        // Chấm trạng thái Online: Chấm xanh phát sáng khi trực tuyến (#22c55e), xám khi ngoại tuyến
        Circle statusDot = new Circle(4.5);
        if (isOnline) {
            statusDot.setFill(Color.web("#22c55e"));
            statusDot.setStroke(Color.web("#86efac"));
            statusDot.setStrokeWidth(1.2);
            statusDot.setEffect(new DropShadow(6, Color.web("#22c55e")));
        } else {
            statusDot.setFill(Color.web("#64748b"));
            statusDot.setStroke(Color.web("#475569"));
            statusDot.setStrokeWidth(0.8);
        }

        VBox info = new VBox(1);
        Label nameLbl = new Label(name);
        nameLbl.setFont(Theme.fontMedium(13));
        nameLbl.setTextFill(isOnline ? Theme.C_TEXT_PRIMARY : Theme.C_TEXT_MUTED);

        Label lvLbl = new Label("Lv." + fLevel + (isOnline ? " • Trực tuyến" : " • Ngoại tuyến"));
        lvLbl.setFont(Theme.fontRegular(10));
        lvLbl.setTextFill(isOnline ? Color.web("#4ade80") : Theme.C_TEXT_MUTED);
        info.getChildren().addAll(nameLbl, lvLbl);

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        // Nút mũi tên sổ xuống (▾) mở menu thao tác
        Button optBtn = new Button("▾");
        optBtn.setTooltip(new Tooltip("Tùy chọn"));
        optBtn.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: " + (isOnline ? Theme.TEXT_PRIMARY : Theme.TEXT_MUTED) + ";" +
            "-fx-font-size: 13px;" +
            "-fx-font-weight: bold;" +
            "-fx-cursor: hand;" +
            "-fx-padding: 2px 6px;" +
            "-fx-background-radius: 4px;"
        );
        optBtn.setOnMouseEntered(e -> optBtn.setStyle(
            "-fx-background-color: " + Theme.SURFACE_HOVER + ";" +
            "-fx-text-fill: " + Theme.TEXT_PRIMARY + ";" +
            "-fx-font-size: 13px;" +
            "-fx-font-weight: bold;" +
            "-fx-cursor: hand;" +
            "-fx-padding: 2px 6px;" +
            "-fx-background-radius: 4px;"
        ));
        optBtn.setOnMouseExited(e -> optBtn.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: " + (isOnline ? Theme.TEXT_PRIMARY : Theme.TEXT_MUTED) + ";" +
            "-fx-font-size: 13px;" +
            "-fx-font-weight: bold;" +
            "-fx-cursor: hand;" +
            "-fx-padding: 2px 6px;" +
            "-fx-background-radius: 4px;"
        ));

        ContextMenu optMenu = new ContextMenu();
        optMenu.setAutoHide(true);

        MenuItem chatItem = new MenuItem("💬 Nhắn tin");
        chatItem.setOnAction(e -> showChatView(name));

        MenuItem challengeItem = new MenuItem("⚔ Thách đấu");
        challengeItem.setDisable(!isOnline);
        challengeItem.setOnAction(e -> {
            SoundManager.playButtonClick();
            network.sendChallengeRequest(name);
            showChallengeSentNotification(name);
        });

        MenuItem removeItem = new MenuItem("✕ Xóa bạn");
        removeItem.setOnAction(e -> confirmRemoveFriend(name));

        optMenu.getItems().addAll(chatItem, challengeItem, removeItem);

        optBtn.setOnAction(e -> {
            if (optMenu.isShowing()) {
                optMenu.hide();
            } else {
                if (activeFriendMenu != null && activeFriendMenu.isShowing()) {
                    activeFriendMenu.hide();
                }
                activeFriendMenu = optMenu;
                optMenu.show(optBtn, Side.BOTTOM, -70, 4);
            }
        });

        optMenu.setOnHidden(e -> {
            if (activeFriendMenu == optMenu) {
                activeFriendMenu = null;
                if (pendingFriendListData != null) {
                    String pending = pendingFriendListData;
                    pendingFriendListData = null;
                    updateFriendList(pending);
                }
            }
        });

        item.getChildren().addAll(statusDot, info, sp, optBtn);
        return item;
    }

    private void showChallengeSentNotification(String targetName) {
        showToast("⚔ Đã gửi lời mời thách đấu tới " + targetName + "! Đang chờ phản hồi trong 10 giây...", 10.0);
    }

    private void dismissCurrentToast() {
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(this::dismissCurrentToast);
            return;
        }
        if (currentToast != null) {
            if (currentToastAnim != null) {
                currentToastAnim.stop();
                currentToastAnim = null;
            }
            if (root != null) {
                root.getChildren().remove(currentToast);
            }
            currentToast = null;
        }
    }

    private void showToast(String message) {
        showToast(message, 3.0);
    }

    private void showToast(String message, double durationSeconds) {
        if (root == null) return;
        Platform.runLater(() -> {
            dismissCurrentToast();

            Label toast = new Label(message);
            toast.setFont(Theme.fontMedium(13));
            toast.setTextFill(Color.web("#e2e8f0"));
            toast.setStyle(
                "-fx-background-color: rgba(17, 24, 22, 0.95);" +
                "-fx-border-color: #22c55e;" +
                "-fx-border-width: 1px;" +
                "-fx-border-radius: 6px;" +
                "-fx-background-radius: 6px;" +
                "-fx-padding: 8 16;"
            );
            toast.setEffect(new DropShadow(10, Color.rgb(0, 0, 0, 0.6)));
            StackPane.setAlignment(toast, Pos.TOP_CENTER);
            StackPane.setMargin(toast, new Insets(65, 0, 0, 0));

            root.getChildren().add(toast);
            currentToast = toast;

            FadeTransition fadeIn = new FadeTransition(Duration.millis(250), toast);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);

            PauseTransition stay = new PauseTransition(Duration.seconds(durationSeconds));

            FadeTransition fadeOut = new FadeTransition(Duration.millis(350), toast);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(e -> {
                if (currentToast == toast) {
                    root.getChildren().remove(toast);
                    currentToast = null;
                }
            });

            currentToastAnim = new SequentialTransition(fadeIn, stay, fadeOut);
            currentToastAnim.play();
        });
    }

    private void showChallengeReceivedDialog(String challenger) {
        closeActiveChallengeDialog();

        SoundManager.playTurnAlert();

        Stage dialog = new Stage();
        dialog.initOwner(sceneManager.getPrimaryStage());
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.TRANSPARENT);
        dialog.setAlwaysOnTop(true);
        dialog.setTitle("Lời mời thách đấu");

        VBox content = new VBox(14);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(24, 28, 24, 28));

        // Tiêu đề
        Label titleLabel = new Label("⚔ LỜI MỜI THÁCH ĐẤU 1v1");
        titleLabel.setFont(Font.font(Theme.FONT_FAMILY, FontWeight.BOLD, 17));
        titleLabel.setTextFill(Theme.C_TEXT_PRIMARY);

        Label subLabel = new Label("Thách đấu trực tiếp từ bạn bè");
        subLabel.setFont(Font.font(Theme.FONT_FAMILY, 12));
        subLabel.setTextFill(Theme.C_TEXT_MUTED);

        VBox headerBox = new VBox(4, titleLabel, subLabel);
        headerBox.setAlignment(Pos.CENTER);

        // Khung thông tin người thách đấu
        VBox challengerBox = new VBox(6);
        challengerBox.setAlignment(Pos.CENTER);
        challengerBox.setPadding(new Insets(12, 16, 12, 16));
        challengerBox.setStyle(
            "-fx-background-color: " + Theme.SURFACE_INPUT + ";" +
            "-fx-border-color: " + Theme.BORDER + ";" +
            "-fx-border-width: 1px;"
        );

        Label nameLabel = new Label(challenger);
        nameLabel.setFont(Font.font(Theme.FONT_FAMILY, FontWeight.BOLD, 16));
        nameLabel.setTextFill(Color.web("#86efac"));

        Label inviteLabel = new Label("muốn thách đấu một trận bi-a với bạn!");
        inviteLabel.setFont(Font.font(Theme.FONT_FAMILY, 13));
        inviteLabel.setTextFill(Theme.C_TEXT_PRIMARY);

        challengerBox.getChildren().addAll(nameLabel, inviteLabel);

        // Đếm ngược 10 giây
        final int TOTAL_SECONDS = 10;
        Label countdownLabel = new Label("⏳ Tự động từ chối sau: 10 giây");
        countdownLabel.setFont(Font.font(Theme.FONT_FAMILY, FontWeight.SEMI_BOLD, 13));
        countdownLabel.setTextFill(Theme.C_ACCENT_BRASS);

        ProgressBar countdownBar = new ProgressBar(1.0);
        countdownBar.setPrefWidth(300);
        countdownBar.setPrefHeight(6);
        countdownBar.setStyle("-fx-accent: " + Theme.ACCENT_BRASS + "; -fx-control-inner-background: #141D1A;");

        // Các nút bấm
        HBox buttonBox = new HBox(12);
        buttonBox.setAlignment(Pos.CENTER);

        Button acceptBtn = new Button("⚔ Đồng ý");
        acceptBtn.setPrefSize(140, 38);
        Theme.stylePrimaryButton(acceptBtn);

        Button declineBtn = new Button("✕ Từ chối");
        declineBtn.setPrefSize(140, 38);
        Theme.styleSecondaryButton(declineBtn);

        buttonBox.getChildren().addAll(acceptBtn, declineBtn);

        content.getChildren().addAll(headerBox, challengerBox, countdownLabel, countdownBar, buttonBox);

        SilverCornerPane cornerPane = new SilverCornerPane(14, 2.0, content);
        cornerPane.setCustomBackground(Theme.SURFACE, Theme.BORDER);
        cornerPane.setPrefWidth(390);

        Scene dialogScene = new Scene(cornerPane);
        dialogScene.setFill(Color.TRANSPARENT);
        dialog.setScene(dialogScene);

        final boolean[] responded = { false };
        Consumer<Boolean> sendResponse = accept -> {
            if (responded[0]) return;
            responded[0] = true;
            closeActiveChallengeDialog();
            network.sendChallengeResponse(challenger, accept);
            if (!accept) {
                showToast("✕ Đã từ chối lời mời thách đấu.");
            }
        };

        // Timeline đếm ngược 10 giây
        final long startTimeMillis = System.currentTimeMillis();
        final int TOTAL_MILLIS = TOTAL_SECONDS * 1000;
        activeChallengeTimeline = new Timeline(new KeyFrame(Duration.millis(100), e -> {
            long elapsed = System.currentTimeMillis() - startTimeMillis;
            double progress = 1.0 - ((double) elapsed / TOTAL_MILLIS);
            countdownBar.setProgress(Math.max(0.0, Math.min(1.0, progress)));

            int remaining = Math.max(0, (int) Math.ceil((double) (TOTAL_MILLIS - elapsed) / 1000.0));
            countdownLabel.setText("⏳ Tự động từ chối sau: " + remaining + " giây");
        }));
        activeChallengeTimeline.setCycleCount(TOTAL_SECONDS * 10);
        activeChallengeTimeline.setOnFinished(e -> {
            if (!responded[0]) {
                responded[0] = true;
                closeActiveChallengeDialog();
                network.sendChallengeResponse(challenger, false);
                showToast("⌛ Đã hết 10 giây, lời mời thách đấu từ " + challenger + " đã hết hạn.");
            }
        });

        acceptBtn.setOnAction(e -> {
            SoundManager.playButtonClick();
            sendResponse.accept(true);
        });

        declineBtn.setOnAction(e -> {
            SoundManager.playButtonClick();
            sendResponse.accept(false);
        });

        dialog.setOnCloseRequest(e -> sendResponse.accept(false));

        activeChallengeDialog = dialog;
        dialog.show();
        dialog.toFront();
        dialog.requestFocus();

        Stage owner = sceneManager.getPrimaryStage();
        if (owner != null) {
            double w = dialog.getWidth() > 0 ? dialog.getWidth() : 390;
            double h = dialog.getHeight() > 0 ? dialog.getHeight() : 250;
            dialog.setX(owner.getX() + Math.max(0, (owner.getWidth() - w) / 2));
            dialog.setY(owner.getY() + Math.max(0, (owner.getHeight() - h) / 2));
        }

        activeChallengeTimeline.play();
    }

    private void closeActiveChallengeDialog() {
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(this::closeActiveChallengeDialog);
            return;
        }
        if (activeChallengeTimeline != null) {
            activeChallengeTimeline.stop();
            activeChallengeTimeline = null;
        }
        if (activeChallengeDialog != null) {
            if (activeChallengeDialog.isShowing()) {
                activeChallengeDialog.close();
            }
            activeChallengeDialog = null;
        }
    }

    private void confirmRemoveFriend(String friendName) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận");
        alert.setHeaderText(null);
        alert.setContentText("Bạn có chắc chắn muốn xóa " + friendName + " khỏi danh sách bạn bè?");
        ButtonType btnYes = new ButtonType("Xóa", ButtonBar.ButtonData.YES);
        ButtonType btnNo = new ButtonType("Hủy", ButtonBar.ButtonData.NO);
        alert.getButtonTypes().setAll(btnYes, btnNo);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == btnYes) {
            lastFriendListData = null;
            network.sendRemoveFriend(friendName);
            if (friendName.equals(currentChatFriend)) {
                showFriendListView();
            }
        }
    }

    private void handleLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.initOwner(sceneManager.getPrimaryStage());
        alert.setTitle("Đăng xuất");
        alert.setHeaderText("Xác nhận đăng xuất");
        alert.setContentText("Bạn có chắc chắn muốn đăng xuất và trở về màn hình đăng nhập?");
        ButtonType btnYes = new ButtonType("Đăng xuất", ButtonBar.ButtonData.YES);
        ButtonType btnNo  = new ButtonType("Hủy", ButtonBar.ButtonData.NO);
        alert.getButtonTypes().setAll(btnYes, btnNo);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == btnYes) {
            closeActiveChallengeDialog();
            dismissCurrentToast();
            stopFriendAutoRefresh();
            network.disconnect();
            network.connect();
            sceneManager.showLogin();
        }
    }

    private void refreshAvatarNode() {
        avatarContainer.getChildren().clear();
        avatarContainer.getChildren().add(AvatarManager.createAvatarNode(avatarId, 38));
    }

    private void openSettingsDialog() {
        SettingsDialog dialog = new SettingsDialog(sceneManager.getPrimaryStage(), network, (newAvatarId, newCueColor) -> {
            this.avatarId = newAvatarId;
            this.cueColor = newCueColor;
            refreshAvatarNode();
            updateMusicLabel();
        });
        dialog.show();
    }

    private void openFriendSearchDialog() {
        FriendSearchDialog dialog = new FriendSearchDialog(sceneManager.getPrimaryStage(), network);
        dialog.setOnFriendListChanged(() -> {});
        dialog.show();

        dialog.getDialog().setOnHidden(e -> {
            setupNetworkCallback();
            startFriendAutoRefresh();
            lastFriendListData = null;
            network.sendFriendListRequest();
        });
    }

    private void setupNetworkCallback() {
        network.setOnPacketReceived(pkt -> {
            try {
                switch (pkt.type) {
                    case MATCH_FOUND -> {
                        String opponentName     = pkt.readString();
                        int    opponentLevel    = pkt.readInt();
                        int    opponentAvatarId = pkt.readInt();
                        String opponentCueColor = pkt.readString();
                        Platform.runLater(() -> {
                            stopFriendAutoRefresh();
                            closeActiveChallengeDialog();
                            dismissCurrentToast();
                            sceneManager.showPreGame(username, level, avatarId, cueColor,
                                                     opponentName, opponentLevel, opponentAvatarId, opponentCueColor);
                        });
                    }
                    case FRIEND_LIST_RESPONSE -> {
                        String data = pkt.readString();
                        updateFriendList(data);
                    }
                    case REMOVE_FRIEND_RESPONSE -> {
                        boolean success = pkt.readBoolean();
                        String message = pkt.readString();
                        Platform.runLater(() -> {
                            if (success) {
                                network.sendFriendListRequest();
                            } else {
                                Alert alert = new Alert(Alert.AlertType.ERROR);
                                alert.setTitle("Lỗi");
                                alert.setHeaderText(null);
                                alert.setContentText(message);
                                alert.showAndWait();
                            }
                        });
                    }
                    case CHAT_MESSAGE_RESPONSE -> {
                        String senderUsername = pkt.readString();
                        String message = pkt.readString();
                        String time = new java.text.SimpleDateFormat("HH:mm").format(new java.util.Date());
                        Platform.runLater(() -> {
                            if (!senderUsername.equals(currentChatFriend)) {
                                // Mở view chat sẽ tự động gửi yêu cầu lấy toàn bộ lịch sử (bao gồm tin nhắn mới này)
                                showChatView(senderUsername);
                            } else {
                                appendChatMessage(senderUsername, time, message, false);
                            }
                        });
                    }
                    case CHAT_HISTORY_RESPONSE -> {
                        String friendUsername = pkt.readString();
                        String historyData = pkt.readString();
                        Platform.runLater(() -> {
                            // Chỉ áp dụng lịch sử nếu đúng là cuộc trò chuyện đang mở hiện tại (L06)
                            if (currentChatFriend == null || !currentChatFriend.equals(friendUsername)) {
                                return;
                            }

                            // Lưu giữ các tin nhắn do mình vừa gõ gửi trong lúc đợi history phản hồi
                            List<String> unsavedMyMessages = new ArrayList<>();
                            for (Node child : chatMessagesBox.getChildren()) {
                                if (child.getUserData() != null) {
                                    String data = (String) child.getUserData();
                                    if (data.startsWith("Bạn|")) {
                                        unsavedMyMessages.add(data);
                                    }
                                }
                            }

                            chatMessagesBox.getChildren().clear();

                            if (!historyData.isEmpty()) {
                                String[] messages = historyData.split(";");
                                for (String msgStr : messages) {
                                    String[] parts = msgStr.split("\\|", 3);
                                    if (parts.length == 3) {
                                        String sender = parts[0];
                                        String time = parts[1];
                                        String content = parts[2];
                                        boolean isMe = sender.equals(username);
                                        appendChatMessage(isMe ? "Bạn" : sender, time, content, isMe);
                                        if (isMe) {
                                            for (int i = 0; i < unsavedMyMessages.size(); i++) {
                                                if (unsavedMyMessages.get(i).endsWith("|" + content)) {
                                                    unsavedMyMessages.remove(i);
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Khôi phục các tin nhắn vừa gửi chưa kịp ghi vào DB lúc history được tải
                            for (String unsaved : unsavedMyMessages) {
                                String[] parts = unsaved.split("\\|", 3);
                                if (parts.length == 3) {
                                    appendChatMessage("Bạn", parts[1], parts[2], true);
                                }
                            }

                            chatScrollPane.setVvalue(1.0);
                        });
                    }
                    case CHALLENGE_RECEIVE -> {
                        String challenger = pkt.readString();
                        Platform.runLater(() -> showChallengeReceivedDialog(challenger));
                    }
                    case CHALLENGE_RESULT -> {
                        boolean success = pkt.readBoolean();
                        String message = pkt.readString();
                        Platform.runLater(() -> {
                            closeActiveChallengeDialog();
                            dismissCurrentToast();
                            showToast((success ? "⚔ " : "✕ ") + message, 4.0);
                        });
                    }
                    default -> {}
                }
            } catch (Exception e) {
                System.err.println("[LobbyScene] Lỗi xử lý gói tin: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    public Scene getScene() {
        return scene;
    }
}
