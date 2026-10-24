package com.billiards.client.scenes;

import com.billiards.client.NetworkClient;
import com.billiards.client.audio.SoundManager;
import com.billiards.client.theme.Theme;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Hộp thoại cấu hình IP Máy Chủ (LAN / Mạng nội bộ) - Phong cách Modern Billiards Club.
 */
public class ServerConfigDialog {

    public static void show(Stage ownerStage, NetworkClient network, Runnable onConnectedCallback) {
        Stage dialog = new Stage();
        dialog.initOwner(ownerStage);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.TRANSPARENT);
        dialog.setTitle("Cấu hình máy chủ");

        VBox rootContent = new VBox(16);
        rootContent.setAlignment(Pos.CENTER);
        rootContent.setPadding(new Insets(24, 28, 24, 28));

        com.billiards.client.theme.SilverCornerPane root = new com.billiards.client.theme.SilverCornerPane(rootContent);
        root.setPrefWidth(460);

        // --- TIÊU ĐỀ ---
        Label titleLabel = new Label("Cấu hình máy chủ");
        titleLabel.setFont(Font.font(Theme.FONT_FAMILY, FontWeight.BOLD, 18));
        titleLabel.setTextFill(Color.web(Theme.HEX_TEXT));

        Label subtitleLabel = new Label("Kết nối máy chủ trong mạng LAN / Wi-Fi");
        subtitleLabel.setFont(Font.font(Theme.FONT_FAMILY, 12));
        subtitleLabel.setTextFill(Color.web(Theme.HEX_TEXT_MUTED));

        VBox headerBox = new VBox(4, titleLabel, subtitleLabel);
        headerBox.setAlignment(Pos.CENTER);

        // --- KHUNG IP MÁY NÀY ---
        String localLanIp = NetworkClient.getLocalLanIp();
        HBox myIpBox = new HBox(10);
        myIpBox.setAlignment(Pos.CENTER_LEFT);
        myIpBox.setPadding(new Insets(8, 12, 8, 12));
        myIpBox.setStyle(
            "-fx-background-color: #141D1A;" +
            "-fx-background-radius: 0;" +
            "-fx-border-color: " + Theme.HEX_BORDER + ";" +
            "-fx-border-radius: 0;" +
            "-fx-border-width: 1.0;"
        );

        Label myIpTitle = new Label("IP máy này:");
        myIpTitle.setFont(Font.font(Theme.FONT_FAMILY, FontWeight.BOLD, 12));
        myIpTitle.setTextFill(Color.web(Theme.HEX_TEXT_MUTED));

        Label myIpVal = new Label(localLanIp);
        myIpVal.setFont(Font.font("Consolas", FontWeight.BOLD, 13));
        myIpVal.setTextFill(Color.web(Theme.HEX_PRIMARY));

        Region myIpSpacer = new Region();
        HBox.setHgrow(myIpSpacer, Priority.ALWAYS);

        Button copyIpBtn = Theme.btnSecondary("Sao chép", 80, 28);
        copyIpBtn.setStyle(copyIpBtn.getStyle() + "; -fx-font-size: 11px; -fx-padding: 2 8;");
        copyIpBtn.setOnAction(e -> {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(localLanIp);
            clipboard.setContent(content);
            copyIpBtn.setText("Đã chép! ✓");
        });

        myIpBox.getChildren().addAll(myIpTitle, myIpVal, myIpSpacer, copyIpBtn);

        // --- NHẬP IP & PORT ---
        VBox formBox = new VBox(8);
        formBox.setAlignment(Pos.CENTER_LEFT);

        Label ipPrompt = new Label("Địa chỉ IP Server:");
        ipPrompt.setFont(Font.font(Theme.FONT_FAMILY, FontWeight.BOLD, 12));
        ipPrompt.setTextFill(Color.web(Theme.HEX_TEXT));

        TextField ipField = new TextField(network.getHost());
        ipField.setPromptText("Ví dụ: 192.168.1.15 hoặc 127.0.0.1");
        ipField.setStyle(Theme.inputStyle());

        Label portPrompt = new Label("Cổng (Port):");
        portPrompt.setFont(Font.font(Theme.FONT_FAMILY, FontWeight.BOLD, 12));
        portPrompt.setTextFill(Color.web(Theme.HEX_TEXT));

        TextField portField = new TextField(String.valueOf(network.getPort()));
        portField.setPromptText("12345");
        portField.setStyle(Theme.inputStyle());

        formBox.getChildren().addAll(ipPrompt, ipField, portPrompt, portField);

        // --- CÁC NÚT CHỌN NHANH & DÒ TỰ ĐỘNG ---
        HBox quickActionRow = new HBox(8);
        quickActionRow.setAlignment(Pos.CENTER);

        Button btnLocalhost = Theme.btnSecondary("Localhost", 90, 30);
        btnLocalhost.setStyle(btnLocalhost.getStyle() + "; -fx-font-size: 11px;");
        btnLocalhost.setOnAction(e -> {
            SoundManager.playButtonClick();
            ipField.setText("127.0.0.1");
        });

        Button btnUseMyIp = Theme.btnSecondary("IP máy này", 100, 30);
        btnUseMyIp.setStyle(btnUseMyIp.getStyle() + "; -fx-font-size: 11px;");
        btnUseMyIp.setOnAction(e -> {
            SoundManager.playButtonClick();
            ipField.setText(localLanIp);
        });

        Button btnScanLan = Theme.btnSecondary("Dò Server LAN", 120, 30);
        btnScanLan.setStyle(btnScanLan.getStyle() + "; -fx-font-size: 11px;");

        quickActionRow.getChildren().addAll(btnLocalhost, btnUseMyIp, btnScanLan);

        // --- DÒNG THÔNG BÁO TRẠNG THÁI ---
        Label statusLabel = new Label("💡 Bấm 'Dò Server LAN' để tự tìm máy chủ trong Wi-Fi.");
        statusLabel.setFont(Font.font(Theme.FONT_FAMILY, 12));
        statusLabel.setTextFill(Color.web(Theme.HEX_TEXT_MUTED));
        statusLabel.setWrapText(true);
        statusLabel.setAlignment(Pos.CENTER);

        btnScanLan.setOnAction(e -> {
            SoundManager.playButtonClick();
            btnScanLan.setDisable(true);
            btnScanLan.setText("Đang quét...");
            statusLabel.setTextFill(Color.web(Theme.HEX_BRONZE));
            statusLabel.setText("Đang quét mạng nội bộ để tìm máy chủ...");

            NetworkClient.autoDiscoverServerAsync(
                foundIp -> {
                    btnScanLan.setDisable(false);
                    btnScanLan.setText("Dò Server LAN");
                    ipField.setText(foundIp);
                    statusLabel.setTextFill(Color.web(Theme.HEX_PRIMARY));
                    statusLabel.setText("Đã tìm thấy Server LAN tại: " + foundIp);
                },
                () -> {
                    btnScanLan.setDisable(false);
                    btnScanLan.setText("Dò Server LAN");
                    statusLabel.setTextFill(Color.web(Theme.HEX_DANGER));
                    statusLabel.setText("Không tìm thấy Server tự động. Hãy nhập IP thủ công!");
                }
            );
        });

        // --- CÁC NÚT DƯỚI CÙNG ---
        HBox bottomActions = new HBox(12);
        bottomActions.setAlignment(Pos.CENTER_RIGHT);

        Button btnClose = Theme.btnSecondary("Đóng", 80, 36);
        btnClose.setOnAction(e -> {
            SoundManager.playButtonClick();
            dialog.close();
        });

        Button btnConnect = Theme.btnPrimary("Kết nối & Lưu", 140, 36);

        btnConnect.setOnAction(e -> {
            SoundManager.playButtonClick();
            String hostInput = ipField.getText().trim();
            String portInput = portField.getText().trim();

            if (hostInput.isEmpty()) {
                statusLabel.setTextFill(Color.web(Theme.HEX_DANGER));
                statusLabel.setText("Vui lòng nhập địa chỉ IP!");
                return;
            }

            int port = NetworkClient.DEFAULT_PORT;
            try {
                port = Integer.parseInt(portInput);
            } catch (NumberFormatException ex) {
                statusLabel.setTextFill(Color.web(Theme.HEX_DANGER));
                statusLabel.setText("Cổng (Port) không hợp lệ!");
                return;
            }

            btnConnect.setDisable(true);
            btnConnect.setText("Đang kết nối...");
            statusLabel.setTextFill(Color.web(Theme.HEX_BRONZE));
            statusLabel.setText("Đang thử kết nối tới " + hostInput + ":" + port + "...");

            final int finalPort = port;
            new Thread(() -> {
                boolean ok = network.connect(hostInput, finalPort);
                Platform.runLater(() -> {
                    btnConnect.setDisable(false);
                    btnConnect.setText("Kết nối & Lưu");
                    if (ok) {
                        statusLabel.setTextFill(Color.web(Theme.HEX_PRIMARY));
                        statusLabel.setText("Kết nối máy chủ thành công!");
                        if (onConnectedCallback != null) {
                            onConnectedCallback.run();
                        }
                        new Thread(() -> {
                            try { Thread.sleep(500); } catch (InterruptedException ignored) {}
                            Platform.runLater(dialog::close);
                        }).start();
                    } else {
                        statusLabel.setTextFill(Color.web(Theme.HEX_DANGER));
                        statusLabel.setText("Không thể kết nối! Đảm bảo Server đang chạy.");
                    }
                });
            }).start();
        });

        bottomActions.getChildren().addAll(btnClose, btnConnect);

        rootContent.getChildren().addAll(headerBox, myIpBox, formBox, quickActionRow, statusLabel, bottomActions);

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        dialog.setScene(scene);
        dialog.show();
    }
}
