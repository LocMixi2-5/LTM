package com.billiards.client.scenes;

import com.billiards.client.NetworkClient;
import javafx.stage.Stage;

/**
 * Quản lý việc chuyển đổi giữa các màn hình (Scene).
 * 
 * Mỗi Scene được tạo mới khi cần, đảm bảo state sạch.
 * NetworkClient được truyền vào từng Scene để giao tiếp.
 */
public class SceneManager {

    private final Stage         primaryStage;
    private final NetworkClient network;

    public SceneManager(Stage primaryStage, NetworkClient network) {
        this.primaryStage = primaryStage;
        this.network      = network;
    }

    // ============================================================
    // CHUYỂN MÀN HÌNH
    // ============================================================

    /** Màn hình đăng nhập */
    public void showLogin() {
        LoginScene scene = new LoginScene(network, this);
        primaryStage.setScene(scene.getScene());
        primaryStage.setTitle("8-Ball Pool – Đăng Nhập");
    }

    /** Màn hình đăng ký */
    public void showRegister() {
        RegisterScene scene = new RegisterScene(network, this);
        primaryStage.setScene(scene.getScene());
        primaryStage.setTitle("8-Ball Pool – Đăng Ký");
    }

    /** Màn hình lobby chính */
    public void showLobby(String username, int exp, int level, int wins, int losses) {
        if (network != null && network.getSession() != null) {
            var sess = network.getSession();
            if (exp == 0 && level <= 1 && wins == 0 && losses == 0) {
                exp    = sess.exp;
                level  = sess.level;
                wins   = sess.wins;
                losses = sess.losses;
                if (sess.username != null && !sess.username.isEmpty()) {
                    username = sess.username;
                }
            }
        }
        LobbyScene scene = new LobbyScene(network, this, username, exp, level, wins, losses);
        primaryStage.setScene(scene.getScene());
        primaryStage.setTitle("8-Ball Pool – Lobby | " + username);
    }

    /** Màn hình đang tìm trận */
    public void showMatchmaking(String username) {
        MatchmakingScene scene = new MatchmakingScene(network, this, username);
        primaryStage.setScene(scene.getScene());
        primaryStage.setTitle("8-Ball Pool – Tìm Trận...");
    }

    public Stage getPrimaryStage() { return primaryStage; }

    /** Màn hình chuẩn bị trước game */
    public void showPreGame(String myName, int myLevel, String opponentName, int opponentLevel) {
        showPreGame(myName, myLevel, 0, "#DEB887", opponentName, opponentLevel, 0, "#DEB887");
    }

    public void showPreGame(String myName, int myLevel, int myAvatarId, String myCueColor,
                            String opponentName, int opponentLevel, int opponentAvatarId, String opponentCueColor) {
        PreGameScene scene = new PreGameScene(network, this,
                                              myName, myLevel, myAvatarId, myCueColor,
                                              opponentName, opponentLevel, opponentAvatarId, opponentCueColor);
        primaryStage.setScene(scene.getScene());
        primaryStage.setTitle("8-Ball Pool – Chuẩn Bị...");
    }

    /** Màn hình gameplay */
    public void showGame(String myName, String opponentName,
                         int playerIndex, boolean isFirstTurn) {
        showGame(myName, 0, "#DEB887", opponentName, 0, "#DEB887", playerIndex, isFirstTurn);
    }

    public void showGame(String myName, int myAvatarId, String myCueColor,
                         String opponentName, int opponentAvatarId, String opponentCueColor,
                         int playerIndex, boolean isFirstTurn) {
        showGame(myName, myAvatarId, myCueColor, opponentName, opponentAvatarId, opponentCueColor,
                 playerIndex, isFirstTurn, System.nanoTime() ^ ((long)(Math.random() * 1_000_000_000L) << 32));
    }

    public void showGame(String myName, int myAvatarId, String myCueColor,
                         String opponentName, int opponentAvatarId, String opponentCueColor,
                         int playerIndex, boolean isFirstTurn, long matchSeed) {
        GameScene scene = new GameScene(network, this,
                                        myName, myAvatarId, myCueColor,
                                        opponentName, opponentAvatarId, opponentCueColor,
                                        playerIndex, isFirstTurn, matchSeed);
        primaryStage.setScene(scene.getScene());
        primaryStage.setTitle("8-Ball Pool – " + myName + " vs " + opponentName);
    }

    /** Màn hình luyện tập */
    public void showPractice(String myName) {
        GameScene scene = new GameScene(network, this, myName);
        primaryStage.setScene(scene.getScene());
        primaryStage.setTitle("8-Ball Pool – Luyện Tập");
    }

    /** Màn hình kết quả */
    public void showResult(boolean youWin, int expGained, String myName, String opponentName) {
        ResultScene scene = new ResultScene(network, this, youWin, expGained, myName, opponentName);
        primaryStage.setScene(scene.getScene());
        primaryStage.setTitle("8-Ball Pool – " + (youWin ? "Chiến Thắng!" : "Thất Bại"));
    }

    /** Màn hình bảng xếp hạng */
    public void showLeaderboard(String myUsername, int exp, int level, int wins, int losses) {
        LeaderboardScene scene = new LeaderboardScene(network, this,
                                                      myUsername, exp, level, wins, losses);
        primaryStage.setScene(scene.getScene());
        primaryStage.setTitle("8-Ball Pool – Bảng Xếp Hạng");
    }
}
