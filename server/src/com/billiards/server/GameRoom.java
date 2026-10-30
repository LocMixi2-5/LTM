package com.billiards.server;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Phòng game cho 2 người chơi.
 * 
 * Server đóng vai trò TRỌNG TÀI:
 * - Nhận cú đánh từ người đang có lượt
 * - Broadcast cú đánh tới cả 2 client (mỗi client tự chạy vật lý)
 * - Nhận thông báo kết thúc lượt → chuyển lượt
 * - Xử lý disconnect
 * 
 * Luật 8-ball đơn giản hoá cho Server:
 * - Server chỉ cần biết: ai đến lượt, game đã kết thúc chưa
 * - Toàn bộ vật lý chạy ở Client
 * - Client gửi lên: CUE_SHOT (angle, force) khi đánh
 * - Server forward tới opponent → opponent chạy vật lý → sync
 */
public class GameRoom {

    private static final int EXP_GAIN = 100;

    private final ClientHandler player1;
    private final ClientHandler player2;
    private final DatabaseManager db;

    // 0 = player1's turn, 1 = player2's turn
    private final AtomicInteger currentTurnIndex = new AtomicInteger(0);

    private boolean gameStarted  = false;
    private boolean gameFinished = false;

    // Scheduler quan ly dem nguoc 60s cho luot danh
    private final ScheduledExecutorService turnScheduler = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> turnTimeoutFuture;

    // Danh sách người chơi đã gửi PREGAME_READY (ngăn 1 người gửi 2 lần mở trận)
    private final Set<ClientHandler> readyPlayers = new HashSet<>();

    // Trạng thái đang diễn ra cú đánh (chặn gửi lặp lại cú đánh trong cùng 1 lượt)
    private boolean shotInProgress = false;

    public GameRoom(ClientHandler player1, ClientHandler player2, DatabaseManager db) {
        this.player1 = player1;
        this.player2 = player2;
        this.db = db;
    }

    // ============================================================
    // PRE-GAME: Đếm ngược và sẵn sàng
    // ============================================================

    /**
     * Được gọi khi một người nhấn "Bắt Đầu Ngay" (hoặc timeout đếm ngược ở client).
     * Chỉ bắt đầu trận khi CẢ 2 người chơi hợp lệ trong phòng đều đã sẵn sàng.
     */
    public synchronized void playerReady(ClientHandler client) {
        if (gameStarted) return;
        if (client != player1 && client != player2) return;

        if (readyPlayers.add(client)) {
            System.out.println("[Room] " + client.getPlayerInfo().username + " đã sẵn sàng. ("
                    + readyPlayers.size() + "/2)");
        }

        if (readyPlayers.size() >= 2) {
            startGame();
        }
    }

    /**
     * Client tự đếm ngược 13s rồi gọi PREGAME_READY → server nhận đủ 2 → start.
     * Nhưng để đơn giản, chúng ta tự động start sau khi 1 người sẵn sàng + timeout.
     * Thực tế: client gửi PREGAME_READY sau 13s (hoặc khi nhấn nút).
     * Server start khi CẢ 2 gửi, hoặc server tự start sau timeout.
     * 
     * Giải pháp đơn giản: start khi 1 trong 2 gửi (server quyết định).
     * Chỉnh lại: start khi cả 2 gửi PREGAME_READY.
     */
    public void startGame() {
        if (gameStarted) return;
        gameStarted = true;
        shotInProgress = false;
        currentTurnIndex.set(0); // player1 đi trước

        long matchSeed = System.nanoTime() ^ ((long) (Math.random() * 1_000_000_000L) << 32);

        System.out.println("[Room] Game bắt đầu! " + player1.getPlayerInfo().username
                + " vs " + player2.getPlayerInfo().username + " (MatchSeed=" + matchSeed + ")");

        // Gửi GAME_START cho cả 2 kèm matchSeed để cả 2 client xáo ngẫu nhiên đồng bộ 100%
        // playerIndex 0 = player1 (đánh trước), 1 = player2 (chờ)
        player1.sendGameStart(0, true, matchSeed);
        player2.sendGameStart(1, false, matchSeed);
        resetTurnTimer();
    }

    // ============================================================
    // XỬ LÝ CÚ ĐÁNH (CUE_SHOT)
    // ============================================================

    /**
     * Client gửi CUE_SHOT → server kiểm tra có phải lượt họ không
     * → chuyển tiếp CÚ ĐÁNH tới ĐỐI THỦ (đối thủ chạy mô phỏng vật lý).
     * Người bắn KHÔNG nhận lại GAME_STATE để tránh bắn 2 lần.
     */
    public synchronized void processCueShot(ClientHandler shooter, double angle, double force, double cueX, double cueY, double spinX, double spinY) {
        if (!gameStarted || gameFinished) return;

        // Kiểm tra hợp lệ các thông số số thực (chặn NaN / Infinity tránh lỗi mô phỏng vật lý)
        if (!Double.isFinite(angle) || !Double.isFinite(force)
                || !Double.isFinite(cueX) || !Double.isFinite(cueY)
                || !Double.isFinite(spinX) || !Double.isFinite(spinY)) {
            System.err.println("[Room] " + shooter.getPlayerInfo().username
                    + " gửi tham số cú đánh không hợp lệ (NaN/Infinity). Bỏ qua.");
            return;
        }
        if (force < 0) return;

        // Kiem tra dung luot chua
        ClientHandler currentPlayer = currentTurnIndex.get() == 0 ? player1 : player2;
        if (shooter != currentPlayer) {
            System.out.println("[Room] " + shooter.getPlayerInfo().username
                    + " danh khong dung luot, bo qua.");
            return;
        }

        // Chặn gửi nhiều cú đánh lặp lại trong khi cú đánh trước đang diễn ra
        if (shotInProgress) {
            System.out.println("[Room] " + shooter.getPlayerInfo().username
                    + " gửi cú đánh lặp khi cú đánh trước chưa xong. Bỏ qua.");
            return;
        }
        shotInProgress = true;

        System.out.println("[Room] " + shooter.getPlayerInfo().username
                + " danh: angle=" + String.format("%.2f", angle)
                + " force=" + String.format("%.2f", force)
                + " spinX=" + String.format("%.2f", spinX)
                + " spinY=" + String.format("%.2f", spinY));

        cancelTurnTimer();

        // CHỈ gửi cho ĐỐI THỦ (người bắn đã tự chạy physics cục bộ)
        ClientHandler opponent = (shooter == player1) ? player2 : player1;
        opponent.sendGameState(angle, force, cueX, cueY, spinX, spinY);

        // KHONG chuyen luot ngay - doi PHYSICS_DONE tu shooter
    }

    /**
     * Client gửi tọa độ bi cái vừa đặt (CUE_BALL_PLACED) khi thả bi cái (Ball-in-hand hoặc phá bi).
     * Chuyển tiếp tọa độ này sang cho đối thủ xem trước khi bắn.
     */
    public synchronized void processCueBallPlaced(ClientHandler sender, double cueX, double cueY) {
        if (!gameStarted || gameFinished || shotInProgress) return;
        if (!Double.isFinite(cueX) || !Double.isFinite(cueY)) return;

        // Chỉ cho phép người đang có lượt đặt bi
        ClientHandler currentPlayer = currentTurnIndex.get() == 0 ? player1 : player2;
        if (sender != currentPlayer) return;

        ClientHandler opponent = (sender == player1) ? player2 : player1;
        opponent.sendCueBallPlaced(cueX, cueY);
    }

    /**
     * Client gửi trạng thái ngắm cơ và kéo lực thời gian thực (CUE_AIM).
     * Chuyển tiếp ngay lập tức tới đối thủ để render animation gậy và đường ngắm y hệt.
     */
    public synchronized void processCueAim(ClientHandler sender, boolean isAiming, double angle, double forceRatio, double spinX, double spinY) {
        if (!gameStarted || gameFinished || shotInProgress) return;
        if (!Double.isFinite(angle) || !Double.isFinite(forceRatio)
                || !Double.isFinite(spinX) || !Double.isFinite(spinY)) return;

        // Chỉ cho phép người đang có lượt gửi trạng thái ngắm
        ClientHandler currentPlayer = currentTurnIndex.get() == 0 ? player1 : player2;
        if (sender != currentPlayer) return;

        ClientHandler opponent = (sender == player1) ? player2 : player1;
        if (opponent != null) {
            opponent.sendCueAim(isAiming, angle, forceRatio, spinX, spinY);
        }
    }

    /** Chuyen luot danh */
    private void switchTurn(boolean isFoul, String reason) {
        shotInProgress = false;
        int newTurn = currentTurnIndex.get() == 0 ? 1 : 0;
        currentTurnIndex.set(newTurn);

        ClientHandler nextPlayer = newTurn == 0 ? player1 : player2;
        ClientHandler waiting    = newTurn == 0 ? player2 : player1;

        nextPlayer.sendTurnChange(true,  isFoul, isFoul ? "⚠️ Đối thủ phạm lỗi! Bạn được đặt bi tự do (Ball-in-hand)!" : "🎯 Đến lượt bạn!");
        waiting.sendTurnChange(false, false, isFoul ? "💀 Bạn phạm lỗi! Lượt chuyển sang đối thủ." : "⏳ Chờ đối thủ đánh...");

        System.out.println("[Room] Chuyen luot sang: " + nextPlayer.getPlayerInfo().username + " (isFoul=" + isFoul + ")");
        resetTurnTimer();
    }

    /**
     * Duoc goi khi client bao physics da xong (PHYSICS_DONE).
     * Chi chuyen luot neu nguoi gui la nguoi dang co luot.
     * @param keepTurn true neu nguoi choi danh vao bi muc tieu va khong loi, duoc tiep tuc danh
     * @param isFoul true neu nguoi choi pham loi (bi trang vao lo, khong cham bi)
     */
    public synchronized void onPhysicsDone(ClientHandler sender, boolean keepTurn, boolean isFoul) {
        if (!gameStarted || gameFinished) return;

        ClientHandler currentPlayer = currentTurnIndex.get() == 0 ? player1 : player2;
        if (sender != currentPlayer) return; // Bo qua neu sai nguoi

        shotInProgress = false;

        if (keepTurn) {
            System.out.println("[Room] Physics xong - " + sender.getPlayerInfo().username + " danh vao bi, tiep tuc luot.");
            sender.sendTurnChange(true, false, "Bạn được đánh tiếp!");
            ClientHandler waiting = (sender == player1) ? player2 : player1;
            waiting.sendTurnChange(false, false, "Đối thủ được đánh tiếp!");
            resetTurnTimer();
        } else {
            System.out.println("[Room] Physics xong - " + sender.getPlayerInfo().username + ". Chuyen luot (isFoul=" + isFoul + ").");
            switchTurn(isFoul, "Physics xong, chuyen luot.");
        }
    }

    // ============================================================
    // KẾT THÚC GAME
    // ============================================================

    /**
     * Được gọi khi một client thắng (đánh bi 8 vào lỗ hợp lệ).
     * Client phát hiện điều kiện thắng và gửi một PacketType đặc biệt.
     * Hoặc server có thể phát hiện.
     * 
     * Ở đây: client gửi GAME_OVER → server confirm và update DB.
     */
    public synchronized void endGame(ClientHandler sender, boolean senderWon) {
        if (gameFinished) return;
        gameFinished = true;

        ClientHandler winner = senderWon ? sender : ((sender == player1) ? player2 : player1);
        ClientHandler loser  = (winner == player1) ? player2 : player1;

        System.out.println("[Room] Game kết thúc! Người thắng: " + winner.getPlayerInfo().username);

        // Cập nhật DB
        db.recordMatchResult(winner.getPlayerInfo().id, loser.getPlayerInfo().id);
        winner.refreshPlayerInfo(db);
        loser.refreshPlayerInfo(db);

        // Thông báo kết quả
        winner.sendGameOver(true,  EXP_GAIN);
        loser.sendGameOver(false, 0);

        // Giải phóng room
        cleanupRoom();
    }

    // ============================================================
    // XỬ LÝ NGẮT KẾT NỐI
    // ============================================================

    public synchronized void handleDisconnect(ClientHandler disconnected) {
        if (gameFinished) return;
        gameFinished = true;

        // Người còn lại tự động thắng
        ClientHandler survivor = (disconnected == player1) ? player2 : player1;

        System.out.println("[Room] " + disconnected.getPlayerInfo().username
                + " ngắt kết nối. " + survivor.getPlayerInfo().username + " thắng!");

        db.recordMatchResult(survivor.getPlayerInfo().id, disconnected.getPlayerInfo().id);
        survivor.refreshPlayerInfo(db);
        disconnected.refreshPlayerInfo(db);
        survivor.sendGameOver(true, EXP_GAIN);

        cleanupRoom();
    }

    private synchronized void resetTurnTimer() {
        cancelTurnTimer();
        if (gameFinished || !gameStarted) return;

        turnTimeoutFuture = turnScheduler.schedule(() -> {
            synchronized (GameRoom.this) {
                if (gameFinished || !gameStarted) return;
                ClientHandler currentPlayer = currentTurnIndex.get() == 0 ? player1 : player2;
                System.out.println("[Room] Timeout 60s! " + currentPlayer.getPlayerInfo().username + " không đánh kịp. Tự động đổi lượt.");
                switchTurn(true, "Hết giờ 60s (Timeout)!");
            }
        }, 61500, TimeUnit.MILLISECONDS);
    }

    private synchronized void cancelTurnTimer() {
        if (turnTimeoutFuture != null && !turnTimeoutFuture.isDone()) {
            turnTimeoutFuture.cancel(false);
            turnTimeoutFuture = null;
        }
    }

    private void cleanupRoom() {
        shotInProgress = false;
        cancelTurnTimer();
        try {
            turnScheduler.shutdownNow();
        } catch (Exception ignored) {}
        player1.setCurrentRoom(null);
        player2.setCurrentRoom(null);
    }

    // ============================================================
    // GETTERS
    // ============================================================
    public ClientHandler getPlayer1() { return player1; }
    public ClientHandler getPlayer2() { return player2; }
    public boolean isGameFinished()   { return gameFinished; }
}
