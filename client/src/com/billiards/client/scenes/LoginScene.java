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
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.io.IOException;

/**
 * Màn hình Đăng Nhập phong cách Modern Billiards Club:
 * - Khung hình vuông không bo tròn, tráng bạc 4 góc (Silver Corners).
 * - Tông màu tối sâu lắng, typography cân đối.
 */
public class LoginScene {

    private final NetworkClient network;
    private final SceneManager  sceneManager;
    private Scene scene;

    private TextField     usernameField;
    private PasswordField passwordField;
    private Label         errorLabel;
    private Label         serverStatusLabel;
    private HBox          serverStatusBox;

    public LoginScene(NetworkClient network, SceneManager sceneManager) {
        this.network      = network;
        this.sceneManager = sceneManager;
        build();
        setupNetworkCallback();
    }

    private void build() {
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: " + Theme.BG_MAIN + ";");

        // 1. Nền phòng CLB bi-a hiện đại
        DragonCloudBackground bg = new DragonCloudBackground();

        // 2. Thẻ Đăng Nhập Hình Vuông Tráng Bạc 4 Góc
        VBox cardContent = new VBox(22);
        cardContent.setAlignment(Pos.CENTER);
        cardContent.setPadding(new Insets(32, 36, 32, 36));

        SilverCornerPane card = new SilverCornerPane(cardContent);
        card.setMaxWidth(400);

        // Header: Biểu tượng Bi số 8 & Tên CLB
        StackPane ballBadge = createCueBallBadge();

        Label title = new Label("CÂU LẠC BỘ BI-A");
        title.setFont(Theme.fontTitle(20));
        title.setTextFill(Theme.C_TEXT_PRIMARY);

        Label subtitle = new Label("Đăng nhập để vào sảnh thi đấu");
        subtitle.setFont(Theme.fontRegular(13));
        subtitle.setTextFill(Theme.C_TEXT_MUTED);

        VBox headerBox = new VBox(6, ballBadge, title, subtitle);
        headerBox.setAlignment(Pos.CENTER);

        // Form đăng nhập
        VBox form = new VBox(14);
        form.setAlignment(Pos.CENTER_LEFT);

        // Trường: Tên đăng nhập
        VBox userBox = new VBox(5);
        Label userLabel = new Label("Tên đăng nhập");
        userLabel.setFont(Theme.fontMedium(12));
        userLabel.setTextFill(Theme.C_TEXT_MUTED);

        usernameField = new TextField();
        usernameField.setPromptText("Nhập tên tài khoản của bạn");
        Theme.styleTextInput(usernameField);
        usernameField.setOnAction(e -> doLogin());
        userBox.getChildren().addAll(userLabel, usernameField);

        // Trường: Mật khẩu
        VBox passBox = new VBox(5);
        Label passLabel = new Label("Mật khẩu");
        passLabel.setFont(Theme.fontMedium(12));
        passLabel.setTextFill(Theme.C_TEXT_MUTED);

        passwordField = new PasswordField();
        passwordField.setPromptText("Nhập mật khẩu");
        Theme.styleTextInput(passwordField);
        passwordField.setOnAction(e -> doLogin());
        passBox.getChildren().addAll(passLabel, passwordField);

        // Nhãn thông báo lỗi / trạng thái
        errorLabel = new Label("");
        errorLabel.setTextFill(Theme.C_DANGER);
        errorLabel.setFont(Theme.fontMedium(12));
        errorLabel.setWrapText(true);
        errorLabel.setMinHeight(16);

        // Nút Đăng nhập (Chính)
        Button loginBtn = new Button("Đăng nhập");
        loginBtn.setMaxWidth(Double.MAX_VALUE);
        loginBtn.setPrefHeight(42);
        Theme.stylePrimaryButton(loginBtn);
        loginBtn.setOnAction(e -> {
            SoundManager.playButtonClick();
            doLogin();
        });

        // Nút Đăng ký (Phụ)
        Button registerBtn = new Button("Tạo tài khoản mới");
        registerBtn.setMaxWidth(Double.MAX_VALUE);
        registerBtn.setPrefHeight(40);
        Theme.styleSecondaryButton(registerBtn);
        registerBtn.setOnAction(e -> {
            SoundManager.playButtonClick();
            sceneManager.showRegister();
        });

        // Khu vực thông tin máy chủ
        serverStatusBox = new HBox(8);
        serverStatusBox.setAlignment(Pos.CENTER);
        serverStatusBox.setPadding(new Insets(10, 0, 0, 0));

        serverStatusLabel = new Label();
        serverStatusLabel.setFont(Theme.fontRegular(11));

        Button changeIpBtn = new Button("Đổi máy chủ");
        changeIpBtn.setFont(Theme.fontMedium(11));
        changeIpBtn.setStyle(
            "-fx-background-color: " + Theme.SURFACE_HOVER + ";" +
            "-fx-text-fill: " + Theme.ACCENT_BRASS + ";" +
            "-fx-border-color: " + Theme.BORDER + ";" +
            "-fx-border-radius: 0;" +
            "-fx-background-radius: 0;" +
            "-fx-padding: 3px 8px;" +
            "-fx-cursor: hand;"
        );
        changeIpBtn.setOnAction(e -> {
            SoundManager.playButtonClick();
            ServerConfigDialog.show(sceneManager.getPrimaryStage(), network, this::updateServerStatus);
        });

        serverStatusBox.getChildren().addAll(serverStatusLabel, changeIpBtn);
        updateServerStatus();

        form.getChildren().addAll(userBox, passBox, errorLabel, loginBtn, registerBtn, serverStatusBox);
        cardContent.getChildren().addAll(headerBox, form);

        root.getChildren().addAll(bg, card);

        scene = new Scene(root, 960, 640);
        scene.setFill(Theme.C_BG_MAIN);
    }

    /** Tạo biểu tượng bi 8 tối giản tinh tế */
    private StackPane createCueBallBadge() {
        Circle outer = new Circle(22, Color.web("#141D1A"));
        outer.setStroke(Theme.C_ACCENT_SILVER);
        outer.setStrokeWidth(1.5);

        Circle inner = new Circle(10, Color.web("#EDF1E8"));

        Text num = new Text("8");
        num.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        num.setFill(Color.web("#111816"));

        StackPane badge = new StackPane(outer, inner, num);
        badge.setPrefSize(48, 48);
        return badge;
    }

    public void updateServerStatus() {
        if (network != null && network.isConnected()) {
            serverStatusLabel.setText("● Máy chủ: " + network.getHost() + ":" + network.getPort());
            serverStatusLabel.setTextFill(Theme.C_SUCCESS);
        } else {
            serverStatusLabel.setText("○ Chưa kết nối tới máy chủ");
            serverStatusLabel.setTextFill(Theme.C_TEXT_MUTED);
        }
    }

    private void doLogin() {
        String u = usernameField.getText().trim();
        String p = passwordField.getText();

        if (u.isEmpty() || p.isEmpty()) {
            errorLabel.setText("Vui lòng điền đầy đủ tên đăng nhập và mật khẩu.");
            return;
        }

        if (network == null || !network.isConnected()) {
            errorLabel.setText("Chưa kết nối máy chủ! Hãy bấm 'Đổi máy chủ' để cấu hình.");
            return;
        }

        errorLabel.setTextFill(Theme.C_ACCENT_BRASS);
        errorLabel.setText("Đang đăng nhập...");
        network.sendLogin(u, p);
    }

    private void setupNetworkCallback() {
        network.setOnPacketReceived(pkt -> {
            try {
                if (pkt.type == PacketType.LOGIN_RESPONSE) {
                    boolean success  = pkt.readBoolean();
                    String  msg      = pkt.readString();

                    if (success) {
                        int exp      = pkt.readInt();
                        int level    = pkt.readInt();
                        int wins     = pkt.readInt();
                        int losses   = pkt.readInt();
                        int avatarId = pkt.readInt();
                        String cueColor = pkt.readString();

                        String user = usernameField.getText().trim();
                        network.setSession(user, exp, level, wins, losses, avatarId, cueColor);

                        Platform.runLater(() -> {
                            sceneManager.showLobby(user, exp, level, wins, losses);
                        });
                    } else {
                        Platform.runLater(() -> {
                            errorLabel.setTextFill(Theme.C_DANGER);
                            errorLabel.setText(msg);
                            SoundManager.playError();
                        });
                    }
                }
            } catch (IOException e) {
                System.err.println("[LoginScene] Lỗi đọc packet: " + e.getMessage());
            }
        });
    }

    public Scene getScene() { return scene; }
}
