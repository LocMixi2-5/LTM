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
 * Màn hình Đăng Ký Tài Khoản mới:
 * - Khung hình vuông không bo tròn, tráng bạc 4 góc.
 */
public class RegisterScene {

    private final NetworkClient network;
    private final SceneManager  sceneManager;
    private Scene scene;

    private static final java.util.regex.Pattern EMAIL_PATTERN =
        java.util.regex.Pattern.compile("^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,}$");

    private TextField     emailField;
    private TextField     usernameField;
    private PasswordField passwordField;
    private PasswordField confirmField;
    private Label         errorLabel;

    public RegisterScene(NetworkClient network, SceneManager sceneManager) {
        this.network      = network;
        this.sceneManager = sceneManager;
        build();
        setupNetworkCallback();
    }

    private void build() {
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: " + Theme.BG_MAIN + ";");

        DragonCloudBackground bg = new DragonCloudBackground();

        // Card Đăng Ký Hình Vuông Tráng Bạc 4 Góc
        VBox cardContent = new VBox(16);
        cardContent.setAlignment(Pos.CENTER);
        cardContent.setPadding(new Insets(24, 36, 24, 36));

        SilverCornerPane card = new SilverCornerPane(cardContent);
        card.setMaxWidth(400);

        // Header
        StackPane ballBadge = createCueBallBadge();

        Label title = new Label("TẠO TÀI KHOẢN");
        title.setFont(Theme.fontTitle(20));
        title.setTextFill(Theme.C_TEXT_PRIMARY);

        Label subtitle = new Label("Đăng ký thành viên CLB Bi-a");
        subtitle.setFont(Theme.fontRegular(13));
        subtitle.setTextFill(Theme.C_TEXT_MUTED);

        VBox headerBox = new VBox(6, ballBadge, title, subtitle);
        headerBox.setAlignment(Pos.CENTER);

        // Form
        VBox form = new VBox(10);
        form.setAlignment(Pos.CENTER_LEFT);

        // Email
        VBox emailBox = new VBox(4);
        Label emailLabel = new Label("Email");
        emailLabel.setFont(Theme.fontMedium(12));
        emailLabel.setTextFill(Theme.C_TEXT_MUTED);

        emailField = new TextField();
        emailField.setPromptText("Ví dụ: cothu@gmail.com");
        Theme.styleTextInput(emailField);
        emailField.setOnAction(e -> usernameField.requestFocus());
        emailBox.getChildren().addAll(emailLabel, emailField);

        // Tên tài khoản
        VBox userBox = new VBox(4);
        Label userLabel = new Label("Tên đăng nhập (3 - 20 ký tự)");
        userLabel.setFont(Theme.fontMedium(12));
        userLabel.setTextFill(Theme.C_TEXT_MUTED);

        usernameField = new TextField();
        usernameField.setPromptText("Ví dụ: cothu_pro");
        Theme.styleTextInput(usernameField);
        usernameField.setOnAction(e -> passwordField.requestFocus());
        userBox.getChildren().addAll(userLabel, usernameField);

        // Mật khẩu
        VBox passBox = new VBox(4);
        Label passLabel = new Label("Mật khẩu (ít nhất 6 ký tự)");
        passLabel.setFont(Theme.fontMedium(12));
        passLabel.setTextFill(Theme.C_TEXT_MUTED);

        passwordField = new PasswordField();
        passwordField.setPromptText("Nhập mật khẩu");
        Theme.styleTextInput(passwordField);
        passwordField.setOnAction(e -> confirmField.requestFocus());
        passBox.getChildren().addAll(passLabel, passwordField);

        // Xác nhận mật khẩu
        VBox confBox = new VBox(4);
        Label confLabel = new Label("Xác nhận mật khẩu");
        confLabel.setFont(Theme.fontMedium(12));
        confLabel.setTextFill(Theme.C_TEXT_MUTED);

        confirmField = new PasswordField();
        confirmField.setPromptText("Nhập lại mật khẩu");
        Theme.styleTextInput(confirmField);
        confirmField.setOnAction(e -> doRegister());
        confBox.getChildren().addAll(confLabel, confirmField);

        // Thông báo lỗi
        errorLabel = new Label("");
        errorLabel.setTextFill(Theme.C_DANGER);
        errorLabel.setFont(Theme.fontMedium(12));
        errorLabel.setWrapText(true);
        errorLabel.setMinHeight(16);

        // Nút Đăng ký (Chính)
        Button registerBtn = new Button("Đăng ký tài khoản");
        registerBtn.setMaxWidth(Double.MAX_VALUE);
        registerBtn.setPrefHeight(40);
        Theme.stylePrimaryButton(registerBtn);
        registerBtn.setOnAction(e -> {
            SoundManager.playButtonClick();
            doRegister();
        });

        // Nút Quay lại Đăng nhập
        Button backBtn = new Button("← Quay lại Đăng nhập");
        backBtn.setMaxWidth(Double.MAX_VALUE);
        backBtn.setPrefHeight(36);
        Theme.styleSecondaryButton(backBtn);
        backBtn.setOnAction(e -> {
            SoundManager.playButtonClick();
            sceneManager.showLogin();
        });

        form.getChildren().addAll(emailBox, userBox, passBox, confBox, errorLabel, registerBtn, backBtn);
        cardContent.getChildren().addAll(headerBox, form);

        root.getChildren().addAll(bg, card);

        scene = new Scene(root, 960, 640);
        scene.setFill(Theme.C_BG_MAIN);
    }

    private StackPane createCueBallBadge() {
        Circle outer = new Circle(20, Color.web("#141D1A"));
        outer.setStroke(Theme.C_ACCENT_SILVER);
        outer.setStrokeWidth(1.5);

        Circle inner = new Circle(9, Color.web("#EDF1E8"));

        Text num = new Text("+");
        num.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        num.setFill(Color.web("#111816"));

        StackPane badge = new StackPane(outer, inner, num);
        badge.setPrefSize(44, 44);
        return badge;
    }

    private void doRegister() {
        String email = emailField.getText().trim();
        String u = usernameField.getText().trim();
        String p = passwordField.getText();
        String c = confirmField.getText();

        if (email.isEmpty() || u.isEmpty() || p.isEmpty() || c.isEmpty()) {
            errorLabel.setText("Vui lòng điền đầy đủ các thông tin.");
            return;
        }

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            errorLabel.setText("Địa chỉ email không hợp lệ (ví dụ: cothu@gmail.com).");
            emailField.requestFocus();
            return;
        }

        if (u.length() < 3 || u.length() > 20) {
            errorLabel.setText("Tên đăng nhập phải từ 3 đến 20 ký tự.");
            usernameField.requestFocus();
            return;
        }

        if (u.contains(" ")) {
            errorLabel.setText("Tên đăng nhập không được chứa khoảng trắng.");
            usernameField.requestFocus();
            return;
        }

        if (p.length() < 6) {
            errorLabel.setText("Mật khẩu phải có ít nhất 6 ký tự.");
            passwordField.requestFocus();
            return;
        }

        if (!p.equals(c)) {
            errorLabel.setText("Mật khẩu xác nhận không khớp!");
            confirmField.requestFocus();
            return;
        }

        if (network == null || !network.isConnected()) {
            errorLabel.setText("Chưa kết nối máy chủ! Hãy quay lại cấu hình IP.");
            return;
        }

        errorLabel.setTextFill(Theme.C_ACCENT_BRASS);
        errorLabel.setText("Đang đăng ký tài khoản...");
        network.sendRegister(email, u, p);
    }

    private void setupNetworkCallback() {
        network.setOnPacketReceived(pkt -> {
            try {
                if (pkt.type == PacketType.REGISTER_RESPONSE) {
                    boolean success = pkt.readBoolean();
                    String  msg     = pkt.readString();

                    Platform.runLater(() -> {
                        if (success) {
                            Alert alert = new Alert(Alert.AlertType.INFORMATION);
                            alert.setTitle("Đăng Ký Thành Công");
                            alert.setHeaderText(null);
                            alert.setContentText(msg + "\nBạn có thể đăng nhập ngay bây giờ!");
                            alert.showAndWait();
                            sceneManager.showLogin();
                        } else {
                            errorLabel.setTextFill(Theme.C_DANGER);
                            errorLabel.setText(msg);
                            SoundManager.playError();
                        }
                    });
                }
            } catch (IOException e) {
                System.err.println("[RegisterScene] Lỗi đọc packet: " + e.getMessage());
            }
        });
    }

    public Scene getScene() { return scene; }
}
