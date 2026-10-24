package com.billiards.client;

import com.billiards.client.scenes.SceneManager;
import com.billiards.client.scenes.ServerConfigDialog;
import com.billiards.client.audio.SoundManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

/**
 * Điểm khởi động ứng dụng Client.
 * 
 * Quy trình:
 * 1. Tạo JavaFX window
 * 2. Kết nối tới server (IP lưu trong server_ip.txt hoặc mặc định)
 * 3. Hiển thị màn hình đăng nhập (nếu chưa kết nối được, mở hộp thoại cấu hình IP)
 */
public class ClientMain extends Application {

    private NetworkClient network;

    @Override
    public void start(Stage primaryStage) {
        // Khoi tao am thanh va phat nhac nen
        SoundManager.init();
        SoundManager.playBGM();
        
        // Thiết lập window
        primaryStage.setTitle("🎱 8-Ball Pool Multiplayer");
        primaryStage.setResizable(false);
        primaryStage.setOnCloseRequest(e -> {
            if (network != null) network.disconnect();
            Platform.exit();
            System.exit(0);
        });

        // Kết nối server (đọc từ server_ip.txt hoặc mặc định)
        network = new NetworkClient();
        boolean connected = network.connect();

        // Tạo SceneManager và hiển thị màn hình đăng nhập
        SceneManager sceneManager = new SceneManager(primaryStage, network);
        sceneManager.showLogin();

        primaryStage.show();
        System.out.println("[Client] Ứng dụng khởi động thành công!");

        // Nếu chưa kết nối được ngay từ đầu, tự động mở hộp thoại cấu hình IP
        if (!connected) {
            Platform.runLater(() -> {
                ServerConfigDialog.show(primaryStage, network, null);
            });
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
