package com.billiards.server;

import java.sql.*;

/**
 * Quản lý kết nối và truy vấn PostgreSQL database.
 * 
 * Sử dụng JDBC để:
 * - Đăng ký / Đăng nhập người chơi
 * - Cập nhật EXP, Level sau trận
 * - Lưu lịch sử trận đấu
 * - Truy vấn bảng xếp hạng
 */
public class DatabaseManager {

    // ============================================================
    // Cấu hình kết nối PostgreSQL – Chỉnh sửa nếu cần
    // ============================================================
    private static final String DB_URL      = "jdbc:postgresql://localhost:5432/billiards_db";
    private static final String DB_USER     = "postgres";
    private static final String DB_PASSWORD = "123456"; // Đổi thành mật khẩu PostgreSQL của bạn

    /** EXP nhận được khi thắng 1 trận */
    private static final int EXP_PER_WIN = 100;
    /** EXP cần để lên cấp: level * 200 */
    private static final int EXP_PER_LEVEL = 200;

    private Connection connection;

    // ============================================================
    // Kết nối Database
    // ============================================================

    public DatabaseManager() {
        connect();
    }

    private void connect() {
        try {
            Class.forName("org.postgresql.Driver");
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            System.out.println("[DB] Kết nối PostgreSQL thành công!");

            // Tự động kiểm tra và thêm cột avatar_id, cue_color nếu chưa có
            try (Statement stmt = connection.createStatement()) {
                stmt.executeUpdate("ALTER TABLE players ADD COLUMN IF NOT EXISTS avatar_id INT DEFAULT 0");
                stmt.executeUpdate("ALTER TABLE players ADD COLUMN IF NOT EXISTS cue_color VARCHAR(20) DEFAULT '#DEB887'");
                // Tạo bảng friendships nếu chưa tồn tại
                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS friendships (" +
                    "user_id INT REFERENCES players(id), " +
                    "friend_id INT REFERENCES players(id), " +
                    "status VARCHAR(20) DEFAULT 'PENDING', " +
                    "created_at TIMESTAMP DEFAULT NOW(), " +
                    "PRIMARY KEY (user_id, friend_id))");
            } catch (SQLException e) {
                System.err.println("[DB] Migration check: " + e.getMessage());
            }
        } catch (ClassNotFoundException e) {
            System.err.println("[DB] Không tìm thấy driver PostgreSQL! Hãy đặt postgresql.jar vào server/lib/");
            System.err.println("[DB] " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("[DB] Lỗi kết nối: " + e.getMessage());
            System.err.println("[DB] Đảm bảo PostgreSQL đang chạy và database 'billiards_db' đã được tạo.");
        }
    }

    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    // ============================================================
    // ĐĂNG KÝ
    // ============================================================

    /**
     * Tạo tài khoản mới.
     * @return "OK" nếu thành công, "EMAIL_EXISTS", "USERNAME_EXISTS", hoặc "ERROR"
     */
    public String register(String email, String username, String passwordHash) {
        if (!isConnected()) return "ERROR";

        if (email == null || email.isBlank()) return "ERROR";
        email = email.trim().toLowerCase();
        username = username != null ? username.trim() : "";

        // Kiểm tra email tồn tại chưa (không phân biệt chữ hoa/thường)
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT id FROM players WHERE LOWER(email) = LOWER(?)")) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return "EMAIL_EXISTS";
        } catch (SQLException e) {
            System.err.println("[DB] register - check email: " + e.getMessage());
            return "ERROR";
        }

        // Kiểm tra username tồn tại chưa (không phân biệt chữ hoa/thường)
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT id FROM players WHERE LOWER(username) = LOWER(?)")) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return "USERNAME_EXISTS";
        } catch (SQLException e) {
            System.err.println("[DB] register - check username: " + e.getMessage());
            return "ERROR";
        }

        // Tạo tài khoản mới – gán avatar ngẫu nhiên
        int randomAvatar = new java.util.Random().nextInt(16);
        try (PreparedStatement stmt = connection.prepareStatement(
                "INSERT INTO players (email, username, password_hash, avatar_id) VALUES (?, ?, ?, ?)")) {
            stmt.setString(1, email);
            stmt.setString(2, username);
            stmt.setString(3, passwordHash);
            stmt.setInt(4, randomAvatar);
            stmt.executeUpdate();
            System.out.println("[DB] Tạo tài khoản mới: " + username + " (email=" + email + ", avatar=" + randomAvatar + ")");
            return "OK";
        } catch (SQLException e) {
            System.err.println("[DB] register - insert: " + e.getMessage());
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("email")) {
                return "EMAIL_EXISTS";
            }
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("username")) {
                return "USERNAME_EXISTS";
            }
            return "ERROR";
        }
    }

    // ============================================================
    // ĐĂNG NHẬP
    // ============================================================

    /**
     * Lấy password hash của username hoặc email để client xác thực.
     * @return PlayerInfo nếu tìm thấy, null nếu không tồn tại
     */
    public PlayerInfo getPlayerByUsername(String username) {
        if (!isConnected() || username == null) return null;

        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT id, username, password_hash, exp, level, wins, losses, " +
                "COALESCE(avatar_id, 0) AS avatar_id, COALESCE(cue_color, '#DEB887') AS cue_color " +
                "FROM players WHERE LOWER(username) = LOWER(?) OR LOWER(email) = LOWER(?)")) {
            stmt.setString(1, username);
            stmt.setString(2, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                PlayerInfo info = new PlayerInfo();
                info.id           = rs.getInt("id");
                info.username     = rs.getString("username");
                info.passwordHash = rs.getString("password_hash");
                info.exp          = rs.getInt("exp");
                info.level        = rs.getInt("level");
                info.wins         = rs.getInt("wins");
                info.losses       = rs.getInt("losses");
                info.avatarId     = rs.getInt("avatar_id");
                info.cueColor     = rs.getString("cue_color");
                return info;
            }
        } catch (SQLException e) {
            System.err.println("[DB] getPlayerByUsername: " + e.getMessage());
        }
        return null;
    }

    // ============================================================
    // CẬP NHẬT KẾT QUẢ TRẬN ĐẤU
    // ============================================================

    /**
     * Cập nhật EXP/Level/Wins/Losses cho người thắng và thua.
     * Lưu lịch sử trận vào match_history.
     */
    public void recordMatchResult(int winnerId, int loserId) {
        if (!isConnected()) return;

        try {
            // Cập nhật người thắng
            try (PreparedStatement stmt = connection.prepareStatement(
                    "UPDATE players SET wins = wins + 1, exp = exp + ?, level = ? WHERE id = ?")) {
                PlayerInfo winner = getPlayerById(winnerId);
                if (winner != null) {
                    int newExp   = winner.exp + EXP_PER_WIN;
                    int newLevel = calculateLevel(newExp);
                    stmt.setInt(1, EXP_PER_WIN);
                    stmt.setInt(2, newLevel);
                    stmt.setInt(3, winnerId);
                    stmt.executeUpdate();
                }
            }

            // Cập nhật người thua
            try (PreparedStatement stmt = connection.prepareStatement(
                    "UPDATE players SET losses = losses + 1 WHERE id = ?")) {
                stmt.setInt(1, loserId);
                stmt.executeUpdate();
            }

            // Lưu lịch sử
            try (PreparedStatement stmt = connection.prepareStatement(
                    "INSERT INTO match_history (winner_id, loser_id, exp_gained) VALUES (?, ?, ?)")) {
                stmt.setInt(1, winnerId);
                stmt.setInt(2, loserId);
                stmt.setInt(3, EXP_PER_WIN);
                stmt.executeUpdate();
            }

            System.out.println("[DB] Lưu kết quả: winnerId=" + winnerId + " loserId=" + loserId);
        } catch (SQLException e) {
            System.err.println("[DB] recordMatchResult: " + e.getMessage());
        }
    }

    /** Tính level từ tổng EXP */
    private int calculateLevel(int exp) {
        return (exp / EXP_PER_LEVEL) + 1;
    }

    private PlayerInfo getPlayerById(int id) {
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT id, username, password_hash, exp, level, wins, losses, " +
                "COALESCE(avatar_id, 0) AS avatar_id, COALESCE(cue_color, '#DEB887') AS cue_color " +
                "FROM players WHERE id = ?")) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                PlayerInfo info = new PlayerInfo();
                info.id           = rs.getInt("id");
                info.username     = rs.getString("username");
                info.passwordHash = rs.getString("password_hash");
                info.exp          = rs.getInt("exp");
                info.level        = rs.getInt("level");
                info.wins         = rs.getInt("wins");
                info.losses       = rs.getInt("losses");
                info.avatarId     = rs.getInt("avatar_id");
                info.cueColor     = rs.getString("cue_color");
                return info;
            }
        } catch (SQLException e) {
            System.err.println("[DB] getPlayerById: " + e.getMessage());
        }
        return null;
    }

    // ============================================================
    // CẬP NHẬT CÀI ĐẶT (Avatar, Màu gậy)
    // ============================================================

    public boolean updateSettings(int playerId, int avatarId, String cueColor) {
        if (!isConnected()) return false;
        try (PreparedStatement stmt = connection.prepareStatement(
                "UPDATE players SET avatar_id = ?, cue_color = ? WHERE id = ?")) {
            stmt.setInt(1, avatarId);
            stmt.setString(2, cueColor);
            stmt.setInt(3, playerId);
            stmt.executeUpdate();
            System.out.println("[DB] Cập nhật settings: playerId=" + playerId + " avatar=" + avatarId + " cue=" + cueColor);
            return true;
        } catch (SQLException e) {
            System.err.println("[DB] updateSettings: " + e.getMessage());
            return false;
        }
    }

    // ============================================================
    // BẢNG XẾP HẠNG
    // ============================================================

    /**
     * Trả về top N người chơi theo EXP giảm dần.
     * Mỗi phần tử: username|level|exp|wins|losses
     */
    public String getLeaderboard(int topN) {
        if (!isConnected()) return "";

        StringBuilder sb = new StringBuilder();
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT username, level, exp, wins, losses FROM players ORDER BY exp DESC LIMIT ?")) {
            stmt.setInt(1, topN);
            ResultSet rs = stmt.executeQuery();
            int rank = 1;
            boolean first = true;
            while (rs.next()) {
                if (!first) sb.append(";");
                sb.append(rank).append("|")
                  .append(rs.getString("username")).append("|")
                  .append(rs.getInt("level")).append("|")
                  .append(rs.getInt("exp")).append("|")
                  .append(rs.getInt("wins")).append("|")
                  .append(rs.getInt("losses"));
                first = false;
                rank++;
            }
        } catch (SQLException e) {
            System.err.println("[DB] getLeaderboard: " + e.getMessage());
        }
        return sb.toString();
    }

    // ============================================================
    // TÌM KIẾM NGƯỜI CHƠI
    // ============================================================

    /**
     * Tìm người chơi theo keyword (tìm trong username).
     * Trả về danh sách "username|level|avatarId;..." (tối đa 10, loại trừ chính mình).
     */
    public String searchPlayers(String keyword, String excludeUsername) {
        if (!isConnected()) return "";

        StringBuilder sb = new StringBuilder();
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT username, level, COALESCE(avatar_id, 0) AS avatar_id " +
                "FROM players WHERE LOWER(username) LIKE LOWER(?) AND username != ? " +
                "ORDER BY level DESC LIMIT 10")) {
            stmt.setString(1, "%" + keyword + "%");
            stmt.setString(2, excludeUsername);
            ResultSet rs = stmt.executeQuery();
            boolean first = true;
            while (rs.next()) {
                if (!first) sb.append(";");
                sb.append(rs.getString("username")).append("|")
                  .append(rs.getInt("level")).append("|")
                  .append(rs.getInt("avatar_id"));
                first = false;
            }
        } catch (SQLException e) {
            System.err.println("[DB] searchPlayers: " + e.getMessage());
        }
        return sb.toString();
    }

    // ============================================================
    // QUẢN LÝ BẠN BÈ
    // ============================================================

    /**
     * Gửi lời mời kết bạn (tạo quan hệ PENDING).
     * Chỉ tạo 1 hàng: user_id (người gửi) → friend_id (người nhận), status = 'PENDING'.
     * @return "OK", "ALREADY_FRIENDS", "ALREADY_PENDING", "USER_NOT_FOUND", hoặc "ERROR"
     */
    public String addFriend(int userId, String friendUsername) {
        if (!isConnected()) return "ERROR";

        // Tìm friend ID
        PlayerInfo friend = getPlayerByUsername(friendUsername);
        if (friend == null) return "USER_NOT_FOUND";
        if (friend.id == userId) return "ERROR"; // Không tự kết bạn

        // Kiểm tra đã là bạn hoặc đã gửi lời mời chưa
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT status FROM friendships WHERE (user_id = ? AND friend_id = ?) OR (user_id = ? AND friend_id = ?)")) {
            stmt.setInt(1, userId);
            stmt.setInt(2, friend.id);
            stmt.setInt(3, friend.id);
            stmt.setInt(4, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String status = rs.getString("status");
                if ("ACCEPTED".equals(status)) return "ALREADY_FRIENDS";
                if ("PENDING".equals(status)) return "ALREADY_PENDING";
            }
        } catch (SQLException e) {
            System.err.println("[DB] addFriend - check: " + e.getMessage());
            return "ERROR";
        }

        // Tạo lời mời kết bạn (chỉ 1 chiều, PENDING)
        try (PreparedStatement stmt = connection.prepareStatement(
                "INSERT INTO friendships (user_id, friend_id, status) VALUES (?, ?, 'PENDING')")) {
            stmt.setInt(1, userId);
            stmt.setInt(2, friend.id);
            stmt.executeUpdate();
            System.out.println("[DB] Gửi lời mời kết bạn: userId=" + userId + " → friendId=" + friend.id);
            return "OK";
        } catch (SQLException e) {
            System.err.println("[DB] addFriend - insert: " + e.getMessage());
            return "ERROR";
        }
    }

    /**
     * Chấp nhận lời mời kết bạn.
     * Cập nhật hàng PENDING thành ACCEPTED và tạo thêm hàng ngược lại.
     * @param userId ID của người nhận (người chấp nhận)
     * @param senderUsername tên người gửi lời mời
     * @return "OK", "NOT_FOUND", hoặc "ERROR"
     */
    public String acceptFriend(int userId, String senderUsername) {
        if (!isConnected()) return "ERROR";

        PlayerInfo sender = getPlayerByUsername(senderUsername);
        if (sender == null) return "NOT_FOUND";

        try {
            // Cập nhật lời mời PENDING → ACCEPTED
            try (PreparedStatement stmt = connection.prepareStatement(
                    "UPDATE friendships SET status = 'ACCEPTED' WHERE user_id = ? AND friend_id = ? AND status = 'PENDING'")) {
                stmt.setInt(1, sender.id);
                stmt.setInt(2, userId);
                int updated = stmt.executeUpdate();
                if (updated == 0) return "NOT_FOUND"; // Không có lời mời nào
            }
            // Tạo hàng ngược lại (userId → senderId, ACCEPTED)
            try (PreparedStatement stmt = connection.prepareStatement(
                    "INSERT INTO friendships (user_id, friend_id, status) VALUES (?, ?, 'ACCEPTED') " +
                    "ON CONFLICT (user_id, friend_id) DO UPDATE SET status = 'ACCEPTED'")) {
                stmt.setInt(1, userId);
                stmt.setInt(2, sender.id);
                stmt.executeUpdate();
            }
            System.out.println("[DB] Chấp nhận kết bạn: userId=" + userId + " ← senderId=" + sender.id);
            return "OK";
        } catch (SQLException e) {
            System.err.println("[DB] acceptFriend: " + e.getMessage());
            return "ERROR";
        }
    }

    /**
     * Từ chối lời mời kết bạn (xóa hàng PENDING).
     * @param userId ID của người nhận (người từ chối)
     * @param senderUsername tên người gửi lời mời
     * @return "OK", "NOT_FOUND", hoặc "ERROR"
     */
    public String declineFriend(int userId, String senderUsername) {
        if (!isConnected()) return "ERROR";

        PlayerInfo sender = getPlayerByUsername(senderUsername);
        if (sender == null) return "NOT_FOUND";

        try (PreparedStatement stmt = connection.prepareStatement(
                "DELETE FROM friendships WHERE user_id = ? AND friend_id = ? AND status = 'PENDING'")) {
            stmt.setInt(1, sender.id);
            stmt.setInt(2, userId);
            int deleted = stmt.executeUpdate();
            if (deleted == 0) return "NOT_FOUND";
            System.out.println("[DB] Từ chối kết bạn: userId=" + userId + " ← senderId=" + sender.id);
            return "OK";
        } catch (SQLException e) {
            System.err.println("[DB] declineFriend: " + e.getMessage());
            return "ERROR";
        }
    }

    /**
     * Xóa bạn bè (xóa hàng ACCEPTED).
     * @param userId ID của người yêu cầu xóa
     * @param friendUsername Tên người bạn muốn xóa
     * @return "OK", "NOT_FOUND", hoặc "ERROR"
     */
    public String removeFriend(int userId, String friendUsername) {
        if (!isConnected()) return "ERROR";

        PlayerInfo friend = getPlayerByUsername(friendUsername);
        if (friend == null) return "NOT_FOUND";

        try (PreparedStatement stmt = connection.prepareStatement(
                "DELETE FROM friendships WHERE " +
                "(user_id = ? AND friend_id = ?) OR (user_id = ? AND friend_id = ?)")) {
            stmt.setInt(1, userId);
            stmt.setInt(2, friend.id);
            stmt.setInt(3, friend.id);
            stmt.setInt(4, userId);
            int deleted = stmt.executeUpdate();
            if (deleted == 0) return "NOT_FOUND";
            
            // Xóa toàn bộ lịch sử chat giữa 2 người này
            try (PreparedStatement delMsg = connection.prepareStatement(
                    "DELETE FROM messages WHERE (sender_id = ? AND receiver_id = ?) OR (sender_id = ? AND receiver_id = ?)")) {
                delMsg.setInt(1, userId);
                delMsg.setInt(2, friend.id);
                delMsg.setInt(3, friend.id);
                delMsg.setInt(4, userId);
                delMsg.executeUpdate();
            }

            System.out.println("[DB] Xóa bạn bè và lịch sử chat: userId=" + userId + " và friendId=" + friend.id);
            return "OK";
        } catch (SQLException e) {
            System.err.println("[DB] removeFriend: " + e.getMessage());
            return "ERROR";
        }
    }

    /**
     * Lấy danh sách lời mời kết bạn đang chờ (người khác gửi cho mình).
     * Trả về "username|level|avatarId;..."
     */
    public String getPendingRequests(int userId) {
        if (!isConnected()) return "";

        StringBuilder sb = new StringBuilder();
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT p.username, p.level, COALESCE(p.avatar_id, 0) AS avatar_id " +
                "FROM friendships f JOIN players p ON f.user_id = p.id " +
                "WHERE f.friend_id = ? AND f.status = 'PENDING' ORDER BY f.created_at DESC")) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            boolean first = true;
            while (rs.next()) {
                if (!first) sb.append(";");
                sb.append(rs.getString("username")).append("|")
                  .append(rs.getInt("level")).append("|")
                  .append(rs.getInt("avatar_id"));
                first = false;
            }
        } catch (SQLException e) {
            System.err.println("[DB] getPendingRequests: " + e.getMessage());
        }
        return sb.toString();
    }

    /**
     * Lấy danh sách bạn bè của người chơi (chỉ trạng thái ACCEPTED).
     * Trả về "username|level|avatarId|status;..."
     */
    public String getFriends(int userId) {
        if (!isConnected()) return "";

        StringBuilder sb = new StringBuilder();
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT p.username, p.level, COALESCE(p.avatar_id, 0) AS avatar_id, f.status " +
                "FROM friendships f JOIN players p ON f.friend_id = p.id " +
                "WHERE f.user_id = ? AND f.status = 'ACCEPTED' ORDER BY p.username")) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            boolean first = true;
            while (rs.next()) {
                if (!first) sb.append(";");
                sb.append(rs.getString("username")).append("|")
                  .append(rs.getInt("level")).append("|")
                  .append(rs.getInt("avatar_id")).append("|")
                  .append(rs.getString("status"));
                first = false;
            }
        } catch (SQLException e) {
            System.err.println("[DB] getFriends: " + e.getMessage());
        }
        return sb.toString();
    }

    // ============================================================
    // TIN NHẮN CHAT
    // ============================================================

    /**
     * Lưu tin nhắn chat.
     */
    public void saveChatMessage(int senderId, String receiverUsername, String content) {
        if (!isConnected()) return;
        PlayerInfo receiver = getPlayerByUsername(receiverUsername);
        if (receiver == null) return;
        try (PreparedStatement stmt = connection.prepareStatement(
                "INSERT INTO messages (sender_id, receiver_id, content) VALUES (?, ?, ?)")) {
            stmt.setInt(1, senderId);
            stmt.setInt(2, receiver.id);
            stmt.setString(3, content);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[DB] saveChatMessage: " + e.getMessage());
        }
    }

    /**
     * Lấy lịch sử chat (tối đa 50 tin nhắn gần nhất).
     * Trả về định dạng: "senderUsername|content;..." (cũ nhất -> mới nhất)
     */
    public String getChatHistory(int userId, String friendUsername) {
        if (!isConnected()) return "";
        PlayerInfo friend = getPlayerByUsername(friendUsername);
        if (friend == null) return "";

        StringBuilder sb = new StringBuilder();
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT * FROM ( " +
                "  SELECT p.username as sender_username, m.content, m.sent_at " +
                "  FROM messages m JOIN players p ON m.sender_id = p.id " +
                "  WHERE (m.sender_id = ? AND m.receiver_id = ?) OR (m.sender_id = ? AND m.receiver_id = ?) " +
                "  ORDER BY m.sent_at DESC LIMIT 50 " +
                ") sub ORDER BY sent_at ASC")) {
            stmt.setInt(1, userId);
            stmt.setInt(2, friend.id);
            stmt.setInt(3, friend.id);
            stmt.setInt(4, userId);
            ResultSet rs = stmt.executeQuery();
            boolean first = true;
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm dd/MM");
            while (rs.next()) {
                if (!first) sb.append(";");
                String time = sdf.format(rs.getTimestamp("sent_at"));
                sb.append(rs.getString("sender_username")).append("|").append(time).append("|").append(rs.getString("content"));
                first = false;
            }
        } catch (SQLException e) {
            System.err.println("[DB] getChatHistory: " + e.getMessage());
        }
        return sb.toString();
    }

    // ============================================================
    // Inner class: Thông tin người chơi
    // ============================================================
    public static class PlayerInfo {
        public int    id;
        public String username;
        public String passwordHash;
        public int    exp;
        public int    level;
        public int    wins;
        public int    losses;
        public int    avatarId;
        public String cueColor = "#DEB887";
    }
}
