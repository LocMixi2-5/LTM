package com.billiards.server;

import com.billiards.server.protocol.Packet;
import com.billiards.server.protocol.PacketType;

import java.io.*;
import java.net.Socket;

/**
 * Xử lý giao tiếp với một Client cụ thể.
 * Mỗi ClientHandler chạy trên một Thread riêng.
 * 
 * Vòng lặp chính:
 *   while (running) {
 *     đọc PacketType → xử lý theo loại → gửi phản hồi
 *   }
 */
public class ClientHandler implements Runnable {

    private final Socket socket;
    private final DataInputStream  in;
    private final DataOutputStream out;
    private final AuthManager    authManager;
    private final LobbyManager   lobbyManager;
    private final DatabaseManager db;

    // Thông tin người chơi sau khi đăng nhập
    private boolean isLoggedIn = false;
    private DatabaseManager.PlayerInfo playerInfo = null;

    // Trò chơi hiện tại
    private GameRoom currentRoom = null;
    private boolean running = true;

    public ClientHandler(Socket socket, AuthManager authManager,
                         LobbyManager lobbyManager, DatabaseManager db) throws IOException {
        this.socket       = socket;
        this.authManager  = authManager;
        this.lobbyManager = lobbyManager;
        this.db           = db;
        this.in  = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        this.out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
    }

    // ============================================================
    // Vòng lặp nhận gói tin
    // ============================================================

    @Override
    public void run() {
        System.out.println("[Server] Client mới kết nối: " + socket.getRemoteSocketAddress());
        try {
            while (running) {
                PacketType type = Packet.readType(in);
                handlePacket(type);
            }
        } catch (EOFException | java.net.SocketException e) {
            // Client ngắt kết nối bình thường
        } catch (IOException e) {
            System.err.println("[Handler] Lỗi IO: " + e.getMessage());
        } finally {
            onDisconnect();
        }
    }

    private void handlePacket(PacketType type) throws IOException {
        switch (type) {
            case LOGIN_REQUEST    -> handleLogin();
            case REGISTER_REQUEST -> handleRegister();
            case FIND_MATCH       -> handleFindMatch();
            case CANCEL_FIND      -> handleCancelFind();
            case PREGAME_READY    -> handlePregameReady();
            case CUE_SHOT         -> handleCueShot();
            case CUE_BALL_PLACED  -> handleCueBallPlaced();
            case CUE_AIM          -> handleCueAim();
            case PHYSICS_DONE     -> handlePhysicsDone();
            case CLAIM_WIN        -> handleClaimWin();
            case LEADERBOARD_REQUEST -> handleLeaderboard();
            case SETTINGS_UPDATE  -> handleSettingsUpdate();
            case SEARCH_PLAYERS_REQUEST   -> handleSearchPlayers();
            case ADD_FRIEND_REQUEST       -> handleAddFriend();
            case FRIEND_LIST_REQUEST      -> handleFriendList();
            case FRIEND_ACCEPT_REQUEST    -> handleAcceptFriend();
            case FRIEND_DECLINE_REQUEST   -> handleDeclineFriend();
            case PENDING_REQUESTS_REQUEST -> handlePendingRequests();
            case REMOVE_FRIEND_REQUEST    -> handleRemoveFriend();
            case CHAT_MESSAGE_REQUEST     -> handleChatMessage();
            case CHAT_HISTORY_REQUEST     -> handleChatHistory();
            case CHALLENGE_REQUEST        -> handleChallengeRequest();
            case CHALLENGE_RESPONSE       -> handleChallengeResponse();
            case PING             -> handlePing();
            case DISCONNECT       -> { running = false; }
            default -> System.out.println("[Handler] Goi tin khong xu ly: " + type);
        }
    }

    // ============================================================
    // KIỂM TRA ĐỘ TRỄ (PING / RTT)
    // ============================================================
    private void handlePing() throws IOException {
        long timestamp = Packet.readLong(in);
        synchronized (out) {
            Packet.writeType(out, PacketType.PING);
            Packet.writeLong(out, timestamp);
            Packet.flush(out);
        }
    }

    // ============================================================
    // ĐĂNG NHẬP
    // ============================================================
    private void handleLogin() throws IOException {
        String username     = Packet.readString(in);
        String plainPassword = Packet.readString(in);

        if (isLoggedIn) {
            synchronized (out) {
                Packet.writeType(out, PacketType.LOGIN_RESPONSE);
                Packet.writeBoolean(out, false);
                Packet.writeString(out, "Kết nối này đã được đăng nhập!");
                Packet.flush(out);
            }
            return;
        }

        DatabaseManager.PlayerInfo player = authManager.login(username, plainPassword);

        synchronized (out) {
            Packet.writeType(out, PacketType.LOGIN_RESPONSE);
            if (player != null) {
                // Kiểm tra xem tài khoản này đã có người đang đăng nhập hay chưa (ngăn 2 người cùng đăng nhập 1 tài khoản)
                synchronized (ServerMain.onlinePlayers) {
                    ClientHandler existing = ServerMain.onlinePlayers.get(player.username);
                    if (existing != null && existing.running && existing.socket != null && !existing.socket.isClosed()) {
                        Packet.writeBoolean(out, false); // Đăng nhập thất bại
                        Packet.writeString(out, "Tài khoản hiện đang được đăng nhập ở thiết bị khác!");
                        System.out.println("[Handler] Từ chối đăng nhập (tài khoản đã online): " + player.username);
                        Packet.flush(out);
                        return;
                    } else if (existing != null) {
                        ServerMain.onlinePlayers.remove(player.username, existing);
                    }

                    isLoggedIn = true;
                    playerInfo = player;
                    ServerMain.onlinePlayers.put(player.username, this);
                }

                Packet.writeBoolean(out, true);  // success
                Packet.writeString(out, player.username);
                Packet.writeInt(out, player.exp);
                Packet.writeInt(out, player.level);
                Packet.writeInt(out, player.wins);
                Packet.writeInt(out, player.losses);
                Packet.writeInt(out, player.avatarId);
                Packet.writeString(out, player.cueColor != null ? player.cueColor : "#DEB887");
                System.out.println("[Handler] Đăng nhập thành công: " + player.username);
            } else {
                Packet.writeBoolean(out, false); // fail
                Packet.writeString(out, "Sai tên đăng nhập hoặc mật khẩu!");
                System.out.println("[Handler] Đăng nhập thất bại: " + username);
            }
            Packet.flush(out);
        }
    }

    // ============================================================
    // ĐĂNG KÝ
    // ============================================================
    private void handleRegister() throws IOException {
        String email         = Packet.readString(in);
        String username      = Packet.readString(in);
        String plainPassword = Packet.readString(in);

        String result = authManager.register(email, username, plainPassword);

        synchronized (out) {
            Packet.writeType(out, PacketType.REGISTER_RESPONSE);
            switch (result) {
                case "OK" -> {
                    Packet.writeBoolean(out, true);
                    Packet.writeString(out, "Đăng ký thành công! Vui lòng đăng nhập.");
                    System.out.println("[Handler] Đăng ký thành công: " + username);
                }
                case "INVALID_EMAIL" -> {
                    Packet.writeBoolean(out, false);
                    Packet.writeString(out, "Định dạng email không hợp lệ!");
                }
                case "EMAIL_EXISTS" -> {
                    Packet.writeBoolean(out, false);
                    Packet.writeString(out, "Email này đã được sử dụng!");
                }
                case "USERNAME_EXISTS" -> {
                    Packet.writeBoolean(out, false);
                    Packet.writeString(out, "Tên đăng nhập đã tồn tại!");
                }
                default -> {
                    Packet.writeBoolean(out, false);
                    Packet.writeString(out, "Lỗi hệ thống, vui lòng thử lại.");
                }
            }
            Packet.flush(out);
        }
    }

    // ============================================================
    // TÌM TRẬN
    // ============================================================
    private void handleFindMatch() throws IOException {
        if (!isLoggedIn) return;
        System.out.println("[Handler] " + playerInfo.username + " đang tìm trận...");
        lobbyManager.addToQueue(this);
    }

    private void handleCancelFind() throws IOException {
        if (!isLoggedIn) return;
        lobbyManager.removeFromQueue(this);
        System.out.println("[Handler] " + playerInfo.username + " hủy tìm trận.");
    }

    // ============================================================
    // PRE-GAME READY
    // ============================================================
    private void handlePregameReady() throws IOException {
        if (currentRoom != null) {
            currentRoom.playerReady(this);
        }
    }

    // ============================================================
    // CÚ ĐÁNH (CUE_SHOT)
    // ============================================================
    private void handleCueShot() throws IOException {
        double angle = Packet.readDouble(in);
        double force = Packet.readDouble(in);
        double cueX  = Packet.readDouble(in);
        double cueY  = Packet.readDouble(in);
        double spinX = Packet.readDouble(in);
        double spinY = Packet.readDouble(in);

        if (currentRoom != null) {
            currentRoom.processCueShot(this, angle, force, cueX, cueY, spinX, spinY);
        }
    }

    // ============================================================
    // ĐẶT BI (CUE_BALL_PLACED)
    // ============================================================
    private void handleCueBallPlaced() throws IOException {
        double cueX = Packet.readDouble(in);
        double cueY = Packet.readDouble(in);
        if (currentRoom != null) {
            currentRoom.processCueBallPlaced(this, cueX, cueY);
        }
    }

    // ============================================================
    // TRẠNG THÁI NGẮM CƠ (CUE_AIM)
    // ============================================================
    private void handleCueAim() throws IOException {
        boolean isAiming = Packet.readBoolean(in);
        double angle = Packet.readDouble(in);
        double forceRatio = Packet.readDouble(in);
        double spinX = Packet.readDouble(in);
        double spinY = Packet.readDouble(in);
        if (currentRoom != null) {
            currentRoom.processCueAim(this, isAiming, angle, forceRatio, spinX, spinY);
        }
    }

    // ============================================================
    // PHYSICS DONE - Client bao physics da xong
    // ============================================================
    private void handlePhysicsDone() throws IOException {
        boolean keepTurn = false;
        boolean isFoul = false;
        try {
            keepTurn = Packet.readBoolean(in);
            isFoul = Packet.readBoolean(in);
        } catch (EOFException e) {
            // Tuong thich nguoc
        }
        if (currentRoom != null) {
            currentRoom.onPhysicsDone(this, keepTurn, isFoul);
        }
    }

    // ============================================================
    // CLAIM WIN - Client bao minh thang/thua
    // ============================================================
    private void handleClaimWin() throws IOException {
        boolean isWin = Packet.readBoolean(in);
        if (currentRoom != null) {
            currentRoom.endGame(this, isWin);
        }
    }

    // ============================================================
    // BẢNG XẾP HẠNG
    // ============================================================
    private void handleLeaderboard() throws IOException {
        String data = db.getLeaderboard(10);
        synchronized (out) {
            Packet.writeType(out, PacketType.LEADERBOARD_RESPONSE);
            Packet.writeString(out, data);
            Packet.flush(out);
        }
    }

    // ============================================================
    // CÀI ĐẶT (Avatar, Màu gậy)
    // ============================================================
    private void handleSettingsUpdate() throws IOException {
        int avatarId = Packet.readInt(in);
        String cueColor = Packet.readString(in);
        boolean success = false;
        if (isLoggedIn && playerInfo != null) {
            // Kiểm tra bảo mật phía Server: Người chơi có đủ cấp độ yêu cầu để mở khóa skin này không?
            if (com.billiards.server.protocol.CueSkinConfig.isSkinUnlocked(cueColor, playerInfo.level)) {
                success = db.updateSettings(playerInfo.id, avatarId, cueColor);
                if (success) {
                    playerInfo.avatarId = avatarId;
                    playerInfo.cueColor = cueColor;
                    System.out.println("[Handler] " + playerInfo.username + " trang bị skin thành công: " + cueColor);
                }
            } else {
                System.out.println("[Handler] Từ chối: " + playerInfo.username + " (Lv." + playerInfo.level 
                    + ") chưa đủ cấp để trang bị skin: " + cueColor);
            }
        }
        synchronized (out) {
            Packet.writeType(out, PacketType.SETTINGS_RESPONSE);
            Packet.writeBoolean(out, success);
            Packet.flush(out);
        }
    }

    // ============================================================
    // TÌM KIẾM NGƯỜI CHƠI
    // ============================================================
    private void handleSearchPlayers() throws IOException {
        String keyword = Packet.readString(in);
        String result = "";
        if (isLoggedIn && playerInfo != null) {
            result = db.searchPlayers(keyword, playerInfo.username);
        }
        synchronized (out) {
            Packet.writeType(out, PacketType.SEARCH_PLAYERS_RESPONSE);
            Packet.writeString(out, result);
            Packet.flush(out);
        }
    }

    // ============================================================
    // THÊM BẠN BÈ
    // ============================================================
    private void handleAddFriend() throws IOException {
        String friendUsername = Packet.readString(in);
        boolean success = false;
        String message = "Lỗi hệ thống!";

        if (isLoggedIn && playerInfo != null) {
            String result = db.addFriend(playerInfo.id, friendUsername);
            switch (result) {
                case "OK" -> {
                    success = true;
                    message = "Đã gửi lời mời kết bạn tới " + friendUsername + "!";
                }
                case "ALREADY_FRIENDS" -> {
                    message = friendUsername + " đã là bạn bè của bạn rồi!";
                }
                case "ALREADY_PENDING" -> {
                    message = "Bạn đã gửi lời mời tới " + friendUsername + " rồi!";
                }
                case "USER_NOT_FOUND" -> {
                    message = "Không tìm thấy người chơi " + friendUsername + "!";
                }
                default -> {
                    message = "Lỗi khi gửi lời mời kết bạn!";
                }
            }
        }

        synchronized (out) {
            Packet.writeType(out, PacketType.ADD_FRIEND_RESPONSE);
            Packet.writeBoolean(out, success);
            Packet.writeString(out, message);
            Packet.flush(out);
        }
    }

    // ============================================================
    // CHẤP NHẬN LỜI MỜI KẾT BẠN
    // ============================================================
    private void handleAcceptFriend() throws IOException {
        String senderUsername = Packet.readString(in);
        boolean success = false;
        String message = "Lỗi hệ thống!";

        if (isLoggedIn && playerInfo != null) {
            String result = db.acceptFriend(playerInfo.id, senderUsername);
            switch (result) {
                case "OK" -> {
                    success = true;
                    message = "Đã chấp nhận lời mời kết bạn từ " + senderUsername + "!";
                }
                case "NOT_FOUND" -> {
                    message = "Không tìm thấy lời mời kết bạn từ " + senderUsername + "!";
                }
                default -> {
                    message = "Lỗi khi chấp nhận lời mời!";
                }
            }
        }

        synchronized (out) {
            Packet.writeType(out, PacketType.FRIEND_ACCEPT_RESPONSE);
            Packet.writeBoolean(out, success);
            Packet.writeString(out, message);
            Packet.flush(out);
        }
    }

    // ============================================================
    // TỪ CHỐI LỜI MỜI KẾT BẠN
    // ============================================================
    private void handleDeclineFriend() throws IOException {
        String senderUsername = Packet.readString(in);
        boolean success = false;
        String message = "Lỗi hệ thống!";

        if (isLoggedIn && playerInfo != null) {
            String result = db.declineFriend(playerInfo.id, senderUsername);
            switch (result) {
                case "OK" -> {
                    success = true;
                    message = "Đã từ chối lời mời kết bạn từ " + senderUsername + ".";
                }
                case "NOT_FOUND" -> {
                    message = "Không tìm thấy lời mời kết bạn từ " + senderUsername + "!";
                }
                default -> {
                    message = "Lỗi khi từ chối lời mời!";
                }
            }
        }

        synchronized (out) {
            Packet.writeType(out, PacketType.FRIEND_DECLINE_RESPONSE);
            Packet.writeBoolean(out, success);
            Packet.writeString(out, message);
            Packet.flush(out);
        }
    }

    // ============================================================
    // DANH SÁCH LỜI MỜI KẾT BẠN CHỜ
    // ============================================================
    private void handlePendingRequests() throws IOException {
        String result = "";
        if (isLoggedIn && playerInfo != null) {
            result = db.getPendingRequests(playerInfo.id);
        }
        synchronized (out) {
            Packet.writeType(out, PacketType.PENDING_REQUESTS_RESPONSE);
            Packet.writeString(out, result);
            Packet.flush(out);
        }
    }

    // ============================================================
    // DANH SÁCH BẠN BÈ
    // ============================================================
    private void handleFriendList() throws IOException {
        String result = "";
        if (isLoggedIn && playerInfo != null) {
            String rawFriends = db.getFriends(playerInfo.id);
            if (rawFriends != null && !rawFriends.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                String[] list = rawFriends.split(";");
                boolean first = true;
                for (String item : list) {
                    String[] parts = item.split("\\|");
                    if (parts.length >= 3) {
                        String fName = parts[0];
                        String fLevel = parts[1];
                        String fAvatar = parts[2];
                        boolean isOnline = ServerMain.onlinePlayers.containsKey(fName);
                        if (!first) sb.append(";");
                        sb.append(fName).append("|")
                          .append(fLevel).append("|")
                          .append(fAvatar).append("|")
                          .append(isOnline);
                        first = false;
                    }
                }
                result = sb.toString();
            }
        }
        synchronized (out) {
            Packet.writeType(out, PacketType.FRIEND_LIST_RESPONSE);
            Packet.writeString(out, result);
            Packet.flush(out);
        }
    }

    // ============================================================
    // XÓA BẠN BÈ
    // ============================================================
    private void handleRemoveFriend() throws IOException {
        String friendUsername = Packet.readString(in);
        boolean success = false;
        String message = "Lỗi hệ thống!";

        if (isLoggedIn && playerInfo != null) {
            String result = db.removeFriend(playerInfo.id, friendUsername);
            switch (result) {
                case "OK" -> {
                    success = true;
                    message = "Đã xóa bạn với " + friendUsername + ".";
                }
                case "NOT_FOUND" -> {
                    message = "Không tìm thấy người bạn này!";
                }
                default -> {
                    message = "Lỗi khi xóa bạn!";
                }
            }
        }

        synchronized (out) {
            Packet.writeType(out, PacketType.REMOVE_FRIEND_RESPONSE);
            Packet.writeBoolean(out, success);
            Packet.writeString(out, message);
            Packet.flush(out);
        }
    }

    // ============================================================
    // TIN NHẮN CHAT
    // ============================================================
    private void handleChatMessage() throws IOException {
        String recipientUsername = Packet.readString(in);
        String messageText = Packet.readString(in);

        if (isLoggedIn && playerInfo != null) {
            // Lưu vào DB
            db.saveChatMessage(playerInfo.id, recipientUsername, messageText);
            
            ClientHandler recipient = ServerMain.onlinePlayers.get(recipientUsername);
            if (recipient != null) {
                recipient.sendChatMessage(playerInfo.username, messageText);
            }
        }
    }

    private void handleChatHistory() throws IOException {
        String friendUsername = Packet.readString(in);
        String history = "";
        
        if (isLoggedIn && playerInfo != null) {
            history = db.getChatHistory(playerInfo.id, friendUsername);
        }
        
        synchronized (out) {
            Packet.writeType(out, PacketType.CHAT_HISTORY_RESPONSE);
            Packet.writeString(out, friendUsername);
            Packet.writeString(out, history);
            Packet.flush(out);
        }
    }

    public void sendChatMessage(String senderUsername, String messageText) {
        try {
            synchronized (out) {
                Packet.writeType(out, PacketType.CHAT_MESSAGE_RESPONSE);
                Packet.writeString(out, senderUsername);
                Packet.writeString(out, messageText);
                Packet.flush(out);
            }
        } catch (IOException e) { running = false; }
    }

    // ============================================================
    // THÁCH ĐẤU (CHALLENGE)
    // ============================================================
    private String currentChallenger = null;
    private java.util.Timer challengeTimer = null;

    /**
     * Tìm ClientHandler trực tuyến theo tên (hỗ trợ không phân biệt hoa thường và loại bỏ khoảng trắng).
     */
    private ClientHandler getOnlinePlayer(String username) {
        if (username == null) return null;
        String trimmed = username.trim();
        ClientHandler direct = ServerMain.onlinePlayers.get(trimmed);
        if (direct != null) return direct;
        for (java.util.Map.Entry<String, ClientHandler> entry : ServerMain.onlinePlayers.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(trimmed)) {
                return entry.getValue();
            }
        }
        return null;
    }

    private void handleChallengeRequest() throws IOException {
        String targetUsername = Packet.readString(in);
        
        if (isLoggedIn && playerInfo != null) {
            if (targetUsername == null || targetUsername.trim().isEmpty()) {
                sendChallengeResult(false, "Tên người chơi không hợp lệ.");
                return;
            }

            ClientHandler target = getOnlinePlayer(targetUsername);
            
            if (target == null) {
                sendChallengeResult(false, "Người chơi không trực tuyến.");
                return;
            }
            if (target == this || (target.playerInfo != null && target.playerInfo.username.equalsIgnoreCase(playerInfo.username))) {
                sendChallengeResult(false, "Không thể tự thách đấu chính mình.");
                return;
            }
            if (target.getCurrentRoom() != null) {
                sendChallengeResult(false, target.playerInfo != null ? target.playerInfo.username : targetUsername + " đang trong một trận đấu khác.");
                return;
            }
            if (target.currentChallenger != null) {
                sendChallengeResult(false, (target.playerInfo != null ? target.playerInfo.username : targetUsername) + " đang bận.");
                return;
            }
            
            // Gửi lời mời tới target
            target.receiveChallenge(playerInfo.username);
        }
    }

    private void receiveChallenge(String challengerUsername) {
        this.currentChallenger = challengerUsername;
        
        try {
            synchronized (out) {
                Packet.writeType(out, PacketType.CHALLENGE_RECEIVE);
                Packet.writeString(out, challengerUsername);
                Packet.flush(out);
            }
        } catch (IOException e) { running = false; return; }
        
        // Hủy sau 15s nếu không nhận phản hồi (client hiển thị 10s, server cho 15s để bù độ trễ mạng)
        if (challengeTimer != null) {
            challengeTimer.cancel();
            challengeTimer = null;
        }
        challengeTimer = new java.util.Timer();
        challengeTimer.schedule(new java.util.TimerTask() {
            @Override
            public void run() {
                if (currentChallenger != null && challengerUsername.equalsIgnoreCase(currentChallenger)) {
                    currentChallenger = null;
                    ClientHandler challenger = getOnlinePlayer(challengerUsername);
                    if (challenger != null) {
                        challenger.sendChallengeResult(false, "Đã hết thời gian chờ phản hồi thách đấu.");
                    }
                }
            }
        }, 15000);
    }

    private void handleChallengeResponse() throws IOException {
        String challengerUsername = Packet.readString(in);
        boolean accept = Packet.readBoolean(in);
        
        // Chỉ xử lý và hủy timer nếu phản hồi khớp với lời thách đấu hiện tại (L03)
        if (currentChallenger == null || challengerUsername == null
                || (!challengerUsername.trim().equalsIgnoreCase(currentChallenger.trim()) && !challengerUsername.equals(currentChallenger))) {
            return;
        }
        
        if (challengeTimer != null) {
            challengeTimer.cancel();
            challengeTimer = null;
        }
        
        String validChallenger = currentChallenger;
        currentChallenger = null;
        ClientHandler challenger = getOnlinePlayer(validChallenger);
            
            if (challenger == null) {
                sendChallengeResult(false, "Người thách đấu đã ngắt kết nối.");
                return;
            }
            if (challenger.getCurrentRoom() != null) {
                sendChallengeResult(false, challenger.playerInfo.username + " đã vào một trận đấu khác.");
                return;
            }
            
            if (!accept) {
                challenger.sendChallengeResult(false, playerInfo.username + " đã từ chối lời mời.");
                return;
            }
            
            // Bắt đầu game (xóa cả hai khỏi hàng đợi tìm trận nếu đang tìm)
            lobbyManager.removeFromQueue(challenger);
            lobbyManager.removeFromQueue(this);
            lobbyManager.createGame(challenger, this);
    }

    public void sendChallengeResult(boolean success, String message) {
        try {
            synchronized (out) {
                Packet.writeType(out, PacketType.CHALLENGE_RESULT);
                Packet.writeBoolean(out, success);
                Packet.writeString(out, message);
                Packet.flush(out);
            }
        } catch (IOException e) { running = false; }
    }

    // ============================================================
    // NGẮT KẾT NỐI
    // ============================================================
    private void onDisconnect() {
        running = false;
        if (playerInfo != null) {
            ServerMain.onlinePlayers.remove(playerInfo.username, this);
        }
        System.out.println("[Handler] Client ngắt kết nối: "
                + (playerInfo != null ? playerInfo.username : socket.getRemoteSocketAddress()));
        lobbyManager.removeFromQueue(this);
        if (currentRoom != null) {
            currentRoom.handleDisconnect(this);
        }
        if (challengeTimer != null) {
            challengeTimer.cancel();
            challengeTimer = null;
        }
        if (currentChallenger != null) {
            ClientHandler c = getOnlinePlayer(currentChallenger);
            if (c != null) {
                c.sendChallengeResult(false, (playerInfo != null ? playerInfo.username : "Đối thủ") + " đã ngắt kết nối.");
            }
            currentChallenger = null;
        }
        try { socket.close(); } catch (IOException ignored) {}
    }

    // ============================================================
    // PHƯƠNG THỨC GỬI GÓI TIN (dùng bởi GameRoom)
    // ============================================================

    /** Gửi thông báo trận bắt đầu kèm avatar và màu gậy của đối thủ */
    public void sendMatchFound(String opponentName, int opponentLevel, int opponentAvatarId, String opponentCueColor) {
        try {
            synchronized (out) {
                Packet.writeType(out, PacketType.MATCH_FOUND);
                Packet.writeString(out, opponentName);
                Packet.writeInt(out, opponentLevel);
                Packet.writeInt(out, opponentAvatarId);
                Packet.writeString(out, opponentCueColor != null ? opponentCueColor : "#DEB887");
                Packet.flush(out);
            }
        } catch (IOException e) { running = false; }
    }

    /** Gửi lệnh bắt đầu game (playerIndex: 0 hoặc 1, isFirstTurn: người đánh trước, matchSeed: hạt giống ngẫu nhiên đồng bộ cả 2 bên) */
    public void sendGameStart(int playerIndex, boolean isFirstTurn, long matchSeed) {
        try {
            synchronized (out) {
                Packet.writeType(out, PacketType.GAME_START);
                Packet.writeInt(out, playerIndex);
                Packet.writeBoolean(out, isFirstTurn);
                Packet.writeLong(out, matchSeed);
                Packet.flush(out);
            }
        } catch (IOException e) { running = false; }
    }

    public void sendGameStart(int playerIndex, boolean isFirstTurn) {
        sendGameStart(playerIndex, isFirstTurn, System.nanoTime());
    }

    /** Broadcast cú đánh tới client này */
    public void sendGameState(double angle, double force, double cueX, double cueY, double spinX, double spinY) {
        try {
            synchronized (out) {
                Packet.writeType(out, PacketType.GAME_STATE);
                Packet.writeDouble(out, angle);
                Packet.writeDouble(out, force);
                Packet.writeDouble(out, cueX);
                Packet.writeDouble(out, cueY);
                Packet.writeDouble(out, spinX);
                Packet.writeDouble(out, spinY);
                Packet.flush(out);
            }
        } catch (IOException e) { running = false; }
    }

    /** Chuyển tiếp tọa độ đặt bi cái tới đối thủ */
    public void sendCueBallPlaced(double cueX, double cueY) {
        try {
            synchronized (out) {
                Packet.writeType(out, PacketType.CUE_BALL_PLACED);
                Packet.writeDouble(out, cueX);
                Packet.writeDouble(out, cueY);
                Packet.flush(out);
            }
        } catch (IOException e) { running = false; }
    }

    /** Chuyển tiếp trạng thái ngắm cơ và kéo lực tới đối thủ */
    public void sendCueAim(boolean isAiming, double angle, double forceRatio, double spinX, double spinY) {
        try {
            synchronized (out) {
                Packet.writeType(out, PacketType.CUE_AIM);
                Packet.writeBoolean(out, isAiming);
                Packet.writeDouble(out, angle);
                Packet.writeDouble(out, forceRatio);
                Packet.writeDouble(out, spinX);
                Packet.writeDouble(out, spinY);
                Packet.flush(out);
            }
        } catch (IOException e) { running = false; }
    }

    /** Thông báo thay đổi lượt kèm cờ ball-in-hand */
    public void sendTurnChange(boolean isYourTurn, boolean isBallInHand, String reason) {
        try {
            synchronized (out) {
                Packet.writeType(out, PacketType.TURN_CHANGE);
                Packet.writeBoolean(out, isYourTurn);
                Packet.writeBoolean(out, isBallInHand);
                Packet.writeString(out, reason);
                Packet.flush(out);
            }
        } catch (IOException e) { running = false; }
    }

    /** Thông báo bi rơi vào lỗ */
    public void sendBallPocketed(int ballIndex) {
        try {
            synchronized (out) {
                Packet.writeType(out, PacketType.BALL_POCKETED);
                Packet.writeInt(out, ballIndex);
                Packet.flush(out);
            }
        } catch (IOException e) { running = false; }
    }

    /** Thông báo kết thúc game */
    public void sendGameOver(boolean youWin, int expGained) {
        try {
            synchronized (out) {
                Packet.writeType(out, PacketType.GAME_OVER);
                Packet.writeBoolean(out, youWin);
                Packet.writeInt(out, expGained);
                Packet.flush(out);
            }
        } catch (IOException e) { running = false; }
    }

    // ============================================================
    // GETTER
    // ============================================================
    public DatabaseManager.PlayerInfo getPlayerInfo() { return playerInfo; }
    public boolean isLoggedIn() { return isLoggedIn; }
    public void setCurrentRoom(GameRoom room) { this.currentRoom = room; }
    public GameRoom getCurrentRoom() { return currentRoom; }

    public void refreshPlayerInfo(DatabaseManager db) {
        if (playerInfo != null) {
            DatabaseManager.PlayerInfo updated = db.getPlayerByUsername(playerInfo.username);
            if (updated != null) {
                this.playerInfo = updated;
            }
        }
    }
}
