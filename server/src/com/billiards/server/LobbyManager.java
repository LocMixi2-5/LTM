package com.billiards.server;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Quản lý hàng đợi ghép trận ngẫu nhiên.
 * 
 * Khi có 2 người trong queue → tạo GameRoom → xóa khỏi queue.
 * Thread-safe: sử dụng synchronized + ConcurrentLinkedQueue.
 */
public class LobbyManager {

    private final Queue<ClientHandler> matchQueue = new ConcurrentLinkedQueue<>();
    private final DatabaseManager db;

    public LobbyManager(DatabaseManager db) {
        this.db = db;
    }

    // ============================================================
    // THÊM VÀO HÀNG ĐỢI TÌM TRẬN
    // ============================================================

    /**
     * Thêm client vào hàng đợi tìm trận.
     * Nếu đã có người chờ → ghép ngay lập tức.
     */
    public synchronized void addToQueue(ClientHandler client) {
        // Không cho thêm nếu đang trong room
        if (client.getCurrentRoom() != null) return;
        // Không cho thêm nếu đã trong queue
        if (matchQueue.contains(client)) return;

        matchQueue.add(client);
        System.out.println("[Lobby] Queue: " + matchQueue.size() + " người đang chờ.");

        // Nếu có đủ 2 người → ghép trận
        if (matchQueue.size() >= 2) {
            ClientHandler player1 = matchQueue.poll();
            ClientHandler player2 = matchQueue.poll();

            if (player1 != null && player2 != null) {
                createGame(player1, player2);
            }
        }
    }

    /** Xóa client khỏi hàng đợi (khi hủy tìm trận hoặc ngắt kết nối) */
    public synchronized void removeFromQueue(ClientHandler client) {
        matchQueue.remove(client);
    }

    // ============================================================
    // TẠO PHÒNG GAME
    // ============================================================

    public void createGame(ClientHandler player1, ClientHandler player2) {
        System.out.println("[Lobby] Ghép trận: "
                + player1.getPlayerInfo().username + " vs "
                + player2.getPlayerInfo().username);

        GameRoom room = new GameRoom(player1, player2, db);

        player1.setCurrentRoom(room);
        player2.setCurrentRoom(room);

        // Thông báo cho cả 2 tìm thấy đối thủ kèm avatar và màu gậy
        var p1Info = player1.getPlayerInfo();
        var p2Info = player2.getPlayerInfo();
        player1.sendMatchFound(p2Info.username, p2Info.level, p2Info.avatarId, p2Info.cueColor);
        player2.sendMatchFound(p1Info.username, p1Info.level, p1Info.avatarId, p1Info.cueColor);
    }
}
